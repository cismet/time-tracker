<html lang="de-DE" xml:lang="de-DE">
    <head>
        <%!String currentPage = "User.jsp";%>
        <%@ include file="Resources/CodeFrags/CheckLogin.jspf"%>
        <meta content="no-cache" http-equiv="Cache-Control"/>
        <meta content="no-cache" http-equiv="Pragma"/>
        <title>Timetracker Benutzer</title>
        <link href="/favicon.ico" rel="shortcut icon"/>
        <link href="Resources/stylesheet.css" rel="stylesheet" type="text/css"/>
        <script src="Resources/UserFunctions.js" type="text/javascript"></script>		
    </head>
    <body>
        <%@ page import="java.sql.*" %>
        <%@ page import="de.cismet.web.timetracker.*" %>
        <%@ include file="WEB-INF/jspf/header.jspf" %>

        <table align="center" class="MainTable" border="0" cellpadding="0" cellspacing="0" summary="" width="100%">
            <tbody>
                <tr style="height: 100%;" valign="top">
                    <td id="main" width="100%">
                        <h1>Benutzer</h1>
                        
                        <!--open Database-->
                        <%
                        de.cismet.web.timetracker.Database db =
                                new de.cismet.web.timetracker.Database(application.getRealPath("/").replace('\\','/'));
                        ControlElements ce = new ControlElements(db);
                        %>
                        
                        <%
                        if(!db.isConnectionOk()){
                            out.println("Fehler beim Verbinden mit der Datenbank:<br />");
                            out.println(db.getErrorMessage());
                        }
                        %> 
                        <%@include file="/WEB-INF/jspf/HandleErrorMsg.jspf" %>
                        <form method="post" action="Users" id="form1" onsubmit="return checkform()">
                            <table border="0" cellpadding="0" cellspacing="0" summary="" width="100%">
                                <tbody>
                                    <tr>
                                        <td rowspan="5"></td>
                                        <td rowspan="5" align="left">
                                            Benutzer<br/>
                                            <%
                                                out.println(ce.getUserList(request.getParameter("u_id")));
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
                                            Passwort
                                        </td>
                                        <td>
                                            <input maxlength="30" name="pwd" id="pwdId" size="20" type="password" disabled="true" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            Passwort Wiederholung
                                        </td>
                                        <td>
                                            <input maxlength="30" name="pwdWh" id="pwdWhId" size="20" type="password" disabled="true" >
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            Buddyname
                                        </td>
                                        <td>
                                            <input maxlength="30" name="buddyname" size="20" id="buddynameId" type="text" disabled="true" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <br />
                                            Firma
                                        </td>
                                        <td>
                                            <input maxlength="30" name="company" id="companyId" size="20" type="text" disabled="true" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="text-align:right;">
                                            <button type="button" title="neu" onclick='newData()'>*</button>
                                        </td>
                                        <td style="text-align:left;">
                                            <input type="button" value="ausw&auml;hlen" title="Benutzer ausw&auml;hlen" onclick='userSelect()'/>
                                        </td>
                                        <td>
                                            <br />
                                            Exakte Urlaubsberechnung
                                        </td>
                                        <td>
                                            <input maxlength="30" name="exactHolidays" id="exactHolidaysId" type="checkbox" value="ja" disabled="true" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                        </td>
                                        <td>
                                        </td>
                                        <td>
                                            <br />
                                            netto Arbeitszeit
                                        </td>
                                        <td>
                                            <input maxlength="30" name="netHoursOfWork" id="netHoursOfWorkId" type="checkbox" value="ja" disabled="true" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td></td>
                                        <td></td>
                                        <td></td>
                                        <td>
                                            <br />
                                           <input type="submit" name="submitButton" value="&uuml;bernehmen" title="&Auml;nderungen &uuml;bernehmen" />
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                            <input maxlength="20" name="userId" id="userId" size="30" type="hidden"/>						
                        </form>
                        <% 
                        //Erzeuge versteckte inputfelder, die alle Daten der Benutzer-Tabelle enthalten
                        out.print(ce.getUserHiddenInputs());
                        %>
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


