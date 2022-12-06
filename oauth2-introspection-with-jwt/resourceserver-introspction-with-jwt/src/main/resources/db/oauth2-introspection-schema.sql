DROP TABLE IF EXISTS `oauth2_introspection`;
CREATE TABLE `oauth2_introspection`
(
    `id`                varchar(100) NOT NULL,
    `client_id`         varchar(100) NOT NULL,
    `client_secret`     varchar(200)  DEFAULT NULL,
    `issuer_uri`        varchar(1000) DEFAULT NULL,
    `introspection_uri` varchar(1000) DEFAULT NULL,
    PRIMARY KEY (`id`)
);
