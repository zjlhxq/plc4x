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
package org.apache.plc4x.java.luxtronik2.protocol;

import org.apache.plc4x.java.luxtronik2.configuration.Luxtronik2Configuration;
import org.apache.plc4x.java.luxtronik2.message.LoginMessage;
import org.apache.plc4x.java.luxtronik2.message.Luxtronik2Message;
import org.apache.plc4x.java.spi.ConversationContext;
import org.apache.plc4x.java.spi.Plc4xProtocolBase;
import org.apache.plc4x.java.spi.configuration.HasConfiguration;

public class Luxtronik2ProtocolLogic extends Plc4xProtocolBase<Luxtronik2Message> implements HasConfiguration<Luxtronik2Configuration> {

    private Luxtronik2Configuration configuration;

    @Override
    public void setConfiguration(Luxtronik2Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void onConnect(ConversationContext<Luxtronik2Message> context) {
        context.sendToWire(new LoginMessage("999999"));
    }

    @Override
    public void close(ConversationContext<Luxtronik2Message> context) {

    }

    @Override
    protected void decode(ConversationContext<Luxtronik2Message> context, Luxtronik2Message msg) throws Exception {
        super.decode(context, msg);
    }

}
