# spring-jdbc-conversion
为spring-jdbc提供类似于mybatis的类型转换的支持

Spring配置 
```xml 
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:jdbc="http://www.springframework.org/schema/jdbc"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
    http://www.springframework.org/schema/beans/spring-beans.xsd
    http://www.springframework.org/schema/jdbc
    http://www.springframework.org/schema/jdbc/spring-jdbc.xsd">

  <jdbc:embedded-database id="dataSource" type="H2">
    <jdbc:script location="classpath:sql/create-table.sql"/>
  </jdbc:embedded-database>

  <bean class="cn.yxffcode.jdbc.NamedParameterJdbcTemplate">
    <constructor-arg index="0" ref="dataSource"/>
    <constructor-arg index="1" ref="typeHandlerRegistry"/>
  </bean>

  <bean name="typeHandlerRegistry" class="cn.yxffcode.jdbc.DefaultTypeHandlerRegistry">
    <property name="typeHandlers">
      <list>
        <bean class="cn.yxffcode.jdbc.TestEnumTypeHandler"/>
      </list>
    </property>
  </bean>

  <bean class="ConfigurableMappingRowMap"></bean>
</beans>

```

Java代码示例 
```java 
package cn.yxffcode.jdbc;

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

    List<User> users = namedParameterJdbcTemplate.query("select * from user", new MappingRowMapper<User>(typeHandlerRegistry) {
      @Override
      protected void configMapping() {
        addMapping("id", "id");
        addMapping("name", "name");
      }
    });
    System.out.println(users);
  }
}

```
