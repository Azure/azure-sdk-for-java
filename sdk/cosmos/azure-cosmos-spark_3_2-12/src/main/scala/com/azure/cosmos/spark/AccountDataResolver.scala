// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark

private trait AccountDataResolver {
    def getAccountDataConfig(configs : Map[String, String]): Map[String, String]
}
