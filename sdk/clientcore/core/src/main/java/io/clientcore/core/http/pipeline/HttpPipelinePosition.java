// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.http.pipeline;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.utils.ExpandableEnum;

/**
 * Enum representing where in an {@link HttpPipeline} a given {@link HttpPipelinePolicy} is positioned when added to an
 * {@link HttpPipelineBuilder}.
 * <p>
 * By default, policies are added at {@link #AFTER_RETRY} with the assumption of
 * {@link HttpPipelinePolicy policies} are idempotent and may need to be updated each time a network request is sent
 * (each try) and may mutate the results of authentication policies.
 * <p>
 * HttpPipelinePosition ensures that the creation of an {@link HttpPipeline} follows a strict positioning of policies.
 * Here is a visual representation of the order of policies in an {@link HttpPipeline}:
 * <pre>
 *     +------------------------+
 *     |    Before redirect     |
 *     +------------------------+
 *     |    Redirect Policy     |
 *     +------------------------+
 *     |     After redirect     |
 *     +------------------------+
 *     |      Retry Policy      |
 *     +------------------------+
 *     |      After retry       |
 *     +------------------------+
 *     | Authentication Policy  |
 *     +------------------------+
 *     |  After authentication  |
 *     +------------------------+
 *     | Instrumentation Policy |
 *     +------------------------+
 *     | After instrumentation  |
 *     +------------------------+
 * </pre>
 */
@Metadata(properties = MetadataProperties.IMMUTABLE)
public final class HttpPipelinePosition implements ExpandableEnum<Integer> {
    private final int value;

    private HttpPipelinePosition(Integer value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    /**
     * The policy will be position before the {@link HttpRedirectPolicy}.
     * <p>
     * If multiple {@link HttpPipelinePolicy policies} are added with this position, the first added will be the furthest
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
    public static final HttpPipelinePosition BEFORE_REDIRECT = new HttpPipelinePosition(1000);

    /**
     * The policy will be position after the {@link HttpRedirectPolicy}.
     * <p>
     * If multiple {@link HttpPipelinePolicy policies} are added with this position, they will be executed in the order
     * they were added after the {@link HttpRedirectPolicy}. Or, visually:
     * <pre>
     *     +-----------------+
     *     | Redirect Policy |
     *     +-----------------+
     *     | 1st added after |
     *     +-----------------+
     *     | 2nd added after |
     *     +-----------------+
     *     | 3rd added after |
     *     +-----------------+
     * </pre>
     */
    public static final HttpPipelinePosition AFTER_REDIRECT = new HttpPipelinePosition(3000);

    /**
     * The policy will be position after the {@link HttpRetryPolicy}.
     * <p>
     * If multiple {@link HttpPipelinePolicy policies} are added with this position, they will be executed in the order
     * they were added after the {@link HttpRetryPolicy}. Or, visually:
     * <pre>
     *     +---------------------+
     *     |    Retry Policy     |
     *     +---------------------+
     *     |   1st added after   |
     *     +---------------------+
     *     |   2nd added after   |
     *     +---------------------+
     *     |   3rd added after   |
     *     +---------------------+
     * </pre>
     */
    public static final HttpPipelinePosition AFTER_RETRY = new HttpPipelinePosition(5000);

    /**
     * The policy will be position after the {@link HttpCredentialPolicy}.
     * <p>
     * If multiple {@link HttpPipelinePolicy policies} are added with this position, they will be executed in the order
     * they were added after the {@link HttpCredentialPolicy}. Or, visually:
     * <pre>
     *     +-----------------------+
     *     | Authentication Policy |
     *     +-----------------------+
     *     |    1st added after    |
     *     +-----------------------+
     *     |    2nd added after    |
     *     +-----------------------+
     *     |    3rd added after    |
     *     +-----------------------+
     * </pre>
     */
    public static final HttpPipelinePosition AFTER_AUTHENTICATION = new HttpPipelinePosition(7000);

    /**
     * The policy will be position after the {@link HttpInstrumentationPolicy}.
     * <p>
     * If multiple {@link HttpPipelinePolicy policies} are added with this position, they will be executed in the order
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
    public static final HttpPipelinePosition AFTER_INSTRUMENTATION = new HttpPipelinePosition(9000);

    // Package-private HttpPipelinePosition constants for pillar policies.
    static final HttpPipelinePosition REDIRECT = new HttpPipelinePosition(2000);
    static final HttpPipelinePosition RETRY = new HttpPipelinePosition(4000);
    static final HttpPipelinePosition AUTHENTICATION = new HttpPipelinePosition(6000);
    static final HttpPipelinePosition INSTRUMENTATION = new HttpPipelinePosition(8000);

}
