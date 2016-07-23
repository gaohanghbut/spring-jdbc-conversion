package cn.yxffcode.jdbc;

import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * @author gaohang on 7/22/16.
 */
public abstract class MappingRowMap<T> extends TypeToken<T> implements RowMapper<T> {

  private final TypeHandlerRegistry typeHandlerRegistry;
  private final Class<T> type;

  private final Map<String, String> property2ColumnMap = Maps.newHashMap();

  public MappingRowMap(TypeHandlerRegistry typeHandlerRegistry) {
    this.typeHandlerRegistry = typeHandlerRegistry;
    this.type = (Class<T>) getType();
    configMapping();
  }

  public T mapRow(ResultSet rs, int rowNum) throws SQLException {
    T rowValue = BeanUtils.instantiate(type);
    for (Map.Entry<String, String> en : property2ColumnMap.entrySet()) {
      Field field = ReflectionUtils.findField(type, en.getKey());
      Class<?> propertyType = field.getType();
      TypeHandler<?> typeHandler = typeHandlerRegistry.get(propertyType);
      Object propertyValue = typeHandler.getNullableColumnValue(rs, en.getValue());
      field.setAccessible(true);
      ReflectionUtils.setField(field, rowValue, propertyValue);
    }
    return rowValue;
  }

  protected void addMapping(String column, String property) {
    property2ColumnMap.put(property, column);
  }

  protected abstract void configMapping();
}
