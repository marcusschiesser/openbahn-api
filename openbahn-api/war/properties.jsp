<%@ page language="java" %>
<%@ page import="java.util.*, java.util.Map.*" %>
<html>
<body>
<table border="1">
<tr><td><b>Property Names</b></td><td><b>Property Values</b></td></tr>
<%
  Set<Entry<Object, Object>>properties = System.getProperties().entrySet();
  for(Entry<Object, Object> property: properties) {
      String pname = property.getKey().toString();
      String pvalue = property.getValue().toString();
%>
<tr>
   <td><%= pname %></td>
   <td><%= pvalue %></td>
</tr>
<% } %>
</table>
</body>
</html>
