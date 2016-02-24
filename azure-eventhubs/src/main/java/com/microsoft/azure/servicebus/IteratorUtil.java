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

import java.util.Iterator;

public final class IteratorUtil
{
	private IteratorUtil()
	{
	}

	public static <T> boolean sizeEquals(Iterable<T> iterable, int expectedSize)
	{
		Iterator<T> iterator = iterable.iterator();
		
		int currentSize = 0;
		while(iterator.hasNext())
		{
			if (expectedSize > currentSize)
			{
				currentSize++;
				iterator.next();
				continue;
			}
			else
			{
				return false;
			}
		}
		
		return true;		
	}
	
	public static <T> T getLast(Iterator<T> iterator)
	{
		T last = null;
		while(iterator.hasNext())
		{
			last = iterator.next();
		}
		
		return last;
	}
}
