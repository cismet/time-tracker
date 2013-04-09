<html lang="de-DE" xml:lang="de-DE">
    <head>
        <%String currentPage = "Projects.jsp";%>
        <%@ include file="Resources/CodeFrags/CheckLogin.jspf"%>
        <meta content="no-cache" http-equiv="Cache-Control"/>
        <meta content="no-cache" http-equiv="Pragma"/>
        <title>Timetracker Projekte</title>
        <link href="/favicon.ico" rel="shortcut icon"/>
        <link href="Resources/stylesheet.css" rel="stylesheet" type="text/css"/>
        <script src="Resources/ProjectFunctions.js" type="text/javascript"></script>		
    </head>
    <body>
        <%@ page import="java.sql.*" %>
        <%@ page import="de.cismet.web.timetracker.*" %>
        
        <%@ include file="WEB-INF/jspf/header.jspf" %>
        <table align="center" class="MainTable" border="0" cellpadding="0" cellspacing="0" summary="" width="100%">
            <tbody>
                <tr style="height: 100%;" valign="top">
                    <td id="main" width="100%">
                        <h1>Projekte</h1>
                        
                        <!--open Database-->
                        <%
                            de.cismet.web.timetracker.Database db =
                                    new de.cismet.web.timetracker.Database( application.getRealPath("/").replace('\\','/') );
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
                        <form action="Projects" method="POST" onsubmit="return checkform();">
                            <table border="0" cellpadding="0" cellspacing="0" summary="" width="100%">
                                <tbody>
                                    <tr>
                                        <td rowspan="4">
                                        <td rowspan="4" style="text-align:left;">
                                            Projekte<br/>
                                            <%
                                                out.println(ce.getProjectList(request.getParameter("p_id")));
                                            %>
                                        </td>
                                        <td>
                                            Name
                                        </td>
                                        <td>
                                            <input maxlength="30" name="name" id="nameId" size="20" type="text" disabled="true" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            Ist Unterprojekt von
                                        </td>
                                        <td>
                                            <%
                                            out.print(ce.getProjectCombo(request.getParameter("mp_id")));
                                            %>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            Aktiv
                                        </td>
                                        <td>
                                            <input name="aktiv" id="aktivId" type="checkbox" value="ja" disabled="true" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            Projektk&uuml;rzel
                                        </td>
                                        <td>
                                            <input name="shortcut" id="shortcutId" type="text" size="20" disabled="true" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="text-align:right">
                                            <button type="button" onclick='newData()' title="neu">*</button>
                                        </td>
                                        <td style="text-align:left;">
                                            <input type="button" value="ausw&auml;hlen" title="Projekt ausw&auml;hlen" onclick='projectSelect()'/>
                                        </td>
                                        <td></td>
                                        <td>
                                            <input type="submit" value="&uuml;bernehmen" title="&Auml;nderungen &uuml;bernehmen" onmouseover='setActionText("uebernehmen")'/>
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                            <input maxlength="20" name="act" id="actId" size="30" type="hidden"/>						
                            <input maxlength="20" name="row" id="rowId" size="30" type="hidden"/>						
                            <% 
                            //Erzeuge versteckte inputfelder, die alle Daten der user-Tabelle enthalten
                            out.print(ce.getProjectHiddenInputs());
                            %>
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


