// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.batch;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.batch.protocol.models.BatchErrorException;
import com.microsoft.azure.batch.protocol.models.Certificate;
import com.microsoft.azure.batch.protocol.models.CertificateAddOptions;
import com.microsoft.azure.batch.protocol.models.CertificateAddParameter;
import com.microsoft.azure.batch.protocol.models.CertificateCancelDeletionOptions;
import com.microsoft.azure.batch.protocol.models.CertificateDeleteOptions;
import com.microsoft.azure.batch.protocol.models.CertificateFormat;
import com.microsoft.azure.batch.protocol.models.CertificateGetOptions;
import com.microsoft.azure.batch.protocol.models.CertificateListOptions;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;

/**
 * Performs certificate-related operations on an Azure Batch account.
 * Warning: This operation is deprecated and will be removed after February, 2024. Please use the [Azure KeyVault Extension](https://learn.microsoft.com/azure/batch/batch-certificate-migration-guide) instead.
 */
@Deprecated
public class CertificateOperations implements IInheritedBehaviors {

    private Collection<BatchClientBehavior> customBehaviors;

    private final BatchClient parentBatchClient;

    /**
     * The SHA certificate algorithm.
     */
    public static final String SHA1_CERTIFICATE_ALGORITHM = "sha1";

    CertificateOperations(BatchClient batchClient, Iterable<BatchClientBehavior> inheritedBehaviors) {
        parentBatchClient = batchClient;

        // inherit from instantiating parent
        InternalHelper.inheritClientBehaviorsAndSetPublicProperty(this, inheritedBehaviors);
    }

    /**
     * Gets a collection of behaviors that modify or customize requests to the Batch service.
     *
     * @return A collection of {@link BatchClientBehavior} instances.
     */
    @Override
    public Collection<BatchClientBehavior> customBehaviors() {
        return customBehaviors;
    }

    /**
     * Sets a collection of behaviors that modify or customize requests to the Batch service.
     *
     * @param behaviors The collection of {@link BatchClientBehavior} instances.
     * @return The current instance.
     */
    @Override
    public IInheritedBehaviors withCustomBehaviors(Collection<BatchClientBehavior> behaviors) {
        customBehaviors = behaviors;
        return this;
    }

    private static String getThumbPrint(java.security.cert.Certificate cert) throws NoSuchAlgorithmException, CertificateEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] der = cert.getEncoded();
        md.update(der);
        byte[] digest = md.digest();
        return hexify(digest);
    }

    private static String hexify(byte[] bytes) {
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        StringBuilder buf = new StringBuilder(bytes.length * 2);

        for (byte b : bytes) {
            buf.append(hexDigits[(b & 0xf0) >> 4]);
            buf.append(hexDigits[b & 0x0f]);
        }

        return buf.toString();
    }

    /**
     * Adds a certificate to the Batch account.
     *
     * @param certStream The certificate data in .cer format.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     * @throws CertificateException Exception thrown when an error is encountered processing the provided certificate.
     * @throws NoSuchAlgorithmException Exception thrown if the X.509 provider is not registered in the Java security provider list.
     */
    public void createCertificate(InputStream certStream) throws BatchErrorException, IOException, CertificateException, NoSuchAlgorithmException {
        createCertificate(certStream, null);
    }

    /**
     * Adds a certificate to the Batch account.
     *
     * @param certStream The certificate data in .cer format.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     * @throws CertificateException Exception thrown when an error is encountered processing the provided certificate.
     * @throws NoSuchAlgorithmException Exception thrown if the X.509 provider is not registered in the Java security provider list.
     */
    public void createCertificate(InputStream certStream, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException, CertificateException, NoSuchAlgorithmException {
        CertificateFactory x509CertFact = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) x509CertFact.generateCertificate(certStream);

        CertificateAddParameter addParam = new CertificateAddParameter()
            .withCertificateFormat(CertificateFormat.CER)
            .withThumbprintAlgorithm(SHA1_CERTIFICATE_ALGORITHM)
            .withThumbprint(getThumbPrint(cert))
            .withData(Base64.encodeBase64String(cert.getEncoded()));

        createCertificate(addParam, additionalBehaviors);
    }

    /**
     * Adds a certificate to the Batch account.
     *
     * @param certificate The certificate to be added.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void createCertificate(CertificateAddParameter certificate) throws BatchErrorException, IOException {
        createCertificate(certificate, null);
    }

    /**
     * Adds a certificate to the Batch account.
     *
     * @param certificate The certificate to be added.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void createCertificate(CertificateAddParameter certificate, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        CertificateAddOptions options = new CertificateAddOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this.parentBatchClient.protocolLayer().certificates().add(certificate, options);
    }

    /**
     * Cancels a failed deletion of the specified certificate. This operation can be performed only when
     * the certificate is in the {@link com.microsoft.azure.batch.protocol.models.CertificateState#DELETE_FAILED Delete Failed} state, and restores
     * the certificate to the {@link com.microsoft.azure.batch.protocol.models.CertificateState#ACTIVE Active} state.
     *
     * @param thumbprintAlgorithm The algorithm used to derive the thumbprint parameter. This must be sha1.
     * @param thumbprint The thumbprint of the certificate that failed to delete.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void cancelDeleteCertificate(String thumbprintAlgorithm, String thumbprint) throws BatchErrorException, IOException {
        cancelDeleteCertificate(thumbprintAlgorithm, thumbprint, null);
    }

    /**
     * Cancels a failed deletion of the specified certificate. This operation can be performed only when
     * the certificate is in the {@link com.microsoft.azure.batch.protocol.models.CertificateState#DELETE_FAILED Delete Failed} state, and restores
     * the certificate to the {@link com.microsoft.azure.batch.protocol.models.CertificateState#ACTIVE Active} state.
     *
     * @param thumbprintAlgorithm The algorithm used to derive the thumbprint parameter. This must be sha1.
     * @param thumbprint The thumbprint of the certificate that failed to delete.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void cancelDeleteCertificate(String thumbprintAlgorithm, String thumbprint, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        CertificateCancelDeletionOptions options = new CertificateCancelDeletionOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this.parentBatchClient.protocolLayer().certificates().cancelDeletion(thumbprintAlgorithm, thumbprint, options);
    }

    /**
     * Deletes the certificate from the Batch account.
     * <p>The delete operation requests that the certificate be deleted. The request puts the certificate in the {@link com.microsoft.azure.batch.protocol.models.CertificateState#DELETING Deleting} state.
     * The Batch service will perform the actual certificate deletion without any further client action.</p>
     * <p>You cannot delete a certificate if a resource (pool or compute node) is using it. Before you can delete a certificate, you must therefore make sure that:</p>
     * <ul>
     *  <li>The certificate is not associated with any pools.</li>
     *  <li>The certificate is not installed on any compute nodes. (Even if you remove a certificate from a pool, it is not removed from existing compute nodes in that pool until they restart.)</li>
     * </ul>
     * <p>If you try to delete a certificate that is in use, the deletion fails. The certificate state changes to {@link com.microsoft.azure.batch.protocol.models.CertificateState#DELETE_FAILED Delete Failed}.
     * You can use {@link #cancelDeleteCertificate(String, String)} to set the status back to Active if you decide that you want to continue using the certificate.</p>
     *
     * @param thumbprintAlgorithm The algorithm used to derive the thumbprint parameter. This must be sha1.
     * @param thumbprint The thumbprint of the certificate to delete.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void deleteCertificate(String thumbprintAlgorithm, String thumbprint) throws BatchErrorException, IOException {
        deleteCertificate(thumbprintAlgorithm, thumbprint, null);
    }

    /**
     * Deletes the certificate from the Batch account.
     * <p>The delete operation requests that the certificate be deleted. The request puts the certificate in the {@link com.microsoft.azure.batch.protocol.models.CertificateState#DELETING Deleting} state.
     * The Batch service will perform the actual certificate deletion without any further client action.</p>
     * <p>You cannot delete a certificate if a resource (pool or compute node) is using it. Before you can delete a certificate, you must therefore make sure that:</p>
     * <ul>
     *  <li>The certificate is not associated with any pools.</li>
     *  <li>The certificate is not installed on any compute nodes. (Even if you remove a certificate from a pool, it is not removed from existing compute nodes in that pool until they restart.)</li>
     * </ul>
     * <p>If you try to delete a certificate that is in use, the deletion fails. The certificate state changes to {@link com.microsoft.azure.batch.protocol.models.CertificateState#DELETE_FAILED Delete Failed}.
     *
     * You can use {@link #cancelDeleteCertificate(String, String)} to set the status back to Active if you decide that you want to continue using the certificate.</p>
     * @param thumbprintAlgorithm The algorithm used to derive the thumbprint parameter. This must be sha1.
     * @param thumbprint The thumbprint of the certificate to delete.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void deleteCertificate(String thumbprintAlgorithm, String thumbprint, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        CertificateDeleteOptions options = new CertificateDeleteOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this.parentBatchClient.protocolLayer().certificates().delete(thumbprintAlgorithm, thumbprint, options);
    }

    /**
     * Gets the specified {@link Certificate}.
     *
     * @param thumbprintAlgorithm The algorithm used to derive the thumbprint parameter. This must be sha1.
     * @param thumbprint The thumbprint of the certificate to get.
     * @return A {@link Certificate} containing information about the specified certificate in the Azure Batch account.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public Certificate getCertificate(String thumbprintAlgorithm, String thumbprint) throws BatchErrorException, IOException {
        return getCertificate(thumbprintAlgorithm, thumbprint, null, null);
    }

    /**
     * Gets the specified {@link Certificate}.
     *
     * @param thumbprintAlgorithm The algorithm used to derive the thumbprint parameter. This must be sha1.
     * @param thumbprint The thumbprint of the certificate to get.
     * @param detailLevel A {@link DetailLevel} used for controlling which properties are retrieved from the service.
     * @return A {@link Certificate} containing information about the specified certificate in the Azure Batch account.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public Certificate getCertificate(String thumbprintAlgorithm, String thumbprint, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return getCertificate(thumbprintAlgorithm, thumbprint, detailLevel, null);
    }

    /**
     * Gets the specified {@link Certificate}.
     *
     * @param thumbprintAlgorithm the algorithm used to derive the thumbprint parameter. This must be sha1.
     * @param thumbprint the thumbprint of the certificate to get.
     * @param detailLevel A {@link DetailLevel} used for controlling which properties are retrieved from the service.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return A {@link Certificate} containing information about the specified certificate in the Azure Batch account.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public Certificate getCertificate(String thumbprintAlgorithm, String thumbprint, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        CertificateGetOptions getCertificateOptions = new CertificateGetOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(getCertificateOptions);

        return this.parentBatchClient.protocolLayer().certificates().get(thumbprintAlgorithm, thumbprint, getCertificateOptions);
    }

    /**
     * Lists the {@link Certificate certificates} in the Batch account.
     *
     * @return A list of {@link Certificate} objects.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public PagedList<Certificate> listCertificates() throws BatchErrorException, IOException {
        return listCertificates(null, null);
    }

    /**
     * Lists the {@link Certificate certificates} in the Batch account.
     *
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @return A list of {@link Certificate} objects.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public PagedList<Certificate> listCertificates(DetailLevel detailLevel) throws BatchErrorException, IOException {
        return listCertificates(detailLevel, null);
    }

    /**
     * Lists the {@link Certificate certificates} in the Batch account.
     *
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return A list of {@link Certificate} objects.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public PagedList<Certificate> listCertificates(DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {

        CertificateListOptions certificateListOptions = new CertificateListOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(certificateListOptions);

        return this.parentBatchClient.protocolLayer().certificates().list(certificateListOptions);
    }
}
