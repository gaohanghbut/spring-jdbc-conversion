package cn.yxffcode.jdbc;

import cn.yxffcode.jdbc.typehandler.ResultMap;
import cn.yxffcode.jdbc.typehandler.NamedParameterJdbcTemplate;
import cn.yxffcode.jdbc.typehandler.ParamMap;
import cn.yxffcode.jdbc.typehandler.TypeHandlerRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
    User user = new User();
    user.setId(TestEnum.B);
    user.setName("gaohang");
    namedParameterJdbcTemplate.update("insert into user (id, name) values (:id, :name)", ParamMap.fromNotNull(user));

    List<User> users = namedParameterJdbcTemplate.query("select * from user", new ResultMap<User>(typeHandlerRegistry) {
      @Override
      protected void configMapping() {
        addMapping("id", "id");
        addMapping("name", "name");
      }
    });
    System.out.println(users);
  }
}
