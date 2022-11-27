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
 * @author: ReLive
 * @date: 2022/11/21 18:48
 */
public class JdbcOAuth2IntrospectionService implements OAuth2IntrospectionService {
    private static final String COLUMN_NAMES = "id," +
            "client_id," +
            "client_secret," +
            "issuer_uri," +
            "introspection_uri";
    private static final String TABLE_NAME = "oauth2_introspection";

    private static final String PK_FILTER = "issuer_uri = ?";

    private static final String LOAD_OAUTH2_INTROSPECTION_SQL = "SELECT " + COLUMN_NAMES + " FROM " + TABLE_NAME + " WHERE ";

    private static final String REMOVE_OAUTH2_INTROSPECTION_SQL = "DELETE FROM " + TABLE_NAME + " WHERE " + PK_FILTER;

    private static final String INSERT_OAUTH2_INTROSPECTION_SQL = "INSERT INTO " + TABLE_NAME + "(" + COLUMN_NAMES + ") VALUES(?,?,?,?,?)";

    private static final String UPDATE_OAUTH2_INTROSPECTION_SQL = "UPDATE " + TABLE_NAME + " SET client_id = ?," +
            "client_secret = ?,issuer_uri = ?,introspection_uri = ?," +
            "WHERE id = ?";

    private final JdbcOperations jdbcOperations;
    private RowMapper<OAuth2Introspection> oAuth2IntrospectionRowMapper;
    private Function<OAuth2Introspection, List<SqlParameterValue>> oAuth2IntrospectionListParametersMapper;


    public JdbcOAuth2IntrospectionService(JdbcOperations jdbcOperations) {
        Assert.notNull(jdbcOperations, "JdbcOperations can not be null");
        this.jdbcOperations = jdbcOperations;
        this.oAuth2IntrospectionRowMapper = new OAuth2IntrospectionRowMapper();
        this.oAuth2IntrospectionListParametersMapper = new OAuth2IntrospectionParametersMapper();
    }

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

    public OAuth2Introspection findById(String id) {
        Assert.hasText(id, "id cannot be empty");
        SqlParameterValue[] parameters = new SqlParameterValue[]{
                new SqlParameterValue(Types.VARCHAR, id)};
        PreparedStatementSetter pss = new ArgumentPreparedStatementSetter(parameters);
        List<OAuth2Introspection> result = this.jdbcOperations.query(LOAD_OAUTH2_INTROSPECTION_SQL + "id = ?", pss,
                this.oAuth2IntrospectionRowMapper);
        return !result.isEmpty() ? result.get(0) : null;
    }

    private void updateOAuth2Introspection(OAuth2Introspection oAuth2Introspection) {
        List<SqlParameterValue> parameters = this.oAuth2IntrospectionListParametersMapper
                .apply(oAuth2Introspection);
        SqlParameterValue idParameter = parameters.remove(0);
        parameters.add(idParameter);
        PreparedStatementSetter statementSetter = new ArgumentPreparedStatementSetter(parameters.toArray());
        this.jdbcOperations.update(UPDATE_OAUTH2_INTROSPECTION_SQL, statementSetter);

    }

    private void insertOAuth2Introspection(OAuth2Introspection oAuth2Introspection) {
        List<SqlParameterValue> parameters = this.oAuth2IntrospectionListParametersMapper
                .apply(oAuth2Introspection);
        PreparedStatementSetter statementSetter = new ArgumentPreparedStatementSetter(parameters.toArray());
        this.jdbcOperations.update(INSERT_OAUTH2_INTROSPECTION_SQL, statementSetter);
    }

    @Override
    public void removeOAuth2Introspection(String issuer) {
        Assert.hasText(issuer, "issuer cannot be empty");
        SqlParameterValue[] parameters = new SqlParameterValue[]{
                new SqlParameterValue(Types.VARCHAR, issuer)};
        PreparedStatementSetter pss = new ArgumentPreparedStatementSetter(parameters);
        this.jdbcOperations.update(REMOVE_OAUTH2_INTROSPECTION_SQL, pss);
    }

    public final void setOAuth2IntrospectionRowMapper(RowMapper<OAuth2Introspection> oAuth2IntrospectionRowMapper) {
        Assert.notNull(oAuth2IntrospectionRowMapper, "oAuth2IntrospectionRowMapper cannot be null");
        this.oAuth2IntrospectionRowMapper = oAuth2IntrospectionRowMapper;
    }

    public final void setOAuth2IntrospectionListParametersMapper(
            Function<OAuth2Introspection, List<SqlParameterValue>> oAuth2IntrospectionListParametersMapper) {
        Assert.notNull(oAuth2IntrospectionListParametersMapper, "oAuth2IntrospectionListParametersMapper cannot be null");
        this.oAuth2IntrospectionListParametersMapper = oAuth2IntrospectionListParametersMapper;
    }

    /**
     * The default {@link RowMapper} that maps the current row in
     * {@code java.sql.ResultSet} to {@link OAuth2Introspection}.
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
     * The default {@code Function} that maps {@link OAuth2Introspection} to a
     * {@code List} of {@link SqlParameterValue}.
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
