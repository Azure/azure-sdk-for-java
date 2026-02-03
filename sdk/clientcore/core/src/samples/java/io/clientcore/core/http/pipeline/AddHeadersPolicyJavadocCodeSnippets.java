// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;

/**
 * Code snippets for {@link AddHeadersPolicy}.
 */
public final class AddHeadersPolicyJavadocCodeSnippets {
    public void createAddHeaderPolicy() {
        // BEGIN: io.clientcore.core.http.pipeline.AddHeaderPolicy.constructor
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaderName.USER_AGENT, "MyApp/1.0");
        headers.set(HttpHeaderName.CONTENT_TYPE, "application/json");

        AddHeadersPolicy policy = new AddHeadersPolicy(headers);
        // END: io.clientcore.core.http.pipeline.AddHeaderPolicy.constructor
    }
}
