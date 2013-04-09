/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cismet.web.timetracker.types;

import java.util.Iterator;
import java.util.Vector;

/**
 *
 * @author therter
 */
public class TimesheetSet {
    private Vector<TimesheetEntry> entries = new Vector<TimesheetEntry>();
    private int index = 0;
    
    public void add(TimesheetEntry entry) {
        entries.add(entry);
    }
    
    public TimesheetEntry next() {
        if ( index < entries.size() ) {
            return entries.get(index++);
        } else {
            ++index;
            return null;
        }

    }
    
    public void previous() {
        --index;
    }
    
    public void setBegin() {
        index = 0;
    }
}
