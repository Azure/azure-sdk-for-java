package com.azure.ai.formrecognizer.models;

import java.util.List;

/**
 * Class to represent the Float value for
 * {@link com.azure.ai.formrecognizer.implementation.models.FieldValue#getValueNumber()}
 */
public class FloatValue extends FieldValue<Float> {
    /*
     * Floating point value.
     */
    private Float valueNumber;

    /**
     * Constructs a FloatValue.
     *
     * @param text The text content of the extracted field.
     * @param boundingBox Bounding box of the field value.
     * @param valueNumber Floating point value.
     */
    public FloatValue(String text, BoundingBox boundingBox, Float valueNumber) {
        super(text, boundingBox);
        this.valueNumber = valueNumber;
    }

    @Override
    public Float getValue() {
        return this.valueNumber;
    }

    @Override
    public void setValue(Float value) {
        this.valueNumber = value;
    }

    @Override
    public List<Element> getElements() {
        return super.getElements();
    }

    @Override
    public Float getConfidence() {
        return super.getConfidence();
    }
}
