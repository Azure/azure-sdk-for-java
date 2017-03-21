/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.servicebus.implementation.NamespaceInner;
import com.microsoft.azure.management.servicebus.implementation.ServiceBusManager;
import org.joda.time.DateTime;

/**
 * An immutable client-side representation of an Azure service bus namespace.
 */
@Fluent
public interface Namespace extends
        GroupableResource<ServiceBusManager, NamespaceInner>,
        Refreshable<Namespace>,
        Updatable<Namespace.Update> {
    /**
     * @return the relative DNS name of the service bus namespace
     */
    String dnsLabel();
    /**
     * @return fully qualified domain name (FQDN) of the service bus namespace
     */
    String fqdn();
    /**
     * @return sku value
     */
    NamespaceSku sku();
    /**
     * @return time the namespace was created
     */
    DateTime createdAt();
    /**
     * @return time the namespace was updated
     */
    DateTime updatedAt();

    /**
     * @return entry point to manage queue entities in the service bus namespace
     */
    Queues queues();
    /**
     * @return entry point to manage topics entities in the service bus namespace
     */
    Topics topics();
    /**
     * @return entry point to manage authorization rules for the service bus namespace
     */
    NamespaceAuthorizationRules authorizationRules();

    /**
     * The entirety of the service bus namespace definition.
     */
    interface Definition extends
            Namespace.DefinitionStages.Blank,
            Namespace.DefinitionStages.WithGroup,
            Namespace.DefinitionStages.WithCreate {
    }

    /**
     * Grouping of service bus namespace definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a service bus namespace definition.
         */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * The stage of the service bus namespace definition allowing to specify the resource group.
         */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithCreate> {
        }

        /**
         * The stage of the service bus namespace definition allowing to specify the sku.
         */
        interface WithSku {
            /**
             * Specifies the namespace sku.
             *
             * @param namespaceSku the sku
             * @return next stage of the service bus namespace definition
             */
            WithCreate withSku(NamespaceSku namespaceSku);
        }

        /**
         * The stage of the service bus namespace definition allowing to add a new queue in the namespace.
         */
        interface WithQueue {
            /**
             * Creates a queue entity in the service bus namespace.
             *
             * @param name queue name
             * @param maxSizeInMB maximum size of memory allocated for the queue entity
             * @return next stage of the service bus namespace definition
             */
            WithCreate withNewQueue(String name, int maxSizeInMB);
        }

        /**
         * The stage of the service bus namespace definition allowing to add a new topic in the namespace.
         */
        interface WithTopic {
            /**
             * Creates a topic entity in the service bus namespace.
             *
             * @param name topic name
             * @param maxSizeInMB maximum size of memory allocated for the topic entity
             * @return next stage of the service bus namespace definition
             */
            WithCreate withNewTopic(String name, int maxSizeInMB);
        }

        /**
         * The stage of the service bus namespace definition allowing to add an authorization rule for accessing
         * the namespace.
         */
        interface WithAuthorizationRule {
            /**
             * Creates an authorization rule for the service bus namespace.
             *
             * @param name rule name
             * @param rights rule rights
             * @return next stage of the service bus namespace definition
             */
            WithCreate withNewAuthorizationRule(String name, AccessRights... rights);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends
                Creatable<Namespace>,
                Resource.DefinitionWithTags<Update>,
                Namespace.DefinitionStages.WithSku,
                Namespace.DefinitionStages.WithQueue,
                Namespace.DefinitionStages.WithTopic,
                Namespace.DefinitionStages.WithAuthorizationRule {
        }
    }

    /**
     * The template for a service bus namespace update operation, containing all the settings that can be modified.
     */
    interface Update extends
            Appliable<Namespace>,
            Resource.UpdateWithTags<Update>,
            Namespace.UpdateStages.WithSku,
            Namespace.UpdateStages.WithQueue,
            Namespace.UpdateStages.WithTopic,
            Namespace.UpdateStages.WithAuthorizationRule {
    }

    /**
     * Grouping of all the service bus namespace update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the service bus namespace update allowing to change the sku.
         */
        interface WithSku {
            /**
             * Specifies the namespace sku.
             *
             * @param namespaceSku the sku
             * @return next stage of the service bus namespace update
             */
            Update withSku(NamespaceSku namespaceSku);
        }

        /**
         * The stage of the service bus namespace update allowing to manage queues in the namespace.
         */
        interface WithQueue {
            /**
             * Creates a queue entity in the service bus namespace.
             *
             * @param name queue name
             * @param maxSizeInMB maximum size of memory allocated for the queue entity
             * @return next stage of the service bus namespace update
             */
            Update withNewQueue(String name, int maxSizeInMB);

            /**
             * Removes a queue entity from the service bus namespace.
             *
             * @param name queue name
             * @return next stage of the service bus namespace update
             */
            Update withoutQueue(String name);
        }

        /**
         * The stage of the service bus namespace update allowing to manage topics in the namespace.
         */
        interface WithTopic {
            /**
             * Creates a topic entity in the service bus namespace.
             *
             * @param name topic name
             * @param maxSizeInMB maximum size of memory allocated for the topic entity
             * @return next stage of the service bus namespace update
             */
            Update withNewTopic(String name, int maxSizeInMB);

            /**
             * Removes a topic entity from the service bus namespace.
             *
             * @param name topic name
             * @return  next stage of the service bus namespace update
             */
            Update withoutTopic(String name);
        }

        /**
         * The stage of the service bus namespace update allowing manage authorization rules
         * for the namespace.
         */
        interface WithAuthorizationRule {
            /**
             * Creates an authorization rule for the service bus namespace.
             *
             * @param name rule name
             * @param rights rule rights
             * @return next stage of the service bus namespace update
             */
            Update withNewAuthorizationRule(String name, AccessRights... rights);

            /**
             * Removes an authorization rule from the service bus namespace.
             *
             * @param name rule name
             * @return next stage of the service bus namespace update
             */
            Update withoutAuthorizationRule(String name);
        }
    }

}