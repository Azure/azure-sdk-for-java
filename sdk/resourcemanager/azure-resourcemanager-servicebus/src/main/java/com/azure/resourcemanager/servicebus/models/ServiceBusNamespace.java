// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import com.azure.resourcemanager.servicebus.ServiceBusManager;
import com.azure.resourcemanager.servicebus.fluent.models.SBNamespaceInner;

import java.time.OffsetDateTime;

/**
 * An immutable client-side representation of an Azure Service Bus namespace.
 */
@Fluent
public interface ServiceBusNamespace extends
    GroupableResource<ServiceBusManager, SBNamespaceInner>,
    Refreshable<ServiceBusNamespace>,
    Updatable<ServiceBusNamespace.Update> {
    /**
     * @return the relative DNS name of the Service Bus namespace
     */
    String dnsLabel();
    /**
     * @return fully qualified domain name (FQDN) of the Service Bus namespace
     */
    String fqdn();
    /**
     * @return sku value
     */
    NamespaceSku sku();
    /**
     * @return time the namespace was created
     */
    OffsetDateTime createdAt();
    /**
     * @return time the namespace was updated
     */
    OffsetDateTime updatedAt();

    /**
     * @return entry point to manage queue entities in the Service Bus namespace
     */
    Queues queues();
    /**
     * @return entry point to manage topics entities in the Service Bus namespace
     */
    Topics topics();
    /**
     * @return entry point to manage authorization rules for the Service Bus namespace
     */
    NamespaceAuthorizationRules authorizationRules();

    /**
     * The entirety of the Service Bus namespace definition.
     */
    interface Definition extends
            ServiceBusNamespace.DefinitionStages.Blank,
            ServiceBusNamespace.DefinitionStages.WithGroup,
            ServiceBusNamespace.DefinitionStages.WithCreate {
    }

    /**
     * Grouping of Service Bus namespace definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a Service Bus namespace definition.
         */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * The stage of the Service Bus namespace definition allowing to specify the resource group.
         */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithCreate> {
        }

        /**
         * The stage of the Service Bus namespace definition allowing to specify the sku.
         */
        interface WithSku {
            /**
             * Specifies the namespace sku.
             *
             * @param namespaceSku the sku
             * @return next stage of the Service Bus namespace definition
             */
            WithCreate withSku(NamespaceSku namespaceSku);
        }

        /**
         * The stage of the Service Bus namespace definition allowing to add a new queue in the namespace.
         */
        interface WithQueue {
            /**
             * Creates a queue entity in the Service Bus namespace.
             *
             * @param name queue name
             * @param maxSizeInMB maximum size of memory allocated for the queue entity
             * @return next stage of the Service Bus namespace definition
             */
            WithCreate withNewQueue(String name, int maxSizeInMB);
        }

        /**
         * The stage of the Service Bus namespace definition allowing to add a new topic in the namespace.
         */
        interface WithTopic {
            /**
             * Creates a topic entity in the Service Bus namespace.
             *
             * @param name topic name
             * @param maxSizeInMB maximum size of memory allocated for the topic entity
             * @return next stage of the Service Bus namespace definition
             */
            WithCreate withNewTopic(String name, int maxSizeInMB);
        }

        /**
         * The stage of the Service Bus namespace definition allowing to add an authorization rule for accessing
         * the namespace.
         */
        interface WithAuthorizationRule {
            /**
             * Creates a send authorization rule for the Service Bus namespace.
             *
             * @param name rule name
             * @return next stage of the Service Bus namespace definition
             */
            WithCreate withNewSendRule(String name);
            /**
             * Creates a listen authorization rule for the Service Bus namespace.
             *
             * @param name rule name
             * @return next stage of the Service Bus namespace definition
             */
            WithCreate withNewListenRule(String name);
            /**
             * Creates a manage authorization rule for the Service Bus namespace.
             *
             * @param name rule name
             * @return next stage of the Service Bus namespace definition
             */
            WithCreate withNewManageRule(String name);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends
            Creatable<ServiceBusNamespace>,
            Resource.DefinitionWithTags<WithCreate>,
            ServiceBusNamespace.DefinitionStages.WithSku,
            ServiceBusNamespace.DefinitionStages.WithQueue,
            ServiceBusNamespace.DefinitionStages.WithTopic,
            ServiceBusNamespace.DefinitionStages.WithAuthorizationRule {
        }
    }

    /**
     * The template for a Service Bus namespace update operation, containing all the settings that can be modified.
     */
    interface Update extends
        Appliable<ServiceBusNamespace>,
        Resource.UpdateWithTags<Update>,
        ServiceBusNamespace.UpdateStages.WithSku,
        ServiceBusNamespace.UpdateStages.WithQueue,
        ServiceBusNamespace.UpdateStages.WithTopic,
        ServiceBusNamespace.UpdateStages.WithAuthorizationRule {
    }

    /**
     * Grouping of all the Service Bus namespace update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the Service Bus namespace update allowing to change the sku.
         */
        interface WithSku {
            /**
             * Specifies the namespace sku.
             *
             * @param namespaceSku the sku
             * @return next stage of the Service Bus namespace update
             */
            Update withSku(NamespaceSku namespaceSku);
        }

        /**
         * The stage of the Service Bus namespace update allowing to manage queues in the namespace.
         */
        interface WithQueue {
            /**
             * Creates a queue entity in the Service Bus namespace.
             *
             * @param name queue name
             * @param maxSizeInMB maximum size of memory allocated for the queue entity
             * @return next stage of the Service Bus namespace update
             */
            Update withNewQueue(String name, int maxSizeInMB);

            /**
             * Removes a queue entity from the Service Bus namespace.
             *
             * @param name queue name
             * @return next stage of the Service Bus namespace update
             */
            Update withoutQueue(String name);
        }

        /**
         * The stage of the Service Bus namespace update allowing to manage topics in the namespace.
         */
        interface WithTopic {
            /**
             * Creates a topic entity in the Service Bus namespace.
             *
             * @param name topic name
             * @param maxSizeInMB maximum size of memory allocated for the topic entity
             * @return next stage of the Service Bus namespace update
             */
            Update withNewTopic(String name, int maxSizeInMB);

            /**
             * Removes a topic entity from the Service Bus namespace.
             *
             * @param name topic name
             * @return next stage of the Service Bus namespace update
             */
            Update withoutTopic(String name);
        }

        /**
         * The stage of the Service Bus namespace update allowing manage authorization rules
         * for the namespace.
         */
        interface WithAuthorizationRule {
            /**
             * Creates a send authorization rule for the Service Bus namespace.
             *
             * @param name rule name
             * @return next stage of the Service Bus namespace update
             */
            Update withNewSendRule(String name);
            /**
             * Creates a listen authorization rule for the Service Bus namespace.
             *
             * @param name rule name
             * @return next stage of the Service Bus namespace update
             */
            Update withNewListenRule(String name);
            /**
             * Creates a manage authorization rule for the Service Bus namespace.
             *
             * @param name rule name
             * @return next stage of the Service Bus namespace update
             */
            Update withNewManageRule(String name);
            /**
             * Removes an authorization rule from the Service Bus namespace.
             *
             * @param name rule name
             * @return next stage of the Service Bus namespace update
             */
            Update withoutAuthorizationRule(String name);
        }
    }

}
