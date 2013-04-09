/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.web.timetracker.servlets.timesheet;

import java.io.*;

import java.lang.reflect.Constructor;

import java.net.*;

import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.*;
import javax.servlet.http.*;

import de.cismet.web.timetracker.CalendarItem;
import de.cismet.web.timetracker.Database;
import de.cismet.web.timetracker.views.AbstractView;

/**
 * DOCUMENT ME!
 *
 * @author   Thorsten
 * @version  DOCUMENT ME!
 */
public class week_schedule_getItems extends HttpServlet {

    //~ Methods ----------------------------------------------------------------

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param   request   servlet request
     * @param   response  servlet response
     *
     * @throws  ServletException  DOCUMENT ME!
     * @throws  IOException       DOCUMENT ME!
     */
    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
        IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param   request   servlet request
     * @param   response  servlet response
     *
     * @throws  ServletException  DOCUMENT ME!
     * @throws  IOException       DOCUMENT ME!
     */
    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
        IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }
    // </editor-fold>

    //~ Instance fields --------------------------------------------------------

    ServletContext application;

    //~ Methods ----------------------------------------------------------------

    /**
     * Initializes the servlet.
     *
     * @param   config  DOCUMENT ME!
     *
     * @throws  ServletException  DOCUMENT ME!
     */
    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
        application = config.getServletContext();
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param   request   servlet request
     * @param   response  servlet response
     *
     * @throws  ServletException  DOCUMENT ME!
     * @throws  IOException       DOCUMENT ME!
     */
    protected void processRequest(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        final PrintWriter out = response.getWriter();
        final String PACKAGE = "de.cismet.web.timetracker.views.";
        final Database db = new Database(application.getRealPath("/").replace('\\', '/'));

        try {
            final String strDay = request.getParameter("day");
            final String strMonth = request.getParameter("month");
            final String strYear = request.getParameter("year");
            final String strUser = request.getParameter("user");
            final String viewClass = request.getParameter("view");
            int day;
            int month;
            int year;
            int user;

            // Anfang- und End-Datum der Woche berechnen

            try {
                day = Integer.parseInt(strDay);
                month = Integer.parseInt(strMonth) - 1;
                year = Integer.parseInt(strYear);
                user = Integer.parseInt(strUser);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                day = 1;
                month = 1;
                year = 1900;
                user = 1;
            }

            final GregorianCalendar dateFrom = new GregorianCalendar(year, month, day, 0, 0);
            final GregorianCalendar dateTill = new GregorianCalendar(year, month, day, 23, 59, 59);

            dateFrom.setFirstDayOfWeek(GregorianCalendar.MONDAY);

            while (dateFrom.get(GregorianCalendar.DAY_OF_WEEK) != GregorianCalendar.MONDAY) {
                dateFrom.add(GregorianCalendar.DATE, -1);
            }

            dateTill.setFirstDayOfWeek(GregorianCalendar.MONDAY);
            while (dateTill.get(GregorianCalendar.DAY_OF_WEEK) != GregorianCalendar.SUNDAY) {
                dateTill.add(GregorianCalendar.DATE, 1);
            }

            // Instanz der geforderten View erzeugen
            final Class c = Class.forName(PACKAGE + viewClass);
            final Constructor<AbstractView> constructor = c.getConstructor(Database.class); // newInstance();
            final AbstractView view = constructor.newInstance(db);

            for (int i = 0; i < 7; ++i) {
                try {
                    final Vector items = view.getItemsOfDay((GregorianCalendar)dateFrom.clone(), user);
                    final Iterator it = items.iterator();
                    while (it.hasNext()) {
                        final String appointment = ((CalendarItem)it.next()).toString();
                        out.print(appointment);
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                dateFrom.add(GregorianCalendar.DATE, 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

/*
        neues Format:
        <item>
                <id> </id>
                <action> </action>
                <project></project>
                <annotation> </annotation>
                <eventStartDate> </eventStartDate>
                <eventEndDate> </eventEndDate>
        </item>
*/
        db.close();
        out.close();
    }
}
