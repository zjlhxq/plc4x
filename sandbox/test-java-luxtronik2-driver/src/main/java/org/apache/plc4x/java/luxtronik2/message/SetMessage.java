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

import org.apache.plc4x.java.api.value.PlcValue;
import org.apache.plc4x.java.luxtronik2.model.Operation;

public class SetMessage extends Luxtronik2Message {

    private final String address;
    private final String value;

    public SetMessage(String address, String value) {
        this.address = address;
        this.value = value;
    }

    @Override
    public Operation getOperation() {
        return Operation.SET;
    }

    public String getAddress() {
        return address;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int getLengthInBytes() {
        return 5 + address.length() + value.length();
    }

    @Override
    public PlcValue toPlcValue() {
        return null;
    }

}
