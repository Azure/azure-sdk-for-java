// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.phonenumbers.siprouting;

import com.azure.communication.phonenumbers.SipRoutingClientBuilder;

public class BuilderJavaDocCodeSnippets {

    /**
     * Sample code for creating a SIP Routing Client Builder.
     *
     * @return the SIP Routing Client Builder.
     */
    public SipRoutingClientBuilder createSipRoutingClientBuilder() {
        // BEGIN: com.azure.communication.phonenumbers.siprouting.builder.instantiation
        SipRoutingClientBuilder builder = new SipRoutingClientBuilder();
        // END: com.azure.communication.phonenumbers.siprouting.builder.instantiation
        return builder;
    }

}
