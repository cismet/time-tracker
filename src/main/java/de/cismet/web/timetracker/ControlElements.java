package de.cismet.web.timetracker;

import java.sql.*;
import java.util.*;

public class ControlElements {

    private Database db;

    public ControlElements(Database db) {
        this.db = db;
    }

    public String getProjectCombo(String sProject) {
        return getList(sProject, "SELECT id, title FROM tt_projects ORDER BY id", "mp_id", 1, "mpId", true, null);
    }

    public String getUserCombo(String sUser) {
        return getList(sUser, "SELECT id, name FROM tt_user ORDER BY id", "u_id", 1, "uId", false, null);
    }

    public String getUserCombo(String sUser, String options) {
        return getList(sUser, "SELECT id, name FROM tt_user ORDER BY id", "u_id", 1, "uId", false, options);
    }

    public String getUserList(String sUser) {
        return getList(sUser, "SELECT id, name FROM tt_user ORDER BY id", "u_id", 9, "uId", false, null);
    }

    public String getProjectList(String sProject) {
        return getList(sProject, "SELECT id, title FROM tt_projects ORDER BY id", "p_id", 15, "pId", false, null);
    }

    public String getViewCombo(String filename, String options) {
        String selected;
        StringBuffer table = new StringBuffer();
        Config conf = new Config(filename);
        Hashtable h = conf.getAllViews();
        Enumeration keys = h.keys();
        boolean hasElements = false;

        table.append("<select name=\"confSelect\"" + " id=\"confId\"" + " size=\"0\" " + options + ">\n");

        while (keys.hasMoreElements()) {
            hasElements = true;
            Object viewName = keys.nextElement();
            String viewClass = (String) h.get(viewName);
            selected = ((String) h.get(viewName)).equals("BasicView") ? "selected " : "";

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
     */
    private String getList(String sProject, String sqlQuery, String name, int size, String id, boolean withNull, String options) {
        ResultSet rSet;
        StringBuffer table = new StringBuffer();

        try {
            if (db.isConnectionOk()) {
                if (options == null) {
                    options = " ";
                }

                table.append("<select name=\"" + name + "\"" + " id=\"" + id + "\"" + " size=\"" + size + "\" " + options + ">\n");

                rSet = db.execute(sqlQuery);

                if (withNull) {
                    if (sProject != null && sProject.equals("")) {
                        table.append("<option selected></option>\n");
                    } else {
                        table.append("<option></option>\n");
                    }
                }


                while (rSet.next()) {
                    String currentUser;
                    currentUser = rSet.getString(1);

                    if (currentUser.equals(sProject)) {
                        table.append("<option selected value=\"" + currentUser + "\">" + rSet.getString(2) + "</option>\n");
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
     *liefert versteckte Inputfelder mit den Aktionsnamen
     */
    public String getActionNamesHidden() {
        ResultSet rSet;
        StringBuffer table = new StringBuffer();

        try {
            if (db.isConnectionOk()) {

                rSet = db.execute("SELECT coalesce(description, actionname) AS title FROM tt_timesheet_action ORDER BY title");

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
     *liefert versteckte Inputfelder mit den Projektnamen
     */
    public String getProjectTitleHidden() {
        ResultSet rSet;
        StringBuffer table = new StringBuffer();

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
     *liefert versteckte Inputfelder mit den Projekt-Daten
     */
    public String getProjectHiddenInputs() {
        ResultSet rSet;
        StringBuffer table = new StringBuffer();

        try {
            if (db.isConnectionOk()) {

                rSet = db.execute("SELECT p.id, p.title, p.issubproject, p.mainprojectid, p.active, ps.shortcut " +
                        "FROM tt_projects p LEFT OUTER JOIN tt_projectshortcuts ps ON (p.id = ps.projectid) " +
                        "ORDER BY id");

                while (rSet.next()) {
                    table.append("<input name=\"HiddenData\" maxlength=\"250\" type=\"hidden\"");
                    table.append("value=\"" + rSet.getString(1) + ";" + rSet.getString(2) + ";" + rSet.getString(3) + ";" + rSet.getString(4) + ";" + rSet.getString(5) + ";" + rSet.getString(6) + "\"/>\n");
                }
            }
        } catch (SQLException se) {
            table.append("Datenbankfehler");
            se.printStackTrace();
        }
        return table.toString();
    }

    /**
     *liefert versteckte Inputfelder mit den User-Daten
     */
    public String getUserHiddenInputs() {
        ResultSet rSet;
        StringBuffer table = new StringBuffer();

        try {
            if (db.isConnectionOk()) {
                rSet = db.execute("SELECT id, name, pass, buddyname, company, \"exactHoliday\" FROM tt_user ORDER BY id");
                GregorianCalendar now = new GregorianCalendar();
                while (rSet.next()) {
                    String isInNetMode = "" + db.isUserInNetMode(rSet.getInt("id"));
                    table.append("<input name=\"HiddenData\" maxlength=\"200\" type=\"hidden\"");
                    table.append("value=\"" + rSet.getString("id") + ";" + rSet.getString("name") + ";" + getSecretChars(rSet.getString("pass").length()) + ";" + rSet.getString("buddyname") + ";" + rSet.getString("company") + ";" + rSet.getString("exactHoliday") + ";" + isInNetMode.substring(0, 1) + "\"/>\n");
                }
            }
        } catch (SQLException se) {
            table.append("Datenbankfehler");
            se.printStackTrace();
        }
        return table.toString();
    }

    private String getSecretChars(int i) {
        String ret = "";

        for (int n = 0; n < i; ++n) {
            ret += "?";
        }
        return ret;
    }
}
