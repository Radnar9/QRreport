/*
 * Company functionalities
 */

/*
 * Auxiliary function to return the company item representation
 */
CREATE OR REPLACE FUNCTION company_item_representation(id BIGINT, name TEXT, state TEXT, tmstamp TIMESTAMP)
RETURNS JSON
AS
$$
BEGIN
    RETURN json_build_object('id', id, 'name', name, 'state', state, 'timestamp', tmstamp);
END$$ LANGUAGE plpgsql;

/**
  * Auxiliary function to check if two persons are from the same company
  */
CREATE OR REPLACE FUNCTION from_same_company(person1 UUID, person2 UUID, param_company BIGINT)
RETURNS BOOL
AS
$$
BEGIN
    RETURN (EXISTS(SELECT company FROM PERSON_COMPANY WHERE person = person1 AND company IN (
            SELECT company FROM PERSON_COMPANY WHERE person = person2 AND company = param_company)
        )
    );
END$$LANGUAGE plpgsql;

/**
  * Auxiliary function to obtain in an array the companies names of an employee/manager
  */
CREATE OR REPLACE FUNCTION get_person_companies(person_id UUID)
RETURNS TEXT[]
AS
$$
DECLARE arr TEXT[];
BEGIN
    SELECT ARRAY(SELECT name
            FROM PERSON_COMPANY p INNER JOIN COMPANY ON (id = company)
            WHERE person = person_id AND p.state = 'active'
            ORDER BY name) INTO arr;
    RETURN (CASE WHEN array_length(arr, 1) IS NULL THEN NULL ELSE arr END);
END$$LANGUAGE plpgsql;

/**
  * Auxiliary function to obtain in an array the companies (id & name) of an employee/manager
  */
CREATE OR REPLACE FUNCTION get_person_companies_with_id(person_id UUID)
RETURNS JSON[]
AS
$$
DECLARE rec RECORD; companies JSON[];
BEGIN
    FOR rec IN
        SELECT id, name FROM COMPANY WHERE id IN
            (SELECT company FROM PERSON_COMPANY WHERE person = person_id)
    LOOP
        companies = array_append(
            companies,
            json_build_object('id', rec.id, 'name', rec.name)
        );
    END LOOP;
    RETURN companies;
END$$LANGUAGE plpgsql;

/*
 * Creates a new company
 * Returns the company item representation
 * Throws exception in case there is no row added or when the unique constraint is violated
 */
CREATE OR REPLACE PROCEDURE create_company(company_rep OUT JSON, cname TEXT)
AS
$$
DECLARE
    company_id BIGINT; cstate TEXT; tmstamp TIMESTAMP;
    prev_id BIGINT; current_id BIGINT; ex_constraint TEXT;
BEGIN
    SELECT last_value INTO prev_id FROM company_id_seq;

    INSERT INTO COMPANY(name) VALUES (cname) RETURNING id, name, state, timestamp INTO company_id, cname, cstate, tmstamp;
    company_rep = company_item_representation(company_id, cname, cstate, tmstamp);

EXCEPTION
    WHEN unique_violation THEN
        SELECT last_value INTO current_id FROM company_id_seq;
        IF (prev_id < current_id) THEN
            PERFORM setval('company_id_seq', current_id - 1);
        END IF;

        GET STACKED DIAGNOSTICS ex_constraint = CONSTRAINT_NAME;
        IF (ex_constraint = 'unique_company_name') THEN
            RAISE 'unique-constraint' USING DETAIL = 'company name', HINT = cname;
        END IF;
END$$ LANGUAGE plpgsql;

/*
 * Updates a company
 * Returns the updated company item representation
 * Throws exception when the company id does not exist, when company has the state set to inactive,
 * when all updatable parameters are null, when the unique constraint is violated or when there is no row updated
 */
CREATE OR REPLACE PROCEDURE update_company(company_rep OUT JSON, company_id BIGINT, new_name TEXT)
AS
$$
DECLARE
    cid BIGINT; current_name TEXT; cstate TEXT; tmstamp TIMESTAMP; ex_constraint TEXT;
BEGIN
    SELECT id, name, state, timestamp INTO cid, current_name, cstate, tmstamp FROM COMPANY WHERE id = company_id;
    IF (cid IS NULL) THEN
        RAISE 'resource-not-found' USING DETAIL = 'company', HINT = company_id;
    END IF;

    CASE
        WHEN (cstate = 'inactive') THEN
            RAISE 'inactive-resource';
        WHEN (new_name = current_name) THEN
            -- Does not update when the names are the same, returns the representation with the same values.
        ELSE
            UPDATE COMPANY SET name = new_name WHERE id = company_id
            RETURNING id, name, state, timestamp INTO company_id, current_name, cstate, tmstamp;
    END CASE;

    company_rep = company_item_representation(company_id, current_name, cstate, tmstamp);
EXCEPTION
    WHEN unique_violation THEN
        GET STACKED DIAGNOSTICS ex_constraint = CONSTRAINT_NAME;
        IF (ex_constraint = 'unique_company_name') THEN
            RAISE 'unique-constraint' USING DETAIL = 'company name', HINT = new_name;
        END IF;
END$$ LANGUAGE plpgsql;

/*
 * Gets all new possible companies to assign an employee or a manager
 */
CREATE OR REPLACE FUNCTION get_new_companies(user_id UUID, limit_rows INT DEFAULT NULL, skip_rows INT DEFAULT NULL)
RETURNS JSON
AS
$$
DECLARE
    rec RECORD;
    companies JSON[];
    collection_size INT = 0;
BEGIN
    FOR rec IN
        SELECT id, name, state, timestamp FROM COMPANY
        WHERE id NOT IN (SELECT company FROM PERSON_COMPANY WHERE person = user_id)
        LIMIT limit_rows OFFSET skip_rows
    LOOP
        companies = array_append(
            companies,
            company_item_representation(rec.id, rec.name, rec.state, rec.timestamp)
        );
    END LOOP;

    SELECT COUNT(id) INTO collection_size FROM COMPANY
        WHERE id NOT IN (SELECT company FROM PERSON_COMPANY WHERE person = user_id);

    RETURN json_build_object('companies', companies, 'companiesCollectionSize', collection_size);
END$$ LANGUAGE plpgsql;

/*
 * Gets all the companies where an employee or manager is active or inactive
 * Se for um manager a realizar o pedido, apenas vai retornar as companies que ambos os utilizadores têm em comum
 */
CREATE OR REPLACE FUNCTION get_user_companies(
    admin_manager_id UUID,
    is_manager BOOL,
    user_id UUID,
    rel_state TEXT DEFAULT 'active',
    limit_rows INT DEFAULT NULL,
    skip_rows INT DEFAULT NULL
)
RETURNS JSON
AS
$$
DECLARE
    rec RECORD;
    companies JSON[];
    collection_size INT = 0;
BEGIN
    FOR rec IN
        SELECT id, name, state, timestamp
            FROM COMPANY WHERE
                CASE WHEN (is_manager) THEN
                    id IN (SELECT company FROM PERSON_COMPANY pc WHERE person = user_id AND
                    state = rel_state AND
                    company IN (SELECT company FROM PERSON_COMPANY WHERE person = admin_manager_id))
                ELSE
                    id IN (SELECT company FROM PERSON_COMPANY pc WHERE person = user_id AND
                    state = rel_state)
                END
            LIMIT limit_rows OFFSET skip_rows
    LOOP
        companies = array_append(
            companies,
            company_item_representation(rec.id, rec.name, rec.state, rec.timestamp)
        );
    END LOOP;

    SELECT COUNT(id) INTO collection_size FROM COMPANY WHERE
                CASE WHEN (is_manager) THEN
                    id IN (SELECT company FROM PERSON_COMPANY pc WHERE person = user_id AND
                    state = rel_state AND
                    company IN (SELECT company FROM PERSON_COMPANY WHERE person = admin_manager_id))
                ELSE
                    id IN (SELECT company FROM PERSON_COMPANY pc WHERE person = user_id AND
                    state = rel_state)
                END;
    RETURN json_build_object('companies', companies, 'companiesCollectionSize', collection_size);
END$$ LANGUAGE plpgsql;

/*
 * Gets all the companies or in case of manager or employee, gets only the companies that he belongs
 * Returns a list with all the company item representation
 */
CREATE OR REPLACE FUNCTION get_companies(person_id UUID, limit_rows INT DEFAULT NULL, skip_rows INT DEFAULT NULL)
RETURNS JSON
AS
$$
DECLARE
    rec RECORD;
    companies JSON[];
    collection_size INT = 0;
BEGIN
    FOR rec IN
        SELECT id, name, state, timestamp
            FROM COMPANY WHERE
                CASE WHEN (person_id IS NOT NULL) THEN
                    id IN (SELECT company FROM PERSON_COMPANY WHERE person = person_id AND state = 'active')
                ELSE TRUE END
            ORDER BY id
            LIMIT limit_rows OFFSET skip_rows

    LOOP
        companies = array_append(
            companies,
            company_item_representation(rec.id, rec.name, rec.state, rec.timestamp)
        );
    END LOOP;

    SELECT COUNT(id) INTO collection_size FROM COMPANY WHERE
        CASE WHEN (person_id IS NOT NULL) THEN
            id IN (SELECT company FROM PERSON_COMPANY WHERE person = person_id)
        ELSE TRUE END;
    RETURN json_build_object('companies', companies, 'companiesCollectionSize', collection_size);
END$$ LANGUAGE plpgsql;

/*
 * Gets a specific company
 * Returns the company representation
 * Throws exception when the company id does not exist
 */
CREATE OR REPLACE FUNCTION get_company(company_id BIGINT, limit_rows INT DEFAULT NULL, skip_rows INT DEFAULT NULL)
RETURNS JSON
AS
$$
DECLARE
    buildings_rep JSON; test TEXT;
    cid BIGINT; cname TEXT; cstate TEXT; tmstamp TIMESTAMP;
BEGIN
    SELECT id, name, state, timestamp INTO cid, cname, cstate, tmstamp FROM COMPANY WHERE id = company_id;
    IF (cid IS NULL) THEN
        RAISE 'resource-not-found' USING DETAIL = 'company', HINT = company_id;
    END IF;

    RETURN json_build_object(
        'id', company_id, 'name', cname, 'state', cstate, 'timestamp', tmstamp,
        'buildings', get_buildings(company_id, limit_rows, skip_rows)
    );
END$$ LANGUAGE plpgsql;

/*
 * Deactivates a specific company, this is, sets its state to Inactive
 * Returns the company item representation
 * Throws exception when the company id does not exist
 */
CREATE OR REPLACE PROCEDURE deactivate_company(company_rep OUT JSON, company_id BIGINT)
AS
$$
DECLARE
    cid BIGINT; cname TEXT; cstate TEXT; tmstamp TIMESTAMP;
BEGIN
    SELECT id, name, state, timestamp INTO cid, cname, cstate, tmstamp FROM COMPANY WHERE id = company_id;
    CASE
        WHEN (cid IS NULL) THEN
            RAISE 'resource-not-found' USING DETAIL = 'company', HINT = company_id;
        WHEN (cstate = 'active') THEN
            UPDATE COMPANY SET state = 'inactive', timestamp = CURRENT_TIMESTAMP WHERE id = company_id
            RETURNING state, timestamp INTO cstate, tmstamp;
        ELSE
            -- Do nothing when it's already inactive
    END CASE;

    company_rep = company_item_representation(company_id, cname, cstate, tmstamp);
END$$ LANGUAGE plpgsql;

/*
 * Activates a specific company, this is, sets its state to Active
 * Returns the company item representation
 * Throws exception when the company id does not exist
 */
CREATE OR REPLACE PROCEDURE activate_company(company_rep OUT JSON, company_id BIGINT)
AS
$$
DECLARE
    cid BIGINT; cname TEXT; cstate TEXT; tmstamp TIMESTAMP;
BEGIN
    SELECT id, name, state, timestamp INTO cid, cname, cstate, tmstamp FROM COMPANY WHERE id = company_id;
    CASE
        WHEN (cid IS NULL) THEN
            RAISE 'resource-not-found' USING DETAIL = 'company', HINT = company_id;
        WHEN (cstate = 'inactive') THEN
            UPDATE COMPANY SET state = 'active', timestamp = CURRENT_TIMESTAMP WHERE id = company_id
            RETURNING state, timestamp INTO cstate, tmstamp;
        ELSE
            -- Do nothing when it's already active
    END CASE;

    company_rep = company_item_representation(company_id, cname, cstate, tmstamp);
END$$ LANGUAGE plpgsql;

/**
  Trigger to change the state of all rooms belonging to the building whose state was changed
 */
CREATE OR REPLACE FUNCTION update_buildings_states() RETURNS TRIGGER
AS
$$
BEGIN
	IF NEW.state != OLD.state THEN
        UPDATE BUILDING SET state = NEW.state, timestamp = CURRENT_TIMESTAMP WHERE company = NEW.id;
	END IF;
	RETURN NEW;
END$$LANGUAGE plpgsql;

CREATE TRIGGER change_building_state_trigger
    BEFORE UPDATE ON COMPANY
    FOR EACH ROW
    EXECUTE PROCEDURE update_buildings_states();