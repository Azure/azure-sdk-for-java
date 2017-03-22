/**
 * The MIT License (MIT)
 * Copyright (c) 2016 Microsoft Corporation
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

public interface FeedResponsePageListValidator<T extends Resource> {

    void validate(List<FeedResponsePage<T>> feedList);

    class Builder<T extends Resource> {
        private List<FeedResponsePageListValidator<? extends Resource>> validators = new ArrayList<>();

        public FeedResponsePageListValidator<T> build() {
            return new FeedResponsePageListValidator<T>() {

                @SuppressWarnings({ "rawtypes", "unchecked" })
                @Override
                public void validate(List<FeedResponsePage<T>> feedList) {
                    for (FeedResponsePageListValidator validator : validators) {
                        validator.validate(feedList);
                    }
                }
            };
        }
        
        public Builder<T> totalSize(final int expectedCount) {
            validators.add(new FeedResponsePageListValidator<T>() {
                @Override
                public void validate(List<FeedResponsePage<T>> feedList) {
                    int resultCount = feedList.stream().mapToInt(f -> f.getResults().size()).sum();
                    assertThat(resultCount).isEqualTo(expectedCount);
                }
            });
            return this;
        }
        
        public Builder<T> containsExactly(List<String> expectedIds) {
            validators.add(new FeedResponsePageListValidator<T>() {
                @Override
                public void validate(List<FeedResponsePage<T>> feedList) {
                     List<String> actualIds = feedList
                        .stream()
                        .flatMap(f -> f.getResults().stream())
                        .map(r -> r.getResourceId())
                        .collect(Collectors.toList());
                     assertThat(actualIds).containsExactlyElementsOf(expectedIds);
                }
            });
            return this;
        }
        
        public Builder<T> numberOfPages(int expectedNumberOfPages) {
            validators.add(new FeedResponsePageListValidator<T>() {
                @Override
                public void validate(List<FeedResponsePage<T>> feedList) {
                     assertThat(feedList).hasSize(expectedNumberOfPages);
                }
            });
            return this;
        }
        
        public Builder<T> pageSatisfy(int pageNumber, FeedResponsePageValidator<T> pageValidator) {
            validators.add(new FeedResponsePageListValidator<T>() {
                @Override
                public void validate(List<FeedResponsePage<T>> feedList) {
                    assertThat(feedList.size()).isGreaterThan(pageNumber);
                    pageValidator.validate(feedList.get(pageNumber));
                }
            });
            return this;
        }
        
        public Builder<T> allPagesSatisfy(FeedResponsePageValidator<T> pageValidator) {
            validators.add(new FeedResponsePageListValidator<T>() {
                @Override
                public void validate(List<FeedResponsePage<T>> feedList) {
                    
                    for(FeedResponsePage<T> fp: feedList) {
                        pageValidator.validate(fp); 
                    }

                }
            });
            return this;
        }
    }
}
