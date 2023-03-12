// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.graalvm.samples.spring.storage;

import java.io.InputStream;
import java.util.stream.Stream;

/**
 * Interface used to represent a location that allows for files to be stored, retrieved, and deleted.
 */
public interface StorageService {

	/**
	 * This method is called once when the web app starts to allow for initialisation of resources.
	 */
	void init();

	/**
	 * This method is called to store a file in the storage service.
	 * @param filename The filename of the file being uploaded.
	 * @param inputStream A stream containing the contents of the file.
	 * @param length The content length of the file.
	 */
	void store(final String filename, final InputStream inputStream, final long length);

	/**
	 * This method returns a stream of all files stored in the storage service.
	 * @return
	 */
	Stream<StorageItem> listAllFiles();

	/**
	 * This method will return a {@link StorageItem} of the file, from which various metadata and content may be
	 * retrieved.
	 * @param filename The filename of the file, which must match what was used to originally store it.
	 */
	StorageItem getFile(String filename);

	/**
	 * This method will delete the specified file from the storage service, returning true if successful or false if not.
	 * @param filename The filename of the file, which must match what was used to originally store it.
	 * @return True if deletion was successful, or false if it failed.
	 */
	boolean deleteFile(String filename);
}