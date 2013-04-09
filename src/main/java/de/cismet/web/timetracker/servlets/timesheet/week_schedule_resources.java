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

import java.util.Iterator;
import java.util.Vector;

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
public class week_schedule_resources extends HttpServlet {

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
        final String resource = request.getParameter("resource");
        final String viewClass = request.getParameter("view");
        final String user = request.getParameter("u_id");
        final String PACKAGE = "de.cismet.web.timetracker.views.";
        final Database db = new Database(application.getRealPath("/").replace('\\', '/'));

        try {
            final StringBuffer sqlQuery = null;
            String tagname = null;
            Vector list = null;

            // Instanz der geforderten View erzeugen
            final Class c = Class.forName(PACKAGE + viewClass);
            final Constructor<AbstractView> constructor = c.getConstructor(Database.class); // newInstance();
            final AbstractView view = constructor.newInstance(db);

            if ((resource != null) && resource.equals("actions")) {
                tagname = "action";
                list = view.getActions();
            } else if ((resource != null) && resource.equals("timeIntervals")) {
                int u_id;
                try {
                    u_id = Integer.parseInt(user);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    u_id = 1;
                }
                tagname = "timeIntervals";
                list = view.getTimeIntervals(u_id);
            } else if ((resource != null) && resource.equals("projects")) {
                tagname = "projects";
                list = view.getProjectNames();
            }

            if ((list != null) && (tagname != null)) {
                final Iterator it = list.iterator();
                while (it.hasNext()) {
                    final String send = "<" + tagname + ">" + (String)it.next() + "</" + tagname + ">";
//                              System.out.println(send);
                    out.println(send);
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        db.close();
        out.close();
    }
}
