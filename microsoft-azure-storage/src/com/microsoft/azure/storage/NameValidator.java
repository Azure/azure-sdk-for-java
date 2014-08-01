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
package com.microsoft.azure.storage;

import java.util.regex.Pattern;

import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.Utility;

/**
 * Provides helpers to validate resource names across the Microsoft Azure
 * Storage Services.
 */
public class NameValidator {
	private static final int BLOB_FILE_DIRECTORY_MIN_LENGTH = 1;
	private static final int CONTAINER_SHARE_QUEUE_TABLE_MIN_LENGTH = 3;
	private static final int CONTAINER_SHARE_QUEUE_TABLE_MAX_LENGTH = 63;
	private static final int FILE_DIRECTORY_MAX_LENGTH = 255;
	private static final int BLOB_MAX_LENGTH = 1024;
	private static final Pattern FILE_DIRECTORY_REGEX = Pattern.compile("^[^\"\\/:|<>*?]*/{0,1}");
	private static final Pattern SHARE_CONTAINER_QUEUE_REGEX = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");
	private static final Pattern TABLE_REGEX = Pattern.compile("^[A-Za-z][A-Za-z0-9]*$");
	private static final Pattern METRICS_TABLE_REGEX = Pattern.compile("^\\$Metrics(HourPrimary|MinutePrimary|HourSecondary|MinuteSecondary)?(Transactions)(Blob|Queue|Table)$");
	private static final String[] RESERVED_FILE_NAMES = { ".", "..", "LPT1",
			"LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9",
			"COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8",
			"COM9", "PRN", "AUX", "NUL", "CON", "CLOCK$" };

	/**
	 * Checks if a container name is valid.
	 * 
	 * @param containerName
	 *            A String representing the container name to validate.
	 */
	public static void validateContainerName(String containerName) {
		if (!("$root".equals(containerName) || "$logs".equals(containerName))) {
			NameValidator.validateShareContainerQueueHelper(containerName, SR.CONTAINER);
		}
	}

	/**
	 * Checks if a queue name is valid.
	 * 
	 * @param queueName
	 *            A String representing the queue name to validate.
	 */
	public static void validateQueueName(String queueName) {
		NameValidator.validateShareContainerQueueHelper(queueName, SR.QUEUE);
	}

	/**
	 * Checks if a share name is valid.
	 * 
	 * @param shareName
	 *            A String representing the share name to validate.
	 */
	public static void validateShareName(String shareName) {
		NameValidator.validateShareContainerQueueHelper(shareName, SR.SHARE);
	}

	private static void validateShareContainerQueueHelper(String resourceName,
			String resourceType) {
		if (Utility.isNullOrEmptyOrWhitespace(resourceName)) {
			throw new IllegalArgumentException(String.format(Utility.LOCALE_US, SR.RESOURCE_NAME_EMPTY, resourceType));
		}

		if (resourceName.length() < NameValidator.CONTAINER_SHARE_QUEUE_TABLE_MIN_LENGTH || resourceName.length() > NameValidator.CONTAINER_SHARE_QUEUE_TABLE_MAX_LENGTH) {
			throw new IllegalArgumentException(String.format(Utility.LOCALE_US, SR.INVALID_RESOURCE_NAME_LENGTH, resourceType, NameValidator.CONTAINER_SHARE_QUEUE_TABLE_MIN_LENGTH, NameValidator.CONTAINER_SHARE_QUEUE_TABLE_MAX_LENGTH));
		}

		if (!NameValidator.SHARE_CONTAINER_QUEUE_REGEX.matcher(resourceName).matches())
		{
			throw new IllegalArgumentException(String.format(Utility.LOCALE_US, SR.INVALID_RESOURCE_NAME, resourceType));
		}
	}

	/**
	 * Checks if a blob name is valid.
	 * 
	 * @param blobName
	 *            A String representing the blob name to validate.
	 */
	public static void validateBlobName(String blobName) {
		if (Utility.isNullOrEmptyOrWhitespace(blobName)) {
			throw new IllegalArgumentException(String.format(Utility.LOCALE_US, SR.RESOURCE_NAME_EMPTY, SR.BLOB));
		}

		if (blobName.length() < NameValidator.BLOB_FILE_DIRECTORY_MIN_LENGTH || blobName.length() > NameValidator.BLOB_MAX_LENGTH) {
			throw new IllegalArgumentException(String.format(Utility.LOCALE_US, SR.INVALID_RESOURCE_NAME_LENGTH, SR.BLOB, NameValidator.BLOB_FILE_DIRECTORY_MIN_LENGTH, NameValidator.BLOB_MAX_LENGTH));
		}
		
		int slashCount =  0;
		for (int i = 0; i < blobName.length(); i++)
		{
			if (blobName.charAt(i) == '/')
			{
				slashCount++;
			}
		}
		
		if (slashCount >= 254)
		{
			throw new IllegalArgumentException(SR.TOO_MANY_PATH_SEGMENTS);
		}
	}

	/**
	 * Checks if a file name is valid.
	 * 
	 * @param fileName
	 *            A String representing the file name to validate.
	 */
	public static void validateFileName(String fileName) {
		NameValidator.ValidateFileDirectoryHelper(fileName, SR.FILE);

		if (fileName.endsWith("/")) {
			throw new IllegalArgumentException(String.format(Utility.LOCALE_US, SR.INVALID_RESOURCE_NAME, SR.FILE));
		}

		for (String s : NameValidator.RESERVED_FILE_NAMES) {
			if (s.equalsIgnoreCase(fileName)) {
				throw new IllegalArgumentException(String.format(Utility.LOCALE_US, SR.INVALID_RESOURCE_RESERVED_NAME, SR.FILE));
			}
		}
	}

	/**
	 * Checks if a directory name is valid.
	 * 
	 * @param directoryName
	 *            A String representing the directory name to validate.
	 */
	public static void validateDirectoryName(String directoryName) {
		NameValidator.ValidateFileDirectoryHelper(directoryName, SR.DIRECTORY);
	}

	private static void ValidateFileDirectoryHelper(String resourceName, String resourceType) {
		if (Utility.isNullOrEmptyOrWhitespace(resourceName)) {
			throw new IllegalArgumentException(String.format(Utility.LOCALE_US, SR.RESOURCE_NAME_EMPTY, resourceType));
		}

		if (resourceName.length() < NameValidator.BLOB_FILE_DIRECTORY_MIN_LENGTH || resourceName.length() > NameValidator.FILE_DIRECTORY_MAX_LENGTH) {
			throw new IllegalArgumentException(String.format(Utility.LOCALE_US, SR.INVALID_RESOURCE_NAME_LENGTH, resourceType, NameValidator.BLOB_FILE_DIRECTORY_MIN_LENGTH, NameValidator.FILE_DIRECTORY_MAX_LENGTH ));
		}
		
		if (!NameValidator.FILE_DIRECTORY_REGEX.matcher(resourceName).matches()) {
			throw new IllegalArgumentException(String.format(Utility.LOCALE_US, SR.INVALID_RESOURCE_NAME, resourceType));
		}
	}

	/**
	 * Checks if a table name is valid.
	 * 
	 * @param tableName
	 *            A String representing the table name to validate.
	 */
	public static void validateTableName(String tableName) {
		if (Utility.isNullOrEmptyOrWhitespace(tableName)) {
			throw new IllegalArgumentException(String.format(Utility.LOCALE_US, SR.RESOURCE_NAME_EMPTY, SR.TABLE));
		}
		
		if (tableName.length() < NameValidator.CONTAINER_SHARE_QUEUE_TABLE_MIN_LENGTH || tableName.length() > NameValidator.CONTAINER_SHARE_QUEUE_TABLE_MAX_LENGTH) {
			throw new IllegalArgumentException(String.format(Utility.LOCALE_US, SR.INVALID_RESOURCE_NAME_LENGTH, SR.TABLE, NameValidator.CONTAINER_SHARE_QUEUE_TABLE_MIN_LENGTH, NameValidator.CONTAINER_SHARE_QUEUE_TABLE_MAX_LENGTH));
		}

		if (!(NameValidator.TABLE_REGEX.matcher(tableName).matches()
				|| NameValidator.METRICS_TABLE_REGEX.matcher(tableName).matches() 
				|| tableName.equalsIgnoreCase("$MetricsCapacityBlob"))) {
			throw new IllegalArgumentException(String.format(Utility.LOCALE_US, SR.INVALID_RESOURCE_NAME, SR.TABLE));
		}
	}
}
