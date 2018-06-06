DROP DATABASE IF EXISTS `pyrates`;
CREATE DATABASE IF NOT EXISTS `pyrates`;

USE `pyrates`;

DROP TABLE IF EXISTS `famous_pyrates`;

CREATE TABLE IF NOT EXISTS `famous_pyrates`(

	id 		TINYINT PRIMARY KEY AUTO_INCREMENT NOT NULL,
	name 	VARCHAR(50) UNIQUE NOT NULL DEFAULT "no_name",
	surname VARCHAR(50) NOT NULL DEFAULT "no_surname",
	nickname VARCHAR(50) NOT NULL DEFAULT "no_nickname",
	birth_place VARCHAR(50) NOT NULL DEFAULT 'no_city',
	death_place VARCHAR(50) NOT NULL DEFAULT 'no_city',
	birth_date DATE NOT NULL DEFAULT '0000-00-00',
	death_date DATE NOT NULL DEFAULT '0000-00-00',
	sex ENUM("F","M") NOT NULL DEFAULT "F"
);

CREATE TABLE IF NOT EXISTS `ships`(

	id 			TINYINT PRIMARY KEY AUTO_INCREMENT NOT NULL, 
	name 		VARCHAR(50) UNIQUE NOT NULL DEFAULT "no_name",
	owner_id 	TINYINT NOT NULL DEFAULT 0,
	quantity 	TINYINT NOT NULL DEFAULT 1
	
);

INSERT INTO `ships` (name, owner_id) VALUES ("Man'o war", 1 ) ON DUPLICATE KEY UPDATE quantity = quantity + 1;

INSERT INTO `ships` (name, owner_id) VALUES ("Man'o war", 1 ) ON DUPLICATE KEY UPDATE quantity = quantity + 1;

INSERT INTO `ships` (name, owner_id) VALUES ("Man'o war", 1 ) ON DUPLICATE KEY UPDATE quantity = quantity + 1;


INSERT INTO `famous_pyrates` (`name`	, `surname`	,`nickname`		,`birth_place`	,`death_place`	,`birth_date`	,	`death_date`, `sex` ) 
			
			VALUES 
			
			("Teach", "Edward"	,"Black Beard"	,"Bristol"		,"Ocracoke"		,"1680-01-01"	,	"1718-11-22", "A"),
			("Vane"	, "Charles"	,"?"			,"London"		,"Port Royal"	,"1680-01-01"	,	"1721-03-29", "M"),
			("Kidd"	, "William"	,"?"			,"Greenock"		,"Wapping"		,"1645-01-01"	,	"1701-05-23", "G");
			


ALTER TABLE `ships` ADD `is_unique_ship` ENUM("1","0") NOT NULL DEFAULT "0" AFTER `name`;
ALTER TABLE `famous_pyrates`MODIFY `name` VARCHAR(30) UNIQUE NOT NULL DEFAULT "no_name";

INSERT INTO `ships` (name, is_unique_ship, owner_id) VALUES ("Queen Anne's Revenge", "1", 1 );





