/*
 * Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT License.
 */

package com.azure.cosmos.encryption.implementation.mdesrc.cryptography;

import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Provides methods for getting serializer implementations, such as by type and ID.
 *
 */
public class SqlSerializerFactory extends SerializerFactory {

    public final static String BIGINT_ID = "bigint";
    public final static String BINARY_ID = "binary";
    public final static String BIT_ID = "bit";
    public final static String CHAR_ID = "char";
    public final static String DATE_ID = "date";
    public final static String DATETIME2_ID = "datetime2";
    public final static String DATETIMEOFFSET_ID = "datetimeoffset";
    public final static String DATETIME_ID = "datetime";
    public final static String DECIMAL_ID = "decimal";
    public final static String NUMERIC_ID = "numeric";
    public final static String FLOAT_ID = "float";
    public final static String INTEGER_ID = "integer";
    public final static String MONEY_ID = "money";
    public final static String NCHAR_ID = "nchar";
    public final static String NVARCHAR_ID = "nvarchar";
    public final static String REAL_ID = "real";
    public final static String SMALLDATETIME_ID = "smalldatetime";
    public final static String SMALLINT_ID = "smallint";
    public final static String SMALLMONEY_ID = "smallmoney";
    public final static String TIME_ID = "time";
    public final static String TINYINT_ID = "tinyint";
    public final static String UNIQUEIDENTIFIER_ID = "uniqueidentifier";
    public final static String VARBINARY_ID = "varbinary";
    public final static String VARCHAR_ID = "varchar";

    private Map<String, ISerializer> serializerByIdentifier = new ConcurrentHashMap<String, ISerializer>();
    private Map<Type, ISerializer> serializerByType = new ConcurrentHashMap<Type, ISerializer>();

    private static Map<Type, ISerializer> serializerCache = new ConcurrentHashMap<Type, ISerializer>();

    /**
     * Initializes a new instance of the SqlSerializerFactory class.
     *
     * @throws MicrosoftDataEncryptionException
     *         on error
     */
    public SqlSerializerFactory() throws MicrosoftDataEncryptionException {
        initialize();
    }

    /**
     * Gets a registered serializer by its Identifier Property. The accepted Strings can be found from the static IDs
     * provided by this factory class.
     *
     * @param id
     *        The identifier uniquely identifies a particular Serializer implementation.
     * @return The ISerializer implementation
     * @throws MicrosoftDataEncryptionException
     *         on error
     */
    @Override
    public ISerializer getSerializer(String id) throws MicrosoftDataEncryptionException {
        Utils.validateNotNull(id, "Serializer id");

        return serializerByIdentifier.get(id);
    }

    /**
     * Returns a cached instance of the ISerializer or, if not present, creates a new one.
     *
     * @param id
     *        The type of serializer to get or create.
     * @param size
     *        The maximum size of value.
     * @param precision
     *        The maximum number of digits.
     * @param scale
     *        The number of decimal places.
     * @return a serializer for the corresponding type.
     * @throws MicrosoftDataEncryptionException
     *         on error
     */
    public static ISerializer getOrCreate(String id, int size, int precision, int scale) throws MicrosoftDataEncryptionException {
        return getOrCreate(id, size, precision, scale, null);
    }

    /**
     * Returns a cached instance of the ISerializer or, if not present, creates a new one.
     *
     * @param id
     *        The type of serializer to get or create.
     * @param size
     *        The maximum size of value.
     * @param precision
     *        The maximum number of digits.
     * @param scale
     *        The number of decimal places.
     * @param codepage
     *        The code page to represent the value.
     * @return a serializer for the corresponding type.
     * @throws MicrosoftDataEncryptionException
     *         on error
     */
    public static ISerializer getOrCreate(String id, int size, int precision, int scale,
                                          String codepage) throws MicrosoftDataEncryptionException {
        ISerializer s = serializerCache.get(new Type(id, size, precision, scale, codepage));
        if (null == s) {
            s = createSerializer(id, size, precision, scale, true, codepage);
        }
        return s;
    }

    private static ISerializer createSerializer(String id, int size, int precision, int scale, boolean isByType,
                                                String codepage) throws MicrosoftDataEncryptionException {
        ISerializer s = null;
        switch (id.toLowerCase()) {
            case BIGINT_ID:
                s = new SqlBigIntSerializer(size, precision, scale);
                break;
            case BINARY_ID:
                s = new SqlBinarySerializer(size, precision, scale);
                break;
            case BIT_ID:
                s = new SqlBooleanSerializer(size, precision, scale);
                break;
            case CHAR_ID:
                s = new SqlCharSerializer(size, precision, scale);
                ((SqlCharSerializer) s).setCodepage(codepage);
                break;
            case DATE_ID:
                s = new SqlDateSerializer(size, precision, scale);
                break;
            case DATETIME2_ID:
                s = new SqlDatetime2Serializer(size, precision, scale);
                break;
            case DATETIMEOFFSET_ID:
                s = new SqlDatetimeoffsetSerializer(size, precision, scale);
                break;
            case DATETIME_ID:
                s = new SqlDatetimeSerializer(size, precision, scale);
                break;
            case DECIMAL_ID:
                s = new SqlDecimalSerializer(size, precision, scale);
                break;
            case NUMERIC_ID:
                s = new SqlNumericSerializer(size, precision, scale);
                break;
            case FLOAT_ID:
                s = new SqlFloatSerializer(size, precision, scale);
                break;
            case INTEGER_ID:
                s = new SqlIntegerSerializer(size, precision, scale);
                break;
            case MONEY_ID:
                s = new SqlMoneySerializer(size, precision, scale);
                break;
            case NCHAR_ID:
                s = new SqlNcharSerializer(size, precision, scale);
                ((SqlNcharSerializer) s).setCodepage(codepage);
                break;
            case NVARCHAR_ID:
                s = new SqlNvarcharSerializer(size, precision, scale);
                ((SqlNvarcharSerializer) s).setCodepage(codepage);
                break;
            case REAL_ID:
                s = new SqlRealSerializer(size, precision, scale);
                break;
            case SMALLDATETIME_ID:
                s = new SqlSmalldatetimeSerializer(size, precision, scale);
                break;
            case SMALLINT_ID:
                s = new SqlSmallintSerializer(size, precision, scale);
                break;
            case SMALLMONEY_ID:
                s = new SqlSmallmoneySerializer(size, precision, scale);
                break;
            case TIME_ID:
                s = new SqlTimeSerializer(size, precision, scale);
                break;
            case TINYINT_ID:
                s = new SqlTinyintSerializer(size, precision, scale);
                break;
            case UNIQUEIDENTIFIER_ID:
                s = new SqlUniqueidentifierSerializer(size, precision, scale);
                break;
            case VARBINARY_ID:
                s = new SqlVarbinarySerializer(size, precision, scale);
                break;
            case VARCHAR_ID:
                s = new SqlVarcharSerializer(size, precision, scale);
                ((SqlVarcharSerializer) s).setCodepage(codepage);
                break;
            default:
                MessageFormat form = new MessageFormat(MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidSerializerName"));
                Object[] msgArgs = {id.toLowerCase()};
                throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }
        serializerCache.put(new Type(id, size, precision, scale, codepage), s);
        return s;
    }

    private Serializer<?> createSerializer(String id) throws MicrosoftDataEncryptionException {
        Serializer<?> s = null;
        switch (id) {
            case "java.lang.Boolean":
                s = new SqlBooleanSerializer(0, 0, 0);
                break;
            case "java.lang.Integer":
                s = new SqlIntegerSerializer(0, 0, 0);
                break;
            case "java.lang.Long":
                s = new SqlBigIntSerializer(0, 0, 0);
                break;
            case "java.lang.Byte":
                s = new SqlBinarySerializer(0, 0, 0);
                break;
            case "java.lang.Double":
                s = new SqlFloatSerializer(0, 0, 0);
                break;
            case "java.lang.Float":
                s = new SqlRealSerializer(0, 0, 0);
                break;
            case "java.lang.String":
                s = new SqlVarcharSerializer(0, 0, 0);
                break;
            case "java.lang.Character":
                s = new SqlCharSerializer(0, 0, 0);
                break;
            case "java.util.UUID":
                s = new SqlUniqueidentifierSerializer(0, 0, 0);
                break;
            case "java.util.Date":
                s = new SqlDateSerializer(0, 0, 0);
                break;
            default:
                MessageFormat form = new MessageFormat(MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidSerializerName"));
                Object[] msgArgs = {id.toLowerCase()};
                throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }
        serializerByIdentifier.put(id, s);
        return s;
    }

    @Override
    public <T> ISerializer getDefaultSerializer(Class<?> clazz) throws MicrosoftDataEncryptionException {
        ISerializer s = serializerByIdentifier.get(clazz.getName());
        if (null == s) {
            s = createSerializer(clazz.getName());
        }
        return s;
    }

    @Override
    public void registerSerializer(Type type, ISerializer sqlSerializer, boolean overrideDefault) {
        serializerByIdentifier.put(type.getId(), sqlSerializer);

        if (overrideDefault || !hasDefaultSqlSerializer(type)) {
            serializerByType.put(type, sqlSerializer);
        }
    }

    private void initialize() throws MicrosoftDataEncryptionException {
        registerDefaultSqlSerializers();
    }

    private void registerDefaultSqlSerializers() throws MicrosoftDataEncryptionException {
        registerSerializer(new Type(BIGINT_ID, 0, 0, 0, null), new SqlBigIntSerializer(0, 0, 0), false);
        registerSerializer(new Type(BINARY_ID, 0, 0, 0, null), new SqlBinarySerializer(0, 0, 0), false);
        registerSerializer(new Type(BIT_ID, 0, 0, 0, null), new SqlBooleanSerializer(0, 0, 0), false);
        registerSerializer(new Type(CHAR_ID, 0, 0, 0, null), new SqlCharSerializer(0, 0, 0), false);
        registerSerializer(new Type(DATE_ID, 0, 0, 0, null), new SqlDateSerializer(0, 0, 0), false);
        registerSerializer(new Type(DATETIME2_ID, 0, 0, 0, null), new SqlDatetime2Serializer(0, 0, 0), false);
        registerSerializer(new Type(DATETIMEOFFSET_ID, 0, 0, 0, null), new SqlDatetimeoffsetSerializer(0, 0, 0), false);
        registerSerializer(new Type(DATETIME_ID, 0, 0, 0, null), new SqlDatetimeSerializer(0, 0, 0), false);
        registerSerializer(new Type(DECIMAL_ID, 0, 0, 0, null), new SqlDecimalSerializer(0, 0, 0), false);
        registerSerializer(new Type(NUMERIC_ID, 0, 0, 0, null), new SqlNumericSerializer(0, 0, 0), false);
        registerSerializer(new Type(FLOAT_ID, 0, 0, 0, null), new SqlFloatSerializer(0, 0, 0), false);
        registerSerializer(new Type(INTEGER_ID, 0, 0, 0, null), new SqlIntegerSerializer(0, 0, 0), false);
        registerSerializer(new Type(MONEY_ID, 0, 0, 0, null), new SqlMoneySerializer(0, 0, 0), false);
        registerSerializer(new Type(NCHAR_ID, 0, 0, 0, null), new SqlNcharSerializer(0, 0, 0), false);
        registerSerializer(new Type(NVARCHAR_ID, 0, 0, 0, null), new SqlNvarcharSerializer(0, 0, 0), false);
        registerSerializer(new Type(REAL_ID, 0, 0, 0, null), new SqlRealSerializer(0, 0, 0), false);
        registerSerializer(new Type(SMALLDATETIME_ID, 0, 0, 0, null), new SqlSmalldatetimeSerializer(0, 0, 0), false);
        registerSerializer(new Type(SMALLINT_ID, 0, 0, 0, null), new SqlSmallintSerializer(0, 0, 0), false);
        registerSerializer(new Type(SMALLMONEY_ID, 0, 0, 0, null), new SqlSmallmoneySerializer(0, 0, 0), false);
        registerSerializer(new Type(TIME_ID, 0, 0, 0, null), new SqlTimeSerializer(0, 0, 0), false);
        registerSerializer(new Type(TINYINT_ID, 0, 0, 0, null), new SqlTinyintSerializer(0, 0, 0), false);
        registerSerializer(new Type(UNIQUEIDENTIFIER_ID, 0, 0, 0, null), new SqlUniqueidentifierSerializer(0, 0, 0),
                false);
        registerSerializer(new Type(VARBINARY_ID, 0, 0, 0, null), new SqlVarbinarySerializer(0, 0, 0), false);
        registerSerializer(new Type(VARCHAR_ID, 0, 0, 0, null), new SqlVarcharSerializer(0, 0, 0), false);
    }

    private boolean hasDefaultSqlSerializer(Type type) {
        return serializerByType.containsKey(type);
    }
}
