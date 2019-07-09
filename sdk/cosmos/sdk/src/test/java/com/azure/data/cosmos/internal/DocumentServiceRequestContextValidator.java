
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