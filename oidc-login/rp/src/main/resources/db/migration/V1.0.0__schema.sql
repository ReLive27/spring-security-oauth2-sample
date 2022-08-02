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

CREATE TABLE `user`
(
    `id`       bigint       NOT NULL AUTO_INCREMENT,
    `username` varchar(100) NOT NULL,
    `password` varchar(200) NOT NULL,
    PRIMARY KEY (`id`)
);
CREATE TABLE `role`
(
    `id`        bigint       NOT NULL AUTO_INCREMENT,
    `role_code` varchar(100) NOT NULL,
    PRIMARY KEY (`id`)
);
CREATE TABLE `user_mtm_role`
(
    `id`      bigint NOT NULL AUTO_INCREMENT,
    `user_id` bigint NOT NULL,
    `role_id` bigint NOT NULL,
    PRIMARY KEY (`id`)
);


CREATE TABLE `oauth2_client_role`
(
    `id`                     bigint       NOT NULL AUTO_INCREMENT,
    `client_registration_id` varchar(100) NOT NULL,
    `role_code`              varchar(100) DEFAULT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `oauth2_client_role_mapping`
(
    `id`                   bigint NOT NULL AUTO_INCREMENT,
    `oauth_client_role_id` bigint NOT NULL,
    `role_id`              bigint NOT NULL,
    PRIMARY KEY (`id`)
);

INSERT INTO `oauth2_client_registered` (`registration_id`, `client_id`, `client_secret`, `client_authentication_method`,
                                        `authorization_grant_type`, `client_name`, `redirect_uri`, `scopes`,
                                        `authorization_uri`, `token_uri`, `jwk_set_uri`, `issuer_uri`, `user_info_uri`,
                                        `user_info_authentication_method`, `user_name_attribute_name`,
                                        `configuration_metadata`)
VALUES ('messaging-client-oidc', 'relive-client', 'relive-client', 'client_secret_basic', 'authorization_code',
        'ReLive27', '{baseUrl}/login/oauth2/code/{registrationId}', 'openid,profile,email',
        'http://127.0.0.1:8080/oauth2/authorize', 'http://127.0.0.1:8080/oauth2/token',
        'http://127.0.0.1:8080/oauth2/jwks', NULL, 'http://127.0.0.1:8080/userinfo', 'form', 'sub',
        '{\"@class\":\"java.util.Collections$UnmodifiableMap\"}');
INSERT INTO `role` (`id`, `role_code`)
VALUES (1, 'ROLE_SYSTEM');
INSERT INTO `role` (`id`, `role_code`)
VALUES (2, 'ROLE_OPERATION');
INSERT INTO `user` (`id`, `username`, `password`)
VALUES (1, 'admin', '{noop}password');
INSERT INTO `user_mtm_role` (`id`, `user_id`, `role_id`)
VALUES (1, 1, 1);
INSERT INTO `oauth2_client_role` (`id`, `client_registration_id`, `role_code`)
VALUES (1, 'messaging-client-oidc', 'ROLE_ADMIN');
INSERT INTO `oauth2_client_role_mapping` (`id`, `oauth_client_role_id`, `role_id`)
VALUES (1, 1, 2);
