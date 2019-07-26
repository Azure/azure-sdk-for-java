// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

/**
 * Resource types in the Azure Cosmos DB database service.
 */
public enum ResourceType {

    // REQUIRED: This enum must be kept in sync with the ResourceType enum in backend native

    Unknown(-1),
    Attachment(3),
    BatchApply(112),
    DocumentCollection(1),
    ComputeGatewayCharges(131),
    Conflict(107),
    Database(0),
    DatabaseAccount(118),
    Document(2),
    Index(104),
    IndexBookmark(105),
    IndexSize(106),
    LargeInvalid(100),
    LogStoreLogs(126),
    MasterPartition(120),
    Module(9),
    ModuleCommand(103),
    Offer(113),
    PartitionKeyRange(125),
    PartitionSetInformation(114),
    Permission(5),
    PreviousImage(128),
    Progress(6),
    Record(108),
    Replica(7),
    RestoreMetadata(127),
    RidRange(130),
    Schema(124),
    SchemaContainer(123),
    ServerPartition(121),
    SmallMaxInvalid(10),
    StoredProcedure(109),
    Timestamp(117),
    Tombstone(8),
    Topology(122),
    Trigger(110),
    User(4),
    UserDefinedFunction(111),
    UserDefinedType(133),
    VectorClock(129),
    XPReplicatorAddress(115),

    // These names make it unclear what they map to in ResourceType.
    Address(-5),
    Key(-2),
    Media(-3),
    ServiceFabricService(-4);

    final private int value;

    ResourceType(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }

    public boolean isCollectionChild() {
        return this == ResourceType.Document ||
                this == ResourceType.Attachment ||
                this == ResourceType.Conflict ||
                this == ResourceType.Schema ||
                this.isScript();
    }

    public boolean isMasterResource() {
        return this == ResourceType.Offer ||
                this == ResourceType.Database ||
                this == ResourceType.User ||
                this == ResourceType.Permission ||
                this == ResourceType.Topology ||
                this == ResourceType.PartitionKeyRange ||
                this == ResourceType.DocumentCollection;
    }

    /// <summary>
    /// Resources for which this method returns true, are spread between multiple partitions
    /// </summary>
    public boolean isPartitioned() {
        return this == ResourceType.Document ||
                this == ResourceType.Attachment ||
                this == ResourceType.Conflict;
    }

    public boolean isScript() {
        return this == ResourceType.UserDefinedFunction ||
                this == ResourceType.Trigger ||
                this == ResourceType.StoredProcedure;
    }

}
