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
package com.microsoft.azure.storage.file;

import com.microsoft.azure.storage.Constants;

/**
 * Holds the Constants used for the File Service.
 */
final class FileConstants {

    /**
     * XML element for a file.
     */
    public static final String FILE_ELEMENT = "File";

    /**
     * XML element for a directory.
     */
    public static final String DIRECTORY_ELEMENT = "Directory";

    /**
     * XML element for a file range.
     */
    public static final String FILE_RANGE_ELEMENT = "Range";

    /**
     * XML element for a share.
     */
    public static final String SHARE_ELEMENT = "Share";

    /**
     * XML element for shares.
     */
    public static final String SHARES_ELEMENT = "Shares";

    /**
     * XML element for share quota.
     */
    public static final String SHARE_QUOTA_ELEMENT = "Quota";

    /**
     * XML element for file range start elements.
     */
    public static final String START_ELEMENT = "Start";

    /**
     * The number of default concurrent requests for parallel operation.
     */
    public static final int DEFAULT_CONCURRENT_REQUEST_COUNT = 1;

    /**
     * The largest possible share quota in GB.
     */
    public static final int MAX_SHARE_QUOTA = 5120;

    /**
     * The header that specifies file cache control.
     */
    public static final String CACHE_CONTROL_HEADER = Constants.PREFIX_FOR_STORAGE_HEADER + "cache-control";

    /**
     * The header that specifies file content encoding.
     */
    public static final String CONTENT_DISPOSITION_HEADER = Constants.PREFIX_FOR_STORAGE_HEADER + "content-disposition";

    /**
     * The header that specifies file content encoding.
     */
    public static final String CONTENT_ENCODING_HEADER = Constants.PREFIX_FOR_STORAGE_HEADER + "content-encoding";

    /**
     * The header that specifies file content language.
     */
    public static final String CONTENT_LANGUAGE_HEADER = Constants.PREFIX_FOR_STORAGE_HEADER + "content-language";

    /**
     * The header that specifies file content length.
     */
    public static final String CONTENT_LENGTH_HEADER = Constants.PREFIX_FOR_STORAGE_HEADER + "content-length";

    /**
     * The header that specifies file content type.
     */
    public static final String CONTENT_TYPE_HEADER = Constants.PREFIX_FOR_STORAGE_HEADER + "content-type";

    /**
     * The header that specifies file content MD5.
     */
    public static final String FILE_CONTENT_MD5_HEADER = Constants.PREFIX_FOR_STORAGE_HEADER + "content-md5";

    /**
     * The header for the file type.
     */
    public static final String FILE_TYPE_HEADER = Constants.PREFIX_FOR_STORAGE_HEADER + "type";

    /**
     * Specifies the file type.
     */
    public static final String FILE = "File";

    /**
     * The header that specifies range write mode.
     */
    public static final String FILE_RANGE_WRITE = Constants.PREFIX_FOR_STORAGE_HEADER + "write";

    /**
     * The header for the share quota.
     */
    public static final String SHARE_QUOTA_HEADER = Constants.PREFIX_FOR_STORAGE_HEADER + "share-quota";

    /**
     * Private Default Constructor.
     */
    private FileConstants() {
        // No op
    }
}
