package de.cismet.web.timetracker;

import de.cismet.web.timetracker.types.HoursOfWork;
import de.cismet.web.timetracker.types.TimesheetEntry;
import de.cismet.web.timetracker.types.TimesheetSet;
import de.cismet.web.timetracker.types.HolidayStruct;
import de.cismet.web.timetracker.types.ContractInfos;
import de.cismet.web.timetracker.types.ProjectInfos;
import de.cismet.web.timetracker.types.TimeDurationPair;
import de.cismet.web.timetracker.types.TitleTimePair;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;



public class Query{
    private DatabaseCache db;
    
    
    public Query(Database db) {
        this.db = new DatabaseCache(db);
//        if ( !db.isConnectionOk() ) {
//            throw new SQLException("No connection to the database");
//        }
    }
    
    /**
     *	liefert die zum uebergebenen Buddyname gehoerende id
     *	@param buddy Buuddyname
     */
    public int getIdByBuddyName(String buddy) throws SQLException {
        int id = 1;
        
        id = db.getIdByBuddyName(buddy);

        return id;
    }
    
    
    /**
     * liefert die Tagesarbeitszeit des uebergebenen Benutzers am uebergebenen Tag
     *
     * @param u_id Benutzer-ID
     * @param day Tagt, fuer den die Arbeitszeit berechnet werden soll
     * @param work speichert die berechnete Arbeitszeit.
     * 				Wenn autoPause des work-Objektes gesetzt ist, dann wird 1 Stunde Pause abgezogen, falls
     *				keine Pause manuell eingetragen wurde und Arbeitszeit > TimeTrackerConstants.HOURS_WITHOUT_AUTO_PAUSE.
     * @throws SQLException	wird bei Datenbankfehler geworfen
     * @throws QueryException wird geworfen, wenn die, zur Kommen-Buchung gehoerende, Gehen-Buchung fehlt
     */
    public void getHoursOfWork(int u_id, GregorianCalendar day, HoursOfWork work) throws SQLException, QueryException{
        getHoursOfWork(u_id, day, work, null, null, true);
    }
    
    
    /**
     * liefert die Tagesarbeitszeit des uebergebenen Benutzers am uebergebenen Tag
     * (todo: Methode ueberarbeiten ist zu gross)
     * @param u_id Benutzer-ID
     * @param day Tagt, fuer den die Arbeitszeit berechnet werden soll
     * @param work speichert die berechnete Arbeitszeit.
     * 				Wenn autoPause des work-Objektes gesetzt ist, dann wird 1 Stunde Pause abgezogen, falls
     *				keine Pause manuell eingetragen wurde und Arbeitszeit > TimeTrackerConstants.HOURS_WITHOUT_AUTO_PAUSE.
     * @param fromTime Uhrzeit, an dem uebergebenen Tag, von der an die Arbeitszeit berechnet werden soll
     * @param toTime Uhrzeit, an dem uebergebenen Tag, bis zu der die Arbeitszeit berechnet werden soll
     * @param withCorrections  gibt an, ob Korrekturen beruecksichtigt werden sollen. Wenn false, dann werden nur Korrekturen ohne Projekt beruecksichtigt
     * @throws SQLException	wird bei Datenbankfehler geworfen
     * @throws QueryException wird geworfen, wenn die, zur Kommen-Buchung gehoerende, Gehen-Buchung fehlt
     */
    public void getHoursOfWork(int u_id, GregorianCalendar time, HoursOfWork work, GregorianCalendar fromTime, GregorianCalendar toTime, boolean withCorrections) throws SQLException, QueryException{
        work.setNeedAutoPause(false);
        GregorianCalendar day = time == null ? new GregorianCalendar() : time;
        boolean isToday = TimeTrackerFunctions.getDateString(day).equals(TimeTrackerFunctions.getDateString(new GregorianCalendar()));
        work.setHours(0.0);
        SimpleDateFormat formaterDB = new SimpleDateFormat("yyyy-MM-d");
        //       System.out.println(TimeTrackerConstants.dateFormater.format(day.getTime()));
        boolean netHoursOfWork = db.isUserInNetMode(u_id, day);
        TimesheetSet set = db.getTimeOfWork(u_id, day);
        TimesheetEntry entry;
        
        if ( set != null) {
            GregorianCalendar start = null;
            GregorianCalendar end = null;
            boolean firstCome = true;
            //speichert den Start einer Pause in Millis. Wenn 0, dann wurde noch keine Pause begonnen
            long startPause = 0;
            //speichert, ob es eine Pause gab, oder ob eine Auto-Pause hinzugefuegt werden soll
            boolean hasPause = false;
            
            while ( ( entry = set.next() ) != null) {
                
                if (entry.getAction() == TimeTrackerConstants.COME && (firstCome && !netHoursOfWork)  ) {                               //Arbeitsstart ermitteln
                    firstCome = false;
                    start = entry.getTime();
                    if ( fromTime != null && fromTime.after(start)){
                        start = fromTime;
                    }
                } else if(entry.getAction() == TimeTrackerConstants.GO && (TimeTrackerFunctions.isLastGo(set) && !netHoursOfWork) ) {	//Arbeitsende ermitteln
                    if (!(isToday && TimeTrackerFunctions.hasRelevantEntry(set))) {
                        end = entry.getTime();
                        
                        if (start == null) {
                            throw new QueryException("Fuer den " + formaterDB.format(end.getTime()) + " gibt es kein COME vor dem letzten GO");
                        }
                        
                        if(toTime != null && toTime.before(end)){
                            end = toTime;
                        }
                        work.setHours(work.getHours() + ( (end.getTimeInMillis() - start.getTimeInMillis()) / (1000.0 * 3600.0) ) );
                        
                        // letzte Pause ging bis zum Ende
                        if (startPause != 0) {
                            long endPause = entry.getTime().getTimeInMillis();
                            double duration = ((endPause - startPause) / (1000.0 * 3600.0));
                            
                            work.setHours(work.getHours() - duration);
                            hasPause = true;
                            startPause = 0;
                        }
                    }
                } else if (entry.getAction() == TimeTrackerConstants.PROJECT_SUBSEQUENT) {			//Projekt nachtraeglich
                    //Project-Subsequent wird nur gezaehlt, wenn fromTime und toTime == null
                    if(fromTime == null && toTime == null) {
                        if (entry.getTitle() != null && entry.getTitle().equals("Pause")) {
                            hasPause = true;
                            work.setHours(work.getHours() - entry.getDuration_in_hours());
                        } else{
                            if (start != null || TimeTrackerFunctions.hasRelevantEntry(set)) {
                            } else {											//nur Korrektur (ohne weitere Arbeitszeit)
//                                start = entry.getTime();
//
//                                end = (GregorianCalendar)start.clone();
//                                end.add(GregorianCalendar.MINUTE, (int)(entry.getDuration_in_hours() * 60));
                            }
//                            work.setHours(work.getHours() + entry.getDuration_in_hours());
                        }
                    } else {
                        //es muss trotzdem erkannt werden, ob eine Pause gemacht wurde
                        if (entry.getTitle() != null && entry.getTitle().equals("Pause")) {
                            hasPause = true;
                        }
                    }
                } else if(entry.getAction() == TimeTrackerConstants.COME && netHoursOfWork) {				//neue netto Arbeitszeit beginnt
                    if (start == null) {                                                                                //sonst waren 2 COMEs in Folge
                        start = entry.getTime();
                    }
                } else if(entry.getAction() == TimeTrackerConstants.GO && netHoursOfWork) {				//netto Arbeitszeit wird durch ein GO unterbrochen
                    if (start != null) {                                                                                //sonst hat man ein GO ohne ein COME und das GO wird ignoriert
                        if (fromTime == null || !fromTime.after(entry.getTime())) {
                            end = entry.getTime();
                            if ( fromTime != null && fromTime.after(start)){
                                start = fromTime;
                            }
                            if ( toTime != null && toTime.before(end)){
                                end = toTime;
                            }
                            work.setHours( work.getHours() + ( (end.getTimeInMillis() - start.getTimeInMillis()) / (1000.0 * 3600.0) ) );
                            start = null;
                            end = null;
                        } else {
                            start = null;
                        }
                    }
                } else if (entry.getAction() == TimeTrackerConstants.PROJECT_COME) {				//Projektstart
                    
                    //Project-COME wird nur gezaehlt, wenn fromTime und toTime == null
                    if(fromTime == null && toTime == null) {
                        if (entry.getTitle() != null && entry.getTitle().equals("Pause")) {			//Beginn einer Pause
                            startPause = entry.getTime().getTimeInMillis();
                        } else {												//Moegliches Ende einer Pause
                            if (startPause != 0) {
                                long endPause = entry.getTime().getTimeInMillis();

                                if (toTime != null && toTime.getTimeInMillis() < entry.getTime().getTimeInMillis()) {
                                    endPause = toTime.getTimeInMillis();
                                }

                                double duration = ((endPause - startPause) / (1000.0 * 3600.0));

                                work.setHours(work.getHours() - duration);
                                hasPause = true;
                                startPause = 0;
                            }
                        }
                    } else {
                        //es muss trotzdem erkannt werden, ob eine Pause gemacht wurde
                        if (entry.getTitle() != null && entry.getTitle().equals("Pause")) {			//Beginn einer Pause
                            hasPause = true;
                        }               
                    }
                } else if (entry.getAction() == TimeTrackerConstants.CORRECTION && (withCorrections || entry.getTitle() == null)) {	//Korrektur
                    if (start != null || TimeTrackerFunctions.hasRelevantEntry(set)) {
                    } else {                                                                                                            //nur Korrektur (ohne weitere Arbeitszeit)
                        start = new GregorianCalendar();
                        start.setTimeInMillis(entry.getTime().getTimeInMillis());
                        
                        end = (GregorianCalendar)start.clone();
                        end.add(GregorianCalendar.MINUTE, (int)(entry.getDuration_in_hours() * 60));
                        //wenn nur Korrektur eingetragen wird, dann gibt es keine Auto-Pause
                        hasPause = true;
                    }
                    work.setHours(work.getHours() + entry.getDuration_in_hours());
                }
            }
            
            if((start == null || end == null) && !netHoursOfWork) {
                if (start != null && isToday) {
                    end = new GregorianCalendar();
                    if (toTime != null && toTime.before(end)) {
                        end = toTime;
                    }
                    work.setHours(work.getHours() + ( (end.getTimeInMillis() - start.getTimeInMillis()) / (1000.0 * 3600.0) ) );
                    
                    // letzte Pause ging bis zum Ende
                    if (startPause != 0) {
                        long endPause = end.getTimeInMillis();
                        double duration = ((endPause - startPause) / (1000.0 * 3600.0));
                        
                        work.setHours(work.getHours() - duration);
                        hasPause = true;
                        startPause = 0;
                    }
                } else if(start != null) {
                    throw new QueryException("Gehen-Buchung fehlt");
                } else {
                    work.setHours(0.0);
                }
            } else if (end == null && netHoursOfWork) {
                if (start != null && isToday) {
                    end = new GregorianCalendar();
                    if ( fromTime == null || !fromTime.after(end)) {
                        if ( fromTime != null && fromTime.after(start)){
                            start = fromTime;
                        }
                        if ( toTime != null && toTime.before(end)){
                            end = toTime;
                        }
                        work.setHours( work.getHours() + ( (end.getTimeInMillis() - start.getTimeInMillis()) / (1000.0 * 3600.0) ) );
                        start = null;
                        end = null;
                    }
                }
            }
            
            //Auto-Pause einfuegen
            boolean hasAutoPause = db.hasAutopause(u_id);
            if (!hasPause && TimeTrackerConstants.HOURS_WITHOUT_AUTO_PAUSE <= work.getHours() && hasAutoPause) {
                if (work.getAutoPause()) {
                    work.setHours(work.getHours() - 1.0);
                } else {
                    work.setNeedAutoPause(true);
                }
            } else {
                work.setAutoPause(false);
            }
            
            
            int hourAsInt = (int)(work.getHours() * 100);
            work.setHours(hourAsInt / 100.0);
            
        } else {
            System.out.println(db.getErrorMessage());
        }
    }
    
    
    /**
     * liefert die Wochenarbeitszeit des uebergebenen Benutzers der aktuellen Woche
     *
     * @param u_id Benutzer-ID
     * @return die Wochenarbeitszeit
     * @throws SQLException wird geworfen, wenn ein Datenbankfehler auftritt
     */
    public double getHoursOfWorkInWeek(int u_id) throws SQLException{
        HoursOfWork how = new HoursOfWork();
        GregorianCalendar currentDay = new GregorianCalendar();
        double hoursInWeek = 0.0;
        GregorianCalendar now = new GregorianCalendar();
        now.setFirstDayOfWeek(GregorianCalendar.MONDAY);
        now.setMinimalDaysInFirstWeek(4);
        currentDay.setFirstDayOfWeek(GregorianCalendar.MONDAY);
        currentDay.setMinimalDaysInFirstWeek(4);
        currentDay.set(GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.MONDAY);
        currentDay.set(GregorianCalendar.WEEK_OF_YEAR, now.get(GregorianCalendar.WEEK_OF_YEAR));
        
        while(currentDay.get(GregorianCalendar.DAY_OF_WEEK) != GregorianCalendar.SUNDAY){
            how = new HoursOfWork();
            try{
                how.setAutoPause(true);
                getHoursOfWork(u_id, currentDay, how);
            }catch(QueryException e){
                how.setHours(0.0);
            }
            hoursInWeek += how.getHours();
            currentDay.add(GregorianCalendar.DATE, 1);
        }
        return (double)((int)(hoursInWeek * 100) / 100.0);
    }
    
    
    
    /**
     * liefert die Monatsarbeitszeit des uebergebenen Benutzers der aktuellen Woche
     *
     * @param u_id ID des Benutzers
     * @return die Monatsarbeitszeit in Stunden
     */
    public double getHoursOfWorkInMonth(int u_id) throws SQLException, QueryException{
        GregorianCalendar now = new GregorianCalendar();

        return getHoursOfWorkInMonth(u_id, now.get(GregorianCalendar.MONTH), now.get(GregorianCalendar.YEAR));
    }
    
    
    /**
     * liefert die Monatsarbeitszeit des uebergebenen Benutzers der aktuellen Woche
     *
     * @param u_id ID des Benutzers
     * @return die Monatsarbeitszeit in Stunden
     */
    public double getHoursOfWorkInMonth(int u_id, int month, int year) throws SQLException, QueryException{
        HoursOfWork how = new HoursOfWork();
        double hoursInMonth = 0.0;
        boolean firstDay = true;
        GregorianCalendar currentDay;
        
        currentDay = new GregorianCalendar(year, month, 1);
        
        while(currentDay.get(GregorianCalendar.DATE) != 1 || firstDay){
            how = new HoursOfWork();
            try{
                how.setAutoPause(true);
                getHoursOfWork(u_id, currentDay, how);
            }catch(QueryException e){
                how.setHours(0.0);
            }
            hoursInMonth += how.getHours();
            currentDay.add(GregorianCalendar.DATE, 1);
            firstDay = false;
        }
        return (double)((int)(hoursInMonth * 100) / 100.0);
    }    
    
    /**
     * liefert eine Tabelle mit den Urlaubstagen in HTML
     *
     * @param u_id die ID des Benutzers, dessen Urlaubstabelle generiert werden soll
     * @param year das Jahr, fuer das eine Urlaubstabelle generiert werden soll
     *
     * @return eine HTML-Tabelle mit den Urlaubstagen
     */
    public String getHolidayTable(int u_id, int year) throws SQLException {
        GregorianCalendar toTime = new GregorianCalendar();
        
        if ( toTime.get(GregorianCalendar.YEAR) > year) {
            toTime = new GregorianCalendar(year, GregorianCalendar.DECEMBER, 31);
        }
        
        ResultSet holiday = db.getHolidaysForYear(u_id, toTime);
        String table = getTableOfYear(u_id, holiday, null, toTime);
        holiday.close();
        
        return table;
    }
    
    
    /**
     * liefert eine Tabelle mit dem Monatsuebersicht der Arbeitszeit des uebergebenen Jahres
     *
     * @param u_id die ID des Benutzers, dessen Uebersicht generiert werden soll
     * @param year das Jahr, fuer das die Uebersicht generiert werden soll
     *
     * @return eine HTML-Tabelle
     */
    public String getMonthTable(int u_id, int year) throws SQLException, QueryException {
        GregorianCalendar time = new GregorianCalendar();
        String[] months = {"Januar", "Februar", "M&auml;rz", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember"};
        
        if (time.get(GregorianCalendar.YEAR) > year) {
            time = new GregorianCalendar(year, GregorianCalendar.DECEMBER, 31, 23, 59,59);
        }
        
        StringBuffer table = new StringBuffer("<table border=\"5\" cellspacing=\"5\" width=\"100%\">");
        table.append("<tr><th>Monat</th><th>Stunden</th></tr>");
        
        while (time.get(GregorianCalendar.YEAR) == year) {
            table.append("<tr><td>" + months[time.get(GregorianCalendar.MONTH)] + 
                         "</td><td>" + TimeTrackerFunctions.convert2Time( getHoursOfWorkInMonth(u_id, time.get(GregorianCalendar.MONTH), year), false ) + 
                         "</td></tr>");
            time.add(GregorianCalendar.MONTH, -1 );
        }
        table.append("</table>");
        
        return table.toString();
    }

    
    /**
     * liefert eine Tabelle mit dem Jahresueberblick ueber die Arbeitszeit
     *
     * @param u_id die ID des Benutzers, dessen Jahresueberblick generiert werden soll
     * @param year das Jahr, fuer das ein Jahresueberblick generiert werden soll
     *
     * @return eine HTML-Tabelle mit einem Jahresueberblick
     */
    public String getYearTable(int u_id, int year) throws SQLException{
        GregorianCalendar toTime = new GregorianCalendar();
        
        if (toTime.get(GregorianCalendar.YEAR) > year) {
            toTime = new GregorianCalendar(year, GregorianCalendar.DECEMBER, 31);
        }
        
        ResultSet holiday = db.getHolidaysForYear(u_id, toTime);
        ResultSet illness = db.getIllnessForYear(u_id, toTime);
        
        String table = getTableOfYear(u_id, holiday, illness, toTime);
        holiday.close();
        illness.close();
        
        return table;
    }
    
    
    /**
     * liefert eine Tabelle mit einem Jahresueberblick
     * Wenn illness == null, dann werden nur die Urlaubstage eingetragen
     * Sonst werden Fehltage wegen Krankheit, Urlaub und die Tagesarbeitszeit
     */
    private String getTableOfYear(int u_id, ResultSet holiday, ResultSet illness, GregorianCalendar toTime) throws SQLException {
        StringBuffer table = new StringBuffer();
        StringBuffer row = new StringBuffer();
        StringBuffer headRow = new StringBuffer("<table border=\"5\" cellspacing=\"5\" width=\"100%\">");
        String dayOfWeek[] = {"Mo", "Di", "Mi", "Do", "FR", "Sa", "So"};
        int year = (toTime).get(GregorianCalendar.YEAR);
        String value;
        GregorianCalendar nextHoliday = new GregorianCalendar();
        GregorianCalendar nextIllness = new GregorianCalendar();
        GregorianCalendar gc = new GregorianCalendar();
        gc.setMinimalDaysInFirstWeek(4);
        GregorianCalendar now = toTime;
        double week = 0.0;
        double sum = 0.0;
        Holidays holidays = new Holidays(this.db);
        StringBuffer holidaySymbol = new StringBuffer();
        double holidayTime;
        
        gc.setFirstDayOfWeek(GregorianCalendar.MONDAY);
        gc.set(GregorianCalendar.YEAR, year);
        gc.set(GregorianCalendar.DATE, 1);
        gc.set(GregorianCalendar.MONTH, 0);
        
        //nextHoliday initialisieren
        if(holiday.next()) {
            nextHoliday.setTimeInMillis(holiday.getTimestamp(1).getTime());
        } else {
            nextHoliday = null;
        }
        //nextIllness initialisieren
        if (illness != null && illness.next()) {
            nextIllness.setTimeInMillis(illness.getTimestamp(1).getTime());
        } else {
            nextIllness = null;
        }
        
        
        //Kopfzeile
        headRow.append("<tr><th>KW</th><th>Datum</th>");
        for (int i = 0; i < dayOfWeek.length; ++i) {
            headRow.append("<th>" + dayOfWeek[i] + "</th>");
        }
        if (illness != null) {
            headRow.append("<th>gesamt</th><th>Abweichung</th>");
            week = 0.0;
        }
        
        headRow.append("</tr>");
        
        //Tabellen body
        int DayTillTheFirstOfYear = gc.get(GregorianCalendar.DAY_OF_WEEK);
        if (DayTillTheFirstOfYear == 1) {
            DayTillTheFirstOfYear = 8;
        }
        
        if (gc.get(GregorianCalendar.DAY_OF_WEEK) != GregorianCalendar.MONDAY) {
            row.append("<tr><td>" + gc.get(GregorianCalendar.WEEK_OF_YEAR) + "</td>");
        }
        
        GregorianCalendar date = new GregorianCalendar(gc.get(GregorianCalendar.YEAR), gc.get(GregorianCalendar.MONTH), gc.get(GregorianCalendar.DATE));
        while (date.get(GregorianCalendar.DAY_OF_WEEK) != GregorianCalendar.MONDAY) {
            date.add(GregorianCalendar.DATE, -1);
        }
        
        if(gc.get(GregorianCalendar.DAY_OF_WEEK) != GregorianCalendar.MONDAY) {
            row.append("<td>" + TimeTrackerFunctions.getDateString(date) + "</td>");
        }
        
        for(int i = 1; i < (DayTillTheFirstOfYear - 1); ++i) {
            row.append("<td>&nbsp;</td>");
        }
        
        while((illness != null && (gc.before(now) || TimeTrackerFunctions.isSameDate(gc, now)))  || (illness == null && gc.get(GregorianCalendar.YEAR) == year) ) {
            holidayTime = getHolidayTime(u_id, gc, holidaySymbol, holidays);
            
            if (gc.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.MONDAY) {                  //neue Woche ==> KW eintragen
                if (illness != null && gc.get(GregorianCalendar.DAY_OF_YEAR) != 1) {                  //Ausgabe Wochenarbeitszeit
                    GregorianCalendar lastSunday = (GregorianCalendar)gc.clone();
                    lastSunday.add(GregorianCalendar.DATE, -1);
                    double saldo = week - getTargetHoursPerWeek(u_id, lastSunday, null, true);
                    appendWeekFazit(table, row, week, saldo, gc, u_id);
                    sum += saldo;
                    week = 0.0;
                } else {
                    table.append(row);
                    row.delete(0, row.length()); 
                }
                row.append("<tr><td>").append(gc.get(GregorianCalendar.WEEK_OF_YEAR)).append("</td>");
                row.append("<td>").append(TimeTrackerFunctions.getDateString(gc)).append("</td>");
            }
            if (nextHoliday != null && nextHoliday.get(GregorianCalendar.DAY_OF_YEAR) == gc.get(GregorianCalendar.DAY_OF_YEAR)) {
                //Urlaub
                StringBuffer valueTmp = new StringBuffer();
                week += addAbsentDay(u_id, gc, valueTmp, holiday, nextHoliday, false, holidayTime);
                value = holidaySymbol.toString() + valueTmp + "U";

                if (illness != null && nextIllness != null && nextIllness.get(GregorianCalendar.DAY_OF_YEAR) == gc.get(GregorianCalendar.DAY_OF_YEAR)) {
                    //Krank und Urlaub am selben Tag
                    StringBuffer valueBuffer = new StringBuffer();
                    week += addAbsentDay(u_id, gc, valueBuffer, illness, nextIllness, true, holidayTime);
                    
                    //Arbeitszeit wurde doppelt gezaehlt (bei Urlaub und Krankheit), deshalb:
                    {
                        HoursOfWork work = new HoursOfWork(false);
                        work.setAutoPause(true);
                        try{
                            getHoursOfWork(u_id, gc, work);
                        }catch(Exception e){
                            work.setHours(0.0);
                        }
                        week -= work.getHours();
                    }
                    
                    if(valueBuffer.indexOf("/") != -1){
                        value += valueBuffer.substring(valueBuffer.indexOf("/")) + "K";
                    } else {
                        value += valueBuffer + "/K";
                    }
                }
            } else if(illness != null && nextIllness != null && nextIllness.get(GregorianCalendar.DAY_OF_YEAR) == gc.get(GregorianCalendar.DAY_OF_YEAR)) {
                //Krank
                StringBuffer valueTmp = new StringBuffer();
                week += addAbsentDay(u_id, gc, valueTmp, illness, nextIllness, true, holidayTime);
                value = holidaySymbol.toString() + valueTmp + "K";
            } else if(illness != null) {
                //Tagesarbeitszeit wird angezeigt
                value = holidaySymbol.toString();
                try{
                    HoursOfWork work = new HoursOfWork(true);
                    getHoursOfWork(u_id, gc, work);
                    week += work.getHours();
                    if (!(holidaySymbol.length() != 0 && work.getHours() == 0.0)){
                        value += TimeTrackerFunctions.convert2Time(work.getHours(), false);
                    }
                    if (work.getAutoPause()) {
                        value = value + "*";
                    }
                } catch(QueryException e) {
                    value += "unbek.";
                }
            } else {
                value = holidaySymbol.toString();
                value += "&nbsp;";
            }
            if (gc.get(GregorianCalendar.DAY_OF_WEEK) != GregorianCalendar.SUNDAY && gc.get(GregorianCalendar.DAY_OF_WEEK) != GregorianCalendar.SATURDAY) {
                week += holidayTime;
            }
            row.append("<td>" + value + "</td>\n");
            
            gc.add(GregorianCalendar.DATE, 1);
        }
        
        //letzte Woche wird aufgefuellt
        while (gc.get(GregorianCalendar.DAY_OF_WEEK) != GregorianCalendar.MONDAY) {
            row.append("<td>&nbsp;</td>\n");
            gc.add(GregorianCalendar.DATE, 1);
        }
        if (illness != null) {
            GregorianCalendar lastSunday = (GregorianCalendar)gc.clone();
            lastSunday.add(GregorianCalendar.DATE, -1);
            double saldo = week - getTargetHoursPerWeek(u_id, lastSunday, null, true);
            appendWeekFazit(table, row, week, saldo, gc, u_id);
            sum += saldo;
            table.append("<tr><td /><td /><td /><td /><td /><td /><td /><td /><td colspan='2'>gesamt:</td>");
            table.append("<td>" + TimeTrackerFunctions.convert2Time(sum, true) + "</td></tr>");
        } else {
            table.append(row);
            row.delete(0, row.length());
        }
        table.append("</table>");
        table.insert(0, headRow);
        
        return table.toString();
    }
    
    
    /**
     *	Wenn der uebergebene Tag ein Feiertag ist, dannn werden die Stunden, die dem uebergebenen User
     *	aufgrund des Feiertags zustehen zurueckgeliefert. Sonst Rueckgabe 0.0
     *
     *  @param u_id ID des Benutzers
     *  @param day Tag, der auf Feiertag geprueft wird
     *  @param holidaySymbol darin wird der Grund der zurueckgelieferten Stunden gespeichert.
     *  					 (F fuer Feiertag, H fuer halber Feiertag oder "" fuer Arbeitstag)
     *  					 Diese Variable wird von der Methode veraendert.
     *  					 Wenn holidaySymbol nicht erwuenscht dann null uebergeben.
     *  @param holidays Objekt, mit dem geprueft wird, ob es sich um einen Feiertag handelt
     *  @return die Stunden, die dem User aufgrund eines eventuellen Feiertages zustehen
     *  @throws SQLException falls bei einer Datenbankabfrage ein Fehler auftritt
     */
    private double getHolidayTime(int u_id, GregorianCalendar day, StringBuffer holidaySymbol, Holidays holidays) throws SQLException {
        double holidayTime = 0.0;
        if(holidaySymbol == null){
            holidaySymbol = new StringBuffer();
        }
        holidaySymbol.delete(0, holidaySymbol.length());
        
        if(holidays.isHoliday(day) != Holidays.WORKDAY){
            if(holidays.isHoliday(day) == Holidays.HALF_HOLIDAY){
                if(day.get(GregorianCalendar.DAY_OF_WEEK) != GregorianCalendar.SATURDAY &&
                        day.get(GregorianCalendar.DAY_OF_WEEK) != GregorianCalendar.SUNDAY){
                    //zusaetzliche Zeit wird nur fuer Feiertage angerechnet, die nicht auf einem Wochenende liegen
                    holidayTime = getTargetHoursPerWeek(u_id, (GregorianCalendar)day.clone(), null, false) / 10;
                }
                holidaySymbol.append("H ");
            }else{
                if(day.get(GregorianCalendar.DAY_OF_WEEK) != GregorianCalendar.SATURDAY &&
                        day.get(GregorianCalendar.DAY_OF_WEEK) != GregorianCalendar.SUNDAY){
                    //zusaetzliche Zeit wird nur fuer Feiertage angerechnet, die nicht auf einem Wochenende liegen
                    holidayTime = getTargetHoursPerWeek(u_id, (GregorianCalendar)day.clone(), null, false) / TimeTrackerConstants.WORK_DAYS_PER_WEEK;
                }
                holidaySymbol.append("F ");
            }
        }
        return holidayTime;
    }
    
    
    /**
     *	speichert in table die Wochenzusammenfassung. Wird von getTableOfYear() aufgerufen
     */
    private void appendWeekFazit(StringBuffer table, StringBuffer row, double week, double saldo, GregorianCalendar day, int u_id){
        row.append("<td>" + TimeTrackerFunctions.convert2Time(week, false) + "</td>");
        row.append("<td style=\"color:" + (saldo < 0 ? "red" : "green") + "\">" + TimeTrackerFunctions.convert2Time(saldo, true) + "</td></tr>\n");
        table.insert(0, row);
        row.delete(0, row.length());
    }
    
    
    /**
     * liefert die Beschreibung eines Tages mit abwesenden Stunden
     * eingesetzt bei der Jahresstatistik gesamt
     */
    private double addAbsentDay(int u_id, GregorianCalendar currentDate, StringBuffer value, ResultSet absentDays, GregorianCalendar nextAbsentDay, boolean isIllness, double holidayTime) throws SQLException{
        double time = 0.0;
        double targetHoursPerDay = getTargetHoursPerWeek(u_id, currentDate, null, false) / TimeTrackerConstants.WORK_DAYS_PER_WEEK;

        if (absentDays.getMetaData().getColumnCount() == 2) {	//trifft wahrscheinlich immer zu
            double hoursFromAbsentDay = absentDays.getDouble(2);
            if(hoursFromAbsentDay == 0.0){
                hoursFromAbsentDay = targetHoursPerDay;
            }
            
            time = hoursFromAbsentDay;
            
            try {
                HoursOfWork work = new HoursOfWork(true);
                getHoursOfWork(u_id, currentDate, work);
                if (work.getHours() != 0.0) {
                    if (isIllness && (time + work.getHours()) > targetHoursPerDay) { //Abwesenheit durch Krankheit wird gekuerzt, so dass keine Ueberstunden durch Krankheit entstehen
                        time = targetHoursPerDay - work.getHours();
                        if (time < 0) {
                            time = 0;
                        }
                        value.append(TimeTrackerFunctions.convert2Time(work.getHours(), false) + "/" + TimeTrackerFunctions.convert2Time(time, false));
                    }else {
                        value.append(TimeTrackerFunctions.convert2Time(work.getHours(), false) + "/" + TimeTrackerFunctions.convert2Time(time, false));
                    }
                    time += work.getHours();
                } else {
                    if (time != targetHoursPerDay) {
                        value.append(TimeTrackerFunctions.convert2Time(time, false));
                    }
                }
            } catch(QueryException e) {
                value.append("unbek." + "/" + TimeTrackerFunctions.convert2Time(time, false));
            }
        }
        
        if (absentDays.next()) {
            nextAbsentDay.setTimeInMillis(absentDays.getTimestamp(1).getTime());
        } else {
            nextAbsentDay = null;
        }
        return time;
    }
    
    
    /**
     * liefert die Sollstunden der angegebenen Woche
     *
     * @param u_id ID des Benutzers
     * @param day Tag in der Woche, von der die Stundenzahl berechnet werden soll
     * @param validTill darin wird der Zeitpunkt gespeichert,
     * 			zu dem der zur Zeit day laufende Vertrag auslaeuft
     * 			Wenn dieser Zeitpunkt nicht benoetigt wird,
     * 			dann kann null uebergeben werden.
     * 			Wenn Auslauf-Datum offen, dann
     * 			validTill.get(GregorianCalendar.YEAR) == getMaximum(GregorianCalendar.YEAR).
     * @param averageOverWeek Wenn averageOverWeek == true, dann wird der Durchschnittswert der
     * 				angegebenen Woche ermittlet. Tage aus dem alten Jahr zaehlen dabei nicht.
     * @return die Sollstunden der angegebenen Woche
     */
    public double getTargetHoursPerWeek(int u_id, GregorianCalendar day, GregorianCalendar validTill, boolean averageOverWeek) {
        GregorianCalendar tmpDay = (GregorianCalendar)day.clone();
        if (tmpDay == null){
            tmpDay  = new GregorianCalendar();
        }
        
        if (averageOverWeek){
            return getTargetHoursAverage(u_id, day, validTill);
        }
        
        try{
            ContractInfos contract = db.getHoursOfWork(u_id, tmpDay);
            double whow;
            GregorianCalendar toDate = (GregorianCalendar)tmpDay.clone();
            
            if (contract != null) {
                if (contract.getToDate() != null) {
                    toDate = contract.getToDate();
                } else {
                    int currentYear = (new GregorianCalendar()).get(GregorianCalendar.YEAR);
                    toDate.set(GregorianCalendar.YEAR, currentYear + 1);
                }
                
                if (validTill != null){
                    validTill.setTimeInMillis(toDate.getTimeInMillis());
                }
                
                whow = contract.getWhow();
            } else {
                //kein Vertrag gefunden
                whow = 0.0;
            }
            
            return whow;
        }catch (SQLException se) {
            se.printStackTrace();
        }
        return 0.0;
    }
    
    
    /**
     *	liefert die durchschnittliche Sollstunden der angegebenen Woche. D.h. Vertragsaenderungen waehrend
     *  der Woche werden beruecksichtigt. Die Woche beginnt immer mit dem letzten Montag vor dem
     *	uebergebenen Datum. Sonst wie getTargetHoursPerWeek
     */
    private double getTargetHoursAverage(int u_id, GregorianCalendar day, GregorianCalendar validTill){
        GregorianCalendar tmpDay = (GregorianCalendar)day.clone();
        boolean validTillInvalid = false;
        double hoursPerWeek = 0.0;
        double hoursLastDay = 0.0;
        double tmp = 0.0;
        
        if (tmpDay == null){
            tmpDay  = new GregorianCalendar();
        }
        
        //tmpDay auf den Montag der uebergebenen Woche setzen
        int year = tmpDay.get(GregorianCalendar.YEAR);
        
        while(tmpDay.get(GregorianCalendar.DAY_OF_WEEK) != GregorianCalendar.MONDAY && tmpDay.get(GregorianCalendar.YEAR) == year){
            tmpDay.add(GregorianCalendar.DATE, -1);
        }
        if(tmpDay.get(GregorianCalendar.YEAR) != year){
            tmpDay.add(GregorianCalendar.DATE, 1);
        }
        
        do{
            tmp = (getTargetHoursPerWeek(u_id, tmpDay, validTill, false) / TimeTrackerConstants.WORK_DAYS_PER_WEEK);
            hoursPerWeek += tmp;
            if(hoursLastDay != tmp && tmpDay.get(GregorianCalendar.DAY_OF_WEEK) != GregorianCalendar.MONDAY){
                validTillInvalid = true;
            }
            hoursLastDay = tmp;
            tmpDay.add(GregorianCalendar.DATE, 1);
        }while(tmpDay.get(GregorianCalendar.DAY_OF_WEEK) != GregorianCalendar.SATURDAY);
        
        if(validTillInvalid && validTill != null){
            validTill.setTimeInMillis(1);
        }
        
        return hoursPerWeek;
    }
    
    
    /**
     *	liefert die Anzahl der Fehltage wegen Krankheit des aktuellen Jahres
     */
    public double getIllnessDays(int u_id) throws SQLException{
        double timeOfWork = getTargetHoursPerWeek(u_id, new GregorianCalendar(), null, false) / TimeTrackerConstants.WORK_DAYS_PER_WEEK;
        
        return db.getIllnessDays(u_id, timeOfWork);
    }
    
    
    /**
     * fuellt das uebergebene Holiday-Objekt.
     * Wenn year == null, dann gilt das aktuelle Jahr.
     *
     * @param u_id Benutzer, dessen Urlaub bestimmnt werden soll
     * @param year Jahr, auf das sich der Urlaub bezieht
     * @param holidays Object vom Typ HolidayStruct, in dem der ermittelte Urlaub gespeichert wird
     * @throws SQLException
     */
    public void getHoliday(int u_id, GregorianCalendar year, HolidayStruct holidays) throws SQLException{
        if (year == null){
            year = new GregorianCalendar();
        }
        
        GregorianCalendar yearCopy = (GregorianCalendar)year.clone();
        yearCopy.set(GregorianCalendar.DATE, 1);
        
        //verbrauchter Urlaub im aktuellen Jahr
        Vector<TimeDurationPair> usedHolidays = db.getUsedHolidaysForYear(u_id, year);
        
        if (usedHolidays != null) {
            for (TimeDurationPair timeDuration : usedHolidays) {
                double holidaysPerDay = 1.0;
                if (timeDuration.getDuration() != 0 ) {
                    double daysOfWorkPerWeek = (getTargetHoursPerWeek(u_id, timeDuration.getTime(), null, false) / TimeTrackerConstants.WORK_DAYS_PER_WEEK);
                    holidaysPerDay = timeDuration.getDuration() / daysOfWorkPerWeek;
                }
                holidays.setHolidaysThisYear(holidays.getHolidaysThisYear() + holidaysPerDay);
            }
        }
        
        //Resturlaub des letzten Jahres bestimmen
        if (yearCopy.get(GregorianCalendar.YEAR) > getYearOfFirstContract(u_id)){
            HolidayStruct holidaysLastYear = new HolidayStruct();
            GregorianCalendar lastYear = (GregorianCalendar)yearCopy.clone();
            lastYear.add(GregorianCalendar.YEAR, -1);
            
            getHoliday(u_id, lastYear, holidaysLastYear);
            holidays.setResidualLeaveFromLastYear(holidaysLastYear.getResidualLeaveFromThisYear());
        }else{
            holidays.setResidualLeaveFromLastYear(0);
        }
        
        //Gesamter Resturlaub dieses Jahr
        holidays.setHolidayPerYear(db.getHolidayForYear(u_id, yearCopy));
        
        Vector<TimeDurationPair> holidayCorrections = db.getHolidayCorrectionsForYear(u_id, year);
        
        double manualAmount = 0.0;
        if (holidayCorrections != null) {
            for (TimeDurationPair timeDuration : holidayCorrections) {
                double manualAmountTmp = 0.0;
                if (timeDuration.getDuration() != 0 ) {
                    double daysOfWorkPerWeek = (getTargetHoursPerWeek(u_id, timeDuration.getTime(), null, false) / TimeTrackerConstants.WORK_DAYS_PER_WEEK);
                    manualAmountTmp = timeDuration.getDuration() / daysOfWorkPerWeek;
                }
                manualAmount += manualAmountTmp;
            }
        }
        
        double tmp = holidays.getHolidayPerYear() + manualAmount - holidays.getHolidaysThisYear();
        tmp += holidays.getResidualLeaveFromLastYear();
        
        holidays.setResidualLeaveFromThisYear(tmp);
    }
    
    
    
    /**
     *	liefert das Jahr des ersten Vertrags des uebergebenen Benutzers
     */
    private int getYearOfFirstContract(int u_id) throws SQLException{
        GregorianCalendar tmp;
        tmp = db.getDateOfFirstContract(u_id);
        if(tmp != null){
            return tmp.get(GregorianCalendar.YEAR);
        }else{
            return 0;
        }
    }
    
    
    /**
     *	liefert den aktuellen Kontostand des uebergebenen Benutzers
     */
    public String getCurrentAccountBalance(int u_id) throws SQLException{
        long start = (new java.util.Date()).getTime();
        double accountBalance = 0.0;
        GregorianCalendar lastReset = db.getLastReset(u_id);
        Holidays holidays = new Holidays(this.db);
        double holidayTime;
        
        if(lastReset == null){
            lastReset = db.getDateOfFirstContract(u_id);
        }
        
        //Urlaubs- und Krankheitstage ermitteln
        String holidayQuery = db.getHolidayQueryString(u_id, lastReset);
        ResultSet holiday = db.execute(holidayQuery);
        
        String illnessQuery = db.getIllnessQueryString(u_id, lastReset);
        ResultSet illness = db.execute(illnessQuery);
        
        GregorianCalendar nextHoliday = new GregorianCalendar();
        GregorianCalendar nextIllness = new GregorianCalendar();
        
        //nextHoliday initialisieren
        if(holiday.next()){
            nextHoliday.setTimeInMillis(holiday.getTimestamp(1).getTime());
        }else{
            nextHoliday = null;
        }
        //nextIllness initialisieren
        if(illness.next()){
            nextIllness.setTimeInMillis(illness.getTimestamp(1).getTime());
        }else{
            nextIllness = null;
        }
        
        GregorianCalendar now = new GregorianCalendar();
        GregorianCalendar tmpTime = (GregorianCalendar)lastReset.clone();
        GregorianCalendar contractValidTill = null;
        
        HoursOfWork time = new HoursOfWork();
        double dailyTimeOfWork = 0.0;
        
        while(!TimeTrackerFunctions.getDateString(tmpTime).equals(TimeTrackerFunctions.getDateString(now)) && tmpTime.before(now) ){
            time.setAutoPause(false);
            time.setHours(0.0);
            
            if((contractValidTill != null && contractValidTill.before(tmpTime)) || contractValidTill == null){		//neue Wochenarbeitszeit anfordern
                contractValidTill = new GregorianCalendar();
                dailyTimeOfWork = (getTargetHoursPerWeek(u_id, tmpTime, contractValidTill, false) / TimeTrackerConstants.WORK_DAYS_PER_WEEK);
            }
            
            
            try{
                getHoursOfWork(u_id, tmpTime, time);
            }catch(SQLException e){
                e.printStackTrace();
                time.setHours(0.0);
            }catch(QueryException e){
                time.setHours(0.0);
            }
            
            holidayTime = getHolidayTime(u_id, tmpTime, null, holidays);
            
            if(	tmpTime.get(GregorianCalendar.DAY_OF_WEEK) != GregorianCalendar.SATURDAY &&
                    tmpTime.get(GregorianCalendar.DAY_OF_WEEK) != GregorianCalendar.SUNDAY ){
                
                if(nextIllness != null && TimeTrackerFunctions.getDateString(nextIllness).equals(TimeTrackerFunctions.getDateString(tmpTime))){	//wenn ganzer oder halber Fehltag wegen Krankheit
                    double holidayDuration = 0.0;
                    
                    if(nextHoliday != null && TimeTrackerFunctions.getDateString(nextHoliday).equals(TimeTrackerFunctions.getDateString(tmpTime))){
                        holidayDuration = holiday.getDouble(2) == 0.0 ? dailyTimeOfWork : holiday.getDouble(2);
                        if(holiday.next()){
                            nextHoliday.setTimeInMillis(holiday.getTimestamp(1).getTime());
                        }else{
                            nextHoliday = null;
                        }
                    }
                    
                    double illnessDuration = illness.getDouble(2) == 0.0 ? dailyTimeOfWork : illness.getDouble(2);
                    if((illnessDuration + time.getHours() + holidayTime + holidayDuration) < dailyTimeOfWork){
                        accountBalance += (time.getHours() + illnessDuration + holidayDuration + holidayTime) - dailyTimeOfWork ;
                    }
                    if(illness.next()){
                        nextIllness.setTimeInMillis(illness.getTimestamp(1).getTime());
                    }else{
                        nextIllness = null;
                    }
                }else if(nextHoliday != null && TimeTrackerFunctions.getDateString(nextHoliday).equals(TimeTrackerFunctions.getDateString(tmpTime))){//wenn ganzer oder halber Urlaubstag
                    double holidayDuration = holiday.getDouble(2) == 0.0 ? dailyTimeOfWork : holiday.getDouble(2);
                    accountBalance += (time.getHours() + holidayDuration + holidayTime) - dailyTimeOfWork ;
                    if(holiday.next()){
                        nextHoliday.setTimeInMillis(holiday.getTimestamp(1).getTime());
                    }else{
                        nextHoliday = null;
                    }
                }else{
                    if (time.isNeedAutoPause()){
                        time.setHours(time.getHours() - 1.0);
                    }else{
                        time.setHours(time.getHours());
                    }
                    
                    accountBalance += (time.getHours() + holidayTime) - dailyTimeOfWork ;
                }
                
            }else{
                if (time.isNeedAutoPause()){
                    time.setHours(time.getHours() - 1.0);
                }else{
                    time.setHours(time.getHours());
                }
                
                accountBalance += time.getHours();
            }
            tmpTime.add(GregorianCalendar.DATE, 1);
        }
        
        System.out.println("Zeit: " + ( (new java.util.Date()).getTime() - start ) + " ms" );
        return TimeTrackerFunctions.convert2Time(accountBalance, false);
    }
    
    
    /**
     *	liefert die Projektzeiten des uebergebenen Jahres oder Monats
     */
    public Hashtable getProjectTimes(int u_id, GregorianCalendar date, boolean onlyMonth) throws SQLException {
        Hashtable projectTimes = new Hashtable();
        GregorianCalendar projectStart = new GregorianCalendar(date.get(GregorianCalendar.YEAR), date.get(GregorianCalendar.MONTH), 1, 0, 0, 0);
        boolean projectToLate = false;
        GregorianCalendar firstProjectDay = new GregorianCalendar();
        String timeInterval;
        GregorianCalendar endOfTimeInterval = new GregorianCalendar();
        TitleTimePair projectStartStruct = null;
        
        //endOfTimeInterval auf das Ende des gegebenen Zeitintervalls setzen
        if (onlyMonth) {
            endOfTimeInterval.set(date.get(GregorianCalendar.YEAR), date.get(GregorianCalendar.MONTH), date.getActualMaximum(GregorianCalendar.DAY_OF_MONTH), 23, 59, 59);
            timeInterval = "month";
       } else {
            endOfTimeInterval.set(date.get(GregorianCalendar.YEAR), GregorianCalendar.DECEMBER, 31, 23, 59, 59);
            timeInterval = "year";
            projectStart = (GregorianCalendar)date.clone();
            projectStart.set(GregorianCalendar.MONTH, GregorianCalendar.JANUARY);
        }
        
        Vector<TitleTimePair> projectStartVector = db.getProjectComes(u_id, date, timeInterval);
        Iterator<TitleTimePair> projectStartIt = projectStartVector.iterator();
        
        Vector<ProjectInfos> projectSubsequents = db.getProjectSubsequents(u_id, date, timeInterval);
        Iterator<ProjectInfos> projectSubIt = projectSubsequents.iterator();
        ProjectInfos nextSubsequentProject = null;
        
        if (projectSubIt.hasNext()) {
            nextSubsequentProject = projectSubIt.next();
        }      
        
        //projectStart initialisieren
        if (!projectStartIt.hasNext()) {
            //es wurde noch nie ein Projekt gestartet ==> Verlassen der Funktion
            //zwischengezogene Projektzeiten abziehen
            while (nextSubsequentProject != null) {
                if (nextSubsequentProject.getAction() != null && nextSubsequentProject.getTitle() != null) {
                    insertIntoHashtable(projectTimes, nextSubsequentProject.getTitle(), nextSubsequentProject.getDuration());
                }
                //Projekt im benoetigten Zeitraum?
                if ( projectSubIt.hasNext() ) {
                    nextSubsequentProject = projectSubIt.next();
                } else {
                    nextSubsequentProject = null;
                }
            }
            return projectTimes;
        } else {
            projectStartStruct = projectStartIt.next();
        }

        if ( projectStart.getTimeInMillis() < projectStartStruct.getTime().getTimeInMillis() ) {
            projectStart = (GregorianCalendar)projectStartStruct.getTime().clone();
        }
        
        
        while (nextSubsequentProject != null && nextSubsequentProject.getTime().getTimeInMillis() < projectStart.getTimeInMillis()) {
            if (nextSubsequentProject.getAction() != null && nextSubsequentProject.getTitle() != null) {
                insertIntoHashtable(projectTimes, nextSubsequentProject.getTitle(), nextSubsequentProject.getDuration());
            }
            //Projekt im benoetigten Zeitraum?
            if ( projectSubIt.hasNext() ) {
                nextSubsequentProject = projectSubIt.next();
            } else {
                nextSubsequentProject = null;
            }
        }

        //	durchlaeuft die Projekt comes
        //	solange bis kein Projekt-Start mehr vorhanden ist, der heutige Tag erreicht ist oder
        //	das vorgegebene Zeitintervall abgelaufen ist
        while (	projectStart != null &&
                !TimeTrackerFunctions.getDateString(projectStart).equals(TimeTrackerFunctions.getDateString(new GregorianCalendar())) &&
                !(projectStart.getTimeInMillis() > endOfTimeInterval.getTimeInMillis()) ) {
            if (projectStartStruct != null) {
                double projectTime = 0.0;
                GregorianCalendar tmpTime = (GregorianCalendar)projectStart.clone();
                HoursOfWork time = new HoursOfWork(true);
                String lastProjectTitle = projectStartStruct.getTitle();
                GregorianCalendar projectEnd;
                
                //projectStart auf das Datum des naechsten Projekt Starts setzen
                if (projectStartIt.hasNext()) {
                    projectStartStruct = projectStartIt.next();
                    projectEnd = (GregorianCalendar)projectStartStruct.getTime().clone();
                } else {
                    projectEnd = new GregorianCalendar();
                }
                
                //Arbeitszeiten vom einen Project Come bis zum naechsten Project Come addieren
                //erster Tag des Projekts:
                try {
                    time.setAutoPause(true);
                    getHoursOfWork(u_id, tmpTime, time, tmpTime, null, false);
                    projectTime += time.getHours();
                } catch(Exception e) {/*ignorieren*/}
                tmpTime.add(GregorianCalendar.DATE, 1);
                
                //volle Projekt-Tage
                while(	TimeTrackerFunctions.isDateLess(tmpTime, projectEnd) && 
                        TimeTrackerFunctions.isDateLess(tmpTime, endOfTimeInterval)) {
                    try{
                        if (!TimeTrackerFunctions.getDateString(tmpTime).equals(TimeTrackerFunctions.getDateString(firstProjectDay))) {
                            GregorianCalendar tmpStartTime = new GregorianCalendar(tmpTime.get(GregorianCalendar.YEAR), tmpTime.get(GregorianCalendar.MONTH), tmpTime.get(GregorianCalendar.DAY_OF_MONTH), 0, 0, 0);
                            //nicht erster Projekttag
                            time.setAutoPause(true);
                            //Startzeit wird angegeben, damit die Pausen, ausser die AutoPause, nicht beruecksichtigt werden
                            getHoursOfWork(u_id, tmpTime, time, tmpStartTime, null, false);
                            projectTime += time.getHours();
                        }
                    } catch(Exception e) {/*ignorieren*/}
                    tmpTime.add(GregorianCalendar.DATE, 1);
                }
                
                //letzter Tag des Projekts
                try {
                    if (TimeTrackerFunctions.getDateString(projectStart).equals(TimeTrackerFunctions.getDateString(projectEnd))) {
                        //Start und Ende des Projekts liegen am selben Tag
                        time.setAutoPause(true);
                        getHoursOfWork(u_id, projectStart, time, projectStart, projectEnd, false);
                        projectTime = time.getHours();
                    } else {
                        time.setAutoPause(true);
                        getHoursOfWork(u_id, tmpTime, time, null, projectEnd, false);
                        projectTime += time.getHours();
                    }
                } catch (Exception e) {/*ignorieren*/}
                
                projectStart = projectEnd;
                
                //zwischengezogene Projektzeiten abziehen
                if (nextSubsequentProject != null) {
                    GregorianCalendar tmp = new GregorianCalendar();
                    tmp = (GregorianCalendar)nextSubsequentProject.getTime().clone();
                    
                    if ( tmp.getTimeInMillis() > projectEnd.getTimeInMillis() ) {
                        projectToLate = true;
                    } else {
                        projectToLate = false;
                    }
                    
                    while (nextSubsequentProject != null && !projectToLate) {
                        if (nextSubsequentProject.getAction() != null && nextSubsequentProject.getAction().equals("CORRECTION")) {
                            if (nextSubsequentProject.getTitle() != null) {
                                insertIntoHashtable(projectTimes, nextSubsequentProject.getTitle(), nextSubsequentProject.getDuration());
                            }
                        } else if (!nextSubsequentProject.getTitle().equals( lastProjectTitle )) {
                            projectTime -= nextSubsequentProject.getDuration();
                            insertIntoHashtable(projectTimes, nextSubsequentProject.getTitle(), nextSubsequentProject.getDuration());
                        }
                        
                        //Projekt im benoetigten Zeitraum?
                        if ( projectSubIt.hasNext() ) {
                            nextSubsequentProject = projectSubIt.next();
                            tmp = (GregorianCalendar)nextSubsequentProject.getTime().clone();
                            
                            if (tmp.getTimeInMillis() > projectEnd.getTimeInMillis() || tmp.getTimeInMillis() > endOfTimeInterval.getTimeInMillis()) {
                                projectToLate = true;
                            }
                        } else {
                            projectToLate = true;
                            nextSubsequentProject = null;
                        }
                    }
                }
                insertIntoHashtable(projectTimes, lastProjectTitle, projectTime);
            }
        }
        
        //zwischengezogene Projektzeiten einfuegen, die noch nicht bearbeitet wurden
        while (nextSubsequentProject != null) {
            if (nextSubsequentProject.getAction() != null && nextSubsequentProject.getTitle() != null) {
                insertIntoHashtable(projectTimes, nextSubsequentProject.getTitle(), nextSubsequentProject.getDuration());
            }
            //Projekt im benoetigten Zeitraum?
            if ( projectSubIt.hasNext() ) {
                nextSubsequentProject = projectSubIt.next();
            } else {
                nextSubsequentProject = null;
            }
        }        
        return projectTimes;
    }
    
    
    
    /**
     * fuegt der uebergebenen Hashtable den Wert add zum Key name hinzu
     */
    private void insertIntoHashtable(Hashtable ht, String name, double add) {
        Double tmp;
        
        if (!name.equals("Pause")) {			//Pause-Projekte werden nicht beruecksichtigt
            tmp = (Double)ht.get(name);
            
            if (tmp != null ) {
                ht.put( name, new Double(tmp.doubleValue() + add) );
            } else {
                ht.put( name, new Double(add) );
            }
        }
    }
    
    
    //Zum Testen
    public static void main(String args[]){
        try{
            Database db = new Database("org.postgresql.Driver",
                    "jdbc:postgresql://flexo.cismet.de/timetracker",
                    "postgres", "");
            System.out.println("isConnectionOk: " + db.isConnectionOk());
            if(!db.isConnectionOk()){
                System.out.println(db.getErrorMessage());
            }
            Query q = new Query(db);
            System.out.println("acc: " + q.getCurrentAccountBalance(14));
            
//            System.out.println(q.getTargetHoursPerWeek(9, new GregorianCalendar(2006, 9, 1), null, false));
//            System.out.println(q.getTargetHoursPerWeek(9, new GregorianCalendar(2006, 9, 1), null, true));
//			Tagesarbeitszeittest
/*    		HoursOfWork work = new HoursOfWork();
                work.setAutoPause(true);
                        q.getHoursOfWork(3, new GregorianCalendar(2006,8,06), work);
                        System.out.println("\n\nZeit: " + work.getHours() + "\n\n");
 */
//    		q.getYearTable(3);
//    		long start = (new GregorianCalendar()).getTimeInMillis();
//    		String bal = q.getCurrentAccountBalance(3);
//    		q.getProjectTimes(1,new GregorianCalendar() ,false);
//    		System.out.println(bal);
            //  		long ende = (new GregorianCalendar()).getTimeInMillis();
            //		System.out.println("Dauer: " + ((ende - start) / 1000.0));
            //Projekt-Test
/*			Hashtable ht = q.getProjectTimes(3, new GregorianCalendar(2006,4,12), true);
                        Enumeration enumYear = ht.keys();
                        String project;
                        while(enumYear.hasMoreElements()){
                                project = (String)enumYear.nextElement();
                                Double d = (Double)ht.get(project);
                                System.out.println(project + ": " + d.doubleValue());
                        }
 */
//            //Urlaubstest
//            HolidayStruct hs = new HolidayStruct();
//            
//            q.getHoliday(9, new GregorianCalendar(), hs);
//            System.out.println("Urlaub/Jahr: " + hs.getHolidayPerYear());
//            System.out.println("Resturlaub dieses Jahr: " + hs.getResidualLeaveFromThisYear());
//            System.out.println("Resturlaub vom letzten Jahr: " + hs.getResidualLeaveFromLastYear());
//            System.out.println("Urlaub dieses Jahr: " + hs.getHolidaysThisYear());
//            
//            long start = (new java.util.Date()).getTime();
//            System.out.println(q.getCurrentAccountBalance(4));
//            System.out.println("Zeit: " + ((new java.util.Date()).getTime() - start) + " ms");
        } catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
