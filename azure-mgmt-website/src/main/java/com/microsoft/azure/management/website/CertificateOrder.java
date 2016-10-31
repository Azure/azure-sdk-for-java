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
import com.microsoft.azure.management.website.implementation.AppServiceCertificateInner;
import com.microsoft.azure.management.website.implementation.AppServiceCertificateOrderInner;
import org.joda.time.DateTime;

import java.util.Map;

/**
 * An immutable client-side representation of an Azure Web App.
 */
public interface CertificateOrder extends
        GroupableResource,
        Refreshable<CertificateOrder>,
        Updatable<CertificateOrder.Update>,
        Wrapper<AppServiceCertificateOrderInner> {

    /**
     * @return State of the Key Vault secret.
     */
    Map<String, AppServiceCertificateInner> certificates();

    /**
     * @return Certificate distinguished name.
     */
    String distinguishedName();

    /**
     * @return Domain Verification Token.
     */
    String domainVerificationToken();

    /**
     * @return Duration in years (must be between 1 and 3).
     */
    int validityInYears();

    /**
     * @return Certificate Key Size.
     */
    int keySize();

    /**
     * @return Certificate product type. Possible values include:
     * 'StandardDomainValidatedSsl', 'StandardDomainValidatedWildCardSsl'.
     */
    CertificateProductType productType();

    /**
     * @return Auto renew.
     */
    boolean autoRenew();

    /**
     * @return Status of certificate order. Possible values include: 'Succeeded',
     * 'Failed', 'Canceled', 'InProgress', 'Deleting'.
     */
    ProvisioningState provisioningState();

    /**
     * @return Current order status. Possible values include: 'Pendingissuance',
     * 'Issued', 'Revoked', 'Canceled', 'Denied', 'Pendingrevocation',
     * 'PendingRekey', 'Unused', 'Expired', 'NotSubmitted'.
     */
    CertificateOrderStatus status();

    /**
     * @return Signed certificate.
     */
    CertificateDetails signedCertificate();

    /**
     * @return Last CSR that was created for this order.
     */
    String csr();

    /**
     * @return Intermediate certificate.
     */
    CertificateDetails intermediate();

    /**
     * @return Root certificate.
     */
    CertificateDetails root();

    /**
     * @return Current serial number of the certificate.
     */
    String serialNumber();

    /**
     * @return Certificate last issuance time.
     */
    DateTime lastCertificateIssuanceTime();

    /**
     * @return Certificate expiration time.
     */
    DateTime expirationTime();

    /**************************************************************
     * Fluent interfaces to provision a App service plan
     **************************************************************/

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithHostName,
            DefinitionStages.WithCertificateSku,
            DefinitionStages.WithValidYears,
            DefinitionStages.WithKeyVault,
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
            WithCertificateSku withHostName(String hostName);
        }

        interface WithCertificateSku {
            WithValidYears withSku(CertificateProductType sku);
        }

        interface WithValidYears {
            WithKeyVault withValidYears(int years);
        }

        interface WithKeyVault {
            WithCreate withExistingKeyVault(String keyVaultId);
        }

        /**
         * An app service plan definition with sufficient inputs to create a new
         * website in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreate extends Creatable<CertificateOrder> {
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