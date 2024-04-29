// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

trait AccountDataResolver {
    def getAccountDataConfig(configs : Map[String, String]): Map[String, String]

    // @TODO fabianm - leaving this commented out for now. Below is how I would envision
    // exposing the option to use linked service tokens eventually when the LinkedService for
    // Cosmos DB supports ManagedIdentity authentication
    // def getManagedIdentityTokenProvider: List[String] => Mono[CosmosAccessToken]
}
