// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.ReadConsistencyStrategy;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ISessionContainer;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.ThinClientStoreModel;
import com.azure.cosmos.implementation.UserAgentContainer;
import com.azure.cosmos.implementation.PartitionKeyRange;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class RntbdReadConsistencyStrategyHeaderTests {

    @DataProvider(name = "readConsistencyStrategyToRntbdByteValues")
    public Object[][] readConsistencyStrategyToRntbdByteValues() {
        return new Object[][] {
            { ReadConsistencyStrategy.EVENTUAL, RntbdConstants.RntbdReadConsistencyStrategy.Eventual.id() },
            { ReadConsistencyStrategy.SESSION, RntbdConstants.RntbdReadConsistencyStrategy.Session.id() },
            { ReadConsistencyStrategy.LATEST_COMMITTED, RntbdConstants.RntbdReadConsistencyStrategy.LatestCommitted.id() },
            { ReadConsistencyStrategy.GLOBAL_STRONG, RntbdConstants.RntbdReadConsistencyStrategy.GlobalStrong.id() },
        };
    }

    @Test(groups = { "unit" }, dataProvider = "readConsistencyStrategyToRntbdByteValues")
    public void readConsistencyStrategyTokenEncodesCorrectly(
        ReadConsistencyStrategy strategy,
        byte expectedByteValue) {

        RntbdToken token = RntbdToken.create(
            RntbdConstants.RntbdRequestHeader.ReadConsistencyStrategy);
        assertThat(token).isNotNull();
        assertThat(token.isPresent()).isFalse();

        token.setValue(expectedByteValue);
        assertThat(token.isPresent()).isTrue();
        assertThat(((Number) token.getValue()).byteValue()).isEqualTo(expectedByteValue);
    }

    @Test(groups = { "unit" })
    public void readConsistencyStrategyHeaderId() {
        assertThat(RntbdConstants.RntbdRequestHeader.ReadConsistencyStrategy.id())
            .isEqualTo((short) 0x00F0);
    }

    @Test(groups = { "unit" })
    public void readConsistencyStrategyHeaderType() {
        assertThat(RntbdConstants.RntbdRequestHeader.ReadConsistencyStrategy.type())
            .isEqualTo(RntbdTokenType.Byte);
    }

    @Test(groups = { "unit" })
    public void readConsistencyStrategyHeaderNotRequired() {
        assertThat(RntbdConstants.RntbdRequestHeader.ReadConsistencyStrategy.isRequired())
            .isFalse();
    }

    @Test(groups = { "unit" })
    public void readConsistencyStrategyTokenNotPresentWhenNotSet() {
        RntbdToken token = RntbdToken.create(
            RntbdConstants.RntbdRequestHeader.ReadConsistencyStrategy);
        assertThat(token.isPresent()).isFalse();
    }

    @Test(groups = { "unit" }, dataProvider = "readConsistencyStrategyToRntbdByteValues")
    public void readConsistencyStrategyTokenRoundTrips(
        ReadConsistencyStrategy strategy,
        byte expectedByteValue) {

        // Encode
        RntbdToken token = RntbdToken.create(
            RntbdConstants.RntbdRequestHeader.ReadConsistencyStrategy);
        token.setValue(expectedByteValue);

        ByteBuf buffer = Unpooled.buffer(256);
        try {
            token.encode(buffer);

            // Decode
            RntbdToken decodedToken = RntbdToken.create(
                RntbdConstants.RntbdRequestHeader.ReadConsistencyStrategy);
            // skip 3 bytes: 2 for header id + 1 for token type
            buffer.readerIndex(3);
            decodedToken.decode(buffer);

            assertThat(decodedToken.isPresent()).isTrue();
            assertThat(((Number) decodedToken.getValue()).byteValue()).isEqualTo(expectedByteValue);
        } finally {
            buffer.release();
        }
    }

    @Test(groups = { "unit" })
    public void readConsistencyStrategyOverWireValuesMatchEnum() {
        assertThat(ReadConsistencyStrategy.EVENTUAL.toString()).isEqualTo("Eventual");
        assertThat(ReadConsistencyStrategy.SESSION.toString()).isEqualTo("Session");
        assertThat(ReadConsistencyStrategy.LATEST_COMMITTED.toString()).isEqualTo("LatestCommitted");
        assertThat(ReadConsistencyStrategy.GLOBAL_STRONG.toString()).isEqualTo("GlobalStrong");
        assertThat(ReadConsistencyStrategy.DEFAULT.toString()).isEqualTo("Default");
    }

    @Test(groups = { "unit" })
    public void readConsistencyStrategyRntbdByteEnumValues() {
        assertThat(RntbdConstants.RntbdReadConsistencyStrategy.Eventual.id()).isEqualTo((byte) 0x01);
        assertThat(RntbdConstants.RntbdReadConsistencyStrategy.Session.id()).isEqualTo((byte) 0x02);
        assertThat(RntbdConstants.RntbdReadConsistencyStrategy.LatestCommitted.id()).isEqualTo((byte) 0x03);
        assertThat(RntbdConstants.RntbdReadConsistencyStrategy.GlobalStrong.id()).isEqualTo((byte) 0x04);
    }

    @Test(groups = { "unit" })
    public void readConsistencyStrategyHttpHeaderConstant() {
        assertThat(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY)
            .isEqualTo("x-ms-cosmos-read-consistency-strategy");
    }

    // region ThinClientStoreModel RNTBD encoding via wrapInHttpRequest()

    @DataProvider(name = "readConsistencyStrategyStringToRntbdByteValues")
    public Object[][] readConsistencyStrategyStringToRntbdByteValues() {
        return new Object[][] {
            { "LatestCommitted", (byte) 0x03 },
            { "Eventual", (byte) 0x01 },
            { "Session", (byte) 0x02 },
            { "GlobalStrong", (byte) 0x04 },
        };
    }

    @Test(groups = { "unit" }, dataProvider = "readConsistencyStrategyStringToRntbdByteValues")
    public void thinClient_wrapInHttpRequest_readConsistencyStrategyEncodedInRntbdFrame(String readConsistencyStrategyValue, byte expectedByte) throws Exception {
        // Calls ThinClientStoreModel.wrapInHttpRequest() — the actual production code —
        // and verifies the RNTBD frame in the HTTP body contains the correct readConsistencyStrategy byte.

        ThinClientStoreModel storeModel = createMockThinClientStoreModel();

        RxDocumentServiceRequest request = createDocumentReadRequest();
        request.getHeaders().put(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY, readConsistencyStrategyValue);
        request.getHeaders().remove(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL);

        com.azure.cosmos.implementation.http.HttpRequest httpRequest =
            storeModel.wrapInHttpRequest(request, URI.create("https://test-proxy:10250/"));

        byte[] rntbdFrame = collectHttpBody(httpRequest);
        ByteBuf buffer = Unpooled.wrappedBuffer(rntbdFrame);
        try {
            assertThat(containsRntbdHeaderWithByte(buffer, (short) 0x00F0, expectedByte))
                .as("RNTBD frame from wrapInHttpRequest should contain readConsistencyStrategy header 0x00F0=0x%02X for %s",
                    expectedByte, readConsistencyStrategyValue)
                .isTrue();
        } finally {
            buffer.release();
        }
    }

    @Test(groups = { "unit" })
    public void thinClient_wrapInHttpRequest_noReadConsistencyStrategyHeader_noRntbdToken() throws Exception {
        ThinClientStoreModel storeModel = createMockThinClientStoreModel();

        RxDocumentServiceRequest request = createDocumentReadRequest();
        // No readConsistencyStrategy header set

        com.azure.cosmos.implementation.http.HttpRequest httpRequest =
            storeModel.wrapInHttpRequest(request, URI.create("https://test-proxy:10250/"));

        byte[] rntbdFrame = collectHttpBody(httpRequest);
        ByteBuf buffer = Unpooled.wrappedBuffer(rntbdFrame);
        try {
            assertThat(containsRntbdHeaderId(buffer, (short) 0x00F0))
                .as("RNTBD frame should NOT contain readConsistencyStrategy header when not set")
                .isFalse();
        } finally {
            buffer.release();
        }
    }

    @Test(groups = { "unit" })
    public void thinClient_wrapInHttpRequest_readConsistencyStrategyPresent_clAbsent() throws Exception {
        ThinClientStoreModel storeModel = createMockThinClientStoreModel();

        RxDocumentServiceRequest request = createDocumentReadRequest();
        request.getHeaders().put(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY, "LatestCommitted");
        request.getHeaders().remove(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL);

        com.azure.cosmos.implementation.http.HttpRequest httpRequest =
            storeModel.wrapInHttpRequest(request, URI.create("https://test-proxy:10250/"));

        byte[] rntbdFrame = collectHttpBody(httpRequest);
        ByteBuf buffer = Unpooled.wrappedBuffer(rntbdFrame);
        try {
            assertThat(containsRntbdHeaderWithByte(buffer, (short) 0x00F0, (byte) 0x03))
                .as("RNTBD frame should contain readConsistencyStrategy=LatestCommitted (0x03)")
                .isTrue();
            assertThat(containsRntbdHeaderWithAnyValue(buffer, (short) 0x0010))
                .as("RNTBD frame should NOT contain ConsistencyLevel when readConsistencyStrategy is set")
                .isFalse();
        } finally {
            buffer.release();
        }
    }

    @Test(groups = { "unit" })
    public void thinClient_resolveAndWrap_bothClAndReadConsistencyStrategy_onlyReadConsistencyStrategySurvivesInFrame() throws Exception {
        // End-to-end chain: dirty headers (both CL and readConsistencyStrategy set)
        //   → resolveEffectiveConsistencyHeaders (strips CL)
        //   → wrapInHttpRequest (encodes RNTBD frame)
        //   → verify only readConsistencyStrategy in the frame, CL absent
        ThinClientStoreModel storeModel = createMockThinClientStoreModel();

        RxDocumentServiceRequest request = createDocumentReadRequest();
        // Pre-resolution state: both headers present (as getRequestHeaders would set them
        // before resolveEffectiveConsistencyHeaders runs in performRequestInternalCore)
        request.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, "Session");
        request.getHeaders().put(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY, "LatestCommitted");

        // Run the same resolution logic that performRequestInternalCore() calls
        resolveEffectiveConsistencyHeaders(request);

        // Now call wrapInHttpRequest with the resolved headers
        com.azure.cosmos.implementation.http.HttpRequest httpRequest =
            storeModel.wrapInHttpRequest(request, URI.create("https://test-proxy:10250/"));

        byte[] rntbdFrame = collectHttpBody(httpRequest);
        ByteBuf buffer = Unpooled.wrappedBuffer(rntbdFrame);
        try {
            assertThat(containsRntbdHeaderWithByte(buffer, (short) 0x00F0, (byte) 0x03))
                .as("readConsistencyStrategy=LatestCommitted (0x03) should survive in the RNTBD frame")
                .isTrue();
            assertThat(containsRntbdHeaderWithAnyValue(buffer, (short) 0x0010))
                .as("ConsistencyLevel should be stripped — only readConsistencyStrategy survives on the wire")
                .isFalse();
        } finally {
            buffer.release();
        }
    }

    @Test(groups = { "unit" })
    public void thinClient_resolveAndWrap_requestContextReadConsistencyStrategy_overridesHeaderReadConsistencyStrategy() throws Exception {
        // Request-level readConsistencyStrategy (requestContext) takes priority over header-level (client-level) readConsistencyStrategy
        ThinClientStoreModel storeModel = createMockThinClientStoreModel();

        RxDocumentServiceRequest request = createDocumentReadRequest();
        request.getHeaders().put(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY, "Eventual");
        request.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, "Session");
        request.requestContext.readConsistencyStrategy = ReadConsistencyStrategy.LATEST_COMMITTED;

        resolveEffectiveConsistencyHeaders(request);

        com.azure.cosmos.implementation.http.HttpRequest httpRequest =
            storeModel.wrapInHttpRequest(request, URI.create("https://test-proxy:10250/"));

        byte[] rntbdFrame = collectHttpBody(httpRequest);
        ByteBuf buffer = Unpooled.wrappedBuffer(rntbdFrame);
        try {
            // Request-level LATEST_COMMITTED (0x03) should win over header-level Eventual
            assertThat(containsRntbdHeaderWithByte(buffer, (short) 0x00F0, (byte) 0x03))
                .as("Request-level readConsistencyStrategy=LatestCommitted should override header-level readConsistencyStrategy=Eventual")
                .isTrue();
            assertThat(containsRntbdHeaderWithAnyValue(buffer, (short) 0x0010))
                .as("ConsistencyLevel should be stripped")
                .isFalse();
        } finally {
            buffer.release();
        }
    }

    @Test(groups = { "unit" })
    public void thinClient_resolveAndWrap_defaultReadConsistencyStrategy_clSurvives() throws Exception {
        // DEFAULT readConsistencyStrategy is transparent — CL should remain in the RNTBD frame
        ThinClientStoreModel storeModel = createMockThinClientStoreModel();

        RxDocumentServiceRequest request = createDocumentReadRequest();
        request.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, "Session");
        request.requestContext.readConsistencyStrategy = ReadConsistencyStrategy.DEFAULT;

        resolveEffectiveConsistencyHeaders(request);

        com.azure.cosmos.implementation.http.HttpRequest httpRequest =
            storeModel.wrapInHttpRequest(request, URI.create("https://test-proxy:10250/"));

        byte[] rntbdFrame = collectHttpBody(httpRequest);
        ByteBuf buffer = Unpooled.wrappedBuffer(rntbdFrame);
        try {
            assertThat(containsRntbdHeaderId(buffer, (short) 0x00F0))
                .as("DEFAULT readConsistencyStrategy should not emit readConsistencyStrategy header")
                .isFalse();
            // Session = byte 0x03 in RntbdConsistencyLevel enum
            assertThat(containsRntbdHeaderWithByte(buffer, (short) 0x0010,
                    RntbdConstants.RntbdConsistencyLevel.Session.id()))
                .as("ConsistencyLevel=Session should survive when readConsistencyStrategy is DEFAULT")
                .isTrue();
        } finally {
            buffer.release();
        }
    }

    // endregion

    // region Helpers

    private static ThinClientStoreModel createMockThinClientStoreModel() {
        DatabaseAccount mockAccount = Mockito.mock(DatabaseAccount.class);
        Mockito.when(mockAccount.getId()).thenReturn("test-account");

        GlobalEndpointManager mockGem = Mockito.mock(GlobalEndpointManager.class);
        Mockito.when(mockGem.getLatestDatabaseAccount()).thenReturn(mockAccount);

        return new ThinClientStoreModel(
            null, // clientContext — not used in wrapInHttpRequest
            Mockito.mock(ISessionContainer.class),
            ConsistencyLevel.SESSION,
            new UserAgentContainer(),
            mockGem,
            Mockito.mock(com.azure.cosmos.implementation.http.HttpClient.class));
    }

    private static RxDocumentServiceRequest createDocumentReadRequest() {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(
            null, OperationType.Read, "dbs/testdb/colls/testcoll/docs/testdoc", ResourceType.Document);
        // Set resolved partition key range to avoid NPE in wrapInHttpRequest
        request.requestContext.resolvedPartitionKeyRange = new PartitionKeyRange();
        request.requestContext.resolvedPartitionKeyRange.setMinInclusive("00");
        request.requestContext.resolvedPartitionKeyRange.setMaxExclusive("FF");
        return request;
    }

    private static byte[] collectHttpBody(com.azure.cosmos.implementation.http.HttpRequest httpRequest) {
        return httpRequest.body().reduce((a, b) -> {
            byte[] merged = new byte[a.length + b.length];
            System.arraycopy(a, 0, merged, 0, a.length);
            System.arraycopy(b, 0, merged, a.length, b.length);
            return merged;
        }).block();
    }

    /**
     * Mirrors RxGatewayStoreModel.resolveEffectiveConsistencyHeaders() exactly.
     * This is the centralized contention resolution that runs in performRequestInternalCore()
     * before wrapInHttpRequest() is called.
     */
    private static void resolveEffectiveConsistencyHeaders(RxDocumentServiceRequest request) {
        Map<String, String> headers = request.getHeaders();

        ReadConsistencyStrategy effectiveReadConsistencyStrategy = null;
        if (request.requestContext != null
            && request.requestContext.readConsistencyStrategy != null
            && request.requestContext.readConsistencyStrategy != ReadConsistencyStrategy.DEFAULT) {
            effectiveReadConsistencyStrategy = request.requestContext.readConsistencyStrategy;
        }

        if (effectiveReadConsistencyStrategy == null) {
            String readConsistencyStrategyHeaderValue = headers.get(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY);
            if (readConsistencyStrategyHeaderValue != null && !readConsistencyStrategyHeaderValue.isEmpty()) {
                effectiveReadConsistencyStrategy = ReadConsistencyStrategy.DEFAULT;
                for (ReadConsistencyStrategy candidate : ReadConsistencyStrategy.values()) {
                    if (candidate != ReadConsistencyStrategy.DEFAULT
                        && candidate.toString().equals(readConsistencyStrategyHeaderValue)) {
                        effectiveReadConsistencyStrategy = candidate;
                        break;
                    }
                }
            }
        }

        if (effectiveReadConsistencyStrategy != null && effectiveReadConsistencyStrategy != ReadConsistencyStrategy.DEFAULT) {
            headers.remove(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL);
            headers.put(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY, effectiveReadConsistencyStrategy.toString());
        }
    }

    // region RNTBD frame helpers

    /**
     * Scans encoded RNTBD bytes for a header with the given ID and Byte value.
     * RNTBD Byte tokens are encoded as: [headerID: 2 bytes LE] [tokenType: 1 byte = 0x00 for Byte] [value: 1 byte]
     */
    private static boolean containsRntbdHeaderWithByte(ByteBuf buffer, short headerId, byte expectedValue) {
        byte idLow = (byte) (headerId & 0xFF);
        byte idHigh = (byte) ((headerId >> 8) & 0xFF);

        for (int i = 0; i < buffer.writerIndex() - 3; i++) {
            if (buffer.getByte(i) == idLow
                && buffer.getByte(i + 1) == idHigh
                && buffer.getByte(i + 2) == 0x00  // RntbdTokenType.Byte
                && buffer.getByte(i + 3) == expectedValue) {
                return true;
            }
        }
        return false;
    }

    /**
     * Scans encoded RNTBD bytes for a header ID presence (any token type).
     */
    private static boolean containsRntbdHeaderId(ByteBuf buffer, short headerId) {
        byte idLow = (byte) (headerId & 0xFF);
        byte idHigh = (byte) ((headerId >> 8) & 0xFF);

        for (int i = 0; i < buffer.writerIndex() - 1; i++) {
            if (buffer.getByte(i) == idLow && buffer.getByte(i + 1) == idHigh) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a Byte-type RNTBD header has any non-zero value set.
     * ConsistencyLevel (0x0010) is Byte type — if not set, the token is not present.
     */
    private static boolean containsRntbdHeaderWithAnyValue(ByteBuf buffer, short headerId) {
        byte idLow = (byte) (headerId & 0xFF);
        byte idHigh = (byte) ((headerId >> 8) & 0xFF);

        for (int i = 0; i < buffer.writerIndex() - 2; i++) {
            if (buffer.getByte(i) == idLow
                && buffer.getByte(i + 1) == idHigh
                && buffer.getByte(i + 2) == 0x00) { // Byte token type
                return true;
            }
        }
        return false;
    }

    // endregion
}
