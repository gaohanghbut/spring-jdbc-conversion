package cn.yxffcode.jdbc.typehandler;

import java.util.List;

/**
 * @author gaohang on 7/22/16.
 */
public interface TypeHandlerRegistry {

  void setTypeHandlers(List<TypeHandler<?>> typeHandlers);

  <T> TypeHandler<? super T> get(Class<T> type);
}
