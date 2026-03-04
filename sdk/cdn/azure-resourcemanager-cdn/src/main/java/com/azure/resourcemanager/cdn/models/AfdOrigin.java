// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.cdn.fluent.models.AfdOriginInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ExternalChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;

/**
 * An immutable client-side representation of an Azure Front Door (AFD) origin that lives under an
 * {@link AfdOriginGroup}.
 */
@Fluent
public interface AfdOrigin extends ExternalChildResource<AfdOrigin, AfdOriginGroup>, HasInnerModel<AfdOriginInner> {

    /**
     * Gets the name of the origin group which contains this origin.
     *
     * @return the origin group name
     */
    String originGroupName();

    /**
     * Gets the resource reference to the Azure origin resource.
     *
     * @return the Azure origin resource reference
     */
    ResourceReference azureOrigin();

    /**
     * Gets the address of the origin.
     *
     * @return the hostname
     */
    String hostname();

    /**
     * Gets the HTTP port for the origin.
     *
     * @return the HTTP port
     */
    Integer httpPort();

    /**
     * Gets the HTTPS port for the origin.
     *
     * @return the HTTPS port
     */
    Integer httpsPort();

    /**
     * Gets the host header value sent to the origin with each request.
     *
     * @return the origin host header
     */
    String originHostHeader();

    /**
     * Gets the priority of this origin in the origin group for load balancing.
     *
     * @return the priority (1–5)
     */
    Integer priority();

    /**
     * Gets the weight of this origin in the origin group for load balancing.
     *
     * @return the weight (1–1000)
     */
    Integer weight();

    /**
     * Gets the shared private link resource properties for this private origin.
     *
     * @return the shared private link resource properties
     */
    SharedPrivateLinkResourceProperties sharedPrivateLinkResource();

    /**
     * Gets the capacity resource properties for this origin.
     *
     * @return the origin capacity resource properties
     */
    OriginCapacityResourceProperties originCapacityResource();

    /**
     * Gets whether health probes are enabled for this origin.
     *
     * @return the enabled state
     */
    EnabledState enabledState();

    /**
     * Gets whether certificate name check is enforced at origin level.
     *
     * @return true if certificate name check is enforced
     */
    Boolean enforceCertificateNameCheck();

    /**
     * Gets the provisioning state reported by the service.
     *
     * @return the provisioning state
     */
    AfdProvisioningState provisioningState();

    /**
     * Gets the deployment status for the origin.
     *
     * @return the deployment status
     */
    DeploymentStatus deploymentStatus();

    /**
     * Grouping of AFD origin definition stages as part of a parent {@link AfdOriginGroup} definition.
     */
    interface DefinitionStages {
        /**
         * The first stage of an AFD origin definition.
         *
         * @param <ParentT> the stage of the parent AFD origin group definition to return to after attaching
         */
        interface Blank<ParentT> extends WithHostname<ParentT> {
        }

        /**
         * The stage of an AFD origin definition requiring the hostname to be specified.
         *
         * @param <ParentT> the stage of the parent AFD origin group definition to return to after attaching
         */
        interface WithHostname<ParentT> {
            /**
             * Specifies the hostname (address) of the origin.
             *
             * @param hostname the hostname of the origin
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withHostname(String hostname);
        }

        /**
         * The stage of the definition containing optional settings prior to attachment.
         *
         * @param <ParentT> the stage of the parent AFD origin group definition to return to after attaching
         */
        interface WithAttach<ParentT> extends Attachable<ParentT> {
            /**
             * Specifies a resource reference to an Azure origin resource.
             *
             * @param azureOrigin the Azure origin resource reference
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withAzureOrigin(ResourceReference azureOrigin);

            /**
             * Specifies the HTTP port for the origin.
             *
             * @param httpPort the HTTP port (1–65535)
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withHttpPort(Integer httpPort);

            /**
             * Specifies the HTTPS port for the origin.
             *
             * @param httpsPort the HTTPS port (1–65535)
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withHttpsPort(Integer httpsPort);

            /**
             * Specifies the host header value sent to the origin with each request.
             *
             * @param originHostHeader the origin host header
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withOriginHostHeader(String originHostHeader);

            /**
             * Specifies the priority of this origin in the origin group for load balancing.
             *
             * @param priority the priority (1–5)
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withPriority(Integer priority);

            /**
             * Specifies the weight of this origin in the origin group for load balancing.
             *
             * @param weight the weight (1–1000)
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withWeight(Integer weight);

            /**
             * Specifies the shared private link resource properties for a private origin.
             *
             * @param sharedPrivateLinkResource the shared private link resource properties
             * @return the next stage of the definition
             */
            WithAttach<ParentT>
                withSharedPrivateLinkResource(SharedPrivateLinkResourceProperties sharedPrivateLinkResource);

            /**
             * Specifies the capacity resource properties for the origin.
             *
             * @param originCapacityResource the origin capacity resource properties
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withOriginCapacityResource(OriginCapacityResourceProperties originCapacityResource);

            /**
             * Specifies whether health probes should be enabled for this origin.
             *
             * @param enabledState the enabled state
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withEnabledState(EnabledState enabledState);

            /**
             * Specifies whether certificate name check should be enforced at origin level.
             *
             * @param enforce true to enforce certificate name check
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withEnforceCertificateNameCheck(Boolean enforce);
        }

        /**
         * The final stage of an AFD origin definition.
         *
         * @param <ParentT> the stage of the parent AFD origin group definition to return to after attaching
         */
        interface Attachable<ParentT> {
            /**
             * Attaches the defined origin to the parent AFD origin group.
             *
             * @return the next stage of the parent definition
             */
            ParentT attach();
        }
    }

    /**
     * The entirety of an AFD origin definition.
     *
     * @param <ParentT> the stage of the parent AFD origin group definition to return to after attaching
     */
    interface Definition<ParentT> extends DefinitionStages.Blank<ParentT>, DefinitionStages.WithHostname<ParentT>,
        DefinitionStages.WithAttach<ParentT> {
    }

    /**
     * Grouping of AFD origin definition stages that run as part of an {@link AfdOriginGroup.Update} flow.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of an AFD origin definition inside an origin group update.
         *
         * @param <ParentT> the stage of the parent AFD origin group update to return to after attaching
         */
        interface Blank<ParentT> extends WithHostname<ParentT> {
        }

        /**
         * The stage of an AFD origin update-definition requiring the hostname to be specified.
         *
         * @param <ParentT> the stage of the parent AFD origin group update to return to after attaching
         */
        interface WithHostname<ParentT> {
            /**
             * Specifies the hostname (address) of the origin.
             *
             * @param hostname the hostname of the origin
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withHostname(String hostname);
        }

        /**
         * The stage of the definition containing optional settings prior to attachment.
         *
         * @param <ParentT> the stage of the parent AFD origin group update to return to after attaching
         */
        interface WithAttach<ParentT> extends Attachable<ParentT> {
            /**
             * Specifies a resource reference to an Azure origin resource.
             *
             * @param azureOrigin the Azure origin resource reference
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withAzureOrigin(ResourceReference azureOrigin);

            /**
             * Specifies the HTTP port for the origin.
             *
             * @param httpPort the HTTP port (1–65535)
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withHttpPort(Integer httpPort);

            /**
             * Specifies the HTTPS port for the origin.
             *
             * @param httpsPort the HTTPS port (1–65535)
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withHttpsPort(Integer httpsPort);

            /**
             * Specifies the host header value sent to the origin with each request.
             *
             * @param originHostHeader the origin host header
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withOriginHostHeader(String originHostHeader);

            /**
             * Specifies the priority of this origin in the origin group for load balancing.
             *
             * @param priority the priority (1–5)
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withPriority(Integer priority);

            /**
             * Specifies the weight of this origin in the origin group for load balancing.
             *
             * @param weight the weight (1–1000)
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withWeight(Integer weight);

            /**
             * Specifies the shared private link resource properties for a private origin.
             *
             * @param sharedPrivateLinkResource the shared private link resource properties
             * @return the next stage of the definition
             */
            WithAttach<ParentT>
                withSharedPrivateLinkResource(SharedPrivateLinkResourceProperties sharedPrivateLinkResource);

            /**
             * Specifies the capacity resource properties for the origin.
             *
             * @param originCapacityResource the origin capacity resource properties
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withOriginCapacityResource(OriginCapacityResourceProperties originCapacityResource);

            /**
             * Specifies whether health probes should be enabled for this origin.
             *
             * @param enabledState the enabled state
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withEnabledState(EnabledState enabledState);

            /**
             * Specifies whether certificate name check should be enforced at origin level.
             *
             * @param enforce true to enforce certificate name check
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withEnforceCertificateNameCheck(Boolean enforce);
        }

        /**
         * The final stage of an AFD origin definition inside an origin group update.
         *
         * @param <ParentT> the stage of the parent AFD origin group update to return to after attaching
         */
        interface Attachable<ParentT> {
            /**
             * Attaches the defined origin to the parent AFD origin group update.
             *
             * @return the next stage of the parent update
             */
            ParentT attach();
        }
    }

    /**
     * The entirety of an AFD origin update inside an {@link AfdOriginGroup.Update} flow.
     */
    interface Update extends Settable<AfdOriginGroup.Update> {
        /**
         * Specifies the hostname (address) of the origin.
         *
         * @param hostname the hostname of the origin
         * @return the next stage of the update
         */
        Update withHostname(String hostname);

        /**
         * Specifies a resource reference to an Azure origin resource.
         *
         * @param azureOrigin the Azure origin resource reference
         * @return the next stage of the update
         */
        Update withAzureOrigin(ResourceReference azureOrigin);

        /**
         * Specifies the HTTP port for the origin.
         *
         * @param httpPort the HTTP port (1–65535)
         * @return the next stage of the update
         */
        Update withHttpPort(Integer httpPort);

        /**
         * Specifies the HTTPS port for the origin.
         *
         * @param httpsPort the HTTPS port (1–65535)
         * @return the next stage of the update
         */
        Update withHttpsPort(Integer httpsPort);

        /**
         * Specifies the host header value sent to the origin with each request.
         *
         * @param originHostHeader the origin host header
         * @return the next stage of the update
         */
        Update withOriginHostHeader(String originHostHeader);

        /**
         * Specifies the priority of this origin in the origin group for load balancing.
         *
         * @param priority the priority (1–5)
         * @return the next stage of the update
         */
        Update withPriority(Integer priority);

        /**
         * Specifies the weight of this origin in the origin group for load balancing.
         *
         * @param weight the weight (1–1000)
         * @return the next stage of the update
         */
        Update withWeight(Integer weight);

        /**
         * Specifies the shared private link resource properties for a private origin.
         *
         * @param sharedPrivateLinkResource the shared private link resource properties
         * @return the next stage of the update
         */
        Update withSharedPrivateLinkResource(SharedPrivateLinkResourceProperties sharedPrivateLinkResource);

        /**
         * Specifies the capacity resource properties for the origin.
         *
         * @param originCapacityResource the origin capacity resource properties
         * @return the next stage of the update
         */
        Update withOriginCapacityResource(OriginCapacityResourceProperties originCapacityResource);

        /**
         * Specifies whether health probes should be enabled for this origin.
         *
         * @param enabledState the enabled state
         * @return the next stage of the update
         */
        Update withEnabledState(EnabledState enabledState);

        /**
         * Specifies whether certificate name check should be enforced at origin level.
         *
         * @param enforce true to enforce certificate name check
         * @return the next stage of the update
         */
        Update withEnforceCertificateNameCheck(Boolean enforce);
    }
}
