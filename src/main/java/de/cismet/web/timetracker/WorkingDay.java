/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cismet.web.timetracker;

import de.cismet.web.timetracker.types.HoursOfWork;
import de.cismet.web.timetracker.types.TimesheetEntry;
import de.cismet.web.timetracker.types.TimesheetSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

/**
 *
 * @author therter
 */
public class WorkingDay {
    private GregorianCalendar start;
    private GregorianCalendar end;
    private HoursOfWork work = new HoursOfWork();
    private boolean firstCome = true;
    private GregorianCalendar fromTime;
    private GregorianCalendar toTime;
    private boolean netHoursOfWork;
    private boolean isToday;
    SimpleDateFormat formaterDB = new SimpleDateFormat("yyyy-MM-d");
    //speichert den Start einer Pause in Millis. Wenn 0, dann wurde noch keine Pause begonnen
    long startPause = 0;
    //speichert, ob es eine Pause gab, oder ob eine Auto-Pause hinzugefuegt werden soll
    boolean hasPause = false;
    boolean withCorrections;


    public WorkingDay(boolean autoPause, boolean netHoursOfWork, boolean isToday, boolean withCorrections) {
        this(autoPause, netHoursOfWork, isToday, withCorrections, null, null);
    }


    public WorkingDay(boolean autoPause, boolean netHoursOfWork, boolean isToday, boolean withCorrections, GregorianCalendar fromTime, GregorianCalendar toTime) {
        work.setAutoPause(autoPause);
        this.withCorrections = withCorrections;
        this.netHoursOfWork = netHoursOfWork;
        this.isToday = isToday;
        this.fromTime = fromTime;
        this.toTime = toTime;
    }


    public void addDataSet(TimesheetSet set) throws SQLException, QueryException {
        TimesheetEntry entry;

        while ( ( entry = set.next() ) != null) {
            if (entry.getAction() == TimeTrackerConstants.COME && (firstCome && !netHoursOfWork)  ) {                               //Arbeitsstart ermitteln
                handleFirstCOME(entry);
            } else if(entry.getAction() == TimeTrackerConstants.GO && (TimeTrackerFunctions.isLastGo(set) && !netHoursOfWork) ) {	//Arbeitsende ermitteln
                handleLastGO(entry, set);
            } else if (entry.getAction() == TimeTrackerConstants.PROJECT_SUBSEQUENT) {		//Projekt nachtraeglich
                handleProjectSubsequent(entry);
            } else if(entry.getAction() == TimeTrackerConstants.COME && netHoursOfWork) {	//neue netto Arbeitszeit beginnt
                handleComeInNetMode(entry);
            } else if(entry.getAction() == TimeTrackerConstants.GO && netHoursOfWork) {		//netto Arbeitszeit wird durch ein GO unterbrochen
                handleGOInNetMode(entry);
            } else if (entry.getAction() == TimeTrackerConstants.PROJECT_COME) {		//Projektstart
                handleProjectCome(entry);
            } else if (entry.getAction() == TimeTrackerConstants.CORRECTION && (withCorrections || entry.getTitle() == null)) {	//Korrektur
                handleCorrection(entry, set);
            }
        }
    }


    public HoursOfWork getTime(boolean hasAutoPause) throws QueryException {
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

        return work;
    }


    private void handleFirstCOME(TimesheetEntry entry) {
        firstCome = false;
        start = entry.getTime();
        if ( fromTime != null && fromTime.after(start)){
            start = fromTime;
        }
    }


    private void handleLastGO(TimesheetEntry entry, TimesheetSet set) throws SQLException, QueryException {
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
    }


    private void handleProjectSubsequent(TimesheetEntry entry) {
        //Project-Subsequent wird nur gezaehlt, wenn fromTime und toTime == null
        if(fromTime == null && toTime == null) {
            if (entry.getTitle() != null && entry.getTitle().equals("Pause")) {
                hasPause = true;
                work.setHours(work.getHours() - entry.getDuration_in_hours());
            }
        } else {
            //es muss trotzdem erkannt werden, ob eine Pause gemacht wurde
            if (entry.getTitle() != null && entry.getTitle().equals("Pause")) {
                hasPause = true;
            }
        }
    }


    private void handleGOInNetMode(TimesheetEntry entry) {
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
    }


    private void handleProjectCome(TimesheetEntry entry) {
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
    }


    private void handleComeInNetMode(TimesheetEntry entry) {
        if (start == null) {                                                              //sonst waren 2 COMEs in Folge
            start = entry.getTime();
        }
    }

    private void handleCorrection(TimesheetEntry entry, TimesheetSet set) throws SQLException {
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
