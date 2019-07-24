
// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.internal.directconnectivity.StoreResponse;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public interface DocumentServiceRequestContextValidator<T extends DocumentServiceRequestContext> {

    static <T extends DocumentServiceRequestContext> Builder builder() {
        return new Builder();
    }

    void validate(T v);

    class Builder<T extends DocumentServiceRequestContext> {
        private List<DocumentServiceRequestContextValidator<? extends DocumentServiceRequestContext>> validators = new ArrayList<>();

        public DocumentServiceRequestContextValidator<T> build() {
            return new DocumentServiceRequestContextValidator<T>() {

                @SuppressWarnings({ "rawtypes", "unchecked" })
                @Override
                public void validate(T v) {
                    for (DocumentServiceRequestContextValidator validator : validators) {
                        validator.validate(v);
                    }
                }
            };
        }


        public Builder<T> add(DocumentServiceRequestContextValidator validator) {
            validators.add(validator);
            return this;
        }

        public Builder<T> qurorumSelectedLSN(long quoriumSelectedLSN) {
            add(new DocumentServiceRequestContextValidator() {
                @Override
                public void validate(DocumentServiceRequestContext v) {
                    assertThat(v.quorumSelectedLSN).isEqualTo(quoriumSelectedLSN);
                }
            });
            return this;
        }

        public Builder<T> globalCommittedSelectedLSN(long globalCommittedSelectedLSN) {
            add(new DocumentServiceRequestContextValidator() {
                @Override
                public void validate(DocumentServiceRequestContext v) {
                    assertThat(v.globalCommittedSelectedLSN).isEqualTo(globalCommittedSelectedLSN);
                }
            });
            return this;
        }

        public Builder<T> storeResponses(List<StoreResponse> storeResponses) {
            add(new DocumentServiceRequestContextValidator() {
                @Override
                public void validate(DocumentServiceRequestContext v) {
                    assertThat(v.storeResponses).isEqualTo(storeResponses);
                }
            });
            return this;
        }
    }
}