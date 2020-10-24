package com.azure.cosmos.implementation.directconnectivity.MockServer;

import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdContextRequestDecoder;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestDecoder;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestFramer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

public class MockServerMain {
    private final static Logger logger = LoggerFactory.getLogger(RntbdServerMock.class);
    private final int port;
    // private static EventExecutor requestExpirationExecutor = new DefaultEventExecutor();

    public MockServerMain(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws InterruptedException, SSLException, CertificateException {
        MockServerMain server = new MockServerMain(8082);
        server.start();
    }

    public void start() throws InterruptedException, CertificateException, SSLException {
        SelfSignedCertificate testCertificate  = new SelfSignedCertificate();
        SslContext sslContext = SslContextBuilder.forServer(testCertificate.certificate(), testCertificate.privateKey()).build();

        EventLoopGroup parent = new NioEventLoopGroup();
        EventLoopGroup child = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();

            bootstrap.group(parent, child)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel channel) throws Exception {

                        SSLEngine engine = sslContext.newEngine(channel.alloc());
                        engine.setUseClientMode(false);

                        ChannelPipeline pipeline = channel.pipeline();

                        pipeline.addLast(
                            new SslHandler(engine),
                            new RntbdRequestFramer(),
                            new RntbdRequestDecoder(),
                            new RntbdContextRequestDecoder()
                        );

                        LogLevel logLevel = null;

                        if (logger.isTraceEnabled()) {
                            logLevel = LogLevel.TRACE;
                        } else if (logger.isDebugEnabled()) {
                            logLevel = LogLevel.DEBUG;
                        }

                        if (logLevel != null) {
                            pipeline.addFirst(new LoggingHandler(logLevel));
                        }
                    }
                })
                .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture channelFuture = bootstrap.bind(port).sync();

            System.err.printf("%s started and listening for connections on %s",
                RntbdServerMock.class.getSimpleName(),
                channelFuture.channel().localAddress()
            );

            channelFuture.channel().closeFuture().sync();
        } finally {
            parent.shutdownGracefully().sync();
            child.shutdownGracefully().sync();
        }
    }
}
