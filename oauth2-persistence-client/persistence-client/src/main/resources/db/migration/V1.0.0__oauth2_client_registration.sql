CREATE TABLE `oauth2_registered_client`
(
    `registration_id`                 varchar(100)  NOT NULL,
    `client_id`                       varchar(100)  NOT NULL,
    `client_secret`                   varchar(200)  DEFAULT NULL,
    `client_authentication_method`    varchar(100)  NOT NULL,
    `authorization_grant_type`        varchar(100)  NOT NULL,
    `client_name`                     varchar(200)  DEFAULT NULL,
    `redirect_uri`                    varchar(1000) NOT NULL,
    `scopes`                          varchar(1000) NOT NULL,
    `authorization_uri`               varchar(1000) DEFAULT NULL,
    `token_uri`                       varchar(1000) NOT NULL,
    `jwk_set_uri`                     varchar(1000) DEFAULT NULL,
    `issuer_uri`                      varchar(1000) DEFAULT NULL,
    `user_info_uri`                   varchar(1000) DEFAULT NULL,
    `user_info_authentication_method` varchar(100)  DEFAULT NULL,
    `user_name_attribute_name`        varchar(100)  DEFAULT NULL,
    `configuration_metadata`          varchar(2000) DEFAULT NULL,
    PRIMARY KEY (`registration_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;