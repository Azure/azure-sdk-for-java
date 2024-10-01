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
import com.azure.security.attestation.models.AttestationPolicySetOptions;
import com.azure.security.attestation.models.AttestationResponse;
import com.azure.security.attestation.models.AttestationSignerCollection;
import com.azure.security.attestation.models.AttestationSigningKey;
import com.azure.security.attestation.models.AttestationTokenValidationOptions;
import com.azure.security.attestation.models.AttestationType;
import com.azure.security.attestation.models.PolicyCertificatesModificationResult;
import com.azure.security.attestation.models.PolicyManagementCertificateOptions;
import com.azure.security.attestation.models.PolicyResult;

/**
 *
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
 * */
@ServiceClient(builder = AttestationAdministrationClientBuilder.class)
public final class AttestationAdministrationClient {
    private final AttestationAdministrationAsyncClient asyncClient;

    AttestationAdministrationClient(AttestationAdministrationAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
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
     * <p><strong>Retrieve the current attestation policy for SGX enclaves.</strong></p>
     * <!-- src_embed com.azure.security.attestation.AttestationAdministrationClient.getPolicy -->
     * <pre>
     * String policy = client.getAttestationPolicy&#40;AttestationType.SGX_ENCLAVE&#41;;
     * </pre>
     * <!-- end com.azure.security.attestation.AttestationAdministrationClient.getPolicy -->
     *
     * @param attestationType Specifies the trusted execution environment whose policy should be retrieved.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response to an attestation policy operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public String getAttestationPolicy(AttestationType attestationType) {
        return asyncClient.getAttestationPolicy(attestationType).block();
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
     * <p><strong>Retrieve the current attestation policy for SGX enclaves.</strong></p>
     * <!-- src_embed com.azure.security.attestation.AttestationAdministrationClient.getPolicyWithResponse -->
     * <pre>
     * Response&lt;String&gt; response = client.getAttestationPolicyWithResponse&#40;AttestationType.SGX_ENCLAVE, null,
     *     Context.NONE&#41;;
     * </pre>
     * <!-- end com.azure.security.attestation.AttestationAdministrationClient.getPolicyWithResponse -->
     *
     * @param attestationType Specifies the trusted execution environment whose policy should be retrieved.
     * @param options Options used when validating the attestation token.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response to an attestation policy operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public String getAttestationPolicy(AttestationType attestationType, AttestationTokenValidationOptions options) {
        return asyncClient.getAttestationPolicy(attestationType, options).block();
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
     * <p><strong>Retrieve the current attestation policy for SGX enclaves.</strong></p>
     * <!-- src_embed com.azure.security.attestation.AttestationAdministrationClient.getPolicyWithResponse -->
     * <pre>
     * Response&lt;String&gt; response = client.getAttestationPolicyWithResponse&#40;AttestationType.SGX_ENCLAVE, null,
     *     Context.NONE&#41;;
     * </pre>
     * <!-- end com.azure.security.attestation.AttestationAdministrationClient.getPolicyWithResponse -->
     *
     * @param attestationType Specifies the trusted execution environment whose policy should be retrieved.
     * @param validationOptions Options used when validating the token returned by the attestation service.
     * @param context Context for the operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the attestation policy expressed as a string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AttestationResponse<String> getAttestationPolicyWithResponse(AttestationType attestationType,
        AttestationTokenValidationOptions validationOptions, Context context) {
        return asyncClient.getAttestationPolicyWithResponse(attestationType, validationOptions, context).block();
    }

    /**
     * Sets the attestation policy for the specified attestation type for an AAD mode attestation instance.
     *
     * Note that this function will only work on AAD mode attestation instances, because there is
     * no key signing certificate provided.
     *
     * More information about Attestation Policy can be found <a href='https://docs.microsoft.com/azure/attestation/basic-concepts#attestation-policy'>here.</a>
     *
     * <p>Set attestation policy to a constant value.</p>
     * <!-- src_embed com.azure.security.attestation.AttestationAdministrationClient.setPolicySimple -->
     * <pre>
     * String policyToSet = &quot;version=1.0; authorizationrules&#123;=&gt; permit&#40;&#41;;&#125;; issuancerules&#123;&#125;;&quot;;
     * PolicyResult result = client.setAttestationPolicy&#40;AttestationType.OPEN_ENCLAVE, policyToSet&#41;;
     * </pre>
     * <!-- end com.azure.security.attestation.AttestationAdministrationClient.setPolicySimple -->
     *
     * @param attestationType The {@link AttestationType} to be updated.
     * @param policyToSet Attestation Policy to set on the instance.
     * @return {@link PolicyResult} expressing the result of the attestation operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PolicyResult setAttestationPolicy(AttestationType attestationType, String policyToSet) {
        return asyncClient.setAttestationPolicy(attestationType, policyToSet).block();
    }

    /**
     * Sets the attestation policy for the specified attestation type, with policy and signing key.
     * Sets the current policy for an attestation type.
     *
     * Setting the attestation requires that the caller provide an {@link AttestationPolicySetOptions} object
     * which provides the options for setting the policy. There are two major components to a setPolicy
     * request:
     * <ul>
     *     <li>The policy to set</li>
     *     <li>A signing key used to sign the policy sent to the service (OPTIONAL)</li>
     * </ul>
     *
     * On Isolated mode attestation instances, the signing key MUST be one of the configured policy signing
     * certificates.
     *
     * <!-- src_embed com.azure.security.attestation.AttestationAdministrationClient.setPolicy -->
     * <pre>
     * String policyToSet = &quot;version=1.0; authorizationrules&#123;=&gt; permit&#40;&#41;;&#125;; issuancerules&#123;&#125;;&quot;;
     * PolicyResult result = client.setAttestationPolicy&#40;AttestationType.OPEN_ENCLAVE,
     *     new AttestationPolicySetOptions&#40;&#41;
     *         .setAttestationPolicy&#40;policyToSet&#41;
     *         .setAttestationSigner&#40;new AttestationSigningKey&#40;certificate, privateKey&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.attestation.AttestationAdministrationClient.setPolicy -->
     *
     * @param attestationType The {@link AttestationType} to be updated.
     * @param options {@link AttestationPolicySetOptions} for the request.
     * @return {@link PolicyResult} expressing the result of the attestation operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PolicyResult setAttestationPolicy(AttestationType attestationType, AttestationPolicySetOptions options) {
        return asyncClient.setAttestationPolicy(attestationType, options).block();
    }

    /**
     * Sets the attestation policy for the specified attestation type.
     *
     * Setting the attestation requires that the caller provide an {@link AttestationPolicySetOptions} object
     * which provides the options for setting the policy. There are two major components to a setPolicy
     * request:
     * <ul>
     *     <li>The policy to set</li>
     *     <li>A signing key used to sign the policy sent to the service (OPTIONAL)</li>
     * </ul>
     *
     * On Isolated mode attestation instances, the signing key MUST include one of the configured policy signing
     * certificates.
     *
     * <!-- src_embed com.azure.security.attestation.AttestationAdministrationClient.setPolicyWithResponse -->
     * <pre>
     * Response&lt;PolicyResult&gt; response = client.setAttestationPolicyWithResponse&#40;AttestationType.OPEN_ENCLAVE,
     *     new AttestationPolicySetOptions&#40;&#41;
     *         .setAttestationPolicy&#40;policyToSet&#41;
     *         .setAttestationSigner&#40;new AttestationSigningKey&#40;certificate, privateKey&#41;&#41;, Context.NONE&#41;;
     *
     * </pre>
     * <!-- end com.azure.security.attestation.AttestationAdministrationClient.setPolicyWithResponse -->
     *
     *
     * @param attestationType The {@link AttestationType} to be updated.
     * @param options {@link AttestationPolicySetOptions} for the request.
     * @param context Context for the operation.
     * @return {@link PolicyResult} expressing the result of the attestation operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AttestationResponse<PolicyResult> setAttestationPolicyWithResponse(AttestationType attestationType,
        AttestationPolicySetOptions options, Context context) {
        return asyncClient.setAttestationPolicyWithResponse(attestationType, options, context).block();
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
     * <!-- src_embed com.azure.security.attestation.AttestationAdministrationClient.checkPolicyTokenHash -->
     * <pre>
     * BinaryData expectedHash = client.calculatePolicyTokenHash&#40;policyToSet, null&#41;;
     * BinaryData actualHash = result.getPolicyTokenHash&#40;&#41;;
     * String expectedString = Hex.toHexString&#40;expectedHash.toBytes&#40;&#41;&#41;;
     * String actualString = Hex.toHexString&#40;actualHash.toBytes&#40;&#41;&#41;;
     * if &#40;!expectedString.equals&#40;actualString&#41;&#41; &#123;
     *     throw new RuntimeException&#40;&quot;Policy was set but not received!!!&quot;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.security.attestation.AttestationAdministrationClient.checkPolicyTokenHash -->
     *
     * @param policy AttestationPolicy document use in the underlying JWT.
     * @param signer Optional signing key used to sign the underlying JWT.
     * @return A {@link BinaryData} containing the SHA-256 hash of the attestation policy token generated
     * using the specified policy and signer.
     */
    public BinaryData calculatePolicyTokenHash(String policy, AttestationSigningKey signer) {
        return asyncClient.calculatePolicyTokenHash(policy, signer);
    }

    /**
     * Resets the attestation policy for the specified attestation type to the default value.
     * Resets the current policy for an attestation type to the default policy.
     *
     * Note: This is a convenience method that will only work on attestation service instances in AAD mode.
     *
     * Each AttestationType has a "default" attestation policy, the resetAttestationPolicy API resets the value
     * of the attestation policy to the "default" policy.
     *
     * This API allows an attestation instance owner to undo the result of a
     * {@link AttestationAdministrationAsyncClient#setAttestationPolicy(AttestationType, AttestationPolicySetOptions)} API call.
     *
     * <p><strong>Reset an attestation policy to its defaults on an AAD instance</strong></p>
     * <!-- src_embed com.azure.security.attestation.AttestationAdministrationClient.resetPolicySimple -->
     * <pre>
     * PolicyResult result = client.resetAttestationPolicy&#40;AttestationType.OPEN_ENCLAVE&#41;;
     * </pre>
     * <!-- end com.azure.security.attestation.AttestationAdministrationClient.resetPolicySimple -->
     *
     * @param attestationType The {@link AttestationType} to be updated.
     * @return {@link PolicyResult} expressing the result of the attestation operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PolicyResult resetAttestationPolicy(AttestationType attestationType) {
        return asyncClient.resetAttestationPolicy(attestationType).block();
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
     * <p><strong>Reset an attestation policy to its defaults</strong></p>
     * <!-- src_embed com.azure.security.attestation.AttestationAdministrationClient.resetPolicy -->
     * <pre>
     * PolicyResult result = client.resetAttestationPolicy&#40;AttestationType.OPEN_ENCLAVE,
     *     new AttestationPolicySetOptions&#40;&#41;
     *         .setAttestationSigner&#40;new AttestationSigningKey&#40;certificate, privateKey&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.attestation.AttestationAdministrationClient.resetPolicy -->
     *
     * @param attestationType The {@link AttestationType} to be updated.
     * @param options {@link AttestationPolicySetOptions} for the request.
     * @return {@link PolicyResult} expressing the result of the attestation operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PolicyResult resetAttestationPolicy(AttestationType attestationType, AttestationPolicySetOptions options) {
        return asyncClient.resetAttestationPolicy(attestationType, options).block();
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
     * <p><strong>Reset an attestation policy to its defaults</strong></p>
     * <!-- src_embed com.azure.security.attestation.AttestationAdministrationClient.resetPolicyWithResponse -->
     * <pre>
     * Response&lt;PolicyResult&gt; response = client.resetAttestationPolicyWithResponse&#40;AttestationType.OPEN_ENCLAVE,
     *     new AttestationPolicySetOptions&#40;&#41;
     *         .setAttestationSigner&#40;new AttestationSigningKey&#40;certificate, privateKey&#41;&#41;, Context.NONE&#41;;
     *
     * </pre>
     * <!-- end com.azure.security.attestation.AttestationAdministrationClient.resetPolicyWithResponse -->
     *
     * @param attestationType The {@link AttestationType} to be updated.
     * @param options {@link AttestationPolicySetOptions} for the request.
     * @param context Context for the operation.
     * @return {@link PolicyResult} expressing the result of the attestation operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AttestationResponse<PolicyResult> resetAttestationPolicyWithResponse(AttestationType attestationType,
        AttestationPolicySetOptions options, Context context) {
        return asyncClient.resetAttestationPolicyWithResponse(attestationType, options, context).block();
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
     * <p><strong>Retrieve the set of policy management certificates for this instance.</strong></p>
     * <!-- src_embed com.azure.security.attestation.AttestationAdministrationClient.listPolicyManagementCertificatesSimple -->
     * <pre>
     * AttestationSignerCollection signers = client.listPolicyManagementCertificates&#40;&#41;;
     * System.out.printf&#40;&quot;There are %d signers on the instance&#92;n&quot;, signers.getAttestationSigners&#40;&#41;.size&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.attestation.AttestationAdministrationClient.listPolicyManagementCertificatesSimple -->
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response to an attestation policy operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AttestationSignerCollection listPolicyManagementCertificates() {
        return asyncClient.listPolicyManagementCertificates().block();
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
     * <p><strong>Retrieve the set of policy management certificates for this instance.</strong></p>
     * <!-- src_embed com.azure.security.attestation.AttestationAdministrationClient.listPolicyManagementCertificatesWithResponse -->
     * <pre>
     * AttestationResponse&lt;AttestationSignerCollection&gt; signersResponse =
     *     client.listPolicyManagementCertificatesWithResponse&#40;
     *         new AttestationTokenValidationOptions&#40;&#41;.setValidationSlack&#40;Duration.ofSeconds&#40;10&#41;&#41;, Context.NONE&#41;;
     * System.out.printf&#40;&quot;There are %d signers on the instance&#92;n&quot;,
     *     signersResponse.getValue&#40;&#41;.getAttestationSigners&#40;&#41;.size&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.attestation.AttestationAdministrationClient.listPolicyManagementCertificatesWithResponse -->
     *
     * @param tokenValidationOptions Options to be used validating the token returned by the attestation service.
     * @param context Context for the operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the attestation policy expressed as a string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AttestationResponse<AttestationSignerCollection> listPolicyManagementCertificatesWithResponse(
        AttestationTokenValidationOptions tokenValidationOptions, Context context) {
        return asyncClient.listPolicyManagementCertificatesWithResponse(tokenValidationOptions, context).block();
    }

    /**
     * Adds a new certificate to the set of policy management certificates on this instance.
     *<p>
     * Each Isolated mode attestation service instance maintains a set of certificates which can be used to authorize
     * policy modification operations (in Isolated mode, each policy modification request needs to be signed with
     * the private key associated with one of the policy management certificates).
     *</p>
     * <p>
     * This API allows the caller to add a new certificate to the set of policy management certificates.
     *</p>
     * <p>
     * The request to add a new certificate must be signed with one of the existing policy management certificates,
     * so the {@link PolicyManagementCertificateOptions} object requires both the new certificate to be added and
     * a {@link AttestationSigningKey} to sign the add request.
     *</p>
     * <p><strong>Add a new certificate to the set of policy management certificates for this instance.</strong></p>
     * <!-- src_embed com.azure.security.attestation.AttestationAdministrationClient.addPolicyManagementCertificate -->
     * <pre>
     * PolicyCertificatesModificationResult addResult = client.addPolicyManagementCertificate&#40;
     *     new PolicyManagementCertificateOptions&#40;certificateToAdd, new AttestationSigningKey&#40;certificate, privateKey&#41;&#41;&#41;;
     * System.out.printf&#40;&quot; Result: %s&#92;n&quot;, addResult.getCertificateResolution&#40;&#41;.toString&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.attestation.AttestationAdministrationClient.addPolicyManagementCertificate -->
     *
     * <p><strong><i>Note:</i></strong> It is not considered an error to add the same certificate twice. If
     * the same certificate is added twice, the service ignores the second add request.</p>
     *
     * @param options Options for this API call, encapsulating both the X.509 certificate to add to the set of policy
     *               signing certificates and the signing key used to sign the request to the service.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response to an attestation policy operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PolicyCertificatesModificationResult
        addPolicyManagementCertificate(PolicyManagementCertificateOptions options) {
        return asyncClient.addPolicyManagementCertificate(options).block();
    }

    /**
     * Adds a new certificate to the set of policy management certificates on this instance.
     *
     * Each Isolated mode attestation service instance maintains a set of certificates which can be used to authorize
     * policy modification operations (in Isolated mode, each policy modification request needs to be signed with
     * the private key associated with one of the policy management certificates).
     *
     * This API allows the caller to add a new certificate to the set of policy management certificates.
     *
     * The request to add a new certificate must be signed with one of the existing policy management certificates,
     * so the {@link PolicyManagementCertificateOptions} object requires both the new certificate to be added and
     * a {@link AttestationSigningKey} to sign the add request.
     *
     * <p><strong>Add a new certificate to the set of policy management certificates for this instance.</strong></p>
     * <!-- src_embed com.azure.security.attestation.AttestationAdministrationClient.addPolicyManagementCertificateWithResponse -->
     * <pre>
     * Response&lt;PolicyCertificatesModificationResult&gt; addResponse = client.addPolicyManagementCertificateWithResponse&#40;
     *     new PolicyManagementCertificateOptions&#40;certificateToAdd, new AttestationSigningKey&#40;certificate, privateKey&#41;&#41;,
     *     Context.NONE&#41;;
     * System.out.printf&#40;&quot; Result: %s&#92;n&quot;, addResponse.getValue&#40;&#41;.getCertificateResolution&#40;&#41;.toString&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.attestation.AttestationAdministrationClient.addPolicyManagementCertificateWithResponse -->
     * <p><strong><i>Note:</i></strong> It is not considered an error to add the same certificate twice. If
     * the same certificate is added twice, the service ignores the second add request.</p>
     *
     * @param options Options for this API call, encapsulating both the X.509 certificate to add to the set of policy
     *               signing certificates and the signing key used to sign the request to the service.
     * @param context Context for the operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response to an attestation policy operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AttestationResponse<PolicyCertificatesModificationResult>
        addPolicyManagementCertificateWithResponse(PolicyManagementCertificateOptions options, Context context) {
        return asyncClient.addPolicyManagementCertificateWithResponse(options, context).block();
    }

    /**
     * Deletes a policy management certificate from the set of policy management certificates.
     * <p>
     * Each Isolated mode attestation service instance maintains a set of certificates which can be used to authorize
     * policy modification operations (in Isolated mode, each policy modification request needs to be signed with
     * the private key associated with one of the policy management certificates).
     *</p>
     * <p>
     * This API allows the caller to remove an existing certificate from the set of policy management certificates.
     *</p>
     * <p>
     * The request to add a new certificate must be signed with one of the existing policy management certificates,
     * so the {@link PolicyManagementCertificateOptions} object requires both the new certificate to be added and
     * a {@link AttestationSigningKey} to sign the add request.
     *</p>
     * <p><strong>Add a new certificate to the set of policy management certificates for this instance.</strong></p>
     * <!-- src_embed com.azure.security.attestation.AttestationAdministrationClient.removePolicyManagementCertificate -->
     * <pre>
     * PolicyCertificatesModificationResult removeResult = client.deletePolicyManagementCertificate&#40;
     *     new PolicyManagementCertificateOptions&#40;certificateToAdd, new AttestationSigningKey&#40;certificate, privateKey&#41;&#41;&#41;;
     * System.out.printf&#40;&quot; Result: %s&#92;n&quot;, removeResult.getCertificateResolution&#40;&#41;.toString&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.attestation.AttestationAdministrationClient.removePolicyManagementCertificate -->
     *
     * <p><strong><i>Note:</i></strong> It is not considered an error to removethe same certificate twice. If
     * the same certificate is removed twice, the service ignores the second remove request. This also means that
     * it is not an error to remove a certificate which was not actually in the set of policy certificates.</p>
     *
     * @param options Options for this API call, encapsulating both the X.509 certificate to remove from the set of policy
     *               signing certificates and the signing key used to sign the request to the service.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response to an attestation policy operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PolicyCertificatesModificationResult
        deletePolicyManagementCertificate(PolicyManagementCertificateOptions options) {
        return asyncClient.deletePolicyManagementCertificate(options).block();
    }

    /**
     * Removes a policy management certificate from the set of policy management certificates.
     * <p>
     * Each Isolated mode attestation service instance maintains a set of certificates which can be used to authorize
     * policy modification operations (in Isolated mode, each policy modification request needs to be signed with
     * the private key associated with one of the policy management certificates).
     *</p>
     * <p>
     * This API allows the caller to remove an existing certificate from the set of policy management certificates.
     *</p>
     * <p>
     * The request to add a new certificate must be signed with one of the existing policy management certificates,
     * so the {@link PolicyManagementCertificateOptions} object requires both the new certificate to be added and
     * a {@link AttestationSigningKey} to sign the add request.
     *</p>
     * <p><strong>Add a new certificate to the set of policy management certificates for this instance.</strong></p>
     * <!-- src_embed com.azure.security.attestation.AttestationAdministrationClient.removePolicyManagementCertificateWithResponse -->
     * <pre>
     * Response&lt;PolicyCertificatesModificationResult&gt; removeResponse = client.addPolicyManagementCertificateWithResponse&#40;
     *     new PolicyManagementCertificateOptions&#40;certificateToAdd, new AttestationSigningKey&#40;certificate, privateKey&#41;&#41;,
     *     Context.NONE&#41;;
     * System.out.printf&#40;&quot; Result: %s&#92;n&quot;, removeResponse.getValue&#40;&#41;.getCertificateResolution&#40;&#41;.toString&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.attestation.AttestationAdministrationClient.removePolicyManagementCertificateWithResponse -->
     *
     * <p><strong><i>Note:</i></strong> It is not considered an error to remove the same certificate twice. If
     * the same certificate is removed twice, the service ignores the second remove request. This also means that
     * it is not an error to remove a certificate which was not actually in the set of policy certificates.</p>
     *
     * @param options Options for this API call, encapsulating both the X.509 certificate to remove from the set of policy
     *               signing certificates and the signing key used to sign the request to the service.
     * @param context Context for the operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response to an attestation policy operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AttestationResponse<PolicyCertificatesModificationResult>
        deletePolicyManagementCertificateWithResponse(PolicyManagementCertificateOptions options, Context context) {
        return asyncClient.deletePolicyManagementCertificateWithResponse(options, context).block();
    }
}
