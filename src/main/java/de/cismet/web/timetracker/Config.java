/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.web.timetracker;

import org.jdom.*;
import org.jdom.input.*;

import java.io.File;

import java.util.*;

/**
 * Liest die XML-Konfigurationsdatei aus.
 *
 * @version  $Revision$, $Date$
 */
public class Config {

    //~ Instance fields --------------------------------------------------------

    private Hashtable allViews = new Hashtable();
    private Vector horizontalResize = new Vector();
    private Vector enableProjects = new Vector();
    private String dbUser;
    private String dbPwd;
    private String dbPath;
    private String dbDriver;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Config object.
     *
     * @param  filename  DOCUMENT ME!
     */
    public Config(final String filename) {
        readConfFile(filename);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * fuellt die Membervariablen mit den Werten aus der Konfigurationsdatei.
     *
     * @param  filename  DOCUMENT ME!
     */
    private synchronized void readConfFile(final String filename) {
        try {
            final SAXBuilder saxb = new SAXBuilder();
            final Document doc = saxb.build(new File(filename));
            final Element root = doc.getRootElement();

            final List views = root.getChildren("view");
            final Iterator it = views.iterator();
            while (it.hasNext()) {
                try {
                    final Element tmp = (Element)it.next();
                    final String key = tmp.getChild("viewName").getText();
                    final String value = tmp.getChild("className").getText();

                    allViews.put(key, value);

                    final Element horizontalResizeElement = tmp.getChild("horizontalResize");
                    String horizontalResizeString = null;
                    if (horizontalResizeElement != null) {
                        horizontalResizeString = horizontalResizeElement.getText();
                    }
                    if ((horizontalResizeString != null) && horizontalResizeString.equals("true")) {
                        horizontalResize.add(value);
                    }

                    final Element enableProjectsElement = tmp.getChild("enableProjects");
                    String enableProjectsString = null;
                    if (enableProjectsElement != null) {
                        enableProjectsString = enableProjectsElement.getText();
                    }
                    if ((enableProjectsString != null) && enableProjectsString.equals("true")) {
                        enableProjects.add(value);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                final Element dbSettings = root.getChild("dbSettings");

                try {
                    setDbUser(dbSettings.getChild("user").getText());
                    setDbPwd(dbSettings.getChild("pwd").getText());
                    setDbPath(dbSettings.getChild("path").getText());
                    setDbDriver(dbSettings.getChild("driver").getText());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        final Config c = new Config("config.xml");
        final Hashtable h = c.getAllViews();
        final Enumeration keys = h.keys();

        while (keys.hasMoreElements()) {
            System.out.println((String)h.get(keys.nextElement()));
        }
    }

    /**
     * liefert alle in der Konfigurationsdatei eingestellten Views in einer Hashtable. View-Name = key und
     * View-Klassenname = value
     *
     * @return  Hashtable mit allen in der Konfigurationsdatei eingestellten Views
     */
    public Hashtable getAllViews() {
        return allViews;
    }

    /**
     * liefert einen Vektor, in dem alle Klassen enthalten sind, bei denen die horizontale Groessenaenderung erlaubt
     * ist.
     *
     * @return  Vektor mit den Klassennamen, bei denen die horizontale Groessenaenderung erlaubt ist
     */
    public Vector getHorizontalResize() {
        return horizontalResize;
    }

    /**
     * liefert einen Vektor, in dem alle Klassen enthalten sind, bei denen die Anzeige von Projekten erlaubt ist.
     *
     * @return  Vektor mit den Klassennamen, bei denen die die Anzeige von Projekten erlaubt ist
     */
    public Vector getEnableProjects() {
        return enableProjects;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getDbUser() {
        return dbUser;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  dbUser  DOCUMENT ME!
     */
    public void setDbUser(final String dbUser) {
        this.dbUser = dbUser;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getDbPwd() {
        return dbPwd;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  dbPwd  DOCUMENT ME!
     */
    public void setDbPwd(final String dbPwd) {
        this.dbPwd = dbPwd;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getDbPath() {
        return dbPath;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  dbPath  DOCUMENT ME!
     */
    public void setDbPath(final String dbPath) {
        this.dbPath = dbPath;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getDbDriver() {
        return dbDriver;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  dbDriver  DOCUMENT ME!
     */
    public void setDbDriver(final String dbDriver) {
        this.dbDriver = dbDriver;
    }
}
