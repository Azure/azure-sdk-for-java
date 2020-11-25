package com.azure.cosmos.implementation.changefeed.implementation;

public enum ChangeFeedStartFromTypes {
    BEGINNING,
    NOW,
    POINT_IN_TIME,
    CONTINUATION,
    LEASE
}
