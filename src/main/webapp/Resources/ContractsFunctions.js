//aktiviert bzw. deaktiviert die angezeigten Steuerelemente
function deActivateInputs(disable){
    document.getElementById("whowId").disabled = disable;
    document.getElementById("ydohId").disabled = disable;
    document.getElementById("to_dateId").disabled = disable;
    document.getElementById("from_dateId").disabled = disable;
}

//der ausgewählte Datensatz wird in die Steuerelemente eingetragen
function rowSelect(id, date_from, date_to, whow, ydoh){
    deActivateInputs(false);
    if (date_to == "null"){
        date_to = "";
    }
    document.getElementById("whowId").value = whow;
    document.getElementById("ydohId").value = ydoh;
    document.getElementById("to_dateId").value = date_to;
    document.getElementById("from_dateId").value = date_from;
    document.getElementById("contractId").value = id;
}


//prüft, ob alle Eingaben korrekt sind
function checkform(){
    if ( document.getElementById("whowId").disabled == true) {
        document.getElementById("contractId").value = "";
        return true;
    }

    if (!isDate(document.getElementById("from_dateId").value, "yyyy-MM-dd")) {
        alert("von-Feld muss im Format yyyy-mm-dd ausgefuellt werden");
        return false;
    } else if (document.getElementById("to_dateId").value != "" && !isDate(document.getElementById("to_dateId").value, "yyyy-MM-dd")) {
        alert("bis-Feld muss entweder im Format yyyy-mm-dd ausgefuellt werden oder leer bleiben");
        return false;
    }

    return true;
}

