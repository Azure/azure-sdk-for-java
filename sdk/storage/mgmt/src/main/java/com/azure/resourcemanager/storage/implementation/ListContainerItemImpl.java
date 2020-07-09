// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

// package com.azure.management.storage.implementation;
//
// import com.azure.management.storage.ImmutabilityPolicyProperties;
// import com.azure.management.storage.LeaseDuration;
// import com.azure.management.storage.LeaseState;
// import com.azure.management.storage.LeaseStatus;
// import com.azure.management.storage.LegalHoldProperties;
// import com.azure.management.storage.ListContainerItem;
// import com.azure.management.storage.PublicAccess;
// import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
// import org.joda.time.DateTime;
//
// import java.util.Map;
//
// public class ListContainerItemImpl extends WrapperImpl<ListContainerItemInner> implements ListContainerItem {
//
//    private final StorageManager manager;
//
//    ListContainerItemImpl(ListContainerItemInner inner, StorageManager manager) {
//        super(inner);
//        this.manager = manager;
//    }
//
//
//    @Override
//    public PublicAccess publicAccess() {
//        return this.inner().publicAccess();
//    }
//
//    @Override
//    public DateTime lastModifiedTime() {
//        return this.inner().lastModifiedTime();
//    }
//
//    @Override
//    public LeaseStatus leaseStatus() {
//        return this.inner().leaseStatus();
//    }
//
//    @Override
//    public LeaseState leaseState() {
//        return this.inner().leaseState();
//    }
//
//    @Override
//    public LeaseDuration leaseDuration() {
//        return this.inner().leaseDuration();
//    }
//
//    @Override
//    public Map<String, String> metadata() {
//        return this.inner().metadata();
//    }
//
//    @Override
//    public ImmutabilityPolicyProperties immutabilityPolicy() {
//        return this.inner().immutabilityPolicy();
//    }
//
//    @Override
//    public LegalHoldProperties legalHold() {
//        return this.inner().legalHold();
//    }
//
//    @Override
//    public Boolean hasLegalHold() {
//        return this.inner().hasLegalHold();
//    }
//
//    @Override
//    public Boolean hasImmutabilityPolicy() {
//        return this.inner().hasImmutabilityPolicy();
//    }
// }
