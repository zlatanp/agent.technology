function f(x) {
	return x * x;
}

function Agent(agId) {
	this.id = agId;
	this.onMessage = function() {
		return "Agent id is: " + this.id;
	}
}

function getAgent(agId) {
	return new Agent(agId);
}