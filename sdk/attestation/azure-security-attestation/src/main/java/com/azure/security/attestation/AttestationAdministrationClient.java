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
 * to verify that they are authorized to perform the operation specified. This additional proof is
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
     * <p>
     * <b>NOTE:</b>
     *     The {@link AttestationAdministrationClient#getAttestationPolicyWithResponse(AttestationType, Context)} API returns the underlying
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
     *         The {@link Response} object is an instance of an {@link com.azure.security.attestation.models.AttestationResponse} object
     *         and the caller can retrieve the full information maintained by the service by calling the {@link AttestationResponse#getToken()} method.
     *         The returned {@link com.azure.security.attestation.models.AttestationToken} object will be
     *         the value stored by the attestation service.
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
     * Sets the attestation policy for the specified attestation type.
     *
     * The value of the policy to set is contained in the {@link AttestationPolicySetOptions} object, as
     * is the (optional) signing key used to sign the setPolicy request.
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
     * @return A {@link BinaryData} containing the SHA-256 hash of the attestation policy token generated
     * using the specified policy and signer.
     */
    public BinaryData calculatePolicyTokenHash(String policy, AttestationSigningKey signer) {
        return asyncClient.calculatePolicyTokenHash(policy, signer);
    }
    //endregion

    //region Reset Attestation Policy
    /**
     * Resets the attestation policy for the specified attestation type to the default, using the specified options.
     *
     * Each AttestationType has a "default" attestation policy, the resetAttestationPolicy API resets the value
     * of the attestation policy to the "default" policy.
     *
     * This API allows an attestation instance owner to undo the result of a
     * {@link AttestationAdministrationClient#setAttestationPolicy(AttestationType, AttestationPolicySetOptions)} API call.
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
     * Resets the attestation policy for the specified attestation type to the default value.
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
