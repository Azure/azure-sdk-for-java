package com.azure.core.amqp.implementation;

import com.azure.core.util.AzureAttributeBuilder;
import com.azure.core.util.metrics.AzureMeterProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

class AttributeCache<T extends Enum<T>> {
    private final static AzureMeterProvider METER_PROVIDER = AzureMeterProvider.getDefaultProvider();
    private final AzureAttributeBuilder commonAttr;
    private final AzureAttributeBuilder unknownAttr;
    private final Map<T, AzureAttributeBuilder> otherAttr;
    private final T commonValue;

    AttributeCache(String dimensionName, T commonValue, Consumer<AzureAttributeBuilder> addCommonAttributes) {
        this.commonValue = commonValue;

        commonAttr = METER_PROVIDER.createAttributeBuilder();
        addCommonAttributes.accept(commonAttr);
        commonAttr.add(dimensionName, commonValue.toString());

        unknownAttr = METER_PROVIDER.createAttributeBuilder();
        addCommonAttributes.accept(unknownAttr);
        unknownAttr.add(dimensionName, commonValue.toString());

        otherAttr = new ConcurrentHashMap<>();
        for (T val : commonValue.getDeclaringClass().getEnumConstants()) {
            AzureAttributeBuilder attributes = METER_PROVIDER.createAttributeBuilder();
            addCommonAttributes.accept(attributes);
            attributes.add(dimensionName, val.toString());
            otherAttr.put(val, attributes);
        }
    }

    AzureAttributeBuilder getAttributeBuilder(T value) {
        if (value.equals(commonValue)) {
            return commonAttr;
        }

        if (value == null) {
            return unknownAttr;
        }

        AzureAttributeBuilder cachedAttr = otherAttr.get(value);
        return cachedAttr != null ? cachedAttr : unknownAttr;
    }
}
