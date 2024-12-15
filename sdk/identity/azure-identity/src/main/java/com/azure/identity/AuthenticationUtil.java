// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.util.Context;

import java.util.function.Supplier;

/**
 * Utility methods for working with authentication.
 */
public final class AuthenticationUtil {

    private AuthenticationUtil() {
    }

    /**
     * Creates a {@link Supplier} that provides a Bearer token from the specified credential.
     * The token is cached and will refresh when it expires.
     * <p><strong>Using the supplier:</strong></p>
     * <!-- src_embed com.azure.identity.util.getBearerTokenSupplier -->
     * <pre>
     * DefaultAzureCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
     * String scope = &quot;https:&#47;&#47;cognitiveservices.azure.com&#47;.default&quot;;
     * Supplier&lt;String&gt; supplier = AuthenticationUtil.getBearerTokenSupplier&#40;credential, scope&#41;;
     *
     * &#47;&#47; This example simply uses the Azure SDK HTTP library to demonstrate setting the header.
     * &#47;&#47; Use the token as is appropriate for your circumstances.
     * HttpRequest request = new HttpRequest&#40;HttpMethod.GET, &quot;https:&#47;&#47;www.example.com&quot;&#41;;
     * request.setHeader&#40;HttpHeaderName.AUTHORIZATION, &quot;Bearer &quot; + supplier.get&#40;&#41;&#41;;
     * HttpClient client = HttpClient.createDefault&#40;&#41;;
     * client.sendSync&#40;request, Context.NONE&#41;;
     * </pre>
     * <!-- end com.azure.identity.util.getBearerTokenSupplier -->
     *
     * @param credential The {@link TokenCredential} from which to retrieve a token.
     * @param scopes The scopes as appropriate for the token you are retrieving.
     * @return A {@link Supplier} which returns the bearer token as a {@link String}.
     */
    public static Supplier<String> getBearerTokenSupplier(TokenCredential credential, String... scopes) {
        HttpPipeline pipeline
            = new HttpPipelineBuilder().policies(new BearerTokenAuthenticationPolicy(credential, scopes)).build();
        return () -> {
            // This request will never need to go anywhere; it is simply to cause the policy to interact with
            // the user's credential
            HttpRequest req = new HttpRequest(HttpMethod.GET, "https://www.example.com");
            try (HttpResponse res = pipeline.sendSync(req, Context.NONE)) {
                return res.getRequest().getHeaders().get(HttpHeaderName.AUTHORIZATION).getValue().split(" ")[1];
            }
        };
    }
}
