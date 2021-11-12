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
import com.azure.security.attestation.models.AttestationSigningKey;
import com.azure.security.attestation.models.AttestationType;
import com.azure.security.attestation.models.PolicyResult;

/**
 *
 * The AttestationAdministrationClient provides access to the administrative policy APIs
 * implemented by the Attestation Service.
 *
 * More information on attestation policies can be found <a href='https://docs.microsoft.com/azure/attestation/basic-concepts#attestation-policy'>here</a>
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
 *<p>
 * When an attestation instance is in Isolated mode, additional proof needs to be provided by the customer
 * to verify that they are authorized to perform the operation specified.
 *</p>
 */
@ServiceClient(builder = AttestationAdministrationClientBuilder.class)
public final class AttestationAdministrationClient {
    private final AttestationAdministrationAsyncClient asyncClient;

    AttestationAdministrationClient(AttestationAdministrationAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    //region Get Attestation Policy
    /**
     * Retrieves the current policy for an attestation type.
     *
     * @param attestationType Specifies the trusted execution environment whose policy should be retrieved.
     * @param context Context for the operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the attestation policy expressed as a string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<String> getAttestationPolicyWithResponse(AttestationType attestationType, Context context) {
        return asyncClient.getAttestationPolicyWithResponse(attestationType, context).block();
    }

    /**
     * Retrieves the current policy for an attestation type.
     *  <p>
     *      <b>NOTE:</b>
     *     The {@link AttestationAdministrationClient#getAttestationPolicy(AttestationType)} API returns the underlying
     *     attestation policy specified by the user. This is NOT the full attestation policy maintained by
     *     the attestation service. Specifically it does not include the signing certificates used to verify the attestation
     *     policy.
     *     </p>
     *     <p>
     *         To retrieve the signing certificates used to sign the policy, use the {@link AttestationAdministrationClient#getAttestationPolicyWithResponse(AttestationType, Context)} API.
     *  </p>
     *
     * @param attestationType Specifies the trusted execution environment to be used to validate the evidence.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response to an attestation policy operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public String getAttestationPolicy(AttestationType attestationType) {
        return asyncClient.getAttestationPolicy(attestationType).block();
    }

//endregion

    //region Set Attestation Policy

    /**
     * Sets the attestation policy for the specified attestation type, with policy and signing key.
     *
     * More information about Attestation Policy can be found <a href='https://docs.microsoft.com/azure/attestation/basic-concepts#attestation-policy'>here.</a>
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
     * Sets the attestation policy for the specified attestation type for an AAD mode attestation instance.
     *
     * Note that this function will only work on AAD mode attestation instances, because there is
     * no key signing certificate provided.
     *
     * More information about Attestation Policy can be found <a href='https://docs.microsoft.com/azure/attestation/basic-concepts#attestation-policy'>here.</a>
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
     * Sets the attestation policy for the specified attestation type
     *
     * Note that this function will only work on AAD mode attestation instances, because there is
     * no key signing certificate provided.
     *
     * More information about Attestation Policy can be found <a href='https://docs.microsoft.com/azure/attestation/basic-concepts#attestation-policy'>here.</a>
     *
     * @param attestationType The {@link AttestationType} to be updated.
     * @param options {@link AttestationPolicySetOptions} for the request.
     * @param context Context for the operation.
     * @return {@link PolicyResult} expressing the result of the attestation operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PolicyResult> setAttestationPolicyWithResponse(AttestationType attestationType, AttestationPolicySetOptions options, Context context) {
        return asyncClient.setAttestationPolicyWithResponse(attestationType, options, context).block();
    }

    /**
     * Calculates the PolicyTokenHash for a given policy string.
     *
     * The policyTokenHash is calculated by generating a policy set JSON Web Token signed by the key
     * specified in the (optional) {@link AttestationSigningKey}.
     *
     * @param policy AttestationPolicy document use in the underlying JWT.
     * @param signer Optional signing key used to sign the underlying JWT.
     * @return A {@link BinaryData} containing the SHA-256 hash of the attestation policy token corresponding
     * to the policy and signer.
     */
    public BinaryData calculatePolicyTokenHash(String policy, AttestationSigningKey signer) {
        return asyncClient.calculatePolicyTokenHash(policy, signer);
    }
    //endregion

    //region Reset Attestation Policy
    /**
     * Resets the attestation policy for the specified attestation type, using the specified signing key.
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
     * Resets the attestation policy for the specified attestation type for an AAD mode attestation instance to the default value.
     *
     * Note that this function will only work on AAD mode attestation instances, because there is
     * no token signing certificate provided.
     *
     * @param attestationType The {@link AttestationType} to be updated.
     * @return {@link PolicyResult} expressing the result of the attestation operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PolicyResult resetAttestationPolicy(AttestationType attestationType) {
        return asyncClient.resetAttestationPolicy(attestationType).block();
    }

    /**
     * Resets the attestation policy for the specified attestation type to the default value.
     *
     * @param attestationType The {@link AttestationType} to be updated.
     * @param options {@link AttestationPolicySetOptions} for the request.
     * @param context Context for the operation.
     * @return {@link PolicyResult} expressing the result of the attestation operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PolicyResult> resetAttestationPolicyWithResponse(AttestationType attestationType, AttestationPolicySetOptions options, Context context) {
        return asyncClient.resetAttestationPolicyWithResponse(attestationType, options, context).block();
    }

//  endregion
};
