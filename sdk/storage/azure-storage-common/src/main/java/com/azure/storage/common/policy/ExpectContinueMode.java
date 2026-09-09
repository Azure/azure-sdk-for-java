// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

/**
 * This type holds possible options for applying the HTTP header {@code Expect: 100-continue} to requests that carry a
 * body. They may be used with {@link ExpectContinueOptions}.
 */
public enum ExpectContinueMode {
    /**
     * Tells the pipeline to apply {@code Expect: 100-continue} only after the service has recently indicated that it is
     * under load, and to keep applying it until a period of time has passed since the last such response.
     *
     * <p>Response codes that trigger this behavior are 429, 500, and 503.</p>
     *
     * <p>This is the default behavior when no options are provided.</p>
     */
    APPLY_ON_THROTTLE,

    /**
     * Tells the pipeline to apply {@code Expect: 100-continue} regardless of recent error status. The header is still
     * subject to {@link ExpectContinueOptions#getContentLengthThreshold()}.
     */
    ON,

    /**
     * Tells the pipeline to never apply {@code Expect: 100-continue}.
     */
    OFF
}
