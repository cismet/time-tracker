//Variablen des History Stacks
function stack(length) {
	this.stack = new Array(length);
	this.stackPointer = -1;


	this.init = function(length){
		stack = new Array(length);
	};

	this.insert = function(object){
		if ((this.stackPointer + 1) == this.stack.length){
			for (var i = 1; i < this.stack.length; ++i){
				this.stack[i - 1] = this.stack[i];
			}
			this.stack[this.stackPointer] = object;
		}else{
			this.stack[this.stackPointer + 1] = object;
			this.stackPointer  += 1;
		}
	};

	this.get = function(){
		if(this.stackPointer == -1){
			return false;
		}else{
			var obj = this.stack[this.stackPointer];
			this.stackPointer -= 1;
			return obj;
		}
	};
	
	
	this.clear = function(){
		this.stackPointer = -1;
	}
	
	this.isEmpty = function(){
		return (this.stackPointer == -1);
	}
	
	this.replaceId = function(oldId, newId){
		for (var i = 0; i <= this.stackPointer; ++i){
			if (this.stack[i]['id'] == oldId){
				this.stack[i]['id'] = newId;
			}
		}
	}
	
}