// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http.rest;

import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.DecodeException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceExistsException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.TooManyRedirectsException;
import com.azure.core.exception.UnexpectedLengthException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.implementation.ResponseExceptionConstructorCache;
import com.azure.core.implementation.http.UnexpectedExceptionInformation;
import com.azure.core.implementation.util.BinaryDataContent;
import com.azure.core.implementation.util.BinaryDataHelper;
import com.azure.core.implementation.util.FluxByteBufferContent;
import com.azure.core.implementation.util.InputStreamContent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods that aid processing in RestProxy.
 */
public final class RestProxyUtils {

    private static final ByteBuffer VALIDATION_BUFFER = ByteBuffer.allocate(0);
    private static final ResponseExceptionConstructorCache RESPONSE_EXCEPTION_CONSTRUCTOR_CACHE =
        new ResponseExceptionConstructorCache();
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
            Long bodyLength = body.getLength();
            long expectedLength = Long.parseLong(request.getHeaders().getValue("Content-Length"));
            if (bodyLength != null) {
                if (bodyLength < expectedLength) {
                    throw new UnexpectedLengthException(String.format(BODY_TOO_SMALL,
                        bodyLength, expectedLength), bodyLength, expectedLength);
                } else if (bodyLength > expectedLength) {
                    throw new UnexpectedLengthException(String.format(BODY_TOO_LARGE,
                        bodyLength, expectedLength), bodyLength, expectedLength);
                }
            } else {
                BinaryDataContent content = BinaryDataHelper.getContent(body);
                if (content instanceof InputStreamContent) {
                    InputStream validatingInputStream = new LengthValidatingInputStream(
                        content.toStream(), expectedLength);
                    request.setBody(BinaryData.fromStream(validatingInputStream));
                } else {
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
            return binaryData;
        }

        final long expectedLength = Long.parseLong(request.getHeaders().getValue("Content-Length"));
        Long length = binaryData.getLength();
        BinaryDataContent bdc = BinaryDataHelper.getContent(binaryData);
        if (length == null) {
            if (bdc instanceof FluxByteBufferContent) {
                throw new IllegalStateException("Flux Byte Buffer is not supported in Synchronous Rest Proxy.");
            } else if (bdc instanceof InputStreamContent) {
                InputStreamContent inputStreamContent = ((InputStreamContent) bdc);
                InputStream inputStream = inputStreamContent.toStream();
                com.azure.core.implementation.util.LengthValidatingInputStream lengthValidatingInputStream =
                    new com.azure.core.implementation.util.LengthValidatingInputStream(inputStream, expectedLength);
                return BinaryData.fromStream(lengthValidatingInputStream);
            } else {
                byte[] b = (bdc).toBytes();
                long len = b.length;
                if (len > expectedLength) {
                    throw new UnexpectedLengthException(String.format(BODY_TOO_LARGE,
                        len, expectedLength), len, expectedLength);
                }

                if (len < expectedLength) {
                    throw new UnexpectedLengthException(String.format(BODY_TOO_SMALL,
                        len, expectedLength), len, expectedLength);
                }
                return BinaryData.fromBytes(b);
            }
        } else {
            if (length > expectedLength) {
                throw new UnexpectedLengthException(String.format(BODY_TOO_LARGE,
                    length, expectedLength), length, expectedLength);
            }

            if (length < expectedLength) {
                throw new UnexpectedLengthException(String.format(BODY_TOO_SMALL,
                    length, expectedLength), length, expectedLength);
            }
            return binaryData;
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
        if (method.isAnnotationPresent(com.azure.core.annotation.ResumeOperation.class)) {
            throw LOGGER.logExceptionAsError(new IllegalStateException("'ResumeOperation' isn't supported."));
        }
    }

    /**
     * Creates the Unexpected Exception using the details provided in http response and its content.
     *
     * @param exception the excepion holding UnexpectedException's details.
     * @param httpResponse the http response to parse when constructing exception
     * @param responseContent the response body to use when constructing exception
     * @param responseDecodedContent the decoded response content to use when constructing exception
     * @return the Unexpected Exception
     */
    public static Exception instantiateUnexpectedException(final UnexpectedExceptionInformation exception,
                                                           final HttpResponse httpResponse, final byte[] responseContent, final Object responseDecodedContent) {
        StringBuilder exceptionMessage = new StringBuilder("Status code ")
            .append(httpResponse.getStatusCode())
            .append(", ");

        final String contentType = httpResponse.getHeaderValue("Content-Type");
        if ("application/octet-stream".equalsIgnoreCase(contentType)) {
            exceptionMessage.append("(").append(httpResponse.getHeaderValue("Content-Length")).append("-byte body)");
        } else if (responseContent == null || responseContent.length == 0) {
            exceptionMessage.append("(empty body)");
        } else {
            exceptionMessage.append("\"").append(new String(responseContent, StandardCharsets.UTF_8)).append("\"");
        }

        // For HttpResponseException types that exist in azure-core, call the constructor directly.
        Class<? extends HttpResponseException> exceptionType = exception.getExceptionType();
        if (exceptionType == HttpResponseException.class) {
            return new HttpResponseException(exceptionMessage.toString(), httpResponse, responseDecodedContent);
        } else if (exceptionType == ClientAuthenticationException.class) {
            return new ClientAuthenticationException(exceptionMessage.toString(), httpResponse, responseDecodedContent);
        } else if (exceptionType == DecodeException.class) {
            return new DecodeException(exceptionMessage.toString(), httpResponse, responseDecodedContent);
        } else if (exceptionType == ResourceExistsException.class) {
            return new ResourceExistsException(exceptionMessage.toString(), httpResponse, responseDecodedContent);
        } else if (exceptionType == ResourceModifiedException.class) {
            return new ResourceModifiedException(exceptionMessage.toString(), httpResponse, responseDecodedContent);
        } else if (exceptionType == ResourceNotFoundException.class) {
            return new ResourceNotFoundException(exceptionMessage.toString(), httpResponse, responseDecodedContent);
        } else if (exceptionType == TooManyRedirectsException.class) {
            return new TooManyRedirectsException(exceptionMessage.toString(), httpResponse, responseDecodedContent);
        } else {
            // Finally, if the HttpResponseException subclass doesn't exist in azure-core, use reflection to create a
            // new instance of it.
            try {
                MethodHandle handle = RESPONSE_EXCEPTION_CONSTRUCTOR_CACHE.get(exceptionType,
                    exception.getExceptionBodyType());
                return ResponseExceptionConstructorCache.invoke(handle, exceptionMessage.toString(), httpResponse,
                    responseDecodedContent);
            } catch (RuntimeException e) {
                // And if reflection fails, return an IOException.
                // TODO (alzimmer): Determine if this should be an IOException or HttpResponseException.
                exceptionMessage.append(". An instance of ")
                    .append(exceptionType.getCanonicalName())
                    .append(" couldn't be created.");
                return new IOException(exceptionMessage.toString(), e);
            }
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
