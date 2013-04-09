//prüft, ob alle Eingaben korrekt sind
function checkform(){
	if(document.getElementById("actId").value == "anzeigen" && 
	   document.getElementById("uId").value == null){
		return false;
	}

	return true;
}
