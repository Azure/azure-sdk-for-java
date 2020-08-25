/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.eventhub.implementation;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.eventhub.DisasterRecoveryPairingAuthorizationRule;
import com.microsoft.azure.management.eventhub.DisasterRecoveryPairingAuthorizationRules;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceId;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Observable;
import rx.functions.Func1;

/**
 * Implementation for {@link DisasterRecoveryPairingAuthorizationRules}.
 */
@LangDefinition
class DisasterRecoveryPairingAuthorizationRulesImpl
        extends ReadableWrappersImpl<DisasterRecoveryPairingAuthorizationRule, DisasterRecoveryPairingAuthorizationRuleImpl, AuthorizationRuleInner>
        implements DisasterRecoveryPairingAuthorizationRules {

    private final EventHubManager manager;

    DisasterRecoveryPairingAuthorizationRulesImpl(EventHubManager manager) {
        this.manager = manager;
    }

    @Override
    public PagedList<DisasterRecoveryPairingAuthorizationRule> listByDisasterRecoveryPairing(String resourceGroupName, String namespaceName, String pairingName) {
        return (new PagedListConverter<AuthorizationRuleInner, DisasterRecoveryPairingAuthorizationRule>() {
            @Override
            public Observable<DisasterRecoveryPairingAuthorizationRule> typeConvertAsync(final AuthorizationRuleInner inner) {
                return Observable.<DisasterRecoveryPairingAuthorizationRule>just(wrapModel(inner));
            }
        }).convert(inner().listAuthorizationRules(resourceGroupName, namespaceName, pairingName));
    }

    @Override
    public Observable<DisasterRecoveryPairingAuthorizationRule> listByDisasterRecoveryPairingAsync(String resourceGroupName, String namespaceName, String pairingName) {
         return this.manager.inner().disasterRecoveryConfigs().listAuthorizationRulesAsync(resourceGroupName, namespaceName, pairingName)
                .flatMapIterable(new Func1<Page<AuthorizationRuleInner>, Iterable<AuthorizationRuleInner>>() {
                    @Override
                    public Iterable<AuthorizationRuleInner> call(Page<AuthorizationRuleInner> page) {
                        return page.items();
                    }
                })
                .map(new Func1<AuthorizationRuleInner, DisasterRecoveryPairingAuthorizationRule>() {
                    @Override
                    public DisasterRecoveryPairingAuthorizationRule call(AuthorizationRuleInner inner) {
                        return  wrapModel(inner);
                    }
                });
    }

    @Override
    public Observable<DisasterRecoveryPairingAuthorizationRule> getByNameAsync(String resourceGroupName, String namespaceName, String pairingName, String name) {
        return this.manager.inner().disasterRecoveryConfigs().getAuthorizationRuleAsync(resourceGroupName,
                namespaceName,
                pairingName,
                name)
                .map(new Func1<AuthorizationRuleInner, DisasterRecoveryPairingAuthorizationRule>() {
                    @Override
                    public DisasterRecoveryPairingAuthorizationRule call(AuthorizationRuleInner inner) {
                        if (inner == null) {
                            return null;
                        }
                        return  wrapModel(inner);
                    }
                });
    }

    @Override
    public DisasterRecoveryPairingAuthorizationRule getByName(String resourceGroupName, String namespaceName, String pairingName, String name) {
        return getByNameAsync(resourceGroupName, namespaceName, pairingName, name).toBlocking().last();
    }

    @Override
    public DisasterRecoveryPairingAuthorizationRule getById(String id) {
        return getByIdAsync(id).toBlocking().last();
    }

    @Override
    public Observable<DisasterRecoveryPairingAuthorizationRule> getByIdAsync(String id) {
        ResourceId resourceId = ResourceId.fromString(id);
        return this.getByNameAsync(resourceId.resourceGroupName(),
                resourceId.parent().name(),
                resourceId.parent().parent().name(),
                resourceId.name());
    }

    @Override
    public ServiceFuture<DisasterRecoveryPairingAuthorizationRule> getByIdAsync(String id, ServiceCallback<DisasterRecoveryPairingAuthorizationRule> callback) {
        return ServiceFuture.fromBody(getByIdAsync(id), callback);
    }

    @Override
    public EventHubManager manager() {
        return this.manager;
    }

    @Override
    public DisasterRecoveryConfigsInner inner() {
        return this.manager.inner().disasterRecoveryConfigs();
    }

    @Override
    protected DisasterRecoveryPairingAuthorizationRuleImpl wrapModel(AuthorizationRuleInner inner) {
        return new DisasterRecoveryPairingAuthorizationRuleImpl(inner, manager);
    }
}
