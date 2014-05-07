/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.azure.storage.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * A class which provides utility methods
 * 
 */
public final class Utility {
	static {
		// Uncomment the following to use Fiddler
		// System.setProperty("http.proxyHost", "localhost");
		// System.setProperty("http.proxyPort", "8888");
	}

	/**
	 * Stores the storage connection strings.
	 * 
	 */
	public static final String storageConnectionString = "DefaultEndpointsProtocol=http;AccountName=myaccountname;AccountKey=myaccountkey";

	/**
	 * Prints out the exception information .
	 */
	public static void printException(Throwable t) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		t.printStackTrace(printWriter);
		System.out.println(String.format("Got an exception from running samples. Exception details:\n%s\n",
				stringWriter.toString()));
	}

	/**
	 * Prints out the sample start information .
	 */
	public static void printSampleStartInfo(String sampleName) {
		System.out.println(String.format("The Azure storage client library sample %s starting...", sampleName));
	}

	/**
	 * Prints out the sample complete information .
	 */
	public static void printSampleCompleteInfo(String sampleName) {
		System.out.println(String.format("The Azure storage client library sample %s completed.", sampleName));
	}
}
