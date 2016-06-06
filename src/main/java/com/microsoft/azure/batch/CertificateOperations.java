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

public class CertificateOperations implements IInheritedBehaviors {

    private Collection<BatchClientBehavior> _customBehaviors;

    private BatchClient _parentBatchClient;

    public static final String SHA1_CERTIFICATE_ALGORITHM = "sha1";

    CertificateOperations(BatchClient batchClient, Iterable<BatchClientBehavior> inheritedBehaviors) {
        _parentBatchClient = batchClient;

        // inherit from instantiating parent
        InternalHelper.InheritClientBehaviorsAndSetPublicProperty(this, inheritedBehaviors);
    }

    @Override
    public Collection<BatchClientBehavior> getCustomBehaviors() {
        return _customBehaviors;
    }

    @Override
    public void setCustomBehaviors(Collection<BatchClientBehavior> behaviors) {
        this._customBehaviors = behaviors;
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

    public void createCertificate(InputStream certStream) throws BatchErrorException, IOException, CertificateException, NoSuchAlgorithmException {
        createCertificate(certStream, null);
    }

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

    public void createCertificate(InputStream certStream, String password, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException, CertificateException, NoSuchAlgorithmException {
        // Need load cert to Keystore first

        CertificateAddParameter addParam = new CertificateAddParameter();
        addParam.withCertificateFormat(CertificateFormat.PFX);
        addParam.withThumbprintAlgorithm(SHA1_CERTIFICATE_ALGORITHM);
        //addParam.setThumbprint(getThumbPrint(cert));
        //addParam.setData(Base64.getEncoder().encodeToString(cert.getEncoded());
        addParam.withPassword(password);

        createCertificate(addParam, additionalBehaviors);
    }

    public void createCertificate(CertificateAddParameter certificate) throws BatchErrorException, IOException {
        createCertificate(certificate, null);
    }

    public void createCertificate(CertificateAddParameter certificate, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        CertificateAddOptions options = new CertificateAddOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().certificates().add(certificate, options);
    }

    public void cancelDeleteCertificate(String thumbprintAlgorithm, String thumbprint) throws BatchErrorException, IOException {
        cancelDeleteCertificate(thumbprintAlgorithm, thumbprint, null);
    }

    public void cancelDeleteCertificate(String thumbprintAlgorithm, String thumbprint, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        CertificateCancelDeletionOptions options = new CertificateCancelDeletionOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().certificates().cancelDeletion(thumbprintAlgorithm, thumbprint, options);
    }

    public void deleteCertificate(String thumbprintAlgorithm, String thumbprint) throws BatchErrorException, IOException {
        deleteCertificate(thumbprintAlgorithm, thumbprint, null);
    }

    public void deleteCertificate(String thumbprintAlgorithm, String thumbprint, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        CertificateDeleteOptions options = new CertificateDeleteOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().certificates().delete(thumbprintAlgorithm, thumbprint, options);
    }

    public Certificate getCertificate(String thumbprintAlgorithm, String thumbprint) throws BatchErrorException, IOException {
        return getCertificate(thumbprintAlgorithm, thumbprint, null, null);
    }

    public Certificate getCertificate(String thumbprintAlgorithm, String thumbprint, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return getCertificate(thumbprintAlgorithm, thumbprint, detailLevel, null);
    }

    public Certificate getCertificate(String thumbprintAlgorithm, String thumbprint, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        CertificateGetOptions getCertificateOptions = new CertificateGetOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(getCertificateOptions);

        ServiceResponseWithHeaders<Certificate, CertificateGetHeaders> response = this._parentBatchClient.getProtocolLayer().certificates().get(thumbprintAlgorithm, thumbprint, getCertificateOptions);
        return response.getBody();
    }

    public List<Certificate> listCertificates() throws BatchErrorException, IOException {
        return listCertificates(null, null);
    }

    public List<Certificate> listCertificates(DetailLevel detailLevel) throws BatchErrorException, IOException {
        return listCertificates(detailLevel, null);
    }

    public List<Certificate> listCertificates(DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {

        CertificateListOptions certificateListOptions = new CertificateListOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(certificateListOptions);

        ServiceResponseWithHeaders<PagedList<Certificate>, CertificateListHeaders> response = this._parentBatchClient.getProtocolLayer().certificates().list(certificateListOptions);

        return response.getBody();
    }
}
