<%@ page errorPage="../../../../ErrorPage.jsp" %>
<jsp:include page="../../../../AdminHeader.jsp" />

<jsp:useBean id="openidDatabaseUser" scope="session" class="fr.paris.lutece.plugins.mylutece.modules.openiddatabase.authentication.web.OpenIdDatabaseJspBean" />

<% 	openidDatabaseUser.init( request, openidDatabaseUser.RIGHT_MANAGE_DATABASE_USERS ); %>
<%= openidDatabaseUser.getManageUsers( request ) %>

<%@ include file="../../../../AdminFooter.jsp" %>
