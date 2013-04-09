//aktiviert bzw. deaktiviert die angezeigten Steuerelemente
function deActivateInputs(disable) {
    document.getElementById("nameId").disabled = disable;
    document.getElementById("pwdId").disabled = disable;
    document.getElementById("pwdWhId").disabled = disable;
    document.getElementById("buddynameId").disabled = disable;
    document.getElementById("companyId").disabled = disable;
    document.getElementById("exactHolidaysId").disabled = disable;
    document.getElementById("netHoursOfWorkId").disabled = disable;
}


//wird aufgerufen, wenn neue Daten angelegt werden sollen
function newData() {
    document.getElementById("nameId").value = "";
    document.getElementById("pwdId").value = "";
    document.getElementById("pwdWhId").value = "";
    document.getElementById("buddynameId").value = "";
    document.getElementById("companyId").value = "";
    document.getElementById("netHoursOfWorkId").checked = false;
    document.getElementById("exactHolidaysId").checked = false;
    document.getElementById("userId").value = "new";
    deActivateInputs(false);
}


//der ausgewählte Datensatz wird in die Steuerelemente eingetragen
function userSelect() {
    var id = document.getElementById("uId").value.split(" ");
    var inputId = -1;
    var name;
    var pwd;
    var buddy;
    var company;
    var exactHolidays;
    var netHoursOfWork;
    var i = 0;

    if(id != ""){
        while(inputId != id[0]){
            var parts = document.getElementsByName("HiddenData")[i].value.split(";");
            inputId = parts[0];
            name = parts[1];
            pwd = parts[2];
            buddy = parts[3];
            company = parts[4];
            exactHolidays = parts[5];
            netHoursOfWork = parts[6];
            ++i;
        }

        deActivateInputs(false);
        document.getElementById("nameId").value = name;
        document.getElementById("userId").value = inputId;
        document.getElementById("pwdId").value = pwd;
        document.getElementById("pwdWhId").value = pwd;
        document.getElementById("buddynameId").value = buddy;
        document.getElementById("companyId").value = company;
       // document.getElementById("exactHolidays").value = exactHolidays;
        
        if(exactHolidays == "t"){
            document.getElementById("exactHolidaysId").checked = true;
        }else{
            document.getElementById("exactHolidaysId").checked = false;			
        }

        if(netHoursOfWork == "t"){
            document.getElementById("netHoursOfWorkId").checked = true;
        }else{
            document.getElementById("netHoursOfWorkId").checked = false;			
        }
    }	
}


//prüft, ob alle Eingaben korrekt sind
function checkform() {
    if ( document.getElementById("buddynameId").disabled == true ) {
        return false;
    }


    if (document.getElementById("buddynameId").value == ""){
        alert("Feld Buddyname darf nicht leer sein!");
        return false;
    }

    if (document.getElementById("pwdId").value == ""){
        alert("Feld Passwort darf nicht leer sein!");
        return false;
    }

    if (document.getElementById("pwdId").value != document.getElementById("pwdWhId").value){
        alert("Das Passwort und die Passwortwiederholung stimmen nicht ueberein!");
        return false;
    }

    return true;
}
