/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website;

import com.microsoft.azure.management.keyvault.Vault;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.website.implementation.AppServiceCertificateOrderInner;
import org.joda.time.DateTime;
import rx.Observable;

/**
 * An immutable client-side representation of an Azure App Service Certificate Order.
 */
public interface AppServiceCertificateOrder extends
        GroupableResource,
        Refreshable<AppServiceCertificateOrder>,
        Updatable<AppServiceCertificateOrder.Update>,
        Wrapper<AppServiceCertificateOrderInner> {

    /**
     * @return certificate's distinguished name
     */
    String distinguishedName();

    /**
     * @return the domain verification token
     */
    String domainVerificationToken();

    /**
     * @return duration in years (must be between 1 and 3)
     */
    int validityInYears();

    /**
     * @return the certificate key size
     */
    int keySize();

    /**
     * @return the certificate product type
     */
    CertificateProductType productType();

    /**
     * @return if the certificate should be automatically renewed upon expiration
     */
    boolean autoRenew();

    /**
     * @return current order status
     */
    CertificateOrderStatus status();

    /**
     * @return the signed certificate
     */
    CertificateDetails signedCertificate();

    /**
     * @return last certificate signing request that was created for this order
     */
    String certificateSigningRequest();

    /**
     * @return the intermediate certificate
     */
    CertificateDetails intermediate();

    /**
     * @return the root certificate
     */
    CertificateDetails root();

    /**
     * @return current serial number of the certificate
     */
    String serialNumber();

    /**
     * @return last issuance time
     */
    DateTime lastCertificateIssuanceTime();

    /**
     * @return expiration time
     */
    DateTime expirationTime();

    /**
     * Bind a Key Vault secret to a certificate store that will be used for storing the certificate once it's ready.
     * @param certificateName the name of the Key Vault Secret
     * @param vault the key vault to store the certificate
     * @return a binding containing the key vault information
     */
    AppServiceCertificateKeyVaultBinding createKeyVaultBinding(String certificateName, Vault vault);

    /**
     * Bind a Key Vault secret to a certificate store that will be used for storing the certificate once it's ready.
     * @param certificateName the name of the Key Vault Secret
     * @param vault the key vault to store the certificate
     * @return a binding containing the key vault information
     */
    Observable<AppServiceCertificateKeyVaultBinding> createKeyVaultBindingAsync(String certificateName, Vault vault);

    /**
     * @return the state of the Key Vault secret
     */
    AppServiceCertificateKeyVaultBinding getKeyVaultBinding();

    /**
     * @return the state of the Key Vault secret
     */
    Observable<AppServiceCertificateKeyVaultBinding> getKeyVaultBindingAsync();

    /**************************************************************
     * Fluent interfaces to provision a App service certificate order
     **************************************************************/

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithHostName,
            DefinitionStages.WithCertificateSku,
            DefinitionStages.WithValidYears,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of all the app service certificate order definition stages.
     */
    interface DefinitionStages {
        /**
         * An app service certificate order definition allowing resource group to be set.
         */
        interface Blank extends GroupableResource.DefinitionStages.WithGroup<WithHostName> {
        }

        /**
         * An app service certificate order definition allowing hostname to be set.
         */
        interface WithHostName {
            /**
             * Specifies the hostname the certificate binds to.
             * @param hostName the bare host name, without "www". Use *. prefix if it's a wild card certificate
             * @return the next stage of the app service certificate definition
             */
            WithCertificateSku withHostName(String hostName);
        }

        /**
         * An app service certificate order definition allowing SKU to be set.
         */
        interface WithCertificateSku {
            /**
             * Specifies the SKU of the certificate. Standard type will only provide
             * SSL support to the hostname, and www.hostname. Wildcard type will provide
             * SSL support to any sub-domain under the hostname.
             * @param sku the SKU of the certificate
             * @return the next stage of the app service certificate definition
             */
            WithValidYears withSku(CertificateProductType sku);
        }

        /**
         * An app service certificate order definition allowing valid years to be set.
         */
        interface WithValidYears {
            /**
             * Specifies the valid years of the certificate.
             * @param years minimum 1 year, and maximum 3 years
             * @return the next stage of the app service certificate definition
             */
            WithCreate withValidYears(int years);
        }

        /**
         * An app service certificate order definition allowing auto-renew settings to be set.
         */
        interface WithAutoRenew {
            /**
             * Specifies if the certificate should be auto-renewed.
             * @param enabled true if the certificate order should be auto-renewed
             * @return the next stage of the app service certificate definition
             */
            WithCreate withAutoRenew(boolean enabled);
        }

        /**
         * An app service certificate order definition with sufficient inputs to create a new
         * app service certificate order in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreate extends
                Creatable<AppServiceCertificateOrder>,
                WithAutoRenew,
                GroupableResource.DefinitionWithTags<WithCreate> {
        }
    }

    /**
     * Grouping of all the app service certificate order update stages.
     */
    interface UpdateStages {
        /**
         * An app service certificate order definition allowing auto-renew settings to be set.
         */
        interface WithAutoRenew {
            /**
             * Specifies if the certificate should be auto-renewed.
             * @param enabled true if the certificate order should be auto-renewed
             * @return the next stage of the app service certificate definition
             */
            Update withAutoRenew(boolean enabled);
        }
    }

    /**
     * The template for an app service certificate order update operation, containing all the settings that can be modified.
     */
    interface Update extends
            Appliable<AppServiceCertificateOrder>,
            UpdateStages.WithAutoRenew,
            GroupableResource.UpdateWithTags<Update> {
    }
}