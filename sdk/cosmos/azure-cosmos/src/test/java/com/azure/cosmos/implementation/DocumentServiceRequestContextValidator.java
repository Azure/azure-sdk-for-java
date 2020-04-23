
// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.directconnectivity.StoreResponse;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public interface DocumentServiceRequestContextValidator<T extends DocumentServiceRequestContext> {

    static <T extends DocumentServiceRequestContext> Builder<T> builder() {
        return new Builder<T>();
    }

    void validate(T v);

    class Builder<T extends DocumentServiceRequestContext> {
        private List<DocumentServiceRequestContextValidator<T>> validators = new ArrayList<>();

        public DocumentServiceRequestContextValidator<T> build() {
            return new DocumentServiceRequestContextValidator<T>() {

                @SuppressWarnings({ "rawtypes", "unchecked" })
                @Override
                public void validate(T v) {
                    for (DocumentServiceRequestContextValidator<T> validator : validators) {
                        validator.validate(v);
                    }
                }
            };
        }


        public Builder<T> add(DocumentServiceRequestContextValidator<T> validator) {
            validators.add(validator);
            return this;
        }

        public Builder<T> qurorumSelectedLSN(long quoriumSelectedLSN) {
            add(new DocumentServiceRequestContextValidator<T>() {
                @Override
                public void validate(DocumentServiceRequestContext v) {
                    assertThat(v.quorumSelectedLSN).isEqualTo(quoriumSelectedLSN);
                }
            });
            return this;
        }

        public Builder<T> globalCommittedSelectedLSN(long globalCommittedSelectedLSN) {
            add(new DocumentServiceRequestContextValidator<T>() {
                @Override
                public void validate(DocumentServiceRequestContext v) {
                    assertThat(v.globalCommittedSelectedLSN).isEqualTo(globalCommittedSelectedLSN);
                }
            });
            return this;
        }

        public Builder<T> storeResponses(List<StoreResponse> storeResponses) {
            add(new DocumentServiceRequestContextValidator<T>() {
                @Override
                public void validate(DocumentServiceRequestContext v) {
                    assertThat(v.storeResponses).isEqualTo(storeResponses);
                }
            });
            return this;
        }
    }
}
