/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * Projects.java
 *
 * Created on 12. Juli 2007, 14:59
 */
package de.cismet.web.timetracker.servlets;

import java.io.IOException;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.cismet.web.timetracker.Database;
import de.cismet.web.timetracker.TimeTrackerFunctions;

/**
 * DOCUMENT ME!
 *
 * @author   Thorsten
 * @version  DOCUMENT ME!
 */
public class Projects extends HttpServlet {

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
        if (!((String)request.getSession().getAttribute("role")).equals("admin")) {
            response.sendRedirect(response.encodeRedirectURL(
                    "Projects.jsp?u_id="
                            + request.getParameter("u_id")
                            + "&errorMsg=Zugriff verweigert. Diese Seite ist Administratoren vorbehalten."));
            return;
        }

        final Database db = new Database(application.getRealPath("/").replace('\\', '/'));

        if (!db.isConnectionOk()) {
            response.sendRedirect(response.encodeRedirectURL(
                    "Projects.jsp?u_id="
                            + request.getParameter("u_id")
                            + "&errorMsg=Fehler beim Verbinden mit der Datenbank: "
                            + db.getErrorMessage()));
            db.close();
            return;
        }

        // modified or new data received
        try {
            if ((request.getParameter("act") != null) && request.getParameter("act").equals("uebernehmen")
                        && (request.getParameter("row") != null)) {
                final String v_title = TimeTrackerFunctions.prepareString(request.getParameter("name"));
                final boolean v_active = ((request.getParameter("aktiv") == null) ? false : true);
                final String v_shortcut = TimeTrackerFunctions.prepareString(request.getParameter("shortcut"));
                final String mainProject = request.getParameter("mp_id");
                final String mainProjectId = getMainProjectId(mainProject);
                final boolean v_isSubproject = !mainProjectId.equals("null");

                if (request.getParameter("row").equals("new")) {
                    final int v_projectId = (db.getMaxId("tt_projects") + 1);
                    StringBuffer sqlQuery = new StringBuffer(
                            "INSERT INTO tt_projects (id, mainprojectid, issubproject, title, active) VALUES(");
                    sqlQuery.append(v_projectId + ", ");
                    sqlQuery.append(mainProjectId + ", ");
                    sqlQuery.append(v_isSubproject + ", ");
                    sqlQuery.append(v_title + ", ");
                    sqlQuery.append(v_active + ")");

                    db.executeUpdate(sqlQuery.toString());
                    if (!v_shortcut.equals("null")) {
                        sqlQuery = new StringBuffer(
                                "INSERT INTO tt_projectshortcuts (id, projectid, shortcut) VALUES(");
                        sqlQuery.append((db.getMaxId("tt_projectshortcuts") + 1) + ", ");
                        sqlQuery.append(v_projectId + ", ");
                        sqlQuery.append(v_shortcut + ")");

                        db.executeUpdate(sqlQuery.toString());
                    }
                } else if (request.getParameter("row") != null) {
                    StringBuffer sqlQuery = new StringBuffer("UPDATE tt_projects SET ");
                    sqlQuery.append("mainprojectid = " + mainProjectId);
                    sqlQuery.append(", issubproject = " + v_isSubproject);
                    sqlQuery.append(", title = " + v_title);
                    sqlQuery.append(", active = " + v_active);
                    sqlQuery.append(" WHERE id = " + request.getParameter("row"));

                    db.executeUpdate(sqlQuery.toString());

                    // pruefe, ob Shortcut angelegt oder geaendert werden muessen
                    sqlQuery = new StringBuffer("SELECT shortcut FROM tt_projectshortcuts WHERE projectid = "
                                    + request.getParameter("row"));

                    final ResultSet rSet = db.execute(sqlQuery.toString());
                    if ((rSet != null) && rSet.next()) {
                        sqlQuery = new StringBuffer();

                        if (v_shortcut.equals("null")) {                    // Shortcut loeschen
                            sqlQuery.append("DELETE FROM tt_projectshortcuts WHERE ");
                            sqlQuery.append("projectid = " + request.getParameter("row"));
                            db.executeUpdate(sqlQuery.toString());
                        } else if (!rSet.getString(1).equals(v_shortcut)) { // Shortcut wird geaendert
                            sqlQuery.append("UPDATE tt_projectshortcuts SET ");
                            sqlQuery.append("shortcut = " + v_shortcut);
                            sqlQuery.append(" WHERE projectid = " + request.getParameter("row"));
                        }

                        db.executeUpdate(sqlQuery.toString());
                    } else if (!v_shortcut.equals("null")) { // neuen Shortcut erstellen
                        sqlQuery = new StringBuffer(
                                "INSERT INTO tt_projectshortcuts (id, projectid, shortcut) VALUES(");
                        sqlQuery.append((db.getMaxId("tt_projectshortcuts") + 1) + ", ");
                        sqlQuery.append(request.getParameter("row") + ", " + v_shortcut + ")");

                        db.executeUpdate(sqlQuery.toString());
                    }
                }
            }
        } catch (SQLException e) {
            response.sendRedirect(response.encodeRedirectURL(
                    "Projects.jsp?u_id="
                            + request.getParameter("u_id")
                            + "&errorMsg=Folgender Fehler ist aufgetreten: "
                            + e.getMessage()));
            db.close();
            return;
        }
        db.close();
        response.sendRedirect(response.encodeRedirectURL("Projects.jsp?u_id=" + request.getParameter("u_id")));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mainProject  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getMainProjectId(final String mainProject) {
        String mainProjectId = "null";

        // Hauptprojekt ID bestimmen
        if ((mainProject != null) && (mainProject.indexOf('-') != -1)) {
            mainProjectId = mainProject.substring(0, mainProject.indexOf('-'));
        } else if ((mainProject != null) && !mainProject.equals("")) {
            try {
                mainProjectId = Integer.parseInt(mainProject) + "";
            } catch (NumberFormatException e) {
                // do nothing
            }
        }

        return mainProjectId;
    }
}
