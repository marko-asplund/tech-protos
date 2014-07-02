package fi.markoa.proto.oauth2.provider;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuer;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.issuer.UUIDValueGenerator;
import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet(name="authorizationServlet", urlPatterns={"/authorize"})
public class AuthorizationServlet extends HttpServlet {
  private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationServlet.class);

  private OAuthIssuer oauthIssuer = new OAuthIssuerImpl(new UUIDValueGenerator());

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    LOGGER.debug("doGet: "+request);

    OAuthAuthzRequest oauthRequest = null;

    try {
      // dynamically recognize an OAuth profile based on request characteristic (params,
      // method, content type etc.), perform validation
      oauthRequest = new OAuthAuthzRequest(request);

      // validate:
      // 1. redirect URI
      // 2. authentication
      // 2. user consent / authorization
      validateRedirectionURI(oauthRequest);

      //build OAuth response
      OAuthResponse resp = OAuthASResponse
        .authorizationResponse(request, HttpServletResponse.SC_FOUND)
        .setCode(oauthIssuer.authorizationCode())
        .location(oauthRequest.getRedirectURI())
        .buildQueryMessage();

      response.sendRedirect(resp.getLocationUri());

      // if something goes wrong
    } catch(OAuthProblemException ex) {
      final OAuthResponse resp;
      try {
        resp = OAuthASResponse
          .errorResponse(HttpServletResponse.SC_FOUND)
          .error(ex)
          .location(oauthRequest.getRedirectURI())
          .buildQueryMessage();
      } catch (OAuthSystemException e) {
        LOGGER.error("auth failed 1", ex);
        throw new RuntimeException(ex);
      }
      response.sendRedirect(resp.getLocationUri());
    } catch (OAuthSystemException ex) {
      LOGGER.error("auth failed 2", ex);
    }

  }

  private void validateRedirectionURI(OAuthAuthzRequest oauthRequest) {
    LOGGER.debug("validateRedirectionURI: "+oauthRequest);
    LOGGER.debug(ReflectionToStringBuilder.toString(oauthRequest));
  }

}
