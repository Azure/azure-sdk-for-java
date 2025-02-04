// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.http.pipeline;

import io.clientcore.core.util.ExpandableEnum;

/**
 * Enum representing where in an {@link HttpPipeline} a given {@link HttpPipelinePolicy} is positioned when added to an
 * {@link HttpPipelineBuilder}.
 * <p>
 * By default, policies are added at {@link #BETWEEN_RETRY_AND_AUTHENTICATION} with the assumption of
 * {@link HttpPipelinePolicy policies} are idempotent and may need to be updated each time a network request is sent
 * (each try) and may mutate the results of authentication policies.
 * <p>
 * HttpPipelineOrder ensures that the creation of an {@link HttpPipeline} follows a strict ordering of policies. This
 * ordering is based on what is believed to be the best order of policy execution. Here is a visual representation of
 * the order of policies in an {@link HttpPipeline}:
 * <pre>
 *     +--------------------------+
 *     | Policies before redirect |
 *     +--------------------------+
 *     |     Redirect Policy      |
 *     +--------------------------+
 *     |     Between redirect     |
 *     |        and retry         |
 *     +--------------------------+
 *     |       Retry Policy       |
 *     +--------------------------+
 *     |      Between retry       |
 *     |    and authentication    |
 *     +--------------------------+
 *     |  Authentication Policy   |
 *     +--------------------------+
 *     |  Between authentication  |
 *     |   and instrumentation    |
 *     +--------------------------+
 *     |  Instrumentation Policy  |
 *     +--------------------------+
 *     |      Policies after      |
 *     |     instrumentation      |
 *     +--------------------------+
 * </pre>
 */
public final class HttpPipelineOrder implements ExpandableEnum<String> {
    private final String value;

    private HttpPipelineOrder(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    /**
     * The policy will be position before the {@link HttpRedirectPolicy}.
     * <p>
     * If multiple {@link HttpPipelinePolicy policies} are added with this order, the first added will be the furthest
     * before the {@link HttpRedirectPolicy} and the last added will be the closest. Or, visually:
     * <pre>
     *     +------------------+
     *     | 1st added Before |
     *     +------------------+
     *     | 2nd added Before |
     *     +------------------+
     *     | 3rd added Before |
     *     +------------------+
     *     | Redirect Policy  |
     *     +------------------+
     * </pre>
     */
    public static final HttpPipelineOrder BEFORE_REDIRECT = new HttpPipelineOrder("BEFORE_REDIRECT");

    /**
     * The policy will be position after the {@link HttpRedirectPolicy} and before the {@link HttpRetryPolicy}.
     * <p>
     * If multiple {@link HttpPipelinePolicy policies} are added with this order, they will be executed in the order
     * they were added between the {@link HttpRedirectPolicy} and {@link HttpRetryPolicy}. Or, visually:
     * <pre>
     *     +-------------------+
     *     |  Redirect Policy  |
     *     +-------------------+
     *     | 1st added Between |
     *     +-------------------+
     *     | 2nd added Between |
     *     +-------------------+
     *     | 3rd added Between |
     *     +-------------------+
     *     |   Retry Policy    |
     *     +-------------------+
     * </pre>
     */
    public static final HttpPipelineOrder BETWEEN_REDIRECT_AND_RETRY
        = new HttpPipelineOrder("BETWEEN_REDIRECT_AND_RETRY");

    /**
     * The policy will be position after the {@link HttpRetryPolicy} and before the {@link HttpCredentialPolicy}.
     * <p>
     * If multiple {@link HttpPipelinePolicy policies} are added with this order, they will be executed in the order
     * they were added between the {@link HttpRetryPolicy} and {@link HttpCredentialPolicy}. Or, visually:
     * <pre>
     *     +-----------------------+
     *     |     Retry Policy      |
     *     +-----------------------+
     *     |   1st added Between   |
     *     +-----------------------+
     *     |   2nd added Between   |
     *     +-----------------------+
     *     |   3rd added Between   |
     *     +-----------------------+
     *     | Authentication Policy |
     *     +-----------------------+
     * </pre>
     */
    public static final HttpPipelineOrder BETWEEN_RETRY_AND_AUTHENTICATION
        = new HttpPipelineOrder("BETWEEN_RETRY_AND_AUTHENTICATION");

    /**
     * The policy will be position after the {@link HttpCredentialPolicy} and before the
     * {@link HttpInstrumentationPolicy}.
     * <p>
     * If multiple {@link HttpPipelinePolicy policies} are added with this order, they will be executed in the order
     * they were added between the {@link HttpCredentialPolicy} and {@link HttpInstrumentationPolicy}. Or, visually:
     * <pre>
     *     +------------------------+
     *     | Authentication Policy  |
     *     +------------------------+
     *     |   1st added Between    |
     *     +------------------------+
     *     |   2nd added Between    |
     *     +------------------------+
     *     |   3rd added Between    |
     *     +------------------------+
     *     | Instrumentation Policy |
     *     +------------------------+
     * </pre>
     */
    public static final HttpPipelineOrder BETWEEN_AUTHENTICATION_AND_INSTRUMENTATION
        = new HttpPipelineOrder("BETWEEN_AUTHENTICATION_AND_INSTRUMENTATION");

    /**
     * The policy will be position after the {@link HttpInstrumentationPolicy}.
     * <p>
     * If multiple {@link HttpPipelinePolicy policies} are added with this order, they will be executed in the order
     * they were added after {@link HttpInstrumentationPolicy}. Or, visually:
     * <pre>
     *     +------------------------+
     *     | Instrumentation Policy |
     *     +------------------------+
     *     |    1st added After     |
     *     +------------------------+
     *     |    2nd added After     |
     *     +------------------------+
     *     |    3rd added After     |
     *     +------------------------+
     * </pre>
     */
    public static final HttpPipelineOrder AFTER_INSTRUMENTATION = new HttpPipelineOrder("AFTER_INSTRUMENTATION");

    // Package-private HttpPipelineOrder constants for pillar policies.
    static final HttpPipelineOrder REDIRECT = new HttpPipelineOrder("REDIRECT");
    static final HttpPipelineOrder RETRY = new HttpPipelineOrder("RETRY");
    static final HttpPipelineOrder AUTHENTICATION = new HttpPipelineOrder("AUTHENTICATION");
    static final HttpPipelineOrder INSTRUMENTATION = new HttpPipelineOrder("INSTRUMENTATION");

}
