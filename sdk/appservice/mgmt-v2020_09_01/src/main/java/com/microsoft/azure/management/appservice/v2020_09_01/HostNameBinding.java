/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 * Code generated by Microsoft (R) AutoRest Code Generator.
 */

package com.microsoft.azure.management.appservice.v2020_09_01;

import com.microsoft.azure.arm.model.HasInner;
import com.microsoft.azure.management.appservice.v2020_09_01.implementation.HostNameBindingInner;
import com.microsoft.azure.arm.model.Indexable;
import com.microsoft.azure.arm.model.Refreshable;
import com.microsoft.azure.arm.model.Updatable;
import com.microsoft.azure.arm.model.Appliable;
import com.microsoft.azure.arm.model.Creatable;
import com.microsoft.azure.arm.resources.models.HasManager;
import com.microsoft.azure.management.appservice.v2020_09_01.implementation.AppServiceManager;

/**
 * Type representing HostNameBinding.
 */
public interface HostNameBinding extends HasInner<HostNameBindingInner>, Indexable, Refreshable<HostNameBinding>, Updatable<HostNameBinding.Update>, HasManager<AppServiceManager> {
    /**
     * @return the azureResourceName value.
     */
    String azureResourceName();

    /**
     * @return the azureResourceType value.
     */
    AzureResourceType azureResourceType();

    /**
     * @return the customHostNameDnsRecordType value.
     */
    CustomHostNameDnsRecordType customHostNameDnsRecordType();

    /**
     * @return the domainId value.
     */
    String domainId();

    /**
     * @return the hostNameType value.
     */
    HostNameType hostNameType();

    /**
     * @return the id value.
     */
    String id();

    /**
     * @return the kind value.
     */
    String kind();

    /**
     * @return the name value.
     */
    String name();

    /**
     * @return the siteName value.
     */
    String siteName();

    /**
     * @return the sslState value.
     */
    SslState sslState();

    /**
     * @return the systemData value.
     */
    SystemData systemData();

    /**
     * @return the thumbprint value.
     */
    String thumbprint();

    /**
     * @return the type value.
     */
    String type();

    /**
     * @return the virtualIP value.
     */
    String virtualIP();

    /**
     * The entirety of the HostNameBinding definition.
     */
    interface Definition extends DefinitionStages.Blank, DefinitionStages.WithSite, DefinitionStages.WithCreate {
    }

    /**
     * Grouping of HostNameBinding definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a HostNameBinding definition.
         */
        interface Blank extends WithSite {
        }

        /**
         * The stage of the hostnamebinding definition allowing to specify Site.
         */
        interface WithSite {
           /**
            * Specifies resourceGroupName, name.
            * @param resourceGroupName Name of the resource group to which the resource belongs
            * @param name Name of the app
            * @return the next definition stage
            */
            WithCreate withExistingSite(String resourceGroupName, String name);
        }

        /**
         * The stage of the hostnamebinding definition allowing to specify AzureResourceName.
         */
        interface WithAzureResourceName {
            /**
             * Specifies azureResourceName.
             * @param azureResourceName Azure resource name
             * @return the next definition stage
             */
            WithCreate withAzureResourceName(String azureResourceName);
        }

        /**
         * The stage of the hostnamebinding definition allowing to specify AzureResourceType.
         */
        interface WithAzureResourceType {
            /**
             * Specifies azureResourceType.
             * @param azureResourceType Azure resource type. Possible values include: 'Website', 'TrafficManager'
             * @return the next definition stage
             */
            WithCreate withAzureResourceType(AzureResourceType azureResourceType);
        }

        /**
         * The stage of the hostnamebinding definition allowing to specify CustomHostNameDnsRecordType.
         */
        interface WithCustomHostNameDnsRecordType {
            /**
             * Specifies customHostNameDnsRecordType.
             * @param customHostNameDnsRecordType Custom DNS record type. Possible values include: 'CName', 'A'
             * @return the next definition stage
             */
            WithCreate withCustomHostNameDnsRecordType(CustomHostNameDnsRecordType customHostNameDnsRecordType);
        }

        /**
         * The stage of the hostnamebinding definition allowing to specify DomainId.
         */
        interface WithDomainId {
            /**
             * Specifies domainId.
             * @param domainId Fully qualified ARM domain resource URI
             * @return the next definition stage
             */
            WithCreate withDomainId(String domainId);
        }

        /**
         * The stage of the hostnamebinding definition allowing to specify HostNameType.
         */
        interface WithHostNameType {
            /**
             * Specifies hostNameType.
             * @param hostNameType Hostname type. Possible values include: 'Verified', 'Managed'
             * @return the next definition stage
             */
            WithCreate withHostNameType(HostNameType hostNameType);
        }

        /**
         * The stage of the hostnamebinding definition allowing to specify Kind.
         */
        interface WithKind {
            /**
             * Specifies kind.
             * @param kind Kind of resource
             * @return the next definition stage
             */
            WithCreate withKind(String kind);
        }

        /**
         * The stage of the hostnamebinding definition allowing to specify SiteName.
         */
        interface WithSiteName {
            /**
             * Specifies siteName.
             * @param siteName App Service app name
             * @return the next definition stage
             */
            WithCreate withSiteName(String siteName);
        }

        /**
         * The stage of the hostnamebinding definition allowing to specify SslState.
         */
        interface WithSslState {
            /**
             * Specifies sslState.
             * @param sslState SSL type. Possible values include: 'Disabled', 'SniEnabled', 'IpBasedEnabled'
             * @return the next definition stage
             */
            WithCreate withSslState(SslState sslState);
        }

        /**
         * The stage of the hostnamebinding definition allowing to specify SystemData.
         */
        interface WithSystemData {
            /**
             * Specifies systemData.
             * @param systemData The system metadata relating to this resource
             * @return the next definition stage
             */
            WithCreate withSystemData(SystemData systemData);
        }

        /**
         * The stage of the hostnamebinding definition allowing to specify Thumbprint.
         */
        interface WithThumbprint {
            /**
             * Specifies thumbprint.
             * @param thumbprint SSL certificate thumbprint
             * @return the next definition stage
             */
            WithCreate withThumbprint(String thumbprint);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends Creatable<HostNameBinding>, DefinitionStages.WithAzureResourceName, DefinitionStages.WithAzureResourceType, DefinitionStages.WithCustomHostNameDnsRecordType, DefinitionStages.WithDomainId, DefinitionStages.WithHostNameType, DefinitionStages.WithKind, DefinitionStages.WithSiteName, DefinitionStages.WithSslState, DefinitionStages.WithSystemData, DefinitionStages.WithThumbprint {
        }
    }
    /**
     * The template for a HostNameBinding update operation, containing all the settings that can be modified.
     */
    interface Update extends Appliable<HostNameBinding>, UpdateStages.WithAzureResourceName, UpdateStages.WithAzureResourceType, UpdateStages.WithCustomHostNameDnsRecordType, UpdateStages.WithDomainId, UpdateStages.WithHostNameType, UpdateStages.WithKind, UpdateStages.WithSiteName, UpdateStages.WithSslState, UpdateStages.WithSystemData, UpdateStages.WithThumbprint {
    }

    /**
     * Grouping of HostNameBinding update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the hostnamebinding update allowing to specify AzureResourceName.
         */
        interface WithAzureResourceName {
            /**
             * Specifies azureResourceName.
             * @param azureResourceName Azure resource name
             * @return the next update stage
             */
            Update withAzureResourceName(String azureResourceName);
        }

        /**
         * The stage of the hostnamebinding update allowing to specify AzureResourceType.
         */
        interface WithAzureResourceType {
            /**
             * Specifies azureResourceType.
             * @param azureResourceType Azure resource type. Possible values include: 'Website', 'TrafficManager'
             * @return the next update stage
             */
            Update withAzureResourceType(AzureResourceType azureResourceType);
        }

        /**
         * The stage of the hostnamebinding update allowing to specify CustomHostNameDnsRecordType.
         */
        interface WithCustomHostNameDnsRecordType {
            /**
             * Specifies customHostNameDnsRecordType.
             * @param customHostNameDnsRecordType Custom DNS record type. Possible values include: 'CName', 'A'
             * @return the next update stage
             */
            Update withCustomHostNameDnsRecordType(CustomHostNameDnsRecordType customHostNameDnsRecordType);
        }

        /**
         * The stage of the hostnamebinding update allowing to specify DomainId.
         */
        interface WithDomainId {
            /**
             * Specifies domainId.
             * @param domainId Fully qualified ARM domain resource URI
             * @return the next update stage
             */
            Update withDomainId(String domainId);
        }

        /**
         * The stage of the hostnamebinding update allowing to specify HostNameType.
         */
        interface WithHostNameType {
            /**
             * Specifies hostNameType.
             * @param hostNameType Hostname type. Possible values include: 'Verified', 'Managed'
             * @return the next update stage
             */
            Update withHostNameType(HostNameType hostNameType);
        }

        /**
         * The stage of the hostnamebinding update allowing to specify Kind.
         */
        interface WithKind {
            /**
             * Specifies kind.
             * @param kind Kind of resource
             * @return the next update stage
             */
            Update withKind(String kind);
        }

        /**
         * The stage of the hostnamebinding update allowing to specify SiteName.
         */
        interface WithSiteName {
            /**
             * Specifies siteName.
             * @param siteName App Service app name
             * @return the next update stage
             */
            Update withSiteName(String siteName);
        }

        /**
         * The stage of the hostnamebinding update allowing to specify SslState.
         */
        interface WithSslState {
            /**
             * Specifies sslState.
             * @param sslState SSL type. Possible values include: 'Disabled', 'SniEnabled', 'IpBasedEnabled'
             * @return the next update stage
             */
            Update withSslState(SslState sslState);
        }

        /**
         * The stage of the hostnamebinding update allowing to specify SystemData.
         */
        interface WithSystemData {
            /**
             * Specifies systemData.
             * @param systemData The system metadata relating to this resource
             * @return the next update stage
             */
            Update withSystemData(SystemData systemData);
        }

        /**
         * The stage of the hostnamebinding update allowing to specify Thumbprint.
         */
        interface WithThumbprint {
            /**
             * Specifies thumbprint.
             * @param thumbprint SSL certificate thumbprint
             * @return the next update stage
             */
            Update withThumbprint(String thumbprint);
        }

    }
}
