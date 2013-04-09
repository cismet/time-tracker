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
public class ProjectInfos {
    private String title;
    private String action;
    private GregorianCalendar time;
    private double duration;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public GregorianCalendar getTime() {
        return time;
    }

    public void setTime(GregorianCalendar time) {
        this.time = time;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }
}
