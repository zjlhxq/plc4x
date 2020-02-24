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
package org.apache.plc4x.java.luxtronik2;

import org.apache.plc4x.java.luxtronik2.configuration.Luxtronik2Configuration;
import org.apache.plc4x.java.luxtronik2.field.Luxtronik2FieldHandler;
import org.apache.plc4x.java.luxtronik2.message.Luxtronik2Message;
import org.apache.plc4x.java.luxtronik2.message.Luxtronik2MessageIO;
import org.apache.plc4x.java.luxtronik2.protocol.Luxtronik2ProtocolLogic;
import org.apache.plc4x.java.spi.configuration.Configuration;
import org.apache.plc4x.java.spi.connection.GeneratedDriverBase;
import org.apache.plc4x.java.spi.connection.PlcFieldHandler;
import org.apache.plc4x.java.spi.connection.ProtocolStackConfigurer;
import org.apache.plc4x.java.spi.connection.SingleProtocolStackConfigurer;

public class Luxtronik2Driver extends GeneratedDriverBase<Luxtronik2Message> {

    @Override
    public String getProtocolCode() {
        return "lt2";
    }

    @Override
    public String getProtocolName() {
        return "Luxtronik 2.0 Driver";
    }

    @Override
    protected Class<? extends Configuration> getConfigurationType() {
        return Luxtronik2Configuration.class;
    }

    @Override
    protected String getDefaultTransport() {
        return "lt2ws";
    }

    @Override
    protected PlcFieldHandler getFieldHandler() {
        return new Luxtronik2FieldHandler();
    }

    @Override
    protected ProtocolStackConfigurer<Luxtronik2Message> getStackConfigurer() {
        return SingleProtocolStackConfigurer.builder(Luxtronik2Message.class, Luxtronik2MessageIO.class)
            .withProtocol(Luxtronik2ProtocolLogic.class)
            .build();
    }

}
