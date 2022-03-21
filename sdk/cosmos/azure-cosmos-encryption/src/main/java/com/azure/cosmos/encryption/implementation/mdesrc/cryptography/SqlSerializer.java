/*
 * Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT License.
 */

package com.azure.cosmos.encryption.implementation.mdesrc.cryptography;

import java.math.BigDecimal;
import java.text.MessageFormat;


abstract class SqlSerializer extends Serializer<Object> {
    final String SIZE_TEXT = "size";
    final String PRECISION_TEXT = "precision";
    final String SCALE_TEXT = "scale";

    int size = 0;
    int precision = 0;
    int scale = 0;
}


final class SqlBigIntSerializer extends SqlSerializer {

    public SqlBigIntSerializer(int size, int precision, int scale) {
        this.size = size;
        this.precision = precision;
        this.scale = scale;
    }

    @Override
    public byte[] serialize(Object value) throws MicrosoftDataEncryptionException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.BIGINT, value, precision, scale);
        return null;
    }

    @Override
    public Long deserialize(byte[] bytes) throws MicrosoftDataEncryptionException {
        if (null == bytes || bytes.length == 0)
            return null;

        return (Long) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.BIGINT, SSType.BIGINT, precision, scale,
                null);
    }
}


class SqlBinarySerializer extends SqlSerializer {

    private int defaultSize = 30;
    private int minSize = 1;
    private int maxSize = 8000;

    public SqlBinarySerializer(int size, int precision, int scale) throws MicrosoftDataEncryptionException {
        if (size == 0) {
            this.size = defaultSize;
        } else {
            this.size = size;
        }

        if (this.size < minSize || this.size > maxSize) {
            MessageFormat form = new MessageFormat(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_parameterOutOfRange"));
            Object[] msgArgs = {SIZE_TEXT};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }

        this.precision = precision;
        this.scale = scale;
    }

    @Override
    public byte[] serialize(Object value) throws MicrosoftDataEncryptionException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.BINARY, value, size, scale);
        return null;
    }

    @Override
    public byte[] deserialize(byte[] bytes) throws MicrosoftDataEncryptionException {
        if (null == bytes || bytes.length == 0)
            return null;

        return (byte[]) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.BINARY, SSType.BINARY, size, scale, null);
    }
}


class SqlBooleanSerializer extends SqlSerializer {

    public SqlBooleanSerializer(int size, int precision, int scale) {
        this.size = size;
        this.precision = precision;
        this.scale = scale;
    }

    @Override
    public byte[] serialize(Object value) throws MicrosoftDataEncryptionException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.BIT, value, precision, scale);
        return null;
    }

    @Override
    public Boolean deserialize(byte[] bytes) throws MicrosoftDataEncryptionException {
        if (null == bytes || bytes.length == 0)
            return null;

        return (Boolean) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.BIT, SSType.BIT, precision, scale, null);
    }
}


class SqlCharSerializer extends SqlSerializer {

    private int defaultSize = 30;
    private int minSize = 1;
    private int maxSize = 8000;
    private String customCodepage = null;

    public SqlCharSerializer(int size, int precision, int scale) throws MicrosoftDataEncryptionException {
        if (size == 0) {
            this.size = defaultSize;
        } else {
            this.size = size;
        }

        if (this.size < minSize || this.size > maxSize) {
            MessageFormat form = new MessageFormat(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_parameterOutOfRange"));
            Object[] msgArgs = {SIZE_TEXT};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }

        this.precision = precision;
        this.scale = scale;
    }

    public void setCodepage(String s) {
        customCodepage = s;
    }

    @Override
    public byte[] serialize(Object value) throws MicrosoftDataEncryptionException {
        if (value != null)
            return (null == customCodepage) ? SqlSerializerUtil.normalizedValue(JDBCType.CHAR, value, size,
                    scale) : SqlSerializerUtil.normalizedValue(JDBCType.CHAR, value, size, scale, customCodepage);
        return null;
    }

    @Override
    public String deserialize(byte[] bytes) throws MicrosoftDataEncryptionException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (null == customCodepage) ? (String) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.CHAR,
                    SSType.CHAR, size, scale, null)
                                            : (String) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.CHAR,
                                                    SSType.CHAR, size, scale, null, customCodepage);
    }
}


class SqlDateSerializer extends SqlSerializer {

    public SqlDateSerializer(int size, int precision, int scale) {
        this.size = size;
        this.precision = precision;
        this.scale = scale;
    }

    @Override
    public byte[] serialize(Object value) throws MicrosoftDataEncryptionException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.DATE, value, precision, scale);
        return null;
    }

    @Override
    public java.sql.Date deserialize(byte[] bytes) throws MicrosoftDataEncryptionException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (java.sql.Date) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.DATE, SSType.DATE, precision,
                    scale, null);
    }
}


class SqlDatetime2Serializer extends SqlSerializer {

    private int defaultPrecision = 7;
    private int minPrecision = 0;
    private int maxPrecision = 7;

    public SqlDatetime2Serializer(int size, int precision, int scale) throws MicrosoftDataEncryptionException {
        if (precision == 0) {
            this.precision = defaultPrecision;
        } else {
            this.precision = precision;
        }

        if (this.precision < minPrecision || this.precision > maxPrecision) {
            MessageFormat form = new MessageFormat(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_parameterOutOfRange"));
            Object[] msgArgs = {PRECISION_TEXT};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }

        this.size = size;
        this.scale = scale;
    }

    @Override
    public byte[] serialize(Object value) throws MicrosoftDataEncryptionException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.TIMESTAMP, value, precision, scale);
        return null;
    }

    @Override
    public java.sql.Timestamp deserialize(byte[] bytes) throws MicrosoftDataEncryptionException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (java.sql.Timestamp) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.TIMESTAMP, SSType.DATETIME2,
                    precision, scale, null);
    }
}


class SqlDatetimeoffsetSerializer extends SqlSerializer {

    private int defaultPrecision = 7;
    private int minPrecision = 0;
    private int maxPrecision = 7;

    public SqlDatetimeoffsetSerializer(int size, int precision, int scale) throws MicrosoftDataEncryptionException {
        if (precision == 0) {
            this.precision = defaultPrecision;
        } else {
            this.precision = precision;
        }

        if (this.precision < minPrecision || this.precision > maxPrecision) {
            MessageFormat form = new MessageFormat(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_parameterOutOfRange"));
            Object[] msgArgs = {PRECISION_TEXT};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }

        this.size = size;
        this.scale = scale;
    }

    @Override
    public byte[] serialize(Object value) throws MicrosoftDataEncryptionException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.DATETIMEOFFSET, value, precision, scale);
        return null;
    }

    @Override
    public DateTimeOffset deserialize(byte[] bytes) throws MicrosoftDataEncryptionException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (DateTimeOffset) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.DATETIMEOFFSET,
                    SSType.DATETIMEOFFSET, precision, scale, null);
    }
}


class SqlDatetimeSerializer extends SqlSerializer {

    public SqlDatetimeSerializer(int size, int precision, int scale) {
        this.size = size;
        this.precision = precision;
        this.scale = scale;
    }

    @Override
    public byte[] serialize(Object value) throws MicrosoftDataEncryptionException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.DATETIME, value, precision, scale);
        return null;
    }

    @Override
    public java.sql.Timestamp deserialize(byte[] bytes) throws MicrosoftDataEncryptionException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (java.sql.Timestamp) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.DATETIME, SSType.DATETIME,
                    precision, scale, null);
    }
}


class SqlDecimalSerializer extends SqlSerializer {

    private int defaultPrecision = 18;
    private int minPrecision = 1;
    private int maxPrecision = 38;
    private int minScale = 0;

    public SqlDecimalSerializer(int size, int precision, int scale) throws MicrosoftDataEncryptionException {
        if (precision == 0) {
            this.precision = defaultPrecision;
        } else {
            this.precision = precision;

        }

        if (this.precision < minPrecision || this.precision > maxPrecision) {
            MessageFormat form = new MessageFormat(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_parameterOutOfRange"));
            Object[] msgArgs = {PRECISION_TEXT};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }

        if (scale < minScale || scale > precision) {
            MessageFormat form = new MessageFormat(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_parameterOutOfRange"));
            Object[] msgArgs = {SCALE_TEXT};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }

        this.size = size;
        this.scale = scale;
    }

    @Override
    public byte[] serialize(Object value) throws MicrosoftDataEncryptionException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.DECIMAL, value, precision, scale);
        return null;
    }

    @Override
    public BigDecimal deserialize(byte[] bytes) throws MicrosoftDataEncryptionException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (BigDecimal) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.DECIMAL, SSType.DECIMAL, precision,
                    scale, null);
    }
}


class SqlFloatSerializer extends SqlSerializer {

    public SqlFloatSerializer(int size, int precision, int scale) {
        this.size = size;
        this.precision = precision;
        this.scale = scale;
    }

    @Override
    public byte[] serialize(Object value) throws MicrosoftDataEncryptionException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.FLOAT, value, precision, scale);
        return null;
    }

    @Override
    public Double deserialize(byte[] bytes) throws MicrosoftDataEncryptionException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (Double) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.FLOAT, SSType.FLOAT, precision, scale,
                    null);
    }
}


class SqlIntegerSerializer extends SqlSerializer {

    public SqlIntegerSerializer(int size, int precision, int scale) {
        this.size = size;
        this.precision = precision;
        this.scale = scale;
    }

    @Override
    public byte[] serialize(Object value) throws MicrosoftDataEncryptionException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.INTEGER, value, precision, scale);
        return null;
    }

    @Override
    public Integer deserialize(byte[] bytes) throws MicrosoftDataEncryptionException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (Integer) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.INTEGER, SSType.INTEGER, precision,
                    scale, null);
    }
}


class SqlMoneySerializer extends SqlSerializer {

    public SqlMoneySerializer(int size, int precision, int scale) {
        this.size = size;
        this.precision = precision;
        this.scale = scale;
    }

    @Override
    public byte[] serialize(Object value) throws MicrosoftDataEncryptionException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.MONEY, value, precision, scale);
        return null;
    }

    @Override
    public BigDecimal deserialize(byte[] bytes) throws MicrosoftDataEncryptionException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (BigDecimal) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.MONEY, SSType.MONEY, precision,
                    scale, null);
    }
}


class SqlNcharSerializer extends SqlSerializer {

    private int defaultSize = 30;
    private int minSize = 1;
    private int maxSize = 4000;
    private String customCodepage = null;

    public SqlNcharSerializer(int size, int precision, int scale) throws MicrosoftDataEncryptionException {
        if (size == 0) {
            this.size = defaultSize;
        } else {
            this.size = size;
        }

        if (this.size < minSize || this.size > maxSize) {
            MessageFormat form = new MessageFormat(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_parameterOutOfRange"));
            Object[] msgArgs = {SIZE_TEXT};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }

        this.precision = precision;
        this.scale = scale;
    }

    @Override
    public byte[] serialize(Object value) throws MicrosoftDataEncryptionException {
        if (value != null)
            return (null == customCodepage) ? SqlSerializerUtil.normalizedValue(JDBCType.NCHAR, value, size,
                    scale) : SqlSerializerUtil.normalizedValue(JDBCType.NCHAR, value, size, scale, customCodepage);
        return null;
    }

    @Override
    public String deserialize(byte[] bytes) throws MicrosoftDataEncryptionException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (null == customCodepage) ? (String) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.NCHAR,
                    SSType.NCHAR, size, scale, null)
                                            : (String) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.NCHAR,
                                                    SSType.NCHAR, size, scale, null, customCodepage);
    }

    public void setCodepage(String codepage) {
        customCodepage = codepage;
    }
}


class SqlNvarcharSerializer extends SqlSerializer {

    private int max = -1;
    private int defaultSize = 30;
    private int minSize = 1;
    private int maxSize = 4000;
    private String customCodepage = null;

    public SqlNvarcharSerializer(int size, int precision, int scale) throws MicrosoftDataEncryptionException {
        if (size == 0) {
            this.size = defaultSize;
        } else {
            this.size = size;
        }

        if (size != max && (this.size < minSize || this.size > maxSize)) {
            MessageFormat form = new MessageFormat(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_parameterOutOfRange"));
            Object[] msgArgs = {SIZE_TEXT};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }

        this.precision = precision;
        this.scale = scale;
    }

    @Override
    public byte[] serialize(Object value) throws MicrosoftDataEncryptionException {
        if (value != null)
            return (null == customCodepage) ? SqlSerializerUtil.normalizedValue(JDBCType.NVARCHAR, value, size,
                    scale) : SqlSerializerUtil.normalizedValue(JDBCType.NVARCHAR, value, size, scale, customCodepage);
        return null;
    }

    @Override
    public String deserialize(byte[] bytes) throws MicrosoftDataEncryptionException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (null == customCodepage) ? (String) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.NVARCHAR,
                    SSType.NVARCHAR, size, scale, null)
                                            : (String) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.NVARCHAR,
                                                    SSType.NVARCHAR, size, scale, null, customCodepage);
    }

    public void setCodepage(String codepage) {
        customCodepage = codepage;
    }
}


class SqlNumericSerializer extends SqlSerializer {

    private int defaultPrecision = 18;
    private int minPrecision = 1;
    private int maxPrecision = 38;
    private int minScale = 0;

    public SqlNumericSerializer(int size, int precision, int scale) throws MicrosoftDataEncryptionException {
        if (precision == 0) {
            this.precision = defaultPrecision;
        } else {
            this.precision = precision;
        }

        if (this.precision < minPrecision || this.precision > maxPrecision) {
            MessageFormat form = new MessageFormat(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_parameterOutOfRange"));
            Object[] msgArgs = {PRECISION_TEXT};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }

        if (scale < minScale || scale > precision) {
            MessageFormat form = new MessageFormat(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_parameterOutOfRange"));
            Object[] msgArgs = {SCALE_TEXT};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }

        this.size = size;
        this.scale = scale;
    }

    @Override
    public byte[] serialize(Object value) throws MicrosoftDataEncryptionException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.NUMERIC, value, precision, scale);
        return null;
    }

    @Override
    public BigDecimal deserialize(byte[] bytes) throws MicrosoftDataEncryptionException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (BigDecimal) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.NUMERIC, SSType.NUMERIC, precision,
                    scale, null);
    }
}


class SqlRealSerializer extends SqlSerializer {

    public SqlRealSerializer(int size, int precision, int scale) {
        this.size = size;
        this.precision = precision;
        this.scale = scale;
    }

    @Override
    public byte[] serialize(Object value) throws MicrosoftDataEncryptionException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.REAL, value, precision, scale);
        return null;
    }

    @Override
    public Float deserialize(byte[] bytes) throws MicrosoftDataEncryptionException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (Float) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.REAL, SSType.REAL, precision, scale,
                    null);
    }
}


class SqlSmalldatetimeSerializer extends SqlSerializer {

    public SqlSmalldatetimeSerializer(int size, int precision, int scale) {
        this.size = size;
        this.precision = precision;
        this.scale = scale;
    }

    @Override
    public byte[] serialize(Object value) throws MicrosoftDataEncryptionException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.SMALLDATETIME, value, precision, scale);
        return null;
    }

    @Override
    public java.sql.Timestamp deserialize(byte[] bytes) throws MicrosoftDataEncryptionException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (java.sql.Timestamp) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.SMALLDATETIME,
                    SSType.SMALLDATETIME, precision, scale, null);
    }
}


class SqlSmallintSerializer extends SqlSerializer {

    public SqlSmallintSerializer(int size, int precision, int scale) {
        this.size = size;
        this.precision = precision;
        this.scale = scale;
    }

    @Override
    public byte[] serialize(Object value) throws MicrosoftDataEncryptionException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.SMALLINT, value, precision, scale);
        return null;
    }

    @Override
    public Short deserialize(byte[] bytes) throws MicrosoftDataEncryptionException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (Short) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.SMALLINT, SSType.SMALLINT, precision,
                    scale, null);
    }
}


class SqlSmallmoneySerializer extends SqlSerializer {

    public SqlSmallmoneySerializer(int size, int precision, int scale) {
        this.size = size;
        this.precision = precision;
        this.scale = scale;
    }

    @Override
    public byte[] serialize(Object value) throws MicrosoftDataEncryptionException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.SMALLMONEY, value, precision, scale);
        return null;
    }

    @Override
    public BigDecimal deserialize(byte[] bytes) throws MicrosoftDataEncryptionException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (BigDecimal) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.SMALLMONEY, SSType.SMALLMONEY,
                    precision, scale, null);
    }
}


class SqlTimeSerializer extends SqlSerializer {

    public SqlTimeSerializer(int size, int precision, int scale) {
        this.size = size;
        this.precision = precision;
        this.scale = scale;
    }

    @Override
    public byte[] serialize(Object value) throws MicrosoftDataEncryptionException {
        if (value != null) {
            if (value instanceof java.sql.Time) {
                String time = "1900-01-01 " + value.toString();
                java.sql.Timestamp ts = java.sql.Timestamp.valueOf(time);
                return SqlSerializerUtil.normalizedValue(JDBCType.TIME, ts, precision, scale);
            } else {
                return SqlSerializerUtil.normalizedValue(JDBCType.TIME, value, precision, scale);

            }
        }
        return null;
    }

    @Override
    public java.sql.Time deserialize(byte[] bytes) throws MicrosoftDataEncryptionException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (java.sql.Time) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.TIME, SSType.TIME, precision,
                    scale, null);
    }
}


class SqlTinyintSerializer extends SqlSerializer {

    public SqlTinyintSerializer(int size, int precision, int scale) {
        this.size = size;
        this.precision = precision;
        this.scale = scale;
    }

    @Override
    public byte[] serialize(Object value) throws MicrosoftDataEncryptionException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.TINYINT, value, precision, scale);
        return null;
    }

    @Override
    public Short deserialize(byte[] bytes) throws MicrosoftDataEncryptionException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (Short) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.TINYINT, SSType.TINYINT, precision,
                    scale, null);
    }
}


class SqlUniqueidentifierSerializer extends SqlSerializer {

    public SqlUniqueidentifierSerializer(int size, int precision, int scale) {
        this.size = size;
        this.precision = precision;
        this.scale = scale;
    }

    @Override
    public byte[] serialize(Object value) throws MicrosoftDataEncryptionException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.GUID, value, precision, scale);
        return null;
    }

    @Override
    public String deserialize(byte[] bytes) throws MicrosoftDataEncryptionException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (String) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.GUID, SSType.GUID, precision, scale,
                    null);
    }
}


class SqlVarbinarySerializer extends SqlSerializer {

    private int max = -1;
    private int defaultSize = 30;
    private int minSize = 1;
    private int maxSize = 8000;

    public SqlVarbinarySerializer(int size, int precision, int scale) throws MicrosoftDataEncryptionException {
        if (size == 0) {
            this.size = defaultSize;
        } else {
            this.size = size;
        }

        if (size != max && (this.size < minSize || this.size > maxSize)) {
            MessageFormat form = new MessageFormat(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_parameterOutOfRange"));
            Object[] msgArgs = {SIZE_TEXT};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }

        this.precision = precision;
        this.scale = scale;
    }

    @Override
    public byte[] serialize(Object value) throws MicrosoftDataEncryptionException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.VARBINARY, value, size, scale);
        return null;
    }

    @Override
    public byte[] deserialize(byte[] bytes) throws MicrosoftDataEncryptionException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (byte[]) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.VARBINARY, SSType.VARBINARY, size,
                    scale, null);
    }
}


class SqlVarcharSerializer extends SqlSerializer {

    private int max = -1;
    private int defaultSize = 30;
    private int minSize = 1;
    private int maxSize = 8000;
    private String customCodepage = null;

    public SqlVarcharSerializer(int size, int precision, int scale) throws MicrosoftDataEncryptionException {
        if (size == 0) {
            this.size = defaultSize;
        } else {
            this.size = size;
        }

        if (size != max && (this.size < minSize || this.size > maxSize)) {
            MessageFormat form = new MessageFormat(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_parameterOutOfRange"));
            Object[] msgArgs = {SIZE_TEXT};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }

        this.precision = precision;
        this.scale = scale;
    }

    @Override
    public byte[] serialize(Object value) throws MicrosoftDataEncryptionException {
        if (value != null)
            return (null == customCodepage) ? SqlSerializerUtil.normalizedValue(JDBCType.VARCHAR, value, size,
                    scale) : SqlSerializerUtil.normalizedValue(JDBCType.VARCHAR, value, size, scale, customCodepage);
        return null;
    }

    @Override
    public String deserialize(byte[] bytes) throws MicrosoftDataEncryptionException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (null == customCodepage) ? (String) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.VARCHAR,
                    SSType.VARCHAR, size, scale, null)
                                            : (String) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.VARCHAR,
                                                    SSType.VARCHAR, size, scale, null, customCodepage);
    }

    public void setCodepage(String codepage) {
        customCodepage = codepage;
    }
}
