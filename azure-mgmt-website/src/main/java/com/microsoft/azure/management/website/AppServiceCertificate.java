/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website;

import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.website.implementation.CertificateInner;
import org.joda.time.DateTime;

import java.io.File;
import java.util.List;

/**
 * An immutable client-side representation of an Azure app service certificate.
 */
public interface AppServiceCertificate extends
        GroupableResource,
        Refreshable<AppServiceCertificate>,
        Wrapper<CertificateInner> {
    /**
     * @return the friendly name of the certificate
     */
    String friendlyName();

    /**
     * @return the subject name of the certificate
     */
    String subjectName();

    /**
     * @return the host names the certificate applies to
     */
    List<String> hostNames();

    /**
     * @return the pfx blob
     */
    String pfxBlob();

    /**
     * @return the app name
     */
    String siteName();

    /**
     * @return the self link
     */
    String selfLink();

    /**
     * @return the certificate issuer
     */
    String issuer();

    /**
     * @return the certificate issue Date
     */
    DateTime issueDate();

    /**
     * @return the certificate expriration date
     */
    DateTime expirationDate();

    /**
     * @return the certificate password
     */
    String password();

    /**
     * @return the certificate thumbprint
     */
    String thumbprint();

    /**
     * @return if the certificate valid
     */
    Boolean valid();

    /**
     * @return the raw bytes of .cer file
     */
    String cerBlob();

    /**
     * @return the public key hash
     */
    String publicKeyHash();

    /**
     * @return the specification for the App Service Environment to use for the certificate
     */
    HostingEnvironmentProfile hostingEnvironmentProfile();


    /**************************************************************
     * Fluent interfaces to provision a App service certificate
     **************************************************************/

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithCertificate,
            DefinitionStages.WithPfxFilePassword,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of all the site definition stages.
     */
    interface DefinitionStages {
        /**
         * An app service certificate definition allowing region to be set.
         */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * An app service certificate definition allowing resource group to be set.
         */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithCertificate> {
        }

        /**
         * An app service certificate definition allowing PFX certificate file to be set.
         */
        interface WithCertificate {
            /**
             * Specifies the PFX certificate file to upload.
             * @param file the PFX certificate file
             * @return the next stage of app service certificate definition
             */
            WithPfxFilePassword withPfxFile(File file);

            WithCreate withKeyVaultSecretCertificateStore(String vaultId, String secretName);
        }
        /**
         * An app service certificate definition allowing PFX certificate password to be set.
         */
        interface WithPfxFilePassword {
            /**
             * Specifies the password to the PFX certificate
             * @param password the PFX certificate password
             * @return the next stage of app service certificate definition
             */
            WithCreate withPfxFilePassword(String password);
        }

        /**
         * An app service certificate definition with sufficient inputs to create a new
         * app service certificate in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreate extends
                Creatable<AppServiceCertificate> {
        }
    }
}