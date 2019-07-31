// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal;

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