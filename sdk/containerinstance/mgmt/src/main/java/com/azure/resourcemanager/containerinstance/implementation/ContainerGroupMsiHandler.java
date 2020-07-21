// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerinstance.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.authorization.implementation.RoleAssignmentHelper;
import com.azure.resourcemanager.containerinstance.fluent.inner.ContainerGroupInner;
import com.azure.resourcemanager.containerinstance.models.ContainerGroupIdentity;
import com.azure.resourcemanager.containerinstance.models.ContainerGroupIdentityUserAssignedIdentities;
import com.azure.resourcemanager.containerinstance.models.ResourceIdentityType;
import com.azure.resourcemanager.msi.models.Identity;
import com.azure.resourcemanager.resources.fluentcore.dag.TaskGroup;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class ContainerGroupMsiHandler extends RoleAssignmentHelper {
    private final ContainerGroupImpl containerGroup;
    private final ClientLogger logger = new ClientLogger(getClass());

    private List<String> creatableIdentityKeys;
    private Map<String, ContainerGroupIdentityUserAssignedIdentities> userAssignedIdentities;

    ContainerGroupMsiHandler(ContainerGroupImpl containerGroup) {
        super(containerGroup.manager().authorizationManager(), containerGroup.taskGroup(), containerGroup.idProvider());
        this.containerGroup = containerGroup;
        this.creatableIdentityKeys = new ArrayList<>();
        this.userAssignedIdentities = new HashMap<>();
    }

    void processCreatedExternalIdentities() {
        for (String key : this.creatableIdentityKeys) {
            Identity identity = (Identity) this.containerGroup.taskGroup().taskResult(key);
            Objects.requireNonNull(identity);
            this.userAssignedIdentities.put(identity.id(), new ContainerGroupIdentityUserAssignedIdentities());
        }
        this.creatableIdentityKeys.clear();
    }

    void handleExternalIdentities() {
        if (!this.userAssignedIdentities.isEmpty()) {
            this.containerGroup.inner().identity().withUserAssignedIdentities(this.userAssignedIdentities);
        }
    }

    /**
     * Specifies that Local Managed Service Identity needs to be enabled in the virtual machine. If MSI extension is not
     * already installed then it will be installed with access token port as 50342.
     *
     * @return ContainerGroupMsiHandler
     */
    ContainerGroupMsiHandler withLocalManagedServiceIdentity() {
        this.initContainerInstanceIdentity(ResourceIdentityType.SYSTEM_ASSIGNED);
        return this;
    }

    /**
     * Specifies that given identity should be set as one of the External Managed Service Identity of the container
     * instance.
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
     * Specifies that given identity should be set as one of the External Managed Service Identity of the container
     * instance.
     *
     * @param identity an identity to associate
     * @return ContainerGroupMsiHandler
     */
    ContainerGroupMsiHandler withExistingExternalManagedServiceIdentity(Identity identity) {
        this.initContainerInstanceIdentity(ResourceIdentityType.USER_ASSIGNED);
        this.userAssignedIdentities.put(identity.id(), new ContainerGroupIdentityUserAssignedIdentities());
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
            throw logger.logExceptionAsError(new IllegalArgumentException("Invalid argument: " + identityType));
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
            containerGroupInner.identity().withType(ResourceIdentityType.SYSTEM_ASSIGNED__USER_ASSIGNED);
        }
    }
}
