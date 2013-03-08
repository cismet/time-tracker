/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cismet.web.timetracker;

import de.cismet.web.timetracker.types.ContractInfos;
import de.cismet.web.timetracker.types.ProjectInfos;
import de.cismet.web.timetracker.types.TimeDurationPair;
import de.cismet.web.timetracker.types.TimesheetSet;
import de.cismet.web.timetracker.types.TitleTimePair;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.Vector;

/**
 *
 * @author therter
 */
public interface DatabaseInterface {

    /**
     * fuehrt die uebergebene SQL-Anweisung aus
     */
    ResultSet execute(String sqlQuery) throws SQLException;

    /**
     * fuehrt das uebergebene Insert-Statement aus und liefert die
     * beim neuen Eintrag verwendete OID zurueck
     *
     * @param sqlQuery enthaelt das auszufuehrende Insert-Kommando
     * @return die verwendete oid oder 0, falls keine oid generiert wurde
     */
    long executeInsert(String sqlQuery) throws SQLException;

    /**
     * fuehrt die uebergebene SQL-Anweisung aus
     */
    void executeUpdate(String sqlQuery) throws SQLException;

    /**
     * prueft, ob der uebergebene Wert existiert
     */
    boolean exist(String value, String col, String table);

    /**
     * liefert das Verbindungsobjekt
     * @return das Verbindungsobjekt
     */
    Connection getConnection();

    /**
     * liefert das Datum des ersten Vertrags des uebergebenen Benutzers
     */
    GregorianCalendar getDateOfFirstContract(int u_id) throws SQLException;

    /**
     * liefert eine Fehlermeldung
     * @return im Fehlerfall wird die Fehlermeldung geliefert, sonst null
     */
    String getErrorMessage();

    Vector<TimeDurationPair> getHolidayCorrectionsForYear(int u_id, GregorianCalendar year) throws SQLException;

    /**
     * liefert den Jahresurlaub des uebergebenen Benutzers fuer das uebergebene Jahr.
     * Wenn year == null, dann gilt das aktuelle Jahr.
     */
    int getHolidayForYear(int u_id, GregorianCalendar year) throws SQLException;

    /**
     * liefert einen SQL-String zur Abfrage der Ferien des uebergebenen Users seit since
     */
    String getHolidayQueryString(int u_id, GregorianCalendar since);

    ResultSet getHolidaysForYear(int uId, GregorianCalendar toTime) throws SQLException;

    ContractInfos getHoursOfWork(int uId, GregorianCalendar day) throws SQLException;

    /**
     * liefert die zum uebergebenen Buddyname gehoerende id
     * @param buddy Buuddyname
     * @return die zum uebergebenen Buddynamen gehoerende ID.
     * Falls der Buddyname nicht existiert, dann wird 1 zurueckgegeben.
     */
    int getIdByBuddyName(String buddy) throws SQLException;

    int getIdByName(String firstName, String lastName) throws SQLException;

    ResultSet getIllnessForYear(int uId, GregorianCalendar toTime) throws SQLException;

    /**
     * liefert einen SQL-String zur Abfrage der Krankheitstage des uebergebenen Users seit since
     */
    String getIllnessQueryString(int u_id, GregorianCalendar since);

    /**
     * liefert das letzte Konto reset des uebergebenen Benutzers.
     * Falls kein Reset existiert, dann wird null zurueckgegeben
     */
    GregorianCalendar getLastReset(int u_id) throws SQLException;

    /**
     * liefert die groesste ID der angegebenen Tabelle
     */
    int getMaxId(String tablename) throws SQLException;

    /**
     * liefert einen Vector mit den PROJECT COME-Aktionen, die fuer die Berechnung der Projektzeiten
     * erforderlich sind. Also alle, ab dem ersten, das in den uebergebenen Zeitraum hineinreicht
     *
     * @param timeInterval gueltige Werte zum Beispiel: year, month
     */
    Vector<TitleTimePair> getProjectComes(int u_id, GregorianCalendar date, String timeInterval) throws SQLException;

    /**
     *
     *
     * @param timeInterval gueltige Werte zum Beispiel: year, month
     */
    Vector<ProjectInfos> getProjectSubsequents(int u_id, GregorianCalendar date, String timeInterval) throws SQLException;

    Vector<TimeDurationPair> getUsedHolidaysForYear(int u_id, GregorianCalendar year) throws SQLException;

    boolean hasAutopause(int uId) throws SQLException;

    /**
     *	liefert die Anzahl der Fehltage wegen Krankheit des aktuellen Jahres
     *  @param timeOfWork Soll-Tagesarbeitszeit
     */
    public double getIllnessDays(int u_id, double timeOfWork) throws SQLException;

    
    /**
     * prueft, ob die Verbindung ordnungsgemaess aufgebaut wurde
     * @return wenn die Verbindung ordnungsgemaess aufgebaut wurde, dann wird true zurueckgegeben
     */
    boolean isConnectionOk();
    
    public void close();

}
