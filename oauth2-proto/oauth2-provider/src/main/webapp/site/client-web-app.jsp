<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
  <title>my web app</title>
</head>
<body>
<h1>my web app</h1>
<p>
    lorem lorem lipsum
</p>

<c:url var="authUrl" value="http://localhost:8080/oauth2/authenticate.jsp">
    <c:param name="client_id" value="131804060198305"/>
    <c:param name="response_type" value="code"/>
    <c:param name="redirect_uri" value="http://localhost:8080/oauth2/site/snoop.jsp"/>
</c:url>
<p>
    <a href="${authUrl}">log in via oauth2</a>
</p>
</body>
</html>