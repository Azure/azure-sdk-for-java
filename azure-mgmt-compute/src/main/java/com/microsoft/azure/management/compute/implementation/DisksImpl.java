/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.AccessLevel;
import com.microsoft.azure.management.compute.Disk;
import com.microsoft.azure.management.compute.Disks;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

/**
 * The implementation for Disks.
 */
@LangDefinition
class DisksImpl
    extends TopLevelModifiableResourcesImpl<
        Disk,
        DiskImpl,
        DiskInner,
        DisksInner,
        ComputeManager>
    implements Disks {

    DisksImpl(ComputeManager computeManager) {
        super(computeManager.inner().disks(), computeManager);
    }

    @Override
    public String grantAccess(String resourceGroupName,
                              String diskName,
                              AccessLevel accessLevel,
                              int accessDuration) {
        return this.grantAccessAsync(resourceGroupName, diskName, accessLevel, accessDuration)
                .toBlocking()
                .last();
    }

    @Override
    public Observable<String> grantAccessAsync(String resourceGroupName, String diskName, AccessLevel accessLevel, int accessDuration) {
        GrantAccessDataInner grantAccessDataInner = new GrantAccessDataInner();
        grantAccessDataInner.withAccess(accessLevel)
                .withDurationInSeconds(accessDuration);
        return this.inner().grantAccessAsync(resourceGroupName, diskName, grantAccessDataInner)
                .map(new Func1<AccessUriInner, String>() {
                    @Override
                    public String call(AccessUriInner accessUriInner) {
                        return accessUriInner.accessSAS();
                    }
                });
    }

    @Override
    public ServiceFuture<String> grantAccessAsync(String resourceGroupName, String diskName, AccessLevel accessLevel, int accessDuration, ServiceCallback<String> callback) {
        return ServiceFuture.fromBody(this.grantAccessAsync(resourceGroupName, diskName, accessLevel, accessDuration), callback);
    }

    @Override
    public void revokeAccess(String resourceGroupName, String diskName) {
        this.inner().revokeAccess(resourceGroupName, diskName);
    }

    @Override
    public Completable revokeAccessAsync(String resourceGroupName, String diskName) {
        return this.inner().revokeAccessAsync(resourceGroupName, diskName).toCompletable();
    }

    @Override
    public ServiceFuture<Void> revokeAccessAsync(String resourceGroupName, String diskName, ServiceCallback<Void> callback) {
        return ServiceFuture.fromBody(this.revokeAccessAsync(resourceGroupName, diskName), callback);
    }

    @Override
    protected DiskImpl wrapModel(String name) {
        return new DiskImpl(name, new DiskInner(), this.manager());
    }

    @Override
    protected DiskImpl wrapModel(DiskInner inner) {
        if (inner == null) {
            return null;
        }
        return new DiskImpl(inner.name(), inner, this.manager());
    }

    @Override
    public Disk.DefinitionStages.Blank define(String name) {
        return this.wrapModel(name);
    }
}