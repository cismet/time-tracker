/*
(C) www.dhtmlgoodies.com, January 2006

This is a script from www.dhtmlgoodies.com. You will find this and a lot of other scripts at our website.	

Version:	1.0	: February 13th - 2006
1.1	: March 22nd - 2006	: Made it possible to disable inline textaarea edit(inlineTextAreaEnabled) and instead open the window from external file.

Terms of use:
You are free to use this script as long as the copyright message is kept intact. However, you may not
redistribute, sell or repost it without our permission.

Thank you!

www.dhtmlgoodies.com
Alf Magne Kalleland

*/


/* User variables */
var headerDateFormat = 'd.m';	// Format of day, month in header, i.e. at the right of days
var instantSave = true;	// Save items to server every time something has been changed (i.e. moved, resized or text changed) - NB! New items are not saved until a description has been written?
var externalSourceFile_items = 'week_schedule_getItems';	// File called by ajax when changes are loaded from the server(by Ajax).
var externalSourceFile_save = 'week_schedule_save';	// File called by ajax when changes are made to an element
var externalSourceFile_delete = 'week_schedule_delete';	// File called by ajax when an element is deleted. Input to this file is the variable "eventToDeleteId=<id>"
var externalSourceFile_Resources = 'week_schedule_resources'; 
var popupWindowUrl = false;	// Called when double clicking on an event. use false if this option is disabled.
var horizontalResize = false;
var hasProjects = false;
var historyLength = 10;
var historyStack;

var txt_deleteEvent = 'Click OK to delete this event';	// Text in dialog box - confirm before deleting an event

var appointmentMarginSize = 5;	// Margin at the left and right of appointments;
var initTopHour = 9;	// Initially auto scroll scheduler to the position of this hour
var initMinutes = 5;	// Used to auto set start time. Example: 15 = auto set start time to 0,15,30 or 45. It all depends on where the mouse is located ondragstart
var snapToMinutes = 5;	// Snap to minutes, example: 5 = allow minute 0,5,10,15,20,25,30,35,40,45,50,55

var inlineTextAreaEnabled = true;	// Edit events from inline textarea?

/* End user variables */

var horizontalOffset = 299;		//horizontal start of the calendar
var weekScheduler_container = false;
var weekScheduler_appointments = false;

var newAppointmentCounter = -1;
var moveAppointmentCounter = -1;
var resizeAppointmentCounter = -1;
var resizeAppointmentInitHeight = false;

var el_x;	// x position of element
var el_y;	// y position of element
var mouse_x;
var mouse_y;
var elWidth;

//speichert, ob die angeforderten Daten bereits angekommen sind
var actionsAreArrived = false;
var timeIntervalsAreArrived = false;
var projectsAreArrived = false;
var itemsAreArrived = false;

var user = 1;
var view = false;
var currentAppointmentDiv = false;
var currentStartTimeDiv = false;	
var currentEndTimeDiv = false;		

var appointmentsOffsetTop = false;
var appointmentsOffsetLeft = false;

var dayPositionArray = new Array();
var dayDateArray = new Array();

var weekSchedule_ajaxObjects = new Array();

var validActions = new Array();
var validProjects = new Array();

var dateStartOfWeek = false;
var newAppointmentWidth = false;

var startIdOfNewItems = 500000000;
var contentEditInProgress = false;
var toggleViewCounter = -1;
var objectToToggle = false;
var timerId = false;
var timeInterval =  new Array();

var objectToModify = false;

var appointmentProperties = new Array();	// Array holding properties of appointments/events.
var opera = navigator.userAgent.toLowerCase().indexOf('opera')>=0?true:false;

var activeEventObj;	// Reference to element currently active, i.e. with blue header;

var resizeAppointmentInitWidth;
var horizontalResizeCounter;

// speichert, ob get�tigte Anderungen schon gespeichert wurden
// siehe undo calendarUndo, getItemFromServer
var changesDone = true;



function trimString(sInString) {
    sInString = sInString.replace( /^\s+/g, "" );
    return sInString.replace( /\s+$/g, "" );
}

function editEventWindow(e,inputDiv)
{
    if(!inputDiv)inputDiv = this;
    if(!popupWindowUrl)return;
    if(inputDiv.id.indexOf('new_')>=0)return;
    
    
    
    var editEvent = window.open(popupWindowUrl + '?id=' + inputDiv.id,'editEvent','width=500,height=500,status=no');
    editEvent.focus();
}


function setElementActive(e,inputDiv)
{
    if(!inputDiv)inputDiv = this;
    var subDivs = inputDiv.getElementsByTagName('DIV');
    for(var no=0;no<subDivs.length;no++){
        if(subDivs[no].className=='weekScheduler_appointment_header'){
            subDivs[no].className = 'weekScheduler_appointment_headerActive';
        }	
    }
    
    if(activeEventObj && activeEventObj!=inputDiv){
        setElementInactive(activeEventObj);
    }
    activeEventObj = inputDiv;
}




function setElementInactive(inputDiv)
{
    var subDivs = inputDiv.getElementsByTagName('DIV');
    for(var no=0;no<subDivs.length;no++){
        if(subDivs[no].className=='weekScheduler_appointment_headerActive'){
            subDivs[no].className = 'weekScheduler_appointment_header';
        }	
    }	
    
    
}

function parseItemsFromServer(ajaxIndex)
{
    //stellt sicher, dass diese Funktion erst ausgef�hrt wird, wenn die angeforderten Daten angekommen sind
    if(!actionsAreArrived){
        setTimeout('parseItemsFromServer(' + ajaxIndex + ')' , 1000);
        return;
    }
    
    if(!timeIntervalsAreArrived){
        setTimeout('parseItemsFromServer(' + ajaxIndex + ')' , 1000);
        return;
    }
    
    if (hasProjects && !projectsAreArrived){
        setTimeout('parseItemsFromServer(' + ajaxIndex + ')' , 1000);
        return;
    }
    var itemsToBeCreated = new Array();
    var items = weekSchedule_ajaxObjects[ajaxIndex].response.split(/<item>/g);
    weekSchedule_ajaxObjects[ajaxIndex] = false;
    for(var no=1;no<items.length;no++){
        var lines = items[no].split(/\n/g);
        itemsToBeCreated[no] = new Array();
        
        for(var no2=0;no2<lines.length;no2++){
            var key = lines[no2].replace(/<([^>]+)>.*/g,'$1');
            if(key)key = trimString(key);
            var pattern = new RegExp("<\/?" + key + ">","g");
            var value = lines[no2].replace(pattern,'');
            value = trimString(value);
            if(key=='eventStartDate' || key=='eventEndDate'){
                var oldTimeOffset = parseInt(value.substr(value.length - 2, 2), 10);
                var d = new Date(value);
                value = String(d);
                var newTimeOffset = d.getTimezoneOffset();
                
                var timeInMillis = d.getTime() + (( (oldTimeOffset * 60) + newTimeOffset) * 60*1000);
                d.setTime(timeInMillis); 
                value = d;
            }		
            itemsToBeCreated[no][key] = value;
        }
        
        if(itemsToBeCreated[no]['id']){
            //Breite berechnen
            var days = itemsToBeCreated[no]['eventEndDate'].getTime() - itemsToBeCreated[no]['eventStartDate'].getTime();
            days = Math.floor(days / (1000*60*60*24)) + 1;
            var itemWidth = newAppointmentWidth * days - (appointmentMarginSize*2);
            
            //Datum darf zur Bestimmung der H�he nicht beachtet werden, da eventuell der End-Tag != Start-Tag
            //tmp-End-Tag erzeugen
            var tmpEndDate = new Date();
            tmpEndDate.setMinutes(itemsToBeCreated[no]['eventEndDate'].getMinutes());
            tmpEndDate.setHours(itemsToBeCreated[no]['eventEndDate'].getHours());
            tmpEndDate.setYear(itemsToBeCreated[no]['eventStartDate'].getFullYear());
            tmpEndDate.setMonth(itemsToBeCreated[no]['eventStartDate'].getMonth());
            tmpEndDate.setDate(itemsToBeCreated[no]['eventStartDate'].getDate());
            
            var dayDiff = itemsToBeCreated[no]['eventStartDate'].getTime() - dateStartOfWeek.getTime();
            dayDiff = Math.floor(dayDiff / (1000*60*60*24));
            el_x = dayPositionArray[dayDiff];
            topPos = getYPositionFromTime(itemsToBeCreated[no]['eventStartDate'].getHours(),itemsToBeCreated[no]['eventStartDate'].getMinutes());
            
            
            var elHeight = (tmpEndDate.getTime() - itemsToBeCreated[no]['eventStartDate'].getTime()) / (60 * 60*1000);
            elHeight = Math.round((elHeight * (itemRowHeight + 1)) - 2);
            
            if(itemsToBeCreated[no]['annotation'] == undefined){
                itemsToBeCreated[no]['annotation'] = '';
            }
            
            if (hasProjects && itemsToBeCreated[no]['project'] == undefined){
                itemsToBeCreated[no]['project'] = '';
            }
            
            currentAppointmentDiv = createNewAppointmentDiv((el_x - appointmentsOffsetLeft),
                topPos,itemWidth, 
                itemsToBeCreated[no]['action'], itemsToBeCreated[no]['project'], 
                itemsToBeCreated[no]['annotation'], elHeight);		
            currentAppointmentDiv.id = itemsToBeCreated[no]['id'];
            //je kleiner, desto groesser der zIndex
            currentAppointmentDiv.style.zIndex = 3000 - currentAppointmentDiv.style.height.replace(/px/,"");
            currentStartTimeDiv = getCurrentTimeDiv(currentAppointmentDiv, 'weekScheduler_start_time');
            currentEndTimeDiv = getCurrentTimeDiv(currentAppointmentDiv, 'weekScheduler_end_time');
            
            refreshStartEndTime();
            autoResizeAppointment();
            
            currentAppointmentDiv = false;	
            currentStartTimeDiv = false;
            currentEndTimeDiv = false;
            
            var newIndex = itemsToBeCreated[no]['id'];
            appointmentProperties[newIndex] = new Array();
            appointmentProperties[newIndex]['id'] = itemsToBeCreated[no]['id'];
            appointmentProperties[newIndex]['action'] = itemsToBeCreated[no]['action'];
            appointmentProperties[newIndex]['annotation'] = itemsToBeCreated[no]['annotation'];
            appointmentProperties[newIndex]['project'] = itemsToBeCreated[no]['project'];
            appointmentProperties[newIndex]['eventStartDate'] = itemsToBeCreated[no]['eventStartDate'];		
            appointmentProperties[newIndex]['eventEndDate'] = itemsToBeCreated[no]['eventEndDate'];
            appointmentProperties[newIndex]['object'] = currentAppointmentDiv;
        }
    }
    itemsAreArrived = true;
}


/* Update date and hour properties for an appointment after move or drag */

function updateAppointmentProperties(id)
{
    var obj = document.getElementById(id);
    var timeArray = getTimeAsArray(obj); 
    var startDate = getAppointmentDate(obj);
    var endDate = new Date();
    endDate = getAppointmentEndDate(obj)
    
    startDate.setHours(timeArray[0]);
    startDate.setMinutes(timeArray[1]);
    
    endDate.setHours(timeArray[2]);
    endDate.setMinutes(timeArray[3]);
    appointmentProperties[obj.id]['eventStartDate'] = startDate;
    appointmentProperties[obj.id]['eventEndDate'] = endDate;
    
    
    if(instantSave){
        saveAnItemToServer(obj.id);
    }
    
}

function getYPositionFromTime(hour,minute){
    return Math.floor(hour * (itemRowHeight+1) + (minute/60 * (itemRowHeight+1)));
}

function getItemsFromServer()
{
    if (!changesDone) {
        setTimeout('getItemsFromServer()' , 200);
        return;
    }
    itemsAreArrived = false;
    var ajaxIndex = weekSchedule_ajaxObjects.length;
    weekSchedule_ajaxObjects[ajaxIndex] = new sack();	
    weekSchedule_ajaxObjects[ajaxIndex].requestFile = externalSourceFile_items  + '?year=' + dateStartOfWeek.getFullYear() + '&month=' + (dateStartOfWeek.getMonth()/1+1) + '&day=' + dateStartOfWeek.getDate() + '&user=' + user + '&view=' + view;	// Specifying which file to get
    weekSchedule_ajaxObjects[ajaxIndex].onCompletion = function(){ 
        parseItemsFromServer(ajaxIndex);
    };	// Specify function that will be executed after file has been found
    weekSchedule_ajaxObjects[ajaxIndex].runAJAX();		// Execute AJAX function		
}

function getCurrentTimeDiv(inputObj, className)
{
    var subDivs = inputObj.getElementsByTagName('DIV');
    for(var no=0;no<subDivs.length;no++){
        if(subDivs[no].className==className){
            return subDivs[no];
        }
    }
}


function changeUser(e){
    user = document.getElementById("uId").value;
    historyStack.clear();
    refreshUndoButton();
    newTimeIntervalResource();
    updateHeaderDates();	
    clearAppointments();
    getItemsFromServer();
}


function changeView(e){
    initView();
    historyStack.clear();
    refreshUndoButton();
    updateHeaderDates();	
    clearAppointments();
    getItemsFromServer();
}

function initView(){
    timeIntervalsAreArrived = false;
    projectsAreArrived = false;
    actionsAreArrived = false;
    
    view = document.getElementById("confId").value;
    newTimeIntervalResource();
    
    horizontalResize = false;
    for(var i = 0; i < enableHorizontalResize.length; ++i){
        if (view == enableHorizontalResize[i]){
            horizontalResize = true;
        }
    }
    
    hasProjects = false;
    for(var i = 0; i < enableProjects.length; ++i){
        if (view == enableProjects[i]){
            hasProjects = true;
        }
    }
    initActionNames();
    initProjectNames();
    newTimeIntervalResource();
}

function newTimeIntervalResource(){
    var saveString = "?resource=timeIntervals" + '&view=' + view + "&u_id=" + user;
    var ajaxIndexAction = weekSchedule_ajaxObjects.length;
    weekSchedule_ajaxObjects[ajaxIndexAction] = new sack();	
    weekSchedule_ajaxObjects[ajaxIndexAction].requestFile = externalSourceFile_Resources  + saveString;
    weekSchedule_ajaxObjects[ajaxIndexAction].onCompletion = function(){ 
        receiveResources_complete(ajaxIndexAction, "timeIntervals");
    };
    weekSchedule_ajaxObjects[ajaxIndexAction].runAJAX();		// Execute AJAX function	
}


function getAppointmentDate(inputObj)
{
    var leftPos = getLeftPos(inputObj);
    
    var d = new Date();
    var tmpTime = dateStartOfWeek.getTime();
    tmpTime = tmpTime + (1000*60*60*24 * Math.floor((leftPos-appointmentsOffsetLeft - horizontalOffset) / (dayPositionArray[1] - dayPositionArray[0])));
    d.setTime(tmpTime);
    return d;
}


function getAppointmentEndDate(inputObj){
    leftPos = getLeftPos(inputObj) + (inputObj.style.width.replace(/px/,'') * 1) - newAppointmentWidth;
    
    var d = new Date();
    var tmpTime = dateStartOfWeek.getTime();
    tmpTime = tmpTime + (1000*60*60*24 * Math.floor((leftPos-appointmentsOffsetLeft - horizontalOffset) / (dayPositionArray[1] - dayPositionArray[0])));
    d.setTime(tmpTime);
    return d;
}



function getTimeAsArray(inputObj)
{
    var startTime = (getTopPos(inputObj) - appointmentsOffsetTop) / (itemRowHeight+1);
    if(startTime>23)startTime = startTime - 24;
    var startHour = Math.floor(startTime);
    var startMinute = Math.floor((startTime - startHour) *60);
    var endTime = (getTopPos(inputObj) + inputObj.offsetHeight - appointmentsOffsetTop) / (itemRowHeight+1);
    if(endTime>23)endTime = endTime - 24;
    var endHour = Math.floor(endTime);
    var endMinute = Math.floor((endTime - endHour) *60);
    return Array(startHour,startMinute,endHour,endMinute);	
}


function getTime(inputObj)
{
    return getStartTime(inputObj) + '-' + getEndTime(inputObj);	
    
}


function getStartTime(inputObj)
{
    var startTime = (getTopPos(inputObj) - appointmentsOffsetTop) / (itemRowHeight+1);
    if(startTime>23)startTime = startTime - 24;
    var startHour = Math.floor(startTime);
    var hourPrefix = "";
    if(startHour<10)hourPrefix = "0";
    var startMinute = Math.floor((startTime - startHour) *60);
    var startMinutePrefix = "";
    if(startMinute<10)startMinutePrefix="0";	
    
    return hourPrefix + startHour + ':' + startMinutePrefix + "" + startMinute;
}


function getEndTime(inputObj)
{
    var endTime = (getTopPos(inputObj) + inputObj.offsetHeight - appointmentsOffsetTop) / (itemRowHeight+1);
    if(endTime>23)endTime = endTime - 24;
    var endHour = Math.floor(endTime);
    
    var endHourPrefix = "";
    if(endHour<10)endHourPrefix = "0";	
    var endMinute = Math.floor((endTime - endHour) *60);
    var endMinutePrefix = "";
    if(endMinute<10)endMinutePrefix="0";
    
    return endHourPrefix + endHour + ':' + endMinutePrefix + "" +  endMinute;	
}


function initNewAppointment(e,inputObj)
{
    if(document.all)e = event;
    if(!inputObj)inputObj = this;
    newAppointmentCounter = 0;
    el_x = getLeftPos(inputObj);	
    el_y = getTopPos(inputObj);
    elWidth = inputObj.offsetWidth;
    
    mouse_x = e.clientX;
    mouse_y = e.clientY;
    timerNewAppointment();
    
    return false;
}

function timerNewAppointment()
{
    if(newAppointmentCounter>=0 && newAppointmentCounter<10){
        newAppointmentCounter = newAppointmentCounter + 1;
        setTimeout('timerNewAppointment()',30);
        return;
    }	
    if(newAppointmentCounter==10){
        
        if(initMinutes){
            var topPos = mouse_y - appointmentsOffsetTop + document.documentElement.scrollTop + document.getElementById('weekScheduler_content').scrollTop;
            topPos = topPos - (getMinute(topPos) % initMinutes);
            var rest = (getMinute(topPos) % initMinutes);
            if(rest!=0){
                topPos = topPos - (getMinute(topPos) % initMinutes);
            }
        }else{
            var topPos = (el_y - appointmentsOffsetTop);
        }
    
        currentAppointmentDiv = createNewAppointmentDiv( (el_x - appointmentsOffsetLeft),topPos,(elWidth-(appointmentMarginSize*2)),false );
        currentAppointmentDiv.id = 'new_' + startIdOfNewItems;
        var action = getCurrentElementByClass(currentAppointmentDiv, 'weekScheduler_appointment_action');
    
        appointmentProperties[currentAppointmentDiv.id] = new Array();
        appointmentProperties[currentAppointmentDiv.id]['project'] = '';
        appointmentProperties[currentAppointmentDiv.id]['annotation'] = '';
        appointmentProperties[currentAppointmentDiv.id]['action'] = action.innerHTML.replace('<span>','').replace('</span>','');	//Span-Tags entfernen
        appointmentProperties[currentAppointmentDiv.id]['object'] = currentAppointmentDiv;
        appointmentProperties[currentAppointmentDiv.id]['id'] = currentAppointmentDiv.id;
        startIdOfNewItems++;
        currentAppointmentDiv.style.zIndex = 3000 - currentAppointmentDiv.style.height.replace(/px/,"");
        currentAppointmentDiv.style.height='20px';
    
        currentStartTimeDiv = getCurrentTimeDiv(currentAppointmentDiv, 'weekScheduler_start_time');
        currentEndTimeDiv = getCurrentTimeDiv(currentAppointmentDiv, 'weekScheduler_end_time');
        saveInHistory(currentAppointmentDiv.id, true);
    }
}


function initHorizontalResize(e)
{
    if(document.all)e = event;
    currentAppointmentDiv = this.parentNode;
    
    //in History speichern
    saveInHistory(currentAppointmentDiv.id, false);
    
    horizontalResizeCounter = 0;
    el_x = getLeftPos(currentAppointmentDiv);	
    el_y = getTopPos(currentAppointmentDiv);	
    mouse_x = e.clientX;
    mouse_y = e.clientY;
    
    
    resizeAppointmentInitWidth = currentAppointmentDiv.style.width.replace('px','')/1;
    
    timerHorizontalResize();	
    return false;
}


function timerHorizontalResize()
{
    if(horizontalResizeCounter>=0 && horizontalResizeCounter<10){
        horizontalResizeCounter = horizontalResizeCounter + 1;
        timerId = setTimeout('timerHorizontalResize()',10);
        return;
    }	
    if(horizontalResizeCounter==10){
        
}
}


function initResizeAppointment(e)
{
    if(document.all)e = event;
    currentAppointmentDiv = this.parentNode;
    
    //in History speichern
    
    saveInHistory(currentAppointmentDiv.id, false);
    
    resizeAppointmentCounter = 0;
    el_x = getLeftPos(currentAppointmentDiv);	
    el_y = getTopPos(currentAppointmentDiv);	
    mouse_x = e.clientX;
    mouse_y = e.clientY;
    
    resizeAppointmentInitHeight = currentAppointmentDiv.style.height.replace('px','')/1;
    timerResizeAppointment();	
    return false;
}

function timerResizeAppointment()
{
    if(resizeAppointmentCounter>=0 && resizeAppointmentCounter<10){
        resizeAppointmentCounter = resizeAppointmentCounter + 1;
        timerId = setTimeout('timerResizeAppointment()',10);
        return;
    }	
    if(resizeAppointmentCounter==10){
        currentStartTimeDiv = getCurrentTimeDiv(currentAppointmentDiv, 'weekScheduler_start_time');
        currentEndTimeDiv = getCurrentTimeDiv(currentAppointmentDiv, 'weekScheduler_end_time');
    }
}

function initMoveAppointment(e,inputObj)
{
    if(document.all)e = event;
    if(!inputObj)inputObj = this.parentNode;
    currentAppointmentDiv = inputObj;
    
    //in History speichern
    saveInHistory(currentAppointmentDiv.id, false);
    
    currentAppointmentDiv.style.zIndex = 3000 - currentAppointmentDiv.style.height.replace(/px/,"");
    moveAppointmentCounter = 0;
    el_x = getLeftPos(inputObj);	
    el_y = getTopPos(inputObj);
    elWidth = inputObj.offsetWidth;
    
    mouse_x = e.clientX;
    mouse_y = e.clientY;
    
    
    
    timerMoveAppointment();
    return false;
    
}

function timerMoveAppointment()
{
    if(moveAppointmentCounter>=0 && moveAppointmentCounter<10){ 
        moveAppointmentCounter = moveAppointmentCounter + 1;
        timerId = setTimeout('timerMoveAppointment()',10);
        return;
    }
    if(moveAppointmentCounter==10){
        currentStartTimeDiv = getCurrentTimeDiv(currentAppointmentDiv, 'weekScheduler_start_time');
        currentEndTimeDiv = getCurrentTimeDiv(currentAppointmentDiv, 'weekScheduler_end_time');
    }	
}

//liefert die Zeit einer �bergebenen Position in Minuten seit 0:00 Uhr
function getMinute(topPos)
{
    var time = (topPos) / (itemRowHeight+1);
    
    var hour = Math.floor(time);
    var minute = Math.floor((time - hour) *60);
    return minute;
}


function schedulerMouseMove(e)
{
    if(document.all)e = event;
    if(newAppointmentCounter==10){
        if(!currentAppointmentDiv)return;
        var tmpHeight = e.clientY - mouse_y;
        var newHeight = Math.max(20,tmpHeight);
        currentAppointmentDiv.style.height = newHeight + 'px';
        
        if(timeInterval){									//falls nur in bestimmten Intervallen vergr��ert werden kann
            intervalResize(e, newHeight);
        }
        refreshStartEndTime();
    }
    
    if(moveAppointmentCounter==10){
        var topPos = (e.clientY - mouse_y + el_y - appointmentsOffsetTop);
        
        currentAppointmentDiv.style.top = topPos + 'px';
        
        var destinationLeftPos = false;
        for(var no=0;no<dayPositionArray.length;no++){
            if((e.clientX + window.scrollX) > dayPositionArray[no]){
                destinationLeftPos = dayPositionArray[no] - horizontalOffset;
            }
        }
        
        //verhindert das Ziehen �ber den linken Rand hinaus
        if(e.clientX < (dayPositionArray[0] + 1 - window.scrollX) ){			//HER
            destinationLeftPos = (dayPositionArray[0] - horizontalOffset);		//HER
        }										//HER
        
        if ((destinationLeftPos * 1 + currentAppointmentDiv.style.width.replace(/px/, '')  * 1) < dayPositionArray[6] + (dayPositionArray[1] - dayPositionArray[0]) - horizontalOffset ) {
            currentAppointmentDiv.style.left = (destinationLeftPos + appointmentMarginSize -2 ) + 'px';
        }
        
        //Annotation-Feld anpassen
        resizeAnnotation(currentAppointmentDiv)
        
        refreshStartEndTime();
    }
    
    
    if(resizeAppointmentCounter==10){
        var newHeight = Math.max((resizeAppointmentInitHeight + e.clientY - mouse_y),10);
        currentAppointmentDiv.style.height = newHeight + 'px';
        
        if(timeInterval){									//falls nur in bestimmten Intervallen vergr��ert werden kann
            intervalResize(e, newHeight);
        }
        
        //		var anno = getCurrentElementByClass(currentAppointmentDiv, 'weekScheduler_appointment_annotation');
        //		anno.innerHTML = '<span>' + a + ' ' + minutes + ' mi ' +  timeInterval[i] + ' '+ (i < timeInterval.length) + ':' + (minutes < timeInterval[i]) + '</span>';
        
        refreshStartEndTime();
    }
    
    
    if(horizontalResizeCounter==10){
        var intervals = Math.floor( (e.clientX - mouse_x) / newAppointmentWidth );
        
        var leftPos = currentAppointmentDiv.style.left.replace(/px/, '') * 1;
        var newerWidth = Math.max(resizeAppointmentInitWidth + (intervals * (newAppointmentWidth) ) - (appointmentMarginSize*2), ( newAppointmentWidth-(appointmentMarginSize*2)));
        newerWidth = newerWidth + getLeftPos(currentAppointmentDiv);
        
        var i = 0;
        while(i < (dayPositionArray.length - 1) && newerWidth > dayPositionArray[i] + newAppointmentWidth){
            ++i;
        }
        currentAppointmentDiv.style.width = Math.max( (dayPositionArray[i] - leftPos - horizontalOffset) + newAppointmentWidth - (appointmentMarginSize * 2)  , ( newAppointmentWidth-(appointmentMarginSize*2) ) ) + 'px';
        refreshStartEndTime();
    }
}

//�ndert, falls n�tig die Gr��e eines Termins, welcher nur in definierten Intervallen vergr��ert werden kann
function intervalResize(e, newHeight){
    var endTime = getEndTime(currentAppointmentDiv);
    var startTime = getStartTime(currentAppointmentDiv);
    var minutes =  getMinutesOfAppointment(currentAppointmentDiv);
    
    var i;
    for(i = 0; i < timeInterval.length && minutes > timeInterval[i]; ++i){}
    
    if (i >= timeInterval.length){
        i = timeInterval.length -1;
    }
    newHeight = Math.floor(timeInterval[i] * ((itemRowHeight+1) / 60)); 
    currentAppointmentDiv.style.height = newHeight + 'px';
    
    var a = 0;
    
    //Rundungsfehler ausb�geln
    while(minutes != timeInterval[i] && a < 10){
        currentAppointmentDiv.style.height = --newHeight + 'px';
        minutes =  getMinutesOfAppointment(currentAppointmentDiv);
        a++;
    }
    if(minutes != timeInterval[i]){
        a = 0;
        while(minutes != timeInterval[i] && a < 20){
            currentAppointmentDiv.style.height = ++newHeight + 'px';
            minutes =  getMinutesOfAppointment(currentAppointmentDiv);
            a++;
        }
    }
}

//liefert die Dauer des �bergebeben Termins in Minuten
function getMinutesOfAppointment(appointment){
    var endTime = getEndTime(appointment);
    var startTime = getStartTime(appointment);
    var minutes =  (endTime.substr(0, 2) * 60 + (endTime.substr(3,5) * 1)) - (startTime.substr(0, 2) * 60 + (startTime.substr(3,5) * 1))
    return minutes;
}


//aktualisiert die Startzeit und Endzeit
function refreshStartEndTime(){
    currentStartTimeDiv.innerHTML = '<span>Start: ' + getStartTime(currentAppointmentDiv) + '</span>';	
    currentEndTimeDiv.innerHTML = '<span>Ende:  ' + getEndTime(currentAppointmentDiv) + '</span>';	
}


function repositionFooter(inputDiv)
{
    var subDivs = inputDiv.getElementsByTagName('DIV');
    for(var no=0;no<subDivs.length;no++){
        if(subDivs[no].className=='weekScheduler_appointment_footer'){
            subDivs[no].style.bottom = '-1px';
            subDivs[no].style.display = 'block';
        }
    }	
}



function saveAnItemToServer_complete(index,oldId)
{
    changesDone = true;
    //Ben�tigten Rechte nicht vorhanden
    var response = replaceAll(weekSchedule_ajaxObjects[index].response, '\r', '');
    response = replaceAll(response, '\n', '');
    if(response == 'NO_RIGHTS') {
        alert('Diese Aktion darf nur vom Benutzer selbst oder vom zust�ndigen Admin ausgef�hrt werden.');
    }else{
        if(typeof oldId == 'string' && oldId.indexOf('new_')>=0){
            appointmentProperties[oldId]['id'] = weekSchedule_ajaxObjects[index].response;
            //   appointmentProperties[oldId]['object'].id = weekSchedule_ajaxObjects[index].response;
            appointmentProperties[weekSchedule_ajaxObjects[index].response] = appointmentProperties[oldId];
        
            //ID in dem History-Stack aktualisieren
            historyStack.replaceId(oldId,weekSchedule_ajaxObjects[index].response);
        
            weekSchedule_ajaxObjects[index] = false;
        
            if(!inlineTextAreaEnabled){
                editEventWindow(false,appointmentProperties[oldId]['object']);
            }
        }
    }
}


/* 
This function clears all appointments from the screen - Used when switching from one week to another or when switching the view
*/
function clearAppointments()
{
    for(var prop in appointmentProperties){
        if(appointmentProperties[prop]['id']){
            if(document.getElementById(appointmentProperties[prop]['id'])){
                var obj = document.getElementById(appointmentProperties[prop]['id']);
                obj.parentNode.removeChild(obj);
            }
            appointmentProperties[prop]['id'] = false;	
        }
        
    }	
}

function saveAnItemToServer(inputId)
{
    if (!appointmentProperties[inputId]['action']) 			appointmentProperties[inputId]['action']='';
    if (!appointmentProperties[inputId]['project']) 		appointmentProperties[inputId]['project']='';
    if (!appointmentProperties[inputId]['annotation']) 		appointmentProperties[inputId]['annotation']='';
    if (!appointmentProperties[inputId]['eventStartDate']) {
        updateAppointmentProperties(inputId);
        return;
    }
    
    
    var saveString = "?saveAnItem=true&id=" + appointmentProperties[inputId]['id']
    + '&action=' + escape(appointmentProperties[inputId]['action'])
    + '&project=' + escape(appointmentProperties[inputId]['project'])
    + '&annotation=' + escape(appointmentProperties[inputId]['annotation'])
    + '&eventStartDate=' + appointmentProperties[inputId]['eventStartDate']		//.toGMTString().replace('UTC','GMT')
    + '&eventEndDate=' + appointmentProperties[inputId]['eventEndDate']			//.toGMTString().replace('UTC','GMT')
    + '&view=' + view
    + '&uid=' + user;
    
    if(appointmentProperties[inputId]['id'].indexOf('new_')>=0){
        saveString = saveString + '&newItem=1';	
    }
    
    
    var ajaxIndex = weekSchedule_ajaxObjects.length;
    weekSchedule_ajaxObjects[ajaxIndex] = new sack();	
    weekSchedule_ajaxObjects[ajaxIndex].requestFile = externalSourceFile_save  + saveString;
    weekSchedule_ajaxObjects[ajaxIndex].onCompletion = function(){ 
        saveAnItemToServer_complete(ajaxIndex,appointmentProperties[inputId]['id']);
    };	// Specify function that will be executed after file has been found
    weekSchedule_ajaxObjects[ajaxIndex].runAJAX();		// Execute AJAX function	
}

function ffEndEdit(e)
{	
    if (!objectToModify)return;
    
    if (e.target) source = e.target;
    else if (e.srcElement) source = e.srcElement;
    
    if (source.nodeType == 3) // defeat Safari bug
        source = source.parentNode;	
    
    if(source.className !='weekScheduler_actionInput'){
        if(!objectToModify) return;
        objectToModify.blur();
    }
    
    if(source.className !='weekScheduler_annotationInput'){
        if(!objectToModify) return;
        objectToModify.blur();
    }
}



/* Checking keyboard events*/

function keyboardEventAction(e)
{
    if(document.all)e = event;
    if(e.keyCode==27){	// Escape key
        applyChangesAction(e);
    }

    if(e.keyCode==46 && activeEventObj){
        deleteAppointment(activeEventObj.id);
    }
}

//verarbeitet Tastendr�cke auf das Annotation Textfeld
function keyboardEventAnnotation(e){
    if(document.all)e = event;
    if(e.keyCode==27){	// Escape key
        applyChangesAnnotation(e);
    }
}


//wird bei mousedown auf annotation aufgerufen
function showTextArea(e){
    if(objectToModify != false){				//zuletzt bearbeitetes Objekt wird "geschlossen"
        objectToModify.blur();
    }
    if(document.all)e = event;
    if (e.target) source = e.target;
    else if (e.srcElement) source = e.srcElement;
    if (source.nodeType == 3) // defeat Safari bug
        source = source.parentNode;	
    
    if(source.className && source.className!='weekScheduler_appointment_annotation' && source.className!='weekScheduler_anAppointment'){		
        return;
    }		
    
    var annotation;
    if (source.parentNode.className == 'weekScheduler_appointment_annotation'){
        annotation = source.parentNode;
    }else if (source.className == 'weekScheduler_appointment_annotation'){
        annotation = source;
    }else{
        return;
    }

    var currentAppointment = annotation.parentNode;
    var annotationInput = currentAppointment.getElementsByTagName("TEXTAREA")[0];

    objectToModify = annotationInput;
    contentEditInProgress = true;
    annotation.style.display = 'none';
    annotationInput.style.display = 'block';
    annotationInput.focus();
}


//uebernimmt �nderungen der Annotation
function applyChangesAnnotation(e){
    if (newAppointmentCounter != -1){
        return ;
    }
    if(document.all)e = event;
    if (e.target) source = e.target;
    else if (e.srcElement) source = e.srcElement;
    if (source.nodeType == 3) // defeat Safari bug
        source = source.parentNode;	
    
    if(source.className && source.className!='weekScheduler_appointment_textarea' && source.className!='weekScheduler_anAppointment'){		
        return;
    }		
    
    var annotation;
    var annotationInput = source;
    tmpElements = source.parentNode.getElementsByTagName("DIV");
    for( var i = 0; i < tmpElements.length; ++i){
        if(tmpElements[i].className == 'weekScheduler_appointment_annotation'){
            annotation  = tmpElements[i];
        }
        if(tmpElements[i].className == 'weekScheduler_appointment_textarea'){
            annotationInput = tmpElements[i];
        }
    }
    
    var currentAppointment = annotation.parentNode;
    
    annotation.innerHTML = '<span>' + annotationInput.value + '</span>';
    annotationInput.style.display = 'none';
    annotation.style.display = 'block';
    
    //in History speichern
    saveInHistory(currentAppointment.id, false);
    
    appointmentProperties[currentAppointment.id]['annotation'] = annotationInput.value;
    
    objectToModify = false;
    
    if(instantSave){
        saveAnItemToServer(currentAppointment.id);
    }
    contentEditInProgress = false;
    repositionFooter(currentAppointment);
}



//wird bei mousedown auf action und project aufgerufen
function showActionSelect(e){
    if(objectToModify != false){				//zuletzt bearbeitetes Objekt wird "geschlossen"
        objectToModify.blur();
    }
    if(document.all)e = event;
    if (e.target) source = e.target;
    else if (e.srcElement) source = e.srcElement;
    if (source.nodeType == 3) // defeat Safari bug
        source = source.parentNode;	
    
    if(source.className && source.className!='weekScheduler_action' && source.className!='weekScheduler_project' && source.className!='weekScheduler_anAppointment'){		
        return;
    }		
    
    if(source.className == 'weekScheduler_anAppointment'){
        alert(source.className);
    }
    
    var action = source.parentNode;
    var inputClassName = 'weekScheduler_' + action.className.substr(26) + 'Input';
    var currentAppointment = action.parentNode;
    var actionInput = getCurrentElementByClass(currentAppointment, inputClassName);
    
    objectToModify = actionInput;
    contentEditInProgress = true;
    action.style.display = 'none';
    actionInput.style.display = 'block';
}


//uebernimmt Aenderungen der Aktion und des Projekts
function applyChangesAction(e){
    if(document.all)e = event;
    if (e.target) source = e.target;
    else if (e.srcElement) source = e.srcElement;
    if (source.nodeType == 3) // defeat Safari bug
        source = source.parentNode;	
    
    if(source.className && source.className!='weekScheduler_actionInput' && source.className!='weekScheduler_projectInput' && source.className!='weekScheduler_anAppointment'){		
        return;
    }		
    
    var actionInput = source;
    var className;
    var propertyName;
    
    if (actionInput.className == 'weekScheduler_actionInput'){
        //von actionInput aufgerufen
        className = 'weekScheduler_appointment_action';
        propertyName = 'action';
    }else{
        className = 'weekScheduler_appointment_project';
        propertyName = 'project';
    }

    var action = getCurrentElementByClass(source.parentNode, className);;

    var currentAppointment = action.parentNode;

    action.innerHTML = '<span>' + actionInput.value + '</span>';
    actionInput.style.display = 'none';
    action.style.display = 'block';

    //in History speichern
    saveInHistory(currentAppointment.id, false);

    appointmentProperties[currentAppointment.id][propertyName] = actionInput.value;
    objectToModify = false;
    contentEditInProgress = false;
    if(instantSave){
        saveAnItemToServer(currentAppointment.id);
    }
}


function MouseUp(e){
    newAppointmentCounter = -1;
    moveAppointmentCounter = -1;
    resizeAppointmentCounter = -1;
    horizontalResizeCounter = -1;
    clearTimeout(timerId);
    resizeAppointmentInitHeight = false;
}


/*
Creating new appointment DIV
*/
function createNewAppointmentDiv(leftPos, topPos,width, actionText, projectText, annotationText, height)
{
    var div = document.createElement('DIV');
    div.onclick = setElementActive;
    div.ondblclick = editEventWindow;
    div.className='weekScheduler_anAppointment';
    div.style.left = (leftPos - horizontalOffset) + 'px'; //HER
    div.style.top = topPos + 'px';
    div.style.width = width + 'px';
    if(height)div.style.height = height + 'px';
    
    //Startzeit
    var startTimeDiv = document.createElement('DIV');
    startTimeDiv.className='weekScheduler_start_time';
    startTimeDiv.innerHTML = '<span></span>';
    div.appendChild(startTimeDiv);
    
    //Endzeit
    var endTimeDiv = document.createElement('DIV');
    endTimeDiv.className='weekScheduler_end_time';
    endTimeDiv.innerHTML = '<span></span>';
    div.appendChild(endTimeDiv);
    
    //header
    var header = document.createElement('DIV');
    header.className= 'weekScheduler_appointment_header';
    header.innerHTML = '<span></span>';
    header.onmousedown = initMoveAppointment;
    header.onmouseup = MouseUp;
    header.style.cursor = 'move';
    div.appendChild(header);
    
    //horizontaler resize-Balken
    if (horizontalResize == true){
        var rightBar = document.createElement('DIV');
        rightBar.className = 'weekScheduler_appointment_rightBar'
        rightBar.innerHTML = '<span></span>';
        rightBar.onmousedown = initHorizontalResize;
        rightBar.onmouseup = MouseUp;
        rightBar.style.cursor = 'e-resize';
        div.appendChild(rightBar);
    }
    
    //action
    var action = document.createElement('DIV');	
    action.className = 'weekScheduler_appointment_action';
    action.innerHTML = '<span>' + actionText + '</span>';
    action.onmousedown = showActionSelect;
    div.appendChild(action);
    
    
    //action-select
    var actionInput = document.createElement('SELECT');
    actionInput.className = 'weekScheduler_actionInput';
    actionInput.onselect = applyChangesAction;
    actionInput.onkeyup = keyboardEventAction;
    actionInput.onblur = applyChangesAction;
    
    for(var i = 0; i < validActions.length; ++i){
        var newEntry = new Option(validActions[(validActions.length - 1) - i], validActions[(validActions.length - 1) - i], false, true);
        actionInput.options[actionInput.length] = newEntry;
    }
    
    if (!actionText){
        action.innerHTML = '<span>' + actionInput.value + '</span>';
    }
    
    div.appendChild(actionInput);
    
    
    //annotations
    if(annotationText == undefined){
        annotationText = '';
    }
    var annotation = document.createElement('DIV');	
    annotation.className = 'weekScheduler_appointment_annotation';
    annotation.innerHTML = '<span>' + annotationText + '</span>';
    annotation.onmousedown = showTextArea;
    div.appendChild(annotation);
    
    //Eingabe in annotation-Feld
    var textarea = document.createElement('TEXTAREA');
    textarea.className = 'weekScheduler_appointment_textarea';
    textarea.innerHTML = annotationText;
    textarea.onblur = applyChangesAnnotation;
    textarea.onkeyup = keyboardEventAnnotation;
    div.appendChild(textarea);
    
    //Projekte
    if (hasProjects){
        //Project-Auswahl
        var project = document.createElement('DIV');
        project.className = 'weekScheduler_appointment_project';
        project.innerHTML = '<span>' + projectText + '</span>';
        project.onmousedown = showActionSelect;
        div.appendChild(project);
        
        var projectInput = document.createElement('SELECT');
        projectInput.className = 'weekScheduler_projectInput';
        projectInput.onselect = applyChangesAction;
        projectInput.onblur = applyChangesAction;
        projectInput.onkeyup = keyboardEventAction;
        
        for(var i = 0; i < validProjects.length; ++i){
            var newEntry = new Option(validProjects[i], validProjects[i], false, true);
            projectInput.options[projectInput.length] = newEntry;
        }
        
        if (!projectText){
            project.innerHTML = '<span>' + projectInput.value + '</span>';
        }
        
        div.appendChild(projectInput);
        
        //annotation nach unten schieben
        annotation.style.top = '85px';
        textarea.style.top = '85px';
    }
    
    var footerDiv = document.createElement('DIV');
    footerDiv.className='weekScheduler_appointment_footer';
    footerDiv.style.cursor = 'n-resize';
    footerDiv.innerHTML = '<span></span>';
    footerDiv.onmousedown = initResizeAppointment;
    footerDiv.onmouseup = MouseUp;
    div.appendChild(footerDiv);
    
    weekScheduler_appointments.appendChild(div);		
    return div;
}



function schedulerMouseUp()
{
    if(newAppointmentCounter>=0){
        if(newAppointmentCounter==10){
            if(!currentAppointmentDiv)return;
        }
    }
    if(snapToMinutes && currentAppointmentDiv && moveAppointmentCounter==10){
        topPos = getTopPos(currentAppointmentDiv) - appointmentsOffsetTop;
        
        var minute = getMinute(topPos);
        var rest = (minute % snapToMinutes);
        if(rest>(snapToMinutes/2)){
            topPos = topPos + (snapToMinutes/60*(itemRowHeight+1)) - ((rest/60)*(itemRowHeight+1));
        }else{
            topPos = topPos - ((rest/60)*(itemRowHeight+1));
        }
        minute = getMinute(topPos);
        rest = (minute % snapToMinutes);
        if(rest!=0){
            topPos = topPos - ((rest/60)*(itemRowHeight+1));
        }
    
        minute = getMinute(topPos);
        rest = (minute % snapToMinutes);
        if(rest!=0){
            topPos = topPos - ((rest/60)*(itemRowHeight+1));
        }
        currentAppointmentDiv.style.top = topPos + 'px';
    }

    if(currentAppointmentDiv && snapToMinutes && (resizeAppointmentCounter==10 || newAppointmentCounter)){
        autoResizeAppointment();
    }

    //Annotation-Feld anpassen
    resizeAnnotation(currentAppointmentDiv)

    if(currentAppointmentDiv && !contentEditInProgress){
        repositionFooter(currentAppointmentDiv);
        updateAppointmentProperties(currentAppointmentDiv.id);
    } else {
        if(newAppointmentCounter==10){
            saveAnItemToServer(currentAppointmentDiv.id);
        }
    }



    refreshStartEndTime();

    currentAppointmentDiv = false;
    currentStartTimeDiv = false;
    currentEndTimeDiv = false;
    moveAppointmentCounter = -1;
    resizeAppointmentCounter = -1;
    newAppointmentCounter = -1;
    toggleViewCounter = -1;
    horizontalResizeCounter = -1;
    clearTimeout(timerId);
    resizeAppointmentInitHeight = false;
}


function autoResizeAppointment()
{
    var tmpPos = getTopPos(currentAppointmentDiv) - appointmentsOffsetTop + currentAppointmentDiv.offsetHeight;
    var startPos = tmpPos;
    
    var minute = getMinute(tmpPos);
    
    var rest = (minute % snapToMinutes);
    var height = currentAppointmentDiv.style.height.replace('px','')/1;
    
    if(rest>(snapToMinutes/2)){
        tmpPos = tmpPos + snapToMinutes - (minute % snapToMinutes);
    }else{
        tmpPos = tmpPos - (minute % snapToMinutes);
    }		

    var minute = getMinute(tmpPos);
    if((minute % snapToMinutes)!=0){
        tmpPos = tmpPos - (minute % snapToMinutes);
    }
    var minute = getMinute(tmpPos);
    if((minute % snapToMinutes)!=0){
        tmpPos = tmpPos - (minute % snapToMinutes);
    }

    currentAppointmentDiv.style.height = (height + tmpPos - startPos) + 'px';
    currentAppointmentDiv.style.zIndex = 3000 - currentAppointmentDiv.style.height.replace(/px/,"");


    //Annotation-Feld anpassen
    resizeAnnotation(currentAppointmentDiv)
}

function deleteEventFromView(index)
{	
    changesDone = true;
    var resp = weekSchedule_ajaxObjects[index].response;
    resp = replaceAll(resp, /\n/, '');
    resp = replaceAll(resp, /\r/, '');
    
    if(resp=='OK'){
        activeEventObj.parentNode.removeChild(activeEventObj);
        activeEventObj = false;	
    }else if(resp == 'NO_RIGHTS') {
        alert('Diese Aktion darf nur vom Benutzer selbst oder vom zust�ndigen Admin ausgef�hrt werden.');
    }else{
        // Error handling - event not deleted

        alert('Could not confirm that event has been deleted. Make sure that the script is configured correctly');
    }
}


function schedulerKeyboardEvent(e){
    if(document.all)e = event;
    if(e.keyCode==46 && activeEventObj){
        deleteAppointment(activeEventObj.id);
    }	
}


function deleteAppointment(id){
    if(confirm(txt_deleteEvent)){
        saveInHistory(id, true);
        var ajaxIndex = weekSchedule_ajaxObjects.length;
        weekSchedule_ajaxObjects[ajaxIndex] = new sack();
        weekSchedule_ajaxObjects[ajaxIndex].requestFile = externalSourceFile_delete  + '?eventToDeleteId=' + id + '&view=' + view;
        weekSchedule_ajaxObjects[ajaxIndex].onCompletion = function(){ 
            deleteEventFromView(ajaxIndex);
        };	// Specify function that will be executed after file has been found
        weekSchedule_ajaxObjects[ajaxIndex].runAJAX();		// Execute AJAX function	
    }		
}


// liefert das erste Subelement, des �bergebenen Elements, mit dem Klassennamen className 
function getCurrentElementByClass(currentAppointment, className){
    if(!currentAppointment){
        return false;
    }
    
    var tmpElements = currentAppointment.childNodes;
    
    for( var i = 0; i < tmpElements.length; ++i){
        if(tmpElements[i].className == className){
            return tmpElements[i];
        }
    }
    
    return false;
}


function getTopPos(inputObj)
{		
    var returnValue = inputObj.offsetTop;
    while((inputObj = inputObj.offsetParent) != null){
        if(inputObj.tagName!='HTML')returnValue += inputObj.offsetTop;
    }
    return returnValue;
}

function getLeftPos(inputObj)
{
    var returnValue = inputObj.offsetLeft;
    while((inputObj = inputObj.offsetParent) != null){
        if(inputObj.tagName!='HTML')returnValue += inputObj.offsetLeft;
    }
    
    return returnValue; // - 262;				//HER;
}

function cancelSelectionEvent(e)
{
    if(document.all)e = event;
    
    if (e.target) source = e.target;
    else if (e.srcElement) source = e.srcElement;
    if (source.nodeType == 3) // defeat Safari bug
        source = source.parentNode;
    if(source.tagName.toLowerCase()=='input' || source.tagName.toLowerCase()=='textarea')return true;
    
    return false;
    
}


function initWeekScheduler()
{
    window.onresize = resize;
    resize();
    user = document.getElementById("uId").value;
    
    historyStack = new stack(historyLength);
    //	historyStack.init(historyLength);
    initView();
    
    weekScheduler_container = document.getElementById('weekScheduler_container');
    if(!document.all)weekScheduler_container.onclick = ffEndEdit;
    weekScheduler_appointments = document.getElementById('weekScheduler_appointments');
    var subDivs = weekScheduler_appointments.getElementsByTagName('DIV');
    for(var no=0;no<subDivs.length;no++){
        if(subDivs[no].className=='weekScheduler_appointmentHour'){
            subDivs[no].onmousedown = initNewAppointment;
            
            if(!newAppointmentWidth)newAppointmentWidth = subDivs[no].offsetWidth;
        }
        
        if(subDivs[no].className=='weekScheduler_appointments_day'){
            horizontalOffset = subDivs[no].offsetParent.offsetLeft;		//leftOffset of the table
            dayPositionArray[dayPositionArray.length] = getLeftPos(subDivs[no]);
        }
    }
    document.getElementById('weekScheduler_content').scrollTop = (initTopHour*(itemRowHeight+1));
    
    //	initTopHour
    appointmentsOffsetTop = getTopPos(weekScheduler_appointments);
    appointmentsOffsetLeft = 2 - appointmentMarginSize;	
    
    document.documentElement.onmousemove = schedulerMouseMove;
    document.documentElement.onselectstart = cancelSelectionEvent;
    document.documentElement.onmouseup = schedulerMouseUp;
    document.documentElement.onkeydown = schedulerKeyboardEvent;
    
    var tmpDate = new Date();
    var dateItems = initDateToShow.split(/\-/g);
    tmpDate.setFullYear(dateItems[0]);
    tmpDate.setDate(dateItems[2]/1);
    tmpDate.setMonth(dateItems[1]/1-1);
    tmpDate.setHours(3);			//nicht auf Null setzen, da es sonst zu Problemen am 26.3. kommt (Umstellung auf Sommerzeit)
    tmpDate.setMinutes(0);
    tmpDate.setSeconds(0);
    
    var day = tmpDate.getDay();
    if(day==0)day=7;
    if(day>1){
        var time = tmpDate.getTime();
        time = time - (1000*60*60*24) * (day-1);
        tmpDate.setTime(time);	
    }
    dateStartOfWeek = new Date(tmpDate);
    
    updateHeaderDates();
    
    if(externalSourceFile_items){
        getItemsFromServer();		
    }
    refreshUndoButton();
}

//sendet eine Anfrage, um die g�ltigen Aktionsnamen zu erfahren
function initActionNames(){
    //Aktions- und Projektnamen bersorgen
    var saveString = "?resource=actions" + '&view=' + view;
    var ajaxIndexAction = weekSchedule_ajaxObjects.length;
    weekSchedule_ajaxObjects[ajaxIndexAction] = new sack();	
    weekSchedule_ajaxObjects[ajaxIndexAction].requestFile = externalSourceFile_Resources  + saveString;
    weekSchedule_ajaxObjects[ajaxIndexAction].onCompletion = function(){ 
        receiveResources_complete(ajaxIndexAction, "action");
    };
    weekSchedule_ajaxObjects[ajaxIndexAction].runAJAX();		// Execute AJAX function	
}


//sendet eine Anfrage, um die g�ltigen Projektnamen zu erfahren
function initProjectNames(){
    //Aktions- und Projektnamen bersorgen
    var saveString = "?resource=projects" + '&view=' + view;
    var ajaxIndexAction = weekSchedule_ajaxObjects.length;
    weekSchedule_ajaxObjects[ajaxIndexAction] = new sack();	
    weekSchedule_ajaxObjects[ajaxIndexAction].requestFile = externalSourceFile_Resources  + saveString;
    weekSchedule_ajaxObjects[ajaxIndexAction].onCompletion = function(){ 
        receiveResources_complete(ajaxIndexAction, "projects");
    };
    weekSchedule_ajaxObjects[ajaxIndexAction].runAJAX();		// Execute AJAX function	
}


//passt die Gr��e des Annotation-Feldes an
function resizeAnnotation(currentAppointmentDiv){
    if(currentAppointmentDiv){
        currentAppointmentHeight = currentAppointmentDiv.style.height.replace('px','');
        var annotation = getCurrentElementByClass(currentAppointmentDiv, 'weekScheduler_appointment_annotation');
        var annotationText = getCurrentElementByClass(currentAppointmentDiv, 'weekScheduler_appointment_textarea');
        if(currentAppointmentHeight > 75){
            annotation.style.height = (currentAppointmentHeight - 75) + 'px';
            annotationText.style.height = (currentAppointmentHeight - 75) + 'px';
        }
    }
}


//wird aufgerufen, wenn Aktions- oder Projektnamen oder die g�ltigen Zeitintervalle empfangen wurden
function receiveResources_complete(ajaxIndex, resource){
    var receive = weekSchedule_ajaxObjects[ajaxIndex].response;
    var resources = receive.split('<' + resource + '>');
    
    if(resource == "action"){
        actionsAreArrived = true;
        validActions = new Array();
    }else if(resource == "timeIntervals"){
        timeIntervalsAreArrived = true;
        if(receive.length > 0){
            timeInterval = new Array();
        }else{
            timeInterval = false;
        }
    }else {
        projectsAreArrived = true;
        validProjects = new Array();
    }


    weekSchedule_ajaxObjects[ajaxIndex] = false;

    for(var no=1;no < resources.length;++no){
        var line = resources[no].replace('</' + resource + '>', '');
    
        line = replaceAll(line, /\n/, '');
        line = replaceAll(line, /\r/, '');
    
        if (resource == "action"){
            validActions[no - 1] = line;
        }else if(resource == "timeIntervals"){
            timeInterval[no - 1] = line;
        }else{
            validProjects[no - 1] = line;
        }
    }
}


//ersetzt alle durch regexp angegebenen Zeichen im �bergebenen String durch newString
function replaceAll(string, regExp, newString){
    while(string.search(regExp) != -1){
        string = string.replace(regExp, newString);
    }
    return string;
}



function displayPreviousWeek()
{
    var tmpTime = dateStartOfWeek.getTime();
    tmpTime = tmpTime - (1000*60*60*24*7);
    dateStartOfWeek.setTime(tmpTime);
    historyStack.clear(); 
    refreshUndoButton();
    updateHeaderDates();	
    clearAppointments();
    getItemsFromServer();
    
}


function displayNextWeek()
{
    var tmpTime = dateStartOfWeek.getTime();
    tmpTime = tmpTime + (1000*60*60*24*7);
    dateStartOfWeek.setTime(tmpTime);
    historyStack.clear();
    refreshUndoButton();
    updateHeaderDates();
    clearAppointments();
    getItemsFromServer();
}


function updateHeaderDates()
{
    var weekScheduler_dayRow = document.getElementById('weekScheduler_dayRow');
    var subDivs = weekScheduler_dayRow.getElementsByTagName('DIV');		
    var tmpDate2 = new Date(dateStartOfWeek);
    
    
    for(var no=0;no<subDivs.length;no++){
        var month = tmpDate2.getMonth()/1 + 1;
        var date = tmpDate2.getDate();
        var tmpHeaderFormat = " " + headerDateFormat;
        tmpHeaderFormat = tmpHeaderFormat.replace('d',date);
        tmpHeaderFormat = tmpHeaderFormat.replace('m',month);
        
        subDivs[no].getElementsByTagName('SPAN')[0].innerHTML = tmpHeaderFormat;
        
        dayDateArray[no] = month + '|' + date;
        
        var time = tmpDate2.getTime();
        time = time + (1000*60*60*24);
        tmpDate2.setTime(time);
    }	
}

//�ndert die Gr��e des Kalenders auf den Wert in der Variablen itemRowHeight
function zoom(oldHight){
    //stellt sicher, dass diese Funktion erst ausgef�hrt wird, wenn die angeforderten Daten angekommen sind
    if(!itemsAreArrived){
        setTimeout('zoom(' + oldHight + ')' , 1000);
        return;
    }
    
    
    var elements = document.getElementsByTagName("*");
    var scrollBarPosition = document.getElementById('weekScheduler_content').scrollTop / (oldHight+1);
    
    for (var i = 0; i < elements.length; ++i){
        if (elements[i].className == 'calendarContentTime' || elements[i].className == 'weekScheduler_appointmentHour'){
            elements[i].style.height = itemRowHeight + 'px';
        }else if (elements[i].className == 'calendarContentTime' || elements[i].className == 'content_hour'){
            elements[i].style.height = itemRowHeight + 'px';
        }
    }

    clearAppointments();
    getItemsFromServer();
    //Scrollbar richtig positionieren
    document.getElementById('weekScheduler_content').scrollTop = (scrollBarPosition*(itemRowHeight+1));

}

//vergr��ert den Kalender
function calendarZoomIn(){
    if (itemRowHeight < 100){
        var oldHeight = itemRowHeight;
        itemRowHeight += 5;
        zoom(oldHeight);
    }
}

//verkleinert den Kalender
function calendarZoomOut(){
    if (itemRowHeight > 40){
        var oldHeight = itemRowHeight;
        itemRowHeight -= 5;
        zoom(oldHeight);
    }
}


// aktiviert bzw. deaktiviert den Undo-Button
function refreshUndoButton(){
    if(historyStack.isEmpty() == true){
        document.getElementById('CalendarUndoId').disabled = true;
    }else{
        document.getElementById('CalendarUndoId').disabled = false;
    }
}


//speichert das, durch die �bergebene id identifizierte, Objekt in dem History-Stack
function saveInHistory(id, newObject){
    var object = new Array();
    object['id'] = id; 
    object['action'] = appointmentProperties[id]['action']; 
    object['project'] = appointmentProperties[id]['project']; 
    object['annotation'] = appointmentProperties[id]['annotation']; 
    object['eventStartDate'] = appointmentProperties[id]['eventStartDate']; 
    object['eventEndDate'] = appointmentProperties[id]['eventEndDate']; 
    object['newObject'] = newObject;
    historyStack.insert(object);
    refreshUndoButton();
}


//macht die letzte Aktionn r�ckg�ngig
function calendarUndo(){
    var lastModifiedAppointment = historyStack.get();
    
    if (lastModifiedAppointment['newObject'] == true && lastModifiedAppointment['id'].indexOf('new') >= 0) {
        changesDone = false;
        deleteAppointment(lastModifiedAppointment.id);
    } else { 
        if (lastModifiedAppointment['newObject'] == true) {
            lastModifiedAppointment.id = 'new_' + startIdOfNewItems;
            startIdOfNewItems += 1;
            appointmentProperties[lastModifiedAppointment.id] = lastModifiedAppointment;
            changesDone = false;
            saveAnItemToServer(lastModifiedAppointment.id);
        } else {
            appointmentProperties[lastModifiedAppointment.id]['action'] = lastModifiedAppointment['action'];
            appointmentProperties[lastModifiedAppointment.id]['project'] = lastModifiedAppointment['project'];
            appointmentProperties[lastModifiedAppointment.id]['annotation'] = lastModifiedAppointment['annotation'];
            appointmentProperties[lastModifiedAppointment.id]['eventStartDate'] = lastModifiedAppointment['eventStartDate'];
            appointmentProperties[lastModifiedAppointment.id]['eventEndDate'] = lastModifiedAppointment['eventEndDate'];
            changesDone = false;
            saveAnItemToServer(lastModifiedAppointment.id);
        }
    }

    clearAppointments();
    //damit der Server genuegend Zeit hat, die vorigen �nderungen zu t�tigen
    //        setTimeout("getItemsFromServer()", 500);
    getItemsFromServer();
    refreshUndoButton();
}


window.onload = initWeekScheduler;