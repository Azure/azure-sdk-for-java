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
package com.microsoft.windowsazure.services.table.models;

import com.microsoft.windowsazure.services.table.TableContract;

/**
 * Represents the options that may be set on a
 * {@link TableContract#deleteEntity(String, String, String, DeleteEntityOptions)} request. An optional ETag value may
 * be set to require that the deleted entity have the same ETag value to be deleted. Set a <code>null</code> ETag value
 * to delete the entity unconditionally.
 */
public class DeleteEntityOptions extends TableServiceOptions {
    private String etag;

    /**
     * Gets the ETag value to match in order to delete the entity set in this {@link DeleteEntityOptions} instance.
     * 
     * @return
     *         A {@link String} containing the ETag value the entity must match to be deleted.
     */
    public String getEtag() {
        return etag;
    }

    /**
     * Sets the ETag value to match in order to delete the entity. Set the <em>etag</em> parameter to <code>null</code>
     * to delete the entity unconditionally.
     * 
     * @param etag
     *            A {@link String} containing the ETag value the entity must match to be deleted, or <code>null</code>.
     * @return
     *         A reference to this {@link DeleteEntityOptions} instance.
     */
    public DeleteEntityOptions setEtag(String etag) {
        this.etag = etag;
        return this;
    }

}
