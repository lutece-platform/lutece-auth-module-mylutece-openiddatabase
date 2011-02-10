<%@ page errorPage="../../../../ErrorPage.jsp" %>

<jsp:useBean id="openidDatabaseUser" scope="session" class="fr.paris.lutece.plugins.mylutece.modules.openiddatabase.authentication.web.OpenIdDatabaseJspBean" />

<% 
	openidDatabaseUser.init( request, openidDatabaseUser.RIGHT_MANAGE_DATABASE_USERS );
    response.sendRedirect( openidDatabaseUser.getRemoveUser( request ) );
%>
