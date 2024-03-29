/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * Timesheet.java
 *
 * Created on 9. Mai 2007, 15:59
 */
package de.cismet.web.timetracker.servlets;

import java.io.IOException;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.cismet.web.timetracker.Database;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  DOCUMENT ME!
 */
public class Timesheet extends HttpServlet {

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
        final Database db = new Database(application.getRealPath("/").replace('\\', '/'));
        String errors = "";

        if (!db.isConnectionOk()) {
            response.sendRedirect(response.encodeRedirectURL(
                    "Timesheet.jsp?u_id="
                            + request.getParameter("u_id")
                            + "&errorMsg=Fehler beim Verbinden mit der Datenbank: "
                            + db.getErrorMessage()));
            return;
        }

        // Variablen fuellen
        final String user = ((request.getParameter("u_id") == null) ? "" : request.getParameter("u_id"));

        try {
            // modified or new data received
            if ((request.getParameter("act") != null) && request.getParameter("act").equals("uebernehmen")
                        && (request.getParameter("mods") != null)) {
                // Auf Rechte pruefen
                final boolean actionIsAllowed = isActionAllowed(user, request.getSession(), db, request);

                if (actionIsAllowed) {
                    final Hashtable newIds = new Hashtable(); // speichert neue OIDs
                    final StringTokenizer st = new StringTokenizer(request.getParameter("mods"), "|");

                    // Durchlaeuft die durch modId uebergebene Liste
                    while (st.countTokens() >= 3) {
                        // speichert die oids der neu angelegten Datensaetze
                        final String oid = st.nextToken();
                        final String col = st.nextToken();
                        final String value = st.nextToken();
                        final String role = (String)request.getSession().getAttribute("role");

                        if (col.indexOf("New") != -1) {
                            // neuen Datensatz einfuegen
                            errors += insertNewData(value, oid, db, role, newIds);
                        } else if (col.equals("delete")) {
                            // Datensatz loeschen
                            errors += deleteData(oid, db, role);
                        } else {
                            // existierenden Datensatz aendern
                            errors += changeData(value, oid, col, db, role, newIds);
                        }
                    }
                } else {
                    errors +=
                        "<script language='javascript'>alert('Diese Aktion darf nur vom Benutzer selbst oder vom zustaendigen Admin ausgefuehrt werden.')</script>";
                }
            }
        } catch (SQLException e) {
            errors += e.getMessage();
        }
        db.close();
        response.sendRedirect(response.encodeRedirectURL(
                "Timesheet.jsp?u_id="
                        + request.getParameter("u_id")
                        + "&KWvon="
                        + request.getParameter("KWvon")
                        + "&KWbis="
                        + request.getParameter("KWbis")
                        + "&jahr="
                        + request.getParameter("jahr")
                        + "&errorMsg="
                        + errors));
    }

    /**
     * aendert bestehenede Datensaetze ab.
     *
     * @param   value   DOCUMENT ME!
     * @param   oid     DOCUMENT ME!
     * @param   col     DOCUMENT ME!
     * @param   db      DOCUMENT ME!
     * @param   role    DOCUMENT ME!
     * @param   newIds  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String changeData(String value,
            String oid,
            final String col,
            final Database db,
            final String role,
            final Hashtable newIds) {
        if (oid.indexOf("New") != -1) {
            if (newIds.get(oid) != null) {
                oid = (String)newIds.get(oid);
            }
        }

        if ((col.equals("annotation") || col.equals("time")) && !value.equals("null")) {
            value = "'" + value + "'";
        }

        try {
            ResultSet rSet = null;
            boolean error = false;

            // auf Admin-Aktion pruefen
            StringBuffer sqlQuery = new StringBuffer(
                    "SELECT admin FROM tt_timesheet_action ta, tt_timesheet ts WHERE ta.id = ts.action AND ts.oid="
                            + oid);
            rSet = db.execute(sqlQuery.toString());
            final StringBuffer sqlQueryAction = new StringBuffer(
                    "SELECT admin FROM tt_timesheet_action WHERE description="
                            + (((value == null) || value.startsWith("'")) ? value : ("'" + value + "'")));
            final ResultSet rSetAction = db.execute(sqlQueryAction.toString());

            if ((rSet != null) && rSet.next()) {
                if ((rSet.getBoolean(1) && !role.equals("admin"))
                            || (rSetAction.next() && rSetAction.getBoolean(1) && !role.equals("admin"))) {
                    // nur ein Admin darf Admin-Aktionen hinzufuegen. Schleifendurchlauf beenden
                    return
                        "<script language='javascript'>alert('Zur &Auml;nderung, L&ouml;schung oder Anlegung eines Admin-Datensatzes werden Admin-Rechte ben&ouml;tigt. Aktion wurde ignoriert')</script>";
                }
            } else {
                return
                    "<script language='javascript'>alert('Es konnte nicht erkannt werden, ob es sich um einen Admin-Datensatz handelt. Aktion wurde ignoriert')</script>";
            }

            if (col.equals("action")) {
                // Aktions-ID ermitteln
                sqlQuery = new StringBuffer("SELECT id FROM tt_timesheet_action WHERE description='" + value + "'");

                rSet = db.execute(sqlQuery.toString());
                if ((rSet != null) && rSet.next()) {
                    value = rSet.getString(1);
                }
            }
            if (col.equals("project_id")) {
                // Projekt-ID ermitteln
                if (!value.equals("null")) {
                    sqlQuery = new StringBuffer("SELECT id FROM tt_projects WHERE title='" + value + "'");

                    rSet = db.execute(sqlQuery.toString());
                    if ((rSet != null) && rSet.next()) {
                        value = rSet.getString(1);
                    } else {
                        error = true;
                    }
                }
            }

            if (!error) {
                sqlQuery = new StringBuffer("UPDATE tt_timesheet SET ");
                sqlQuery.append(col + "=" + value);
                sqlQuery.append(", manual=true");
                sqlQuery.append(" WHERE oid = " + oid);

                db.executeUpdate(sqlQuery.toString());
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return "Fehler beim &Auml;ndern";
        }
        return "";
    }

    /**
     * loescht einen Datensatz.
     *
     * @param   oid   DOCUMENT ME!
     * @param   db    DOCUMENT ME!
     * @param   role  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String deleteData(final String oid, final Database db, final String role) {
        try {
            // auf Admin-Aktion pruefen
            StringBuffer sqlQuery = new StringBuffer(
                    "SELECT admin FROM tt_timesheet_action ta, tt_timesheet ts WHERE ta.id = ts.action AND ts.oid="
                            + oid);
            final ResultSet rSet = db.execute(sqlQuery.toString());

            if ((rSet != null) && rSet.next()) {
                if (rSet.getBoolean(1) && !role.equals("admin")) {
                    // nur ein Admin darf Admin-Aktionen hinzufuegen. Schleifendurchlauf beenden
                    return
                        "<script language='javascript'>alert('Zur &Auml;nderung, L&ouml;schung oder Anlegung eines Admin-Datensatzes werden Admin-Rechte ben&ouml;tigt. Aktion wurde ignoriert')</script>";
                }
            } else {
                return
                    "<script language='javascript'>alert('Es konnte nicht erkannt werden, ob es sich um einen Admin-Datensatz handelt. Aktion wurde ignoriert')</script>";
            }

            sqlQuery = new StringBuffer("DELETE FROM tt_timesheet WHERE oid=");
            sqlQuery.append("" + oid);
            db.executeUpdate(sqlQuery.toString());
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return "Fehler beim L&ouml;schen";
        }

        return "";
    }

    /**
     * legt einen neuen Datensatz an.
     *
     * @param   value   DOCUMENT ME!
     * @param   oid     DOCUMENT ME!
     * @param   db      DOCUMENT ME!
     * @param   role    DOCUMENT ME!
     * @param   newIds  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String insertNewData(final String value,
            final String oid,
            final Database db,
            final String role,
            final Hashtable newIds) {
        // neuen Datensatz einfuegen

        final StringTokenizer stValue = new StringTokenizer(value, ";");
        if (stValue.countTokens() == 3) {
            final String action = stValue.nextToken();
            final String time = stValue.nextToken();
            final String userId = stValue.nextToken();

            // bestimmt die zum Aktionsnamen(-description) passende Aktions-Id und Admin-Flag
            try {
                StringBuffer sqlQuery = new StringBuffer("SELECT id, admin FROM tt_timesheet_action WHERE description="
                                + (((action == null) || action.startsWith("'")) ? action : ("'" + action + "'")));
                final String actionId;
                ResultSet rSet = db.execute(sqlQuery.toString());

                if ((rSet != null) && rSet.next()) {
                    if (rSet.getBoolean(2) && !role.equals("admin")) {
                        // nur ein Admin darf Admin-Aktionen hinzufuegen. Schleifendurchlauf beenden
                        return
                            "<script language='javascript'>alert('Zur &Auml;nderung, L&ouml;schung oder Anlegung eines Admin-Datensatzes werden Admin-Rechte ben&ouml;tigt. Aktion wurde ignoriert')</script>";
                    }
                } else {
                    return
                        "<script language='javascript'>alert('Datenbankfehler. Es konnte nicht erkannt werden, ob es sich um einen Admin-Datensatz handelt. Aktion wurde ignoriert')</script>";
                }

                rSet = db.execute(sqlQuery.toString());
                if ((rSet != null) && rSet.next()) {
                    actionId = rSet.getString(1);

                    sqlQuery = new StringBuffer("INSERT INTO tt_timesheet (action, time, u_id, manual) VALUES(");
                    sqlQuery.append(actionId + ", '" + time + "', " + userId + ", true)");
                    db.executeUpdate(sqlQuery.toString());

                    // oid bestimmen und speichern
                    sqlQuery = new StringBuffer("SELECT oid FROM tt_timesheet WHERE u_id=" + userId);
                    sqlQuery.append(" AND time='" + time + "' ORDER BY oid DESC");
                    rSet = db.execute(sqlQuery.toString());
                    if ((rSet != null) && rSet.next()) {
                        newIds.put(oid, rSet.getString(1));
                    }
                } else {
                    return "ActionId konnte nicht ermittelt werden";
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                return "Fehler beim Einf&uuml;gen";
            }
        }
        return "";
    }

    /**
     * prueft, ob die gewuenschtern Operationen erlaubt sind.
     *
     * @param   user     DOCUMENT ME!
     * @param   session  DOCUMENT ME!
     * @param   db       DOCUMENT ME!
     * @param   request  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    private boolean isActionAllowed(final String user,
            final HttpSession session,
            final Database db,
            final HttpServletRequest request) throws SQLException {
        boolean actionIsAllowed = false;

        if (!user.equals("")) {
            final String suid = (String)(session.getAttribute("id"));
            String company = "";

            final String sqlString = "SELECT company FROM tt_user WHERE id = " + user;

            final ResultSet resultSet = db.execute(sqlString);
            if ((resultSet != null) && resultSet.next()) {
                company = resultSet.getString(1);
            }

            if (suid.equals(user)
                        || (session.getAttribute("role").equals("admin")
                            && ((String)session.getAttribute("company")).equalsIgnoreCase(company))) {
                actionIsAllowed = true;
            } else if ((request.getParameter("act") != null) && request.getParameter("act").equals("uebernehmen")
                        && (request.getParameter("mods") != null)) {
                return false;
            }
        }
        return actionIsAllowed;
    }
}
