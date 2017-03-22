/**
 * The MIT License (MIT)
 * Copyright (c) 2017 Microsoft Corporation
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
package com.microsoft.azure.documentdb.rx;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.microsoft.azure.documentdb.FeedResponsePage;
import com.microsoft.azure.documentdb.Resource;

interface FeedResponsePageValidator<T extends Resource> {
    
    void validate(FeedResponsePage<T> feedList);

    class Builder<T extends Resource> {
        private List<FeedResponsePageValidator<? extends Resource>> validators = new ArrayList<>();

        public FeedResponsePageValidator<T> build() {
            return new FeedResponsePageValidator<T>() {

                @SuppressWarnings({ "rawtypes", "unchecked" })
                @Override
                public void validate(FeedResponsePage<T> feedPage) {
                    for (FeedResponsePageValidator validator : validators) {
                        validator.validate(feedPage);
                    }
                }
            };
        }
        
        public Builder<T> pageSizeOf(final int expectedCount) {

            validators.add(new FeedResponsePageValidator<T>() {
                @Override
                public void validate(FeedResponsePage<T> feedPage) {
                    assertThat(feedPage.getResults()).hasSize(expectedCount);
                }
            });
            return this;
        }
        
        public Builder<T> positiveRequestCharge() {

            validators.add(new FeedResponsePageValidator<T>() {
                @Override
                public void validate(FeedResponsePage<T> feedPage) {
                    assertThat(feedPage.getRequestCharge()).isPositive();
                }
            });
            return this;
        }

        public Builder<T> requestChargeGreaterThanOrEqualTo(double minRequestCharge) {

            validators.add(new FeedResponsePageValidator<T>() {
                @Override
                public void validate(FeedResponsePage<T> feedPage) {
                    assertThat(feedPage.getRequestCharge()).isGreaterThanOrEqualTo(minRequestCharge);
                }
            });
            return this;
        }
        
        public Builder<T> requestChargeLessThanOrEqualTo(double maxRequestCharge) {

            validators.add(new FeedResponsePageValidator<T>() {
                @Override
                public void validate(FeedResponsePage<T> feedPage) {
                    assertThat(feedPage.getRequestCharge()).isLessThanOrEqualTo(maxRequestCharge);
                }
            });
            return this;
        }
        
        public Builder<T> idsExactlyAre(final List<String> expectedIds) {
            validators.add(new FeedResponsePageValidator<T>() {
                @Override
                public void validate(FeedResponsePage<T> feedPage) {
                    assertThat(feedPage
                            .getResults().stream()
                            .map(r -> r.getResourceId())
                            .collect(Collectors.toList()))
                            .containsExactlyElementsOf(expectedIds);
                }
            });
            return this;
        }
    }
}