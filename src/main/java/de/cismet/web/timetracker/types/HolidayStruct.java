/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.web.timetracker.types;

/**
 * speichert die Urlaubstage des aktuellen Jahres, den Resturlaub des aktuellen Jahres, den Resturlaub des letzten
 * Jahres und die Anzahl der Urlaubstage pro Jahr.
 *
 * @version  $Revision$, $Date$
 */
public class HolidayStruct {

    //~ Instance fields --------------------------------------------------------

    private double holidaysThisYear;
    private double residualLeaveFromThisYear;
    private double residualLeaveFromLastYear;
    private double holidayPerYear;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new HolidayStruct object.
     */
    public HolidayStruct() {
        this.holidaysThisYear = 0.0;
        this.residualLeaveFromLastYear = 0.0;
        this.holidayPerYear = 0.0;
        this.residualLeaveFromThisYear = 0.0;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getHolidaysThisYear() {
        return holidaysThisYear;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  holidaysThisYear  DOCUMENT ME!
     */
    public void setHolidaysThisYear(final double holidaysThisYear) {
        this.holidaysThisYear = holidaysThisYear;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getResidualLeaveFromLastYear() {
        return residualLeaveFromLastYear;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  residualLeaveFromLastYear  DOCUMENT ME!
     */
    public void setResidualLeaveFromLastYear(final double residualLeaveFromLastYear) {
        this.residualLeaveFromLastYear = residualLeaveFromLastYear;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getHolidayPerYear() {
        return holidayPerYear;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  holidayPerYear  DOCUMENT ME!
     */
    public void setHolidayPerYear(final double holidayPerYear) {
        this.holidayPerYear = holidayPerYear;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getResidualLeaveFromThisYear() {
        return residualLeaveFromThisYear;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  residualLeaveFromThisYear  DOCUMENT ME!
     */
    public void setResidualLeaveFromThisYear(final double residualLeaveFromThisYear) {
        this.residualLeaveFromThisYear = residualLeaveFromThisYear;
    }
}
