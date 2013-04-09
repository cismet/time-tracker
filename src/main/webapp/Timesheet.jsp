<%@ page import="java.sql.*" %>
<%@ page import="de.cismet.web.timetracker.*" %>
<%@ page import="java.util.*" %>
<%@ page import="de.cismet.web.timetracker.TimeTrackerFunctions" %>

<html lang="de-DE" xml:lang="de-DE">
    <head>
        <%!String currentPage = "Timesheet.jsp";%>
        <%@ include file="Resources/CodeFrags/CheckLogin.jspf"%>
        <meta content="no-cache" http-equiv="Cache-Control"/>
        <meta content="no-cache" http-equiv="Pragma"/>
        <title>Timetracker Timesheet</title>
        <link href="/favicon.ico" rel="shortcut icon"/>
        <link href="Resources/stylesheet.css" rel="stylesheet" type="text/css"/>
        <script src="Resources/BrowserRecognition.js" type="text/javascript"></script>
        <script src="Resources/TimesheetFunctions.js" type="text/javascript"></script>

        <style type="text/css">@import url(Resources/jscalendar-1.0/calendar-win2k-1.css);</style>
        <script type="text/javascript" src="Resources/jscalendar-1.0/calendar.js"></script>
        <script type="text/javascript" src="Resources/jscalendar-1.0/lang/calendar-en.js"></script>
        <script type="text/javascript" src="Resources/jscalendar-1.0/calendar-setup.js"></script>

    </head>
    <body>

        <%@ include file="WEB-INF/jspf/header.jspf" %>
        <table align="center" class="MainTable" border="0" cellpadding="0" cellspacing="0" summary="" width="100%">
            <tbody>
                <tr style="height: 100%;" valign="top">
                    <td id="main" width="100%">
                        <h1>Timesheet</h1>

                            <!--open Database-->
                            <%
                                de.cismet.web.timetracker.Database db = new de.cismet.web.timetracker.Database(application.getRealPath("/").replace('\\','/'));
                                ControlElements ce = new ControlElements(db);

                                if(!db.isConnectionOk()){
                                        out.println("Fehler beim Verbinden mit der Datenbank:<br />");
                                        out.println(db.getErrorMessage());
                                }


                                //Variablen füllen
                                String user = (request.getParameter("u_id") == null ? "" : request.getParameter("u_id"));
                                int from = 0;
                                int till = 0;
                                int year = (new GregorianCalendar()).get(GregorianCalendar.YEAR);
                                //KWvon, KWbis, und jahr zu int-Werten konvertieren
                                String strFrom = request.getParameter("KWvon");
                                String strTill = request.getParameter("KWbis");
                                String strYear = request.getParameter("jahr");

                                //aktuelle Kalenderwoche bestimmen
                                int currentWeek = TimeTrackerFunctions.getCurrentCW();

                                from = TimeTrackerFunctions.parseStringToInt(strFrom, currentWeek);
                                till = TimeTrackerFunctions.parseStringToInt(strTill, currentWeek);
                                year = TimeTrackerFunctions.parseStringToInt(strYear, -1);

                            %>

                            <%@include file="/WEB-INF/jspf/HandleErrorMsg.jspf" %>
                            <form method="post" action="Timesheet" id="form1" >
                                <table border="0" cellpadding="0" cellspacing="0" summary="" width="100%">
                                    <tbody>
                                        <tr>
                                            <td>von KW</td>
                                            <td>
                                                    <%
                                                        out.println("<select name=\"KWvon\" id=\"KWvonId\" size=\"1\">");
                                                        out.print("<option />");

                                                        for (int i = 53; i >= 0;--i){
                                                            if (from == i) {
                                                                out.print("<option selected>");
                                                            } else {
                                                                out.print("<option>");
                                                            }
                                                            out.println(i + "</option>");
                                                        }
                                                        out.println("</select>");
                                                    %>
                                            </td>
                                            <td>
                                                    <input type="submit" value="Tabelle zeigen" onmouseover='setActionText("auswaehlen")'/>
                                            </td>
                                            <td>bis KW</td>
                                            <td>
                                                    <%
                                                        out.println("<select name=\"KWbis\" id=\"KWbisId\" size=\"1\">");
                                                        out.println("<option />");

                                                        for (int i = 53; i >= 0;--i) {
                                                            if (till == i) {
                                                                out.print("<option selected>");
                                                            } else {
                                                                out.print("<option>");
                                                            }
                                                            out.println(i + "</option>");
                                                        }
                                                        out.println("</select>");
                                                    %>
                                            </td>
                                    </tr>
                                    <tr>
                                            <td>Jahr</td>
                                            <td>
                                                    <%
                                                        out.println("<select name=\"jahr\" id=\"jahrId\" size=\"1\">");
                                                        for(int i = (new GregorianCalendar()).get(GregorianCalendar.YEAR); i >= 2006;--i){
                                                            if (year == i) {
                                                                out.print("<option selected>");
                                                            } else {
                                                                out.print("<option>");
                                                            }
                                                            out.println(i + "</option>");
                                                        }
                                                        out.println("</select>");
                                                    %>
                                            </td>
                                            <td>
                                                    <input type="submit" value="Änderungen speichern" onmouseover='setActionText("uebernehmen")'/>
                                            </td>
                                            <td>Benutzer</td>
                                            <td>
                                                    <%
                                                    //	out.println(ce.getUserCombo(request.getParameter("u_id")));
                                                        out.println(ce.getUserCombo(request.getParameter("u_id"), "onchange=\"show()\""));
                                                    %>
                                            </td>
                                    </tr>
                                    <!--tr>
                                        <td style="text-align:left" colspan="2">
                                                <input type="submit" value="Änderungen speichern" onmouseover='setActionText("uebernehmen")'/>
                                        </td>
                                        <td style="text-align:left" colspan="2">
                                                <input type="submit" value="Tabelle zeigen" onmouseover='setActionText("auswaehlen")'/>
                                        </td>
                                    </tr-->
                                    </tbody>
                                </table>

                                <input maxlength="20" name="act" id="actId" size="30" type="hidden"/>
                                <input maxlength="20" name="row" id="rowId" size="30" type="hidden"/>
                                <input maxlength="1000" name="mods" id="modId" size="30" type="hidden"/>

                                <!--userHidden wird benötigt, falls "Änderungen speichern" gedrückt wird und zuvor
                                        user-Combobox auf einen anderen User gesetzt wurde-->
                                <input maxlength="20" name="userHidden" id="userHiddenId" size="30" type="hidden"
                                <%
                                    if(user != null){
                                        out.print("value=\"" + user + "\"");
                                    }
                                %>
                                />
                            </form>

                            <%
                                    if(user != null && !user.equals("")){
                                            //Zeichne Ausschnitt aus der Timesheet-Tabelle
                                            //Daten konvertieren
                                            if(from == -1 && till == -1){
                                                    GregorianCalendar gc = new GregorianCalendar();
                                                    gc.setFirstDayOfWeek(GregorianCalendar.MONDAY);
                                                    gc.setMinimalDaysInFirstWeek(4);
                                                    from = gc.get(GregorianCalendar.WEEK_OF_YEAR);
                                                    till = from;
                                            }

                                            if (year == -1){
                                                    year = (new GregorianCalendar()).get(GregorianCalendar.YEAR);
                                            }

                                            GregorianCalendar dateFrom = new GregorianCalendar(year,0,1,0,0,0);
                                            GregorianCalendar dateTill = new GregorianCalendar(year,0,1,23,59,59);

                                            dateFrom.setMinimalDaysInFirstWeek(4);
                                            dateFrom.setFirstDayOfWeek(GregorianCalendar.MONDAY);
                                            dateFrom.set(GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.MONDAY);
                                            dateFrom.set(GregorianCalendar.WEEK_OF_YEAR, from);

                                            dateTill.setMinimalDaysInFirstWeek(4);
                                            dateTill.setFirstDayOfWeek(GregorianCalendar.MONDAY);
                                            dateTill.set(GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.SUNDAY);
                                            dateTill.set(GregorianCalendar.WEEK_OF_YEAR, till);

                                            String stringFrom = TimeTrackerConstants.formater.format( dateFrom.getTime() );
                                            String stringTill = TimeTrackerConstants.formater.format( dateTill.getTime() );

                                            //SQL-Abfrage konstruieren
                                            StringBuffer sqlQuery = new StringBuffer("SELECT ts.oid, action, description, annotation, manual, ts.project_id, title, duration_in_hours, time " +
                                                                                                                             "FROM tt_timesheet ts LEFT OUTER JOIN tt_projects p ON (ts.project_id = p.id), tt_timesheet_action ta " +
                                                                                                                             "WHERE ts.action = ta.id ");
                                            sqlQuery.append(" AND ts.u_id = " + user);

                                            if(from != -1){
                                                    sqlQuery.append(" AND time > '" + stringFrom + "'");
                                            }
                                            if(till != -1){
                                                    sqlQuery.append(" AND time < '" + stringTill + "'");
                                            }

                                            sqlQuery.append(" ORDER BY time DESC");

                                            //out.println("\nQuery: " + sqlQuery.toString());
                                            ResultSet rSet = db.execute(sqlQuery.toString());
                                            if(rSet != null){
                                                    GregorianCalendar calendarInitDate = new GregorianCalendar();
                                                    calendarInitDate.setFirstDayOfWeek(GregorianCalendar.MONDAY);
                                                    calendarInitDate.setMinimalDaysInFirstWeek(4);

                                                    if(! (calendarInitDate.get(GregorianCalendar.WEEK_OF_YEAR) == dateTill.get(GregorianCalendar.WEEK_OF_YEAR))){
                                                            calendarInitDate = dateTill;
                                                    }

                                                    if (year == -1){
                                                            year = calendarInitDate.get(GregorianCalendar.YEAR);
                                                    }
                                    %>
                                    <table border="1" id="tsTable" cellpadding="0" cellspacing="0" summary="" width="100%">
                                            <thead>
                                                <tr>
                                                    <th width="4%">
                                                            <button onclick='newSelect(<%=year%>, <%=calendarInitDate.get(GregorianCalendar.MONTH) + 1%>,
                                                                                                                     <%=calendarInitDate.get(GregorianCalendar.DATE)%>)' type="button">
                                                                     *
                                                            </button>
                                                    </th>
                                                    <th width="19%">Zeit</th>
                                                    <th width='25%'>Aktion</th>
                                                    <th width='17%'>Kommentar</th>
                                                    <th width='25%'>Projektname</th>
                                                    <th width='10%'>Dauer in Stunden</th>
                                                </tr>
                                            </thead><tbody id='tsTbody'>
                                    <%
                                            while(rSet.next()){
                                    %>
                                            <tr id="<%=rSet.getString(1)%>">
                                                <td>
                                                    <button onclick="rowDelete('<%=rSet.getString(1)%>')" type="button">X</button>
                                                </td>
                                                <td id ="time<%=rSet.getString(1)%>"><%=rSet.getString(9).substring(0, 19)%></td>
                                                <td id ="action<%=rSet.getString(1)%>" onclick="actionSelect('<%=rSet.getString(1)%>')" ><%=rSet.getString(3)%></td>
                                                <td id ="annotation<%=rSet.getString(1)%>" onclick="annotationSelect('<%=rSet.getString(1)%>')"><%=(rSet.getString(4) == null || rSet.getString(4) == "" ? "&nbsp;" : rSet.getString(4))%></td>
                                                <td id ="project<%=rSet.getString(1)%>" onclick="projectSelect('<%=rSet.getString(1)%>')"><%=(rSet.getString(7) == null || rSet.getString(7) == "" ? "&nbsp;" : rSet.getString(7))%></td>
                                                <td id ="duration<%=rSet.getString(1)%>" onclick="durationSelect('<%=rSet.getString(1)%>')"><%=(rSet.getString(8) == null || rSet.getString(8) == "" ? "&nbsp;" : rSet.getString(8))%></td>
                                            </tr>
                                    <%
                                            }
                                    %>
                                            </tbody></table>
                                    <%
                                                    }else{
                                                            out.println("Fehler bei der Datenbankabfrage");
                                                    }
                                            }
                                    %>

                                    <%
                                            //erzeuge versteckte Inputfelder mit den Aktions- und Projektnamen
                                            out.println(ce.getActionNamesHidden());
                                            out.println(ce.getProjectTitleHidden());
                                    %>
                    </td>
                </tr>
            </tbody>
        </table>
        <%@ include file="WEB-INF/jspf/footer.jspf" %>
        <script type="text/javascript">
            initCalendar();
        </script>
       <%db.close();%>
    </body>
</html>
