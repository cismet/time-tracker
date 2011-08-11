<html lang="de-DE" xml:lang="de-DE">
    <head>
        <%String currentPage = "Queries.jsp";%>
        <%@ include file="Resources/CodeFrags/CheckLogin.jspf"%>
        <meta content="no-cache" http-equiv="Cache-Control"/>
        <meta content="no-cache" http-equiv="Pragma"/>
        <title>Timetracker Auswertungen</title>
        <link href="/favicon.ico" rel="shortcut icon"/>
        <link href="Resources/stylesheet.css" rel="stylesheet" type="text/css"/>
        <script src="Resources/QueryFunctions.js" type="text/javascript"></script>
    </head>
    <body>
        <%@ page import="java.sql.*" %>
        <%@ page import="java.util.GregorianCalendar" %>
        <%@ page import="java.util.Hashtable" %>
        <%@ page import="java.util.Enumeration" %>
        <%@ page import="de.cismet.web.timetracker.*" %>
        <%@ page import="de.cismet.web.timetracker.types.*" %>

        <%@ include file="WEB-INF/jspf/header.jspf" %>

        <table align="center" class="MainTable" border="0" cellpadding="0" cellspacing="0" summary="" width="100%">
            <tbody>
                <tr style="height: 100%;" valign="top">
                    <td id="main" width="100%">
                        <h1>Auswertungen</h1>

                        <!--open Database-->
                        <%
                        de.cismet.web.timetracker.Database db = new de.cismet.web.timetracker.Database(application.getRealPath("/").replace('\\','/'));
                        ControlElements ce = new ControlElements(db);
                        de.cismet.web.timetracker.Query queryObject = new de.cismet.web.timetracker.Query(db);

                        //user, query und year bestimmen
                        String query = "";
                        String strUser;
                        int user = 0;
                        int selectedYear = (new GregorianCalendar()).get(GregorianCalendar.YEAR);
                        if(request.getParameter("u_id") != null){
                            query = request.getParameter("query");
                            strUser = request.getParameter("u_id");
                            boolean callByName = false;
                            //Nutzer-ID bestimmen
                            try{
                                user = Integer.parseInt(strUser);
                            }catch(Exception e){
                                callByName = true;
                            }
                            if(callByName){
                                user = queryObject.getIdByBuddyName(request.getParameter("u_id"));
                            }
                            try {
                                selectedYear = Integer.parseInt(request.getParameter("year"));
                            } catch (Exception e) {
                            }
                        }
                        %>


                        <form action="Queries.jsp" method="GET" onsubmit="return checkform();">
                            <table border="0" cellpadding="0" cellspacing="0" summary="" width="100%">
                                <tbody>
                                    <tr>
                                        <td width="50%">
                                            Benutzer<br/>
                                            <%
                                            out.println(ce.getUserCombo("" + user, "onchange=\"this.form.submit()\""));
                                            %>
                                        </td>
                                        <td width="20%">
                                                Auswertung<br/>
                                                <select name="query" id="queryId" size="1" onchange="this.form.submit()">
                                                    <option selected> </option>
                                                    <%
                                                    String titles[] = {"Projekte", "Urlaubs&uuml;bersicht", "Monats&uuml;bersicht", "&Uuml;bersicht allgemein"};
                                                    String values[] = {"projects", "holiday", "month overview" ,"overview"};
                                                    for(int i = 1; i <= titles.length; ++i){
                                                        out.print("<option");
                                                        if(request.getParameter("query") != null && request.getParameter("query").equals(values[i-1])){
                                                            out.print(" selected");
                                                        }
                                                        out.println(" value=\""+ values[i-1] +"\">" + titles[i-1] + "</option>");
                                                    }
                                                    %>
                                                </select>
                                        </td>
                                        <td width="30%">
                                                Jahr<br/>

                                                <%
                                                out.println("<select name=\"year\" id=\"yearId\" size=\"1\"  onchange=\"this.form.submit()\">");
                                                GregorianCalendar thisYear = new GregorianCalendar();
                                                for(int i = thisYear.get(GregorianCalendar.YEAR); i >= 2006;--i){
                                                    out.print("<option");
                                                    if (i == selectedYear) {
                                                        out.print(" selected");
                                                    }
                                                    out.print(">" + i + "</option>");
                                                }
                                                out.println("</select>");
                                                %>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td colspan="3"><hr /></td>
                                    </tr>
                                    <tr>
                                        <td colspan="3">
                                            <%
                                            if(!db.isConnectionOk()){
                                                out.println("Fehler beim Verbinden mit der Datenbank:<br />");
                                                out.println(db.getErrorMessage());
                                            }

                                            try{
                                                if(request.getParameter("u_id") != null) {
                                                    //Tagesarbeitszeit
                                                    HoursOfWork how = new HoursOfWork();
                                                    queryObject.getHoursOfWork(user, null, how);
                                                    out.println("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" summary=\"\" width=\"100%\">");
                                                    out.println("<tr><td style=\"width: 70%;\">Tagesarbeitszeit: " + TimeTrackerFunctions.convert2Time(how.getHours(), false) + " Stunden<br />");

                                                    //Wochenarbeitszeit
                                                    out.println("Wochenarbeitszeit: " + TimeTrackerFunctions.convert2Time(queryObject.getHoursOfWorkInWeek(user), false) + " Stunden <br />");

                                                    //Monatsarbeitszeit
                                                    out.println("Monatsarbeitszeit: " + TimeTrackerFunctions.convert2Time(queryObject.getHoursOfWorkInMonth(user), false) + " Stunden <br />");
                                                    out.println("Kontostand: " + queryObject.getCurrentAccountBalance(user) + " Stunden");

                                                    //Krankheitstage
                                                    out.println("</td><td style=\"width: 30%;\">Krankheitstage: " + queryObject.getIllnessDays(user));

                                                    out.println("<br />");

                                                    //Urlaubstage
                                                    HolidayStruct hol = new HolidayStruct();

                                                    queryObject.getHoliday(user, new GregorianCalendar(), hol);

                                                    out.println("gesamter Urlaub/Jahr: " + hol.getHolidayPerYear() + "<br />");

                                                    out.println("verbrauchte Urlaubstage: " + hol.getHolidaysThisYear() + "<br />");
                                                    out.println("Resturlaub: " + hol.getResidualLeaveFromThisYear() + " Tage");
                                                    if(hol.getResidualLeaveFromLastYear() != 0.0){
                                                        out.println("<br />davon vom letzten Jahr: " + hol.getResidualLeaveFromLastYear() + " Tage");
                                                    }
                                                    out.println("</td></tr></table><br />");

                                                    if(request.getParameter("query") != null) {
                                                        if(query.equals("holiday")) {
                                                            //Urlaubs�bersicht
                                                            out.println(queryObject.getHolidayTable(user, selectedYear));
                                                            out.println("<br /><div style=\"font-size:80%\"> U = Urlaub, F = Feiertag, H = halber Feiertag </div><br />");
                                                        } else if(query.equals("overview")) {
                                                            //�bersicht allgemein
                                                            out.println(queryObject.getYearTable(user, selectedYear));
                                                            out.println("<br /><div style=\"font-size:80%\"> U = Urlaub, K = Fehltag wegen Krankheit, F = Feiertag, H = halber Feiertag, Zahl = Tagesarbeitszeit, unbek. = Arbeitszeit konnte nicht ermittelt werden</div><br />");
                                                        } else if(query.equals("month overview")) {
                                                            //�bersicht allgemein
                                                            out.println(queryObject.getMonthTable(user, selectedYear));
                                                        } else if(query.equals("projects")) {
                                                            //Projekt�bersicht
                                                            GregorianCalendar time = new GregorianCalendar();
                                                            if (time.get(GregorianCalendar.YEAR) > selectedYear ) {
                                                                time = new GregorianCalendar(selectedYear, GregorianCalendar.DECEMBER, 31);
                                                            }

                                                            Hashtable projectTimesYear = queryObject.getProjectTimes(user, time, false);
                                                            Enumeration enumYear = projectTimesYear.keys();
                                                            Hashtable projectTimesMonth = queryObject.getProjectTimes(user, time, true);

                                                            out.println("<table border=\"5\" cellpadding=\"0\" cellspacing=\"5\" summary=\"\" width=\"100%\">");
                                                            out.print( "<tr><th>Projekt</th><th>Monat: " + TimeTrackerFunctions.toNameOfMonth(time.get(GregorianCalendar.MONTH)) + " " + time.get(GregorianCalendar.YEAR) );
                                                            out.println( "</th><th>Jahr: " + time.get(GregorianCalendar.YEAR) + "</th></tr>" );

                                                            while(enumYear.hasMoreElements()) {
                                                                String project = (String)enumYear.nextElement();
                                                                Double timeYear = (Double)projectTimesYear.get(project);
                                                                Double timeMonth = (Double)projectTimesMonth.get(project);
                                                                String stringTimeMonth;

                                                                if (timeMonth != null) {
                                                                    stringTimeMonth = TimeTrackerFunctions.convert2Time(timeMonth.doubleValue(), false);
                                                                } else{
                                                                    stringTimeMonth = "-";
                                                                }

                                                                out.println("<tr align=\"center\"><td>" + project + "</td><td>" +
                                                                stringTimeMonth  + "</td><td>" +
                                                                TimeTrackerFunctions.convert2Time(timeYear.doubleValue(), false) + "</td></tr>");
                                                            }
                                                            out.println("</table>");
                                                        }
                                                    }
                                                }
                                            } catch(NumberFormatException e) {
                                                System.err.println(e.getMessage());
                                                e.printStackTrace();
                                            } catch(Exception e) {
                                                System.err.println(e.getMessage());
                                                e.printStackTrace();
                                            } finally {
                                                db.close();
                                            }
                                            %>
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </form>
                    </td>
                </tr>
            </tbody>
        </table>
        <%@ include file="WEB-INF/jspf/footer.jspf" %>
    </body>
</html>


