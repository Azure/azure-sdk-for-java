/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

import java.time.*;
import java.util.concurrent.*;

public class WorkItem<T>
{
	private final TimeoutTracker tracker;
	private final CompletableFuture<T> work;

	public WorkItem(final CompletableFuture<T> completableFuture, final Duration timeout)
	{
		this(completableFuture, TimeoutTracker.create(timeout));
	}

	public WorkItem(final CompletableFuture<T> completableFuture, final TimeoutTracker tracker)
	{
		this.work = completableFuture;
		this.tracker = tracker;
	}

	public TimeoutTracker getTimeoutTracker()
	{
		return this.tracker;
	}

	public CompletableFuture<T> getWork()
	{
		return this.work;
	}
}
