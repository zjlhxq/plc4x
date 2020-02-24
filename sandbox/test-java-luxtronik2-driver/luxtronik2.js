//
//  Licensed to the Apache Software Foundation (ASF) under one or more
//  contributor license agreements.  See the NOTICE file distributed with
//  this work for additional information regarding copyright ownership.
//  The ASF licenses this file to You under the Apache License, Version 2.0
//  (the "License"); you may not use this file except in compliance with
//  the License.  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//

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


