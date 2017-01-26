/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.apigeneration.Method;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasName;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.appservice.implementation.AppServiceManager;
import com.microsoft.azure.management.appservice.implementation.DomainInner;
import org.joda.time.DateTime;
import rx.Observable;

import java.util.List;
import java.util.Map;

/**
 * An immutable client-side representation of a domain.
 *
 * Domains in Azure are purchased from 3rd party domain providers. By calling
 * {@link Creatable#create()} or {@link Creatable#createAsync()} you agree to
 * the agreements listed in {@link AppServiceDomains#listAgreements(String)}.
 */
@Fluent(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
public interface AppServiceDomain extends
        GroupableResource<AppServiceManager>,
        HasName,
        Refreshable<AppServiceDomain>,
        Updatable<AppServiceDomain.Update>,
        Wrapper<DomainInner> {

    /**
     * @return admin contact information
     */
    Contact adminContact();

    /**
     * @return billing contact information
     */
    Contact billingContact();

    /**
     * @return registrant contact information
     */
    Contact registrantContact();

    /**
     * @return technical contact information
     */
    Contact techContact();

    /**
     * @return domain registration status
     */
    DomainStatus registrationStatus();

    /**
     * @return name servers
     */
    List<String> nameServers();

    /**
     * @return true if domain privacy is enabled for this domain
     */
    boolean privacy();

    /**
     * @return domain creation timestamp.
     */
    DateTime createdTime();

    /**
     * @return domain expiration timestamp.
     */
    DateTime expirationTime();

    /**
     * @return timestamp when the domain was renewed last time
     */
    DateTime lastRenewedTime();

    /**
     * @return true if domain will renewed automatically
     */
    boolean autoRenew();

    /**
     * @return true if Azure can assign this domain to Web Apps. This value will
     * be true if domain registration status is active and it is hosted on
     * name servers Azure has programmatic access to.
     */
    boolean readyForDnsRecordManagement();

    /**
     * @return all hostnames derived from the domain and assigned to Azure resources
     */
    Map<String, HostName> managedHostNames();

    /**
     * @return legal agreement consent.
     */
    DomainPurchaseConsent consent();

    /**
     * Verifies the ownership of the domain for a certificate order bound to this domain.
     * @param certificateOrderName the name of the certificate order
     * @param domainVerificationToken the domain verification token for the certificate order
     */
    void verifyDomainOwnership(String certificateOrderName, String domainVerificationToken);

    /**
     * Verifies the ownership of the domain for a certificate order bound to this domain.
     * @param certificateOrderName the name of the certificate order
     * @param domainVerificationToken the domain verification token for the certificate order
     * @return the Observable to the result
     */
    Observable<Void> verifyDomainOwnershipAsync(String certificateOrderName, String domainVerificationToken);

    /**************************************************************
     * Fluent interfaces to provision a domain
     **************************************************************/

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithAdminContact,
            DefinitionStages.WithBillingContact,
            DefinitionStages.WithRegistrantContact,
            DefinitionStages.WithTechContact,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of all the domain definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the domain definition.
         */
        interface Blank extends GroupableResource.DefinitionStages.WithExistingResourceGroup<WithRegistrantContact> {
        }

        /**
         * A domain definition allowing admin contact to be set.
         */
        interface WithAdminContact {
            /**
             * Specify the admin contact.
             *
             * @param contact the admin contact
             * @return the next stage of domain definition
             */
            WithCreate withAdminContact(Contact contact);
        }

        /**
         * A domain definition allowing billing contact to be set.
         */
        interface WithBillingContact {
            /**
             * Specify the billing contact.
             *
             * @param contact the billing contact
             * @return the next stage of domain definition
             */
            WithCreate withBillingContact(Contact contact);
        }

        /**
         * A domain definition allowing registrant contact to be set.
         */
        interface WithRegistrantContact {
            /**
             * Specify the registrant contact. By default, this is also the contact for
             * admin, billing, and tech.
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
            @Method
            DomainContact.DefinitionStages.Blank<WithCreate> defineRegistrantContact();
        }

        /**
         * A domain definition allowing tech contact to be set.
         */
        interface WithTechContact {
            /**
             * Specify the tech contact.
             *
             * @param contact the tech contact
             * @return the next stage of domain definition.
             */
            WithCreate withTechContact(Contact contact);
        }

        /**
         * A domain definition allowing domain privacy to be set.
         */
        interface WithDomainPrivacy {
            /**
             * Specifies if the registrant contact information is exposed publicly.
             * If domain privacy is turned on, the contact information will NOT be
             * available publicly.
             *
             * @param domainPrivacy true if domain privacy is turned on
             * @return the next stage of domain definition
             */
            WithCreate withDomainPrivacyEnabled(boolean domainPrivacy);
        }

        /**
         * A domain definition allowing auto-renew setting to be set.
         */
        interface WithAutoRenew {
            /**
             * Specifies if the domain should be automatically renewed when it's
             * about to expire.
             *
             * @param autoRenew true if the domain should be automatically renewed
             * @return the next stage of domain definition
             */
            WithCreate withAutoRenewEnabled(boolean autoRenew);
        }

        /**
         * A domain definition with sufficient inputs to create a new
         * domain in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreate extends
                WithDomainPrivacy,
                WithAutoRenew,
                WithAdminContact,
                WithBillingContact,
                WithTechContact,
                Creatable<AppServiceDomain>,
                DefinitionWithTags<WithCreate> {
        }
    }

    /**
     * Grouping of all the domain update stages.
     */
    interface UpdateStages {
        /**
         * A domain definition allowing admin contact to be set.
         */
        interface WithAdminContact {
            /**
             * Specify the admin contact.
             *
             * @param contact the admin contact
             * @return the next stage of domain definition
             */
            Update withAdminContact(Contact contact);
        }

        /**
         * A domain definition allowing billing contact to be set.
         */
        interface WithBillingContact {
            /**
             * Specify the billing contact.
             *
             * @param contact the billing contact
             * @return the next stage of domain definition
             */
            Update withBillingContact(Contact contact);
        }

        /**
         * A domain definition allowing tech contact to be set.
         */
        interface WithTechContact {
            /**
             * Specify the tech contact.
             *
             * @param contact the tech contact
             * @return the next stage of domain definition.
             */
            Update withTechContact(Contact contact);
        }

        /**
         * A domain definition allowing domain privacy to be set.
         */
        interface WithDomainPrivacy {
            /**
             * Specifies if the registrant contact information is exposed publicly.
             * If domain privacy is turned on, the contact information will NOT be
             * available publicly.
             *
             * @param domainPrivacy true if domain privacy is turned on
             * @return the next stage of domain definition
             */
            Update withDomainPrivacyEnabled(boolean domainPrivacy);
        }

        /**
         * A domain definition allowing auto-renew setting to be set.
         */
        interface WithAutoRenew {
            /**
             * Specifies if the domain should be automatically renewed when it's
             * about to expire.
             *
             * @param autoRenew true if the domain should be automatically renewed
             * @return the next stage of domain definition
             */
            Update withAutoRenewEnabled(boolean autoRenew);
        }
    }

    /**
     * The template for a domain update operation, containing all the settings that can be modified.
     */
    interface Update extends
            Appliable<AppServiceDomain>,
            UpdateStages.WithAdminContact,
            UpdateStages.WithBillingContact,
            UpdateStages.WithTechContact,
            UpdateStages.WithAutoRenew,
            UpdateStages.WithDomainPrivacy,
            GroupableResource.UpdateWithTags<Update> {
    }
}