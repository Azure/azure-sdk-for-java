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

package com.microsoft.azure.cosmosdb.internal.query;

import java.util.Comparator;

public final class ItemComparator implements Comparator<Object> {
    private ItemComparator() {
    }

    private static class SingletonHelper {
        private static final ItemComparator INSTANCE = new ItemComparator();
    }

    public static ItemComparator getInstance() {
        return SingletonHelper.INSTANCE;
    }

    @Override
    public int compare(Object obj1, Object obj2) {
        ItemType type1 = ItemTypeHelper.getOrderByItemType(obj1);
        ItemType type2 = ItemTypeHelper.getOrderByItemType(obj2);

        int cmp = Integer.compare(type1.getVal(), type2.getVal());

        if (cmp != 0) {
            return cmp;
        }

        switch (type1) {
        case NoValue:
        case Null:
            return 0;
        case Boolean:
            return Boolean.compare((Boolean) obj1, (Boolean) obj2);
        case Number:
            return Double.compare(((Number) obj1).doubleValue(), ((Number) obj2).doubleValue());
        case String:
            return ((String) obj1).compareTo((String) obj2);
        default:
            throw new ClassCastException(String.format("Unexpected type: %s", type1.toString()));
        }
    }
}
