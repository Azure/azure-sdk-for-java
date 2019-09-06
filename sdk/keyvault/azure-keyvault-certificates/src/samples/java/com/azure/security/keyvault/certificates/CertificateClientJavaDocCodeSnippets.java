// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;


import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.util.Context;
import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.certificates.models.CertificateBase;
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.certificates.models.Issuer;

import java.util.List;


/**
 * This class contains code samples for generating javadocs through doclets for {@link CertificateClient}
 */
public final class CertificateClientJavaDocCodeSnippets {

    /**
     * Implementation for.CertificateClient
     * @return sync CertificateClient
     */
    private CertificateClient getCertificateClient() {
        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.instantiation
        CertificateClient certificateClient = new CertificateClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("https://myvault.vault.azure.net/")
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
            .buildClient();
        // END: com.azure.security.keyvault.certificates.certificateClient.instantiation
        return certificateClient;
    }
    

    /**
     * Method to insert code snippets for {@link CertificateClient#getCertificatePolicy(String)}
     */
    public void getCertiificatePolicyCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.getCertificatePolicy#string

        // END: com.azure.security.keyvault.certificates.certificateClient.getCertificatePolicy#string

        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.getCertificatePolicyWithResponse#string

        // END: com.azure.security.keyvault.certificates.certificateClient.getCertificatePolicyWithResponse#string
    }



    /**
     * Method to insert code snippets for {@link CertificateClient#getCertificateWithPolicy(String)}
     */
    public void getCertificateWithResponseCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.getCertificateWithPolicy

        // END: com.azure.security.keyvault.certificates.certificateClient.getCertificateWithPolicy

        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.getCertificateWithResponse

        // END: com.azure.security.keyvault.certificates.async.certificateclient.getCertificateWithResponse

        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.getCertificate

        // END: com.azure.security.keyvault.certificates.certificateClient.getCertificate

        //BEGIN: com.azure.security.keyvault.certificates.certificateClient.getCertificate#CertificateBase

        // END: com.azure.security.keyvault.certificates.certificateClient.getCertificate#CertificateBase
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#createCertificate(String, CertificatePolicy)}
     */
    public void createCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.createCertificate#tags-timeout

        // END: com.azure.security.keyvault.certificates.certificateClient.createCertificate#tags-timeout

        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.createCertificate#tags

        // END: com.azure.security.keyvault.certificates.certificateClient.createCertificate#tags        
        
        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.createCertificate#timeout

        // END: com.azure.security.keyvault.certificates.certificateClient.createCertificate#timeout      
        
        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.createCertificate

        // END: com.azure.security.keyvault.certificates.certificateClient.createCertificate
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#createCertificateIssuer(String, String)}
     */
    public void createCertificateIssuerCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.createCertificateIssuer

        // END: com.azure.security.keyvault.certificates.certificateClient.createCertificateIssuer

        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.createCertificateIssuer#issuer

        // END: com.azure.security.keyvault.certificates.certificateClient.createCertificateIssuer#issuer

        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.createCertificateIssuerWithResponse#issuer

        // END: com.azure.security.keyvault.certificates.certificateClient.createCertificateIssuerWithResponse#issuer
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#getCertificateIssuer(String)}
     */
    public void getCertificateIssuerCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.getCertificateIssuer#string

        // END: com.azure.security.keyvault.certificates.certificateClient.getCertificateIssuer#string

        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.getCertificateIssuerWithResponse#string-context

        // END: com.azure.security.keyvault.certificates.certificateClient.getCertificateIssuerWithResponse#string-context
        // END: com.azure.security.keyvault.certificates.certificateClient.getCertificateIssuerWithResponse#string-context

        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.getCertificateIssuer#issuerBase

        // END: com.azure.security.keyvault.certificates.certificateClient.getCertificateIssuer#issuerBase


        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.getCertificateIssuerWithResponse#issuerBase-context

        // END: com.azure.security.keyvault.certificates.certificateClient.getCertificateIssuerWithResponse#issuerBase-context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#updateCertificate(CertificateBase)}
     */
    public void updateCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.updateCertificate#CertificateBase

        // END: com.azure.security.keyvault.certificates.certificateClient.updateCertificate#CertificateBase
        
        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.updateCertificateWithResponse#CertificateBase

        // END: com.azure.security.keyvault.certificates.certificateClient.updateCertificateWithResponse#CertificateBase
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#updateCertificateIssuer(Issuer)}
     */
    public void updateCertificateIssuerCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.updateCertificateIssuer#issuer

        // END: com.azure.security.keyvault.certificates.certificateClient.updateCertificateIssuer#issuer

        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.updateCertificateIssuerWithResponse#issuer

        // END: com.azure.security.keyvault.certificates.certificateClient.updateCertificateIssuerWithResponse#issuer
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#updateCertificatePolicy(String, CertificatePolicy)}
     */
    public void updateCertificatePolicyCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.updateCertificatePolicy#string

        // END: com.azure.security.keyvault.certificates.certificateClient.updateCertificatePolicy#string

        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.updateCertificatePolicyWithResponse#string

        // END: com.azure.security.keyvault.certificates.certificateClient.updateCertificatePolicyWithResponse#string
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#updateCertificate(CertificateBase)} (CertificateBase)}
     */
    public void updateCertificateWithResponseCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.updateCertificateWithResponse#CertificateBase

        // END: com.azure.security.keyvault.certificates.certificateClient.updateCertificateWithResponse#CertificateBase
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#deleteCertificate(String)}
     */
    public void deleteCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.deleteCertificate#string

        // END: com.azure.security.keyvault.certificates.certificateClient.deleteCertificate#string

        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.deleteCertificateWithResponse#string

        // END: com.azure.security.keyvault.certificates.certificateClient.deleteCertificateWithResponse#string
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#deleteCertificateIssuer(String)}
     */
    public void deleteCertificateIssuerCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.deleteCertificateIssuerWithResponse#string-context

        // END: com.azure.security.keyvault.certificates.certificateClient.deleteCertificateIssuerWithResponse#string-context

        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.deleteCertificateIssuer#string

        // END: com.azure.security.keyvault.certificates.certificateClient.deleteCertificateIssuer#string
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#getDeletedCertificate(String)}
     */
    public void getDeletedCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.getDeletedCertificate#string

        // END: com.azure.security.keyvault.certificates.async.certificateclient.getDeletedCertificate#string

        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.getDeletedCertificateWithResponse#string}

        // END: com.azure.security.keyvault.certificates.certificateClient.getDeletedCertificateWithResponse#string}
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#purgeDeletedCertificate(String, Context)}
     */
    public void purgeDeletedCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.purgeDeletedCertificate#string

        // END: com.azure.security.keyvault.certificates.async.certificateclient.purgeDeletedCertificate#string     
        
        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.purgeDeletedCertificate#string-Context

        // END: com.azure.security.keyvault.certificates.async.certificateclient.purgeDeletedCertificate#string-Context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#recoverDeletedCertificate(String)} (String)}
     */
    public void recoverDeletedCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.certificatevault.certificates.certificateClient.recoverDeletedCertificate#string

        // END: com.azure.security.certificatevault.certificates.certificateClient.recoverDeletedCertificate#string

        // BEGIN: com.azure.security.certificatevault.certificates.certificateClient.recoverDeletedCertificateWithResponse#string

        // END: com.azure.security.certificatevault.certificates.certificateClient.recoverDeletedCertificateWithResponse#string
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#backupCertificate(String)}
     */
    public void backupCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.backupCertificate#string

        // END: com.azure.security.keyvault.certificates.certificateClient.backupCertificate#string

        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.backupCertificateWithResponse#string

        // END: com.azure.security.keyvault.certificates.certificateClient.backupCertificateWithResponse#string
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#restoreCertificate(byte[])}
     */
    public void restoreCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.restoreCertificate#byte

        // END: com.azure.security.keyvault.certificates.certificateClient.restoreCertificate#byte

        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.restoreCertificateWithResponse#byte

        // END: com.azure.security.keyvault.certificates.certificateClient.restoreCertificateWithResponse#byte
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#listCertificates()}
     */
    public void listCertificatesCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.listCertificates

        // END: com.azure.security.keyvault.certificates.certificateClient.listCertificates

        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.listCertificates#context

        // END: com.azure.security.keyvault.certificates.certificateClient.listCertificates#context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#listCertificateIssuers()}
     */
    public void listCertificateIssuersCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.listCertificateIssuers

        // END: com.azure.security.keyvault.certificates.certificateClient.listCertificateIssuers

        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.listCertificateIssuers#context

        // END: com.azure.security.keyvault.certificates.certificateClient.listCertificateIssuers#context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#listDeletedCertificates()}
     */
    public void listDeletedCertificatesCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.listDeletedCertificates

        // END: com.azure.security.keyvault.certificates.certificateClient.listDeletedCertificates

        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.listDeletedCertificates#context

        // END: com.azure.security.keyvault.certificates.certificateClient.listDeletedCertificates#context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#listCertificateVersions(String)}
     */
    public void listCertificateVersionsCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.listCertificateVersions

        // END: com.azure.security.keyvault.certificates.certificateClient.listCertificateVersions

        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.listCertificateVersions#context

        // END: com.azure.security.keyvault.certificates.certificateClient.listCertificateVersions#context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#setCertificateContacts(List)}
     */
    public void contactsOperationsCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.setCertificateContacts#contacts

        // END: com.azure.security.keyvault.certificates.certificateClient.setCertificateContacts#contacts

        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.setCertificateContacts#contacts-context

        // END: com.azure.security.keyvault.certificates.certificateClient.setCertificateContacts#contacts-context

        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.listCertificateContacts

        // END: com.azure.security.keyvault.certificates.certificateClient.listCertificateContacts

        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.listCertificateContacts#context

        // END: com.azure.security.keyvault.certificates.certificateClient.listCertificateContacts#context

        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.deleteCertificateContacts

        // END: com.azure.security.keyvault.certificates.certificateClient.deleteCertificateContacts

        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.deleteCertificateContacts#context

        // END: com.azure.security.keyvault.certificates.certificateClient.deleteCertificateContacts#context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#cancelCertificateOperation(String)} and
     * {@link CertificateClient#cancelCertificateOperationWithResponse(String, Context)} (String)}
     */
    public void certificateOperationCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.cancelCertificateOperation#string

        // END: com.azure.security.keyvault.certificates.certificateClient.cancelCertificateOperation#string

        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.cancelCertificateOperationWithResponse#string

        // END: com.azure.security.keyvault.certificates.certificateClient.cancelCertificateOperationWithResponse#string

        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.deleteCertificateOperationWithResponse#string

        // END: com.azure.security.keyvault.certificates.certificateClient.deleteCertificateOperationWithResponse#string

        // BEGIN: com.azure.security.keyvault.certificates.certificateClient.deleteCertificateOperation#string

        // END: com.azure.security.keyvault.certificates.certificateClient.deleteCertificateOperation#string
    }


}
