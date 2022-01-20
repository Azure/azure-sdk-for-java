// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.dag;

/**
 * An TaskCancelledException is emitted when a task cannot be executed because the parent task group is
 * marked as cancelled.
 */
final class TaskCancelledException extends Exception {
}
