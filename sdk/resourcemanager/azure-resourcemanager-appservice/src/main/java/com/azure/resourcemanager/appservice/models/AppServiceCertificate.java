// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.appservice.fluent.models.CertificateInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import java.io.File;
import java.time.OffsetDateTime;
import java.util.List;

/** An immutable client-side representation of an Azure app service certificate. */
@Fluent
public interface AppServiceCertificate
    extends GroupableResource<AppServiceManager, CertificateInner>, Refreshable<AppServiceCertificate> {
    /**
     * Gets the friendly name of the certificate.
     *
     * @return the friendly name of the certificate
     */
    String friendlyName();

    /**
     * Gets the subject name of the certificate.
     *
     * @return the subject name of the certificate
     */
    String subjectName();

    /**
     * Gets the host names the certificate applies to.
     *
     * @return the host names the certificate applies to
     */
    List<String> hostNames();

    /**
     * Gets the pfx blob.
     *
     * @return the pfx blob
     */
    byte[] pfxBlob();

    /**
     * Gets the app name.
     *
     * @return the app name
     */
    String siteName();

    /**
     * Gets the self link.
     *
     * @return the self link
     */
    String selfLink();

    /**
     * Gets the certificate issuer.
     *
     * @return the certificate issuer
     */
    String issuer();

    /**
     * Gets the certificate issue Date.
     *
     * @return the certificate issue Date
     */
    OffsetDateTime issueDate();

    /**
     * Gets the certificate expriration date .
     *
     * @return the certificate expriration date
     */
    OffsetDateTime expirationDate();

    /**
     * Gets the certificate password.
     *
     * @return the certificate password
     */
    String password();

    /**
     * Gets the certificate thumbprint.
     *
     * @return the certificate thumbprint
     */
    String thumbprint();

    /**
     * Check whether the certificate valid.
     *
     * @return if the certificate valid
     */
    Boolean valid();

    /**
     * Gets the raw bytes of .cer file.
     *
     * @return the raw bytes of .cer file
     */
    byte[] certificateBlob();

    /**
     * Gets the public key hash.
     *
     * @return the public key hash
     */
    String publicKeyHash();

    /**
     * Gets the specification for the App Service Environment to use for the certificate.
     *
     * @return the specification for the App Service Environment to use for the certificate
     */
    HostingEnvironmentProfile hostingEnvironmentProfile();

    /** Container interface for all the definitions that need to be implemented. */
    interface Definition extends DefinitionStages.Blank, DefinitionStages.WithGroup, DefinitionStages.WithCertificate,
        DefinitionStages.WithPfxFilePassword, DefinitionStages.WithCreate {
    }

    /** Grouping of all the site definition stages. */
    interface DefinitionStages {
        /** An app service certificate definition allowing region to be set. */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /** An app service certificate definition allowing resource group to be set. */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithCertificate> {
        }

        /** An app service certificate definition allowing PFX certificate file to be set. */
        interface WithCertificate {
            /**
             * Specifies the PFX certificate file to upload.
             *
             * @param file the PFX certificate file
             * @return the next stage of the definition
             */
            WithPfxFilePassword withPfxFile(File file);

            /**
             * Specifies the PFX byte array to upload.
             *
             * @param pfxByteArray the PFX byte array
             * @return the next stage of the definition
             */
            WithPfxFilePassword withPfxByteArray(byte[] pfxByteArray);

            /**
             * Specifies the PFX file from a URL.
             *
             * @param url the URL pointing to the PFX file.
             * @return the next stage of the definition
             */
            WithPfxFilePassword withPfxFileFromUrl(String url);

            /**
             * Specifies the app service certificate.
             *
             * @param certificateOrder the app service certificate order
             * @return the next stage of the definition
             */
            WithCreate withExistingCertificateOrder(AppServiceCertificateOrder certificateOrder);
        }

        /** An app service certificate definition allowing PFX certificate password to be set. */
        interface WithPfxFilePassword {
            /**
             * Specifies the password to the PFX certificate.
             *
             * @param password the PFX certificate password
             * @return the next stage of the definition
             */
            WithCreate withPfxPassword(String password);
        }

        /**
         * An app service certificate definition with sufficient inputs to create a new app service certificate in the
         * cloud, but exposing additional optional inputs to specify.
         */
        interface WithCreate extends Creatable<AppServiceCertificate> {
        }
    }
}
