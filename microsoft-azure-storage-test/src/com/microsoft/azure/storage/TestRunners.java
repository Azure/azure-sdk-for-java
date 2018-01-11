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
package com.microsoft.azure.storage;

import com.microsoft.azure.storage.analytics.CloudAnalyticsClientTests;
import com.microsoft.azure.storage.blob.BlobOutputStreamTests;
import com.microsoft.azure.storage.blob.CloudAppendBlobTests;
import com.microsoft.azure.storage.blob.CloudBlobClientEncryptionTests;
import com.microsoft.azure.storage.blob.CloudBlobClientTests;
import com.microsoft.azure.storage.blob.CloudBlobContainerTests;
import com.microsoft.azure.storage.blob.CloudBlobDirectoryTests;
import com.microsoft.azure.storage.blob.CloudBlobServerEncryptionTests;
import com.microsoft.azure.storage.blob.CloudBlockBlobTests;
import com.microsoft.azure.storage.blob.CloudPageBlobTests;
import com.microsoft.azure.storage.blob.LeaseTests;
import com.microsoft.azure.storage.blob.SasTests;
import com.microsoft.azure.storage.file.CloudFileClientTests;
import com.microsoft.azure.storage.file.CloudFileDirectoryTests;
import com.microsoft.azure.storage.file.CloudFileServerEncryptionTests;
import com.microsoft.azure.storage.file.CloudFileShareTests;
import com.microsoft.azure.storage.file.CloudFileTests;
import com.microsoft.azure.storage.file.FileSasTests;
import com.microsoft.azure.storage.queue.CloudQueueClientGB18030Test;
import com.microsoft.azure.storage.queue.CloudQueueClientTests;
import com.microsoft.azure.storage.queue.CloudQueueEncryptionTests;
import com.microsoft.azure.storage.queue.CloudQueueTests;
import com.microsoft.azure.storage.table.TableBatchOperationTests;
import com.microsoft.azure.storage.table.TableClientTests;
import com.microsoft.azure.storage.table.TableDateTests;
import com.microsoft.azure.storage.table.TableEncryptionTests;
import com.microsoft.azure.storage.table.TableEscapingTests;
import com.microsoft.azure.storage.table.TableODataTests;
import com.microsoft.azure.storage.table.TableOperationTests;
import com.microsoft.azure.storage.table.TableQueryTests;
import com.microsoft.azure.storage.table.TableSerializerTests;
import com.microsoft.azure.storage.table.TableTests;

import org.junit.experimental.categories.Categories;
import org.junit.experimental.categories.Categories.ExcludeCategory;
import org.junit.experimental.categories.Categories.IncludeCategory;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Contains the various test suites and test categories used to run JUnit tests
 *
 * To run with junit from command line:
 * 1. Install junit and add it to the classpath.
 * 2. Add any project dependencies to the classpath. (ex CLASSPATH=%CLASSPATH%;{path to dependency};)
 * 3. If you have the compiled source jar and compiled test jar, add them to the classpath. If you do not, either run
 * mvn jar:jar and mvn jar:test-jar which will add a compiled source and compiled test jar to the target directory or
 * run mvn install -DskipTests=true which will add those jars to both the target directory and your local repository
 * directory.
 * 4. Suite: java org.junit.runner.JUnitCore com.microsoft.azure.storage.TestRunners$FastTestSuite
 * 5. Class: java org.junit.runner.JUnitCore com.microsoft.azure.storage.TableOperationTests
 * 6. Test: can't do this natively; must make a runner
 *
 * To run with maven from command line:
 * 1. Suite: mvn test -DrunSuite="TestRunners.FastTestSuite"
 * 2. Class: mvn test -Dtest=TableOperationTests
 * 3. Test: mvn test -Dtest=TableOperationTests$testDelete
 *
 * To run with eclipse:
 * 1. Right click the suite/class/test you'd like to run in the package explorer and click Run As > JUnit Test
 *
 * Other notes about suites:
 * 1. Can't include or exclude multipe categories, instead make two suites and merge them into one.
 * 2. With maven you cannot (easily) run tests from inside a jar.
 * 3. To make new suites:
 * Make a new public static class with the name of the suite
 * Specify which test classes to look at (ex, core, blob, queue, table or all) with @SuiteClasses
 * Optional: Specify which category (only one!) to include within those test classes with @IncludeCategory
 * Optional: Specify which category (only one!) to ignore within those test classes with @ExcludeCategory
 * If using categories, specify @RunWith(Categories.class), otherwise @RunWith(Suite.class)
 * 4. To make new categories:
 * Make a public interface with the category name you'd like to use
 * Annotate test classes AND/OR test methods with that category using @Category(categoryName.class)
 * Follow the instructions right above in 3 to use these categories with a suite
 */
public class TestRunners {

    // Speed test category
    public interface SlowTests {
    }

    // Tenant type test categories
    public interface DevStoreTests {
    }

    public interface CloudTests {
    }

    public interface DevFabricTests {
    }

    public interface PremiumBlobTests {
    }

    // Test suites
    @RunWith(Suite.class)
    @SuiteClasses({ AccountSasTests.class, EventFiringTests.class, GenericTests.class, LoggerTests.class,
        MaximumExecutionTimeTests.class, SecondaryTests.class,  ServicePropertiesTests.class, StorageAccountTests.class,
        StorageUriTests.class })
    public static class CoreTestSuite {
    }

    @RunWith(Suite.class)
    @SuiteClasses({ BlobOutputStreamTests.class, CloudBlobClientTests.class, CloudBlobContainerTests.class,
            CloudBlobDirectoryTests.class, CloudAppendBlobTests.class, CloudBlockBlobTests.class,
            CloudPageBlobTests.class, CloudBlobClientEncryptionTests.class, CloudBlobServerEncryptionTests.class,
            LeaseTests.class, SasTests.class })
    public static class BlobTestSuite {
    }

    @RunWith(Suite.class)
    @SuiteClasses({ CloudQueueClientGB18030Test.class, CloudQueueClientTests.class, CloudQueueEncryptionTests.class,
            CloudQueueTests.class })
    public static class QueueTestSuite {
    }

    @RunWith(Suite.class)
    @SuiteClasses({ TableBatchOperationTests.class, TableClientTests.class, TableDateTests.class,
            TableEncryptionTests.class, TableEscapingTests.class, TableODataTests.class, TableOperationTests.class,
            TableQueryTests.class, TableSerializerTests.class, TableTests.class })
    public static class TableTestSuite {
    }

    @RunWith(Suite.class)
    @SuiteClasses({ CloudFileClientTests.class, CloudFileDirectoryTests.class, CloudFileServerEncryptionTests.class,
        CloudFileShareTests.class, CloudFileTests.class, FileSasTests.class })
    public static class FileTestSuite {
    }

    @RunWith(Suite.class)
    @SuiteClasses({ CloudAnalyticsClientTests.class })
    public static class AnalyticsTestSuite {
    }

    @RunWith(Suite.class)
    @SuiteClasses({ CloudBlobClientEncryptionTests.class, CloudBlobServerEncryptionTests.class,
            CloudQueueEncryptionTests.class, TableEncryptionTests.class })
    public static class EncryptionTestSuite {
    }

    @RunWith(Suite.class)
    @SuiteClasses({ CoreTestSuite.class, BlobTestSuite.class, QueueTestSuite.class, TableTestSuite.class,
            FileTestSuite.class, AnalyticsTestSuite.class })
    public static class AllTestSuite {
    }

    // Test suites with categories:

    @RunWith(Categories.class)
    @IncludeCategory(DevFabricTests.class)
    @ExcludeCategory(SlowTests.class)
    @SuiteClasses(AllTestSuite.class)
    public static class DevFabricFastTestSuite {
    }

    @RunWith(Categories.class)
    @IncludeCategory(DevFabricTests.class)
    @ExcludeCategory(SecondaryTests.class)
    @SuiteClasses(AllTestSuite.class)
    public static class DevFabricNoSecondarySuite {
    }

    @RunWith(Categories.class)
    @ExcludeCategory(SlowTests.class)
    @SuiteClasses(AllTestSuite.class)
    public static class FastTestSuite {
    }

    @RunWith(Categories.class)
    @IncludeCategory(PremiumBlobTests.class)
    @SuiteClasses(AllTestSuite.class)
    public static class PremiumBlobTestSuite {
    }
}