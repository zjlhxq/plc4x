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
package io.netty.handler.codec.http.websocketx;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.net.URI;

/**
 * Customized version of the original handshaker however with removed 'origin' header and with header names which are
 * not just lower-case, as the web-server of the Luxtronik device seems to be quite restrictive.
 */
public class NoOriginWebSocketClientHandshaker13 extends WebSocketClientHandshaker13 {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(NoOriginWebSocketClientHandshaker13.class);

    private String expectedChallengeResponseString;

    public NoOriginWebSocketClientHandshaker13(URI webSocketURL, WebSocketVersion version, String subprotocol, boolean allowExtensions, HttpHeaders customHeaders, int maxFramePayloadLength) {
        super(webSocketURL, version, subprotocol, allowExtensions, customHeaders, maxFramePayloadLength);
    }

    @Override
    protected FullHttpRequest newHandshakeRequest() {
        URI wsURL = uri();

        // Get 16 bit nonce and base 64 encode it
        byte[] nonce = WebSocketUtil.randomBytes(16);
        String key = WebSocketUtil.base64(nonce);

        String acceptSeed = key + MAGIC_GUID;
        byte[] sha1 = WebSocketUtil.sha1(acceptSeed.getBytes(CharsetUtil.US_ASCII));
        expectedChallengeResponseString = WebSocketUtil.base64(sha1);

        if (logger.isDebugEnabled()) {
            logger.debug(
                "Luxtronik 2.0 WebSocket version 13 client handshake key: {}, expected response: {}",
                key, expectedChallengeResponseString);
        }

        // Format request
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, upgradeUrl(wsURL),
            Unpooled.EMPTY_BUFFER);
        HttpHeaders headers = request.headers();

        headers.set(AsciiString.cached("Upgrade"), HttpHeaderValues.WEBSOCKET)
            .set(AsciiString.cached("Connection"), HttpHeaderValues.UPGRADE)
            .set(AsciiString.cached("Host"), websocketHostValue(wsURL))
            .set(AsciiString.cached("Sec-WebSocket-Protocol"), "Lux_WS")
            .set(AsciiString.cached("Sec-WebSocket-Version"), "13")
            .set(AsciiString.cached("Sec-WebSocket-Key"), key)
            .set(AsciiString.cached("Sec-WebSocket-Extensions"), "permessage-deflate; client_max_window_bits");

        return request;
    }

    @Override
    protected void verify(FullHttpResponse response) {
        final HttpResponseStatus status = HttpResponseStatus.SWITCHING_PROTOCOLS;
        final HttpHeaders headers = response.headers();

        if (!response.status().equals(status)) {
            throw new WebSocketHandshakeException("Invalid handshake response getStatus: " + response.status());
        }

        CharSequence upgrade = headers.get(HttpHeaderNames.UPGRADE);
        if (!HttpHeaderValues.WEBSOCKET.contentEqualsIgnoreCase(upgrade)) {
            throw new WebSocketHandshakeException("Invalid handshake response upgrade: " + upgrade);
        }

        if (!headers.containsValue(HttpHeaderNames.CONNECTION, HttpHeaderValues.UPGRADE, true)) {
            throw new WebSocketHandshakeException("Invalid handshake response connection: "
                + headers.get(HttpHeaderNames.CONNECTION));
        }

        CharSequence accept = headers.get(HttpHeaderNames.SEC_WEBSOCKET_ACCEPT);
        if (accept == null || !accept.equals(expectedChallengeResponseString)) {
            throw new WebSocketHandshakeException(String.format(
                "Invalid challenge. Actual: %s. Expected: %s", accept, expectedChallengeResponseString));
        }
    }

}
