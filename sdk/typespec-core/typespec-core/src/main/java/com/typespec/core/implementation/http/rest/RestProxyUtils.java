// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.http.rest;

import com.typespec.core.exception.UnexpectedLengthException;
import com.typespec.core.http.HttpHeaderName;
import com.typespec.core.http.HttpPipeline;
import com.typespec.core.http.HttpPipelineBuilder;
import com.typespec.core.http.HttpRequest;
import com.typespec.core.http.policy.CookiePolicy;
import com.typespec.core.http.policy.HttpPipelinePolicy;
import com.typespec.core.http.policy.RetryPolicy;
import com.typespec.core.http.policy.UserAgentPolicy;
import com.typespec.core.http.rest.RequestOptions;
import com.typespec.core.implementation.util.BinaryDataContent;
import com.typespec.core.implementation.util.BinaryDataHelper;
import com.typespec.core.implementation.util.FluxByteBufferContent;
import com.typespec.core.implementation.util.InputStreamContent;
import com.typespec.core.util.BinaryData;
import com.typespec.core.util.Context;
import com.typespec.core.util.CoreUtils;
import com.typespec.core.util.logging.ClientLogger;
import com.typespec.core.util.serializer.JacksonAdapter;
import com.typespec.core.util.serializer.SerializerAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods that aid processing in RestProxy.
 */
public final class RestProxyUtils {

    private static final ByteBuffer VALIDATION_BUFFER = ByteBuffer.allocate(0);
    public static final String BODY_TOO_LARGE = "Request body emitted %d bytes, more than the expected %d bytes.";
    public static final String BODY_TOO_SMALL = "Request body emitted %d bytes, less than the expected %d bytes.";
    public static final ClientLogger LOGGER = new ClientLogger(RestProxyUtils.class);

    private RestProxyUtils() {
    }

    public static Mono<HttpRequest> validateLengthAsync(final HttpRequest request) {
        final BinaryData body = request.getBodyAsBinaryData();

        if (body == null) {
            return Mono.just(request);
        }

        return Mono.fromCallable(() -> {
            BinaryDataContent content = BinaryDataHelper.getContent(body);
            long expectedLength = Long.parseLong(request.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH));
            if (content instanceof InputStreamContent) {
                InputStream validatingInputStream = new LengthValidatingInputStream(content.toStream(), expectedLength);
                request.setBody(BinaryData.fromStream(validatingInputStream, expectedLength));
            } else if (content instanceof FluxByteBufferContent) {
                request.setBody(validateFluxLength(body.toFluxByteBuffer(), expectedLength));
            } else {
                Long bodyLength = body.getLength();
                if (bodyLength != null) {
                    if (bodyLength < expectedLength) {
                        throw new UnexpectedLengthException(String.format(BODY_TOO_SMALL,
                            bodyLength, expectedLength), bodyLength, expectedLength);
                    } else if (bodyLength > expectedLength) {
                        throw new UnexpectedLengthException(String.format(BODY_TOO_LARGE,
                            bodyLength, expectedLength), bodyLength, expectedLength);
                    }
                } else  {
                    request.setBody(validateFluxLength(body.toFluxByteBuffer(), expectedLength));
                }
            }

            return request;
        });
    }

    private static Flux<ByteBuffer> validateFluxLength(Flux<ByteBuffer> bbFlux, long expectedLength) {
        if (bbFlux == null) {
            return Flux.empty();
        }

        return Flux.defer(() -> {
            final long[] currentTotalLength = new long[1];
            return Flux.concat(bbFlux, Flux.just(VALIDATION_BUFFER)).handle((buffer, sink) -> {
                if (buffer == null) {
                    return;
                }

                if (buffer == VALIDATION_BUFFER) {
                    if (expectedLength != currentTotalLength[0]) {
                        sink.error(new UnexpectedLengthException(String.format(BODY_TOO_SMALL,
                            currentTotalLength[0], expectedLength), currentTotalLength[0], expectedLength));
                    } else {
                        sink.complete();
                    }
                    return;
                }

                currentTotalLength[0] += buffer.remaining();
                if (currentTotalLength[0] > expectedLength) {
                    sink.error(new UnexpectedLengthException(String.format(BODY_TOO_LARGE,
                        currentTotalLength[0], expectedLength), currentTotalLength[0], expectedLength));
                    return;
                }

                sink.next(buffer);
            });
        });
    }

    /**
     * Validates the Length of the input request matches its configured Content Length.
     * @param request the input request to validate.
     * @return the requests body as BinaryData on successful validation.
     */
    public static BinaryData validateLengthSync(final HttpRequest request) {
        final BinaryData binaryData = request.getBodyAsBinaryData();
        if (binaryData == null) {
            return null;
        }

        final long expectedLength = Long.parseLong(request.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH));
        Long length = binaryData.getLength();
        BinaryDataContent bdc = BinaryDataHelper.getContent(binaryData);
        if (bdc instanceof FluxByteBufferContent) {
            throw new IllegalStateException("Flux Byte Buffer is not supported in Synchronous Rest Proxy.");
        } else if (bdc instanceof InputStreamContent) {
            InputStreamContent inputStreamContent = ((InputStreamContent) bdc);
            InputStream inputStream = inputStreamContent.toStream();
            LengthValidatingInputStream lengthValidatingInputStream =
                new LengthValidatingInputStream(inputStream, expectedLength);
            return BinaryData.fromStream(lengthValidatingInputStream, expectedLength);
        } else {
            if (length == null) {
                byte[] b = (bdc).toBytes();
                length = ((Integer) b.length).longValue();
                validateLength(length, expectedLength);
                return BinaryData.fromBytes(b);
            } else {
                validateLength(length, expectedLength);
                return binaryData;
            }
        }
    }

    private static void validateLength(long length, long expectedLength) {
        if (length > expectedLength) {
            throw new UnexpectedLengthException(String.format(BODY_TOO_LARGE,
                length, expectedLength), length, expectedLength);
        }

        if (length < expectedLength) {
            throw new UnexpectedLengthException(String.format(BODY_TOO_SMALL,
                length, expectedLength), length, expectedLength);
        }
    }

    /**
     * Merges the Context with the Context provided with Options.
     *
     * @param context the Context to merge
     * @param options the options holding the context to merge with
     * @return the merged context.
     */
    public static Context mergeRequestOptionsContext(Context context, RequestOptions options) {
        if (options == null) {
            return context;
        }

        Context optionsContext = options.getContext();
        if (optionsContext != null && optionsContext != Context.NONE) {
            context = CoreUtils.mergeContexts(context, optionsContext);
        }

        return context;
    }

    /**
     * Validates the input Method is not annotated with Resume Operation
     * @param method the method input to validate
     * @throws IllegalStateException if the input method is annotated with the Resume Operation.
     */
    @SuppressWarnings("deprecation")
    public static void validateResumeOperationIsNotPresent(Method method) {
        // Use the fully-qualified class name as javac will throw deprecation warnings on imports when the class is
        // marked as deprecated.
        if (method.isAnnotationPresent(com.typespec.core.annotation.ResumeOperation.class)) {
            throw LOGGER.logExceptionAsError(new IllegalStateException("'ResumeOperation' isn't supported."));
        }
    }

    /**
     * Create an instance of the default serializer.
     *
     * @return the default serializer
     */
    public static SerializerAdapter createDefaultSerializer() {
        return JacksonAdapter.createDefaultSerializerAdapter();
    }

    /**
     * Create the default HttpPipeline.
     *
     * @return the default HttpPipeline
     */
    public static HttpPipeline createDefaultPipeline() {
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new UserAgentPolicy());
        policies.add(new RetryPolicy());
        policies.add(new CookiePolicy());

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .build();
    }
}
