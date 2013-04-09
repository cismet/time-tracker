package de.cismet.web.timetracker;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

public class CalendarItem {
	public static final int PROJECT_NOT_DEFINED = -1;
	private String id;
	private String annotation;
	private String project;
	private String action;
	private GregorianCalendar start;
	private GregorianCalendar end;
	
	
	public CalendarItem(){
		
	}
	
	
	public CalendarItem(String action, String annotation, GregorianCalendar end, String id, String project, GregorianCalendar start) {
		this.action = action;
		this.annotation = annotation;
		this.end = end;
		this.id = id;
		this.project = project;
		this.start = start;
	}

	
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getAnnotation() {
		return annotation;
	}
	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}
	public GregorianCalendar getEnd() {
		return end;
	}
	public void setEnd(GregorianCalendar end) {
		this.end = end;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getProject() {
		return project;
	}
	public void setProject(String project) {
		this.project = project;
	}
	public GregorianCalendar getStart() {
		return start;
	}
	public void setStart(GregorianCalendar start) {
		this.start = start;
	}
	
	
	/**
	 * 
	 * @return die Daten des CalendarItem im XML-Format
	 */
	public String toString(){
		SimpleDateFormat formater = new SimpleDateFormat("E, dd MMM yyyy HH:mm", Locale.US);
		StringBuffer xmlResult = new StringBuffer();
		xmlResult.append("<item>\n");
		xmlResult.append("<id>" + id + "</id>\n");
		xmlResult.append("<annotation>" + annotation + "</annotation>\n");
		xmlResult.append("<eventStartDate>" + formater.format( start.getTime())  + " GMT+2</eventStartDate>\n");
		xmlResult.append("<eventEndDate>" + formater.format( end.getTime() ) + " GMT+2</eventEndDate>\n");
		xmlResult.append("<action>" + action + "</action>\n");
		if (project != null){
			xmlResult.append("<project>" + project + "</project>\n");
		}
		xmlResult.append("</item>\n");
		
		return xmlResult.toString();
	}


}
