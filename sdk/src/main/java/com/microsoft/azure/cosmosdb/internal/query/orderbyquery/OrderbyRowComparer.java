/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb.internal.query.orderbyquery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import com.microsoft.azure.cosmosdb.internal.query.ItemComparator;
import com.microsoft.azure.cosmosdb.internal.query.QueryItem;
import com.microsoft.azure.cosmosdb.internal.query.SortOrder;

public final class OrderbyRowComparer<T> implements Comparator<OrderByRowResult<T>> {
    private final List<SortOrder> sortOrders;

    public OrderbyRowComparer(Collection<SortOrder> sortOrders) {
        this.sortOrders = new ArrayList<SortOrder>(sortOrders);
    }

    @Override
    public int compare(OrderByRowResult<T> r1, OrderByRowResult<T> r2) {
        // comparing document (row) vs document (row)
        List<QueryItem> result1 = r1.getOrderByItems();
        List<QueryItem> result2 = r2.getOrderByItems();

        for (int i = 0; i < result1.size(); ++i) {
            int cmp = ItemComparator.getInstance().compare(result1.get(i).getItem(), result2.get(i).getItem());
            if (cmp != 0) {
                switch (this.sortOrders.get(i)) {
                case Ascending:
                    return cmp;
                case Descending:
                    return -cmp;
                }
            }
        }

        return r1.getSourcePartitionKeyRange().getMinInclusive().compareTo(r2.getSourcePartitionKeyRange().getMinInclusive());
    }
}
