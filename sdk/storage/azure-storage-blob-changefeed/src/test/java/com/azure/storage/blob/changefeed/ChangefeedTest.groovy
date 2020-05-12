package com.azure.storage.blob.changefeed

import com.azure.core.http.rest.PagedFlux
import com.azure.core.http.rest.PagedResponse
import com.azure.core.http.rest.PagedResponseBase
import com.azure.storage.blob.BlobAsyncClient
import com.azure.storage.blob.BlobContainerAsyncClient
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor
import com.azure.storage.blob.models.BlobItem
import com.azure.storage.blob.models.ListBlobsOptions
import com.fasterxml.jackson.core.JsonParseException
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatcher
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.function.Function
import java.util.function.Supplier

import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.*

class ChangefeedTest extends HelperSpec {

    BlobContainerAsyncClient mockContainer
    SegmentFactory mockSegmentFactory

    def setup() {
        mockContainer = mock(BlobContainerAsyncClient.class)
        mockSegmentFactory = mock(SegmentFactory.class)
    }

    def "changefeed does not exist"() {
        setup:
        when(mockContainer.exists())
            .thenReturn(Mono.just(false))

        when:
        ChangefeedFactory changefeedFactory = new ChangefeedFactory()
        Changefeed changefeed = changefeedFactory.getChangefeed(mockContainer, null, null)

        def sv = StepVerifier.create(changefeed.getEvents())

        then:
        sv.expectErrorMessage("Changefeed has not been enabled for this account.")
        verify(mockContainer).exists() || true
    }

    def "changefeed metadata error"() {
        setup:
        BlobAsyncClient mockMetadataClient = mock(BlobAsyncClient.class)
        when(mockContainer.exists())
            .thenReturn(Mono.just(true))
        when(mockContainer.getBlobAsyncClient(anyString()))
            .thenReturn(mockMetadataClient)
        when(mockMetadataClient.download())
            .thenReturn(Flux.just(ByteBuffer.wrap("not json metadata".getBytes())))

        when:
        ChangefeedFactory changefeedFactory = new ChangefeedFactory()
        Changefeed changefeed = changefeedFactory.getChangefeed(mockContainer, null, null)

        def sv = StepVerifier.create(changefeed.getEvents())

        then:
        sv.verifyError(JsonParseException.class)
        verify(mockContainer).exists() || true
        verify(mockContainer).getBlobAsyncClient(Changefeed.METADATA_SEGMENT_PATH) || true
        verify(mockMetadataClient).download() || true
    }

    /* 4 years 2017-2020. */
    Supplier<Mono<PagedResponse<BlobItem>>> yearSupplier = new Supplier<Mono<PagedResponse<BlobItem>>>() {
        @Override
        Mono<PagedResponse<BlobItem>> get() {
            return Mono.just(new PagedResponseBase<>(
                null, 200, null,
                List.of(new BlobItem().setName("idx/segments/2017"), new BlobItem().setName("idx/segments/2018"),
                    new BlobItem().setName("idx/segments/2019"), new BlobItem().setName("idx/segments/2020")),
                null, null))
        }
    }

    /* 4 segments 12:00, 3:00, 5:00, 6:00 */
    Function<String, Supplier<Mono<PagedResponse<BlobItem>>>> segmentSupplier = { year ->
        new Supplier<Mono<PagedResponse<BlobItem>>>() {
            @Override
            Mono<PagedResponse<BlobItem>> get() {
                return Mono.just(new PagedResponseBase<>(
                    null, 200, null,
                    List.of(new BlobItem().setName("idx/segments/" + year + "/01/01/1200/meta.json"), new BlobItem().setName("idx/segments/" + year + "/01/01/0300/meta.json"),
                        new BlobItem().setName("idx/segments/"+ year +"/01/01/0500/meta.json"), new BlobItem().setName("idx/segments/" + year + "/01/01/0600/meta.json")),
                    null, null))
            }
        }
    }

    /* No options. */
    def "changefeed min"() {
        setup:
        BlobAsyncClient mockMetadataClient = mock(BlobAsyncClient.class)
        when(mockContainer.exists())
            .thenReturn(Mono.just(true))
        when(mockContainer.getBlobAsyncClient(anyString()))
            .thenReturn(mockMetadataClient)
        when(mockMetadataClient.download())
            .thenReturn(readFile("changefeed_manifest.json"))
        when(mockContainer.listBlobsByHierarchy(anyString()))
            .thenReturn(new PagedFlux<>(yearSupplier))
        Function<String, ArgumentMatcher<ListBlobsOptions>> isYear = { year -> { options -> options == null ? false : options.getPrefix().equals("idx/segments/" + year) } }
        when(mockContainer.listBlobs(argThat(isYear.apply("2017"))))
            .thenReturn(new PagedFlux<>(segmentSupplier.apply("2017")))
        when(mockContainer.listBlobs(argThat(isYear.apply("2018"))))
            .thenReturn(new PagedFlux<>(segmentSupplier.apply("2018")))
        when(mockContainer.listBlobs(argThat(isYear.apply("2019"))))
            .thenReturn(new PagedFlux<>(segmentSupplier.apply("2019")))
        when(mockContainer.listBlobs(argThat(isYear.apply("2020"))))
            .thenReturn(new PagedFlux<>(segmentSupplier.apply("2020")))

        def mockSegment = mock(Segment.class)

        when(mockSegmentFactory.getSegment(any(BlobContainerAsyncClient.class), anyString(), any(ChangefeedCursor.class), isNull()))
            .thenReturn(mockSegment)
        when(mockSegment.getEvents())
            .thenReturn(Flux.empty()) /* TODO (gapra) : Does it really matter I return real events? I check that we get the correct segments. */

        when:
        ChangefeedFactory changefeedFactory = new ChangefeedFactory(mockSegmentFactory)
        Changefeed changefeed = changefeedFactory.getChangefeed(mockContainer, null, null)

        def sv = StepVerifier.create(changefeed.getEvents())

        then:
        sv.verifyComplete()
        changefeed.lastConsumable == OffsetDateTime.of(2020, 5, 4, 19, 0, 0, 0, ZoneOffset.UTC)
        verify(mockContainer).exists() || true
        verify(mockContainer).getBlobAsyncClient(Changefeed.METADATA_SEGMENT_PATH) || true
        verify(mockMetadataClient).download() || true
        verify(mockContainer).listBlobsByHierarchy(Changefeed.SEGMENT_PREFIX) || true
        ArgumentCaptor<ListBlobsOptions> options = ArgumentCaptor.forClass(ListBlobsOptions.class);
        verify(mockContainer, times(4)).listBlobs(options.capture()) || true
        verifyEvents(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)
        verifyYears(options.getAllValues(), "idx/segments/2017", "idx/segments/2018", "idx/segments/2019", "idx/segments/2020")
        verify(mockSegment, times(16)).getEvents() || true
    }

    /* startTime years. */
    @Unroll
    def "changefeed startTime years"() {
        setup:
        BlobAsyncClient mockMetadataClient = mock(BlobAsyncClient.class)
        when(mockContainer.exists())
            .thenReturn(Mono.just(true))
        when(mockContainer.getBlobAsyncClient(anyString()))
            .thenReturn(mockMetadataClient)
        when(mockMetadataClient.download())
            .thenReturn(readFile("changefeed_manifest.json"))
        when(mockContainer.listBlobsByHierarchy(anyString()))
            .thenReturn(new PagedFlux<>(yearSupplier))
        Function<String, ArgumentMatcher<ListBlobsOptions>> isYear = { year -> { options -> options == null ? false : options.getPrefix().equals("idx/segments/" + year) } }
        when(mockContainer.listBlobs(argThat(isYear.apply("2017"))))
            .thenReturn(new PagedFlux<>(segmentSupplier.apply("2017")))
        when(mockContainer.listBlobs(argThat(isYear.apply("2018"))))
            .thenReturn(new PagedFlux<>(segmentSupplier.apply("2018")))
        when(mockContainer.listBlobs(argThat(isYear.apply("2019"))))
            .thenReturn(new PagedFlux<>(segmentSupplier.apply("2019")))
        when(mockContainer.listBlobs(argThat(isYear.apply("2020"))))
            .thenReturn(new PagedFlux<>(segmentSupplier.apply("2020")))

        def mockSegment = mock(Segment.class)

        when(mockSegmentFactory.getSegment(any(BlobContainerAsyncClient.class), anyString(), any(ChangefeedCursor.class), isNull()))
            .thenReturn(mockSegment)
        when(mockSegment.getEvents())
            .thenReturn(Flux.empty())

        when:
        ChangefeedFactory changefeedFactory = new ChangefeedFactory(mockSegmentFactory)
        Changefeed changefeed = changefeedFactory.getChangefeed(mockContainer, OffsetDateTime.of(year, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), null)

        def sv = StepVerifier.create(changefeed.getEvents())

        then:
        sv.verifyComplete()
        assert changefeed.lastConsumable == OffsetDateTime.of(2020, 5, 4, 19, 0, 0, 0, ZoneOffset.UTC)
        verify(mockContainer).exists() || true
        verify(mockContainer).getBlobAsyncClient(Changefeed.METADATA_SEGMENT_PATH) || true
        verify(mockMetadataClient).download() || true
        verify(mockContainer).listBlobsByHierarchy(Changefeed.SEGMENT_PREFIX) || true
        ArgumentCaptor<ListBlobsOptions> options = ArgumentCaptor.forClass(ListBlobsOptions.class);
        verify(mockContainer, times(yearPaths.size())).listBlobs(options.capture()) || true
        verifyEvents(OffsetDateTime.MAX, eventNums as int[])
        verifyYears(options.getAllValues(), yearPaths as String[])
        verify(mockSegment, times(eventNums.size())).getEvents() || true

        where:
        year || yearPaths                                                                               | eventNums
        2016 || ["idx/segments/2017", "idx/segments/2018", "idx/segments/2019", "idx/segments/2020"]   | [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15]
        2017 || ["idx/segments/2017", "idx/segments/2018", "idx/segments/2019", "idx/segments/2020"]   | [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15]
        2018 || ["idx/segments/2018", "idx/segments/2019", "idx/segments/2020"]                        | [4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15]
        2019 || ["idx/segments/2019", "idx/segments/2020"]                                             | [8, 9, 10, 11, 12, 13, 14, 15]
        2020 || ["idx/segments/2020"]                                                                  | [12, 13, 14, 15]
        2021 || []                                                                                     | []
    }

    /* endTime years. */
    @Unroll
    def "changefeed endTime years"() {
        setup:
        BlobAsyncClient mockMetadataClient = mock(BlobAsyncClient.class)
        when(mockContainer.exists())
            .thenReturn(Mono.just(true))
        when(mockContainer.getBlobAsyncClient(anyString()))
            .thenReturn(mockMetadataClient)
        when(mockMetadataClient.download())
            .thenReturn(readFile("changefeed_manifest.json"))
        when(mockContainer.listBlobsByHierarchy(anyString()))
            .thenReturn(new PagedFlux<>(yearSupplier))
        Function<String, ArgumentMatcher<ListBlobsOptions>> isYear = { year -> { options -> options == null ? false : options.getPrefix().equals("idx/segments/" + year) } }
        when(mockContainer.listBlobs(argThat(isYear.apply("2017"))))
            .thenReturn(new PagedFlux<>(segmentSupplier.apply("2017")))
        when(mockContainer.listBlobs(argThat(isYear.apply("2018"))))
            .thenReturn(new PagedFlux<>(segmentSupplier.apply("2018")))
        when(mockContainer.listBlobs(argThat(isYear.apply("2019"))))
            .thenReturn(new PagedFlux<>(segmentSupplier.apply("2019")))
        when(mockContainer.listBlobs(argThat(isYear.apply("2020"))))
            .thenReturn(new PagedFlux<>(segmentSupplier.apply("2020")))

        def mockSegment = mock(Segment.class)

        when(mockSegmentFactory.getSegment(any(BlobContainerAsyncClient.class), anyString(), any(ChangefeedCursor.class), isNull()))
            .thenReturn(mockSegment)
        when(mockSegment.getEvents())
            .thenReturn(Flux.empty())

        when:
        OffsetDateTime end = OffsetDateTime.of(year, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        ChangefeedFactory changefeedFactory = new ChangefeedFactory(mockSegmentFactory)
        Changefeed changefeed = changefeedFactory.getChangefeed(mockContainer, null, end)

        def sv = StepVerifier.create(changefeed.getEvents())

        then:
        sv.verifyComplete()
        assert changefeed.lastConsumable == OffsetDateTime.of(2020, 5, 4, 19, 0, 0, 0, ZoneOffset.UTC)
        verify(mockContainer).exists() || true
        verify(mockContainer).getBlobAsyncClient(Changefeed.METADATA_SEGMENT_PATH) || true
        verify(mockMetadataClient).download() || true
        verify(mockContainer).listBlobsByHierarchy(Changefeed.SEGMENT_PREFIX) || true
        ArgumentCaptor<ListBlobsOptions> options = ArgumentCaptor.forClass(ListBlobsOptions.class);
        verify(mockContainer, times(yearPaths.size())).listBlobs(options.capture()) || true
        verifyEvents(end, eventNums as int[])
        verifyYears(options.getAllValues(), yearPaths as String[])
        verify(mockSegment, times(eventNums.size())).getEvents() || true

        where:
        year || yearPaths                                                                               | eventNums
        2016 || []                                                                                      | []
        2017 || ["idx/segments/2017"]                                                                   | []
        2018 || ["idx/segments/2017", "idx/segments/2018"]                                              | [0, 1, 2, 3]
        2019 || ["idx/segments/2017", "idx/segments/2018", "idx/segments/2019"]                         | [0, 1, 2, 3, 4, 5, 6, 7]
        2020 || ["idx/segments/2017", "idx/segments/2018", "idx/segments/2019", "idx/segments/2020"]    | [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11]
        2021 || ["idx/segments/2017", "idx/segments/2018", "idx/segments/2019", "idx/segments/2020"]    | [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15]
    }

    /* startTime segments. */
    @Unroll
    def "changefeed startTime segments"() {
        setup:
        BlobAsyncClient mockMetadataClient = mock(BlobAsyncClient.class)
        when(mockContainer.exists())
            .thenReturn(Mono.just(true))
        when(mockContainer.getBlobAsyncClient(anyString()))
            .thenReturn(mockMetadataClient)
        when(mockMetadataClient.download())
            .thenReturn(readFile("changefeed_manifest.json"))
        when(mockContainer.listBlobsByHierarchy(anyString()))
            .thenReturn(new PagedFlux<>(yearSupplier))
        Function<String, ArgumentMatcher<ListBlobsOptions>> isYear = { year -> { options -> options == null ? false : options.getPrefix().equals("idx/segments/" + year) } }
        when(mockContainer.listBlobs(argThat(isYear.apply("2017"))))
            .thenReturn(new PagedFlux<>(segmentSupplier.apply("2017")))
        when(mockContainer.listBlobs(argThat(isYear.apply("2018"))))
            .thenReturn(new PagedFlux<>(segmentSupplier.apply("2018")))
        when(mockContainer.listBlobs(argThat(isYear.apply("2019"))))
            .thenReturn(new PagedFlux<>(segmentSupplier.apply("2019")))
        when(mockContainer.listBlobs(argThat(isYear.apply("2020"))))
            .thenReturn(new PagedFlux<>(segmentSupplier.apply("2020")))

        def mockSegment = mock(Segment.class)

        when(mockSegmentFactory.getSegment(any(BlobContainerAsyncClient.class), anyString(), any(ChangefeedCursor.class), isNull()))
            .thenReturn(mockSegment)
        when(mockSegment.getEvents())
            .thenReturn(Flux.empty())

        when:
        ChangefeedFactory changefeedFactory = new ChangefeedFactory(mockSegmentFactory)
        Changefeed changefeed = changefeedFactory.getChangefeed(mockContainer, startTime, null)

        def sv = StepVerifier.create(changefeed.getEvents())

        then:
        sv.verifyComplete()
        assert changefeed.lastConsumable == OffsetDateTime.of(2020, 5, 4, 19, 0, 0, 0, ZoneOffset.UTC)
        verify(mockContainer).exists() || true
        verify(mockContainer).getBlobAsyncClient(Changefeed.METADATA_SEGMENT_PATH) || true
        verify(mockMetadataClient).download() || true
        verify(mockContainer).listBlobsByHierarchy(Changefeed.SEGMENT_PREFIX) || true
        ArgumentCaptor<ListBlobsOptions> options = ArgumentCaptor.forClass(ListBlobsOptions.class);
        verify(mockContainer, times(yearPaths.size())).listBlobs(options.capture()) || true
        verifyEvents(OffsetDateTime.MAX, eventNums as int[])
        verifyYears(options.getAllValues(), yearPaths as String[])
        verify(mockSegment, times(eventNums.size())).getEvents() || true

        where:
         startTime                                                   || yearPaths                                                                               | eventNums
         OffsetDateTime.of(2017, 1, 1, 2, 0, 0, 0, ZoneOffset.UTC)   || ["idx/segments/2017", "idx/segments/2018", "idx/segments/2019", "idx/segments/2020"]    | [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15] /* All segments taken from year. */
         OffsetDateTime.of(2018, 1, 2, 0, 0, 0, 0, ZoneOffset.UTC)   || ["idx/segments/2018", "idx/segments/2019", "idx/segments/2020"]                         | [8, 9, 10, 11, 12, 13, 14, 15]                         /* No segments taken from year. */
         OffsetDateTime.of(2019, 1, 1, 6, 0, 0, 0, ZoneOffset.UTC)   || ["idx/segments/2019", "idx/segments/2020"]                                              | [10, 11, 12, 13, 14, 15]                               /* Partial segments taken from year. Checks isEqual. */
         OffsetDateTime.of(2020, 1, 1, 8, 0, 0, 0, ZoneOffset.UTC)   || ["idx/segments/2020"]                                                                   | [15]                                                   /* Partial segments taken from year. Checks is strictly before. */
    }

    /* endTime segments. */
    @Unroll
    def "changefeed endTime segments"() {
        setup:
        BlobAsyncClient mockMetadataClient = mock(BlobAsyncClient.class)
        when(mockContainer.exists())
            .thenReturn(Mono.just(true))
        when(mockContainer.getBlobAsyncClient(anyString()))
            .thenReturn(mockMetadataClient)
        when(mockMetadataClient.download())
            .thenReturn(readFile("changefeed_manifest.json"))
        when(mockContainer.listBlobsByHierarchy(anyString()))
            .thenReturn(new PagedFlux<>(yearSupplier))
        Function<String, ArgumentMatcher<ListBlobsOptions>> isYear = { year -> { options -> options == null ? false : options.getPrefix().equals("idx/segments/" + year) } }
        when(mockContainer.listBlobs(argThat(isYear.apply("2017"))))
            .thenReturn(new PagedFlux<>(segmentSupplier.apply("2017")))
        when(mockContainer.listBlobs(argThat(isYear.apply("2018"))))
            .thenReturn(new PagedFlux<>(segmentSupplier.apply("2018")))
        when(mockContainer.listBlobs(argThat(isYear.apply("2019"))))
            .thenReturn(new PagedFlux<>(segmentSupplier.apply("2019")))
        when(mockContainer.listBlobs(argThat(isYear.apply("2020"))))
            .thenReturn(new PagedFlux<>(segmentSupplier.apply("2020")))

        def mockSegment = mock(Segment.class)

        when(mockSegmentFactory.getSegment(any(BlobContainerAsyncClient.class), anyString(), any(ChangefeedCursor.class), isNull()))
            .thenReturn(mockSegment)
        when(mockSegment.getEvents())
            .thenReturn(Flux.empty())

        when:
        ChangefeedFactory changefeedFactory = new ChangefeedFactory(mockSegmentFactory)
        Changefeed changefeed = changefeedFactory.getChangefeed(mockContainer, null, endTime)

        def sv = StepVerifier.create(changefeed.getEvents())

        then:
        sv.verifyComplete()
        assert changefeed.lastConsumable == OffsetDateTime.of(2020, 5, 4, 19, 0, 0, 0, ZoneOffset.UTC)
        verify(mockContainer).exists() || true
        verify(mockContainer).getBlobAsyncClient(Changefeed.METADATA_SEGMENT_PATH) || true
        verify(mockMetadataClient).download() || true
        verify(mockContainer).listBlobsByHierarchy(Changefeed.SEGMENT_PREFIX) || true
        ArgumentCaptor<ListBlobsOptions> options = ArgumentCaptor.forClass(ListBlobsOptions.class);
        verify(mockContainer, times(yearPaths.size())).listBlobs(options.capture()) || true
        verifyEvents(endTime, eventNums as int[])
        verifyYears(options.getAllValues(), yearPaths as String[])
        verify(mockSegment, times(eventNums.size())).getEvents() || true

        where:
        endTime                                                     || yearPaths                                                                               | eventNums
        OffsetDateTime.of(2017, 12, 12, 2, 0, 0, 0, ZoneOffset.UTC) || ["idx/segments/2017"]                                                                   | [0, 1, 2, 3]                                         /* All segments taken from year. */
        OffsetDateTime.of(2018, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)   || ["idx/segments/2017", "idx/segments/2018"]                                              | [0, 1, 2, 3]                                         /* No segments taken from year. */
        OffsetDateTime.of(2019, 1, 1, 6, 0, 0, 0, ZoneOffset.UTC)   || ["idx/segments/2017", "idx/segments/2018", "idx/segments/2019"]                         | [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10]                   /* Partial segments taken from year. Checks isEqual. */
        OffsetDateTime.of(2020, 1, 1, 8, 0, 0, 0, ZoneOffset.UTC)   || ["idx/segments/2017", "idx/segments/2018", "idx/segments/2019", "idx/segments/2020"]    | [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14]   /* Partial segments taken from year. Checks is strictly before. */
    }

    boolean verifyYears(List<ListBlobsOptions> captured, String... yearPaths) {
        for (def yearPath : yearPaths) {
            assert captured.get(0).getPrefix() == yearPath
            captured.remove(0)
        }
        assert captured.size() == 0
        return true
    }

    boolean verifyEvents(OffsetDateTime endTime, int... eventNum) {
        if (0 in eventNum)
            assert verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2017/01/01/0300/meta.json", new ChangefeedCursor(endTime).toSegmentCursor(OffsetDateTime.of(2017, 1, 1, 3, 0, 0, 0, ZoneOffset.UTC)), null) || true
        if (1 in eventNum)
            assert verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2017/01/01/0500/meta.json", new ChangefeedCursor(endTime).toSegmentCursor(OffsetDateTime.of(2017, 1, 1, 5, 0, 0, 0, ZoneOffset.UTC)), null) || true
        if (2 in eventNum)
            assert verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2017/01/01/0600/meta.json", new ChangefeedCursor(endTime).toSegmentCursor(OffsetDateTime.of(2017, 1, 1, 6, 0, 0, 0, ZoneOffset.UTC)), null) || true
        if (3 in eventNum)
            assert verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2017/01/01/1200/meta.json", new ChangefeedCursor(endTime).toSegmentCursor(OffsetDateTime.of(2017, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC)), null) || true

        if (4 in eventNum)
            assert verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2018/01/01/0300/meta.json", new ChangefeedCursor(endTime).toSegmentCursor(OffsetDateTime.of(2018, 1, 1, 3, 0, 0, 0, ZoneOffset.UTC)), null) || true
        if (5 in eventNum)
            assert verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2018/01/01/0500/meta.json", new ChangefeedCursor(endTime).toSegmentCursor(OffsetDateTime.of(2018, 1, 1, 5, 0, 0, 0, ZoneOffset.UTC)), null) || true
        if (6 in eventNum)
            assert verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2018/01/01/0600/meta.json", new ChangefeedCursor(endTime).toSegmentCursor(OffsetDateTime.of(2018, 1, 1, 6, 0, 0, 0, ZoneOffset.UTC)), null) || true
        if (7 in eventNum)
            assert verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2018/01/01/1200/meta.json", new ChangefeedCursor(endTime).toSegmentCursor(OffsetDateTime.of(2018, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC)), null) || true

        if (8 in eventNum)
            assert verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2019/01/01/0300/meta.json", new ChangefeedCursor(endTime).toSegmentCursor(OffsetDateTime.of(2019, 1, 1, 3, 0, 0, 0, ZoneOffset.UTC)), null) || true
        if (9 in eventNum)
            assert verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2019/01/01/0500/meta.json", new ChangefeedCursor(endTime).toSegmentCursor(OffsetDateTime.of(2019, 1, 1, 5, 0, 0, 0, ZoneOffset.UTC)), null) || true
        if (10 in eventNum)
            assert verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2019/01/01/0600/meta.json", new ChangefeedCursor(endTime).toSegmentCursor(OffsetDateTime.of(2019, 1, 1, 6, 0, 0, 0, ZoneOffset.UTC)), null) || true
        if (11 in eventNum)
            assert verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2019/01/01/1200/meta.json", new ChangefeedCursor(endTime).toSegmentCursor(OffsetDateTime.of(2019, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC)), null) || true

        if (12 in eventNum)
            assert verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2020/01/01/0300/meta.json", new ChangefeedCursor(endTime).toSegmentCursor(OffsetDateTime.of(2020, 1, 1, 3, 0, 0, 0, ZoneOffset.UTC)), null) || true
        if (13 in eventNum)
            assert verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2020/01/01/0500/meta.json", new ChangefeedCursor(endTime).toSegmentCursor(OffsetDateTime.of(2020, 1, 1, 5, 0, 0, 0, ZoneOffset.UTC)), null) || true
        if (14 in eventNum)
            assert verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2020/01/01/0600/meta.json", new ChangefeedCursor(endTime).toSegmentCursor(OffsetDateTime.of(2020, 1, 1, 6, 0, 0, 0, ZoneOffset.UTC)), null) || true
        if (15 in eventNum)
            assert verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2020/01/01/1200/meta.json", new ChangefeedCursor(endTime).toSegmentCursor(OffsetDateTime.of(2020, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC)), null) || true
        return true;
    }

    /* TODO: (gapra): Test cursor. Test lastConsumable before endTime. */

}
