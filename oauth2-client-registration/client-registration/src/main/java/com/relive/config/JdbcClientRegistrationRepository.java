package com.relive.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.*;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthenticationMethod;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;

/**
 * JdbcClientRegistrationRepository 实现类，用于通过 JDBC 操作数据库存储和读取 OAuth2 客户端注册信息。
 * 该类实现了 ClientRegistrationRepository 接口，用于管理 OAuth2 客户端注册信息。
 * 它提供了插入、更新、查询等功能，适用于数据库存储客户端注册信息的场景。
 *
 * @author: ReLive27
 * @date: 2024/6/8 22:18
 */
public class JdbcClientRegistrationRepository implements ClientRegistrationRepository, Iterable<ClientRegistration> {

    // 数据库表格字段名称
    private static final String COLUMN_NAMES = "registration_id,client_id,client_secret,client_authentication_method,authorization_grant_type,client_name,redirect_uri,scopes,authorization_uri,token_uri,jwk_set_uri,issuer_uri,user_info_uri,user_info_authentication_method,user_name_attribute_name,configuration_metadata";
    // 数据库表格名称
    private static final String TABLE_NAME = "oauth2_client_registered";
    // 查询所有客户端注册信息的 SQL
    private static final String LOAD_CLIENT_REGISTERED_SQL = "SELECT " + COLUMN_NAMES + " FROM " + TABLE_NAME;
    // 查询指定条件的客户端注册信息的 SQL
    private static final String LOAD_CLIENT_REGISTERED_QUERY_SQL = LOAD_CLIENT_REGISTERED_SQL + " WHERE ";
    // 插入客户端注册信息的 SQL
    private static final String INSERT_CLIENT_REGISTERED_SQL = "INSERT INTO " + TABLE_NAME + "(" + COLUMN_NAMES + ") VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    // 更新客户端注册信息的 SQL
    private static final String UPDATE_CLIENT_REGISTERED_SQL = "UPDATE " + TABLE_NAME + " SET client_id = ?,client_secret = ?,client_authentication_method = ?,authorization_grant_type = ?,client_name = ?,redirect_uri = ?,scopes = ?,authorization_uri = ?,token_uri = ?,jwk_set_uri = ?,issuer_uri = ?,user_info_uri = ?,user_info_authentication_method = ?,user_name_attribute_name = ?,configuration_metadata = ? WHERE registration_id = ?";

    // JdbcOperations 用于执行 SQL 查询和更新操作
    private final JdbcOperations jdbcOperations;

    // 行映射器，用于将 SQL 结果集映射为 ClientRegistration 对象
    private RowMapper<ClientRegistration> clientRegistrationRowMapper;

    // 参数映射器，用于将 ClientRegistration 对象转换为 SQL 参数
    private Function<ClientRegistration, List<SqlParameterValue>> clientRegistrationListParametersMapper;

    /**
     * 构造方法，初始化 JdbcClientRegistrationRepository。
     *
     * @param jdbcOperations JdbcOperations 实例，用于执行数据库操作
     */
    public JdbcClientRegistrationRepository(JdbcOperations jdbcOperations) {
        Assert.notNull(jdbcOperations, "JdbcOperations can not be null");
        this.jdbcOperations = jdbcOperations;
        this.clientRegistrationRowMapper = new ClientRegistrationRowMapper();
        this.clientRegistrationListParametersMapper = new ClientRegistrationParametersMapper();
    }

    /**
     * 根据注册 ID 查找客户端注册信息。
     *
     * @param registrationId 注册 ID
     * @return 找到的 ClientRegistration 对象，若未找到则返回 null
     */
    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        Assert.hasText(registrationId, "registrationId cannot be empty");
        return this.findBy("registration_id = ?", registrationId);
    }

    /**
     * 根据指定条件查找客户端注册信息。
     *
     * @param filter SQL 查询条件
     * @param args   查询参数
     * @return 查找到的第一个 ClientRegistration 对象，若未找到则返回 null
     */
    private ClientRegistration findBy(String filter, Object... args) {
        List<ClientRegistration> result = this.jdbcOperations.query(LOAD_CLIENT_REGISTERED_QUERY_SQL + filter, this.clientRegistrationRowMapper, args);
        return !result.isEmpty() ? result.get(0) : null;
    }

    /**
     * 保存客户端注册信息，如果已存在则更新，否则插入新记录。
     *
     * @param clientRegistration 客户端注册信息
     */
    public void save(ClientRegistration clientRegistration) {
        Assert.notNull(clientRegistration, "clientRegistration cannot be null");
        ClientRegistration existingClientRegistration = this.findByRegistrationId(clientRegistration.getRegistrationId());
        if (existingClientRegistration != null) {
            this.updateRegisteredClient(clientRegistration);
        } else {
            this.insertClientRegistration(clientRegistration);
        }
    }

    /**
     * 更新现有的客户端注册信息。
     *
     * @param clientRegistration 客户端注册信息
     */
    private void updateRegisteredClient(ClientRegistration clientRegistration) {
        List<SqlParameterValue> parameterValues = new ArrayList(this.clientRegistrationListParametersMapper.apply(clientRegistration));
        SqlParameterValue id = parameterValues.remove(0);
        parameterValues.add(id);
        PreparedStatementSetter statementSetter = new ArgumentPreparedStatementSetter(parameterValues.toArray());
        this.jdbcOperations.update(UPDATE_CLIENT_REGISTERED_SQL, statementSetter);
    }

    /**
     * 插入新的客户端注册信息。
     *
     * @param clientRegistration 客户端注册信息
     */
    private void insertClientRegistration(ClientRegistration clientRegistration) {
        List<SqlParameterValue> parameterValues = this.clientRegistrationListParametersMapper.apply(clientRegistration);
        PreparedStatementSetter statementSetter = new ArgumentPreparedStatementSetter(parameterValues.toArray());
        this.jdbcOperations.update(INSERT_CLIENT_REGISTERED_SQL, statementSetter);
    }

    /**
     * 查找所有客户端注册信息。
     *
     * @return 所有的 ClientRegistration 对象的列表
     */
    public List<ClientRegistration> findAny() {
        List<ClientRegistration> result = this.jdbcOperations.query(LOAD_CLIENT_REGISTERED_SQL, this.clientRegistrationRowMapper);
        return result.isEmpty() ? Collections.emptyList() : result;
    }

    /**
     * 迭代器方法，遍历所有客户端注册信息。
     *
     * @return 客户端注册信息的迭代器
     */
    public Iterator<ClientRegistration> iterator() {
        return this.findAny().iterator();
    }

    /**
     * 客户端注册信息的 RowMapper 实现，用于将 SQL 结果集映射为 ClientRegistration 对象。
     */
    public static class ClientRegistrationRowMapper implements RowMapper<ClientRegistration> {
        private ObjectMapper objectMapper = new ObjectMapper();

        public ClientRegistrationRowMapper() {
            ClassLoader classLoader = JdbcClientRegistrationRepository.class.getClassLoader();
            List<Module> securityModules = SecurityJackson2Modules.getModules(classLoader);
            this.objectMapper.registerModules(securityModules);
        }

        /**
         * 将 SQL 结果集的一行映射为 ClientRegistration 对象。
         *
         * @param rs     SQL 结果集
         * @param rowNum 行号
         * @return 映射后的 ClientRegistration 对象
         * @throws SQLException 如果 SQL 执行出错
         */
        @Override
        public ClientRegistration mapRow(ResultSet rs, int rowNum) throws SQLException {
            Set<String> scopes = StringUtils.commaDelimitedListToSet(rs.getString("scopes"));
            ClientRegistration.Builder builder = ClientRegistration.withRegistrationId(rs.getString("registration_id"))
                    .clientId(rs.getString("client_id"))
                    .clientSecret(rs.getString("client_secret"))
                    .clientAuthenticationMethod(resolveClientAuthenticationMethod(rs.getString("client_authentication_method")))
                    .authorizationGrantType(resolveAuthorizationGrantType(rs.getString("authorization_grant_type")))
                    .clientName(rs.getString("client_name"))
                    .redirectUri(rs.getString("redirect_uri"))
                    .scope(scopes)
                    .authorizationUri(rs.getString("authorization_uri"))
                    .tokenUri(rs.getString("token_uri"))
                    .jwkSetUri(rs.getString("jwk_set_uri"))
                    .issuerUri(rs.getString("issuer_uri"))
                    .userInfoUri(rs.getString("user_info_uri"))
                    .userInfoAuthenticationMethod(resolveUserInfoAuthenticationMethod(rs.getString("user_info_authentication_method")))
                    .userNameAttributeName(rs.getString("user_name_attribute_name"));

            Map<String, Object> configurationMetadata = this.parseMap(rs.getString("configuration_metadata"));
            builder.providerConfigurationMetadata(configurationMetadata);
            return builder.build();
        }

        // 解析 OAuth2 授权类型
        private static AuthorizationGrantType resolveAuthorizationGrantType(String authorizationGrantType) {
            if (AuthorizationGrantType.AUTHORIZATION_CODE.getValue().equals(authorizationGrantType)) {
                return AuthorizationGrantType.AUTHORIZATION_CODE;
            } else if (AuthorizationGrantType.CLIENT_CREDENTIALS.getValue().equals(authorizationGrantType)) {
                return AuthorizationGrantType.CLIENT_CREDENTIALS;
            } else if (AuthorizationGrantType.PASSWORD.getValue().equals(authorizationGrantType)) {
                return AuthorizationGrantType.PASSWORD;
            } else if (AuthorizationGrantType.JWT_BEARER.getValue().equals(authorizationGrantType)) {
                return AuthorizationGrantType.JWT_BEARER;
            } else if (AuthorizationGrantType.DEVICE_CODE.getValue().equals(authorizationGrantType)) {
                return AuthorizationGrantType.DEVICE_CODE;
            } else {
                return AuthorizationGrantType.REFRESH_TOKEN.getValue().equals(authorizationGrantType) ? AuthorizationGrantType.REFRESH_TOKEN : new AuthorizationGrantType(authorizationGrantType);
            }
        }

        // 解析客户端认证方法
        private static ClientAuthenticationMethod resolveClientAuthenticationMethod(String clientAuthenticationMethod) {
            if (ClientAuthenticationMethod.CLIENT_SECRET_BASIC.getValue().equals(clientAuthenticationMethod)) {
                return ClientAuthenticationMethod.CLIENT_SECRET_BASIC;
            } else if (ClientAuthenticationMethod.CLIENT_SECRET_POST.getValue().equals(clientAuthenticationMethod)) {
                return ClientAuthenticationMethod.CLIENT_SECRET_POST;
            } else if (ClientAuthenticationMethod.CLIENT_SECRET_JWT.getValue().equals(clientAuthenticationMethod)) {
                return ClientAuthenticationMethod.CLIENT_SECRET_JWT;
            } else if (ClientAuthenticationMethod.PRIVATE_KEY_JWT.getValue().equals(clientAuthenticationMethod)) {
                return ClientAuthenticationMethod.PRIVATE_KEY_JWT;
            } else {
                return ClientAuthenticationMethod.NONE.getValue().equals(clientAuthenticationMethod) ? ClientAuthenticationMethod.NONE : new ClientAuthenticationMethod(clientAuthenticationMethod);
            }
        }

        // 解析用户信息认证方法
        private static AuthenticationMethod resolveUserInfoAuthenticationMethod(String userInfoAuthenticationMethod) {
            if (AuthenticationMethod.FORM.getValue().equals(userInfoAuthenticationMethod)) {
                return AuthenticationMethod.FORM;
            } else if (AuthenticationMethod.HEADER.getValue().equals(userInfoAuthenticationMethod)) {
                return AuthenticationMethod.HEADER;
            } else {
                return AuthenticationMethod.QUERY.getValue().equals(userInfoAuthenticationMethod) ? AuthenticationMethod.QUERY : new AuthenticationMethod(userInfoAuthenticationMethod);
            }
        }

        // 将字符串解析为 Map
        private Map<String, Object> parseMap(String data) {
            try {
                return this.objectMapper.readValue(data, new TypeReference<Map<String, Object>>() {
                });
            } catch (Exception var3) {
                throw new IllegalArgumentException(var3.getMessage(), var3);
            }
        }
    }

    /**
     * 将 ClientRegistration 转换为 SQL 参数的映射器。
     */
    public static class ClientRegistrationParametersMapper implements Function<ClientRegistration, List<SqlParameterValue>> {
        private ObjectMapper objectMapper = new ObjectMapper();

        public ClientRegistrationParametersMapper() {
            ClassLoader classLoader = JdbcClientRegistrationRepository.class.getClassLoader();
            List<Module> securityModules = SecurityJackson2Modules.getModules(classLoader);
            this.objectMapper.registerModules(securityModules);
        }

        /**
         * 将 ClientRegistration 对象转换为 SQL 参数列表。
         *
         * @param clientRegistration 客户端注册信息
         * @return SQL 参数列表
         */
        @Override
        public List<SqlParameterValue> apply(ClientRegistration clientRegistration) {
            return Arrays.asList(new SqlParameterValue(12, clientRegistration.getRegistrationId()), new SqlParameterValue(12, clientRegistration.getClientId()), new SqlParameterValue(12, clientRegistration.getClientSecret()), new SqlParameterValue(12, clientRegistration.getClientAuthenticationMethod().getValue()), new SqlParameterValue(12, clientRegistration.getAuthorizationGrantType().getValue()), new SqlParameterValue(12, clientRegistration.getClientName()), new SqlParameterValue(12, clientRegistration.getRedirectUri()), new SqlParameterValue(12, StringUtils.collectionToCommaDelimitedString(clientRegistration.getScopes())), new SqlParameterValue(12, clientRegistration.getProviderDetails().getAuthorizationUri()), new SqlParameterValue(12, clientRegistration.getProviderDetails().getTokenUri()), new SqlParameterValue(12, clientRegistration.getProviderDetails().getJwkSetUri()), new SqlParameterValue(12, clientRegistration.getProviderDetails().getIssuerUri()), new SqlParameterValue(12, clientRegistration.getProviderDetails().getUserInfoEndpoint().getUri()), new SqlParameterValue(12, clientRegistration.getProviderDetails().getUserInfoEndpoint().getAuthenticationMethod().getValue()), new SqlParameterValue(12, clientRegistration.getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName()), new SqlParameterValue(12, this.writeMap(clientRegistration.getProviderDetails().getConfigurationMetadata())));
        }

        private String writeMap(Map<String, Object> data) {
            try {
                return this.objectMapper.writeValueAsString(data);
            } catch (Exception e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
        }
    }
}
