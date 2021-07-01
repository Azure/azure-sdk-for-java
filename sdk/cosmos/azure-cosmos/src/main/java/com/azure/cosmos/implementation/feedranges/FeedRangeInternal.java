// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.feedranges;

import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.IRoutingMapProvider;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.MetadataDiagnosticsContext;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.routing.HexConvert;
import com.azure.cosmos.implementation.routing.Int128;
import com.azure.cosmos.implementation.routing.NumberPartitionKeyComponent;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.PartitionKeyDefinitionVersion;
import com.azure.cosmos.models.PartitionKind;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

@JsonDeserialize(using = FeedRangeInternalDeserializer.class)
public abstract class FeedRangeInternal extends JsonSerializable implements FeedRange {
    private final static Logger LOGGER = LoggerFactory.getLogger(FeedRangeInternal.class);
    private final static Long UINT64_TO_DOUBLE_MASK = Long.parseUnsignedLong("9223372036854775808");
    private final static Long UINT_MAX_VALUE = Long.parseUnsignedLong("4294967295");

    public static FeedRangeInternal convert(final FeedRange feedRange) {
        checkNotNull(feedRange, "Argument 'feedRange' must not be null");
        if (feedRange instanceof FeedRangeInternal) {
            return (FeedRangeInternal)feedRange;
        }

        String json = feedRange.toString();
        return fromBase64EncodedJsonString(json);
    }

    /**
     * Creates a range from a previously obtained string representation.
     *
     * @param base64EncodedJson A string representation of a feed range
     * @return A feed range
     */
    public static FeedRangeInternal fromBase64EncodedJsonString(String base64EncodedJson) {
        checkNotNull(base64EncodedJson, "Argument 'base64EncodedJson' must not be null");

        String json = new String(
            Base64.getUrlDecoder().decode(base64EncodedJson),
            StandardCharsets.UTF_8);

        FeedRangeInternal parsedRange = FeedRangeInternal.tryParse(json);

        if (parsedRange == null) {
            throw new IllegalArgumentException(
                String.format(
                    "The provided string '%s' does not represent any known format.",
                    json));
        }

        return parsedRange;
    }

    protected abstract Mono<Range<String>> getEffectiveRange(
        IRoutingMapProvider routingMapProvider,
        MetadataDiagnosticsContext metadataDiagnosticsCtx,
        Mono<Utils.ValueHolder<DocumentCollection>> collectionResolutionMono);

    public static Range<String> normalizeRange(Range<String> range) {
        if (range.isMinInclusive() && !range.isMaxInclusive()) {
            return range;
        }

        String min;
        String max;

        if (range.isMinInclusive()) {
            min = range.getMin();
        } else {
            min = addToEffectivePartitionKey(range.getMin(), -1);
        }

        if (!range.isMaxInclusive()) {
            max = range.getMax();
        } else {
            max = addToEffectivePartitionKey(range.getMax(), 1);
        }

        return new Range<>(min, max, true, false);
    }

    // Will return a normalized range with minInclusive and maxExclusive boundaries
    public Mono<Range<String>> getNormalizedEffectiveRange(
        IRoutingMapProvider routingMapProvider,
        MetadataDiagnosticsContext metadataDiagnosticsCtx,
        Mono<Utils.ValueHolder<DocumentCollection>> collectionResolutionMono
    ) {
        return this.getEffectiveRange(routingMapProvider, metadataDiagnosticsCtx, collectionResolutionMono)
                   .map(FeedRangeInternal::normalizeRange);
    }

    private static String addToEffectivePartitionKey(
        String effectivePartitionKey,
        int value) {

        checkArgument(
            value == 1 || value == -1,
            "Argument 'value' has invalid value - only 1 and -1 are allowed");

        byte[] blob = hexBinaryToByteArray(effectivePartitionKey);

        if (value == 1) {
            for (int i = blob.length - 1; i >= 0; i--) {
                if ((0xff & blob[i]) < 255) {
                    blob[i] = (byte)((0xff & blob[i]) + 1);
                    break;
                } else {
                    blob[i] = 0;
                }
            }
        } else {
            for (int i = blob.length - 1; i >= 0; i--) {
                if ((0xff & blob[i]) != 0) {
                    blob[i] = (byte)((0xff & blob[i]) - 1);
                    break;
                } else {
                    blob[i] = (byte)255;
                }
            }
        }

        return HexConvert.bytesToHex(blob);
    }

    public abstract Mono<List<String>> getPartitionKeyRanges(
        IRoutingMapProvider routingMapProvider,
        RxDocumentServiceRequest request,
        Mono<Utils.ValueHolder<DocumentCollection>> collectionResolutionMono);

    public abstract Mono<RxDocumentServiceRequest> populateFeedRangeFilteringHeaders(
        IRoutingMapProvider routingMapProvider,
        RxDocumentServiceRequest request,
        Mono<Utils.ValueHolder<DocumentCollection>> collectionResolutionMono);

    public void populatePropertyBag() {
        setProperties(this, false);
    }

    @Override
    public String toString() {
        String json = this.toJson();

        if (json == null) {
            return "";
        }

        return Base64.getUrlEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    public abstract void removeProperties(JsonSerializable serializable);

    public void setProperties(
        JsonSerializable serializable,
        boolean populateProperties) {

        if (populateProperties) {
            super.populatePropertyBag();
        }
    }

    public static FeedRangeInternal tryParse(final String jsonString) {
        checkNotNull(jsonString, "Argument 'jsonString' must not be null");
        final ObjectMapper mapper = Utils.getSimpleObjectMapper();

        try {
            return mapper.readValue(jsonString, FeedRangeInternal.class);
        } catch (final IOException ioError) {
            LOGGER.debug("Failed to parse feed range JSON {}", jsonString, ioError);
            return null;
        }
    }

    public Mono<List<FeedRangeEpkImpl>> trySplit(
        IRoutingMapProvider routingMapProvider,
        MetadataDiagnosticsContext metadataDiagnosticsCtx,
        Mono<Utils.ValueHolder<DocumentCollection>> collectionResolutionMono,
        int targetedSplitCount) {

        return Mono.zip(
            this.getNormalizedEffectiveRange(
                routingMapProvider,
                metadataDiagnosticsCtx,
                collectionResolutionMono),
            collectionResolutionMono)
                   .map(tuple -> {

                       Range<String> effectiveRange = tuple.getT1();
                       Utils.ValueHolder<DocumentCollection> collectionValueHolder = tuple.getT2();

                       if (collectionValueHolder.v == null) {
                           throw new IllegalStateException("Collection should have been resolved.");
                       }

                       PartitionKeyDefinition pkDefinition =
                           collectionValueHolder.v.getPartitionKey();

                       if (targetedSplitCount <= 1 ||
                           effectiveRange.isSingleValue() ||
                           // splitting ranges into sub ranges only possible for hash partitioning
                           pkDefinition.getKind() != PartitionKind.HASH) {

                           return Collections.singletonList(new FeedRangeEpkImpl(effectiveRange));
                       }

                       PartitionKeyDefinitionVersion effectivePKVersion =
                           pkDefinition.getVersion() != null
                           ? pkDefinition.getVersion()
                           : PartitionKeyDefinitionVersion.V1;
                       switch (effectivePKVersion) {
                           case V1:
                               return trySplitWithHashV1(effectiveRange, targetedSplitCount);

                           case V2:
                               return trySplitWithHashV2(effectiveRange, targetedSplitCount);

                           default:
                               return Collections.singletonList(new FeedRangeEpkImpl(effectiveRange));
                       }
                   });
    }

    static List<FeedRangeEpkImpl> trySplitWithHashV1(
        Range<String> effectiveRange,
        int targetedSplitCount) {

        long min = 0;
        long max = UINT_MAX_VALUE;

        if (!effectiveRange.getMin().equalsIgnoreCase(
            PartitionKeyInternalHelper.MinimumInclusiveEffectivePartitionKey)) {

            min = fromHexEncodedBinaryString(effectiveRange.getMin());
        }

        if (!effectiveRange.getMax().equalsIgnoreCase(
            PartitionKeyInternalHelper.MaximumExclusiveEffectivePartitionKey)) {

            max = fromHexEncodedBinaryString(effectiveRange.getMax());
        }

        String minRange = effectiveRange.getMin();
        long diff = max - min;
        List<FeedRangeEpkImpl> splitFeedRanges = new ArrayList<>(targetedSplitCount);
        for (int i = 1; i < targetedSplitCount; i++) {
            long splitPoint = min + (i * (diff / targetedSplitCount));
            String maxRange = PartitionKeyInternalHelper.toHexEncodedBinaryString(
                new NumberPartitionKeyComponent[] {
                    new NumberPartitionKeyComponent(splitPoint)
                });
            splitFeedRanges.add(
                new FeedRangeEpkImpl(
                    new Range<>(
                        minRange,
                        maxRange,
                        i > 1 || effectiveRange.isMinInclusive(),
                        false)));

            minRange = maxRange;
        }

        splitFeedRanges.add(
            new FeedRangeEpkImpl(
                new Range<>(
                    minRange,
                    effectiveRange.getMax(),
                    true,
                    effectiveRange.isMaxInclusive())));

        return splitFeedRanges;
    }

    static List<FeedRangeEpkImpl> trySplitWithHashV2(
        Range<String> effectiveRange,
        int targetedSplitCount) {

        Int128 min = new Int128(0);
        if (!effectiveRange.getMin().equalsIgnoreCase(
            PartitionKeyInternalHelper.MinimumInclusiveEffectivePartitionKey)) {

            byte[] minBytes = hexBinaryToByteArray(effectiveRange.getMin());
            min = new Int128(minBytes);
        }

        Int128 max = PartitionKeyInternalHelper.MaxHashV2Value;
        if (!effectiveRange.getMax().equalsIgnoreCase(
            PartitionKeyInternalHelper.MaximumExclusiveEffectivePartitionKey)) {

            byte[] maxBytes = hexBinaryToByteArray(effectiveRange.getMax());
            max = new Int128(maxBytes);
        }

        if (Int128.lt(
            Int128.subtract(max, min),
            new Int128(targetedSplitCount))) {

            return Collections.singletonList(new FeedRangeEpkImpl(effectiveRange));
        }

        String minRange = effectiveRange.getMin();
        Int128 diff = Int128.subtract(max, min);
        Int128 splitCountInt128 = new Int128(targetedSplitCount);
        List<FeedRangeEpkImpl> splitFeedRanges = new ArrayList<>(targetedSplitCount);
        for (int i = 1; i < targetedSplitCount; i++) {
            byte[] currentBlob = Int128.add(
                min,
                Int128.multiply(new Int128(i), Int128.div(diff, splitCountInt128))
            ).bytes();

            String maxRange = HexConvert.bytesToHex(currentBlob);
            splitFeedRanges.add(
                new FeedRangeEpkImpl(
                    new Range<>(
                        minRange,
                        maxRange,
                        i > 1 || effectiveRange.isMinInclusive(),
                        false)));

            minRange = maxRange;
        }

        splitFeedRanges.add(
            new FeedRangeEpkImpl(
                new Range<>(
                    minRange,
                    effectiveRange.getMax(),
                    true,
                    effectiveRange.isMaxInclusive())));

        return splitFeedRanges;
    }

    private static double decodeDoubleFromUInt64Long(long value) {
        value = (value < UINT64_TO_DOUBLE_MASK) ? -value : value ^ UINT64_TO_DOUBLE_MASK;
        return Double.longBitsToDouble(value);
    }

    static long fromHexEncodedBinaryString(String hexBinary) {
        byte[] byteString = hexBinaryToByteArray(hexBinary);
        if (byteString.length < 2 || byteString[0] != 5) {
            throw new IllegalStateException("Invalid hex-byteString");
        }
        int byteStringOffset = 1;
        int offset = 64;
        long payload = 0;

        // Decode first 8-bit chunk
        offset -= 8;
        payload |= (((long)byteString[byteStringOffset++]) & 0x00FF) << offset;
        // Decode remaining 7-bit chunks
        while (true) {
            if (byteStringOffset >= byteString.length) {
                throw new IllegalStateException("Incorrect byte string without termination");
            }

            byte currentByte = byteString[byteStringOffset++];

            offset -= 7;
            payload |= (((((long)(currentByte)) & 0x00FF) >> 1) << offset);

            if ((currentByte & 0x01) == 0) {
                break;
            }
        }

        return (long)decodeDoubleFromUInt64Long(payload);
    }

    private static byte[] hexBinaryToByteArray(String hexBinary) {
        checkNotNull(hexBinary, "Argument 'hexBinary' must not be null.");

        int len = hexBinary.length();
        checkArgument(
            (len & 0x01) == 0,
            "Argument 'hexBinary' must not have odd number of characters.");

        byte[] blob = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            blob[i / 2] = (byte)((Character.digit(hexBinary.charAt(i), 16) << 4)
                + Character.digit(hexBinary.charAt(i + 1), 16));
        }

        return blob;
    }
}
