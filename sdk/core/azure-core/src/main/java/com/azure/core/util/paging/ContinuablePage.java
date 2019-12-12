package com.azure.core.util.paging;

import java.util.List;

/**
 * Represents Page from service that has reference (a.k.a continuation token) to next set
 * of one or more pages.
 *
 * @param <C> Type of the continuation token
 * @param <T> Type of the items in the page
 *
 * @see ContinuablePagedFlux
 */
public interface ContinuablePage<C, T> {
    /**
     * @return list of items in the page.
     */
    List<T> getItems();
    /**
     * @return A reference to the next page, or {@code null} if there are no more pages.
     */
    C getContinuationToken();
}
