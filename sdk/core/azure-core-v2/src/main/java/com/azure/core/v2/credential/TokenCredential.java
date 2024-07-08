// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.credential;

/**
 * <p>
 * Token Credential interface serves as a fundamental component for managing and providing access tokens required for
 * <a href="https://learn.microsoft.com/azure/active-directory/fundamentals/">Azure Active Directory (Azure AD)</a>
 * authentication when making requests to Azure services.
 * </p>
 *
 * <p>
 * The {@link TokenCredential} interface, offers {@link TokenCredential#getToken(TokenRequestContext)}
 * and {@link TokenCredential#getToken(TokenRequestContext)} methods. These methods are responsible for
 * retrieving an access token that can be used to authenticate requests to Azure services. The scopes parameter
 * specified as part of {@link TokenRequestContext} represents the resources or permissions required for the
 * token.
 * </p>
 *
 * <p>
 * The Token Credential interface is implemented by various credential classes in the
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable">Azure
 * Identity</a>
 * library. These credential classes handle the authentication process and provide the necessary access tokens based on
 * the specified scopes and any additional configuration.
 * </p>
 *
 * <p>
 * By utilizing the Token Credential interface, you can abstract the authentication logic away from your
 * application code. This allows for flexibility in choosing authentication mechanisms and simplifies the management
 * of access tokens, including token caching and refreshing. It provides a consistent approach to authenticate requests
 * across different Azure services and libraries.
 * </p>
 *
 * <p>
 * Here are some examples of credential classes that implement the Token Credential interface:
 * </p>
 *
 * <ul>
 * <li><a href=
 * "https://learn.microsoft.com/java/api/com.azure.identity.defaultazurecredential?view=azure-java-stable">DefaultAzureCredential</a>:
 * Represents a credential that tries a series of authentication methods to
 * authenticate requests automatically. It simplifies the process by automatically selecting an appropriate
 * authentication mechanism based on the environment, such as environment variables, managed identities, and
 * developer tool credentials.</li>
 *
 * <li><a href=
 * "https://learn.microsoft.com/java/api/com.azure.identity.clientsecretcredential?view=azure-java-stable">ClientSecretCredential</a>:
 * Represents a credential that uses a client ID, client secret, and tenant
 * ID to authenticate. It is suitable for scenarios where you have a client application that needs to authenticate
 * with Azure services using a client secret.</li>
 *
 * <li><a href=
 * "https://learn.microsoft.com/java/api/com.azure.identity.clientcertificatecredential?view=azure-java-stable">ClientCertificateCredential</a>:
 * Represents a credential that uses a client ID, client certificate, and
 * tenant ID for authentication. This credential is useful when your client application has a client certificate
 * available for authentication.</li>
 *
 * <li><a href=
 * "https://learn.microsoft.com/java/api/com.azure.identity.interactivebrowsercredential?view=azure-java-stable">InteractiveBrowserCredential</a>:
 * Represents a credential that performs an interactive authentication
 * flow with the user in a browser. It is useful for scenarios where the user needs to provide consent or
 * multi-factor authentication is required.</li>
 * </ul>
 *
 * <p>
 * You can find more credential classes that implement the {@link TokenCredential} interface in our
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable">Azure
 * Identity</a>
 * library.
 * </p>
 *
 * <p>
 * These credential classes can be used in combination with various Azure client libraries to authenticate requests
 * and access Azure services without the need to manage access tokens manually. The Token Credential interface provides
 * a consistent way to handle Azure Active Directory (AAD) authentication across different Azure services and SDKs in
 * a secure and efficient manner.
 * </p>
 *
 * @see com.azure.core.credential
 */
@FunctionalInterface
public interface TokenCredential {
    /**
     * Synchronously get a token for a given resource/audience.
     *
     * This method is called automatically by Azure SDK client libraries.
     * You may call this method directly, but you must also handle token
     * caching and token refreshing.
     *
     * @param request the details of the token request
     * @return The Access Token
     */
    AccessToken getToken(TokenRequestContext request);
}
