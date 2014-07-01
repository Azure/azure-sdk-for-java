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

import com.microsoft.azure.storage.blob.CloudBlobDirectory;
import com.microsoft.azure.storage.table.TableServiceEntity;

/**
 * Represents a continuation token for listing operations. Continuation tokens are used in methods that return a
 * {@link ResultSegment} object, such as {@link CloudBlobDirectory#listBlobsSegmented}.
 */
public final class ResultContinuation {
    /**
     * Represents the next marker for continuing results of listing operations.
     */
    private String nextMarker;

    /**
     * Represents the next partition key for TableServiceEntity enumeration operations.
     */
    private String nextPartitionKey;

    /**
     * Represents the next row key for TableServiceEntity enumeration operations.
     */
    private String nextRowKey;

    /**
     * Represents the next table name for Table enumeration operations.
     */
    private String nextTableName;

    /**
     * Represents the type of the continuation token.
     */
    private ResultContinuationType continuationType;

    /**
     * Represents the location that the token applies to.
     */
    private StorageLocation targetLocation;

    /**
     * Creates an instance of the <code>ResultContinuation</code> class.
     */
    public ResultContinuation() {
        // Empty default constructor.
    }

    /**
     * Gets the type of the continuation token.
     * 
     * @return The {@link ResultContinuationType} value.
     */
    public ResultContinuationType getContinuationType() {
        return this.continuationType;
    }

    /**
     * Gets the next marker for continuing results of listing operations.
     * 
     * @return A <code>String</code> which represents the the next marker.
     */
    public String getNextMarker() {
        return this.nextMarker;
    }

    /**
     * Gets the next partition key for {@link TableServiceEntity} enumeration operations.
     * 
     * @return A <code>String</code> which represents the the next partition key.
     */
    public String getNextPartitionKey() {
        return this.nextPartitionKey;
    }

    /**
     * Gets the next row key for {@link TableServiceEntity} enumeration operations.
     * 
     * @return A <code>String</code> which represents the the next row key.
     */
    public String getNextRowKey() {
        return this.nextRowKey;
    }

    /**
     * Gets the next table name for Table enumeration operations.
     * 
     * @return A <code>String</code> which represents the the next table name.
     */
    public String getNextTableName() {
        return this.nextTableName;
    }

    /**
     * Gets the location that the token applies to.
     * 
     * @return A {@link StorageLocation} value which indicates the location.
     */
    public StorageLocation getTargetLocation() {
        return this.targetLocation;
    }

    /**
     * Indicates whether continuation information is available.
     * 
     * @return <code>true</code> if any continuation information is available; otherwise <code>false</code>.
     */
    public boolean hasContinuation() {
        return this.getNextMarker() != null || this.nextPartitionKey != null || this.nextRowKey != null
                || this.nextTableName != null;
    }

    /**
     * Sets the type of the continuation token.
     * 
     * @param continuationType
     *            The {@link ResultContinuationType} value to set.
     */
    public void setContinuationType(final ResultContinuationType continuationType) {
        this.continuationType = continuationType;
    }

    /**
     * Sets the next marker for continuing results of listing operations.
     * 
     * @param nextMarker
     *            A <code>String</code> which represents the the next marker to set.
     */
    public void setNextMarker(final String nextMarker) {
        this.nextMarker = nextMarker;
    }

    /**
     * Sets the next partition key for {@link TableServiceEntity} enumeration operations.
     * 
     * @param nextPartitionKey
     *            A <code>String</code> which represents the the next partition key to set.
     */
    public void setNextPartitionKey(final String nextPartitionKey) {
        this.nextPartitionKey = nextPartitionKey;
    }

    /**
     * Sets the next row key for {@link TableServiceEntity} enumeration operations.
     * 
     * @param nextRowKey
     *            A <code>String</code> which represents the the next row key to set.
     */
    public void setNextRowKey(final String nextRowKey) {
        this.nextRowKey = nextRowKey;
    }

    /**
     * Sets the next table name for Table enumeration operations.
     * 
     * @param nextTableName
     *            A <code>String</code> which represents the the next table name to set.
     */
    public void setNextTableName(final String nextTableName) {
        this.nextTableName = nextTableName;
    }

    /**
     * Sets the location that the token applies to.
     * 
     * @param targetLocation
     *            A {@link StorageLocation} value which indicates the location to set.
     */
    public void setTargetLocation(StorageLocation targetLocation) {
        this.targetLocation = targetLocation;
    }
}
