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
package com.azure.data.cosmos.directconnectivity;

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
