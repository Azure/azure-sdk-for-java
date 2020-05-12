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
        List<ListBlobsOptions> captured = options.getAllValues()
        captured.get(0).getPrefix() == "idx/segments/2017"
        captured.get(1).getPrefix() == "idx/segments/2018"
        captured.get(2).getPrefix() == "idx/segments/2019"
        captured.get(3).getPrefix() == "idx/segments/2020"
        captured.size() == 4
        verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2017/01/01/0300/meta.json", new ChangefeedCursor(OffsetDateTime.MAX).toSegmentCursor(OffsetDateTime.of(2017, 1, 1, 3, 0, 0, 0, ZoneOffset.UTC)), null) || true
        verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2017/01/01/0500/meta.json", new ChangefeedCursor(OffsetDateTime.MAX).toSegmentCursor(OffsetDateTime.of(2017, 1, 1, 5, 0, 0, 0, ZoneOffset.UTC)), null) || true
        verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2017/01/01/0600/meta.json", new ChangefeedCursor(OffsetDateTime.MAX).toSegmentCursor(OffsetDateTime.of(2017, 1, 1, 6, 0, 0, 0, ZoneOffset.UTC)), null) || true
        verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2017/01/01/1200/meta.json", new ChangefeedCursor(OffsetDateTime.MAX).toSegmentCursor(OffsetDateTime.of(2017, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC)), null) || true

        verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2018/01/01/0300/meta.json", new ChangefeedCursor(OffsetDateTime.MAX).toSegmentCursor(OffsetDateTime.of(2018, 1, 1, 3, 0, 0, 0, ZoneOffset.UTC)), null) || true
        verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2018/01/01/0500/meta.json", new ChangefeedCursor(OffsetDateTime.MAX).toSegmentCursor(OffsetDateTime.of(2018, 1, 1, 5, 0, 0, 0, ZoneOffset.UTC)), null) || true
        verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2018/01/01/0600/meta.json", new ChangefeedCursor(OffsetDateTime.MAX).toSegmentCursor(OffsetDateTime.of(2018, 1, 1, 6, 0, 0, 0, ZoneOffset.UTC)), null) || true
        verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2018/01/01/1200/meta.json", new ChangefeedCursor(OffsetDateTime.MAX).toSegmentCursor(OffsetDateTime.of(2018, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC)), null) || true

        verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2019/01/01/0300/meta.json", new ChangefeedCursor(OffsetDateTime.MAX).toSegmentCursor(OffsetDateTime.of(2019, 1, 1, 3, 0, 0, 0, ZoneOffset.UTC)), null) || true
        verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2019/01/01/0500/meta.json", new ChangefeedCursor(OffsetDateTime.MAX).toSegmentCursor(OffsetDateTime.of(2019, 1, 1, 5, 0, 0, 0, ZoneOffset.UTC)), null) || true
        verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2019/01/01/0600/meta.json", new ChangefeedCursor(OffsetDateTime.MAX).toSegmentCursor(OffsetDateTime.of(2019, 1, 1, 6, 0, 0, 0, ZoneOffset.UTC)), null) || true
        verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2019/01/01/1200/meta.json", new ChangefeedCursor(OffsetDateTime.MAX).toSegmentCursor(OffsetDateTime.of(2019, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC)), null) || true

        verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2020/01/01/0300/meta.json", new ChangefeedCursor(OffsetDateTime.MAX).toSegmentCursor(OffsetDateTime.of(2020, 1, 1, 3, 0, 0, 0, ZoneOffset.UTC)), null) || true
        verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2020/01/01/0500/meta.json", new ChangefeedCursor(OffsetDateTime.MAX).toSegmentCursor(OffsetDateTime.of(2020, 1, 1, 5, 0, 0, 0, ZoneOffset.UTC)), null) || true
        verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2020/01/01/0600/meta.json", new ChangefeedCursor(OffsetDateTime.MAX).toSegmentCursor(OffsetDateTime.of(2020, 1, 1, 6, 0, 0, 0, ZoneOffset.UTC)), null) || true
        verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2020/01/01/1200/meta.json", new ChangefeedCursor(OffsetDateTime.MAX).toSegmentCursor(OffsetDateTime.of(2020, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC)), null) || true

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
        verify(mockContainer, times(numYears)).listBlobs(options.capture()) || true
        List<ListBlobsOptions> captured = options.getAllValues()
        if (year <= 2017) {
            captured.get(0).getPrefix() == "idx/segments/2017"
            captured.remove(0)
            verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2017/01/01/0300/meta.json", new ChangefeedCursor(OffsetDateTime.MAX).toSegmentCursor(OffsetDateTime.of(2017, 1, 1, 3, 0, 0, 0, ZoneOffset.UTC)), null) || true
            verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2017/01/01/0500/meta.json", new ChangefeedCursor(OffsetDateTime.MAX).toSegmentCursor(OffsetDateTime.of(2017, 1, 1, 5, 0, 0, 0, ZoneOffset.UTC)), null) || true
            verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2017/01/01/0600/meta.json", new ChangefeedCursor(OffsetDateTime.MAX).toSegmentCursor(OffsetDateTime.of(2017, 1, 1, 6, 0, 0, 0, ZoneOffset.UTC)), null) || true
            verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2017/01/01/1200/meta.json", new ChangefeedCursor(OffsetDateTime.MAX).toSegmentCursor(OffsetDateTime.of(2017, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC)), null) || true
        }
        if (year <= 2018) {
            captured.get(0).getPrefix() == "idx/segments/2018"
            captured.remove(0)
            verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2018/01/01/0300/meta.json", new ChangefeedCursor(OffsetDateTime.MAX).toSegmentCursor(OffsetDateTime.of(2018, 1, 1, 3, 0, 0, 0, ZoneOffset.UTC)), null) || true
            verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2018/01/01/0500/meta.json", new ChangefeedCursor(OffsetDateTime.MAX).toSegmentCursor(OffsetDateTime.of(2018, 1, 1, 5, 0, 0, 0, ZoneOffset.UTC)), null) || true
            verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2018/01/01/0600/meta.json", new ChangefeedCursor(OffsetDateTime.MAX).toSegmentCursor(OffsetDateTime.of(2018, 1, 1, 6, 0, 0, 0, ZoneOffset.UTC)), null) || true
            verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2018/01/01/1200/meta.json", new ChangefeedCursor(OffsetDateTime.MAX).toSegmentCursor(OffsetDateTime.of(2018, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC)), null) || true
        }
        if (year <= 2019) {
            captured.get(0).getPrefix() == "idx/segments/2019"
            captured.remove(0)
            verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2019/01/01/0300/meta.json", new ChangefeedCursor(OffsetDateTime.MAX).toSegmentCursor(OffsetDateTime.of(2019, 1, 1, 3, 0, 0, 0, ZoneOffset.UTC)), null) || true
            verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2019/01/01/0500/meta.json", new ChangefeedCursor(OffsetDateTime.MAX).toSegmentCursor(OffsetDateTime.of(2019, 1, 1, 5, 0, 0, 0, ZoneOffset.UTC)), null) || true
            verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2019/01/01/0600/meta.json", new ChangefeedCursor(OffsetDateTime.MAX).toSegmentCursor(OffsetDateTime.of(2019, 1, 1, 6, 0, 0, 0, ZoneOffset.UTC)), null) || true
            verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2019/01/01/1200/meta.json", new ChangefeedCursor(OffsetDateTime.MAX).toSegmentCursor(OffsetDateTime.of(2019, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC)), null) || true
        }
        if (year <= 2020) {
            captured.get(0).getPrefix() == "idx/segments/2020"
            captured.remove(0)
            verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2020/01/01/0300/meta.json", new ChangefeedCursor(OffsetDateTime.MAX).toSegmentCursor(OffsetDateTime.of(2020, 1, 1, 3, 0, 0, 0, ZoneOffset.UTC)), null) || true
            verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2020/01/01/0500/meta.json", new ChangefeedCursor(OffsetDateTime.MAX).toSegmentCursor(OffsetDateTime.of(2020, 1, 1, 5, 0, 0, 0, ZoneOffset.UTC)), null) || true
            verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2020/01/01/0600/meta.json", new ChangefeedCursor(OffsetDateTime.MAX).toSegmentCursor(OffsetDateTime.of(2020, 1, 1, 6, 0, 0, 0, ZoneOffset.UTC)), null) || true
            verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2020/01/01/1200/meta.json", new ChangefeedCursor(OffsetDateTime.MAX).toSegmentCursor(OffsetDateTime.of(2020, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC)), null) || true
        }
        captured.size() == 0
        verify(mockSegment, times(numEvents)).getEvents() || true

        where:
        year || numYears | numEvents
        2016 || 4        | 16
        2017 || 4        | 16
        2018 || 3        | 12
        2019 || 2        | 8
        2020 || 1        | 4
        2021 || 0        | 0
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
        verify(mockContainer, times(numYears)).listBlobs(options.capture()) || true
        List<ListBlobsOptions> captured = options.getAllValues()
        if (year >= 2017) {
            captured.get(0).getPrefix() == "idx/segments/2017"
            captured.remove(0)
        }
        if (year >= 2018) {
            captured.get(0).getPrefix() == "idx/segments/2018"
            captured.remove(0)
            verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2017/01/01/0300/meta.json", new ChangefeedCursor(end).toSegmentCursor(OffsetDateTime.of(2017, 1, 1, 3, 0, 0, 0, ZoneOffset.UTC)), null) || true
            verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2017/01/01/0500/meta.json", new ChangefeedCursor(end).toSegmentCursor(OffsetDateTime.of(2017, 1, 1, 5, 0, 0, 0, ZoneOffset.UTC)), null) || true
            verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2017/01/01/0600/meta.json", new ChangefeedCursor(end).toSegmentCursor(OffsetDateTime.of(2017, 1, 1, 6, 0, 0, 0, ZoneOffset.UTC)), null) || true
            verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2017/01/01/1200/meta.json", new ChangefeedCursor(end).toSegmentCursor(OffsetDateTime.of(2017, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC)), null) || true
        }
        if (year >= 2019) {
            captured.get(0).getPrefix() == "idx/segments/2019"
            captured.remove(0)
            verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2018/01/01/0300/meta.json", new ChangefeedCursor(end).toSegmentCursor(OffsetDateTime.of(2018, 1, 1, 3, 0, 0, 0, ZoneOffset.UTC)), null) || true
            verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2018/01/01/0500/meta.json", new ChangefeedCursor(end).toSegmentCursor(OffsetDateTime.of(2018, 1, 1, 5, 0, 0, 0, ZoneOffset.UTC)), null) || true
            verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2018/01/01/0600/meta.json", new ChangefeedCursor(end).toSegmentCursor(OffsetDateTime.of(2018, 1, 1, 6, 0, 0, 0, ZoneOffset.UTC)), null) || true
            verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2018/01/01/1200/meta.json", new ChangefeedCursor(end).toSegmentCursor(OffsetDateTime.of(2018, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC)), null) || true
        }
        if (year >= 2020) {
            captured.get(0).getPrefix() == "idx/segments/2020"
            captured.remove(0)
            verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2019/01/01/0300/meta.json", new ChangefeedCursor(end).toSegmentCursor(OffsetDateTime.of(2019, 1, 1, 3, 0, 0, 0, ZoneOffset.UTC)), null) || true
            verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2019/01/01/0500/meta.json", new ChangefeedCursor(end).toSegmentCursor(OffsetDateTime.of(2019, 1, 1, 5, 0, 0, 0, ZoneOffset.UTC)), null) || true
            verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2019/01/01/0600/meta.json", new ChangefeedCursor(end).toSegmentCursor(OffsetDateTime.of(2019, 1, 1, 6, 0, 0, 0, ZoneOffset.UTC)), null) || true
            verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2019/01/01/1200/meta.json", new ChangefeedCursor(end).toSegmentCursor(OffsetDateTime.of(2019, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC)), null) || true
        }
        if (year >= 2021) {
            verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2020/01/01/0300/meta.json", new ChangefeedCursor(end).toSegmentCursor(OffsetDateTime.of(2020, 1, 1, 3, 0, 0, 0, ZoneOffset.UTC)), null) || true
            verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2020/01/01/0500/meta.json", new ChangefeedCursor(end).toSegmentCursor(OffsetDateTime.of(2020, 1, 1, 5, 0, 0, 0, ZoneOffset.UTC)), null) || true
            verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2020/01/01/0600/meta.json", new ChangefeedCursor(end).toSegmentCursor(OffsetDateTime.of(2020, 1, 1, 6, 0, 0, 0, ZoneOffset.UTC)), null) || true
            verify(mockSegmentFactory).getSegment(mockContainer, "idx/segments/2020/01/01/1200/meta.json", new ChangefeedCursor(end).toSegmentCursor(OffsetDateTime.of(2020, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC)), null) || true
        }
        captured.size() == 0
        verify(mockSegment, times(numEvents)).getEvents() || true

        where:
        year || numYears | numEvents
        2016 || 0        | 0
        2017 || 1        | 0
        2018 || 2        | 4
        2019 || 3        | 8
        2020 || 4        | 12
        2021 || 4        | 16
    }


}
