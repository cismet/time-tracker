/*
	alle �nderungen werden im Input-Feld modId gespeichert. Sobald '�nderungen speichern' gedr�ckt wird, wird 
	das modId-Feld �bertragen und die enthaltenen Daten werden vom Server interpretiert.

	Aufbau modId:
	
	Daten �ndern: 			oid|Spalte|neueDaten					Bsp: 1232|action|GO|
	Datensatz l�schen:		oid|delete| |							Bsp: 1222|delete| |
	neuer Datensatz anlegen:tmpOid|tmpOid|Aktion;Zeit;BenutzerId|	Bsp: New1|New1|GO;2006-02-12 14:33:00;3|

	tmpOid = 'New' + fortlaufende Nummer
*/

//speichert den Wert des n�chsten neu angelegten Datensatzes
var newId = 1;
//bestimmt, ob gerade ein neuer Datensatz angelegt wird
var CreateNewElement = false;


function show(){
    setActionText("auswaehlen");
    document.getElementById("form1").submit();
}


//der ausgew�hlte Datensatz wird in die Steuerelemente eingetragen
function rowSelect(id, name, desc){
    if (desc == "null"){
        desc = "";
    }
    document.getElementById("nameId").value = name;
    document.getElementById("descId").value = desc;
    document.getElementById("rowId").value = id;
}


//neuer Datensatz wird angelegt
function newSelect(year, month, day){
    var table = document.getElementById("tsTable");
    var tbody = document.getElementById("tsTbody");
    var tableRow = document.createElement("TR");
	
    var tableBlankCol = document.createElement("TD");
    var tableTimeCol = document.createElement("TD");
    var tableActCol = document.createElement("TD");
    var tableAnnoCol = document.createElement("TD");
    var tableProjCol = document.createElement("TD");
    var tableDurCol = document.createElement("TD");
    var currenttext;
    var button;
	
    tableRow.setAttribute("id", "New".concat(newId));
	
    //Delete-Button erzeugen
    button  = document.createElement("button");
    button.setAttribute("onclick", "rowDelete('New" + newId + "')");
    button.setAttribute("type", "button");
    currenttext=document.createTextNode("X");
    button.appendChild(currenttext);
	
    //Tabellenspalten f�llen
    tableBlankCol.appendChild(button);
	
    currenttext=document.createTextNode("\u00A0");
    tableActCol.appendChild(currenttext);

    currenttext=document.createTextNode("\u00A0");
    tableAnnoCol.appendChild(currenttext);

    currenttext=document.createTextNode("\u00A0");
	
    tableProjCol.appendChild(currenttext);

    currenttext=document.createTextNode("\u00A0");
    tableDurCol.appendChild(currenttext);

    var now = new Date();
    currenttext=document.createTextNode( year + "-" + month + "-" + day + now.getHours + ":" + now.getMinutes + ":00");
    tableTimeCol.appendChild(currenttext);
	
    tableActCol.setAttribute("id", "actionNew".concat(newId));
    var val = "actionSelect('".concat("New".concat(newId)).concat("')");
    var opt = "onclick";
    tableActCol.setAttribute(opt, val );

    tableTimeCol.setAttribute("id", "timeNew".concat(newId));

    tableAnnoCol.setAttribute("id", "annotationNew".concat(newId));
    val = "annotationSelect('".concat("New".concat(newId)).concat("')");
    tableAnnoCol.setAttribute("onclick", val);

    tableProjCol.setAttribute("id", "projectNew".concat(newId));
    val = "projectSelect('".concat("New".concat(newId)).concat("')") ;
    tableProjCol.setAttribute("onclick", val);

    tableDurCol.setAttribute("id", "durationNew".concat(newId));
    val = "durationSelect('".concat("New".concat(newId)).concat("')");
    tableDurCol.setAttribute("onclick", val );


    //Spalten zur Zeile hinzuf�gen
    tableRow.appendChild(tableBlankCol);
    tableRow.appendChild(tableTimeCol);
    tableRow.appendChild(tableActCol);
    tableRow.appendChild(tableAnnoCol);
    tableRow.appendChild(tableProjCol);
    tableRow.appendChild(tableDurCol);
	
    tbody.appendChild(tableRow);
	
    //Zeit und Aktion ausw�hlen
    timeSelect("New".concat(newId));
    CreateNewElement = true;
    actionSelect("New".concat(newId));
    actionClicked("New".concat(newId));
    timeSelect("New".concat(newId));
    tableTimeCol.onclick();

    newId = newId + 1;
}


//l�scht die Zeile mit der �bergebenen oid 
function rowDelete(oid){
    var index = document.getElementById(oid).rowIndex;
    document.getElementById("tsTable").deleteRow(index);
	
    if (oid.indexOf("New") != -1) {			//Zeile wurde erst neu angelegt. D.h. nur Befehl zum Anlegen wird gel�scht
        var newVal = "";
        var oldVal = document.getElementById("modId").value;
        var modArray = oldVal.split("|");
        for (var i = 0;i < (modArray.length - 1);++i) {
            if (modArray[i] == oid) {
                i = i + 2;
            } else {
                newVal = newVal + modArray[i] + "|";
            }
        }
        document.getElementById("modId").value = newVal;
    } else{
        var oldVal = document.getElementById("modId").value;
        document.getElementById("modId").value = oldVal.concat(oid.concat("|delete| |"));
    }
    var oldVal = document.getElementById("modId").value;
}


//pr�ft, ob die �bergebene Aktion g�ltig ist
function actionIsValid(val){
    var i = 0;

    for(i = 0; i < document.getElementsByName("HiddenActionData").length;++i){
        if (document.getElementsByName("HiddenActionData")[i].value.toUpperCase() == val.toUpperCase()){
            return document.getElementsByName("HiddenActionData")[i].value;
        }
    }
    return null;
}


//pr�ft, ob der �bergebene Wert ein g�lltiges Projekt ist
function projectIsValid(val){
    var i = 0;
	
    if(val == ""){
        return "";
    }
	
    for(i = 0; i < document.getElementsByName("HiddenProjectData").length;++i){
        if (document.getElementsByName("HiddenProjectData")[i].value.toUpperCase() == val.toUpperCase()){
            return document.getElementsByName("HiddenProjectData")[i].value;
        }
    }
    return null;
}


//neue Aktion wird ausgewaehlt
function actionClicked(oid){
    var elementId = "action".concat(oid);
    var oldElementValue = document.getElementById(elementId).firstChild.nodeValue;
    var combo = document.getElementById("comboAction".concat(oid));
    var val = combo.value;
    var newVal;

    if(val != null && (newVal = actionIsValid(val)) != null){
        //Combo-Box l�schen, onclick-Event-Handler wieder erzeugen
        document.getElementById(elementId).removeChild(combo);
        document.getElementById(elementId).firstChild.nodeValue = newVal;
        document.getElementById(elementId).setAttribute("onclick", "actionSelect('".concat(oid).concat("')"));

        document.getElementById(elementId).firstChild.nodeValue = newVal;
        if(oldElementValue != " " && !CreateNewElement){
            //Zeile ist keine neu angelegte Zeile
            var oldVal = document.getElementById("modId").value;
            document.getElementById("modId").value = oldVal.concat(oid.concat("|action|" + newVal + "|"));
        }
    }else if(val != null){
        var message = val.concat(" ist keine g�ltige Aktion.\nG�ltige Aktionen sind:\n");
        for(i = 0; i < document.getElementsByName("HiddenActionData").length;++i){
            message = message.concat(document.getElementsByName("HiddenActionData")[i].value + "\n");
        }
        alert(message);
        return false;
    }
}


//Combo-Box zur Auswahl einer neuen Aktion wird angezeigt
function actionSelect(oid){
    var elementId = "action".concat(oid);
    var oldVal = document.getElementById(elementId).firstChild.nodeValue;
    var i;
    var actionname;

    //Zeichendaten des ausgew�hlten Knotens l�schen
    var charCount = document.getElementById(elementId).firstChild.nodeValue.length;
    document.getElementById(elementId).firstChild.deleteData(0, charCount);


    //Combo-Box erstellen und fuellen
    var combo = document.createElement("select");
    combo.setAttribute("id", "comboAction".concat(oid));
    combo.setAttribute("onchange", "actionClicked('".concat(oid).concat("')"));

    for(i = 0; i < document.getElementsByName("HiddenActionData").length;++i){
        actionname = document.getElementsByName("HiddenActionData")[i].value;
        combo.options[i] = new Option(actionname, actionname);
    }
	
    if(!CreateNewElement){
        combo.value = oldVal;
    }else{
        //neues Element beginnt mit eine Kommen-Aktion
        combo.value = combo.options[4].value;
    }
	
    document.getElementById(elementId).appendChild(combo);
	
    //Event-Handler bei Tabellen-Zelle deaktivieren
    //firefox
    document.getElementById(elementId).removeAttribute("onclick");
    //opera
    document.getElementById(elementId).onclick = null;
}


//initialisiert ein Kalender-Objekt
function timeSelect(oid){
    var startDate = new Date();
    startDate.setFullYear(2006);
    startDate.setMonth(4);
    startDate.setDate(11);
	
    var elementId = "time".concat(oid);
    Calendar.setup(
    {
        inputField : elementId, // ID of the input field
        ifFormat : "%Y-%m-%d %H:%M", // the date format
        button : elementId, // ID of the button
        showsTime : true,
        timeFormat : "24",
        displayArea : elementId,
        daFormat    : "%Y-%m-%d %H:%M:00",
        onUpdate : saveNewTime,
        singleClick : true,
        date: startDate,
        electric : false
    }
    );
}


//Callback-Funktion des Kalenders
function saveNewTime(cal){
    var val = cal.params.inputField.firstChild.nodeValue;
    var id = cal.params.inputField.id;
    var oid = id.substring(4, id.length);
    var oldVal = document.getElementById("modId").value;

    //dieses Element ist nicht gerade erst neu angelegt worden
    if(!CreateNewElement){
        document.getElementById("modId").value = oldVal.concat(oid.concat("|time|" + val + "|"));
    }else{
        var action = document.getElementById("action".concat(oid)).firstChild.nodeValue;
        var userId = document.getElementById("userHiddenId").value;
        var time = document.getElementById("time".concat(oid)).firstChild.nodeValue;
        document.getElementById("modId").value = oldVal.concat(oid + "|" + oid + "|" + action + ";" + time + ";" + userId + "|");
        CreateNewElement = false;
    }
}



//initialisiert alle Kalender-Objekte
function initCalendar(){
    var table = document.getElementById("tsTable");
    if (table != null) {
        var rows = table.rows;
        var i = 1;

        for(; i < rows.length; ++i) {
            var oid = rows[i].id;
            timeSelect(oid);
        }
    }
}




//ausgew�hlter Kommentar wird ge�ndert
function annotationModified(oid){
    var elementId = "annotation".concat(oid);
    var text = document.getElementById( "textAnnotation" + oid );
    var newValue = text.value;

    document.getElementById(elementId).removeChild(text);
    document.getElementById(elementId).firstChild.nodeValue = newValue;
    document.getElementById(elementId).setAttribute("onclick", "annotationSelect('".concat(oid).concat("')"));
	
    //neue Daten speichern
    var oldVal = document.getElementById("modId").value;
    document.getElementById("modId").value = oldVal.concat(oid.concat("|annotation|"));

    if(newValue == ""){
        oldVal = document.getElementById("modId").value;
        document.getElementById("modId").value = oldVal.concat("null|");
    }else{
        oldVal = document.getElementById("modId").value;
        document.getElementById("modId").value = oldVal.concat(newValue.concat("|"));
    }

}


function annotationSelect(oid){
    var elementId = "annotation".concat(oid);
    var oldVal = document.getElementById(elementId).firstChild.nodeValue;

    //Zeichendaten des ausgew�hlten Knotens l�schen
    var charCount = document.getElementById(elementId).firstChild.nodeValue.length;
    document.getElementById(elementId).firstChild.deleteData(0, charCount);
	
    //Textfeld erstellen und f�llen
    var text = document.createElement("Input");
    text.setAttribute("id", "textAnnotation".concat(oid));
    text.setAttribute("onblur", "annotationModified('".concat(oid).concat("')"));
    text.setAttribute("size", "15");
    text.value = oldVal;

    document.getElementById(elementId).appendChild(text);

    //Event-Handler bei Tabellen-Zelle deaktivieren
    // firefox
    document.getElementById(elementId).removeAttribute("onclick");
    //opera
    document.getElementById(elementId).onclick = null;
}



//neues Projekt wird ausgewaehlt
function projectClicked(oid){
    var elementId = "project".concat(oid);
    var combo = document.getElementById("combo".concat(oid));
    var val = combo.value;
    var newVal;
	
    if(val != null && (newVal = projectIsValid(val)) != null){
        //Combo-Box l�schen, onclick-Event-Handler wieder erzeugen
        document.getElementById(elementId).removeChild(combo);
        document.getElementById(elementId).firstChild.nodeValue = newVal;
        document.getElementById(elementId).setAttribute("onclick", "projectSelect('".concat(oid).concat("')"));


        var oldVal = document.getElementById("modId").value;
        document.getElementById("modId").value = oldVal.concat(oid.concat("|project_id|"));
        if(val == ""){
            oldVal = document.getElementById("modId").value;
            document.getElementById("modId").value = oldVal.concat("null|");
        }else{
            oldVal = document.getElementById("modId").value;
            document.getElementById("modId").value = oldVal.concat(newVal.concat("|"));
        }
    }else if(val != null){
        var message = val.concat(" ist keine g�ltiges Projekt.\nG�ltige Projekte sind:\n");
        for(i = 0; i < document.getElementsByName("HiddenActionData").length;++i){
            message = message.concat(document.getElementsByName("HiddenProjectData")[i].value + "\n");
        }
        alert(message);
    }
}


//Combobox zur Auswahl eines neuen Projekts wird angezeigt
function projectSelect(oid){
    var elementId = "project".concat(oid);
    var oldVal = document.getElementById(elementId).firstChild.nodeValue;
    var i;
    var projectname;

    //Zeichendaten des ausgew�hlten Knotens l�schen
    var charCount = document.getElementById(elementId).firstChild.nodeValue.length;
    document.getElementById(elementId).firstChild.deleteData(0, charCount);

    //Combo-Box erstellen und fuellen
    var combo = document.createElement("select");
    combo.setAttribute("id", "combo".concat(oid));
    combo.setAttribute("onchange", "projectClicked('".concat(oid).concat("')"));

    combo.options[0] = new Option("", "");

    for(i = 0; i < document.getElementsByName("HiddenProjectData").length;++i){
        projectname = document.getElementsByName("HiddenProjectData")[i].value;
        combo.options[i + 1] = new Option(projectname, projectname);
    }
    combo.value = oldVal;
    document.getElementById(elementId).appendChild(combo);
	
    //Event-HaremoveAttributendler bei Tabellen-Zelle deaktivieren
    //firefox
    document.getElementById(elementId).removeAttribute("onclick");
    //opera
    document.getElementById(elementId).onclick = null;
}


//ausgew�hlte 'Dauer in Stunden' wird ge�ndert
function durationModified(oid, oldValue){
    var elementId = "duration".concat(oid);
    var text = document.getElementById( "textDuration" + oid );
    var newValue = text.value;

    document.getElementById(elementId).removeChild(text);
    document.getElementById(elementId).firstChild.nodeValue = newValue;
    document.getElementById(elementId).setAttribute("onclick", "durationSelect('".concat(oid).concat("')"));
	
    //neue Daten speichern
    if (newValue != null && isNaN(parseFloat(newValue)) && newValue != "") {
        alert("Ihr Eingabewert ist keine g�ltige Zahl!");
        document.getElementById(elementId).firstChild.nodeValue = oldValue;
    } else {
        if (newValue != null) {
            newValue = newValue.replace(",", ".");
            newValue = "" + parseFloat(newValue);
            if (newValue == "NaN") {
                document.getElementById(elementId).firstChild.nodeValue = "";
            } else {
                document.getElementById(elementId).firstChild.nodeValue = newValue;
            }
			
            var oldVal = document.getElementById("modId").value;
            document.getElementById("modId").value = oldVal.concat(oid.concat("|duration_in_hours|"));
            if (newValue == "" || newValue == "NaN") {
                oldVal = document.getElementById("modId").value;
                document.getElementById("modId").value = oldVal.concat("null|");
            } else {
                oldVal = document.getElementById("modId").value;
                document.getElementById("modId").value = oldVal.concat(newValue.concat("|"));
            }
        }
    }
}


function durationSelect(oid){
    var elementId = "duration".concat(oid);
    var oldVal = document.getElementById(elementId).firstChild.nodeValue;

    //Zeichendaten des ausgew�hlten Knotens l�schen
    var charCount = document.getElementById(elementId).firstChild.nodeValue.length;
    document.getElementById(elementId).firstChild.deleteData(0, charCount);
	
    //Textfeld erstellen und f�llen
    var text = document.createElement("Input");
    text.setAttribute("id", "textDuration".concat(oid));
    text.setAttribute("onblur",  "durationModified('" + oid + "', '" + oldVal + "')");
    text.setAttribute("size",  "7");
    text.value = oldVal;

    document.getElementById(elementId).appendChild(text);

    //Event-Handler bei Tabellen-Zelle deaktivieren
    //firefox
    document.getElementById(elementId).removeAttribute("onclick");
    //opera
    document.getElementById(elementId).onclick = null;
}


//speichert die aktuelle Aktion
function setActionText(val){
    document.getElementById("actId").value = val;
}
