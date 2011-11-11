package com.microsoft.azure.utils;

import com.microsoft.azure.configuration.builder.Builder;

public class Exports implements Builder.Exports {
    public void register(Builder.Registry registry) {
        registry.add(DateFactory.class, DefaultDateFactory.class);
    }
}
