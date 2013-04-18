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
package com.microsoft.windowsazure.services.blob.client;

/**
 * RESERVED FOR INTERNAL USE. Represents properties for writing to a page blob.
 */
final class PageProperties {

    /**
     * Gets or sets the type of write operation.
     */
    private PageOperationType pageOperation = PageOperationType.UPDATE;

    /**
     * Gets or sets the range of bytes to write to.
     */
    private PageRange range = new PageRange(-1, -1);

    /**
     * Initializes a new instance of the PageProperties class.
     */
    public PageProperties() {
        // Empty Default Ctor
    }

    /**
     * @return the pageOperation
     */
    public PageOperationType getPageOperation() {
        return this.pageOperation;
    }

    /**
     * @return the range
     */
    public PageRange getRange() {
        return this.range;
    }

    /**
     * @param pageOperation
     *            the pageOperation to set
     */
    public void setPageOperation(final PageOperationType pageOperation) {
        this.pageOperation = pageOperation;
    }

    /**
     * @param range
     *            the range to set
     */
    public void setRange(final PageRange range) {
        this.range = range;
    }
}
