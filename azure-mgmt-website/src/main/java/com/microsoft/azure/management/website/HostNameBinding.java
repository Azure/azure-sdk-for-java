/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website;

import com.microsoft.azure.management.resources.fluentcore.arm.models.ExternalChildResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.website.implementation.HostNameBindingInner;

/**
 * A host name binding object.
 */
public interface HostNameBinding
        extends
        Wrapper<HostNameBindingInner>,
        ExternalChildResource<HostNameBinding, WebAppBase<?>>, Resource {
    /**
     * @return Hostname.
     */
    String hostName();

    /**
     * @return Web app name.
     */
    String siteName();

    /**
     * @return Fully qualified ARM domain resource URI.
     */
    String domainId();

    /**
     * @return Azure resource name.
     */
    String azureResourceName();

    /**
     * @return Azure resource type. Possible values include: 'Website',
     * 'TrafficManager'.
     */
    AzureResourceType azureResourceType();

    /**
     * @return Custom DNS record type. Possible values include: 'CName', 'A'.
     */
    CustomHostNameDnsRecordType DnsRecordType();

    /**
     * @return Host name type. Possible values include: 'Verified', 'Managed'.
     */
    HostNameType hostNameType();

    interface Definition<ParentT> extends
            DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithDomain<ParentT>,
            DefinitionStages.WithHostNameDnsRecordType<ParentT>,
            DefinitionStages.WithAttach<ParentT> {
    }

    /**
     * Grouping of security rule definition stages applicable as part of a network security group creation.
     */
    interface DefinitionStages {
        /**
         * The first stage of a host name binding definition.
         *
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface Blank<ParentT> extends WithDomain<ParentT> {
        }

        interface WithDomain<ParentT> {
            WithHostNameDnsRecordType<ParentT> withAzureManagedDomain(Domain domain);
            WithHostNameDnsRecordType<ParentT> withThirdPartyDomain(String domain);
        }

        interface WithHostNameDnsRecordType<ParentT> {
            WithAttach<ParentT> withDnsRecordType(CustomHostNameDnsRecordType hostNameDnsRecordType);
        }

        interface WithAttach<ParentT> extends
                Attachable.InDefinition<ParentT> {
        }
    }

    interface UpdateDefinition<ParentT> extends
            UpdateDefinitionStages.Blank<ParentT>,
            UpdateDefinitionStages.WithDomain<ParentT>,
            UpdateDefinitionStages.WithHostNameDnsRecordType<ParentT>,
            UpdateDefinitionStages.WithAttach<ParentT> {
    }

    /**
     * Grouping of security rule definition stages applicable as part of a network security group creation.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of a host name binding definition.
         *
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface Blank<ParentT> extends WithDomain<ParentT> {
        }

        interface WithDomain<ParentT> {
            WithHostNameDnsRecordType<ParentT> withAzureManagedDomain(Domain domain);
            WithHostNameDnsRecordType<ParentT> withThirdPartyDomain(String domain);
        }

        interface WithHostNameDnsRecordType<ParentT> {
            WithAttach<ParentT> withDnsRecordType(CustomHostNameDnsRecordType hostNameDnsRecordType);
        }

        interface WithAttach<ParentT> extends
                Attachable.InUpdate<ParentT> {
        }
    }
}