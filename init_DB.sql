/*
PostgreSQL 11.2
*/

ALTER TABLE "file_connections"
    DROP CONSTRAINT IF EXISTS "file_connections_fk0";
ALTER TABLE "file_connections"
    DROP CONSTRAINT IF EXISTS "file_connections_fk1";
ALTER TABLE "messages"
    DROP CONSTRAINT IF EXISTS "messages_fk0";
ALTER TABLE "tokens"
    DROP CONSTRAINT IF EXISTS "tokens_fk0";
DROP TABLE IF EXISTS "profiles";
DROP TABLE IF EXISTS "files";
DROP TABLE IF EXISTS "file_connections";
DROP TABLE IF EXISTS "messages";
DROP TABLE IF EXISTS "tokens";


CREATE TABLE "profiles"
(
    "id"        serial       NOT NULL,
    "nickname"  VARCHAR(255) NOT NULL UNIQUE,
    "signature" VARCHAR(255),
    "password"  VARCHAR(300) NOT NULL,
    "isAdmin"   BOOLEAN      NOT NULL,
    CONSTRAINT profiles_pk PRIMARY KEY ("id")
) WITH (
      OIDS= FALSE
    );

CREATE TABLE "files"
(
    "id"            serial       NOT NULL,
    "original_name" VARCHAR(255) NOT NULL,
    "name"          VARCHAR(255) NOT NULL,
    "filesize"      integer      NOT NULL,
    "description"   VARCHAR(255) NOT NULL,
    CONSTRAINT files_pk PRIMARY KEY ("id")
) WITH (
      OIDS= FALSE
    );

CREATE TABLE "file_connections"
(
    "user_id" integer NOT NULL,
    "file_id" integer NOT NULL,
    CONSTRAINT file_connections_pk PRIMARY KEY ("user_id", "file_id")
) WITH (
      OIDS= FALSE
    );

CREATE TABLE "messages"
(
    "id"       serial       NOT NULL,
    "user_id"  integer,
    "nickname" VARCHAR(255) NOT NULL,
    "message"  VARCHAR(255) NOT NULL,
    CONSTRAINT messages_pk PRIMARY KEY ("id")
) WITH (
      OIDS= FALSE
    );

CREATE TABLE "tokens"
(
    "user_id"     integer      NOT NULL,
    "token"       VARCHAR(255) NOT NULL,
    "last_active" timestamp    NOT NULL,
    CONSTRAINT tokens_pk PRIMARY KEY ("user_id", "token")
) WITH (
      OIDS= FALSE
    );

ALTER TABLE "file_connections"
    ADD CONSTRAINT "file_connections_fk0" FOREIGN KEY ("user_id") REFERENCES "profiles" ("id") ON DELETE CASCADE;
ALTER TABLE "file_connections"
    ADD CONSTRAINT "file_connections_fk1" FOREIGN KEY ("file_id") REFERENCES "files" ("id") ON DELETE CASCADE;

ALTER TABLE "messages"
    ADD CONSTRAINT "messages_fk0" FOREIGN KEY ("user_id") REFERENCES "profiles" ("id") ON DELETE SET NULL;

ALTER TABLE "tokens"
    ADD CONSTRAINT "tokens_fk0" FOREIGN KEY ("user_id") REFERENCES "profiles" ("id") ON DELETE CASCADE;

CREATE OR REPLACE FUNCTION add_file(userid integer, original varchar, filename varchar, file_size integer,
                                    description varchar) RETURNS integer AS
$$
DECLARE
    cur_files  integer;
    total_size integer;
    fileid     integer;
BEGIN
    SELECT COUNT(*), SUM(files.filesize) INTO cur_files,total_size
    FROM files
             JOIN file_connections ON files.id = file_connections.file_id
    WHERE file_connections.user_id = userid;
    IF cur_files = 5 THEN
        RETURN 1;
    END IF;
    IF total_size + file_size >= 1024 * 1024 * 1024 THEN
        RETURN 2;
    END IF;
    INSERT INTO files (original_name, name, filesize, description)
    VALUES (original, filename, file_size, description) RETURNING id INTO fileid;
    INSERT INTO file_connections (user_id, file_id) VALUES (userid, fileid);
    RETURN 0;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION add_message(user_id integer, message varchar) RETURNS integer AS
$$
DECLARE
    poster_nickname varchar;
    message_id      integer;
BEGIN
    SELECT nickname INTO poster_nickname FROM profiles WHERE user_id = id;
    INSERT INTO messages (user_id, nickname, message)
    VALUES (user_id, poster_nickname, message) RETURNING id INTO message_id;
    RETURN message_id;
END;
$$ LANGUAGE plpgsql;