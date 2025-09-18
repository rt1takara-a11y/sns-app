ALTER TABLE users
    ADD COLUMN avatar_url varchar(1024),
    ADD COLUMN birthdate date,
    ADD COLUMN bio text;
