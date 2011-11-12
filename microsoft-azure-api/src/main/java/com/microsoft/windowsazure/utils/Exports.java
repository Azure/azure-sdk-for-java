package com.microsoft.windowsazure.utils;

import com.microsoft.windowsazure.configuration.builder.Builder;

public class Exports implements Builder.Exports {
    public void register(Builder.Registry registry) {
        registry.add(DateFactory.class, DefaultDateFactory.class);
    }
}
