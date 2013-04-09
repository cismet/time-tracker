/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * ProjectShortcuts.java
 *
 * Created on 12. Juli 2007, 14:20
 */
package de.cismet.web.timetracker.servlets;

import java.io.IOException;

import java.sql.SQLException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.cismet.web.timetracker.Database;

/**
 * DOCUMENT ME!
 *
 * @author   Thorsten
 * @version  DOCUMENT ME!
 */
public class ProjectShortcuts extends HttpServlet {

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
        final String project = request.getParameter("p_id");
        if (!((String)request.getSession().getAttribute("role")).equals("admin")) {
            response.sendRedirect(response.encodeRedirectURL(
                    "ProjectShortcuts.jsp?u_id="
                            + request.getParameter("u_id")
                            + "&p_id="
                            + project
                            + "&errorMsg=Zugriff verweigert. Diese Seite ist Administratoren vorbehalten."));
            return;
        }

        final Database db = new Database(application.getRealPath("/").replace('\\', '/'));
        if (!db.isConnectionOk()) {
            response.sendRedirect(response.encodeRedirectURL(
                    "ProjectShortcuts.jsp?u_id="
                            + request.getParameter("u_id")
                            + "&p_id="
                            + project
                            + "&errorMsg=Fehler beim Verbinden mit der Datenbank: "
                            + db.getErrorMessage()));
            db.close();
            return;
        }

        // modified or new data received
        try {
            final String shortcutId = request.getParameter("shortcutId");
            if ((request.getParameter("act") != null)
                        && (request.getParameter("act").equals("uebernehmen")
                            || request.getParameter("act").equals("delete"))
                        && (shortcutId != null)) {
                String shortcut = request.getParameter("name");
                shortcut = ((shortcut == null) ? "null" : ("'" + shortcut + "'"));

                if (request.getParameter("act").equals("delete")) { // loeschen
                    final StringBuffer sqlQuery = new StringBuffer("DELETE FROM tt_projectshortcuts WHERE id = "
                                    + shortcutId);

                    db.executeUpdate(sqlQuery.toString());
                } else if (shortcutId.equals("new")) { // hinzufuegen
                    final StringBuffer sqlQuery = new StringBuffer(
                            "INSERT INTO tt_projectshortcuts (id, projectid, shortcut) VALUES(");
                    sqlQuery.append((db.getMaxId("tt_projectshortcuts") + 1) + ", ");
                    sqlQuery.append(project + ", ");
                    sqlQuery.append(shortcut + ")");

                    db.executeUpdate(sqlQuery.toString());
                } else if (shortcutId != null) { // aendern
                    final StringBuffer sqlQuery = new StringBuffer("UPDATE tt_projectshortcuts SET ");
                    sqlQuery.append("shortcut = " + shortcut);
                    sqlQuery.append(" WHERE id = " + shortcutId);

                    db.executeUpdate(sqlQuery.toString());
                }
            }
        } catch (SQLException e) {
            response.sendRedirect(response.encodeRedirectURL(
                    "ProjectShortcuts.jsp?u_id="
                            + request.getParameter("u_id")
                            + "&p_id="
                            + project
                            + "&errorMsg=Folgender Fehler ist aufgetreten: "
                            + e.getMessage()));
            db.close();
            return;
        }
        response.sendRedirect(response.encodeRedirectURL(
                "ProjectShortcuts.jsp?u_id="
                        + request.getParameter("u_id")
                        + "&p_id="
                        + project));
    }
}
