package com.azure.cosmos.implementation.directconnectivity.MockServer;

import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdContext;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdContextRequest;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequest;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.ReferenceCounted;

public final class RntbdServerRequestManager extends ChannelDuplexHandler {

    @Override
    public void channelRead(final ChannelHandlerContext context, final Object message) {
        try{
            if (message instanceof RntbdContextRequest) {
                RntbdContextRequest request = (RntbdContextRequest) message;

                // let us write something
                RntbdContext response = new RntbdContext(
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
            else if (message instanceof RntbdRequest) {
                // emulate the server close the channel
                context.channel().unsafe().closeForcibly();
            } else {
                context.fireChannelRead(message);
            }
        }
        finally {
            if (message instanceof ReferenceCounted) {
                ((ReferenceCounted) message).release();
            }
        }

    }
}
