package de.cismet.web.timetracker.types;

/**
 *	speichert die Urlaubstage des aktuellen Jahres, den Resturlaub des aktuellen Jahres, den Resturlaub
 * 	des letzten Jahres und die Anzahl der Urlaubstage pro Jahr
 */
public class HolidayStruct{
	private double holidaysThisYear;
	private double residualLeaveFromThisYear;
	private double residualLeaveFromLastYear;
	private double holidayPerYear;


	public HolidayStruct(){
		this.holidaysThisYear = 0.0;
		this.residualLeaveFromLastYear = 0.0;
		this.holidayPerYear = 0.0;
		this.residualLeaveFromThisYear = 0.0;	
	}
	
	public double getHolidaysThisYear(){
		return holidaysThisYear;		
	}
	
	
	public void setHolidaysThisYear(double holidaysThisYear){
		this.holidaysThisYear = holidaysThisYear;		
	}

	public double getResidualLeaveFromLastYear(){
		return residualLeaveFromLastYear;		
	}
	
	
	public void setResidualLeaveFromLastYear(double residualLeaveFromLastYear){
		this.residualLeaveFromLastYear = residualLeaveFromLastYear;		
	}


	public double getHolidayPerYear(){
		return holidayPerYear;		
	}
	
	
	public void setHolidayPerYear(double holidayPerYear){
		this.holidayPerYear = holidayPerYear;		
	}


	public double getResidualLeaveFromThisYear(){
		return residualLeaveFromThisYear;		
	}
	
	
	public void setResidualLeaveFromThisYear(double residualLeaveFromThisYear){
		this.residualLeaveFromThisYear = residualLeaveFromThisYear;		
	}
	

}
