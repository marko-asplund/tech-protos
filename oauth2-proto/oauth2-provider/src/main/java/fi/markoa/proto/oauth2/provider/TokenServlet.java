package fi.markoa.proto.oauth2.provider;

import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuer;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthTokenRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name="tokenServlet", urlPatterns={"/token"})
public class TokenServlet extends HttpServlet {
  private static final Logger LOGGER = LoggerFactory.getLogger(TokenServlet.class);

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    LOGGER.debug("doPost");

    OAuthTokenRequest oauthRequest;

    OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());

    try {
      oauthRequest = new OAuthTokenRequest(request);

      validateClient(oauthRequest);

      String authzCode = oauthRequest.getCode();
      LOGGER.debug("code: "+authzCode);

      // some code

      String accessToken = oauthIssuerImpl.accessToken();
      String refreshToken = oauthIssuerImpl.refreshToken();

      // some code


      OAuthResponse r = OAuthASResponse
        .tokenResponse(HttpServletResponse.SC_OK)
        .setAccessToken(accessToken)
        .setExpiresIn("3600")
        .setRefreshToken(refreshToken)
        .buildJSONMessage();

      response.setStatus(r.getResponseStatus());
      PrintWriter pw = response.getWriter();
      pw.print(r.getBody());
      pw.flush();
      pw.close();
      LOGGER.debug("access token / success");

      //if something goes wrong
    } catch(OAuthProblemException ex) {
      LOGGER.error("access token / error", ex);

      OAuthResponse r;
      try {
        r = OAuthResponse
          .errorResponse(401)
          .error(ex)
          .buildJSONMessage();
      } catch (OAuthSystemException e) {
        LOGGER.error("failed to build response", ex);
        throw new RuntimeException("failed to build response", ex);
      }

      response.setStatus(r.getResponseStatus());

      PrintWriter pw = response.getWriter();
      pw.print(r.getBody());
      pw.flush();
      pw.close();

      response.sendError(401);
    } catch (OAuthSystemException ex) {
      LOGGER.error("oauthsystemexception failure", ex);
    }

  }

  private void validateClient(OAuthTokenRequest oauthRequest) {
    LOGGER.debug("validateClient: "+oauthRequest);
  }


}