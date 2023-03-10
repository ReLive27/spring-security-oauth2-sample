CREATE TABLE `user`
(
    `id`       bigint       NOT NULL AUTO_INCREMENT,
    `username` varchar(100) NOT NULL,
    `password` varchar(200) DEFAULT NULL,
    `phone`    varchar(11)  DEFAULT NULL,
    `email`    varchar(50)  DEFAULT NULL,
    PRIMARY KEY (`id`)
);
CREATE TABLE `role`
(
    `id`        bigint       NOT NULL AUTO_INCREMENT,
    `role_code` varchar(100) NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `permission`
(
    `id`              bigint       NOT NULL AUTO_INCREMENT,
    `permission_name` varchar(100) NOT NULL,
    `permission_code` varchar(100) DEFAULT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `oauth2_client_role`
(
    `id`                     bigint       NOT NULL AUTO_INCREMENT,
    `client_registration_id` varchar(100) NOT NULL,
    `role_code`              varchar(100) DEFAULT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `user_mtm_role`
(
    `id`      bigint NOT NULL AUTO_INCREMENT,
    `user_id` bigint NOT NULL,
    `role_id` bigint NOT NULL,
    PRIMARY KEY (`id`)
);


CREATE TABLE `oauth2_client_role_mapping`
(
    `id`                   bigint NOT NULL AUTO_INCREMENT,
    `oauth_client_role_id` bigint NOT NULL,
    `role_id`              bigint NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `role_mtm_permission`
(
    `id`            bigint NOT NULL AUTO_INCREMENT,
    `role_id`       bigint NOT NULL,
    `permission_id` bigint NOT NULL,
    PRIMARY KEY (`id`)
);

INSERT INTO `user` (`id`, `username`, `password`, `phone`, `email`) VALUES (1, 'admin', '{noop}password', '13523456789', '123456@163.com');
INSERT INTO `role` (`id`, `role_code`) VALUES (1, 'ROLE_ADMIN');
INSERT INTO `role` (`id`, `role_code`) VALUES (2, 'ROLE_OPERATION');
INSERT INTO `permission` (`id`, `permission_name`, `permission_code`) VALUES (1, 'read the article', 'read');
INSERT INTO `permission` (`id`, `permission_name`, `permission_code`) VALUES (2, 'write the article', 'write');
INSERT INTO `user_mtm_role` (`id`, `user_id`, `role_id`) VALUES (1, 1, 1);
INSERT INTO `role_mtm_permission` (`id`, `role_id`, `permission_id`) VALUES (1, 1, 1);
INSERT INTO `role_mtm_permission` (`id`, `role_id`, `permission_id`) VALUES (2, 1, 2);
INSERT INTO `role_mtm_permission` (`id`, `role_id`, `permission_id`) VALUES (3, 2, 1);
