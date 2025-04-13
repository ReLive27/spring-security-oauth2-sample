package com.relive.introspection;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.*;
import org.springframework.util.Assert;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 使用 JDBC 进行 OAuth2 Introspection 数据库操作的服务类。
 * 该类提供了加载、保存、更新和删除 OAuth2Introspection 数据的功能，支持数据库中的操作。
 * 它实现了 OAuth2IntrospectionService 接口，并使用 JDBC 执行 SQL 操作。
 *
 * @author: ReLive
 * @date: 2022/11/21 18:48
 */
public class JdbcOAuth2IntrospectionService implements OAuth2IntrospectionService {
    // 定义表的列名
    private static final String COLUMN_NAMES = "id," +
            "client_id," +
            "client_secret," +
            "issuer_uri," +
            "introspection_uri";

    // 定义表名
    private static final String TABLE_NAME = "oauth2_introspection";

    // 定义主键过滤条件
    private static final String PK_FILTER = "issuer_uri = ?";

    // 查询 OAuth2 Introspection 数据的 SQL 语句
    private static final String LOAD_OAUTH2_INTROSPECTION_SQL = "SELECT " + COLUMN_NAMES + " FROM " + TABLE_NAME + " WHERE ";

    // 删除 OAuth2 Introspection 数据的 SQL 语句
    private static final String REMOVE_OAUTH2_INTROSPECTION_SQL = "DELETE FROM " + TABLE_NAME + " WHERE " + PK_FILTER;

    // 插入 OAuth2 Introspection 数据的 SQL 语句
    private static final String INSERT_OAUTH2_INTROSPECTION_SQL = "INSERT INTO " + TABLE_NAME + "(" + COLUMN_NAMES + ") VALUES(?,?,?,?,?)";

    // 更新 OAuth2 Introspection 数据的 SQL 语句
    private static final String UPDATE_OAUTH2_INTROSPECTION_SQL = "UPDATE " + TABLE_NAME + " SET client_id = ?," +
            "client_secret = ?,issuer_uri = ?,introspection_uri = ?," +
            "WHERE id = ?";

    // JdbcOperations 实例，用于执行数据库操作
    private final JdbcOperations jdbcOperations;

    // 用于将查询结果映射到 OAuth2Introspection 对象的 RowMapper
    private RowMapper<OAuth2Introspection> oAuth2IntrospectionRowMapper;

    // 用于将 OAuth2Introspection 对象映射为 SQL 参数的函数
    private Function<OAuth2Introspection, List<SqlParameterValue>> oAuth2IntrospectionListParametersMapper;

    /**
     * 构造函数，初始化 JDBC 操作和映射器。
     *
     * @param jdbcOperations JDBC 操作实例
     */
    public JdbcOAuth2IntrospectionService(JdbcOperations jdbcOperations) {
        Assert.notNull(jdbcOperations, "JdbcOperations can not be null");
        this.jdbcOperations = jdbcOperations;
        this.oAuth2IntrospectionRowMapper = new OAuth2IntrospectionRowMapper();
        this.oAuth2IntrospectionListParametersMapper = new OAuth2IntrospectionParametersMapper();
    }

    /**
     * 加载指定 issuer 的 OAuth2Introspection 数据。
     *
     * @param issuer OAuth2 的 issuer 地址
     * @return OAuth2Introspection 对象，如果找不到，则返回 null
     */
    @Override
    public OAuth2Introspection loadIntrospection(String issuer) {
        Assert.hasText(issuer, "issuer cannot be empty");
        SqlParameterValue[] parameters = new SqlParameterValue[]{
                new SqlParameterValue(Types.VARCHAR, issuer)};
        PreparedStatementSetter pss = new ArgumentPreparedStatementSetter(parameters);
        List<OAuth2Introspection> result = this.jdbcOperations.query(LOAD_OAUTH2_INTROSPECTION_SQL + PK_FILTER, pss,
                this.oAuth2IntrospectionRowMapper);
        return !result.isEmpty() ? result.get(0) : null;
    }

    /**
     * 保存 OAuth2Introspection 数据。如果 ID 已存在则更新，否则插入。
     *
     * @param oAuth2Introspection OAuth2Introspection 对象
     */
    @Override
    public void saveOAuth2Introspection(OAuth2Introspection oAuth2Introspection) {
        Assert.notNull(oAuth2Introspection, "oAuth2Introspection cannot be null");
        boolean existsAuthorizedClient = null != this.findById(
                oAuth2Introspection.getId());
        if (existsAuthorizedClient) {
            updateOAuth2Introspection(oAuth2Introspection);
        } else {
            try {
                insertOAuth2Introspection(oAuth2Introspection);
            } catch (DuplicateKeyException ex) {
                updateOAuth2Introspection(oAuth2Introspection);
            }
        }
    }

    /**
     * 根据 ID 查找 OAuth2Introspection 数据。
     *
     * @param id OAuth2Introspection 的 ID
     * @return 对应的 OAuth2Introspection 对象，如果找不到则返回 null
     */
    public OAuth2Introspection findById(String id) {
        Assert.hasText(id, "id cannot be empty");
        SqlParameterValue[] parameters = new SqlParameterValue[]{
                new SqlParameterValue(Types.VARCHAR, id)};
        PreparedStatementSetter pss = new ArgumentPreparedStatementSetter(parameters);
        List<OAuth2Introspection> result = this.jdbcOperations.query(LOAD_OAUTH2_INTROSPECTION_SQL + "id = ?", pss,
                this.oAuth2IntrospectionRowMapper);
        return !result.isEmpty() ? result.get(0) : null;
    }

    /**
     * 更新 OAuth2Introspection 数据。
     *
     * @param oAuth2Introspection OAuth2Introspection 对象
     */
    private void updateOAuth2Introspection(OAuth2Introspection oAuth2Introspection) {
        List<SqlParameterValue> parameters = this.oAuth2IntrospectionListParametersMapper
                .apply(oAuth2Introspection);
        SqlParameterValue idParameter = parameters.remove(0);
        parameters.add(idParameter);
        PreparedStatementSetter statementSetter = new ArgumentPreparedStatementSetter(parameters.toArray());
        this.jdbcOperations.update(UPDATE_OAUTH2_INTROSPECTION_SQL, statementSetter);
    }

    /**
     * 插入 OAuth2Introspection 数据。
     *
     * @param oAuth2Introspection OAuth2Introspection 对象
     */
    private void insertOAuth2Introspection(OAuth2Introspection oAuth2Introspection) {
        List<SqlParameterValue> parameters = this.oAuth2IntrospectionListParametersMapper
                .apply(oAuth2Introspection);
        PreparedStatementSetter statementSetter = new ArgumentPreparedStatementSetter(parameters.toArray());
        this.jdbcOperations.update(INSERT_OAUTH2_INTROSPECTION_SQL, statementSetter);
    }

    /**
     * 删除指定 issuer 的 OAuth2Introspection 数据。
     *
     * @param issuer OAuth2 的 issuer 地址
     */
    @Override
    public void removeOAuth2Introspection(String issuer) {
        Assert.hasText(issuer, "issuer cannot be empty");
        SqlParameterValue[] parameters = new SqlParameterValue[]{
                new SqlParameterValue(Types.VARCHAR, issuer)};
        PreparedStatementSetter pss = new ArgumentPreparedStatementSetter(parameters);
        this.jdbcOperations.update(REMOVE_OAUTH2_INTROSPECTION_SQL, pss);
    }

    // 设置 OAuth2Introspection 的 RowMapper
    public final void setOAuth2IntrospectionRowMapper(RowMapper<OAuth2Introspection> oAuth2IntrospectionRowMapper) {
        Assert.notNull(oAuth2IntrospectionRowMapper, "oAuth2IntrospectionRowMapper cannot be null");
        this.oAuth2IntrospectionRowMapper = oAuth2IntrospectionRowMapper;
    }

    // 设置将 OAuth2Introspection 映射为 SQL 参数的函数
    public final void setOAuth2IntrospectionListParametersMapper(
            Function<OAuth2Introspection, List<SqlParameterValue>> oAuth2IntrospectionListParametersMapper) {
        Assert.notNull(oAuth2IntrospectionListParametersMapper, "oAuth2IntrospectionListParametersMapper cannot be null");
        this.oAuth2IntrospectionListParametersMapper = oAuth2IntrospectionListParametersMapper;
    }

    /**
     * 默认的 RowMapper 实现，将 ResultSet 中的当前行映射为 OAuth2Introspection 对象。
     */
    public static class OAuth2IntrospectionRowMapper implements RowMapper<OAuth2Introspection> {

        @Override
        public OAuth2Introspection mapRow(ResultSet rs, int rowNum) throws SQLException {
            OAuth2Introspection.Builder builder = OAuth2Introspection.withIssuer(rs.getString("issuer_uri"))
                    .id(rs.getString("id"))
                    .clientId(rs.getString("client_id"))
                    .clientSecret(rs.getString("client_secret"))
                    .introspectionUri(rs.getString("introspection_uri"));
            return builder.build();
        }
    }

    /**
     * 默认的 Function 实现，将 OAuth2Introspection 对象映射为 SqlParameterValue 列表。
     */
    public static class OAuth2IntrospectionParametersMapper implements Function<OAuth2Introspection, List<SqlParameterValue>> {

        @Override
        public List<SqlParameterValue> apply(OAuth2Introspection oAuth2Introspection) {
            List<SqlParameterValue> parameters = new ArrayList<>();
            parameters.add(new SqlParameterValue(12, oAuth2Introspection.getId()));
            parameters.add(new SqlParameterValue(12, oAuth2Introspection.getClientId()));
            parameters.add(new SqlParameterValue(12, oAuth2Introspection.getClientSecret()));
            parameters.add(new SqlParameterValue(12, oAuth2Introspection.getIssuer()));
            parameters.add(new SqlParameterValue(12, oAuth2Introspection.getIntrospectionUri()));
            return parameters;
        }
    }
}
