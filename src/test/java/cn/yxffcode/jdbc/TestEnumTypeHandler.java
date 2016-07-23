package cn.yxffcode.jdbc;

import cn.yxffcode.jdbc.typehandler.TypeHandler;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author gaohang on 7/23/16.
 */
public class TestEnumTypeHandler implements TypeHandler<TestEnum> {
  @Override
  public Object mapNotNull(TestEnum parameter) throws SQLException {
    return parameter.ordinal();
  }

  @Override
  public TestEnum getNullableColumnValue(ResultSet rs, int columnIndex) throws SQLException {
    Integer value = (Integer) rs.getObject(columnIndex);
    if (value == null) {
      return null;
    }
    int v = value;
    for (TestEnum testEnum : TestEnum.values()) {
      if (testEnum.ordinal() == v) {
        return testEnum;
      }
    }
    return null;
  }

  @Override
  public TestEnum getNullableColumnValue(ResultSet rs, String columnName) throws SQLException {
    Integer value = (Integer) rs.getObject(columnName);
    if (value == null) {
      return null;
    }
    int v = value;
    for (TestEnum testEnum : TestEnum.values()) {
      if (testEnum.ordinal() == v) {
        return testEnum;
      }
    }
    return null;
  }
}
