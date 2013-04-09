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
package de.cismet.web.timetracker.types;

import java.util.GregorianCalendar;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class TimesheetEntry {

    //~ Instance fields --------------------------------------------------------

    private int action;
    private int projectId;
    private String title;
    private double duration_in_hours;
    private GregorianCalendar time;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getAction() {
        return action;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  action  DOCUMENT ME!
     */
    public void setAction(final int action) {
        this.action = action;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getProjectId() {
        return projectId;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  projectId  DOCUMENT ME!
     */
    public void setProjectId(final int projectId) {
        this.projectId = projectId;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getTitle() {
        return title;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  title  DOCUMENT ME!
     */
    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getDuration_in_hours() {
        return duration_in_hours;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  duration_in_hours  DOCUMENT ME!
     */
    public void setDuration_in_hours(final double duration_in_hours) {
        this.duration_in_hours = duration_in_hours;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public GregorianCalendar getTime() {
        return time;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  time  DOCUMENT ME!
     */
    public void setTime(final GregorianCalendar time) {
        this.time = time;
    }
}
