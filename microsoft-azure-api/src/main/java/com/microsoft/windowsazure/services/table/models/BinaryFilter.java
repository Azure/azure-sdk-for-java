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

public class BinaryFilter extends Filter {
    private String operator;
    private Filter left;
    private Filter right;

    public String getOperator() {
        return operator;
    }

    public BinaryFilter setOperator(String operator) {
        this.operator = operator;
        return this;
    }

    public Filter getLeft() {
        return left;
    }

    public BinaryFilter setLeft(Filter left) {
        this.left = left;
        return this;
    }

    public Filter getRight() {
        return right;
    }

    public BinaryFilter setRight(Filter right) {
        this.right = right;
        return this;
    }
}
