<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // Redirect to dashboard (AuthenticationFilter will redirect to /login if unauthenticated)
    response.sendRedirect(request.getContextPath() + "/dashboard");
%>
