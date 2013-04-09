/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * netModusAction.java
 *
 * Created on 17. Juli 2008, 13:23
 *
 * To change this template, choose Tools | Template Manager
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
public class NetModusAction {

    //~ Static fields/initializers ---------------------------------------------

    public static final int START = 0;
    public static final int END = 1;

    //~ Instance fields --------------------------------------------------------

    private int modus;
    private GregorianCalendar time;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of netModusAction.
     */
    public NetModusAction() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getModus() {
        return modus;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  modus  DOCUMENT ME!
     */
    public void setModus(final int modus) {
        this.modus = modus;
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
