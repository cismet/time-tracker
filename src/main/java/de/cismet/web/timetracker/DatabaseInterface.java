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

import java.util.GregorianCalendar;
import java.util.Vector;

import de.cismet.web.timetracker.types.ContractInfos;
import de.cismet.web.timetracker.types.ProjectInfos;
import de.cismet.web.timetracker.types.TimeDurationPair;
import de.cismet.web.timetracker.types.TimesheetSet;
import de.cismet.web.timetracker.types.TitleTimePair;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface DatabaseInterface {

    //~ Methods ----------------------------------------------------------------

    /**
     * fuehrt die uebergebene SQL-Anweisung aus.
     *
     * @param   sqlQuery  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    ResultSet execute(String sqlQuery) throws SQLException;

    /**
     * fuehrt das uebergebene Insert-Statement aus und liefert die beim neuen Eintrag verwendete OID zurueck.
     *
     * @param   sqlQuery  enthaelt das auszufuehrende Insert-Kommando
     *
     * @return  die verwendete oid oder 0, falls keine oid generiert wurde
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    long executeInsert(String sqlQuery) throws SQLException;

    /**
     * fuehrt die uebergebene SQL-Anweisung aus.
     *
     * @param   sqlQuery  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    void executeUpdate(String sqlQuery) throws SQLException;

    /**
     * prueft, ob der uebergebene Wert existiert.
     *
     * @param   value  DOCUMENT ME!
     * @param   col    DOCUMENT ME!
     * @param   table  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean exist(String value, String col, String table);

    /**
     * liefert das Verbindungsobjekt.
     *
     * @return  das Verbindungsobjekt
     */
    Connection getConnection();

    /**
     * liefert das Datum des ersten Vertrags des uebergebenen Benutzers.
     *
     * @param   u_id  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    GregorianCalendar getDateOfFirstContract(int u_id) throws SQLException;

    /**
     * liefert eine Fehlermeldung.
     *
     * @return  im Fehlerfall wird die Fehlermeldung geliefert, sonst null
     */
    String getErrorMessage();

    /**
     * DOCUMENT ME!
     *
     * @param   u_id  DOCUMENT ME!
     * @param   year  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    Vector<TimeDurationPair> getHolidayCorrectionsForYear(int u_id, GregorianCalendar year) throws SQLException;

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
    int getHolidayForYear(int u_id, GregorianCalendar year) throws SQLException;

    /**
     * liefert einen SQL-String zur Abfrage der Ferien des uebergebenen Users seit since.
     *
     * @param   u_id   DOCUMENT ME!
     * @param   since  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getHolidayQueryString(int u_id, GregorianCalendar since);

    /**
     * DOCUMENT ME!
     *
     * @param   uId     DOCUMENT ME!
     * @param   toTime  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    ResultSet getHolidaysForYear(int uId, GregorianCalendar toTime) throws SQLException;

    /**
     * DOCUMENT ME!
     *
     * @param   uId  DOCUMENT ME!
     * @param   day  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    ContractInfos getHoursOfWork(int uId, GregorianCalendar day) throws SQLException;

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
    int getIdByBuddyName(String buddy) throws SQLException;

    /**
     * DOCUMENT ME!
     *
     * @param   firstName  DOCUMENT ME!
     * @param   lastName   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    int getIdByName(String firstName, String lastName) throws SQLException;

    /**
     * DOCUMENT ME!
     *
     * @param   uId     DOCUMENT ME!
     * @param   toTime  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    ResultSet getIllnessForYear(int uId, GregorianCalendar toTime) throws SQLException;

    /**
     * liefert einen SQL-String zur Abfrage der Krankheitstage des uebergebenen Users seit since.
     *
     * @param   u_id   DOCUMENT ME!
     * @param   since  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getIllnessQueryString(int u_id, GregorianCalendar since);

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
    GregorianCalendar getLastReset(int u_id) throws SQLException;

    /**
     * liefert die groesste ID der angegebenen Tabelle.
     *
     * @param   tablename  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    int getMaxId(String tablename) throws SQLException;

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
    Vector<TitleTimePair> getProjectComes(int u_id, GregorianCalendar date, String timeInterval) throws SQLException;

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
    Vector<ProjectInfos> getProjectSubsequents(int u_id, GregorianCalendar date, String timeInterval)
            throws SQLException;

    /**
     * DOCUMENT ME!
     *
     * @param   u_id  DOCUMENT ME!
     * @param   year  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    Vector<TimeDurationPair> getUsedHolidaysForYear(int u_id, GregorianCalendar year) throws SQLException;

    /**
     * DOCUMENT ME!
     *
     * @param   uId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    boolean hasAutopause(int uId) throws SQLException;

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
    double getIllnessDays(int u_id, double timeOfWork) throws SQLException;

    /**
     * prueft, ob die Verbindung ordnungsgemaess aufgebaut wurde.
     *
     * @return  wenn die Verbindung ordnungsgemaess aufgebaut wurde, dann wird true zurueckgegeben
     */
    boolean isConnectionOk();

    /**
     * DOCUMENT ME!
     */
    void close();
}
