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
