/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.web.timetracker;

import java.text.SimpleDateFormat;

import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class CalendarItem {

    //~ Static fields/initializers ---------------------------------------------

    public static final int PROJECT_NOT_DEFINED = -1;

    //~ Instance fields --------------------------------------------------------

    private String id;
    private String annotation;
    private String project;
    private String action;
    private GregorianCalendar start;
    private GregorianCalendar end;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CalendarItem object.
     */
    public CalendarItem() {
    }

    /**
     * Creates a new CalendarItem object.
     *
     * @param  action      DOCUMENT ME!
     * @param  annotation  DOCUMENT ME!
     * @param  end         DOCUMENT ME!
     * @param  id          DOCUMENT ME!
     * @param  project     DOCUMENT ME!
     * @param  start       DOCUMENT ME!
     */
    public CalendarItem(final String action,
            final String annotation,
            final GregorianCalendar end,
            final String id,
            final String project,
            final GregorianCalendar start) {
        this.action = action;
        this.annotation = annotation;
        this.end = end;
        this.id = id;
        this.project = project;
        this.start = start;
    }

    //~ Methods ----------------------------------------------------------------

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
    public String getAnnotation() {
        return annotation;
    }
    /**
     * DOCUMENT ME!
     *
     * @param  annotation  DOCUMENT ME!
     */
    public void setAnnotation(final String annotation) {
        this.annotation = annotation;
    }
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public GregorianCalendar getEnd() {
        return end;
    }
    /**
     * DOCUMENT ME!
     *
     * @param  end  DOCUMENT ME!
     */
    public void setEnd(final GregorianCalendar end) {
        this.end = end;
    }
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getId() {
        return id;
    }
    /**
     * DOCUMENT ME!
     *
     * @param  id  DOCUMENT ME!
     */
    public void setId(final String id) {
        this.id = id;
    }
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getProject() {
        return project;
    }
    /**
     * DOCUMENT ME!
     *
     * @param  project  DOCUMENT ME!
     */
    public void setProject(final String project) {
        this.project = project;
    }
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public GregorianCalendar getStart() {
        return start;
    }
    /**
     * DOCUMENT ME!
     *
     * @param  start  DOCUMENT ME!
     */
    public void setStart(final GregorianCalendar start) {
        this.start = start;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  die Daten des CalendarItem im XML-Format
     */
    @Override
    public String toString() {
        final SimpleDateFormat formater = new SimpleDateFormat("E, dd MMM yyyy HH:mm", Locale.US);
        final StringBuffer xmlResult = new StringBuffer();
        xmlResult.append("<item>\n");
        xmlResult.append("<id>" + id + "</id>\n");
        xmlResult.append("<annotation>" + annotation + "</annotation>\n");
        xmlResult.append("<eventStartDate>" + formater.format(start.getTime()) + " GMT+2</eventStartDate>\n");
        xmlResult.append("<eventEndDate>" + formater.format(end.getTime()) + " GMT+2</eventEndDate>\n");
        xmlResult.append("<action>" + action + "</action>\n");
        if (project != null) {
            xmlResult.append("<project>" + project + "</project>\n");
        }
        xmlResult.append("</item>\n");

        return xmlResult.toString();
    }
}
