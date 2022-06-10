// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.appservice.fluent.models.HostnameBindingInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ExternalChildResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;

/** An immutable representation of a host name binding. */
@Fluent
public interface HostnameBinding
    extends HasInnerModel<HostnameBindingInner>, ExternalChildResource<HostnameBinding, WebAppBase>, Resource {
    /** @return the web app name */
    String webAppName();

    /** @return the fully qualified ARM domain resource URI */
    String domainId();

    /** @return Azure resource name to bind to */
    String azureResourceName();

    /** @return Azure resource type */
    AzureResourceType azureResourceType();

    /** @return custom DNS record type */
    CustomHostnameDnsRecordType dnsRecordType();

    /** @return the host name type */
    HostnameType hostnameType();

    /**
     * The entirety of a hostname binding definition.
     *
     * @param <ParentT> the return type of the final {@link Attachable#attach()}
     */
    interface Definition<ParentT>
        extends DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithDomain<ParentT>,
            DefinitionStages.WithSubDomain<ParentT>,
            DefinitionStages.WithHostNameDnsRecordType<ParentT>,
            DefinitionStages.WithAttach<ParentT> {
    }

    /** Grouping of hostname binding definition stages applicable as part of a web app creation. */
    interface DefinitionStages {
        /**
         * The first stage of a host name binding definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithDomain<ParentT> {
        }

        /**
         * The stage of a hostname binding definition allowing domain to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithDomain<ParentT> {
            /**
             * Binds to a domain purchased from Azure.
             *
             * @param domain the domain purchased from Azure
             * @return the next stage of the definition
             */
            WithSubDomain<ParentT> withAzureManagedDomain(AppServiceDomain domain);

            /**
             * Binds to a 3rd party domain.
             *
             * @param domain the 3rd party domain name
             * @return the next stage of the definition
             */
            WithSubDomain<ParentT> withThirdPartyDomain(String domain);
        }

        /**
         * The stage of a hostname binding definition allowing sub-domain to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithSubDomain<ParentT> {
            /**
             * Specifies the sub-domain to bind to.
             *
             * @param subDomain the sub-domain name excluding the top level domain, e.g., "@", "www"
             * @return the next stage of the definition
             */
            WithHostNameDnsRecordType<ParentT> withSubDomain(String subDomain);
        }

        /**
         * The stage of a hostname binding definition allowing DNS record type to be set.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithHostNameDnsRecordType<ParentT> {
            /**
             * Specifies the DNS record type.
             *
             * @param hostnameDnsRecordType the DNS record type
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withDnsRecordType(CustomHostnameDnsRecordType hostnameDnsRecordType);
        }

        /**
         * The final stage of the hostname binding definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the hostname binding definition can be
         * attached to the parent web app definition using {@link WithAttach#attach()}.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends Attachable.InDefinition<ParentT> {
        }
    }

    /**
     * The entirety of a hostname binding definition as part of a web app update.
     *
     * @param <ParentT> the return type of the final {@link UpdateDefinitionStages.WithAttach#attach()}
     */
    interface UpdateDefinition<ParentT>
        extends UpdateDefinitionStages.Blank<ParentT>,
            UpdateDefinitionStages.WithDomain<ParentT>,
            UpdateDefinitionStages.WithSubDomain<ParentT>,
            UpdateDefinitionStages.WithHostNameDnsRecordType<ParentT>,
            UpdateDefinitionStages.WithAttach<ParentT> {
    }

    /** Grouping of host name binding definition stages applicable as part of a web app creation. */
    interface UpdateDefinitionStages {
        /**
         * The first stage of a host name binding definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithDomain<ParentT> {
        }

        /**
         * The stage of a hostname binding definition allowing domain to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithDomain<ParentT> {
            /**
             * Binds to a domain purchased from Azure.
             *
             * @param domain the domain purchased from Azure
             * @return the next stage of the definition
             */
            WithSubDomain<ParentT> withAzureManagedDomain(AppServiceDomain domain);

            /**
             * Binds to a 3rd party domain.
             *
             * @param domain the 3rd party domain name
             * @return the next stage of the definition
             */
            WithSubDomain<ParentT> withThirdPartyDomain(String domain);
        }

        /**
         * The stage of a hostname binding definition allowing sub-domain to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithSubDomain<ParentT> {
            /**
             * Specifies the sub-domain to bind to.
             *
             * @param subDomain the sub-domain name excluding the top level domain, e.g., "@", "www"
             * @return the next stage of the definition
             */
            WithHostNameDnsRecordType<ParentT> withSubDomain(String subDomain);
        }

        /**
         * The stage of a hostname binding definition allowing DNS record type to be set.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithHostNameDnsRecordType<ParentT> {
            /**
             * Specifies the DNS record type.
             *
             * @param hostnameDnsRecordType the DNS record type
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withDnsRecordType(CustomHostnameDnsRecordType hostnameDnsRecordType);
        }

        /**
         * The final stage of the hostname binding definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the hostname binding definition can be
         * attached to the parent web app update using {@link WithAttach#attach()}.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends Attachable.InUpdate<ParentT> {
        }
    }
}
