/*
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

package com.azure.storage.blob.nio

class AzureFileSystemProviderSpec extends APISpec {
    // TODO: Be sure to test directories
    // TODO: Be sure to test operating on containers that already have data
    // TODO: Test configurations

    // Create a file system success
    // Check container existence (already existing and new)
    // Sas and Shared Key and token
    // Check configurations? How?

    // Create file system fail
    // invalid account
    // no account in uri
    // Already open FileSystem
    // Fail initial connection check (insufficient sas)
    // No containers listed, etc.

    // Illegal arguments for option

    // Assert that an IOException occurs and not a BlobStorageException

    // Close file system
    // Try operating on it
    // Try getting it from FileSystemProvider
    // Try creating a new one with the same way

    // getFileSystem

    // getScheme
}
