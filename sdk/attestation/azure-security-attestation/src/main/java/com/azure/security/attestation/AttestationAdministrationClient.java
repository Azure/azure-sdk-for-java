// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.security.attestation.models.AttestationPolicySetOptions;
import com.azure.security.attestation.models.AttestationType;
import com.azure.security.attestation.models.PolicyResult;

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
     * More information about Attestation Policy can be found <a href='https://docs.microsoft.com/en-us/azure/attestation/basic-concepts#attestation-policy'>here.</a>
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
     * More information about Attestation Policy can be found <a href='https://docs.microsoft.com/en-us/azure/attestation/basic-concepts#attestation-policy'>here.</a>
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
     * More information about Attestation Policy can be found <a href='https://docs.microsoft.com/en-us/azure/attestation/basic-concepts#attestation-policy'>here.</a>
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
     * Sets the attestation policy for the specified attestation type for an AAD mode attestation instance.
     *
     * Note that this function will only work on AAD mode attestation instances, because there is
     * no key signing certificate provided.
     *
     * More information about Attestation Policy can be found <a href='https://docs.microsoft.com/en-us/azure/attestation/basic-concepts#attestation-policy'>here.</a>
     *
     * @param attestationType The {@link AttestationType} to be updated.
     * @param policyToSet Attestation Policy to set on the instance.
     * @param context Context for the operation.
     * @return {@link PolicyResult} expressing the result of the attestation operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PolicyResult> setAttestationPolicyWithResponse(AttestationType attestationType, String policyToSet, Context context) {
        return asyncClient.setAttestationPolicyWithResponse(attestationType, policyToSet, context).block();
    }

    //endregion
};
