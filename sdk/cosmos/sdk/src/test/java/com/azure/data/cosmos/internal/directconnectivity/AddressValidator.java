// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.internal.directconnectivity.Address;
import com.azure.data.cosmos.internal.directconnectivity.Protocol;
import com.azure.data.cosmos.internal.directconnectivity.Address;
import com.azure.data.cosmos.internal.directconnectivity.Protocol;
import org.assertj.core.api.Condition;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This is a helper class for validating a partition address for tests.
 */
public interface AddressValidator {

    void validate(Address address);

    class Builder {
        private List<AddressValidator> validators = new ArrayList<>();

        public AddressValidator build() {
            return new AddressValidator() {

                @Override
                public void validate(Address address) {
                    for (AddressValidator validator : validators) {
                        validator.validate(address);
                    }
                }
            };
        }

        public Builder withId(final String resourceId) {
            validators.add(new AddressValidator() {

                @Override
                public void validate(Address address) {
                    assertThat(address.id()).as("check Resource Id").isEqualTo(resourceId);
                }
            });
            return this;
        }



        public Builder withProperty(String propertyName, Condition<Object> validatingCondition) {
            validators.add(new AddressValidator() {

                @Override
                public void validate(Address address) {
                    assertThat(address.get(propertyName)).is(validatingCondition);

                }
            });
            return this;
        }

        public Builder withProperty(String propertyName, Object value) {
            validators.add(new AddressValidator() {

                @Override
                public void validate(Address address) {
                    assertThat(address.get(propertyName)).isEqualTo(value);

                }
            });
            return this;
        }

        public Builder isPrimary(boolean isPrimary) {
            validators.add(new AddressValidator() {

                @Override
                public void validate(Address address) {
                    assertThat(address.IsPrimary()).isTrue();
                }
            });
            return this;
        }

        public Builder httpsProtocol() {
            validators.add(new AddressValidator() {

                @Override
                public void validate(Address address) {
                    assertThat(address.getProtocolScheme()).isEqualTo("https");
                }
            });
            return this;
        }

        public Builder protocol(Protocol protocol) {
            validators.add(new AddressValidator() {

                @Override
                public void validate(Address address) {
                    if (protocol == Protocol.HTTPS) {
                        assertThat(address.getProtocolScheme()).isEqualTo("https");
                    } else if (protocol == Protocol.TCP){
                        assertThat(address.getProtocolScheme()).isEqualTo("rntbd");
                    }
                }
            });
            return this;
        }

        public Builder withRid(String rid) {
            validators.add(new AddressValidator() {

                @Override
                public void validate(Address address) {
                    assertThat(address.resourceId()).isEqualTo(rid);
                }
            });
            return this;
        }

        public Builder withPartitionKeyRangeId(String partitionKeyRangeId) {
            validators.add(new AddressValidator() {

                @Override
                public void validate(Address address) {
                    assertThat(address.getParitionKeyRangeId()).isEqualTo(partitionKeyRangeId);
                }
            });
            return this;
        }
    }
}
