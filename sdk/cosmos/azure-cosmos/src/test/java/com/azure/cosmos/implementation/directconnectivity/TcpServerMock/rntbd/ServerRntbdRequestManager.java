// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.TcpServerMock.rntbd;

import com.azure.cosmos.implementation.directconnectivity.TcpServerMock.RequestResponseType;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.ReferenceCounted;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Processing incoming requests.
 */
public final class ServerRntbdRequestManager extends ChannelDuplexHandler {

    private final Queue<RequestResponseType> responseQueue;

    public ServerRntbdRequestManager() {
        super();
        this.responseQueue = new LinkedBlockingQueue<>();
    }

    @Override
    public void channelRead(final ChannelHandlerContext context, final Object message) {
        try{
            if (message instanceof ServerRntbdContextRequest) {
                ServerRntbdContextRequest request = (ServerRntbdContextRequest) message;

                // let us write something
                ServerRntbdContext response = new ServerRntbdContext(
                    request.getActivityId(),
                    HttpResponseStatus.OK,
                    "",
                    120L,
                    0,
                    25L,
                    "RntbdMockServer",
                    "1.0");

                context.channel().writeAndFlush(response);
            }
            else if (message instanceof ServerRntbdRequest) {
                RequestResponseType response = this.responseQueue.poll();
                // emulate the server close the channel
                if (response == RequestResponseType.CHANNEL_RST) {
                    context.channel().config().setOption(ChannelOption.SO_LINGER, 0);
                }
                context.channel().unsafe().closeForcibly();
            } else {
                // Can add more logic here to cover more scenarios.
                context.fireChannelRead(message);
            }
        }
        finally {
            if (message instanceof ReferenceCounted) {
                ((ReferenceCounted) message).release();
            }
        }
    }

    public void injectServerResponse(RequestResponseType requestResponseType) {
        this.responseQueue.add(requestResponseType);
    }
}
