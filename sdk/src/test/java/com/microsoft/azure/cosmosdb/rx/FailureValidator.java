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

import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.Error;

public interface FailureValidator {

    void validate(Throwable t);

    class Builder {
        private List<FailureValidator> validators = new ArrayList<>();

        public FailureValidator build() {
            return new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    for (FailureValidator validator : validators) {
                        validator.validate(t);
                    }
                }
            };
        }

        public <T extends Throwable> Builder statusCode(int statusCode) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(DocumentClientException.class);
                    assertThat(((DocumentClientException) t).getStatusCode()).isEqualTo(statusCode);
                }
            });
            return this;
        }
        
        public <T extends Throwable> Builder errorMessageContain(int statusCode) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(DocumentClientException.class);
                    assertThat(((DocumentClientException) t).getStatusCode()).isEqualTo(statusCode);
                }
            });
            return this;
        }
        
        public <T extends Throwable> Builder notNullActivityId() {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(DocumentClientException.class);
                    assertThat(((DocumentClientException) t).getActivityId()).isNotNull();
                }
            });
            return this;
        }
        
        public <T extends Throwable> Builder error(Error error) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(DocumentClientException.class);
                    assertThat(((DocumentClientException) t).getError().toJson()).isEqualTo(error.toJson());
                }
            });
            return this;
        }
        
        public <T extends Throwable> Builder subStatusCode(int substatusCode) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(DocumentClientException.class);
                    assertThat(((DocumentClientException) t).getSubStatusCode()).isEqualTo(substatusCode);
                }
            });
            return this;
        }
        
        public <T extends Throwable> Builder instanceOf(Class<T> cls) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(cls);
                }
            });
            return this;
        }
        
        public <T extends Throwable> Builder resourceNotFound() {
            
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(DocumentClientException.class);
                    DocumentClientException ex = (DocumentClientException) t;
                    assertThat(ex.getStatusCode()).isEqualTo(404);
                    
                }
            });
            return this;
        }
        
        public <T extends Throwable> Builder resourceAlreadyExists() {
            
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(DocumentClientException.class);
                    DocumentClientException ex = (DocumentClientException) t;
                    assertThat(ex.getStatusCode()).isEqualTo(409);
                    
                }
            });
            return this;
        }
        
        public <T extends Throwable> Builder causeInstanceOf(Class<T> cls) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t.getCause()).isNotNull();
                    assertThat(t.getCause()).isInstanceOf(cls);
                }
            });
            return this;
        }
    }
}
