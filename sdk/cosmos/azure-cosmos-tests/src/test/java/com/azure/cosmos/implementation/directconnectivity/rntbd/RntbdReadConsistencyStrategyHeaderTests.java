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
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.RxGatewayStoreModel;
import com.azure.cosmos.implementation.ThinClientStoreModel;
import com.azure.cosmos.implementation.UserAgentContainer;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Unit tests for the ReadConsistencyStrategy RNTBD header (token ID 0x00FE, Byte type).
 *
 * <h3>Why these tests exist</h3>
 * The thin client proxy (Gateway V2) deserializes ReadConsistencyStrategy from the RNTBD binary
 * frame — not from HTTP headers. A mismatch between the Java SDK's enum byte values and the
 * proxy's C++ enum causes the proxy to either reject the request or apply the wrong strategy
 * silently. These tests guard the contract:
 * <ul>
 *   <li>Token metadata (ID = 0x00FE, type = Byte, optional)</li>
 *   <li>Byte encoding per strategy (Eventual=0x01, Session=0x02, LatestCommitted=0x03, GlobalStrong=0x04)</li>
 *   <li>HTTP header string → RNTBD byte mapping via {@code RntbdRequestHeaders.addReadConsistencyStrategy()}</li>
 *   <li>Full resolve → encode pipeline: {@code resolveEffectiveConsistencyHeaders} + {@code wrapInHttpRequest}</li>
 * </ul>
 *
 * <h3>Consistency headers decision matrix</h3>
 * Users can set ConsistencyLevel (ConsistencyLevel) and ReadConsistencyStrategy (ReadConsistencyStrategy) at both client and
 * request level. The SDK resolves contention before wire serialization:
 * <pre>
 * | Client ConsistencyLevel | Client ReadConsistencyStrategy | Request ConsistencyLevel | Request ReadConsistencyStrategy | Effective on wire           |
 * |-----------|------------|------------|-------------|-----------------------------|
 * | Session   | —          | —          | —           | ConsistencyLevel=Session (default)        |
 * | Session   | —          | —          | LC          | ReadConsistencyStrategy=LC, ConsistencyLevel stripped         |
 * | Session   | Eventual   | —          | LC          | ReadConsistencyStrategy=LC (req ReadConsistencyStrategy > client)   |
 * | Session   | Eventual   | Eventual   | —           | ReadConsistencyStrategy=Eventual, ConsistencyLevel stripped   |
 * | Session   | —          | Eventual   | LC          | ReadConsistencyStrategy=LC (req ReadConsistencyStrategy > req ConsistencyLevel)   |
 * | Session   | LC         | —          | —           | ReadConsistencyStrategy=LC, ConsistencyLevel stripped         |
 * | Session   | —          | —          | GLOBAL_STRONG| BadRequestException (non-Strong acct) |
 * </pre>
 * Resolution rule: request-level ReadConsistencyStrategy &gt; client-level ReadConsistencyStrategy &gt; ConsistencyLevel. When a non-DEFAULT ReadConsistencyStrategy is
 * effective, ConsistencyLevel is stripped to prevent dual-header rejection by the compute gateway or proxy.
 *
 * <h3>Test regions</h3>
 * <ol>
 *   <li><b>Token-level</b> — RNTBD token encoding, decoding, metadata, and enum constants.</li>
 *   <li><b>ThinClientStoreModel encoding</b> — {@code wrapInHttpRequest()} produces correct RNTBD
 *       frame bytes for each strategy value.</li>
 *   <li><b>Resolve + encode pipeline</b> — {@code resolveEffectiveConsistencyHeaders()} followed by
 *       {@code wrapInHttpRequest()} produces the correct frame for contention scenarios (both ConsistencyLevel
 *       and ReadConsistencyStrategy set, request-level overrides client-level, DEFAULT is transparent).</li>
 * </ol>
 */
public class RntbdReadConsistencyStrategyHeaderTests {

    // region Data providers

    @DataProvider(name = "readConsistencyStrategyToRntbdByteValues")
    public Object[][] readConsistencyStrategyToRntbdByteValues() {
        return new Object[][] {
            { ReadConsistencyStrategy.EVENTUAL, RntbdConstants.RntbdReadConsistencyStrategy.Eventual.id() },
            { ReadConsistencyStrategy.SESSION, RntbdConstants.RntbdReadConsistencyStrategy.Session.id() },
            { ReadConsistencyStrategy.LATEST_COMMITTED, RntbdConstants.RntbdReadConsistencyStrategy.LatestCommitted.id() },
            { ReadConsistencyStrategy.GLOBAL_STRONG, RntbdConstants.RntbdReadConsistencyStrategy.GlobalStrong.id() },
        };
    }

    @DataProvider(name = "readConsistencyStrategyStringToRntbdByteValues")
    public Object[][] readConsistencyStrategyStringToRntbdByteValues() {
        return new Object[][] {
            { "LatestCommitted", (byte) 0x03 },
            { "Eventual", (byte) 0x01 },
            { "Session", (byte) 0x02 },
            { "GlobalStrong", (byte) 0x04 },
        };
    }

    // endregion

    // region Token-level tests — RNTBD token metadata, encoding, and constants

    @Test(groups = { "unit" })
    public void readConsistencyStrategyTokenMetadata() {
        assertThat(RntbdConstants.RntbdRequestHeader.ReadConsistencyStrategy.id())
            .isEqualTo((short) 0x00FE);
        assertThat(RntbdConstants.RntbdRequestHeader.ReadConsistencyStrategy.type())
            .isEqualTo(RntbdTokenType.Byte);
        assertThat(RntbdConstants.RntbdRequestHeader.ReadConsistencyStrategy.isRequired())
            .isFalse();
    }

    @Test(groups = { "unit" }, dataProvider = "readConsistencyStrategyToRntbdByteValues")
    public void readConsistencyStrategyTokenEncodesCorrectly(
        ReadConsistencyStrategy ignoredStrategy,
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
    public void readConsistencyStrategyTokenNotPresentWhenNotSet() {
        RntbdToken token = RntbdToken.create(
            RntbdConstants.RntbdRequestHeader.ReadConsistencyStrategy);
        assertThat(token.isPresent()).isFalse();
    }

    // endregion

    // region Token round-trip — verifies encode/decode symmetry for the ReadConsistencyStrategy
    // Byte token. Guards against RNTBD frame corruption if the token serialization format changes.

    @Test(groups = { "unit" }, dataProvider = "readConsistencyStrategyToRntbdByteValues")
    public void readConsistencyStrategyTokenRoundTrips(
        ReadConsistencyStrategy ignoredStrategy,
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

    // region No contention — single header encoding. Only one of ConsistencyLevel or ReadConsistencyStrategy is set (or neither).
    // No resolution needed; tests pure RNTBD encoder correctness.

    @Test(groups = { "unit" }, dataProvider = "readConsistencyStrategyStringToRntbdByteValues")
    public void thinClient_wrapInHttpRequest_readConsistencyStrategyEncodedInRntbdFrame(String readConsistencyStrategyValue, byte expectedByte) throws Exception {
        // Calls ThinClientStoreModel.wrapInHttpRequest() — the actual production code —
        // and verifies the RNTBD frame in the HTTP body contains the correct readConsistencyStrategy byte.

        ThinClientStoreModel storeModel = createMockThinClientStoreModel();

        RxDocumentServiceRequest request = createDocumentReadRequest();
        request.getHeaders().put(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY, readConsistencyStrategyValue);
        request.getHeaders().remove(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL);

        HttpRequest httpRequest =
            storeModel.wrapInHttpRequest(request, URI.create("https://test-proxy:10250/"));

        byte[] rntbdFrame = collectHttpBody(httpRequest);
        RntbdRequest decoded = decodeRntbdFrame(rntbdFrame);
        Byte rcsValue = decoded.getHeader(RntbdConstants.RntbdRequestHeader.ReadConsistencyStrategy);
        assertThat(rcsValue)
            .as("RNTBD frame should contain readConsistencyStrategy 0x%02X for %s", expectedByte, readConsistencyStrategyValue)
            .isEqualTo(expectedByte);
    }

    @Test(groups = { "unit" })
    public void thinClient_wrapInHttpRequest_noReadConsistencyStrategyHeader_noRntbdToken() throws Exception {
        ThinClientStoreModel storeModel = createMockThinClientStoreModel();

        RxDocumentServiceRequest request = createDocumentReadRequest();
        // No readConsistencyStrategy header set

        HttpRequest httpRequest =
            storeModel.wrapInHttpRequest(request, URI.create("https://test-proxy:10250/"));

        byte[] rntbdFrame = collectHttpBody(httpRequest);
        RntbdRequest decoded = decodeRntbdFrame(rntbdFrame);
        Byte rcsValue = decoded.getHeader(RntbdConstants.RntbdRequestHeader.ReadConsistencyStrategy);
        assertThat(rcsValue)
            .as("ReadConsistencyStrategy token should not be set when header is absent (0 = unset)")
            .isEqualTo((byte) 0);
    }

    @Test(groups = { "unit" })
    public void thinClient_wrapInHttpRequest_readConsistencyStrategyPresent_consistencyLevelAbsent() throws Exception {
        ThinClientStoreModel storeModel = createMockThinClientStoreModel();

        RxDocumentServiceRequest request = createDocumentReadRequest();
        request.getHeaders().put(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY, "LatestCommitted");
        request.getHeaders().remove(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL);

        HttpRequest httpRequest =
            storeModel.wrapInHttpRequest(request, URI.create("https://test-proxy:10250/"));

        byte[] rntbdFrame = collectHttpBody(httpRequest);
        RntbdRequest decoded = decodeRntbdFrame(rntbdFrame);
        Byte rcsValue = decoded.getHeader(RntbdConstants.RntbdRequestHeader.ReadConsistencyStrategy);
        assertThat(rcsValue)
            .as("ReadConsistencyStrategy should be LatestCommitted (0x03)")
            .isEqualTo((byte) 0x03);
        Byte clValue = decoded.getHeader(RntbdConstants.RntbdRequestHeader.ConsistencyLevel);
        assertThat(clValue)
            .as("ConsistencyLevel should not be set when ReadConsistencyStrategy is present (0 = unset)")
            .isEqualTo((byte) 0);
    }

    // endregion

    // region Contention — both ConsistencyLevel and ReadConsistencyStrategy headers present. Tests resolveEffectiveConsistencyHeaders()
    // followed by wrapInHttpRequest() to verify the correct header wins on the wire.

    @Test(groups = { "unit" })
    public void thinClient_resolveAndWrap_bothClAndReadConsistencyStrategy_onlyReadConsistencyStrategySurvivesInFrame() throws Exception {
        // End-to-end chain: dirty headers (both ConsistencyLevel and readConsistencyStrategy set)
        //   → resolveEffectiveConsistencyHeaders (strips ConsistencyLevel)
        //   → wrapInHttpRequest (encodes RNTBD frame)
        //   → verify only readConsistencyStrategy in the frame, ConsistencyLevel absent
        ThinClientStoreModel storeModel = createMockThinClientStoreModel();

        RxDocumentServiceRequest request = createDocumentReadRequest();
        // Pre-resolution state: both headers present (as getRequestHeaders would set them
        // before resolveEffectiveConsistencyHeaders runs in performRequestInternalCore)
        request.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, "Session");
        request.getHeaders().put(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY, "LatestCommitted");

        // Run the same resolution logic that performRequestInternalCore() calls
        resolveEffectiveConsistencyHeaders(request);

        // Now call wrapInHttpRequest with the resolved headers
        HttpRequest httpRequest =
            storeModel.wrapInHttpRequest(request, URI.create("https://test-proxy:10250/"));

        byte[] rntbdFrame = collectHttpBody(httpRequest);
        RntbdRequest decoded = decodeRntbdFrame(rntbdFrame);
        Byte rcsValue = decoded.getHeader(RntbdConstants.RntbdRequestHeader.ReadConsistencyStrategy);
        assertThat(rcsValue)
            .as("readConsistencyStrategy=LatestCommitted (0x03) should survive in the RNTBD frame")
            .isEqualTo((byte) 0x03);
        Byte clValue = decoded.getHeader(RntbdConstants.RntbdRequestHeader.ConsistencyLevel);
        assertThat(clValue)
            .as("ConsistencyLevel should be stripped — only readConsistencyStrategy survives on the wire (0 = unset)")
            .isEqualTo((byte) 0);
    }

    @Test(groups = { "unit" })
    public void thinClient_resolveAndWrap_requestContextReadConsistencyStrategy_overridesHeaderReadConsistencyStrategy() throws Exception {
        // Header-level ReadConsistencyStrategy ("Eventual") = set by CosmosClientBuilder.readConsistencyStrategy(),
        // applied to every request via getRequestHeaders().
        // Request-level ReadConsistencyStrategy (LATEST_COMMITTED) = set by CosmosItemRequestOptions.setReadConsistencyStrategy(),
        // a per-operation override stored in requestContext.
        // Resolution rule: requestContext (per-request) > headers (client-level).
        ThinClientStoreModel storeModel = createMockThinClientStoreModel();

        RxDocumentServiceRequest request = createDocumentReadRequest();
        request.getHeaders().put(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY, "Eventual");       // client-level
        request.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, "Session");
        request.requestContext.readConsistencyStrategy = ReadConsistencyStrategy.LATEST_COMMITTED;        // per-request override

        resolveEffectiveConsistencyHeaders(request);

        HttpRequest httpRequest =
            storeModel.wrapInHttpRequest(request, URI.create("https://test-proxy:10250/"));

        byte[] rntbdFrame = collectHttpBody(httpRequest);
        RntbdRequest decoded = decodeRntbdFrame(rntbdFrame);
        Byte rcsValue = decoded.getHeader(RntbdConstants.RntbdRequestHeader.ReadConsistencyStrategy);
        assertThat(rcsValue)
            .as("Request-level readConsistencyStrategy=LatestCommitted should override header-level Eventual")
            .isEqualTo((byte) 0x03);
        Byte clValue = decoded.getHeader(RntbdConstants.RntbdRequestHeader.ConsistencyLevel);
        assertThat(clValue)
            .as("ConsistencyLevel should be stripped (0 = unset)")
            .isEqualTo((byte) 0);
    }

    @Test(groups = { "unit" })
    public void thinClient_resolveAndWrap_defaultReadConsistencyStrategy_consistencyLevelSurvives() throws Exception {
        // DEFAULT readConsistencyStrategy is transparent — ConsistencyLevel should remain in the RNTBD frame
        ThinClientStoreModel storeModel = createMockThinClientStoreModel();

        RxDocumentServiceRequest request = createDocumentReadRequest();
        request.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, "Session");
        request.requestContext.readConsistencyStrategy = ReadConsistencyStrategy.DEFAULT;

        resolveEffectiveConsistencyHeaders(request);

        HttpRequest httpRequest =
            storeModel.wrapInHttpRequest(request, URI.create("https://test-proxy:10250/"));

        byte[] rntbdFrame = collectHttpBody(httpRequest);
        RntbdRequest decoded = decodeRntbdFrame(rntbdFrame);
        Byte rcsValue = decoded.getHeader(RntbdConstants.RntbdRequestHeader.ReadConsistencyStrategy);
        assertThat(rcsValue)
            .as("DEFAULT readConsistencyStrategy should not be set (0 = unset)")
            .isEqualTo((byte) 0);
        Byte clValue = decoded.getHeader(RntbdConstants.RntbdRequestHeader.ConsistencyLevel);
        assertThat(clValue)
            .as("ConsistencyLevel=Session should survive when readConsistencyStrategy is DEFAULT")
            .isEqualTo(RntbdConstants.RntbdConsistencyLevel.Session.id());
    }

    @Test(groups = { "unit" })
    public void resolve_noHeaders_noOp() {
        Map<String, String> headers = new java.util.HashMap<>();
        RxGatewayStoreModel.resolveEffectiveConsistencyHeaders(headers, null);
        assertThat(headers.size()).isEqualTo(0);
    }

    @Test(groups = { "unit" })
    public void resolve_idempotent_multipleInvocations() {
        // Resolution should be idempotent — multiple calls produce the same result.
        // Validates safety for shared header maps across availability strategy clones.
        Map<String, String> headers = new java.util.HashMap<>();
        headers.put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, "Session");
        headers.put(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY, "LatestCommitted");

        RxGatewayStoreModel.resolveEffectiveConsistencyHeaders(headers, null);
        RxGatewayStoreModel.resolveEffectiveConsistencyHeaders(headers, null);
        RxGatewayStoreModel.resolveEffectiveConsistencyHeaders(headers, null);

        assertThat(headers.containsKey(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL)).isFalse();
        assertThat(headers.get(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY))
            .isEqualTo("LatestCommitted");
    }

    // endregion

    // region Invariant — every non-DEFAULT enum value must produce a non-zero RNTBD byte.

    /**
     * Guard against silent drops in {@code RntbdRequestHeaders.addReadConsistencyStrategy()}'s switch
     * statement. The switch ends with {@code default: assert false; break;} — in production JVMs
     * ({@code -da} is the default) a new {@link ReadConsistencyStrategy} enum value would slip through
     * without emitting an RNTBD byte and the thin-client proxy would silently apply the wrong
     * consistency. This test iterates {@link ReadConsistencyStrategy#values()} so adding a new value
     * forces this test to fail loudly until the switch is updated.
     */
    @Test(groups = { "unit" })
    public void thinClient_wrapInHttpRequest_allNonDefaultEnumValues_emitNonZeroRntbdByte() throws Exception {
        ThinClientStoreModel storeModel = createMockThinClientStoreModel();

        for (ReadConsistencyStrategy strategy : ReadConsistencyStrategy.values()) {
            if (strategy == ReadConsistencyStrategy.DEFAULT) {
                // DEFAULT is intentionally transparent — no RNTBD byte should be written.
                continue;
            }

            RxDocumentServiceRequest request = createDocumentReadRequest();
            request.getHeaders().put(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY, strategy.toString());
            request.getHeaders().remove(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL);

            HttpRequest httpRequest =
                storeModel.wrapInHttpRequest(request, URI.create("https://test-proxy:10250/"));

            byte[] rntbdFrame = collectHttpBody(httpRequest);
            RntbdRequest decoded = decodeRntbdFrame(rntbdFrame);
            Byte rcsValue = decoded.getHeader(RntbdConstants.RntbdRequestHeader.ReadConsistencyStrategy);

            assertThat(rcsValue)
                .as("ReadConsistencyStrategy.%s must encode to a non-zero RNTBD byte. "
                    + "If you added a new enum value, update RntbdRequestHeaders.addReadConsistencyStrategy() "
                    + "to map it to a RntbdConstants.RntbdReadConsistencyStrategy id.", strategy)
                .isNotEqualTo((byte) 0);
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
            Mockito.mock(HttpClient.class),
            null);
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

    private static byte[] collectHttpBody(HttpRequest httpRequest) {
        return httpRequest.body().reduce((a, b) -> {
            byte[] merged = new byte[a.length + b.length];
            System.arraycopy(a, 0, merged, 0, a.length);
            System.arraycopy(b, 0, merged, a.length, b.length);
            return merged;
        }).block();
    }

    private static void resolveEffectiveConsistencyHeaders(RxDocumentServiceRequest request) {
        RxGatewayStoreModel.resolveEffectiveConsistencyHeaders(
            request.getHeaders(),
            request.requestContext != null ? request.requestContext.readConsistencyStrategy : null);
    }

    // region RNTBD frame helpers

    /**
     * Decodes the RNTBD binary frame using the production decoder.
     * Token presence/absence is determined by the actual RNTBD wire format.
     */
    private static RntbdRequest decodeRntbdFrame(byte[] rntbdFrame) {
        ByteBuf buffer = Unpooled.wrappedBuffer(rntbdFrame);
        return RntbdRequest.decode(buffer);
    }

    // endregion
}
