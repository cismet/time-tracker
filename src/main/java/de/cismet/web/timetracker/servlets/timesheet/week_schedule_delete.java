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

import javax.servlet.*;
import javax.servlet.http.*;

import de.cismet.web.timetracker.Database;
import de.cismet.web.timetracker.views.AbstractView;

/**
 * DOCUMENT ME!
 *
 * @author   Thorsten
 * @version  DOCUMENT ME!
 */
public class week_schedule_delete extends HttpServlet {

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
        final Database db = new Database(application.getRealPath("/").replace('\\', '/'));

        try {
            final String PACKAGE = "de.cismet.web.timetracker.views.";
            final String id = request.getParameter("eventToDeleteId");
            final String viewClass = request.getParameter("view");

            if (request.getParameter("eventToDeleteId") != null) {
                // Auf Rechte pruefen
                final String suid = (String)(request.getSession().getAttribute("id"));
                int uid = 0;
                String company = "";

                String sqlString = "SELECT u_id FROM tt_timesheet WHERE oid = " + id;
                ResultSet rSet = db.execute(sqlString);
                if ((rSet != null) && rSet.next()) {
                    uid = rSet.getInt(1);
                } else {
                    out.println("-1");
                    return;
                }

                sqlString = "SELECT company FROM tt_user WHERE id = " + uid;
                rSet = db.execute(sqlString);
                if ((rSet != null) && rSet.next()) {
                    company = rSet.getString(1);
                } else {
                    company = "";
                }

                if (suid.equals("" + uid)
                            || (request.getSession().getAttribute("role").equals("admin")
                                && ((String)request.getSession().getAttribute("company")).equalsIgnoreCase(company))) {
                    // Instanz der geforderten View erzeugen
                    final Class c = Class.forName(PACKAGE + viewClass);
                    final Constructor<AbstractView> constructor = c.getConstructor(Database.class); // newInstance();
                    final AbstractView view = constructor.newInstance(db);

                    if (view.deleteItem(id)) {
                        out.println("OK");
                    }
                } else {
                    // die benoetigten rechte fehlen
                    out.println("NO_RIGHTS");
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.close();
        out.close();
    }
}
