// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.TcpServerMock.rntbd;

import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdConstants;
import com.azure.cosmos.implementation.guava25.collect.ImmutableMap;
import com.azure.cosmos.implementation.guava25.collect.ImmutableSet;
import com.azure.cosmos.implementation.guava25.collect.Sets;
import io.netty.handler.codec.DecoderException;

import java.util.EnumSet;
import java.util.stream.Collector;

import static com.azure.cosmos.implementation.guava27.Strings.lenientFormat;

/**
 * Methods included in this class are copied from {@link RntbdConstants}.
 */
public class ServerRntbdConstants {

    static final int CURRENT_PROTOCOL_VERSION = 0x00000001;

    private ServerRntbdConstants() {
    }

    enum RntbdIndexingDirective {

        Default((byte) 0x00),
        Include((byte) 0x01),
        Exclude((byte) 0x02),
        Invalid((byte) 0xFF);

        private final byte id;

        RntbdIndexingDirective(final byte id) {
            this.id = id;
        }

        public static RntbdIndexingDirective fromId(final byte id) {
            switch (id) {
                case (byte) 0x00:
                    return Default;
                case (byte) 0x01:
                    return Include;
                case (byte) 0x02:
                    return Exclude;
                case (byte) 0xFF:
                    return Invalid;
            }
            throw new IllegalArgumentException("id");
        }

        public byte id() {
            return this.id;
        }
    }

    public enum RntbdOperationType {

        Connection((short) 0x0000, null),
        Create((short) 0x0001, OperationType.Create),
        Patch((short) 0x0002, OperationType.Patch),
        Read((short) 0x0003, OperationType.Read),
        ReadFeed((short) 0x0004, OperationType.ReadFeed),
        Delete((short) 0x0005, OperationType.Delete),
        Replace((short) 0x0006, OperationType.Replace),
        // Obsolete and now undefined: JPathQuery((short)0x0007),
        ExecuteJavaScript((short) 0x0008, OperationType.ExecuteJavaScript),
        SqlQuery((short) 0x0009, OperationType.SqlQuery),
        Pause((short) 0x000A, OperationType.Pause),
        Resume((short) 0x000B, OperationType.Resume),
        Stop((short) 0x000C, OperationType.Stop),
        Recycle((short) 0x000D, OperationType.Recycle),
        Crash((short) 0x000E, OperationType.Crash),
        Query((short) 0x000F, OperationType.Query),
        ForceConfigRefresh((short) 0x0010, OperationType.ForceConfigRefresh),
        Head((short) 0x0011, OperationType.Head),
        HeadFeed((short) 0x0012, OperationType.HeadFeed),
        Upsert((short) 0x0013, OperationType.Upsert),
        Recreate((short) 0x0014, OperationType.Recreate),
        Throttle((short) 0x0015, OperationType.Throttle),
        GetSplitPoint((short) 0x0016, OperationType.GetSplitPoint),
        PreCreateValidation((short) 0x0017, OperationType.PreCreateValidation),
        BatchApply((short) 0x0018, OperationType.BatchApply),
        AbortSplit((short) 0x0019, OperationType.AbortSplit),
        CompleteSplit((short) 0x001A, OperationType.CompleteSplit),
        OfferUpdateOperation((short) 0x001B, OperationType.OfferUpdateOperation),
        OfferPreGrowValidation((short) 0x001C, OperationType.OfferPreGrowValidation),
        BatchReportThroughputUtilization((short) 0x001D, OperationType.BatchReportThroughputUtilization),
        CompletePartitionMigration((short) 0x001E, OperationType.CompletePartitionMigration),
        AbortPartitionMigration((short) 0x001F, OperationType.AbortPartitionMigration),
        PreReplaceValidation((short) 0x0020, OperationType.PreReplaceValidation),
        AddComputeGatewayRequestCharges((short) 0x0021, OperationType.AddComputeGatewayRequestCharges),
        MigratePartition((short) 0x0022, OperationType.MigratePartition),
        Batch((short) 0x0025, OperationType.Batch);

        private final short id;
        private final OperationType type;

        RntbdOperationType(final short id, final OperationType type) {
            this.id = id;
            this.type = type;
        }

        public static ServerRntbdConstants.RntbdOperationType fromId(final short id) {

            switch (id) {
                case 0x0000:
                    return ServerRntbdConstants.RntbdOperationType.Connection;
                case 0x0001:
                    return ServerRntbdConstants.RntbdOperationType.Create;
                case 0x0002:
                    return ServerRntbdConstants.RntbdOperationType.Patch;
                case 0x0003:
                    return ServerRntbdConstants.RntbdOperationType.Read;
                case 0x0004:
                    return ServerRntbdConstants.RntbdOperationType.ReadFeed;
                case 0x0005:
                    return ServerRntbdConstants.RntbdOperationType.Delete;
                case 0x0006:
                    return ServerRntbdConstants.RntbdOperationType.Replace;
                // Obsolete and now undefined: case 0x0007: return RntbdOperationType.JPathQuery;
                case 0x0008:
                    return ServerRntbdConstants.RntbdOperationType.ExecuteJavaScript;
                case 0x0009:
                    return ServerRntbdConstants.RntbdOperationType.SqlQuery;
                case 0x000A:
                    return ServerRntbdConstants.RntbdOperationType.Pause;
                case 0x000B:
                    return ServerRntbdConstants.RntbdOperationType.Resume;
                case 0x000C:
                    return ServerRntbdConstants.RntbdOperationType.Stop;
                case 0x000D:
                    return ServerRntbdConstants.RntbdOperationType.Recycle;
                case 0x000E:
                    return ServerRntbdConstants.RntbdOperationType.Crash;
                case 0x000F:
                    return ServerRntbdConstants.RntbdOperationType.Query;
                case 0x0010:
                    return ServerRntbdConstants.RntbdOperationType.ForceConfigRefresh;
                case 0x0011:
                    return ServerRntbdConstants.RntbdOperationType.Head;
                case 0x0012:
                    return ServerRntbdConstants.RntbdOperationType.HeadFeed;
                case 0x0013:
                    return ServerRntbdConstants.RntbdOperationType.Upsert;
                case 0x0014:
                    return ServerRntbdConstants.RntbdOperationType.Recreate;
                case 0x0015:
                    return ServerRntbdConstants.RntbdOperationType.Throttle;
                case 0x0016:
                    return ServerRntbdConstants.RntbdOperationType.GetSplitPoint;
                case 0x0017:
                    return ServerRntbdConstants.RntbdOperationType.PreCreateValidation;
                case 0x0018:
                    return ServerRntbdConstants.RntbdOperationType.BatchApply;
                case 0x0019:
                    return ServerRntbdConstants.RntbdOperationType.AbortSplit;
                case 0x001A:
                    return ServerRntbdConstants.RntbdOperationType.CompleteSplit;
                case 0x001B:
                    return ServerRntbdConstants.RntbdOperationType.OfferUpdateOperation;
                case 0x001C:
                    return ServerRntbdConstants.RntbdOperationType.OfferPreGrowValidation;
                case 0x001D:
                    return ServerRntbdConstants.RntbdOperationType.BatchReportThroughputUtilization;
                case 0x001E:
                    return ServerRntbdConstants.RntbdOperationType.CompletePartitionMigration;
                case 0x001F:
                    return ServerRntbdConstants.RntbdOperationType.AbortPartitionMigration;
                case 0x0020:
                    return ServerRntbdConstants.RntbdOperationType.PreReplaceValidation;
                case 0x0021:
                    return ServerRntbdConstants.RntbdOperationType.AddComputeGatewayRequestCharges;
                case 0x0022:
                    return ServerRntbdConstants.RntbdOperationType.MigratePartition;
                case 0x0025:
                    return ServerRntbdConstants.RntbdOperationType.Batch;
                default:
                    throw new DecoderException(lenientFormat("expected byte value matching %s value, not %s",
                        RntbdConstants.RntbdOperationType.class.getSimpleName(),
                        id));
            }
        }

        public static RntbdConstants.RntbdOperationType fromType(OperationType type) {
            switch (type) {
                case Crash:
                    return RntbdConstants.RntbdOperationType.Crash;
                case Create:
                    return RntbdConstants.RntbdOperationType.Create;
                case Delete:
                    return RntbdConstants.RntbdOperationType.Delete;
                case ExecuteJavaScript:
                    return RntbdConstants.RntbdOperationType.ExecuteJavaScript;
                case Query:
                    return RntbdConstants.RntbdOperationType.Query;
                case Pause:
                    return RntbdConstants.RntbdOperationType.Pause;
                case Read:
                    return RntbdConstants.RntbdOperationType.Read;
                case ReadFeed:
                    return RntbdConstants.RntbdOperationType.ReadFeed;
                case Recreate:
                    return RntbdConstants.RntbdOperationType.Recreate;
                case Recycle:
                    return RntbdConstants.RntbdOperationType.Recycle;
                case Replace:
                    return RntbdConstants.RntbdOperationType.Replace;
                case Resume:
                    return RntbdConstants.RntbdOperationType.Resume;
                case Stop:
                    return RntbdConstants.RntbdOperationType.Stop;
                case SqlQuery:
                    return RntbdConstants.RntbdOperationType.SqlQuery;
                case Patch:
                    return RntbdConstants.RntbdOperationType.Patch;
                case ForceConfigRefresh:
                    return RntbdConstants.RntbdOperationType.ForceConfigRefresh;
                case Head:
                    return RntbdConstants.RntbdOperationType.Head;
                case HeadFeed:
                    return RntbdConstants.RntbdOperationType.HeadFeed;
                case Upsert:
                    return RntbdConstants.RntbdOperationType.Upsert;
                case Throttle:
                    return RntbdConstants.RntbdOperationType.Throttle;
                case PreCreateValidation:
                    return RntbdConstants.RntbdOperationType.PreCreateValidation;
                case GetSplitPoint:
                    return RntbdConstants.RntbdOperationType.GetSplitPoint;
                case AbortSplit:
                    return RntbdConstants.RntbdOperationType.AbortSplit;
                case CompleteSplit:
                    return RntbdConstants.RntbdOperationType.CompleteSplit;
                case BatchApply:
                    return RntbdConstants.RntbdOperationType.BatchApply;
                case OfferUpdateOperation:
                    return RntbdConstants.RntbdOperationType.OfferUpdateOperation;
                case OfferPreGrowValidation:
                    return RntbdConstants.RntbdOperationType.OfferPreGrowValidation;
                case BatchReportThroughputUtilization:
                    return RntbdConstants.RntbdOperationType.BatchReportThroughputUtilization;
                case AbortPartitionMigration:
                    return RntbdConstants.RntbdOperationType.AbortPartitionMigration;
                case CompletePartitionMigration:
                    return RntbdConstants.RntbdOperationType.CompletePartitionMigration;
                case PreReplaceValidation:
                    return RntbdConstants.RntbdOperationType.PreReplaceValidation;
                case MigratePartition:
                    return RntbdConstants.RntbdOperationType.MigratePartition;
                case AddComputeGatewayRequestCharges:
                    return RntbdConstants.RntbdOperationType.AddComputeGatewayRequestCharges;
                case Batch:
                    return RntbdConstants.RntbdOperationType.Batch;
                default:
                    throw new IllegalArgumentException(lenientFormat("unrecognized operation type: %s", type));
            }
        }

        public short id() {
            return this.id;
        }

        public OperationType type() {
            return this.type;
        }
    }

    public enum RntbdReadFeedKeyType {

        Invalid((byte) 0x00),
        ResourceId((byte) 0x01),
        EffectivePartitionKey((byte) 0x02);

        private final byte id;

        RntbdReadFeedKeyType(final byte id) {
            this.id = id;
        }

        public byte id() {
            return this.id;
        }
    }

    public enum RntbdRemoteStorageType {

        Invalid((byte) 0x00),
        NotSpecified((byte) 0x01),
        Standard((byte) 0x02),
        Premium((byte) 0x03);

        private final byte id;

        RntbdRemoteStorageType(final byte id) {
            this.id = id;
        }

        public byte id() {
            return this.id;
        }
    }

    public enum RntbdRequestHeader implements RntbdHeader {

        ResourceId((short) 0x0000, ServerRntbdTokenType.Bytes, false),
        AuthorizationToken((short) 0x0001, ServerRntbdTokenType.String, false),
        PayloadPresent((short) 0x0002, ServerRntbdTokenType.Byte, true),
        Date((short) 0x0003, ServerRntbdTokenType.SmallString, false),
        PageSize((short) 0x0004, ServerRntbdTokenType.ULong, false),
        SessionToken((short) 0x0005, ServerRntbdTokenType.String, false),
        ContinuationToken((short) 0x0006, ServerRntbdTokenType.String, false),
        IndexingDirective((short) 0x0007, ServerRntbdTokenType.Byte, false),
        Match((short) 0x0008, ServerRntbdTokenType.String, false),
        PreTriggerInclude((short) 0x0009, ServerRntbdTokenType.String, false),
        PostTriggerInclude((short) 0x000A, ServerRntbdTokenType.String, false),
        IsFanout((short) 0x000B, ServerRntbdTokenType.Byte, false),
        CollectionPartitionIndex((short) 0x000C, ServerRntbdTokenType.ULong, false),
        CollectionServiceIndex((short) 0x000D, ServerRntbdTokenType.ULong, false),
        PreTriggerExclude((short) 0x000E, ServerRntbdTokenType.String, false),
        PostTriggerExclude((short) 0x000F, ServerRntbdTokenType.String, false),
        ConsistencyLevel((short) 0x0010, ServerRntbdTokenType.Byte, false),
        EntityId((short) 0x0011, ServerRntbdTokenType.String, false),
        ResourceSchemaName((short) 0x0012, ServerRntbdTokenType.SmallString, false),
        ReplicaPath((short) 0x0013, ServerRntbdTokenType.String, true),
        ResourceTokenExpiry((short) 0x0014, ServerRntbdTokenType.ULong, false),
        DatabaseName((short) 0x0015, ServerRntbdTokenType.String, false),
        CollectionName((short) 0x0016, ServerRntbdTokenType.String, false),
        DocumentName((short) 0x0017, ServerRntbdTokenType.String, false),
        AttachmentName((short) 0x0018, ServerRntbdTokenType.String, false),
        UserName((short) 0x0019, ServerRntbdTokenType.String, false),
        PermissionName((short) 0x001A, ServerRntbdTokenType.String, false),
        StoredProcedureName((short) 0x001B, ServerRntbdTokenType.String, false),
        UserDefinedFunctionName((short) 0x001C, ServerRntbdTokenType.String, false),
        TriggerName((short) 0x001D, ServerRntbdTokenType.String, false),
        EnableScanInQuery((short) 0x001E, ServerRntbdTokenType.Byte, false),
        EmitVerboseTracesInQuery((short) 0x001F, ServerRntbdTokenType.Byte, false),
        ConflictName((short) 0x0020, ServerRntbdTokenType.String, false),
        BindReplicaDirective((short) 0x0021, ServerRntbdTokenType.String, false),
        PrimaryMasterKey((short) 0x0022, ServerRntbdTokenType.String, false),
        SecondaryMasterKey((short) 0x0023, ServerRntbdTokenType.String, false),
        PrimaryReadonlyKey((short) 0x0024, ServerRntbdTokenType.String, false),
        SecondaryReadonlyKey((short) 0x0025, ServerRntbdTokenType.String, false),
        ProfileRequest((short) 0x0026, ServerRntbdTokenType.Byte, false),
        EnableLowPrecisionOrderBy((short) 0x0027, ServerRntbdTokenType.Byte, false),
        ClientVersion((short) 0x0028, ServerRntbdTokenType.SmallString, false),
        CanCharge((short) 0x0029, ServerRntbdTokenType.Byte, false),
        CanThrottle((short) 0x002A, ServerRntbdTokenType.Byte, false),
        PartitionKey((short) 0x002B, ServerRntbdTokenType.String, false),
        PartitionKeyRangeId((short) 0x002C, ServerRntbdTokenType.String, false),
        NotUsed2D((short) 0x002D, ServerRntbdTokenType.Invalid, false),
        NotUsed2E((short) 0x002E, ServerRntbdTokenType.Invalid, false),
        NotUsed2F((short) 0x002F, ServerRntbdTokenType.Invalid, false),
        // not used 0x0030,
        MigrateCollectionDirective((short) 0x0031, ServerRntbdTokenType.Byte, false),
        NotUsed32((short) 0x0032, ServerRntbdTokenType.Invalid, false),
        SupportSpatialLegacyCoordinates((short) 0x0033, ServerRntbdTokenType.Byte, false),
        PartitionCount((short) 0x0034, ServerRntbdTokenType.ULong, false),
        CollectionRid((short) 0x0035, ServerRntbdTokenType.String, false),
        PartitionKeyRangeName((short) 0x0036, ServerRntbdTokenType.String, false),
        // not used((short)0x0037), RoundTripTimeInMsec
        // not used((short)0x0038), RequestMessageSentTime
        // not used((short)0x0039), RequestMessageTimeOffset
        SchemaName((short) 0x003A, ServerRntbdTokenType.String, false),
        FilterBySchemaRid((short) 0x003B, ServerRntbdTokenType.String, false),
        UsePolygonsSmallerThanAHemisphere((short) 0x003C, ServerRntbdTokenType.Byte, false),
        GatewaySignature((short) 0x003D, ServerRntbdTokenType.String, false),
        EnableLogging((short) 0x003E, ServerRntbdTokenType.Byte, false),
        A_IM((short) 0x003F, ServerRntbdTokenType.String, false),
        PopulateQuotaInfo((short) 0x0040, ServerRntbdTokenType.Byte, false),
        DisableRUPerMinuteUsage((short) 0x0041, ServerRntbdTokenType.Byte, false),
        PopulateQueryMetrics((short) 0x0042, ServerRntbdTokenType.Byte, false),
        ResponseContinuationTokenLimitInKb((short) 0x0043, ServerRntbdTokenType.ULong, false),
        PopulatePartitionStatistics((short) 0x0044, ServerRntbdTokenType.Byte, false),
        RemoteStorageType((short) 0x0045, ServerRntbdTokenType.Byte, false),
        CollectionRemoteStorageSecurityIdentifier((short) 0x0046, ServerRntbdTokenType.String, false),
        IfModifiedSince((short) 0x0047, ServerRntbdTokenType.String, false),
        PopulateCollectionThroughputInfo((short) 0x0048, ServerRntbdTokenType.Byte, false),
        RemainingTimeInMsOnClientRequest((short) 0x0049, ServerRntbdTokenType.ULong, false),
        ClientRetryAttemptCount((short) 0x004A, ServerRntbdTokenType.ULong, false),
        TargetLsn((short) 0x004B, ServerRntbdTokenType.LongLong, false),
        TargetGlobalCommittedLsn((short) 0x004C, ServerRntbdTokenType.LongLong, false),
        TransportRequestID((short) 0x004D, ServerRntbdTokenType.ULong, false),
        RestoreMetadaFilter((short) 0x004E, ServerRntbdTokenType.String, false),
        RestoreParams((short) 0x004F, ServerRntbdTokenType.String, false),
        ShareThroughput((short) 0x0050, ServerRntbdTokenType.Byte, false),
        PartitionResourceFilter((short) 0x0051, ServerRntbdTokenType.String, false),
        IsReadOnlyScript((short) 0x0052, ServerRntbdTokenType.Byte, false),
        IsAutoScaleRequest((short) 0x0053, ServerRntbdTokenType.Byte, false),
        ForceQueryScan((short) 0x0054, ServerRntbdTokenType.Byte, false),
        // not used((short)0x0055), LeaseSeqNumber
        CanOfferReplaceComplete((short) 0x0056, ServerRntbdTokenType.Byte, false),
        ExcludeSystemProperties((short) 0x0057, ServerRntbdTokenType.Byte, false),
        BinaryId((short) 0x0058, ServerRntbdTokenType.Bytes, false),
        TimeToLiveInSeconds((short) 0x0059, ServerRntbdTokenType.Long, false),
        EffectivePartitionKey((short) 0x005A, ServerRntbdTokenType.Bytes, false),
        BinaryPassthroughRequest((short) 0x005B, ServerRntbdTokenType.Byte, false),
        UserDefinedTypeName((short) 0x005C, ServerRntbdTokenType.String, false),
        EnableDynamicRidRangeAllocation((short) 0x005D, ServerRntbdTokenType.Byte, false),
        EnumerationDirection((short) 0x005E, ServerRntbdTokenType.Byte, false),
        StartId((short) 0x005F, ServerRntbdTokenType.Bytes, false),
        EndId((short) 0x0060, ServerRntbdTokenType.Bytes, false),
        FanoutOperationState((short) 0x0061, ServerRntbdTokenType.Byte, false),
        StartEpk((short) 0x0062, ServerRntbdTokenType.Bytes, false),
        EndEpk((short) 0x0063, ServerRntbdTokenType.Bytes, false),
        ReadFeedKeyType((short) 0x0064, ServerRntbdTokenType.Byte, false),
        ContentSerializationFormat((short) 0x0065, ServerRntbdTokenType.Byte, false),
        AllowTentativeWrites((short) 0x0066, ServerRntbdTokenType.Byte, false),
        IsUserRequest((short) 0x0067, ServerRntbdTokenType.Byte, false),
        SharedOfferThroughput((short) 0x0068, ServerRntbdTokenType.ULong, false),
        IsBatchAtomic((short) 0x0073, ServerRntbdTokenType.Byte, false),
        ShouldBatchContinueOnError((short) 0x0074, ServerRntbdTokenType.Byte, false),
        IsBatchOrdered((short) 0x0075, ServerRntbdTokenType.Byte, false),
        ReturnPreference((short) 0x0082, ServerRntbdTokenType.Byte, false);

        public static final ImmutableMap<Short, RntbdRequestHeader> map;
        public static final ImmutableSet<RntbdRequestHeader> set = Sets.immutableEnumSet(EnumSet.allOf(RntbdRequestHeader.class));

        static {
            final Collector<RntbdRequestHeader, ?, ImmutableMap<Short, RntbdRequestHeader>> collector = ImmutableMap.toImmutableMap(RntbdRequestHeader::id, h -> h);
            map = set.stream().collect(collector);
        }

        private final short id;
        private final boolean isRequired;
        private final ServerRntbdTokenType type;

        RntbdRequestHeader(final short id, final ServerRntbdTokenType type, final boolean isRequired) {
            this.id = id;
            this.type = type;
            this.isRequired = isRequired;
        }

        public boolean isRequired() {
            return this.isRequired;
        }

        public short id() {
            return this.id;
        }

        public ServerRntbdTokenType type() {
            return this.type;
        }
    }

    public enum RntbdResourceType {

        Connection((short) 0x0000, null),
        Database((short) 0x0001, ResourceType.Database),
        Collection((short) 0x0002, ResourceType.DocumentCollection),
        Document((short) 0x0003, ResourceType.Document),
        Attachment((short) 0x0004, ResourceType.Attachment),
        User((short) 0x0005, ResourceType.User),
        Permission((short) 0x0006, ResourceType.Permission),
        StoredProcedure((short) 0x0007, ResourceType.StoredProcedure),
        Conflict((short) 0x0008, ResourceType.Conflict),
        Trigger((short) 0x0009, ResourceType.Trigger),
        UserDefinedFunction((short) 0x000A, ResourceType.UserDefinedFunction),
        Module((short) 0x000B, ResourceType.Module),
        Replica((short) 0x000C, ResourceType.Replica),
        ModuleCommand((short) 0x000D, ResourceType.ModuleCommand),
        Record((short) 0x000E, ResourceType.Record),
        Offer((short) 0x000F, ResourceType.Offer),
        PartitionSetInformation((short) 0x0010, ResourceType.PartitionSetInformation),
        XPReplicatorAddress((short) 0x0011, ResourceType.XPReplicatorAddress),
        MasterPartition((short) 0x0012, ResourceType.MasterPartition),
        ServerPartition((short) 0x0013, ResourceType.ServerPartition),
        DatabaseAccount((short) 0x0014, ResourceType.DatabaseAccount),
        Topology((short) 0x0015, ResourceType.Topology),
        PartitionKeyRange((short) 0x0016, ResourceType.PartitionKeyRange),
        // Obsolete and now undefined: Timestamp((short)0x0017),
        Schema((short) 0x0018, ResourceType.Schema),
        BatchApply((short) 0x0019, ResourceType.BatchApply),
        RestoreMetadata((short) 0x001A, ResourceType.RestoreMetadata),
        ComputeGatewayCharges((short) 0x001B, ResourceType.ComputeGatewayCharges),
        RidRange((short) 0x001C, ResourceType.RidRange),
        UserDefinedType((short) 0x001D, ResourceType.UserDefinedType);

        private final short id;
        private final ResourceType type;

        RntbdResourceType(final short id, final ResourceType type) {
            this.id = id;
            this.type = type;
        }

        public static ServerRntbdConstants.RntbdResourceType fromId(final short id) throws IllegalArgumentException {
            switch (id) {
                case 0x0000:
                    return ServerRntbdConstants.RntbdResourceType.Connection;
                case 0x0001:
                    return ServerRntbdConstants.RntbdResourceType.Database;
                case 0x0002:
                    return ServerRntbdConstants.RntbdResourceType.Collection;
                case 0x0003:
                    return ServerRntbdConstants.RntbdResourceType.Document;
                case 0x0004:
                    return ServerRntbdConstants.RntbdResourceType.Attachment;
                case 0x0005:
                    return ServerRntbdConstants.RntbdResourceType.User;
                case 0x0006:
                    return ServerRntbdConstants.RntbdResourceType.Permission;
                case 0x0007:
                    return ServerRntbdConstants.RntbdResourceType.StoredProcedure;
                case 0x0008:
                    return ServerRntbdConstants.RntbdResourceType.Conflict;
                case 0x0009:
                    return ServerRntbdConstants.RntbdResourceType.Trigger;
                case 0x000A:
                    return ServerRntbdConstants.RntbdResourceType.UserDefinedFunction;
                case 0x000B:
                    return ServerRntbdConstants.RntbdResourceType.Module;
                case 0x000C:
                    return ServerRntbdConstants.RntbdResourceType.Replica;
                case 0x000D:
                    return ServerRntbdConstants.RntbdResourceType.ModuleCommand;
                case 0x000E:
                    return ServerRntbdConstants.RntbdResourceType.Record;
                case 0x000F:
                    return ServerRntbdConstants.RntbdResourceType.Offer;
                case 0x0010:
                    return ServerRntbdConstants.RntbdResourceType.PartitionSetInformation;
                case 0x0011:
                    return ServerRntbdConstants.RntbdResourceType.XPReplicatorAddress;
                case 0x0012:
                    return ServerRntbdConstants.RntbdResourceType.MasterPartition;
                case 0x0013:
                    return ServerRntbdConstants.RntbdResourceType.ServerPartition;
                case 0x0014:
                    return ServerRntbdConstants.RntbdResourceType.DatabaseAccount;
                case 0x0015:
                    return ServerRntbdConstants.RntbdResourceType.Topology;
                case 0x0016:
                    return ServerRntbdConstants.RntbdResourceType.PartitionKeyRange;
                case 0x0018:
                    return ServerRntbdConstants.RntbdResourceType.Schema;
                case 0x0019:
                    return ServerRntbdConstants.RntbdResourceType.BatchApply;
                case 0x001A:
                    return ServerRntbdConstants.RntbdResourceType.RestoreMetadata;
                case 0x001B:
                    return ServerRntbdConstants.RntbdResourceType.ComputeGatewayCharges;
                case 0x001C:
                    return ServerRntbdConstants.RntbdResourceType.RidRange;
                case 0x001D:
                    return ServerRntbdConstants.RntbdResourceType.UserDefinedType;
                default:
                    throw new DecoderException(lenientFormat("expected byte value matching %s value, not %s",
                        RntbdConstants.RntbdResourceType.class.getSimpleName(),
                        id));
            }
        }

        public static RntbdConstants.RntbdResourceType fromType(ResourceType type) {

            switch (type) {
                case Database:
                    return RntbdConstants.RntbdResourceType.Database;
                case DocumentCollection:
                    return RntbdConstants.RntbdResourceType.Collection;
                case Document:
                    return RntbdConstants.RntbdResourceType.Document;
                case Attachment:
                    return RntbdConstants.RntbdResourceType.Attachment;
                case User:
                    return RntbdConstants.RntbdResourceType.User;
                case Permission:
                    return RntbdConstants.RntbdResourceType.Permission;
                case StoredProcedure:
                    return RntbdConstants.RntbdResourceType.StoredProcedure;
                case Conflict:
                    return RntbdConstants.RntbdResourceType.Conflict;
                case Trigger:
                    return RntbdConstants.RntbdResourceType.Trigger;
                case UserDefinedFunction:
                    return RntbdConstants.RntbdResourceType.UserDefinedFunction;
                case Module:
                    return RntbdConstants.RntbdResourceType.Module;
                case Replica:
                    return RntbdConstants.RntbdResourceType.Replica;
                case ModuleCommand:
                    return RntbdConstants.RntbdResourceType.ModuleCommand;
                case Record:
                    return RntbdConstants.RntbdResourceType.Record;
                case Offer:
                    return RntbdConstants.RntbdResourceType.Offer;
                case PartitionSetInformation:
                    return RntbdConstants.RntbdResourceType.PartitionSetInformation;
                case XPReplicatorAddress:
                    return RntbdConstants.RntbdResourceType.XPReplicatorAddress;
                case MasterPartition:
                    return RntbdConstants.RntbdResourceType.MasterPartition;
                case ServerPartition:
                    return RntbdConstants.RntbdResourceType.ServerPartition;
                case DatabaseAccount:
                    return RntbdConstants.RntbdResourceType.DatabaseAccount;
                case Topology:
                    return RntbdConstants.RntbdResourceType.Topology;
                case PartitionKeyRange:
                    return RntbdConstants.RntbdResourceType.PartitionKeyRange;
                case Schema:
                    return RntbdConstants.RntbdResourceType.Schema;
                case BatchApply:
                    return RntbdConstants.RntbdResourceType.BatchApply;
                case RestoreMetadata:
                    return RntbdConstants.RntbdResourceType.RestoreMetadata;
                case ComputeGatewayCharges:
                    return RntbdConstants.RntbdResourceType.ComputeGatewayCharges;
                case RidRange:
                    return RntbdConstants.RntbdResourceType.RidRange;
                case UserDefinedType:
                    return RntbdConstants.RntbdResourceType.UserDefinedType;
                default:
                    throw new IllegalArgumentException(lenientFormat("unrecognized resource type: %s", type));
            }
        }

        public short id() {
            return this.id;
        }

        public ResourceType type() {
            return this.type;
        }
    }

    public enum RntbdResponseHeader implements RntbdHeader {

        PayloadPresent((short) 0x0000, ServerRntbdTokenType.Byte, true),
        // not used((short)0x0001),
        LastStateChangeDateTime((short) 0x0002, ServerRntbdTokenType.SmallString, false),
        ContinuationToken((short) 0x0003, ServerRntbdTokenType.String, false),
        ETag((short) 0x0004, ServerRntbdTokenType.String, false),
        // not used((short)0x005,)
        // not used((short)0x006,)
        ReadsPerformed((short) 0x0007, ServerRntbdTokenType.ULong, false),
        WritesPerformed((short) 0x0008, ServerRntbdTokenType.ULong, false),
        QueriesPerformed((short) 0x0009, ServerRntbdTokenType.ULong, false),
        IndexTermsGenerated((short) 0x000A, ServerRntbdTokenType.ULong, false),
        ScriptsExecuted((short) 0x000B, ServerRntbdTokenType.ULong, false),
        RetryAfterMilliseconds((short) 0x000C, ServerRntbdTokenType.ULong, false),
        IndexingDirective((short) 0x000D, ServerRntbdTokenType.Byte, false),
        StorageMaxResoureQuota((short) 0x000E, ServerRntbdTokenType.String, false),
        StorageResourceQuotaUsage((short) 0x000F, ServerRntbdTokenType.String, false),
        SchemaVersion((short) 0x0010, ServerRntbdTokenType.SmallString, false),
        CollectionPartitionIndex((short) 0x0011, ServerRntbdTokenType.ULong, false),
        CollectionServiceIndex((short) 0x0012, ServerRntbdTokenType.ULong, false),
        LSN((short) 0x0013, ServerRntbdTokenType.LongLong, false),
        ItemCount((short) 0x0014, ServerRntbdTokenType.ULong, false),
        RequestCharge((short) 0x0015, ServerRntbdTokenType.Double, false),
        // not used((short)0x0016),
        OwnerFullName((short) 0x0017, ServerRntbdTokenType.String, false),
        OwnerId((short) 0x0018, ServerRntbdTokenType.String, false),
        DatabaseAccountId((short) 0x0019, ServerRntbdTokenType.String, false),
        QuorumAckedLSN((short) 0x001A, ServerRntbdTokenType.LongLong, false),
        RequestValidationFailure((short) 0x001B, ServerRntbdTokenType.Byte, false),
        SubStatus((short) 0x001C, ServerRntbdTokenType.ULong, false),
        CollectionUpdateProgress((short) 0x001D, ServerRntbdTokenType.ULong, false),
        CurrentWriteQuorum((short) 0x001E, ServerRntbdTokenType.ULong, false),
        CurrentReplicaSetSize((short) 0x001F, ServerRntbdTokenType.ULong, false),
        CollectionLazyIndexProgress((short) 0x0020, ServerRntbdTokenType.ULong, false),
        PartitionKeyRangeId((short) 0x0021, ServerRntbdTokenType.String, false),
        // not used((short)0x0022), RequestMessageReceivedTime
        // not used((short)0x0023), ResponseMessageSentTime
        // not used((short)0x0024), ResponseMessageTimeOffset
        LogResults((short) 0x0025, ServerRntbdTokenType.String, false),
        XPRole((short) 0x0026, ServerRntbdTokenType.ULong, false),
        IsRUPerMinuteUsed((short) 0x0027, ServerRntbdTokenType.Byte, false),
        QueryMetrics((short) 0x0028, ServerRntbdTokenType.String, false),
        GlobalCommittedLSN((short) 0x0029, ServerRntbdTokenType.LongLong, false),
        NumberOfReadRegions((short) 0x0030, ServerRntbdTokenType.ULong, false),
        OfferReplacePending((short) 0x0031, ServerRntbdTokenType.Byte, false),
        ItemLSN((short) 0x0032, ServerRntbdTokenType.LongLong, false),
        RestoreState((short) 0x0033, ServerRntbdTokenType.String, false),
        CollectionSecurityIdentifier((short) 0x0034, ServerRntbdTokenType.String, false),
        TransportRequestID((short) 0x0035, ServerRntbdTokenType.ULong, false),
        ShareThroughput((short) 0x0036, ServerRntbdTokenType.Byte, false),
        // not used((short)0x0037), LeaseSeqNumber
        DisableRntbdChannel((short) 0x0038, ServerRntbdTokenType.Byte, false),
        ServerDateTimeUtc((short) 0x0039, ServerRntbdTokenType.SmallString, false),
        LocalLSN((short) 0x003A, ServerRntbdTokenType.LongLong, false),
        QuorumAckedLocalLSN((short) 0x003B, ServerRntbdTokenType.LongLong, false),
        ItemLocalLSN((short) 0x003C, ServerRntbdTokenType.LongLong, false),
        HasTentativeWrites((short) 0x003D, ServerRntbdTokenType.Byte, false),
        SessionToken((short) 0x003E, ServerRntbdTokenType.String, false);

        public static final ImmutableMap<Short, RntbdConstants.RntbdResponseHeader> map;
        public static final ImmutableSet<RntbdConstants.RntbdResponseHeader> set = Sets.immutableEnumSet(EnumSet.allOf(RntbdConstants.RntbdResponseHeader.class));

        static {
            final Collector<RntbdConstants.RntbdResponseHeader, ?, ImmutableMap<Short, RntbdConstants.RntbdResponseHeader>> collector = ImmutableMap.toImmutableMap(RntbdConstants.RntbdResponseHeader::id, header -> header);
            map = set.stream().collect(collector);
        }

        private final short id;
        private final boolean isRequired;
        private final ServerRntbdTokenType type;

        RntbdResponseHeader(final short id, final ServerRntbdTokenType type, final boolean isRequired) {
            this.id = id;
            this.type = type;
            this.isRequired = isRequired;
        }

        public boolean isRequired() {
            return this.isRequired;
        }

        public short id() {
            return this.id;
        }

        public ServerRntbdTokenType type() {
            return this.type;
        }
    }

    enum RntbdContextRequestHeader implements RntbdHeader {

        ProtocolVersion((short) 0x0000, ServerRntbdTokenType.ULong, true),
        ClientVersion((short) 0x0001, ServerRntbdTokenType.SmallString, true),
        UserAgent((short) 0x0002, ServerRntbdTokenType.SmallString, true);

        public static final ImmutableMap<Short, RntbdContextRequestHeader> map;
        public static final ImmutableSet<RntbdContextRequestHeader> set = Sets.immutableEnumSet(EnumSet.allOf(RntbdContextRequestHeader.class));

        static {
            final Collector<RntbdContextRequestHeader, ?, ImmutableMap<Short, RntbdContextRequestHeader>> collector = ImmutableMap.toImmutableMap(h -> h.id(), h -> h);
            map = set.stream().collect(collector);
        }

        private final short id;
        private final boolean isRequired;
        private final ServerRntbdTokenType type;

        RntbdContextRequestHeader(final short id, final ServerRntbdTokenType type, final boolean isRequired) {
            this.id = id;
            this.type = type;
            this.isRequired = isRequired;
        }

        public boolean isRequired() {
            return this.isRequired;
        }

        public short id() {
            return this.id;
        }

        public ServerRntbdTokenType type() {
            return this.type;
        }
    }

    interface RntbdHeader {

        boolean isRequired();

        short id();

        String name();

        ServerRntbdTokenType type();
    }
}
