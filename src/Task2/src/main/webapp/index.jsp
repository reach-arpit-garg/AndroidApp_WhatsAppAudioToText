<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%= request.getAttribute("doctype") %>
<%--
* @author Arpit Garg
--%>

<html>
<head>
    <title>Akshar Analytics</title>
</head>
<body>
<h1><b><%= "Analytics" %></b></h1>
<br/>
<h2><%= "The analysis is as follows:" %></h2>
<h3><%= request.getAttribute("responseTime") %></h3>
<h3><%= request.getAttribute("apiTime") %></h3>
<h3><%= request.getAttribute("phoneBrand") %></h3>
<h3><%= request.getAttribute("phoneZone") %></h3>
<h3><%= request.getAttribute("text") %></h3>
<br/>
<h2><%= "The logs are as follows:" %></h2>
<table width="59%" border="1">
        <% String[] logs = ((String[])request.getAttribute("logs"));
            for(int i = 0; i<logs.length; i++) { %>
        <tr><td>
            <%= logs[i]%>
        </td></tr>
        <%}%>
</table>
</body>
</html>