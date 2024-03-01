// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.sink.idstrategy;

public class ProvidedInKeyStrategy extends ProvidedInStrategy {
    public ProvidedInKeyStrategy() {
        super(ProvidedIn.KEY);
    }
}
