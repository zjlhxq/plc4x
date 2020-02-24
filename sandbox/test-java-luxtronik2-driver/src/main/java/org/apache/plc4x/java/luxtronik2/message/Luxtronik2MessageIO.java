/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/
package org.apache.plc4x.java.luxtronik2.message;

import org.apache.plc4x.java.spi.generation.MessageIO;
import org.apache.plc4x.java.spi.generation.ParseException;
import org.apache.plc4x.java.spi.generation.ReadBuffer;
import org.apache.plc4x.java.spi.generation.WriteBuffer;

public class Luxtronik2MessageIO implements MessageIO<Luxtronik2Message, Luxtronik2Message> {

    @Override
    public Luxtronik2Message parse(ReadBuffer io, Object... args) throws ParseException {
        return null;
    }

    @Override
    public void serialize(WriteBuffer io, Luxtronik2Message value, Object... args) throws ParseException {
        if(value instanceof LoginMessage) {
            LoginMessage loginMessage = (LoginMessage) value;
            writeString(io, "LOGIN;" + loginMessage.getPassword());
        } else if(value instanceof RefreshMessage) {
            writeString(io, "REFRESH");
        } else if(value instanceof GetMessage) {
            GetMessage getMessage = (GetMessage) value;
            writeString(io, "GET;" + getMessage.getAddress());
        } else if(value instanceof SetMessage) {
            SetMessage setMessage = (SetMessage) value;
            writeString(io, "SET;" + setMessage.getAddress() + ";" + setMessage.getValue());
        } else if(value instanceof SaveMessage) {
            SaveMessage saveMessage = (SaveMessage) value;
            writeString(io, "SAVE;" + (saveMessage.getSave() ? "1" : "0"));
        }
    }

    protected void writeString(WriteBuffer io, String str) throws ParseException {
        io.writeString(str.length() * 8, "UTF-8", str);
    }

}
