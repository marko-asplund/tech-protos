package fi.markoa.proto.oauth2.provider;

/**
 * Created with IntelliJ IDEA.
 * User: aspluma
 * Date: 2014-06-29
 * Time: 00:23
 * To change this template use File | Settings | File Templates.
 */
public class User {
  private String id;
  private String password;
  private String name;

  public User(String id, String password, String name) {
    this.id = id;
    this.password = password;
    this.name = name;
  }

}
