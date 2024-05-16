// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark.diagnostics

import java.util.UUID

private[spark] case class DiagnosticsContext(correlationActivityId: UUID, details: String)
