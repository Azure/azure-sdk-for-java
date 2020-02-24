// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;


import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.InternalServerErrorException;
import org.apache.commons.collections4.map.UnmodifiableMap;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.azure.data.cosmos.internal.Utils.ValueHolder;

/**
 * Models vector clock bases session token. SESSION token has the following format:
 * {Version}#{GlobalLSN}#{RegionId1}={LocalLsn1}#{RegionId2}={LocalLsn2}....#{RegionIdN}={LocalLsnN}
 * 'Version' captures the configuration number of the partition which returned this session token.
 * 'Version' is incremented everytime topology of the partition is updated (say due to Add/Remove/Failover).
 * * The choice of separators '#' and '=' is important. Separators ';' and ',' are used to delimit
 * per-partitionKeyRange session token
 * session
 *
 * We make assumption that instances of this class are immutable (read only after they are constructed), so if you want to change
 * this behaviour please review all of its uses and make sure that mutability doesn't break anything.
 */
public class VectorSessionToken implements ISessionToken {
    private final static Logger logger = LoggerFactory.getLogger(VectorSessionToken.class);
    private final static char SegmentSeparator = '#';
    private final static char RegionProgressSeparator = '=';

    private final long version;
    private final long globalLsn;
    private final UnmodifiableMap<Integer, Long> localLsnByRegion;
    private final String sessionToken;

    private VectorSessionToken(long version, long globalLsn, UnmodifiableMap<Integer, Long> localLsnByRegion) {
        this(version, globalLsn, localLsnByRegion, null);
    }

    private VectorSessionToken(long version, long globalLsn, UnmodifiableMap<Integer, Long> localLsnByRegion, String sessionToken) {
        this.version = version;
        this.globalLsn = globalLsn;
        this.localLsnByRegion = localLsnByRegion;
        if (sessionToken == null) {
            String regionProgress = String.join(
                    Character.toString(VectorSessionToken.SegmentSeparator),
                    localLsnByRegion.
                            entrySet()
                            .stream()
                            .map(kvp -> new StringBuilder().append(kvp.getKey()).append(VectorSessionToken.RegionProgressSeparator).append(kvp.getValue()))
                            .collect(Collectors.toList()));

            if (Strings.isNullOrEmpty(regionProgress)) {
                StringBuilder sb = new StringBuilder();
                sb.append(this.version)
                        .append(VectorSessionToken.SegmentSeparator)
                        .append(this.globalLsn);
                this.sessionToken = sb.toString();
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(this.version)
                        .append(VectorSessionToken.SegmentSeparator)
                        .append(this.globalLsn)
                        .append(VectorSessionToken.SegmentSeparator)
                        .append(regionProgress);
                this.sessionToken = sb.toString();
            }
        } else {
            this.sessionToken = sessionToken;
        }
    }

    public static boolean tryCreate(String sessionToken, ValueHolder<ISessionToken> parsedSessionToken) {
        ValueHolder<Long> versionHolder = ValueHolder.initialize(-1l);
        ValueHolder<Long> globalLsnHolder = ValueHolder.initialize(-1l);

        ValueHolder<UnmodifiableMap<Integer, Long>> localLsnByRegion = ValueHolder.initialize(null);

        if (VectorSessionToken.tryParseSessionToken(
                sessionToken,
                versionHolder,
                globalLsnHolder,
                localLsnByRegion)) {
            parsedSessionToken.v = new VectorSessionToken(versionHolder.v, globalLsnHolder.v, localLsnByRegion.v, sessionToken);
            return true;
        } else {
            return false;
        }
    }

    public long getLSN() {
        return this.globalLsn;
    }

    @Override
    public boolean equals(Object obj) {
        VectorSessionToken other = Utils.as(obj, VectorSessionToken.class);

        if (other == null) {
            return false;
        }

        return this.version == other.version
                && this.globalLsn == other.globalLsn
                && this.areRegionProgressEqual(other.localLsnByRegion);
    }

    public boolean isValid(ISessionToken otherSessionToken) throws CosmosClientException {
        VectorSessionToken other = Utils.as(otherSessionToken, VectorSessionToken.class);

        if (other == null) {
            throw new IllegalArgumentException("otherSessionToken");
        }

        if (other.version < this.version || other.globalLsn < this.globalLsn) {
            return false;
        }

        if (other.version == this.version && other.localLsnByRegion.size() != this.localLsnByRegion.size()) {
            throw new InternalServerErrorException(
                    String.format(RMResources.InvalidRegionsInSessionToken, this.sessionToken, other.sessionToken));
        }

        for (Map.Entry<Integer, Long> kvp : other.localLsnByRegion.entrySet()) {
            Integer regionId = kvp.getKey();
            long otherLocalLsn = kvp.getValue();
            ValueHolder<Long> localLsn = ValueHolder.initialize(-1l);


            if (!Utils.tryGetValue(this.localLsnByRegion, regionId, localLsn)) {
                // Region mismatch: other session token has progress for a region which is missing in this session token
                // Region mismatch can be ignored only if this session token version is smaller than other session token version
                if (this.version == other.version) {
                    throw new InternalServerErrorException(
                            String.format(RMResources.InvalidRegionsInSessionToken, this.sessionToken, other.sessionToken));
                } else {
                    // ignore missing region as other session token version > this session token version
                }
            } else {
                // region is present in both session tokens.
                if (otherLocalLsn < localLsn.v) {
                    return false;
                }
            }
        }

        return true;
    }

    // Merge is commutative operation, so a.Merge(b).Equals(b.Merge(a))
    public ISessionToken merge(ISessionToken obj) throws CosmosClientException {
        VectorSessionToken other = Utils.as(obj, VectorSessionToken.class);

        if (other == null) {
            throw new IllegalArgumentException("obj");
        }

        if (this.version == other.version && this.localLsnByRegion.size() != other.localLsnByRegion.size()) {
            throw new InternalServerErrorException(
                    String.format(RMResources.InvalidRegionsInSessionToken, this.sessionToken, other.sessionToken));
        }

        VectorSessionToken sessionTokenWithHigherVersion;
        VectorSessionToken sessionTokenWithLowerVersion;

        if (this.version < other.version) {
            sessionTokenWithLowerVersion = this;
            sessionTokenWithHigherVersion = other;
        } else {
            sessionTokenWithLowerVersion = other;
            sessionTokenWithHigherVersion = this;
        }

        Map<Integer, Long> highestLocalLsnByRegion = new HashMap<>();

        for (Map.Entry<Integer, Long> kvp : sessionTokenWithHigherVersion.localLsnByRegion.entrySet()) {
            Integer regionId = kvp.getKey();

            long localLsn1 = kvp.getValue();
            ValueHolder<Long> localLsn2 = ValueHolder.initialize(-1l);

            if (Utils.tryGetValue(sessionTokenWithLowerVersion.localLsnByRegion, regionId, localLsn2)) {
                highestLocalLsnByRegion.put(regionId, Math.max(localLsn1, localLsn2.v));
            } else if (this.version == other.version) {
                throw new InternalServerErrorException(
                        String.format(RMResources.InvalidRegionsInSessionToken, this.sessionToken, other.sessionToken));
            } else {
                highestLocalLsnByRegion.put(regionId, localLsn1);
            }
        }

        return new VectorSessionToken(
                Math.max(this.version, other.version),
                Math.max(this.globalLsn, other.globalLsn),
                (UnmodifiableMap) UnmodifiableMap.unmodifiableMap(highestLocalLsnByRegion));
    }

    public String convertToString() {
        return this.sessionToken;
    }

    private boolean areRegionProgressEqual(UnmodifiableMap<Integer, Long> other) {
        if (this.localLsnByRegion.size() != other.size()) {
            return false;
        }

        for (Map.Entry<Integer, Long> kvp : this.localLsnByRegion.entrySet()) {
            Integer regionId = kvp.getKey();
            ValueHolder<Long> localLsn1 = ValueHolder.initialize(kvp.getValue());
            ValueHolder<Long> localLsn2 = ValueHolder.initialize(-1l);

            if (Utils.tryGetValue(other, regionId, localLsn2)) {
                if (ObjectUtils.notEqual(localLsn1.v, localLsn2.v)) {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean tryParseSessionToken(
            String sessionToken,
            ValueHolder<Long> version,
            ValueHolder<Long> globalLsn,
            ValueHolder<UnmodifiableMap<Integer, Long>> localLsnByRegion) {
        version.v = 0L;
        localLsnByRegion.v = null;
        globalLsn.v = -1L;

        if (Strings.isNullOrEmpty(sessionToken)) {
            logger.warn("SESSION token is empty");
            return false;
        }

        String[] segments = StringUtils.split(sessionToken, VectorSessionToken.SegmentSeparator);

        if (segments.length < 2) {
            return false;
        }

        if (!tryParseLong(segments[0], version)
                || !tryParseLong(segments[1], globalLsn)) {
            logger.warn("Unexpected session token version number '{}' OR global lsn '{}'.", segments[0], segments[1]);
            return false;
        }

        Map<Integer, Long> lsnByRegion = new HashMap<>();

        for (int i = 2; i < segments.length; i++) {
            String regionSegment = segments[i];

            String[] regionIdWithLsn = StringUtils.split(regionSegment, VectorSessionToken.RegionProgressSeparator);

            if (regionIdWithLsn.length != 2) {
                logger.warn("Unexpected region progress segment length '{}' in session token.", regionIdWithLsn.length);
                return false;
            }

            ValueHolder<Integer> regionId = ValueHolder.initialize(0);
            ValueHolder<Long> localLsn = ValueHolder.initialize(-1l);

            if (!tryParseInt(regionIdWithLsn[0], regionId)
                    || !tryParseLong(regionIdWithLsn[1], localLsn)) {
                logger.warn("Unexpected region progress '{}' for region '{}' in session token.", regionIdWithLsn[0], regionIdWithLsn[1]);
                return false;
            }

            lsnByRegion.put(regionId.v, localLsn.v);
        }

        localLsnByRegion.v = (UnmodifiableMap) UnmodifiableMap.unmodifiableMap(lsnByRegion);
        return true;
    }

    private static boolean tryParseLong(String str, ValueHolder<Long> value) {
        try {
            value.v = Long.parseLong(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean tryParseInt(String str, ValueHolder<Integer> value) {
        try {
            value.v = Integer.parseInt(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
