// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.client.core.http.policy;

/**
 * Code snippets for {@link RequestIdPolicy}
 */
public class RequestIdPolicyJavaDocCodeSnippet {

    /**
     * Code snippets for using {@link RequestIdPolicy#RequestIdPolicy(String)} }
     */
    public void overrideRequestIdHeaderName() {

        // BEGIN: com.client.core.http.policy.RequestIdPolicy.constructor.overrideRequestIdHeaderName
        new RequestIdPolicy("x-ms-my-custom-request-id");
        // END: com.client.core.http.policy.RequestIdPolicy.constructor.overrideRequestIdHeaderName
    }
}
