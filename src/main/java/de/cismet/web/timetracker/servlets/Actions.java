/*
 * Action.java
 *
 * Created on 6. Juli 2007, 15:25
 */

package de.cismet.web.timetracker.servlets;

import de.cismet.web.timetracker.Database;
import java.io.*;
import java.sql.SQLException;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author Thorsten
 * @version
 */
public class Actions extends HttpServlet {
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

        if(!db.isConnectionOk()){
            response.sendRedirect( response.encodeRedirectURL("Actions.jsp?u_id=" + request.getParameter("u_id") + "&errorMsg=Fehler beim Verbinden mit der Datenbank: " + db.getErrorMessage()) );
            return;
        }

        if(request.getParameter("actionId") != null) {
            String actionId = request.getParameter("actionId");
            String name = request.getParameter("name") ;
            String desc = request.getParameter("desc") ;

            name = name == null ? "null" : "'" + name + "'";
            desc = desc == null || desc.equals("") ? "null" : "'" + desc + "'";
            
            if(actionId.equals("new")) {
                //neue Daten hinzufuegen
                try {
                    StringBuffer sqlQuery = new StringBuffer("INSERT INTO tt_timesheet_action (id, actionname, description) VALUES(");
                    sqlQuery.append((db.getMaxId("tt_timesheet_action") + 1) + ", ");
                    sqlQuery.append(name + ", ");
                    sqlQuery.append(desc + ")");
                    db.executeUpdate(sqlQuery.toString());
                } catch(SQLException e) {
                    response.sendRedirect( response.encodeRedirectURL("Actions.jsp?u_id=" + request.getParameter("u_id") + "&errorMsg=" + e.getMessage()) );
                    db.close();
                    return;
                }
                
            } else if(actionId != null) {
                //Daten aendern
                StringBuffer sqlQuery = new StringBuffer("UPDATE tt_timesheet_action SET ");
                sqlQuery.append("actionname = " + name);
                sqlQuery.append(", description = " + desc);
                sqlQuery.append(" WHERE id = " + actionId);
                
                try {
                    db.executeUpdate(sqlQuery.toString());
                } catch(SQLException e) {
                    response.sendRedirect( response.encodeRedirectURL("Actions.jsp?u_id=" + request.getParameter("u_id") + "&errorMsg=" + e.getMessage()) );
                    db.close();
                    return;
                }
            }
        }
       db.close();
       response.sendRedirect( response.encodeRedirectURL( "Actions.jsp" ) );
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
