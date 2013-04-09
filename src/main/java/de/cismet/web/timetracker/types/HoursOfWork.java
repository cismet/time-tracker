package de.cismet.web.timetracker.types;


public class HoursOfWork{
	boolean autoPause;
	double hours;
	boolean needAutoPause;
	
	public HoursOfWork(){
		autoPause = false;
		hours = 0.0;	
	}


	public HoursOfWork(boolean autoPause){
		this.autoPause = autoPause;
		this.hours = 0.0;	
	}
	
	public double getHours(){
		return hours;		
	}
	
	
	public boolean getAutoPause(){
		return autoPause;		
	}
	
	public void setHours(double hours){
		this.hours = hours;		
	}

	
	public void setAutoPause(boolean autoPause){
		this.autoPause = autoPause;		
	}


	public boolean isNeedAutoPause() {
		return needAutoPause;
	}


	public void setNeedAutoPause(boolean needAutoPause) {
		this.needAutoPause = needAutoPause;
	}
	
}