// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.appservice.models.ManagedServiceIdentity;
import com.azure.resourcemanager.appservice.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.appservice.fluent.models.SiteInner;
import com.azure.resourcemanager.appservice.fluent.models.SitePatchResourceInner;
import com.azure.resourcemanager.appservice.models.UserAssignedIdentity;
import com.azure.resourcemanager.appservice.models.WebAppBase;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.authorization.utils.RoleAssignmentHelper;
import com.azure.resourcemanager.msi.models.Identity;
import com.azure.resourcemanager.resources.fluentcore.dag.TaskGroup;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Utility class to set Managed Service Identity (MSI) property on a web app, install or update MSI extension and create
 * role assignments for the service principal associated with the web app.
 */
public class WebAppMsiHandler<FluentT extends WebAppBase, FluentImplT extends WebAppBaseImpl<FluentT, FluentImplT>>
    extends RoleAssignmentHelper {

    private final ClientLogger logger = new ClientLogger(getClass());

    private WebAppBaseImpl<FluentT, FluentImplT> webAppBase;

    private List<String> creatableIdentityKeys;
    private Map<String, UserAssignedIdentity> userAssignedIdentities;

    /**
     * Creates VirtualMachineMsiHandler.
     *
     * @param authorizationManager the graph rbac manager
     * @param webAppBase the web app to which MSI extension needs to be installed and for which role assignments needs
     *     to be created
     */
    WebAppMsiHandler(final AuthorizationManager authorizationManager, WebAppBaseImpl<FluentT, FluentImplT> webAppBase) {
        super(authorizationManager, webAppBase.taskGroup(), webAppBase.idProvider());
        this.webAppBase = webAppBase;
        this.creatableIdentityKeys = new ArrayList<>();
        this.userAssignedIdentities = new HashMap<>();
    }

    /**
     * Specifies that Local Managed Service Identity needs to be enabled in the web app. If MSI extension is not already
     * installed then it will be installed with access token port as 50342.
     *
     * @return WebAppMsiHandler
     */
    WebAppMsiHandler<FluentT, FluentImplT> withLocalManagedServiceIdentity() {
        this.initSiteIdentity(ManagedServiceIdentityType.SYSTEM_ASSIGNED);
        return this;
    }

    /**
     * Specifies that Local Managed Service Identity needs to be disabled in the web app.
     *
     * @return WebAppMsiHandler
     */
    WebAppMsiHandler<FluentT, FluentImplT> withoutLocalManagedServiceIdentity() {
        SiteInner siteInner = this.webAppBase.innerModel();

        if (siteInner.identity() == null
            || siteInner.identity().type() == null
            || siteInner.identity().type().equals(ManagedServiceIdentityType.NONE)
            || siteInner.identity().type().equals(ManagedServiceIdentityType.USER_ASSIGNED)) {
            return this;
        } else if (siteInner.identity().type().equals(ManagedServiceIdentityType.SYSTEM_ASSIGNED)) {
            siteInner.identity().withType(ManagedServiceIdentityType.NONE);
        } else if (siteInner.identity().type().equals(ManagedServiceIdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED)) {
            siteInner.identity().withType(ManagedServiceIdentityType.USER_ASSIGNED);
        }
        return this;
    }

    /**
     * Specifies that given identity should be set as one of the External Managed Service Identity of the web app.
     *
     * @param creatableIdentity yet-to-be-created identity to be associated with the virtual machine
     * @return WebAppMsiHandler
     */
    WebAppMsiHandler<FluentT, FluentImplT> withNewExternalManagedServiceIdentity(
        Creatable<Identity> creatableIdentity) {
        this.initSiteIdentity(ManagedServiceIdentityType.USER_ASSIGNED);

        TaskGroup.HasTaskGroup dependency = (TaskGroup.HasTaskGroup) creatableIdentity;
        Objects.requireNonNull(dependency);

        this.webAppBase.taskGroup().addDependency(dependency);
        this.creatableIdentityKeys.add(creatableIdentity.key());

        return this;
    }

    /**
     * Specifies that given identity should be set as one of the External Managed Service Identity of the web app.
     *
     * @param identity an identity to associate
     * @return WebAppMsiHandler
     */
    WebAppMsiHandler<FluentT, FluentImplT> withExistingExternalManagedServiceIdentity(Identity identity) {
        this.initSiteIdentity(ManagedServiceIdentityType.USER_ASSIGNED);
        this.userAssignedIdentities.put(identity.id(), new UserAssignedIdentity());
        return this;
    }

    /**
     * Specifies that given identity should be removed from the list of External Managed Service Identity associated
     * with the web app.
     *
     * @param identityId resource id of the identity
     * @return WebAppMsiHandler
     */
    WebAppMsiHandler<FluentT, FluentImplT> withoutExternalManagedServiceIdentity(String identityId) {
        this.userAssignedIdentities.put(identityId, null);
        return this;
    }

    void processCreatedExternalIdentities() {
        for (String key : this.creatableIdentityKeys) {
            Identity identity = (Identity) this.webAppBase.taskGroup().taskResult(key);
            Objects.requireNonNull(identity);
            this.userAssignedIdentities.put(identity.id(), new UserAssignedIdentity());
        }
        this.creatableIdentityKeys.clear();
    }

    void handleExternalIdentities() {
        SiteInner siteInner = this.webAppBase.innerModel();
        if (!this.userAssignedIdentities.isEmpty()) {
            siteInner.identity().withUserAssignedIdentities(this.userAssignedIdentities);
        }
    }

    void handleExternalIdentities(SitePatchResourceInner siteUpdate) {
        if (this.handleRemoveAllExternalIdentitiesCase(siteUpdate)) {
            return;
        } else {
            // At this point one of the following condition is met:
            //
            // 1. User don't want touch the 'Site.Identity.userAssignedIdentities' property
            //      [this.userAssignedIdentities.empty() == true]
            // 2. User want to add some identities to 'Site.Identity.userAssignedIdentities'
            //      [this.userAssignedIdentities.empty() == false and this.webAppBase.inner().identity() != null]
            // 3. User want to remove some (not all) identities in 'Site.Identity.userAssignedIdentities'
            //      [this.userAssignedIdentities.empty() == false and this.webAppBase.inner().identity() != null]
            //      Note: The scenario where this.webAppBase.inner().identity() is null in #3 is already handled in
            //      handleRemoveAllExternalIdentitiesCase method
            // 4. User want to add and remove (all or subset) some identities in 'Site.Identity.userAssignedIdentities'
            //      [this.userAssignedIdentities.empty() == false and this.webAppBase.inner().identity() != null]
            //
            SiteInner siteInner = this.webAppBase.innerModel();
            ManagedServiceIdentity currentIdentity = siteInner.identity();
            siteUpdate.withIdentity(currentIdentity);
            if (!this.userAssignedIdentities.isEmpty()) {
                // At this point its guaranteed that 'currentIdentity' is not null so vmUpdate.identity() is.
                siteUpdate.identity().withUserAssignedIdentities(this.userAssignedIdentities);
            } else {
                // User don't want to touch 'VM.Identity.userAssignedIdentities' property
                if (currentIdentity != null) {
                    // and currently there is identity exists or user want to manipulate some other properties of
                    // identity, set identities to null so that it won't send over wire.
                    currentIdentity.withUserAssignedIdentities(null);
                }
            }
        }
    }

    /** Clear VirtualMachineMsiHandler post-run specific internal state. */
    void clear() {
        this.userAssignedIdentities = new HashMap<>();
    }

    /**
     * Method that handle the case where user request indicates all it want to do is remove all identities associated
     * with the virtual machine.
     *
     * @param siteUpdate the vm update payload model
     * @return true if user indented to remove all the identities.
     */
    private boolean handleRemoveAllExternalIdentitiesCase(SitePatchResourceInner siteUpdate) {
        SiteInner siteInner = this.webAppBase.innerModel();
        if (!this.userAssignedIdentities.isEmpty()) {
            int rmCount = 0;
            for (UserAssignedIdentity v : this.userAssignedIdentities.values()) {
                if (v == null) {
                    rmCount++;
                } else {
                    break;
                }
            }
            boolean containsRemoveOnly = rmCount > 0 && rmCount == this.userAssignedIdentities.size();
            // Check if user request contains only request for removal of identities.
            if (containsRemoveOnly) {
                Set<String> currentIds = new HashSet<>();
                ManagedServiceIdentity currentIdentity = siteInner.identity();
                if (currentIdentity != null && currentIdentity.userAssignedIdentities() != null) {
                    for (String id : currentIdentity.userAssignedIdentities().keySet()) {
                        currentIds.add(id.toLowerCase(Locale.ROOT));
                    }
                }
                Set<String> removeIds = new HashSet<>();
                for (Map.Entry<String, UserAssignedIdentity> entrySet
                    : this.userAssignedIdentities.entrySet()) {
                    if (entrySet.getValue() == null) {
                        removeIds.add(entrySet.getKey().toLowerCase(Locale.ROOT));
                    }
                }
                // If so check user want to remove all the identities
                boolean removeAllCurrentIds =
                    currentIds.size() == removeIds.size() && currentIds.containsAll(removeIds);
                if (removeAllCurrentIds) {
                    // If so adjust  the identity type [Setting type to SYSTEM_ASSIGNED orNONE will remove all the
                    // identities]
                    if (currentIdentity == null || currentIdentity.type() == null) {
                        siteUpdate.withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.NONE));
                    } else if (currentIdentity
                        .type()
                        .equals(ManagedServiceIdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED)) {
                        siteUpdate.withIdentity(currentIdentity);
                        siteUpdate.identity().withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED);
                    } else if (currentIdentity.type().equals(ManagedServiceIdentityType.USER_ASSIGNED)) {
                        siteUpdate.withIdentity(currentIdentity);
                        siteUpdate.identity().withType(ManagedServiceIdentityType.NONE);
                    }
                    // and set identities property in the payload model to null so that it won't be sent
                    siteUpdate.identity().withUserAssignedIdentities(null);
                    return true;
                } else {
                    // Check user is asking to remove identities though there is no identities currently associated
                    if (currentIds.size() == 0 && removeIds.size() != 0 && currentIdentity == null) {
                        // If so we are in a invalid state but we want to send user input to service and let service
                        // handle it (ignore or error).
                        siteUpdate.withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.NONE));
                        siteUpdate.identity().withUserAssignedIdentities(null);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Initialize VM's identity property.
     *
     * @param identityType the identity type to set
     */
    private void initSiteIdentity(ManagedServiceIdentityType identityType) {
        if (!identityType.equals(ManagedServiceIdentityType.USER_ASSIGNED)
            && !identityType.equals(ManagedServiceIdentityType.SYSTEM_ASSIGNED)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Invalid argument: " + identityType));
        }

        SiteInner siteInner = this.webAppBase.innerModel();
        if (siteInner.identity() == null) {
            siteInner.withIdentity(new ManagedServiceIdentity());
        }
        if (siteInner.identity().type() == null
            || siteInner.identity().type().equals(ManagedServiceIdentityType.NONE)
            || siteInner.identity().type().equals(identityType)) {
            siteInner.identity().withType(identityType);
        } else {
            siteInner.identity().withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED);
        }
    }
}
