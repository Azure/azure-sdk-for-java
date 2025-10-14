// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.core.utils.configuration.Configuration;

/**
 * The {@link UserAgentPolicy} class is an implementation of the {@link HttpPipelinePolicy} interface. This policy is
 * used to add a "User-Agent" header to each {@code HttpRequest}.
 * <p>
 * If the {@code User-Agent} header is already present in the request, it will not be overwritten.
 * <p>
 * This class is useful when you need to add a specific "User-Agent" header for all requests in a pipeline. It ensures
 * that the "User-Agent" header is set correctly for each request. The "User-Agent" header is used to provide the server
 * with information about the software used by the client.
 *
 * <p><strong>Code sample:</strong></p>
 *
 * <p>In this example, a {@code UserAgentPolicy} is created with a "User-Agent" header value of "MyApp/1.0".
 * Once added to the pipeline, requests will have their "User-Agent" header set to "MyApp/1.0" by the
 * {@code UserAgentPolicy}.</p>
 *
 * <!-- src_embed io.clientcore.core.http.pipeline.UserAgentPolicy.constructor -->
 * <pre>
 * UserAgentPolicy policy = new UserAgentPolicy&#40;&quot;MyApp&#47;1.0&quot;&#41;;
 * </pre>
 * <!-- end io.clientcore.core.http.pipeline.UserAgentPolicy.constructor -->
 *
 * @see io.clientcore.core.http.pipeline
 * @see HttpPipelinePolicy
 * @see HttpPipeline
 * @see HttpRequest
 * @see Response
 * @see HttpHeaderName
 */
@Metadata(properties = MetadataProperties.IMMUTABLE)
public class UserAgentPolicy implements HttpPipelinePolicy {
    private final String userAgent;

    /**
     * Creates a {@link UserAgentPolicy} with a default user agent string.
     */
    public UserAgentPolicy() {
        this((String) null);
    }

    /**
     * Creates a UserAgentPolicy with {@code userAgent} as the header value. If {@code userAgent} is {@code null}, then
     * the default user agent value is used.
     *
     * @param userAgent The user agent string to add to request headers.
     */
    public UserAgentPolicy(String userAgent) {
        if (userAgent != null) {
            this.userAgent = userAgent;
        } else {
            this.userAgent = DEFAULT_USER_AGENT_HEADER;
        }
    }

    /**
     * Creates a UserAgentPolicy with the {@code sdkName} and {@code sdkVersion} in the User-Agent header value.
     *
     * @param userAgentOptions Options allowing to customize the user agent string.
     */
    public UserAgentPolicy(UserAgentOptions userAgentOptions) {
        this.userAgent = toUserAgentString(userAgentOptions);
    }

    @Override
    public Response<BinaryData> process(HttpRequest httpRequest, HttpPipelineNextPolicy next) {
        httpRequest.getHeaders().set(HttpHeaderName.USER_AGENT, userAgent);
        return next.process();
    }

    @Override
    public final HttpPipelinePosition getPipelinePosition() {
        return HttpPipelinePosition.BEFORE_REDIRECT;
    }

    /**
     * Default {@code UserAgent} header.
     */
    static final String DEFAULT_USER_AGENT_HEADER = "sdk-java";

    /**
     * Return user agent string for the given sdk name and version.
     *
     * @param userAgentOptions Options allowing to customize the user agent string.
     *
     * @throws IllegalArgumentException If {@code applicationId} contains spaces.
     */
    private static String toUserAgentString(UserAgentOptions userAgentOptions) {
        StringBuilder userAgentBuilder = new StringBuilder();

        String applicationId = userAgentOptions.getApplicationId();

        if (!CoreUtils.isNullOrEmpty(applicationId)) {
            if (applicationId.contains(" ")) {
                throw new IllegalArgumentException("'applicationid' cannot contain spaces.");
            } else {
                userAgentBuilder.append(applicationId).append(" ");
            }
        }

        // Add the required default User-Agent string.
        userAgentBuilder.append(DEFAULT_USER_AGENT_HEADER)
            .append("-")
            .append(userAgentOptions.getSdkName())
            .append("/")
            .append(userAgentOptions.getSdkVersion());
        appendPlatformInfo(userAgentBuilder);

        return userAgentBuilder.toString();
    }

    /**
     * Appends the platform information telemetry to the User-Agent header.
     */
    private static void appendPlatformInfo(StringBuilder stringBuilder) {
        String javaVersion = Configuration.getGlobalConfiguration().get("java.version");
        String osName = Configuration.getGlobalConfiguration().get("os.name");
        String osVersion = Configuration.getGlobalConfiguration().get("os.version");

        stringBuilder.append(" (")
            .append(javaVersion)
            .append("; ")
            .append(osName)
            .append("; ")
            .append(osVersion)
            .append(")");
    }
}
