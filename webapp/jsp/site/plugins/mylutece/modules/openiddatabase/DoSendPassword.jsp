<jsp:useBean id="myluteceDatabaseApp" scope="request" class="fr.paris.lutece.plugins.mylutece.modules.openiddatabase.authentication.web.MyLuteceOpenIdDatabaseApp" />
<jsp:include page="../../PortalHeader.jsp" />

<%
	response.sendRedirect( myluteceDatabaseApp.doSendPassword( request ) );
%>
