// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.utils;

/**
 * Code snippets for {@link ClientOptions}.
 */
public final class ClientOptionsJavadocCodeSnippets {
    /**
     * Codesnippets for {@link ClientOptions#setApplicationId(String)}.
     */
    public void setApplicationId() {
        // BEGIN: io.clientcore.core.util.ClientOptions.setApplicationId#String
        ClientOptions clientOptions = new ClientOptions()
            .setApplicationId("myApplicationId");
        // END: io.clientcore.core.util.ClientOptions.setApplicationId#String
    }
}
