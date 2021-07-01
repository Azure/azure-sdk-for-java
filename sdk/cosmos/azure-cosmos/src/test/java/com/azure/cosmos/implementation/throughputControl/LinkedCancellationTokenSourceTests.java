// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LinkedCancellationTokenSourceTests {

    @Test(groups = "unit")
    public void linkedCancellationTokenSource() {
        LinkedCancellationTokenSource source1 = new LinkedCancellationTokenSource();
        LinkedCancellationTokenSource source2 = new LinkedCancellationTokenSource(source1.getToken());
        LinkedCancellationToken source2Token = source2.getToken();
        LinkedCancellationTokenSource source3 = new LinkedCancellationTokenSource(source2.getToken());
        LinkedCancellationToken source3Token = source3.getToken();

        source1.close();

        assertThat(source2.isClosed()).isTrue();
        assertThat(source2Token.isCancellationRequested()).isTrue();
        assertThat(source3.isClosed()).isTrue();
        assertThat(source3Token.isCancellationRequested()).isTrue();

    }
}
