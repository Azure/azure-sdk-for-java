// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.appservice.fluent.inner.AppServiceCertificateOrderInner;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import java.time.OffsetDateTime;
import reactor.core.publisher.Mono;

/** An immutable client-side representation of an Azure App Service certificate order. */
@Fluent
public interface AppServiceCertificateOrder
    extends GroupableResource<AppServiceManager, AppServiceCertificateOrderInner>,
        Refreshable<AppServiceCertificateOrder>,
        Updatable<AppServiceCertificateOrder.Update> {

    /** @return certificate's distinguished name */
    String distinguishedName();

    /** @return the domain verification token */
    String domainVerificationToken();

    /** @return duration in years (must be between 1 and 3) */
    int validityInYears();

    /** @return the certificate key size */
    int keySize();

    /** @return the certificate product type */
    CertificateProductType productType();

    /** @return if the certificate should be automatically renewed upon expiration */
    boolean autoRenew();

    /** @return current order status */
    CertificateOrderStatus status();

    /** @return the signed certificate */
    CertificateDetails signedCertificate();

    /** @return last certificate signing request that was created for this order */
    String certificateSigningRequest();

    /** @return the intermediate certificate */
    CertificateDetails intermediate();

    /** @return the root certificate */
    CertificateDetails root();

    /** @return current serial number of the certificate */
    String serialNumber();

    /** @return last issuance time */
    OffsetDateTime lastCertificateIssuanceTime();

    /** @return expiration time */
    OffsetDateTime expirationTime();

    /**
     * Bind a Key Vault secret to a certificate store that will be used for storing the certificate once it's ready.
     *
     * @param certificateName the name of the Key Vault Secret
     * @param vault the key vault to store the certificate
     * @return a binding containing the key vault information
     */
    AppServiceCertificateKeyVaultBinding createKeyVaultBinding(String certificateName, Vault vault);

    /**
     * Bind a Key Vault secret to a certificate store that will be used for storing the certificate once it's ready.
     *
     * @param certificateName the name of the Key Vault Secret
     * @param vault the key vault to store the certificate
     * @return a binding containing the key vault information
     */
    Mono<AppServiceCertificateKeyVaultBinding> createKeyVaultBindingAsync(String certificateName, Vault vault);

    /** @return the state of the Key Vault secret */
    AppServiceCertificateKeyVaultBinding getKeyVaultBinding();

    /** @return the state of the Key Vault secret */
    Mono<AppServiceCertificateKeyVaultBinding> getKeyVaultBindingAsync();

    /**
     * Verifies the ownership of the domain by providing the Azure purchased domain.
     *
     * @param domain the Azure managed domain
     */
    void verifyDomainOwnership(AppServiceDomain domain);

    /**
     * Verifies the ownership of the domain by providing the Azure purchased domain.
     *
     * @param domain the Azure managed domain
     * @return an Observable to the result
     */
    Mono<Void> verifyDomainOwnershipAsync(AppServiceDomain domain);

    /**************************************************************
     * Fluent interfaces to provision a App service certificate order
     **************************************************************/

    /** Container interface for all the definitions that need to be implemented. */
    interface Definition
        extends DefinitionStages.Blank,
            DefinitionStages.WithHostName,
            DefinitionStages.WithCertificateSku,
            DefinitionStages.WithDomainVerificationFromWebApp,
            DefinitionStages.WithKeyVault,
            DefinitionStages.WithCreate {
    }

    /** Grouping of all the app service certificate order definition stages. */
    interface DefinitionStages {
        /** An app service certificate order definition allowing resource group to be set. */
        interface Blank extends GroupableResource.DefinitionStages.WithExistingResourceGroup<WithHostName> {
        }

        /** An app service certificate order definition allowing hostname to be set. */
        interface WithHostName {
            /**
             * Specifies the hostname the certificate binds to.
             *
             * @param hostName the bare host name, without "www". Use *. prefix if it's a wild card certificate
             * @return the next stage of the definition
             */
            WithCertificateSku withHostName(String hostName);
        }

        /** An app service certificate order definition allowing SKU to be set. */
        interface WithCertificateSku {
            /**
             * Specifies the SKU of the certificate to be standard. It will only provide SSL support to the hostname,
             * and www.hostname. Wildcard type will provide SSL support to any sub-domain under the hostname.
             *
             * @return the next stage of the definition
             */
            WithDomainVerificationFromWebApp withStandardSku();

            /**
             * Specifies the SKU of the certificate to be wildcard. It will provide SSL support to any sub-domain under
             * the hostname.
             *
             * @return the next stage of the definition
             */
            WithDomainVerification withWildcardSku();
        }

        /** An app service certificate order definition allowing domain verification method to be set. */
        interface WithDomainVerification {
            /**
             * Specifies the Azure managed domain to verify the ownership of the domain.
             *
             * @param domain the Azure managed domain
             * @return the next stage of the definition
             */
            WithKeyVault withDomainVerification(AppServiceDomain domain);
        }

        /** An app service certificate order definition allowing more domain verification methods to be set. */
        interface WithDomainVerificationFromWebApp extends WithDomainVerification {
            /**
             * Specifies the web app to verify the ownership of the domain. The web app needs to be bound to the
             * hostname for the certificate.
             *
             * @param webApp the web app bound to the hostname
             * @return the next stage of the definition
             */
            WithKeyVault withWebAppVerification(WebAppBase webApp);
        }

        /** An app service certificate order definition allowing more domain verification methods to be set. */
        interface WithKeyVault {
            /**
             * Specifies an existing key vault to store the certificate private key.
             *
             * <p>The vault MUST allow 2 service principals to read/write secrets: f3c21649-0979-4721-ac85-b0216b2cf413
             * and abfa0a7c-a6b6-4736-8310-5855508787cd. If they don't have access, an attempt will be made to grant
             * access. If you are logged in from an identity without access to the Active Directory Graph, this attempt
             * will fail.
             *
             * @param vault the vault to store the private key
             * @return the next stage of the definition
             */
            WithCreate withExistingKeyVault(Vault vault);

            /**
             * Creates a new key vault to store the certificate private key.
             *
             * <p>DO NOT use this method if you are logged in from an identity without access to the Active Directory
             * Graph.
             *
             * @param vaultName the name of the new key vault
             * @param region the region to create the vault
             * @return the next stage of the definition
             */
            WithCreate withNewKeyVault(String vaultName, Region region);
        }

        /** An app service certificate order definition allowing valid years to be set. */
        interface WithValidYears {
            /**
             * Specifies the valid years of the certificate.
             *
             * @param years minimum 1 year, and maximum 3 years
             * @return the next stage of the definition
             */
            WithCreate withValidYears(int years);
        }

        /** An app service certificate order definition allowing auto-renew settings to be set. */
        interface WithAutoRenew {
            /**
             * Specifies if the certificate should be auto-renewed.
             *
             * @param enabled true if the certificate order should be auto-renewed
             * @return the next stage of the definition
             */
            WithCreate withAutoRenew(boolean enabled);
        }

        /**
         * An app service certificate order definition with sufficient inputs to create a new app service certificate
         * order in the cloud, but exposing additional optional inputs to specify.
         */
        interface WithCreate
            extends Creatable<AppServiceCertificateOrder>,
                WithValidYears,
                WithAutoRenew,
                GroupableResource.DefinitionWithTags<WithCreate> {
        }
    }

    /** Grouping of all the app service certificate order update stages. */
    interface UpdateStages {
        /** An app service certificate order definition allowing auto-renew settings to be set. */
        interface WithAutoRenew {
            /**
             * Specifies if the certificate should be auto-renewed.
             *
             * @param enabled true if the certificate order should be auto-renewed
             * @return the next stage of the update
             */
            Update withAutoRenew(boolean enabled);
        }
    }

    /**
     * The template for an app service certificate order update operation, containing all the settings that can be
     * modified.
     */
    interface Update
        extends Appliable<AppServiceCertificateOrder>,
            UpdateStages.WithAutoRenew,
            GroupableResource.UpdateWithTags<Update> {
    }
}
