package cn.yxffcode.jdbc.typehandler;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * To handle the special type and convert it to one of jdbc types
 *
 * @author gaohang on 7/21/16.
 */
public interface TypeHandler<T> {

  Object mapNotNull(T parameter) throws SQLException;

  /**
   * get the special type value from ResultSet, the value returned could be null.
   */
  T getNullableColumnValue(ResultSet rs, int columnIndex) throws SQLException;

  /**
   * get the special type value from ResultSet, the value returned could be null.
   */
  T getNullableColumnValue(ResultSet rs, String columnName) throws SQLException;

}
