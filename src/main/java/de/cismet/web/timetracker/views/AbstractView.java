/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.web.timetracker.views;

import java.sql.SQLException;

import java.util.GregorianCalendar;
import java.util.Vector;

import de.cismet.web.timetracker.CalendarItem;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public abstract class AbstractView {

    //~ Methods ----------------------------------------------------------------

    /**
     * liefert die Termine bzw. Projekte eines Tages im XML-Format. Format:
     *
     * <pre>
                    &lt;item&gt;
                                    &lt;id&gt;             &lt;/id&gt;
                                    &lt;action&gt;         &lt;/action&gt;
                                    &lt;project&gt;        &lt;/project&gt;
                                    &lt;annotation&gt;     &lt;/annotation&gt;
                                    &lt;eventStartDate&gt; &lt;/eventStartDate&gt;
                                    &lt;eventEndDate&gt;   &lt;/eventEndDate&gt;
                    &lt;/item&gt;
     *</pre>
     *
     * @param   day   Tag, auf den sich die zurueckgegebenen Termine beziehen
     * @param   u_id  Id des Benutzers, auf den sich die zurueckgegebenen Termine beziehen
     *
     * @return  ein Vektor mit den CalendarItem-Objekten eines Tages
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    public abstract Vector getItemsOfDay(GregorianCalendar day, int u_id) throws SQLException;

    /**
     * liefert die moeglichen Actioncommands.
     *
     * @return  Actioncommands
     */
    public abstract Vector getActions();

    /**
     * speichert das uebergebene Objekt in der Datenbank.
     *
     * @param   item  DOCUMENT ME!
     * @param   u_id  DOCUMENT ME!
     *
     * @return  die Id des gespeicherten Items, im Fehlerfall -1
     */
    public abstract String saveNewItem(CalendarItem item, int u_id);

    /**
     * Speichert die Aenderungen des uebergebenen Objekts in der Datenbank.
     *
     * @param   item  DOCUMENT ME!
     * @param   u_id  DOCUMENT ME!
     *
     * @return  true, wenn die Aenderungen erfolgreich gespeichert wurden. Sonst false.
     */
    public abstract boolean saveModifiedItem(CalendarItem item, int u_id);

    /**
     * loescht das durch die uebergebene ID bestimmte Element.
     *
     * @param   id  id des zu loeschenden Elements
     *
     * @return  true, falls das Loeschen erfolgreich war.
     */
    public abstract boolean deleteItem(String id);

    /**
     * liefert die Zeitintervallen, in denen ein Element vergroessert werden darf (in Minuten).
     *
     * @param   u_id  Id des Benutzers, dessen Zeitintervalle zurueckgegeben werden sollen
     *
     * @return  einen Vektor, mit den, von der aktuellen View, bestimmten Zeitintervallen als Strings.
     */
    public Vector getTimeIntervals(final int u_id) {
        return null;
    }

    /**
     * liefert die Projektnamen, die von der View unterstuetzt werden.
     *
     * @return  die Projektnamen
     */
    public Vector getProjectNames() {
        return null;
    }
}
