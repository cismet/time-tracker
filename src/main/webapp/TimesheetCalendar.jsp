<%!String currentPage = "TimesheetCalendar.jsp";%>
<%@ include file="Resources/CodeFrags/CheckLogin.jspf"%>
<%@ page import="java.util.*" %>
<%@ page import="java.text.*" %>
<%@ page import="de.cismet.web.timetracker.*" %>
<%
int rowHeight = 49;
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
<link href="/favicon.ico" rel="shortcut icon"/>
<link href="Resources/stylesheet.css" rel="stylesheet" type="text/css"/>

<title>Timesheet-Einträge</title>

<style type="text/css">
html{
	margin:0px;
	padding:0px;
	height:100%;
	width:100%;
}
 
p,h2{
	margin:2px;
}

h1{
	font-size:1.4em;
	margin:2px;
}
h2{
	font-size:1.3em;
}
.weekButton{
	width:60px;
	height: 25px;
	font-size:11pt;
	font-family:arial;
}
#weekScheduler_container{
	border:1px solid #000;
	width:986px;
        margin-left: 6px;
        margin-right: 6px;
        margin-bottom: 5px;
}

.weekScheduler_appointments_day{	/* Column for each day */
	width:130px;
	float:left;
	background-color: #FFFFD5;
	border-right:1px solid #F6DBA2;
	position:relative;
}
#weekScheduler_top{
	background-color:buttonface;
	height:20px;
	border-bottom:1px solid #ACA899;
}
.calendarContentTime,.spacer{
	background-color:buttonface;
	text-align:center;
	font-family:arial;
	font-size:20px;
	line-height:<% out.print(rowHeight); %>px;
	height:<% out.print(rowHeight); %>px;	/* Height of hour rows */
	border-right:1px solid #ACA899;
	width:50px;
}

.weekScheduler_appointmentHour{	/* Small squares for each hour inside the appointment div */
	height:<% out.print(rowHeight); %>px; /* Height of hour rows */
	border-bottom:1px solid #F6DBA2;
}

.spacer{
	height:20px;
	float:left;
}

#weekScheduler_hours{
	width:50px;
	float:left;
}
.calendarContentTime{
	border-bottom:1px solid #ACA899;

}

#weekScheduler_appointments{	/* Big div for appointments */
	width:917px;
	float:left;
}
.calendarContentTime .content_hour{
	font-size:10px;
	vertical-align: super;
	line-height:<% out.print(rowHeight); %>px;
}

#weekScheduler_top{
	position:relative;
	clear:both;
}
#weekScheduler_content{
	clear:both;
	height:550px;
	position:relative;
	overflow:auto;
}
.days div{
	width:130px;
	float:left;
	background-color:buttonface;
	text-align:center;
	font-family:arial;
	height:20px;
	font-size:0.9em;
	line-height:20px;
	border-right:1px solid #ACA899;
}

.weekScheduler_anAppointment{	/* A new appointment */
	position:absolute;
	background-color:#FFF;
	border:1px solid #000;
	z-index:1000;
	overflow:hidden;


}

.weekScheduler_appointment_header{	/* Appointment header row */
	height:4px;
	background-color:#ACA899;
}

.weekScheduler_appointment_rightBar{
	position:absolute;
	right:-1px;
	border-right:1px solid #000;
	width:2px;
	height:100%;
	font-size:0.8em;
	z-index:12000;
	background-color:#000;
}


.weekScheduler_appointment_headerActive{ /* Appointment header row  - when active*/
	height:4px;
	background-color:#00F;
}

.weekScheduler_appointment_txt{
	position:absolute;
	left:0px;
	top:5px;
	width:116px;
	height:12px;
	font-size:0.7em;
	font-family:arial;
}

.weekScheduler_appointment_footer{
	position:absolute;
	bottom:-1px;
	border-top:1px solid #000;
	height:4px;
	width:100%;
	font-size:0.8em;
	z-index:120000;
	background-color:#000;
}

.weekScheduler_appointment_time{
	position:absolute;
	border:1px solid #000;
	right:1px;
	top:5px;
	width:58px;
	height:12px;
	z-index:10000;
	font-size:0.7em;
	padding:1px;
	background-color:#F6DBA2;
}


.weekScheduler_start_time{
	position:absolute;
	border:1px solid #000;
	left:0px;
	top:25px;
	width:100%;
	height:12px;
	z-index:1000;
	font-size:0.7em;
	padding:1px;
	background-color:#F6DBA2;
}


.weekScheduler_end_time{
	position:absolute;
	border:1px solid #000;
	left:0px;
	top:45px;
	width:99%;
	height:12px;
	z-index:1000;
	font-size:0.7em;
	padding:1px;
	background-color:#F6DBA2;
}

.weekScheduler_appointment_action{
	position:absolute;
	left:0px;
	top:5px;
	width:116px;
	height:12px;
	z-index:2000;
	font-size:0.7em;
}

.weekScheduler_actionInput{
	position:absolute;
	left:0px;
	top:5px;
	width:116px;
	height:17px;
	z-index:0;
	font-size:0.7em;
	display:none;
}

.weekScheduler_appointment_project{
	position:absolute;
	left:0px;
	top:65px;
	width:116px;
	height:12px;
	z-index:2000;
	font-size:0.7em;
}

.weekScheduler_projectInput{
	position:absolute;
	left:0px;
	top:65px;
	width:116px;
	height:17px;
	z-index:0;
	font-size:0.7em;
	display:none;
}

.weekScheduler_appointment_annotation{
	position:absolute;
	left:0px;
	top:65px;
	width:100%;
	height:48px;
	z-index:5000;
	display:block;
	font-size:0.7em;
}

.weekScheduler_appointment_textarea{
	position:absolute;
	left:0px;
	top:65px;
	width:100%;
	height:48px;
	z-index:5000;
	font-size:0.7em;
	font-family:arial;
	display:none;
}

.eventIndicator{
	background-color:#00F;
	z-index:50;
	display:none;
	position:absolute;
}
</style>
<script type="text/javascript" src="Resources/TimesheetCalendar/ajax.js"></script>
<script type="text/javascript">
// It's important that this JS section is above the line below wher dhtmlgoodies-week-planner.js is included
var itemRowHeight=<%out.print(rowHeight);%>;
<%	
	GregorianCalendar gc = new GregorianCalendar(); 
	SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd"); 
%>
var initDateToShow = '<%=formater.format(gc.getTime())%>';	// Initial date to show
</script>
<%
	formater = new SimpleDateFormat("yyyyMMdd"); 
%>
<script src="Resources/TimesheetCalendar/stack.js" type="text/javascript"></script>
<script src="Resources/TimesheetCalendar/dhtmlgoodies-week-planner.js?random=<%=formater.format(gc.getTime())%>" type="text/javascript"></script>
</head>
<body>
<%@ include file="WEB-INF/jspf/header.jspf" %>
<%
        de.cismet.web.timetracker.Database db = new de.cismet.web.timetracker.Database(application.getRealPath("/").replace('\\','/'));
	de.cismet.web.timetracker.ControlElements ce = new de.cismet.web.timetracker.ControlElements(db);
%>

<script type="text/javascript">
	var enableHorizontalResize = new Array();
<%
	String CONFIG_PATH = application.getRealPath("/").replace('\\','/') + "WEB-INF/config/config.xml";
	Config conf = new Config(CONFIG_PATH);
	Iterator it = conf.getHorizontalResize().iterator();
	while (it.hasNext()){
		out.println("enableHorizontalResize[enableHorizontalResize.length] = '" + (String)it.next() + "'");
	}
%>

	var enableProjects = new Array();
<%
	it = conf.getEnableProjects().iterator();
	while (it.hasNext()){
		out.println("enableProjects[enableProjects.length] = '" + (String)it.next() + "'");
	}
%>
</script>

<table style="margin-left: auto; margin-right: auto;" border="0" cellpadding="0" cellspacing="0" summary="" width="1000px">
	<tbody>
		<tr style="height: 100%;" valign="top">
			<td>
			<h1>Timesheet</h1>
                        <%
                            if(!db.isConnectionOk()){
                                out.println("Fehler beim Verbinden mit der Datenbank:<br />");
                                out.println(db.getErrorMessage());
                                System.out.println(db.getErrorMessage());
                            }
                        %>
				
			<script type="text/javascript">window.onload = initWeekScheduler;</script>

                        <%
                            out.println(ce.getUserCombo(request.getParameter("u_id"), "onchange=\"changeUser()\""));
                        %>
                        <input type="button" class="weekButton" value="<" title="Woche zur&uuml;ck" onclick="displayPreviousWeek();return false" />
			<input type="button" class="weekButton" value=">" title="Woche vor" onclick="displayNextWeek();return false" />

			<button class="weekButton" type="button" name="zoomIn" title="vergr&ouml;&szlig;ern" onclick='calendarZoomIn();'>
                            <img src="Resources/Icons/004021.ico" width="16" height="16" alt="zoom in" />+
			</button>

                        <button class="weekButton" type="button" name="zoomOut" title="verkleinern" onclick='calendarZoomOut();'>
			        <img src="Resources/Icons/004021.ico" width="16" height="16" alt="zoom in" />-
			</button>

			<button class="weekButton" type="button" name="CalendarUndo" title="&Auml;nderung r&uuml;ckg&auml;ngig machen" id="CalendarUndoId" onclick='calendarUndo();' disabled>
                            undo
			</button>
                        <%
                                out.println(ce.getViewCombo(CONFIG_PATH, "onchange=\"changeView()\""));
                        %>

			<div id="weekScheduler_container">
				<div id="weekScheduler_top">
					<div class="spacer"><span></span></div>
					<div class="days" id="weekScheduler_dayRow">
						<div>Monday <span></span></div>
						<div>Tuesday <span></span></div>
						<div>Wednesday <span></span></div>
						<div>Thursday <span></span></div>
						<div>Friday <span></span></div>
						<div>Saturday <span></span></div>
						<div>Sunday <span></span></div>
					</div>
				</div>
				<div id="weekScheduler_content">
						<div id="weekScheduler_hours">
					<%
					String prefix;
					String time;
					for(int no=0;no<24;no++){
						if(no<10) prefix = "0"; else prefix="";
						time = prefix + no + "<span class=\"content_hour\">00</span>";
						%>
						<div class="calendarContentTime"><% out.print(time); %></div>
						<%
					}
					%>
					</div>

					<div id="weekScheduler_appointments">
						<%
						for(int no=0;no<7;no++){	// Looping through the days of a week
							%>
							<div class="weekScheduler_appointments_day">
							<%
							for(int no2=0;no2<24;no2++){
								out.print("<div id=\"weekScheduler_appointment_hour" + no + "_" + no2 + "\" class=\"weekScheduler_appointmentHour\"></div>\n");
							}
							%>
							</div>
							<%
						}
						%>
					</div>
				</div>
			</div>
			<td>
		</tr>
	</tbody>
        <%@ include file="WEB-INF/jspf/footer.jspf" %>
    </table>
       <%db.close();%>
       <script type="text/javascript">
            function iecompattest() {
                return (document.compatMode && document.compatMode!="BackCompat")? document.documentElement : document.body;
            }

            // gets the height of the displayable area of the browser window
            function calcHeight(){
                if (iecompattest().clientHeight) {
                    return iecompattest().clientHeight;
                } else {
                    // this should not happen
                    return null;
                }
            }

            function resize() {
                var newHeight = (calcHeight() - 245);

                if (newHeight < 200) {
                    newHeight = 200;
                }
                var cont = document.getElementById( 'weekScheduler_content' );
                cont.style.height = newHeight + "px";
            }
       </script>
 </body>
</html>