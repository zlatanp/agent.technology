function send() {
	var text = $('#tekst').val();

	if (text == "") {
		message('Please enter a message\n');
		return;
	}
	try {
		socket.send(text);
		message('I have sent:' + text);
	} catch (exception) {
		message('Error: ' + exception + "\n");
	}
}
function message(msg) {
	console.log(msg);
	$('#msgs').append(msg + "\n");
}

if (!("WebSocket" in window)) {
	$('#tekst, #send').fadeOut("fast");
	$('Oh no, you need a browser that supports WebSockets. How about <a href="http://www.google.com/chrome">Google/a>?')
			.appendTo('#msgs');
} else {
	var host = "ws://localhost:8080/TestREST/websocket";
	try {
		socket = new WebSocket(host);
		message('connect. Socket Status: ' + socket.readyState + "\n");

		socket.onopen = function() {
			message('onopen. Socket Status: ' + socket.readyState
					+ ' (open)\n');
		}

		socket.onmessage = function(msg) {
			message('onmessage. Received: ' + msg.data + "\n");
		}

		socket.onclose = function() {
			message('onclose. Socket Status: ' + socket.readyState
					+ ' (Closed)\n');
			socket = null;
		}

	} catch (exception) {
		message('Error' + exception + "\n");
	}
}
$('#send').click(function() {
	send();
});