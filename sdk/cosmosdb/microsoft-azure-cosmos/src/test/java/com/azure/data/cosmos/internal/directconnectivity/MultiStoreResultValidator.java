// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.internal.FailureValidator;
import com.google.common.base.Predicates;
import org.apache.commons.lang3.mutable.MutableObject;
import org.assertj.core.description.Description;
import org.assertj.core.description.TextDescription;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;

/**
 * this is meant to be used when there reading multiple replicas for the same thing
 */
public interface MultiStoreResultValidator {

    static Builder create() {
        return new Builder();
    }

    void validate(List<StoreResult> storeResults);

    class Builder {
        private List<MultiStoreResultValidator> validators = new ArrayList<>();

        public MultiStoreResultValidator build() {
            return new MultiStoreResultValidator() {

                @SuppressWarnings({"rawtypes", "unchecked"})
                @Override
                public void validate(List<StoreResult> storeResults) {
                    for (MultiStoreResultValidator validator : validators) {
                        validator.validate(storeResults);
                    }
                }
            };
        }

        public Builder validateEachWith(StoreResultValidator storeResultValidator) {
            validators.add(new MultiStoreResultValidator() {

                @Override
                public void validate(List<StoreResult> storeResults) {
                    for(StoreResult srr: storeResults) {
                        storeResultValidator.validate(srr);
                    }
                }
            });
            return this;
        }

        public Builder validateEachWith(StoreResponseValidator storeResponseValidator) {
            validators.add(new MultiStoreResultValidator() {

                @Override
                public void validate(List<StoreResult> storeResults) {
                    for(StoreResult srr: storeResults) {
                        try {
                            storeResponseValidator.validate(srr.toResponse());
                        } catch (CosmosClientException e) {
                            fail(e.getMessage());
                        }
                    }
                }
            });
            return this;
        }

        public Builder withMinimumLSN(long minimumLSN) {
            this.validateEachWith(StoreResultValidator.create().withMinLSN(minimumLSN).build());
            return this;
        }

        public <T> Builder withAggregate(BiFunction<StoreResult, T, T> aggregator,
                                         T initialValue,
                                         Predicate<T> finalValuePredicate,
                                         Description description) {
            MutableObject<T> total = new MutableObject<>(initialValue);
            validators.add(new MultiStoreResultValidator() {

                @Override
                public void validate(List<StoreResult> storeResults) {
                    for(StoreResult srr: storeResults) {
                        total.setValue(aggregator.apply(srr, total.getValue()));
                    }

                    assertThat(finalValuePredicate.test(total.getValue()))
                            .describedAs(Description.mostRelevantDescription(description,
                                                                             String.format("actual value %s.",
                                                                                           total.getValue().toString())))
                            .isTrue();
                }
            });
            return this;
        }

        public Builder withTotalRequestCharge(double totalExpectedRC) {
            this.withAggregate((srr, v) -> srr.requestCharge + v.doubleValue(),
                               0d,
                               Predicates.equalTo(totalExpectedRC),
                               new TextDescription("total request charge is expected to be %f", totalExpectedRC));
            return this;
        }

        public Builder withNonZeroRequestCharge() {

            this.withAggregate((srr, v) -> srr.requestCharge + v.doubleValue(),
                               0d,
                               aDouble -> aDouble > 0,
                               new TextDescription("total request charge expected to be greater than 0"));
            return this;
        }

        public Builder validateEachWith(FailureValidator failureValidator) {
            validators.add(new MultiStoreResultValidator() {

                @Override
                public void validate(List<StoreResult> storeResults) {
                    for(StoreResult srr: storeResults) {
                        try {
                            failureValidator.validate(srr.getException());
                        } catch (CosmosClientException e) {
                            fail(e.getMessage());
                        }
                    }
                }
            });
            return this;
        }

        public Builder noFailure() {
            this.validateEachWith(StoreResultValidator.create().isValid().noException().build());
            return this;
        }

        public Builder withSize(int expectedNumber) {
            validators.add(new MultiStoreResultValidator() {

                @Override
                public void validate(List<StoreResult> storeResults) {
                    assertThat(storeResults).hasSize(expectedNumber);
                }
            });
            return this;
        }
    }
}
