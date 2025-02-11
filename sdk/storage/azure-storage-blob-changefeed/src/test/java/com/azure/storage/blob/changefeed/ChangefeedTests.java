// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob.changefeed;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor;
import com.azure.storage.blob.changefeed.implementation.models.SegmentCursor;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.ListBlobsOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ChangefeedTests {
    private BlobContainerAsyncClient mockContainer;
    private SegmentFactory mockSegmentFactory;
    private Segment mockSegment;
    private BlobAsyncClient mockMetadataClient;
    private static final String URL_HOST = "testaccount.blob.core.windows.net";

    @BeforeEach
    public void setup() {
        mockContainer = mock(BlobContainerAsyncClient.class);
        mockSegmentFactory = mock(SegmentFactory.class);

        mockMetadataClient = mock(BlobAsyncClient.class);
        when(mockContainer.getBlobContainerUrl())
            .thenReturn("https://testaccount.blob.core.windows.net/$blobchangefeed");
        when(mockContainer.exists()).thenReturn(Mono.just(true));
        when(mockContainer.getBlobAsyncClient(anyString())).thenReturn(mockMetadataClient);
        when(mockMetadataClient.download())
            .thenReturn(MockedChangefeedResources.readFile("changefeed_manifest.json", getClass()));
        when(mockContainer.listBlobsByHierarchy(anyString())).thenReturn(new PagedFlux<>(yearSupplier));
        Function<String, ArgumentMatcher<ListBlobsOptions>> isYear
            = year -> options -> options != null && options.getPrefix().equals("idx/segments/" + year);
        when(mockContainer.listBlobs(argThat(isYear.apply("2017"))))
            .thenReturn(new PagedFlux<>(segmentSupplier.apply("2017")));
        when(mockContainer.listBlobs(argThat(isYear.apply("2018"))))
            .thenReturn(new PagedFlux<>(segmentSupplier.apply("2018")));
        when(mockContainer.listBlobs(argThat(isYear.apply("2019"))))
            .thenReturn(new PagedFlux<>(segmentSupplier.apply("2019")));
        when(mockContainer.listBlobs(argThat(isYear.apply("2020"))))
            .thenReturn(new PagedFlux<>(segmentSupplier.apply("2020")));

        mockSegment = mock(Segment.class);

        when(mockSegmentFactory.getSegment(anyString(), any(ChangefeedCursor.class), nullable(SegmentCursor.class)))
            .thenReturn(mockSegment);
        when(mockSegment.getEvents()).thenReturn(Flux.empty());
    }

    @Test
    public void changefeedDoesNotExist() {
        when(mockContainer.exists()).thenReturn(Mono.just(false));

        ChangefeedFactory changefeedFactory = new ChangefeedFactory(mockSegmentFactory, mockContainer);
        Changefeed changefeed = changefeedFactory.getChangefeed(null, null);

        StepVerifier.create(changefeed.getEvents())
            .verifyErrorMessage("Changefeed has not been enabled for this account.");
        verify(mockContainer).exists();
    }

    @Test
    public void changefeedMetadataError() {
        BlobAsyncClient mockMetadataClient = mock(BlobAsyncClient.class);
        when(mockContainer.exists()).thenReturn(Mono.just(true));
        when(mockContainer.getBlobAsyncClient(anyString())).thenReturn(mockMetadataClient);
        when(mockMetadataClient.download()).thenReturn(Flux.just(ByteBuffer.wrap("not json metadata".getBytes())));

        ChangefeedFactory changefeedFactory = new ChangefeedFactory(mockSegmentFactory, mockContainer);
        Changefeed changefeed = changefeedFactory.getChangefeed(null, null);

        StepVerifier.create(changefeed.getEvents()).verifyError(UncheckedIOException.class);
        verify(mockContainer).exists();
        verify(mockContainer).getBlobAsyncClient(Changefeed.METADATA_SEGMENT_PATH);
        verify(mockMetadataClient).download();
    }

    @ParameterizedTest
    @MethodSource("changefeedLastConsumablePopulatedSupplier")
    public void changefeedLastConsumablePopulated(OffsetDateTime endTime, OffsetDateTime safeEndTime) {
        ChangefeedFactory changefeedFactory = new ChangefeedFactory(mockSegmentFactory, mockContainer);
        Changefeed changefeed
            = changefeedFactory.getChangefeed(null, endTime); /* End time definitely later than this.endTime */

        StepVerifier.create(changefeed.populateLastConsumable()).expectNext(safeEndTime).verifyComplete();

        verify(mockContainer).getBlobAsyncClient(Changefeed.METADATA_SEGMENT_PATH);
        verify(mockMetadataClient).download();
    }

    private static Stream<Arguments> changefeedLastConsumablePopulatedSupplier() {
        // endTime, safeEndTime
        return Stream.of(Arguments.of(OffsetDateTime.MAX, OffsetDateTime.of(2020, 5, 4, 20, 0, 0, 0, ZoneOffset.UTC)), // endTime is after. Limited by lastConsumable.
            Arguments.of(OffsetDateTime.of(2019, 5, 4, 19, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2019, 5, 4, 19, 0, 0, 0, ZoneOffset.UTC)) // endTime is before. Limited by endTime.
        );
    }

    /* 4 years 2017-2020. */
    Supplier<Mono<PagedResponse<BlobItem>>> yearSupplier
        = () -> Mono
            .just(new PagedResponseBase<>(null, 200, null,
                Arrays.asList(new BlobItem().setName("idx/segments/2017"), new BlobItem().setName("idx/segments/2018"),
                    new BlobItem().setName("idx/segments/2019"), new BlobItem().setName("idx/segments/2020")),
                null, null));

    /* 4 segments 12:00, 3:00, 5:00, 6:00 */
    Function<String, Supplier<Mono<PagedResponse<BlobItem>>>> segmentSupplier
        = year -> () -> Mono.just(new PagedResponseBase<>(null, 200, null,
            Arrays.asList(new BlobItem().setName("idx/segments/" + year + "/01/01/1200/meta.json"),
                new BlobItem().setName("idx/segments/" + year + "/01/01/0300/meta.json"),
                new BlobItem().setName("idx/segments/" + year + "/01/01/0500/meta.json"),
                new BlobItem().setName("idx/segments/" + year + "/01/01/0600/meta.json")),
            null, null));

    /* No options. */
    @Test
    public void changefeedMin() {
        ChangefeedFactory changefeedFactory = new ChangefeedFactory(mockSegmentFactory, mockContainer);
        Changefeed changefeed = spy(changefeedFactory.getChangefeed(null, null));

        StepVerifier.create(changefeed.getEvents()).verifyComplete();

        verify(mockContainer).exists();
        verify(mockContainer).getBlobAsyncClient(Changefeed.METADATA_SEGMENT_PATH);
        verify(mockMetadataClient).download();
        verify(mockContainer).listBlobsByHierarchy(Changefeed.SEGMENT_PREFIX);
        ArgumentCaptor<ListBlobsOptions> options = ArgumentCaptor.forClass(ListBlobsOptions.class);
        verify(mockContainer, times(4)).listBlobs(options.capture());
        verifyEvents(OffsetDateTime.MAX, Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15), null);
        verifyYears(options.getAllValues(),
            Arrays.asList("idx/segments/2017", "idx/segments/2018", "idx/segments/2019", "idx/segments/2020"));
        verify(mockSegment, times(16)).getEvents();
        //        verify(changefeed).listYears(OffsetDateTime.of(2020, 5, 4, 19, 0, 0, 0, ZoneOffset.UTC));
    }

    @ParameterizedTest
    @MethodSource("changefeedStartTimeSupplier")
    public void changefeedStartTime(OffsetDateTime startTime, List<String> yearPaths, List<Integer> eventNums) {
        ChangefeedFactory changefeedFactory = new ChangefeedFactory(mockSegmentFactory, mockContainer);
        Changefeed changefeed = changefeedFactory.getChangefeed(startTime, null);

        StepVerifier.create(changefeed.getEvents()).verifyComplete();

        verify(mockContainer).exists();
        verify(mockContainer).getBlobAsyncClient(Changefeed.METADATA_SEGMENT_PATH);
        verify(mockMetadataClient).download();
        verify(mockContainer).listBlobsByHierarchy(Changefeed.SEGMENT_PREFIX);
        ArgumentCaptor<ListBlobsOptions> options = ArgumentCaptor.forClass(ListBlobsOptions.class);
        verify(mockContainer, times(yearPaths.size())).listBlobs(options.capture());
        verifyEvents(OffsetDateTime.MAX, eventNums, null);
        verifyYears(options.getAllValues(), yearPaths);
        verify(mockSegment, times(eventNums.size())).getEvents();
    }

    private static Stream<Arguments> changefeedStartTimeSupplier() {
        // startTime, yearPaths, eventNums
        return Stream.of(
            Arguments.of(OffsetDateTime.of(2016, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                Arrays.asList("idx/segments/2017", "idx/segments/2018", "idx/segments/2019", "idx/segments/2020"),
                Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)),
            Arguments.of(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                Arrays.asList("idx/segments/2017", "idx/segments/2018", "idx/segments/2019", "idx/segments/2020"),
                Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)),
            Arguments.of(OffsetDateTime.of(2018, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                Arrays.asList("idx/segments/2018", "idx/segments/2019", "idx/segments/2020"),
                Arrays.asList(4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)),
            Arguments.of(OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                Arrays.asList("idx/segments/2019", "idx/segments/2020"), Arrays.asList(8, 9, 10, 11, 12, 13, 14, 15)),
            Arguments.of(OffsetDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), Arrays.asList("idx/segments/2020"),
                Arrays.asList(12, 13, 14, 15)),
            Arguments.of(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), Arrays.asList(), Arrays.asList()),
            Arguments.of(OffsetDateTime.of(2017, 1, 1, 2, 0, 0, 0, ZoneOffset.UTC),
                Arrays.asList("idx/segments/2017", "idx/segments/2018", "idx/segments/2019", "idx/segments/2020"),
                Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)), // All segments taken from year.
            Arguments.of(OffsetDateTime.of(2018, 1, 2, 0, 0, 0, 0, ZoneOffset.UTC),
                Arrays.asList("idx/segments/2018", "idx/segments/2019", "idx/segments/2020"),
                Arrays.asList(8, 9, 10, 11, 12, 13, 14, 15)), // No segments taken from year.
            Arguments.of(OffsetDateTime.of(2019, 1, 1, 6, 0, 0, 0, ZoneOffset.UTC),
                Arrays.asList("idx/segments/2019", "idx/segments/2020"), Arrays.asList(10, 11, 12, 13, 14, 15)), // Partial segments taken from year. Checks isEqual.
            Arguments.of(OffsetDateTime.of(2020, 1, 1, 8, 0, 0, 0, ZoneOffset.UTC), Arrays.asList("idx/segments/2020"),
                Arrays.asList(15)) // Partial segments taken from year. Checks is strictly before.
        );
    }

    @ParameterizedTest
    @MethodSource("changefeedEndTimeSupplier")
    public void changefeedEndTime(OffsetDateTime endTime, List<String> yearPaths, List<Integer> eventNums) {
        ChangefeedFactory changefeedFactory = new ChangefeedFactory(mockSegmentFactory, mockContainer);
        Changefeed changefeed = changefeedFactory.getChangefeed(null, endTime);

        StepVerifier.create(changefeed.getEvents()).verifyComplete();

        verify(mockContainer).exists();
        verify(mockContainer).getBlobAsyncClient(Changefeed.METADATA_SEGMENT_PATH);
        verify(mockMetadataClient).download();
        verify(mockContainer).listBlobsByHierarchy(Changefeed.SEGMENT_PREFIX);
        ArgumentCaptor<ListBlobsOptions> options = ArgumentCaptor.forClass(ListBlobsOptions.class);
        verify(mockContainer, times(yearPaths.size())).listBlobs(options.capture());
        verifyEvents(endTime, eventNums, null);
        verifyYears(options.getAllValues(), yearPaths);
        verify(mockSegment, times(eventNums.size())).getEvents();
    }

    private static Stream<Arguments> changefeedEndTimeSupplier() {
        // endTime, yearPaths, eventNums
        return Stream.of(
            Arguments.of(OffsetDateTime.of(2016, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), Arrays.asList(), Arrays.asList()),
            Arguments.of(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), Arrays.asList("idx/segments/2017"),
                Arrays.asList()),
            Arguments.of(OffsetDateTime.of(2018, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                Arrays.asList("idx/segments/2017", "idx/segments/2018"), Arrays.asList(0, 1, 2, 3)),
            Arguments.of(OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                Arrays.asList("idx/segments/2017", "idx/segments/2018", "idx/segments/2019"),
                Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7)),
            Arguments.of(OffsetDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                Arrays.asList("idx/segments/2017", "idx/segments/2018", "idx/segments/2019", "idx/segments/2020"),
                Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)),
            Arguments.of(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                Arrays.asList("idx/segments/2017", "idx/segments/2018", "idx/segments/2019", "idx/segments/2020"),
                Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)),
            Arguments.of(OffsetDateTime.of(2017, 12, 12, 2, 0, 0, 0, ZoneOffset.UTC),
                Arrays.asList("idx/segments/2017"), Arrays.asList(0, 1, 2, 3)), // All segments taken from year.
            Arguments.of(OffsetDateTime.of(2018, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                Arrays.asList("idx/segments/2017", "idx/segments/2018"), Arrays.asList(0, 1, 2, 3)), // No segments taken from year.
            Arguments.of(OffsetDateTime.of(2019, 1, 1, 6, 0, 0, 0, ZoneOffset.UTC),
                Arrays.asList("idx/segments/2017", "idx/segments/2018", "idx/segments/2019"),
                Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)), // Partial segments taken from year. Checks isEqual.
            Arguments.of(OffsetDateTime.of(2020, 1, 1, 8, 0, 0, 0, ZoneOffset.UTC),
                Arrays.asList("idx/segments/2017", "idx/segments/2018", "idx/segments/2019", "idx/segments/2020"),
                Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14)) // Partial segments taken from year. Checks is strictly before.
        );
    }

    private static final List<ChangefeedCursor> CURSOR_START = Arrays.asList(
        new ChangefeedCursor(URL_HOST, OffsetDateTime.MAX).toSegmentCursor("idx/segments/2017/01/01/0300/meta.json",
            null),
        new ChangefeedCursor(URL_HOST, OffsetDateTime.MAX).toSegmentCursor("idx/segments/2017/01/01/0500/meta.json",
            null),
        new ChangefeedCursor(URL_HOST, OffsetDateTime.MAX).toSegmentCursor("idx/segments/2017/01/01/0600/meta.json",
            null),
        new ChangefeedCursor(URL_HOST, OffsetDateTime.MAX).toSegmentCursor("idx/segments/2017/01/01/1200/meta.json",
            null),
        new ChangefeedCursor(URL_HOST, OffsetDateTime.MAX).toSegmentCursor("idx/segments/2018/01/01/0300/meta.json",
            null),
        new ChangefeedCursor(URL_HOST, OffsetDateTime.MAX).toSegmentCursor("idx/segments/2018/01/01/0500/meta.json",
            null),
        new ChangefeedCursor(URL_HOST, OffsetDateTime.MAX).toSegmentCursor("idx/segments/2018/01/01/0600/meta.json",
            null),
        new ChangefeedCursor(URL_HOST, OffsetDateTime.MAX).toSegmentCursor("idx/segments/2018/01/01/1200/meta.json",
            null),
        new ChangefeedCursor(URL_HOST, OffsetDateTime.MAX).toSegmentCursor("idx/segments/2019/01/01/0300/meta.json",
            null),
        new ChangefeedCursor(URL_HOST, OffsetDateTime.MAX).toSegmentCursor("idx/segments/2019/01/01/0500/meta.json",
            null),
        new ChangefeedCursor(URL_HOST, OffsetDateTime.MAX).toSegmentCursor("idx/segments/2019/01/01/0600/meta.json",
            null),
        new ChangefeedCursor(URL_HOST, OffsetDateTime.MAX).toSegmentCursor("idx/segments/2019/01/01/1200/meta.json",
            null),
        new ChangefeedCursor(URL_HOST, OffsetDateTime.MAX).toSegmentCursor("idx/segments/2020/01/01/0300/meta.json",
            null),
        new ChangefeedCursor(URL_HOST, OffsetDateTime.MAX).toSegmentCursor("idx/segments/2020/01/01/0500/meta.json",
            null),
        new ChangefeedCursor(URL_HOST, OffsetDateTime.MAX).toSegmentCursor("idx/segments/2020/01/01/0600/meta.json",
            null),
        new ChangefeedCursor(URL_HOST, OffsetDateTime.MAX).toSegmentCursor("idx/segments/2020/01/01/1200/meta.json",
            null));

    @ParameterizedTest
    @MethodSource("changefeedCursorStartTimeSupplier")
    public void changefeedCursorStartTime(int cursorNum, List<String> yearPaths, List<Integer> eventNums) {
        ChangefeedFactory changefeedFactory = new ChangefeedFactory(mockSegmentFactory, mockContainer);
        Changefeed changefeed = changefeedFactory.getChangefeed(CURSOR_START.get(cursorNum).serialize());

        StepVerifier.create(changefeed.getEvents()).verifyComplete();

        verify(mockContainer).exists();
        verify(mockContainer).getBlobAsyncClient(Changefeed.METADATA_SEGMENT_PATH);
        verify(mockMetadataClient).download();
        verify(mockContainer).listBlobsByHierarchy(Changefeed.SEGMENT_PREFIX);
        ArgumentCaptor<ListBlobsOptions> options = ArgumentCaptor.forClass(ListBlobsOptions.class);
        verify(mockContainer, times(yearPaths.size())).listBlobs(options.capture());
        verifyEvents(OffsetDateTime.MAX, eventNums, CURSOR_START.get(cursorNum).getCurrentSegmentCursor());
        verifyYears(options.getAllValues(), yearPaths);
        verify(mockSegment, times(eventNums.size())).getEvents();
    }

    private static Stream<Arguments> changefeedCursorStartTimeSupplier() {
        // cursorNum, yearPaths, eventNums
        return Stream.of(
            Arguments.of(0,
                Arrays.asList("idx/segments/2017", "idx/segments/2018", "idx/segments/2019", "idx/segments/2020"),
                Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)),
            Arguments.of(1,
                Arrays.asList("idx/segments/2017", "idx/segments/2018", "idx/segments/2019", "idx/segments/2020"),
                Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)),
            Arguments.of(2,
                Arrays.asList("idx/segments/2017", "idx/segments/2018", "idx/segments/2019", "idx/segments/2020"),
                Arrays.asList(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)),
            Arguments.of(3,
                Arrays.asList("idx/segments/2017", "idx/segments/2018", "idx/segments/2019", "idx/segments/2020"),
                Arrays.asList(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)),
            Arguments.of(4, Arrays.asList("idx/segments/2018", "idx/segments/2019", "idx/segments/2020"),
                Arrays.asList(4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)),
            Arguments.of(5, Arrays.asList("idx/segments/2018", "idx/segments/2019", "idx/segments/2020"),
                Arrays.asList(5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)),
            Arguments.of(6, Arrays.asList("idx/segments/2018", "idx/segments/2019", "idx/segments/2020"),
                Arrays.asList(6, 7, 8, 9, 10, 11, 12, 13, 14, 15)),
            Arguments.of(7, Arrays.asList("idx/segments/2018", "idx/segments/2019", "idx/segments/2020"),
                Arrays.asList(7, 8, 9, 10, 11, 12, 13, 14, 15)),
            Arguments.of(8, Arrays.asList("idx/segments/2019", "idx/segments/2020"),
                Arrays.asList(8, 9, 10, 11, 12, 13, 14, 15)),
            Arguments.of(9, Arrays.asList("idx/segments/2019", "idx/segments/2020"),
                Arrays.asList(9, 10, 11, 12, 13, 14, 15)),
            Arguments.of(10, Arrays.asList("idx/segments/2019", "idx/segments/2020"),
                Arrays.asList(10, 11, 12, 13, 14, 15)),
            Arguments.of(11, Arrays.asList("idx/segments/2019", "idx/segments/2020"),
                Arrays.asList(11, 12, 13, 14, 15)),
            Arguments.of(12, Arrays.asList("idx/segments/2020"), Arrays.asList(12, 13, 14, 15)),
            Arguments.of(13, Arrays.asList("idx/segments/2020"), Arrays.asList(13, 14, 15)),
            Arguments.of(14, Arrays.asList("idx/segments/2020"), Arrays.asList(14, 15)),
            Arguments.of(15, Arrays.asList("idx/segments/2020"), Arrays.asList(15)));
    }

    private static final List<ChangefeedCursor> CURSOR_END = Arrays.asList(
        new ChangefeedCursor(URL_HOST, OffsetDateTime.of(2017, 1, 1, 3, 0, 0, 0, ZoneOffset.UTC))
            .toSegmentCursor("idx/segments/2017/01/01/0300/meta.json", null),
        new ChangefeedCursor(URL_HOST, OffsetDateTime.of(2017, 1, 1, 5, 0, 0, 0, ZoneOffset.UTC))
            .toSegmentCursor("idx/segments/2017/01/01/0300/meta.json", null),
        new ChangefeedCursor(URL_HOST, OffsetDateTime.of(2017, 1, 1, 6, 0, 0, 0, ZoneOffset.UTC))
            .toSegmentCursor("idx/segments/2017/01/01/0300/meta.json", null),
        new ChangefeedCursor(URL_HOST, OffsetDateTime.of(2017, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC))
            .toSegmentCursor("idx/segments/2017/01/01/0300/meta.json", null),
        new ChangefeedCursor(URL_HOST, OffsetDateTime.of(2018, 1, 1, 3, 0, 0, 0, ZoneOffset.UTC))
            .toSegmentCursor("idx/segments/2017/01/01/0300/meta.json", null),
        new ChangefeedCursor(URL_HOST, OffsetDateTime.of(2018, 1, 1, 5, 0, 0, 0, ZoneOffset.UTC))
            .toSegmentCursor("idx/segments/2017/01/01/0300/meta.json", null),
        new ChangefeedCursor(URL_HOST, OffsetDateTime.of(2018, 1, 1, 6, 0, 0, 0, ZoneOffset.UTC))
            .toSegmentCursor("idx/segments/2017/01/01/0300/meta.json", null),
        new ChangefeedCursor(URL_HOST, OffsetDateTime.of(2018, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC))
            .toSegmentCursor("idx/segments/2017/01/01/0300/meta.json", null),
        new ChangefeedCursor(URL_HOST, OffsetDateTime.of(2019, 1, 1, 3, 0, 0, 0, ZoneOffset.UTC))
            .toSegmentCursor("idx/segments/2017/01/01/0300/meta.json", null),
        new ChangefeedCursor(URL_HOST, OffsetDateTime.of(2019, 1, 1, 5, 0, 0, 0, ZoneOffset.UTC))
            .toSegmentCursor("idx/segments/2017/01/01/0300/meta.json", null),
        new ChangefeedCursor(URL_HOST, OffsetDateTime.of(2019, 1, 1, 6, 0, 0, 0, ZoneOffset.UTC))
            .toSegmentCursor("idx/segments/2017/01/01/0300/meta.json", null),
        new ChangefeedCursor(URL_HOST, OffsetDateTime.of(2019, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC))
            .toSegmentCursor("idx/segments/2017/01/01/0300/meta.json", null),
        new ChangefeedCursor(URL_HOST, OffsetDateTime.of(2020, 1, 1, 3, 0, 0, 0, ZoneOffset.UTC))
            .toSegmentCursor("idx/segments/2017/01/01/0300/meta.json", null),
        new ChangefeedCursor(URL_HOST, OffsetDateTime.of(2020, 1, 1, 5, 0, 0, 0, ZoneOffset.UTC))
            .toSegmentCursor("idx/segments/2017/01/01/0300/meta.json", null),
        new ChangefeedCursor(URL_HOST, OffsetDateTime.of(2020, 1, 1, 6, 0, 0, 0, ZoneOffset.UTC))
            .toSegmentCursor("idx/segments/2017/01/01/0300/meta.json", null),
        new ChangefeedCursor(URL_HOST, OffsetDateTime.of(2020, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC))
            .toSegmentCursor("idx/segments/2017/01/01/0300/meta.json", null));

    @ParameterizedTest
    @MethodSource("changefeedCursorEndTimeSupplier")
    public void changefeedCursorEndTime(int cursorNum, List<String> yearPaths, List<Integer> eventNums) {
        ChangefeedFactory changefeedFactory = new ChangefeedFactory(mockSegmentFactory, mockContainer);
        Changefeed changefeed = changefeedFactory.getChangefeed(CURSOR_END.get(cursorNum).serialize());

        StepVerifier.create(changefeed.getEvents()).verifyComplete();

        verify(mockContainer).exists();
        verify(mockContainer).getBlobAsyncClient(Changefeed.METADATA_SEGMENT_PATH);
        verify(mockMetadataClient).download();
        verify(mockContainer).listBlobsByHierarchy(Changefeed.SEGMENT_PREFIX);
        ArgumentCaptor<ListBlobsOptions> options = ArgumentCaptor.forClass(ListBlobsOptions.class);
        verify(mockContainer, times(yearPaths.size())).listBlobs(options.capture());
        verifyEvents(CURSOR_END.get(cursorNum).getEndTime(), eventNums,
            CURSOR_END.get(cursorNum).getCurrentSegmentCursor());
        verifyYears(options.getAllValues(), yearPaths);
        verify(mockSegment, times(eventNums.size())).getEvents();
    }

    public static Stream<Arguments> changefeedCursorEndTimeSupplier() {
        // cursorNum, yearPaths, eventNums
        return Stream.of(Arguments.of(0, Arrays.asList("idx/segments/2017"), Arrays.asList()),
            Arguments.of(1, Arrays.asList("idx/segments/2017"), Arrays.asList(0)),
            Arguments.of(2, Arrays.asList("idx/segments/2017"), Arrays.asList(0, 1)),
            Arguments.of(3, Arrays.asList("idx/segments/2017"), Arrays.asList(0, 1, 2)),
            Arguments.of(4, Arrays.asList("idx/segments/2017", "idx/segments/2018"), Arrays.asList(0, 1, 2, 3)),
            Arguments.of(5, Arrays.asList("idx/segments/2017", "idx/segments/2018"), Arrays.asList(0, 1, 2, 3, 4)),
            Arguments.of(6, Arrays.asList("idx/segments/2017", "idx/segments/2018"), Arrays.asList(0, 1, 2, 3, 4, 5)),
            Arguments.of(7, Arrays.asList("idx/segments/2017", "idx/segments/2018"),
                Arrays.asList(0, 1, 2, 3, 4, 5, 6)),
            Arguments.of(8, Arrays.asList("idx/segments/2017", "idx/segments/2018", "idx/segments/2019"),
                Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7)),
            Arguments.of(9, Arrays.asList("idx/segments/2017", "idx/segments/2018", "idx/segments/2019"),
                Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8)),
            Arguments.of(10, Arrays.asList("idx/segments/2017", "idx/segments/2018", "idx/segments/2019"),
                Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)),
            Arguments.of(11, Arrays.asList("idx/segments/2017", "idx/segments/2018", "idx/segments/2019"),
                Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)),
            Arguments.of(12,
                Arrays.asList("idx/segments/2017", "idx/segments/2018", "idx/segments/2019", "idx/segments/2020"),
                Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)),
            Arguments.of(13,
                Arrays.asList("idx/segments/2017", "idx/segments/2018", "idx/segments/2019", "idx/segments/2020"),
                Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)),
            Arguments.of(14,
                Arrays.asList("idx/segments/2017", "idx/segments/2018", "idx/segments/2019", "idx/segments/2020"),
                Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13)),
            Arguments.of(15,
                Arrays.asList("idx/segments/2017", "idx/segments/2018", "idx/segments/2019", "idx/segments/2020"),
                Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14)));
    }

    // TODO (gapra) : Maybe add some that do both endTime + startTime

    private static void verifyYears(List<ListBlobsOptions> captured, List<String> yearPaths) {
        for (String yearPath : yearPaths) {
            assertEquals(yearPath, captured.get(0).getPrefix());
            captured.remove(0);
        }

        assertEquals(0, captured.size());
    }

    private void verifyEvents(OffsetDateTime endTime, List<Integer> eventNum, SegmentCursor userCursor) {
        if (eventNum.contains(0)) {
            verify(mockSegmentFactory).getSegment("idx/segments/2017/01/01/0300/meta.json",
                new ChangefeedCursor(URL_HOST, endTime).toSegmentCursor("idx/segments/2017/01/01/0300/meta.json", null),
                eventNum.get(0) == 0 ? userCursor : null);
        }

        if (eventNum.contains(1)) {
            verify(mockSegmentFactory).getSegment("idx/segments/2017/01/01/0500/meta.json",
                new ChangefeedCursor(URL_HOST, endTime).toSegmentCursor("idx/segments/2017/01/01/0500/meta.json", null),
                eventNum.get(0) == 1 ? userCursor : null);
        }

        if (eventNum.contains(2)) {
            verify(mockSegmentFactory).getSegment("idx/segments/2017/01/01/0600/meta.json",
                new ChangefeedCursor(URL_HOST, endTime).toSegmentCursor("idx/segments/2017/01/01/0600/meta.json", null),
                eventNum.get(0) == 2 ? userCursor : null);
        }

        if (eventNum.contains(3)) {
            verify(mockSegmentFactory).getSegment("idx/segments/2017/01/01/1200/meta.json",
                new ChangefeedCursor(URL_HOST, endTime).toSegmentCursor("idx/segments/2017/01/01/1200/meta.json", null),
                eventNum.get(0) == 3 ? userCursor : null);
        }

        if (eventNum.contains(4)) {
            verify(mockSegmentFactory).getSegment("idx/segments/2018/01/01/0300/meta.json",
                new ChangefeedCursor(URL_HOST, endTime).toSegmentCursor("idx/segments/2018/01/01/0300/meta.json", null),
                eventNum.get(0) == 4 ? userCursor : null);
        }

        if (eventNum.contains(5)) {
            verify(mockSegmentFactory).getSegment("idx/segments/2018/01/01/0500/meta.json",
                new ChangefeedCursor(URL_HOST, endTime).toSegmentCursor("idx/segments/2018/01/01/0500/meta.json", null),
                eventNum.get(0) == 5 ? userCursor : null);
        }

        if (eventNum.contains(6)) {
            verify(mockSegmentFactory).getSegment("idx/segments/2018/01/01/0600/meta.json",
                new ChangefeedCursor(URL_HOST, endTime).toSegmentCursor("idx/segments/2018/01/01/0600/meta.json", null),
                eventNum.get(0) == 6 ? userCursor : null);
        }

        if (eventNum.contains(7)) {
            verify(mockSegmentFactory).getSegment("idx/segments/2018/01/01/1200/meta.json",
                new ChangefeedCursor(URL_HOST, endTime).toSegmentCursor("idx/segments/2018/01/01/1200/meta.json", null),
                eventNum.get(0) == 7 ? userCursor : null);
        }

        if (eventNum.contains(8)) {
            verify(mockSegmentFactory).getSegment("idx/segments/2019/01/01/0300/meta.json",
                new ChangefeedCursor(URL_HOST, endTime).toSegmentCursor("idx/segments/2019/01/01/0300/meta.json", null),
                eventNum.get(0) == 8 ? userCursor : null);
        }

        if (eventNum.contains(9)) {
            verify(mockSegmentFactory).getSegment("idx/segments/2019/01/01/0500/meta.json",
                new ChangefeedCursor(URL_HOST, endTime).toSegmentCursor("idx/segments/2019/01/01/0500/meta.json", null),
                eventNum.get(0) == 9 ? userCursor : null);
        }

        if (eventNum.contains(10)) {
            verify(mockSegmentFactory).getSegment("idx/segments/2019/01/01/0600/meta.json",
                new ChangefeedCursor(URL_HOST, endTime).toSegmentCursor("idx/segments/2019/01/01/0600/meta.json", null),
                eventNum.get(0) == 10 ? userCursor : null);
        }

        if (eventNum.contains(11)) {
            verify(mockSegmentFactory).getSegment("idx/segments/2019/01/01/1200/meta.json",
                new ChangefeedCursor(URL_HOST, endTime).toSegmentCursor("idx/segments/2019/01/01/1200/meta.json", null),
                eventNum.get(0) == 11 ? userCursor : null);
        }

        if (eventNum.contains(12)) {
            verify(mockSegmentFactory).getSegment("idx/segments/2020/01/01/0300/meta.json",
                new ChangefeedCursor(URL_HOST, endTime).toSegmentCursor("idx/segments/2020/01/01/0300/meta.json", null),
                eventNum.get(0) == 12 ? userCursor : null);
        }

        if (eventNum.contains(13)) {
            verify(mockSegmentFactory).getSegment("idx/segments/2020/01/01/0500/meta.json",
                new ChangefeedCursor(URL_HOST, endTime).toSegmentCursor("idx/segments/2020/01/01/0500/meta.json", null),
                eventNum.get(0) == 13 ? userCursor : null);
        }

        if (eventNum.contains(14)) {
            verify(mockSegmentFactory).getSegment("idx/segments/2020/01/01/0600/meta.json",
                new ChangefeedCursor(URL_HOST, endTime).toSegmentCursor("idx/segments/2020/01/01/0600/meta.json", null),
                eventNum.get(0) == 14 ? userCursor : null);
        }

        if (eventNum.contains(15)) {
            verify(mockSegmentFactory).getSegment("idx/segments/2020/01/01/1200/meta.json",
                new ChangefeedCursor(URL_HOST, endTime).toSegmentCursor("idx/segments/2020/01/01/1200/meta.json", null),
                eventNum.get(0) == 15 ? userCursor : null);
        }
    }
}
