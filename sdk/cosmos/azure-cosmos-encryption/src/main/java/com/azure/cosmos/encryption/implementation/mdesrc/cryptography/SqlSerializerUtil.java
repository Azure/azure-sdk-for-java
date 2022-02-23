/*
 * Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT License.
 */

package com.azure.cosmos.encryption.implementation.mdesrc.cryptography;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_16LE;
import static java.nio.charset.StandardCharsets.UTF_8;


final class SqlSerializerUtil {
    final static char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    final static int DAYS_INTO_CE_LENGTH = 3;
    final static int MINUTES_OFFSET_LENGTH = 2;
    final static int MAX_FRACTIONAL_SECONDS_SCALE = 7;
    final static int maxDecimalPrecision = 38; // @@max_precision for SQL 2000 and 2005 is 38.
    final static int defaultDecimalPrecision = 18;
    final static private int MAX = -1;

    // Number of days in a "normal" (non-leap) year according to SQL Server.
    final static int DAYS_PER_YEAR = 365;

    final static int BASE_YEAR_1900 = 1900;
    final static int BASE_YEAR_1970 = 1970;
    final static String BASE_DATE_1970 = "1970-01-01";

    public static byte[] normalizedValue(JDBCType destJdbcType, Object value, int destPrecision, int destScale,
                                         String codepage) throws MicrosoftDataEncryptionException {
        switch (destJdbcType) {
            case CHAR:
            case VARCHAR:
            case LONGVARCHAR:

                // Throw exception if length sent in column metadata is smaller than actual data
                if (destPrecision != MAX && ((String) value).length() > destPrecision) {
                    MessageFormat form = new MessageFormat(
                            MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidPSLength"));
                    Object[] msgArgs = {value, destJdbcType, destPrecision, destScale};
                    throw new MicrosoftDataEncryptionException(form.format(msgArgs));
                }
                return ((String) value).getBytes(Charset.forName(codepage));

            case NCHAR:
            case NVARCHAR:
            case LONGNVARCHAR:
                // Throw exception if length sent in column metadata is smaller than actual data
                if (destPrecision != MAX && ((String) value).length() > destPrecision) {
                    MessageFormat form = new MessageFormat(
                            MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidPSLength"));
                    Object[] msgArgs = {value, destJdbcType, destPrecision, destScale};
                    throw new MicrosoftDataEncryptionException(form.format(msgArgs));
                }
                return ((String) value).getBytes(Charset.forName(codepage));

            default:
                MessageFormat form = new MessageFormat(
                        MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidType"));
                Object[] msgArgs = {destJdbcType};
                throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }
    }

    public static byte[] normalizedValue(JDBCType destJdbcType, Object value, int destPrecision,
                                         int destScale) throws MicrosoftDataEncryptionException {
        Long longValue = null;
        byte[] byteValue = null;
        int srcDataPrecision, srcDataScale;
        GregorianCalendar calendar;
        long utcMillis;

        try {
            switch (destJdbcType) {
                case BIT:
                    longValue = (long) ((Boolean) value ? 1 : 0);
                    return ByteBuffer.allocate(Long.SIZE / Byte.SIZE).order(ByteOrder.LITTLE_ENDIAN).putLong(longValue)
                            .array();

                case TINYINT:
                case SMALLINT:
                    if (value instanceof Integer) {
                        int intValue = (int) value;
                        short shortValue = (short) intValue;
                        longValue = (long) shortValue;
                    } else {
                        longValue = (long) (short) value;
                    }
                    return ByteBuffer.allocate(Long.SIZE / Byte.SIZE).order(ByteOrder.LITTLE_ENDIAN).putLong(longValue)
                            .array();

                case INTEGER:
                    longValue = Long.valueOf((Integer) value);
                    return ByteBuffer.allocate(Long.SIZE / Byte.SIZE).order(ByteOrder.LITTLE_ENDIAN).putLong(longValue)
                            .array();

                case BIGINT:
                    longValue = (long) value;
                    return ByteBuffer.allocate(Long.SIZE / Byte.SIZE).order(ByteOrder.LITTLE_ENDIAN).putLong(longValue)
                            .array();

                case BINARY:
                case VARBINARY:
                case LONGVARBINARY:
                    byte[] byteArrayValue;
                    if (value instanceof String) {
                        byteArrayValue = CryptographyExtensions.fromHexString((String) value);
                    } else {
                        byteArrayValue = (byte[]) value;
                    }
                    if (destPrecision != MAX && byteArrayValue.length > destPrecision) {
                        MessageFormat form = new MessageFormat(
                                MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidPSLength"));
                        Object[] msgArgs = {byteArrayValue, destJdbcType, destPrecision, destScale};
                        throw new MicrosoftDataEncryptionException(form.format(msgArgs));
                    }
                    return byteArrayValue;
                case GUID:
                    return asGuidByteArray(UUID.fromString((String) value));

                case CHAR:
                case VARCHAR:
                case LONGVARCHAR:

                    // Throw exception if length sent in column metadata is smaller than actual data
                    if (destPrecision != MAX && ((String) value).length() > destPrecision) {
                        MessageFormat form = new MessageFormat(
                                MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidPSLength"));
                        Object[] msgArgs = {value, destJdbcType, destPrecision, destScale};
                        throw new MicrosoftDataEncryptionException(form.format(msgArgs));
                    }
                    return ((String) value).getBytes(UTF_8);

                case NCHAR:
                case NVARCHAR:
                case LONGNVARCHAR:
                    // Throw exception if length sent in column metadata is smaller than actual data
                    if (destPrecision != MAX && ((String) value).length() > destPrecision) {
                        MessageFormat form = new MessageFormat(
                                MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidPSLength"));
                        Object[] msgArgs = {value, destJdbcType, destPrecision, destScale};
                        throw new MicrosoftDataEncryptionException(form.format(msgArgs));
                    }
                    return ((String) value).getBytes(UTF_16LE);

                case REAL:
                    Float floatValue = (value instanceof String) ? Float.parseFloat((String) value) : (Float) value;
                    return ByteBuffer.allocate((Float.SIZE / Byte.SIZE)).order(ByteOrder.LITTLE_ENDIAN)
                            .putFloat(floatValue).array();

                case FLOAT:
                case DOUBLE:
                    Double doubleValue = (value instanceof String) ? Double.parseDouble((String) value)
                                                                   : (Double) value;
                    return ByteBuffer.allocate((Double.SIZE / Byte.SIZE)).order(ByteOrder.LITTLE_ENDIAN)
                            .putDouble(doubleValue).array();

                case NUMERIC:
                case DECIMAL:
                    srcDataScale = ((BigDecimal) value).scale();
                    srcDataPrecision = ((BigDecimal) value).precision();
                    BigDecimal bigDataValue = (BigDecimal) value;
                    if (destPrecision != MAX && (srcDataPrecision > destPrecision) || (srcDataScale > destScale)) {
                        MessageFormat form = new MessageFormat(
                                MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidPSLength"));
                        Object[] msgArgs = {value, destJdbcType, destPrecision, destScale};
                        throw new MicrosoftDataEncryptionException(form.format(msgArgs));
                    } else if (srcDataScale < destScale)
                        // update the scale of source data based on the metadata for scale sent early
                        bigDataValue = bigDataValue.setScale(destScale);

                    byteValue = convertBigDecimalToBytes(bigDataValue, bigDataValue.scale());
                    byte[] decimalbyteValue = new byte[16];
                    // removing the precision and scale information from the decimalToByte array
                    System.arraycopy(byteValue, 2, decimalbyteValue, 0, byteValue.length - 2);
                    return decimalbyteValue;

                case SMALLMONEY:
                case MONEY:
                    // For TDS we need to send the money value multiplied by 10^4 - this gives us the
                    // money value as integer. 4 is the default and only scale available with money.
                    // smallmoney is noralized to money.
                    BigDecimal bdValue = (BigDecimal) value;
                    // Need to validate range in the client side as we are converting BigDecimal to integers.
                    validateMoneyRange(bdValue, destJdbcType);

                    // Get the total number of digits after the multiplication. Scale is hardcoded to 4. This is needed
                    // to get the proper rounding.
                    int digitCount = (bdValue.precision() - bdValue.scale()) + 4;

                    long moneyVal = ((BigDecimal) value).multiply(new BigDecimal(10000),
                            new java.math.MathContext(digitCount, java.math.RoundingMode.HALF_UP)).longValue();
                    ByteBuffer bbuf = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
                    bbuf.putInt((int) (moneyVal >> 32)).array();
                    bbuf.putInt((int) moneyVal).array();
                    return bbuf.array();

                case DATE:
                    calendar = new GregorianCalendar(java.util.TimeZone.getDefault(), java.util.Locale.US);
                    calendar.setLenient(true);
                    calendar.clear();
                    calendar.setTimeInMillis(((java.sql.Date) value).getTime());
                    return getNormalizedTemporal(calendar, 0, // subsecond nanos (none for a date value)
                            0, // scale (dates are not scaled)
                            SSType.DATE, (short) 0);

                case TIME:
                    calendar = new GregorianCalendar(java.util.TimeZone.getDefault(), java.util.Locale.US);
                    calendar.setLenient(true);
                    calendar.clear();
                    if (value instanceof java.sql.Timestamp) {
                        utcMillis = ((java.sql.Timestamp) value).getTime();
                    } else {
                        // java.sql.time
                        utcMillis = ((java.sql.Time) value).getTime();
                    }
                    calendar.setTimeInMillis(utcMillis);
                    int subSecondNanos;
                    if (value instanceof java.sql.Timestamp) {
                        subSecondNanos = ((java.sql.Timestamp) value).getNanos();
                    } else {
                        subSecondNanos = Nanos.PER_MILLISECOND * (int) (utcMillis % 1000);
                        if (subSecondNanos < 0)
                            subSecondNanos += Nanos.PER_SECOND;
                    }
                    return getNormalizedTemporal(calendar, subSecondNanos, destScale, SSType.TIME, (short) 0);

                case TIMESTAMP:
                    calendar = new GregorianCalendar(java.util.TimeZone.getDefault(), java.util.Locale.US);
                    calendar.setLenient(true);
                    calendar.clear();
                    utcMillis = ((java.sql.Timestamp) value).getTime();
                    calendar.setTimeInMillis(utcMillis);
                    subSecondNanos = ((java.sql.Timestamp) value).getNanos();
                    return getNormalizedTemporal(calendar, subSecondNanos, destScale, SSType.DATETIME2, (short) 0);

                case DATETIME:
                case SMALLDATETIME:
                    calendar = new GregorianCalendar(java.util.TimeZone.getDefault(), java.util.Locale.US);
                    calendar.setLenient(true);
                    calendar.clear();
                    utcMillis = ((java.sql.Timestamp) value).getTime();
                    calendar.setTimeInMillis(utcMillis);
                    subSecondNanos = ((java.sql.Timestamp) value).getNanos();
                    return getNormalizedDateTimeAsBytes(calendar, subSecondNanos, destJdbcType);

                case DATETIMEOFFSET:
                    DateTimeOffset dtoValue = (DateTimeOffset) value;
                    utcMillis = dtoValue.getTimestamp().getTime();
                    subSecondNanos = dtoValue.getTimestamp().getNanos();
                    int minutesOffset = dtoValue.getMinutesOffset();
                    calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
                    calendar.setLenient(true);
                    calendar.clear();
                    calendar.setTimeInMillis(utcMillis);
                    return getNormalizedTemporal(calendar, subSecondNanos, destScale, SSType.DATETIMEOFFSET,
                            (short) minutesOffset);
                default:
                    MessageFormat form = new MessageFormat(
                            MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidType"));
                    Object[] msgArgs = {destJdbcType};
                    throw new MicrosoftDataEncryptionException(form.format(msgArgs));
            }
        }
        // we don't want to throw R_errorConvertingValue error as it might expose decrypted data if source was encrypted
        catch (NumberFormatException ex) {
            MessageFormat form = new MessageFormat(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidValueToType"));
            Object[] msgArgs = {destJdbcType};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        } catch (IllegalArgumentException ex) {
            MessageFormat form = new MessageFormat(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidValueToType"));
            Object[] msgArgs = {destJdbcType};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        } catch (ClassCastException ex) {
            MessageFormat form = new MessageFormat(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidValueToType"));
            Object[] msgArgs = {destJdbcType};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }
    }

    public static Object denormalizedValue(byte[] decryptedValue, JDBCType jdbcType, SSType ssType, int precision,
                                           int scale, Calendar cal, String codepage) throws MicrosoftDataEncryptionException {
        Charset charset = Charset.forName(codepage);
        switch (ssType) {
            case CHAR:
            case VARCHAR:
            case NCHAR:
            case NVARCHAR:
            case VARCHARMAX:
            case NVARCHARMAX: {
                try {
                    String strVal = new String(decryptedValue, 0, decryptedValue.length, charset);
                    if ((SSType.CHAR == ssType) || (SSType.NCHAR == ssType)) {
                        // Right pad the string for CHAR types.
                        StringBuilder sb = new StringBuilder(strVal);
                        int padLength = precision - strVal.length();
                        for (int i = 0; i < padLength; i++) {
                            sb.append(' ');
                        }
                        strVal = sb.toString();
                    }
                    return convertStringToObject(strVal, jdbcType);
                } catch (IllegalArgumentException e) {
                    MessageFormat form = new MessageFormat(
                            MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidDataType"));
                    Object[] msgArgs = {ssType};
                    throw new MicrosoftDataEncryptionException(form.format(msgArgs));
                } catch (UnsupportedEncodingException e) {
                    MessageFormat form = new MessageFormat(
                            MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidEncoding"));
                    Object[] msgArgs = {charset};
                    throw new MicrosoftDataEncryptionException(form.format(msgArgs));
                }
            }

            default:
                MessageFormat form = new MessageFormat(
                        MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidDataType"));
                Object[] msgArgs = {ssType};
                throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }
    }

    public static Object denormalizedValue(byte[] decryptedValue, JDBCType jdbcType, SSType ssType, int precision,
                                           int scale, Calendar cal) throws MicrosoftDataEncryptionException {
        Charset charset = null;
        switch (ssType) {
            case CHAR:
            case VARCHAR:
            case NCHAR:
            case NVARCHAR:
            case VARCHARMAX:
            case NVARCHARMAX: {
                try {
                    switch (ssType) {
                        case CHAR:
                        case VARCHAR:
                        case VARCHARMAX:
                            charset = UTF_8;
                            break;
                        case NCHAR:
                        case NVARCHAR:
                        case NVARCHARMAX:
                            charset = UTF_16LE;
                            break;
                        default:
                            // impossible to come here
                            MessageFormat form = new MessageFormat(
                                    MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidDataType"));
                            Object[] msgArgs = {ssType};
                            throw new MicrosoftDataEncryptionException(form.format(msgArgs));
                    }
                    String strVal = new String(decryptedValue, 0, decryptedValue.length, charset);
                    if ((SSType.CHAR == ssType) || (SSType.NCHAR == ssType)) {
                        // Right pad the string for CHAR types.
                        StringBuilder sb = new StringBuilder(strVal);
                        int padLength = precision - strVal.length();
                        for (int i = 0; i < padLength; i++) {
                            sb.append(' ');
                        }
                        strVal = sb.toString();
                    }
                    return convertStringToObject(strVal, jdbcType);
                } catch (IllegalArgumentException e) {
                    MessageFormat form = new MessageFormat(
                            MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidDataType"));
                    Object[] msgArgs = {ssType};
                    throw new MicrosoftDataEncryptionException(form.format(msgArgs));
                } catch (UnsupportedEncodingException e) {
                    MessageFormat form = new MessageFormat(
                            MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidEncoding"));
                    Object[] msgArgs = {charset};
                    throw new MicrosoftDataEncryptionException(form.format(msgArgs));
                }
            }

            case BIT:
            case TINYINT:
            case SMALLINT:
            case INTEGER:
            case BIGINT: {
                // If data is encrypted, then these types are normalized to BIGINT. Need to denormalize here.
                if (8 != decryptedValue.length) {
                    // Integer datatypes are normalized to bigint for AE.
                    MessageFormat form = new MessageFormat(
                            MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidDataType"));
                    Object[] msgArgs = {ssType};
                    throw new MicrosoftDataEncryptionException(form.format(msgArgs));
                }
                return convertLongToObject(readLong(decryptedValue, 0), jdbcType, ssType);

            }

            case REAL:
            case FLOAT: {
                // JDBC driver does not normalize real to float.
                if (8 == decryptedValue.length) {
                    return convertDoubleToObject(
                            ByteBuffer.wrap(decryptedValue).order(ByteOrder.LITTLE_ENDIAN).getDouble(),
                            JDBCType.VARBINARY == jdbcType ? ssType.getJDBCType() : jdbcType);
                } else if (4 == decryptedValue.length) {
                    return convertFloatToObject(
                            ByteBuffer.wrap(decryptedValue).order(ByteOrder.LITTLE_ENDIAN).getFloat(),
                            JDBCType.VARBINARY == jdbcType ? ssType.getJDBCType() : jdbcType);
                } else {
                    MessageFormat form = new MessageFormat(
                            MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidDataType"));
                    Object[] msgArgs = {ssType};
                    throw new MicrosoftDataEncryptionException(form.format(msgArgs));
                }
            }
            case SMALLMONEY: {
                return convertMoneyToObject(new BigDecimal(BigInteger.valueOf(readInt(decryptedValue, 4)), 4),
                        JDBCType.VARBINARY == jdbcType ? ssType.getJDBCType() : jdbcType, 4);
            }

            case MONEY: {
                BigInteger bi = BigInteger.valueOf(
                        ((long) readInt(decryptedValue, 0) << 32) | (readInt(decryptedValue, 4) & 0xFFFFFFFFL));

                return convertMoneyToObject(new BigDecimal(bi, 4),
                        JDBCType.VARBINARY == jdbcType ? ssType.getJDBCType() : jdbcType, 8);
            }

            case NUMERIC:
            case DECIMAL: {
                return convertBigDecimalToObject(readBigDecimal(decryptedValue, decryptedValue.length, scale),
                        JDBCType.VARBINARY == jdbcType ? ssType.getJDBCType() : jdbcType);
            }

            case BINARY:
            case VARBINARY:
            case VARBINARYMAX: {
                return convertBytesToObject(decryptedValue, jdbcType, ssType, precision);
            }

            case DATE: {
                // get the number of days !! Size should be 3
                if (3 != decryptedValue.length) {
                    MessageFormat form = new MessageFormat(
                            MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidDataType"));
                    Object[] msgArgs = {ssType};
                    throw new MicrosoftDataEncryptionException(form.format(msgArgs));
                }

                // Getting number of days
                // Following Lines of code copied from IOBuffer.readDaysIntoCE as we
                // cannot reuse method
                int daysIntoCE = getDaysIntoCE(decryptedValue, ssType);

                return convertTemporalToObject(jdbcType, ssType, cal, daysIntoCE, 0, 0);

            }

            case TIME: {
                long localNanosSinceMidnight = readNanosSinceMidnightAE(decryptedValue, scale, ssType);

                return convertTemporalToObject(jdbcType, SSType.TIME, cal, 0, localNanosSinceMidnight, scale);
            }

            case DATETIME2: {
                if (8 != decryptedValue.length) {
                    MessageFormat form = new MessageFormat(
                            MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidDataType"));
                    Object[] msgArgs = {ssType};
                    throw new MicrosoftDataEncryptionException(form.format(msgArgs));
                }

                // Last three bytes are for date and remaining for time
                int dateOffset = decryptedValue.length - 3;
                byte[] timePortion = new byte[dateOffset];
                byte[] datePortion = new byte[3];
                System.arraycopy(decryptedValue, 0, timePortion, 0, dateOffset);
                System.arraycopy(decryptedValue, dateOffset, datePortion, 0, 3);
                long localNanosSinceMidnight = readNanosSinceMidnightAE(timePortion, scale, ssType);

                int daysIntoCE = getDaysIntoCE(datePortion, ssType);

                // Convert the DATETIME2 value to the desired Java type.
                return convertTemporalToObject(jdbcType, SSType.DATETIME2, cal, daysIntoCE, localNanosSinceMidnight,
                        scale);

            }

            case SMALLDATETIME: {
                if (4 != decryptedValue.length) {
                    MessageFormat form = new MessageFormat(
                            MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidDataType"));
                    Object[] msgArgs = {ssType};
                    throw new MicrosoftDataEncryptionException(form.format(msgArgs));
                }

                // SQL smalldatetime has less precision. It stores 2 bytes
                // for the days since SQL Base Date and 2 bytes for minutes
                // after midnight.
                return convertTemporalToObject(jdbcType, SSType.DATETIME, cal, readUnsignedShort(decryptedValue, 0),
                        readUnsignedShort(decryptedValue, 2) * 60L * 1000L, 0);
            }

            case DATETIME: {
                int ticksSinceMidnight = (readInt(decryptedValue, 4) * 10 + 1) / 3;

                if (8 != decryptedValue.length || Integer.MAX_VALUE < ticksSinceMidnight) {
                    MessageFormat form = new MessageFormat(
                            MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidDataType"));
                    Object[] msgArgs = {ssType};
                    throw new MicrosoftDataEncryptionException(form.format(msgArgs));
                }

                // SQL datetime is 4 bytes for days since SQL Base Date
                // (January 1, 1900 00:00:00 GMT) and 4 bytes for
                // the number of three hundredths (1/300) of a second since midnight.
                return convertTemporalToObject(jdbcType, SSType.DATETIME, cal, readInt(decryptedValue, 0),
                        ticksSinceMidnight, 0);
            }

            case DATETIMEOFFSET: {
                // Last 5 bytes are for date and offset
                int dateOffset = decryptedValue.length - 5;
                byte[] timePortion = new byte[dateOffset];
                byte[] datePortion = new byte[3];
                byte[] offsetPortion = new byte[2];
                System.arraycopy(decryptedValue, 0, timePortion, 0, dateOffset);
                System.arraycopy(decryptedValue, dateOffset, datePortion, 0, 3);
                System.arraycopy(decryptedValue, dateOffset + 3, offsetPortion, 0, 2);
                long localNanosSinceMidnight = readNanosSinceMidnightAE(timePortion, scale, ssType);

                int daysIntoCE = getDaysIntoCE(datePortion, ssType);

                int localMinutesOffset = ByteBuffer.wrap(offsetPortion).order(ByteOrder.LITTLE_ENDIAN).getShort();

                return convertTemporalToObject(jdbcType, SSType.DATETIMEOFFSET,
                        new GregorianCalendar(new SimpleTimeZone(localMinutesOffset * 60 * 1000, ""), Locale.US),
                        daysIntoCE, localNanosSinceMidnight, scale);

            }

            case GUID: {
                return readGUID(decryptedValue);
            }

            default:
                MessageFormat form = new MessageFormat(
                        MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidDataType"));
                Object[] msgArgs = {ssType};
                throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }
    }

    /**
     * Convert an Integer object to desired target user type.
     *
     * @param intValue
     *        the value to convert.
     * @param valueLength
     *        the value to convert.
     * @param jdbcType
     *        the jdbc type required.
     * @param streamType
     *        the type of stream required.
     * @return the required object.
     */
    static final Object convertIntegerToObject(int intValue, int valueLength, JDBCType jdbcType) {
        switch (jdbcType) {
            case INTEGER:
                return intValue;
            case SMALLINT: // 2.21 small and tinyint returned as short
            case TINYINT:
                return (short) intValue;
            case BIT:
            case BOOLEAN:
                return 0 != intValue;
            case BIGINT:
                return (long) intValue;
            case DECIMAL:
            case NUMERIC:
            case MONEY:
            case SMALLMONEY:
                return new BigDecimal(Integer.toString(intValue));
            case FLOAT:
            case DOUBLE:
                return (double) intValue;
            case REAL:
                return (float) intValue;
            case BINARY:
                return convertIntToBytes(intValue, valueLength);
            case SQL_VARIANT:
                // return short or bit if the underlying datatype of sql_variant is tinyint, smallint or bit
                // otherwise, return integer
                // Longer datatypes such as double and float are handled by convertLongToObject instead.
                if (valueLength == 1) {
                    return 0 != intValue;
                } else if (valueLength == 3 || valueLength == 4) {
                    return (short) intValue;
                } else {
                    return intValue;
                }
            default:
                return Integer.toString(intValue);
        }
    }

    /**
     * Convert a Long object to desired target user type.
     *
     * @param longVal
     *        the value to convert.
     * @param jdbcType
     *        the jdbc type required.
     * @param baseSSType
     *        the base SQLServer type.
     * @param streamType
     *        the stream type.
     * @return the required object.
     */
    static final Object convertLongToObject(long longVal, JDBCType jdbcType, SSType baseSSType) {
        switch (jdbcType) {
            case BIGINT:
            case SQL_VARIANT:
                return longVal;
            case INTEGER:
                return (int) longVal;
            case SMALLINT: // small and tinyint returned as short
            case TINYINT:
                return (short) longVal;
            case BIT:
            case BOOLEAN:
                return 0 != longVal;
            case DECIMAL:
            case NUMERIC:
            case MONEY:
            case SMALLMONEY:
                return new BigDecimal(Long.toString(longVal));
            case FLOAT:
            case DOUBLE:
                return (double) longVal;
            case REAL:
                return (float) longVal;
            case BINARY:
                byte[] convertedBytes = convertLongToBytes(longVal);
                int bytesToReturnLength;
                byte[] bytesToReturn;

                switch (baseSSType) {
                    case BIT:
                    case TINYINT:
                        bytesToReturnLength = 1;
                        bytesToReturn = new byte[bytesToReturnLength];
                        System.arraycopy(convertedBytes, convertedBytes.length - bytesToReturnLength, bytesToReturn, 0,
                                bytesToReturnLength);
                        return bytesToReturn;
                    case SMALLINT:
                        bytesToReturnLength = 2;
                        bytesToReturn = new byte[bytesToReturnLength];
                        System.arraycopy(convertedBytes, convertedBytes.length - bytesToReturnLength, bytesToReturn, 0,
                                bytesToReturnLength);
                        return bytesToReturn;
                    case INTEGER:
                        bytesToReturnLength = 4;
                        bytesToReturn = new byte[bytesToReturnLength];
                        System.arraycopy(convertedBytes, convertedBytes.length - bytesToReturnLength, bytesToReturn, 0,
                                bytesToReturnLength);
                        return bytesToReturn;
                    case BIGINT:
                        bytesToReturnLength = 8;
                        bytesToReturn = new byte[bytesToReturnLength];
                        System.arraycopy(convertedBytes, convertedBytes.length - bytesToReturnLength, bytesToReturn, 0,
                                bytesToReturnLength);
                        return bytesToReturn;
                    default:
                        return convertedBytes;
                }

            case VARBINARY:
                switch (baseSSType) {
                    case BIGINT:
                        return longVal;
                    case INTEGER:
                        return (int) longVal;
                    case SMALLINT: // small and tinyint returned as short
                    case TINYINT:
                        return (short) longVal;
                    case BIT:
                        return 0 != longVal;
                    case DECIMAL:
                    case NUMERIC:
                    case MONEY:
                    case SMALLMONEY:
                        return new BigDecimal(Long.toString(longVal));
                    case FLOAT:
                        return (double) longVal;
                    case REAL:
                        return (float) longVal;
                    case BINARY:
                        return convertLongToBytes(longVal);
                    default:
                        return Long.toString(longVal);
                }
            default:
                return Long.toString(longVal);
        }
    }

    /**
     * Encodes an integer value to a byte array in big-endian order.
     *
     * @param intValue
     *        the integer value to encode.
     * @param valueLength
     *        the number of bytes to encode.
     * @return the byte array containing the big-endian encoded value.
     */
    static final byte[] convertIntToBytes(int intValue, int valueLength) {
        byte bytes[] = new byte[valueLength];
        for (int i = valueLength; i-- > 0;) {
            bytes[i] = (byte) (intValue & 0xFF);
            intValue >>= 8;
        }
        return bytes;
    }

    /**
     * Convert a Float object to desired target user type.
     *
     * @param floatVal
     *        the value to convert.
     * @param jdbcType
     *        the jdbc type required.
     * @param streamType
     *        the stream type.
     * @return the required object.
     */
    static final Object convertFloatToObject(float floatVal, JDBCType jdbcType) {
        switch (jdbcType) {
            case REAL:
            case SQL_VARIANT:
                return floatVal;
            case INTEGER:
                return (int) floatVal;
            case SMALLINT: // small and tinyint returned as short
            case TINYINT:
                return (short) floatVal;
            case BIT:
            case BOOLEAN:
                return 0 != Float.compare(0.0f, floatVal);
            case BIGINT:
                return (long) floatVal;
            case DECIMAL:
            case NUMERIC:
            case MONEY:
            case SMALLMONEY:
                return new BigDecimal(Float.toString(floatVal));
            case FLOAT:
            case DOUBLE:
                return (Float.valueOf(floatVal)).doubleValue();
            case BINARY:
                return convertIntToBytes(Float.floatToRawIntBits(floatVal), 4);
            default:
                return Float.toString(floatVal);
        }
    }

    /**
     * Encodes a long value to a byte array in big-endian order.
     *
     * @param longValue
     *        the long value to encode.
     * @return the byte array containing the big-endian encoded value.
     */
    static final byte[] convertLongToBytes(long longValue) {
        byte bytes[] = new byte[8];
        for (int i = 8; i-- > 0;) {
            bytes[i] = (byte) (longValue & 0xFF);
            longValue >>= 8;
        }
        return bytes;
    }

    /**
     * Convert a Double object to desired target user type.
     *
     * @param doubleVal
     *        the value to convert.
     * @param jdbcType
     *        the jdbc type required.
     * @param streamType
     *        the stream type.
     * @return the required object.
     */
    static final Object convertDoubleToObject(double doubleVal, JDBCType jdbcType) {
        switch (jdbcType) {
            case FLOAT:
            case DOUBLE:
            case SQL_VARIANT:
                return doubleVal;
            case REAL:
                return (Double.valueOf(doubleVal)).floatValue();
            case INTEGER:
                return (int) doubleVal;
            case SMALLINT: // small and tinyint returned as short
            case TINYINT:
                return (short) doubleVal;
            case BIT:
            case BOOLEAN:
                return 0 != Double.compare(0.0d, doubleVal);
            case BIGINT:
                return (long) doubleVal;
            case DECIMAL:
            case NUMERIC:
            case MONEY:
            case SMALLMONEY:
                return new BigDecimal(Double.toString(doubleVal));
            case BINARY:
                return convertLongToBytes(Double.doubleToRawLongBits(doubleVal));
            default:
                return Double.toString(doubleVal);
        }
    }

    static final byte[] convertBigDecimalToBytes(BigDecimal bigDecimalVal, int scale) {
        byte[] valueBytes;

        if (bigDecimalVal == null) {
            valueBytes = new byte[2];
            valueBytes[0] = (byte) scale;
            valueBytes[1] = 0; // data length
        } else {
            boolean isNegative = (bigDecimalVal.signum() < 0);

            // NOTE: Handle negative scale as a special case for JDK 1.5 and later VMs.
            if (bigDecimalVal.scale() < 0)
                bigDecimalVal = bigDecimalVal.setScale(0);

            BigInteger bi = bigDecimalVal.unscaledValue();

            if (isNegative)
                bi = bi.negate();

            byte[] unscaledBytes = bi.toByteArray();

            valueBytes = new byte[unscaledBytes.length + 3];
            int j = 0;
            valueBytes[j++] = (byte) bigDecimalVal.scale();
            valueBytes[j++] = (byte) (unscaledBytes.length + 1); // data length + sign
            valueBytes[j++] = (byte) (isNegative ? 0 : 1); // 1 = +ve, 0 = -ve
            for (int i = unscaledBytes.length - 1; i >= 0; i--)
                valueBytes[j++] = unscaledBytes[i];
        }

        return valueBytes;
    }

    static final byte[] convertMoneyToBytes(BigDecimal bigDecimalVal, int bLength) {
        byte[] valueBytes = new byte[bLength];

        BigInteger bi = bigDecimalVal.unscaledValue();

        if (bLength == 8) {
            // money
            byte[] longbArray = new byte[bLength];
            writeLong(bi.longValue(), longbArray, 0);
            /*
             * TDS 2.2.5.5.1.4 Fixed-Point Numbers Money is represented as a 8 byte signed integer, with one 4-byte
             * integer that represents the more significant half, and one 4-byte integer that represents the less
             * significant half.
             */
            System.arraycopy(longbArray, 0, valueBytes, 4, 4);
            System.arraycopy(longbArray, 4, valueBytes, 0, 4);
        } else {
            // smallmoney
            writeInt(bi.intValue(), valueBytes, 0);
        }

        return valueBytes;
    }

    /**
     * Convert a BigDecimal object to desired target user type.
     *
     * @param bigDecimalVal
     *        the value to convert.
     * @param jdbcType
     *        the jdbc type required.
     * @param streamType
     *        the stream type.
     * @return the required object.
     */
    static final Object convertBigDecimalToObject(BigDecimal bigDecimalVal, JDBCType jdbcType) {
        switch (jdbcType) {
            case DECIMAL:
            case NUMERIC:
            case MONEY:
            case SMALLMONEY:
            case SQL_VARIANT:
                return bigDecimalVal;
            case FLOAT:
            case DOUBLE:
                return bigDecimalVal.doubleValue();
            case REAL:
                return bigDecimalVal.floatValue();
            case INTEGER:
                return bigDecimalVal.intValue();
            case SMALLINT: // small and tinyint returned as short
            case TINYINT:
                return bigDecimalVal.shortValue();
            case BIT:
            case BOOLEAN:
                return 0 != bigDecimalVal.compareTo(BigDecimal.valueOf(0));
            case BIGINT:
                return bigDecimalVal.longValue();
            case BINARY:
                return convertBigDecimalToBytes(bigDecimalVal, bigDecimalVal.scale());
            default:
                return bigDecimalVal.toString();
        }
    }

    /**
     * Convert a Money object to desired target user type.
     *
     * @param bigDecimalVal
     *        the value to convert.
     * @param jdbcType
     *        the jdbc type required.
     * @param streamType
     *        the stream type.
     * @param numberOfBytes
     *        the number of bytes to convert
     * @return the required object.
     */
    static final Object convertMoneyToObject(BigDecimal bigDecimalVal, JDBCType jdbcType, int numberOfBytes) {
        switch (jdbcType) {
            case DECIMAL:
            case NUMERIC:
            case MONEY:
            case SMALLMONEY:
                return bigDecimalVal;
            case FLOAT:
            case DOUBLE:
                return bigDecimalVal.doubleValue();
            case REAL:
                return bigDecimalVal.floatValue();
            case INTEGER:
                return bigDecimalVal.intValue();
            case SMALLINT: // small and tinyint returned as short
            case TINYINT:
                return bigDecimalVal.shortValue();
            case BIT:
            case BOOLEAN:
                return 0 != bigDecimalVal.compareTo(BigDecimal.valueOf(0));
            case BIGINT:
                return bigDecimalVal.longValue();
            case BINARY:
                return convertToBytes(bigDecimalVal, bigDecimalVal.scale(), numberOfBytes);
            default:
                return bigDecimalVal.toString();
        }
    }

    // converts big decimal to money and smallmoney
    private static byte[] convertToBytes(BigDecimal value, int scale, int numBytes) {
        boolean isNeg = value.signum() < 0;

        value = value.setScale(scale);

        BigInteger bigInt = value.unscaledValue();

        byte[] unscaledBytes = bigInt.toByteArray();

        byte[] ret = new byte[numBytes];
        if (unscaledBytes.length < numBytes) {
            for (int i = 0; i < numBytes - unscaledBytes.length; ++i) {
                ret[i] = (byte) (isNeg ? -1 : 0);
            }
        }
        int offset = numBytes - unscaledBytes.length;
        System.arraycopy(unscaledBytes, 0, ret, offset, numBytes - offset);
        return ret;
    }

    /**
     * Convert a byte array to desired target user type.
     *
     * @param bytesValue
     *        the value to convert.
     * @param jdbcType
     *        the jdbc type required.
     * @param baseTypeInfo
     *        the type information associated with bytesValue.
     * @return the required object.
     * @throws MicrosoftDataEncryptionException
     *         when an error occurs.
     */
    static final Object convertBytesToObject(byte[] bytesValue, JDBCType jdbcType, SSType ssType,
                                             int precision) throws MicrosoftDataEncryptionException {
        switch (jdbcType) {
            case CHAR:
                String str = CryptographyExtensions.toHexString(bytesValue);

                if ((SSType.BINARY == ssType) && (str.length() < (precision * 2))) {

                    StringBuilder strbuf = new StringBuilder(str);

                    while (strbuf.length() < (precision * 2)) {
                        strbuf.append('0');
                    }
                    return strbuf.toString();
                }
                return str;

            case BINARY:
            case VARBINARY:
            case LONGVARBINARY:
                if ((SSType.BINARY == ssType) && (bytesValue.length < precision)) {

                    byte[] newBytes = new byte[precision];
                    System.arraycopy(bytesValue, 0, newBytes, 0, bytesValue.length);
                    return newBytes;
                }

                return bytesValue;

            default:
                MessageFormat form = new MessageFormat("The conversion from {0} to {1} is unsupported.");
                Object[] msgArgs = {jdbcType, ssType};
                throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }
    }

    /**
     * Convert a String object to desired target user type.
     *
     * @param stringVal
     *        the value to convert.
     * @param charset
     *        the character set.
     * @param jdbcType
     *        the jdbc type required.
     * @return the required object.
     */
    static final Object convertStringToObject(String stringVal,
                                              JDBCType jdbcType) throws UnsupportedEncodingException, IllegalArgumentException {
        switch (jdbcType) {
            // Convert String to Numeric types.
            case DECIMAL:
            case NUMERIC:
            case MONEY:
            case SMALLMONEY:
                return new BigDecimal(stringVal.trim());
            case FLOAT:
            case DOUBLE:
                return Double.valueOf(stringVal.trim());
            case REAL:
                return Float.valueOf(stringVal.trim());
            case INTEGER:
                return Integer.valueOf(stringVal.trim());
            case SMALLINT: // small and tinyint returned as short
            case TINYINT:
                return Short.valueOf(stringVal.trim());
            case BIT:
            case BOOLEAN:
                String trimmedString = stringVal.trim();
                return (1 == trimmedString.length()) ? Boolean.valueOf('1' == trimmedString.charAt(0))
                                                     : Boolean.valueOf(trimmedString);
            case BIGINT:
                return Long.valueOf(stringVal.trim());

            // Convert String to Temporal types.
            case TIMESTAMP:
                return java.sql.Timestamp.valueOf(stringVal.trim());
            case LOCALDATETIME:
                return parseStringIntoLDT(stringVal.trim());
            case DATE:
                return java.sql.Date.valueOf(getDatePart(stringVal.trim()));
            case TIME: {
                // Accepted character formats for conversion to java.sql.Time are:
                // hh:mm:ss[.nnnnnnnnn]
                // YYYY-MM-DD hh:mm:ss[.nnnnnnnnn]
                //
                // To handle either of these formats:
                // 1) Normalize and parse as a Timestamp
                // 2) Round fractional seconds up to the nearest millisecond (max resolution of java.sql.Time)
                // 3) Renormalize (as rounding may have changed the date) to a java.sql.Time
                java.sql.Timestamp ts = java.sql.Timestamp
                        .valueOf(BASE_DATE_1970 + " " + getTimePart(stringVal.trim()));
                GregorianCalendar cal = new GregorianCalendar(Locale.US);
                cal.clear();
                cal.setTimeInMillis(ts.getTime());
                if (ts.getNanos() % Nanos.PER_MILLISECOND >= Nanos.PER_MILLISECOND / 2)
                    cal.add(Calendar.MILLISECOND, 1);
                cal.set(BASE_YEAR_1970, Calendar.JANUARY, 1);
                return new java.sql.Time(cal.getTimeInMillis());
            }

            case BINARY:
                return stringVal.getBytes();

            default:
                return stringVal;
        }
    }

    /**
     * Taken from java.sql.Timestamp implementation
     *
     * @param s
     *        String to be parsed
     * @return LocalDateTime
     */
    private static LocalDateTime parseStringIntoLDT(String s) {
        final int YEAR_LENGTH = 4;
        final int MONTH_LENGTH = 2;
        final int DAY_LENGTH = 2;
        final int MAX_MONTH = 12;
        final int MAX_DAY = 31;
        int year = 0;
        int month = 0;
        int day = 0;
        int hour;
        int minute;
        int second;
        int a_nanos = 0;
        int firstDash;
        int secondDash;
        int dividingSpace;
        int firstColon;
        int secondColon;
        int period;
        String formatError = MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidTimestampFormat");

        if (s == null)
            throw new java.lang.IllegalArgumentException(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_NullString"));

        // Split the string into date and time components
        s = s.trim();
        dividingSpace = s.indexOf(' ');
        if (dividingSpace < 0) {
            throw new java.lang.IllegalArgumentException(formatError);
        }

        // Parse the date
        firstDash = s.indexOf('-');
        secondDash = s.indexOf('-', firstDash + 1);

        // Parse the time
        firstColon = s.indexOf(':', dividingSpace + 1);
        secondColon = s.indexOf(':', firstColon + 1);
        period = s.indexOf('.', secondColon + 1);

        // Convert the date
        boolean parsedDate = false;
        if (firstDash > 0 && secondDash > 0 && secondDash < dividingSpace - 1) {
            if (firstDash == YEAR_LENGTH && (secondDash - firstDash > 1 && secondDash - firstDash <= MONTH_LENGTH + 1)
                    && (dividingSpace - secondDash > 1 && dividingSpace - secondDash <= DAY_LENGTH + 1)) {
                year = Integer.parseInt(s.substring(0, firstDash));
                month = Integer.parseInt(s.substring(firstDash + 1, secondDash));
                day = Integer.parseInt(s.substring(secondDash + 1, dividingSpace));

                if ((month >= 1 && month <= MAX_MONTH) && (day >= 1 && day <= MAX_DAY)) {
                    parsedDate = true;
                }
            }
        }
        if (!parsedDate) {
            throw new java.lang.IllegalArgumentException(formatError);
        }

        // Convert the time; default missing nanos
        int len = s.length();
        if (firstColon > 0 && secondColon > 0 && secondColon < len - 1) {
            hour = Integer.parseInt(s.substring(dividingSpace + 1, firstColon));
            minute = Integer.parseInt(s.substring(firstColon + 1, secondColon));
            if (period > 0 && period < len - 1) {
                second = Integer.parseInt(s.substring(secondColon + 1, period));
                int nanoPrecision = len - (period + 1);
                if (nanoPrecision > 9)
                    throw new java.lang.IllegalArgumentException(formatError);
                if (!Character.isDigit(s.charAt(period + 1)))
                    throw new java.lang.IllegalArgumentException(formatError);
                int tmpNanos = Integer.parseInt(s.substring(period + 1, len));
                while (nanoPrecision < 9) {
                    tmpNanos *= 10;
                    nanoPrecision++;
                }
                a_nanos = tmpNanos;
            } else if (period > 0) {
                throw new java.lang.IllegalArgumentException(formatError);
            } else {
                second = Integer.parseInt(s.substring(secondColon + 1, len));
            }
        } else {
            throw new java.lang.IllegalArgumentException(formatError);
        }
        return LocalDateTime.of(year, month, day, hour, minute, second, a_nanos);
    }

    // Returns date portion of string.
    // Expects one of "<date>" or "<date><space><time>".
    private static String getDatePart(String s) {
        int sp = s.indexOf(' ');
        if (-1 == sp)
            return s;
        return s.substring(0, sp);
    }

    // Returns time portion of string.
    // Expects one of "<time>" or "<date><space><time>".
    private static String getTimePart(String s) {
        int sp = s.indexOf(' ');
        if (-1 == sp)
            return s;
        return s.substring(sp + 1);
    }

    // Formats nanoseconds as a String of the form ".nnnnnnn...." where the number
    // of digits is equal to the scale. Returns the empty string for scale = 0;
    private static String fractionalSecondsString(long subSecondNanos, int scale) {
        assert 0 <= subSecondNanos && subSecondNanos < Nanos.PER_SECOND;
        assert 0 <= scale && scale <= MAX_FRACTIONAL_SECONDS_SCALE;

        // Fast path for 0 scale (avoids creation of two BigDecimal objects and
        // two Strings when the answer is going to be "" anyway...)
        if (0 == scale)
            return "";

        return java.math.BigDecimal.valueOf(subSecondNanos % Nanos.PER_SECOND, 9).setScale(scale).toPlainString()
                .substring(1);
    }

    /**
     * Modified version of TDSWriter.writeEncryptedScaledTemporal that does the same thing as the original except it
     * doesn't encrypt.
     *
     * @param cal
     *        Calendar representing the value to write, except for any sub-second nanoseconds
     * @param subSecondNanos
     *        the sub-second nanoseconds (0 - 999,999,999)
     * @param scale
     *        the scale (in digits: 0 - 7) to use for the sub-second nanos component
     * @param ssType
     *        the SQL Server data type (DATE, TIME, DATETIME2, or DATETIMEOFFSET)
     * @param minutesOffset
     *        the offset value for DATETIMEOFFSET
     * @throws MicrosoftDataEncryptionException
     *         if an I/O error occurs or if the value is not in the valid range
     */
    static byte[] getNormalizedTemporal(GregorianCalendar cal, int subSecondNanos, int scale, SSType ssType,
                                        short minutesOffset) throws MicrosoftDataEncryptionException {
        assert SSType.DATE == ssType || SSType.TIME == ssType || SSType.DATETIME2 == ssType
                || SSType.DATETIMEOFFSET == ssType : "Unexpected SSType: " + ssType;

        // store the time and minutesOffset portion of DATETIME2 and DATETIMEOFFSET to be used with date portion
        byte encodedBytesForEncryption[] = null;

        int secondsSinceMidnight = 0;
        long divisor = 0;
        long scaledNanos = 0;

        // First, for types with a time component, write the scaled nanos since midnight
        if (SSType.TIME == ssType || SSType.DATETIME2 == ssType || SSType.DATETIMEOFFSET == ssType) {
            assert subSecondNanos >= 0;
            assert subSecondNanos < Nanos.PER_SECOND;
            assert scale >= 0;
            assert scale <= MAX_FRACTIONAL_SECONDS_SCALE;

            secondsSinceMidnight = cal.get(Calendar.SECOND) + 60 * cal.get(Calendar.MINUTE)
                    + 60 * 60 * cal.get(Calendar.HOUR_OF_DAY);

            // Scale nanos since midnight to the desired scale, rounding the value as necessary
            divisor = Nanos.PER_MAX_SCALE_INTERVAL * (long) Math.pow(10, MAX_FRACTIONAL_SECONDS_SCALE - scale);

            // The scaledNanos variable represents the fractional seconds of the value at the scale
            // indicated by the scale variable. So, for example, scaledNanos = 3 means 300 nanoseconds
            // at scale TDS.MAX_FRACTIONAL_SECONDS_SCALE, but 3000 nanoseconds at
            // TDS.MAX_FRACTIONAL_SECONDS_SCALE - 1
            scaledNanos = (((long) Nanos.PER_SECOND * secondsSinceMidnight + getRoundedSubSecondNanos(subSecondNanos)
                    + divisor / 2) / divisor) * divisor / 100;

            // for encrypted time value, SQL server cannot do rounding or casting,
            // So, driver needs to cast it before encryption.
            if (SSType.TIME == ssType && 864000000000L <= scaledNanos) {
                scaledNanos = (((long) Nanos.PER_SECOND * secondsSinceMidnight
                        + getRoundedSubSecondNanos(subSecondNanos)) / divisor) * divisor / 100;
            }

            // SQL Server rounding behavior indicates that it always rounds up unless
            // we are at the max value of the type(NOT every day), in which case it truncates.
            // Side effect on Calendar date:
            // If rounding nanos to the specified scale rolls the value to the next day ...
            if (Nanos.PER_DAY / divisor == scaledNanos) {

                // If the type is time, always truncate
                if (SSType.TIME == ssType) {
                    --scaledNanos;
                }
                // If the type is datetime2 or datetimeoffset, truncate only if its the max value supported
                else {
                    assert SSType.DATETIME2 == ssType || SSType.DATETIMEOFFSET == ssType : "Unexpected SSType: "
                            + ssType;

                    // ... then bump the date, provided that the resulting date is still within
                    // the valid date range.
                    //
                    // Extreme edge case (literally, the VERY edge...):
                    // If nanos overflow rolls the date value out of range (that is, we have a value
                    // a few nanoseconds later than 9999-12-31 23:59:59) then truncate the nanos
                    // instead of rolling.
                    //
                    // This case is very likely never hit by "real world" applications, but exists
                    // here as a security measure to ensure that such values don't result in a
                    // connection-closing TDS exception.
                    cal.add(Calendar.SECOND, 1);

                    if (cal.get(Calendar.YEAR) <= 9999) {
                        scaledNanos = 0;
                    } else {
                        cal.add(Calendar.SECOND, -1);
                        --scaledNanos;
                    }
                }
            }

            // Encode the scaled nanos to TDS
            int encodedLength = nanosSinceMidnightLength(MAX_FRACTIONAL_SECONDS_SCALE);
            byte[] encodedBytes = scaledNanosToEncodedBytes(scaledNanos, encodedLength);

            if (SSType.TIME == ssType) {
                return encodedBytes;
            } else if (SSType.DATETIME2 == ssType) {
                // for DATETIME2 sends both date and time part together for encryption
                encodedBytesForEncryption = new byte[encodedLength + 3];
                System.arraycopy(encodedBytes, 0, encodedBytesForEncryption, 0, encodedBytes.length);
            } else if (SSType.DATETIMEOFFSET == ssType) {
                // for DATETIMEOFFSET sends date, time and offset part together for encryption
                encodedBytesForEncryption = new byte[encodedLength + 5];
                System.arraycopy(encodedBytes, 0, encodedBytesForEncryption, 0, encodedBytes.length);
            }
        }

        // Second, for types with a date component, write the days into the Common Era
        if (SSType.DATE == ssType || SSType.DATETIME2 == ssType || SSType.DATETIMEOFFSET == ssType) {
            // Computation of the number of days into the Common Era assumes that
            // the DAY_OF_YEAR field reflects a pure Gregorian calendar - one that
            // uses Gregorian leap year rules across the entire range of dates.
            //
            // For the DAY_OF_YEAR field to accurately reflect pure Gregorian behavior,
            // we need to use a pure Gregorian calendar for dates that are Julian dates
            // under a standard Gregorian calendar and for (Gregorian) dates later than
            // the cutover date in the cutover year.
            if (cal.getTimeInMillis() < GregorianChange.STANDARD_CHANGE_DATE.getTime()
                    || cal.getActualMaximum(Calendar.DAY_OF_YEAR) < DAYS_PER_YEAR) {
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int date = cal.get(Calendar.DATE);

                // Set the cutover as early as possible (pure Gregorian behavior)
                cal.setGregorianChange(GregorianChange.PURE_CHANGE_DATE);

                // Initialize the date field by field (preserving the "wall calendar" value)
                cal.set(year, month, date);
            }

            int daysIntoCE = daysSinceBaseDate(cal.get(Calendar.YEAR), cal.get(Calendar.DAY_OF_YEAR), 1);

            // Last-ditch verification that the value is in the valid range for the
            // DATE/DATETIME2/DATETIMEOFFSET TDS data type (1/1/0001 to 12/31/9999).
            // If it's not, then throw an exception now so that statement execution
            // is safely canceled. Attempting to put an invalid value on the wire
            // would result in a TDS exception, which would close the connection.
            if (daysIntoCE < 0 || daysIntoCE >= daysSinceBaseDate(10000, 1, 1)) {
                throw new MicrosoftDataEncryptionException(
                        MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidTemporalValue"));
            }

            byte encodedBytes[] = new byte[3];
            encodedBytes[0] = (byte) ((daysIntoCE >> 0) & 0xFF);
            encodedBytes[1] = (byte) ((daysIntoCE >> 8) & 0xFF);
            encodedBytes[2] = (byte) ((daysIntoCE >> 16) & 0xFF);

            if (SSType.DATE == ssType) {
                return encodedBytes;
            } else if (SSType.DATETIME2 == ssType) {
                // for Max value, does not round up, do casting instead.
                if (3652058 == daysIntoCE) { // 9999-12-31
                    if (864000000000L == scaledNanos) { // 24:00:00 in nanoseconds
                        // does not round up
                        scaledNanos = (((long) Nanos.PER_SECOND * secondsSinceMidnight
                                + getRoundedSubSecondNanos(subSecondNanos)) / divisor) * divisor / 100;

                        int encodedLength = nanosSinceMidnightLength(MAX_FRACTIONAL_SECONDS_SCALE);
                        byte[] encodedNanoBytes = scaledNanosToEncodedBytes(scaledNanos, encodedLength);

                        // for DATETIME2 sends both date and time part together for encryption
                        encodedBytesForEncryption = new byte[encodedLength + 3];
                        System.arraycopy(encodedNanoBytes, 0, encodedBytesForEncryption, 0, encodedNanoBytes.length);
                    }
                }
                if (null != encodedBytesForEncryption) {
                    // Copy the 3 byte date value
                    System.arraycopy(encodedBytes, 0, encodedBytesForEncryption, (encodedBytesForEncryption.length - 3),
                            3);
                }

                return encodedBytesForEncryption;
            } else {
                // for Max value, does not round up, do casting instead.
                if (3652058 == daysIntoCE) { // 9999-12-31
                    if (864000000000L == scaledNanos) { // 24:00:00 in nanoseconds
                        // does not round up
                        scaledNanos = (((long) Nanos.PER_SECOND * secondsSinceMidnight
                                + getRoundedSubSecondNanos(subSecondNanos)) / divisor) * divisor / 100;

                        int encodedLength = nanosSinceMidnightLength(MAX_FRACTIONAL_SECONDS_SCALE);
                        byte[] encodedNanoBytes = scaledNanosToEncodedBytes(scaledNanos, encodedLength);

                        // for DATETIMEOFFSET sends date, time and offset part together for encryption
                        encodedBytesForEncryption = new byte[encodedLength + 5];
                        System.arraycopy(encodedNanoBytes, 0, encodedBytesForEncryption, 0, encodedNanoBytes.length);
                    }
                }

                if (null != encodedBytesForEncryption) {
                    // Copy the 3 byte date value
                    System.arraycopy(encodedBytes, 0, encodedBytesForEncryption, (encodedBytesForEncryption.length - 5),
                            3);
                    // Copy the 2 byte minutesOffset value
                    System.arraycopy(
                            ByteBuffer.allocate(Short.SIZE / Byte.SIZE).order(ByteOrder.LITTLE_ENDIAN)
                                    .putShort(minutesOffset).array(),
                            0, encodedBytesForEncryption, (encodedBytesForEncryption.length - 2), 2);
                }
                return encodedBytesForEncryption;
            }
        }

        // Invalid type ssType. This condition should never happen.
        throw new MicrosoftDataEncryptionException(
                MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidTemporalValue"));
    }

    // getEncryptedDateTimeAsBytes is called if jdbcType/ssType is SMALLDATETIME or DATETIME
    static byte[] getNormalizedDateTimeAsBytes(GregorianCalendar cal, int subSecondNanos,
                                               JDBCType jdbcType) throws MicrosoftDataEncryptionException {
        int daysSinceSQLBaseDate = daysSinceBaseDate(cal.get(Calendar.YEAR), cal.get(Calendar.DAY_OF_YEAR),
                BASE_YEAR_1900);

        // Next, figure out the number of milliseconds since midnight of the current day.
        int millisSinceMidnight = (subSecondNanos + Nanos.PER_MILLISECOND / 2) / Nanos.PER_MILLISECOND + // Millis into
                                                                                                         // the current
                                                                                                         // second
                1000 * cal.get(Calendar.SECOND) + // Seconds into the current minute
                60 * 1000 * cal.get(Calendar.MINUTE) + // Minutes into the current hour
                60 * 60 * 1000 * cal.get(Calendar.HOUR_OF_DAY); // Hours into the current day

        // The last millisecond of the current day is always rounded to the first millisecond
        // of the next day because DATETIME is only accurate to 1/300th of a second.
        if (millisSinceMidnight >= 1000 * 60 * 60 * 24 - 1) {
            ++daysSinceSQLBaseDate;
            millisSinceMidnight = 0;
        }

        if (JDBCType.SMALLDATETIME == jdbcType) {

            int secondsSinceMidnight = (millisSinceMidnight / 1000);
            int minutesSinceMidnight = (secondsSinceMidnight / 60);

            // Values that are 29.998 seconds or less are rounded down to the nearest minute
            minutesSinceMidnight = ((secondsSinceMidnight % 60) > 29.998) ? minutesSinceMidnight + 1
                                                                          : minutesSinceMidnight;

            // minutesSinceMidnight for (23:59:30)
            int maxMinutesSinceMidnight_SmallDateTime = 1440;
            // Verification for smalldatetime to be within valid range of (1900.01.01) to (2079.06.06)
            // smalldatetime for unencrypted does not allow insertion of 2079.06.06 23:59:59 and it is rounded up
            // to 2079.06.07 00:00:00, therefore, we are checking minutesSinceMidnight for that condition. If it's not
            // within valid range, then
            // throw an exception now so that statement execution is safely canceled.
            // 157 is the calculated day of year from 06-06 , 1440 is minutesince midnight for (23:59:30)
            if ((daysSinceSQLBaseDate < daysSinceBaseDate(1900, 1, BASE_YEAR_1900)
                    || daysSinceSQLBaseDate > daysSinceBaseDate(2079, 157, BASE_YEAR_1900))
                    || (daysSinceSQLBaseDate == daysSinceBaseDate(2079, 157, BASE_YEAR_1900)
                            && minutesSinceMidnight >= maxMinutesSinceMidnight_SmallDateTime)) {
                throw new MicrosoftDataEncryptionException(
                        MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidTemporalValue"));
            }

            ByteBuffer days = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN);
            days.putShort((short) daysSinceSQLBaseDate);
            ByteBuffer seconds = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN);
            seconds.putShort((short) minutesSinceMidnight);

            byte[] value = new byte[4];
            System.arraycopy(days.array(), 0, value, 0, 2);
            System.arraycopy(seconds.array(), 0, value, 2, 2);
            return value;
        } else if (JDBCType.DATETIME == jdbcType) {
            // Last-ditch verification that the value is in the valid range for the
            // DATETIMEN TDS data type (1/1/1753 to 12/31/9999). If it's not, then
            // throw an exception now so that statement execution is safely canceled.
            // Attempting to put an invalid value on the wire would result in a TDS
            // exception, which would close the connection.
            // These are based on SQL Server algorithms
            // And put it all on the wire...
            if (daysSinceSQLBaseDate < daysSinceBaseDate(1753, 1, BASE_YEAR_1900)
                    || daysSinceSQLBaseDate >= daysSinceBaseDate(10000, 1, BASE_YEAR_1900)) {
                throw new MicrosoftDataEncryptionException(
                        MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidTemporalValue"));
            }

            // Number of days since the SQL Server Base Date (January 1, 1900)
            ByteBuffer days = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
            days.putInt(daysSinceSQLBaseDate);
            ByteBuffer seconds = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
            seconds.putInt((3 * millisSinceMidnight + 5) / 10);

            byte[] value = new byte[8];
            System.arraycopy(days.array(), 0, value, 0, 4);
            System.arraycopy(seconds.array(), 0, value, 4, 4);
            return value;
        }

        assert false : "Unexpected JDBCType type " + jdbcType;
        return null;
    }

    /**
     * Returns subSecondNanos rounded to the maximum precision supported. The maximum fractional scale is
     * MAX_FRACTIONAL_SECONDS_SCALE(7). Eg1: if you pass 456,790,123 the function would return 456,790,100 Eg2: if you
     * pass 456,790,150 the function would return 456,790,200 Eg3: if you pass 999,999,951 the function would return
     * 1,000,000,000 This is done to ensure that we have consistent rounding behaviour in setters and getters. Bug
     * #507919
     */
    private static int getRoundedSubSecondNanos(int subSecondNanos) {
        int roundedNanos = ((subSecondNanos + (Nanos.PER_MAX_SCALE_INTERVAL / 2)) / Nanos.PER_MAX_SCALE_INTERVAL)
                * Nanos.PER_MAX_SCALE_INTERVAL;
        return roundedNanos;
    }

    private static int nanosSinceMidnightLength(int scale) {
        final int[] scaledLengths = {3, 3, 3, 4, 4, 5, 5, 5};
        assert scale >= 0;
        assert scale <= MAX_FRACTIONAL_SECONDS_SCALE;
        return scaledLengths[scale];
    }

    private static byte[] scaledNanosToEncodedBytes(long scaledNanos, int encodedLength) {
        byte encodedBytes[] = new byte[encodedLength];
        for (int i = 0; i < encodedLength; i++)
            encodedBytes[i] = (byte) ((scaledNanos >> (8 * i)) & 0xFF);
        return encodedBytes;
    }

    /**
     * Convert a SQL Server temporal value to the desired Java object type.
     *
     * Accepted SQL server data types:
     *
     * DATETIME SMALLDATETIME DATE TIME DATETIME2 DATETIMEOFFSET
     *
     * Converts to Java types (determined by JDBC type):
     *
     * java.sql.Date java.sql.Time java.sql.Timestamp java.lang.String
     *
     * @param jdbcType
     *        the JDBC type indicating the desired conversion
     *
     * @param ssType
     *        the SQL Server data type of the value being converted
     *
     * @param timeZoneCalendar
     *        (optional) a Calendar representing the time zone to associate with the resulting converted value. For
     *        DATETIMEOFFSET, this parameter represents the time zone associated with the value. Null means to use the
     *        default VM time zone.
     *
     * @param daysSinceBaseDate
     *        The date part of the value, expressed as a number of days since the base date for the specified SQL Server
     *        data type. For DATETIME and SMALLDATETIME, the base date is 1/1/1900. For other types, the base date is
     *        1/1/0001. The number of days assumes Gregorian leap year behavior over the entire supported range of
     *        values. For TIME values, this parameter must be the number of days between 1/1/0001 and 1/1/1900 when
     *        converting to java.sql.Timestamp.
     *
     * @param ticksSinceMidnight
     *        The time part of the value, expressed as a number of time units (ticks) since midnight. For DATETIME and
     *        SMALLDATETIME SQL Server data types, time units are in milliseconds. For other types, time units are in
     *        nanoseconds. For DATE values, this parameter must be 0.
     *
     * @param fractionalSecondsScale
     *        the desired fractional seconds scale to use when formatting the value as a String. Ignored for conversions
     *        to Java types other than String.
     *
     * @return a Java object of the desired type.
     * @throws MicrosoftDataEncryptionException
     */
    static final Object convertTemporalToObject(JDBCType jdbcType, SSType ssType, Calendar timeZoneCalendar,
                                                int daysSinceBaseDate, long ticksSinceMidnight,
                                                int fractionalSecondsScale) throws MicrosoftDataEncryptionException {

        // In cases where a Calendar object (and therefore Timezone) is not passed to the method,
        // use the path below instead to optimize performance.
        if (null == timeZoneCalendar) {
            return convertTemporalToObject(jdbcType, ssType, daysSinceBaseDate, ticksSinceMidnight,
                    fractionalSecondsScale);
        }

        // Determine the local time zone to associate with the value. Use the default VM
        // time zone if no time zone is otherwise specified.
        TimeZone localTimeZone = timeZoneCalendar.getTimeZone();

        // Assumed time zone associated with the date and time parts of the value.
        //
        // For DATETIMEOFFSET, the date and time parts are assumed to be relative to UTC.
        // For other data types, the date and time parts are assumed to be relative to the local time zone.
        TimeZone componentTimeZone = (SSType.DATETIMEOFFSET == ssType) ? UTC.timeZone : localTimeZone;

        int subSecondNanos;
        // The date and time parts assume a Gregorian calendar with Gregorian leap year behavior
        // over the entire supported range of values. Create and initialize such a calendar to
        // use to interpret the date and time parts in their associated time zone.
        GregorianCalendar cal = new GregorianCalendar(componentTimeZone, Locale.US);

        // Allow overflow in "smaller" fields (such as MILLISECOND and DAY_OF_YEAR) to update
        // "larger" fields (such as HOUR, MINUTE, SECOND, and YEAR, MONTH, DATE).
        cal.setLenient(true);

        // Clear old state from the calendar. Newly created calendars are always initialized to the
        // current date and time.
        cal.clear();

        // Set the calendar value according to the specified local time zone and constituent
        // date (days since base date) and time (ticks since midnight) parts.
        switch (ssType) {
            case TIME: {
                // Set the calendar to the specified value. Lenient calendar behavior will update
                // individual fields according to standard Gregorian leap year rules, which are sufficient
                // for all TIME values.
                //
                // When initializing the value, set the date component to 1/1/1900 to facilitate conversion
                // to String and java.sql.Timestamp. Note that conversion to java.sql.Time, which is
                // the expected majority conversion, resets the date to 1/1/1970. It is not measurably
                // faster to conditionalize the date on the target data type to avoid resetting it.
                //
                // Ticks are in nanoseconds.
                cal.set(BASE_YEAR_1900, Calendar.JANUARY, 1, 0, 0, 0);
                cal.set(Calendar.MILLISECOND, (int) (ticksSinceMidnight / Nanos.PER_MILLISECOND));

                subSecondNanos = (int) (ticksSinceMidnight % Nanos.PER_SECOND);
                break;
            }

            case DATE:
            case DATETIME2:
            case DATETIMEOFFSET: {
                // For dates after the standard Julian-Gregorian calendar change date,
                // the calendar value can be accurately set using a straightforward
                // (and measurably better performing) assignment.
                //
                // This optimized path is not functionally correct for dates earlier
                // than the standard Gregorian change date.
                if (daysSinceBaseDate >= GregorianChange.DAYS_SINCE_BASE_DATE_HINT) {
                    // Set the calendar to the specified value. Lenient calendar behavior will update
                    // individual fields according to pure Gregorian calendar rules.
                    //
                    // Ticks are in nanoseconds.

                    cal.set(1, Calendar.JANUARY, 1 + daysSinceBaseDate + GregorianChange.EXTRA_DAYS_TO_BE_ADDED, 0, 0,
                            0);
                    cal.set(Calendar.MILLISECOND, (int) (ticksSinceMidnight / Nanos.PER_MILLISECOND));
                }

                // For dates before the standard change date, it is necessary to rationalize
                // the difference between SQL Server (pure Gregorian) calendar behavior and
                // Java (standard Gregorian) calendar behavior. Rationalization ensures that
                // the "wall calendar" representation of the value on both server and client
                // are the same, taking into account the difference in the respective calendars'
                // leap year rules.
                //
                // This code path is functionally correct, but less performant, than the
                // optimized path above for dates after the standard Gregorian change date.
                else {
                    cal.setGregorianChange(GregorianChange.PURE_CHANGE_DATE);

                    // Set the calendar to the specified value. Lenient calendar behavior will update
                    // individual fields according to pure Gregorian calendar rules.
                    //
                    // Ticks are in nanoseconds.
                    cal.set(1, Calendar.JANUARY, 1 + daysSinceBaseDate, 0, 0, 0);
                    cal.set(Calendar.MILLISECOND, (int) (ticksSinceMidnight / Nanos.PER_MILLISECOND));

                    // Recompute the calendar's internal UTC milliseconds value according to the historically
                    // standard Gregorian cutover date, which is needed for constructing java.sql.Time,
                    // java.sql.Date, and java.sql.Timestamp values from UTC milliseconds.
                    int year = cal.get(Calendar.YEAR);
                    int month = cal.get(Calendar.MONTH);
                    int date = cal.get(Calendar.DATE);
                    int hour = cal.get(Calendar.HOUR_OF_DAY);
                    int minute = cal.get(Calendar.MINUTE);
                    int second = cal.get(Calendar.SECOND);
                    int millis = cal.get(Calendar.MILLISECOND);

                    cal.setGregorianChange(GregorianChange.STANDARD_CHANGE_DATE);
                    cal.set(year, month, date, hour, minute, second);
                    cal.set(Calendar.MILLISECOND, millis);
                }

                // For DATETIMEOFFSET values, recompute the calendar's UTC milliseconds value according
                // to the specified local time zone (the time zone associated with the offset part
                // of the DATETIMEOFFSET value).
                //
                // Optimization: Skip this step if there is no time zone difference
                // (i.e. the time zone of the DATETIMEOFFSET value is UTC).
                if (SSType.DATETIMEOFFSET == ssType && !componentTimeZone.hasSameRules(localTimeZone)) {
                    GregorianCalendar localCalendar = new GregorianCalendar(localTimeZone, Locale.US);
                    localCalendar.clear();
                    localCalendar.setTimeInMillis(cal.getTimeInMillis());
                    cal = localCalendar;
                }

                subSecondNanos = (int) (ticksSinceMidnight % Nanos.PER_SECOND);
                break;
            }

            case DATETIME: // and SMALLDATETIME
            {
                // For Yukon (and earlier) data types DATETIME and SMALLDATETIME, there is no need to
                // change the Gregorian cutover because the earliest representable value (1/1/1753)
                // is after the historically standard cutover date (10/15/1582).

                // Set the calendar to the specified value. Lenient calendar behavior will update
                // individual fields according to standard Gregorian leap year rules, which are sufficient
                // for all values in the supported DATETIME range.
                //
                // Ticks are in milliseconds.
                cal.set(BASE_YEAR_1900, Calendar.JANUARY, 1 + daysSinceBaseDate, 0, 0, 0);
                cal.set(Calendar.MILLISECOND, (int) ticksSinceMidnight);

                subSecondNanos = (int) ((ticksSinceMidnight * Nanos.PER_MILLISECOND) % Nanos.PER_SECOND);
                break;
            }

            default:
                MessageFormat form = new MessageFormat(
                        MicrosoftDataEncryptionExceptionResource.getResource("R_UnexpectedSourceType"));
                Object[] msgArgs = {ssType};
                throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }

        int localMillisOffset = timeZoneCalendar.get(Calendar.ZONE_OFFSET);

        // Convert the calendar value (in local time) to the desired Java object type.
        switch (jdbcType.category) {
            case BINARY:
            case SQL_VARIANT: {
                switch (ssType) {
                    case DATE: {
                        // Per JDBC spec, the time part of java.sql.Date values is initialized to midnight
                        // in the specified local time zone.
                        cal.set(Calendar.HOUR_OF_DAY, 0);
                        cal.set(Calendar.MINUTE, 0);
                        cal.set(Calendar.SECOND, 0);
                        cal.set(Calendar.MILLISECOND, 0);
                        return new java.sql.Date(cal.getTimeInMillis());
                    }

                    case DATETIME:
                    case DATETIME2: {
                        java.sql.Timestamp ts = new java.sql.Timestamp(cal.getTimeInMillis());
                        ts.setNanos(subSecondNanos);
                        return ts;
                    }

                    case DATETIMEOFFSET: {
                        // Per driver spec, conversion to DateTimeOffset is only supported from
                        // DATETIMEOFFSET SQL Server values.
                        assert SSType.DATETIMEOFFSET == ssType;

                        // For DATETIMEOFFSET SQL Server values, the time zone offset is in minutes.
                        // The offset from Java TimeZone objects is in milliseconds. Because we
                        // are only dealing with DATETIMEOFFSET SQL Server values here, we can assume
                        // that the offset is precise only to the minute and that rescaling from
                        // milliseconds precision results in no loss of precision.
                        assert 0 == localMillisOffset % (60 * 1000);

                        java.sql.Timestamp ts = new java.sql.Timestamp(cal.getTimeInMillis());
                        ts.setNanos(subSecondNanos);
                        return DateTimeOffset.valueOf(ts, localMillisOffset / (60 * 1000));
                    }

                    case TIME: {
                        // Per driver spec, values of sql server data types types (including TIME) which have greater
                        // than millisecond precision are rounded, not truncated, to the nearest millisecond when
                        // converting to java.sql.Time. Since the milliseconds value in the calendar is truncated,
                        // round it now.
                        if (subSecondNanos % Nanos.PER_MILLISECOND >= Nanos.PER_MILLISECOND / 2)
                            cal.add(Calendar.MILLISECOND, 1);

                        // Per JDBC spec, the date part of java.sql.Time values is initialized to 1/1/1970
                        // in the specified local time zone. This must be done after rounding (above) to
                        // prevent rounding values within nanoseconds of the next day from ending up normalized
                        // to 1/2/1970 instead...
                        cal.set(BASE_YEAR_1970, Calendar.JANUARY, 1);

                        return new java.sql.Time(cal.getTimeInMillis());
                    }

                    default:
                        MessageFormat form = new MessageFormat(
                                MicrosoftDataEncryptionExceptionResource.getResource("R_UnexpectedSourceType"));
                        Object[] msgArgs = {ssType};
                        throw new MicrosoftDataEncryptionException(form.format(msgArgs));
                }
            }

            case DATE: {
                // Per JDBC spec, the time part of java.sql.Date values is initialized to midnight
                // in the specified local time zone.
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                return new java.sql.Date(cal.getTimeInMillis());
            }

            case TIME: {
                // Per driver spec, values of sql server data types types (including TIME) which have greater
                // than millisecond precision are rounded, not truncated, to the nearest millisecond when
                // converting to java.sql.Time. Since the milliseconds value in the calendar is truncated,
                // round it now.
                if (subSecondNanos % Nanos.PER_MILLISECOND >= Nanos.PER_MILLISECOND / 2)
                    cal.add(Calendar.MILLISECOND, 1);

                // Per JDBC spec, the date part of java.sql.Time values is initialized to 1/1/1970
                // in the specified local time zone. This must be done after rounding (above) to
                // prevent rounding values within nanoseconds of the next day from ending up normalized
                // to 1/2/1970 instead...
                cal.set(BASE_YEAR_1970, Calendar.JANUARY, 1);

                return new java.sql.Time(cal.getTimeInMillis());
            }

            case TIMESTAMP: {
                java.sql.Timestamp ts = new java.sql.Timestamp(cal.getTimeInMillis());
                ts.setNanos(subSecondNanos);
                if (jdbcType == JDBCType.LOCALDATETIME) {
                    return ts.toLocalDateTime();
                }
                return ts;
            }

            case DATETIMEOFFSET: {
                // Per driver spec, conversion to DateTimeOffset is only supported from
                // DATETIMEOFFSET SQL Server values.
                assert SSType.DATETIMEOFFSET == ssType;

                // For DATETIMEOFFSET SQL Server values, the time zone offset is in minutes.
                // The offset from Java TimeZone objects is in milliseconds. Because we
                // are only dealing with DATETIMEOFFSET SQL Server values here, we can assume
                // that the offset is precise only to the minute and that rescaling from
                // milliseconds precision results in no loss of precision.
                assert 0 == localMillisOffset % (60 * 1000);

                java.sql.Timestamp ts = new java.sql.Timestamp(cal.getTimeInMillis());
                ts.setNanos(subSecondNanos);
                return DateTimeOffset.valueOf(ts, localMillisOffset / (60 * 1000));
            }

            case CHARACTER: {
                switch (ssType) {
                    case DATE: {
                        return String.format(Locale.US, "%1$tF", // yyyy-mm-dd
                                cal);
                    }

                    case TIME: {
                        return String.format(Locale.US, "%1$tT%2$s", // hh:mm:ss[.nnnnnnn]
                                cal, fractionalSecondsString(subSecondNanos, fractionalSecondsScale));
                    }

                    case DATETIME2: {
                        return String.format(Locale.US, "%1$tF %1$tT%2$s", // yyyy-mm-dd hh:mm:ss[.nnnnnnn]
                                cal, fractionalSecondsString(subSecondNanos, fractionalSecondsScale));
                    }

                    case DATETIMEOFFSET: {
                        // The offset part of a DATETIMEOFFSET value is precise only to the minute,
                        // but TimeZone returns the raw offset as precise to the millisecond.
                        assert 0 == localMillisOffset % (60 * 1000);

                        int unsignedMinutesOffset = Math.abs(localMillisOffset / (60 * 1000));
                        return String.format(Locale.US, "%1$tF %1$tT%2$s %3$c%4$02d:%5$02d", // yyyy-mm-dd
                                                                                             // hh:mm:ss[.nnnnnnn]
                                                                                             // [+|-]hh:mm
                                cal, fractionalSecondsString(subSecondNanos, fractionalSecondsScale),
                                (localMillisOffset >= 0) ? '+' : '-', unsignedMinutesOffset / 60,
                                unsignedMinutesOffset % 60);
                    }

                    case DATETIME: // and SMALLDATETIME
                    {
                        return (new java.sql.Timestamp(cal.getTimeInMillis())).toString();
                    }

                    default:
                        MessageFormat form = new MessageFormat(
                                MicrosoftDataEncryptionExceptionResource.getResource("R_UnexpectedSourceType"));
                        Object[] msgArgs = {ssType};
                        throw new MicrosoftDataEncryptionException(form.format(msgArgs));
                }
            }

            default:
                MessageFormat form = new MessageFormat(
                        MicrosoftDataEncryptionExceptionResource.getResource("R_UnexpectedTargetType"));
                Object[] msgArgs = {jdbcType};
                throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }
    }

    private static Object convertTemporalToObject(JDBCType jdbcType, SSType ssType, int daysSinceBaseDate,
                                                  long ticksSinceMidnight, int fractionalSecondsScale) throws MicrosoftDataEncryptionException {
        int subSecondNanos;

        // In cases where Timezone values don't need to be considered, use LocalDateTime go avoid
        // overhead from using a Calendar object.
        // Note that DateTimeOffset path is not handled in this method, since DateTimeOffset always has
        // its own Calendar object (UTC timezone) associated with it.
        LocalDateTime ldt = null;

        switch (ssType) {
            case TIME: {
                ldt = LocalDateTime.of(BASE_YEAR_1900, 1, 1, 0, 0, 0).plusNanos(ticksSinceMidnight);

                subSecondNanos = (int) (ticksSinceMidnight % Nanos.PER_SECOND);
                break;
            }

            case DATE:
            case DATETIME2:
            case DATETIMEOFFSET: {
                ldt = LocalDateTime.of(1, 1, 1, 0, 0, 0);
                ldt = ldt.plusDays(daysSinceBaseDate);
                // If the target is java.sql.Date, don't add the time component since it needs to be at midnight.
                if (jdbcType.category != JDBCType.Category.DATE) {
                    ldt = ldt.plusNanos(ticksSinceMidnight);
                }
                subSecondNanos = (int) (ticksSinceMidnight % Nanos.PER_SECOND);
                break;
            }

            case DATETIME: // and SMALLDATETIME
            {
                ldt = LocalDateTime.of(BASE_YEAR_1900, 1, 1, 0, 0, 0);
                ldt = ldt.plusDays(daysSinceBaseDate);
                // If the target is java.sql.Date, don't add the time component since it needs to be at midnight.
                if (jdbcType.category != JDBCType.Category.DATE) {
                    ldt = ldt.plusNanos(ticksSinceMidnight * Nanos.PER_MILLISECOND);
                }

                subSecondNanos = (int) ((ticksSinceMidnight * Nanos.PER_MILLISECOND) % Nanos.PER_SECOND);
                break;
            }

            default:
                MessageFormat form = new MessageFormat(
                        MicrosoftDataEncryptionExceptionResource.getResource("R_UnexpectedSourceType"));
                Object[] msgArgs = {ssType};
                throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }

        switch (jdbcType.category) {
            case BINARY:
            case SQL_VARIANT: {
                switch (ssType) {
                    case DATE: {
                        return java.sql.Date.valueOf(ldt.toLocalDate());
                    }

                    case DATETIME:
                    case DATETIME2: {
                        java.sql.Timestamp ts = java.sql.Timestamp.valueOf(ldt);
                        ts.setNanos(subSecondNanos);
                        return ts;
                    }

                    case TIME: {
                        if (subSecondNanos % Nanos.PER_MILLISECOND >= Nanos.PER_MILLISECOND / 2) {
                            ldt = ldt.plusNanos(1000000);
                        }
                        java.sql.Time t = java.sql.Time.valueOf(ldt.toLocalTime());
                        t.setTime(t.getTime() + (ldt.getNano() / Nanos.PER_MILLISECOND));
                        return t;
                    }

                    default:
                        MessageFormat form = new MessageFormat(
                                MicrosoftDataEncryptionExceptionResource.getResource("R_UnexpectedSourceType"));
                        Object[] msgArgs = {ssType};
                        throw new MicrosoftDataEncryptionException(form.format(msgArgs));
                }
            }

            case DATE: {
                return java.sql.Date.valueOf(ldt.toLocalDate());
            }

            case TIME: {
                if (subSecondNanos % Nanos.PER_MILLISECOND >= Nanos.PER_MILLISECOND / 2) {
                    ldt = ldt.plusNanos(1000000);
                }
                java.sql.Time t = java.sql.Time.valueOf(ldt.toLocalTime());
                t.setTime(t.getTime() + (ldt.getNano() / Nanos.PER_MILLISECOND));
                return t;
            }

            case TIMESTAMP: {
                if (jdbcType == JDBCType.LOCALDATETIME) {
                    return ldt;
                }

                java.sql.Timestamp ts = java.sql.Timestamp.valueOf(ldt);
                ts.setNanos(subSecondNanos);
                return ts;
            }

            case CHARACTER: {
                switch (ssType) {
                    case DATE: {
                        return String.format(Locale.US, "%1$tF", // yyyy-mm-dd
                                java.sql.Timestamp.valueOf(ldt));
                    }

                    case TIME: {
                        return String.format(Locale.US, "%1$tT%2$s", // hh:mm:ss[.nnnnnnn]
                                ldt, fractionalSecondsString(subSecondNanos, fractionalSecondsScale));
                    }

                    case DATETIME2: {
                        return String.format(Locale.US, "%1$tF %1$tT%2$s", // yyyy-mm-dd hh:mm:ss[.nnnnnnn]
                                java.sql.Timestamp.valueOf(ldt),
                                fractionalSecondsString(subSecondNanos, fractionalSecondsScale));
                    }

                    case DATETIME: // and SMALLDATETIME
                    {
                        return (java.sql.Timestamp.valueOf(ldt)).toString();
                    }

                    default:
                        MessageFormat form = new MessageFormat(
                                MicrosoftDataEncryptionExceptionResource.getResource("R_UnexpectedSourceType"));
                        Object[] msgArgs = {ssType};
                        throw new MicrosoftDataEncryptionException(form.format(msgArgs));
                }
            }

            default:
                MessageFormat form = new MessageFormat(
                        MicrosoftDataEncryptionExceptionResource.getResource("R_UnexpectedTargetType"));
                Object[] msgArgs = {jdbcType};
                throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }
    }

    /**
     * Returns the number of days elapsed from January 1 of the specified baseYear (Gregorian) to the specified
     * dayOfYear in the specified year, assuming pure Gregorian calendar rules (no Julian to Gregorian cutover).
     */
    private static int daysSinceBaseDate(int year, int dayOfYear, int baseYear) {
        assert year >= 1;
        assert baseYear >= 1;
        assert dayOfYear >= 1;

        return (dayOfYear - 1) + // Days into the current year
                (year - baseYear) * DAYS_PER_YEAR + // plus whole years (in days) ...
                leapDaysBeforeYear(year) - // ... plus leap days
                leapDaysBeforeYear(baseYear);
    }

    /**
     * Returns the number of leap days that have occurred between January 1, 1AD and January 1 of the specified year,
     * assuming a Proleptic Gregorian Calendar
     */
    private static int leapDaysBeforeYear(int year) {
        assert year >= 1;

        // On leap years, the US Naval Observatory says:
        // "According to the Gregorian calendar, which is the civil calendar
        // in use today, years evenly divisible by 4 are leap years, with
        // the exception of centurial years that are not evenly divisible
        // by 400. Therefore, the years 1700, 1800, 1900 and 2100 are not
        // leap years, but 1600, 2000, and 2400 are leap years."
        //
        // So, using year 1AD as a base, we can compute the number of leap
        // days between 1AD and the specified year as follows:
        return (year - 1) / 4 - (year - 1) / 100 + (year - 1) / 400;
    }

    // Maximum allowed RPC decimal value (raw integer value with scale removed).
    // This limits the value to 38 digits of precision for SQL.
    private final static BigInteger maxRPCDecimalValue = new BigInteger("99999999999999999999999999999999999999");

    // Returns true if input bigDecimalValue exceeds allowable
    // TDS wire format precision or scale for DECIMAL TDS token.
    static final boolean exceedsMaxRPCDecimalPrecisionOrScale(BigDecimal bigDecimalValue) {
        if (null == bigDecimalValue)
            return false;

        // Maximum scale allowed is same as maximum precision allowed.
        if (bigDecimalValue.scale() > maxDecimalPrecision)
            return true;

        // Convert to unscaled integer value, then compare with maxRPCDecimalValue.
        // NOTE: Handle negative scale as a special case for JDK 1.5 and later VMs.
        BigInteger bi = (bigDecimalValue.scale() < 0) ? bigDecimalValue.setScale(0).unscaledValue()
                                                      : bigDecimalValue.unscaledValue();
        if (bigDecimalValue.signum() < 0)
            bi = bi.negate();
        return (bi.compareTo(maxRPCDecimalValue) > 0);
    }

    static final boolean isCharType(int jdbcType) {
        switch (jdbcType) {
            case java.sql.Types.CHAR:
            case java.sql.Types.NCHAR:
            case java.sql.Types.VARCHAR:
            case java.sql.Types.NVARCHAR:
            case java.sql.Types.LONGVARCHAR:
            case java.sql.Types.LONGNVARCHAR:
                return true;
            default:
                return false;
        }
    }

    static final Boolean isCharType(SSType ssType) {
        switch (ssType) {
            case CHAR:
            case NCHAR:
            case VARCHAR:
            case NVARCHAR:
            case VARCHARMAX:
            case NVARCHARMAX:
                return true;
            default:
                return false;
        }
    }

    static final Boolean isBinaryType(SSType ssType) {
        switch (ssType) {
            case BINARY:
            case VARBINARY:
            case VARBINARYMAX:
            case IMAGE:
                return true;
            default:
                return false;
        }
    }

    static final Boolean isBinaryType(int jdbcType) {
        switch (jdbcType) {
            case java.sql.Types.BINARY:
            case java.sql.Types.VARBINARY:
            case java.sql.Types.LONGVARBINARY:
                return true;
            default:
                return false;
        }
    }

    /**
     * Read a short int from a byte stream
     *
     * @param data
     *        the databytes
     * @param nOffset
     *        offset to read from
     * @return the value
     */
    /* L0 */ static short readShort(byte data[], int nOffset) {
        return (short) ((data[nOffset] & 0xff) | ((data[nOffset + 1] & 0xff) << 8));
    }

    /**
     * Read an unsigned short int (16 bits) from a byte stream
     *
     * @param data
     *        the databytes
     * @param nOffset
     *        offset to read from
     * @return the value
     */
    /* L0 */ static int readUnsignedShort(byte data[], int nOffset) {
        return ((data[nOffset] & 0xff) | ((data[nOffset + 1] & 0xff) << 8));
    }

    static int readUnsignedShortBigEndian(byte data[], int nOffset) {
        return ((data[nOffset] & 0xFF) << 8) | (data[nOffset + 1] & 0xFF);
    }

    static void writeShort(short value, byte valueBytes[], int offset) {
        valueBytes[offset + 0] = (byte) ((value >> 0) & 0xFF);
        valueBytes[offset + 1] = (byte) ((value >> 8) & 0xFF);
    }

    static void writeShortBigEndian(short value, byte valueBytes[], int offset) {
        valueBytes[offset + 0] = (byte) ((value >> 8) & 0xFF);
        valueBytes[offset + 1] = (byte) ((value >> 0) & 0xFF);
    }

    /**
     * Read an int from a byte stream
     *
     * @param data
     *        the databytes
     * @param nOffset
     *        offset to read from
     * @return the value
     */
    /* L0 */ static int readInt(byte data[], int nOffset) {
        int b1 = ((int) data[nOffset + 0] & 0xff);
        int b2 = ((int) data[nOffset + 1] & 0xff) << 8;
        int b3 = ((int) data[nOffset + 2] & 0xff) << 16;
        int b4 = ((int) data[nOffset + 3] & 0xff) << 24;
        return b4 | b3 | b2 | b1;
    }

    static int readIntBigEndian(byte data[], int nOffset) {
        return ((data[nOffset + 3] & 0xFF) << 0) | ((data[nOffset + 2] & 0xFF) << 8)
                | ((data[nOffset + 1] & 0xFF) << 16) | ((data[nOffset + 0] & 0xFF) << 24);
    }

    static void writeInt(int value, byte valueBytes[], int offset) {
        valueBytes[offset + 0] = (byte) ((value >> 0) & 0xFF);
        valueBytes[offset + 1] = (byte) ((value >> 8) & 0xFF);
        valueBytes[offset + 2] = (byte) ((value >> 16) & 0xFF);
        valueBytes[offset + 3] = (byte) ((value >> 24) & 0xFF);
    }

    static void writeIntBigEndian(int value, byte valueBytes[], int offset) {
        valueBytes[offset + 0] = (byte) ((value >> 24) & 0xFF);
        valueBytes[offset + 1] = (byte) ((value >> 16) & 0xFF);
        valueBytes[offset + 2] = (byte) ((value >> 8) & 0xFF);
        valueBytes[offset + 3] = (byte) ((value >> 0) & 0xFF);
    }

    static void writeLongBigEndian(long value, byte valueBytes[], int offset) {
        valueBytes[offset + 0] = (byte) ((value >> 56) & 0xFF);
        valueBytes[offset + 1] = (byte) ((value >> 48) & 0xFF);
        valueBytes[offset + 2] = (byte) ((value >> 40) & 0xFF);
        valueBytes[offset + 3] = (byte) ((value >> 32) & 0xFF);
        valueBytes[offset + 4] = (byte) ((value >> 24) & 0xFF);
        valueBytes[offset + 5] = (byte) ((value >> 16) & 0xFF);
        valueBytes[offset + 6] = (byte) ((value >> 8) & 0xFF);
        valueBytes[offset + 7] = (byte) ((value >> 0) & 0xFF);
    }

    static BigDecimal readBigDecimal(byte valueBytes[], int valueLength, int scale) {
        int sign = (0 == valueBytes[0]) ? -1 : 1;
        byte[] magnitude = new byte[valueLength - 1];
        for (int i = 1; i <= magnitude.length; i++)
            magnitude[magnitude.length - i] = valueBytes[i];
        return new BigDecimal(new BigInteger(sign, magnitude), scale);
    }

    /**
     * Reads a long value from byte array.
     *
     * @param data
     *        the byte array.
     * @param nOffset
     *        the offset into byte array to start reading.
     * @return long value as read from bytes.
     */
    /* L0 */static long readLong(byte data[], int nOffset) {
        return ((long) (data[nOffset + 7] & 0xff) << 56) | ((long) (data[nOffset + 6] & 0xff) << 48)
                | ((long) (data[nOffset + 5] & 0xff) << 40) | ((long) (data[nOffset + 4] & 0xff) << 32)
                | ((long) (data[nOffset + 3] & 0xff) << 24) | ((long) (data[nOffset + 2] & 0xff) << 16)
                | ((long) (data[nOffset + 1] & 0xff) << 8) | ((long) (data[nOffset] & 0xff));
    }

    /**
     * Writes a long to byte array.
     *
     * @param value
     *        long value to write.
     * @param valueBytes
     *        the byte array.
     * @param offset
     *        the offset inside byte array.
     */
    static void writeLong(long value, byte valueBytes[], int offset) {
        valueBytes[offset++] = (byte) ((value) & 0xFF);
        valueBytes[offset++] = (byte) ((value >> 8) & 0xFF);
        valueBytes[offset++] = (byte) ((value >> 16) & 0xFF);
        valueBytes[offset++] = (byte) ((value >> 24) & 0xFF);
        valueBytes[offset++] = (byte) ((value >> 32) & 0xFF);
        valueBytes[offset++] = (byte) ((value >> 40) & 0xFF);
        valueBytes[offset++] = (byte) ((value >> 48) & 0xFF);
        valueBytes[offset] = (byte) ((value >> 56) & 0xFF);
    }

    static final String readGUID(byte[] inputGUID) throws MicrosoftDataEncryptionException {
        String guidTemplate = "NNNNNNNN-NNNN-NNNN-NNNN-NNNNNNNNNNNN";
        byte guid[] = inputGUID;

        StringBuilder sb = new StringBuilder(guidTemplate.length());
        for (int i = 0; i < 4; i++) {
            sb.append(hexChars[(guid[3 - i] & 0xF0) >> 4]);
            sb.append(hexChars[guid[3 - i] & 0x0F]);
        }
        sb.append('-');
        for (int i = 0; i < 2; i++) {
            sb.append(hexChars[(guid[5 - i] & 0xF0) >> 4]);
            sb.append(hexChars[guid[5 - i] & 0x0F]);
        }
        sb.append('-');
        for (int i = 0; i < 2; i++) {
            sb.append(hexChars[(guid[7 - i] & 0xF0) >> 4]);
            sb.append(hexChars[guid[7 - i] & 0x0F]);
        }
        sb.append('-');
        for (int i = 0; i < 2; i++) {
            sb.append(hexChars[(guid[8 + i] & 0xF0) >> 4]);
            sb.append(hexChars[guid[8 + i] & 0x0F]);
        }
        sb.append('-');
        for (int i = 0; i < 6; i++) {
            sb.append(hexChars[(guid[10 + i] & 0xF0) >> 4]);
            sb.append(hexChars[guid[10 + i] & 0x0F]);
        }

        return sb.toString();
    }

    static final byte[] asGuidByteArray(UUID aId) {
        long msb = aId.getMostSignificantBits();
        long lsb = aId.getLeastSignificantBits();
        byte[] buffer = new byte[16];
        writeLongBigEndian(msb, buffer, 0);
        writeLongBigEndian(lsb, buffer, 8);

        // For the first three fields, UUID uses network byte order,
        // Guid uses native byte order. So we need to reverse
        // the first three fields before sending to server.

        byte tmpByte;

        // Reverse the first 4 bytes
        tmpByte = buffer[0];
        buffer[0] = buffer[3];
        buffer[3] = tmpByte;
        tmpByte = buffer[1];
        buffer[1] = buffer[2];
        buffer[2] = tmpByte;

        // Reverse the 5th and the 6th
        tmpByte = buffer[4];
        buffer[4] = buffer[5];
        buffer[5] = tmpByte;

        // Reverse the 7th and the 8th
        tmpByte = buffer[6];
        buffer[6] = buffer[7];
        buffer[7] = tmpByte;

        return buffer;
    }

    private static long readNanosSinceMidnightAE(byte[] value, int scale,
            SSType baseSSType) throws MicrosoftDataEncryptionException {
        long hundredNanosSinceMidnight = 0;
        for (int i = 0; i < value.length; i++)
            hundredNanosSinceMidnight |= (value[i] & 0xFFL) << (8 * i);

        if (!(0 <= hundredNanosSinceMidnight && hundredNanosSinceMidnight < Nanos.PER_DAY / 100)) {
            throw new MicrosoftDataEncryptionException(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidTemporalValue"));
        }

        return 100 * hundredNanosSinceMidnight;
    }

    private static int getDaysIntoCE(byte[] datePortion, SSType baseSSType) throws MicrosoftDataEncryptionException {
        int daysIntoCE = 0;
        for (int i = 0; i < datePortion.length; i++) {
            daysIntoCE |= ((datePortion[i] & 0xFF) << (8 * i));
        }

        if (daysIntoCE < 0) {
            throw new MicrosoftDataEncryptionException(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidTemporalValue"));
        }

        return daysIntoCE;
    }

    static void validateMoneyRange(BigDecimal bd, JDBCType jdbcType) throws MicrosoftDataEncryptionException {
        if (null == bd)
            return;

        switch (jdbcType) {
            case MONEY:
                if ((1 != bd.compareTo(SSType.MAX_VALUE_MONEY)) && (-1 != bd.compareTo(SSType.MIN_VALUE_MONEY))) {
                    return;
                }
                break;
            case SMALLMONEY:
                if ((1 != bd.compareTo(SSType.MAX_VALUE_SMALLMONEY))
                        && (-1 != bd.compareTo(SSType.MIN_VALUE_SMALLMONEY))) {
                    return;
                }
                break;
            default:
                break;
        }
        MessageFormat form = new MessageFormat(
                MicrosoftDataEncryptionExceptionResource.getResource("R_valueOutOfRange"));
        Object[] msgArgs = {jdbcType};
        throw new MicrosoftDataEncryptionException(form.format(msgArgs));
    }
}


enum JDBCType {
    UNKNOWN(Category.UNKNOWN, 999, Object.class.getName()),
    ARRAY(Category.UNKNOWN, java.sql.Types.ARRAY, Object.class.getName()),
    BIGINT(Category.NUMERIC, java.sql.Types.BIGINT, Long.class.getName()),
    BINARY(Category.BINARY, java.sql.Types.BINARY, "[B"),
    BIT(Category.NUMERIC, java.sql.Types.BIT, Boolean.class.getName()),
    BLOB(Category.BLOB, java.sql.Types.BLOB, java.sql.Blob.class.getName()),
    BOOLEAN(Category.NUMERIC, java.sql.Types.BOOLEAN, Boolean.class.getName()),
    CHAR(Category.CHARACTER, java.sql.Types.CHAR, String.class.getName()),
    CLOB(Category.CLOB, java.sql.Types.CLOB, java.sql.Clob.class.getName()),
    DATALINK(Category.UNKNOWN, java.sql.Types.DATALINK, Object.class.getName()),
    DATE(Category.DATE, java.sql.Types.DATE, java.sql.Date.class.getName()),
    DATETIMEOFFSET(Category.DATETIMEOFFSET, Types.DATETIMEOFFSET, DateTimeOffset.class.getName()),
    DECIMAL(Category.NUMERIC, java.sql.Types.DECIMAL, BigDecimal.class.getName()),
    DISTINCT(Category.UNKNOWN, java.sql.Types.DISTINCT, Object.class.getName()),
    DOUBLE(Category.NUMERIC, java.sql.Types.DOUBLE, Double.class.getName()),
    FLOAT(Category.NUMERIC, java.sql.Types.FLOAT, Double.class.getName()),
    INTEGER(Category.NUMERIC, java.sql.Types.INTEGER, Integer.class.getName()),
    JAVA_OBJECT(Category.UNKNOWN, java.sql.Types.JAVA_OBJECT, Object.class.getName()),
    LONGNVARCHAR(Category.LONG_NCHARACTER, -16, String.class.getName()),
    LONGVARBINARY(Category.LONG_BINARY, java.sql.Types.LONGVARBINARY, "[B"),
    LONGVARCHAR(Category.LONG_CHARACTER, java.sql.Types.LONGVARCHAR, String.class.getName()),
    NCHAR(Category.NCHARACTER, -15, String.class.getName()),
    NCLOB(Category.NCLOB, 2011, java.sql.NClob.class.getName()),
    NULL(Category.UNKNOWN, java.sql.Types.NULL, Object.class.getName()),
    NUMERIC(Category.NUMERIC, java.sql.Types.NUMERIC, BigDecimal.class.getName()),
    NVARCHAR(Category.NCHARACTER, -9, String.class.getName()),
    OTHER(Category.UNKNOWN, java.sql.Types.OTHER, Object.class.getName()),
    REAL(Category.NUMERIC, java.sql.Types.REAL, Float.class.getName()),
    REF(Category.UNKNOWN, java.sql.Types.REF, Object.class.getName()),
    ROWID(Category.UNKNOWN, -8, Object.class.getName()),
    SMALLINT(Category.NUMERIC, java.sql.Types.SMALLINT, Short.class.getName()),
    SQLXML(Category.SQLXML, 2009, Object.class.getName()),
    STRUCT(Category.UNKNOWN, java.sql.Types.STRUCT, Object.class.getName()),
    TIME(Category.TIME, java.sql.Types.TIME, java.sql.Time.class.getName()),
    TIME_WITH_TIMEZONE(Category.TIME_WITH_TIMEZONE, 2013, java.time.OffsetTime.class.getName()),
    TIMESTAMP(Category.TIMESTAMP, java.sql.Types.TIMESTAMP, java.sql.Timestamp.class.getName()),
    TIMESTAMP_WITH_TIMEZONE(Category.TIMESTAMP_WITH_TIMEZONE, 2014, java.time.OffsetDateTime.class.getName()),
    TINYINT(Category.NUMERIC, java.sql.Types.TINYINT, Short.class.getName()),
    VARBINARY(Category.BINARY, java.sql.Types.VARBINARY, "[B"),
    VARCHAR(Category.CHARACTER, java.sql.Types.VARCHAR, String.class.getName()),
    MONEY(Category.NUMERIC, Types.MONEY, BigDecimal.class.getName()),
    SMALLMONEY(Category.NUMERIC, Types.SMALLMONEY, BigDecimal.class.getName()),
    TVP(Category.TVP, Types.STRUCTURED, Object.class.getName()),
    DATETIME(Category.TIMESTAMP, Types.DATETIME, java.sql.Timestamp.class.getName()),
    SMALLDATETIME(Category.TIMESTAMP, Types.SMALLDATETIME, java.sql.Timestamp.class.getName()),
    GUID(Category.CHARACTER, Types.GUID, String.class.getName()),
    SQL_VARIANT(Category.SQL_VARIANT, Types.SQL_VARIANT, Object.class.getName()),
    GEOMETRY(Category.GEOMETRY, Types.GEOMETRY, Object.class.getName()),
    GEOGRAPHY(Category.GEOGRAPHY, Types.GEOGRAPHY, Object.class.getName()),
    LOCALDATETIME(Category.TIMESTAMP, java.sql.Types.TIMESTAMP, LocalDateTime.class.getName());

    final Category category;
    private final int intValue;
    private final String className;
    private static final JDBCType[] VALUES = values();

    final String className() {
        return className;
    }

    private JDBCType(Category category, int intValue, String className) {
        this.category = category;
        this.intValue = intValue;
        this.className = className;
    }

    /**
     * Returns the integer value of JDBCType.
     *
     * @return integer representation of JDBCType
     */
    public int getIntValue() {
        return this.intValue;
    }

    enum Category {
        CHARACTER,
        LONG_CHARACTER,
        CLOB,
        NCHARACTER,
        LONG_NCHARACTER,
        NCLOB,
        BINARY,
        LONG_BINARY,
        BLOB,
        NUMERIC,
        DATE,
        TIME,
        TIMESTAMP,
        TIME_WITH_TIMEZONE,
        TIMESTAMP_WITH_TIMEZONE,
        DATETIMEOFFSET,
        SQLXML,
        UNKNOWN,
        TVP,
        GUID,
        SQL_VARIANT,
        GEOMETRY,
        GEOGRAPHY;

        private static final Category[] VALUES = values();
    }
}


enum SSType {
    UNKNOWN(Category.UNKNOWN, "unknown", JDBCType.UNKNOWN),
    TINYINT(Category.NUMERIC, "tinyint", JDBCType.TINYINT),
    BIT(Category.NUMERIC, "bit", JDBCType.BIT),
    SMALLINT(Category.NUMERIC, "smallint", JDBCType.SMALLINT),
    INTEGER(Category.NUMERIC, "int", JDBCType.INTEGER),
    BIGINT(Category.NUMERIC, "bigint", JDBCType.BIGINT),
    FLOAT(Category.NUMERIC, "float", JDBCType.DOUBLE),
    REAL(Category.NUMERIC, "real", JDBCType.REAL),
    SMALLDATETIME(Category.DATETIME, "smalldatetime", JDBCType.SMALLDATETIME),
    DATETIME(Category.DATETIME, "datetime", JDBCType.DATETIME),
    DATE(Category.DATE, "date", JDBCType.DATE),
    TIME(Category.TIME, "time", JDBCType.TIME),
    DATETIME2(Category.DATETIME2, "datetime2", JDBCType.TIMESTAMP),
    DATETIMEOFFSET(Category.DATETIMEOFFSET, "datetimeoffset", JDBCType.DATETIMEOFFSET),
    SMALLMONEY(Category.NUMERIC, "smallmoney", JDBCType.SMALLMONEY),
    MONEY(Category.NUMERIC, "money", JDBCType.MONEY),
    CHAR(Category.CHARACTER, "char", JDBCType.CHAR),
    VARCHAR(Category.CHARACTER, "varchar", JDBCType.VARCHAR),
    VARCHARMAX(Category.LONG_CHARACTER, "varchar", JDBCType.LONGVARCHAR),
    TEXT(Category.LONG_CHARACTER, "text", JDBCType.LONGVARCHAR),
    NCHAR(Category.NCHARACTER, "nchar", JDBCType.NCHAR),
    NVARCHAR(Category.NCHARACTER, "nvarchar", JDBCType.NVARCHAR),
    NVARCHARMAX(Category.LONG_NCHARACTER, "nvarchar", JDBCType.LONGNVARCHAR),
    NTEXT(Category.LONG_NCHARACTER, "ntext", JDBCType.LONGNVARCHAR),
    BINARY(Category.BINARY, "binary", JDBCType.BINARY),
    VARBINARY(Category.BINARY, "varbinary", JDBCType.VARBINARY),
    VARBINARYMAX(Category.LONG_BINARY, "varbinary", JDBCType.LONGVARBINARY),
    IMAGE(Category.LONG_BINARY, "image", JDBCType.LONGVARBINARY),
    DECIMAL(Category.NUMERIC, "decimal", JDBCType.DECIMAL),
    NUMERIC(Category.NUMERIC, "numeric", JDBCType.NUMERIC),
    GUID(Category.GUID, "uniqueidentifier", JDBCType.GUID),
    SQL_VARIANT(Category.SQL_VARIANT, "sql_variant", JDBCType.SQL_VARIANT),
    UDT(Category.UDT, "udt", JDBCType.VARBINARY),
    XML(Category.XML, "xml", JDBCType.LONGNVARCHAR),
    TIMESTAMP(Category.TIMESTAMP, "timestamp", JDBCType.BINARY),
    GEOMETRY(Category.UDT, "geometry", JDBCType.GEOMETRY),
    GEOGRAPHY(Category.UDT, "geography", JDBCType.GEOGRAPHY);

    final Category category;
    private final String name;
    private final JDBCType jdbcType;
    private static final SSType[] VALUES = values();

    static final BigDecimal MAX_VALUE_MONEY = new BigDecimal("922337203685477.5807");
    static final BigDecimal MIN_VALUE_MONEY = new BigDecimal("-922337203685477.5808");
    static final BigDecimal MAX_VALUE_SMALLMONEY = new BigDecimal("214748.3647");
    static final BigDecimal MIN_VALUE_SMALLMONEY = new BigDecimal("-214748.3648");

    private SSType(Category category, String name, JDBCType jdbcType) {
        this.category = category;
        this.name = name;
        this.jdbcType = jdbcType;
    }

    public String toString() {
        return name;
    }

    final JDBCType getJDBCType() {
        return jdbcType;
    }

    static SSType of(String typeName) throws MicrosoftDataEncryptionException {
        for (SSType ssType : VALUES)
            if (ssType.name.equalsIgnoreCase(typeName))
                return ssType;

        return SSType.UNKNOWN;
    }

    enum Category {
        BINARY,
        CHARACTER,
        DATE,
        DATETIME,
        DATETIME2,
        DATETIMEOFFSET,
        GUID,
        LONG_BINARY,
        LONG_CHARACTER,
        LONG_NCHARACTER,
        NCHARACTER,
        NUMERIC,
        UNKNOWN,
        TIME,
        TIMESTAMP,
        UDT,
        SQL_VARIANT,
        XML;

        private static final Category[] VALUES = values();
    }

    enum GetterConversion {
        NUMERIC(SSType.Category.NUMERIC, EnumSet.of(JDBCType.Category.NUMERIC, JDBCType.Category.CHARACTER,
                JDBCType.Category.BINARY)),

        DATETIME(SSType.Category.DATETIME, EnumSet.of(JDBCType.Category.DATE, JDBCType.Category.TIME,
                JDBCType.Category.TIMESTAMP, JDBCType.Category.CHARACTER, JDBCType.Category.BINARY)),

        DATETIME2(SSType.Category.DATETIME2, EnumSet.of(JDBCType.Category.DATE, JDBCType.Category.TIME,
                JDBCType.Category.TIMESTAMP, JDBCType.Category.CHARACTER)),

        DATE(SSType.Category.DATE, EnumSet.of(JDBCType.Category.DATE, JDBCType.Category.TIMESTAMP,
                JDBCType.Category.CHARACTER)),

        TIME(SSType.Category.TIME, EnumSet.of(JDBCType.Category.TIME, JDBCType.Category.TIMESTAMP,
                JDBCType.Category.CHARACTER)),

        DATETIMEOFFSET(SSType.Category.DATETIMEOFFSET, EnumSet.of(JDBCType.Category.DATE, JDBCType.Category.TIME,
                JDBCType.Category.TIMESTAMP, JDBCType.Category.DATETIMEOFFSET, JDBCType.Category.CHARACTER)),

        CHARACTER(SSType.Category.CHARACTER, EnumSet.of(JDBCType.Category.NUMERIC, JDBCType.Category.DATE,
                JDBCType.Category.TIME, JDBCType.Category.TIMESTAMP, JDBCType.Category.CHARACTER,
                JDBCType.Category.LONG_CHARACTER, JDBCType.Category.BINARY, JDBCType.Category.GUID)),

        LONG_CHARACTER(SSType.Category.LONG_CHARACTER, EnumSet.of(JDBCType.Category.NUMERIC, JDBCType.Category.DATE,
                JDBCType.Category.TIME, JDBCType.Category.TIMESTAMP, JDBCType.Category.CHARACTER,
                JDBCType.Category.LONG_CHARACTER, JDBCType.Category.BINARY, JDBCType.Category.CLOB)),

        NCHARACTER(SSType.Category.NCHARACTER, EnumSet.of(JDBCType.Category.NUMERIC, JDBCType.Category.CHARACTER,
                JDBCType.Category.LONG_CHARACTER, JDBCType.Category.NCHARACTER, JDBCType.Category.LONG_NCHARACTER,
                JDBCType.Category.BINARY, JDBCType.Category.DATE, JDBCType.Category.TIME, JDBCType.Category.TIMESTAMP)),

        LONG_NCHARACTER(SSType.Category.LONG_NCHARACTER, EnumSet.of(JDBCType.Category.NUMERIC,
                JDBCType.Category.CHARACTER, JDBCType.Category.LONG_CHARACTER, JDBCType.Category.NCHARACTER,
                JDBCType.Category.LONG_NCHARACTER, JDBCType.Category.BINARY, JDBCType.Category.DATE,
                JDBCType.Category.TIME, JDBCType.Category.TIMESTAMP, JDBCType.Category.CLOB, JDBCType.Category.NCLOB)),

        BINARY(SSType.Category.BINARY, EnumSet.of(JDBCType.Category.BINARY, JDBCType.Category.LONG_BINARY,
                JDBCType.Category.CHARACTER, JDBCType.Category.LONG_CHARACTER, JDBCType.Category.GUID)),

        LONG_BINARY(SSType.Category.LONG_BINARY, EnumSet.of(JDBCType.Category.BINARY, JDBCType.Category.LONG_BINARY,
                JDBCType.Category.CHARACTER, JDBCType.Category.LONG_CHARACTER, JDBCType.Category.BLOB)),

        TIMESTAMP(SSType.Category.TIMESTAMP, EnumSet.of(JDBCType.Category.BINARY, JDBCType.Category.LONG_BINARY,
                JDBCType.Category.CHARACTER)),

        XML(SSType.Category.XML, EnumSet.of(JDBCType.Category.CHARACTER, JDBCType.Category.LONG_CHARACTER,
                JDBCType.Category.CLOB, JDBCType.Category.NCHARACTER, JDBCType.Category.LONG_NCHARACTER,
                JDBCType.Category.NCLOB, JDBCType.Category.BINARY, JDBCType.Category.LONG_BINARY,
                JDBCType.Category.BLOB, JDBCType.Category.SQLXML)),

        UDT(SSType.Category.UDT, EnumSet.of(JDBCType.Category.BINARY, JDBCType.Category.LONG_BINARY,
                JDBCType.Category.CHARACTER, JDBCType.Category.GEOMETRY, JDBCType.Category.GEOGRAPHY)),

        GUID(SSType.Category.GUID, EnumSet.of(JDBCType.Category.BINARY, JDBCType.Category.CHARACTER)),

        SQL_VARIANT(SSType.Category.SQL_VARIANT, EnumSet.of(JDBCType.Category.CHARACTER, JDBCType.Category.SQL_VARIANT,
                JDBCType.Category.NUMERIC, JDBCType.Category.DATE, JDBCType.Category.TIME, JDBCType.Category.BINARY,
                JDBCType.Category.TIMESTAMP, JDBCType.Category.NCHARACTER, JDBCType.Category.GUID));

        private final SSType.Category from;
        private final EnumSet<JDBCType.Category> to;
        private static final GetterConversion[] VALUES = values();

        private GetterConversion(SSType.Category from, EnumSet<JDBCType.Category> to) {
            this.from = from;
            this.to = to;
        }

        private static final EnumMap<Category, EnumSet<JDBCType.Category>> conversionMap = new EnumMap<>(
                SSType.Category.class);

        static {
            for (SSType.Category category : SSType.Category.VALUES)
                conversionMap.put(category, EnumSet.noneOf(JDBCType.Category.class));

            for (GetterConversion conversion : VALUES)
                conversionMap.get(conversion.from).addAll(conversion.to);
        }

        static final boolean converts(SSType fromSSType, JDBCType toJDBCType) {
            return conversionMap.get(fromSSType.category).contains(toJDBCType.category);
        }
    }

    boolean convertsTo(JDBCType jdbcType) {
        return GetterConversion.converts(this, jdbcType);
    }
}


final class Nanos {
    static final int MAX_FRACTIONAL_SECONDS_SCALE = 7;
    static final int PER_SECOND = 1000000000;
    static final int PER_MAX_SCALE_INTERVAL = PER_SECOND / (int) Math.pow(10, MAX_FRACTIONAL_SECONDS_SCALE);
    static final int PER_MILLISECOND = PER_SECOND / 1000;
    static final long PER_DAY = 24 * 60 * 60 * (long) PER_SECOND;

    private Nanos() {}
}


final class UTC {

    // UTC/GMT time zone singleton.
    static final TimeZone timeZone = new SimpleTimeZone(0, "UTC");

    private UTC() {}
}


// Constants relating to the historically accepted Julian-Gregorian calendar cutover date (October 15, 1582).
//
// Used in processing SQL Server temporal data types whose date component may precede that date.
//
// Scoping these constants to a class defers their initialization to first use.
class GregorianChange {
    final static int DAYS_PER_YEAR = 365;
    // Cutover date for a pure Gregorian calendar - that is, a proleptic Gregorian calendar with
    // Gregorian leap year behavior throughout its entire range. This is the cutover date is used
    // with temporal server values, which are represented in terms of number of days relative to a
    // base date.
    static final java.util.Date PURE_CHANGE_DATE = new java.util.Date(Long.MIN_VALUE);

    // The standard Julian to Gregorian cutover date (October 15, 1582) that the JDBC temporal
    // classes (Time, Date, Timestamp) assume when converting to and from their UTC milliseconds
    // representations.
    static final java.util.Date STANDARD_CHANGE_DATE = (new GregorianCalendar(Locale.US)).getGregorianChange();

    // A hint as to the number of days since 1/1/0001, past which we do not need to
    // not rationalize the difference between SQL Server behavior (pure Gregorian)
    // and Java behavior (standard Gregorian).
    //
    // Not having to rationalize the difference has a substantial (measured) performance benefit
    // for temporal getters.
    //
    // The hint does not need to be exact, as long as it's later than the actual change date.
    static final int DAYS_SINCE_BASE_DATE_HINT = daysSinceBaseDate(1583, 1, 1);

    // Extra days that need to added to a pure gregorian date, post the gergorian
    // cut over date, to match the default julian-gregorain calendar date of java.
    static final int EXTRA_DAYS_TO_BE_ADDED;

    static {
        // This issue refers to the following bugs in java(same issue).
        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=7109480
        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6459836
        // The issue is fixed in JRE 1.7
        // and exists in all the older versions.
        // Due to the above bug, in older JVM versions(1.6 and before),
        // the date calculation is incorrect at the Gregorian cut over date.
        // i.e. the next date after Oct 4th 1582 is Oct 17th 1582, where as
        // it should have been Oct 15th 1582.
        // We intentionally do not make a check based on JRE version.
        // If we do so, our code would break if the bug is fixed in a later update
        // to an older JRE. So, we check for the existence of the bug instead.

        GregorianCalendar cal = new GregorianCalendar(Locale.US);
        cal.clear();
        cal.set(1, Calendar.FEBRUARY, 577738, 0, 0, 0);// 577738 = 1+577737(no of days since epoch that brings us to oct
                                                       // 15th 1582)
        if (cal.get(Calendar.DAY_OF_MONTH) == 15) {
            // If the date calculation is correct(the above bug is fixed),
            // post the default gregorian cut over date, the pure gregorian date
            // falls short by two days for all dates compared to julian-gregorian date.
            // so, we add two extra days for functional correctness.
            // Note: other ways, in which this issue can be fixed instead of
            // trying to detect the JVM bug is
            // a) use unoptimized code path in the function convertTemporalToObject
            // b) use cal.add api instead of cal.set api in the current optimized code path
            // In both the above approaches, the code is about 6-8 times slower,
            // resulting in an overall perf regression of about (10-30)% for perf test cases
            EXTRA_DAYS_TO_BE_ADDED = 2;
        } else
            EXTRA_DAYS_TO_BE_ADDED = 0;
    }

    /**
     * Returns the number of days elapsed from January 1 of the specified baseYear (Gregorian) to the specified
     * dayOfYear in the specified year, assuming pure Gregorian calendar rules (no Julian to Gregorian cutover).
     */
    static int daysSinceBaseDate(int year, int dayOfYear, int baseYear) {
        assert year >= 1;
        assert baseYear >= 1;
        assert dayOfYear >= 1;

        return (dayOfYear - 1) + // Days into the current year
                (year - baseYear) * DAYS_PER_YEAR + // plus whole years (in days) ...
                leapDaysBeforeYear(year) - // ... plus leap days
                leapDaysBeforeYear(baseYear);
    }

    /**
     * Returns the number of leap days that have occurred between January 1, 1AD and January 1 of the specified year,
     * assuming a Proleptic Gregorian Calendar
     */
    private static int leapDaysBeforeYear(int year) {
        assert year >= 1;

        // On leap years, the US Naval Observatory says:
        // "According to the Gregorian calendar, which is the civil calendar
        // in use today, years evenly divisible by 4 are leap years, with
        // the exception of centurial years that are not evenly divisible
        // by 400. Therefore, the years 1700, 1800, 1900 and 2100 are not
        // leap years, but 1600, 2000, and 2400 are leap years."
        //
        // So, using year 1AD as a base, we can compute the number of leap
        // days between 1AD and the specified year as follows:
        return (year - 1) / 4 - (year - 1) / 100 + (year - 1) / 400;
    }
}
