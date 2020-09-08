// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.eventhubs.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.eventhubs.EventHubsManager;
import com.azure.resourcemanager.eventhubs.fluent.inner.AuthorizationRuleInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * The base type representing authorization rule of event hub namespace and event hub.
 *
 * @param <RuleT> the specific authorization rule type
 */
@Fluent
public interface AuthorizationRule<RuleT extends AuthorizationRule<RuleT>> extends
        NestedResource, HasInner<AuthorizationRuleInner>, HasManager<EventHubsManager>,
        Refreshable<RuleT> {
    /**
     * @return rights associated with the authorization rule
     */
    List<AccessRights> rights();
    /**
     * @return a representation of the deferred computation of this call,
     * returning access keys (primary, secondary) and the connection strings
     */
    Mono<EventHubAuthorizationKey> getKeysAsync();
    /**
     * @return the access keys (primary, secondary) and the connection strings
     */
    EventHubAuthorizationKey getKeys();
    /**
     * Regenerates primary or secondary access keys.
     *
     * @param keyType the key to regenerate
     * @return a representation of the deferred computation of this call,
     * returning access keys (primary, secondary) and the connection strings
     */
    Mono<EventHubAuthorizationKey> regenerateKeyAsync(KeyType keyType);
    /**
     * Regenerates primary or secondary keys.
     *
     * @param keyType the key to regenerate
     * @return the access keys (primary, secondary) and the connection strings
     */
    EventHubAuthorizationKey regenerateKey(KeyType keyType);

    /**
     * Grouping of commons authorization rule definition stages shared
     * between event hub namespace authorization rule and event hub authorization rule.
     */
    interface DefinitionStages {
        /**
         * The stage of the event hub namespace or event hub authorization rule definition
         * allowing to enable listen policy.
         *
         * @param <T> the next stage of the definition
         */
        interface WithListen<T> {
            /**
             * Specifies that the rule should have listening access enabled.
             *
             * @return the next stage of the definition
             */
            T withListenAccess();
        }

        /**
         * The stage of the event hub namespace or event hub authorization rule definition
         * allowing to enable send policy.
         *
         * @param <T> the next stage of the definition
         */
        interface WithSend<T> {
            /**
             * Specifies that the rule should have sending access enabled.
             *
             * @return the next stage of the definition
             */
            T withSendAccess();
        }

        /**
         * The stage of the event hub namespace or event hub authorization rule definition
         * allowing to enable manage policy.
         *
         * @param <T> the next stage of the definition
         */
        interface WithManage<T> {
            /**
             * Specifies that the rule should have management access enabled.
             *
             * @return the next stage of the definition
             */
            T withManageAccess();
        }

        /**
         * The stage of the event hub namespace or event hub authorization rule definition
         * allowing to enable send or manage policy.
         *
         * @param <T> the next stage of the definition
         */
        interface WithSendOrManage<T> extends WithSend<T>, WithManage<T> {
        }

        /**
         * The stage of the event hub namespace or event hub authorization rule definition
         * allowing to enable listen, send or manage policy.
         *
         * @param <T> the next stage of the definition
         */
        interface WithListenOrSendOrManage<T> extends WithListen<T>, WithSendOrManage<T> {
            /**
             * Specifies that the rule should have sending and listening access enabled.
             *
             * @return the next stage of the definition
             */
            T withSendAndListenAccess();
        }
    }

    /**
     * Grouping of commons authorization rule update stages shared
     * between event hub namespace authorization rule and event hub authorization rule.
     */
    interface UpdateStages {
        /**
         * The stage of the event hub namespace or event hub authorization rule update
         * allowing to enable listen policy.
         *
         * @param <T> the next stage of the update
         */
        interface WithListen<T> {
            /**
             * Specifies that the rule should have listening access enabled.
             *
             * @return the next stage of the update
             */
            T withListenAccess();
        }

        /**
         * The stage of the event hub namespace or event hub authorization rule update
         * allowing to enable send policy.
         *
         * @param <T> the next stage of the update
         */
        interface WithSend<T> {
            /**
             * Specifies that the rule should have sending access enabled.
             *
             * @return the next stage of the update
             */
            T withSendAccess();
        }

        /**
         * The stage of event hub namespace or event hub authorization rule update
         * allowing to enable manage policy.
         *
         * @param <T> the next stage of the update
         */
        interface WithManage<T> {
            /**
             * Specifies that the rule should have sending access enabled.
             *
             * @return the next stage of the update
             */
            T withManageAccess();
        }

        /**
         * The stage of the event hub namespace or event hub authorization rule update
         * allowing to enable send or manage policy.
         *
         * @param <T> the next stage of the update
         */
        interface WithSendOrManage<T> extends WithSend<T>, WithManage<T> {
        }

        /**
         * The stage of the event hub namespace or event hub authorization rule update
         * allowing to enable listen, send or manage policy.
         *
         * @param <T> the next stage of the update
         */
        interface WithListenOrSendOrManage<T> extends WithListen<T>, WithSendOrManage<T> {
        }
    }
}
