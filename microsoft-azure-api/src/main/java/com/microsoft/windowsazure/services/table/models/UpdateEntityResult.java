/**
 * Copyright 2012 Microsoft Corporation
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
package com.microsoft.windowsazure.services.table.models;

import com.microsoft.windowsazure.services.table.TableContract;

/**
 * Represents the response to a request to modify a single table entity in the storage account returned
 * from a Table Service REST API Update Entity, Merge Entity, Insert or Replace Entity, or Insert or Merge Entity
 * operation. This is returned by calls to implementations of {@link TableContract#mergeEntity(String, Entity)},
 * {@link TableContract#mergeEntity(String, Entity, TableServiceOptions)},
 * {@link TableContract#insertOrMergeEntity(String, Entity)},
 * {@link TableContract#insertOrMergeEntity(String, Entity, TableServiceOptions)},
 * {@link TableContract#insertOrReplaceEntity(String, Entity)},
 * {@link TableContract#insertOrReplaceEntity(String, Entity, TableServiceOptions)},
 * {@link TableContract#updateEntity(String, Entity)} and
 * {@link TableContract#updateEntity(String, Entity, TableServiceOptions)}.
 * <p>
 * See the <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179427.aspx">Update Entity</a> documentation
 * on MSDN for details of the underlying Table Service REST API operations.
 */
public class UpdateEntityResult {
    private String etag;

    /**
     * Gets the ETag value of the modified entity returned in the server response.
     * 
     * @return
     *         A {@link String} containing the ETag value of the modified entity.
     */
    public String getEtag() {
        return etag;
    }

    /**
     * Reserved for internal use. Sets the ETag value of the modified entity from the <code>ETag</code> header value
     * in the server response.
     * <p>
     * This method is invoked by the API to set the value from the Table Service REST API operation response returned by
     * the server.
     * 
     * @param etag
     *            A {@link String} containing the ETag value of the modified entity.
     */
    public void setEtag(String etag) {
        this.etag = etag;
    }
}
