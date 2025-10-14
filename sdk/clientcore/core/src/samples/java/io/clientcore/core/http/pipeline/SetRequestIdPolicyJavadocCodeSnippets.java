// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.models.HttpHeaderName;

/**
 * Code snippets for {@link RequestIdPolicy}.
 */
public final class SetRequestIdPolicyJavadocCodeSnippets {
    public void overrideRequestIdHeaderName() {
        // BEGIN: io.clientcore.core.http.pipeline.SetRequestIdPolicy.constructor
        RequestIdPolicy policy = new RequestIdPolicy(HttpHeaderName.fromString("my-request-id"));
        // END: io.clientcore.core.http.pipeline.SetRequestIdPolicy.constructor
    }
}
