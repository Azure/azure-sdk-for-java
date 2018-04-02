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
package com.microsoft.azure.cosmosdb.rx;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import com.microsoft.azure.cosmosdb.Resource;

interface ResourceValidator<T extends Resource> {
    
    void validate(T v);

    class Builder<T extends Resource> {
        private List<ResourceValidator<? extends Resource>> validators = new ArrayList<>();

        public ResourceValidator<T> build() {
            return new ResourceValidator<T>() {

                @SuppressWarnings({ "rawtypes", "unchecked" })
                @Override
                public void validate(T v) {
                    for (ResourceValidator validator : validators) {
                        validator.validate(v);
                    }
                }
            };
        }
        
        public Builder<T> areEqual(T expectedValue) {
            validators.add(new ResourceValidator<T>() {
                @Override
                public void validate(T v) {
                    
                    assertThat(v.getHashMap().keySet())
                    .describedAs("number of fields").
                    hasSize(expectedValue.getHashMap().keySet().size());
                    expectedValue.getHashMap().keySet();
                    for(String key: expectedValue.getHashMap().keySet()) {
                        assertThat(expectedValue.get(key))
                        .describedAs("value for " + key)
                        .isEqualTo(expectedValue.get(key));
                    }
                }
            });
            return this;
        }
        
    }
}