package cn.yxffcode.jdbc;

/**
 * @author gaohang on 7/23/16.
 */
public final class User {
  private TestEnum id;
  private String name;

  public TestEnum getId() {
    return id;
  }

  public void setId(TestEnum id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "User{" +
            "id=" + id +
            ", name='" + name + '\'' +
            '}';
  }
}