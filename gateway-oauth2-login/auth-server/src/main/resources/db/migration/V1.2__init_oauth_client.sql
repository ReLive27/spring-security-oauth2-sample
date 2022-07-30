CREATE TABLE `oauth2_client_registered`
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
);

CREATE TABLE oauth2_authorized_client
(
    client_registration_id  varchar(100)                            NOT NULL,
    principal_name          varchar(200)                            NOT NULL,
    access_token_type       varchar(100)                            NOT NULL,
    access_token_value      blob                                    NOT NULL,
    access_token_issued_at  timestamp                               NOT NULL,
    access_token_expires_at timestamp                               NOT NULL,
    access_token_scopes     varchar(1000) DEFAULT NULL,
    refresh_token_value     blob          DEFAULT NULL,
    refresh_token_issued_at timestamp     DEFAULT NULL,
    created_at              timestamp     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    PRIMARY KEY (client_registration_id, principal_name)
);
