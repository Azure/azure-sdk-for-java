// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.guava27.Strings;
import io.netty.buffer.ByteBuf;

import java.util.UUID;

import static com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdConstants.RntbdOperationType;
import static com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdConstants.RntbdResourceType;

final class RntbdRequestFrame {

    // region Fields

    static final int LENGTH = Integer.BYTES  // messageLength
        + Short.BYTES  // resourceType
        + Short.BYTES  // operationType
        + 2 * Long.BYTES;  // activityId

    private final UUID activityId;
    private final RntbdOperationType operationType;
    private final RntbdResourceType resourceType;

    // region Constructors

    RntbdRequestFrame(final UUID activityId, final OperationType operationType, final ResourceType resourceType) {
        this(activityId, map(operationType), map(resourceType));
    }

    RntbdRequestFrame(final UUID activityId, final RntbdOperationType operationType, final RntbdResourceType resourceType) {
        this.activityId = activityId;
        this.operationType = operationType;
        this.resourceType = resourceType;
    }

    // endregion

    // region Methods

    UUID getActivityId() {
        return this.activityId;
    }

    RntbdOperationType getOperationType() {
        return this.operationType;
    }

    RntbdResourceType getResourceType() {
        return this.resourceType;
    }

    static RntbdRequestFrame decode(final ByteBuf in) {

        final RntbdResourceType resourceType = RntbdResourceType.fromId(in.readShortLE());
        final RntbdOperationType operationType = RntbdOperationType.fromId(in.readShortLE());
        final UUID activityId = RntbdUUID.decode(in);

        return new RntbdRequestFrame(activityId, operationType, resourceType);
    }

    void encode(final ByteBuf out) {
        out.writeShortLE(this.resourceType.id());
        out.writeShortLE(this.operationType.id());
        RntbdUUID.encode(this.activityId, out);
    }

    private static RntbdResourceType map(final ResourceType resourceType) {

        switch (resourceType) {
            case Attachment:
                return RntbdResourceType.Attachment;
            case DocumentCollection:
                return RntbdResourceType.Collection;
            case Conflict:
                return RntbdResourceType.Conflict;
            case Database:
                return RntbdResourceType.Database;
            case Document:
                return RntbdResourceType.Document;
            case Module:
                return RntbdResourceType.Module;
            case ModuleCommand:
                return RntbdResourceType.ModuleCommand;
            case Record:
                return RntbdResourceType.Record;
            case Permission:
                return RntbdResourceType.Permission;
            case Replica:
                return RntbdResourceType.Replica;
            case StoredProcedure:
                return RntbdResourceType.StoredProcedure;
            case Trigger:
                return RntbdResourceType.Trigger;
            case User:
                return RntbdResourceType.User;
            case UserDefinedType:
                return RntbdResourceType.UserDefinedType;
            case UserDefinedFunction:
                return RntbdResourceType.UserDefinedFunction;
            case Offer:
                return RntbdResourceType.Offer;
            case PartitionSetInformation:
                return RntbdResourceType.PartitionSetInformation;
            case XPReplicatorAddress:
                return RntbdResourceType.XPReplicatorAddress;
            case MasterPartition:
                return RntbdResourceType.MasterPartition;
            case ServerPartition:
                return RntbdResourceType.ServerPartition;
            case DatabaseAccount:
                return RntbdResourceType.DatabaseAccount;
            case Topology:
                return RntbdResourceType.Topology;
            case PartitionKeyRange:
                return RntbdResourceType.PartitionKeyRange;
            case Schema:
                return RntbdResourceType.Schema;
            case BatchApply:
                return RntbdResourceType.BatchApply;
            case RestoreMetadata:
                return RntbdResourceType.RestoreMetadata;
            case ComputeGatewayCharges:
                return RntbdResourceType.ComputeGatewayCharges;
            case RidRange:
                return RntbdResourceType.RidRange;
            default:
                final String reason = Strings.lenientFormat("Unrecognized resource type: %s", resourceType);
                throw new UnsupportedOperationException(reason);
        }
    }

    private static RntbdOperationType map(final OperationType operationType) {

        switch (operationType) {
            case Crash:
                return RntbdOperationType.Crash;
            case Create:
                return RntbdOperationType.Create;
            case Delete:
                return RntbdOperationType.Delete;
            case ExecuteJavaScript:
                return RntbdOperationType.ExecuteJavaScript;
            case Query:
                return RntbdOperationType.Query;
            case Pause:
                return RntbdOperationType.Pause;
            case Read:
                return RntbdOperationType.Read;
            case ReadFeed:
                return RntbdOperationType.ReadFeed;
            case Recreate:
                return RntbdOperationType.Recreate;
            case Recycle:
                return RntbdOperationType.Recycle;
            case Replace:
                return RntbdOperationType.Replace;
            case Resume:
                return RntbdOperationType.Resume;
            case Stop:
                return RntbdOperationType.Stop;
            case SqlQuery:
                return RntbdOperationType.SqlQuery;
            case Patch:
                return RntbdOperationType.Patch;
            case ForceConfigRefresh:
                return RntbdOperationType.ForceConfigRefresh;
            case Head:
                return RntbdOperationType.Head;
            case HeadFeed:
                return RntbdOperationType.HeadFeed;
            case Upsert:
                return RntbdOperationType.Upsert;
            case Throttle:
                return RntbdOperationType.Throttle;
            case PreCreateValidation:
                return RntbdOperationType.PreCreateValidation;
            case GetSplitPoint:
                return RntbdOperationType.GetSplitPoint;
            case AbortSplit:
                return RntbdOperationType.AbortSplit;
            case CompleteSplit:
                return RntbdOperationType.CompleteSplit;
            case BatchApply:
                return RntbdOperationType.BatchApply;
            case OfferUpdateOperation:
                return RntbdOperationType.OfferUpdateOperation;
            case OfferPreGrowValidation:
                return RntbdOperationType.OfferPreGrowValidation;
            case BatchReportThroughputUtilization:
                return RntbdOperationType.BatchReportThroughputUtilization;
            case AbortPartitionMigration:
                return RntbdOperationType.AbortPartitionMigration;
            case CompletePartitionMigration:
                return RntbdOperationType.CompletePartitionMigration;
            case PreReplaceValidation:
                return RntbdOperationType.PreReplaceValidation;
            case MigratePartition:
                return RntbdOperationType.MigratePartition;
            case AddComputeGatewayRequestCharges:
                return RntbdOperationType.AddComputeGatewayRequestCharges;
            case Batch:
                return RntbdOperationType.Batch;
            default:
                final String reason = Strings.lenientFormat("Unrecognized operation type: %s", operationType);
                throw new UnsupportedOperationException(reason);
        }
    }

    // endregion
}
