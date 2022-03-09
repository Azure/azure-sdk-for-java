// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.appservice.models.ManagedServiceIdentity;
import com.azure.resourcemanager.appservice.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.appservice.fluent.models.SiteInner;
import com.azure.resourcemanager.appservice.fluent.models.SitePatchResourceInner;
import com.azure.resourcemanager.appservice.models.WebAppBase;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.authorization.utils.RoleAssignmentHelper;

/**
 * Utility class to set Managed Service Identity (MSI) property on a web app, install or update MSI extension and create
 * role assignments for the service principal associated with the web app.
 */
public class WebAppMsiHandler<FluentT extends WebAppBase, FluentImplT extends WebAppBaseImpl<FluentT, FluentImplT>>
    extends RoleAssignmentHelper {

    private final ClientLogger logger = new ClientLogger(getClass());

    private WebAppBaseImpl<FluentT, FluentImplT> webAppBase;

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

    void handleExternalIdentities(SitePatchResourceInner siteUpdate) {
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

        // User don't want to touch 'VM.Identity.userAssignedIdentities' property
        if (currentIdentity != null) {
            // and currently there is identity exists or user want to manipulate some other properties of
            // identity, set identities to null so that it won't send over wire.
            currentIdentity.withUserAssignedIdentities(null);
        }
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
