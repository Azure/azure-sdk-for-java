package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Undefined;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;

import java.util.ArrayList;
import java.util.List;

public class PartitionKeyBuilder {
    private List<Object> partitionKeyValues;

    /**
     * Constructor. CREATE a new instance of the PartitionKeyBuilder object.
     */
    public PartitionKeyBuilder()
    {
        this.partitionKeyValues = new ArrayList<Object>();
    }

    public PartitionKeyBuilder Add(String value)
    {
        this.partitionKeyValues.add(value);
        return this;
    }

    public PartitionKeyBuilder Add(double value)
    {
        this.partitionKeyValues.add(value);
        return this;
    }

    public PartitionKeyBuilder Add(boolean value)
    {
        this.partitionKeyValues.add(value);
        return this;
    }

    public PartitionKeyBuilder AddNullValue()
    {
        this.partitionKeyValues.add(null);
        return this;
    }

    public PartitionKeyBuilder AddNoneValue()
    {
        this.partitionKeyValues.add(PartitionKey.NONE);
        return this;
    }

    public PartitionKey Build()
    {
        // Why these checks?
        // These changes are being added for SDK to support multiple paths in a partition key.
        //
        // Currently, when a resource does not specify a value for the PartitionKey,
        // we assign a temporary value `PartitionKey.None` and later discern whether
        // it is a PartitionKey.Undefined or PartitionKey.Empty based on the Collection Type.
        // We retain this behaviour for single path partition keys.
        //
        // For collections with multiple path keys, absence of a partition key values is
        // always treated as a PartitionKey.Undefined.
        if(this.partitionKeyValues.size() == 0)
        {
            throw new IllegalArgumentException("No partition key value has been specified");
        }

        if(this.partitionKeyValues.size() == 1 && PartitionKey.NONE.equals(this.partitionKeyValues.get(0)))
        {
            return PartitionKey.NONE;
        }

        PartitionKeyInternal partitionKeyInternal;
        Object[] valueArray = new Object[this.partitionKeyValues.size()];
        for(int i = 0; i < this.partitionKeyValues.size(); i++)
        {
            Object val = this.partitionKeyValues.get(i);
            if(PartitionKey.NONE.equals(val))
            {
                valueArray[i] = Undefined.value();
            }
            else
            {
                valueArray[i] = val;
            }
        }

        partitionKeyInternal = PartitionKeyInternal.fromObjectArray(valueArray, true);
        return new PartitionKey(partitionKeyInternal);
    }
}
