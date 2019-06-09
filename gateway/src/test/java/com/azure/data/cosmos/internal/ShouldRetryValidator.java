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

import com.azure.data.cosmos.rx.FailureValidator;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This is a helper class for validating a partition address for tests.
 */
public interface ShouldRetryValidator {

    void validate(IRetryPolicy.ShouldRetryResult shouldRetryResult);

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        private List<ShouldRetryValidator> validators = new ArrayList<>();

        public ShouldRetryValidator build() {
            return new ShouldRetryValidator() {

                @Override
                public void validate(IRetryPolicy.ShouldRetryResult shouldRetryResult) {
                    for (ShouldRetryValidator validator : validators) {
                        validator.validate(shouldRetryResult);
                    }
                }
            };
        }

        public Builder nullException() {
            validators.add(new ShouldRetryValidator() {

                @Override
                public void validate(IRetryPolicy.ShouldRetryResult shouldRetryResult) {
                    assertThat(shouldRetryResult.exception).isNull();
                }
            });
            return this;
        }

        public Builder hasException() {
            validators.add(new ShouldRetryValidator() {

                @Override
                public void validate(IRetryPolicy.ShouldRetryResult shouldRetryResult) {
                    assertThat(shouldRetryResult.exception).isNotNull();
                }
            });
            return this;
        }

        public Builder exceptionOfType(Class<? extends Exception> klass) {
            validators.add(new ShouldRetryValidator() {

                @Override
                public void validate(IRetryPolicy.ShouldRetryResult shouldRetryResult) {
                    assertThat(shouldRetryResult.exception).isNotNull();
                    assertThat(shouldRetryResult.exception).isInstanceOf(klass);
                }
            });
            return this;
        }

        public Builder withException(FailureValidator failureValidator) {
            validators.add(new ShouldRetryValidator() {

                @Override
                public void validate(IRetryPolicy.ShouldRetryResult shouldRetryResult) {
                    assertThat(shouldRetryResult.exception).isNotNull();
                    failureValidator.validate(shouldRetryResult.exception);
                }
            });
            return this;
        }

        public Builder withException(Exception exception) {
            validators.add(new ShouldRetryValidator() {

                @Override
                public void validate(IRetryPolicy.ShouldRetryResult shouldRetryResult) {
                    assertThat(shouldRetryResult.exception).isNotNull();
                    assertThat(shouldRetryResult.exception).isEqualTo(exception);
                }
            });
            return this;
        }

        public Builder shouldRetry(boolean value) {
            validators.add(new ShouldRetryValidator() {

                @Override
                public void validate(IRetryPolicy.ShouldRetryResult shouldRetryResult) {
                    assertThat(shouldRetryResult.shouldRetry).isEqualTo(value);
                }
            });
            return this;
        }


        public Builder backOfTime(Duration backOfTime) {
            validators.add(new ShouldRetryValidator() {

                @Override
                public void validate(IRetryPolicy.ShouldRetryResult shouldRetryResult) {
                    assertThat(shouldRetryResult.backOffTime).isEqualTo(backOfTime);
                }
            });
            return this;
        }
    }
}
