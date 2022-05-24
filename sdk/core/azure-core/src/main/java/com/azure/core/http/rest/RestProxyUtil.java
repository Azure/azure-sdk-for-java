package com.azure.core.http.rest;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.implementation.http.UnexpectedExceptionInformation;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RestProxyUtil {

    static Context mergeRequestOptionsContext(Context context, RequestOptions options) {
        if (options == null) {
            return context;
        }

        Context optionsContext = options.getContext();
        if (optionsContext != null && optionsContext != Context.NONE) {
            context = CoreUtils.mergeContexts(context, optionsContext);
        }

        return context;
    }

    @SuppressWarnings("deprecation")
    static void validateResumeOperationIsNotPresent(Method method, ClientLogger logger) {
        // Use the fully-qualified class name as javac will throw deprecation warnings on imports when the class is
        // marked as deprecated.
        if (method.isAnnotationPresent(com.azure.core.annotation.ResumeOperation.class)) {
            throw logger.logExceptionAsError(new IllegalStateException("'ResumeOperation' isn't supported."));
        }
    }

    static Exception instantiateUnexpectedException(final UnexpectedExceptionInformation exception,
                                                    final HttpResponse httpResponse, final byte[] responseContent, final Object responseDecodedContent) {
        final int responseStatusCode = httpResponse.getStatusCode();
        final String contentType = httpResponse.getHeaderValue("Content-Type");
        final String bodyRepresentation;
        if ("application/octet-stream".equalsIgnoreCase(contentType)) {
            bodyRepresentation = "(" + httpResponse.getHeaderValue("Content-Length") + "-byte body)";
        } else {
            bodyRepresentation = responseContent == null || responseContent.length == 0
                ? "(empty body)"
                : "\"" + new String(responseContent, StandardCharsets.UTF_8) + "\"";
        }

        Exception result;
        try {
            final Constructor<? extends HttpResponseException> exceptionConstructor = exception.getExceptionType()
                .getConstructor(String.class, HttpResponse.class, exception.getExceptionBodyType());
            result = exceptionConstructor.newInstance("Status code " + responseStatusCode + ", " + bodyRepresentation,
                httpResponse, responseDecodedContent);
        } catch (ReflectiveOperationException e) {
            String message = "Status code " + responseStatusCode + ", but an instance of "
                + exception.getExceptionType().getCanonicalName() + " cannot be created."
                + " Response body: " + bodyRepresentation;

            result = new IOException(message, e);
        }
        return result;
    }

    /**
     * Create an instance of the default serializer.
     *
     * @return the default serializer
     */
    static SerializerAdapter createDefaultSerializer() {
        return JacksonAdapter.createDefaultSerializerAdapter();
    }

    /**
     * Create the default HttpPipeline.
     *
     * @return the default HttpPipeline
     */
    static HttpPipeline createDefaultPipeline() {
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new UserAgentPolicy());
        policies.add(new RetryPolicy());
        policies.add(new CookiePolicy());

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .build();
    }
}
