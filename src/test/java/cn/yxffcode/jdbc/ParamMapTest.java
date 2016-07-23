package cn.yxffcode.jdbc;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author gaohang on 7/23/16.
 */
public class ParamMapTest {

  @Test
  public void test() {
    User user = new User();
    user.setId(TestEnum.A);
    user.setName("gaohang");

    ParamMap paramMap = ParamMap.fromNotNull(user);
    Assert.assertEquals(paramMap.get("id"), TestEnum.A);
    Assert.assertEquals(paramMap.get("name"), "gaohang");
  }
}
