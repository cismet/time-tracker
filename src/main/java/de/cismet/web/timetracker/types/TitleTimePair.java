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
public class TitleTimePair {
    private String title;
    private GregorianCalendar time;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public GregorianCalendar getTime() {
        return time;
    }

    public void setTime(GregorianCalendar time) {
        this.time = time;
    }
}
