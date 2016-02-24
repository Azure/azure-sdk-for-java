/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package com.microsoft.azure.servicebus;

import java.time.*;

public class TimeoutTracker
{
	private final Duration originalTimeout;
	private boolean isTimerStarted;
	private Instant startTime;
	
	/**
	 * @param timeout original operationTimeout
	 * @param startTrackingTimeout whether/not to start the timeout tracking - right now. if not started now, timer tracking will start upon the first call to {@link TimeoutTracker#elapsed()}/{@link TimeoutTracker#remaining()} 
	 */
	public TimeoutTracker(Duration timeout, boolean startTrackingTimeout)
	{
		if (timeout.compareTo(Duration.ZERO) < 0)
		{
			throw new IllegalArgumentException("timeout should be non-negative");
		}
		
		this.originalTimeout = timeout;
		
		if (startTrackingTimeout)
		{
			this.startTime = Instant.now();
		}
		
		this.isTimerStarted = startTrackingTimeout;
	}
	
	public static TimeoutTracker create(Duration timeout)
	{
		return new TimeoutTracker(timeout, true);
	}

	public Duration remaining()
	{
		return this.originalTimeout.minus(this.elapsed());
	}
	
	public Duration elapsed()
	{
		if (!this.isTimerStarted)
		{
			this.startTime = Instant.now();
			this.isTimerStarted = true;
		}
		
		return Duration.between(this.startTime, Instant.now());
	}
}
