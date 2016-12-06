/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.batch.protocol.models.BatchErrorException;
import com.microsoft.azure.batch.protocol.models.Certificate;
import com.microsoft.azure.batch.protocol.models.CertificateAddOptions;
import com.microsoft.azure.batch.protocol.models.CertificateAddParameter;
import com.microsoft.azure.batch.protocol.models.CertificateCancelDeletionOptions;
import com.microsoft.azure.batch.protocol.models.CertificateDeleteOptions;
import com.microsoft.azure.batch.protocol.models.CertificateFormat;
import com.microsoft.azure.batch.protocol.models.CertificateGetHeaders;
import com.microsoft.azure.batch.protocol.models.CertificateGetOptions;
import com.microsoft.azure.batch.protocol.models.CertificateListHeaders;
import com.microsoft.azure.batch.protocol.models.CertificateListOptions;
import com.microsoft.rest.ServiceResponseWithHeaders;
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
import java.util.List;

/**
 * Performs certificate related operations on an Azure Batch account.
 */
public class CertificateOperations implements IInheritedBehaviors {

    private Collection<BatchClientBehavior> _customBehaviors;

    private BatchClient _parentBatchClient;

    /**
     * The SHA certificate algorithm
     */
    public static final String SHA1_CERTIFICATE_ALGORITHM = "sha1";

    CertificateOperations(BatchClient batchClient, Iterable<BatchClientBehavior> inheritedBehaviors) {
        _parentBatchClient = batchClient;

        // inherit from instantiating parent
        InternalHelper.InheritClientBehaviorsAndSetPublicProperty(this, inheritedBehaviors);
    }

    /**
     * Gets a list of behaviors that modify or customize requests to the Batch service.
     *
     * @return A list of BatchClientBehavior
     */
    @Override
    public Collection<BatchClientBehavior> customBehaviors() {
        return _customBehaviors;
    }

    /**
     * Sets a list of behaviors that modify or customize requests to the Batch service.
     *
     * @param behaviors The collection of BatchClientBehavior classes
     * @return The current instance
     */
    @Override
    public IInheritedBehaviors withCustomBehaviors(Collection<BatchClientBehavior> behaviors) {
        _customBehaviors = behaviors;
        return this;
    }

    private static String getThumbPrint(java.security.cert.Certificate cert) throws NoSuchAlgorithmException, CertificateEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] der = cert.getEncoded();
        md.update(der);
        byte[] digest = md.digest();
        return hexify(digest);
    }

    private static String hexify (byte bytes[]) {
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        StringBuffer buf = new StringBuffer(bytes.length * 2);

        for (int i = 0; i < bytes.length; ++i) {
            buf.append(hexDigits[(bytes[i] & 0xf0) >> 4]);
            buf.append(hexDigits[bytes[i] & 0x0f]);
        }

        return buf.toString();
    }

    /**
     * Creates a new {@link Certificate} from .cer format data in stream.
     *
     * @param certStream The certificate data in .cer format.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     * @throws CertificateException Exception thrown on parsing errors
     * @throws NoSuchAlgorithmException Exception thrown if the X509 provider is not registered in the security provider list.
     */
    public void createCertificate(InputStream certStream) throws BatchErrorException, IOException, CertificateException, NoSuchAlgorithmException {
        createCertificate(certStream, null);
    }

    /**
     * Creates a new {@link Certificate} from .cer format data in stream.
     *
     * @param certStream The certificate data in .cer format.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     * @throws CertificateException Exception thrown on parsing errors
     * @throws NoSuchAlgorithmException Exception thrown if the X509 provider is not registered in the security provider list.
     */
    public void createCertificate(InputStream certStream, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException, CertificateException, NoSuchAlgorithmException {
        CertificateFactory x509CertFact = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate)x509CertFact.generateCertificate(certStream);

        CertificateAddParameter addParam = new CertificateAddParameter();
        addParam.withCertificateFormat(CertificateFormat.CER);
        addParam.withThumbprintAlgorithm(SHA1_CERTIFICATE_ALGORITHM);
        addParam.withThumbprint(getThumbPrint(cert));
        addParam.withData(Base64.encodeBase64String(cert.getEncoded()));

        createCertificate(addParam, additionalBehaviors);
    }

    /**
     * Creates a new {@link Certificate} by {@link CertificateAddParameter}
     *
     * @param certificate The parameter to create certificate
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void createCertificate(CertificateAddParameter certificate) throws BatchErrorException, IOException {
        createCertificate(certificate, null);
    }

    /**
     * Creates a new {@link Certificate} by {@link CertificateAddParameter}
     *
     * @param certificate The parameter to create certificate
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void createCertificate(CertificateAddParameter certificate, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        CertificateAddOptions options = new CertificateAddOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().certificates().add(certificate, options);
    }

    /**
     * Cancels a failed deletion of the specified certificate.  This can be done only when
     * the certificate is in the DeleteFailed state, and restores the certificate to the Active state.
     *
     * @param thumbprintAlgorithm The algorithm used to derive the thumbprint parameter. This must be sha1.
     * @param thumbprint The thumbprint of the certificate that failed to delete.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void cancelDeleteCertificate(String thumbprintAlgorithm, String thumbprint) throws BatchErrorException, IOException {
        cancelDeleteCertificate(thumbprintAlgorithm, thumbprint, null);
    }

    /**
     * Cancels a failed deletion of the specified certificate.  This can be done only when
     * the certificate is in the DeleteFailed state, and restores the certificate to the Active state.
     *
     * @param thumbprintAlgorithm The algorithm used to derive the thumbprint parameter. This must be sha1.
     * @param thumbprint The thumbprint of the certificate that failed to delete.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void cancelDeleteCertificate(String thumbprintAlgorithm, String thumbprint, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        CertificateCancelDeletionOptions options = new CertificateCancelDeletionOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().certificates().cancelDeletion(thumbprintAlgorithm, thumbprint, options);
    }

    /**
     * Deletes the certificate from the Batch account.
     *
     * @param thumbprintAlgorithm The algorithm used to derive the thumbprint parameter. This must be sha1.
     * @param thumbprint The thumbprint of the certificate to delete.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void deleteCertificate(String thumbprintAlgorithm, String thumbprint) throws BatchErrorException, IOException {
        deleteCertificate(thumbprintAlgorithm, thumbprint, null);
    }

    /**
     * Deletes the certificate from the Batch account.
     *
     * @param thumbprintAlgorithm The algorithm used to derive the thumbprint parameter. This must be sha1.
     * @param thumbprint The thumbprint of the certificate to delete.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void deleteCertificate(String thumbprintAlgorithm, String thumbprint, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        CertificateDeleteOptions options = new CertificateDeleteOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().certificates().delete(thumbprintAlgorithm, thumbprint, options);
    }

    /**
     * Gets the specified {@link Certificate}.
     *
     * @param thumbprintAlgorithm The algorithm used to derive the thumbprint parameter. This must be sha1.
     * @param thumbprint The thumbprint of the certificate to get.
     * @return A {@link Certificate} containing information about the specified certificate in the Azure Batch account.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public Certificate getCertificate(String thumbprintAlgorithm, String thumbprint) throws BatchErrorException, IOException {
        return getCertificate(thumbprintAlgorithm, thumbprint, null, null);
    }

    /**
     * Gets the specified {@link Certificate}.
     *
     * @param thumbprintAlgorithm The algorithm used to derive the thumbprint parameter. This must be sha1.
     * @param thumbprint The thumbprint of the certificate to get.
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @return A {@link Certificate} containing information about the specified certificate in the Azure Batch account.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public Certificate getCertificate(String thumbprintAlgorithm, String thumbprint, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return getCertificate(thumbprintAlgorithm, thumbprint, detailLevel, null);
    }

    /**
     * Gets the specified {@link Certificate}.
     *
     * @param thumbprintAlgorithm the algorithm used to derive the thumbprint parameter. This must be sha1.
     * @param thumbprint the thumbprint of the certificate to get.
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return A {@link Certificate} containing information about the specified certificate in the Azure Batch account.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public Certificate getCertificate(String thumbprintAlgorithm, String thumbprint, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        CertificateGetOptions getCertificateOptions = new CertificateGetOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(getCertificateOptions);

        ServiceResponseWithHeaders<Certificate, CertificateGetHeaders> response = this._parentBatchClient.protocolLayer().certificates().get(thumbprintAlgorithm, thumbprint, getCertificateOptions);
        return response.getBody();
    }

    /**
     * Enumerates the {@link Certificate certificates} in the Batch account.
     *
     * @return A collection of {@link Certificate certificates}
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public List<Certificate> listCertificates() throws BatchErrorException, IOException {
        return listCertificates(null, null);
    }

    /**
     * Enumerates the {@link Certificate certificates} in the Batch account.
     *
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @return A collection of {@link Certificate certificates}
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public List<Certificate> listCertificates(DetailLevel detailLevel) throws BatchErrorException, IOException {
        return listCertificates(detailLevel, null);
    }

    /**
     * Enumerates the {@link Certificate certificates} in the Batch account.
     *
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return A collection of {@link Certificate certificates}
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public List<Certificate> listCertificates(DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {

        CertificateListOptions certificateListOptions = new CertificateListOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(certificateListOptions);

        ServiceResponseWithHeaders<PagedList<Certificate>, CertificateListHeaders> response = this._parentBatchClient.protocolLayer().certificates().list(certificateListOptions);

        return response.getBody();
    }
}
