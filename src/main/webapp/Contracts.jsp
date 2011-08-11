<html lang="de-DE" xml:lang="de-DE">
    <head>
        <%!String currentPage = "Contracts.jsp";%>
        <%@ include file="Resources/CodeFrags/CheckLogin.jspf"%>
        <meta content="no-cache" http-equiv="Cache-Control"/>
        <meta content="no-cache" http-equiv="Pragma"/>
        <title>Timetracker Verträge</title>
        <link href="/favicon.ico" rel="shortcut icon"/>
        <link href="Resources/stylesheet.css" rel="stylesheet" type="text/css"/>
        <script src="Resources/dateLib.js" type="text/javascript"></script>
        <script src="Resources/ContractsFunctions.js" type="text/javascript"></script>		
    </head>
    <body>
        <%@ page import="java.sql.*" %>
        <%@ page import="de.cismet.web.timetracker.*" %>
        
        <%@ include file="WEB-INF/jspf/header.jspf" %>
        <table align="center" class="MainTable" border="0" cellpadding="0" cellspacing="0" summary="" width="100%">
            <tbody>
                <tr style="height: 100%;" valign="top">
                    <td id="main" width="100%">
                        <h1>Verträge</h1>
                        
                        <!--open Database-->
                        <%
                            de.cismet.web.timetracker.Database db = new de.cismet.web.timetracker.Database( application.getRealPath("/").replace('\\','/') );
                            ControlElements ce = new ControlElements(db);
                        %>
                        
                        <%@include file="/WEB-INF/jspf/HandleErrorMsg.jspf" %>
                        <form action="Contracts"  method="POST" onsubmit="return checkform();">
                            <table border="0" cellpadding="0" cellspacing="0" summary="" width="100%">
                                <tbody>
                                    <tr>
                                        <td rowspan="5" align="center">
                                            Benutzer<br/>
                                            <%
                                                out.println(ce.getUserList(request.getParameter("u_id")));
                                            %>
                                        </td>
                                        <td>
                                            <div align="right">
                                                Vertragsdaten
                                            </div>
                                        </td>
                                        <td></td>
                                    </tr>
                                    <tr>
                                        <td>
                                            Stunden/Woche
                                        </td>
                                        <td>
                                            <input maxlength="10" name="whow" id="whowId" size="30" type="text" disabled="true" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            Urlaubstage/Jahr
                                        </td>
                                        <td>
                                            <input maxlength="20" name="ydoh" size="30" id="ydohId" type="text" disabled="true" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            von
                                        </td>
                                        <td>
                                            <input maxlength="20" name="from_date" id="from_dateId" size="30" type="text" disabled="true" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            bis
                                        </td>
                                        <td>
                                            <input maxlength="20" name="to_date" size="30" id="to_dateId" type="text" disabled="true" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="text-align:center;">
                                            <input type="submit" name="button" value="ausw&auml;hlen" title="Benutzer ausw&auml;hlen" />
                                        </td>
                                        <td>
                                        </td>
                                        <td>
                                            <input type="submit" name="button" value="&uuml;bernehmen" title="&Auml;nderungen &uuml;bernehmen" />
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                            <input maxlength="20" name="contractId" id="contractId" size="30" type="hidden"/>						
                        </form>
                        
                        <%
                        if(!db.isConnectionOk()){
                            out.println("Fehler beim Verbinden mit der Datenbank:<br />");
                            out.println(db.getErrorMessage());
                        }
                        
                        
                        //draw contracts-table with current user-id   						 	
                        if(request.getParameter("u_id") != null){       
                            String user = request.getParameter("u_id");
                            StringBuffer sqlQuery = new StringBuffer("SELECT id, from_date, to_date, whow, ydoh FROM tt_contracts WHERE u_id = ");
                            sqlQuery.append(user); 
                            sqlQuery.append(" ORDER BY from_date"); 
                            ResultSet rSet = db.execute(sqlQuery.toString());
                        %>
                        <table border="1" cellpadding="0" cellspacing="0" summary="" width="100%">
                            <thead>
                                <tr>
                                    <th>
                                        <button onclick='rowSelect("new", "","" ,"","")' type="button">
                                            *<!--<img src="resources/arrow.gif" width="27" height="27" border="0" alt="">-->
                                        </button>
                                    </th>
                                    <th>von</th>
                                    <th>bis</th>
                                    <th>Stunden/Woche</th>
                                    <th>Urlaubstage/Jahr</th>
                                </tr>
                            </thead><tbody>
                                <%	
                                try{
                                    while(rSet.next()){
                                        out.println("<tr>" +
                                                    "<td>" +
                                                    "<button onclick='rowSelect(\"" + rSet.getString("id") + "\", \"" +
                                                    rSet.getString(2) + "\", \"" +
                                                    rSet.getString(3) + "\", \"" +
                                                    rSet.getString(4) + "\", \"" +
                                                    rSet.getString(5) + "\")' type=\"button\">" + 
                                                    "<img src=\"Resources/arrow.gif\" width=\"13\" height=\"13\" border=\"0\" alt=\"\">" +
                                                    "</button>" +
                                                    "</td><td>" + rSet.getString("from_date") + 
                                                    "</td><td>" + rSet.getString("to_date") +
                                                    "</td><td>" + rSet.getString("whow") +
                                                    "</td><td>" + rSet.getString("ydoh") + "</td></tr>");
                                    }
                                } catch(SQLException e) {
                                    out.println("Fehler beim Aktualisieren");
                                    System.err.println(e.getMessage());
                                }
                                %>
                        </tbody></table>
                        <%                       
                        }
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


