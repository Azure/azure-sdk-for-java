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
        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.instantiation
        CertificateClient certificateClient = new CertificateClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("https://myvault.vault.azure.net/")
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
            .buildClient();
        // END: com.azure.security.keyvault.certificates.certificateclient.instantiation
        return certificateClient;
    }
    

    /**
     * Method to insert code snippets for {@link CertificateClient#getCertificatePolicy(String)}
     */
    public void getCertiificatePolicyCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.getCertificatePolicy#string

        // END: com.azure.security.keyvault.certificates.certificateclient.getCertificatePolicy#string

        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.getCertificatePolicyWithResponse#string

        // END: com.azure.security.keyvault.certificates.certificateclient.getCertificatePolicyWithResponse#string
    }



    /**
     * Method to insert code snippets for {@link CertificateClient#getCertificateWithPolicy(String)}
     */
    public void getCertificateWithResponseCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.getCertificateWithPolicy

        // END: com.azure.security.keyvault.certificates.certificateclient.getCertificateWithPolicy

        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.getCertificateWithResponse

        // END: com.azure.security.keyvault.certificates.async.certificateclient.getCertificateWithResponse

        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.getCertificate

        // END: com.azure.security.keyvault.certificates.certificateclient.getCertificate

        //BEGIN: com.azure.security.keyvault.certificates.certificateclient.getCertificate#CertificateBase

        // END: com.azure.security.keyvault.certificates.certificateclient.getCertificate#CertificateBase
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#createCertificate(String, CertificatePolicy)}
     */
    public void createCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.createCertificate#tags-timeout

        // END: com.azure.security.keyvault.certificates.certificateclient.createCertificate#tags-timeout

        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.createCertificate#tags

        // END: com.azure.security.keyvault.certificates.certificateclient.createCertificate#tags        
        
        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.createCertificate#timeout

        // END: com.azure.security.keyvault.certificates.certificateclient.createCertificate#timeout      
        
        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.createCertificate

        // END: com.azure.security.keyvault.certificates.certificateclient.createCertificate
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#createCertificateIssuer(String, String)}
     */
    public void createCertificateIssuerCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.createCertificateIssuer

        // END: com.azure.security.keyvault.certificates.certificateclient.createCertificateIssuer

        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.createCertificateIssuer#issuer

        // END: com.azure.security.keyvault.certificates.certificateclient.createCertificateIssuer#issuer

        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.createCertificateIssuerWithResponse#issuer

        // END: com.azure.security.keyvault.certificates.certificateclient.createCertificateIssuerWithResponse#issuer
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#getCertificateIssuer(String)}
     */
    public void getCertificateIssuerCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.getCertificateIssuer#string

        // END: com.azure.security.keyvault.certificates.certificateclient.getCertificateIssuer#string

        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.getCertificateIssuerWithResponse#string-context

        // END: com.azure.security.keyvault.certificates.certificateclient.getCertificateIssuerWithResponse#string-context
        // END: com.azure.security.keyvault.certificates.certificateclient.getCertificateIssuerWithResponse#string-context

        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.getCertificateIssuer#issuerBase

        // END: com.azure.security.keyvault.certificates.certificateclient.getCertificateIssuer#issuerBase


        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.getCertificateIssuerWithResponse#issuerBase-context

        // END: com.azure.security.keyvault.certificates.certificateclient.getCertificateIssuerWithResponse#issuerBase-context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#updateCertificate(CertificateBase)}
     */
    public void updateCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.updateCertificate#CertificateBase

        // END: com.azure.security.keyvault.certificates.certificateclient.updateCertificate#CertificateBase
        
        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.updateCertificateWithResponse#CertificateBase

        // END: com.azure.security.keyvault.certificates.certificateclient.updateCertificateWithResponse#CertificateBase
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#updateCertificateIssuer(Issuer)}
     */
    public void updateCertificateIssuerCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.updateCertificateIssuer#issuer

        // END: com.azure.security.keyvault.certificates.certificateclient.updateCertificateIssuer#issuer

        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.updateCertificateIssuerWithResponse#issuer

        // END: com.azure.security.keyvault.certificates.certificateclient.updateCertificateIssuerWithResponse#issuer
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#updateCertificatePolicy(String, CertificatePolicy)}
     */
    public void updateCertificatePolicyCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.updateCertificatePolicy#string

        // END: com.azure.security.keyvault.certificates.certificateclient.updateCertificatePolicy#string

        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.updateCertificatePolicyWithResponse#string

        // END: com.azure.security.keyvault.certificates.certificateclient.updateCertificatePolicyWithResponse#string
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#updateCertificate(CertificateBase)} (CertificateBase)}
     */
    public void updateCertificateWithResponseCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.updateCertificateWithResponse#CertificateBase

        // END: com.azure.security.keyvault.certificates.certificateclient.updateCertificateWithResponse#CertificateBase
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#deleteCertificate(String)}
     */
    public void deleteCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.deleteCertificate#string

        // END: com.azure.security.keyvault.certificates.certificateclient.deleteCertificate#string

        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.deleteCertificateWithResponse#string

        // END: com.azure.security.keyvault.certificates.certificateclient.deleteCertificateWithResponse#string
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#deleteCertificateIssuer(String)}
     */
    public void deleteCertificateIssuerCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.deleteCertificateIssuerWithResponse#string-context

        // END: com.azure.security.keyvault.certificates.certificateclient.deleteCertificateIssuerWithResponse#string-context

        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.deleteCertificateIssuer#string

        // END: com.azure.security.keyvault.certificates.certificateclient.deleteCertificateIssuer#string
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#getDeletedCertificate(String)}
     */
    public void getDeletedCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.getDeletedCertificate#string

        // END: com.azure.security.keyvault.certificates.async.certificateclient.getDeletedCertificate#string

        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.getDeletedCertificateWithResponse#string}

        // END: com.azure.security.keyvault.certificates.certificateclient.getDeletedCertificateWithResponse#string}
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
        // BEGIN: com.azure.security.certificatevault.certificates.certificateclient.recoverDeletedCertificate#string

        // END: com.azure.security.certificatevault.certificates.certificateclient.recoverDeletedCertificate#string

        // BEGIN: com.azure.security.certificatevault.certificates.certificateclient.recoverDeletedCertificateWithResponse#string

        // END: com.azure.security.certificatevault.certificates.certificateclient.recoverDeletedCertificateWithResponse#string
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#backupCertificate(String)}
     */
    public void backupCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.backupCertificate#string

        // END: com.azure.security.keyvault.certificates.certificateclient.backupCertificate#string

        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.backupCertificateWithResponse#string

        // END: com.azure.security.keyvault.certificates.certificateclient.backupCertificateWithResponse#string
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#restoreCertificate(byte[])}
     */
    public void restoreCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.restoreCertificate#byte

        // END: com.azure.security.keyvault.certificates.certificateclient.restoreCertificate#byte

        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.restoreCertificateWithResponse#byte

        // END: com.azure.security.keyvault.certificates.certificateclient.restoreCertificateWithResponse#byte
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#listCertificates()}
     */
    public void listCertificatesCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.listCertificates

        // END: com.azure.security.keyvault.certificates.certificateclient.listCertificates

        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.listCertificates#context

        // END: com.azure.security.keyvault.certificates.certificateclient.listCertificates#context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#listCertificateIssuers()}
     */
    public void listCertificateIssuersCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.listCertificateIssuers

        // END: com.azure.security.keyvault.certificates.certificateclient.listCertificateIssuers

        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.listCertificateIssuers#context

        // END: com.azure.security.keyvault.certificates.certificateclient.listCertificateIssuers#context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#listDeletedCertificates()}
     */
    public void listDeletedCertificatesCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.listDeletedCertificates

        // END: com.azure.security.keyvault.certificates.certificateclient.listDeletedCertificates

        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.listDeletedCertificates#context

        // END: com.azure.security.keyvault.certificates.certificateclient.listDeletedCertificates#context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#listCertificateVersions(String)}
     */
    public void listCertificateVersionsCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.listCertificateVersions

        // END: com.azure.security.keyvault.certificates.certificateclient.listCertificateVersions

        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.listCertificateVersions#context

        // END: com.azure.security.keyvault.certificates.certificateclient.listCertificateVersions#context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#setCertificateContacts(List)}
     */
    public void contactsOperationsCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.setCertificateContacts#contacts

        // END: com.azure.security.keyvault.certificates.certificateclient.setCertificateContacts#contacts

        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.setCertificateContacts#contacts-context

        // END: com.azure.security.keyvault.certificates.certificateclient.setCertificateContacts#contacts-context

        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.listCertificateContacts

        // END: com.azure.security.keyvault.certificates.certificateclient.listCertificateContacts

        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.listCertificateContacts#context

        // END: com.azure.security.keyvault.certificates.certificateclient.listCertificateContacts#context

        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.deleteCertificateContacts

        // END: com.azure.security.keyvault.certificates.certificateclient.deleteCertificateContacts

        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.deleteCertificateContacts#context

        // END: com.azure.security.keyvault.certificates.certificateclient.deleteCertificateContacts#context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#cancelCertificateOperation(String)} and
     * {@link CertificateClient#cancelCertificateOperationWithResponse(String, Context)} (String)}
     */
    public void certificateOperationCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.cancelCertificateOperation#string

        // END: com.azure.security.keyvault.certificates.certificateclient.cancelCertificateOperation#string

        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.cancelCertificateOperationWithResponse#string

        // END: com.azure.security.keyvault.certificates.certificateclient.cancelCertificateOperationWithResponse#string

        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.deleteCertificateOperationWithResponse#string

        // END: com.azure.security.keyvault.certificates.certificateclient.deleteCertificateOperationWithResponse#string

        // BEGIN: com.azure.security.keyvault.certificates.certificateclient.deleteCertificateOperation#string

        // END: com.azure.security.keyvault.certificates.certificateclient.deleteCertificateOperation#string
    }


}
