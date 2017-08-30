package com.microsoft.rest.v2.http;

import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoop;
import io.netty.util.concurrent.EventExecutorGroup;
import rx.functions.Func0;

public class ChannelHandlerConfig {
    private final Func0<ChannelHandler> factory;
    public Func0<ChannelHandler> factory() { return factory; }

    private final boolean mayBlock;
    /**
     * Indicates whether the handler returned by the factory may block the thread running its methods.
     * If set to true, the handler will be run on a separate {@link EventExecutorGroup} from the channel's {@link EventLoop}.
     */
    public boolean mayBlock() { return mayBlock; }

    public ChannelHandlerConfig(Func0<ChannelHandler> factory, boolean mayBlock) {
        this.factory = factory;
        this.mayBlock = mayBlock;
    }
}
