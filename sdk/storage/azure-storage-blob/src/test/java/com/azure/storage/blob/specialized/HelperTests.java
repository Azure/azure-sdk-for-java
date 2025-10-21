// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.util.Context;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobTestBase;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.blob.FakeCredentialInTests;
import com.azure.storage.blob.implementation.util.BlobSasImplUtil;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.PageList;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.Utility;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.SasImplUtils;
import com.azure.storage.common.sas.CommonSasQueryParameters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HelperTests extends BlobTestBase {
    /*
    This test is to validate the workaround for the autorest bug that forgets to set the request property on the
    response.
    */

    @Test
    public void requestProperty() {
        assertNotNull(cc.deleteWithResponse(null, null, null).getRequest());
    }

    @ParameterizedTest
    @MethodSource("blobRangeSupplier")
    public void blobRange(int offset, Long count, String result) {
        assertEquals(new BlobRange(offset, count).toHeaderValue(), result);
    }

    private static Stream<Arguments> blobRangeSupplier() {
        return Stream.of(Arguments.of(0, null, null), Arguments.of(0, 5L, "bytes=0-4"),
            Arguments.of(5, 10L, "bytes=5-14"));
    }

    @ParameterizedTest
    @MethodSource("blobRangeIASupplier")
    public void blobRangeIA(int offset, Long count) {
        assertThrows(IllegalArgumentException.class, () -> new BlobRange(offset, count));
    }

    private static Stream<Arguments> blobRangeIASupplier() {
        return Stream.of(Arguments.of(-1, 5L), Arguments.of(0, -1L));
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "blob,blob",
            "path/to]a blob,path/to]a blob",
            "path%2Fto%5Da%20blob,path/to]a blob",
            "斑點,斑點",
            "%E6%96%91%E9%BB%9E,斑點"})
    public void urlParser(String originalBlobName, String finalBlobName) throws MalformedURLException {
        BlobUrlParts parts = BlobUrlParts.parse(new URL(
            "http://host/container/" + originalBlobName + "?snapshot=snapshot&sv=" + Constants.SAS_SERVICE_VERSION
                + "&sr=c&sp=r&sig=" + FakeCredentialInTests.FAKE_SIGNATURE_PLACEHOLDER));

        assertEquals("http", parts.getScheme());
        assertEquals("host", parts.getHost());
        assertEquals("container", parts.getBlobContainerName());
        assertEquals(finalBlobName, parts.getBlobName());
        assertEquals("snapshot", parts.getSnapshot());
        assertEquals("r", parts.getCommonSasQueryParameters().getPermissions());
        assertEquals(Constants.SAS_SERVICE_VERSION, parts.getCommonSasQueryParameters().getVersion());
        assertEquals("c", parts.getCommonSasQueryParameters().getResource());
        assertEquals(Utility.urlDecode("sD3fPKLnFKZUjnSV4qA%2FXoJOqsmDfNfxWcZ7kPtLc0I%3D"),
            parts.getCommonSasQueryParameters().getSignature());
    }

    @Test
    public void blobURLParts() {
        BlobUrlParts parts = new BlobUrlParts();
        parts.setScheme("http")
            .setHost("host")
            .setContainerName("container")
            .setBlobName("blob")
            .setSnapshot("snapshot");
        OffsetDateTime expiryTime = OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        BlobSasPermission p = new BlobSasPermission().setReadPermission(true);
        BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(expiryTime, p);

        BlobSasImplUtil implUtil
            = new BlobSasImplUtil(sasValues, "containerName", "blobName", "snapshot", null, "encryptionScope");
        String sas = implUtil.generateSas(ENVIRONMENT.getPrimaryAccount().getCredential(), Context.NONE);

        parts.setCommonSasQueryParameters(new CommonSasQueryParameters(SasImplUtils.parseQueryString(sas), true));

        String[] splitParts = parts.toUrl().toString().split("\\?");

        // Ensure that there is only one question mark even when sas and snapshot are present
        assertEquals(splitParts.length, 2);
        assertEquals(splitParts[0], "http://host/container/blob");
        assertTrue(splitParts[1].contains("snapshot=snapshot"));
        assertTrue(splitParts[1].contains("sp=r"));
        assertTrue(splitParts[1].contains("sig="));
        assertTrue(splitParts[1].contains("ses=encryptionScope"));
        assertEquals(splitParts[1].split("&").length, 7); // snapshot & sv & sr & sp & sig & ses
    }

    @Test
    public void blobURLPartsImplicitRoot() {
        BlobUrlParts bup = new BlobUrlParts().setScheme("http").setHost("host").setBlobName("blob");

        BlobUrlParts implParts = BlobUrlParts.parse(bup.toUrl());

        assertEquals(implParts.getBlobContainerName(), BlobContainerAsyncClient.ROOT_CONTAINER_NAME);
    }

    @Test
    public void utilityConvertStreamToBufferReplayable() {
        byte[] data = getRandomByteArray(1024);

        Flux<ByteBuffer> flux = Utility.convertStreamToByteBuffer(new ByteArrayInputStream(data), 1024, 1024, true);

        StepVerifier.create(flux)
            .assertNext(buffer -> assertEquals(buffer.compareTo(ByteBuffer.wrap(data)), 0))
            .verifyComplete();
        // subscribe multiple times and ensure data is same each time
        StepVerifier.create(flux)
            .assertNext(buffer -> assertEquals(buffer.compareTo(ByteBuffer.wrap(data)), 0))
            .verifyComplete();
    }

    /*
    This test covers the switch from using available() to using read() to check that a stream is done when converting
    from a stream to a flux. We previously used to assert that, when we had read length bytes from the stream that
    available() == 0, but available only returns an estimate and is not reliable. Now we assert that read() == -1
     */
    @Test
    public void utilityConvertStreamToBufferAvailable() {
        byte[] data = getRandomByteArray(10);

        Flux<ByteBuffer> flux = Utility.convertStreamToByteBuffer(new TestBAIS(data), 10, 10, true);

        //then: "When the stream is the right length but available always returns > 0, do not throw"
        StepVerifier.create(flux)
            .assertNext(buffer -> assertEquals(buffer.compareTo(ByteBuffer.wrap(data)), 0))
            .verifyComplete();
        // subscribe multiple times and ensure data is same each time
        StepVerifier.create(flux)
            .assertNext(buffer -> assertEquals(buffer.compareTo(ByteBuffer.wrap(data)), 0))
            .verifyComplete();

        //when: "When the stream is actually longer than the length, throw"
        flux = Utility.convertStreamToByteBuffer(new TestBAIS(data), 9, 10, true);

        StepVerifier.create(flux).verifyError(IllegalStateException.class);
    }

    static class TestBAIS extends ByteArrayInputStream {

        TestBAIS(byte[] data) {
            super(data);
        }

        @Override
        public synchronized int available() {
            return 10;
        }
    }

    @Test
    public void pageListCustomDeserializer() throws IOException {
        String responseXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>  \n" + "<PageList>  \n" + "   <PageRange>  \n"
            + "      <Start>0</Start>  \n" + "      <End>511</End>  \n" + "   </PageRange>  \n" + "   <ClearRange>  \n"
            + "      <Start>512</Start>  \n" + "      <End>1023</End>  \n" + "   </ClearRange>  \n"
            + "   <PageRange>  \n" + "      <Start>1024</Start>  \n" + "      <End>2047</End>  \n"
            + "   </PageRange>  \n" + "</PageList>";

        PageList pageList = JacksonAdapter.createDefaultSerializerAdapter()
            .deserialize(responseXml, PageList.class, SerializerEncoding.XML);

        assertEquals(2, pageList.getPageRange().size());
        assertEquals(1, pageList.getClearRange().size());
    }

    // Tests that container names are properly URL decoded when retrieved from BlobUrlParts. Container names with special characters are not supported
    // by the service, however, the names should still be encoded.
    @Test
    public void containerNameDecodingOnGet() {
        BlobUrlParts parts = new BlobUrlParts()
            .setScheme("http")
            .setHost("host")
            .setContainerName("my%20container");
        assertEquals("my container", parts.getBlobContainerName());
        // URL should retain the encoded form supplied by caller
        assertTrue(parts.toUrl().toString().contains("my%20container"));
    }

    // Tests that blob names are not automatically URL encoded when set in BlobUrlParts.
    @Test
    public void setBlobNameUnencodedPreserved() {
        BlobUrlParts parts = new BlobUrlParts()
            .setScheme("http")
            .setHost("host")
            .setContainerName("container")
            .setBlobName("my blob");
        // New behavior: value is not auto-encoded
        assertEquals("my blob", parts.getBlobName());
        String url = parts.toUrl().toString();
        assertTrue(url.contains("/container/my blob"));
        // Ensure no accidental encoding
        assertFalse(url.contains("%20"));
    }

    // Tests that blob names are properly URL decoded when retrieved from BlobUrlParts.
    @Test
    public void setBlobNameEncodedNotDoubleEncoded() {
        BlobUrlParts parts = new BlobUrlParts()
            .setScheme("http")
            .setHost("host")
            .setContainerName("container")
            .setBlobName("my%20blob");
        // Getter decodes
        assertEquals("my blob", parts.getBlobName());
        String url = parts.toUrl().toString();
        // Path should contain the original encoded segment exactly once
        assertTrue(url.contains("/container/my%20blob"));
        assertFalse(url.contains("%2520"));
    }
}
