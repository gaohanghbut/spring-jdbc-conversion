package cn.yxffcode.jdbc;

import com.google.common.collect.Maps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.List;

/**
 * @author gaohang on 7/23/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring.xml")
public class Tester {

  @Autowired
  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  @Autowired
  private TypeHandlerRegistry typeHandlerRegistry;

  @Test
  public void test() {
    HashMap<String, Object> params = Maps.newHashMap();
    params.put("id", TestEnum.B);
    params.put("name", "gaohang");
    namedParameterJdbcTemplate.update("insert into user (id, name) values (:id, :name)", params);

    List<User> users = namedParameterJdbcTemplate.query("select * from user", new MappingRowMap<User>(typeHandlerRegistry) {
      @Override
      protected void configMapping() {
        addMapping("id", "id");
        addMapping("name", "name");
      }
    });
    System.out.println(users);
  }

}
