/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website;

import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasName;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.website.implementation.DomainInner;
import org.joda.time.DateTime;

import java.util.List;

/**
 * An immutable client-side representation of an Azure App Service Plan.
 */
public interface Domain extends
        GroupableResource,
        HasName,
        Refreshable<Domain>,
        Updatable<Domain.Update>,
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
    List<HostName> managedHostNames();

    /**
     * @return legal agreement consent.
     */
    DomainPurchaseConsent consent();


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
     * Grouping of all the site definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the app service plan definition.
         */
        interface Blank extends GroupableResource.DefinitionStages.WithGroup<WithAdminContact> {
        }

        /**
         * An app service plan definition allowing pricing tier to be set.
         */
        interface WithAdminContact {
            WithBillingContact withAdminContact(Contact contact);

            WithCreate withContact(Contact contact);
        }

        /**
         * An app service plan definition allowing per site scaling configuration to be set.
         */
        interface WithBillingContact {
            WithRegistrantContact withBillingContact(Contact contact);
        }

        /**
         * An app service plan definition allowing instance capacity to be set.
         */
        interface WithRegistrantContact {
            WithTechContact withRegistrantContact(Contact contact);
        }

        /**
         * An app service plan definition allowing instance capacity to be set.
         */
        interface WithTechContact {
            WithCreate withTechContact(Contact contact);
        }

        interface WithDomainPrivacy {
            WithCreate withDomainPrivacyEnabled(boolean domainPrivacy);
        }

        interface WithAutoRenew {
            WithCreate withAutoRenewEnabled(boolean autoRenew);
        }

        interface WithNameServers {
            WithCreate withNameServer(String nameServer);
            WithCreate withNameServers(List<String> nameServers);
        }

        /**
         * An app service plan definition with sufficient inputs to create a new
         * website in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreate extends
                WithDomainPrivacy,
                WithAutoRenew,
                WithNameServers,
                Creatable<Domain>,
                DefinitionWithTags<WithCreate> {
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
    interface Update extends
            Appliable<Domain>,
            GroupableResource.UpdateWithTags<Update> {
    }
}