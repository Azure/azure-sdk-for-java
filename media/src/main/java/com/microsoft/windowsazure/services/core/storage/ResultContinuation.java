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
package com.microsoft.windowsazure.services.core.storage;

/**
 * Represents a continuation token for listing operations. Continuation tokens
 * are used in methods that return a {@link ResultSegment} object, such as
 * {@link CloudBlobDirectory#listBlobsSegmented}.
 */
public final class ResultContinuation
{
    /**
     * Represents the next marker for continuing results of listing operations.
     */
    private String nextMarker;

    /**
     * Gets or sets the NextPartitionKey for TableServiceEntity enumeration
     * operations.
     */
    private String nextPartitionKey;

    /**
     * Gets or sets the NextRowKey for TableServiceEntity enumeration
     * operations.
     */
    private String nextRowKey;

    /**
     * Gets or sets the NextTableName for Table enumeration operations.
     */
    private String nextTableName;

    /**
     * Gets or sets the type of the continuation token.
     */
    private ResultContinuationType continuationType;

    /**
     * Creates an instance of the <code>ResultContinuation</code> class.
     */
    public ResultContinuation()
    {
        // Empty Default Ctor
    }

    /**
     * @return the continuationType
     */
    public ResultContinuationType getContinuationType()
    {
        return this.continuationType;
    }

    /**
     * @return the nextMarker
     */
    public String getNextMarker()
    {
        return this.nextMarker;
    }

    /**
     * @return the nextPartitionKey
     */
    public String getNextPartitionKey()
    {
        return this.nextPartitionKey;
    }

    /**
     * @return the nextRowKey
     */
    public String getNextRowKey()
    {
        return this.nextRowKey;
    }

    /**
     * @return the nextTableName
     */
    public String getNextTableName()
    {
        return this.nextTableName;
    }

    /**
     * Returns a value that indicates whether continuation information is
     * available.
     * 
     * @return <code>true</code> if any continuation information is available;
     *         otherwise <code>false</code>.
     */
    public boolean hasContinuation()
    {
        return this.getNextMarker() != null || this.nextPartitionKey != null
                || this.nextRowKey != null || this.nextTableName != null;
    }

    /**
     * @param continuationType
     *            the continuationType to set
     */
    public void setContinuationType(
            final ResultContinuationType continuationType)
    {
        this.continuationType = continuationType;
    }

    /**
     * @param nextMarker
     *            the nextMarker to set
     */
    public void setNextMarker(final String nextMarker)
    {
        this.nextMarker = nextMarker;
    }

    /**
     * @param nextPartitionKey
     *            the nextPartitionKey to set
     */
    public void setNextPartitionKey(final String nextPartitionKey)
    {
        this.nextPartitionKey = nextPartitionKey;
    }

    /**
     * @param nextRowKey
     *            the nextRowKey to set
     */
    public void setNextRowKey(final String nextRowKey)
    {
        this.nextRowKey = nextRowKey;
    }

    /**
     * @param nextTableName
     *            the nextTableName to set
     */
    public void setNextTableName(final String nextTableName)
    {
        this.nextTableName = nextTableName;
    }
}
