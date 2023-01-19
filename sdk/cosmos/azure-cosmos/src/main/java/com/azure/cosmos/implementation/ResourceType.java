// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import java.util.Locale;

/**
 * Resource types in the Azure Cosmos DB database service.
 */
public enum ResourceType {

    // REQUIRED: This enum must be kept in sync with the ResourceType enum in backend native

    Unknown("Unknown", -1),
    Attachment("Attachment", 3),
    BatchApply("BatchApply", 112),
    DocumentCollection("DocumentCollection", 1),
    ComputeGatewayCharges("ComputeGatewayCharges", 131),
    Conflict("Conflict", 107),
    Database("Database", 0),
    DatabaseAccount("DatabaseAccount", 118),
    Document("Document", 2),
    Index("Index", 104),
    IndexBookmark("IndexBookmark", 105),
    IndexSize("IndexSize", 106),
    LargeInvalid("LargeInvalid", 100),
    LogStoreLogs("LogStoreLogs", 126),
    MasterPartition("MasterPartition", 120),
    Module("Module", 9),
    ModuleCommand("ModuleCommand", 103),
    Offer("Offer", 113),
    PartitionKeyRange("PartitionKeyRange", 125),
    PartitionKey("PartitionKey", 136),
    PartitionSetInformation("PartitionSetInformation", 114),
    Permission("Permission", 5),
    PreviousImage("PreviousImage", 128),
    Progress("Progress", 6),
    Record("Record", 108),
    Replica("Replica", 7),
    RestoreMetadata("RestoreMetadata", 127),
    RidRange("RidRange", 130),
    Schema("Schema", 124),
    SchemaContainer("SchemaContainer", 123),
    ServerPartition("ServerPartition", 121),
    SmallMaxInvalid("SmallMaxInvalid", 10),
    StoredProcedure("StoredProcedure", 109),
    Timestamp("Timestamp", 117),
    Tombstone("Tombstone", 8),
    Topology("Topology", 122),
    Trigger("Trigger", 110),
    User("User", 4),
    UserDefinedFunction("UserDefinedFunction", 111),
    UserDefinedType("UserDefinedType", 133),
    VectorClock("VectorClock", 129),
    XPReplicatorAddress("XPReplicatorAddress", 115),

    // These names make it unclear what they map to in ResourceType.
    Address("Address", -5),
    Key("Key", -2),
    Media("Media", -3),
    ServiceFabricService("ServiceFabricService", -4),

    ClientEncryptionKey("ClientEncryptionKey", 141),

    //Adding client telemetry resource type, only meant for client side
    ClientTelemetry("ClientTelemetry", 1001);

    private final int value;
    private final String stringValue;
    private final String toLowerStringValue;

    ResourceType(String stringValue, int value) {
        this.stringValue = stringValue;
        this.value = value;
        this.toLowerStringValue = stringValue.toLowerCase(Locale.ROOT);
    }

    @Override
    public String toString() {
        return this.stringValue;
    }

    public String toLowerCase() {
        return this.toLowerStringValue;
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
                this == ResourceType.DocumentCollection ||
                this == ResourceType.ClientEncryptionKey;
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
