package com.microsoft.azure.configuration.builder;

import javax.inject.Inject;
import javax.inject.Named;

public class ClassWithNamedParameter {
    private String hello;

    @Inject
    public ClassWithNamedParameter(@Named("Foo") String hello) {
        this.hello = hello;
    }

    public String getHello() {
        return hello;
    }

    public void setHello(String hello) {
        this.hello = hello;
    }
}
