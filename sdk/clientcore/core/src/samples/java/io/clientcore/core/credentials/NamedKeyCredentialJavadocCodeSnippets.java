// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.credentials;

/**
 * Code snippets for {@link NamedKeyCredential}.
 */
public final class NamedKeyCredentialJavadocCodeSnippets {
    public void namedKeyCredentialClassJavadocExample() {
        // BEGIN: io.clientcore.core.credential.NamedKeyCredential.constructor
        // Create a named credential for a service.
        NamedKeyCredential namedKeyCredential = new NamedKeyCredential("SERVICE-KEY-NAME", "SERVICE-KEY");
        // END: io.clientcore.core.credential.NamedKeyCredential.constructor
    }
}
