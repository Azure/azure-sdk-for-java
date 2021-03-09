// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query.orderbyquery;

import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.query.ItemComparator;
import com.azure.cosmos.implementation.query.ItemType;
import com.azure.cosmos.implementation.query.ItemTypeHelper;
import com.azure.cosmos.implementation.query.QueryItem;
import com.azure.cosmos.implementation.query.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import static java.lang.Integer.signum;

public final class OrderbyRowComparer<T> implements Comparator<OrderByRowResult<T>>, Serializable {
    private static final Logger logger = LoggerFactory.getLogger(OrderbyRowComparer.class);
    private static final long serialVersionUID = 7296627879628897315L;

    private final List<SortOrder> sortOrders;
    private volatile List<ItemType> itemTypes;

    public OrderbyRowComparer(Collection<SortOrder> sortOrders) {
        this.sortOrders = new ArrayList<>(sortOrders);
    }

    @Override
    public int compare(OrderByRowResult<T> r1, OrderByRowResult<T> r2) {
        try {
            // comparing document (row) vs document (row)
            List<QueryItem> result1 = r1.getOrderByItems();
            List<QueryItem> result2 = r2.getOrderByItems();

            if (result1.size() != result2.size()) {
                throw new IllegalStateException("OrderByItems cannot have different sizes.");
            }

            if (result1.size() != this.sortOrders.size()) {
                throw new IllegalStateException("OrderByItems cannot have a different size than sort orders.");
            }

            if (this.itemTypes == null) {
                synchronized (this) {
                    if (this.itemTypes == null) {
                        this.itemTypes = new ArrayList<ItemType>(result1.size());
                        for (QueryItem item : result1) {
                            this.itemTypes.add(ItemTypeHelper.getOrderByItemType(item.getItem()));
                        }
                    }
                }
            }

            for (int i = 0; i < result1.size(); ++i) {
                int cmp = ItemComparator.getInstance().compare(result1.get(i).getItem(), result2.get(i).getItem());
                if (cmp != 0) {
                    switch (this.sortOrders.get(i)) {
                    case Ascending:
                        return signum(cmp);
                    case Descending:
                        return -signum(cmp);
                    }
                }
            }

            return r1.getSourceRange().getRange().getMin()
                       .compareTo(r2.getSourceRange().getRange().getMin());
        } catch (Exception e) {
            // Due to a bug in rxjava-extras <= 0.8.0.15 dependency,
            // if OrderbyRowComparer throws an unexpected exception,
            // then the observable returned by Transformers.orderedMergeWith(.) will never emit a terminal event.
            // rxjava-extras lib provided a quick fix on the bugreport:
            // https://github.com/davidmoten/rxjava-extras/issues/30 (0.8.0.16)
            // we are also capturing the exception stacktrace here
            logger.error("Orderby Row comparision failed {}, {}", r1.toJson(), r2.toJson(), e);
            throw e;
        }
    }

    public List<SortOrder> getSortOrders() {
        return this.sortOrders;
    }
}
