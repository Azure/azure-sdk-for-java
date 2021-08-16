// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation.graalvm.features;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.RecomputeFieldValue;
import com.oracle.svm.core.annotate.RecomputeFieldValue.Kind;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import com.oracle.svm.core.jdk.JDK11OrLater;
import com.oracle.svm.core.jdk.JDK8OrEarlier;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.compression.ZlibCodecFactory;
import io.netty.handler.codec.compression.ZlibWrapper;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectorFailureBehavior;
import io.netty.handler.ssl.CipherSuiteFilter;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextOption;
import io.netty.handler.ssl.SslProvider;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.JdkLoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;

import static io.netty.handler.codec.http.HttpHeaderValues.DEFLATE;
import static io.netty.handler.codec.http.HttpHeaderValues.GZIP;
import static io.netty.handler.codec.http.HttpHeaderValues.X_DEFLATE;
import static io.netty.handler.codec.http.HttpHeaderValues.X_GZIP;

@TargetClass(
    className = "io.netty.util.internal.logging.InternalLoggerFactory",
    onlyWith = Target_io_netty_util_internal_logging_InternalLoggerFactory.OnlyIfInClassPath.class)
final class Target_io_netty_util_internal_logging_InternalLoggerFactory {
    @Substitute
    private static InternalLoggerFactory newDefaultFactory(String name) {
        return JdkLoggerFactory.INSTANCE;
    }

    static class OnlyIfInClassPath implements BooleanSupplier {
        @Override
        public boolean getAsBoolean() {
            try {
                Class.forName(
                    "io.netty.util.internal.logging.InternalLoggerFactory",
                    false,
                    Thread.currentThread().getContextClassLoader());
                return true;
            } catch (ClassNotFoundException e) {
                return false;
            }
        }
    }
}

//*********************************************************
// Taken from
// https://github.com/quarkusio/quarkus/blob/main/extensions/netty/runtime/src/main/java/io/quarkus/netty/runtime/graal/NettySubstitutions.java
//*********************************************************


// SSL
// This whole section is mostly about removing static analysis references to openssl/tcnative

@TargetClass(className = "io.netty.handler.ssl.SslProvider")
final class Target_io_netty_handler_ssl_SslProvider {
    @Substitute
    public static boolean isAlpnSupported(final SslProvider provider) {
        switch (provider) {
            case JDK:
                return Target_io_netty_handler_ssl_JdkAlpnApplicationProtocolNegotiator.isAlpnSupported();
            case OPENSSL:
            case OPENSSL_REFCNT:
                return false;
            default:
                throw new Error("SslProvider unsupported " + provider);
        }
    }
}

@TargetClass(className = "io.netty.handler.ssl.JdkAlpnApplicationProtocolNegotiator",
            onlyWith = Target_io_netty_handler_ssl_JdkAlpnApplicationProtocolNegotiator.OnlyIfInClassPath.class)
final class Target_io_netty_handler_ssl_JdkAlpnApplicationProtocolNegotiator {
    @Alias
    static boolean isAlpnSupported() {
        return true;
    }

    static class OnlyIfInClassPath implements BooleanSupplier {
        @Override
        public boolean getAsBoolean() {
            try {
                Class.forName(
                        "io.netty.handler.ssl.JdkAlpnApplicationProtocolNegotiator",
                        false,
                        Thread.currentThread().getContextClassLoader());
                return true;
            } catch (ClassNotFoundException e) {
                return false;
            }
        }
    }
}

/**
 * Hardcode io.netty.handler.ssl.OpenSsl as non-available
 */
@TargetClass(className = "io.netty.handler.ssl.OpenSsl")
final class Target_io_netty_handler_ssl_OpenSsl {

    @Alias
    @RecomputeFieldValue(kind = Kind.FromAlias)
    private static Throwable UNAVAILABILITY_CAUSE = new RuntimeException("OpenSsl unsupported");

    @Alias
    @RecomputeFieldValue(kind = Kind.FromAlias)
    static List<String> DEFAULT_CIPHERS = Collections.emptyList();

    @Alias
    @RecomputeFieldValue(kind = Kind.FromAlias)
    static Set<String> AVAILABLE_CIPHER_SUITES = Collections.emptySet();

    @Alias
    @RecomputeFieldValue(kind = Kind.FromAlias)
    private static Set<String> AVAILABLE_OPENSSL_CIPHER_SUITES = Collections.emptySet();

    @Alias
    @RecomputeFieldValue(kind = Kind.FromAlias)
    private static Set<String> AVAILABLE_JAVA_CIPHER_SUITES = Collections.emptySet();

    @Alias
    @RecomputeFieldValue(kind = Kind.FromAlias)
    private static boolean SUPPORTS_KEYMANAGER_FACTORY = false;

    @Alias
    @RecomputeFieldValue(kind = Kind.FromAlias)
    private static boolean SUPPORTS_OCSP = false;

    @Alias
    @RecomputeFieldValue(kind = Kind.FromAlias)
    static Set<String> SUPPORTED_PROTOCOLS_SET = Collections.emptySet();

    @Substitute
    public static boolean isAvailable() {
        return false;
    }

    @Substitute
    public static int version() {
        return -1;
    }

    @Substitute
    public static String versionString() {
        return null;
    }

    @Substitute
    public static boolean isCipherSuiteAvailable(String cipherSuite) {
        return false;
    }
}

@TargetClass(className = "io.netty.handler.ssl.JdkSslServerContext")
final class Target_io_netty_handler_ssl_JdkSslServerContext {

    @Alias
    Target_io_netty_handler_ssl_JdkSslServerContext(Provider provider,
                                                    X509Certificate[] trustCertCollection,
                                                    TrustManagerFactory trustManagerFactory,
                                                    X509Certificate[] keyCertChain,
                                                    PrivateKey key,
                                                    String keyPassword,
                                                    KeyManagerFactory keyManagerFactory,
                                                    Iterable<String> ciphers,
                                                    CipherSuiteFilter cipherFilter,
                                                    ApplicationProtocolConfig apn,
                                                    long sessionCacheSize,
                                                    long sessionTimeout,
                                                    ClientAuth clientAuth,
                                                    String[] protocols,
                                                    boolean startTls,
                                                    String keyStore)
        throws SSLException {
    }
}

@TargetClass(className = "io.netty.handler.ssl.JdkSslClientContext")
final class Target_io_netty_handler_ssl_JdkSslClientContext {

    @Alias
    Target_io_netty_handler_ssl_JdkSslClientContext(Provider sslContextProvider,
                                                    X509Certificate[] trustCertCollection,
                                                    TrustManagerFactory trustManagerFactory,
                                                    X509Certificate[] keyCertChain,
                                                    PrivateKey key,
                                                    String keyPassword,
                                                    KeyManagerFactory keyManagerFactory,
                                                    Iterable<String> ciphers,
                                                    CipherSuiteFilter cipherFilter,
                                                    ApplicationProtocolConfig apn,
                                                    String[] protocols,
                                                    long sessionCacheSize,
                                                    long sessionTimeout,
                                                    String keyStoreType)
        throws SSLException {
    }
}

@TargetClass(className = "io.netty.handler.ssl.SslHandler$SslEngineType")
final class Target_io_netty_handler_ssl_SslHandler$SslEngineType {

    @Alias
    public static Target_io_netty_handler_ssl_SslHandler$SslEngineType JDK;

    @Substitute
    static Target_io_netty_handler_ssl_SslHandler$SslEngineType forEngine(SSLEngine engine) {
        return JDK;
    }
}

@TargetClass(
    className = "io.netty.handler.ssl.JdkAlpnApplicationProtocolNegotiator$AlpnWrapper",
    onlyWith = JDK11OrLater.class)
final class Target_io_netty_handler_ssl_JdkAlpnApplicationProtocolNegotiator_AlpnWrapper {
    @Substitute
    public SSLEngine wrapSslEngine(SSLEngine engine,
                                   ByteBufAllocator alloc,
                                   io.netty.handler.ssl.JdkApplicationProtocolNegotiator applicationNegotiator,
                                   boolean isServer) {
        return (SSLEngine) (Object) new Target_io_netty_handler_ssl_JdkAlpnSslEngine(engine, applicationNegotiator,
            isServer);
    }

}

@TargetClass(
    className = "io.netty.handler.ssl.JdkAlpnApplicationProtocolNegotiator$AlpnWrapper",
    onlyWith = JDK8OrEarlier.class)
final class Target_io_netty_handler_ssl_JdkAlpnApplicationProtocolNegotiator_AlpnWrapperJava8 {
    @Substitute
    public SSLEngine wrapSslEngine(SSLEngine engine,
                                   ByteBufAllocator alloc,
                                   io.netty.handler.ssl.JdkApplicationProtocolNegotiator applicationNegotiator,
                                   boolean isServer) {
        if (Target_io_netty_handler_ssl_JettyAlpnSslEngine.isAvailable()) {
            return isServer ?
                   (SSLEngine) (Object) Target_io_netty_handler_ssl_JettyAlpnSslEngine.newServerEngine(engine, applicationNegotiator)
                   : (SSLEngine) (Object) Target_io_netty_handler_ssl_JettyAlpnSslEngine.newClientEngine(engine, applicationNegotiator);
        }
        throw new RuntimeException("Unable to wrap SSLEngine of type " + engine.getClass().getName());
    }

}

@TargetClass(
    className = "io.netty.handler.ssl.JettyAlpnSslEngine",
    onlyWith = JDK8OrEarlier.class)
final class Target_io_netty_handler_ssl_JettyAlpnSslEngine {
    @Substitute
    static boolean isAvailable() {
        return false;
    }

    @Substitute
    static Target_io_netty_handler_ssl_JettyAlpnSslEngine newClientEngine(
        SSLEngine engine, io.netty.handler.ssl.JdkApplicationProtocolNegotiator applicationNegotiator) {
        return null;
    }

    @Substitute
    static Target_io_netty_handler_ssl_JettyAlpnSslEngine newServerEngine(
        SSLEngine engine, io.netty.handler.ssl.JdkApplicationProtocolNegotiator applicationNegotiator) {
        return null;
    }
}

@TargetClass(
    className = "io.netty.handler.ssl.JdkAlpnSslEngine",
    onlyWith = JDK11OrLater.class)
final class Target_io_netty_handler_ssl_JdkAlpnSslEngine {
    @Alias
    Target_io_netty_handler_ssl_JdkAlpnSslEngine(
        final SSLEngine engine, final io.netty.handler.ssl.JdkApplicationProtocolNegotiator applicationNegotiator, final boolean isServer) {

    }
}

@TargetClass(className = "io.netty.handler.ssl.SslContext")
final class Target_io_netty_handler_ssl_SslContext {

    @Substitute
    @SafeVarargs
    @SuppressWarnings("varargs")
    static SslContext newServerContextInternal(SslProvider provider,
                                               Provider sslContextProvider,
                                               X509Certificate[] trustCertCollection,
                                               TrustManagerFactory trustManagerFactory,
                                               X509Certificate[] keyCertChain,
                                               PrivateKey key, String keyPassword,
                                               KeyManagerFactory keyManagerFactory,
                                               Iterable<String> ciphers,
                                               CipherSuiteFilter cipherFilter,
                                               ApplicationProtocolConfig apn,
                                               long sessionCacheSize,
                                               long sessionTimeout,
                                               ClientAuth clientAuth, String[] protocols,
                                               boolean startTls,
                                               boolean enableOcsp,
                                               String keyStoreType,
                                               Map.Entry<SslContextOption<?>, Object>... ctxOptions) throws SSLException {
        if (enableOcsp) {
            throw new IllegalArgumentException("OCSP is not supported with this SslProvider: " + provider);
        }
        return (SslContext) (Object) new Target_io_netty_handler_ssl_JdkSslServerContext(sslContextProvider,
            trustCertCollection, trustManagerFactory, keyCertChain, key, keyPassword,
            keyManagerFactory, ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout,
            clientAuth, protocols, startTls, keyStoreType);
    }

    @Substitute
    @SafeVarargs
    @SuppressWarnings("varargs")
    static SslContext newClientContextInternal(SslProvider provider,
                                               Provider sslContextProvider,
                                               X509Certificate[] trustCert,
                                               TrustManagerFactory trustManagerFactory,
                                               X509Certificate[] keyCertChain,
                                               PrivateKey key,
                                               String keyPassword,
                                               KeyManagerFactory keyManagerFactory,
                                               Iterable<String> ciphers,
                                               CipherSuiteFilter cipherFilter,
                                               ApplicationProtocolConfig apn,
                                               String[] protocols,
                                               long sessionCacheSize,
                                               long sessionTimeout,
                                               boolean enableOcsp,
                                               String keyStoreType,
                                               Map.Entry<SslContextOption<?>, Object>... options) throws SSLException {
        if (enableOcsp) {
            throw new IllegalArgumentException("OCSP is not supported with this SslProvider: " + provider);
        }
        return (SslContext) (Object) new Target_io_netty_handler_ssl_JdkSslClientContext(sslContextProvider,
            trustCert, trustManagerFactory, keyCertChain, key, keyPassword,
            keyManagerFactory, ciphers, cipherFilter, apn, protocols, sessionCacheSize,
            sessionTimeout, keyStoreType);
    }

}

@TargetClass(className = "io.netty.handler.ssl.JdkDefaultApplicationProtocolNegotiator")
final class Target_io_netty_handler_ssl_JdkDefaultApplicationProtocolNegotiator {

    @Alias
    public static Target_io_netty_handler_ssl_JdkDefaultApplicationProtocolNegotiator INSTANCE;
}

@TargetClass(className = "io.netty.handler.ssl.JdkSslContext")
@SuppressWarnings("deprecation")
final class Target_io_netty_handler_ssl_JdkSslContext {

    @Substitute
    static io.netty.handler.ssl.JdkApplicationProtocolNegotiator toNegotiator(ApplicationProtocolConfig config, boolean isServer) {
        if (config == null) {
            return (io.netty.handler.ssl.JdkApplicationProtocolNegotiator) (Object) Target_io_netty_handler_ssl_JdkDefaultApplicationProtocolNegotiator.INSTANCE;
        }

        switch (config.protocol()) {
            case NONE:
                return (io.netty.handler.ssl.JdkApplicationProtocolNegotiator) (Object) Target_io_netty_handler_ssl_JdkDefaultApplicationProtocolNegotiator.INSTANCE;
            case ALPN:
                if (isServer) {
                    SelectorFailureBehavior behavior = config.selectorFailureBehavior();
                    if (behavior == SelectorFailureBehavior.FATAL_ALERT) {
                        return new io.netty.handler.ssl.JdkAlpnApplicationProtocolNegotiator(true, config.supportedProtocols());
                    } else if (behavior == SelectorFailureBehavior.NO_ADVERTISE) {
                        return new io.netty.handler.ssl.JdkAlpnApplicationProtocolNegotiator(false, config.supportedProtocols());
                    } else {
                        throw new UnsupportedOperationException(new StringBuilder("JDK provider does not support ")
                                                                    .append(config.selectorFailureBehavior()).append(" failure behavior").toString());
                    }
                } else {
                    switch (config.selectedListenerFailureBehavior()) {
                        case ACCEPT:
                            return new io.netty.handler.ssl.JdkAlpnApplicationProtocolNegotiator(false, config.supportedProtocols());
                        case FATAL_ALERT:
                            return new io.netty.handler.ssl.JdkAlpnApplicationProtocolNegotiator(true, config.supportedProtocols());
                        default:
                            throw new UnsupportedOperationException(new StringBuilder("JDK provider does not support ")
                                                                        .append(config.selectedListenerFailureBehavior()).append(" failure behavior")
                                                                        .toString());
                    }
                }
            default:
                throw new UnsupportedOperationException(
                    new StringBuilder("JDK provider does not support ").append(config.protocol())
                        .append(" protocol")
                        .toString());
        }
    }

}

@TargetClass(className = "io.netty.buffer.AbstractReferenceCountedByteBuf")
final class Target_io_netty_buffer_AbstractReferenceCountedByteBuf {

    @Alias
    @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FieldOffset, name = "refCnt")
    private static long REFCNT_FIELD_OFFSET;
}

@TargetClass(className = "io.netty.util.AbstractReferenceCounted")
final class Target_io_netty_util_AbstractReferenceCounted {

    @Alias
    @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FieldOffset, name = "refCnt")
    private static long REFCNT_FIELD_OFFSET;
}

// This class is runtime-initialized by NettyProcessor
final class Holder_io_netty_util_concurrent_ScheduledFutureTask {
    static final long START_TIME = System.nanoTime();
}

@TargetClass(className = "io.netty.util.concurrent.ScheduledFutureTask")
final class Target_io_netty_util_concurrent_ScheduledFutureTask {

    // The START_TIME field is kept but not used.
    // All the accesses to it have been replaced with Holder_io_netty_util_concurrent_ScheduledFutureTask

    @Substitute
    static long initialNanoTime() {
        return Holder_io_netty_util_concurrent_ScheduledFutureTask.START_TIME;
    }

    @Substitute
    static long nanoTime() {
        return System.nanoTime() - Holder_io_netty_util_concurrent_ScheduledFutureTask.START_TIME;
    }

    @Alias
    public long deadlineNanos() {
        return 0;
    }

    @Substitute
    public long delayNanos(long currentTimeNanos) {
        return Math.max(0,
            deadlineNanos() - (currentTimeNanos - Holder_io_netty_util_concurrent_ScheduledFutureTask.START_TIME));
    }
}

@TargetClass(className = "io.netty.util.internal.NativeLibraryLoader")
final class Target_io_netty_util_internal_NativeLibraryLoader {

    // This method can trick GraalVM into thinking that Classloader#defineClass is getting called
    @Substitute
    static Class<?> tryToLoadClass(final ClassLoader loader, final Class<?> helper) throws ClassNotFoundException {
        return Class.forName(helper.getName(), false, loader);
    }

}

@TargetClass(className = "io.netty.handler.codec.http.HttpContentDecompressor")
final class Target_io_netty_handler_codec_http_HttpContentDecompressor {

    @Alias
    private boolean strict;

    @Alias
    protected ChannelHandlerContext ctx;

    @Substitute
    protected EmbeddedChannel newContentDecoder(String contentEncoding) throws Exception {
        if (GZIP.contentEqualsIgnoreCase(contentEncoding) || X_GZIP.contentEqualsIgnoreCase(contentEncoding)) {
            return new EmbeddedChannel(ctx.channel().id(), ctx.channel().metadata().hasDisconnect(),
                ctx.channel().config(), ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP));
        }

        if (DEFLATE.contentEqualsIgnoreCase(contentEncoding) || X_DEFLATE.contentEqualsIgnoreCase(contentEncoding)) {
            final ZlibWrapper wrapper = strict ? ZlibWrapper.ZLIB : ZlibWrapper.ZLIB_OR_NONE;
            // To be strict, 'deflate' means ZLIB, but some servers were not implemented correctly.
            return new EmbeddedChannel(ctx.channel().id(), ctx.channel().metadata().hasDisconnect(),
                ctx.channel().config(), ZlibCodecFactory.newZlibDecoder(wrapper));
        }

        // 'identity' or unsupported
        return null;
    }
}

public class NettySubstitutions { }
