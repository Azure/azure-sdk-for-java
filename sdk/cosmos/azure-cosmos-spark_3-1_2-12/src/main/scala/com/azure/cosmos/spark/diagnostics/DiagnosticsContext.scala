// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark.diagnostics

private[spark] case class DiagnosticsContext(correlationActivityId: String, details: String)
