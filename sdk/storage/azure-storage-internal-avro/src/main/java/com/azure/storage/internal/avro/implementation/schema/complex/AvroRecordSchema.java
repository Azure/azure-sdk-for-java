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
 *
 * Field1 Field2 Field3 ....
 */
public class AvroRecordSchema extends AvroSchema<Map<String, Object>> {

    private final List<AvroRecordField> fields;
    private Iterator<AvroRecordField> fieldIterator;
    private AvroRecordField currentField;

    /**
     * Constructs a new AvroRecordSchema.
     *
     * @param name The name of the record.
     * @param fields The fields in the record.
     * @param state The state of the parser.
     * @param onResult The result handler.
     */
    public AvroRecordSchema(String name, List<AvroRecordField> fields, AvroParserState state,
        Consumer<Map<String, Object>> onResult) {
        super(state, onResult);
        this.fields = fields;
        this.result = new LinkedHashMap<>();

        /* Add $record:name to the name so we can determine what type of record this is downstream. */
        this.result.put(AvroConstants.RECORD, name);
    }

    @Override
    public void add() {
        this.state.push(this);

        /* Read the first field, call onField. */
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
     * Field handler.
     * Once we read the field, add the field to the result map.
     * If there are more fields to be read, add the schema of the next field,
     * otherwise, mark yourself as done.
     *
     * @param result The field.
     */
    private void onField(Object result) {
        /* Add the name, result pair to the map. */
        this.result.put(this.currentField.getName(), result);

        /* If there are more fields to be read, read the next field and call onField. */
        if (this.fieldIterator.hasNext()) {
            this.currentField = this.fieldIterator.next();
            AvroSchema fieldSchema = getSchema(
                this.currentField.getType(),
                this.state,
                this::onField
            );
            fieldSchema.add();
        /* If there are no more fields, then we're done. */
        } else {
            this.done = true;
        }
    }

    @Override
    public void progress() {
        /* Progress is defined by progress on the sub-type schemas. */
    }

    @Override
    public boolean canProgress() {
        /* Can always make progress since it is defined by the progress on the sub-type schemas. */
        return true;
    }
}
