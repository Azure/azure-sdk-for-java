package com.microsoft.windowsazure.configuration.builder;

import java.util.Map;

import com.microsoft.windowsazure.common.Builder;

public class AlterClassWithProperties implements Builder.Alteration<ClassWithProperties> {

    public ClassWithProperties alter(ClassWithProperties instance, Builder builder, Map<String, Object> properties) {
        instance.setFoo(instance.getFoo() + " - changed");
        return instance;
    }

}
