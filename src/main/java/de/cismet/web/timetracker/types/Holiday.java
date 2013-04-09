/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.web.timetracker.types;

import java.util.GregorianCalendar;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class Holiday implements Comparable {

    //~ Instance fields --------------------------------------------------------

    private String name;
    private GregorianCalendar date;
    private boolean isHalfDay;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Holiday object.
     */
    public Holiday() {
        this(null, null, false);
    }

    /**
     * Creates a new Holiday object.
     *
     * @param  name  DOCUMENT ME!
     */
    public Holiday(final String name) {
        this(name, null, false);
    }

    /**
     * Creates a new Holiday object.
     *
     * @param  date  DOCUMENT ME!
     */
    public Holiday(final GregorianCalendar date) {
        this(null, date, false);
    }

    /**
     * Creates a new Holiday object.
     *
     * @param  name       DOCUMENT ME!
     * @param  date       DOCUMENT ME!
     * @param  isHalfDay  DOCUMENT ME!
     */
    public Holiday(final String name, GregorianCalendar date, final boolean isHalfDay) {
        this.name = name;
        if (date != null) {
            this.date = (GregorianCalendar)date.clone();
        } else {
            date = null;
        }
        this.isHalfDay = isHalfDay;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  name  DOCUMENT ME!
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  date  DOCUMENT ME!
     */
    public void setDate(final GregorianCalendar date) {
        this.date = (GregorianCalendar)date.clone();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  isHalfDay  DOCUMENT ME!
     */
    public void setIsHalfDay(final boolean isHalfDay) {
        this.isHalfDay = isHalfDay;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getName() {
        return name;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public GregorianCalendar getDate() {
        return date;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isHalfDay() {
        return isHalfDay;
    }

    @Override
    public int compareTo(final Object o) {
        final Holiday other = (Holiday)o;
        if ((this.date.get(GregorianCalendar.YEAR) == other.getDate().get(GregorianCalendar.YEAR))
                    && (this.date.get(GregorianCalendar.MONTH) == other.getDate().get(GregorianCalendar.MONTH))
                    && (this.date.get(GregorianCalendar.DATE) == other.getDate().get(GregorianCalendar.DATE))) {
            return 0;
        } else if (this.date.before(other.getDate())) {
            return -1;
        } else {
            return 1;
        }
    }
}
