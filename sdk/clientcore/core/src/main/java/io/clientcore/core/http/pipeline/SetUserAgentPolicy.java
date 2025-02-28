// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.core.utils.configuration.Configuration;

/**
 * The {@link SetUserAgentPolicy} class is an implementation of the {@link HttpPipelinePolicy} interface. This policy is
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
 * <!-- src_embed io.clientcore.core.http.pipeline.SetUserAgentPolicy.constructor -->
 * <pre>
 * SetUserAgentPolicy policy = new SetUserAgentPolicy&#40;&quot;MyApp&#47;1.0&quot;&#41;;
 * </pre>
 * <!-- end io.clientcore.core.http.pipeline.SetUserAgentPolicy.constructor -->
 *
 * @see io.clientcore.core.http.pipeline
 * @see HttpPipelinePolicy
 * @see HttpPipeline
 * @see HttpRequest
 * @see Response
 * @see HttpHeaderName
 */
public class SetUserAgentPolicy implements HttpPipelinePolicy {
    private final String userAgent;

    /**
     * Creates a {@link SetUserAgentPolicy} with a default user agent string.
     */
    public SetUserAgentPolicy() {
        this(null);
    }

    /**
     * Creates a UserAgentPolicy with {@code userAgent} as the header value. If {@code userAgent} is {@code null}, then
     * the default user agent value is used.
     *
     * @param userAgent The user agent string to add to request headers.
     */
    public SetUserAgentPolicy(String userAgent) {
        if (userAgent != null) {
            this.userAgent = userAgent;
        } else {
            this.userAgent = DEFAULT_USER_AGENT_HEADER;
        }
    }

    /**
     * Creates a UserAgentPolicy with the {@code sdkName} and {@code sdkVersion} in the User-Agent header value.
     *
     * @param applicationId User specified application Id.
     * @param sdkName Name of the client library.
     * @param sdkVersion Version of the client library.
     */
    public SetUserAgentPolicy(String applicationId, String sdkName, String sdkVersion) {
        this.userAgent = toUserAgentString(applicationId, sdkName, sdkVersion);
    }

    @Override
    public Response<?> process(HttpRequest httpRequest, HttpPipelineNextPolicy next) {
        httpRequest.getHeaders().set(HttpHeaderName.USER_AGENT, userAgent);
        return next.process();
    }

    @Override
    public final HttpPipelinePosition getPipelinePosition() {
        return HttpPipelinePosition.BEFORE_REDIRECT;
    }

    private static final int MAX_APPLICATION_ID_LENGTH = 24;
    private static final String INVALID_APPLICATION_ID_LENGTH
        = "'applicationId' length cannot be greater than " + MAX_APPLICATION_ID_LENGTH;
    private static final String INVALID_APPLICATION_ID_SPACE = "'applicationId' cannot contain spaces.";

    /**
     * Default {@code UserAgent} header.
     */
    static final String DEFAULT_USER_AGENT_HEADER = "sdk-java";

    /**
     * Return user agent string for the given sdk name and version.
     *
     * @param applicationId Name of the application.
     * @param sdkName Name of the SDK.
     * @param sdkVersion Version of the SDK.
     * @return User agent string as specified in design guidelines.
     *
     * @throws IllegalArgumentException If {@code applicationId} contains spaces or is larger than 24 characters in
     * length.
     */
    private static String toUserAgentString(String applicationId, String sdkName, String sdkVersion) {
        StringBuilder userAgentBuilder = new StringBuilder();

        if (!CoreUtils.isNullOrEmpty(applicationId)) {
            if (applicationId.length() > MAX_APPLICATION_ID_LENGTH) {
                throw new IllegalArgumentException(INVALID_APPLICATION_ID_LENGTH);
            } else if (applicationId.contains(" ")) {
                throw new IllegalArgumentException(INVALID_APPLICATION_ID_SPACE);
            } else {
                userAgentBuilder.append(applicationId).append(" ");
            }
        }

        // Add the required default User-Agent string.
        userAgentBuilder.append(DEFAULT_USER_AGENT_HEADER).append("-").append(sdkName).append("/").append(sdkVersion);
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
