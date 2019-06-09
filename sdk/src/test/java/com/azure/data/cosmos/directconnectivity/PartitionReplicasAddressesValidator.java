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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This is a helper class for validating partition replicas' addresses for tests.
 */
public interface PartitionReplicasAddressesValidator {

    int MAX_REPLICA_SIZE = 4;

    void validate(Collection<Address> addresses);

    class Builder {
        private List<PartitionReplicasAddressesValidator> validators = new ArrayList<>();

        public PartitionReplicasAddressesValidator build() {
            return new PartitionReplicasAddressesValidator() {

                public void validate(Collection<Address> addresses) {
                    for (PartitionReplicasAddressesValidator validator : validators) {
                        validator.validate(addresses);
                    }
                }
            };
        }

        public Builder size(final int expectedCount) {

            validators.add(new PartitionReplicasAddressesValidator() {
                @Override
                public void validate(Collection<Address> addresses) {
                    assertThat(addresses).hasSize(expectedCount);
                }
            });
            return this;
        }

        public Builder forEach(AddressValidator validator) {

            validators.add(new PartitionReplicasAddressesValidator() {
                @Override
                public void validate(Collection<Address> addresses) {

                    for (Address address : addresses) {
                        validator.validate(address);
                    }

                }
            });
            return this;
        }

        public Builder httpsProtocol() {
            this.forEach(new AddressValidator.Builder().httpsProtocol().build());
            return this;
        }

        public Builder withProtocol(Protocol protocol) {
            this.forEach(new AddressValidator.Builder().protocol(protocol).build());
            return this;
        }

        public Builder replicasOfPartition(String partitionKeyRangeId) {
            validators.add(new PartitionReplicasAddressesValidator() {
                @Override
                public void validate(Collection<Address> addresses) {

                    // if running against prod due to upgrade etc, we may have occasionally 3 or 4 replicas.
                    assertThat(addresses).size().isGreaterThanOrEqualTo(MAX_REPLICA_SIZE - 1).isLessThanOrEqualTo(MAX_REPLICA_SIZE);
                    assertThat(addresses.stream().filter(a -> a.IsPrimary()).count()).isEqualTo(1);

                    Address a = addresses.iterator().next();

                    AddressValidator validator = new AddressValidator.Builder()
                            .withPartitionKeyRangeId(partitionKeyRangeId)
                            .withRid(a.resourceId())
                            .build();

                    for (Address address : addresses) {
                        validator.validate(address);
                    }
                }
            });
            return this;
        }

        public Builder replicasOfSamePartition() {
            validators.add(new PartitionReplicasAddressesValidator() {
                @Override
                public void validate(Collection<Address> addresses) {

                    // if running against prod due to upgrade etc, we may have occasionally 3 or 4 replicas.
                    assertThat(addresses).size().isGreaterThanOrEqualTo(MAX_REPLICA_SIZE - 1).isLessThanOrEqualTo(MAX_REPLICA_SIZE);
                    assertThat(addresses.stream().filter(a -> a.IsPrimary()).count()).isEqualTo(1);

                    Address a = addresses.iterator().next();

                    AddressValidator validator = new AddressValidator.Builder()
                            .withPartitionKeyRangeId(a.getParitionKeyRangeId())
                            .withRid(a.resourceId())
                            .build();

                    for (Address address : addresses) {
                        validator.validate(address);
                    }
                }
            });
            return this;
        }

        public Builder replicasOfPartitions(Collection<String> partitionKeyRangeIds) {
            validators.add(new PartitionReplicasAddressesValidator() {
                @Override
                public void validate(Collection<Address> addresses) {

                    for (String pki : partitionKeyRangeIds) {
                        List<Address> partitionReplicas = addresses.stream()
                                .filter(a -> pki.equals(a.getParitionKeyRangeId()))
                                .collect(Collectors.toList());

                        PartitionReplicasAddressesValidator v = new Builder().replicasOfPartition(pki).build();
                        v.validate(partitionReplicas);
                    }
                }
            });
            return this;
        }
    }
}