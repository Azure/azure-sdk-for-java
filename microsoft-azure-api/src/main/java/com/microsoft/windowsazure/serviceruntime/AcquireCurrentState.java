package com.microsoft.windowsazure.serviceruntime;

import java.math.BigInteger;
import java.util.Calendar;

/**
 * 
 */
class AcquireCurrentState extends CurrentState {
    private final BigInteger incarnation;
    private final CurrentStatus status;
    private final Calendar expiration;

    public AcquireCurrentState(String clientId, BigInteger incarnation,
            CurrentStatus status, Calendar expiration) {
        super(clientId);
        this.incarnation = incarnation;
        this.status = status;
        this.expiration = expiration;
    }

    public BigInteger getIncarnation() {
        return incarnation;
    }

    public CurrentStatus getStatus() {
        return status;
    }

    public Calendar getExpiration() {
        return expiration;
    }
}
