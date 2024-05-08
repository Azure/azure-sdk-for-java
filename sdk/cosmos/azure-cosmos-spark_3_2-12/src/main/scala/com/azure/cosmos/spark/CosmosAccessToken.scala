// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.spark

import java.time.OffsetDateTime

/**
 * Class representing access tokens
 * @param token the actual token
 * @param Offset the expiration time of the token
 */
case class CosmosAccessToken(token: String, Offset: OffsetDateTime)
