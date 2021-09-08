<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="tilesx" uri="http://tiles.apache.org/tags-tiles-extras" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <tilesx:useAttribute id="csslist" name="css" classname="java.util.List"/>
        <c:forEach var="cssfile" items="${csslist}">
            <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}<tiles:insertAttribute value="${cssfile}" flush="true"/>">
        </c:forEach>
        <tilesx:useAttribute id="jslist" name="js" classname="java.util.List"/>
        <c:forEach var="jsfile" items="${jslist}">
            <script type="text/javascript" src="${pageContext.request.contextPath}<tiles:insertAttribute value="${jsfile}" flush="true"/>"></script>
        </c:forEach>
        <title>ODMToolbox</title>
    </head>
    <body <tiles:insertAttribute name="body"/>>
        <div id="page">
            <div id="header">
                <tiles:insertAttribute name="header"/>
            </div>
            <div id="body">
                <div id="content">
                    <tiles:insertAttribute name="content"/>                    
                </div>
            </div>
        </div>
    </body>
</html>

