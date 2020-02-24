const WebSocket = require('ws');
const webSocket = new WebSocket('ws://192.168.42.15:8214', 'Lux_WS');
// Login
webSocket.send('LOGIN;999999');
// Get a list of all available options and their addresses
webSocket.send('REFRESH');
// Get an individual address
webSocket.send('GET;0x28f928');
// Set a particular address to a given value
// TIP: in order to know what value to set a value to, execute GET for the address it will contain a list of options.
webSocket.send('SET;0x28f928;' + value);
// Save the change
webSocket.send('SAVE;1');


