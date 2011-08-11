package de.cismet.web.timetracker.views;

import de.cismet.web.timetracker.CalendarItem;
import de.cismet.web.timetracker.Database;
import de.cismet.web.timetracker.TimeTrackerConstants;
import de.cismet.web.timetracker.TimeTrackerFunctions;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Vector;

public class AdminView extends AbstractView {

    private Database db;
    SimpleDateFormat formaterDB = new SimpleDateFormat("yyyy-MM-d HH:mm");

    public AdminView(Database db) {
        this.db = db;
        if (!db.isConnectionOk()) {
            System.out.println(db.getErrorMessage());
        }
    }

    public Vector getItemsOfDay(GregorianCalendar day, int u_id) throws SQLException {
        Vector items = new Vector();
        GregorianCalendar start = null;
        GregorianCalendar end = new GregorianCalendar();
        String id = null;
        String annotation = null;
        String action = null;
        String project = null;

        StringBuffer sqlQuery = new StringBuffer("SELECT ts.oid, action, description, annotation, ts.project_id, title, duration_in_hours, time " +
                "FROM tt_timesheet ts LEFT OUTER JOIN tt_projects p ON (ts.project_id = p.id), tt_timesheet_action ta " +
                "WHERE ts.action = ta.id ");
        sqlQuery.append(" AND ts.u_id = " + u_id + " AND date_trunc('day', time) = date_trunc('day', timestamp '" + formaterDB.format(day.getTime()) + "')");
        sqlQuery.append(" ORDER BY time");

        ResultSet rSet = db.execute(sqlQuery.toString());

        while (rSet.next()) {
            start = new GregorianCalendar();
            start.setTimeInMillis(rSet.getTimestamp(8).getTime());
            id = rSet.getString(1);
            annotation = (rSet.getString(4) != null ? rSet.getString(4) : "");
            action = rSet.getString(3);

            end = (GregorianCalendar) start.clone();
//          System.out.println(formaterDB.format(end.getTime()));

            if (rSet.getDouble(7) < 1.0) {
                end.add(GregorianCalendar.MINUTE, 60);
//		System.out.println(formaterDB.format(end.getTime()));
            } else {
                end.add(GregorianCalendar.MINUTE, (int) (rSet.getDouble(7) * 60));
            }

            project = rSet.getString(6);
            CalendarItem item = new CalendarItem(action, annotation, end, id, project, start);
            items.add(item);
        }


        return items;
    }

    public Vector getActions() {
        Vector actions = new Vector();

        try {
            String sqlString = "SELECT description FROM tt_timesheet_action";
            ResultSet rSet = db.execute(sqlString);
            while (rSet.next()) {
                actions.add(rSet.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return actions;
    }

    public Vector getProjectNames() {
        Vector projects = new Vector();
        projects.add("              ");

        try {
            String sqlString = "SELECT title FROM tt_projects";
            ResultSet rSet = db.execute(sqlString);
            while (rSet.next()) {
                projects.add(rSet.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return projects;
    }

    public String saveNewItem(CalendarItem item, int u_id) {
        try {
//          System.out.println("save new Item");
            String actionIdString = getActionId(item.getAction());
            int action = Integer.parseInt(actionIdString);
            String durationInHours = getDurationInHours(item, action);

            String project = (item.getProject() != null && !item.getProject().trim().equals("") ? getProjectId(item.getProject()) : "null");
            String annotation = item.getAnnotation().equals("") ? null : "'" + item.getAnnotation() + "'";

            String sqlString = "INSERT INTO tt_timesheet (time, action, annotation, manual, u_id, project_id, duration_in_hours)" +
                    " VALUES ('" + formaterDB.format(item.getStart()) + "', " + actionIdString +
                    ", '" + annotation + "', true, " + u_id + ", " + project +
                    ", " + durationInHours + " )";

            //		System.out.println(sqlString);
            db.execute(sqlString);
        } catch (Exception e) {
        }
        return null;
    }

    public boolean saveModifiedItem(CalendarItem item, int u_id) {
        boolean deleted = false;
        String actionIdString = getActionId(item.getAction());
        String project = (item.getProject() != null && !item.getProject().trim().equals("") ? getProjectId(item.getProject()) : "null");
        try {
            int action = Integer.parseInt(actionIdString);
            String durationInHours = getDurationInHours(item, action);
            String annotation = item.getAnnotation().equals("") ? null : "'" + item.getAnnotation() + "'";

            String sqlString = "UPDATE tt_timesheet SET manual = true, annotation = " + annotation +
                    ", time = '" + formaterDB.format(item.getStart().getTime()) + "', action = " +
                    actionIdString + ", project_Id = " + project + ", duration_in_hours = " +
                    durationInHours + " WHERE oid = " + item.getId();
//          System.out.println(sqlString);
            db.executeUpdate(sqlString);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return deleted;
    }

    public boolean deleteItem(String id) {
        try {
            String sqlString = "DELETE FROM tt_timesheet WHERE oid = " + id;
//			System.out.println(sqlString);
            db.executeUpdate(sqlString);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * liefert die AktionsID zur uebegebenen Aktionsbeschreibung
     * @param action Aktionsbeschreibung, deren ID bestimmt werden soll
     * @return AktionsID
     */
    private String getActionId(String action) {
        String id = null;
        try {
            String sqlString = "SELECT id FROM tt_timesheet_action WHERE description = '" + action + "'";
            ResultSet rSet = db.execute(sqlString);
            if (rSet.next()) {
                id = rSet.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    /**
     * liefert die Projekt-ID zum uebegebenen Projekt-Titel
     * @param project Projekt-Titel, dessen ID bestimmt werden soll
     * @return Projekt-ID
     */
    private String getProjectId(String project) {
        String id = null;
        try {
            String sqlString = "SELECT id FROM tt_projects WHERE title = '" + project + "'";
            ResultSet rSet = db.execute(sqlString);
            if (rSet.next()) {
                id = rSet.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    /**
     * liefert die Dauer des uebergebenen items in Stunden. Wenn die uebergebene Aktion-ID darauf
     * schliessen laesst, dass dieser Termin keine Dauer hat (z.B.COME, GO,...) dann wird null zurueckgeliefert
     * @param item	zu pruefender Termin
     * @param action Aktion-Id des uebergebenen Termins
     * @return Dauer des uebergebenen Termins in Stunden, oder null
     */
    private String getDurationInHours(CalendarItem item, int action) {
        String durationInHours;
        if (action == TimeTrackerConstants.PROJECT_SUBSEQUENT || action == TimeTrackerConstants.CORRECTION ||
                action == TimeTrackerConstants.HOLIDAY_HOURS || action == TimeTrackerConstants.ILLNESS_HOURS) {
            double duration = TimeTrackerFunctions.calcDifferenceInHours(item.getEnd(), item.getStart());
            durationInHours = "" + duration;
        } else {
            durationInHours = null;
        }
        return durationInHours;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        /*        try{
        GregorianCalendar gc = new GregorianCalendar(2006,6,20);

        AdminView av = new AdminView();
        Vector items = av.getItemsOfDay(gc, 3);
        Iterator it = items.iterator();
        while(it.hasNext()){
        CalendarItem ci = (CalendarItem)it.next();
        System.out.println(ci.toString());
        }
        }catch(SQLException e){
        e.printStackTrace();
        }
         */
    }
}
