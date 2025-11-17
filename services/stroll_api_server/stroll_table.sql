CREATE DATABASE stroll;
USE stroll;

DROP TABLE IF EXISTS user;
CREATE TABLE user (
	id CHAR(20) PRIMARY KEY,
	password CHAR(20) NOT NULL,
	nickname CHAR(20) NOT NULL UNIQUE,
	email CHAR(50) NOT NULL,
	pet_type CHAR(20)
);

DROP TABLE IF EXISTS place;
CREATE TABLE place (
	place_no INT PRIMARY KEY AUTO_INCREMENT,
	title VARCHAR(100),
	content TEXT(500),
	category CHAR(20),
	written_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	gu_address CHAR(80) NOT NULL,
	after_gu_address CHAR(80) NOT NULL,
	detail_address VARCHAR(80),
	x DOUBLE NOT NULL,
	y DOUBLE NOT NULL,
	user_id CHAR(20) NOT NULL,
    pet_type CHAR(20),
	CONSTRAINT fk_place_user_id FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
	UNIQUE KEY uq_title_gu_aftergu_detail (title, gu_address, after_gu_address, detail_address)
);
CREATE INDEX ix_place_user_id ON place(user_id);
CREATE INDEX ix_place_gu_address ON place(gu_address);

DROP TABLE IF EXISTS image;
CREATE TABLE image (
	image_no INT PRIMARY KEY AUTO_INCREMENT,
	place_no INT,
	image_path VARCHAR(200),
	CONSTRAINT fk_image_place_no FOREIGN KEY (place_no) REFERENCES place(place_no) ON UPDATE CASCADE
);

DROP TABLE IF EXISTS reply;
CREATE TABLE reply (
	reply_no INT PRIMARY KEY AUTO_INCREMENT,
	user_id CHAR(20) NOT NULL,
	place_no INT NOT NULL,
	content TEXT(500),
	star TINYINT,
	written_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
	CONSTRAINT fk_reply_user_id FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
	CONSTRAINT fk_reply_place_no FOREIGN KEY (place_no) REFERENCES place(place_no) ON DELETE CASCADE
);
CREATE INDEX ix_reply_place_no ON reply(place_no);

DROP TABLE IF EXISTS wish;
CREATE TABLE wish (
	wish_no INT PRIMARY KEY AUTO_INCREMENT,
	user_id CHAR(20) NOT NULL,
	place_no INT NOT NULL,
	CONSTRAINT fk_wish_user_id FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
	CONSTRAINT fk_wish_place_no FOREIGN KEY (place_no) REFERENCES place(place_no) ON DELETE CASCADE,
	UNIQUE KEY uq_user_place (user_id, place_no)
);
CREATE INDEX ix_wish_user_id ON wish(user_id);
