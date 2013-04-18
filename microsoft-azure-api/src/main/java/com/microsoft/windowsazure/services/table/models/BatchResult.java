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

import java.util.ArrayList;
import java.util.List;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.table.TableContract;

/**
 * Represents the response to a request for a batch transaction returned from a Table Service REST API Entity Group
 * Transaction operation. This is returned by calls to implementations of {@link TableContract#batch(BatchOperations)}
 * and {@link TableContract#batch(BatchOperations, TableServiceOptions)}.
 * <p>
 * See the <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd894038.aspx">Performing Entity Group
 * Transactions</a> documentation on MSDN for details of the underlying Table Service REST API operation.
 */
public class BatchResult {
    private List<Entry> entries = new ArrayList<Entry>();

    /**
     * Gets the collection of {@link Entry} results from each MIME change set response corresponding to each request in
     * the batch transaction.
     * 
     * @return
     *         A {@link java.util.List} of {@link Entry} instances corresponding to the responses to each request in
     *         the batch transaction.
     */
    public List<Entry> getEntries() {
        return entries;
    }

    /**
     * Reserved for internal use. Sets the collection of {@link Entry} results from each MIME change set response
     * corresponding to each request in the batch transaction.
     * 
     * @param entries
     *            The {@link java.util.List} of {@link Entry} instances corresponding to the responses to each request
     *            in the batch transaction.
     * @return
     *         A reference to this {@link BatchResult} instance.
     */
    public BatchResult setEntries(List<Entry> entries) {
        this.entries = entries;
        return this;
    }

    /**
     * The abstract base class for entries in the batch transaction response.
     */
    public static abstract class Entry {
    }

    /**
     * Represents the result of an insert entity operation within a batch transaction.
     */
    public static class InsertEntity extends Entry {
        private Entity entity;

        /**
         * Gets the table entity inserted by the operation as returned in the server response.
         * 
         * @return
         *         The {@link Entity} returned in the server response.
         */
        public Entity getEntity() {
            return entity;
        }

        /**
         * Reserved for internal use. Sets the table entity inserted by the operation from the matching
         * <strong>entry</strong> element in the MIME change set response corresponding to the insert request in the
         * batch transaction.
         * 
         * @param entity
         *            The {@link Entity} returned in the server response.
         * @return
         *         A reference to this {@link InsertEntity} instance.
         */
        public InsertEntity setEntity(Entity entity) {
            this.entity = entity;
            return this;
        }
    }

    /**
     * Represents the result of an update entity operation within a batch transaction.
     */
    public static class UpdateEntity extends Entry {
        private String etag;

        /**
         * Gets the updated ETag value for the entity updated by the operation as returned in the server response.
         * 
         * @return
         *         A {@link String} containing the updated ETag value for the entity.
         */
        public String getEtag() {
            return etag;
        }

        /**
         * Reserved for internal use. Sets the ETag for the entity updated by the operation from the matching
         * <code>ETag</code> header within the MIME change set response corresponding to the update request in the
         * batch transaction.
         * 
         * @param etag
         *            A {@link String} containing the updated ETag value for the entity.
         * @return
         *         A reference to this {@link UpdateEntity} instance.
         */
        public UpdateEntity setEtag(String etag) {
            this.etag = etag;
            return this;
        }
    }

    /**
     * Represents the result of a delete entity operation within a batch transaction.
     */
    public static class DeleteEntity extends Entry {

    }

    /**
     * Represents an error result for an insert, update, or delete entity operation within a batch transaction.
     */
    public static class Error extends Entry {
        private ServiceException error;

        /**
         * Gets the {@link ServiceException} instance corresponding to the error returned in the server response.
         * 
         * @return
         *         A {@link ServiceException} instance corresponding to the error returned in the server response.
         */
        public ServiceException getError() {
            return error;
        }

        /**
         * Reserved for internal use. Sets the {@link ServiceException} instance created in response to an error result
         * within the MIME change set response corresponding to the request in the batch transaction.
         * 
         * @param error
         *            A {@link ServiceException} instance corresponding to the error returned in the server response.
         * @return
         *         A reference to this {@link Error} instance.
         */
        public Error setError(ServiceException error) {
            this.error = error;
            return this;
        }
    }
}
