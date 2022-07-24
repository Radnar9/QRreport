BEGIN;
    DELETE FROM ROOM_DEVICE;
    DELETE FROM ANOMALY;
    DELETE FROM PERSON_COMPANY;
    DELETE FROM COMMENT;
    DELETE FROM RATE;
    DELETE FROM FIXING_BY;
    DELETE FROM TICKET;
    DELETE FROM DEVICE;
    DELETE FROM EMPLOYEE_STATE_TRANS;
    DELETE FROM EMPLOYEE_STATE;
    DELETE FROM USER_STATE;
    DELETE FROM PERSON_ROLE;
    DELETE FROM PERSON_SKILL;
    DELETE FROM CATEGORY;
    DELETE FROM ROOM;
    DELETE FROM BUILDING;
    DELETE FROM PERSON;
    DELETE FROM COMPANY;
    DELETE FROM ROLE;

    ALTER SEQUENCE building_id_seq RESTART;
    ALTER SEQUENCE category_id_seq RESTART;
    ALTER SEQUENCE company_id_seq RESTART;
    ALTER SEQUENCE device_id_seq RESTART;
    ALTER SEQUENCE employee_state_id_seq RESTART;
    ALTER SEQUENCE role_id_seq RESTART;
    ALTER SEQUENCE room_id_seq RESTART;
    ALTER SEQUENCE ticket_id_seq RESTART;
    ALTER SEQUENCE user_state_id_seq RESTART;
    ALTER SEQUENCE anomaly_id_seq RESTART;
    ALTER SEQUENCE comment_id_seq RESTART;
COMMIT;