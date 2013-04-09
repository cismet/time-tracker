/*
 * ProjectShortcuts.java
 *
 * Created on 12. Juli 2007, 14:20
 */

package de.cismet.web.timetracker.servlets;

import de.cismet.web.timetracker.Database;
import java.io.IOException;
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
public class ProjectShortcuts extends HttpServlet {
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
        String project = request.getParameter("p_id");
        if ( !((String)request.getSession().getAttribute("role")).equals("admin") ){
            response.sendRedirect( response.encodeRedirectURL("ProjectShortcuts.jsp?u_id=" + request.getParameter("u_id") + "&p_id="+ project + "&errorMsg=Zugriff verweigert. Diese Seite ist Administratoren vorbehalten." ) );
            return;
        }

        Database db = new Database(application.getRealPath("/").replace('\\','/'));
        if(!db.isConnectionOk()){
            response.sendRedirect( response.encodeRedirectURL("ProjectShortcuts.jsp?u_id=" + request.getParameter("u_id") + "&p_id="+ project + "&errorMsg=Fehler beim Verbinden mit der Datenbank: " + db.getErrorMessage()) );
            db.close();
            return;
        }

        //modified or new data received
        try {
            String shortcutId = request.getParameter("shortcutId");
            if(request.getParameter("act") != null && (request.getParameter("act").equals("uebernehmen") || request.getParameter("act").equals("delete")) && shortcutId != null) {
                String shortcut = request.getParameter("name") ;
                shortcut = (shortcut == null ? "null" : "'" + shortcut + "'");

                if(request.getParameter("act").equals("delete")) {			//loeschen
                    StringBuffer sqlQuery = new StringBuffer("DELETE FROM tt_projectshortcuts WHERE id = " + shortcutId);

                    db.executeUpdate(sqlQuery.toString());
                } else if(shortcutId.equals("new")) {		//hinzufuegen
                    StringBuffer sqlQuery = new StringBuffer("INSERT INTO tt_projectshortcuts (id, projectid, shortcut) VALUES(");
                    sqlQuery.append((db.getMaxId("tt_projectshortcuts") + 1) + ", ");
                    sqlQuery.append(project + ", ");
                    sqlQuery.append(shortcut + ")");

                    db.executeUpdate(sqlQuery.toString());
                } else if(shortcutId != null) {				//aendern
                    StringBuffer sqlQuery = new StringBuffer("UPDATE tt_projectshortcuts SET ");
                    sqlQuery.append("shortcut = " + shortcut);
                    sqlQuery.append(" WHERE id = " + shortcutId);

                    db.executeUpdate(sqlQuery.toString());
                }
            }
        } catch(SQLException e) {
            response.sendRedirect( response.encodeRedirectURL("ProjectShortcuts.jsp?u_id=" + request.getParameter("u_id") + "&p_id="+ project + "&errorMsg=Folgender Fehler ist aufgetreten: " + e.getMessage()) );
            db.close();
            return;
        }
        response.sendRedirect( response.encodeRedirectURL("ProjectShortcuts.jsp?u_id=" + request.getParameter("u_id") + "&p_id="+ project) );
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
