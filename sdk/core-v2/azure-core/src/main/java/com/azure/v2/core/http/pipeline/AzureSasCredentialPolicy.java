// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.http.pipeline;

import com.azure.v2.core.credentials.AzureSasCredential;
import io.clientcore.core.http.pipeline.HttpCredentialPolicy;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineNextPolicy;
import io.clientcore.core.http.pipeline.HttpPipelinePolicy;
import io.clientcore.core.models.binarydata.BinaryData;

import java.util.Objects;

/**
 * The {@code AzureSasCredentialPolicy} class is an implementation of the {@link HttpPipelinePolicy} interface. This
 * policy uses an {@link AzureSasCredential} to append a shared access signature (SAS) to the query string of a
 * request.
 *
 * <p>This class is useful when you need to authorize requests with a SAS from Azure. It ensures that the requests are
 * sent over HTTPS to prevent the SAS from being leaked.</p>
 *
 * <p><strong>Code sample:</strong></p>
 *
 * <p>In this example, an {@code AzureSasCredentialPolicy} is created with a SAS. The policy can then added to the
 * pipeline. The request sent by the pipeline will then include the SAS appended to its query string.</p>
 * <!-- src_embed com.azure.core.http.policy.AzureSasCredentialPolicy.constructor -->
 * <!-- end com.azure.core.http.policy.AzureSasCredentialPolicy.constructor -->
 *
 * @see com.azure.v2.core.http.pipeline
 * @see HttpPipelinePolicy
 * @see AzureSasCredential
 * @see HttpPipeline
 * @see HttpRequest
 * @see Response
 */
public final class AzureSasCredentialPolicy extends HttpCredentialPolicy {
    private static final ClientLogger LOGGER = new ClientLogger(AzureSasCredentialPolicy.class);
    private final AzureSasCredential credential;
    private final boolean requireHttps;

    /**
     * Creates a policy that uses the passed {@link AzureSasCredential} to append sas to query string.
     * <p>
     * Requests sent with this pipeline policy are required to use {@code HTTPS}. If the request isn't using
     * {@code HTTPS} an exception will be thrown to prevent leaking the shared access signature.
     *
     * @param credential The {@link AzureSasCredential} containing the shared access signature to use.
     *
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public AzureSasCredentialPolicy(AzureSasCredential credential) {
        this(credential, true);
    }

    /**
     * Creates a policy that uses the passed {@link AzureSasCredential} to append sas to query string.
     *
     * @param credential The {@link AzureSasCredential} containing the shared access signature to use.
     * @param requireHttps A flag indicating whether {@code HTTPS} is required.
     *
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public AzureSasCredentialPolicy(AzureSasCredential credential, boolean requireHttps) {
        Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.credential = credential;
        this.requireHttps = requireHttps;
    }

    @Override
    public Response<BinaryData> process(HttpRequest httpRequest, HttpPipelineNextPolicy next) {

        if (requireHttps && !"https".equals(httpRequest.getUri().getScheme())) {
            throw LOGGER.throwableAtError()
                .log("Shared access signature credentials require HTTPS to prevent leaking"
                    + " the shared access signature.", IllegalStateException::new);
        }

        String signature = credential.getSignature();
        if (signature.startsWith("?")) {
            signature = signature.substring(1);
        }

        String query = httpRequest.getUri().getQuery();
        String url = httpRequest.getUri().toString();
        if (query == null || query.isEmpty()) {
            if (url.endsWith("?")) {
                url = url + signature;
            } else {
                url = url + "?" + signature;
            }
        } else {
            url = url + "&" + signature;
        }
        httpRequest.setUri(url);

        return next.process();
    }
}
