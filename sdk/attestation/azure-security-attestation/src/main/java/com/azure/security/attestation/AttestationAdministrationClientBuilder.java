// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.security.attestation.implementation.AttestationClientImpl;
import com.azure.security.attestation.implementation.AttestationClientImplBuilder;
import com.azure.security.attestation.models.AttestationPolicySetOptions;
import com.azure.security.attestation.models.AttestationTokenValidationOptions;
import com.azure.security.attestation.models.AttestationType;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/** This class provides a fluent builder API to help add in the configuration and instantiation of the
 * administrative APIs implemented by the Attestation Service:
 * {@link com.azure.security.attestation.AttestationAdministrationClient} and
 * {@link com.azure.security.attestation.AttestationAdministrationAsyncClient} classes calling the
 * {@link AttestationClientBuilder#buildClient()} or {@link AttestationClientBuilder#buildAsyncClient()}.
 *
 * <p>
 * More information on attestation policies can be found <a href='https://docs.microsoft.com/azure/attestation/basic-concepts#attestation-policy'>here</a>
 * </p>
 *
 * There are two main families of APIs available from the Administration client.
 * <ul>
 *     <li>Attestation Policy Management</li>
 *     <li>Policy Management Certificate Management</li>
 * </ul>
 *
 * The Policy Management APIs provide the ability to retrieve, modify and reset attestation policies.
 * The policy management APIs are:
 * <ul>
 *     <li>
 * {@link AttestationAdministrationClient#getAttestationPolicy(AttestationType)}
 * </li>
 *     <li>
 * {@link AttestationAdministrationAsyncClient#getAttestationPolicy(AttestationType)}
 * </li>
 *     <li>
 * {@link AttestationAdministrationClient#setAttestationPolicy(AttestationType, AttestationPolicySetOptions)}
 * </li>
 *     <li>
 * {@link AttestationAdministrationAsyncClient#setAttestationPolicy(AttestationType, AttestationPolicySetOptions)}
 * </li>
 *     <li>
 * {@link AttestationAdministrationClient#resetAttestationPolicy(AttestationType, AttestationPolicySetOptions)}
 * </li>
 *     <li>
 * {@link AttestationAdministrationAsyncClient#resetAttestationPolicy(AttestationType, AttestationPolicySetOptions)}
 * </li>
 * </ul>
 * <p>
 *     The Policy Management Certificate APIs provide the ability to manage the certificates which are
 *     used to establish authorization for Isolated mode attestation service instances. They include apis to
 *     enumerate, add and remove policy management certificates.
 * </p>
 *
 * <p>The minimal configuration options required by {@link AttestationClientBuilder} are:
 * <ul>
 *     <li>A {@link String} endpoint.</li>
 *     <li>A {@link TokenCredential} object.</li>
 * </ul>
 *
 * <p><strong>Instantiate a synchronous Attestation Client</strong></p>
 * <!-- src_embed com.azure.security.attestation.AttestationAdministrationClientBuilder.buildClient -->
 * <pre>
 * AttestationAdministrationClient client = new AttestationAdministrationClientBuilder&#40;&#41;
 *     .endpoint&#40;endpoint&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.security.attestation.AttestationAdministrationClientBuilder.buildClient -->
 * <!-- src_embed com.azure.security.attestation.AttestationAdministrationClientBuilder.buildAsyncClient -->
 * <pre>
 * AttestationAdministrationAsyncClient asyncClient = new AttestationAdministrationClientBuilder&#40;&#41;
 *     .endpoint&#40;endpoint&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.security.attestation.AttestationAdministrationClientBuilder.buildAsyncClient -->
 */
@ServiceClientBuilder(
        serviceClients = {
            AttestationAdministrationClient.class,
            AttestationAdministrationAsyncClient.class,
        })
public final class AttestationAdministrationClientBuilder {
    private static final String SDK_NAME = "name";

    private static final String SDK_VERSION = "version";

    private final String[] dataplaneScope = new String[] {"https://attest.azure.net/.default"};

    private final AttestationClientImplBuilder clientImplBuilder;
    private final ClientLogger logger = new ClientLogger(AttestationAdministrationClientBuilder.class);

    private AttestationServiceVersion serviceVersion;
    private AttestationTokenValidationOptions tokenValidationOptions;
    private TokenCredential tokenCredential = null;

    /**
     * Creates a new instance of the AttestationClientBuilder class.
     */
    public AttestationAdministrationClientBuilder() {

        clientImplBuilder = new AttestationClientImplBuilder();
        serviceVersion = AttestationServiceVersion.V2020_10_01;
        tokenValidationOptions = new AttestationTokenValidationOptions();
    }

    /**
     * Sets The attestation endpoint URI, for example https://mytenant.attest.azure.net.
     *
     * @param endpoint The endpoint to connect to.
     * @return the AttestationClientBuilder.
     */
    public AttestationAdministrationClientBuilder endpoint(String endpoint) {
        Objects.requireNonNull(endpoint);
        try {
            new URL(endpoint);
        } catch (MalformedURLException ex) {
            logger.logExceptionAsError(new IllegalArgumentException(ex));
        }
        clientImplBuilder.instanceUrl(endpoint);
        return this;
    }

    /**
     * Sets the desired API version for this attestation client.
     * @param serviceVersion Specifies the API version to use in the outgoing API calls.
     * @return the AttestationClientBuilder.
     */
    public AttestationAdministrationClientBuilder serviceVersion(AttestationServiceVersion serviceVersion) {
        Objects.requireNonNull(serviceVersion);
        this.serviceVersion = serviceVersion;
        return this;
    }
    /**
     * Sets the credential to be used for communicating with the service.
     * @param credential Specifies the credential to be used for authentication.
     * @return the AttestationClientBuilder.
     */
    public AttestationAdministrationClientBuilder credential(TokenCredential credential) {
        Objects.requireNonNull(credential);
        this.tokenCredential = credential;
        return this;
    }

    /**
     * Sets The HTTP pipeline to send requests through.
     *
     * @param pipeline the pipeline value.
     * @return the AttestationClientBuilder.
     */
    public AttestationAdministrationClientBuilder pipeline(HttpPipeline pipeline) {
        clientImplBuilder.pipeline(pipeline);
        return this;
    }

    /**
     * Sets The serializer to serialize an object into a string.
     *
     * @param serializerAdapter the serializerAdapter value.
     * @return the AttestationClientBuilder.
     */
    public AttestationAdministrationClientBuilder serializerAdapter(SerializerAdapter serializerAdapter) {
        clientImplBuilder.serializerAdapter(serializerAdapter);
        return this;
    }

    /**
     * Sets The HTTP client used to send the request.
     *
     * @param httpClient the httpClient value.
     * @return the AttestationClientBuilder.
     */
    public AttestationAdministrationClientBuilder httpClient(HttpClient httpClient) {
        clientImplBuilder.httpClient(httpClient);
        return this;
    }

    /**
     * Sets The configuration store that is used during construction of the service client.
     *
     * @param configuration the configuration value.
     * @return the AttestationClientBuilder.
     */
    public AttestationAdministrationClientBuilder configuration(Configuration configuration) {
        clientImplBuilder.configuration(configuration);
        return this;
    }

    /**
     * Sets The logging configuration for HTTP requests and responses.
     *
     * @param httpLogOptions the httpLogOptions value.
     * @return the AttestationClientBuilder.
     */
    public AttestationAdministrationClientBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
        clientImplBuilder.httpLogOptions(httpLogOptions);
        return this;
    }

    /**
     * Sets The retry policy that will attempt to retry failed requests, if applicable.
     *
     * @param retryPolicy the retryPolicy value.
     * @return the AttestationClientBuilder.
     */
    public AttestationAdministrationClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        clientImplBuilder.retryPolicy(retryPolicy);
        return this;
    }

    /**
     * Adds a custom Http pipeline policy.
     *
     * @param customPolicy The custom Http pipeline policy to add.
     * @return this {@link AttestationAdministrationClientBuilder}.
     */
    public AttestationAdministrationClientBuilder addPolicy(HttpPipelinePolicy customPolicy) {
        clientImplBuilder.addPolicy(customPolicy);
        return this;
    }

    /**
     * Sets {@link com.azure.security.attestation.models.AttestationToken} validation options for clients created from this builder.
     * <p>Because attestation service clients need to have the ability to validate that the data returned by the attestation
     * service actually originated from within the service, most Attestation Service APIs embed their response in a
     * <a href=https://datatracker.ietf.org/doc/html/rfc7519>RFC 7519 JSON Web Token</a>.</p>
        * <p>The {@link AttestationTokenValidationOptions} provides a mechanism for a client to customize the validation
     * of responses sent by the attestation service.</p>
        * <p>The {@code tokenValidationOptions} property sets the default validation options used by the {@link AttestationClient}
     * or {@link AttestationAsyncClient} returned from this builder.</p>
        * <p>Note: most APIs allow this value to be overridden on a per-api basis if that flexibility is needed.</p>
        *
        * <!-- src_embed com.azure.security.attestation.AttestationAdministrationClientBuilder.buildClientWithValidation -->
        * <pre>
        * AttestationAdministrationClient validatedClient = new AttestationAdministrationClientBuilder&#40;&#41;
        *     .endpoint&#40;endpoint&#41;
        *     .tokenValidationOptions&#40;new AttestationTokenValidationOptions&#40;&#41;
        *         .setValidationSlack&#40;Duration.ofSeconds&#40;10&#41;&#41; &#47;&#47; Allow 10 seconds of clock drift between attestation service and client.
        *         .setValidationCallback&#40;&#40;token, signer&#41; -&gt; &#123; &#47;&#47; Perform custom validation steps.
        *             System.out.printf&#40;&quot;Validate token signed by signer %s&#92;n&quot;, signer.getCertificates&#40;&#41;.get&#40;0&#41;.getSubjectDN&#40;&#41;.toString&#40;&#41;&#41;;
        *         &#125;&#41;&#41;
        *     .buildClient&#40;&#41;;
        * </pre>
        *     <!-- end com.azure.security.attestation.AttestationAdministrationClientBuilder.buildClientWithValidation -->
     * @param tokenValidationOptions - Validation options used when validating JSON Web Tokens returned by the attestation service.
     * @return this {@link AttestationAdministrationClientBuilder}
     */
    public AttestationAdministrationClientBuilder tokenValidationOptions(AttestationTokenValidationOptions tokenValidationOptions) {
        this.tokenValidationOptions = tokenValidationOptions;
        return this;
    }

    /**
     * Builds an instance of AttestationClient sync client.
     *
     * Instantiating a synchronous Attestation client:
     * <br>
     * <!-- src_embed com.azure.security.attestation.AttestationAdministrationClientBuilder.buildClient -->
     * <pre>
     * AttestationAdministrationClient client = new AttestationAdministrationClientBuilder&#40;&#41;
     *     .endpoint&#40;endpoint&#41;
     *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
     *     .buildClient&#40;&#41;;
     * </pre>
     * <!-- end com.azure.security.attestation.AttestationAdministrationClientBuilder.buildClient -->
     * @return an instance of {@link AttestationClient}.
     */
    public AttestationAdministrationClient buildClient() {
        return new AttestationAdministrationClient(buildAsyncClient());
    }

    /**
     * Builds an instance of AttestationAsyncClient async client.
     *
     * Instantiating a synchronous Attestation client:
     * <br>
     * <!-- src_embed com.azure.security.attestation.AttestationAdministrationClientBuilder.buildAsyncClient -->
     * <pre>
     * AttestationAdministrationAsyncClient asyncClient = new AttestationAdministrationClientBuilder&#40;&#41;
     *     .endpoint&#40;endpoint&#41;
     *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
     *     .buildAsyncClient&#40;&#41;;
     * </pre>
     * <!-- end com.azure.security.attestation.AttestationAdministrationClientBuilder.buildAsyncClient -->
     * @return an instance of {@link AttestationClient}.
     */
    public AttestationAdministrationAsyncClient buildAsyncClient() {
        return new AttestationAdministrationAsyncClient(buildInnerClient(), this.tokenValidationOptions);
    }

    /**
     * Legacy API surface which will be removed shortly.
     */


    /**
     * Builds an instance of AttestationClientImpl with the provided parameters.
     *
     * @return an instance of AttestationClientImpl.
     */
    private AttestationClientImpl buildInnerClient() {
        AttestationServiceVersion version = serviceVersion != null ? serviceVersion : AttestationServiceVersion.getLatest();
        clientImplBuilder.apiVersion(version.getVersion());
        if (tokenCredential != null) {
            clientImplBuilder.addPolicy(new BearerTokenAuthenticationPolicy(tokenCredential, dataplaneScope));
        }
        return clientImplBuilder.buildClient();
    }
}
