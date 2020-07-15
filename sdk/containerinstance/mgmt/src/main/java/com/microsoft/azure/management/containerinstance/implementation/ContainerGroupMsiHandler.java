/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.containerinstance.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.containerinstance.ContainerGroupIdentity;
import com.microsoft.azure.management.containerinstance.ContainerGroupIdentityUserAssignedIdentitiesValue;
import com.microsoft.azure.management.containerinstance.ResourceIdentityType;
import com.microsoft.azure.management.graphrbac.implementation.GraphRbacManager;
import com.microsoft.azure.management.graphrbac.implementation.RoleAssignmentHelper;
import com.microsoft.azure.management.resources.fluentcore.dag.TaskGroup;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.msi.Identity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@LangDefinition
class ContainerGroupMsiHandler extends RoleAssignmentHelper {
    private final ContainerGroupImpl containerGroup;

    private List<String> creatableIdentityKeys;
    private Map<String, ContainerGroupIdentityUserAssignedIdentitiesValue> userAssignedIdentities;

    ContainerGroupMsiHandler(final GraphRbacManager rbacManager, ContainerGroupImpl containerGroup) {
        super(rbacManager, containerGroup.taskGroup(), containerGroup.idProvider());
        this.containerGroup = containerGroup;
        this.creatableIdentityKeys = new ArrayList<>();
        this.userAssignedIdentities = new HashMap<>();
    }

    void processCreatedExternalIdentities() {
        for (String key : this.creatableIdentityKeys) {
            Identity identity = (Identity) this.containerGroup.taskGroup().taskResult(key);
            Objects.requireNonNull(identity);
            this.userAssignedIdentities.put(identity.id(), new ContainerGroupIdentityUserAssignedIdentitiesValue());
        }
        this.creatableIdentityKeys.clear();
    }

    void handleExternalIdentities() {
        if (!this.userAssignedIdentities.isEmpty()) {
            this.containerGroup.inner().identity().withUserAssignedIdentities(this.userAssignedIdentities);
        }
    }

    /**
     * Specifies that Local Managed Service Identity needs to be enabled in the virtual machine.
     * If MSI extension is not already installed then it will be installed with access token
     * port as 50342.
     *
     * @return ContainerGroupMsiHandler
     */
    ContainerGroupMsiHandler withLocalManagedServiceIdentity() {
        this.initContainerInstanceIdentity(ResourceIdentityType.SYSTEM_ASSIGNED);
        return this;
    }

    /**
     * Specifies that given identity should be set as one of the External Managed Service Identity
     * of the container instance.
     *
     * @param creatableIdentity yet-to-be-created identity to be associated with the container instance
     * @return ContainerGroupMsiHandler
     */
    ContainerGroupMsiHandler withNewExternalManagedServiceIdentity(Creatable<Identity> creatableIdentity) {
        this.initContainerInstanceIdentity(ResourceIdentityType.USER_ASSIGNED);

        TaskGroup.HasTaskGroup dependency = (TaskGroup.HasTaskGroup) creatableIdentity;
        Objects.requireNonNull(dependency);

        this.containerGroup.taskGroup().addDependency(dependency);
        this.creatableIdentityKeys.add(creatableIdentity.key());

        return this;
    }

    /**
     * Specifies that given identity should be set as one of the External Managed Service Identity
     * of the container instance.
     *
     * @param identity an identity to associate
     * @return ContainerGroupMsiHandler
     */
    ContainerGroupMsiHandler withExistingExternalManagedServiceIdentity(Identity identity) {
        this.initContainerInstanceIdentity(ResourceIdentityType.USER_ASSIGNED);
        this.userAssignedIdentities.put(identity.id(), new ContainerGroupIdentityUserAssignedIdentitiesValue());
        return this;
    }

    /**
     * Initialize Container Instance's identity property.
     *
     * @param identityType the identity type to set
     */
    private void initContainerInstanceIdentity(ResourceIdentityType identityType) {
        if (!identityType.equals(ResourceIdentityType.USER_ASSIGNED)
                && !identityType.equals(ResourceIdentityType.SYSTEM_ASSIGNED)) {
            throw new IllegalArgumentException("Invalid argument: " + identityType);
        }

        ContainerGroupInner containerGroupInner = this.containerGroup.inner();
        if (containerGroupInner.identity() == null) {
            containerGroupInner.withIdentity(new ContainerGroupIdentity());
        }
        if (containerGroupInner.identity().type() == null
                || containerGroupInner.identity().type().equals(ResourceIdentityType.NONE)
                || containerGroupInner.identity().type().equals(identityType)) {
            containerGroupInner.identity().withType(identityType);
        } else {
            containerGroupInner.identity().withType(ResourceIdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED);
        }
    }
}
