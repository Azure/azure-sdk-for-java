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
 * An immutable client-side representation of an Azure Web App.
 */
public interface Certificate extends
        GroupableResource,
        Refreshable<Certificate>,
        Wrapper<CertificateInner> {
    /**
     * @return Friendly name of the certificate.
     */
    String friendlyName();

    /**
     * @return Subject name of the certificate.
     */
    String subjectName();

    /**
     * @return Host names the certificate applies to.
     */
    List<String> hostNames();

    /**
     * @return Pfx blob.
     */
    String pfxBlob();

    /**
     * @return App name.
     */
    String siteName();

    /**
     * @return Self link.
     */
    String selfLink();

    /**
     * @return Certificate issuer.
     */
    String issuer();

    /**
     * @return Certificate issue Date.
     */
    DateTime issueDate();

    /**
     * @return Certificate expriration date.
     */
    DateTime expirationDate();

    /**
     * @return Certificate password.
     */
    String password();

    /**
     * @return Certificate thumbprint.
     */
    String thumbprint();

    /**
     * @return Is the certificate valid?.
     */
    Boolean valid();

    /**
     * @return Raw bytes of .cer file.
     */
    String cerBlob();

    /**
     * @return Public key hash.
     */
    String publicKeyHash();

    /**
     * @return Specification for the hosting environment (App Service Environment) to
     * use for the certificate.
     */
    HostingEnvironmentProfile hostingEnvironmentProfile();


    /**************************************************************
     * Fluent interfaces to provision a App service plan
     **************************************************************/

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithPfxFile,
            DefinitionStages.WithPfxFilePassword,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of all the site definition stages.
     */
    interface DefinitionStages {
        /**
         * An app service plan definition allowing resource group to be set.
         */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithPfxFile> {
        }

        interface WithPfxFile {
            WithPfxFilePassword withPfxFile(File file);
        }

        interface WithPfxFilePassword {
            WithCreate withPfxFilePassword(String password);
        }

        /**
         * An app service plan definition with sufficient inputs to create a new
         * website in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreate extends
                Creatable<Certificate> {
        }
    }
}