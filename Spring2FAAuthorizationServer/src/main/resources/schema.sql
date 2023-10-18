-- Users
create table if not exists users
(
	username varchar(200) not null primary key,
	password varchar(500) not null,
	enabled boolean not null
);

create table if not exists usersinfo
(
	username varchar(200) not null primary key,
	isAccountNonExpired boolean not null,
	isAccountNonLocked boolean not null,
	isCredentialsNonExpired boolean not null,
	securityQuestion varchar(200) not null,
	securityAnswer varchar(200) not null,
	mfaSecret varchar(200) not null,
	mfaKeyId varchar(200) not null,
	mfaEnabled boolean not null,
	mfaRegistered boolean not null,
	securityQuestionEnabled boolean not null,
	constraint fk_usersinfo_users foreign key (username) references users (username)
);

create table if not exists authorities
(
	username varchar(200) not null,
	authority varchar(50) not null,
	constraint fk_authorities_users foreign key (username) references users (username),
	constraint username_authority UNIQUE (username, authority)
);
