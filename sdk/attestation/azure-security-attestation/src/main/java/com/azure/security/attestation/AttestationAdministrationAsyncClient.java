// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.security.attestation.implementation.AttestationClientImpl;
import com.azure.security.attestation.implementation.PoliciesImpl;
import com.azure.security.attestation.implementation.PolicyCertificatesImpl;
import com.azure.security.attestation.implementation.SigningCertificatesImpl;
import com.azure.security.attestation.implementation.models.AttestationSignerImpl;
import com.azure.security.attestation.implementation.models.AttestationTokenImpl;
import com.azure.security.attestation.implementation.models.StoredAttestationPolicy;
import com.azure.security.attestation.models.AttestationPolicySetOptions;
import com.azure.security.attestation.models.AttestationSigner;
import com.azure.security.attestation.models.AttestationToken;
import com.azure.security.attestation.models.AttestationTokenValidationOptions;
import com.azure.security.attestation.models.AttestationType;
import com.azure.security.attestation.models.PolicyResult;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.core.util.FluxUtil.withContext;

/**
 /**
 * The AttestationAdministrationClient provides access to the administrative policy APIs
 * implemented by the Attestation Service.
 *
 * More information on attestation policies can be found <a href='https://docs.microsoft.com/en-us/azure/attestation/basic-concepts#attestation-policy'>here</a>
 *
 * There are two main families of APIs available from the Administration client.
 * <ul>
 *     <li>Attestation Policy Management</li>
 *     <li>Attestation Policy Management Certificate Management</li>
 * </ul>
 *
 * Attestation service instances operate in three different modes:
 * <ul>
 *     <li>Shared - a shared instance is a regional instance which is available to all customers.
 *     It does NOT support customer specified policy documents - there is only a default policy available
 *     for each attestation type </li>
 *     <li>AAD - An attestation instance where the customer trusts Azure Active Directory (and Azure
 *     Role Based Access Control) to manage the security of their enclave. </li>
 *     <li>Isolated - an attestation instance where the customer does *not* trust Azure Active Directory
 *     (and RBAC) to manage the security of their enclave </li>
 * </ul>
 *
 * When an attestation instance is in Isolated mode, additional proof needs to be provided by the customer
 * to verify that they are authorized to perform the operation specified.
 *
 * When presented to the attestation service (via the <a href='https://docs.microsoft.com/en-us/rest/api/attestation/policy/set'>SetPolicy REST API</a>),
 * the client sends a JSON Web Token to the service. If the service is in AAD mode, this JSON Web Token can
 * be an unsecured attestation token, or it can be signed with a key of the customer's choice.
 *
 * If the service is in Isolated mode, this JSON Web Token *must* be a token signed by the private key
 * associated with one of the policy management certificates specified by
 * the @link AttestationAdministrationClient#getPolicyManagementSigners} API. This verifies that the caller
 * is in possession of a private key associated with the instance and is thus authorized to make changes to policy.
 *
 * The Java SDK simplifies the experience of creating the Attestation Policy JWT used to sign
 *
 */
@ServiceClient(builder = AttestationClientBuilder.class, isAsync = true)
public final class AttestationAdministrationAsyncClient {
    private final SigningCertificatesImpl signingCertificatesImpl;
    private final PoliciesImpl policyImpl;
    private final PolicyCertificatesImpl certificatesImpl;
    private final ClientLogger logger;
    private final AttestationTokenValidationOptions tokenValidationOptions;
    private final AtomicReference<List<AttestationSigner>> cachedSigners;
    private static final SerializerAdapter SERIALIZER_ADAPTER = JacksonAdapter.createDefaultSerializerAdapter();

    /**
     * Initializes an instance of Attestations client.
     *
     * @param clientImpl the service client implementation.
     */
    AttestationAdministrationAsyncClient(AttestationClientImpl clientImpl, AttestationTokenValidationOptions tokenValidationOptions) {
        this.signingCertificatesImpl = clientImpl.getSigningCertificates();
        this.policyImpl = clientImpl.getPolicies();
        this.certificatesImpl = clientImpl.getPolicyCertificates();
        this.tokenValidationOptions = tokenValidationOptions;
        this.logger = new ClientLogger(AttestationAdministrationAsyncClient.class);
        this.cachedSigners = new AtomicReference<>(null);
    }
//region Get Attestation Policy.
    /**
     * Retrieves the current policy for an attestation type.
     *
     * @param attestationType Specifies the trusted execution environment whose policy should be retrieved.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the attestation policy expressed as a string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<String>> getAttestationPolicyWithResponse(AttestationType attestationType) {
        return withContext(context -> getAttestationPolicyWithResponse(attestationType, context));
    }

    /**
     * Retrieves the current policy for an attestation type.
     *
     * @param attestationType Specifies the trusted execution environment whose policy should be retrieved.
     * @param context Context for the remote call.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the attestation policy expressed as a string.
     */
    public Mono<Response<String>> getAttestationPolicyWithResponse(AttestationType attestationType, Context context) {

        return this.policyImpl.getWithResponseAsync(attestationType, context)
            .flatMap(response -> {
                Response<AttestationTokenImpl> token = Utilities.generateResponseFromModelType(response, new AttestationTokenImpl(response.getValue().getToken()));
                return getCachedAttestationSigners()
                    .map(signers -> {
                        token.getValue().validate(signers, this.tokenValidationOptions);
                        String policyJwt = token.getValue().getBody(PolicyResult.class).getPolicy();
                        AttestationTokenImpl policyToken = new AttestationTokenImpl(policyJwt);
                        StoredAttestationPolicy storedPolicy = policyToken.getBody(StoredAttestationPolicy.class);
                        String policy = null;
                        // If there's a stored attestation policy in the token, convert it to a string.
                        if (storedPolicy != null) {
                            policy = new String(storedPolicy.getAttestationPolicy(), StandardCharsets.UTF_8);
                        } else {
                            policy = null;
                        }
                        return Utilities.generateAttestationResponseFromModelType(token, policyToken, policy);
                    });
            });
    }

    /**
     * Retrieves the current policy for an attestation type.
     *
     * @param attestationType Specifies the trusted execution environment to be used to validate the evidence.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response to an attestation policy operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<String> getAttestationPolicy(AttestationType attestationType) {
        return getAttestationPolicyWithResponse(attestationType)
            .flatMap(FluxUtil::toMono);

    }
//endregion
//region Set Attestation Policy
    /**
     * Sets the current policy for an attestation type.
     *
     * @param attestationType Specifies the trusted execution environment to be used to validate the evidence.
     * @param options Options for the setPolicy operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response to an attestation policy operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PolicyResult>> setAttestationPolicyWithResponse(AttestationType attestationType, AttestationPolicySetOptions options) {
        return withContext(context -> setAttestationPolicyWithResponse(attestationType, options, context));
    }

    /**
     * Sets the current policy for an attestation type.
     *
     * @param attestationType Specifies the trusted execution environment to be used to validate the evidence.
     * @param options Options for the setPolicy operation, including the new policy to be set.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response to an attestation policy operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PolicyResult> setAttestationPolicy(AttestationType attestationType, AttestationPolicySetOptions options) {
        return setAttestationPolicyWithResponse(attestationType, options)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Sets the current policy for an attestation type.
     *
     * @param attestationType Specifies the trusted execution environment to be used to validate the evidence.
     * @param options Options for setPolicy API, including policy to set and signing key.
     * @param context Context for the operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response to an attestation policy operation.
     */
    public Mono<Response<PolicyResult>> setAttestationPolicyWithResponse(AttestationType attestationType, AttestationPolicySetOptions options, Context context) {
        // Ensure that the incoming request makes sense.
        AttestationTokenValidationOptions validationOptions = options.getValidationOptions();
        if (validationOptions == null) {
            validationOptions = this.tokenValidationOptions;
        }

        final AttestationTokenValidationOptions finalOptions = validationOptions;

        StoredAttestationPolicy policyToSet = new StoredAttestationPolicy();
        policyToSet.setAttestationPolicy(options.getAttestationPolicy().getBytes(StandardCharsets.UTF_8));

        // Serialize the StoredAttestationPolicy.
        String serializedPolicy = null;
        try {
            serializedPolicy = SERIALIZER_ADAPTER.serialize(policyToSet, SerializerEncoding.JSON);
        } catch (IOException e) {
            throw logger.logExceptionAsError(new RuntimeException(e.getMessage()));
        }

        // And generate an attestation token for that stored attestation policy.
        AttestationToken setToken;
        if (options.getAttestationSigner() == null) {
            setToken = AttestationTokenImpl.createUnsecuredToken(serializedPolicy);
        } else {
            setToken = AttestationTokenImpl.createSecuredToken(serializedPolicy, options.getAttestationSigner());
        }
        return this.policyImpl.setWithResponseAsync(attestationType, setToken.serialize(), context)
            .flatMap(response -> {
                Response<AttestationTokenImpl> token = Utilities.generateResponseFromModelType(response, new AttestationTokenImpl(response.getValue().getToken()));
                return getCachedAttestationSigners()
                    .map(signers -> {
                        token.getValue().validate(signers, finalOptions);
                        return Utilities.generateAttestationResponseFromModelType(response, token.getValue(), token.getValue().getBody(PolicyResult.class));
                    });
            });
    }

//endregion

    /**
     * Return cached attestation signers, fetching from the internet if needed.
     * @return cached signers.
     */
    Mono<List<AttestationSigner>> getCachedAttestationSigners() {
        if (this.cachedSigners.get() != null) {
            return Mono.just(this.cachedSigners.get());
        } else {
            return this.signingCertificatesImpl.getAsync()
                .map(AttestationSignerImpl::attestationSignersFromJwks)
                .map(signers -> {
                    this.cachedSigners.compareAndSet(null, signers);
                    return this.cachedSigners.get();
                });
        }
    }


}
