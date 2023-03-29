// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This is a helper class for validating a partition address for tests.
 */
public interface ShouldRetryValidator {

    void validate(ShouldRetryResult shouldRetryResult);

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        private List<ShouldRetryValidator> validators = new ArrayList<>();

        public ShouldRetryValidator build() {
            return new ShouldRetryValidator() {

                @Override
                public void validate(ShouldRetryResult shouldRetryResult) {
                    for (ShouldRetryValidator validator : validators) {
                        validator.validate(shouldRetryResult);
                    }
                }
            };
        }

        public Builder nullException() {
            validators.add(new ShouldRetryValidator() {

                @Override
                public void validate(ShouldRetryResult shouldRetryResult) {
                    assertThat(shouldRetryResult.exception).isNull();
                }
            });
            return this;
        }

        public Builder hasException() {
            validators.add(new ShouldRetryValidator() {

                @Override
                public void validate(ShouldRetryResult shouldRetryResult) {
                    assertThat(shouldRetryResult.exception).isNotNull();
                }
            });
            return this;
        }

        public Builder exceptionOfType(Class<? extends Exception> klass) {
            validators.add(new ShouldRetryValidator() {

                @Override
                public void validate(ShouldRetryResult shouldRetryResult) {
                    assertThat(shouldRetryResult.exception).isNotNull();
                    assertThat(shouldRetryResult.exception).isInstanceOf(klass);
                }
            });
            return this;
        }

        public Builder withException(FailureValidator failureValidator) {
            validators.add(new ShouldRetryValidator() {

                @Override
                public void validate(ShouldRetryResult shouldRetryResult) {
                    assertThat(shouldRetryResult.exception).isNotNull();
                    failureValidator.validate(shouldRetryResult.exception);
                }
            });
            return this;
        }

        public Builder withException(Exception exception) {
            validators.add(new ShouldRetryValidator() {

                @Override
                public void validate(ShouldRetryResult shouldRetryResult) {
                    assertThat(shouldRetryResult.exception).isNotNull();
                    assertThat(shouldRetryResult.exception).isEqualTo(exception);
                }
            });
            return this;
        }

        public Builder shouldRetry(boolean value) {
            validators.add(new ShouldRetryValidator() {

                @Override
                public void validate(ShouldRetryResult shouldRetryResult) {
                    assertThat(shouldRetryResult.shouldRetry).isEqualTo(value);
                }
            });
            return this;
        }


        public Builder backOfTime(Duration backOfTime) {
            validators.add(new ShouldRetryValidator() {

                @Override
                public void validate(ShouldRetryResult shouldRetryResult) {
                    assertThat(shouldRetryResult.backOffTime).isEqualTo(backOfTime);
                }
            });
            return this;
        }
    }
}
