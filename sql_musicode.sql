CREATE DATABASE IF NOT EXISTS musicode_td21_2;
USE musicode_td21_2;

DROP TABLE IF EXISTS library;
DROP TABLE IF EXISTS music;
DROP TABLE IF EXISTS user;

CREATE TABLE user (
  user_id INT(5) NOT NULL AUTO_INCREMENT,
  user_name VARCHAR(255) CHARACTER SET utf8mb4 DEFAULT NULL,
  user_mail VARCHAR(255) CHARACTER SET utf8mb4 DEFAULT NULL,
  user_password VARCHAR(255) CHARACTER SET utf8mb4 DEFAULT NULL,
  PRIMARY KEY (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=1;

CREATE TABLE music (
  mus_id INT(5) NOT NULL AUTO_INCREMENT,
  mus_title VARCHAR(255) CHARACTER SET utf8mb4 DEFAULT NULL,
  mus_author VARCHAR(255) CHARACTER SET utf8mb4 DEFAULT NULL,
  mus_album VARCHAR(255) CHARACTER SET utf8mb4 DEFAULT NULL,
  mus_duration VARCHAR(10) CHARACTER SET utf8mb4 DEFAULT NULL,
  PRIMARY KEY (mus_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=1;

CREATE TABLE library (
  user_id INT(5) NOT NULL,
  mus_id INT(5) NOT NULL,
  lib_grade INT(5) DEFAULT 0 CHECK (lib_grade BETWEEN 0 AND 5),
  CONSTRAINT lib_user FOREIGN KEY (user_id) REFERENCES user(user_id),
  CONSTRAINT lib_mus FOREIGN KEY (mus_id) REFERENCES music(mus_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO user (user_name, user_mail, user_password) VALUES
('TomJ', 'TomJ@example.com', 'tom123'),
('ValH', 'ValH@example.com', 'val123'),
('LucasG', 'LucasG@example.com', 'lucas123'),
('ThomasM', 'ThomasM@example.com', 'thomas123');

INSERT INTO music (mus_title, mus_author, mus_album, mus_duration) VALUES
('Imagine', 'John Lennon', 'Imagine', '3:04'),
('Bohemian Rhapsody', 'Queen', 'A Night at the Opera', '5:55'),
('Billie Jean', 'Michael Jackson', 'Thriller', '4:54'),
('Smells Like Teen Spirit', 'Nirvana', 'Nevermind', '5:01');

INSERT INTO library (user_id, mus_id, lib_grade) VALUES
(1, 1, 5),
(1, 2, 4),
(2, 3, 5),
(3, 4, 2);