/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.web.timetracker.views;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.text.SimpleDateFormat;

import java.util.GregorianCalendar;
import java.util.Vector;

import de.cismet.web.timetracker.*;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class BasicView extends AbstractView {

    //~ Instance fields --------------------------------------------------------

    SimpleDateFormat formaterDB = new SimpleDateFormat("yyyy-MM-d HH:mm");

    private Database db;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new BasicView object.
     *
     * @param  db  DOCUMENT ME!
     */
    public BasicView(final Database db) {
        this.db = db;
        if (!db.isConnectionOk()) {
            System.out.println(db.getErrorMessage());
        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean deleteItem(final String id) {
        try {
            String sqlString = "SELECT action, time, u_id FROM tt_timesheet WHERE oid = " + id;
            final ResultSet rSet = db.execute(sqlString);
            if (rSet.next()) {
                if (rSet.getInt(1) == TimeTrackerConstants.COME) {
                    sqlString = "DELETE FROM tt_timesheet WHERE u_id = " + rSet.getString(3)
                                + " AND date_trunc('day', time) = date_trunc('day', timestamp '"
                                + formaterDB.format(rSet.getTimestamp(2)) + "') AND "
                                + "( ( action =" + TimeTrackerConstants.COME + " OR action = " + TimeTrackerConstants.GO
                                + ") OR (action = " + TimeTrackerConstants.PROJECT_SUBSEQUENT + ") OR "
                                + "(action = " + TimeTrackerConstants.PROJECT_COME + " AND Project_id = "
                                + TimeTrackerConstants.PROJ_PAUSE + "))";
                } else {
                    sqlString = "DELETE FROM tt_timesheet WHERE oid = " + id;
                }
//                              System.out.println(sqlString);
                db.executeUpdate(sqlString);
            } else {
                System.out.println("null ret: false");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public String saveNewItem(final CalendarItem item, final int u_id) {
        String id = null;
        try {
            if (item.getAction().equals(TimeTrackerConstants.WORKDAY_STRING)) {
                // kommen-Buchung
                String sqlString = "INSERT INTO tt_timesheet"
                            + " (time, action, annotation, manual, u_id, project_id, duration_in_hours)"
                            + " VALUES ('" + formaterDB.format(item.getStart().getTime()) + "', "
                            + TimeTrackerConstants.COME
                            + ", null, true, " + u_id + ", null, null)";
                db.executeUpdate(sqlString);
                // gehen-Buchung
                sqlString = "INSERT INTO tt_timesheet"
                            + " (time, action, annotation, manual, u_id, project_id, duration_in_hours)"
                            + " VALUES ('" + formaterDB.format(item.getEnd().getTime()) + "', "
                            + TimeTrackerConstants.GO
                            + ", null, true, " + u_id + ", null, null)";

                db.executeUpdate(sqlString);
            } else if (item.getAction().equals(TimeTrackerConstants.HOLIDAY_STRING)
                        || item.getAction().equals(TimeTrackerConstants.ILLNESS_STRING)
                        || item.getAction().equals(TimeTrackerConstants.CORRECTION_STRING)) {
                int action = TimeTrackerConstants.HOLIDAY_HOURS;

                if (item.getAction().equals(TimeTrackerConstants.HOLIDAY_STRING)) {
                    action = TimeTrackerConstants.HOLIDAY_HOURS;
                } else if (item.getAction().equals(TimeTrackerConstants.ILLNESS_STRING)) {
                    action = TimeTrackerConstants.ILLNESS_HOURS;
                } else if (item.getAction().equals(TimeTrackerConstants.CORRECTION_STRING)) {
                    action = TimeTrackerConstants.CORRECTION;
                }

                final long duration = (item.getEnd().getTimeInMillis() - item.getStart().getTimeInMillis());
                final double durationInHours = duration / (1000.0 * 60.0 * 60.0);

                final String sqlString = "INSERT INTO tt_timesheet"
                            + " (time, action, annotation, manual, u_id, project_id, duration_in_hours)"
                            + " VALUES ('" + formaterDB.format(item.getStart().getTime()) + "', " + action
                            + ", null, true, " + u_id + ", null, " + durationInHours + ")";
                db.executeUpdate(sqlString);
            }
            String sqlString = "SELECT oid FROM tt_timesheet"
                        + " WHERE time = '" + formaterDB.format(item.getStart().getTime()) + "' AND u_id = " + u_id;
            final ResultSet oidSet = db.execute(sqlString);
            if (oidSet.next()) {
                id = oidSet.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            id = "-1";
        }
        return id;
    }

    @Override
    public boolean saveModifiedItem(final CalendarItem item, final int u_id) {
        String sqlString = "SELECT time FROM tt_timesheet "
                    + "WHERE oid = " + item.getId();

        try {
            final ResultSet rSet = db.execute(sqlString);

            Vector items;
            final CalendarItem original;

            if ((rSet != null) && rSet.next()) {
                final GregorianCalendar gc = new GregorianCalendar();
                gc.setTimeInMillis(rSet.getTimestamp(1).getTime());

                items = getItemsOfDay(gc, u_id);
            } else {
                items = getItemsOfDay(item.getStart(), u_id);
            }

            original = (CalendarItem)items.firstElement();

            if (original.getStart().get(GregorianCalendar.DATE) != item.getStart().get(GregorianCalendar.DATE)) {
                // Startzeit hat sich um einen oder mehrere Tage verschoben
                final long minutes = Math.abs((original.getStart().getTimeInMillis() / (1000 * 60))
                                - (item.getStart().getTimeInMillis() / (1000 * 60)));
                final char sign = (original.getStart().before(item.getStart()) ? '+' : '-');

                sqlString = "UPDATE tt_timesheet SET time = time " + sign + " INTERVAL '" + minutes + " minutes' "
                            + ", manual=true WHERE u_id=" + u_id + " AND "
                            + "date_trunc('day', time) = date_trunc('day', timestamp '"
                            + formaterDB.format(original.getStart().getTime()) + "') AND "
                            + "(action = " + TimeTrackerConstants.COME + " OR action = " + TimeTrackerConstants.GO
                            + " OR action = " + TimeTrackerConstants.HOLIDAY_HOURS
                            + " OR action = " + TimeTrackerConstants.ILLNESS_HOURS + " OR action = "
                            + TimeTrackerConstants.PROJECT_SUBSEQUENT
                            + " OR action = " + TimeTrackerConstants.PROJECT_COME + " OR action = "
                            + TimeTrackerConstants.CORRECTION + ")";
//                              System.out.println(sqlString);
                db.executeUpdate(sqlString);
            } else {
                if (
                    !TimeTrackerConstants.formater.format(original.getStart().getTime()).equals(
                                TimeTrackerConstants.formater.format(item.getStart().getTime()))) {
                    // Startzeit hat sich geaendert, der Tag aber nicht
                    sqlString = "UPDATE tt_timesheet SET time = '" + formaterDB.format(item.getStart().getTime())
                                + "', manual=true WHERE oid=" + item.getId();
                    db.executeUpdate(sqlString);
                    // Kommen und Gehen, die vor der, zuvor ersten, Kommen-Buchung liegen, loeschen
                    sqlString = "DELETE FROM tt_timesheet WHERE u_id=" + u_id + " AND "
                                + "date_trunc('day', time) = date_trunc('day', timestamp '"
                                + formaterDB.format(item.getStart().getTime()) + "') AND "
                                + "time < timestamp '" + formaterDB.format(item.getStart().getTime()) + "' AND "
                                + "(action=" + TimeTrackerConstants.COME + " OR action=" + TimeTrackerConstants.GO
                                + ")";
//                                      System.out.println(sqlString);
                    db.executeUpdate(sqlString);
                }
                // Wenn sich die Startzeit aendert, aendert sich normalerweise auch die Endzeit
                if (
                    !TimeTrackerConstants.formater.format(original.getEnd().getTime()).equals(
                                TimeTrackerConstants.formater.format(item.getEnd().getTime()))) {
                    // Endzeit hat sich geaendert
                    sqlString = "SELECT oid FROM tt_timesheet "
                                + "WHERE date_trunc('minute', time) = " + "date_trunc('minute', timestamp '"
                                + formaterDB.format(original.getEnd().getTime()) + "') "
                                + "AND action = " + TimeTrackerConstants.GO + " AND u_id = " + u_id
                                + " ORDER BY time DESC LIMIT 1";

                    final ResultSet lastGo = db.execute(sqlString);
                    if (lastGo.next()) {
                        sqlString = "UPDATE tt_timesheet SET time = '" + formaterDB.format(item.getEnd().getTime())
                                    + "', manual=true WHERE oid=" + lastGo.getString(1);
                        db.executeUpdate(sqlString);
                    } else {
                        return false;
                    }
                } else if (!original.getAction().equals(item.getAction())) {
                    // Aktion hat sich geaendert
                    if (!changeAction(original, item, u_id)) {
                        return false;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public Vector getActions() {
        final Vector actions = new Vector();
        actions.add(TimeTrackerConstants.WORKDAY_STRING);
        actions.add(TimeTrackerConstants.CORRECTION_STRING);
        actions.add(TimeTrackerConstants.ILLNESS_STRING);
        actions.add(TimeTrackerConstants.HOLIDAY_STRING);
        return actions;
    }

    @Override
    public Vector getItemsOfDay(final GregorianCalendar day, final int u_id) throws SQLException {
        final Vector calendarItems = new Vector();
        final String sqlQuery = getQueryString(day, u_id);

        final ResultSet rSet = db.execute(sqlQuery.toString());

        if (rSet != null) {
            GregorianCalendar start = null;
            GregorianCalendar end = null;
            String entryDesc = "";
            String id = null;
            String action = null;
            boolean firstCome = true;
            // speichert den Start einer Pause in Millis. Wenn 0, dann wurde noch keine Pause begonnen
            long startPause = 0;
            // speichert, ob es eine Pause gab, oder ob eine Auto-Pause hinzugefuegt werden soll
            boolean hasPause = false;
            double hoursOfWork = 0.0;

            while (rSet.next()) {
                if ((rSet.getInt(2) == TimeTrackerConstants.COME) && firstCome) {                                   // Arbeitsstart ermitteln
                    action = TimeTrackerConstants.WORKDAY_STRING;
                    firstCome = false;
                    start = new GregorianCalendar();
                    start.setTimeInMillis(rSet.getTimestamp(9).getTime());
                    id = rSet.getString(1);                                                                         // id ist die id des ersten comes
                } else if ((rSet.getInt(2) == TimeTrackerConstants.GO) && TimeTrackerFunctions.isLastGo(rSet, 2)) { // Arbeitsende ermitteln

                    end = new GregorianCalendar();
                    end.setTimeInMillis(rSet.getTimestamp(9).getTime());
                    hoursOfWork += TimeTrackerFunctions.calcDifferenceInHours(end, start);

                    if (!hasPause && (TimeTrackerConstants.HOURS_WITHOUT_AUTO_PAUSE <= hoursOfWork)) { // Auto-Pause einfuegen
                        entryDesc += "<br />Auto-Pause: 1:00";
                        hoursOfWork -= 1.0;
                    }
                    // letzte Pause ging bis zum Ende
                    if (startPause != 0) {
                        final long endPause = rSet.getTimestamp(9).getTime();
                        final double duration = ((endPause - startPause) / (1000.0 * 3600.0));

                        entryDesc += "<br />Pause: " + TimeTrackerFunctions.convert2Time(duration, false);
                        hoursOfWork -= duration;
                        hasPause = true;
                        startPause = 0;
                    }
                } else if (rSet.getInt(2) == TimeTrackerConstants.PROJECT_SUBSEQUENT) { // Projekt nachtraeglich
                    if (rSet.getString(7).equals("Pause")) {
                        hasPause = true;
                        hoursOfWork -= rSet.getDouble(8);
                    }
                    entryDesc += "<br />" + rSet.getString(7) + ": "
                                + TimeTrackerFunctions.convert2Time(rSet.getDouble(8), false);
                } else if (rSet.getInt(2) == TimeTrackerConstants.GO) {                 // Arbeitszeit wird durch ein GO
                                                                                        // unterbrochen
                    // tue nichts, weil dies moeglicherweise ein AIM aussetzer ist
                    // Zur Bestimmung der Arbeitszeit zaehlt die erste Kommen-Buchung und die letzte Gehen-Buchung
                } else if ((rSet.getInt(2) == TimeTrackerConstants.HOLIDAY_HOURS)
                            || (rSet.getInt(2) == TimeTrackerConstants.ILLNESS_HOURS)) { // Urlaub oder Krank

                    final String eventName = ((rSet.getInt(2) == TimeTrackerConstants.HOLIDAY_HOURS)
                            ? TimeTrackerConstants.HOLIDAY_STRING : TimeTrackerConstants.ILLNESS_STRING);
                    if (start == null) {               // zu Beginn des Tages Urlaub/Krank
                        firstCome = false;
                        start = new GregorianCalendar();
                        start.setTimeInMillis(rSet.getTimestamp(9).getTime());
                        id = rSet.getString(1);        // id ist die id des Starts
                        if (!hasRelevantEntry(rSet)) { // ganzer Tag Urlaub/Krank
                            final Query q = new Query(db);
                            final double dailyHoursOfWork = q.getTargetHoursPerWeek(u_id, day, null, false)
                                        / TimeTrackerConstants.WORK_DAYS_PER_WEEK;
                            final double duration = ((rSet.getDouble(8) == 0) ? dailyHoursOfWork : rSet.getDouble(8));
                            start.set(GregorianCalendar.HOUR_OF_DAY, 9);
                            start.set(GregorianCalendar.MINUTE, 0);

                            action = eventName;
                            end = (GregorianCalendar)start.clone();
                            end.add(GregorianCalendar.MINUTE, (int)(duration * 60));
                            hoursOfWork -= duration;
                        }
                    } else {
                        entryDesc += "<br />" + eventName + ": "
                                    + TimeTrackerFunctions.convert2Time(rSet.getDouble(8), false);
                        hoursOfWork -= rSet.getDouble(8);
                    }
                } else if (rSet.getInt(2) == TimeTrackerConstants.PROJECT_COME) { // Projektstart

                    if (rSet.getString(7).equals("Pause")) { // Beginn einer Pause
                        startPause = rSet.getTimestamp(9).getTime();
                    } else {                                 // Moegliches Ende einer Pause
                        if (startPause != 0) {
                            final long endPause = rSet.getTimestamp(9).getTime();
                            final double duration = ((endPause - startPause) / (1000.0 * 3600.0));

                            entryDesc += "<br />Pause: " + TimeTrackerFunctions.convert2Time(duration, false);
                            hoursOfWork -= duration;
                            hasPause = true;
                            startPause = 0;
                        }
                    }
                } else if (rSet.getInt(2) == TimeTrackerConstants.CORRECTION) { // Korrektur
                    if ((start != null) || hasRelevantEntry(rSet)) {
                        entryDesc += "<br />zus&auml;tzlich "
                                    + TimeTrackerFunctions.convert2Time(rSet.getDouble(8), false) + "h Korrektur";
                    } else {                                                    // nur Korrektur (ohne weitere
                                                                                // Arbeitszeit)
                        action = TimeTrackerConstants.CORRECTION_STRING;
                        id = rSet.getString(1);
                        start = new GregorianCalendar();
                        start.setTimeInMillis(rSet.getTimestamp(9).getTime());

                        end = (GregorianCalendar)start.clone();
                        end.add(GregorianCalendar.MINUTE, (int)(rSet.getDouble(8) * 60));
                    }
                    hoursOfWork += rSet.getDouble(8);
                } else if (rSet.getInt(2) == TimeTrackerConstants.ACCOUNT_RESET) {
                    entryDesc += "<br />Kontostand zur&uuml;ckgesetzt: ";
                } else if (rSet.getInt(2) == TimeTrackerConstants.HOLIDAY_ADD) {
                    entryDesc += "<br />" + rSet.getDouble(8) + " Urlaubsstunden hinzugef&uuml;gt";
                } else if (rSet.getInt(2) == TimeTrackerConstants.COME) {    // folgt nicht unmittelbar auf ein go und
                                                                             // ist nicht die erste Buchung
                    if (action == null) {
                        action = TimeTrackerConstants.WORKDAY_STRING;
                    }
                }
            }

            if ((start != null) && (end != null) && (action != null)) {
                if (action.equals(TimeTrackerConstants.HOLIDAY_STRING)
                            || action.equals(TimeTrackerConstants.ILLNESS_STRING)
                            || action.equals(TimeTrackerConstants.CORRECTION_STRING)) {
                    entryDesc = "";
                }
                final CalendarItem item = new CalendarItem(action, entryDesc, end, id, null, start);
                calendarItems.add(item);
            } else if ((start != null) && (end != null) && (action == null)) {
                System.out.println("action == null, id == " + id + " am "
                            + TimeTrackerConstants.formater.format(day.getTime()));
            } else {
                // System.out.println("Fuer den " + TimeTrackerConstants.formater.format(day.getTime()) + " sind keine
                // (zumindest keine fehlerfreien) Daten verfuegbar.");
            }
        } else {
            System.out.println(db.getErrorMessage());
        }
//              System.out.println(xmlResult.toString());
        return calendarItems;
    }

    /**
     * liefert den zur Abfrage der Datenbank benoetigten SQL-String.
     *
     * @param   day   DOCUMENT ME!
     * @param   u_id  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getQueryString(final GregorianCalendar day, final int u_id) {
        final StringBuffer sqlQuery = new StringBuffer(
                "SELECT ts.oid, action, description, annotation, manual, ts.project_id, title, duration_in_hours, time "
                        + "FROM tt_timesheet ts LEFT OUTER JOIN tt_projects p ON (ts.project_id = p.id), tt_timesheet_action ta "
                        + "WHERE ts.action = ta.id ");
        sqlQuery.append(" AND ts.u_id = " + u_id + " AND date_trunc('day', time) = date_trunc('day', timestamp '"
                    + formaterDB.format(day.getTime()) + "')");
        sqlQuery.append(" ORDER BY time");
        return sqlQuery.toString();
    }

    /**
     * ueberprueft, ob noch, fuer die Dauer der Tagesarbeitszeit, relevante Datensaetze im ResultSet vorhanden sind.
     *
     * @param   rSet  das zu ueberpruefende ResultSet
     *
     * @return  true, wenn im RseultSet noch eine Gehen-Buchung oder Kommen-Buchung folgt
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    private boolean hasRelevantEntry(final ResultSet rSet) throws SQLException {
        int i = 1; // weil beim letzten rSet.next() die Schleife nicht betreten wird
        boolean relevantEntryExist = false;

        while (rSet.next()) {
            i++;
            if ((rSet.getInt(2) == TimeTrackerConstants.COME) || (rSet.getInt(2) == TimeTrackerConstants.GO)) {
                relevantEntryExist = true;
                --i; // Aktion aus Kommentar oben findet nicht statt.
                break;
            }
        }

        for (; i > 0; --i) {
            rSet.previous();
        }
        return relevantEntryExist;
    }

    /**
     * Speichert eine Aenderung der Aktion bei einem CalendarItem-Objekt.
     *
     * @param   original  das CalandarItem-Objekt, bevor es modifiziert wurde.
     * @param   newItem   das modifizierte CalandarItem-Objekt
     * @param   u_id      die ID des betreffenden Benutzers
     *
     * @return  true, wenn die Aenderung erfolgreich gespeichert wurde, sonst false
     */
    private boolean changeAction(final CalendarItem original, final CalendarItem newItem, final int u_id) {
        try {
            int action;

            if (newItem.getAction().equals(TimeTrackerConstants.HOLIDAY_STRING)) {
                action = TimeTrackerConstants.HOLIDAY_HOURS;
            } else if (newItem.getAction().equals(TimeTrackerConstants.ILLNESS_STRING)) {
                action = TimeTrackerConstants.ILLNESS_HOURS;
            } else if (newItem.getAction().equals(TimeTrackerConstants.CORRECTION_STRING)) {
                action = TimeTrackerConstants.CORRECTION;
            } else {
                action = TimeTrackerConstants.COME;
            }

            if (original.getAction().equals(TimeTrackerConstants.WORKDAY_STRING)) {
                // von Artbeitstag zu Urlaub/Krankheit oder Korrektur
                // Urlaubseintrag anlegen
                String sqlString = "UPDATE tt_timesheet SET action = " + action + ", duration_in_hours = "
                            + TimeTrackerFunctions.calcDifferenceInHours(newItem.getEnd(), newItem.getStart())
                            + " WHERE "
                            + " oid = " + original.getId();

                db.executeUpdate(sqlString);

                // alte COMESs,  GOs und Pausen loeschen
                sqlString = "DELETE FROM tt_timesheet WHERE u_id = " + u_id
                            + " AND date_trunc('day', time) = date_trunc('day', timestamp '"
                            + formaterDB.format(newItem.getStart().getTime()) + "') "
                            + " AND ( (action = " + TimeTrackerConstants.COME + " OR action = "
                            + TimeTrackerConstants.GO + ") )";
                /*
                 * PROJECT COMEs und PROJECT SUBSEQUENTs in Verbindung mit Pause wird zwar nicht mehr benoetigt, eine
                 * Loeschung macht aber eine Rueckkonvertierung unmoeglich. Eventuell folgende anfuegen: "OR (action = "
                 * + PROJECT_COME + " AND project_id = " + PROJECT_PAUSE + ") " + "OR (action = " + PROJECT_SUBSEQUENT +
                 * " AND project_id = " + PROJECT_PAUSE + ") )";
                 */
                db.executeUpdate(sqlString);
            } else { // von Urlaub/Krankheit oder Korrektur
                if (newItem.getAction().equals(TimeTrackerConstants.WORKDAY_STRING)) {
                    // Urlaubs-, Krnkheits-, Korrektureintrag zu COME umformen
                    String sqlString = "UPDATE tt_timesheet SET action = " + action + ", project_id = null "
                                + ", duration_in_hours = 0.0 WHERE "
                                + " oid = " + original.getId();

                    db.executeUpdate(sqlString);

                    // GO anlegen
                    sqlString = "INSERT INTO tt_timesheet (time, action, u_id) VALUES('"
                                + formaterDB.format(newItem.getEnd().getTime()) + "', " + TimeTrackerConstants.GO + ", "
                                + u_id + ")";

                    db.executeUpdate(sqlString);
                } else {
                    // von Urlaub/Krankheit oder Korrektur zu Urlaub/Krankheit oder Korrektur
                    final String sqlString = "UPDATE tt_timesheet SET action = " + action + ", project_id = null WHERE "
                                + " oid = " + original.getId();
                    db.executeUpdate(sqlString);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        /*        BasicView bi = new BasicView();
         * GregorianCalendar cal = new GregorianCalendar(2006,7,31); //              while
         * (cal.get(GregorianCalendar.MONTH) < 8){ try{
         * System.out.println(TimeTrackerConstants.formater.format(cal.getTime())); bi.getItemsOfDay(cal, 1);
         * cal.add(GregorianCalendar.DATE , 1); }catch(SQLException e){ e.printStackTrace(); } //      }
         *
         */
    }
}
