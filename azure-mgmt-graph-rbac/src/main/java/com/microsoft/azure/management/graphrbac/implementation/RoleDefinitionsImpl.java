/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.graphrbac.RoleDefinition;
import com.microsoft.azure.management.graphrbac.RoleDefinitions;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Observable;
import rx.functions.Func1;

/**
 * The implementation of RoleDefinitions and its parent interfaces.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.Graph.RBAC.Fluent")
class RoleDefinitionsImpl
        extends ReadableWrappersImpl<
                            RoleDefinition,
                            RoleDefinitionImpl,
                            RoleDefinitionInner>
        implements
            RoleDefinitions,
            HasInner<RoleDefinitionsInner> {
    private final GraphRbacManager manager;

    RoleDefinitionsImpl(
            final GraphRbacManager manager) {
        this.manager = manager;
    }
    @Override
    protected RoleDefinitionImpl wrapModel(RoleDefinitionInner roleDefinitionInner) {
        if (roleDefinitionInner == null) {
            return null;
        }
        return new RoleDefinitionImpl(roleDefinitionInner, manager());
    }

    @Override
    public RoleDefinitionImpl getById(String objectId) {
        return (RoleDefinitionImpl) getByIdAsync(objectId).toBlocking().single();
    }

    @Override
    public Observable<RoleDefinition> getByIdAsync(String id) {
        return manager().roleInner().roleDefinitions().getByIdAsync(id).map(new Func1<RoleDefinitionInner, RoleDefinition>() {
            @Override
            public RoleDefinition call(RoleDefinitionInner roleDefinitionInner) {
                if (roleDefinitionInner == null) {
                    return null;
                } else {
                    return new RoleDefinitionImpl(roleDefinitionInner, manager());
                }
            }
        });
    }

    @Override
    public ServiceFuture<RoleDefinition> getByIdAsync(String id, ServiceCallback<RoleDefinition> callback) {
        return ServiceFuture.fromBody(getByIdAsync(id), callback);
    }

    @Override
    public RoleDefinitionImpl getByScope(String scope,  String name) {
        return (RoleDefinitionImpl) getByScopeAsync(scope, name).toBlocking().single();
    }

    @Override
    public ServiceFuture<RoleDefinition> getByScopeAsync(String scope, String name, ServiceCallback<RoleDefinition> callback) {
        return ServiceFuture.fromBody(getByScopeAsync(scope, name), callback);
    }

    @Override
    public Observable<RoleDefinition> getByScopeAsync(String scope,  String name) {
        return manager().roleInner().roleDefinitions().getAsync(scope, name)
                .map(new Func1<RoleDefinitionInner, RoleDefinition>() {
                    @Override
                    public RoleDefinition call(RoleDefinitionInner roleDefinitionInner) {
                        if (roleDefinitionInner == null) {
                            return null;
                        }
                        return new RoleDefinitionImpl(roleDefinitionInner, manager());
                    }
                });
    }

    @Override
    public RoleDefinitionImpl getByScopeAndRoleName(String scope,  String roleName) {
        return (RoleDefinitionImpl) getByScopeAndRoleNameAsync(scope, roleName).toBlocking().single();
    }

    @Override
    public Observable<RoleDefinition> listByScopeAsync(String scope) {
        return wrapPageAsync(manager().roleInner().roleDefinitions().listAsync(scope));
    }

    @Override
    public PagedList<RoleDefinition> listByScope(String scope) {
        return wrapList(manager().roleInner().roleDefinitions().list(scope));
    }

    @Override
    public ServiceFuture<RoleDefinition> getByScopeAndRoleNameAsync(String scope, String roleName, ServiceCallback<RoleDefinition> callback) {
        return ServiceFuture.fromBody(getByScopeAndRoleNameAsync(scope, roleName), callback);
    }

    @Override
    public Observable<RoleDefinition> getByScopeAndRoleNameAsync(String scope,  String roleName) {
        return manager().roleInner().roleDefinitions().listAsync(scope, String.format("roleName eq '%s'", roleName))
                .map(new Func1<Page<RoleDefinitionInner>, RoleDefinition>() {
                    @Override
                    public RoleDefinition call(Page<RoleDefinitionInner> roleDefinitionInnerPage) {
                        if (roleDefinitionInnerPage == null || roleDefinitionInnerPage.items() == null || roleDefinitionInnerPage.items().isEmpty()) {
                            return null;
                        }
                        return new RoleDefinitionImpl(roleDefinitionInnerPage.items().get(0), manager());
                    }
                });
    }

    @Override
    public RoleDefinitionsInner inner() {
        return this.manager().roleInner().roleDefinitions();
    }

    @Override
    public GraphRbacManager manager() {
        return manager;
    }
}
