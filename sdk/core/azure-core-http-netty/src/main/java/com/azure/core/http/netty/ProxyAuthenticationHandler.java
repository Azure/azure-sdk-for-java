package com.azure.core.http.netty;

import com.azure.core.http.AuthorizationChallengeHandler;
import io.netty.channel.ChannelHandlerContext;

import java.net.SocketAddress;

public class ProxyAuthenticationHandler extends io.netty.handler.proxy.ProxyHandler {
    private final AuthorizationChallengeHandler challengeHandler;

    ProxyAuthenticationHandler(SocketAddress proxyAddress, String username, String password) {
        super(proxyAddress);
        this.challengeHandler = new AuthorizationChallengeHandler(username, password);
    }

    @Override
    public String protocol() {
        return null;
    }

    @Override
    public String authScheme() {
        return null;
    }

    @Override
    protected void addCodec(ChannelHandlerContext channelHandlerContext) throws Exception {

    }

    @Override
    protected void removeEncoder(ChannelHandlerContext channelHandlerContext) throws Exception {

    }

    @Override
    protected void removeDecoder(ChannelHandlerContext channelHandlerContext) throws Exception {

    }

    @Override
    protected Object newInitialMessage(ChannelHandlerContext channelHandlerContext) throws Exception {
        return null;
    }

    @Override
    protected boolean handleResponse(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        return false;
    }
}
