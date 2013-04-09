package de.cismet.web.timetracker;

import java.io.File;
import java.util.*;
import org.jdom.*;
import org.jdom.input.*;


/**
 *	Liest die XML-Konfigurationsdatei aus 
 */
public class Config {
    private Hashtable allViews = new Hashtable();
    private Vector horizontalResize = new Vector();
    private Vector enableProjects = new Vector();
    private String dbUser;
    private String dbPwd;
    private String dbPath;
    private String dbDriver;

	
    public Config(String filename) {
            readConfFile(filename);
    }


    /**
     * fuellt die Membervariablen mit den Werten aus der Konfigurationsdatei
     */
    private synchronized void readConfFile(String filename) {
        try{
            SAXBuilder saxb = new SAXBuilder();
            Document doc = saxb.build(new File(filename));
            Element root = doc.getRootElement();

            List views = root.getChildren("view");
            Iterator it = views.iterator();
            while (it.hasNext()){
                try {
                    Element tmp = (Element)it.next();
                    String key = tmp.getChild("viewName").getText();
                    String value = tmp.getChild("className").getText();

                    allViews.put(key, value);

                    Element horizontalResizeElement = tmp.getChild("horizontalResize");
                    String horizontalResizeString  = null;
                    if (horizontalResizeElement != null){
                        horizontalResizeString = horizontalResizeElement.getText();
                    }
                    if (horizontalResizeString != null && horizontalResizeString.equals("true")) {
                        horizontalResize.add(value);
                    }

                    Element enableProjectsElement = tmp.getChild("enableProjects");
                    String enableProjectsString  = null;
                    if (enableProjectsElement != null){
                        enableProjectsString = enableProjectsElement.getText();
                    }
                    if (enableProjectsString != null && enableProjectsString.equals("true")) {
                        enableProjects.add(value);
                    }
                }catch(Exception e) {
                    e.printStackTrace();
                }

                Element dbSettings = root.getChild("dbSettings");

                try {
                    setDbUser(dbSettings.getChild("user").getText());
                    setDbPwd(dbSettings.getChild("pwd").getText());
                    setDbPath(dbSettings.getChild("path").getText());
                    setDbDriver(dbSettings.getChild("driver").getText());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
	
	
    public static void main(String[] args) {
        Config c = new Config("config.xml");
        Hashtable h = c.getAllViews();
        Enumeration keys = h.keys();

        while(keys.hasMoreElements()){
            System.out.println((String)h.get(keys.nextElement()));
        }
    }

    /**
     * liefert alle in der Konfigurationsdatei eingestellten Views in einer Hashtable.
     * View-Name = key und View-Klassenname = value
     * @return Hashtable mit allen in der Konfigurationsdatei eingestellten Views
     */
    public Hashtable getAllViews() {
        return allViews;
    }

    /**
     * liefert einen Vektor, in dem alle Klassen enthalten sind, bei denen die horizontale
     * Groessenaenderung erlaubt ist.
     * @return Vektor mit den Klassennamen, bei denen die horizontale Groessenaenderung erlaubt ist
     */
    public Vector getHorizontalResize() {
        return horizontalResize;
    }

    /**
     * liefert einen Vektor, in dem alle Klassen enthalten sind, bei denen die Anzeige von Projekten
     * erlaubt ist.
     * @return Vektor mit den Klassennamen, bei denen die die Anzeige von Projekten erlaubt ist 
     */
    public Vector getEnableProjects() {
        return enableProjects;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public String getDbPwd() {
        return dbPwd;
    }

    public void setDbPwd(String dbPwd) {
        this.dbPwd = dbPwd;
    }

    public String getDbPath() {
        return dbPath;
    }

    public void setDbPath(String dbPath) {
        this.dbPath = dbPath;
    }

    public String getDbDriver() {
        return dbDriver;
    }

    public void setDbDriver(String dbDriver) {
        this.dbDriver = dbDriver;
    }

}
