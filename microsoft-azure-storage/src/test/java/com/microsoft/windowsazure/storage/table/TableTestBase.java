/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.storage.table;

import static org.junit.Assert.*;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.microsoft.windowsazure.storage.StorageCredentialsSharedAccessSignature;
import com.microsoft.windowsazure.storage.StorageException;
import com.microsoft.windowsazure.storage.TestBase;
import com.microsoft.windowsazure.storage.table.TableRequestOptions.PropertyResolver;

/**
 * Table Test Base
 */
public class TableTestBase extends TestBase {

    protected static CloudTableClient tClient;
    protected static String testSuiteTableName = generateRandomTableName();

    @BeforeClass
    public static void setup() throws URISyntaxException, StorageException, InvalidKeyException {
        tClient = createCloudTableClient();
        testSuiteTableName = generateRandomTableName();

        CloudTable table = tClient.getTableReference(testSuiteTableName);
        table.create();
    }

    @AfterClass
    public static void teardown() throws StorageException, URISyntaxException {
        CloudTable table = tClient.getTableReference(testSuiteTableName);
        table.delete();
    }

    public static String generateRandomTableName() {
        String tableName = "table" + UUID.randomUUID().toString();
        return tableName.replace("-", "");
    }

    public static Class1 generateRandomEntity(String pk) {
        Class1 ref = new Class1();

        ref.setA("foo_A");
        ref.setB("foo_B");
        ref.setC("foo_C");
        ref.setD(new byte[] { 0, 1, 2 });
        ref.setPartitionKey(pk);
        ref.setRowKey(UUID.randomUUID().toString());
        return ref;
    }

    public static CloudTableClient getTableForSas(CloudTable table, SharedAccessTablePolicy policy,
            String accessIdentifier, String startPk, String startRk, String endPk, String endRk)
            throws InvalidKeyException, StorageException {
        String sasString = table
                .generateSharedAccessSignature(policy, accessIdentifier, startPk, startRk, endPk, endRk);
        CloudTableClient client = new CloudTableClient(tClient.getEndpoint(),
                new StorageCredentialsSharedAccessSignature(sasString));
        client.setTablePayloadFormat(tClient.getTablePayloadFormat());
        return client;
    }

    public static class Class1 extends TableServiceEntity implements PropertyResolver {

        public String A;

        public String B;

        public String C;

        public byte[] D;

        public Class1() {
            // empty ctor
        }

        public synchronized String getA() {
            return this.A;
        }

        public synchronized String getB() {
            return this.B;
        }

        public synchronized String getC() {
            return this.C;
        }

        public synchronized byte[] getD() {
            return this.D;
        }

        public synchronized void setA(final String a) {
            this.A = a;
        }

        public synchronized void setB(final String b) {
            this.B = b;
        }

        public synchronized void setC(final String c) {
            this.C = c;
        }

        public synchronized void setD(final byte[] d) {
            this.D = d;
        }

        @Override
        public EdmType propertyResolver(String pk, String rk, String key, String value) {
            if (key.equals("A")) {
                return EdmType.STRING;
            }
            else if (key.equals("B")) {
                return EdmType.STRING;
            }
            else if (key.equals("C")) {
                return EdmType.STRING;
            }
            else if (key.equals("D")) {
                return EdmType.BINARY;
            }
            return null;
        }
    }

    public static class Class2 extends TableServiceEntity implements PropertyResolver {
        private String L;
        private String M;

        private String N;

        private String O;

        /**
         * @return the l
         */
        public String getL() {
            return this.L;
        }

        /**
         * @return the m
         */
        public String getM() {
            return this.M;
        }

        /**
         * @return the n
         */
        public String getN() {
            return this.N;
        }

        /**
         * @return the o
         */
        public String getO() {
            return this.O;
        }

        /**
         * @param l
         *            the l to set
         */
        public void setL(String l) {
            this.L = l;
        }

        /**
         * @param m
         *            the m to set
         */
        public void setM(String m) {
            this.M = m;
        }

        /**
         * @param n
         *            the n to set
         */
        public void setN(String n) {
            this.N = n;
        }

        /**
         * @param o
         *            the o to set
         */
        public void setO(String o) {
            this.O = o;
        }

        @Override
        public EdmType propertyResolver(String pk, String rk, String key, String value) {
            if (key.equals("L")) {
                return EdmType.STRING;
            }
            else if (key.equals("M")) {
                return EdmType.STRING;
            }
            else if (key.equals("N")) {
                return EdmType.STRING;
            }
            else if (key.equals("O")) {
                return EdmType.STRING;
            }
            return null;
        }
    }

    public static class EmptyClass extends TableServiceEntity implements PropertyResolver {

        @Override
        public EdmType propertyResolver(String pk, String rk, String key, String value) {
            return null;
        }
    }

    public static class EmptyClassDynamic extends DynamicTableEntity implements PropertyResolver {

        @Override
        public EdmType propertyResolver(String pk, String rk, String key, String value) {
            return null;
        }
    }

    class class1class2PropertyResolver implements PropertyResolver {

        @Override
        public EdmType propertyResolver(String pk, String rk, String key, String value) {
            Class1 class1Reference = new Class1();
            Class2 class2Reference = new Class2();
            EdmType type = class1Reference.propertyResolver(pk, rk, key, value);
            if (type == null) {
                type = class2Reference.propertyResolver(pk, rk, key, value);
            }
            return type;
        }
    }

    public static class StrangeDoubles extends TableServiceEntity implements PropertyResolver {
        private double regularPrimDouble = -1;
        private double nanPrimDouble = -1;
        private double positiveInfinityPrimDouble = -1;
        private double negativeInfinityPrimDouble = -1;
        private double minValuePrimDouble = -1;
        private double maxValuePrimDouble = -1;
        private double minExponentValuePrimDouble = -1;
        private double maxExponentValuePrimDouble = -1;
        private double minNormalValuePrimDouble = -1;
        private double zeroValuePrimDouble = -1;
        private double negativeZeroValuePrimDouble = -1;
        private Double regularDouble = null;
        private Double nanDouble = null;
        private Double positiveInfinityDouble = null;
        private Double negativeInfinityDouble = null;
        private Double minValueDouble = null;
        private Double maxValueDouble = null;
        private Double minExponentValueDouble = null;
        private Double maxExponentValueDouble = null;
        private Double minNormalValueDouble = null;
        private Double zeroValueDouble = null;
        private Double negativeZeroValueDouble = null;

        public void populateEntity() {
            // set primitives
            this.setRegularPrimDouble(5);
            this.setNanPrimDouble(Double.NaN);
            this.setPositiveInfinityPrimDouble(Double.POSITIVE_INFINITY);
            this.setNegativeInfinityPrimDouble(Double.NEGATIVE_INFINITY);
            this.setMinValuePrimDouble(Double.MIN_VALUE);
            this.setMaxValuePrimDouble(Double.MAX_VALUE);
            this.setMinExponentValuePrimDouble(Double.MIN_EXPONENT);
            this.setMaxExponentValuePrimDouble(Double.MAX_EXPONENT);
            this.setMinNormalValuePrimDouble(Double.MIN_NORMAL);
            this.setZeroValuePrimDouble(0.0);
            // this.setNegativeZeroValuePrimDouble(-0.0);

            // set objects
            this.setRegularDouble(new Double(5));
            this.setNanDouble(Double.NaN);
            this.setPositiveInfinityDouble(Double.POSITIVE_INFINITY);
            this.setNegativeInfinityDouble(Double.NEGATIVE_INFINITY);
            this.setMinValueDouble(Double.MIN_VALUE);
            this.setMaxValueDouble(Double.MAX_VALUE);
            this.setMinExponentValueDouble(new Double(Double.MIN_EXPONENT));
            this.setMaxExponentValueDouble(new Double(Double.MAX_EXPONENT));
            this.setMinNormalValueDouble(Double.MIN_NORMAL);
            this.setZeroValueDouble(0.0);
            // this.setNegativeZeroValueDouble(-0.0);
        }

        @Override
        public EdmType propertyResolver(String pk, String rk, String key, String value) {
            return EdmType.DOUBLE;
        }

        public void assertEquality(StrangeDoubles other) {
            assertEquals(this.getPartitionKey(), other.getPartitionKey());
            assertEquals(this.getRowKey(), other.getRowKey());

            // compare primitives
            assertEquals(this.getRegularPrimDouble(), other.getRegularPrimDouble(), 1.0e-10);
            assertEquals(this.getNanPrimDouble(), other.getNanPrimDouble(), 1.0e-10);
            assertEquals(this.getPositiveInfinityPrimDouble(), other.getPositiveInfinityPrimDouble(), 1.0e-10);
            assertEquals(this.getNegativeInfinityPrimDouble(), other.getNegativeInfinityPrimDouble(), 1.0e-10);
            assertEquals(this.getMinValuePrimDouble(), other.getMinValuePrimDouble(), 1.0e-10);
            assertEquals(this.getMaxValuePrimDouble(), other.getMaxValuePrimDouble(), 1.0e-10);
            assertEquals(this.getMinExponentValuePrimDouble(), other.getMinExponentValuePrimDouble(), 1.0e-10);
            assertEquals(this.getMaxExponentValuePrimDouble(), other.getMaxExponentValuePrimDouble(), 1.0e-10);
            assertEquals(this.getMinNormalValuePrimDouble(), other.getMinNormalValuePrimDouble(), 1.0e-10);
            assertEquals(this.getZeroValuePrimDouble(), other.getZeroValuePrimDouble(), 1.0e-10);

            // server sets -0.0 to 0.0
            // assertEquals(this.getNegativeZeroValuePrimDouble(), other.getNegativeZeroValuePrimDouble(), 1.0e-10);

            // compare objects
            assertEquals(this.getRegularDouble(), other.getRegularDouble());
            assertEquals(this.getNanDouble(), other.getNanDouble());
            assertEquals(this.getPositiveInfinityDouble(), other.getPositiveInfinityDouble());
            assertEquals(this.getNegativeInfinityDouble(), other.getNegativeInfinityDouble());
            assertEquals(this.getMinValueDouble(), other.getMinValueDouble());
            assertEquals(this.getMaxValueDouble(), other.getMaxValueDouble());
            assertEquals(this.getMinExponentValueDouble(), other.getMinExponentValueDouble());
            assertEquals(this.getMaxExponentValueDouble(), other.getMaxExponentValueDouble());
            assertEquals(this.getMinNormalValueDouble(), other.getMinNormalValueDouble());
            assertEquals(this.getZeroValueDouble(), other.getZeroValueDouble());

            // server sets -0.0 to 0.0
            // assertEquals(this.getNegativeZeroValueDouble(), other.getNegativeZeroValueDouble());
        }

        public StrangeDoubles() {
            // Empty Ctor
        }

        public double getRegularPrimDouble() {
            return regularPrimDouble;
        }

        public double getNanPrimDouble() {
            return nanPrimDouble;
        }

        public double getPositiveInfinityPrimDouble() {
            return positiveInfinityPrimDouble;
        }

        public double getNegativeInfinityPrimDouble() {
            return negativeInfinityPrimDouble;
        }

        public double getMinValuePrimDouble() {
            return minValuePrimDouble;
        }

        public double getMaxValuePrimDouble() {
            return maxValuePrimDouble;
        }

        public double getMinExponentValuePrimDouble() {
            return minExponentValuePrimDouble;
        }

        public double getMaxExponentValuePrimDouble() {
            return maxExponentValuePrimDouble;
        }

        public double getMinNormalValuePrimDouble() {
            return minNormalValuePrimDouble;
        }

        public Double getRegularDouble() {
            return regularDouble;
        }

        public Double getNanDouble() {
            return nanDouble;
        }

        public Double getPositiveInfinityDouble() {
            return positiveInfinityDouble;
        }

        public Double getNegativeInfinityDouble() {
            return negativeInfinityDouble;
        }

        public Double getMinValueDouble() {
            return minValueDouble;
        }

        public Double getMaxValueDouble() {
            return maxValueDouble;
        }

        public Double getMinExponentValueDouble() {
            return minExponentValueDouble;
        }

        public Double getMaxExponentValueDouble() {
            return maxExponentValueDouble;
        }

        public Double getMinNormalValueDouble() {
            return minNormalValueDouble;
        }

        public void setRegularPrimDouble(double regularPrimDouble) {
            this.regularPrimDouble = regularPrimDouble;
        }

        public void setNanPrimDouble(double nanPrimDouble) {
            this.nanPrimDouble = nanPrimDouble;
        }

        public void setPositiveInfinityPrimDouble(double positiveInfinityPrimDouble) {
            this.positiveInfinityPrimDouble = positiveInfinityPrimDouble;
        }

        public void setNegativeInfinityPrimDouble(double negativeInfinityPrimDouble) {
            this.negativeInfinityPrimDouble = negativeInfinityPrimDouble;
        }

        public void setMinValuePrimDouble(double minValuePrimDouble) {
            this.minValuePrimDouble = minValuePrimDouble;
        }

        public void setMaxValuePrimDouble(double maxValuePrimDouble) {
            this.maxValuePrimDouble = maxValuePrimDouble;
        }

        public void setMinExponentValuePrimDouble(double minExponentValuePrimDouble) {
            this.minExponentValuePrimDouble = minExponentValuePrimDouble;
        }

        public void setMaxExponentValuePrimDouble(double maxExponentValuePrimDouble) {
            this.maxExponentValuePrimDouble = maxExponentValuePrimDouble;
        }

        public void setMinNormalValuePrimDouble(double minNormalValuePrimDouble) {
            this.minNormalValuePrimDouble = minNormalValuePrimDouble;
        }

        public void setRegularDouble(Double regularDouble) {
            this.regularDouble = regularDouble;
        }

        public void setNanDouble(Double nanDouble) {
            this.nanDouble = nanDouble;
        }

        public void setPositiveInfinityDouble(Double positiveInfinityDouble) {
            this.positiveInfinityDouble = positiveInfinityDouble;
        }

        public void setNegativeInfinityDouble(Double negativeInfinityDouble) {
            this.negativeInfinityDouble = negativeInfinityDouble;
        }

        public void setMinValueDouble(Double minValueDouble) {
            this.minValueDouble = minValueDouble;
        }

        public void setMaxValueDouble(Double maxValueDouble) {
            this.maxValueDouble = maxValueDouble;
        }

        public void setMinExponentValueDouble(Double minExponentValueDouble) {
            this.minExponentValueDouble = minExponentValueDouble;
        }

        public void setMaxExponentValueDouble(Double maxExponentValueDouble) {
            this.maxExponentValueDouble = maxExponentValueDouble;
        }

        public void setMinNormalValueDouble(Double minNormalValueDouble) {
            this.minNormalValueDouble = minNormalValueDouble;
        }

        public double getZeroValuePrimDouble() {
            return zeroValuePrimDouble;
        }

        public double getNegativeZeroValuePrimDouble() {
            return negativeZeroValuePrimDouble;
        }

        public Double getZeroValueDouble() {
            return zeroValueDouble;
        }

        public Double getNegativeZeroValueDouble() {
            return negativeZeroValueDouble;
        }

        public void setZeroValuePrimDouble(double zeroValuePrimDouble) {
            this.zeroValuePrimDouble = zeroValuePrimDouble;
        }

        public void setNegativeZeroValuePrimDouble(double negativeZeroValuePrimDouble) {
            this.negativeZeroValuePrimDouble = negativeZeroValuePrimDouble;
        }

        public void setZeroValueDouble(Double zeroValueDouble) {
            this.zeroValueDouble = zeroValueDouble;
        }

        public void setNegativeZeroValueDouble(Double negativeZeroValueDouble) {
            this.negativeZeroValueDouble = negativeZeroValueDouble;
        }
    }

    public static class ComplexEntity extends TableServiceEntity implements PropertyResolver {
        private Date dateTime = null;
        private Boolean Bool = null;
        private boolean BoolPrimitive = false;
        private Byte[] Binary = null;
        private byte[] binaryPrimitive = null;
        private double DoublePrimitive = -1;
        private Double Double = null;
        private UUID Guid = null;
        private int IntegerPrimitive = -1;
        private Integer Int32 = null;
        private long LongPrimitive = -1L;
        private Long Int64 = null;
        private String String = null;

        @Override
        public EdmType propertyResolver(String pk, String rk, String key, String value) {
            if (key.equals("DateTime")) {
                return EdmType.DATE_TIME;
            }
            else if (key.equals("Bool")) {
                return EdmType.BOOLEAN;
            }
            else if (key.equals("BoolPrimitive")) {
                return EdmType.BOOLEAN;
            }
            else if (key.equals("Binary")) {
                return EdmType.BINARY;
            }
            else if (key.equals("BinaryPrimitive")) {
                return EdmType.BINARY;
            }
            else if (key.equals("DoublePrimitive")) {
                return EdmType.DOUBLE;
            }
            else if (key.equals("Double")) {
                return EdmType.DOUBLE;
            }
            else if (key.equals("Guid")) {
                return EdmType.GUID;
            }
            else if (key.equals("IntegerPrimitive")) {
                return EdmType.INT32;
            }
            else if (key.equals("Int32")) {
                return EdmType.INT32;
            }
            else if (key.equals("LongPrimitive")) {
                return EdmType.INT64;
            }
            else if (key.equals("Int64")) {
                return EdmType.INT64;
            }
            else if (key.equals("String")) {
                return EdmType.STRING;
            }
            return null;
        }

        public ComplexEntity() {
            // Empty Ctor
        }

        public void assertEquality(ComplexEntity other) {
            assertEquals(this.getPartitionKey(), other.getPartitionKey());
            assertEquals(this.getRowKey(), other.getRowKey());

            assertEquals(this.getDateTime().toString(), other.getDateTime().toString());

            assertEquals(this.getGuid(), other.getGuid());
            assertEquals(this.getString(), other.getString());

            assertEquals(this.getDouble(), other.getDouble(), 1.0e-10);
            assertEquals(this.getDoublePrimitive(), other.getDoublePrimitive(), 1.0e-10);
            assertEquals(this.getInt32(), other.getInt32());
            assertEquals(this.getIntegerPrimitive(), other.getIntegerPrimitive());
            assertEquals(this.getBool(), other.getBool());
            assertEquals(this.getBoolPrimitive(), other.getBoolPrimitive());
            assertEquals(this.getInt64(), other.getInt64());
            assertEquals(this.getIntegerPrimitive(), other.getIntegerPrimitive());
            assertTrue(Arrays.equals(this.getBinary(), other.getBinary()));
            assertTrue(Arrays.equals(this.getBinaryPrimitive(), other.getBinaryPrimitive()));
        }

        protected void assertDateApproxEquals(Date expected, Date actual, int deltaInMs) {
            if (expected == null || actual == null) {
                assertEquals(expected, actual);
            }
            else {
                long diffInMilliseconds = Math.abs(expected.getTime() - actual.getTime());
                if (diffInMilliseconds > deltaInMs) {
                    assertEquals(expected, actual);
                }
            }
        }

        /**
         * @return the binary
         */
        public Byte[] getBinary() {
            return this.Binary;
        }

        /**
         * @return the binaryPrimitive
         */
        public byte[] getBinaryPrimitive() {
            return this.binaryPrimitive;
        }

        /**
         * @return the bool
         */
        public Boolean getBool() {
            return this.Bool;
        }

        /**
         * @return the bool
         */
        public boolean getBoolPrimitive() {
            return this.BoolPrimitive;
        }

        /**
         * @return the dateTime
         */
        public Date getDateTime() {
            return this.dateTime;
        }

        /**
         * @return the double
         */
        public Double getDouble() {
            return this.Double;
        }

        /**
         * @return the doublePrimitive
         */
        public double getDoublePrimitive() {
            return this.DoublePrimitive;
        }

        /**
         * @return the guid
         */
        public UUID getGuid() {
            return this.Guid;
        }

        /**
         * @return the int32
         */
        public Integer getInt32() {
            return this.Int32;
        }

        /**
         * @return the int64
         */
        public Long getInt64() {
            return this.Int64;
        }

        /**
         * @return the integerPrimitive
         */
        public int getIntegerPrimitive() {
            return this.IntegerPrimitive;
        }

        /**
         * @return the longPrimitive
         */
        public long getLongPrimitive() {
            return this.LongPrimitive;
        }

        /**
         * @return the string
         */
        public String getString() {
            return this.String;
        }

        public void populateEntity() {
            this.setBinary(new Byte[] { 1, 2, 3, 4 });
            this.setBinaryPrimitive(new byte[] { 1, 2, 3, 4 });
            this.setBool(true);
            this.setBoolPrimitive(true);
            this.setDateTime(new Date());
            this.setDouble(2342.2342);
            this.setDoublePrimitive(2349879.2342);
            this.setInt32(2342);
            this.setInt64((long) 87987987);
            this.setIntegerPrimitive(2342);
            this.setLongPrimitive(87987987);
            this.setGuid(UUID.randomUUID());
            this.setString("foo");
        }

        /**
         * @param binary
         *            the binary to set
         */
        public void setBinary(final Byte[] binary) {
            this.Binary = binary;
        }

        /**
         * @param binaryPrimitive
         *            the binaryPrimitive to set
         */
        public void setBinaryPrimitive(byte[] binaryPrimitive) {
            this.binaryPrimitive = binaryPrimitive;
        }

        /**
         * @param bool
         *            the bool to set
         */
        public void setBool(final Boolean bool) {
            this.Bool = bool;
        }

        /**
         * @param boolPrimitive
         *            the boolPrimitive to set
         */
        public void setBoolPrimitive(boolean boolPrimitive) {
            this.BoolPrimitive = boolPrimitive;
        }

        /**
         * @param dateTime
         *            the dateTime to set
         */
        public void setDateTime(final Date dateTime) {
            this.dateTime = dateTime;
        }

        /**
         * @param d
         *            the double to set
         */
        public void setDouble(final Double d) {
            this.Double = d;
        }

        /**
         * @param doublePrimitive
         *            the doublePrimitive to set
         */
        public void setDoublePrimitive(double doublePrimitive) {
            this.DoublePrimitive = doublePrimitive;
        }

        /**
         * @param guid
         *            the guid to set
         */
        public void setGuid(final UUID guid) {
            this.Guid = guid;
        }

        /**
         * @param int32
         *            the int32 to set
         */
        public void setInt32(final Integer int32) {
            this.Int32 = int32;
        }

        /**
         * @param int64
         *            the int64 to set
         */
        public void setInt64(final Long int64) {
            this.Int64 = int64;
        }

        /**
         * @param integerPrimitive
         *            the integerPrimitive to set
         */
        public void setIntegerPrimitive(int integerPrimitive) {
            this.IntegerPrimitive = integerPrimitive;
        }

        /**
         * @param longPrimitive
         *            the longPrimitive to set
         */
        public void setLongPrimitive(long longPrimitive) {
            this.LongPrimitive = longPrimitive;
        }

        /**
         * @param string
         *            the string to set
         */
        public void setString(final String string) {
            this.String = string;
        }
    }

    public static class IgnoreOnGetter extends Class1 {
        private String tString = null;

        /**
         * @return the string
         */
        @Ignore
        public String getIgnoreString() {
            return this.tString;
        }

        /**
         * @param string
         *            the string to set
         */

        public void setIgnoreString(final String string) {
            this.tString = string;
        }
    }

    public static class IgnoreOnGetterAndSetter extends Class1 {
        private String tString = null;

        /**
         * @return the string
         */
        @Ignore
        public String getIgnoreString() {
            return this.tString;
        }

        /**
         * @param string
         *            the string to set
         */
        @Ignore
        public void setIgnoreString(final String string) {
            this.tString = string;
        }
    }

    public static class IgnoreOnSetter extends Class1 {
        private String tString = null;

        /**
         * @return the string
         */
        public String getIgnoreString() {
            return this.tString;
        }

        /**
         * @param string
         *            the string to set
         */
        @Ignore
        public void setIgnoreString(final String string) {
            this.tString = string;
        }
    }

    public static class StoreAsEntity extends ComplexEntity {
        private String storeAsString = null;

        /**
         * @return the string
         */
        @StoreAs(name = "String")
        public String getStoreAsString() {
            return this.storeAsString;
        }

        /**
         * @param string
         *            the string to set
         */
        @StoreAs(name = "String")
        public void setStoreAsString(final String string) {
            this.storeAsString = string;
        }
    }

    public static class InvalidStoreAsEntity extends ComplexEntity {
        private String storeAsString = null;

        /**
         * @return the string
         */
        @StoreAs(name = "PartitionKey")
        public String getStoreAsString() {
            return this.storeAsString;
        }

        /**
         * @param string
         *            the string to set
         */
        @StoreAs(name = "PartitionKey")
        public void setStoreAsString(final String string) {
            this.storeAsString = string;
        }
    }

}
