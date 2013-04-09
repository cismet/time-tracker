/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * Contracts.java
 *
 * Created on 6. Juli 2007, 15:58
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

/**
 * DOCUMENT ME!
 *
 * @author   Thorsten
 * @version  DOCUMENT ME!
 */
public class Contracts extends HttpServlet {

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
                    "Contracts.jsp?u_id="
                            + request.getParameter("u_id")
                            + "&errorMsg=Fehler beim Verbinden mit der Datenbank: "
                            + db.getErrorMessage()));
            db.close();
            return;
        }

        final String contractId = request.getParameter("contractId");
        // modified or new data received
        if ((contractId != null) && !contractId.equals("")) {
            try {
                final String user = request.getParameter("u_id");
                String v_to_date = request.getParameter("to_date");
                String v_from_date = request.getParameter("from_date");
                String v_ydoh = request.getParameter("ydoh");
                String v_whow = request.getParameter("whow");
                final String suid = (String)(request.getSession().getAttribute("id"));
                String company = "";

                v_to_date = v_to_date.equals("") ? "null" : ("'" + v_to_date + "'");
                v_from_date = v_from_date.equals("") ? "null" : ("'" + v_from_date + "'");
                v_ydoh = ((v_ydoh != null) && v_ydoh.equals("")) ? null : v_ydoh;
                v_whow = ((v_whow != null) && v_whow.equals("")) ? null : v_whow;

                // Auf Rechte pruefen
                final String sqlString = "SELECT company FROM tt_user WHERE id = " + user;
                final ResultSet rSet = db.execute(sqlString);
                if ((rSet != null) && rSet.next()) {
                    company = rSet.getString(1);
                }
                final String userCompany = (String)request.getSession().getAttribute("company");
                final boolean isAdmin = request.getSession().getAttribute("role").equals("admin");

                if (!hasPermission(suid, user, userCompany, company, isAdmin)) {
                    response.sendRedirect(response.encodeRedirectURL(
                            "Contracts.jsp?u_id="
                                    + request.getParameter("u_id")
                                    + "&errorMsg=Sie sind nicht befugt, diese Aktion auszufuehren. Das darf nur ein Administrator."));
                    db.close();
                    return;
                }

                if (request.getParameter("contractId").equals("new")) {
                    final StringBuffer sqlQuery = new StringBuffer(
                            "INSERT INTO tt_contracts (id, u_id, from_date, to_date, whow, ydoh) VALUES(");
                    // todo to_date()-function
                    sqlQuery.append((db.getMaxId("tt_contracts") + 1) + ", ");
                    sqlQuery.append(user + ", ");
                    sqlQuery.append(v_from_date + ", ");
                    sqlQuery.append(v_to_date + ", ");
                    sqlQuery.append(v_whow + ", ");
                    sqlQuery.append(v_ydoh + ")");

                    db.executeUpdate(sqlQuery.toString());
                } else {
                    final StringBuffer sqlQuery = new StringBuffer("UPDATE tt_contracts SET ");
                    sqlQuery.append("from_date = " + v_from_date);
                    sqlQuery.append(", to_date = " + v_to_date);
                    sqlQuery.append(", whow = " + v_whow);
                    sqlQuery.append(", ydoh = " + v_ydoh);
                    sqlQuery.append(" WHERE id = " + request.getParameter("contractId"));

                    db.executeUpdate(sqlQuery.toString());
                }
            } catch (SQLException e) {
                response.sendRedirect(response.encodeRedirectURL(
                        "Contracts.jsp?u_id="
                                + request.getParameter("u_id")
                                + "&errorMsg=Folgender Fehler ist aufgetreten: "
                                + e.getMessage()));
                db.close();
                return;
            }
        }
        db.close();
        response.sendRedirect(response.encodeRedirectURL("Contracts.jsp?u_id=" + request.getParameter("u_id")));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   suid         DOCUMENT ME!
     * @param   user         DOCUMENT ME!
     * @param   userCompany  DOCUMENT ME!
     * @param   company      DOCUMENT ME!
     * @param   isAdmin      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean hasPermission(final String suid,
            final String user,
            final String userCompany,
            final String company,
            final boolean isAdmin) {
        return (suid.equals(user.trim()) || (isAdmin && userCompany.equalsIgnoreCase(company)));
    }
}
