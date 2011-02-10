--
-- Table struture for mylutece_database_user
--
DROP TABLE IF EXISTS mylutece_database_openid_user;
CREATE TABLE mylutece_database_openid_user (
	mylutece_database_openid_user_id int NOT NULL,
	login varchar(100) default '' NOT NULL,
	password varchar(100) default '' NOT NULL,
	name_given varchar(100) default '' NOT NULL,
	name_family varchar(100) default '' NOT NULL,
	email varchar(100) default NULL,
        authen_type varchar(100) default NULL,
	PRIMARY KEY (mylutece_database_openid_user_id)
);

--
-- Table struture for mylutece_database_openid_user_role
--
DROP TABLE IF EXISTS mylutece_database_openid_user_role;
CREATE TABLE mylutece_database_openid_user_role (
	mylutece_database_openid_user_id int default 0 NOT NULL,
	role_key varchar(50) default '' NOT NULL,
	PRIMARY KEY (mylutece_database_openid_user_id,role_key)
);

--
-- Table struture for mylutece_database_openid_user_group
--
DROP TABLE IF EXISTS mylutece_database_openid_user_group;
CREATE TABLE mylutece_database_openid_user_group (
	mylutece_database_openid_user_id int default 0 NOT NULL,
	group_key varchar(100) default '' NOT NULL,
	PRIMARY KEY (mylutece_database_openid_user_id,group_key)
);

--
-- Table structure for table `mylutece_database_openid_recovery_user`
--

CREATE TABLE mylutece_database_openid_recovery_user (
  mylutece_database_openid_user_id int(11) default NULL,
  id_recovery_operation varchar(255) collate utf8_unicode_ci NOT NULL,
  PRIMARY KEY  (id_recovery_operation)
);

--
-- Table structure for table `mylutece_database_openid_recovery`
--

CREATE TABLE mylutece_database_openid_recovery (
  id_recovery_operation varchar(255) collate utf8_unicode_ci NOT NULL default '',
  date_recovery_creation datetime default NULL,
  date_recovery_expiration datetime default NULL,
  operation_recovery_accomplished tinyint(1) default NULL,
  PRIMARY KEY  (id_recovery_operation)
);
