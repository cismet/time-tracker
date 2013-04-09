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
public class TimeDurationPair {

    //~ Instance fields --------------------------------------------------------

    private GregorianCalendar time;
    private double duration;

    //~ Methods ----------------------------------------------------------------

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
