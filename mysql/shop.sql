DROP DATABASE IF EXISTS `shop`;
CREATE DATABASE IF NOT EXISTS `shop`;

USE `shop`;

DROP TABLE IF EXISTS `products`;

CREATE TABLE IF NOT EXISTS `products`(

	id 		TINYINT PRIMARY KEY AUTO_INCREMENT 	NOT NULL,
	title 	VARCHAR(50) 						NOT NULL DEFAULT "no_title",
	url 	TEXT								NOT NULL,
	price 	VARCHAR(50) 						NOT NULL DEFAULT "no_nickname"

);

INSERT INTO `products` (
	title,
	url,
	price
) VALUES 	("Assiettes"	, "https://tinyurl.com/ycma3trs"	, 6 ),
			("Verre"		, "https://tinyurl.com/yal7wuvx"	, 3.55 ),
			("Tasses"		, "https://tinyurl.com/y7qrj4jt"	, 3 ),
			("Bols"			, "https://tinyurl.com/y8wj6oo2"	, 6 ),
			("Ramequin"		, "https://tinyurl.com/ycerpmr4"	, 1 );
