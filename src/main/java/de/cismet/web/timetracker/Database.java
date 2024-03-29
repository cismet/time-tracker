/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.web.timetracker;

import java.sql.*;

import java.util.GregorianCalendar;
import java.util.Vector;

import de.cismet.web.timetracker.types.ContractInfos;
import de.cismet.web.timetracker.types.NetModusAction;
import de.cismet.web.timetracker.types.ProjectInfos;
import de.cismet.web.timetracker.types.TimeDurationPair;
import de.cismet.web.timetracker.types.TimesheetEntry;
import de.cismet.web.timetracker.types.TimesheetSet;
import de.cismet.web.timetracker.types.TitleTimePair;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class Database implements DatabaseInterface {

    //~ Instance fields --------------------------------------------------------

    private Connection conn;
    private Statement stmt;
    private String errorMessage;
    private PreparedStatement psTimeOfWork;

    //~ Constructors -----------------------------------------------------------

    /**
     * niemals nutzen. Nur zur Kompatibilitaet mit DatabaseCache
     */
    public Database() {
    }

    /**
     * Creates a new Database object.
     *
     * @param  applicationPath  DOCUMENT ME!
     */
    public Database(final String applicationPath) {
        this.conn = null;
        this.stmt = null;
        this.errorMessage = null;

        try {
            final Config config = new Config(applicationPath + "WEB-INF/config/config.xml");
            // prueft, ob Datenbanktreiber vorhanden
            Class.forName(config.getDbDriver());
            // verbindet mit Datenbank
            conn = DriverManager.getConnection(config.getDbPath(), config.getDbUser(), config.getDbPwd());
            stmt = conn.createStatement();
            createPreparedStatements();
        } catch (Exception e) {
            e.printStackTrace();
            conn = null;
            stmt = null;
            errorMessage = e.getMessage();
        }
    }

    /**
     * Creates a new Database object.
     *
     * @param  driverName  DOCUMENT ME!
     * @param  url         DOCUMENT ME!
     * @param  user        DOCUMENT ME!
     * @param  pwd         DOCUMENT ME!
     */
    public Database(final String driverName, final String url, final String user, final String pwd) {
        this.conn = null;
        this.stmt = null;
        this.errorMessage = null;

        try {
            // prueft, ob Datenbanktreiber vorhanden
            Class.forName(driverName);
            // verbindet mit Datenbank
            conn = DriverManager.getConnection(url, user, pwd);
            stmt = conn.createStatement();
            createPreparedStatements();
        } catch (Exception e) {
            e.printStackTrace();
            conn = null;
            stmt = null;
            errorMessage = e.getMessage();
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private void createPreparedStatements() throws Exception {
        psTimeOfWork = conn.prepareStatement("SELECT action, ts.project_id, title, duration_in_hours, time "
                        + "FROM tt_timesheet ts LEFT OUTER JOIN tt_projects p ON (ts.project_id = p.id) "
                        + "WHERE ts.u_id = ? AND date_trunc('day', time) >= date_trunc('day', timestamp ?) AND "
                        + "date_trunc('day', time) <= date_trunc('day', timestamp ?) ORDER BY time");
    }

    /**
     * DOCUMENT ME!
     *
     * @param   uId   DOCUMENT ME!
     * @param   time  DOCUMENT ME!
     * @param   days  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    public Vector<TimesheetSet> getTimeOfWork(final int uId, final GregorianCalendar time, final int days)
            throws SQLException {
        final Vector<TimesheetSet> result = new Vector<TimesheetSet>();
        final Timestamp startTime = new Timestamp(time.getTimeInMillis());
        final GregorianCalendar end = (GregorianCalendar)time.clone();
        end.add(GregorianCalendar.DATE, days);
        final Timestamp endTime = new Timestamp(end.getTimeInMillis());
        int lastDay = -1;

        psTimeOfWork.setInt(1, uId);
        psTimeOfWork.setTimestamp(2, startTime);
        psTimeOfWork.setTimestamp(3, endTime);

        final ResultSet rSet = psTimeOfWork.executeQuery();

        if (rSet != null) {
            TimesheetSet set = null;
            while (rSet.next()) {
                final TimesheetEntry tmp = new TimesheetEntry();
                final GregorianCalendar timeTmp = new GregorianCalendar();
                timeTmp.setTimeInMillis(rSet.getTimestamp(5).getTime());

                if (timeTmp.get(GregorianCalendar.DATE) != lastDay) {
                    if (lastDay != -1) {
                        result.add(set);
                    }
                    set = new TimesheetSet();
                }

                tmp.setAction(rSet.getInt(1));
                tmp.setProjectId(rSet.getInt(2));
                tmp.setTitle(rSet.getString(3));
                tmp.setDuration_in_hours(rSet.getDouble(4));
                tmp.setTime(timeTmp);
                set.add(tmp);
                lastDay = timeTmp.get(GregorianCalendar.DATE);
            }

            if (set != null) {
                result.add(set);
            }

            rSet.close();
        }

        return result;
    }

    @Override
    public boolean hasAutopause(final int uId) throws SQLException {
        boolean result = true;
        final ResultSet hasAutoPause = stmt.executeQuery("SELECT autopause FROM tt_user WHERE id=" + uId);

        if (hasAutoPause != null) {
            if (hasAutoPause.next()) {
                result = hasAutoPause.getBoolean(1);
            }
            hasAutoPause.close();
        }

        return result;
    }

    @Override
    public ResultSet getHolidaysForYear(final int uId, final GregorianCalendar toTime) throws SQLException {
        final String holidayQuery = "SELECT time, coalesce(duration_in_hours, " + "0" + ") "
                    + "FROM tt_timesheet ts, tt_timesheet_action ta "
                    + "WHERE 	ts.action = ta.id AND "
                    + "actionname = 'HOLIDAYHOURS' AND "
                    + "ts.u_id = " + uId + " AND "
                    + "date_trunc('year',time) = date_trunc('year', timestamp '"
                    + TimeTrackerFunctions.getDateString(toTime) + "') AND "
                    + "to_char(time, 'DY') != 'SAT' AND to_char(time, 'DY') != 'SUN' "
                    + "ORDER BY time";

        return conn.createStatement().executeQuery(holidayQuery);
    }

    @Override
    public ResultSet getIllnessForYear(final int uId, final GregorianCalendar toTime) throws SQLException {
        final String illnessQuery = "SELECT time, coalesce(duration_in_hours, " + "0" + ")"
                    + "FROM tt_timesheet ts, tt_timesheet_action ta "
                    + "WHERE 	ts.action = ta.id AND "
                    + "actionname = 'ILLNESSHOURS' AND "
                    + "ts.u_id = " + uId + " AND "
                    + "date_trunc('year',time) = date_trunc('year', timestamp '"
                    + TimeTrackerFunctions.getDateString(toTime) + "') "
                    + "ORDER BY time";

        final ResultSet illness = conn.createStatement().executeQuery(illnessQuery);
        return illness;
    }

    /**
     * liefert die zum uebergebenen Buddyname gehoerende id.
     *
     * @param   buddy  Buuddyname
     *
     * @return  die zum uebergebenen Buddynamen gehoerende ID. Falls der Buddyname nicht existiert, dann wird 1
     *          zurueckgegeben.
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    @Override
    public int getIdByBuddyName(final String buddy) throws SQLException {
        int id = 1;

        final ResultSet rSet = execute("SELECT id FROM tt_user where buddyname = '" + buddy + '\'');

        if (rSet != null) {
            if (rSet.next()) {
                id = rSet.getInt(1);
            }

            rSet.close();
        }

        return id;
    }

    @Override
    public ContractInfos getHoursOfWork(final int uId, final GregorianCalendar day) throws SQLException {
        final String daysAsString = TimeTrackerFunctions.getDateString(day);
        ContractInfos contract = null;

        final String SQLQuery = "SELECT whow, to_date, from_date "
                    + "FROM tt_contracts "
                    + "WHERE u_id = " + uId
                    + " AND date_trunc('day', from_date) <= date_trunc('day', timestamp '" + daysAsString + "') AND "
                    + " (to_date is null OR date_trunc('day', to_date) >= date_trunc('day', timestamp '" + daysAsString
                    + "'))";
        final ResultSet rSet = execute(SQLQuery);

        if (rSet != null) {
            if (rSet.next()) {
                final GregorianCalendar fromDate = new GregorianCalendar();
                fromDate.setTimeInMillis(rSet.getTimestamp(3).getTime());

                GregorianCalendar toDate = null;
                if (rSet.getTimestamp(2) != null) {
                    toDate = new GregorianCalendar();
                    toDate.setTimeInMillis(rSet.getTimestamp(2).getTime());
                }

                contract = new ContractInfos(fromDate, toDate, uId, rSet.getDouble(1));
                rSet.getInt(1);
            }

            rSet.close();
        }

        return contract;
    }

    /**
     * generiert eine SELECT-Abfrage, die den Urlaub des uebergebenen Jahres und des uebergebenen Benutzers generiert
     * Beachtet dabei auch den Wert des Feldes exactHoliday in der Tabelle tt_user, welches angibt, ob die Urlaubstage
     * der Vertragsdaten relativ zur Laufzeit des Vertrages im aktuellen Jahr berechnet werden sollen.
     *
     * @param   day  gibt das Datum an, von dem aus der Urlaub bestimmt werden soll
     * @param   uId  gibt den Benutzer an, dessen Urlaub bestimmt werden soll
     *
     * @return  eine SELECT-Abfrage, mit der der Jahresurlaub bestimmt werden kann
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    private String getHolidayQuery(final GregorianCalendar day, final int uId) throws SQLException {
        final String year = day.get(GregorianCalendar.YEAR) + "";
        String queryString = "SELECT  \"exactHoliday\" from tt_user WHERE id = " + uId;

        final ResultSet rSet = execute(queryString);

        if ((rSet != null) && rSet.next() && rSet.getBoolean("exactHoliday")) {
            queryString = "SELECT round (sum( "
                        + "( CAST (ydoh AS real) * ( ("
                        + "(CASE WHEN (coalesce(to_date, date '" + year + "-12-31' )) >= '" + year + "-12-31' THEN '"
                        + year + "-12-31' ELSE to_date END) - "
                        + "(CASE WHEN from_date < '" + year + "-01-01' THEN '" + year
                        + "-01-01' ELSE from_date END)))  + 1) / 365)) "
                        + "FROM tt_contracts "
                        + "WHERE u_id = " + uId + " AND "
                        + "(from_date, coalesce(to_date, '" + year + "-12-31')) overlaps (date '" + year
                        + "-01-01', date '" + year + "-12-31' )";
        } else {
            queryString = "SELECT sum( CAST (ydoh AS real) ) FROM tt_contracts "
                        + "WHERE u_id = " + uId + " AND "
                        + "(from_date, coalesce(to_date, '" + year + "-12-31')) overlaps (date '" + year
                        + "-01-01', date '" + year + "-12-31' )";
        }

        return queryString;
    }

    /**
     * liefert den Jahresurlaub des uebergebenen Benutzers fuer das uebergebene Jahr. Wenn year == null, dann gilt das
     * aktuelle Jahr.
     *
     * @param   u_id  DOCUMENT ME!
     * @param   year  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    @Override
    public int getHolidayForYear(final int u_id, GregorianCalendar year) throws SQLException {
        int holidayForYear = 0;

        if (year == null) {
            year = new GregorianCalendar();
        }

        final String holidayQuery = getHolidayQuery(year, u_id);
        final ResultSet rSet = execute(holidayQuery);

        if (rSet != null) {
            if (rSet.next()) {
                holidayForYear = rSet.getInt(1);
            }
            rSet.close();
        }

        return holidayForYear;
    }

    @Override
    public Vector<TimeDurationPair> getUsedHolidaysForYear(final int u_id, final GregorianCalendar year)
            throws SQLException {
        final GregorianCalendar yearCopy = (GregorianCalendar)year.clone();
        yearCopy.set(GregorianCalendar.DATE, 1);
        final String firstDayOfYear = TimeTrackerFunctions.getDateString(yearCopy);
        final Vector<TimeDurationPair> result = new Vector<TimeDurationPair>();

        final String holidayQuery = "SELECT duration_in_hours, time "
                    + "FROM tt_timesheet ts, tt_timesheet_action ta "
                    + "WHERE 	ts.action = ta.id AND "
                    + "actionname = 'HOLIDAYHOURS' AND "
                    + "ts.u_id = " + u_id + " AND "
                    + "to_char(time, 'DY') != 'SAT' AND to_char(time, 'DY') != 'SUN' AND "
                    + "date_trunc('year',time) = date_trunc('year', timestamp '" + firstDayOfYear + "') ";
        final ResultSet rSet = execute(holidayQuery);

        if (rSet != null) {
            while (rSet.next()) {
                final TimeDurationPair tmp = new TimeDurationPair();
                final GregorianCalendar time = new GregorianCalendar();
                time.setTimeInMillis(rSet.getTimestamp(2).getTime());

                tmp.setDuration(rSet.getDouble(1));
                tmp.setTime(time);

                result.add(tmp);
            }
            rSet.close();
        }

        return result;
    }

    @Override
    public Vector<TimeDurationPair> getHolidayCorrectionsForYear(final int u_id, final GregorianCalendar year)
            throws SQLException {
        final GregorianCalendar yearCopy = (GregorianCalendar)year.clone();
        yearCopy.set(GregorianCalendar.DATE, 1);
        final String firstDayOfYear = TimeTrackerFunctions.getDateString(yearCopy);
        final Vector<TimeDurationPair> result = new Vector<TimeDurationPair>();

        final String holidayQuery = "SELECT duration_in_hours, time "
                    + "FROM tt_timesheet ts, tt_timesheet_action ta "
                    + "WHERE 	ts.action = ta.id AND "
                    + "actionname = 'HOLIDAY ADD' AND "
                    + "ts.u_id = " + u_id + " AND "
                    + "date_trunc('year',time) = date_trunc('year', timestamp '" + firstDayOfYear + "')";
        final ResultSet rSet = execute(holidayQuery);

        if (rSet != null) {
            while (rSet.next()) {
                final TimeDurationPair tmp = new TimeDurationPair();
                final GregorianCalendar time = new GregorianCalendar();
                time.setTimeInMillis(rSet.getTimestamp(2).getTime());

                tmp.setDuration(rSet.getDouble(1));
                tmp.setTime(time);

                result.add(tmp);
            }
            rSet.close();
        }

        return result;
    }

    /**
     * liefert einen SQL-String zur Abfrage der Ferien des uebergebenen Users seit since.
     *
     * @param   u_id   DOCUMENT ME!
     * @param   since  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public String getHolidayQueryString(final int u_id, final GregorianCalendar since) {
        final String holidayQuery = "SELECT time, coalesce(duration_in_hours, 0)"
                    + "FROM tt_timesheet ts, tt_timesheet_action ta "
                    + "WHERE 	ts.action = ta.id AND "
                    + "actionname = 'HOLIDAYHOURS' AND "
                    + "ts.u_id = " + u_id + " AND "
                    + "date_trunc('day',time) >= date_trunc('day', timestamp '"
                    + TimeTrackerFunctions.getDateString(since) + "') AND "
                    + "to_char(time, 'DY') != 'SAT' AND to_char(time, 'DY') != 'SUN' "
                    + "ORDER BY time";

        return holidayQuery;
    }

    /**
     * liefert einen SQL-String zur Abfrage der Krankheitstage des uebergebenen Users seit since.
     *
     * @param   u_id   DOCUMENT ME!
     * @param   since  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public String getIllnessQueryString(final int u_id, final GregorianCalendar since) {
        final String illnessQuery = "SELECT time, coalesce(duration_in_hours, 0)"
                    + "FROM tt_timesheet ts, tt_timesheet_action ta "
                    + "WHERE 	ts.action = ta.id AND "
                    + "actionname = 'ILLNESSHOURS' AND "
                    + "ts.u_id = " + u_id + " AND "
                    + "date_trunc('day',time) >= date_trunc('day', timestamp '"
                    + TimeTrackerFunctions.getDateString(since) + "') "
                    + "ORDER BY time";

        return illnessQuery;
    }

    /**
     * liefert einen Vector mit den PROJECT COME-Aktionen, die fuer die Berechnung der Projektzeiten erforderlich sind.
     * Also alle, ab dem ersten, das in den uebergebenen Zeitraum hineinreicht
     *
     * @param   u_id          DOCUMENT ME!
     * @param   date          DOCUMENT ME!
     * @param   timeInterval  gueltige Werte zum Beispiel: year, month
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    @Override
    public Vector<TitleTimePair> getProjectComes(final int u_id,
            final GregorianCalendar date,
            final String timeInterval) throws SQLException {
        final Vector<TitleTimePair> result = new Vector<TitleTimePair>();

        final String projectQuery = "SELECT p.title, ts.time "
                    + "FROM	tt_timesheet_action ta, tt_projects p, tt_timesheet ts left outer join "
                    + "(SELECT time "
                    + "FROM	tt_timesheet ts, tt_timesheet_action ta, tt_projects p "
                    + "WHERE	ts.action = ta.id AND ts.u_id = " + u_id + " AND ts.project_id != 0 AND "
                    + "    	ts.project_id = p.id AND ta.actionname = 'PROJECT COME' AND "
                    + "	date_trunc('" + timeInterval + "',time) < date_trunc('" + timeInterval + "', timestamp '"
                    + TimeTrackerFunctions.getDateString(date) + "') "
                    + "ORDER BY time desc	LIMIT 1)  firstRelevantCome using (time) "
                    + "WHERE	ts.action = ta.id AND ts.u_id = " + u_id + " AND ts.project_id = p.id AND "
                    + "ta.actionname = 'PROJECT COME' AND ts.time >= COALESCE(firstRelevantCome.time , date_trunc('"
                    + timeInterval + "', timestamp '" + TimeTrackerFunctions.getDateString(date) + "')) "
                    + "ORDER BY ts.time asc";
        System.out.println(projectQuery);
        final ResultSet projects = execute(projectQuery.toString());

        if (projects != null) {
            while (projects.next()) {
                final TitleTimePair tmp = new TitleTimePair();
                final GregorianCalendar time = new GregorianCalendar();
                time.setTimeInMillis(projects.getTimestamp(2).getTime());

                tmp.setTitle(projects.getString(1));
                tmp.setTime(time);

                result.add(tmp);
            }
            projects.close();
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   u_id  User-ID
     *
     * @return  true, wenn sich der uebergebene Nutzer im Netto Modus befindet. Sonst false
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    public boolean isUserInNetMode(final int u_id) throws SQLException {
        boolean result = false;
        final String query = "SELECT action FROM tt_timesheet WHERE (action = "
                    + TimeTrackerConstants.NET_HOURS_OF_WORK_START
                    + " OR action = " + TimeTrackerConstants.NET_HOURS_OF_WORK_END + ") AND u_id = " + u_id
                    + " ORDER BY time DESC LIMIT 1 ";
        final ResultSet actions = execute(query.toString());

        if (actions != null) {
            if (actions.next()) {
                if (actions.getInt(1) == TimeTrackerConstants.NET_HOURS_OF_WORK_START) {
                    result = true;
                } else {
                    result = false;
                }
            }
            actions.close();
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   u_id  User-ID
     *
     * @return  true, wenn sich der uebergebene Nutzer im Netto Modus befindet. Sonst false
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    public Vector<NetModusAction> getNetModes(final int u_id) throws SQLException {
        final String query = "SELECT action, time FROM tt_timesheet WHERE (action = "
                    + TimeTrackerConstants.NET_HOURS_OF_WORK_START
                    + " OR action = " + TimeTrackerConstants.NET_HOURS_OF_WORK_END + ") AND u_id = " + u_id
                    + " ORDER BY time DESC ";
        final ResultSet actions = execute(query.toString());
        final Vector<NetModusAction> mods = new Vector<NetModusAction>();

        if (actions != null) {
            final GregorianCalendar tmp = new GregorianCalendar();
            tmp.add(GregorianCalendar.DATE, 1);
            String lastDay = TimeTrackerFunctions.getDateString(tmp);

            while (actions.next()) {
                final GregorianCalendar time = new GregorianCalendar();
                time.setTimeInMillis(actions.getTimestamp(2).getTime());

                if (!lastDay.equals(TimeTrackerFunctions.getDateString(time))) {
                    final NetModusAction action = new NetModusAction();
                    action.setTime(time);
                    action.setModus(((actions.getInt(1) == TimeTrackerConstants.NET_HOURS_OF_WORK_START)
                            ? NetModusAction.START : NetModusAction.END));
                    mods.add(action);
                    lastDay = TimeTrackerFunctions.getDateString(time);
                }
            }
            actions.close();
        }

        return mods;
    }

    /**
     * liefert das letzte Konto reset des uebergebenen Benutzers. Falls kein Reset existiert, dann wird null
     * zurueckgegeben
     *
     * @param   u_id  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    @Override
    public GregorianCalendar getLastReset(final int u_id) throws SQLException {
        final String lastResetQuery = "SELECT max(date_trunc('day', time)) AS dayOfReset "
                    + "FROM 	tt_timesheet ts, tt_timesheet_action ta "
                    + "WHERE	ts.action = ta.id AND ta.actionname = 'ACCOUNT RESET' AND "
                    + " 	    u_id = " + u_id + " AND "
                    + " 		date_trunc('day',time) <= date_trunc('day', timestamp '"
                    + TimeTrackerFunctions.getDateString(new GregorianCalendar()) + "')";
        final ResultSet rSet = execute(lastResetQuery.toString());

        GregorianCalendar lastReset = null;

        if (rSet != null) {
            if (rSet.next() && (rSet.getTimestamp(1) != null)) {
                lastReset = new GregorianCalendar();
                lastReset.setTimeInMillis(rSet.getTimestamp(1).getTime());
            }
            rSet.close();
        }

        return lastReset;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   u_id          DOCUMENT ME!
     * @param   date          DOCUMENT ME!
     * @param   timeInterval  gueltige Werte zum Beispiel: year, month
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    @Override
    public Vector<ProjectInfos> getProjectSubsequents(final int u_id,
            final GregorianCalendar date,
            final String timeInterval) throws SQLException {
        final Vector<ProjectInfos> result = new Vector<ProjectInfos>();
        final String projectSubsequentQuery = "SELECT p.title, time, duration_in_hours, ta.actionname "
                    + "FROM	tt_timesheet ts, tt_timesheet_action ta, tt_projects p "
                    + "WHERE	ts.action = ta.id AND ts.u_id = " + u_id + " AND "
                    + "ts.project_id = p.id AND (ta.actionname = 'PROJECT SUBSEQUENT' OR ta.actionname = 'CORRECTION') "
                    + "AND "
                    + "date_trunc('" + timeInterval + "',time) = date_trunc('" + timeInterval + "', timestamp '"
                    + TimeTrackerFunctions.getDateString(date) + "') "
                    + "ORDER BY time ";

        final ResultSet subsequent = execute(projectSubsequentQuery);

        if (subsequent != null) {
            while (subsequent.next()) {
                final ProjectInfos tmp = new ProjectInfos();
                final GregorianCalendar time = new GregorianCalendar();
                time.setTimeInMillis(subsequent.getTimestamp(2).getTime());

                tmp.setTitle(subsequent.getString(1));
                tmp.setTime(time);
                tmp.setDuration(subsequent.getDouble(3));
                tmp.setAction(subsequent.getString(4));

                result.add(tmp);
            }
            subsequent.close();
        }

        return result;
    }

    /**
     * liefert das Datum des ersten Vertrags des uebergebenen Benutzers.
     *
     * @param   u_id  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    @Override
    public GregorianCalendar getDateOfFirstContract(final int u_id) throws SQLException {
        GregorianCalendar result = null;
        final String contractQuery = "SELECT from_date "
                    + "FROM tt_contracts "
                    + "WHERE u_id = " + u_id
                    + " ORDER BY from_date "
                    + " LIMIT 1";
        final ResultSet rSet = execute(contractQuery.toString());

        if (rSet != null) {
            if (rSet.next()) {
                result = new GregorianCalendar();
                result.setTimeInMillis(rSet.getTimestamp(1).getTime());
            }
            rSet.close();
        }

        return result;
    }

    /**
     * liefert die Anzahl der Fehltage wegen Krankheit des aktuellen Jahres.
     *
     * @param   u_id        DOCUMENT ME!
     * @param   timeOfWork  Soll-Tagesarbeitszeit
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    @Override
    public double getIllnessDays(final int u_id, double timeOfWork) throws SQLException {
        double result = 0.0;
        if (timeOfWork == 0) {
            // es existiert kein Vertrag oder Arbeitszeit = 0
            timeOfWork = 1;
        }

        final String illnessQuery = "SELECT sum( coalesce(duration_in_hours, " + timeOfWork + ") ) / " + timeOfWork
                    + " "
                    + "FROM tt_timesheet ts, tt_timesheet_action ta "
                    + "WHERE 	ts.action = ta.id AND "
                    + "actionname = 'ILLNESSHOURS' AND "
                    + "ts.u_id = " + u_id + " AND "
                    + "date_trunc('year',time) = date_trunc('year', timestamp '"
                    + TimeTrackerFunctions.getDateString((new GregorianCalendar())) + "')";
        final ResultSet rSet = execute(illnessQuery.toString());

        if (rSet != null) {
            if (rSet.next()) {
                result = (int)(rSet.getDouble(1) * 100) / 100.0;
            } else {
                result = 0.0;
            }
            rSet.close();
        }

        return result;
    }

    /**
     * prueft, ob die Verbindung ordnungsgemaess aufgebaut wurde.
     *
     * @return  wenn die Verbindung ordnungsgemaess aufgebaut wurde, dann wird true zurueckgegeben
     */
    @Override
    public boolean isConnectionOk() {
        return conn != null;
    }

    /**
     * liefert eine Fehlermeldung.
     *
     * @return  im Fehlerfall wird die Fehlermeldung geliefert, sonst null
     */
    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * prueft, ob der uebergebene Wert existiert.
     *
     * @param   value  DOCUMENT ME!
     * @param   col    DOCUMENT ME!
     * @param   table  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean exist(final String value, final String col, final String table) {
        final ResultSet rSet;
        boolean valueExist = false;

        try {
            if (conn != null) {
                final String sqlQuery = "SELECT " + col + " FROM " + table + "WHERE " + col + " = " + value;
                rSet = stmt.executeQuery(sqlQuery);

                if (rSet.next()) {
                    valueExist = true;
                } else {
                    valueExist = false;
                }
            }
        } catch (SQLException se) {
            System.err.println(se.getMessage());
        }
        return valueExist;
    }

    /**
     * fuehrt die uebergebene SQL-Anweisung aus.
     *
     * @param   sqlQuery  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    @Override
    public ResultSet execute(final String sqlQuery) throws SQLException {
        ResultSet rSet = null;

        if (conn != null) {
            // legt neues Statement an, damit mehrere ResultSet gleichzeitig leben koennen
            rSet = conn.createStatement().executeQuery(sqlQuery);
        }
        return rSet;
    }

    /**
     * fuehrt die uebergebene SQL-Anweisung aus.
     *
     * @param   sqlQuery  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    @Override
    public void executeUpdate(final String sqlQuery) throws SQLException {
        if (conn != null) {
            stmt.executeUpdate(sqlQuery);
        } else {
            throw new SQLException("Keine Datenbankverbindung");
        }
    }

    /**
     * fuehrt das uebergebene Insert-Statement aus und liefert die beim neuen Eintrag verwendete OID zurueck.
     *
     * @param   sqlQuery  enthaelt das auszufuehrende Insert-Kommando
     *
     * @return  die verwendete oid oder 0, falls keine oid generiert wurde
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    @Override
    public long executeInsert(final String sqlQuery) throws SQLException {
        long oid = 0;
        final ResultSet rSet;

        if (conn != null) {
            final Statement statement = conn.createStatement();
            statement.executeUpdate(sqlQuery);
            rSet = stmt.getGeneratedKeys();

            if ((rSet != null) && rSet.next()) {
                oid = rSet.getLong("oid");
            }
        } else {
            throw new SQLException("Keine Datenbankverbindung");
        }

        System.out.println("neue oid: " + oid);
        return oid;
    }

    /**
     * liefert die groesste ID der angegebenen Tabelle.
     *
     * @param   tablename  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    @Override
    public int getMaxId(final String tablename) throws SQLException {
        int maxId = -2;

        if (conn != null) {
            final ResultSet rs = stmt.executeQuery("SELECT max(id) FROM " + tablename);
            if (rs.next()) {
                maxId = rs.getInt(1);
            }
        } else {
            throw new SQLException("Keine Datenbankverbindung");
        }

        return maxId;
    }

    /**
     * liefert das Verbindungsobjekt.
     *
     * @return  das Verbindungsobjekt
     */
    @Override
    public Connection getConnection() {
        return conn;
    }

    @Override
    public void close() {
        try {
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
        }
    }

    @Override
    public void finalize() {
        try {
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
        }
    }

    @Override
    public int getIdByName(final String firstName, final String lastName) throws SQLException {
        int id = -1;

        final ResultSet rSet = execute("SELECT id FROM tt_user where name ilike '" + firstName + " " + lastName + '\''
                        + " limit 1");

        if (rSet != null) {
            if (rSet.next()) {
                id = rSet.getInt(1);
            }

            rSet.close();
        }

        return id;
    }
}
