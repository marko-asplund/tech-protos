package fi.markoa.proto.oauth2.provider;

import java.util.HashMap;
import java.util.Map;

public class UserStore {
  private Map<String, User> users;

  public UserStore() {
    users = new HashMap<>();
    users.put("user1", new User("user1", "pwd1", "user one"));
  }
}
