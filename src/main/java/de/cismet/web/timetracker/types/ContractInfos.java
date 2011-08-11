package de.cismet.web.timetracker.types;

import java.util.GregorianCalendar;

public class ContractInfos implements Cloneable {
    int uId;
    GregorianCalendar fromDate;
    GregorianCalendar toDate;
    double whow;

    public ContractInfos(GregorianCalendar fromDate, GregorianCalendar toDate, int id, double whow) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        uId = id;
        this.whow = whow;
    }

    public GregorianCalendar getFromDate() {
        return fromDate;
    }

    public void setFromDate(GregorianCalendar fromDate) {
        this.fromDate = fromDate;
    }

    public GregorianCalendar getToDate() {
        return toDate;
    }

    public void setToDate(GregorianCalendar toDate) {
        this.toDate = toDate;
    }

    public int getUId() {
        return uId;
    }

    public void setUId(int id) {
        uId = id;
    }

    public double getWhow() {
            return whow;
    }

    public void setWhow(double whow) {
        this.whow = whow;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
