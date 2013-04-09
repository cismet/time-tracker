/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.web.timetracker;

import java.sql.*;

import java.util.*;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class ControlElements {

    //~ Instance fields --------------------------------------------------------

    private Database db;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ControlElements object.
     *
     * @param  db  DOCUMENT ME!
     */
    public ControlElements(final Database db) {
        this.db = db;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   sProject  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getProjectCombo(final String sProject) {
        return getList(sProject, "SELECT id, title FROM tt_projects ORDER BY id", "mp_id", 1, "mpId", true, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   sUser  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getUserCombo(final String sUser) {
        return getList(sUser, "SELECT id, name FROM tt_user ORDER BY id", "u_id", 1, "uId", false, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   sUser    DOCUMENT ME!
     * @param   options  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getUserCombo(final String sUser, final String options) {
        return getList(sUser, "SELECT id, name FROM tt_user ORDER BY id", "u_id", 1, "uId", false, options);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   sUser  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getUserList(final String sUser) {
        return getList(sUser, "SELECT id, name FROM tt_user ORDER BY id", "u_id", 9, "uId", false, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   sProject  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getProjectList(final String sProject) {
        return getList(sProject, "SELECT id, title FROM tt_projects ORDER BY id", "p_id", 15, "pId", false, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   filename  DOCUMENT ME!
     * @param   options   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getViewCombo(final String filename, final String options) {
        String selected;
        final StringBuffer table = new StringBuffer();
        final Config conf = new Config(filename);
        final Hashtable h = conf.getAllViews();
        final Enumeration keys = h.keys();
        boolean hasElements = false;

        table.append("<select name=\"confSelect\"" + " id=\"confId\"" + " size=\"0\" " + options + ">\n");

        while (keys.hasMoreElements()) {
            hasElements = true;
            final Object viewName = keys.nextElement();
            final String viewClass = (String)h.get(viewName);
            selected = ((String)h.get(viewName)).equals("BasicView") ? "selected " : "";

            table.append("<option " + selected + "value=\"" + viewClass + "\">" + viewName + "</option>\n");
        }
        table.append("</select>");

        if (hasElements) {
            return table.toString();
        } else {
            return "Keine Views gefunden";
        }
    }

    /**
     * liefert eine Liste. Die Eintr�ge der Liste stammen aus der �bergebenen SQL-Anweisung
     *
     * @param   sProject  DOCUMENT ME!
     * @param   sqlQuery  DOCUMENT ME!
     * @param   name      DOCUMENT ME!
     * @param   size      DOCUMENT ME!
     * @param   id        DOCUMENT ME!
     * @param   withNull  DOCUMENT ME!
     * @param   options   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getList(final String sProject,
            final String sqlQuery,
            final String name,
            final int size,
            final String id,
            final boolean withNull,
            String options) {
        final ResultSet rSet;
        final StringBuffer table = new StringBuffer();

        try {
            if (db.isConnectionOk()) {
                if (options == null) {
                    options = " ";
                }

                table.append("<select name=\"" + name + "\"" + " id=\"" + id + "\"" + " size=\"" + size + "\" "
                            + options + ">\n");

                rSet = db.execute(sqlQuery);

                if (withNull) {
                    if ((sProject != null) && sProject.equals("")) {
                        table.append("<option selected></option>\n");
                    } else {
                        table.append("<option></option>\n");
                    }
                }

                while (rSet.next()) {
                    final String currentUser;
                    currentUser = rSet.getString(1);

                    if (currentUser.equals(sProject)) {
                        table.append("<option selected value=\"" + currentUser + "\">" + rSet.getString(2)
                                    + "</option>\n");
                    } else {
                        table.append("<option value=\"" + currentUser + "\">" + rSet.getString(2) + "</option>\n");
                    }
                }

                table.append("</select>\n");
            }
        } catch (SQLException se) {
            table.append("Datenbankfehler");
            se.printStackTrace();
        }
        return table.toString();
    }

    /**
     * liefert versteckte Inputfelder mit den Aktionsnamen.
     *
     * @return  DOCUMENT ME!
     */
    public String getActionNamesHidden() {
        final ResultSet rSet;
        final StringBuffer table = new StringBuffer();

        try {
            if (db.isConnectionOk()) {
                rSet = db.execute(
                        "SELECT coalesce(description, actionname) AS title FROM tt_timesheet_action ORDER BY title");

                while (rSet.next()) {
                    table.append("<input name=\"HiddenActionData\" maxlength=\"100\" type=\"hidden\"");
                    table.append("value=\"" + rSet.getString(1) + "\"/>\n");
                }
            }
        } catch (SQLException se) {
            table.append("Datenbankfehler");
            se.printStackTrace();
        }
        return table.toString();
    }

    /**
     * liefert versteckte Inputfelder mit den Projektnamen.
     *
     * @return  DOCUMENT ME!
     */
    public String getProjectTitleHidden() {
        final ResultSet rSet;
        final StringBuffer table = new StringBuffer();

        try {
            if (db.isConnectionOk()) {
                rSet = db.execute("SELECT title FROM tt_projects ORDER BY title");

                while (rSet.next()) {
                    table.append("<input name=\"HiddenProjectData\" maxlength=\"100\" type=\"hidden\"");
                    table.append("value=\"" + rSet.getString(1) + "\"/>\n");
                }
            }
        } catch (SQLException se) {
            table.append("Datenbankfehler");
            se.printStackTrace();
        }
        return table.toString();
    }

    /**
     * liefert versteckte Inputfelder mit den Projekt-Daten.
     *
     * @return  DOCUMENT ME!
     */
    public String getProjectHiddenInputs() {
        final ResultSet rSet;
        final StringBuffer table = new StringBuffer();

        try {
            if (db.isConnectionOk()) {
                rSet = db.execute("SELECT p.id, p.title, p.issubproject, p.mainprojectid, p.active, ps.shortcut "
                                + "FROM tt_projects p LEFT OUTER JOIN tt_projectshortcuts ps ON (p.id = ps.projectid) "
                                + "ORDER BY id");

                while (rSet.next()) {
                    table.append("<input name=\"HiddenData\" maxlength=\"250\" type=\"hidden\"");
                    table.append("value=\"" + rSet.getString(1) + ";" + rSet.getString(2) + ";" + rSet.getString(3)
                                + ";" + rSet.getString(4) + ";" + rSet.getString(5) + ";" + rSet.getString(6)
                                + "\"/>\n");
                }
            }
        } catch (SQLException se) {
            table.append("Datenbankfehler");
            se.printStackTrace();
        }
        return table.toString();
    }

    /**
     * liefert versteckte Inputfelder mit den User-Daten.
     *
     * @return  DOCUMENT ME!
     */
    public String getUserHiddenInputs() {
        final ResultSet rSet;
        final StringBuffer table = new StringBuffer();

        try {
            if (db.isConnectionOk()) {
                rSet = db.execute(
                        "SELECT id, name, pass, buddyname, company, \"exactHoliday\" FROM tt_user ORDER BY id");
                final GregorianCalendar now = new GregorianCalendar();
                while (rSet.next()) {
                    final String isInNetMode = "" + db.isUserInNetMode(rSet.getInt("id"));
                    table.append("<input name=\"HiddenData\" maxlength=\"200\" type=\"hidden\"");
                    table.append("value=\"" + rSet.getString("id") + ";" + rSet.getString("name") + ";"
                                + getSecretChars(rSet.getString("pass").length()) + ";" + rSet.getString("buddyname")
                                + ";" + rSet.getString("company") + ";" + rSet.getString("exactHoliday") + ";"
                                + isInNetMode.substring(0, 1) + "\"/>\n");
                }
            }
        } catch (SQLException se) {
            table.append("Datenbankfehler");
            se.printStackTrace();
        }
        return table.toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   i  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getSecretChars(final int i) {
        String ret = "";

        for (int n = 0; n < i; ++n) {
            ret += "?";
        }
        return ret;
    }
}
