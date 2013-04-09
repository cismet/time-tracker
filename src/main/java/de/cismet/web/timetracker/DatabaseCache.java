/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cismet.web.timetracker;

import de.cismet.web.timetracker.types.ContractInfos;
import de.cismet.web.timetracker.types.NetModusAction;
import de.cismet.web.timetracker.types.ProjectInfos;
import de.cismet.web.timetracker.types.TimeDurationPair;
import de.cismet.web.timetracker.types.TimesheetSet;
import de.cismet.web.timetracker.types.TitleTimePair;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Diese Klasse dient als Cache fuer die Datenbankklasse
 * @author therter
 */
public class DatabaseCache implements DatabaseInterface {
    private Database db;
    private Hashtable<Integer, Boolean> autoPause = new Hashtable<Integer, Boolean>();
    private Hashtable<String, TimesheetSet> timeOfWorkCache;
    private ArrayList<ContractInfos> contractData = new ArrayList<ContractInfos>();
    private Hashtable<Integer, Vector<NetModusAction>> netMods = new Hashtable<Integer, Vector<NetModusAction>>();
    
    public DatabaseCache(Database db) {
        this.db = db;
        this.timeOfWorkCache = new Hashtable<String, TimesheetSet>();
    }

    public DatabaseCache(String applicationPath) {
        this.db = new Database(applicationPath);
    }


    public DatabaseCache(String driverName, String url, String user, String pwd) {
        this.db = new Database(driverName, url, user, pwd);
    }    

    /*
    public TimesheetSet getTimeOfWork(int uId, GregorianCalendar time) throws SQLException {
        return db.getTimeOfWork(uId, time);
    }    
     */
     
    public TimesheetSet getTimeOfWork(int uId, GregorianCalendar time) throws SQLException {
        String key = uId + TimeTrackerConstants.dateFormater.format(time.getTime());
        TimesheetSet timeOfWork = timeOfWorkCache.get(key);
        
        if (timeOfWork == null) {
            GregorianCalendar startTime = (GregorianCalendar)time.clone();
            startTime.add(GregorianCalendar.DATE, - 5);

            GregorianCalendar endTime = (GregorianCalendar)time.clone();
            endTime.add(GregorianCalendar.DATE, 5);
            Vector<TimesheetSet> timesheets = db.getTimeOfWork(uId, startTime, 10);
            for (TimesheetSet tmp : timesheets) {
                String tmpKey = uId + TimeTrackerConstants.dateFormater.format(tmp.next().getTime().getTime());
                tmp.previous();
                if (timeOfWorkCache.get(tmpKey) == null) {
                    timeOfWorkCache.put(tmpKey, tmp);
                }
            }
            
            
            for (GregorianCalendar i = (GregorianCalendar)startTime.clone(); i.before(endTime); i.add(GregorianCalendar.DATE, 1) ) {
                // TimesheetSet Objekte fuer die Tage, die keine Buchungen enthalten, einfuegen
                String tmpKey = uId + TimeTrackerConstants.dateFormater.format(i.getTime().getTime());
                if ( timeOfWorkCache.get( tmpKey ) == null ) {
                    timeOfWorkCache.put(tmpKey, new TimesheetSet());
                }
            }
          
        }
        
        TimesheetSet res = timeOfWorkCache.get(key);
        
        res = (res != null ? res : new TimesheetSet());
        res.setBegin();

        return res;
    }

    
    public boolean hasAutopause(int uId) throws SQLException {
        Boolean result = autoPause.get(uId);
        if (result != null) {
            return result;
        } else {
            boolean hasAutopause = db.hasAutopause(uId);
            autoPause.put(uId, hasAutopause);
            return hasAutopause;
        }
    }

    
    public ContractInfos getHoursOfWork(int uId, GregorianCalendar day) throws SQLException {
        //falls schon im Cache
        ContractInfos contract = null;
        
        for (ContractInfos tmp : contractData) {
            if ( tmp.getUId() == uId)
                if ( isDateBetween(day, tmp.getFromDate(), tmp.getToDate()) ) {
                    contract = tmp;
                    break;
            }
        }
        
        if (contract == null) {
            contract = db.getHoursOfWork(uId, day);

            if (contract != null) {
                contractData.add(contract);
            }
        }
        
        if (contract == null){
            System.out.println("Fuer den Benutzer " + uId + " existieren Buchungen fuer den Tag " + TimeTrackerFunctions.getDateString(day) + ". Es existiert allerdings noch kein Vertrag.");
        }
        
        try {
            if (contract != null){
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
     * @param u_id User-ID
     * @param day Tag, bei dem geprueft werden soll, ob sich der Nutzer im netto Modus befindet
     * @return true, wenn sich der uebergebene Nutzer im Netto Modus befindet. Sonst false
     */
    public boolean isUserInNetMode(int u_id, GregorianCalendar day) throws SQLException {
        if (netMods.get(u_id) == null) {
            netMods.put(u_id, db.getNetModes(u_id));
        }
        
        Vector<NetModusAction> allMods = netMods.get(u_id);
        int lastMod = NetModusAction.END;
        
        for (int i = (allMods.size() - 1); i >=0 ; --i) {
            if ( TimeTrackerFunctions.isDateLess(allMods.get(i).getTime(), day) || TimeTrackerFunctions.isSameDate(allMods.get(i).getTime(), day) ) {
                lastMod = allMods.get(i).getModus();
            } else {
                break;
            }
        }
        
        return (lastMod == NetModusAction.START);
    }
    
    
    /**
     *	liefert den Jahresurlaub des �bergebenen Benutzers fuer das uebergebene Jahr.
     *	Wenn year == null, dann gilt das aktuelle Jahr.
     */
    public int getHolidayForYear(int u_id, GregorianCalendar year) throws SQLException {
        return db.getHolidayForYear(u_id, year);
    }

    
    public Vector<TimeDurationPair> getUsedHolidaysForYear(int u_id, GregorianCalendar year) throws SQLException {
        return db.getUsedHolidaysForYear(u_id, year);
    }

    
    public Vector<TimeDurationPair> getHolidayCorrectionsForYear(int u_id, GregorianCalendar year) throws SQLException {
        return db.getHolidayCorrectionsForYear(u_id, year);
    }

    
    public String getHolidayQueryString(int u_id, GregorianCalendar since) {
        return db.getHolidayQueryString(u_id, since);
    }

    
    public String getIllnessQueryString(int u_id, GregorianCalendar since) {
        return db.getIllnessQueryString(u_id, since);
    }
    
    
    private boolean isDateBetween(GregorianCalendar date, GregorianCalendar from, GregorianCalendar to) {
        String dayAsString = TimeTrackerFunctions.getDateString(date);
        
        if ((from.before(date) || dayAsString.equals( TimeTrackerFunctions.getDateString(from) ) ) ) {
            if ( to == null || date.before(to) || dayAsString.equals(TimeTrackerFunctions.getDateString(to))  ) {
                return true;
            }
        }        
        return false;
    }

    
   
    public ResultSet getHolidaysForYear(int uId, GregorianCalendar toTime) throws SQLException {
        return db.getHolidaysForYear(uId, toTime);
    }
    

   
    public ResultSet getIllnessForYear(int uId, GregorianCalendar toTime) throws SQLException {
        return db.getIllnessForYear(uId, toTime);
    }

    
    public Vector<TitleTimePair> getProjectComes(int u_id, GregorianCalendar date, String timeInterval) throws SQLException {
        return db.getProjectComes(u_id, date, timeInterval);
    }

   
    public GregorianCalendar getLastReset(int u_id) throws SQLException {
        return db.getLastReset(u_id);
    }

   
    public Vector<ProjectInfos> getProjectSubsequents(int u_id, GregorianCalendar date, String timeInterval) throws SQLException {
        return db.getProjectSubsequents(u_id, date, timeInterval);
    }

   
    public GregorianCalendar getDateOfFirstContract(int u_id) throws SQLException {
        return db.getDateOfFirstContract(u_id);
    }

    
   
    public int getIdByBuddyName(String buddy) throws SQLException{
        return db.getIdByBuddyName(buddy);
    }

    /**
     *	liefert die Anzahl der Fehltage wegen Krankheit des aktuellen Jahres
     *  @param timeOfWork Soll-Tagesarbeitszeit
     */
    public double getIllnessDays(int u_id, double timeOfWork) throws SQLException {
        return db.getIllnessDays(u_id, timeOfWork);
    }
    
    
    /**
     * prueft, ob die Verbindung ordnungsgemaess aufgebaut wurde
     * @return wenn die Verbindung ordnungsgemaess aufgebaut wurde, dann wird true zurueckgegeben
     */
    public boolean isConnectionOk(){
        return db.isConnectionOk();
    }

    /**
     * liefert eine Fehlermeldung
     * @return im Fehlerfall wird die Fehlermeldung geliefert, sonst null
     */
    public String getErrorMessage(){
        return db.getErrorMessage();
    }

	
    /**
     *prueft, ob der uebergebene Wert existiert
     */
    public boolean exist(String value, String col, String table) {
        return db.exist(value, col, table);
    }    
    
    
    public ResultSet execute(String sqlQuery) throws SQLException {
        return db.execute(sqlQuery);
    }

    public void executeUpdate(String sqlQuery) throws SQLException {
        db.execute(sqlQuery);
    }

    public long executeInsert(String sqlQuery) throws SQLException {
        return db.executeInsert(sqlQuery);
    }
    
    
    public int getMaxId(String tablename) throws SQLException {
        return db.getMaxId(tablename);
    }    
    
    public Connection getConnection() {
        return db.getConnection();
    }

    @Override
    public void close() {
        db.close();
    }

    public int getIdByName(String firstName, String lastName) throws SQLException {
        return db.getIdByName(firstName, lastName);
    }
}
