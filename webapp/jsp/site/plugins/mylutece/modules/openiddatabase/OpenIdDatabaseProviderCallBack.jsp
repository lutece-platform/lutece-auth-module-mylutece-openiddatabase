<jsp:useBean id="myluteceDatabaseApp" scope="application" class="fr.paris.lutece.plugins.mylutece.modules.openiddatabase.authentication.BaseAuthentication" />

<%
    response.sendRedirect( myluteceDatabaseApp.verifyResponse( request ));
%>
