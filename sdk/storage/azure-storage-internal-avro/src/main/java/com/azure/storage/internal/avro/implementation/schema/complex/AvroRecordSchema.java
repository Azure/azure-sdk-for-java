package com.azure.storage.internal.avro.implementation.schema.complex;

import com.azure.storage.internal.avro.implementation.AvroConstants;
import com.azure.storage.internal.avro.implementation.schema.AvroRecordField;
import com.azure.storage.internal.avro.implementation.AvroParserState;
import com.azure.storage.internal.avro.implementation.schema.AvroType;
import com.azure.storage.internal.avro.implementation.schema.AvroSchema;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A record is encoded by encoding the values of its fields in the order that they are declared.
 * In other words, a record is encoded as just the concatenation of the encodings of its fields.
 * Field values are encoded per their schema.
 *
 * The field value schemas will do most of the work, so we need to just keep track of which field we are
 * working on and add them to the map as they come in.
 */
public class AvroRecordSchema extends AvroSchema<Map<String, Object>> {

    private final List<AvroRecordField> fields;
    private Iterator<AvroRecordField> fieldIterator;
    private AvroRecordField currentField;

    public AvroRecordSchema(String name, List<AvroRecordField> fields, AvroParserState state,
        Consumer<Map<String, Object>> onResult) {
        super(state, onResult);
        this.fields = fields;
        this.result = new LinkedHashMap<>();

        /* Add $record:name to the name so we can determine what type of record this is downstream. */
        this.result.put(AvroConstants.RECORD, name);
    }

    /**
     * Read the first field by adding its schema.
     * @see AvroSchema#getSchema(AvroType, AvroParserState, Consumer)
     */
    @Override
    public void add() {
        this.state.push(this);
        this.fieldIterator = this.fields.iterator();
        this.currentField = this.fieldIterator.next();

        AvroSchema fieldSchema = getSchema(
            this.currentField.getType(),
            this.state,
            this::onField
        );
        fieldSchema.add();
    }

    /**
     * Once we read the field, add the field to the result map.
     * If there are more fields to be read, add the schema of the next field,
     * otherwise, mark yourself as done.
     *
     * @param result The field.
     */
    private void onField(Object result) {
        this.result.put(this.currentField.getName(), result);

        if (this.fieldIterator.hasNext()) {
            this.currentField = this.fieldIterator.next();
            AvroSchema fieldSchema = getSchema(
                this.currentField.getType(),
                this.state,
                this::onField
            );
            fieldSchema.add();
        } else {
            this.done = true;
        }
    }

    @Override
    public void progress() {

    }

    @Override
    public boolean canProgress() {
        return true;
    }
}
