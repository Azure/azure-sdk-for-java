// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpRequestException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.security.keyvault.certificates.models.Certificate;
import com.azure.security.keyvault.certificates.models.CertificateProperties;
import com.azure.security.keyvault.certificates.models.CertificateOperation;
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.certificates.models.Contact;
import com.azure.security.keyvault.certificates.models.DeletedCertificate;
import com.azure.security.keyvault.certificates.models.Issuer;
import com.azure.security.keyvault.certificates.models.IssuerProperties;
import com.azure.security.keyvault.certificates.models.LifetimeAction;
import com.azure.security.keyvault.certificates.models.LifetimeActionType;
import com.azure.security.keyvault.certificates.models.MergeCertificateOptions;
import com.azure.security.keyvault.certificates.models.CertificateImportOptions;

import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * The CertificateClient provides synchronous methods to manage {@link Certificate certifcates} in the Azure Key Vault. The client
 * supports creating, retrieving, updating, merging, deleting, purging, backing up, restoring and listing the
 * {@link Certificate certificates}. The client also supports listing {@link DeletedCertificate deleted certificates} for
 * a soft-delete enabled Azure Key Vault.
 *
 * <p>The client further allows creating, retrieving, updating, deleting and listing the {@link Issuer certificate issuers}. The client also supports
 * creating, listing and deleting {@link Contact certificate contacts}</p>
 *
 * <p><strong>Samples to construct the sync client</strong></p>
 *
 * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.instantiation}
 *
 * @see CertificateClientBuilder
 * @see PagedIterable
 */
@ServiceClient(builder = CertificateClientBuilder.class, serviceInterfaces = CertificateService.class)
public class CertificateClient {
    private final CertificateAsyncClient client;

    /**
     * Creates a CertificateClient that uses {@code pipeline} to service requests
     *
     * @param client The {@link CertificateAsyncClient} that the client routes its request through.
     */
    CertificateClient(CertificateAsyncClient client) {
        this.client = client;
    }

    /**
     * Creates a new certificate. If this is the first version, the certificate resource is created. This operation requires
     * the certificates/create permission.
     *
     * <p>Create certificate is a long running operation. It indefinitely waits for the create certificate operation to complete on service side.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Create certificate is a long running operation. The createCertificate indefinitely waits for the operation to complete and
     * returns its last status. The details of the last certificate operation status are printed when a response is received</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.createCertificate#String-CertificatePolicy-Map}
     *
     * @param name The name of the certificate to be created.
     * @param policy The policy of the certificate to be created.
     * @param tags The application specific metadata to set.
     * @throws ResourceModifiedException when invalid certificate policy configuration is provided.
     * @return A {@link Poller} polling on the create certificate operation status.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Poller<CertificateOperation, Certificate> beginCreateCertificate(String name, CertificatePolicy policy, Map<String, String> tags) {
        return  client.beginCreateCertificate(name, policy, true, tags);
    }

    /**
     * Creates a new certificate. If this is the first version, the certificate resource is created. This operation requires
     * the certificates/create permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Create certificate is a long running operation. The createCertificate indefinitely waits for the operation to complete and
     * returns its last status. The details of the last certificate operation status are printed when a response is received</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.createCertificate#String-CertificatePolicy}
     *
     * @param name The name of the certificate to be created.
     * @param policy The policy of the certificate to be created.
     * @throws ResourceModifiedException when invalid certificate policy configuration is provided.
     * @return A {@link Poller} polling on the create certificate operation status.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Poller<CertificateOperation, Certificate> beginCreateCertificate(String name, CertificatePolicy policy) {
        return client.beginCreateCertificate(name, policy);
    }

    /**
     * Creates a new certificate with the default policy. If this is the first version, the certificate resource
     * is created. This operation requires the certificates/create permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Create certificate is a long running operation. The createCertificate indefinitely waits for the operation to complete and
     * returns its last status. The details of the last certificate operation status are printed when a response is received</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.createCertificate#String-CertificatePolicy}
     *
     * @param name The name of the certificate to be created.
     * @throws ResourceModifiedException when invalid certificate policy configuration is provided.
     * @return A {@link Poller} polling on the create certificate operation status.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Poller<CertificateOperation, Certificate> beginCreateCertificate(String name) {
        return client.beginCreateCertificate(name);
    }

    /**
     * Gets information about the latest version of the specified certificate. This operation requires the certificates/get permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a specific version of the key in the key vault. Prints out the returned certificate details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.getCertificateWithPolicy#String}
     *
     * @param name The name of the certificate to retrieve, cannot be null
     * @throws ResourceNotFoundException when a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException if {@code name} is empty string.
     * @return The requested {@link Certificate certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Certificate getCertificateWithPolicy(String name) {
        return client.getCertificateWithPolicy(name).block();
    }

    /**
     * Gets information about the certificate which represents the {@link CertificateProperties} from the key vault. This
     * operation requires the certificates/get permission.
     *
     * <p>The list operations {@link CertificateClient#listCertificates()} and {@link CertificateClient#listCertificateVersions(String)} return
     * the {@link PagedIterable} containing {@link CertificateProperties} as output excluding the properties like secretId and keyId of the certificate.
     * This operation can then be used to get the full certificate with its properties excluding the policy from {@code certificateProperties}.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.getCertificate#CertificateProperties}
     *
     * @param certificateProperties The {@link CertificateProperties} holding attributes of the certificate being requested.
     * @throws ResourceNotFoundException when a certificate with {@link CertificateProperties#getName() name} and {@link CertificateProperties#getVersion() version} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link CertificateProperties#getName()}  name} or {@link CertificateProperties#getVersion() version} is empty string.
     * @return The requested {@link Certificate certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Certificate getCertificate(CertificateProperties certificateProperties) {
        return getCertificateWithResponse(certificateProperties.getName(), certificateProperties.getVersion(), Context.NONE).getValue();
    }

    /**
     * Gets information about the latest version of the specified certificate. This operation requires the certificates/get permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a specific version of the key in the key vault. Prints out the returned certificate details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.getCertificateWithResponse#String-String-Context}
     *
     * @param name The name of the certificate to retrieve, cannot be null
     * @param version The version of the certificate to retrieve. If this is an empty String or null, this call is equivalent to calling {@link CertificateClient#getCertificateWithPolicy(String)}, with the latest version being retrieved.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws ResourceNotFoundException when a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException if {@code name} is empty string.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the requested {@link Certificate certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Certificate> getCertificateWithResponse(String name, String version, Context context) {
        return client.getCertificateWithResponse(name, version, context).block();
    }

    /**
     * Gets information about the specified version of the specified certificate. This operation requires the certificates/get permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a specific version of the key in the key vault. Prints out the returned certificate details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.getCertificate#String-String}
     *
     * @param name The name of the certificate to retrieve, cannot be null
     * @param version The version of the certificate to retrieve. If this is an empty String or null, this call is equivalent to calling {@link CertificateClient#getCertificateWithPolicy(String)}, with the latest version being retrieved.
     * @throws ResourceNotFoundException when a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException if {@code name} is empty string.
     * @return The requested {@link Certificate certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Certificate getCertificate(String name, String version) {
        return getCertificateWithResponse(name, version, Context.NONE).getValue();
    }

    /**
     * Updates the specified attributes associated with the specified certificate. The update operation changes specified attributes of an existing
     * stored certificate and attributes that are not specified in the request are left unchanged. This operation requires the certificates/update permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets latest version of the certificate, changes its tags and enabled status and then updates it in the Azure Key Vault. Prints out the
     * returned certificate details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.updateCertificateProperties#CertificateProperties}
     *
     * @param certificateProperties The {@link CertificateProperties} object with updated properties.
     * @throws NullPointerException if {@code certificate} is {@code null}.
     * @throws ResourceNotFoundException when a certificate with {@link CertificateProperties#getName() name} and {@link CertificateProperties#getVersion() version} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link CertificateProperties#getName() name} or {@link CertificateProperties#getVersion() version} is empty string.
     * @return The {@link CertificateProperties updated certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Certificate updateCertificateProperties(CertificateProperties certificateProperties) {
        return updateCertificatePropertiesWithResponse(certificateProperties, Context.NONE).getValue();
    }

    /**
     * Updates the specified attributes associated with the specified certificate. The update operation changes specified attributes of an existing
     * stored certificate and attributes that are not specified in the request are left unchanged. This operation requires the certificates/update permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets latest version of the certificate, changes its tags and enabled status and then updates it in the Azure Key Vault. Prints out the
     * returned certificate details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.updateCertificatePropertiesWithResponse#CertificateProperties-Context}
     *
     * @param certificateProperties The {@link CertificateProperties} object with updated properties.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws NullPointerException if {@code certificate} is {@code null}.
     * @throws ResourceNotFoundException when a certificate with {@link CertificateProperties#getName() name} and {@link CertificateProperties#getVersion() version} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link CertificateProperties#getName() name} or {@link CertificateProperties#getVersion() version} is empty string.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link CertificateProperties updated certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Certificate> updateCertificatePropertiesWithResponse(CertificateProperties certificateProperties, Context context) {
        return client.updateCertificatePropertiesWithResponse(certificateProperties, context).block();
    }


    /**
     * Deletes a certificate from a specified key vault. All the versions of the certificate along with its associated policy
     * get deleted. If soft-delete is enabled on the key vault then the certificate is placed in the deleted state and requires to be
     * purged for permanent deletion else the certificate is permanently deleted. The delete operation applies to any certificate stored in
     * Azure Key Vault but it cannot be applied to an individual version of a certificate. This operation requires the certificates/delete permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes the certificate in the Azure Key Vault. Prints out the
     * deleted certificate details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.deleteCertificate#string}
     *
     * @param name The name of the certificate to be deleted.
     * @throws ResourceNotFoundException when a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a certificate with {@code name} is empty string.
     * @return The {@link DeletedCertificate deleted certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DeletedCertificate deleteCertificate(String name) {
        return deleteCertificateWithResponse(name, Context.NONE).getValue();
    }

    /**
     * Deletes a certificate from a specified key vault. All the versions of the certificate along with its associated policy
     * get deleted. If soft-delete is enabled on the key vault then the certificate is placed in the deleted state and requires to be
     * purged for permanent deletion else the certificate is permanently deleted. The delete operation applies to any certificate stored in
     * Azure Key Vault but it cannot be applied to an individual version of a certificate. This operation requires the certificates/delete permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes the certificate in the Azure Key Vault. Prints out the
     * deleted certificate details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.deleteCertificateWithResponse#String-Context}
     *
     * @param name The name of the certificate to be deleted.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws ResourceNotFoundException when a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a certificate with {@code name} is empty string.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link DeletedCertificate deleted certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DeletedCertificate> deleteCertificateWithResponse(String name, Context context) {
        return client.deleteCertificateWithResponse(name, context).block();
    }

    /**
     * Retrieves information about the specified deleted certificate. The GetDeletedCertificate operation  is applicable for soft-delete
     * enabled vaults and additionally retrieves deleted certificate's attributes, such as retention interval, scheduled permanent deletion and the current deletion recovery level. This operation
     * requires the certificates/get permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p> Gets the deleted certificate from the key vault enabled for soft-delete. Prints out the
     * deleted certificate details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.getDeletedCertificate#string}
     *
     * @param name The name of the deleted certificate.
     * @throws ResourceNotFoundException when a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a certificate with {@code name} is empty string.
     * @return The {@link DeletedCertificate deleted certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DeletedCertificate getDeletedCertificate(String name) {
        return getDeletedCertificateWithResponse(name, Context.NONE).getValue();
    }

    /**
     * Retrieves information about the specified deleted certificate. The GetDeletedCertificate operation  is applicable for soft-delete
     * enabled vaults and additionally retrieves deleted certificate's attributes, such as retention interval, scheduled permanent deletion and the current deletion recovery level. This operation
     * requires the certificates/get permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p> Gets the deleted certificate from the key vault enabled for soft-delete. Prints out the
     * deleted certificate details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.getDeletedCertificateWithResponse#String-Context}
     *
     * @param name The name of the deleted certificate.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws ResourceNotFoundException when a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a certificate with {@code name} is empty string.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link DeletedCertificate deleted certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DeletedCertificate> getDeletedCertificateWithResponse(String name, Context context) {
        return client.getDeletedCertificateWithResponse(name, context).block();
    }

    /**
     * Permanently deletes the specified deleted certificate without possibility for recovery. The Purge Deleted Certificate operation is applicable for
     * soft-delete enabled vaults and is not available if the recovery level does not specify 'Purgeable'. This operation requires the certificate/purge permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Purges the deleted certificate from the key vault enabled for soft-delete. Prints out the
     * status code from the server response when a response has been received.</p>

     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.purgeDeletedCertificate#string}
     *
     * @param name The name of the deleted certificate.
     * @throws ResourceNotFoundException when a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a certificate with {@code name} is empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void purgeDeletedCertificate(String name) {
        purgeDeletedCertificateWithResponse(name, Context.NONE);
    }

    /**
     * Permanently deletes the specified deleted certificate without possibility for recovery. The Purge Deleted Certificate operation is applicable for
     * soft-delete enabled vaults and is not available if the recovery level does not specify 'Purgeable'. This operation requires the certificate/purge permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Purges the deleted certificate from the key vault enabled for soft-delete. Prints out the
     * status code from the server response when a response has been received.</p>

     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.purgeDeletedCertificateWithResponse#string-Context}
     *
     * @param name The name of the deleted certificate.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws ResourceNotFoundException when a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a certificate with {@code name} is empty string.
     * @return A response containing status code and HTTP headers.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> purgeDeletedCertificateWithResponse(String name, Context context) {
        return client.purgeDeletedCertificateWithResponse(name, context).block();
    }

    /**
     * Recovers the deleted certificate back to its current version under /certificates and can only be performed on a soft-delete enabled vault.
     * The RecoverDeletedCertificate operation performs the reversal of the Delete operation and must be issued during the retention interval
     * (available in the deleted certificate's attributes). This operation requires the certificates/recover permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Recovers the deleted certificate from the key vault enabled for soft-delete. Prints out the
     * recovered certificate details when a response has been received.</p>

     * {@codesnippet com.azure.security.certificatevault.certificates.CertificateClient.recoverDeletedCertificate#string}
     *
     * @param name The name of the deleted certificate to be recovered.
     * @throws ResourceNotFoundException when a certificate with {@code name} doesn't exist in the certificate vault.
     * @throws HttpRequestException when a certificate with {@code name} is empty string.
     * @return The {@link Certificate recovered certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Certificate recoverDeletedCertificate(String name) {
        return recoverDeletedCertificateWithResponse(name, Context.NONE).getValue();
    }

    /**
     * Recovers the deleted certificate back to its current version under /certificates and can only be performed on a soft-delete enabled vault.
     * The RecoverDeletedCertificate operation performs the reversal of the Delete operation and must be issued during the retention interval
     * (available in the deleted certificate's attributes). This operation requires the certificates/recover permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Recovers the deleted certificate from the key vault enabled for soft-delete. Prints out the
     * recovered certificate details when a response has been received.</p>

     * {@codesnippet com.azure.security.certificatevault.certificates.CertificateClient.recoverDeletedCertificateWithResponse#String-Context}
     *
     * @param name The name of the deleted certificate to be recovered.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws ResourceNotFoundException when a certificate with {@code name} doesn't exist in the certificate vault.
     * @throws HttpRequestException when a certificate with {@code name} is empty string.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link Certificate recovered certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Certificate> recoverDeletedCertificateWithResponse(String name, Context context) {
        return client.recoverDeletedCertificateWithResponse(name, context).block();
    }

    /**
     * Requests that a backup of the specified certificate be downloaded to the client. All versions of the certificate will
     * be downloaded. This operation requires the certificates/backup permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Backs up the certificate from the key vault. Prints out the
     * length of the certificate's backup byte array returned in the response.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.backupCertificate#string}
     *
     * @param name The name of the certificate.
     * @throws ResourceNotFoundException when a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a certificate with {@code name} is empty string.
     * @return The backed up certificate blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public byte[] backupCertificate(String name) {
        return backupCertificateWithResponse(name, Context.NONE).getValue();
    }

    /**
     * Requests that a backup of the specified certificate be downloaded to the client. All versions of the certificate will
     * be downloaded. This operation requires the certificates/backup permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Backs up the certificate from the key vault. Prints out the
     * length of the certificate's backup byte array returned in the response.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.backupCertificateWithResponse#String-Context}
     *
     * @param name The name of the certificate.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws ResourceNotFoundException when a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a certificate with {@code name} is empty string.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the backed up certificate blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<byte[]> backupCertificateWithResponse(String name, Context context) {
        return client.backupCertificateWithResponse(name, context).block();
    }

    /**
     * Restores a backed up certificate to the vault. All the versions of the certificate are restored to the vault. This operation
     * requires the certificates/restore permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Restores the certificate in the key vault from its backup. Prints out the restored certificate
     * details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.restoreCertificate#byte}
     *
     * @param backup The backup blob associated with the certificate.
     * @throws ResourceModifiedException when {@code backup} blob is malformed.
     * @return The {@link Certificate restored certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Certificate restoreCertificate(byte[] backup) {
        return restoreCertificateWithResponse(backup, Context.NONE).getValue();
    }

    /**
     * Restores a backed up certificate to the vault. All the versions of the certificate are restored to the vault. This operation
     * requires the certificates/restore permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Restores the certificate in the key vault from its backup. Prints out the restored certificate
     * details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.restoreCertificateWithResponse#byte-Context}
     *
     * @param backup The backup blob associated with the certificate.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws ResourceModifiedException when {@code backup} blob is malformed.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link Certificate restored certificate}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Certificate> restoreCertificateWithResponse(byte[] backup, Context context) {
        return client.restoreCertificateWithResponse(backup, context).block();
    }

    /**
     * List certificates in a the key vault. Retrieves the set of certificates resources in the key vault and the individual
     * certificate response in the iterable is represented by {@link CertificateProperties} as only the certificate identifier, thumbprint,
     * attributes and tags are provided in the response. The policy and individual certificate versions are not listed in
     * the response. This operation requires the certificates/list permission.
     *
     * <p>It is possible to get certificates with all the properties excluding the policy from this information. Loop over the {@link CertificateProperties} and
     * call {@link CertificateClient#getCertificate(CertificateProperties)} . This will return the {@link Certificate certificate}
     * with all its properties excluding the policy.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.listCertificates}
     *
     * @return A {@link PagedIterable} containing {@link CertificateProperties certificate} for all the certificates in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CertificateProperties> listCertificates() {
        return new PagedIterable<>(client.listCertificates(false, Context.NONE));
    }

    /**
     * List certificates in a the key vault. Retrieves the set of certificates resources in the key vault and the individual
     * certificate response in the iterable is represented by {@link CertificateProperties} as only the certificate identifier, thumbprint,
     * attributes and tags are provided in the response. The policy and individual certificate versions are not listed in
     * the response. This operation requires the certificates/list permission.
     *
     * <p>It is possible to get certificates with all the properties excluding the policy from this information. Loop over the {@link CertificateProperties} and
     * call {@link CertificateClient#getCertificate(CertificateProperties)} . This will return the {@link Certificate certificate}
     * with all its properties excluding the policy.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.listCertificates#context}
     *
     * @param includePending indicate if pending certificates should be included in the results.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link PagedIterable} containing {@link CertificateProperties certificate} for all the certificates in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CertificateProperties> listCertificates(boolean includePending, Context context) {
        return new PagedIterable<>(client.listCertificates(includePending, context));
    }

    /**
     * Lists the {@link DeletedCertificate deleted certificates} in the key vault currently available for recovery. This operation includes
     * deletion-specific information and is applicable for vaults enabled for soft-delete. This operation requires the
     * {@code certificates/get/list} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Lists the deleted certificates in the key vault. Prints out the
     * recovery id of each deleted certificate when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.listDeletedCertificates}
     *
     * @return A {@link PagedIterable} containing all of the {@link DeletedCertificate deleted certificates} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DeletedCertificate> listDeletedCertificates() {
        return listDeletedCertificates(Context.NONE);
    }


    /**
     * Lists the {@link DeletedCertificate deleted certificates} in the key vault currently available for recovery. This operation includes
     * deletion-specific information and is applicable for vaults enabled for soft-delete. This operation requires the
     * {@code certificates/get/list} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Lists the deleted certificates in the key vault. Prints out the
     * recovery id of each deleted certificate when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.listDeletedCertificates#context}
     *
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link PagedIterable} containing all of the {@link DeletedCertificate deleted certificates} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DeletedCertificate> listDeletedCertificates(Context context) {
        return new PagedIterable<>(client.listDeletedCertificates(context));
    }

    /**
     * List all versions of the specified certificate. The individual certificate response in the iterable is represented by {@link CertificateProperties}
     * as only the certificate identifier, thumbprint, attributes and tags are provided in the response. The policy is not listed in
     * the response. This operation requires the certificates/list permission.
     *
     * <p>It is possible to get the certificates with properties excluding the policy for all the versions from this information. Loop over the {@link CertificateProperties} and
     * call {@link CertificateClient#getCertificate(CertificateProperties)}. This will return the {@link Certificate certificate}
     * with all its properties excluding the policy.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.listCertificateVersions}
     *
     * @param name The name of the certificate.
     * @throws ResourceNotFoundException when a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a certificate with {@code name} is empty string.
     * @return A {@link PagedIterable} containing {@link CertificateProperties certificate} of all the versions of the specified certificate in the vault. Paged Iterable is empty if certificate with {@code name} does not exist in key vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CertificateProperties> listCertificateVersions(String name) {
        return listCertificateVersions(name, Context.NONE);
    }

    /**
     * List all versions of the specified certificate. The individual certificate response in the iterable is represented by {@link CertificateProperties}
     * as only the certificate identifier, thumbprint, attributes and tags are provided in the response. The policy is not listed in
     * the response. This operation requires the certificates/list permission.
     *
     * <p>It is possible to get the certificates with properties excluding the policy for all the versions from this information. Loop over the {@link CertificateProperties} and
     * call {@link CertificateClient#getCertificate(CertificateProperties)}. This will return the {@link Certificate certificate}
     * with all its properties excluding the policy.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.listCertificateVersions#context}
     *
     * @param name The name of the certificate.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws ResourceNotFoundException when a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a certificate with {@code name} is empty string.
     * @return A {@link PagedIterable} containing {@link CertificateProperties certificate} of all the versions of the specified certificate in the vault. Iterable is empty if certificate with {@code name} does not exist in key vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CertificateProperties> listCertificateVersions(String name, Context context) {
        return new PagedIterable<>(client.listCertificateVersions(name, context));
    }

    /**
     * Retrieves the policy of the specified certificate in the key vault. This operation requires the {@code certificates/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the policy of a certirifcate in the key vault. Prints out the
     * returned certificate policy details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.getCertificatePolicy#string}
     *
     * @param name The name of the certificate whose policy is to be retrieved, cannot be null
     * @throws ResourceNotFoundException when a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException if {@code name} is empty string.
     * @return The requested {@link CertificatePolicy certificate policy}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CertificatePolicy getCertificatePolicy(String name) {
        return getCertificatePolicyWithResponse(name, Context.NONE).getValue();
    }

    /**
     * Retrieves the policy of the specified certificate in the key vault. This operation requires the {@code certificates/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the policy of a certirifcate in the key vault. Prints out the
     * returned certificate policy details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.getCertificatePolicyWithResponse#string}
     *
     * @param name The name of the certificate whose policy is to be retrieved, cannot be null
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws ResourceNotFoundException when a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException if {@code name} is empty string.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the requested {@link CertificatePolicy certificate policy}.
     */
    public Response<CertificatePolicy> getCertificatePolicyWithResponse(String name, Context context) {
        return client.getCertificatePolicyWithResponse(name, context).block();
    }

    /**
     * Updates the policy for a certificate. The update operation changes specified attributes of the certificate policy and attributes
     * that are not specified in the request are left unchanged. This operation requires the {@code certificates/update} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the certificate policy, changes its properties and then updates it in the Azure Key Vault. Prints out the
     * returned policy details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.updateCertificatePolicy#string}
     *
     * @param certificateName The name of the certificate whose policy is to be updated.
     * @param policy The certificate policy to be updated.
     * @throws NullPointerException if {@code policy} is {@code null}.
     * @throws ResourceNotFoundException when a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException if {@code name} is empty string or if {@code policy} is invalid.
     * @return The updated {@link CertificatePolicy certificate policy}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CertificatePolicy updateCertificatePolicy(String certificateName, CertificatePolicy policy) {
        return updateCertificatePolicyWithResponse(certificateName, policy, Context.NONE).getValue();
    }

    /**
     * Updates the policy for a certificate. The update operation changes specified attributes of the certificate policy and attributes
     * that are not specified in the request are left unchanged. This operation requires the {@code certificates/update} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the certificate policy, changes its properties and then updates it in the Azure Key Vault. Prints out the
     * returned policy details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.updateCertificatePolicyWithResponse#string}
     *
     * @param certificateName The name of the certificate whose policy is to be updated.
     * @param policy The certificate policy to be updated.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws NullPointerException if {@code policy} is {@code null}.
     * @throws ResourceNotFoundException when a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException if {@code name} is empty string or if {@code policy} is invalid.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the updated {@link CertificatePolicy certificate policy}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CertificatePolicy> updateCertificatePolicyWithResponse(String certificateName, CertificatePolicy policy, Context context) {
        return client.updateCertificatePolicyWithResponse(certificateName, policy, context).block();
    }

    /**
     * Creates the specified certificate issuer. The SetCertificateIssuer operation updates the specified certificate issuer if it
     * already exists or adds it if doesn't exist. This operation requires the certificates/setissuers permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new certificate issuer in the key vault. Prints out the created certificate
     * issuer details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.createIssuer#String-String}
     *
     * @param name The name of the certificate issuer to be created.
     * @param provider The provider of the certificate issuer to be created.
     * @throws ResourceModifiedException when invalid certificate issuer {@code name} or {@code provider} configuration is provided.
     * @throws HttpRequestException when a certificate issuer with {@code name} is empty string.
     * @return The created {@link Issuer certificate issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Issuer createIssuer(String name, String provider) {
        return createIssuerWithResponse(new Issuer(name, provider), Context.NONE).getValue();
    }

    /**
     * Creates the specified certificate issuer. The SetCertificateIssuer operation updates the specified certificate issuer if it
     * already exists or adds it if doesn't exist. This operation requires the certificates/setissuers permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new certificate issuer in the key vault. Prints out the created certificate issuer details when a
     * response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.createIssuer#issuer}
     *
     * @param issuer The configuration of the certificate issuer to be created.
     * @throws ResourceModifiedException when invalid certificate issuer {@code issuer} configuration is provided.
     * @throws HttpRequestException when a certificate issuer with {@code name} is empty string.
     * @return The created {@link Issuer certificate issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Issuer createIssuer(Issuer issuer) {
        return createIssuerWithResponse(issuer, Context.NONE).getValue();
    }

    /**
     * Creates the specified certificate issuer. The SetCertificateIssuer operation updates the specified certificate issuer if it
     * already exists or adds it if doesn't exist. This operation requires the certificates/setissuers permission.

     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new certificate issuer in the key vault. Prints out the created certificate
     * issuer details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.createIssuerWithResponse#Issuer-Context}
     *
     * @param issuer The configuration of the certificate issuer to be created.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws ResourceModifiedException when invalid certificate issuer {@code issuer} configuration is provided.
     * @throws HttpRequestException when a certificate issuer with {@code name} is empty string.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the created {@link Issuer certificate issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Issuer> createIssuerWithResponse(Issuer issuer, Context context) {
        return client.createIssuerWithResponse(issuer, context).block();
    }

    /**
     * Retrieves the specified certificate issuer from the key vault. This operation requires the certificates/manageissuers/getissuers permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the specificed certifcate issuer in the key vault. Prints out the returned certificate issuer details when
     * a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.getIssuerWithResponse#string-context}
     *
     * @param name The name of the certificate to retrieve, cannot be null
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws ResourceNotFoundException when a certificate issuer with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException if {@code name} is empty string.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the requested {@link Issuer certificate issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Issuer> getIssuerWithResponse(String name, Context context) {
        return client.getIssuerWithResponse(name, context).block();
    }

    /**
     * Retrieves the specified certificate issuer from the key vault. This operation requires the certificates/manageissuers/getissuers permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the specified certificate issuer in the key vault. Prints out the returned certificate issuer details
     * when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.getIssuer#string}
     *
     * @param name The name of the certificate to retrieve, cannot be null
     * @throws ResourceNotFoundException when a certificate issuer with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException if {@code name} is empty string.
     * @return The requested {@link Issuer certificate issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Issuer getIssuer(String name) {
        return getIssuerWithResponse(name, Context.NONE).getValue();
    }

    /**
     * Gets information about the certificate issuer which represents the {@link IssuerProperties} from the key vault. This operation
     * requires the certificates/manageissuers/getissuers permission.
     *
     * <p>The list operations {@link CertificateClient#listIssuers()} return the {@link PagedIterable} containing
     * {@link IssuerProperties issuerProperties} as output excluding the properties like accountId and organization details of the certificate issuer.
     * This operation can then be used to get the full certificate issuer with its properties from {@code issuerProperties}.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.getIssuer#issuerProperties}
     *
     * @param issuerProperties The {@link IssuerProperties issuerProperties} holding attributes of the certificate issuer being requested.
     * @throws ResourceNotFoundException when a certificate with {@link IssuerProperties#getName() name} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link IssuerProperties#getName() name} is empty string.
     * @return The requested {@link Issuer certificate issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Issuer getIssuer(IssuerProperties issuerProperties) {
        return getIssuerWithResponse(issuerProperties, Context.NONE).getValue();
    }

    /**
     * Gets information about the certificate issuer which represents the {@link IssuerProperties} from the key vault. This operation
     * requires the certificates/manageissuers/getissuers permission.
     *
     * <p>The list operations {@link CertificateClient#listIssuers()} return the {@link PagedIterable} containing
     * {@link IssuerProperties issuerProperties} as output excluding the properties like accountId and organization details of the certificate issuer.
     * This operation can then be used to get the full certificate issuer with its properties from {@code issuerProperties}.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.getIssuerWithResponse#issuerProperties-context}
     *
     * @param issuerProperties The {@link IssuerProperties issuerProperties} holding attributes of the certificate issuer being requested.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws ResourceNotFoundException when a certificate with {@link IssuerProperties#getName() name} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link IssuerProperties#getName() name} is empty string.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the requested {@link Issuer certificate issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Issuer> getIssuerWithResponse(IssuerProperties issuerProperties, Context context) {
        return client.getIssuerWithResponse(issuerProperties.getName(), context).block();
    }

    /**
     * Deletes the specified certificate issuer. The DeleteCertificateIssuer operation permanently removes the specified certificate
     * issuer from the key vault. This operation requires the {@code certificates/manageissuers/deleteissuers permission}.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes the certificate issuer in the Azure Key Vault. Prints out the
     * deleted certificate details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.deleteIssuerWithResponse#string-context}
     *
     * @param name The name of the certificate issuer to be deleted.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws ResourceNotFoundException when a certificate issuer with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a certificate issuer with {@code name} is empty string.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link Issuer deleted issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Issuer> deleteIssuerWithResponse(String name, Context context) {
        return client.deleteIssuerWithResponse(name, context).block();
    }

    /**
     * Deletes the specified certificate issuer. The DeleteCertificateIssuer operation permanently removes the specified certificate
     * issuer from the key vault. This operation requires the {@code certificates/manageissuers/deleteissuers permission}.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes the certificate issuer in the Azure Key Vault. Prints out the deleted certificate details when a
     * response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.deleteIssuer#string}
     *
     * @param name The name of the certificate issuer to be deleted.
     * @throws ResourceNotFoundException when a certificate issuer with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a certificate issuer with {@code name} is empty string.
     * @return The {@link Issuer deleted issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Issuer deleteIssuer(String name) {
        return deleteIssuerWithResponse(name, Context.NONE).getValue();
    }

    /**
     * List all the certificate issuers resources in the key vault. The individual certificate issuer response in the iterable is represented by {@link IssuerProperties}
     * as only the certificate issuer identifier and provider are provided in the response. This operation requires the
     * {@code certificates/manageissuers/getissuers} permission.
     *
     * <p>It is possible to get the certificate issuer with all of its properties from this information. Loop over the {@link IssuerProperties issuerProperties} and
     * call {@link CertificateClient#getIssuer(IssuerProperties issuerProperties)} . This will return the {@link Issuer issuer}
     * with all its properties.</p>.
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.listIssuers}
     *
     * @return A {@link PagedIterable} containing all of the {@link IssuerProperties certificate issuers} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<IssuerProperties> listIssuers() {
        return listIssuers(Context.NONE);
    }

    /**
     * List all the certificate issuers resources in the key vault. The individual certificate issuer response in the iterable is represented by {@link IssuerProperties}
     * as only the certificate issuer identifier and provider are provided in the response. This operation requires the
     * {@code certificates/manageissuers/getissuers} permission.
     *
     * <p>It is possible to get the certificate issuer with all of its properties from this information. Loop over the {@link IssuerProperties issuerProperties} and
     * call {@link CertificateClient#getIssuer(IssuerProperties issuerProperties)} . This will return the {@link Issuer issuer}
     * with all its properties.</p>.
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.listIssuers#context}
     *
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link PagedIterable} containing all of the {@link IssuerProperties certificate issuers} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<IssuerProperties> listIssuers(Context context) {
        return new PagedIterable<>(client.listIssuers(context));
    }

    /**
     * Updates the specified certificate issuer. The UpdateCertificateIssuer operation updates the specified attributes of
     * the certificate issuer entity. This operation requires the certificates/setissuers permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the certificate issuer, changes its attributes/properties then updates it in the Azure Key Vault. Prints out the
     * returned certificate issuer details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.updateIssuer#issuer}
     *
     * @param issuer The {@link Issuer issuer} with updated properties.
     * @throws NullPointerException if {@code issuer} is {@code null}.
     * @throws ResourceNotFoundException when a certificate issuer with {@link Issuer#getName() name} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link Issuer#getName() name} is empty string.
     * @return The {@link Issuer updated issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Issuer updateIssuer(Issuer issuer) {
        return updateIssuerWithResponse(issuer, Context.NONE).getValue();
    }

    /**
     * Updates the specified certificate issuer. The UpdateCertificateIssuer operation updates the specified attributes of
     * the certificate issuer entity. This operation requires the certificates/setissuers permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the certificate issuer, changes its attributes/properties then updates it in the Azure Key Vault. Prints out the
     * returned certificate issuer details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.updateIssuerWithResponse#Issuer-Context}
     *
     * @param issuer The {@link Issuer issuer} with updated properties.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws NullPointerException if {@code issuer} is {@code null}.
     * @throws ResourceNotFoundException when a certificate issuer with {@link Issuer#getName() name} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link Issuer#getName() name} is empty string.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link Issuer updated issuer}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Issuer> updateIssuerWithResponse(Issuer issuer, Context context) {
        return client.updateIssuerWithResponse(issuer, context).block();
    }


    /**
     * Sets the certificate contacts on the key vault. This operation requires the {@code certificates/managecontacts} permission.
     *
     *<p>The {@link LifetimeAction} of type {@link LifetimeActionType#EMAIL_CONTACTS} set on a {@link CertificatePolicy} emails the contacts set on the vault when triggered.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Sets the certificate contacts in the Azure Key Vault. Prints out the returned contacts details.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.setContacts#contacts}
     *
     * @param contacts The list of contacts to set on the vault.
     * @throws HttpRequestException when a contact information provided is invalid/incomplete.
     * @return A {@link PagedIterable} containing all of the {@link Contact certificate contacts} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<Contact> setContacts(List<Contact> contacts) {
        return setContacts(contacts, Context.NONE);
    }

    /**
     * Sets the certificate contacts on the key vault. This operation requires the {@code certificates/managecontacts} permission.
     *
     *<p>The {@link LifetimeAction} of type {@link LifetimeActionType#EMAIL_CONTACTS} set on a {@link CertificatePolicy} emails the contacts set on the vault when triggered.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Sets the certificate contacts in the Azure Key Vault. Prints out the returned contacts details.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.setContacts#contacts-context}
     *
     * @param contacts The list of contacts to set on the vault.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws HttpRequestException when a contact information provided is invalid/incomplete.
     * @return A {@link PagedIterable} containing all of the {@link Contact certificate contacts} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<Contact> setContacts(List<Contact> contacts, Context context) {
        return new PagedIterable<>(client.setContacts(contacts, context));
    }

    /**
     * Lists the certificate contacts in the key vault. This operation requires the certificates/managecontacts permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Lists the certificate contacts in the Azure Key Vault. Prints out the returned contacts details in the response.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.listContacts}
     *
     * @return A {@link PagedIterable} containing all of the {@link Contact certificate contacts} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<Contact> listContacts() {
        return listContacts(Context.NONE);
    }


    /**
     * Lists the certificate contacts in the key vault. This operation requires the certificates/managecontacts permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Lists the certificate contacts in the Azure Key Vault. Prints out the returned contacts details in the response.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.listContacts#context}
     *
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link PagedIterable} containing all of the {@link Contact certificate contacts} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<Contact> listContacts(Context context) {
        return new PagedIterable<>(client.listContacts(context));
    }

    /**
     * Deletes the certificate contacts in the key vault. This operation requires the {@code certificates/managecontacts} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes the certificate contacts in the Azure Key Vault. Subscribes to the call and prints out the
     * deleted contacts details.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.deleteContacts}
     *
     * @return A {@link PagedIterable} containing all of the deleted {@link Contact certificate contacts} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<Contact> deleteContacts() {
        return deleteContacts(Context.NONE);
    }

    /**
     * Deletes the certificate contacts in the key vault. This operation requires the {@code certificates/managecontacts} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes the certificate contacts in the Azure Key Vault. Prints out the deleted contacts details in the response.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.deleteContacts#context}
     *
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link PagedIterable} containing all of the deleted {@link Contact certificate contacts} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<Contact> deleteContacts(Context context) {
        return new PagedIterable<>(client.deleteContacts(context));
    }

    /**
     * Deletes the creation operation for the specified certificate that is in the process of being created. The certificate is
     * no longer created. This operation requires the {@code certificates/update permission}.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Triggers certificate creation and then deletes the certificate creation operation in the Azure Key Vault. Subscribes to the call and prints out the
     * deleted certificate operation details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.deleteCertificateOperation#string}
     *
     * @param certificateName The name of the certificate which is in the process of being created.
     * @throws ResourceNotFoundException when a certificate operation for a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when the {@code name} is empty string.
     * @return The deleted {@link CertificateOperation certificate operation}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CertificateOperation deleteCertificateOperation(String certificateName) {
        return deleteCertificateOperationWithResponse(certificateName, Context.NONE).getValue();
    }

    /**
     * Deletes the creation operation for the specified certificate that is in the process of being created. The certificate is
     * no longer created. This operation requires the {@code certificates/update permission}.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Triggers certificate creation and then deletes the certificate creation operation in the Azure Key Vault. Subscribes to the call and prints out the
     * deleted certificate operation details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.deleteCertificateOperationWithResponse#string}
     *
     * @param certificateName The name of the certificate which is in the process of being created.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws ResourceNotFoundException when a certificate operation for a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when the {@code name} is empty string.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link CertificateOperation deleted certificate operation}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CertificateOperation> deleteCertificateOperationWithResponse(String certificateName, Context context) {
        return client.deleteCertificateOperationWithResponse(certificateName, context).block();
    }

    /**
     * Cancels a certificate creation operation that is already in progress. This operation requires the {@code certificates/update} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Triggers certificate creation and then cancels the certificate creation operation in the Azure Key Vault. Subscribes to the call and prints out the
     * updated certificate operation details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.cancelCertificateOperation#string}
     *
     * @param certificateName The name of the certificate which is in the process of being created.
     * @throws ResourceNotFoundException when a certificate operation for a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when the {@code name} is empty string.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link CertificateOperation cancelled certificate operation}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CertificateOperation cancelCertificateOperation(String certificateName) {
        return cancelCertificateOperationWithResponse(certificateName, Context.NONE).getValue();
    }


    /**
     * Cancels a certificate creation operation that is already in progress. This operation requires the {@code certificates/update} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Triggers certificate creation and then cancels the certificate creation operation in the Azure Key Vault. Subscribes to the call and prints out the
     * updated certificate operation details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.cancelCertificateOperationWithResponse#string}
     *
     * @param certificateName The name of the certificate which is in the process of being created.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws ResourceNotFoundException when a certificate operation for a certificate with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when the {@code name} is empty string.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link CertificateOperation cancelled certificate operation}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CertificateOperation> cancelCertificateOperationWithResponse(String certificateName, Context context) {
        return client.cancelCertificateOperationWithResponse(certificateName, context).block();
    }

    /**
     * Gets the pending certificate signing request for the specified certificate under pending status.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the pending signing request of a certificate created with third party issuer. Prints out the
     * returned certificate signing request details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.getPendingCertificateSigningRequest#String}
     *
     * @param certificateName the certificate for whom certifcate signing request is needed
     * @return The cerficate signing request blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public byte[] getPendingCertificateSigningRequest(String certificateName) {
        return getPendingCertificateSigningRequestWithResponse(certificateName, Context.NONE).getValue();
    }

    /**
     * Gets the pending certificate signing request for the specified certificate under pending status.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the pending signing request of a certificate created with third party issuer. Prints out the
     * returned certificate signing request details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.getPendingCertificateSigningRequestWithResponse#String-Context}
     *
     * @param certificateName the certificate for whom certifcate signing request is needed
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the certificate signing request blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<byte[]> getPendingCertificateSigningRequestWithResponse(String certificateName, Context context) {
        return client.getPendingCertificateSigningRequestWithResponse(certificateName, context).block();
    }


    /**
     * Merges a certificate or a certificate chain with a key pair currently available in the service. This operation requires
     * the {@code certificates/create} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p> Merges a certificate with a kay pair available in the service.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.mergeCertificate#String-List}
     *
     * @param name the name of the certificate.
     * @param x509Certificates the certificate or certificate chain to merge.
     * @throws HttpRequestException if {@code x509Certificates} is invalid/corrupt or {@code name} is empty.
     * @return The merged certificate.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Certificate mergeCertificate(String name, List<byte[]> x509Certificates) {
        return mergeCertificateWithResponse(name, x509Certificates, Context.NONE).getValue();
    }

    /**
     * Merges a certificate or a certificate chain with a key pair currently available in the service. This operation requires
     * the {@code certificates/create} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p> Merges a certificate with a kay pair available in the service.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.mergeCertificateWithResponse#String-List-Context}
     *
     * @param name the name of the certificate.
     * @param x509Certificates the certificate or certificate chain to merge.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws HttpRequestException if {@code x509Certificates} is invalid/corrupt or {@code name} is empty.
     * @return The merged certificate.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Certificate> mergeCertificateWithResponse(String name, List<byte[]> x509Certificates, Context context) {
        return client.mergeCertificateWithResponse(name, x509Certificates, context).block();
    }

    /**
     * Merges a certificate or a certificate chain with a key pair currently available in the service. This operation requires
     * the {@code certificates/create} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p> Merges a certificate with a kay pair available in the service.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.mergeCertificate#config}
     *
     * @param mergeCertificateConfig the merge certificate configuration holding the x509 certificates.
     * @throws NullPointerException when {@code mergeCertificateConfig} is null.
     * @throws HttpRequestException if {@code mergeCertificateConfig} is invalid/corrupt.
     * @return The merged certificate.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Certificate mergeCertificate(MergeCertificateOptions mergeCertificateConfig) {
        return mergeCertificateWithResponse(mergeCertificateConfig, Context.NONE).getValue();
    }

    /**
     * Merges a certificate or a certificate chain with a key pair currently available in the service. This operation requires
     * the {@code certificates/create} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p> Merges a certificate with a kay pair available in the service.</p>
     *
     * {@codesnippet com.azure.security.keyvault.certificates.CertificateClient.mergeCertificateWithResponse#config}
     *
     * @param mergeCertificateConfig the merge certificate configuration holding the x509 certificates.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws NullPointerException when {@code mergeCertificateConfig} is null.
     * @throws HttpRequestException if {@code mergeCertificateConfig} is invalid/corrupt.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the merged certificate.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Certificate> mergeCertificateWithResponse(MergeCertificateOptions mergeCertificateConfig, Context context) {
        Objects.requireNonNull(mergeCertificateConfig, "'mergeCertificateConfig' cannot be null.");
        return client.mergeCertificateWithResponse(mergeCertificateConfig, context).block();
    }

    /**
     * Imports a pre-existing certificate to the key vault. The specified certificate must be in PFX or PEM format,
     * and must contain the private key as well as the x509 certificates. This operation requires the {@code certificates/import} permission.
     *
     * @param importOptions The details of the certificate to import to the key vault
     * @throws HttpRequestException when the {@code importOptions} are invalid.
     * @return the {@link Certificate imported certificate}.
     */
    public Certificate importCertificate(CertificateImportOptions importOptions) {
        return importCertificateWithResponse(importOptions, Context.NONE).getValue();
    }

    /**
     * Imports a pre-existing certificate to the key vault. The specified certificate must be in PFX or PEM format,
     * and must contain the private key as well as the x509 certificates. This operation requires the {@code certificates/import} permission.
     *
     * @param importOptions The details of the certificate to import to the key vault
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws HttpRequestException when the {@code importOptions} are invalid.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link Certificate imported certificate}.
     */
    public Response<Certificate> importCertificateWithResponse(CertificateImportOptions importOptions, Context context) {
        return client.importCertificateWithResponse(importOptions, context).block();
    }
}
