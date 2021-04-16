// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.servicebus.jms.premium;

import com.azure.spring.sample.servicebus.jms.standard.StandardServiceBusJmsIT;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("premium")
@SpringBootTest
class PremiumServiceBusJmsIT extends StandardServiceBusJmsIT {

}
