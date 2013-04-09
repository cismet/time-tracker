<html lang="de-DE" xml:lang="de-DE">
    <head>
        <%String currentPage = "Actions.jsp";%>
        <%@ include file="Resources/CodeFrags/CheckLogin.jspf"%>
        <meta content="no-cache" http-equiv="Cache-Control"/>
        <meta content="no-cache" http-equiv="Pragma"/>
        <title>Timetracker Aktionen</title>
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
                        <h1>Aktionen</h1>
                        
                        <%
                            if ( !((String)session.getAttribute("role")).equals("admin") ) {
                                out.println("</td></tr></tbody></table>");
                        %>
                                <%@ include file="WEB-INF/jspf/footer.jspf" %>
                        <%
                                out.println("</body></html>");
                                out.println("<script language='javascript'>alert('Zugriff verweigert. Für diese Seite werden Admin-Rechte benötigt')</script>");
                                return;
                            }
                        %>
                        
                        <!--open Database-->
                        <%
                            de.cismet.web.timetracker.Database db = new de.cismet.web.timetracker.Database(application.getRealPath("/").replace('\\','/'));
                        %>
                        
                        <%
                            if(!db.isConnectionOk()){
                                out.println("Fehler beim Verbinden mit der Datenbank:<br />");
                                out.println(db.getErrorMessage());
                            }
                        %> 
                        <%@include file="/WEB-INF/jspf/HandleErrorMsg.jspf" %>
                        <form method="post" action="Actions" id="form1" onsubmit="return checkform()">
                            <table border="0" cellpadding="0" cellspacing="0" summary="" width="100%">
                                <tbody>
                                    <tr>
                                        <td rowspan="3" style="width: 25%">&nbsp;</td>
                                        <td>
                                            Aktionsname<br/>
                                        </td>
                                        <td>
                                            <input maxlength="30" name="name" id="nameId" size="30" type="text" disabled="true" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            Aktionsbeschreibung<br/>
                                        </td>
                                        <td>
                                            <input maxlength="30" name="desc" id="descId" size="30" type="text" disabled="true" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                        </td>
                                        <td>
                                            <input type="submit" name="submitButton" value="&uuml;bernehmen" title="&Auml;nderungen &uuml;bernehmen" />
                                            <br />&nbsp;
                                        </td>
                                    </tr>
                                    <tr>
                                        <td colspan="3">
                                            <%
                                            //draw action-table
                                            StringBuffer sqlQuery = new StringBuffer("SELECT id, actionname, description FROM tt_timesheet_action");
                                            ResultSet rSet = db.execute(sqlQuery.toString());
                                            %>
                                            <table border="1" cellpadding="0" cellspacing="0" summary="" width="100%">
                                                <thead>
                                                    <tr>
                                                        <th>
                                                            <button onclick='rowSelect("new", "","")' type="button" title="neu">
                                                                *
                                                            </button>
                                                        </th>
                                                        <th>Name</th>
                                                        <th>Beschreibung</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <%		
                                                    while(rSet.next()){
                                                        out.println("<tr><td><button title=\"Aktion bearbeiten\" onclick='rowSelect(\"" + rSet.getString("id") + "\", \"" +
                                                            rSet.getString("actionname") + "\", \"" +
                                                            rSet.getString("description") + "\")' type=\"button\">" + 
                                                            "<img src=\"Resources/arrow.gif\" width=\"13\" height=\"13\" border=\"0\" alt=\"\">" +
                                                            "</button> </td><td>" + rSet.getString("actionname") + 
                                                            "</td><td>&nbsp;" + (rSet.getString("description") == null || rSet.getString("description") == "" ? "&nbsp;" : rSet.getString("description"))+ "</td></tr>");
                                                    }
                                                    %>
                                                </tbody>
                                            </table>
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                            <!--speichert die ID der ausgewaehlten Aktion oder new, wenn die Aktion neu angelegt wurde-->
                            <input maxlength="20" name="actionId" id="actionId" size="30" type="hidden"/>						
                        </form>
                    </td>
                </tr>
            </tbody>
        </table>
        <%@ include file="WEB-INF/jspf/footer.jspf" %>
        <script type="text/javascript">
            deActivateInputs(true);
        </script>
        <%db.close();%>
    </body>
</html>


