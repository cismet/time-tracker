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

import java.util.Iterator;
import java.util.Vector;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class TimesheetSet {

    //~ Instance fields --------------------------------------------------------

    private Vector<TimesheetEntry> entries = new Vector<TimesheetEntry>();
    private int index = 0;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  entry  DOCUMENT ME!
     */
    public void add(final TimesheetEntry entry) {
        entries.add(entry);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public TimesheetEntry next() {
        if (index < entries.size()) {
            return entries.get(index++);
        } else {
            ++index;
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void previous() {
        --index;
    }

    /**
     * DOCUMENT ME!
     */
    public void setBegin() {
        index = 0;
    }
}
