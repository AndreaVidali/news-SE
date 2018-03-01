<%--
  Created by IntelliJ IDEA.
  User: kivid
  Date: 01/03/18
  Time: 03:43
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>hello</title>
</head>
<body>
<%
    int a = 3;
    int b = 5;
    int c =  a + b;
    out.print("c="+c);
%>

<h1>Welcome to jsp</h1>
<hr />
<form action="MyServlet">
    <input type="submit" value="Send" />
</form>


</body>
</html>
