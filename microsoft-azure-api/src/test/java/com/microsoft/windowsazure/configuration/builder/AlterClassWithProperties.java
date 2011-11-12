package com.microsoft.windowsazure.configuration.builder;

import java.util.Map;

import com.microsoft.windowsazure.configuration.builder.Builder;

public class AlterClassWithProperties implements Builder.Alteration<ClassWithProperties> {

    public ClassWithProperties alter(ClassWithProperties instance,
            Builder builder, Map<String, Object> properties) throws Exception {
        instance.setFoo(instance.getFoo() + " - changed");
        return instance;
    }

}
