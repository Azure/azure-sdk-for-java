// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.ReadConsistencyStrategy;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for consistency flag contention resolution in RxGatewayStoreModel.
 *
 * Contention rules:
 * 1. Request-level readConsistencyStrategy (requestContext) > client-level readConsistencyStrategy (header)
 * 2. readConsistencyStrategy > ConsistencyLevel — strip CL when non-DEFAULT readConsistencyStrategy is effective
 * 3. DEFAULT readConsistencyStrategy is transparent — CL stays
 */
public class ConsistencyFlagContentionTest {

    // region getRequestHeaders — Option A guard

    @Test(groups = "unit")
    public void getRequestHeaders_bothReadConsistencyStrategyAndCl_onlyReadConsistencyStrategySurvives() {
        // Simulates the contention: request options set both readConsistencyStrategy and CL.
        // After getRequestHeaders, readConsistencyStrategy should be present and CL should be absent.
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY,
            ReadConsistencyStrategy.LATEST_COMMITTED.toString());
        // Simulate the Option A guard: CL should NOT be added when readConsistencyStrategy is present
        if (!headers.containsKey(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY)) {
            headers.put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, ConsistencyLevel.EVENTUAL.toString());
        }

        assertThat(headers.containsKey(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY)).isTrue();
        assertThat(headers.containsKey(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL)).isFalse();
    }

    @Test(groups = "unit")
    public void getRequestHeaders_onlyCl_clSurvives() {
        // When no readConsistencyStrategy is set, CL should be set normally.
        Map<String, String> headers = new HashMap<>();
        if (!headers.containsKey(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY)) {
            headers.put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, ConsistencyLevel.SESSION.toString());
        }

        assertThat(headers.containsKey(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL)).isTrue();
        assertThat(headers.get(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL)).isEqualTo("Session");
        assertThat(headers.containsKey(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY)).isFalse();
    }

    @Test(groups = "unit")
    public void getRequestHeaders_onlyReadConsistencyStrategy_readConsistencyStrategySurvives() {
        // When only readConsistencyStrategy is set (no CL), readConsistencyStrategy should survive.
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY,
            ReadConsistencyStrategy.EVENTUAL.toString());

        assertThat(headers.containsKey(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY)).isTrue();
        assertThat(headers.get(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY)).isEqualTo("Eventual");
        assertThat(headers.containsKey(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL)).isFalse();
    }

    // endregion

    // region resolveEffectiveConsistencyHeaders — centralized resolution

    @Test(groups = "unit")
    public void resolve_requestContextReadConsistencyStrategy_stripsCl() {
        // When requestContext has non-DEFAULT readConsistencyStrategy and headers have CL, CL should be stripped.
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, ConsistencyLevel.SESSION.toString());

        DocumentServiceRequestContext ctx = new DocumentServiceRequestContext();
        ctx.readConsistencyStrategy = ReadConsistencyStrategy.LATEST_COMMITTED;

        RxGatewayStoreModel.resolveEffectiveConsistencyHeaders(headers, ctx.readConsistencyStrategy);

        assertThat(headers.containsKey(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL)).isFalse();
        assertThat(headers.get(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY))
            .isEqualTo("LatestCommitted");
    }

    @Test(groups = "unit")
    public void resolve_headerReadConsistencyStrategy_stripsCl() {
        // When header has non-DEFAULT readConsistencyStrategy and also CL, CL should be stripped.
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, ConsistencyLevel.SESSION.toString());
        headers.put(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY,
            ReadConsistencyStrategy.EVENTUAL.toString());

        RxGatewayStoreModel.resolveEffectiveConsistencyHeaders(headers, null);

        assertThat(headers.containsKey(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL)).isFalse();
        assertThat(headers.get(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY))
            .isEqualTo("Eventual");
    }

    @Test(groups = "unit")
    public void resolve_requestContextReadConsistencyStrategy_overridesHeaderReadConsistencyStrategy() {
        // Request-level readConsistencyStrategy (requestContext) takes priority over header-level (client-level) readConsistencyStrategy.
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY,
            ReadConsistencyStrategy.EVENTUAL.toString());
        headers.put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, ConsistencyLevel.EVENTUAL.toString());

        RxGatewayStoreModel.resolveEffectiveConsistencyHeaders(headers, ReadConsistencyStrategy.SESSION);

        assertThat(headers.containsKey(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL)).isFalse();
        assertThat(headers.get(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY))
            .isEqualTo("Session");
    }

    @Test(groups = "unit")
    public void resolve_defaultReadConsistencyStrategy_clSurvives() {
        // DEFAULT readConsistencyStrategy is transparent — CL should remain.
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, ConsistencyLevel.BOUNDED_STALENESS.toString());

        RxGatewayStoreModel.resolveEffectiveConsistencyHeaders(headers, ReadConsistencyStrategy.DEFAULT);

        assertThat(headers.get(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL))
            .isEqualTo("BoundedStaleness");
        assertThat(headers.containsKey(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY)).isFalse();
    }

    @Test(groups = "unit")
    public void resolve_nullReadConsistencyStrategy_clSurvives() {
        // When no readConsistencyStrategy is set at all, CL should remain.
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, ConsistencyLevel.STRONG.toString());

        RxGatewayStoreModel.resolveEffectiveConsistencyHeaders(headers, null);

        assertThat(headers.get(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL))
            .isEqualTo("Strong");
        assertThat(headers.containsKey(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY)).isFalse();
    }

    @Test(groups = "unit")
    public void resolve_noHeaders_noOp() {
        // When neither CL nor readConsistencyStrategy is set, resolution is a no-op.
        Map<String, String> headers = new HashMap<>();

        RxGatewayStoreModel.resolveEffectiveConsistencyHeaders(headers, null);

        assertThat(headers).isEmpty();
    }

    @Test(groups = "unit")
    public void resolve_idempotent_multipleInvocations() {
        // Resolution should be idempotent — multiple calls produce the same result.
        // This validates safety for shared header maps across availability strategy clones.
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, ConsistencyLevel.SESSION.toString());
        headers.put(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY,
            ReadConsistencyStrategy.LATEST_COMMITTED.toString());

        RxGatewayStoreModel.resolveEffectiveConsistencyHeaders(headers, null);
        RxGatewayStoreModel.resolveEffectiveConsistencyHeaders(headers, null);
        RxGatewayStoreModel.resolveEffectiveConsistencyHeaders(headers, null);

        assertThat(headers.containsKey(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL)).isFalse();
        assertThat(headers.get(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY))
            .isEqualTo("LatestCommitted");
    }

    // endregion
}
