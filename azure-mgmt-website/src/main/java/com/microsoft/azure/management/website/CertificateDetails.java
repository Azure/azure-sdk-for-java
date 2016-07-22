/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website;

import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.website.implementation.CertificateDetailsInner;
import org.joda.time.DateTime;

/**
 * An immutable client-side representation of an Azure Web App.
 */
public interface CertificateDetails extends
        GroupableResource,
        Refreshable<CertificateDetails>,
        Updatable<CertificateDetails.Update>,
        Wrapper<CertificateDetailsInner> {
    /**
     * @return Version.
     */
    Integer version();

    /**
     * @return Serial Number.
     */
    String serialNumber();

    /**
     * @return Thumbprint.
     */
    String thumbprint();

    /**
     * @return Subject.
     */
    String subject();

    /**
     * @return Valid from.
     */
    DateTime notBefore();

    /**
     * @return Valid to.
     */
    DateTime notAfter();

    /**
     * @return Signature Algorithm.
     */
    String signatureAlgorithm();

    /**
     * @return Issuer.
     */
    String issuer();

    /**
     * @return Raw certificate data.
     */
    String rawData();

    /**************************************************************
     * Fluent interfaces to provision a App service plan
     **************************************************************/

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definition extends
        DefinitionStages.Blank,
            DefinitionStages.WithHostName,
        DefinitionStages.WithCreate {
    }

    /**
     * Grouping of all the site definition stages.
     */
    interface DefinitionStages {
        /**
         * An app service plan definition allowing resource group to be set.
         */
        interface Blank extends GroupableResource.DefinitionStages.WithGroup<WithHostName> {
        }

        /**
         * An app service plan definition allowing pricing tier to be set.
         */
        interface WithHostName {
            WithCreate withHostName(String hostName);
        }

        /**
         * An app service plan definition with sufficient inputs to create a new
         * website in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreate extends Creatable<CertificateDetails> {
        }
    }

    /**
     * Grouping of all the site update stages.
     */
    interface UpdateStages {

    }

    /**
     * The template for a site update operation, containing all the settings that can be modified.
     */
    interface Update {
    }
}

