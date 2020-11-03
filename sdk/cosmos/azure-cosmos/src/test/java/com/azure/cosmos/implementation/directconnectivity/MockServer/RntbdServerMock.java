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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class RntbdServerMock {
    private final static Logger logger = LoggerFactory.getLogger(RntbdServerMock.class);
    private final int port;
   // private static EventExecutor requestExpirationExecutor = new DefaultEventExecutor();
   private static final String STOREPASS = "tutorial123";


    public RntbdServerMock(int port) {
        this.port = port;
    }

    public void start() throws InterruptedException, CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {

        final ClassLoader classloader = RntbdServerMock.class.getClassLoader();
        final InputStream inputStream = classloader.getResourceAsStream("server.jks");

        final KeyStore trustStore = KeyStore.getInstance("jks");
        trustStore.load(inputStream, STOREPASS.toCharArray());

        final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(trustStore, STOREPASS.toCharArray());

        final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);

        SslContext sslContext = SslContextBuilder.forServer(keyManagerFactory).trustManager(trustManagerFactory).build();

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
                            new RntbdContextRequestDecoder(),
                            new RntbdContextEncoder(),
                            new RntbdServerRequestManager()
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
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.SO_LINGER, 0);

            ChannelFuture channelFuture = bootstrap.bind(port).sync();

            System.err.printf("%s started and listening for connections on %s",
                RntbdServerMock.class.getSimpleName(),
                channelFuture.channel().localAddress()
            );

            channelFuture.channel().closeFuture().sync();
            ChannelFuture closefuture = channelFuture.channel().closeFuture();
            closefuture.sync();
            closefuture.addListener(future -> {
                if (future.isSuccess()) {
                    logger.info("channel got closed");
                } else {
                    logger.info(future.cause().toString());
                }
            });
        } finally {
            parent.shutdownGracefully().sync();
            child.shutdownGracefully().sync();
        }
    }
}
