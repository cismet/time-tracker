/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.web.timetracker;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Vector;

import de.cismet.web.timetracker.types.ContractInfos;
import de.cismet.web.timetracker.types.NetModusAction;
import de.cismet.web.timetracker.types.ProjectInfos;
import de.cismet.web.timetracker.types.TimeDurationPair;
import de.cismet.web.timetracker.types.TimesheetSet;
import de.cismet.web.timetracker.types.TitleTimePair;

/**
 * Diese Klasse dient als Cache fuer die Datenbankklasse.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class DatabaseCache implements DatabaseInterface {

    //~ Instance fields --------------------------------------------------------

    private Database db;
    private Hashtable<Integer, Boolean> autoPause = new Hashtable<Integer, Boolean>();
    private Hashtable<String, TimesheetSet> timeOfWorkCache;
    private ArrayList<ContractInfos> contractData = new ArrayList<ContractInfos>();
    private Hashtable<Integer, Vector<NetModusAction>> netMods = new Hashtable<Integer, Vector<NetModusAction>>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DatabaseCache object.
     *
     * @param  db  DOCUMENT ME!
     */
    public DatabaseCache(final Database db) {
        this.db = db;
        this.timeOfWorkCache = new Hashtable<String, TimesheetSet>();
    }

    /**
     * Creates a new DatabaseCache object.
     *
     * @param  applicationPath  DOCUMENT ME!
     */
    public DatabaseCache(final String applicationPath) {
        this.db = new Database(applicationPath);
    }

    /**
     * Creates a new DatabaseCache object.
     *
     * @param  driverName  DOCUMENT ME!
     * @param  url         DOCUMENT ME!
     * @param  user        DOCUMENT ME!
     * @param  pwd         DOCUMENT ME!
     */
    public DatabaseCache(final String driverName, final String url, final String user, final String pwd) {
        this.db = new Database(driverName, url, user, pwd);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * public TimesheetSet getTimeOfWork(int uId, GregorianCalendar time) throws SQLException { return
     * db.getTimeOfWork(uId, time); }.
     *
     * @param   uId   DOCUMENT ME!
     * @param   time  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    public TimesheetSet getTimeOfWork(final int uId, final GregorianCalendar time) throws SQLException {
        final String key = uId + TimeTrackerConstants.dateFormater.format(time.getTime());
        final TimesheetSet timeOfWork = timeOfWorkCache.get(key);

        if (timeOfWork == null) {
            final GregorianCalendar startTime = (GregorianCalendar)time.clone();
            startTime.add(GregorianCalendar.DATE, -5);

            final GregorianCalendar endTime = (GregorianCalendar)time.clone();
            endTime.add(GregorianCalendar.DATE, 5);
            final Vector<TimesheetSet> timesheets = db.getTimeOfWork(uId, startTime, 10);
            for (final TimesheetSet tmp : timesheets) {
                final String tmpKey = uId + TimeTrackerConstants.dateFormater.format(tmp.next().getTime().getTime());
                tmp.previous();
                if (timeOfWorkCache.get(tmpKey) == null) {
                    timeOfWorkCache.put(tmpKey, tmp);
                }
            }

            for (final GregorianCalendar i = (GregorianCalendar)startTime.clone(); i.before(endTime);
                        i.add(GregorianCalendar.DATE, 1)) {
                // TimesheetSet Objekte fuer die Tage, die keine Buchungen enthalten, einfuegen
                final String tmpKey = uId + TimeTrackerConstants.dateFormater.format(i.getTime().getTime());
                if (timeOfWorkCache.get(tmpKey) == null) {
                    timeOfWorkCache.put(tmpKey, new TimesheetSet());
                }
            }
        }

        TimesheetSet res = timeOfWorkCache.get(key);

        res = ((res != null) ? res : new TimesheetSet());
        res.setBegin();

        return res;
    }

    @Override
    public boolean hasAutopause(final int uId) throws SQLException {
        final Boolean result = autoPause.get(uId);
        if (result != null) {
            return result;
        } else {
            final boolean hasAutopause = db.hasAutopause(uId);
            autoPause.put(uId, hasAutopause);
            return hasAutopause;
        }
    }

    @Override
    public ContractInfos getHoursOfWork(final int uId, final GregorianCalendar day) throws SQLException {
        // falls schon im Cache
        ContractInfos contract = null;

        for (final ContractInfos tmp : contractData) {
            if (tmp.getUId() == uId) {
                if (isDateBetween(day, tmp.getFromDate(), tmp.getToDate())) {
                    contract = tmp;
                    break;
                }
            }
        }

        if (contract == null) {
            contract = db.getHoursOfWork(uId, day);

            if (contract != null) {
                contractData.add(contract);
            }
        }

        if (contract == null) {
            System.out.println("Fuer den Benutzer " + uId + " existieren Buchungen fuer den Tag "
                        + TimeTrackerFunctions.getDateString(day) + ". Es existiert allerdings noch kein Vertrag.");
        }

        try {
            if (contract != null) {
                contract = (ContractInfos)contract.clone();
            } else {
                contract = new ContractInfos(day, day, uId, 0.0);
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return contract;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   u_id  User-ID
     * @param   day   Tag, bei dem geprueft werden soll, ob sich der Nutzer im netto Modus befindet
     *
     * @return  true, wenn sich der uebergebene Nutzer im Netto Modus befindet. Sonst false
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    public boolean isUserInNetMode(final int u_id, final GregorianCalendar day) throws SQLException {
        if (netMods.get(u_id) == null) {
            netMods.put(u_id, db.getNetModes(u_id));
        }

        final Vector<NetModusAction> allMods = netMods.get(u_id);
        int lastMod = NetModusAction.END;

        for (int i = (allMods.size() - 1); i >= 0; --i) {
            if (TimeTrackerFunctions.isDateLess(allMods.get(i).getTime(), day)
                        || TimeTrackerFunctions.isSameDate(allMods.get(i).getTime(), day)) {
                lastMod = allMods.get(i).getModus();
            } else {
                break;
            }
        }

        return (lastMod == NetModusAction.START);
    }

    /**
     * liefert den Jahresurlaub des �bergebenen Benutzers fuer das uebergebene Jahr. Wenn year == null, dann gilt das
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
    public int getHolidayForYear(final int u_id, final GregorianCalendar year) throws SQLException {
        return db.getHolidayForYear(u_id, year);
    }

    @Override
    public Vector<TimeDurationPair> getUsedHolidaysForYear(final int u_id, final GregorianCalendar year)
            throws SQLException {
        return db.getUsedHolidaysForYear(u_id, year);
    }

    @Override
    public Vector<TimeDurationPair> getHolidayCorrectionsForYear(final int u_id, final GregorianCalendar year)
            throws SQLException {
        return db.getHolidayCorrectionsForYear(u_id, year);
    }

    @Override
    public String getHolidayQueryString(final int u_id, final GregorianCalendar since) {
        return db.getHolidayQueryString(u_id, since);
    }

    @Override
    public String getIllnessQueryString(final int u_id, final GregorianCalendar since) {
        return db.getIllnessQueryString(u_id, since);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   date  DOCUMENT ME!
     * @param   from  DOCUMENT ME!
     * @param   to    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isDateBetween(final GregorianCalendar date,
            final GregorianCalendar from,
            final GregorianCalendar to) {
        final String dayAsString = TimeTrackerFunctions.getDateString(date);

        if ((from.before(date) || dayAsString.equals(TimeTrackerFunctions.getDateString(from)))) {
            if ((to == null) || date.before(to) || dayAsString.equals(TimeTrackerFunctions.getDateString(to))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ResultSet getHolidaysForYear(final int uId, final GregorianCalendar toTime) throws SQLException {
        return db.getHolidaysForYear(uId, toTime);
    }

    @Override
    public ResultSet getIllnessForYear(final int uId, final GregorianCalendar toTime) throws SQLException {
        return db.getIllnessForYear(uId, toTime);
    }

    @Override
    public Vector<TitleTimePair> getProjectComes(final int u_id,
            final GregorianCalendar date,
            final String timeInterval) throws SQLException {
        return db.getProjectComes(u_id, date, timeInterval);
    }

    @Override
    public GregorianCalendar getLastReset(final int u_id) throws SQLException {
        return db.getLastReset(u_id);
    }

    @Override
    public Vector<ProjectInfos> getProjectSubsequents(final int u_id,
            final GregorianCalendar date,
            final String timeInterval) throws SQLException {
        return db.getProjectSubsequents(u_id, date, timeInterval);
    }

    @Override
    public GregorianCalendar getDateOfFirstContract(final int u_id) throws SQLException {
        return db.getDateOfFirstContract(u_id);
    }

    @Override
    public int getIdByBuddyName(final String buddy) throws SQLException {
        return db.getIdByBuddyName(buddy);
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
    public double getIllnessDays(final int u_id, final double timeOfWork) throws SQLException {
        return db.getIllnessDays(u_id, timeOfWork);
    }

    /**
     * prueft, ob die Verbindung ordnungsgemaess aufgebaut wurde.
     *
     * @return  wenn die Verbindung ordnungsgemaess aufgebaut wurde, dann wird true zurueckgegeben
     */
    @Override
    public boolean isConnectionOk() {
        return db.isConnectionOk();
    }

    /**
     * liefert eine Fehlermeldung.
     *
     * @return  im Fehlerfall wird die Fehlermeldung geliefert, sonst null
     */
    @Override
    public String getErrorMessage() {
        return db.getErrorMessage();
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
        return db.exist(value, col, table);
    }

    @Override
    public ResultSet execute(final String sqlQuery) throws SQLException {
        return db.execute(sqlQuery);
    }

    @Override
    public void executeUpdate(final String sqlQuery) throws SQLException {
        db.execute(sqlQuery);
    }

    @Override
    public long executeInsert(final String sqlQuery) throws SQLException {
        return db.executeInsert(sqlQuery);
    }

    @Override
    public int getMaxId(final String tablename) throws SQLException {
        return db.getMaxId(tablename);
    }

    @Override
    public Connection getConnection() {
        return db.getConnection();
    }

    @Override
    public void close() {
        db.close();
    }

    @Override
    public int getIdByName(final String firstName, final String lastName) throws SQLException {
        return db.getIdByName(firstName, lastName);
    }
}
