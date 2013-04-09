//aktiviert bzw. deaktiviert die angezeigten Steuerelemente
function deActivateInputs(disable){
	document.getElementById("nameId").disabled = disable;
	document.getElementById("descId").disabled = disable;
}


//der ausgewählte Datensatz wird in die Steuerelemente eingetragen
function rowSelect(id, name, desc){
	deActivateInputs(false);
	if (desc == "null"){
		desc = "";
	}
	document.getElementById("nameId").value = name;
	document.getElementById("descId").value = desc;
	document.getElementById("actionId").value = id;
}



//prüft, ob alle Eingaben korrekt sind
function checkform(){
	if(document.getElementById("nameId").disabled == true){
		return false;
	}
	
	if(document.getElementById("nameId").value == ""){
		alert("Aktionsname darf nicht leer sein");
		return false;
	}
	return true;
}

