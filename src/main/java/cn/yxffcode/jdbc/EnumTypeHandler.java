package cn.yxffcode.jdbc;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author gaohang on 7/23/16.
 */
public class EnumTypeHandler<E extends Enum<E>> implements TypeHandler<E> {

  private final Class<E> enumType;

  public EnumTypeHandler(Class<E> enumType) {
    this.enumType = enumType;
  }

  @Override
  public Object mapNotNull(E parameter) throws SQLException {
    return parameter.name();
  }

  @Override
  public E getNullableColumnValue(ResultSet rs, int columnIndex) throws SQLException {
    String value = rs.getString(columnIndex);
    if (Strings.isNullOrEmpty(value)) {
      return null;
    }
    return Enum.valueOf(enumType, value);
  }

  @Override
  public E getNullableColumnValue(ResultSet rs, String columnName) throws SQLException {
    String value = rs.getString(columnName);
    if (Strings.isNullOrEmpty(value)) {
      return null;
    }
    return Enum.valueOf(enumType, value);
  }

}
