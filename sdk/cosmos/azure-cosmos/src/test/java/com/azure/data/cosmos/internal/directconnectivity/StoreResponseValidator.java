// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.Resource;
import com.azure.data.cosmos.internal.HttpConstants;
import org.assertj.core.api.Condition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public interface StoreResponseValidator {
    
    void validate(StoreResponse storeResponse);

    public static Builder create() {
        return new Builder();
    }

    public class Builder<T extends Resource> {
        private List<StoreResponseValidator> validators = new ArrayList<>();

        public StoreResponseValidator build() {
            return new StoreResponseValidator() {

                @SuppressWarnings({"rawtypes", "unchecked"})
                @Override
                public void validate(StoreResponse resp) {
                    for (StoreResponseValidator validator : validators) {
                        validator.validate(resp);
                    }
                }
            };
        }
        public Builder hasHeader(String headerKey) {

            validators.add(new StoreResponseValidator() {
                @Override
                public void validate(StoreResponse resp) {
                    assertThat(Arrays.asList(resp.getResponseHeaderNames())).asList().contains(headerKey);
                }
            });
            return this;
        }
        public Builder withHeader(String headerKey, String headerValue) {

            validators.add(new StoreResponseValidator() {
                @Override
                public void validate(StoreResponse resp) {
                    assertThat(Arrays.asList(resp.getResponseHeaderNames())).asList().contains(headerKey);
                    int index = Arrays.asList(resp.getResponseHeaderNames()).indexOf(headerKey);
                    assertThat(resp.getResponseHeaderValues()[index]).isEqualTo(headerValue);
                }
            });
            return this;
        }

        public Builder withHeaderValueCondition(String headerKey, Condition<String> condition) {

            validators.add(new StoreResponseValidator() {
                @Override
                public void validate(StoreResponse resp) {
                    assertThat(Arrays.asList(resp.getResponseHeaderNames())).asList().contains(headerKey);
                    int index = Arrays.asList(resp.getResponseHeaderNames()).indexOf(headerKey);
                    String value = resp.getResponseHeaderValues()[index];
                    condition.matches(value);
                }
            });
            return this;
        }

        public Builder isSameAs(StoreResponse storeResponse) {

            validators.add(new StoreResponseValidator() {
                @Override
                public void validate(StoreResponse resp) {
                    assertThat(resp).isSameAs(storeResponse);
                }
            });
            return this;
        }

        public Builder withContent(String content) {

            validators.add(new StoreResponseValidator() {
                @Override
                public void validate(StoreResponse resp) {
                    assertThat(content).isEqualTo(resp.getResponseBody());
                }
            });
            return this;
        }

        public Builder withStatus(int status) {

            validators.add(new StoreResponseValidator() {
                @Override
                public void validate(StoreResponse resp) {
                    assertThat(status == resp.getStatus()).isTrue();
                }
            });
            return this;
        }

        public Builder in(StoreResponse... storeResponse) {

            validators.add(new StoreResponseValidator() {
                @Override
                public void validate(StoreResponse resp) {
                    assertThat(resp).isIn((Object[]) storeResponse);
                }
            });
            return this;
        }

        public Builder withBEActivityId(String activityId) {
            withHeader(WFConstants.BackendHeaders.ACTIVITY_ID, activityId);
            return this;
        }

        public Builder withRequestCharge(double value) {
            withHeader(HttpConstants.HttpHeaders.REQUEST_CHARGE, Double.toString(value));
            return this;
        }

        public Builder withRequestChargeGreaterThanOrEqualTo(double value) {
            withHeaderValueCondition(HttpConstants.HttpHeaders.REQUEST_CHARGE, new Condition<>(s -> {
                try {
                    double parsed = Double.parseDouble(s);
                    return parsed >= value;
                } catch (Exception e) {
                    return false;
                }
            }, "request charge should be greater than or equal to " + value));
            return this;
        }

        public Builder withRequestChargeLessThanOrEqualTo(double value) {
            withHeaderValueCondition(HttpConstants.HttpHeaders.REQUEST_CHARGE, new Condition<>(s -> {
                try {
                    double parsed = Double.parseDouble(s);
                    return parsed <= value;
                } catch (Exception e) {
                    return false;
                }
            }, "request charge should be greater than or equal to " + value));
            return this;
        }


        public Builder withBELSN(long lsn) {
            withHeader(WFConstants.BackendHeaders.LSN, Long.toString(lsn));
            return this;
        }

        public Builder withBELocalLSN(long lsn) {
            withHeader(WFConstants.BackendHeaders.LOCAL_LSN, Long.toString(lsn));
            return this;
        }

        public Builder withBELSNGreaterThanOrEqualTo(long minLSN) {
            Condition<String> condition = new Condition<>(value -> {
                try {
                    Long valueAsLong = Long.parseLong(value);
                    return valueAsLong > minLSN;
                } catch (Exception e) {
                    return false;
                }
            }, "min lsn");
            withHeaderValueCondition(WFConstants.BackendHeaders.LSN, condition);
            return this;
        }

        public Builder withBEGlobalLSNGreaterThanOrEqualTo(long minLSN) {
            Condition<String> condition = new Condition<>(value -> {
                try {
                    Long valueAsLong = Long.parseLong(value);
                    return valueAsLong > minLSN;
                } catch (Exception e) {
                    return false;
                }
            }, "min global lsn");
            withHeaderValueCondition(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, condition);
            return this;
        }
    }
}