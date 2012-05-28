/**
 * Copyright 2011 Microsoft Corporation
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
package com.microsoft.windowsazure.services.table.client;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.table.TableStorageAccount;

/**
 * Table Test Base
 */
public class TableTestBase {
    public static boolean USE_DEV_FABRIC = false;
    public static final String CLOUD_ACCOUNT_HTTP = "DefaultEndpointsProtocol=http;AccountName=[ACCOUNT NAME];AccountKey=[ACCOUNT KEY]";
    public static final String CLOUD_ACCOUNT_HTTPS = "DefaultEndpointsProtocol=https;AccountName=[ACCOUNT NAME];AccountKey=[ACCOUNT KEY]";

    public static class class1 extends TableServiceEntity {
        public String A;

        public String B;

        public String C;

        public byte[] D;

        public class1() {
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
    }

    public class class2 extends TableServiceEntity {
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
    }

    public static class ComplexEntity extends TableServiceEntity {
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

        public ComplexEntity() {
            // Empty Ctor
        }

        public void assertEquality(ComplexEntity other) {
            Assert.assertEquals(this.getPartitionKey(), other.getPartitionKey());
            Assert.assertEquals(this.getRowKey(), other.getRowKey());

            Assert.assertEquals(this.getDateTime(), other.getDateTime());
            Assert.assertEquals(this.getGuid(), other.getGuid());
            Assert.assertEquals(this.getString(), other.getString());

            Assert.assertEquals(this.getDouble(), other.getDouble());
            Assert.assertEquals(this.getDoublePrimitive(), other.getDoublePrimitive());
            Assert.assertEquals(this.getInt32(), other.getInt32());
            Assert.assertEquals(this.getIntegerPrimitive(), other.getIntegerPrimitive());
            Assert.assertEquals(this.getBool(), other.getBool());
            Assert.assertEquals(this.getBoolPrimitive(), other.getBoolPrimitive());
            Assert.assertEquals(this.getInt64(), other.getInt64());
            Assert.assertEquals(this.getIntegerPrimitive(), other.getIntegerPrimitive());
            Assert.assertTrue(Arrays.equals(this.getBinary(), other.getBinary()));
            Assert.assertTrue(Arrays.equals(this.getBinaryPrimitive(), other.getBinaryPrimitive()));
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

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append(java.lang.String.format("%s:%s\n", "PK", this.getPartitionKey()));
            builder.append(java.lang.String.format("%s:%s\n", "RK", this.getRowKey()));
            builder.append(java.lang.String.format("%s:%s\n", "Timestamp", this.getTimestamp()));
            builder.append(java.lang.String.format("%s:%s\n", "etag", this.getEtag()));

            builder.append(java.lang.String.format("%s:%s\n", "DateTime", this.getDateTime()));
            builder.append(java.lang.String.format("%s:%s\n", "Bool", this.getBool()));
            builder.append(java.lang.String.format("%s:%s\n", "Binary", this.getBinary()));
            builder.append(java.lang.String.format("%s:%s\n", "Double", this.getDouble()));
            builder.append(java.lang.String.format("%s:%s\n", "Guid", this.getGuid()));
            builder.append(java.lang.String.format("%s:%s\n", "Int32", this.getInt32()));
            builder.append(java.lang.String.format("%s:%s\n", "Int64", this.getInt64()));
            builder.append(java.lang.String.format("%s:%s\n", "String", this.getString()));

            return builder.toString();
        }
    }

    public static class IgnoreOnGetter extends class1 {
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

    public static class IgnoreOnGetterAndSetter extends class1 {
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

    public static class IgnoreOnSetter extends class1 {
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

    public static class TableEnt extends TableServiceEntity {
        String TableName;

        /**
         * @return the tableName
         */
        public String getTableName() {
            return this.TableName;
        }

        /**
         * @param tableName
         *            the tableName to set
         */
        public void setTableName(String tableName) {
            this.TableName = tableName;
        }
    }

//    protected static CloudStorageAccount httpAcc;
    protected static CloudTableClient tClient;
    protected static String testSuiteTableName = generateRandomTableName();

    public static class1 generateRandomEnitity(String pk) {
        class1 ref = new class1();

        ref.setA("foo_A");
        ref.setB("foo_B");
        ref.setC("foo_C");
        ref.setD(new byte[] { 0, 1, 2 });
        ref.setPartitionKey(pk);
        ref.setRowKey(UUID.randomUUID().toString());
        return ref;
    }

    @BeforeClass
    public static void setup() throws URISyntaxException, StorageException, InvalidKeyException {

        // UNCOMMENT TO USE FIDDLER
        // System.setProperty("http.proxyHost", "localhost");
        // System.setProperty("http.proxyPort", "8888");
        // System.setProperty("https.proxyHost", "localhost");
        // System.setProperty("https.proxyPort", "8888");
        if (USE_DEV_FABRIC) {
        	tClient = TableStorageAccount.getDevelopmentStorageAccount().createCloudTableClient();
        }
        else {
        	tClient = TableStorageAccount.parse(CLOUD_ACCOUNT_HTTP).createCloudTableClient();
        }

        testSuiteTableName = generateRandomTableName();
        tClient.createTable(testSuiteTableName);
    }

    @AfterClass
    public static void teardown() throws StorageException {
        tClient.deleteTable(testSuiteTableName);
    }

    protected static String generateRandomTableName() {
        String tableName = "table" + UUID.randomUUID().toString();
        return tableName.replace("-", "");
    }
}
