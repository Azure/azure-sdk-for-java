/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

class TestUtilities
{
	static String getStorageConnectionString()
	{
		String retval = System.getenv("EPHTESTSTORAGE");
		return ((retval != null) ? retval : "");
	}
}
