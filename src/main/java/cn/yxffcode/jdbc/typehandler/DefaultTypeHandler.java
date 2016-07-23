package cn.yxffcode.jdbc.typehandler;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author gaohang on 7/23/16.
 */
public class DefaultTypeHandler implements TypeHandler<Object> {
  @Override
  public Object mapNotNull(Object parameter) throws SQLException {
    return parameter;
  }

  @Override
  public Object getNullableColumnValue(ResultSet rs, int columnIndex) throws SQLException {
    return  rs.getObject(columnIndex);
  }

  @Override
  public Object getNullableColumnValue(ResultSet rs, String columnName) throws SQLException {
    return rs.getObject(columnName);
  }
}
