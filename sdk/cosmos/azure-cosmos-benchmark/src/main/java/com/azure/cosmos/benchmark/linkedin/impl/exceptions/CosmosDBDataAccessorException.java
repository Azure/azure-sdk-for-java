// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin.impl.exceptions;

import com.google.common.base.Preconditions;
import java.time.Duration;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

import static com.microsoft.applicationinsights.core.dependencies.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;


/**
 * Exception specific to operations on Cosmos DocumentDB
 */
public class CosmosDBDataAccessorException extends AccessorException {
    private final String _activityId;
    private final int _statusCode;
    private final Duration _retryWaitDuration;
    private final double _costUnits;

    private CosmosDBDataAccessorException(final String message, final String activityId, int statusCode,
        final Duration retryWaitDuration, double costUnits, final Throwable cause) {
        super(message, cause);

        _activityId = activityId;
        _statusCode = statusCode;
        _retryWaitDuration = retryWaitDuration;
        _costUnits = costUnits;
    }

    public String getActivityId() {
        return _activityId;
    }

    public int getStatusCode() {
        return _statusCode;
    }

    public Duration getRetryWaitDuration() {
        return _retryWaitDuration;
    }

    public double getCostUnits() {
        return _costUnits;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CosmosDBDataAccessorException that = (CosmosDBDataAccessorException) o;
        return Objects.equals(getMessage(), that.getMessage()) && Objects.equals(getCause(), that.getCause()) && Objects
            .equals(_statusCode, that._statusCode) && Objects.equals(_retryWaitDuration, that._retryWaitDuration)
            && Objects.equals(_costUnits, that._costUnits);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMessage(), getCause(), _statusCode, _retryWaitDuration, _costUnits);
    }

    /**
     * Builder class to ensure an invalid DocumentAccessorException can never be thrown
     */
    public static class Builder {

        private String _message;
        private Throwable _cause;
        private String _activityId = StringUtils.EMPTY;
        private int _statusCode = SC_INTERNAL_SERVER_ERROR;
        private Duration _retryWaitTime = Duration.ofSeconds(0);
        private double _costUnits = 0.0d;

        public Builder() {
        }

        public Builder(CosmosDBDataAccessorException cosmosDBDataAccessorException) {
            super();
            _message = cosmosDBDataAccessorException.getMessage();
            _cause = cosmosDBDataAccessorException.getCause();
            _activityId = cosmosDBDataAccessorException.getActivityId();
            _statusCode = cosmosDBDataAccessorException.getStatusCode();
            _retryWaitTime = cosmosDBDataAccessorException.getRetryWaitDuration();
            _costUnits = cosmosDBDataAccessorException.getCostUnits();
        }

        // TODO make these methods package-private when the old Azure API is deprecated.
        public Builder setMessage(final String message) {
            Preconditions.checkArgument(StringUtils.isNotBlank(message),
                "The message describing the exception can not be empty/null");
            _message = message;
            return this;
        }

        public Builder setCause(final Throwable cause) {
            Preconditions.checkNotNull(cause, "The exception cause can not be null.");
            Preconditions.checkArgument(!(cause instanceof AccessorException),
                "Cannot create nested AccessorException.");
            _cause = cause;

            return this;
        }

        public Builder setActivityId(final String activityId) {
            _activityId =
                Preconditions.checkNotNull(activityId, "The activityId associated with the request can not be null");

            return this;
        }

        public Builder setStatusCode(int statusCode) {
            Preconditions.checkArgument(statusCode >= 0, "The statusCode can't be < 0 [It's an HTTP status code]");
            _statusCode = statusCode;

            return this;
        }

        public Builder setRetryWaitTime(final Duration retryWaitTime) {
            Preconditions.checkArgument(Objects.nonNull(retryWaitTime) && retryWaitTime.toMillis() >= 0,
                "retryWaitTime can't be null or < 0");
            _retryWaitTime = retryWaitTime;

            return this;
        }

        public Builder setCostUnits(double costUnits) {
            Preconditions.checkArgument(costUnits >= 0.0, "costUnits can't be < 0");
            _costUnits = costUnits;

            return this;
        }

        public CosmosDBDataAccessorException build() {
            Preconditions.checkNotNull(_message, "A message describing the exception must be provided");
            Preconditions.checkNotNull(_cause, "The exception from the underlying data store must be provided");

            return new CosmosDBDataAccessorException(_message, _activityId, _statusCode, _retryWaitTime, _costUnits,
                _cause);
        }
    }
}
