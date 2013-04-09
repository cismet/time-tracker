/*
 * netModusAction.java
 *
 * Created on 17. Juli 2008, 13:23
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package de.cismet.web.timetracker.types;

import java.util.GregorianCalendar;

/**
 *
 * @author therter
 */
public class NetModusAction {
    public static final int START = 0;
    public static final int END = 1;
    private int modus;
    private GregorianCalendar time;
    
    /** Creates a new instance of netModusAction */
    public NetModusAction() {
    }

    public int getModus() {
        return modus;
    }

    public void setModus(int modus) {
        this.modus = modus;
    }

    public GregorianCalendar getTime() {
        return time;
    }

    public void setTime(GregorianCalendar time) {
        this.time = time;
    }
    
}
