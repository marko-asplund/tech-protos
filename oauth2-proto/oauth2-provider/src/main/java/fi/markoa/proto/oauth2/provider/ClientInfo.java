package fi.markoa.proto.oauth2.provider;

public class ClientInfo {
  private String id;
  private String description;
  private String redirectURI;

  public ClientInfo(String id, String description, String redirectURI) {
    this.id = id;
    this.description = description;
    this.redirectURI = redirectURI;
  }

}
