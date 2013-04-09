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
public class ProjectInfos {

    //~ Instance fields --------------------------------------------------------

    private String title;
    private String action;
    private GregorianCalendar time;
    private double duration;

    //~ Methods ----------------------------------------------------------------

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
    public String getAction() {
        return action;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  action  DOCUMENT ME!
     */
    public void setAction(final String action) {
        this.action = action;
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

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getDuration() {
        return duration;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  duration  DOCUMENT ME!
     */
    public void setDuration(final double duration) {
        this.duration = duration;
    }
}
