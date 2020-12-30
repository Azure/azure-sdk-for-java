// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.authorization.fluent.models.MicrosoftGraphApplicationInner;
import com.azure.resourcemanager.authorization.fluent.models.MicrosoftGraphGroupInner;
import com.azure.resourcemanager.authorization.fluent.models.MicrosoftGraphServicePrincipalInner;
import com.azure.resourcemanager.authorization.fluent.models.MicrosoftGraphUserInner;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryGroup;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryObject;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryUser;
import com.azure.resourcemanager.authorization.models.ServicePrincipal;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Implementation for Group and its parent interfaces. */
class ActiveDirectoryGroupImpl
    extends CreatableUpdatableImpl<ActiveDirectoryGroup, MicrosoftGraphGroupInner, ActiveDirectoryGroupImpl>
    implements ActiveDirectoryGroup, ActiveDirectoryGroup.Definition, ActiveDirectoryGroup.Update {

    private final AuthorizationManager manager;
    private Set<Map<String, Object>> membersToAdd;
    private Set<String> membersToRemove;

    ActiveDirectoryGroupImpl(MicrosoftGraphGroupInner innerModel, AuthorizationManager manager) {
        super(innerModel.displayName(), innerModel);
        this.manager = manager;
        membersToAdd = new HashSet<>();
        membersToRemove = new HashSet<>();
    }

    @Override
    public boolean securityEnabled() {
        return ResourceManagerUtils.toPrimitiveBoolean(innerModel().securityEnabled());
    }

    @Override
    public String mail() {
        return innerModel().mail();
    }

    @Override
    public List<ActiveDirectoryObject> listMembers() {
        return listMembersAsync().collectList().block();
    }

    @Override
    public PagedFlux<ActiveDirectoryObject> listMembersAsync() {
        return PagedConverter.flatMapPage(manager()
            .serviceClient()
            .getGroups()
            .listMemberOfAsync(id()),
            directoryObjectInner -> {
                ActiveDirectoryObject result = null;
                if (directoryObjectInner instanceof MicrosoftGraphUserInner) {
                    result = new ActiveDirectoryUserImpl((MicrosoftGraphUserInner) directoryObjectInner, manager());
                } else if (directoryObjectInner instanceof MicrosoftGraphGroupInner) {
                    result = new ActiveDirectoryGroupImpl((MicrosoftGraphGroupInner) directoryObjectInner, manager());
                } else if (directoryObjectInner instanceof MicrosoftGraphServicePrincipalInner) {
                    result = new ServicePrincipalImpl(
                        (MicrosoftGraphServicePrincipalInner) directoryObjectInner, manager());
                } else if (directoryObjectInner instanceof MicrosoftGraphApplicationInner) {
                    result = new ActiveDirectoryApplicationImpl(
                        (MicrosoftGraphApplicationInner) directoryObjectInner, manager());
                }
                return Mono.justOrEmpty(result);
            }
        );
    }

    @Override
    protected Mono<MicrosoftGraphGroupInner> getInnerAsync() {
        return manager().serviceClient().getGroupsGroups().getGroupAsync(id());
    }

    @Override
    public boolean isInCreateMode() {
        return id() == null;
    }

    @Override
    public Mono<ActiveDirectoryGroup> createResourceAsync() {
        Mono<ActiveDirectoryGroup> group = Mono.just(this);
        if (isInCreateMode()) {
            group = manager().serviceClient().getGroupsGroups().createGroupAsync(innerModel())
                .map(innerToFluentMap(this));
        }
        if (!membersToRemove.isEmpty()) {
            group =
                group
                    .flatMap(
                        o ->
                            Flux
                                .fromIterable(membersToRemove)
                                .flatMap(s -> manager().serviceClient().getGroups().deleteRefMemberAsync(id(), s))
                                .singleOrEmpty()
                                .thenReturn(this)
                                .doFinally(signalType -> membersToRemove.clear()));
        }
        if (!membersToAdd.isEmpty()) {
            group =
                group
                    .flatMap(
                        o ->
                            Flux
                                .fromIterable(membersToAdd)
                                .flatMap(s -> manager().serviceClient().getGroups().createRefMemberOfAsync(id(), s))
                                .singleOrEmpty()
                                .thenReturn(this)
                                .doFinally(signalType -> membersToAdd.clear()));
        }
        return group;
    }

    @Override
    public ActiveDirectoryGroupImpl withEmailAlias(String mailNickname) {
        // User providing domain
        if (mailNickname.contains("@")) {
            String[] parts = mailNickname.split("@");
            // domainName = parts[1]; // no use
            mailNickname = parts[0];
        }
        innerModel().withMailNickname(mailNickname);
        return this;
    }

    @Override
    public ActiveDirectoryGroupImpl withMember(String objectId) {
        membersToAdd.add(new HashMap<>() {{
            put("id", objectId);
        }});
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
        return innerModel().id();
    }

    @Override
    public AuthorizationManager manager() {
        return this.manager;
    }
}
