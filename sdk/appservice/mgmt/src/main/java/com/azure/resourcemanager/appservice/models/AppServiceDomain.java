// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.appservice.fluent.inner.DomainInner;
import com.azure.resourcemanager.dns.models.DnsZone;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import reactor.core.publisher.Mono;

/**
 * An immutable client-side representation of a domain.
 *
 * <p>Domains in Azure are purchased from 3rd party domain providers. By calling {@link Creatable#create()} or {@link
 * Creatable#createAsync()} you agree to the agreements listed in {@link AppServiceDomains#listAgreements(String)}.
 */
@Fluent
public interface AppServiceDomain
    extends GroupableResource<AppServiceManager, DomainInner>,
        HasName,
        Refreshable<AppServiceDomain>,
        Updatable<AppServiceDomain.Update> {

    /** @return admin contact information */
    Contact adminContact();

    /** @return billing contact information */
    Contact billingContact();

    /** @return registrant contact information */
    Contact registrantContact();

    /** @return technical contact information */
    Contact techContact();

    /** @return domain registration status */
    DomainStatus registrationStatus();

    /** @return name servers */
    List<String> nameServers();

    /** @return true if domain privacy is enabled for this domain */
    boolean privacy();

    /** @return domain creation timestamp. */
    OffsetDateTime createdTime();

    /** @return domain expiration timestamp. */
    OffsetDateTime expirationTime();

    /** @return timestamp when the domain was renewed last time */
    OffsetDateTime lastRenewedTime();

    /** @return true if domain will renewed automatically */
    boolean autoRenew();

    /**
     * @return true if Azure can assign this domain to Web Apps. This value will be true if domain registration status
     *     is active and it is hosted on name servers Azure has programmatic access to.
     */
    boolean readyForDnsRecordManagement();

    /** @return all hostnames derived from the domain and assigned to Azure resources */
    Map<String, Hostname> managedHostNames();

    /** @return legal agreement consent. */
    DomainPurchaseConsent consent();

    /**
     * @return the type of DNS
     */
    DnsType dnsType();

    /**
     * @return Azure DNS zone id
     */
    String dnsZoneId();

    /**
     * Verifies the ownership of the domain for a certificate order bound to this domain.
     *
     * @param certificateOrderName the name of the certificate order
     * @param domainVerificationToken the domain verification token for the certificate order
     */
    void verifyDomainOwnership(String certificateOrderName, String domainVerificationToken);

    /**
     * Verifies the ownership of the domain for a certificate order bound to this domain.
     *
     * @param certificateOrderName the name of the certificate order
     * @param domainVerificationToken the domain verification token for the certificate order
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> verifyDomainOwnershipAsync(String certificateOrderName, String domainVerificationToken);

    /**************************************************************
     * Fluent interfaces to provision a domain
     **************************************************************/

    /** Container interface for all the definitions that need to be implemented. */
    interface Definition
        extends DefinitionStages.Blank,
            DefinitionStages.WithAdminContact,
            DefinitionStages.WithBillingContact,
            DefinitionStages.WithRegistrantContact,
            DefinitionStages.WithTechContact,
            DefinitionStages.WithCreate {
    }

    /** Grouping of all the domain definition stages. */
    interface DefinitionStages {
        /** The first stage of the domain definition. */
        interface Blank extends GroupableResource.DefinitionStages.WithExistingResourceGroup<WithRegistrantContact> {
        }

        /** A domain definition allowing admin contact to be set. */
        interface WithAdminContact {
            /**
             * Specify the admin contact.
             *
             * @param contact the admin contact
             * @return the next stage of domain definition
             */
            WithCreate withAdminContact(Contact contact);
        }

        /** A domain definition allowing billing contact to be set. */
        interface WithBillingContact {
            /**
             * Specify the billing contact.
             *
             * @param contact the billing contact
             * @return the next stage of domain definition
             */
            WithCreate withBillingContact(Contact contact);
        }

        /** A domain definition allowing registrant contact to be set. */
        interface WithRegistrantContact {
            /**
             * Specify the registrant contact. By default, this is also the contact for admin, billing, and tech.
             *
             * @param contact the registrant contact
             * @return the next stage of domain definition
             */
            WithCreate withRegistrantContact(Contact contact);

            /**
             * Starts the definition of a new domain contact.
             *
             * @return the first stage of the domain contact definition
             */
            DomainContact.DefinitionStages.Blank<WithCreate> defineRegistrantContact();
        }

        /** A domain definition allowing tech contact to be set. */
        interface WithTechContact {
            /**
             * Specify the tech contact.
             *
             * @param contact the tech contact
             * @return the next stage of domain definition.
             */
            WithCreate withTechContact(Contact contact);
        }

        /** A domain definition allowing domain privacy to be set. */
        interface WithDomainPrivacy {
            /**
             * Specifies if the registrant contact information is exposed publicly. If domain privacy is turned on, the
             * contact information will NOT be available publicly.
             *
             * @param domainPrivacy true if domain privacy is turned on
             * @return the next stage of domain definition
             */
            WithCreate withDomainPrivacyEnabled(boolean domainPrivacy);
        }

        /** A domain definition allowing auto-renew setting to be set. */
        interface WithAutoRenew {
            /**
             * Specifies if the domain should be automatically renewed when it's about to expire.
             *
             * @param autoRenew true if the domain should be automatically renewed
             * @return the next stage of domain definition
             */
            WithCreate withAutoRenewEnabled(boolean autoRenew);
        }

        /**
         * A domain definition allowing DNS zone to be set.
         */
        interface WithDnsZone {
            /**
             * Creates a new DNS zone.
             *
             * @param dnsZoneName the name of DNS zone
             * @return the next stage of domain definition
             */
            WithCreate withNewDnsZone(String dnsZoneName);

            /**
             * Creates a new DNS zone.
             *
             * @param dnsZone the creatable definition of DNS zone
             * @return the next stage of domain definition
             */
            WithCreate withNewDnsZone(Creatable<DnsZone> dnsZone);

            /**
             * Specifies an existing DNS zone.
             *
             * @param dnsZoneId the id of DNS zone
             * @return the next stage of domain definition
             */
            WithCreate withExistingDnsZone(String dnsZoneId);

            /**
             * Specifies an existing DNS zone.
             *
             * @param dnsZone the DNS zone
             * @return the next stage of domain definition
             */
            WithCreate withExistingDnsZone(DnsZone dnsZone);
        }

        /**
         * A domain definition with sufficient inputs to create a new domain in the cloud, but exposing additional
         * optional inputs to specify.
         */
        interface WithCreate
            extends WithDomainPrivacy,
                WithAutoRenew,
                WithAdminContact,
                WithBillingContact,
                WithTechContact,
                WithDnsZone,
                Creatable<AppServiceDomain>,
                DefinitionWithTags<WithCreate> {
        }
    }

    /** Grouping of all the domain update stages. */
    interface UpdateStages {
        /** A domain definition allowing admin contact to be set. */
        interface WithAdminContact {
            /**
             * Specify the admin contact.
             *
             * @param contact the admin contact
             * @return the next stage of domain definition
             */
            Update withAdminContact(Contact contact);
        }

        /** A domain definition allowing billing contact to be set. */
        interface WithBillingContact {
            /**
             * Specify the billing contact.
             *
             * @param contact the billing contact
             * @return the next stage of domain definition
             */
            Update withBillingContact(Contact contact);
        }

        /** A domain definition allowing tech contact to be set. */
        interface WithTechContact {
            /**
             * Specify the tech contact.
             *
             * @param contact the tech contact
             * @return the next stage of domain definition.
             */
            Update withTechContact(Contact contact);
        }

        /** A domain definition allowing domain privacy to be set. */
        interface WithDomainPrivacy {
            /**
             * Specifies if the registrant contact information is exposed publicly. If domain privacy is turned on, the
             * contact information will NOT be available publicly.
             *
             * @param domainPrivacy true if domain privacy is turned on
             * @return the next stage of domain definition
             */
            Update withDomainPrivacyEnabled(boolean domainPrivacy);
        }

        /** A domain definition allowing auto-renew setting to be set. */
        interface WithAutoRenew {
            /**
             * Specifies if the domain should be automatically renewed when it's about to expire.
             *
             * @param autoRenew true if the domain should be automatically renewed
             * @return the next stage of domain definition
             */
            Update withAutoRenewEnabled(boolean autoRenew);
        }

        /**
         * A domain definition allowing DNS zone to be set.
         */
        interface WithDnsZone {
            /**
             * Creates a new DNS zone.
             *
             * @param dnsZoneName the name of DNS zone
             * @return the next stage of domain definition
             */
            Update withNewDnsZone(String dnsZoneName);

            /**
             * Creates a new DNS zone.
             *
             * @param dnsZone the creatable definition of DNS zone
             * @return the next stage of domain definition
             */
            Update withNewDnsZone(Creatable<DnsZone> dnsZone);

            /**
             * Specifies an existing DNS zone.
             *
             * @param dnsZoneId the id of DNS zone
             * @return the next stage of domain definition
             */
            Update withExistingDnsZone(String dnsZoneId);

            /**
             * Specifies an existing DNS zone.
             *
             * @param dnsZone the DNS zone
             * @return the next stage of domain definition
             */
            Update withExistingDnsZone(DnsZone dnsZone);
        }
    }

    /** The template for a domain update operation, containing all the settings that can be modified. */
    interface Update
        extends Appliable<AppServiceDomain>,
            UpdateStages.WithAdminContact,
            UpdateStages.WithBillingContact,
            UpdateStages.WithTechContact,
            UpdateStages.WithAutoRenew,
            UpdateStages.WithDomainPrivacy,
            UpdateStages.WithDnsZone,
            GroupableResource.UpdateWithTags<Update> {
    }
}
