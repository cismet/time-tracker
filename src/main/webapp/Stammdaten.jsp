<html lang="de-DE" xml:lang="de-DE">
    <head>
        <%String currentPage = "Stammdaten.jsp";%>
        <%@ include file="Resources/CodeFrags/CheckLogin.jspf"%>
        <meta content="no-cache" http-equiv="Cache-Control"/>
        <meta content="no-cache" http-equiv="Pragma"/>
        <title>Timetracker Stammdatenpflege</title>
        <link href="/favicon.ico" rel="shortcut icon"/>
        <link href="Resources/stylesheet.css" rel="stylesheet" type="text/css"/>
        <script src="Resources/ActionFunctions.js" type="text/javascript"></script>		
    </head>
    <body>
        <%@ page import="java.sql.*" %>
        <%@ page import="de.cismet.web.timetracker.*" %>
        <%@ include file="WEB-INF/jspf/header.jspf" %>
        
        <table align="center" class="MainTable" border="0" cellpadding="0" cellspacing="0" summary="" width="100%">
            <tbody>
                <tr style="height: 100%;" valign="top">
                    <td id="main" width="100%">
                        
                        
                        <h1>Stammdatenpflege</h1>
                        
                        <p>W&auml;hlen Sie bitte eine Tabelle zum Bearbeiten aus.</p>
                    </td>
                </tr>
            </tbody>
        </table>
        <%@ include file="WEB-INF/jspf/footer.jspf" %>
    </body>
</html>
