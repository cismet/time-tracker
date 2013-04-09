<html lang="de-DE" xml:lang="de-DE">
    <head>
        <%String currentPage = "ProjectShortcuts.jsp";%>
        <%@ include file="Resources/CodeFrags/CheckLogin.jspf"%>
        <meta content="no-cache" http-equiv="Cache-Control"/>
        <meta content="no-cache" http-equiv="Pragma"/>
        <title>Timetracker Projektkürzel</title>
        <link href="/favicon.ico" rel="shortcut icon"/>
        <link href="Resources/stylesheet.css" rel="stylesheet" type="text/css"/>
        <script src="Resources/ShortcutFunctions.js" type="text/javascript"></script>		
    </head>
    <body>
        <%@ page import="java.sql.*" %>
        <%@ page import="de.cismet.web.timetracker.*" %>
        
        <%@ include file="WEB-INF/jspf/header.jspf" %>
        <table align="center" class="MainTable" border="0" cellpadding="0" cellspacing="0" summary="" width="100%">
            <tbody>
                <tr style="height: 100%;" valign="top">
                    <td id="main" width="100%">
                        <h1>Projektk&uuml;rzel</h1>
                        
                        <!--open Database-->
                        <%
                            de.cismet.web.timetracker.Database db = new de.cismet.web.timetracker.Database( application.getRealPath("/").replace('\\','/') );
                            ControlElements ce = new ControlElements(db);
                        %>
                        
                        <%
                        if ( !((String)session.getAttribute("role")).equals("admin") ){
                            out.println("</td></tr></tbody></table>");
                        %>
                            <%@ include file="WEB-INF/jspf/footer.jspf" %>
                        <%
                            out.println("</body></html>");
                            out.println("<script language='javascript'>alert('Zugriff verweigert. Für diese Seite werden Admin-Rechte benötigt')</script>");
                            return;
                        }
        
                        if(!db.isConnectionOk()){
                            out.println("Fehler beim Verbinden mit der Datenbank:<br />");
                            out.println(db.getErrorMessage());
                        }
                        %> 
                        
                        <%@include file="/WEB-INF/jspf/HandleErrorMsg.jspf" %>
                        <form action="ProjectShortcuts" method="POST" onsubmit="return checkform();">
                            <table border="0" cellpadding="0" cellspacing="0" summary="" width="100%">
                                <tbody>
                                    <tr>
                                        <td rowspan="3"  style="text-align:center;">
                                                Projekte<br/>
                                                <%
                                                out.println(ce.getProjectList(request.getParameter("p_id")));
                                                %>
                                        </td>
                                        <td>
                                            &nbsp;<br /><br /><br />
                                        </td>
                                        <td rowspan="2">
                                                <%
                                                String project = request.getParameter("p_id");
                                                //draw shortcut-table with current Project-id
                                                if(project != null) {
                                                    StringBuffer sqlQuery = new StringBuffer("SELECT id, shortcut FROM tt_projectshortcuts WHERE projectid = ");
                                                    sqlQuery.append(project);
                                                    ResultSet rSet = db.execute(sqlQuery.toString());
                                                %>
                                                    <table border="1" cellpadding="0" cellspacing="0" summary="" width="200px">
                                                        <thead>
                                                            <tr>
                                                                <th style="width: 10%">&nbsp;</th>
                                                                <th style="width: 10%">
                                                                    <button onclick='rowSelect("new","")' type="button" title="neu">
                                                                        *
                                                                    </button>
                                                                </th>
                                                                <th>Shortcut</th>
                                                            </tr>
                                                        </thead>
                                                        <tbody>
                                                            <%	
                                                            while(rSet.next()) {
                                                                out.println("<tr>" +
                                                                        "<td>" +
                                                                        "<input style=\"width: 25px;\" type=\"submit\" name=\"submitButton\" value=\"X\" onmouseover='setActionText(\"delete\", \"" + rSet.getString(1) + "\")' title=\"l&ouml;schen\"/>" +
                                                                        "</td>" +
                                                                        "<td>" +
                                                                        "<button onclick='rowSelect(\"" + rSet.getString(1) + "\", \"" + rSet.getString(2) + "\")' type=\"button\" title=\"ausw&auml;hlen\">" +
                                                                        "<img src=\"Resources/arrow.gif\" width=\"13\" height=\"13\" border=\"0\" alt=\"\">" +
                                                                        "</button>" +
                                                                        "</td><td>" + rSet.getString(2) + "</td></tr>");
                                                            }
                                                            %>
                                                    </tbody></table>
                                                <%                       
                                                }
                                                %>   
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            &nbsp;<br /><br /><br /><br /><br /><br /><br /><br />
                                        </td>
                                    </tr>
                                    <tr style="vertical-align:bottom;">
                                        <td>
                                            Shortcut
                                        </td>
                                        <td>
                                            <input maxlength="30" name="name" id="nameId" size="20" type="text" disabled="true">
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="text-align:center;">
                                            <input type="submit" name="submitButton" title="Projekt ausw&auml;hlen" value="auswählen" onmouseover='setActionText("auswaehlen", "")'/>
                                        </td>
                                        <td></td>
                                        <td>
                                            <input type="submit" value="übernehmen" title="&Auml;nderungen &uuml;bernehmen" onmouseover='setActionText("uebernehmen", "")'/>
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                            <input maxlength="20" name="act" id="actId" size="30" type="hidden"/>						
                            <input maxlength="20" name="shortcutId" id="shortcutId" size="30" type="hidden"/>						
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


