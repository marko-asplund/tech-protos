<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>authenticate</title>
<body>
<form method="GET" action='/oauth2/authorize' >

    <p>Username:
        <input type="text" name="username">
    </p>

    <p>
        password:
        <input type="password" name="password">
    </p>
    <p>
        <input type="submit" value="Log In">
        <input type="reset">
    </p>

    <input type="hidden" name="response_type" value='<%= request.getParameter("response_type") %>'/>
    <input type="hidden" name="redirect_uri" value='<%= request.getParameter("redirect_uri") %>'/>
    <input type="hidden" name="client_id" value='<%= request.getParameter("client_id") %>'/>

</form>
</body>
</html>