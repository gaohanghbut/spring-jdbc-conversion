package cn.yxffcode.jdbc;

import cn.yxffcode.jdbc.typehandler.TypeHandler;
import cn.yxffcode.jdbc.typehandler.TypeHandlerRegistry;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.ParameterDisposer;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.SqlProvider;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.jdbc.core.namedparam.ParsedSql;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractor;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author gaohang on 7/22/16.
 */
public class NamedParameterJdbcTemplate extends org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate {

  private final TypeHandlerRegistry typeHandlerRegistry;

  public NamedParameterJdbcTemplate(DataSource dataSource,
                                    TypeHandlerRegistry typeHandlerRegistry) {
    super(dataSource);
    this.typeHandlerRegistry = typeHandlerRegistry;
  }

  public NamedParameterJdbcTemplate(JdbcOperations classicJdbcTemplate,
                                    TypeHandlerRegistry typeHandlerRegistry) {
    super(classicJdbcTemplate);
    this.typeHandlerRegistry = typeHandlerRegistry;
  }

  @Override
  protected PreparedStatementCreator getPreparedStatementCreator(String sql, SqlParameterSource paramSource) {
    ParsedSql parsedSql = getParsedSql(sql);
    String sqlToUse = NamedParameterUtils.substituteNamedParameters(parsedSql, paramSource);
    Object[] params = NamedParameterUtils.buildValueArray(parsedSql, paramSource, null);
    List<SqlParameter> declaredParameters = NamedParameterUtils.buildSqlParameterList(parsedSql, paramSource);
    PreparedStatementCreatorFactory pscf = new PreparedStatementCreatorFactory(sqlToUse, declaredParameters);
    return pscf.newPreparedStatementCreator(params);
  }

  private final class PreparedStatementCreatorFactory {

    /**
     * The SQL, which won't change when the parameters change
     */
    private final String sql;

    /**
     * List of SqlParameter objects (may not be {@code null})
     */
    private final List<SqlParameter> declaredParameters;

    private int resultSetType = ResultSet.TYPE_FORWARD_ONLY;

    private boolean updatableResults = false;

    private boolean returnGeneratedKeys = false;

    private String[] generatedKeysColumnNames = null;

    private NativeJdbcExtractor nativeJdbcExtractor;

    /**
     * Create a new factory with the given SQL and parameters.
     *
     * @param sql                SQL
     * @param declaredParameters list of {@link SqlParameter} objects
     * @see SqlParameter
     */
    public PreparedStatementCreatorFactory(String sql, List<SqlParameter> declaredParameters) {
      this.sql = sql;
      this.declaredParameters = declaredParameters;
    }

    /**
     * Return a new PreparedStatementCreator for the given parameters.
     *
     * @param params the parameter array (may be {@code null})
     */
    public PreparedStatementCreator newPreparedStatementCreator(Object[] params) {
      return new PreparedStatementCreatorImpl(params != null ? Arrays.asList(params) : Collections.emptyList());
    }

    /**
     * PreparedStatementCreator implementation returned by this class.
     */
    private final class PreparedStatementCreatorImpl
            implements PreparedStatementCreator, PreparedStatementSetter, SqlProvider, ParameterDisposer {

      private final String actualSql;

      private final List<?> parameters;

      public PreparedStatementCreatorImpl(List<?> parameters) {
        this(sql, parameters);
      }

      public PreparedStatementCreatorImpl(String actualSql, List<?> parameters) {
        this.actualSql = actualSql;
        Assert.notNull(parameters, "Parameters List must not be null");
        this.parameters = parameters;
        if (this.parameters.size() != declaredParameters.size()) {
          // account for named parameters being used multiple times
          Set<String> names = new HashSet<String>();
          for (int i = 0; i < parameters.size(); i++) {
            Object param = parameters.get(i);
            if (param instanceof SqlParameterValue) {
              names.add(((SqlParameterValue) param).getName());
            } else {
              names.add("Parameter #" + i);
            }
          }
          if (names.size() != declaredParameters.size()) {
            throw new InvalidDataAccessApiUsageException(
                    "SQL [" + sql + "]: given " + names.size() +
                            " parameters but expected " + declaredParameters.size());
          }
        }
      }

      @Override
      public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
        PreparedStatement ps;
        if (generatedKeysColumnNames != null || returnGeneratedKeys) {
          if (generatedKeysColumnNames != null) {
            ps = con.prepareStatement(this.actualSql, generatedKeysColumnNames);
          } else {
            ps = con.prepareStatement(this.actualSql, PreparedStatement.RETURN_GENERATED_KEYS);
          }
        } else if (resultSetType == ResultSet.TYPE_FORWARD_ONLY && !updatableResults) {
          ps = con.prepareStatement(this.actualSql);
        } else {
          ps = con.prepareStatement(this.actualSql, resultSetType,
                  updatableResults ? ResultSet.CONCUR_UPDATABLE : ResultSet.CONCUR_READ_ONLY);
        }
        setValues(ps);
        return ps;
      }

      @Override
      public void setValues(PreparedStatement ps) throws SQLException {
        // Determine PreparedStatement to pass to custom types.
        PreparedStatement psToUse = ps;
        if (nativeJdbcExtractor != null) {
          psToUse = nativeJdbcExtractor.getNativePreparedStatement(ps);
        }

        // Set arguments: Does nothing if there are no parameters.
        int sqlColIndx = 1;
        for (int i = 0; i < this.parameters.size(); i++) {
          Object in = this.parameters.get(i);
          SqlParameter declaredParameter;
          // SqlParameterValue overrides declared parameter metadata, in particular for
          // independence from the declared parameter position in case of named parameters.
          if (in instanceof SqlParameterValue) {
            SqlParameterValue paramValue = (SqlParameterValue) in;
            in = paramValue.getValue();
            declaredParameter = paramValue;
          } else {
            if (declaredParameters.size() <= i) {
              throw new InvalidDataAccessApiUsageException(
                      "SQL [" + sql + "]: unable to access parameter number " + (i + 1) +
                              " given only " + declaredParameters.size() + " parameters");

            }
            declaredParameter = declaredParameters.get(i);
          }
          if (in instanceof Collection && declaredParameter.getSqlType() != Types.ARRAY) {
            Collection<?> entries = (Collection<?>) in;
            for (Object entry : entries) {
              if (entry instanceof Object[]) {
                Object[] valueArray = ((Object[]) entry);
                for (Object argValue : valueArray) {
                  //convert the argValue
                  StatementCreatorUtils.setParameterValue(psToUse,
                          sqlColIndx++, declaredParameter, tryConvert(argValue));
                }
              } else {
                StatementCreatorUtils.setParameterValue(psToUse,
                        sqlColIndx++, declaredParameter, tryConvert(entry));
              }
            }
          } else {
            StatementCreatorUtils.setParameterValue(psToUse,
                    sqlColIndx++, declaredParameter, tryConvert(in));
          }
        }
      }

      private Object tryConvert(Object argValue) throws SQLException {
        if (argValue != null) {
          TypeHandler typeHandler = typeHandlerRegistry.get(argValue.getClass());
          argValue = typeHandler.mapNotNull(argValue);
        }
        return argValue;
      }

      @Override
      public String getSql() {
        return sql;
      }

      @Override
      public void cleanupParameters() {
        StatementCreatorUtils.cleanupParameters(this.parameters);
      }

      @Override
      public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PreparedStatementCreatorFactory.PreparedStatementCreatorImpl: sql=[");
        sb.append(sql).append("]; parameters=").append(this.parameters);
        return sb.toString();
      }
    }

  }

}
