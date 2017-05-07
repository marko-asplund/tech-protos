package com.practicingtechie.oauth2

import scalaz.concurrent.Task
import org.http4s.server.{Server, ServerApp, ServerBuilder}
import org.http4s.server.blaze._
import org.http4s._
import org.http4s.dsl._
import org.log4s.getLogger
import java.util.concurrent.Executors

class Service(host: String, port: Int) {
  import org.apache.oltu.oauth2.client.{OAuthClient, URLConnectionClient}
  import org.apache.oltu.oauth2.client.request.{OAuthClientRequest, OAuthBearerClientRequest}
  import org.apache.oltu.oauth2.client.response.{GitHubTokenResponse, OAuthResourceResponse}
  import org.apache.oltu.oauth2.common.{OAuthProviderType, OAuth}
  import org.apache.oltu.oauth2.common.message.types.GrantType

  private val logger = getLogger
  private val pool   = Executors.newCachedThreadPool()

  val WebResources = List(".js", ".css", ".html")
  val StaticPath = "static"
  val Scope = "openid profile"
  val ConfigFile = "oauth2-demo.properties"
  val config = readConfig()
  val ClientId = config("client_id")
  val ClientSecret = config("client_secret")
  val RedirectUri = config("redirect_uri")
  val State = "abc-xyz" // FIXME
  val seedDigest = {
    val md = java.security.MessageDigest.getInstance("SHA1")
    md.update(new scala.util.Random().alphanumeric.take(20).mkString.getBytes)
  }

  def readConfig(): Map[String, String] = {
    import collection.JavaConverters._
    val is = this.getClass.getClassLoader.getResourceAsStream(ConfigFile)
    val props = new java.util.Properties
    props.load(is)
    is.close
    props.asScala.toMap
  }

  def oauthRequestUri =
    OAuthClientRequest.
      authorizationProvider(OAuthProviderType.GITHUB).
      setClientId(ClientId).
      setScope(Scope).
      setState(State).
      setRedirectURI(RedirectUri).
      buildQueryMessage().
      getLocationUri()

  def static(file: String, request: Request) =
    StaticFile.fromResource(s"/$StaticPath/$file", Some(request)).map(Task.now).getOrElse(NotFound())

  val helloWorldService = HttpService {
    case req @ GET -> Root / "authorize" =>

      Ok(oauthRequestUri)
    case req @ GET -> Root / "redirect" =>
      val code = req.params("code")
      val state = req.params("state")
      // TODO: state should match
      println(s"- code: $code")
      println(s"- state: $state")

      val orq = OAuthClientRequest.
        tokenProvider(OAuthProviderType.GITHUB).
        setGrantType(GrantType.AUTHORIZATION_CODE).
        setClientId(ClientId).
        setClientSecret(ClientSecret).
        setRedirectURI(RedirectUri).
        setScope(Scope).
        setCode(code).
        buildQueryMessage()

      val cli = new OAuthClient(new URLConnectionClient())
      val res = cli.accessToken(orq, classOf[GitHubTokenResponse])
      val accessToken = res.getAccessToken()
      val expiresIn = res.getExpiresIn()
      println(s"token: $accessToken")

      val bearerClientRequest = new OAuthBearerClientRequest("https://api.github.com/user")
        .setAccessToken(accessToken).buildQueryMessage()

      val resourceResponse = cli.resource(bearerClientRequest, OAuth.HttpMethod.GET, classOf[OAuthResourceResponse])
      val contentType = resourceResponse.getContentType
      println(s"type: $contentType")
      if (contentType.startsWith("application/json"))
        println(s"res: ${resourceResponse.getBody}")

      Ok("ok")
    case req @ GET -> Root / "static" / path if WebResources.exists(path.endsWith) =>
      //val data = req.headers.get(org.http4s.headers.`User-Agent`) + System.currentTimeMillis.toString
      //val md = org.apache.commons.codec.digest.DigestUtils.sha256Hex(data)
      static(path, req)//.addCookie("auth", md)
  }

  val services = helloWorldService

  def build(): ServerBuilder =
    BlazeBuilder.bindHttp(port, host).
      mountService(services, "/").
      withServiceExecutor(pool)
}

object Main extends ServerApp {
  override def server(args: List[String]): Task[Server] = {
    val ip   = "0.0.0.0"
    val port = 8080

    new Service(ip, port).build().start
  }
}
