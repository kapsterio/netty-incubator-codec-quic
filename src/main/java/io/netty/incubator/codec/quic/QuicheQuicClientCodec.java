/*
 * Copyright 2020 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.incubator.codec.quic;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

/**
 * {@link QuicheQuicCodec} for QUIC clients.
 */
final class QuicheQuicClientCodec extends QuicheQuicCodec {

    QuicheQuicClientCodec(QuicheConfig config) {
        // Let's just use Quic.MAX_DATAGRAM_SIZE as the maximum size for a token on the client side. This should be
        // safe enough and as we not have too many codecs at the same time this should be ok.
        super(config, Quic.MAX_DATAGRAM_SIZE);
    }

    @Override
    protected QuicheQuicChannel quicPacketRead(
            ChannelHandlerContext ctx, InetSocketAddress sender, InetSocketAddress recipient,
            byte type, int version, ByteBuf scid, ByteBuf dcid,
            ByteBuf token) {
        ByteBuffer key = dcid.internalNioBuffer(dcid.readerIndex(), dcid.readableBytes());
        return getChannel(key);
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress,
                        SocketAddress localAddress, ChannelPromise promise) {
        final QuicheQuicChannel channel;
        try {
            channel = QuicheQuicChannel.handleConnect(remoteAddress, nativeConfig);
        } catch (Exception e) {
            promise.setFailure(e);
            return;
        }
        if (channel != null) {
            putChannel(channel);
            channel.finishConnect();
            promise.setSuccess();
            return;
        }
        ctx.connect(remoteAddress, localAddress, promise);
    }
}
