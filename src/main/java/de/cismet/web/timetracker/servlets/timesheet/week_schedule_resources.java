package de.cismet.web.timetracker.servlets.timesheet;

import de.cismet.web.timetracker.views.AbstractView;
import de.cismet.web.timetracker.Database;
import java.io.*;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author Thorsten
 * @version
 */
public class week_schedule_resources extends HttpServlet {
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
        String resource = request.getParameter("resource");
        String viewClass  = request.getParameter("view");
        String user = request.getParameter("u_id");
        final String PACKAGE = "de.cismet.web.timetracker.views.";
        Database db = new Database(application.getRealPath("/").replace('\\','/'));
        
        try {
            
            StringBuffer sqlQuery = null;
            String tagname = null;
            Vector list = null;
            
            //Instanz der geforderten View erzeugen
            Class c = Class.forName(PACKAGE + viewClass);
            Constructor<AbstractView> constructor = c.getConstructor(Database.class);//newInstance();
            AbstractView view = constructor.newInstance(db);
            
            if (resource != null && resource.equals("actions")) {
                tagname = "action";
                list = view.getActions();
            } else if(resource != null && resource.equals("timeIntervals")) {
                int u_id;
                try{
                    u_id = Integer.parseInt(user);
                }catch(NumberFormatException e){
                    e.printStackTrace();
                    u_id = 1;
                }
                tagname = "timeIntervals";
                list = view.getTimeIntervals(u_id);
            } else if(resource != null && resource.equals("projects")) {
                tagname = "projects";
                list = view.getProjectNames();
            }
            
            
            if (list != null && tagname != null) {
                Iterator it = list.iterator();
                while(it.hasNext()){
                    String send = "<" + tagname + ">" + (String)it.next() + "</" + tagname + ">";
//				System.out.println(send);
                    out.println(send);
                }
            }
        } catch(Exception e) {
            System.err.println(e.getMessage());
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
