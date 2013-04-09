//aktiviert bzw. deaktiviert die angezeigten Steuerelemente
function deActivateInputs(disable){
    document.getElementById("nameId").disabled = disable;
    document.getElementById("aktivId").disabled = disable;
    document.getElementById("mpId").disabled = disable;
    document.getElementById("shortcutId").disabled = disable;
}


//wird aufgerufen, wenn neue Daten angelegt werden sollen
function newData(){
    document.getElementById("nameId").value = "";
    document.getElementById("aktivId").value = "";
    document.getElementById("rowId").value = "new";
    document.getElementById("mpId").value = "";
    document.getElementById("shortcutId").value = "";
    deActivateInputs(false);
}


//der ausgewählte Datensatz wird in die Steuerelemente eingetragen
function projectSelect(){
    var id = document.getElementById("pId").value.split(" ");
    var inputId = -1;
    var title;
    var isSp;
    var isActivated;
    var mainProjectId;
    var i = 0;
    var mainProject
    var shortcut;

    if(id != ""){
        //sucht den ausgewählten Datensatz in den versteckten Input-Feldern
        while(inputId != id[0]){
            var parts = document.getElementsByName("HiddenData")[i].value.split(";");
            inputId = parts[0];
            title = parts[1];
            isSp = parts[2];
            mainProjectId = parts[3];
            isActivated = parts[4];
            shortcut = parts[5];
            ++i;
        }

        //Suche Titel des Hauptprojektes
        if(mainProjectId != "null"){
            var tmpId = -1;
            var tmpTitle;
            i = 0;
            while(tmpId != mainProjectId && i < document.getElementsByName("HiddenData").length){
                var parts = document.getElementsByName("HiddenData")[i].value.split(";");
                tmpId = parts[0];
                tmpTitle = parts[1];
                ++i;
            }
            if(tmpId == mainProjectId){
                mainProject = tmpId.concat(" - ").concat(tmpTitle);
            }else{		//zur gegebenen Projekt-ID existiert kein Titel
                mainProject = mainProjectId.concat(" - unbekannt");
            }
        }else{
            mainProject = "";
        }


        if(isActivated == "t"){
            document.getElementById("aktivId").checked = true;
        }else{
            document.getElementById("aktivId").checked = false;			
        }

        if(shortcut == "null"){
            shortcut = "";
        }

        deActivateInputs(false);
        document.getElementById("nameId").value = title;
        document.getElementById("rowId").value = inputId;
        document.getElementById("mpId").value = mainProject;
        document.getElementById("shortcutId").value = shortcut;


        //mainProjectId enthält eine Id, die nicht existiert
        if(document.getElementById("mpId").value != mainProject){
            var optLength = document.getElementById("mpId").options.length;
            document.getElementById("mpId").options[optLength] = new Option(mainProject, mainProject);
            document.getElementById("mpId").value = mainProject;
        }

    }	
}



//speichert die aktuelle Aktion
function setActionText(val){
    document.getElementById("actId").value = val;
}


//prüft, ob alle Eingaben korrekt sind
function checkform(){
    if(document.getElementById("actId").value == "uebernehmen" && 
       document.getElementById("nameId").disabled == true){
        return false;
    }else{
        return true;
    }
}