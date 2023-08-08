// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.Assertions.assertThat;

public class RxDocumentServiceRequestTest {

    private final static String DOCUMENT_DEFINITION = "{ " + "\"id\": \"%s\", " + "\"mypk\": \"%s\", "
            + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]" + "}";
    private static final String PARTITION_KEY_VALUE = "1";

    private final String DOCUMENT_URL_WITH_ID = "/dbs/IXYFAA==/colls/IXYFAOHEBPM=/docs/IXYFAOHEBPMBAAAAAAAAAA==/";
    private final String DOCUMENT_URL_WITH_NAME = "/dbs/testDB/colls/testColl/docs/testDoc/";
    private final String DOCUMENT_URL_WITH_ID_WITHOUT_SLASH = "dbs/IXYFAA==/colls/IXYFAOHEBPM=/docs/IXYFAOHEBPMBAAAAAAAAAA==/";
    private final String DOCUMENT_URL_WITH_NAME_WITHOUT_SLASH = "dbs/testDB/colls/testColl/docs/testDoc/";

    private static final String DATABASE_URL = "/dbs/IXYFAA==/";
    private static final String DOCUMENT_COLLECTION_URL = "/dbs/IXYFAA==/colls/IXYFAOHEBPM=/";
    private static final String STORED_PRCEDURE_URL = "/dbs/IXYFAA==/colls/IXYFAOHEBPM=/sprocs/IXYFAOHEBPMCAAAAAAAAgA==/";
    private static final String USER_DEFINED_FUNCTION_URL = "/dbs/IXYFAA==/colls/IXYFAOHEBPM=/udfs/IXYFAOHEBPMBAAAAAAAAYA==/";
    private static final String USER_URL = "/dbs/IXYFAA==/users/IXYFAE9ZOwA=/";
    private static final String PERMISSION_URL = "/dbs/IXYFAA==/users/IXYFAE9ZOwA=/permissions/IXYFAE9ZOwBGkyqWIsNKAA==/";
    private static final String ATTACHMENT_URL = "/dbs/IXYFAA==/colls/IXYFAOHEBPM=/docs/IXYFAOHEBPMBAAAAAAAAAA==/attachments/IXYFAOHEBPMBAAAAAAAAABJYSJk=/";
    private static final String TRIGGER_URL = "/dbs/IXYFAA==/colls/IXYFAOHEBPM=/triggers/IXYFAOHEBPMCAAAAAAAAcA==/";
    private static final String CONFLICT_URL = "/dbs/k6d9AA==/colls/k6d9ALgBmD8=/conflicts/k6d9ALgBmD8BAAAAAAAAQA==/";

    @DataProvider(name = "documentUrl")
    public Object[][] documentUrlWithId() {
        return new Object[][] { { DOCUMENT_URL_WITH_ID, DOCUMENT_URL_WITH_NAME, OperationType.Read },
                { DOCUMENT_URL_WITH_ID, DOCUMENT_URL_WITH_NAME, OperationType.Delete },
                { DOCUMENT_URL_WITH_ID, DOCUMENT_URL_WITH_NAME, OperationType.Replace },
                { DOCUMENT_URL_WITH_ID_WITHOUT_SLASH, DOCUMENT_URL_WITH_NAME_WITHOUT_SLASH, OperationType.Read },
                { DOCUMENT_URL_WITH_ID_WITHOUT_SLASH, DOCUMENT_URL_WITH_NAME_WITHOUT_SLASH, OperationType.Delete },
                { DOCUMENT_URL_WITH_ID_WITHOUT_SLASH, DOCUMENT_URL_WITH_NAME_WITHOUT_SLASH, OperationType.Replace }, };
    }

    @DataProvider(name = "resourceUrlWithOperationType")
    public Object[][] resourceOperation() {
        return new Object[][] { { DATABASE_URL, ResourceType.Database, OperationType.Read },
                { DOCUMENT_COLLECTION_URL, ResourceType.DocumentCollection, OperationType.Read },
                { STORED_PRCEDURE_URL, ResourceType.StoredProcedure, OperationType.Read },
                { USER_DEFINED_FUNCTION_URL, ResourceType.UserDefinedFunction, OperationType.Read },
                { USER_URL, ResourceType.User, OperationType.Read },
                { PERMISSION_URL, ResourceType.Permission, OperationType.Read },
                { ATTACHMENT_URL, ResourceType.Attachment, OperationType.Read },
                { TRIGGER_URL, ResourceType.Trigger, OperationType.Read },
                { CONFLICT_URL, ResourceType.Conflict, OperationType.Read },

                { DATABASE_URL, ResourceType.Database, OperationType.Create },
                { DOCUMENT_COLLECTION_URL, ResourceType.DocumentCollection, OperationType.Create },
                { STORED_PRCEDURE_URL, ResourceType.StoredProcedure, OperationType.Create },
                { USER_DEFINED_FUNCTION_URL, ResourceType.UserDefinedFunction, OperationType.Create },
                { USER_URL, ResourceType.User, OperationType.Create },
                { PERMISSION_URL, ResourceType.Permission, OperationType.Create },
                { ATTACHMENT_URL, ResourceType.Attachment, OperationType.Create },
                { TRIGGER_URL, ResourceType.Trigger, OperationType.Create },
                { CONFLICT_URL, ResourceType.Conflict, OperationType.Create },

                { DATABASE_URL, ResourceType.Database, OperationType.Delete },
                { DOCUMENT_COLLECTION_URL, ResourceType.DocumentCollection, OperationType.Delete },
                { STORED_PRCEDURE_URL, ResourceType.StoredProcedure, OperationType.Delete },
                { USER_DEFINED_FUNCTION_URL, ResourceType.UserDefinedFunction, OperationType.Delete },
                { USER_URL, ResourceType.User, OperationType.Delete },
                { PERMISSION_URL, ResourceType.Permission, OperationType.Delete },
                { ATTACHMENT_URL, ResourceType.Attachment, OperationType.Delete },
                { TRIGGER_URL, ResourceType.Trigger, OperationType.Delete },
                { CONFLICT_URL, ResourceType.Conflict, OperationType.Delete },

                { DATABASE_URL, ResourceType.Database, OperationType.Replace },
                { DOCUMENT_COLLECTION_URL, ResourceType.DocumentCollection, OperationType.Replace },
                { STORED_PRCEDURE_URL, ResourceType.StoredProcedure, OperationType.Replace },
                { USER_DEFINED_FUNCTION_URL, ResourceType.UserDefinedFunction, OperationType.Replace },
                { USER_URL, ResourceType.User, OperationType.Replace },
                { PERMISSION_URL, ResourceType.Permission, OperationType.Replace },
                { ATTACHMENT_URL, ResourceType.Attachment, OperationType.Replace },
                { TRIGGER_URL, ResourceType.Trigger, OperationType.Replace },
                { CONFLICT_URL, ResourceType.Conflict, OperationType.Replace },

                { DATABASE_URL, ResourceType.Database, OperationType.Query },
                { DOCUMENT_COLLECTION_URL, ResourceType.DocumentCollection, OperationType.Query },
                { STORED_PRCEDURE_URL, ResourceType.StoredProcedure, OperationType.Query },
                { USER_DEFINED_FUNCTION_URL, ResourceType.UserDefinedFunction, OperationType.Query },
                { USER_URL, ResourceType.User, OperationType.Query },
                { PERMISSION_URL, ResourceType.Permission, OperationType.Query },
                { ATTACHMENT_URL, ResourceType.Attachment, OperationType.Query },
                { TRIGGER_URL, ResourceType.Trigger, OperationType.Query },
                { CONFLICT_URL, ResourceType.Conflict, OperationType.Query },

                { DATABASE_URL, ResourceType.Database, OperationType.Patch },
                { DOCUMENT_COLLECTION_URL, ResourceType.DocumentCollection, OperationType.Patch },
                { STORED_PRCEDURE_URL, ResourceType.StoredProcedure, OperationType.Patch },
                { USER_DEFINED_FUNCTION_URL, ResourceType.UserDefinedFunction, OperationType.Patch },
                { USER_URL, ResourceType.User, OperationType.Patch },
                { PERMISSION_URL, ResourceType.Permission, OperationType.Patch },
                { ATTACHMENT_URL, ResourceType.Attachment, OperationType.Patch },
                { TRIGGER_URL, ResourceType.Trigger, OperationType.Patch },
                { CONFLICT_URL, ResourceType.Conflict, OperationType.Patch } };
    }

    @DataProvider(name = "resourceIdOrFullNameRequestAndOperationTypeData")
    public Object[][] resourceIdOrFullNameRequestAndOperationTypeData() {
        return new Object[][]{
                {"IXYFAA==", "dbs/testDB", ResourceType.Database, OperationType.Read},
                {"IXYFAA==", "dbs/testDB", ResourceType.Database, OperationType.Create},

                {"IXYFAOHEBPM=", "dbs/testDB/colls/testColl", ResourceType.DocumentCollection, OperationType.Read},
                {"IXYFAOHEBPM=", "dbs/testDB/colls/testColl", ResourceType.DocumentCollection, OperationType.Create},
                {"IXYFAOHEBPM=", "dbs/testDB/colls/testColl", ResourceType.DocumentCollection, OperationType.Delete},
                {"IXYFAOHEBPM=", "dbs/testDB/colls/testColl", ResourceType.DocumentCollection, OperationType.Query},

                {"IXYFAOHEBPMBAAAAAAAAAA==", "dbs/testDB/colls/testColl/docs/testDoc", ResourceType.Document, OperationType.Read},
                {"IXYFAOHEBPMBAAAAAAAAAA==", "dbs/testDB/colls/testColl/docs/testDoc", ResourceType.Document, OperationType.Create},
                {"IXYFAOHEBPMBAAAAAAAAAA==", "dbs/testDB/colls/testColl/docs/testDoc", ResourceType.Document, OperationType.Delete},
                {"IXYFAOHEBPMBAAAAAAAAAA==", "dbs/testDB/colls/testColl/docs/testDoc", ResourceType.Document, OperationType.Query},
        };
    }

    /**
     * This test case will cover various create methods through resource url with Id in detail for document resource.
     * @param documentUrlWithId Document url with id
     * @param documentUrlWithName Document url with name
     * @param operationType Operation type
     */
    @Test(groups = { "unit" }, dataProvider = "documentUrl")
    public void createWithResourceIdURL(String documentUrlWithId, String documentUrlWithName,
            OperationType operationType) {

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),
                                                                           operationType,
                                                                           ResourceType.Document,
                                                                           documentUrlWithId,
                                                                           new HashedMap<String, String>(), AuthorizationTokenType.PrimaryMasterKey);

        assertThat(request.authorizationTokenType).isEqualTo(AuthorizationTokenType.PrimaryMasterKey);
        assertThat(request.getResourceAddress()).isEqualTo("IXYFAOHEBPMBAAAAAAAAAA==");
        assertThat(request.getResourceId()).isEqualTo("IXYFAOHEBPMBAAAAAAAAAA==");

        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), operationType, "IXYFAOHEBPMBAAAAAAAAAA==", ResourceType.Document,
                new HashedMap<String, String>(), AuthorizationTokenType.PrimaryReadonlyMasterKey);
        assertThat(request.authorizationTokenType).isEqualTo(AuthorizationTokenType.PrimaryReadonlyMasterKey);
        assertThat(request.getResourceAddress()).isEqualTo("IXYFAOHEBPMBAAAAAAAAAA==");
        assertThat(request.getResourceId()).isEqualTo("IXYFAOHEBPMBAAAAAAAAAA==");

        Document document = getDocumentDefinition();
        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), operationType, document, ResourceType.Document, documentUrlWithId,
                new HashedMap<String, String>(), AuthorizationTokenType.Invalid);
        assertThat(request.authorizationTokenType).isEqualTo(AuthorizationTokenType.Invalid);
        assertThat(request.getResourceAddress()).isEqualTo("IXYFAOHEBPMBAAAAAAAAAA==");
        assertThat(request.getResourceId()).isEqualTo("IXYFAOHEBPMBAAAAAAAAAA==");
        assertThat(request.getContentAsByteArray()).isEqualTo(document.toJson().getBytes(StandardCharsets.UTF_8));

        byte[] bytes = document.toJson().getBytes(StandardCharsets.UTF_8);
        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), operationType, ResourceType.Document, documentUrlWithId, bytes,
                new HashedMap<String, String>(), AuthorizationTokenType.SecondaryMasterKey);
        assertThat(request.authorizationTokenType).isEqualTo(AuthorizationTokenType.SecondaryMasterKey);
        assertThat(request.getResourceAddress()).isEqualTo("IXYFAOHEBPMBAAAAAAAAAA==");
        assertThat(request.getResourceId()).isEqualTo("IXYFAOHEBPMBAAAAAAAAAA==");
        assertThat(request.getContentAsByteArray()).isEqualTo(bytes);

        // Creating one request without giving AuthorizationTokenType , it should take
        // PrimaryMasterKey by default
        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), operationType,
                                                  ResourceType.Document,
                                                  documentUrlWithId,
                                                  new HashedMap<String, String>());

        assertThat(request.authorizationTokenType).isEqualTo(AuthorizationTokenType.PrimaryMasterKey);
        assertThat(request.getResourceAddress()).isEqualTo("IXYFAOHEBPMBAAAAAAAAAA==");
        assertThat(request.getResourceId()).isEqualTo("IXYFAOHEBPMBAAAAAAAAAA==");

    }

    /**
     * This test case will cover various create method through resource url with name in detail for document resource.
     * @param documentUrlWithId Document url with id
     * @param documentUrlWithName Document url with name
     * @param operationType Operation type
     */
    @Test(groups = { "unit" }, dataProvider = "documentUrl")
    public void createWithResourceNameURL(String documentUrlWithId, String documentUrlWithName,
            OperationType operationType) {

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),
                                                                           operationType,
                                                                           ResourceType.Document,
                                                                           documentUrlWithName,
                                                                           new HashedMap<String, String>(), AuthorizationTokenType.PrimaryMasterKey);

        assertThat(request.authorizationTokenType).isEqualTo(AuthorizationTokenType.PrimaryMasterKey);
        assertThat(request.getResourceAddress())
                .isEqualTo(StringUtils.removeEnd(StringUtils.removeStart(documentUrlWithName, Paths.ROOT), Paths.ROOT));
        assertThat(request.getResourceId()).isNull();

        Document document = getDocumentDefinition();
        byte[] bytes = document.toJson().getBytes(StandardCharsets.UTF_8);
        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),
                                                  operationType,
                                                  ResourceType.Document,
                                                  documentUrlWithName,
                                                  bytes,
                                                  new HashedMap<String, String>(),
                                                  AuthorizationTokenType.SecondaryMasterKey);

        assertThat(request.authorizationTokenType).isEqualTo(AuthorizationTokenType.SecondaryMasterKey);
        assertThat(request.getResourceAddress())
                .isEqualTo(StringUtils.removeEnd(StringUtils.removeStart(documentUrlWithName, Paths.ROOT), Paths.ROOT));
        assertThat(request.getResourceId()).isNull();
        assertThat(request.getContentAsByteArray()).isEqualTo(bytes);

        // Creating one request without giving AuthorizationTokenType , it should take
        // PrimaryMasterKey by default
        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),
                                                  operationType,
                                                  ResourceType.Document,
                                                  documentUrlWithName,
                                                  new HashedMap<String, String>());

        assertThat(request.authorizationTokenType).isEqualTo(AuthorizationTokenType.PrimaryMasterKey);
        assertThat(request.getResourceAddress())
                .isEqualTo(StringUtils.removeEnd(StringUtils.removeStart(documentUrlWithName, Paths.ROOT), Paths.ROOT));
        assertThat(request.getResourceId()).isNull();
    }


    /**
     * This will cover sanity for most of the combination of different source with various
     * operation.
     * @param resourceUrl Resource Url
     * @param resourceType Resource Type
     * @param operationType Operation type
     */
    @Test(groups = { "unit" }, dataProvider = "resourceUrlWithOperationType")
    public void createDifferentResourceRequestWithDiffOperation(String resourceUrl, ResourceType resourceType,
            OperationType operationType) {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), operationType, resourceType, resourceUrl,
                new HashedMap<String, String>(), AuthorizationTokenType.PrimaryMasterKey);
        assertThat(resourceUrl.contains(request.getResourceAddress())).isTrue();
        assertThat(resourceUrl.contains(request.getResourceId())).isTrue();
        assertThat(request.getResourceType()).isEqualTo(resourceType);
        assertThat(request.getOperationType()).isEqualTo(operationType);
        assertThat(request.getHeaders()).isNotNull();
    }

    /**
     * This will test all the create method without request path.
     *
     * @param resourceId    Resource id
     * @param resourceType  Resource Type
     * @param operationType Operation type
     */
    @Test(groups = {"unit"}, dataProvider = "resourceIdOrFullNameRequestAndOperationTypeData")
    public void createRequestWithoutPath(String resourceId, String resourceFullName, ResourceType resourceType,
                                         OperationType operationType) {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), operationType, resourceId, resourceType, null);
        assertThat(request.getHeaders()).isNotNull();
        assertThat(request.getResourceAddress()).isEqualTo(resourceId);
        assertThat(request.getResourceId()).isEqualTo(resourceId);
        assertThat(request.getResourceType()).isEqualTo(resourceType);
        assertThat(request.getOperationType()).isEqualTo(operationType);
        assertThat(request.authorizationTokenType).isEqualTo(AuthorizationTokenType.PrimaryMasterKey);


        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), operationType, resourceId, resourceType, null, AuthorizationTokenType.ResourceToken);
        assertThat(request.getHeaders()).isNotNull();
        assertThat(request.getResourceAddress()).isEqualTo(resourceId);
        assertThat(request.getResourceId()).isEqualTo(resourceId);
        assertThat(request.getResourceType()).isEqualTo(resourceType);
        assertThat(request.getOperationType()).isEqualTo(operationType);
        assertThat(request.authorizationTokenType).isEqualTo(AuthorizationTokenType.ResourceToken);

        Document document = getDocumentDefinition();
        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), operationType, resourceId, resourceType, document, null);
        assertThat(request.getHeaders()).isNotNull();
        assertThat(request.getResourceAddress()).isEqualTo(resourceId);
        assertThat(request.getResourceId()).isEqualTo(resourceId);
        assertThat(request.getResourceType()).isEqualTo(resourceType);
        assertThat(request.getOperationType()).isEqualTo(operationType);
        assertThat(request.authorizationTokenType).isEqualTo(AuthorizationTokenType.PrimaryMasterKey);
        assertThat(request.getContentAsByteArray()).isEqualTo(document.toJson().getBytes(StandardCharsets.UTF_8));

        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), operationType, resourceId, resourceType, document, null, AuthorizationTokenType.ResourceToken);
        assertThat(request.getHeaders()).isNotNull();
        assertThat(request.getResourceAddress()).isEqualTo(resourceId);
        assertThat(request.getResourceId()).isEqualTo(resourceId);
        assertThat(request.getResourceType()).isEqualTo(resourceType);
        assertThat(request.getOperationType()).isEqualTo(operationType);
        assertThat(request.authorizationTokenType).isEqualTo(AuthorizationTokenType.ResourceToken);
        assertThat(request.getContentAsByteArray()).isEqualTo(document.toJson().getBytes(StandardCharsets.UTF_8));

        request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(), operationType, resourceFullName, resourceType);
        assertThat(request.getHeaders()).isNotNull();
        assertThat(request.getResourceAddress()).isEqualTo(resourceFullName);
        assertThat(request.getResourceId()).isNull();
        assertThat(request.getIsNameBased()).isTrue();
        assertThat(request.getResourceType()).isEqualTo(resourceType);
        assertThat(request.getOperationType()).isEqualTo(operationType);
        assertThat(request.authorizationTokenType).isEqualTo(AuthorizationTokenType.PrimaryMasterKey);

        request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(), operationType, resourceFullName, resourceType, AuthorizationTokenType.ResourceToken);
        assertThat(request.getHeaders()).isNotNull();
        assertThat(request.getResourceAddress()).isEqualTo(resourceFullName);
        assertThat(request.getResourceId()).isNull();
        assertThat(request.getIsNameBased()).isTrue();
        assertThat(request.getResourceType()).isEqualTo(resourceType);
        assertThat(request.getOperationType()).isEqualTo(operationType);
        assertThat(request.authorizationTokenType).isEqualTo(AuthorizationTokenType.ResourceToken);

        request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(), operationType, document, resourceFullName, resourceType);
        assertThat(request.getHeaders()).isNotNull();
        assertThat(request.getResourceAddress()).isEqualTo(resourceFullName);
        assertThat(request.getResourceId()).isNull();
        assertThat(request.getIsNameBased()).isTrue();
        assertThat(request.getResourceType()).isEqualTo(resourceType);
        assertThat(request.getOperationType()).isEqualTo(operationType);
        assertThat(request.authorizationTokenType).isEqualTo(AuthorizationTokenType.PrimaryMasterKey);
        assertThat(request.getContentAsByteArray()).isEqualTo(document.toJson().getBytes(StandardCharsets.UTF_8));

        request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(), operationType, document, resourceFullName, resourceType, AuthorizationTokenType.ResourceToken);
        assertThat(request.getHeaders()).isNotNull();
        assertThat(request.getResourceAddress()).isEqualTo(resourceFullName);
        assertThat(request.getResourceId()).isNull();
        assertThat(request.getIsNameBased()).isTrue();
        assertThat(request.getResourceType()).isEqualTo(resourceType);
        assertThat(request.getOperationType()).isEqualTo(operationType);
        assertThat(request.authorizationTokenType).isEqualTo(AuthorizationTokenType.ResourceToken);
        assertThat(request.getContentAsByteArray()).isEqualTo(document.toJson().getBytes(StandardCharsets.UTF_8));
    }

    @Test(groups = { "unit" }, dataProvider = "documentUrl")
    public void isValidAddress(String documentUrlWithId, String documentUrlWithName, OperationType operationType) {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),
                                                                           operationType,
                                                                           ResourceType.Document,
                                                                           documentUrlWithId,
                                                                           new HashedMap<String, String>());

        assertThat(request.isValidAddress(ResourceType.Database)).isTrue();
        assertThat(request.isValidAddress(ResourceType.DocumentCollection)).isTrue();
        assertThat(request.isValidAddress(ResourceType.Document)).isTrue();
        assertThat(request.isValidAddress(ResourceType.Unknown)).isTrue();
        assertThat(request.isValidAddress(ResourceType.User)).isFalse();
        assertThat(request.isValidAddress(ResourceType.Trigger)).isFalse();
        assertThat(request.isValidAddress(ResourceType.Offer)).isFalse();
        assertThat(request.isValidAddress(ResourceType.Permission)).isFalse();
        assertThat(request.isValidAddress(ResourceType.Attachment)).isFalse();
        assertThat(request.isValidAddress(ResourceType.StoredProcedure)).isFalse();
        assertThat(request.isValidAddress(ResourceType.Conflict)).isFalse();
        assertThat(request.isValidAddress(ResourceType.PartitionKeyRange)).isFalse();

        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),
                                                  operationType,
                                                  ResourceType.Document,
                                                  documentUrlWithName,
                                                  new HashedMap<String, String>());

        assertThat(request.isValidAddress(ResourceType.Document)).isTrue();
        assertThat(request.isValidAddress(ResourceType.Unknown)).isTrue();
        String collectionFullName = "/dbs/testDB/colls/testColl/";
        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), operationType, ResourceType.DocumentCollection, collectionFullName,
                new HashedMap<String, String>());

        assertThat(request.isValidAddress(ResourceType.DocumentCollection)).isTrue();
        assertThat(request.isValidAddress(ResourceType.Unknown)).isTrue();

        String databaseFullName = "/dbs/testDB";
        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),
                                                  operationType,
                                                  ResourceType.Database,
                                                  databaseFullName,
                                                  new HashedMap<String, String>());

        assertThat(request.isValidAddress(ResourceType.Database)).isTrue();
        assertThat(request.isValidAddress(ResourceType.Unknown)).isTrue();

        String permissionFullName = "/dbs/testDB/users/testUser/permissions/testPermission";
        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),
                                                  operationType,
                                                  ResourceType.Permission,
                                                  permissionFullName,
                                                  new HashedMap<String, String>());

        assertThat(request.isValidAddress(ResourceType.Permission)).isTrue();
        assertThat(request.isValidAddress(ResourceType.Unknown)).isTrue();

        String triggerFullName = "/dbs/testDB/colls/testUser/triggers/testTrigger";
        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),
                                                  operationType,
                                                  ResourceType.Trigger,
                                                  triggerFullName,
                                                  new HashedMap<String, String>());

        assertThat(request.isValidAddress(ResourceType.Trigger)).isTrue();
        assertThat(request.isValidAddress(ResourceType.Unknown)).isTrue();

        String attachmentFullName = "/dbs/testDB/colls/testUser/docs/testDoc/attachments/testAttachment";
        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),
                                                  operationType,
                                                  ResourceType.Attachment,
                                                  attachmentFullName,
                                                  new HashedMap<String, String>());

        assertThat(request.isValidAddress(ResourceType.Attachment)).isTrue();
        assertThat(request.isValidAddress(ResourceType.Unknown)).isTrue();
    }

    private Document getDocumentDefinition() {
        String uuid = UUID.randomUUID().toString();
        Document doc = new Document(String.format(DOCUMENT_DEFINITION, uuid, PARTITION_KEY_VALUE));
        return doc;
    }
}
