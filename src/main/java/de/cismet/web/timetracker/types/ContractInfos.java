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
public class ContractInfos implements Cloneable {

    //~ Instance fields --------------------------------------------------------

    int uId;
    GregorianCalendar fromDate;
    GregorianCalendar toDate;
    double whow;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ContractInfos object.
     *
     * @param  fromDate  DOCUMENT ME!
     * @param  toDate    DOCUMENT ME!
     * @param  id        DOCUMENT ME!
     * @param  whow      DOCUMENT ME!
     */
    public ContractInfos(final GregorianCalendar fromDate,
            final GregorianCalendar toDate,
            final int id,
            final double whow) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        uId = id;
        this.whow = whow;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public GregorianCalendar getFromDate() {
        return fromDate;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fromDate  DOCUMENT ME!
     */
    public void setFromDate(final GregorianCalendar fromDate) {
        this.fromDate = fromDate;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public GregorianCalendar getToDate() {
        return toDate;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  toDate  DOCUMENT ME!
     */
    public void setToDate(final GregorianCalendar toDate) {
        this.toDate = toDate;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getUId() {
        return uId;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  id  DOCUMENT ME!
     */
    public void setUId(final int id) {
        uId = id;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getWhow() {
        return whow;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  whow  DOCUMENT ME!
     */
    public void setWhow(final double whow) {
        this.whow = whow;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
