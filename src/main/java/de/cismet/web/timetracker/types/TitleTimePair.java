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
public class TitleTimePair {

    //~ Instance fields --------------------------------------------------------

    private String title;
    private GregorianCalendar time;

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
