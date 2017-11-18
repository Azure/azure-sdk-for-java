/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.graphrbac.RoleAssignment;
import com.microsoft.azure.management.graphrbac.RoleAssignments;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.CreatableResourcesImpl;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

/**
 * The implementation of RoleAssignments and its parent interfaces.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.Graph.RBAC.Fluent")
class RoleAssignmentsImpl
        extends CreatableResourcesImpl<
                    RoleAssignment,
                    RoleAssignmentImpl,
                    RoleAssignmentInner>
        implements
            RoleAssignments,
            HasInner<RoleAssignmentsInner> {
    private final GraphRbacManager manager;

    RoleAssignmentsImpl(
            final GraphRbacManager manager) {
        this.manager = manager;
    }

    @Override
    protected RoleAssignmentImpl wrapModel(RoleAssignmentInner roleAssignmentInner) {
        if (roleAssignmentInner == null) {
            return null;
        }
        return new RoleAssignmentImpl(roleAssignmentInner, manager());
    }

    @Override
    public RoleAssignmentImpl getById(String objectId) {
        return (RoleAssignmentImpl) getByIdAsync(objectId).toBlocking().single();
    }

    @Override
    public Observable<RoleAssignment> getByIdAsync(String id) {
        return manager().roleInner().roleAssignments().getByIdAsync(id).map(new Func1<RoleAssignmentInner, RoleAssignment>() {
            @Override
            public RoleAssignment call(RoleAssignmentInner roleAssignmentInner) {
                if (roleAssignmentInner == null) {
                    return null;
                } else {
                    return new RoleAssignmentImpl(roleAssignmentInner, manager());
                }
            }
        });
    }

    @Override
    public ServiceFuture<RoleAssignment> getByIdAsync(String id, ServiceCallback<RoleAssignment> callback) {
        return ServiceFuture.fromBody(getByIdAsync(id), callback);
    }

    @Override
    public RoleAssignmentImpl getByScope(String scope,  String name) {
        return (RoleAssignmentImpl) getByScopeAsync(scope, name).toBlocking().single();
    }

    @Override
    public Observable<RoleAssignment> listByScopeAsync(String scope) {
        return wrapPageAsync(manager().roleInner().roleAssignments().listForScopeAsync(scope));
    }

    @Override
    public PagedList<RoleAssignment> listByScope(String scope) {
        return wrapList(manager().roleInner().roleAssignments().listForScope(scope));
    }

    @Override
    public ServiceFuture<RoleAssignment> getByScopeAsync(String scope, String name, ServiceCallback<RoleAssignment> callback) {
        return ServiceFuture.fromBody(getByScopeAsync(scope, name), callback);
    }

    @Override
    public Observable<RoleAssignment> getByScopeAsync(String scope,  String name) {
        return manager().roleInner().roleAssignments().getAsync(scope, name)
                .map(new Func1<RoleAssignmentInner, RoleAssignment>() {
                    @Override
                    public RoleAssignment call(RoleAssignmentInner roleAssignmentInner) {
                        if (roleAssignmentInner == null) {
                            return null;
                        }
                        return new RoleAssignmentImpl(roleAssignmentInner, manager());
                    }
                });
    }

    @Override
    public RoleAssignmentsInner inner() {
        return this.manager().roleInner().roleAssignments();
    }

    @Override
    public GraphRbacManager manager() {
        return manager;
    }

    @Override
    protected RoleAssignmentImpl wrapModel(String name) {
        return new RoleAssignmentImpl(new RoleAssignmentInner().withName(name), manager());
    }

    @Override
    public Completable deleteByIdAsync(String id) {
        return manager().roleInner().roleAssignments().deleteByIdAsync(id).toCompletable();
    }

    @Override
    public RoleAssignmentImpl define(String name) {
        return wrapModel(name);
    }
}
