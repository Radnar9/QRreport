/*
 * Person functionalities
 */

/*
 * Auxiliary function to return the person item representation
 */
CREATE OR REPLACE FUNCTION person_item_representation(p_id UUID, name TEXT, phone TEXT, email TEXT)
RETURNS JSON
AS
$$
BEGIN
    RETURN json_build_object('id', p_id, 'name', name, 'phone', phone, 'email', email);
END$$ LANGUAGE plpgsql;

/*
 * Auxiliary function to return the person item representation
 */
CREATE OR REPLACE FUNCTION person_item_representation_temp(p_id UUID, name TEXT, phone TEXT, email TEXT, state TEXT)
RETURNS JSON
AS
$$
BEGIN
    RETURN json_build_object('id', p_id, 'name', name, 'phone', phone, 'email', email, 'state', state);
END$$ LANGUAGE plpgsql;

/**
  * Auxiliary function to verify if a specific person already exists.
  * Returns the person id (UUID) if the person exists and null otherwise.
  */
CREATE OR REPLACE FUNCTION person_exists(person_email TEXT)
RETURNS UUID
AS
$$
DECLARE
    person_id UUID;
BEGIN
    SELECT id INTO person_id FROM PERSON WHERE email = person_email;
    IF (person_id IS NULL) THEN
        RETURN NULL;
    END IF;
    RETURN person_id;
END$$ LANGUAGE plpgsql;

/**
  * Creates a new person and defines her role
  */
DROP PROCEDURE create_person(person_rep JSON, person_role SMALLINT,
person_name TEXT, person_email TEXT, person_password TEXT, person_phone TEXT);
CREATE OR REPLACE PROCEDURE create_person(
    person_rep OUT JSON,
    person_role SMALLINT,
    person_name TEXT,
    person_email TEXT,
    person_password TEXT,
    person_phone TEXT DEFAULT NULL
)AS
$$
DECLARE
    person_id UUID;
BEGIN
    INSERT INTO PERSON(name, email, password, phone)
    VALUES(person_name, person_email, person_password, person_phone) RETURNING id INTO person_id;

    INSERT INTO PERSON_ROLE(person, role) VALUES(person_id, person_role);

    person_rep = person_item_representation(person_id, person_name, person_phone, person_email);
END$$LANGUAGE plpgsql;

/**
  * Returns a representation with all the persons
  */
CREATE OR REPLACE FUNCTION get_persons()
RETURNS JSON
AS
$$
DECLARE
    rec RECORD;
    persons JSON[];
    collection_size INT = 0;
BEGIN
    FOR rec IN
        SELECT id, name, phone, email, state
        FROM PERSON
--         LIMIT limit_rows OFFSET skip_rows
    LOOP
        persons = array_append(
            persons,
            person_item_representation_temp(rec.id, rec.name, rec.phone, rec.email, rec.state)
        );
        collection_size = collection_size + 1;
    END LOOP;

    RETURN json_build_object('persons', persons, 'personsCollectionSize', collection_size);

END$$LANGUAGE plpgsql;