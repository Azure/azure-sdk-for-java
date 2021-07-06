// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.guava25.collect.ImmutableMap;
import com.azure.cosmos.implementation.guava25.collect.ImmutableSet;
import com.azure.cosmos.implementation.guava25.collect.Sets;
import io.netty.handler.codec.DecoderException;

import java.util.EnumSet;
import java.util.stream.Collector;

import static com.azure.cosmos.implementation.guava27.Strings.lenientFormat;

public final class RntbdConstants {

    static final int CURRENT_PROTOCOL_VERSION = 0x00000001;

    private RntbdConstants() {
    }

    public enum RntbdConsistencyLevel {

        Strong((byte) 0x00),
        BoundedStaleness((byte) 0x01),
        Session((byte) 0x02),
        Eventual((byte) 0x03),
        ConsistentPrefix((byte) 0x04),

        Invalid((byte) 0xFF);

        private final byte id;

        RntbdConsistencyLevel(final byte id) {
            this.id = id;
        }

        public byte id() {
            return this.id;
        }
    }

    public enum RntbdContentSerializationFormat {

        JsonText((byte) 0x00),
        CosmosBinary((byte) 0x01),

        Invalid((byte) 0xFF);

        private final byte id;

        RntbdContentSerializationFormat(final byte id) {
            this.id = id;
        }

        public byte id() {
            return this.id;
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    enum RntbdContextHeader implements RntbdHeader {

        ProtocolVersion((short) 0x0000, RntbdTokenType.ULong, false),
        ClientVersion((short) 0x0001, RntbdTokenType.SmallString, false),
        ServerAgent((short) 0x0002, RntbdTokenType.SmallString, true),
        ServerVersion((short) 0x0003, RntbdTokenType.SmallString, true),
        IdleTimeoutInSeconds((short) 0x0004, RntbdTokenType.ULong, false),
        UnauthenticatedTimeoutInSeconds((short) 0x0005, RntbdTokenType.ULong, false);

        public static final ImmutableMap<Short, RntbdContextHeader> map;
        public static final ImmutableSet<RntbdContextHeader> set = Sets.immutableEnumSet(EnumSet.allOf(RntbdContextHeader.class));

        static {
            final Collector<RntbdContextHeader, ?, ImmutableMap<Short, RntbdContextHeader>> collector = ImmutableMap.toImmutableMap(RntbdContextHeader::id, h -> h);
            map = set.stream().collect(collector);
        }

        private final short id;
        private final boolean isRequired;
        private final RntbdTokenType type;

        RntbdContextHeader(final short id, final RntbdTokenType type, final boolean isRequired) {
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

        public RntbdTokenType type() {
            return this.type;
        }
    }

    enum RntbdContextRequestHeader implements RntbdHeader {

        ProtocolVersion((short) 0x0000, RntbdTokenType.ULong, true),
        ClientVersion((short) 0x0001, RntbdTokenType.SmallString, true),
        UserAgent((short) 0x0002, RntbdTokenType.SmallString, true);

        public static final ImmutableMap<Short, RntbdContextRequestHeader> map;
        public static final ImmutableSet<RntbdContextRequestHeader> set = Sets.immutableEnumSet(EnumSet.allOf(RntbdContextRequestHeader.class));

        static {
            final Collector<RntbdContextRequestHeader, ?, ImmutableMap<Short, RntbdContextRequestHeader>> collector = ImmutableMap.toImmutableMap(h -> h.id(), h -> h);
            map = set.stream().collect(collector);
        }

        private final short id;
        private final boolean isRequired;
        private final RntbdTokenType type;

        RntbdContextRequestHeader(final short id, final RntbdTokenType type, final boolean isRequired) {
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

        public RntbdTokenType type() {
            return this.type;
        }
    }

    public enum RntbdEnumerationDirection {

        Invalid((byte) 0x00),

        Forward((byte) 0x01),
        Reverse((byte) 0x02);

        private final byte id;

        RntbdEnumerationDirection(final byte id) {
            this.id = id;
        }

        public byte id() {
            return this.id;
        }
    }

    public enum RntbdFanoutOperationState {

        Started((byte) 0x01),
        Completed((byte) 0x02);

        private final byte id;

        RntbdFanoutOperationState(final byte id) {
            this.id = id;
        }

        public byte id() {
            return this.id;
        }
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

    public enum RntbdMigrateCollectionDirective {

        Thaw((byte) 0x00),
        Freeze((byte) 0x01),

        Invalid((byte) 0xFF);

        private final byte id;

        RntbdMigrateCollectionDirective(final byte id) {
            this.id = id;
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

        public static RntbdOperationType fromId(final short id) {

            switch (id) {
                case 0x0000:
                    return RntbdOperationType.Connection;
                case 0x0001:
                    return RntbdOperationType.Create;
                case 0x0002:
                    return RntbdOperationType.Patch;
                case 0x0003:
                    return RntbdOperationType.Read;
                case 0x0004:
                    return RntbdOperationType.ReadFeed;
                case 0x0005:
                    return RntbdOperationType.Delete;
                case 0x0006:
                    return RntbdOperationType.Replace;
                // Obsolete and now undefined: case 0x0007: return RntbdOperationType.JPathQuery;
                case 0x0008:
                    return RntbdOperationType.ExecuteJavaScript;
                case 0x0009:
                    return RntbdOperationType.SqlQuery;
                case 0x000A:
                    return RntbdOperationType.Pause;
                case 0x000B:
                    return RntbdOperationType.Resume;
                case 0x000C:
                    return RntbdOperationType.Stop;
                case 0x000D:
                    return RntbdOperationType.Recycle;
                case 0x000E:
                    return RntbdOperationType.Crash;
                case 0x000F:
                    return RntbdOperationType.Query;
                case 0x0010:
                    return RntbdOperationType.ForceConfigRefresh;
                case 0x0011:
                    return RntbdOperationType.Head;
                case 0x0012:
                    return RntbdOperationType.HeadFeed;
                case 0x0013:
                    return RntbdOperationType.Upsert;
                case 0x0014:
                    return RntbdOperationType.Recreate;
                case 0x0015:
                    return RntbdOperationType.Throttle;
                case 0x0016:
                    return RntbdOperationType.GetSplitPoint;
                case 0x0017:
                    return RntbdOperationType.PreCreateValidation;
                case 0x0018:
                    return RntbdOperationType.BatchApply;
                case 0x0019:
                    return RntbdOperationType.AbortSplit;
                case 0x001A:
                    return RntbdOperationType.CompleteSplit;
                case 0x001B:
                    return RntbdOperationType.OfferUpdateOperation;
                case 0x001C:
                    return RntbdOperationType.OfferPreGrowValidation;
                case 0x001D:
                    return RntbdOperationType.BatchReportThroughputUtilization;
                case 0x001E:
                    return RntbdOperationType.CompletePartitionMigration;
                case 0x001F:
                    return RntbdOperationType.AbortPartitionMigration;
                case 0x0020:
                    return RntbdOperationType.PreReplaceValidation;
                case 0x0021:
                    return RntbdOperationType.AddComputeGatewayRequestCharges;
                case 0x0022:
                    return RntbdOperationType.MigratePartition;
                case 0x0025:
                    return RntbdOperationType.Batch;
                default:
                    throw new DecoderException(lenientFormat("expected byte value matching %s value, not %s",
                        RntbdOperationType.class.getSimpleName(),
                        id));
            }
        }

        public static RntbdOperationType fromType(OperationType type) {
            switch (type) {
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
        EffectivePartitionKey((byte) 0x02),
        EffectivePartitionKeyRange((byte) 0x03);

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

        ResourceId((short) 0x0000, RntbdTokenType.Bytes, false),
        AuthorizationToken((short) 0x0001, RntbdTokenType.String, false),
        PayloadPresent((short) 0x0002, RntbdTokenType.Byte, true),
        Date((short) 0x0003, RntbdTokenType.SmallString, false),
        PageSize((short) 0x0004, RntbdTokenType.ULong, false),
        SessionToken((short) 0x0005, RntbdTokenType.String, false),
        ContinuationToken((short) 0x0006, RntbdTokenType.String, false),
        IndexingDirective((short) 0x0007, RntbdTokenType.Byte, false),
        Match((short) 0x0008, RntbdTokenType.String, false),
        PreTriggerInclude((short) 0x0009, RntbdTokenType.String, false),
        PostTriggerInclude((short) 0x000A, RntbdTokenType.String, false),
        IsFanout((short) 0x000B, RntbdTokenType.Byte, false),
        CollectionPartitionIndex((short) 0x000C, RntbdTokenType.ULong, false),
        CollectionServiceIndex((short) 0x000D, RntbdTokenType.ULong, false),
        PreTriggerExclude((short) 0x000E, RntbdTokenType.String, false),
        PostTriggerExclude((short) 0x000F, RntbdTokenType.String, false),
        ConsistencyLevel((short) 0x0010, RntbdTokenType.Byte, false),
        EntityId((short) 0x0011, RntbdTokenType.String, false),
        ResourceSchemaName((short) 0x0012, RntbdTokenType.SmallString, false),
        ReplicaPath((short) 0x0013, RntbdTokenType.String, true),
        ResourceTokenExpiry((short) 0x0014, RntbdTokenType.ULong, false),
        DatabaseName((short) 0x0015, RntbdTokenType.String, false),
        CollectionName((short) 0x0016, RntbdTokenType.String, false),
        DocumentName((short) 0x0017, RntbdTokenType.String, false),
        AttachmentName((short) 0x0018, RntbdTokenType.String, false),
        UserName((short) 0x0019, RntbdTokenType.String, false),
        PermissionName((short) 0x001A, RntbdTokenType.String, false),
        StoredProcedureName((short) 0x001B, RntbdTokenType.String, false),
        UserDefinedFunctionName((short) 0x001C, RntbdTokenType.String, false),
        TriggerName((short) 0x001D, RntbdTokenType.String, false),
        EnableScanInQuery((short) 0x001E, RntbdTokenType.Byte, false),
        EmitVerboseTracesInQuery((short) 0x001F, RntbdTokenType.Byte, false),
        ConflictName((short) 0x0020, RntbdTokenType.String, false),
        BindReplicaDirective((short) 0x0021, RntbdTokenType.String, false),
        PrimaryMasterKey((short) 0x0022, RntbdTokenType.String, false),
        SecondaryMasterKey((short) 0x0023, RntbdTokenType.String, false),
        PrimaryReadonlyKey((short) 0x0024, RntbdTokenType.String, false),
        SecondaryReadonlyKey((short) 0x0025, RntbdTokenType.String, false),
        ProfileRequest((short) 0x0026, RntbdTokenType.Byte, false),
        EnableLowPrecisionOrderBy((short) 0x0027, RntbdTokenType.Byte, false),
        ClientVersion((short) 0x0028, RntbdTokenType.SmallString, false),
        CanCharge((short) 0x0029, RntbdTokenType.Byte, false),
        CanThrottle((short) 0x002A, RntbdTokenType.Byte, false),
        PartitionKey((short) 0x002B, RntbdTokenType.String, false),
        PartitionKeyRangeId((short) 0x002C, RntbdTokenType.String, false),
        NotUsed2D((short) 0x002D, RntbdTokenType.Invalid, false),
        NotUsed2E((short) 0x002E, RntbdTokenType.Invalid, false),
        NotUsed2F((short) 0x002F, RntbdTokenType.Invalid, false),
        // not used 0x0030,
        MigrateCollectionDirective((short) 0x0031, RntbdTokenType.Byte, false),
        NotUsed32((short) 0x0032, RntbdTokenType.Invalid, false),
        SupportSpatialLegacyCoordinates((short) 0x0033, RntbdTokenType.Byte, false),
        PartitionCount((short) 0x0034, RntbdTokenType.ULong, false),
        CollectionRid((short) 0x0035, RntbdTokenType.String, false),
        PartitionKeyRangeName((short) 0x0036, RntbdTokenType.String, false),
        // not used((short)0x0037), RoundTripTimeInMsec
        // not used((short)0x0038), RequestMessageSentTime
        // not used((short)0x0039), RequestMessageTimeOffset
        SchemaName((short) 0x003A, RntbdTokenType.String, false),
        FilterBySchemaRid((short) 0x003B, RntbdTokenType.String, false),
        UsePolygonsSmallerThanAHemisphere((short) 0x003C, RntbdTokenType.Byte, false),
        GatewaySignature((short) 0x003D, RntbdTokenType.String, false),
        EnableLogging((short) 0x003E, RntbdTokenType.Byte, false),
        A_IM((short) 0x003F, RntbdTokenType.String, false),
        PopulateQuotaInfo((short) 0x0040, RntbdTokenType.Byte, false),
        DisableRUPerMinuteUsage((short) 0x0041, RntbdTokenType.Byte, false),
        PopulateQueryMetrics((short) 0x0042, RntbdTokenType.Byte, false),
        ResponseContinuationTokenLimitInKb((short) 0x0043, RntbdTokenType.ULong, false),
        PopulatePartitionStatistics((short) 0x0044, RntbdTokenType.Byte, false),
        RemoteStorageType((short) 0x0045, RntbdTokenType.Byte, false),
        CollectionRemoteStorageSecurityIdentifier((short) 0x0046, RntbdTokenType.String, false),
        IfModifiedSince((short) 0x0047, RntbdTokenType.String, false),
        PopulateCollectionThroughputInfo((short) 0x0048, RntbdTokenType.Byte, false),
        RemainingTimeInMsOnClientRequest((short) 0x0049, RntbdTokenType.ULong, false),
        ClientRetryAttemptCount((short) 0x004A, RntbdTokenType.ULong, false),
        TargetLsn((short) 0x004B, RntbdTokenType.LongLong, false),
        TargetGlobalCommittedLsn((short) 0x004C, RntbdTokenType.LongLong, false),
        TransportRequestID((short) 0x004D, RntbdTokenType.ULong, false),
        RestoreMetadaFilter((short) 0x004E, RntbdTokenType.String, false),
        RestoreParams((short) 0x004F, RntbdTokenType.String, false),
        ShareThroughput((short) 0x0050, RntbdTokenType.Byte, false),
        PartitionResourceFilter((short) 0x0051, RntbdTokenType.String, false),
        IsReadOnlyScript((short) 0x0052, RntbdTokenType.Byte, false),
        IsAutoScaleRequest((short) 0x0053, RntbdTokenType.Byte, false),
        ForceQueryScan((short) 0x0054, RntbdTokenType.Byte, false),
        // not used((short)0x0055), LeaseSeqNumber
        CanOfferReplaceComplete((short) 0x0056, RntbdTokenType.Byte, false),
        ExcludeSystemProperties((short) 0x0057, RntbdTokenType.Byte, false),
        BinaryId((short) 0x0058, RntbdTokenType.Bytes, false),
        TimeToLiveInSeconds((short) 0x0059, RntbdTokenType.Long, false),
        EffectivePartitionKey((short) 0x005A, RntbdTokenType.Bytes, false),
        BinaryPassthroughRequest((short) 0x005B, RntbdTokenType.Byte, false),
        UserDefinedTypeName((short) 0x005C, RntbdTokenType.String, false),
        EnableDynamicRidRangeAllocation((short) 0x005D, RntbdTokenType.Byte, false),
        EnumerationDirection((short) 0x005E, RntbdTokenType.Byte, false),
        StartId((short) 0x005F, RntbdTokenType.Bytes, false),
        EndId((short) 0x0060, RntbdTokenType.Bytes, false),
        FanoutOperationState((short) 0x0061, RntbdTokenType.Byte, false),
        StartEpk((short) 0x0062, RntbdTokenType.Bytes, false),
        EndEpk((short) 0x0063, RntbdTokenType.Bytes, false),
        ReadFeedKeyType((short) 0x0064, RntbdTokenType.Byte, false),
        ContentSerializationFormat((short) 0x0065, RntbdTokenType.Byte, false),
        AllowTentativeWrites((short) 0x0066, RntbdTokenType.Byte, false),
        IsUserRequest((short) 0x0067, RntbdTokenType.Byte, false),
        SharedOfferThroughput((short) 0x0068, RntbdTokenType.ULong, false),
        IsBatchAtomic((short) 0x0073, RntbdTokenType.Byte, false),
        ShouldBatchContinueOnError((short) 0x0074, RntbdTokenType.Byte, false),
        IsBatchOrdered((short) 0x0075, RntbdTokenType.Byte, false),
        ReturnPreference((short) 0x0082, RntbdTokenType.Byte, false);

        public static final ImmutableMap<Short, RntbdRequestHeader> map;
        public static final ImmutableSet<RntbdRequestHeader> set = Sets.immutableEnumSet(EnumSet.allOf(RntbdRequestHeader.class));

        static {
            final Collector<RntbdRequestHeader, ?, ImmutableMap<Short, RntbdRequestHeader>> collector = ImmutableMap.toImmutableMap(RntbdRequestHeader::id, h -> h);
            map = set.stream().collect(collector);
        }

        private final short id;
        private final boolean isRequired;
        private final RntbdTokenType type;

        RntbdRequestHeader(final short id, final RntbdTokenType type, final boolean isRequired) {
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

        public RntbdTokenType type() {
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

        public static RntbdResourceType fromId(final short id) throws IllegalArgumentException {
            switch (id) {
                case 0x0000:
                    return RntbdResourceType.Connection;
                case 0x0001:
                    return RntbdResourceType.Database;
                case 0x0002:
                    return RntbdResourceType.Collection;
                case 0x0003:
                    return RntbdResourceType.Document;
                case 0x0004:
                    return RntbdResourceType.Attachment;
                case 0x0005:
                    return RntbdResourceType.User;
                case 0x0006:
                    return RntbdResourceType.Permission;
                case 0x0007:
                    return RntbdResourceType.StoredProcedure;
                case 0x0008:
                    return RntbdResourceType.Conflict;
                case 0x0009:
                    return RntbdResourceType.Trigger;
                case 0x000A:
                    return RntbdResourceType.UserDefinedFunction;
                case 0x000B:
                    return RntbdResourceType.Module;
                case 0x000C:
                    return RntbdResourceType.Replica;
                case 0x000D:
                    return RntbdResourceType.ModuleCommand;
                case 0x000E:
                    return RntbdResourceType.Record;
                case 0x000F:
                    return RntbdResourceType.Offer;
                case 0x0010:
                    return RntbdResourceType.PartitionSetInformation;
                case 0x0011:
                    return RntbdResourceType.XPReplicatorAddress;
                case 0x0012:
                    return RntbdResourceType.MasterPartition;
                case 0x0013:
                    return RntbdResourceType.ServerPartition;
                case 0x0014:
                    return RntbdResourceType.DatabaseAccount;
                case 0x0015:
                    return RntbdResourceType.Topology;
                case 0x0016:
                    return RntbdResourceType.PartitionKeyRange;
                case 0x0018:
                    return RntbdResourceType.Schema;
                case 0x0019:
                    return RntbdResourceType.BatchApply;
                case 0x001A:
                    return RntbdResourceType.RestoreMetadata;
                case 0x001B:
                    return RntbdResourceType.ComputeGatewayCharges;
                case 0x001C:
                    return RntbdResourceType.RidRange;
                case 0x001D:
                    return RntbdResourceType.UserDefinedType;
                default:
                    throw new DecoderException(lenientFormat("expected byte value matching %s value, not %s",
                        RntbdResourceType.class.getSimpleName(),
                        id));
            }
        }

        public static RntbdResourceType fromType(ResourceType type) {

            switch (type) {
                case Database:
                    return RntbdResourceType.Database;
                case DocumentCollection:
                    return RntbdResourceType.Collection;
                case Document:
                    return RntbdResourceType.Document;
                case Attachment:
                    return RntbdResourceType.Attachment;
                case User:
                    return RntbdResourceType.User;
                case Permission:
                    return RntbdResourceType.Permission;
                case StoredProcedure:
                    return RntbdResourceType.StoredProcedure;
                case Conflict:
                    return RntbdResourceType.Conflict;
                case Trigger:
                    return RntbdResourceType.Trigger;
                case UserDefinedFunction:
                    return RntbdResourceType.UserDefinedFunction;
                case Module:
                    return RntbdResourceType.Module;
                case Replica:
                    return RntbdResourceType.Replica;
                case ModuleCommand:
                    return RntbdResourceType.ModuleCommand;
                case Record:
                    return RntbdResourceType.Record;
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
                case UserDefinedType:
                    return RntbdResourceType.UserDefinedType;
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

        PayloadPresent((short) 0x0000, RntbdTokenType.Byte, true),
        // not used((short)0x0001),
        LastStateChangeDateTime((short) 0x0002, RntbdTokenType.SmallString, false),
        ContinuationToken((short) 0x0003, RntbdTokenType.String, false),
        ETag((short) 0x0004, RntbdTokenType.String, false),
        // not used((short)0x005,)
        // not used((short)0x006,)
        ReadsPerformed((short) 0x0007, RntbdTokenType.ULong, false),
        WritesPerformed((short) 0x0008, RntbdTokenType.ULong, false),
        QueriesPerformed((short) 0x0009, RntbdTokenType.ULong, false),
        IndexTermsGenerated((short) 0x000A, RntbdTokenType.ULong, false),
        ScriptsExecuted((short) 0x000B, RntbdTokenType.ULong, false),
        RetryAfterMilliseconds((short) 0x000C, RntbdTokenType.ULong, false),
        IndexingDirective((short) 0x000D, RntbdTokenType.Byte, false),
        StorageMaxResoureQuota((short) 0x000E, RntbdTokenType.String, false),
        StorageResourceQuotaUsage((short) 0x000F, RntbdTokenType.String, false),
        SchemaVersion((short) 0x0010, RntbdTokenType.SmallString, false),
        CollectionPartitionIndex((short) 0x0011, RntbdTokenType.ULong, false),
        CollectionServiceIndex((short) 0x0012, RntbdTokenType.ULong, false),
        LSN((short) 0x0013, RntbdTokenType.LongLong, false),
        ItemCount((short) 0x0014, RntbdTokenType.ULong, false),
        RequestCharge((short) 0x0015, RntbdTokenType.Double, false),
        // not used((short)0x0016),
        OwnerFullName((short) 0x0017, RntbdTokenType.String, false),
        OwnerId((short) 0x0018, RntbdTokenType.String, false),
        DatabaseAccountId((short) 0x0019, RntbdTokenType.String, false),
        QuorumAckedLSN((short) 0x001A, RntbdTokenType.LongLong, false),
        RequestValidationFailure((short) 0x001B, RntbdTokenType.Byte, false),
        SubStatus((short) 0x001C, RntbdTokenType.ULong, false),
        CollectionUpdateProgress((short) 0x001D, RntbdTokenType.ULong, false),
        CurrentWriteQuorum((short) 0x001E, RntbdTokenType.ULong, false),
        CurrentReplicaSetSize((short) 0x001F, RntbdTokenType.ULong, false),
        CollectionLazyIndexProgress((short) 0x0020, RntbdTokenType.ULong, false),
        PartitionKeyRangeId((short) 0x0021, RntbdTokenType.String, false),
        // not used((short)0x0022), RequestMessageReceivedTime
        // not used((short)0x0023), ResponseMessageSentTime
        // not used((short)0x0024), ResponseMessageTimeOffset
        LogResults((short) 0x0025, RntbdTokenType.String, false),
        XPRole((short) 0x0026, RntbdTokenType.ULong, false),
        IsRUPerMinuteUsed((short) 0x0027, RntbdTokenType.Byte, false),
        QueryMetrics((short) 0x0028, RntbdTokenType.String, false),
        GlobalCommittedLSN((short) 0x0029, RntbdTokenType.LongLong, false),
        NumberOfReadRegions((short) 0x0030, RntbdTokenType.ULong, false),
        OfferReplacePending((short) 0x0031, RntbdTokenType.Byte, false),
        ItemLSN((short) 0x0032, RntbdTokenType.LongLong, false),
        RestoreState((short) 0x0033, RntbdTokenType.String, false),
        CollectionSecurityIdentifier((short) 0x0034, RntbdTokenType.String, false),
        TransportRequestID((short) 0x0035, RntbdTokenType.ULong, false),
        ShareThroughput((short) 0x0036, RntbdTokenType.Byte, false),
        // not used((short)0x0037), LeaseSeqNumber
        DisableRntbdChannel((short) 0x0038, RntbdTokenType.Byte, false),
        ServerDateTimeUtc((short) 0x0039, RntbdTokenType.SmallString, false),
        LocalLSN((short) 0x003A, RntbdTokenType.LongLong, false),
        QuorumAckedLocalLSN((short) 0x003B, RntbdTokenType.LongLong, false),
        ItemLocalLSN((short) 0x003C, RntbdTokenType.LongLong, false),
        HasTentativeWrites((short) 0x003D, RntbdTokenType.Byte, false),
        SessionToken((short) 0x003E, RntbdTokenType.String, false),
        BackendRequestDurationMilliseconds((short) 0X0051, RntbdTokenType.Double, false);

        public static final ImmutableMap<Short, RntbdResponseHeader> map;
        public static final ImmutableSet<RntbdResponseHeader> set = Sets.immutableEnumSet(EnumSet.allOf(RntbdResponseHeader.class));

        static {
            final Collector<RntbdResponseHeader, ?, ImmutableMap<Short, RntbdResponseHeader>> collector = ImmutableMap.toImmutableMap(RntbdResponseHeader::id, header -> header);
            map = set.stream().collect(collector);
        }

        private final short id;
        private final boolean isRequired;
        private final RntbdTokenType type;

        RntbdResponseHeader(final short id, final RntbdTokenType type, final boolean isRequired) {
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

        public RntbdTokenType type() {
            return this.type;
        }
    }

    interface RntbdHeader {

        boolean isRequired();

        short id();

        String name();

        RntbdTokenType type();
    }
}
