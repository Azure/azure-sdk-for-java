package com.azure.cosmos.implementation.directconnectivity.rntbd;

public class RntbdBridgeInternal {

    public static boolean isPayloadPresent(RntbdRequestHeaders headers) {

        RntbdToken isPayloadPresent = headers.get(RntbdConstants.RntbdRequestHeader.PayloadPresent);
        if (isPayloadPresent != null) {
            return isPayloadPresent.getValue(Byte.class) != 0;
        }

        return false;
    }
}
