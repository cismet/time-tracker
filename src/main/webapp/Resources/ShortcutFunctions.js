//aktiviert bzw. deaktiviert die angezeigten Steuerelemente
function deActivateInputs(disable){
    document.getElementById("nameId").disabled = disable;
}


//der ausgewählte Datensatz wird in die Steuerelemente eingetragen
function rowSelect(id, shortcut){
    deActivateInputs(false);
    if(shortcut != "delete" && id != "new"){
            document.getElementById("nameId").value = shortcut;
    }else if(id == "new"){
            document.getElementById("nameId").value = "";
    }
    document.getElementById("shortcutId").value = id;
}




//prüft, ob alle Eingaben korrekt sind
function checkform(){
    if(document.getElementById("actId").value == "uebernehmen" && 
       document.getElementById("nameId").disabled == true){
            return false;
    }

    if(document.getElementById("actId").value == "uebernehmen" && 
       document.getElementById("nameId").value == ""){
            alert("Feld Name darf nicht leer sein");
            return false;
    }
    return true;
}




//speichert die aktuelle Aktion
function setActionText(text, id){
    document.getElementById("actId").value = text;
    if(text == "delete"){
            document.getElementById("shortcutId").value = id;	
    }
}
