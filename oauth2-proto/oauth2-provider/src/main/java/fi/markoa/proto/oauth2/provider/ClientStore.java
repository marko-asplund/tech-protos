package fi.markoa.proto.oauth2.provider;

import java.util.HashMap;
import java.util.Map;

public class ClientStore {

  private Map<String, ClientInfo> clients;

  public ClientStore() {
    clients = new HashMap<>();
    clients.put("131804060198305", new ClientInfo("131804060198305", "test account", "http://localhost:8080/oauth2/site/snoop"));
  }
}
