/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.web.timetracker.types;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class HoursOfWork {

    //~ Instance fields --------------------------------------------------------

    boolean autoPause;
    double hours;
    boolean needAutoPause;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new HoursOfWork object.
     */
    public HoursOfWork() {
        autoPause = false;
        hours = 0.0;
    }

    /**
     * Creates a new HoursOfWork object.
     *
     * @param  autoPause  DOCUMENT ME!
     */
    public HoursOfWork(final boolean autoPause) {
        this.autoPause = autoPause;
        this.hours = 0.0;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getHours() {
        return hours;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean getAutoPause() {
        return autoPause;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  hours  DOCUMENT ME!
     */
    public void setHours(final double hours) {
        this.hours = hours;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  autoPause  DOCUMENT ME!
     */
    public void setAutoPause(final boolean autoPause) {
        this.autoPause = autoPause;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isNeedAutoPause() {
        return needAutoPause;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  needAutoPause  DOCUMENT ME!
     */
    public void setNeedAutoPause(final boolean needAutoPause) {
        this.needAutoPause = needAutoPause;
    }
}
