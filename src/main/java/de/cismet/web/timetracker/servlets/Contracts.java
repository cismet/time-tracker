/*
 * Contracts.java
 *
 * Created on 6. Juli 2007, 15:58
 */

package de.cismet.web.timetracker.servlets;

import de.cismet.web.timetracker.Database;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 *
 * @author Thorsten
 * @version
 */
public class Contracts extends HttpServlet {
    ServletContext application;

    /**
     * Initializes the servlet.
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        application = config.getServletContext();
    }

    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        Database db = new Database(application.getRealPath("/").replace('\\','/'));

        if(!db.isConnectionOk()) {
            response.sendRedirect( response.encodeRedirectURL("Contracts.jsp?u_id=" + request.getParameter("u_id") + "&errorMsg=Fehler beim Verbinden mit der Datenbank: " + db.getErrorMessage()) );
            db.close();
            return;
        }

        String contractId = request.getParameter("contractId");
        //modified or new data received
        if(contractId != null && !contractId.equals("") ) {
            try{
                String user = request.getParameter("u_id");
                String v_to_date = request.getParameter("to_date");
                String v_from_date = request.getParameter("from_date") ;
                String v_ydoh = request.getParameter("ydoh");
                String v_whow = request.getParameter("whow");
                String suid = (String)(request.getSession().getAttribute("id"));
                String company = "";

                v_to_date = v_to_date.equals("") ? "null" : "'" + v_to_date + "'";
                v_from_date = v_from_date.equals("") ? "null" : "'" + v_from_date + "'";
                v_ydoh = (v_ydoh != null && v_ydoh.equals("")) ? null : v_ydoh;
                v_whow = (v_whow != null && v_whow.equals("")) ? null : v_whow;

                //Auf Rechte pruefen
                String sqlString = "SELECT company FROM tt_user WHERE id = " + user;
                ResultSet rSet = db.execute(sqlString);
                if(rSet != null && rSet.next()){
                    company = rSet.getString(1);
                }
                String userCompany = (String)request.getSession().getAttribute("company");
                boolean isAdmin = request.getSession().getAttribute("role").equals("admin");

                if ( !hasPermission(suid, user, userCompany, company, isAdmin ) ) {
                    response.sendRedirect( response.encodeRedirectURL("Contracts.jsp?u_id=" + request.getParameter("u_id") + "&errorMsg=Sie sind nicht befugt, diese Aktion auszufuehren. Das darf nur ein Administrator." ) );
                    db.close();
                    return;
                }

                if(request.getParameter("contractId").equals("new") ) {
                    StringBuffer sqlQuery = new StringBuffer("INSERT INTO tt_contracts (id, u_id, from_date, to_date, whow, ydoh) VALUES(");
                    //todo to_date()-function
                    sqlQuery.append((db.getMaxId("tt_contracts") + 1) + ", ");
                    sqlQuery.append(user + ", ");
                    sqlQuery.append(v_from_date + ", ");
                    sqlQuery.append(v_to_date + ", ");
                    sqlQuery.append(v_whow + ", ");
                    sqlQuery.append(v_ydoh + ")");

                    db.executeUpdate(sqlQuery.toString());

                } else {
                    StringBuffer sqlQuery = new StringBuffer("UPDATE tt_contracts SET ");
                    sqlQuery.append("from_date = " + v_from_date);
                    sqlQuery.append(", to_date = " + v_to_date);
                    sqlQuery.append(", whow = " + v_whow);
                    sqlQuery.append(", ydoh = " + v_ydoh);
                    sqlQuery.append(" WHERE id = " + request.getParameter("contractId"));

                    db.executeUpdate(sqlQuery.toString());
                }
            } catch(SQLException e) {
                response.sendRedirect( response.encodeRedirectURL("Contracts.jsp?u_id=" + request.getParameter("u_id") + "&errorMsg=Folgender Fehler ist aufgetreten: " + e.getMessage() ) );
                db.close();
                return;
            }
        }
        db.close();
        response.sendRedirect( response.encodeRedirectURL("Contracts.jsp?u_id=" + request.getParameter("u_id") ) );
    }

    private boolean hasPermission(String suid, String user, String userCompany, String company, boolean isAdmin) {
        return (suid.equals(user.trim()) || (isAdmin && userCompany.equalsIgnoreCase(company)) );
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Short description";
    }
    // </editor-fold>
}
