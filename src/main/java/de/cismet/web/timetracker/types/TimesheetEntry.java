/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cismet.web.timetracker.types;

import java.util.GregorianCalendar;

/**
 *
 * @author therter
 */
public class TimesheetEntry {
    private int action;
    private int projectId;
    private String title;
    private double duration_in_hours;
    private GregorianCalendar time;

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getDuration_in_hours() {
        return duration_in_hours;
    }

    public void setDuration_in_hours(double duration_in_hours) {
        this.duration_in_hours = duration_in_hours;
    }

    public GregorianCalendar getTime() {
        return time;
    }

    public void setTime(GregorianCalendar time) {
        this.time = time;
    }
    
    
    
}
