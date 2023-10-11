// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.http.rest;

import com.generic.core.exception.UnexpectedLengthException;
import com.generic.core.http.HttpHeaderName;
import com.generic.core.http.HttpPipeline;
import com.generic.core.http.HttpPipelineBuilder;
import com.generic.core.http.HttpRequest;
import com.generic.core.http.policy.CookiePolicy;
import com.generic.core.http.policy.HttpPipelinePolicy;
import com.generic.core.http.policy.RetryPolicy;
import com.generic.core.http.policy.UserAgentPolicy;
import com.generic.core.implementation.util.BinaryDataContent;
import com.generic.core.implementation.util.BinaryDataHelper;
import com.generic.core.implementation.util.InputStreamContent;
import com.generic.core.util.BinaryData;
import com.generic.core.util.logging.ClientLogger;
import com.generic.core.util.serializer.JacksonAdapter;
import com.generic.core.util.serializer.SerializerAdapter;

import java.io.InputStream;
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
        if (bdc instanceof InputStreamContent) {
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
