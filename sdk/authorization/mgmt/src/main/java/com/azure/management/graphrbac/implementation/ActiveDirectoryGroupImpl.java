/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.graphrbac.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.management.graphrbac.ActiveDirectoryGroup;
import com.azure.management.graphrbac.ActiveDirectoryObject;
import com.azure.management.graphrbac.ActiveDirectoryUser;
import com.azure.management.graphrbac.GroupAddMemberParameters;
import com.azure.management.graphrbac.GroupCreateParameters;
import com.azure.management.graphrbac.ServicePrincipal;
import com.azure.management.graphrbac.models.ADGroupInner;
import com.azure.management.graphrbac.models.ApplicationInner;
import com.azure.management.graphrbac.models.ServicePrincipalInner;
import com.azure.management.graphrbac.models.UserInner;
import com.azure.management.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import com.azure.management.resources.fluentcore.utils.Utils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation for Group and its parent interfaces.
 */
class ActiveDirectoryGroupImpl
        extends CreatableUpdatableImpl<ActiveDirectoryGroup, ADGroupInner, ActiveDirectoryGroupImpl>
        implements ActiveDirectoryGroup,
            ActiveDirectoryGroup.Definition,
            ActiveDirectoryGroup.Update {

    private final GraphRbacManager manager;
    private GroupCreateParameters createParameters;
    private String domainName;
    private Set<String> membersToAdd;
    private Set<String> membersToRemove;

    ActiveDirectoryGroupImpl(ADGroupInner innerModel, GraphRbacManager manager) {
        super(innerModel.getDisplayName(), innerModel);
        this.manager = manager;
        this.createParameters = new GroupCreateParameters()
                .setDisplayName(innerModel.getDisplayName())
                .setMailEnabled(false)
                .setSecurityEnabled(true);
        membersToAdd = new HashSet<>();
        membersToRemove = new HashSet<>();
    }

    @Override
    public boolean securityEnabled() {
        return Utils.toPrimitiveBoolean(inner().isSecurityEnabled());
    }

    @Override
    public String mail() {
        return inner().getMail();
    }

    @Override
    public Set<ActiveDirectoryObject> listMembers() {
        return Collections.unmodifiableSet(new HashSet(listMembersAsync().buffer().blockLast()));
    }

    @Override
    public PagedFlux<ActiveDirectoryObject> listMembersAsync() {
        return manager().inner().groups().getGroupMembersAsync(id())
                .mapPage(directoryObjectInner -> {
                    if (directoryObjectInner instanceof UserInner) {
                        return new ActiveDirectoryUserImpl((UserInner) directoryObjectInner, manager());
                    } else if (directoryObjectInner instanceof ADGroupInner) {
                        return new ActiveDirectoryGroupImpl((ADGroupInner)directoryObjectInner, manager());
                    } else if (directoryObjectInner instanceof ServicePrincipalInner) {
                        return new ServicePrincipalImpl((ServicePrincipalInner)directoryObjectInner, manager());
                    } else if (directoryObjectInner instanceof ApplicationInner) {
                        return new ActiveDirectoryApplicationImpl((ApplicationInner)directoryObjectInner, manager());
                    } else {
                        return null;
                    }
                });
    }

    @Override
    protected Mono<ADGroupInner> getInnerAsync() {
        return manager().inner().groups().getAsync(id());
    }

    @Override
    public boolean isInCreateMode() {
        return id() == null;
    }

    @Override
    public Mono<ActiveDirectoryGroup> createResourceAsync() {
        Mono<?> group = Mono.just(this);
        if (isInCreateMode()) {
            group = manager().inner().groups().createAsync(createParameters)
                    .map(innerToFluentMap(this));
        }
        if (!membersToRemove.isEmpty()) {
            group = group.flatMap(o -> Flux.fromIterable(membersToRemove)
                    .flatMap(s -> manager().inner().groups().removeMemberAsync(id(), s))
                    .singleOrEmpty()
                    .thenReturn(Mono.just(this))
                    .doFinally(signalType -> membersToRemove.clear()));
        }
        if (!membersToAdd.isEmpty()) {
            group = group.flatMap(o -> Flux.fromIterable(membersToAdd)
                    .flatMap(s -> manager().inner().groups().addMemberAsync(id(), new GroupAddMemberParameters().setUrl(s)))
                    .singleOrEmpty()
                    .thenReturn(Mono.just(this))
                    .doFinally(signalType -> membersToAdd.clear()));
        }
        return group.map(o -> ActiveDirectoryGroupImpl.this);
    }

    @Override
    public ActiveDirectoryGroupImpl withEmailAlias(String mailNickname) {
        // User providing domain
        if (mailNickname.contains("@")) {
            String[] parts = mailNickname.split("@");
            domainName = parts[1];
            mailNickname = parts[0];
        }
        createParameters.setMailNickname(mailNickname);
        return this;
    }

    @Override
    public ActiveDirectoryGroupImpl withMember(String objectId) {
        membersToAdd.add(String.format("%s%s/directoryObjects/%s",
                manager().inner().getHost(), manager().tenantId(), objectId));
        return this;
    }

    @Override
    public ActiveDirectoryGroupImpl withMember(ActiveDirectoryUser user) {
        return withMember(user.id());
    }

    @Override
    public ActiveDirectoryGroupImpl withMember(ActiveDirectoryGroup group) {
        return withMember(group.id());
    }

    @Override
    public ActiveDirectoryGroupImpl withMember(ServicePrincipal servicePrincipal) {
        return withMember(servicePrincipal.id());
    }

    @Override
    public ActiveDirectoryGroupImpl withoutMember(String objectId) {
        membersToRemove.add(objectId);
        return this;
    }

    @Override
    public ActiveDirectoryGroupImpl withoutMember(ActiveDirectoryUser user) {
        return withoutMember(user.id());
    }

    @Override
    public ActiveDirectoryGroupImpl withoutMember(ActiveDirectoryGroup group) {
        return withoutMember(group.id());
    }

    @Override
    public ActiveDirectoryGroupImpl withoutMember(ServicePrincipal servicePrincipal) {
        return withoutMember(servicePrincipal.id());
    }

    @Override
    public String id() {
        return inner().getObjectId();
    }

    @Override
    public GraphRbacManager manager() {
        return this.manager;
    }
}
