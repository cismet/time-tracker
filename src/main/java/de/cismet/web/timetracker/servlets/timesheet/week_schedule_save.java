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

import java.sql.ResultSet;

import java.text.SimpleDateFormat;

import java.util.GregorianCalendar;
import java.util.Locale;

import javax.servlet.*;
import javax.servlet.http.*;

import de.cismet.web.timetracker.CalendarItem;
import de.cismet.web.timetracker.Database;
import de.cismet.web.timetracker.views.AbstractView;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class week_schedule_save extends HttpServlet {

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

        if (request.getParameter("saveAnItem") != null) {
            try {
                // System.out.println("save");
                final String action = request.getParameter("action");
                final String annotation = request.getParameter("annotation");
                String strStart = request.getParameter("eventStartDate");
                String strEnd = request.getParameter("eventEndDate");
                final String u_id = request.getParameter("uid");
                String project = request.getParameter("project");
                final String id = request.getParameter("id");
                final String viewClass = request.getParameter("view");

                if ((project == null) || project.equals("")) {
                    project = "null";
                }

                // Datum parsen
                final SimpleDateFormat format = new SimpleDateFormat("E MMM d yyyy HH:mm:s", Locale.US);
                final GregorianCalendar end = new GregorianCalendar();
                final GregorianCalendar start = new GregorianCalendar();

                strEnd = strEnd.replaceAll(" GMT 0200", "");
                strEnd = strEnd.replaceAll(" GMT 0100", "");
                strStart = strStart.replaceAll(" GMT 0200", "");
                strStart = strStart.replaceAll(" GMT 0100", "");
                end.setTime(format.parse(strEnd));
                start.setTime(format.parse(strStart));

                // Instanz der geforderten View erzeugen
                final Class c = Class.forName(PACKAGE + viewClass);
                final Constructor<AbstractView> constructor = c.getConstructor(Database.class); // newInstance();
                final AbstractView view = constructor.newInstance(db);

                // Rechte pruefen
                final String suid = (String)(request.getSession().getAttribute("id"));
                final String company;

                String sqlString = "SELECT company FROM tt_user WHERE id = " + u_id;
                ResultSet rSet = db.execute(sqlString);

                if ((rSet != null) && rSet.next()) {
                    company = rSet.getString(1);
                } else {
                    out.println("NO_RIGHTS");
                    return;
                }

                if (!suid.equals(u_id)
                            && !(request.getSession().getAttribute("role").equals("admin")
                                && ((String)request.getSession().getAttribute("company")).equalsIgnoreCase(company))) {
                    // die benoetigten Rechte fehlen
                    out.println("NO_RIGHTS");
                    return;
                }

                // Wird AdminAktion erstellt oder von anderer Aktion auf Admin-Aktion umgestellt
                sqlString = "SELECT admin FROM tt_timesheet_action WHERE description = '" + action + "'";
                rSet = db.execute(sqlString);

                if ((rSet != null) && rSet.next() && rSet.getBoolean(1)
                            && !request.getSession().getAttribute("role").equals("admin")) {
                    out.println("NO_RIGHTS");
                    return;
                }

                if (request.getParameter("newItem") != null) {                    // This is a new item
                    final CalendarItem ci = new CalendarItem(action, annotation, end, id, project, start);
                    out.println(view.saveNewItem(ci, Integer.parseInt(u_id)));    // The id is sent back to ajax so that
                                                                                  // it could update the id of the
                                                                                  // entry, i.e. update it next time
                                                                                  // instead of saving another new item.
                } else {
                    final CalendarItem ci = new CalendarItem(action, annotation, end, id, project, start);
                    view.saveModifiedItem(ci, Integer.parseInt(u_id));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        db.close();
        out.close();
    }
}
