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

package com.microsoft.azure.cosmosdb;

/**
 * Specifies the operations on which a trigger should be executed in the Azure Cosmos DB database service.
 */
public enum TriggerOperation {
    /**
     * All operations.
     */
    All(0x0),

    /**
     * Create operations only.
     */
    Create(0x1),

    /**
     * Update operations only.
     */
    Update(0x2),

    /**
     * Delete operations only.
     */
    Delete(0x3),

    /**
     * Replace operations only.
     */
    Replace(0x4);

    private int value;

    TriggerOperation(int value) {
        this.value = value;
    }

    /**
     * Gets the numerical value of the trigger operation.
     *
     * @return the numerical value.
     */
    public int getValue() {
        return value;
    }
}
