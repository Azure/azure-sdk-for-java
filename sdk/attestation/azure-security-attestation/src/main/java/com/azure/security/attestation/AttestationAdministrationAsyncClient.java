// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
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
import com.azure.security.attestation.implementation.models.AttestationCertificateManagementBody;
import com.azure.security.attestation.implementation.models.AttestationSignerImpl;
import com.azure.security.attestation.implementation.models.AttestationTokenImpl;
import com.azure.security.attestation.implementation.models.JsonWebKey;
import com.azure.security.attestation.implementation.models.JsonWebKeySet;
import com.azure.security.attestation.implementation.models.PolicyCertificatesModificationResultImpl;
import com.azure.security.attestation.implementation.models.PolicyResultImpl;
import com.azure.security.attestation.implementation.models.StoredAttestationPolicy;
import com.azure.security.attestation.models.AttestationPolicySetOptions;
import com.azure.security.attestation.models.AttestationResponse;
import com.azure.security.attestation.models.AttestationSigner;
import com.azure.security.attestation.models.AttestationSigningKey;
import com.azure.security.attestation.models.AttestationToken;
import com.azure.security.attestation.models.AttestationTokenValidationOptions;
import com.azure.security.attestation.models.AttestationType;
import com.azure.security.attestation.models.PolicyCertificatesModificationResult;
import com.azure.security.attestation.models.PolicyManagementCertificateOptions;
import com.azure.security.attestation.models.PolicyResult;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.core.util.FluxUtil.withContext;

/**
 * The AttestationAdministrationClient provides access to the administrative policy APIs
 * implemented by the Attestation Service.
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
 *<p>
 * When an attestation instance is in Isolated mode, additional proof needs to be provided by the customer
 * to verify that they are authorized to perform the operation specified.
 *</p>
 * <p>
 *     When an Isolated mode attestation instance is created, the creator provides an X.509 certificate
 *     which forms the set of policy management certificates. Under the covers, each  {@link AttestationAdministrationAsyncClient#setAttestationPolicy(AttestationType, AttestationPolicySetOptions)}.
 *     API call must be signed with the private key which is associated with one of the policy management
 *     certificates. This signing operation allows the attestation service to verify that the caller is
 *     in possession of a private key which has been authorized to add or reset policies, or to modify
 *     the set of attestation policy certificates.
 * </p>
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

    /**
     * Retrieves the current policy for an attestation type.
     * <p>
     * <b>NOTE:</b>
     *     The {@link AttestationAdministrationAsyncClient#getAttestationPolicyWithResponse(AttestationType, AttestationTokenValidationOptions, Context)} API returns the underlying
     *     attestation policy specified by the user. This is NOT the full attestation policy maintained by
     *     the attestation service. Specifically it does not include the signing certificates used to verify the attestation
     *     policy.
     *     </p>
     *     <p>
     *         To retrieve the signing certificates used to sign the policy, {@link Response} object returned from this API
     *         is an instance of an {@link com.azure.security.attestation.models.AttestationResponse} object
     *         and the caller can retrieve the full policy object maintained by the service by calling the
     *         {@link AttestationResponse#getToken()} method.
     *         The returned {@link com.azure.security.attestation.models.AttestationToken} object will be
     *         the value stored by the attestation service.
     *  </p>
     *
     *
     * @param attestationType Specifies the trusted execution environment whose policy should be retrieved.
     * @param validationOptions Options used to validate the response returned by the attestation service.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the attestation policy expressed as a string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<String>> getAttestationPolicyWithResponse(AttestationType attestationType, AttestationTokenValidationOptions validationOptions) {
        return withContext(context -> getAttestationPolicyWithResponse(attestationType, validationOptions, context));
    }

    /**
     * Retrieves the current policy for an attestation type.
     *  <p>
     *      <b>NOTE:</b>
     *     The {@link AttestationAdministrationAsyncClient#getAttestationPolicy(AttestationType)} API returns the underlying
     *     attestation policy specified by the user. This is NOT the full attestation policy maintained by
     *     the attestation service. Specifically it does not include the signing certificates used to verify the attestation
     *     policy.
     *     </p>
     *     <p>
     *         To retrieve the signing certificates used to sign the policy, use the {@link AttestationAdministrationAsyncClient#getAttestationPolicyWithResponse(AttestationType, AttestationTokenValidationOptions)} API.
     *         The {@link Response} object is an instance of an {@link com.azure.security.attestation.models.AttestationResponse} object
     *         and the caller can retrieve the full information maintained by the service by calling the {@link AttestationResponse#getToken()} method.
     *         The returned {@link com.azure.security.attestation.models.AttestationToken} object will be
     *         the value stored by the attestation service.
     *  </p>
     *
     *
     * @param attestationType Specifies the trusted execution environment to be used to validate the evidence.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response to an attestation policy operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<String> getAttestationPolicy(AttestationType attestationType) {
        return getAttestationPolicyWithResponse(attestationType, null)
            .flatMap(FluxUtil::toMono);

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
    Mono<Response<String>> getAttestationPolicyWithResponse(AttestationType attestationType, AttestationTokenValidationOptions validationOptions, Context context) {
        final AttestationTokenValidationOptions validationOptionsToUse = (validationOptions != null ? validationOptions : this.tokenValidationOptions);
        return this.policyImpl.getWithResponseAsync(attestationType, context)
            .flatMap(response -> {
                Response<AttestationTokenImpl> token = Utilities.generateResponseFromModelType(response, new AttestationTokenImpl(response.getValue().getToken()));
                return getCachedAttestationSigners()
                    .map(signers -> {
                        token.getValue().validate(signers, validationOptionsToUse);
                        String policyJwt = token.getValue().getBody(com.azure.security.attestation.implementation.models.PolicyResult.class).getPolicy();
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
     * Sets the current policy for an attestation type with an unsecured attestation policy.
     *
     * <p>Note that this API will only work on AAD mode attestation instances, because it sets the policy
     * using an unsecured attestation token.</p>
     *
     * @param attestationType Specifies the trusted execution environment to be used to validate the evidence.
     * @param newAttestationPolicy Specifies the policy to be set on the instance.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response to an attestation policy operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PolicyResult> setAttestationPolicy(AttestationType attestationType, String newAttestationPolicy) {
        AttestationPolicySetOptions options = new AttestationPolicySetOptions()
            .setAttestationPolicy(newAttestationPolicy);
        return setAttestationPolicyWithResponse(attestationType, options)
            .flatMap(FluxUtil::toMono);
    }

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
    Mono<Response<PolicyResult>> setAttestationPolicyWithResponse(AttestationType attestationType, AttestationPolicySetOptions options, Context context) {
        // Ensure that the incoming request makes sense.
        AttestationTokenValidationOptions validationOptions = options.getValidationOptions();
        if (validationOptions == null) {
            validationOptions = this.tokenValidationOptions;
        }

        final AttestationTokenValidationOptions finalOptions = validationOptions;

        // Generate an attestation token for that stored attestation policy. We use the common function in
        // PolicyResult which is used in creating the SetPolicy hash.
        AttestationToken setToken = generatePolicySetToken(options.getAttestationPolicy(), options.getAttestationSigner());

        return this.policyImpl.setWithResponseAsync(attestationType, setToken.serialize(), context)
            .flatMap(response -> {
                Response<AttestationTokenImpl> token = Utilities.generateResponseFromModelType(response, new AttestationTokenImpl(response.getValue().getToken()));
                return getCachedAttestationSigners()
                    .map(signers -> {
                        token.getValue().validate(signers, finalOptions);
                        PolicyResult policyResult = PolicyResultImpl.fromGenerated(token.getValue().getBody(com.azure.security.attestation.implementation.models.PolicyResult.class));
                        return Utilities.generateAttestationResponseFromModelType(response, token.getValue(), policyResult);
                    });
            });
    }

    /**
     * Calculates the PolicyTokenHash for a given policy string.
     *
     * The policyTokenHash claim in the {@link PolicyResult} class is the SHA-256 hash
     * of the underlying policy set JSON Web Token sent to the attestation service.
     *
     * This helper API allows the caller to independently calculate SHA-256 hash of an
     * attestation token corresponding to the value which would be sent to the attestation
     * service.
     *
     * The value returned by this API must always match the value in the {@link PolicyResult} object,
     * if it does not, it means that the attestation policy received by the service is NOT the one
     * which the customer specified.
     *
     * For an example of how to check the policy token hash:
     * <!-- src_embed com.azure.security.attestation.AttestationAdministrationAsyncClient.checkPolicyTokenHash -->
     * <pre>
     * BinaryData expectedHash = client.calculatePolicyTokenHash&#40;policyToSet, null&#41;;
     * BinaryData actualHash = result.getPolicyTokenHash&#40;&#41;;
     * if &#40;!expectedHash.equals&#40;actualHash&#41;&#41; &#123;
     *     throw new RuntimeException&#40;&quot;Policy was set but not received!!!&quot;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.security.attestation.AttestationAdministrationAsyncClient.checkPolicyTokenHash -->
     *
     * @param policy AttestationPolicy document use in the underlying JWT.
     * @param signer Optional signing key used to sign the underlying JWT.
     * @return A {@link BinaryData} containing the SHA-256 hash of the attestation policy token corresponding
     * to the policy and signer.
     */
    public BinaryData calculatePolicyTokenHash(String policy, AttestationSigningKey signer) {
        AttestationToken policyToken = generatePolicySetToken(policy, signer);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.reset();
            md.update(policyToken.serialize().getBytes(StandardCharsets.UTF_8));
            return BinaryData.fromBytes(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw logger.logExceptionAsError(new RuntimeException(e.getMessage()));
        }
    }

    private AttestationToken generatePolicySetToken(String policy, AttestationSigningKey signer) {
        String serializedPolicy = null;
        if (policy != null) {
            StoredAttestationPolicy policyToSet = new StoredAttestationPolicy();
            policyToSet.setAttestationPolicy(policy.getBytes(StandardCharsets.UTF_8));

            // Serialize the StoredAttestationPolicy.
            try {
                serializedPolicy = SERIALIZER_ADAPTER.serialize(policyToSet, SerializerEncoding.JSON);
            } catch (IOException e) {
                throw logger.logExceptionAsError(new RuntimeException(e.getMessage()));
            }
        }

        // And generate an attestation token for that stored attestation policy.
        AttestationToken setToken;
        if (signer == null) {
            if (policy != null) {
                setToken = AttestationTokenImpl.createUnsecuredToken(serializedPolicy);
            } else {
                setToken = AttestationTokenImpl.createUnsecuredToken();
            }
        } else {
            if (policy != null) {
                setToken = AttestationTokenImpl.createSecuredToken(serializedPolicy, signer);
            } else {
                setToken = AttestationTokenImpl.createSecuredToken(signer);
            }
        }
        return setToken;
    }

    /**
     * Resets the current policy for an attestation type to the default policy.
     *
     * Each AttestationType has a "default" attestation policy, the resetAttestationPolicy API resets the value
     * of the attestation policy to the "default" policy.
     *
     * This API allows an attestation instance owner to undo the result of a
     * {@link AttestationAdministrationAsyncClient#setAttestationPolicy(AttestationType, AttestationPolicySetOptions)} API call.
     *
     *
     * @param attestationType Specifies the trusted execution environment to be used to validate the evidence.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response to an attestation policy operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PolicyResult> resetAttestationPolicy(AttestationType attestationType) {
        return resetAttestationPolicyWithResponse(attestationType, new AttestationPolicySetOptions())
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Resets the current policy for an attestation type to the default policy.
     *
     * Each AttestationType has a "default" attestation policy, the resetAttestationPolicy API resets the value
     * of the attestation policy to the "default" policy.
     *
     * This API allows an attestation instance owner to undo the result of a
     * {@link AttestationAdministrationAsyncClient#setAttestationPolicy(AttestationType, AttestationPolicySetOptions)} API call.
     *
     *
     * @param attestationType Specifies the trusted execution environment to be used to validate the evidence.
     * @param options Options for the setPolicy operation, including the new policy to be set.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response to an attestation policy operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PolicyResult> resetAttestationPolicy(AttestationType attestationType, AttestationPolicySetOptions options) {
        return resetAttestationPolicyWithResponse(attestationType, options)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Resets the current policy for an attestation type to the default policy.
     *
     * Each AttestationType has a "default" attestation policy, the resetAttestationPolicy API resets the value
     * of the attestation policy to the "default" policy.
     *
     * This API allows an attestation instance owner to undo the result of a
     * {@link AttestationAdministrationAsyncClient#setAttestationPolicy(AttestationType, AttestationPolicySetOptions)} API call.
     *
     * @param attestationType Specifies the trusted execution environment to be used to validate the evidence.
     * @param options Options containing the signing key for the reset operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response to an attestation policy operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PolicyResult>> resetAttestationPolicyWithResponse(AttestationType attestationType, AttestationPolicySetOptions options) {
        return withContext(context -> resetAttestationPolicyWithResponse(attestationType, options, context));
    }

    /**
     * Resets the current policy for an attestation type to the default policy.
     *
     * Each AttestationType has a "default" attestation policy, the resetAttestationPolicy API resets the value
     * of the attestation policy to the "default" policy.
     *
     * This API allows an attestation instance owner to undo the result of a
     * {@link AttestationAdministrationAsyncClient#setAttestationPolicy(AttestationType, AttestationPolicySetOptions)} API call.
     *
     * @param attestationType Specifies the trusted execution environment to be used to validate the evidence.
     * @param options Options for setPolicy API, including policy to set and signing key.
     * @param context Context for the operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response to an attestation policy operation.
     */
    Mono<Response<PolicyResult>> resetAttestationPolicyWithResponse(AttestationType attestationType, AttestationPolicySetOptions options, Context context) {
        if (options.getAttestationPolicy() != null) {
            logger.logThrowableAsError(new InvalidParameterException("Attestation policy should not be set in resetAttestationPolicy"));
        }

        // Ensure that the incoming request makes sense.
        AttestationTokenValidationOptions validationOptions = options.getValidationOptions();
        if (validationOptions == null) {
            validationOptions = this.tokenValidationOptions;
        }

        final AttestationTokenValidationOptions finalOptions = validationOptions;

        // And generate an attestation token for that stored attestation policy.
        AttestationToken setToken;
        if (options.getAttestationSigner() == null) {
            setToken = AttestationTokenImpl.createUnsecuredToken();
        } else {
            setToken = AttestationTokenImpl.createSecuredToken(options.getAttestationSigner());
        }
        return this.policyImpl.resetWithResponseAsync(attestationType, setToken.serialize(), context)
            .flatMap(response -> {
                Response<AttestationTokenImpl> token = Utilities.generateResponseFromModelType(response, new AttestationTokenImpl(response.getValue().getToken()));
                return getCachedAttestationSigners()
                    .map(signers -> {
                        token.getValue().validate(signers, finalOptions);
                        PolicyResult policyResult = PolicyResultImpl.fromGenerated(token.getValue().getBody(com.azure.security.attestation.implementation.models.PolicyResult.class));
                        return Utilities.generateAttestationResponseFromModelType(response, token.getValue(), policyResult);
                    });
            });
    }

    /**
     * Retrieves the current set of attestation policy signing certificates for this instance.
     *
     * <p>
     * On an Isolated attestation instance, each {@link AttestationAdministrationAsyncClient#setAttestationPolicy(AttestationType, AttestationPolicySetOptions)}
     * or {@link AttestationAdministrationAsyncClient#resetAttestationPolicy(AttestationType, AttestationPolicySetOptions)} API call
     * must be signed with the private key corresponding to one of the certificates in the list returned
     * by this API.
     *</p>
     * <p>
     *     This establishes that the sender is in possession of the private key associated with the
     *     configured attestation policy management certificates, and thus the sender is authorized
     *     to perform the API operation.
     * </p>
     *
     * @param options Options used to validate the response from the attestation service.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the attestation policy expressed as a string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<List<AttestationSigner>>> listPolicyManagementCertificatesWithResponse(AttestationTokenValidationOptions options) {
        return withContext(context -> listPolicyManagementCertificatesWithResponse(options, context));
    }

    /**
     * Retrieves the current set of attestation policy signing certificates for this instance.
     *
     * <p>
     * On an Isolated attestation instance, each {@link AttestationAdministrationAsyncClient#setAttestationPolicy(AttestationType, AttestationPolicySetOptions)}
     * or {@link AttestationAdministrationAsyncClient#resetAttestationPolicy(AttestationType, AttestationPolicySetOptions)} API call
     * must be signed with the private key corresponding to one of the certificates in the list returned
     * by this API.
     *</p>
     * <p>
     *     This establishes that the sender is in possession of the private key associated with the
     *     configured attestation policy management certificates, and thus the sender is authorized
     *     to perform the API operation.
     * </p>
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response to an attestation policy operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<List<AttestationSigner>> listPolicyManagementCertificates() {
        return listPolicyManagementCertificatesWithResponse(null)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Retrieves the current set of attestation policy signing certificates for this instance.
     *
     * <p>
     * On an Isolated attestation instance, each {@link AttestationAdministrationAsyncClient#setAttestationPolicy(AttestationType, AttestationPolicySetOptions)}
     * or {@link AttestationAdministrationAsyncClient#resetAttestationPolicy(AttestationType, AttestationPolicySetOptions)} API call
     * must be signed with the private key corresponding to one of the certificates in the list returned
     * by this API.
     *</p>
     * <p>
     *     This establishes that the sender is in possession of the private key associated with the
     *     configured attestation policy management certificates, and thus the sender is authorized
     *     to perform the API operation.
     * </p>
     * @param context Context for the remote call.
     * @param validationOptions Options used to validate the response from the attestation service.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the attestation policy expressed as a string.
     */
    Mono<Response<List<AttestationSigner>>> listPolicyManagementCertificatesWithResponse(AttestationTokenValidationOptions validationOptions, Context context) {
        final AttestationTokenValidationOptions optionsToUse = (validationOptions != null ? validationOptions : this.tokenValidationOptions);
        return this.certificatesImpl.getWithResponseAsync(context)
            .flatMap(response -> {
                Response<AttestationTokenImpl> responseWithToken = Utilities.generateResponseFromModelType(response, new AttestationTokenImpl(response.getValue().getToken()));
                return getCachedAttestationSigners()
                    .map(signers -> {
                        responseWithToken.getValue().validate(signers, optionsToUse);
                        JsonWebKeySet policyJwks = responseWithToken.getValue().getBody(com.azure.security.attestation.implementation.models.PolicyCertificatesResult.class).getPolicyCertificates();
                        List<AttestationSigner> policySigners = AttestationSignerImpl.attestationSignersFromJwks(policyJwks);
                        return Utilities.generateAttestationResponseFromModelType(responseWithToken, responseWithToken.getValue(), policySigners);
                    });
            });
    }

    /**
     * Sets the current policy for an attestation type.
     *
     * @param options Options for this API call, encapsulating both the X.509 certificate to add to the set of policy
     *               signing certificates and the signing key used to sign the request to the service.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response to an attestation policy operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PolicyCertificatesModificationResult>> addPolicyManagementCertificateWithResponse(PolicyManagementCertificateOptions options) {
        return withContext(context -> addPolicyManagementCertificateWithResponse(options, context));
    }

    /**
     * Adds a new attestation policy certificate to the set of policy management certificates.
     *
     * @param options Options for this API call, encapsulating both the X.509 certificate to add to the set of policy
     *               signing certificates and the signing key used to sign the request to the service.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response to an attestation policy operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PolicyCertificatesModificationResult> addPolicyManagementCertificate(PolicyManagementCertificateOptions options) {
        return addPolicyManagementCertificateWithResponse(options)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Adds a new policy management certificate to the set of policy management certificates.
     *
     * @param options Options for this API call, encapsulating both the X.509 certificate to add to the set of policy
     *               signing certificates and the signing key used to sign the request to the service.
     * @param context Context for the operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response to an attestation policy operation.
     */
    Mono<Response<PolicyCertificatesModificationResult>> addPolicyManagementCertificateWithResponse(PolicyManagementCertificateOptions options, Context context) {
        Objects.requireNonNull(options.getCertificate());
        Objects.requireNonNull(options.getAttestationSigner());

        final AttestationTokenValidationOptions finalOptions = this.tokenValidationOptions;

        // Generate an attestation token for that stored attestation policy. We use the common function in
        // PolicyResult which is used in creating the SetPolicy hash.
        String base64Certificate = null;

        try {
            base64Certificate = Base64.getEncoder().encodeToString(options.getCertificate().getEncoded());
        } catch (CertificateEncodingException e) {
            throw logger.logExceptionAsError(new RuntimeException(e.getMessage()));
        }
        JsonWebKey jwk = new JsonWebKey(options.getCertificate().getType())
            .setX5C(new ArrayList<String>());
        jwk.getX5C().add(base64Certificate);

        AttestationCertificateManagementBody certificateBody = new AttestationCertificateManagementBody()
            .setPolicyCertificate(jwk);

        AttestationToken addToken = null;
        try {
            addToken = AttestationTokenImpl.createSecuredToken(SERIALIZER_ADAPTER.serialize(certificateBody, SerializerEncoding.JSON), options.getAttestationSigner());
        } catch (IOException e) {
            throw logger.logExceptionAsError(new RuntimeException(e.getMessage()));
        }

        return this.certificatesImpl.addWithResponseAsync(addToken.serialize(), context)
            .flatMap(response -> {
                Response<AttestationTokenImpl> token = Utilities.generateResponseFromModelType(response, new AttestationTokenImpl(response.getValue().getToken()));
                return getCachedAttestationSigners()
                    .map(signers -> {
                        token.getValue().validate(signers, finalOptions);
                        PolicyCertificatesModificationResult addResult = PolicyCertificatesModificationResultImpl.fromGenerated(token.getValue().getBody(com.azure.security.attestation.implementation.models.PolicyCertificatesModificationResult.class));
                        return Utilities.generateAttestationResponseFromModelType(response, token.getValue(), addResult);
                    });
            });
    }

    /**
     * Removes a policy management certificate from the set of policy management certificates.
     *
     * @param options Options for this API call, encapsulating both the X.509 certificate to remove from the set of policy
     *               signing certificates and the signing key used to sign the request to the service.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response to an attestation policy operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PolicyCertificatesModificationResult>> removePolicyManagementCertificateWithResponse(PolicyManagementCertificateOptions options) {
        return withContext(context -> removePolicyManagementCertificateWithResponse(options, context));
    }

    /**
     * Removes a policy management certificate from the set of policy management certificates.
     *
     * @param options Options for this API call, encapsulating both the X.509 certificate to remove from the set of policy
     *               signing certificates and the signing key used to sign the request to the service.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response to an attestation policy operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PolicyCertificatesModificationResult> removePolicyManagementCertificate(PolicyManagementCertificateOptions options) {
        return removePolicyManagementCertificateWithResponse(options)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Removes a policy management certificate from the set of policy management certificates.
     *
     * @param options Options for this API call, encapsulating both the X.509 certificate to remove from the set of policy
     *               signing certificates and the signing key used to sign the request to the service.
     * @param context Context for the operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response to an attestation policy operation.
     */
    Mono<Response<PolicyCertificatesModificationResult>> removePolicyManagementCertificateWithResponse(PolicyManagementCertificateOptions options, Context context) {
        Objects.requireNonNull(options.getCertificate());
        Objects.requireNonNull(options.getAttestationSigner());

        final AttestationTokenValidationOptions finalOptions = this.tokenValidationOptions;

        // Generate an attestation token for that stored attestation policy. We use the common function in
        // PolicyResult which is used in creating the SetPolicy hash.
        String base64Certificate = null;

        try {
            base64Certificate = Base64.getEncoder().encodeToString(options.getCertificate().getEncoded());
        } catch (CertificateEncodingException e) {
            throw logger.logExceptionAsError(new RuntimeException(e.getMessage()));
        }
        JsonWebKey jwk = new JsonWebKey(options.getCertificate().getType())
            .setX5C(new ArrayList<String>());
        jwk.getX5C().add(base64Certificate);

        AttestationCertificateManagementBody certificateBody = new AttestationCertificateManagementBody()
            .setPolicyCertificate(jwk);

        AttestationToken addToken = null;
        try {
            addToken = AttestationTokenImpl.createSecuredToken(SERIALIZER_ADAPTER.serialize(certificateBody, SerializerEncoding.JSON), options.getAttestationSigner());
        } catch (IOException e) {
            throw logger.logExceptionAsError(new RuntimeException(e.getMessage()));
        }

        return this.certificatesImpl.removeWithResponseAsync(addToken.serialize(), context)
            .flatMap(response -> {
                Response<AttestationTokenImpl> token = Utilities.generateResponseFromModelType(response, new AttestationTokenImpl(response.getValue().getToken()));
                return getCachedAttestationSigners()
                    .map(signers -> {
                        token.getValue().validate(signers, finalOptions);
                        PolicyCertificatesModificationResult addResult = PolicyCertificatesModificationResultImpl.fromGenerated(token.getValue().getBody(com.azure.security.attestation.implementation.models.PolicyCertificatesModificationResult.class));
                        return Utilities.generateAttestationResponseFromModelType(response, token.getValue(), addResult);
                    });
            });
    }



    /**
     * Return cached attestation signers, fetching from the internet if needed.
     *<p>
     * Validating an attestation JWT requires a set of attestation signers retrieved from the
     * attestation service using the `signingCertificatesImpl.getAsync()` API. This API can take
     * more than 100ms to complete, so caching the value locally can significantly reduce the time
     * needed to validate the attestation JWT.
     * </p><p>
     *  Note that there is a possible race condition if two threads on the same client are making
     *  calls to the attestation service. In that case, two calls to `signingCertificatesImpl.getAsync()`
     *  may be made. That should not result in any problems - one of the two calls will complete first
     *  and the `compareAndSet` will update the `cachedSigners`. The second call's result will be discarded
     *  because the `compareAndSet` API won't capture a reference to the second `signers` object.
     *
     * </p>
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
