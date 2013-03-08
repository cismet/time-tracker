package de.cismet.web.timetracker.servlets.timesheet;

import de.cismet.web.timetracker.views.AbstractView;
import de.cismet.web.timetracker.Database;
import java.io.*;
import java.lang.reflect.Constructor;
import java.sql.ResultSet;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author Thorsten
 * @version
 */
public class week_schedule_delete extends HttpServlet {
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
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Database db = new Database(application.getRealPath("/").replace('\\','/'));

	try{
            final String PACKAGE = "de.cismet.web.timetracker.views.";
            String id = request.getParameter("eventToDeleteId");
            String viewClass = request.getParameter("view");

            if (request.getParameter("eventToDeleteId") != null){
                //Auf Rechte pruefen
                String suid = (String)(request.getSession().getAttribute("id"));
                int uid = 0;
                String company = "";


                String sqlString = "SELECT u_id FROM tt_timesheet WHERE oid = " + id;
                ResultSet rSet = db.execute(sqlString);
                if(rSet != null && rSet.next()){
                    uid = rSet.getInt(1);
                }else{
                    out.println("-1");
                    return ;
                }

                sqlString = "SELECT company FROM tt_user WHERE id = " + uid;
                rSet = db.execute(sqlString);
                if(rSet != null && rSet.next()){
                    company = rSet.getString(1);
                }else{
                    company = "";
                }


                if (suid.equals("" + uid) ||
                    (request.getSession().getAttribute("role").equals("admin") && ((String)request.getSession().getAttribute("company")).equalsIgnoreCase(company)) ){

                    //Instanz der geforderten View erzeugen
                    Class c = Class.forName(PACKAGE + viewClass);
                    Constructor<AbstractView> constructor = c.getConstructor(Database.class);//newInstance();
                    AbstractView view = constructor.newInstance(db);

                    if(view.deleteItem(id)){
                        out.println("OK");
                    }
                }else{
                    // die benoetigten rechte fehlen
                    out.println("NO_RIGHTS");
                    return ;
                }

            }
	}catch(Exception e){
            e.printStackTrace();
	}
        db.close();
        out.close();
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
