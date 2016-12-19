/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ExternalChildResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.appservice.implementation.HostNameBindingInner;

/**
 * A host name binding object.
 */
@Fluent(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
public interface HostNameBinding
        extends
        Wrapper<HostNameBindingInner>,
        ExternalChildResource<HostNameBinding, WebAppBase>, Resource {
    /**
     * @return the hostname to bind to
     */
    String hostName();

    /**
     * @return the web app name
     */
    String webAppName();

    /**
     * @return the fully qualified ARM domain resource URI
     */
    String domainId();

    /**
     * @return Azure resource name to bind to
     */
    String azureResourceName();

    /**
     * @return Azure resource type
     */
    AzureResourceType azureResourceType();

    /**
     * @return custom DNS record type
     */
    CustomHostNameDnsRecordType dnsRecordType();

    /**
     * @return the host name type
     */
    HostNameType hostNameType();

    /**
     * The entirety of a hostname binding definition.
     * @param <ParentT> the return type of the final {@link Attachable#attach()}
     */
    interface Definition<ParentT> extends
            DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithDomain<ParentT>,
            DefinitionStages.WithSubDomain<ParentT>,
            DefinitionStages.WithHostNameDnsRecordType<ParentT>,
            DefinitionStages.WithAttach<ParentT> {
    }

    /**
     * Grouping of hostname binding definition stages applicable as part of a web app creation.
     */
    interface DefinitionStages {
        /**
         * The first stage of a host name binding definition.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface Blank<ParentT> extends WithDomain<ParentT> {
        }

        /**
         * The stage of a hostname binding definition allowing domain to be specified.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface WithDomain<ParentT> {
            /**
             * Binds to a domain purchased from Azure.
             * @param domain the domain purchased from Azure
             * @return the next stage of the host name binding definition
             */
            WithSubDomain<ParentT> withAzureManagedDomain(AppServiceDomain domain);

            /**
             * Binds to a 3rd party domain.
             * @param domain the 3rd party domain name
             * @return the next stage of the host name binding definition
             */
            WithSubDomain<ParentT> withThirdPartyDomain(String domain);
        }

        /**
         * The stage of a hostname binding definition allowing sub-domain to be specified.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface WithSubDomain<ParentT> {
            /**
             * Specifies the sub-domain to bind to.
             * @param subDomain the sub-domain name excluding the top level domain, e.g., "@", "www"
             * @return the next stage of the host name binding definition
             */
            WithHostNameDnsRecordType<ParentT> withSubDomain(String subDomain);
        }

        /**
         * The stage of a hostname binding definition allowing DNS record type to be set.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface WithHostNameDnsRecordType<ParentT> {
            /**
             * Specifies the DNS record type.
             * @param hostNameDnsRecordType the DNS record type
             * @return the next stage of the host name binding definition
             */
            WithAttach<ParentT> withDnsRecordType(CustomHostNameDnsRecordType hostNameDnsRecordType);
        }

        /**
         * The final stage of the hostname binding definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the hostname binding definition
         * can be attached to the parent web app definition using {@link WithAttach#attach()}.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
                Attachable.InDefinition<ParentT> {
        }
    }

    /**
     * The entirety of a hostname binding definition as part of a web app update.
     * @param <ParentT> the return type of the final {@link UpdateDefinitionStages.WithAttach#attach()}
     */
    interface UpdateDefinition<ParentT> extends
            UpdateDefinitionStages.Blank<ParentT>,
            UpdateDefinitionStages.WithDomain<ParentT>,
            UpdateDefinitionStages.WithSubDomain<ParentT>,
            UpdateDefinitionStages.WithHostNameDnsRecordType<ParentT>,
            UpdateDefinitionStages.WithAttach<ParentT> {
    }

    /**
     * Grouping of host name binding definition stages applicable as part of a web app creation.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of a host name binding definition.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface Blank<ParentT> extends WithDomain<ParentT> {
        }

        /**
         * The stage of a hostname binding definition allowing domain to be specified.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface WithDomain<ParentT> {
            /**
             * Binds to a domain purchased from Azure.
             * @param domain the domain purchased from Azure
             * @return the next stage of the host name binding definition
             */
            WithSubDomain<ParentT> withAzureManagedDomain(AppServiceDomain domain);

            /**
             * Binds to a 3rd party domain.
             * @param domain the 3rd party domain name
             * @return the next stage of the host name binding definition
             */
            WithSubDomain<ParentT> withThirdPartyDomain(String domain);
        }

        /**
         * The stage of a hostname binding definition allowing sub-domain to be specified.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface WithSubDomain<ParentT> {
            /**
             * Specifies the sub-domain to bind to.
             * @param subDomain the sub-domain name excluding the top level domain, e.g., "@", "www"
             * @return the next stage of the host name binding definition
             */
            WithHostNameDnsRecordType<ParentT> withSubDomain(String subDomain);
        }

        /**
         * The stage of a hostname binding definition allowing DNS record type to be set.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface WithHostNameDnsRecordType<ParentT> {
            /**
             * Specifies the DNS record type.
             * @param hostNameDnsRecordType the DNS record type
             * @return the next stage of the host name binding definition
             */
            WithAttach<ParentT> withDnsRecordType(CustomHostNameDnsRecordType hostNameDnsRecordType);
        }

        /**
         * The final stage of the hostname binding definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the hostname binding definition
         * can be attached to the parent web app  update using {@link WithAttach#attach()}.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
                Attachable.InUpdate<ParentT> {
        }
    }
}