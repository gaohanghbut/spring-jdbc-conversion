package cn.yxffcode.jdbc.typehandler;

import com.google.common.collect.Maps;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author gaohang on 7/22/16.
 */
public class DefaultTypeHandlerRegistry implements TypeHandlerRegistry {

  private static final TypeHandler<Object> DEFAULT_TYPE_HANDLER = new DefaultTypeHandler();

  private Map<Class<?>, TypeHandler<?>> typeHandlerMap = Maps.newHashMap();

  @Override
  public void setTypeHandlers(List<TypeHandler<?>> typeHandlers) {
    if (typeHandlers == null || typeHandlers.size() == 0) {
      return;
    }
    for (TypeHandler<?> typeHandler : typeHandlers) {
      if (typeHandler == null) {
        continue;
      }
      Type[] genericInterfaces = typeHandler.getClass().getGenericInterfaces();
      for (Type genericType : genericInterfaces) {
        checkState(genericType instanceof ParameterizedType);
        ParameterizedType ptype = (ParameterizedType) genericType;
        if (ptype.getRawType() != TypeHandler.class) {
          continue;
        }
        Class<?> paramType = (Class<?>) ptype.getActualTypeArguments()[0];
        typeHandlerMap.put(paramType, typeHandler);
        break;
      }
    }
  }

  @Override
  public <T> TypeHandler<? super T> get(Class<T> type) {
    final TypeHandler<? super T> typeHandler = (TypeHandler<? super T>) typeHandlerMap.get(type);
    if (typeHandler != null) {
      return typeHandler;
    }
    if (type.isEnum()) {
      return (TypeHandler<? super T>) new EnumTypeHandler(type);
    }
    return DEFAULT_TYPE_HANDLER;
  }
}
