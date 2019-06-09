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
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.internal.OperationType;
import com.azure.data.cosmos.internal.ResourceType;
import com.azure.data.cosmos.internal.RxDocumentServiceRequest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public interface DocumentServiceRequestValidator<T extends RxDocumentServiceRequest> {

    static <T extends RxDocumentServiceRequest> Builder builder() {
        return new Builder();
    }

    void validate(T v);

    class Builder<T extends RxDocumentServiceRequest> {
        private List<DocumentServiceRequestValidator<? extends RxDocumentServiceRequest>> validators = new ArrayList<>();

        public DocumentServiceRequestValidator<T> build() {
            return new DocumentServiceRequestValidator<T>() {

                @SuppressWarnings({ "rawtypes", "unchecked" })
                @Override
                public void validate(T v) {
                    for (DocumentServiceRequestValidator validator : validators) {
                        validator.validate(v);
                    }
                }
            };
        }

        public Builder<T> add(DocumentServiceRequestValidator validator) {
            validators.add(validator);
            return this;
        }

        public Builder<T> withResourceType(ResourceType resourceType) {
            add(new DocumentServiceRequestValidator<T>() {
                @Override
                public void validate(T v) {
                    assertThat(v.getResourceType()).isEqualTo(resourceType);
                }
            });
            return this;
        }

        public Builder<T> withOperationType(OperationType operationType) {
            add(new DocumentServiceRequestValidator<T>() {
                @Override
                public void validate(T v) {
                    assertThat(v.getOperationType()).isEqualTo(operationType);
                }
            });
            return this;
        }

        public Builder<T> resourceTypeIn(ResourceType... resourceType) {
            add(new DocumentServiceRequestValidator<T>() {
                @Override
                public void validate(T v) {
                    assertThat(v.getResourceType()).isIn((Object[]) resourceType);
                }
            });
            return this;
        }

        public Builder<T> resourceTypeNotIn(ResourceType... resourceType) {
            add(new DocumentServiceRequestValidator<T>() {
                @Override
                public void validate(T v) {
                    assertThat(v.getResourceType()).isNotIn((Object[]) resourceType);
                }
            });
            return this;
        }

        public Builder<T> add(DocumentServiceRequestContextValidator validator) {
            add(new DocumentServiceRequestValidator() {
                @Override
                public void validate(RxDocumentServiceRequest request) {
                    validator.validate(request.requestContext);
                }
            });
            return this;
        }
    }
}