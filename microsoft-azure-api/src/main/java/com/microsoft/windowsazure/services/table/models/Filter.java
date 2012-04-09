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

public class Filter {
    public static UnaryFilter not(Filter operand) {
        return new UnaryFilter("not", operand);
    }

    public static BinaryFilter and(Filter left, Filter right) {
        return new BinaryFilter(left, "and", right);
    }

    public static BinaryFilter or(Filter left, Filter right) {
        return new BinaryFilter(left, "or", right);
    }

    public static BinaryFilter eq(Filter left, Filter right) {
        return new BinaryFilter(left, "eq", right);
    }

    public static BinaryFilter ne(Filter left, Filter right) {
        return new BinaryFilter(left, "ne", right);
    }

    public static BinaryFilter ge(Filter left, Filter right) {
        return new BinaryFilter(left, "ge", right);
    }

    public static BinaryFilter gt(Filter left, Filter right) {
        return new BinaryFilter(left, "gt", right);
    }

    public static BinaryFilter lt(Filter left, Filter right) {
        return new BinaryFilter(left, "lt", right);
    }

    public static BinaryFilter le(Filter left, Filter right) {
        return new BinaryFilter(left, "le", right);
    }

    public static ConstantFilter constant(Object value) {
        return new ConstantFilter(value);
    }

    public static PropertyNameFilter propertyName(String value) {
        return new PropertyNameFilter(value);
    }

    public static QueryStringFilter queryString(String value) {
        return new QueryStringFilter(value);
    }
}
