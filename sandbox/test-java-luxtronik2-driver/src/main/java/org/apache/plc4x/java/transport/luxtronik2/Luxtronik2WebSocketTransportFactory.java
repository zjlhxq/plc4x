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
package org.apache.plc4x.java.transport.luxtronik2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.NoOriginWebSocketClientHandshaker13;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import org.apache.plc4x.java.api.exceptions.PlcRuntimeException;
import org.apache.plc4x.java.transport.tcp.TcpChannelFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

public class Luxtronik2WebSocketTransportFactory extends TcpChannelFactory {

    private URI connectionUri;

    public Luxtronik2WebSocketTransportFactory(SocketAddress address) {
        super(address);
        InetSocketAddress socketAddress = (InetSocketAddress) address;
        try {
            connectionUri = new URI("ws", null, socketAddress.getHostName(), socketAddress.getPort(), "/", null, null);
        } catch (URISyntaxException e) {
            throw new PlcRuntimeException(e);
        }
    }

    @Override
    public void initializePipeline(ChannelPipeline pipeline) {
        final Luxtronik2WebSocketClientHandler handler =
            new Luxtronik2WebSocketClientHandler(
                new NoOriginWebSocketClientHandshaker13(
                    connectionUri, WebSocketVersion.V13, "Lux_WS", true, HttpHeaders.EMPTY_HEADERS, 1280000));

        pipeline.addLast("http-codec", new HttpClientCodec());
        pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
        pipeline.addLast("web-socket-handler", handler);
        pipeline.addLast("wrapper", new ChannelDuplexHandler() {
            @Override
            public void read(ChannelHandlerContext ctx) throws Exception {
                super.read(ctx);
            }

            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                if (msg instanceof ByteBuf) {
                    ByteBuf byteBuf = (ByteBuf) msg;
                    byte[] bytes = new byte[byteBuf.readableBytes()];
                    byteBuf.readBytes(bytes);
                    ctx.write(new TextWebSocketFrame(new String(bytes)), promise);
                } else {
                    ctx.write(msg, promise);
                }
            }
        });
    }

}
