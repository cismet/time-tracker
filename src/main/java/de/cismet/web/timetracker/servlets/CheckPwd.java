/*
 * CheckPwd.java
 *
 * Created on 6. Juli 2007, 15:39
 */
package de.cismet.web.timetracker.servlets;

import de.cismet.web.timetracker.Database;
import de.cismet.web.timetracker.TimeTrackerFunctions;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 *
 * @author Thorsten
 * @version
 */
public class CheckPwd extends HttpServlet {
    ServletContext application;

    /**
     * Initializes the servlet.
     */
    @Override
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
        final int TIME_TILL_EXPIRE = 60 * 60 * 24 * 20;
        String lastPage = (String) request.getSession().getAttribute("lastPage");

        Database db = new Database(application.getRealPath("/").replace('\\', '/'));

        if (!db.isConnectionOk()) {
            response.sendRedirect(response.encodeRedirectURL("Login.jsp?errorMsg=Fehler beim Verbinden mit der Datenbank."));
            return;
        }

        try {
            String sqlString = "SELECT admin, name, id, company FROM tt_user WHERE pass = '" + TimeTrackerFunctions.calcSHA1(request.getParameter("password")) + "' AND (name = '" + request.getParameter("username") + "' OR buddyname = '" + request.getParameter("username") + "')";
            ResultSet login = db.execute(sqlString);

            if (login != null && login.next()) {
                request.getSession().setMaxInactiveInterval(TIME_TILL_EXPIRE);

                //persistentes Cookie anlegen
                Cookie sessionCookie = new Cookie("JSESSIONID", request.getSession().getId());
                sessionCookie.setMaxAge(TIME_TILL_EXPIRE);
                sessionCookie.setPath(request.getContextPath());
                response.addCookie(sessionCookie);

                request.getSession().setAttribute("username", login.getString("name"));
                request.getSession().setAttribute("role", login.getBoolean("admin") ? "admin" : "user");
                request.getSession().setAttribute("id", login.getString("id"));
                request.getSession().setAttribute("company", login.getString("company"));

                if (lastPage == null) {
                    lastPage = "Stammdaten.jsp";
                }

                response.sendRedirect(response.encodeRedirectURL(lastPage + "?u_id=" + login.getString(3)));
                login.close();
            } else {
                response.sendRedirect(response.encodeRedirectURL("Login.jsp?errorMsg=Ungültiges Benutzernamen/Passwort-Paar"));
            }
        } catch (SQLException e) {
            response.sendRedirect(response.encodeRedirectURL("Login.jsp?errorMsg=" + e.getMessage()));
        } catch (NoSuchAlgorithmException e) {
            response.sendRedirect(response.encodeRedirectURL("Login.jsp?errorMsg=" + e.getMessage()));
        }
        db.close();
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
