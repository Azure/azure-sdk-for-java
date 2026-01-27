// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.*;

public class DedicatedGatewayRequestOptionsTest {
    @Test(groups = {"unit"})
    public void setShardKey_shouldThrowOnNullOrEmpty() {
        DedicatedGatewayRequestOptions opts = new DedicatedGatewayRequestOptions();
        assertThatThrownBy(() -> opts.setShardKey(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("shardKey must not be null or empty");
        assertThatThrownBy(() -> opts.setShardKey(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("shardKey must not be null or empty");
    }

    @Test(groups = {"unit"})
    public void setShardKey_shouldThrowOnInvalidCharacters() {
        DedicatedGatewayRequestOptions opts = new DedicatedGatewayRequestOptions();
        assertThatThrownBy(() -> opts.setShardKey("abc$%"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("invalid characters");
        assertThatThrownBy(() -> opts.setShardKey("with space"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("invalid characters");
        assertThatThrownBy(() -> opts.setShardKey("abc@def"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("invalid characters");
    }

    @Test(groups = {"unit"})
    public void setShardKey_shouldAcceptValidKey() {
        DedicatedGatewayRequestOptions opts = new DedicatedGatewayRequestOptions();
        String valid = "abc-DEF-123";
        opts.setShardKey(valid);
        assertThat(opts.getShardKey()).isEqualTo(valid);
    }

    @Test(groups = {"unit"})
    public void setShardKey_shouldAcceptMaxLength() {
        DedicatedGatewayRequestOptions opts = new DedicatedGatewayRequestOptions();
        // 36 chars (max length)
        String maxLen = "a123456789012345678901234567890123456";
        opts.setShardKey(maxLen);
        assertThat(opts.getShardKey()).isEqualTo(maxLen);
    }

    @Test(groups = {"unit"})
    public void setShardKey_shouldAcceptMaxBytes() {
        DedicatedGatewayRequestOptions opts = new DedicatedGatewayRequestOptions();
        // 72 bytes, all ASCII
        String seventyTwoAscii = "a123456789012345678901234567890123456789012345678901234567890123456789012";
        opts.setShardKey(seventyTwoAscii);
        assertThat(opts.getShardKey()).isEqualTo(seventyTwoAscii);
    }
}

