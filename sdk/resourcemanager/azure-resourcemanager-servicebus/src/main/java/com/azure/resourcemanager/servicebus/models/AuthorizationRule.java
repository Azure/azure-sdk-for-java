// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.IndependentChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.servicebus.fluent.models.SBAuthorizationRuleInner;
import com.azure.resourcemanager.servicebus.ServiceBusManager;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Type representing authorization rule.
 *
 * @param <RuleT> the specific rule type
 */
@Fluent
public interface AuthorizationRule<RuleT extends AuthorizationRule<RuleT>> extends
    IndependentChildResource<ServiceBusManager, SBAuthorizationRuleInner>,
    Refreshable<RuleT> {

    /**
     * @return rights associated with the rule
     */
    List<AccessRights> rights();

    /**
     * @return a representation of the deferred computation of this call,
     * returning the primary, secondary keys and the connection strings
     */
    Mono<AuthorizationKeys> getKeysAsync();

    /**
     * @return the primary, secondary keys and connection strings
     */
    AuthorizationKeys getKeys();

    /**
     * Regenerates primary or secondary keys.
     *
     * @param regenerateAccessKeyParameters the parameters for the key to regenerate
     * @return a representation of the deferred computation of this call,
     * returning the primary, secondary keys and the connection strings
     */
    Mono<AuthorizationKeys> regenerateKeyAsync(RegenerateAccessKeyParameters regenerateAccessKeyParameters);

    /**
     * Regenerates primary or secondary keys.
     *
     * @param regenerateAccessKeyParameters the parameters for the key to regenerate
     * @return primary, secondary keys and connection strings
     */
    AuthorizationKeys regenerateKey(RegenerateAccessKeyParameters regenerateAccessKeyParameters);

    /**
     * Regenerates primary or secondary keys.
     *
     * @param keyType the type for the key to regenerate
     * @return primary, secondary keys and connection strings
     */
    AuthorizationKeys regenerateKey(KeyType keyType);

    /**
     * Regenerates primary or secondary keys.
     *
     * @param keyType the type for the key to regenerate
     * @return a representation of the deferred computation of this call,
     * returning the primary, secondary keys and the connection strings
     */
    Mono<AuthorizationKeys> regenerateKeyAsync(KeyType keyType);

    /**
     * Grouping of commons authorization rule definition stages shared between different Service Bus
     * entities (namespace, queue, topic, subscription) access rules.
     */
    interface DefinitionStages {
        /**
         * The stage of the Service Bus authorization rule definition allowing to enable listen policy.
         *
         * @param <T> the next stage of the definition
         */
        interface WithListen<T> {
            /**
             * @return the next stage of the definition
             */
            T withListeningEnabled();
        }

        /**
         * The stage of the Service Bus authorization rule definition allowing to enable send policy.
         *
         * @param <T> the next stage of the definition
         */
        interface WithSend<T> {
            /**
             * @return the next stage of the definition
             */
            T withSendingEnabled();
        }

        /**
         * The stage of the Service Bus authorization rule definition allowing to enable manage policy.
         *
         * @param <T> the next stage of the definition
         */
        interface WithManage<T> {
            /**
             * @return the next stage of the definition
             */
            T withManagementEnabled();
        }

        /**
         * The stage of the Service Bus authorization rule definition allowing to enable send or manage policy.
         *
         * @param <T> the next stage of the definition
         */
        interface WithSendOrManage<T> extends WithSend<T>, WithManage<T> {
        }

        /**
         * The stage of the Service Bus authorization rule definition allowing to enable listen, send or manage policy.
         *
         * @param <T> the next stage of the definition
         */
        interface WithListenOrSendOrManage<T> extends WithListen<T>, WithSendOrManage<T> {
        }
    }

    /**
     * Grouping of commons authorization rule update stages shared between different Service Bus
     * entities (namespace, queue, topic, subscription) access rules.
     */
    interface UpdateStages {
        /**
         * The stage of the Service Bus authorization rule update allowing to enable listen policy.
         *
         * @param <T> the next stage of the update
         */
        interface WithListen<T> {
            /**
             * @return the next stage of the update
             */
            T withListeningEnabled();
        }

        /**
         * The stage of the Service Bus authorization rule update allowing to enable send policy.
         *
         * @param <T> the next stage of the update
         */
        interface WithSend<T> {
            /**
             * @return the next stage of the update
             */
            T withSendingEnabled();
        }

        /**
         * The stage of Service Bus authorization rule update allowing to enable manage policy.
         *
         * @param <T> the next stage of the update
         */
        interface WithManage<T> {
            /**
             * @return the next stage of the update
             */
            T withManagementEnabled();
        }

        /**
         * The stage of the Service Bus authorization rule update allowing to enable send or manage policy.
         *
         * @param <T> the next stage of the update
         */
        interface WithSendOrManage<T> extends WithSend<T>, WithManage<T> {
        }

        /**
         * The stage of the Service Bus authorization rule update allowing to enable listen, send or manage policy.
         *
         * @param <T> the next stage of the update
         */
        interface WithListenOrSendOrManage<T> extends WithListen<T>, WithSendOrManage<T> {
        }
    }
}
