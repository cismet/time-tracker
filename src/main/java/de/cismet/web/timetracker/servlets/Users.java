/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * Users.java
 *
 * Created on 24. Juli 2007, 14:03
 */
package de.cismet.web.timetracker.servlets;

import java.io.IOException;

import java.security.NoSuchAlgorithmException;

import java.sql.SQLException;

import java.text.SimpleDateFormat;

import java.util.GregorianCalendar;
import java.util.Locale;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.cismet.web.timetracker.Database;
import de.cismet.web.timetracker.TimeTrackerConstants;
import de.cismet.web.timetracker.TimeTrackerFunctions;

/**
 * DOCUMENT ME!
 *
 * @author   Thorsten
 * @version  DOCUMENT ME!
 */
public class Users extends HttpServlet {

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

        if (!db.isConnectionOk()) {
            response.sendRedirect(response.encodeRedirectURL(
                    "User.jsp?u_id="
                            + request.getParameter("u_id")
                            + "&errorMsg=Fehler beim Verbinden mit der Datenbank: "
                            + db.getErrorMessage()));
            db.close();
            return;
        }

        String userId = request.getParameter("userId");
        final String name = request.getParameter("name");
        String pwd = request.getParameter("pwd");
        final String company = request.getParameter("company");
        final String buddyname = request.getParameter("buddyname");
        final boolean exactHolidays = ((request.getParameter("exactHolidays") != null)
                        && request.getParameter("exactHolidays").equals("ja"));
        final boolean netHoursOfWork = ((request.getParameter("netHoursOfWork") != null)
                        && request.getParameter("netHoursOfWork").equals("ja"));
        final String suid = (String)(request.getSession().getAttribute("id"));

        // Auf Rechte pruefen
        if (!(suid.equals(userId)
                        || (request.getSession().getAttribute("role").equals("admin")
                            && ((String)request.getSession().getAttribute("company")).equalsIgnoreCase(company)))) {
            response.sendRedirect(response.encodeRedirectURL(
                    "User.jsp?u_id="
                            + request.getParameter("u_id")
                            + "&errorMsg=Sie sind nicht befugt, diese Aktion auszufuehren. Das darf nur ein Administrator."));
            return;
        }

        try {
            // wenn userId == null, dann wurde die ID des zuaendernden Nutzers nicht uebertragen
            if ((request.getParameter("submitButton") != null) && (userId != null)) {
                if (userId.equals("new")) {
                    pwd = ((pwd == null) ? "null" : ("'" + TimeTrackerFunctions.calcSHA1(pwd) + "'"));
                    userId = "" + (db.getMaxId("tt_user") + 1);
                    final StringBuffer sqlQuery = new StringBuffer(
                            "INSERT INTO tt_user (id, name, pass, buddyname, company, \"exactHoliday\") VALUES(");
                    sqlQuery.append(userId);
                    sqlQuery.append(", " + TimeTrackerFunctions.prepareString(name));
                    sqlQuery.append(", " + pwd);
                    sqlQuery.append(", " + TimeTrackerFunctions.prepareString(buddyname));
                    sqlQuery.append(", " + TimeTrackerFunctions.prepareString(company));
                    sqlQuery.append(", " + exactHolidays + ")");

                    db.executeUpdate(sqlQuery.toString());
                } else {
                    String secretPassword = "";
                    for (int i = 0; i < pwd.length(); ++i) {
                        secretPassword += "?";
                    }

                    final StringBuffer sqlQuery = new StringBuffer("UPDATE tt_user SET name = "
                                    + TimeTrackerFunctions.prepareString(name));

                    if ((pwd == null) || !pwd.equals(secretPassword)) {
                        pwd = ((pwd == null) ? "null" : ("'" + TimeTrackerFunctions.calcSHA1(pwd) + "'"));
                        sqlQuery.append(", pass = " + pwd);
                    }

                    sqlQuery.append(", buddyname = " + TimeTrackerFunctions.prepareString(buddyname));
                    sqlQuery.append(", company = " + TimeTrackerFunctions.prepareString(company));
                    sqlQuery.append(", \"exactHoliday\" = " + exactHolidays);
                    sqlQuery.append(" WHERE id = " + userId);

                    db.executeUpdate(sqlQuery.toString());
                }
                // Netto Arbeitszeit Buchung machen, falls noetig
                if (netHoursOfWork != db.isUserInNetMode(Integer.parseInt(userId))) {
                    insertNetHoursOfWork(db, netHoursOfWork, userId);
                }
            }
        } catch (SQLException e) {
            response.sendRedirect(response.encodeRedirectURL(
                    "User.jsp?u_id="
                            + request.getParameter("u_id")
                            + "&errorMsg="
                            + e.getMessage()));
            return;
        } catch (NoSuchAlgorithmException e) {
            response.sendRedirect(response.encodeRedirectURL(
                    "User.jsp?u_id="
                            + request.getParameter("u_id")
                            + "&errorMsg="
                            + e.getMessage()));
            return;
        } finally {
            db.close();
        }

        response.sendRedirect(response.encodeRedirectURL("User.jsp?u_id=" + request.getParameter("u_id")));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   db       DOCUMENT ME!
     * @param   isStart  DOCUMENT ME!
     * @param   u_id     DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    private void insertNetHoursOfWork(final Database db, final boolean isStart, final String u_id) throws SQLException {
        final GregorianCalendar now = new GregorianCalendar();
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        final int action = (isStart ? TimeTrackerConstants.NET_HOURS_OF_WORK_START
                                    : TimeTrackerConstants.NET_HOURS_OF_WORK_END);
        final String sqlQuery = "INSERT INTO tt_timesheet (time, action, u_id, manual) VALUES ('"
                    + formatter.format(now.getTime()) + "', " + action + ", " + u_id + ", true)";
        System.out.println("query:\n" + sqlQuery);
        db.executeUpdate(sqlQuery);
    }
}
