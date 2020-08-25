/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.eventhub;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.eventhub.implementation.AuthorizationRuleInner;
import com.microsoft.azure.management.eventhub.implementation.EventHubManager;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import rx.Observable;

import java.util.List;

/**
 * The base type representing authorization rule of event hub namespace and event hub.
 *
 * @param <RuleT> the specific authorization rule type
 */
@Fluent
@Beta(Beta.SinceVersion.V1_7_0)
public interface AuthorizationRule<RuleT extends AuthorizationRule<RuleT>> extends
        NestedResource, HasInner<AuthorizationRuleInner>, HasManager<EventHubManager>,
        Refreshable<RuleT> {
    /**
     * @return rights associated with the authorization rule
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    List<AccessRights> rights();
    /**
     * @return a representation of the deferred computation of this call, returning access keys (primary, secondary) and the connection strings
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    Observable<EventHubAuthorizationKey> getKeysAsync();
    /**
     * @return the access keys (primary, secondary) and the connection strings
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    EventHubAuthorizationKey getKeys();
    /**
     * Regenerates primary or secondary access keys.
     *
     * @param keyType the key to regenerate
     * @return a representation of the deferred computation of this call, returning access keys (primary, secondary) and the connection strings
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    Observable<EventHubAuthorizationKey> regenerateKeyAsync(KeyType keyType);
    /**
     * Regenerates primary or secondary keys.
     *
     * @param keyType the key to regenerate
     * @return the access keys (primary, secondary) and the connection strings
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    EventHubAuthorizationKey regenerateKey(KeyType keyType);

    /**
     * Grouping of commons authorization rule definition stages shared between event hub namespace authorization rule and event hub authorization rule.
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    interface DefinitionStages {
        /**
         * The stage of the event hub namespace or event hub authorization rule definition allowing to enable listen policy.
         *
         * @param <T> the next stage of the definition
         */
        @Beta(Beta.SinceVersion.V1_7_0)
        interface WithListen<T> {
            /**
             * Specifies that the rule should have listening access enabled.
             *
             * @return the next stage of the definition
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            T withListenAccess();
        }

        /**
         * The stage of the event hub namespace or event hub authorization rule definition allowing to enable send policy.
         *
         * @param <T> the next stage of the definition
         */
        @Beta(Beta.SinceVersion.V1_7_0)
        interface WithSend<T> {
            /**
             * Specifies that the rule should have sending access enabled.
             *
             * @return the next stage of the definition
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            T withSendAccess();
        }

        /**
         * The stage of the event hub namespace or event hub authorization rule definition allowing to enable manage policy.
         *
         * @param <T> the next stage of the definition
         */
        @Beta(Beta.SinceVersion.V1_7_0)
        interface WithManage<T> {
            /**
             * Specifies that the rule should have management access enabled.
             *
             * @return the next stage of the definition
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            T withManageAccess();
        }

        /**
         * The stage of the event hub namespace or event hub authorization rule definition allowing to enable send or manage policy.
         *
         * @param <T> the next stage of the definition
         */
        @Beta(Beta.SinceVersion.V1_7_0)
        interface WithSendOrManage<T> extends WithSend<T>, WithManage<T> {
        }

        /**
         * The stage of the event hub namespace or event hub authorization rule definition allowing to enable listen, send or manage policy.
         *
         * @param <T> the next stage of the definition
         */
        @Beta(Beta.SinceVersion.V1_7_0)
        interface WithListenOrSendOrManage<T> extends WithListen<T>, WithSendOrManage<T> {
            /**
             * Specifies that the rule should have sending and listening access enabled.
             *
             * @return the next stage of the definition
             */
            @Beta(Beta.SinceVersion.V1_30_0)
            T withSendAndListenAccess();
        }
    }

    /**
     * Grouping of commons authorization rule update stages shared between event hub namespace authorization rule and event hub authorization rule.
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    interface UpdateStages {
        /**
         * The stage of the event hub namespace or event hub authorization rule update allowing to enable listen policy.
         *
         * @param <T> the next stage of the update
         */
        @Beta(Beta.SinceVersion.V1_7_0)
        interface WithListen<T> {
            /**
             * Specifies that the rule should have listening access enabled.
             *
             * @return the next stage of the update
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            T withListenAccess();
        }

        /**
         * The stage of the event hub namespace or event hub authorization rule update allowing to enable send policy.
         *
         * @param <T> the next stage of the update
         */
        @Beta(Beta.SinceVersion.V1_7_0)
        interface WithSend<T> {
            /**
             * Specifies that the rule should have sending access enabled.
             *
             * @return the next stage of the update
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            T withSendAccess();
        }

        /**
         * The stage of event hub namespace or event hub authorization rule update allowing to enable manage policy.
         *
         * @param <T> the next stage of the update
         */
        @Beta(Beta.SinceVersion.V1_7_0)
        interface WithManage<T> {
            /**
             * Specifies that the rule should have sending access enabled.
             *
             * @return the next stage of the update
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            T withManageAccess();
        }

        /**
         * The stage of the event hub namespace or event hub authorization rule update allowing to enable send or manage policy.
         *
         * @param <T> the next stage of the update
         */
        @Beta(Beta.SinceVersion.V1_7_0)
        interface WithSendOrManage<T> extends WithSend<T>, WithManage<T> {
        }

        /**
         * The stage of the event hub namespace or event hub authorization rule update allowing to enable listen, send or manage policy.
         *
         * @param <T> the next stage of the update
         */
        @Beta(Beta.SinceVersion.V1_7_0)
        interface WithListenOrSendOrManage<T> extends WithListen<T>, WithSendOrManage<T> {
        }
    }
}