// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http2.DefaultHttp2FrameWriter;
import io.netty.handler.codec.http2.DefaultHttp2HeadersEncoder;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2ConnectionHandler;
import io.netty.handler.codec.http2.Http2FrameWriter;
import io.netty.handler.codec.http2.Http2HeadersEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.Connection;

import java.lang.reflect.Field;

/**
 * Installs the {@link CosmosHttp2SensitivityDetector} on an HTTP/2 connection's HPACK encoder.
 * <p>
 * reactor-netty does not expose Netty's {@code Http2FrameCodecBuilder.headerSensitivityDetector()}
 * through its public API. This utility uses reflection to reach the HPACK encoder's sensitivity
 * detector field inside the already-built {@code Http2FrameCodec}.
 * <p>
 * The reflection chain is:
 * <pre>
 *   Http2FrameCodec (pipeline "reactor.left.httpCodec")
 *     → .encoder()              [public API]
 *     → .frameWriter()          [public API on Http2ConnectionEncoder]
 *     → .headersEncoder         [private field on DefaultHttp2FrameWriter, accessed via reflection]
 *     → .sensitivityDetector    [private field on DefaultHttp2HeadersEncoder, accessed via reflection]
 * </pre>
 * <p>
 * If reflection fails (e.g., due to future Netty internal changes or module restrictions), the
 * failure is logged and the connection proceeds without the optimization — this is a best-effort
 * performance improvement, not a correctness requirement.
 */
final class Http2SensitivityDetectorUtil {

    private static final Logger logger = LoggerFactory.getLogger(Http2SensitivityDetectorUtil.class);

    // Pipeline handler name used by reactor-netty for the Http2FrameCodec
    private static final String REACTOR_HTTP_CODEC = "reactor.left.httpCodec";

    // Cached reflection fields for performance — computed once at class load time
    private static final Field HEADERS_ENCODER_FIELD;
    private static final Field SENSITIVITY_DETECTOR_FIELD;
    private static final Field HPACK_ENCODER_FIELD;

    static {
        Field headersEncoderField = null;
        Field sensitivityDetectorField = null;
        Field hpackEncoderField = null;
        try {
            headersEncoderField = DefaultHttp2FrameWriter.class.getDeclaredField("headersEncoder");
            headersEncoderField.setAccessible(true);
            sensitivityDetectorField = DefaultHttp2HeadersEncoder.class
                .getDeclaredField("sensitivityDetector");
            sensitivityDetectorField.setAccessible(true);
            hpackEncoderField = DefaultHttp2HeadersEncoder.class
                .getDeclaredField("hpackEncoder");
            hpackEncoderField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            logger.warn(
                "Failed to resolve HPACK sensitivity detector fields via reflection. "
                    + "Authorization header HPACK optimization will be disabled. "
                    + "This may occur with an incompatible Netty version.",
                e);
        } catch (SecurityException e) {
            logger.warn(
                "Security manager prevented reflective access to HPACK sensitivity detector fields. "
                    + "Authorization header HPACK optimization will be disabled.",
                e);
        }
        HEADERS_ENCODER_FIELD = headersEncoderField;
        SENSITIVITY_DETECTOR_FIELD = sensitivityDetectorField;
        HPACK_ENCODER_FIELD = hpackEncoderField;
    }

    private Http2SensitivityDetectorUtil() {
    }

    /**
     * Installs the {@link CosmosHttp2SensitivityDetector} on the HTTP/2 connection if
     * the pipeline contains an {@code Http2FrameCodec}.
     * <p>
     * When HPACK diagnostics are enabled (via system property
     * {@code COSMOS_HTTP2_HPACK_DIAGNOSTICS_ENABLED=true}), a tracking wrapper is installed
     * that monitors per-header cardinality and HPACK table utilization. This helps identify
     * which headers have high cardinality and should be marked as never-indexed.
     * <p>
     * This method is safe to call for both HTTP/1.1 and HTTP/2 connections — it checks
     * the pipeline handler type before proceeding.
     *
     * @param connection the reactor-netty connection
     */
    static void installSensitivityDetector(Connection connection) {
        if (HEADERS_ENCODER_FIELD == null || SENSITIVITY_DETECTOR_FIELD == null) {
            // Reflection setup failed at class load time — skip silently
            return;
        }

        ChannelPipeline pipeline = connection.channel().pipeline();
        ChannelHandler codecHandler = pipeline.get(REACTOR_HTTP_CODEC);

        if (!(codecHandler instanceof Http2ConnectionHandler)) {
            // Not an HTTP/2 connection (HTTP/1.1 or codec not yet installed)
            return;
        }

        try {
            Http2ConnectionHandler h2Handler = (Http2ConnectionHandler) codecHandler;
            Http2ConnectionEncoder encoder = h2Handler.encoder();
            Http2FrameWriter frameWriter = encoder.frameWriter();

            if (!(frameWriter instanceof DefaultHttp2FrameWriter)) {
                logger.debug(
                    "Http2FrameWriter is not DefaultHttp2FrameWriter (actual: {}), "
                        + "skipping HPACK sensitivity detector installation.",
                    frameWriter.getClass().getName());
                return;
            }

            Object headersEncoder = HEADERS_ENCODER_FIELD.get(frameWriter);
            if (!(headersEncoder instanceof DefaultHttp2HeadersEncoder)) {
                logger.debug(
                    "Http2HeadersEncoder is not DefaultHttp2HeadersEncoder (actual: {}), "
                        + "skipping HPACK sensitivity detector installation.",
                    headersEncoder.getClass().getName());
                return;
            }

            Http2HeadersEncoder.SensitivityDetector detector;
            if (Http2HpackDiagnostics.isEnabled()) {
                Http2HpackDiagnostics diagnostics =
                    new Http2HpackDiagnostics(CosmosHttp2SensitivityDetector.INSTANCE);

                // Extract HpackEncoder reference for table utilization metrics
                if (HPACK_ENCODER_FIELD != null) {
                    try {
                        Object hpackEncoder = HPACK_ENCODER_FIELD.get(headersEncoder);
                        if (hpackEncoder != null) {
                            diagnostics.setHpackEncoder(hpackEncoder);
                        }
                    } catch (Exception e) {
                        logger.debug("Could not extract HpackEncoder for diagnostics", e);
                    }
                }

                detector = diagnostics;
                logger.info(
                    "Installed HPACK diagnostics on channel {}. "
                        + "Header cardinality and table utilization will be tracked.",
                    connection.channel().id());
            } else {
                detector = CosmosHttp2SensitivityDetector.INSTANCE;
                if (logger.isDebugEnabled()) {
                    logger.debug(
                        "Installed CosmosHttp2SensitivityDetector on channel {}. "
                            + "Authorization header will use HPACK never-indexed representation.",
                        connection.channel().id());
                }
            }

            SENSITIVITY_DETECTOR_FIELD.set(headersEncoder, detector);
        } catch (IllegalAccessException e) {
            logger.warn(
                "Failed to install HPACK sensitivity detector via reflection. "
                    + "Authorization header HPACK optimization will be disabled for this connection.",
                e);
        } catch (Exception e) {
            logger.warn(
                "Unexpected error installing HPACK sensitivity detector. "
                    + "Authorization header HPACK optimization will be disabled for this connection.",
                e);
        }
    }
}
