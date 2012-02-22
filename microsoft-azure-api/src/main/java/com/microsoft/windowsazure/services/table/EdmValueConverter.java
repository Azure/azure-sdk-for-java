package com.microsoft.windowsazure.services.table;

public interface EdmValueConverter {
    String serialize(String edmType, Object value);

    Object deserialize(String edmType, String value);
}
