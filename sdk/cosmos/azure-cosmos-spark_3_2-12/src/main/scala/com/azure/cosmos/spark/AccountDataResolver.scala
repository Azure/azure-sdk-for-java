// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

import reactor.core.publisher.Mono

trait AccountDataResolver {
    def getAccountDataConfig(configs : Map[String, String]): Map[String, String]

    def getManagedIdentityTokenProvider: List[String] => Mono[CosmosAccessToken]
}
