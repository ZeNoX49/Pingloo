DROP DATABASE IF EXISTS musicode_td21_2;
CREATE DATABASE musicode_td21_2;
USE musicode_td21_2;

CREATE TABLE music (
	mus_id INT (5),
	mus_title VARCHAR (255),
	mus_author VARCHAR (255),
	mus_album VARCHAR (255),
	mus_duration VARCHAR (10)
);

CREATE TABLE user (
	user_id INT (5),
	user_name VARCHAR (255),
	user_mail VARCHAR (255),
	user_password VARCHAR (255)
);

CREATE TABLE library (
	user_id INT (5),
	mus_id INT (5),
	lib_grade INT (5),
	CONSTRAINT lib_user FOREIGN KEY(user_id) REFERENCES user(user_id),
	CONSTRAINT lib_mus FOREIGN KEY(mus_id) REFERENCES music(mus_id)
);

