// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.credential;

import io.clientcore.core.implementation.util.Base64Util;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

/**
 * <p>
 * The {@link BasicAuthenticationCredential} is used to authenticate and authorize requests made to
 * Azure services using the Basic authentication scheme. Basic Authentication is a simple authentication scheme
 * that uses a combination of a username and password.
 * </p>
 *
 * <p>
 * Note that Basic Authentication is generally considered less secure than other authentication methods,
 * such as Azure Active Directory (AAD) authentication. It is recommended to use
 * <a href="https://learn.microsoft.com/azure/active-directory/fundamentals/">Azure Active Directory (Azure AD)</a>
 * authentication via {@link TokenCredential} whenever possible, especially for production environments.
 * </p>
 *
 * <p>
 * <strong>Sample: Azure SAS Authentication</strong>
 * </p>
 *
 * <p>
 * The following code sample demonstrates the creation of a
 * {@link com.azure.core.v2.credential.BasicAuthenticationCredential}, using username and password
 * </p>
 *
 * <!-- src_embed com.azure.core.credential.basicAuthenticationCredential -->
 * <pre>
 * BasicAuthenticationCredential basicAuthenticationCredential =
 *     new BasicAuthenticationCredential&#40;&quot;&lt;username&gt;&quot;, &quot;&lt;password&gt;&quot;&#41;;
 * </pre>
 * <!-- end com.azure.core.credential.basicAuthenticationCredential -->
 *
 * @see com.azure.core.credential
 * @see com.azure.core.v2.credential.TokenCredential
 */
public class BasicAuthenticationCredential implements TokenCredential {
    /**
     * Base64 encoded username-password credential.
     */
    private final String encodedCredential;

    /**
     * Creates a basic authentication credential.
     *
     * @param username basic auth user name
     * @param password basic auth password
     */
    public BasicAuthenticationCredential(String username, String password) {
        String credential = username + ":" + password;
        this.encodedCredential = Base64Util.encodeToString(credential.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * @throws RuntimeException If the UTF-8 encoding isn't supported.
     */
    @Override
    public AccessToken getToken(TokenRequestContext request) {
        return new AccessToken(encodedCredential, OffsetDateTime.MAX);
    }
}
