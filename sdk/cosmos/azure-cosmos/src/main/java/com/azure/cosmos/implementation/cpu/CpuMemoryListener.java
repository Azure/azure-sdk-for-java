// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.cpu;

/**
 * any client interested in receiving cpu info should implement this and use
 * and invoke {@link CpuMemoryMonitor#register(CpuMemoryListener)} when starting up and
 * {@link CpuMemoryMonitor#unregister(CpuMemoryListener)} } when shutting down.
 *
 * This is merely is used as a singal to {@link CpuMemoryMonitor} to control whether it should keep using
 * its internal thread or it it should shut it down in the absence of any CosmosClient.
 */
public interface CpuMemoryListener {
}
