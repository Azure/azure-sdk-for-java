/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb.internal;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.microsoft.azure.cosmosdb.rx.internal.BadRequestException;
import com.microsoft.azure.cosmosdb.rx.internal.RMResources;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceRequest;

/**
 * Used internally to provide utility methods to work with the resource's path in the Azure Cosmos DB database service.
 */
public class PathsHelper {
    public static String generatePath(ResourceType resourceType, RxDocumentServiceRequest request, boolean isFeed) {
        if (request.getIsNameBased()) {
            return request.getPath();
        } else {
        return PathsHelper.generatePath(resourceType, request.getResourceId(), isFeed);
    }
    }
    
    public static String generatePath(ResourceType resourceType, String ownerOrResourceId, boolean isFeed) {
        if (isFeed && (ownerOrResourceId == null || ownerOrResourceId.isEmpty()) && 
            resourceType != ResourceType.Database && 
            resourceType != ResourceType.Offer && 
            resourceType != ResourceType.MasterPartition && 
            resourceType != ResourceType.ServerPartition && 
            resourceType != ResourceType.DatabaseAccount &&
                resourceType != ResourceType.Topology) {
            throw new IllegalStateException("Invalid resource type");
        }

        if (isFeed && resourceType == ResourceType.Database) {
            return Paths.DATABASES_PATH_SEGMENT;
        } else if (resourceType == ResourceType.Database) {
            return Paths.DATABASES_PATH_SEGMENT + "/" + ownerOrResourceId;
        } else if (isFeed && resourceType == ResourceType.DocumentCollection) {
            ResourceId documentCollectionId = ResourceId.parse(ownerOrResourceId);

            return Paths.DATABASES_PATH_SEGMENT + "/" + documentCollectionId.getDatabaseId().toString() + "/" +
                    Paths.COLLECTIONS_PATH_SEGMENT;
        } else if (resourceType == ResourceType.DocumentCollection) {
            ResourceId documentCollectionId = ResourceId.parse(ownerOrResourceId);

            return Paths.DATABASES_PATH_SEGMENT + "/" + documentCollectionId.getDatabaseId().toString() + "/" +
                    Paths.COLLECTIONS_PATH_SEGMENT + "/" + documentCollectionId.getDocumentCollectionId().toString();
        } else if (isFeed && resourceType == ResourceType.Offer) {
            return Paths.OFFERS_PATH_SEGMENT;
        } else if (resourceType == ResourceType.Offer) {
            return Paths.OFFERS_PATH_SEGMENT + "/" + ownerOrResourceId;
        } else if (isFeed && resourceType == ResourceType.StoredProcedure) {
            ResourceId documentCollectionId = ResourceId.parse(ownerOrResourceId);

            return
                    Paths.DATABASES_PATH_SEGMENT + "/" + documentCollectionId.getDatabaseId().toString() + "/" +
                            Paths.COLLECTIONS_PATH_SEGMENT + "/" + documentCollectionId.getDocumentCollectionId().toString() + "/" +
                            Paths.STORED_PROCEDURES_PATH_SEGMENT;
        } else if (resourceType == ResourceType.StoredProcedure) {
            ResourceId storedProcedureId = ResourceId.parse(ownerOrResourceId);

            return Paths.DATABASES_PATH_SEGMENT + "/" + storedProcedureId.getDatabaseId().toString() + "/" +
                    Paths.COLLECTIONS_PATH_SEGMENT + "/" + storedProcedureId.getDocumentCollectionId().toString() + "/" +
                    Paths.STORED_PROCEDURES_PATH_SEGMENT + "/" + storedProcedureId.getStoredProcedureId().toString();
        } else if (isFeed && resourceType == ResourceType.UserDefinedFunction) {
            ResourceId documentCollectionId = ResourceId.parse(ownerOrResourceId);

            return
                    Paths.DATABASES_PATH_SEGMENT + "/" + documentCollectionId.getDatabaseId().toString() + "/" +
                            Paths.COLLECTIONS_PATH_SEGMENT + "/" + documentCollectionId.getDocumentCollectionId().toString() + "/" +
                            Paths.USER_DEFINED_FUNCTIONS_PATH_SEGMENT;
        } else if (resourceType == ResourceType.UserDefinedFunction) {
            ResourceId functionId = ResourceId.parse(ownerOrResourceId);

            return Paths.DATABASES_PATH_SEGMENT + "/" + functionId.getDatabaseId().toString() + "/" +
                    Paths.COLLECTIONS_PATH_SEGMENT + "/" + functionId.getDocumentCollectionId().toString() + "/" +
                    Paths.USER_DEFINED_FUNCTIONS_PATH_SEGMENT + "/" + functionId.getUserDefinedFunctionId().toString();
        } else if (isFeed && resourceType == ResourceType.Trigger) {
            ResourceId documentCollectionId = ResourceId.parse(ownerOrResourceId);

            return
                    Paths.DATABASES_PATH_SEGMENT + "/" + documentCollectionId.getDatabaseId().toString() + "/" +
                            Paths.COLLECTIONS_PATH_SEGMENT + "/" + documentCollectionId.getDocumentCollectionId().toString() + "/" +
                            Paths.TRIGGERS_PATH_SEGMENT;
        } else if (resourceType == ResourceType.Trigger) {
            ResourceId triggerId = ResourceId.parse(ownerOrResourceId);

            return Paths.DATABASES_PATH_SEGMENT + "/" + triggerId.getDatabaseId().toString() + "/" +
                    Paths.COLLECTIONS_PATH_SEGMENT + "/" + triggerId.getDocumentCollectionId().toString() + "/" +
                    Paths.TRIGGERS_PATH_SEGMENT + "/" + triggerId.getTriggerId().toString();
        } else if (isFeed && resourceType == ResourceType.Conflict) {
            ResourceId documentCollectionId = ResourceId.parse(ownerOrResourceId);

            return
                    Paths.DATABASES_PATH_SEGMENT + "/" + documentCollectionId.getDatabaseId().toString() + "/" +
                            Paths.COLLECTIONS_PATH_SEGMENT + "/" + documentCollectionId.getDocumentCollectionId().toString() + "/" +
                            Paths.CONFLICTS_PATH_SEGMENT;
        } else if (resourceType == ResourceType.Conflict) {
            ResourceId conflictId = ResourceId.parse(ownerOrResourceId);

            return Paths.DATABASES_PATH_SEGMENT + "/" + conflictId.getDatabaseId().toString() + "/" +
                    Paths.COLLECTIONS_PATH_SEGMENT + "/" + conflictId.getDocumentCollectionId().toString() + "/" +
                    Paths.CONFLICTS_PATH_SEGMENT + "/" + conflictId.getConflictId().toString();
        } else if (isFeed && resourceType == ResourceType.PartitionKeyRange) {
            ResourceId documentCollectionId = ResourceId.parse(ownerOrResourceId);

            return Paths.DATABASES_PATH_SEGMENT + "/" + documentCollectionId.getDatabaseId().toString() + "/" + 
                    Paths.COLLECTIONS_PATH_SEGMENT + "/" + 
                    documentCollectionId.getDocumentCollectionId().toString() + "/" + 
                    Paths.PARTITION_KEY_RANGE_PATH_SEGMENT;
        } else if (resourceType == ResourceType.PartitionKeyRange) {
            ResourceId partitionKeyRangeId = ResourceId.parse(ownerOrResourceId);

            return Paths.DATABASES_PATH_SEGMENT + "/" + partitionKeyRangeId.getDatabaseId().toString() + "/" + 
                    Paths.COLLECTIONS_PATH_SEGMENT + "/" + partitionKeyRangeId.getDocumentCollectionId().toString() + "/" + 
                    Paths.PARTITION_KEY_RANGE_PATH_SEGMENT + "/" + partitionKeyRangeId.getPartitionKeyRangeId().toString();
        } else if (isFeed && resourceType == ResourceType.Attachment) {
            ResourceId documentCollectionId = ResourceId.parse(ownerOrResourceId);

            return
                    Paths.DATABASES_PATH_SEGMENT + "/" + documentCollectionId.getDatabaseId().toString() + "/" +
                            Paths.COLLECTIONS_PATH_SEGMENT + "/" + documentCollectionId.getDocumentCollectionId().toString() + "/" +
                            Paths.DOCUMENTS_PATH_SEGMENT + "/" + documentCollectionId.getDocumentId().toString() + "/" +
                            Paths.ATTACHMENTS_PATH_SEGMENT;
        } else if (resourceType == ResourceType.Attachment) {
            ResourceId attachmentId = ResourceId.parse(ownerOrResourceId);

            return Paths.DATABASES_PATH_SEGMENT + "/" + attachmentId.getDatabaseId().toString() + "/" +
                    Paths.COLLECTIONS_PATH_SEGMENT + "/" + attachmentId.getDocumentCollectionId().toString() + "/" +
                    Paths.DOCUMENTS_PATH_SEGMENT + "/" + attachmentId.getDocumentId().toString() + "/" +
                    Paths.ATTACHMENTS_PATH_SEGMENT + "/" + attachmentId.getAttachmentId().toString();
        } else if (isFeed && resourceType == ResourceType.User) {
            return
                    Paths.DATABASES_PATH_SEGMENT + "/" + ownerOrResourceId + "/" +
                            Paths.USERS_PATH_SEGMENT;
        } else if (resourceType == ResourceType.User) {
            ResourceId userId = ResourceId.parse(ownerOrResourceId);

            return Paths.DATABASES_PATH_SEGMENT + "/" + userId.getDatabaseId().toString() + "/" +
                    Paths.USERS_PATH_SEGMENT + "/" + userId.getUserId().toString();
        } else if (isFeed && resourceType == ResourceType.Permission) {
            ResourceId userId = ResourceId.parse(ownerOrResourceId);

            return
                    Paths.DATABASES_PATH_SEGMENT + "/" + userId.getDatabaseId().toString() + "/" +
                            Paths.USERS_PATH_SEGMENT + "/" + userId.getUserId().toString() + "/" +
                            Paths.PERMISSIONS_PATH_SEGMENT;
        } else if (resourceType == ResourceType.Permission) {
            ResourceId permissionId = ResourceId.parse(ownerOrResourceId);

            return Paths.DATABASES_PATH_SEGMENT + "/" + permissionId.getDatabaseId().toString() + "/" +
                    Paths.USERS_PATH_SEGMENT + "/" + permissionId.getUserId().toString() + "/" +
                    Paths.PERMISSIONS_PATH_SEGMENT + "/" + permissionId.getPermissionId().toString();
        } else if (isFeed && resourceType == ResourceType.Document) {
            ResourceId documentCollectionId = ResourceId.parse(ownerOrResourceId);

            return
                    Paths.DATABASES_PATH_SEGMENT + "/" + documentCollectionId.getDatabaseId().toString() + "/" +
                            Paths.COLLECTIONS_PATH_SEGMENT + "/" + documentCollectionId.getDocumentCollectionId().toString() + "/" +
                            Paths.DOCUMENTS_PATH_SEGMENT;
        } else if (resourceType == ResourceType.Document) {
            ResourceId documentId = ResourceId.parse(ownerOrResourceId);

            return Paths.DATABASES_PATH_SEGMENT + "/" + documentId.getDatabaseId().toString() + "/" +
                    Paths.COLLECTIONS_PATH_SEGMENT + "/" + documentId.getDocumentCollectionId().toString() + "/" +
                    Paths.DOCUMENTS_PATH_SEGMENT + "/" + documentId.getDocumentId().toString();
        } else if (isFeed && resourceType == ResourceType.MasterPartition) {
            return Paths.PARTITIONS_PATH_SEGMENT;
        } else if (resourceType == ResourceType.MasterPartition) {
            return Paths.PARTITIONS_PATH_SEGMENT + "/" + ownerOrResourceId;
        } else if (isFeed && resourceType == ResourceType.ServerPartition) {
            return Paths.PARTITIONS_PATH_SEGMENT;
        } else if (resourceType == ResourceType.ServerPartition) {
            return Paths.PARTITIONS_PATH_SEGMENT + "/" + ownerOrResourceId;
        } else if (isFeed && resourceType == ResourceType.Topology) {
            return Paths.TOPOLOGY_PATH_SEGMENT;
        } else if (resourceType == ResourceType.Topology) {
            return Paths.TOPOLOGY_PATH_SEGMENT + "/" + ownerOrResourceId;
        } else if (isFeed && resourceType == ResourceType.DatabaseAccount) {
            return Paths.DATABASE_ACCOUNT_PATH_SEGMENT;
        } else if (resourceType == ResourceType.DatabaseAccount) {
            return Paths.DATABASE_ACCOUNT_PATH_SEGMENT + "/" + ownerOrResourceId;
        }

        String errorMessage = "invalid resource type";
        throw new IllegalStateException(errorMessage);
    }

    public static PathInfo parsePathSegments(String resourceUrl) {
        String[] segments = StringUtils.strip(resourceUrl, "/").split("/");
        if (segments == null || segments.length < 1) {
            return null;
        }

        int uriSegmentsCount = segments.length;
        String segmentOne = StringUtils.strip(segments[uriSegmentsCount - 1], "/");
        String segmentTwo = (uriSegmentsCount >= 2) ? StringUtils.strip(segments[uriSegmentsCount - 2], "/")
                : StringUtils.EMPTY;

        // handle name based operation
        if (uriSegmentsCount >= 2) {
            // parse the databaseId, if failed, it is name based routing
            // mediaId is special, we will treat it always as id based.
            if (Paths.MEDIA_PATH_SEGMENT.compareTo(segments[0]) != 0
                    && Paths.OFFERS_PATH_SEGMENT.compareTo(segments[0]) != 0
                    && Paths.PARTITIONS_PATH_SEGMENT.compareTo(segments[0]) != 0
                    && Paths.DATABASE_ACCOUNT_PATH_SEGMENT.compareTo(segments[0]) != 0) {
                Pair<Boolean, ResourceId> result = ResourceId.tryParse(segments[1]);
                if (!result.getLeft() || !result.getRight().isDatabaseId()) {
                    return parseNameSegments(resourceUrl, segments);
                }
            }

        }

        // Feed paths have odd number of segments
        if ((uriSegmentsCount % 2 != 0) && isResourceType(segmentOne)) {
            return new PathInfo(true, segmentOne,
                    segmentOne.compareToIgnoreCase(Paths.DATABASES_PATH_SEGMENT) != 0 ? segmentTwo : StringUtils.EMPTY,
                    false);
        } else if (isResourceType(segmentTwo)) {
            return new PathInfo(false, segmentTwo, segmentOne, false);
        }

        return null;
    }

    public static PathInfo parseNameSegments(String resourceUrl, String[] segments) {
        if (segments == null || segments.length < 1) {
            return null;
        }

        if (segments.length % 2 == 0) {
            // even number, assume it is individual resource
            if (isResourceType(segments[segments.length - 2])) {
                return new PathInfo(false, segments[segments.length - 2],
                        StringEscapeUtils.unescapeJava(StringUtils.strip(resourceUrl, Paths.ROOT)), true);
            }
        } else {
            // odd number, assume it is feed request
            if (isResourceType(segments[segments.length - 1])) {
                return new PathInfo(true, segments[segments.length - 1],
                        StringEscapeUtils.unescapeJava(StringUtils.strip(
                                resourceUrl.substring(0,
                                        StringUtils.removeEnd(resourceUrl, Paths.ROOT).lastIndexOf(Paths.ROOT)),
                                Paths.ROOT)),
                        true);
            }
        }

        return null;
    }
        
    private static boolean isResourceType(String resourcePathSegment) {
        if (StringUtils.isEmpty(resourcePathSegment)) {
            return false;
        }
       
        switch (resourcePathSegment.toLowerCase()) {
            case Paths.ATTACHMENTS_PATH_SEGMENT:
            case Paths.COLLECTIONS_PATH_SEGMENT:
            case Paths.DATABASES_PATH_SEGMENT:
            case Paths.PERMISSIONS_PATH_SEGMENT:
            case Paths.USERS_PATH_SEGMENT:
            case Paths.DOCUMENTS_PATH_SEGMENT:
            case Paths.STORED_PROCEDURES_PATH_SEGMENT:
            case Paths.TRIGGERS_PATH_SEGMENT:
            case Paths.USER_DEFINED_FUNCTIONS_PATH_SEGMENT:
            case Paths.CONFLICTS_PATH_SEGMENT:
            case Paths.MEDIA_PATH_SEGMENT:
            case Paths.OFFERS_PATH_SEGMENT:
            case Paths.PARTITIONS_PATH_SEGMENT:
            case Paths.DATABASE_ACCOUNT_PATH_SEGMENT:
            case Paths.TOPOLOGY_PATH_SEGMENT:
            case Paths.PARTITION_KEY_RANGE_PATH_SEGMENT:
            case Paths.SCHEMAS_PATH_SEGMENT:
                return true;
            default:
                return false;
        }
    }

    public static String generatePathForNameBased(ResourceType resourceType, String resourceOwnerFullName, String resourceName) {
        switch (resourceType) {
            case Database:
                return Paths.DATABASES_PATH_SEGMENT + "/" + resourceName;
            case DocumentCollection:
                return resourceOwnerFullName + "/" + Paths.COLLECTIONS_PATH_SEGMENT + "/" + resourceName;
            case StoredProcedure:
                return resourceOwnerFullName + "/" + Paths.STORED_PROCEDURES_PATH_SEGMENT + "/" + resourceName;
            case UserDefinedFunction:
                return resourceOwnerFullName + "/" + Paths.USER_DEFINED_FUNCTIONS_PATH_SEGMENT + "/" + resourceName;
            case Trigger:
                return resourceOwnerFullName + "/" + Paths.TRIGGERS_PATH_SEGMENT + "/" + resourceName;
            case Attachment:
                return resourceOwnerFullName + "/" + Paths.ATTACHMENTS_PATH_SEGMENT + "/" + resourceName;
            case Conflict:
                return resourceOwnerFullName + "/" + Paths.CONFLICTS_PATH_SEGMENT + "/" + resourceName;
            case Document:
                return resourceOwnerFullName + "/" + Paths.DOCUMENTS_PATH_SEGMENT + "/" + resourceName;
            case Offer:
                return resourceOwnerFullName + "/" + Paths.OFFERS_PATH_SEGMENT + "/" + resourceName;
            case Permission:
                return resourceOwnerFullName + "/" + Paths.PERMISSIONS_PATH_SEGMENT + "/" + resourceName;
            case User:
                return resourceOwnerFullName + "/" + Paths.USERS_PATH_SEGMENT + "/" + resourceName;
            case PartitionKeyRange:
                return resourceOwnerFullName + "/" + Paths.PARTITION_KEY_RANGE_PATH_SEGMENT + "/" + resourceName;
            default:
                return null;
        }
    }

    public static String getCollectionPath(String resourceFullName) {
        if (resourceFullName != null) {
            String trimmedResourceFullName = Utils.trimBeginingAndEndingSlashes(resourceFullName);
            int index = indexOfNth(trimmedResourceFullName, '/', 4);
            if (index > 0)
                return trimmedResourceFullName.substring(0, index);
        }

        return resourceFullName;
    }

    public static String getDatabasePath(String resourceFullName) {
        if (resourceFullName != null) {
            int index = indexOfNth(resourceFullName, '/', 2);
            if (index > 0)
                return resourceFullName.substring(0, index);
        }

        return resourceFullName;
    }

    public static boolean isNameBased(String resourceIdOrFullName) {
        // quick way to tell whether it is resourceId nor not, non conclusively.
        if (resourceIdOrFullName != null && !resourceIdOrFullName.isEmpty()
                && resourceIdOrFullName.length() > 4 && resourceIdOrFullName.charAt(3) == '/') {
            return true;
        }
        return false;
    }

    private static int indexOfNth(String str, char value, int nthOccurance) {
        int remaining = nthOccurance;
        char[] characters = str.toCharArray();
        for (int i = 0; i < characters.length; i++) {
            if (characters[i] == value) {
                remaining--;
                if (remaining == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static ResourceType getResourcePathSegment(String resourcePathSegment) throws BadRequestException {
        if (StringUtils.isEmpty(resourcePathSegment)) {
            String message = String.format(RMResources.StringArgumentNullOrEmpty, "resourcePathSegment");
            throw new BadRequestException(message);
        }

        switch (resourcePathSegment) {
            case Paths.ATTACHMENTS_PATH_SEGMENT:
                return ResourceType.Attachment;

            case Paths.COLLECTIONS_PATH_SEGMENT:
                return ResourceType.DocumentCollection;

            case Paths.DATABASES_PATH_SEGMENT:
                return ResourceType.Database;

            case Paths.PERMISSIONS_PATH_SEGMENT:
                return ResourceType.Permission;

            case Paths.USERS_PATH_SEGMENT:
                return ResourceType.User;

            case Paths.DOCUMENTS_PATH_SEGMENT:
                return ResourceType.Document;

            case Paths.STORED_PROCEDURES_PATH_SEGMENT:
                return ResourceType.StoredProcedure;

            case Paths.USER_DEFINED_FUNCTIONS_PATH_SEGMENT:
                return ResourceType.UserDefinedFunction;

            case Paths.TRIGGERS_PATH_SEGMENT:
                return ResourceType.Trigger;

            case Paths.CONFLICTS_PATH_SEGMENT:
                return ResourceType.Conflict;

            case Paths.OFFERS_PATH_SEGMENT:
                return ResourceType.Offer;

            case Paths.SCHEMAS_PATH_SEGMENT:
                return ResourceType.Schema;
        }

        String errorMessage = String.format(RMResources.UnknownResourceType, resourcePathSegment);
        throw new BadRequestException(errorMessage);
    }
    
    public static String getResourcePath(ResourceType resourceType) throws BadRequestException {
        switch (resourceType) {
            case Database:
                return Paths.DATABASES_PATH_SEGMENT;

            case DocumentCollection:
                return Paths.COLLECTIONS_PATH_SEGMENT;

            case Document:
                return Paths.DOCUMENTS_PATH_SEGMENT;

            case StoredProcedure:
                return Paths.STORED_PROCEDURES_PATH_SEGMENT;

            case UserDefinedFunction:
                return Paths.USER_DEFINED_FUNCTIONS_PATH_SEGMENT;

            case Trigger:
                return Paths.TRIGGERS_PATH_SEGMENT;

            case Conflict:
                return Paths.CONFLICTS_PATH_SEGMENT;

            case Attachment:
                return Paths.ATTACHMENTS_PATH_SEGMENT;

            case User:
                return Paths.USERS_PATH_SEGMENT;

            case Permission:
                return Paths.PERMISSIONS_PATH_SEGMENT;

            case Offer:
                return Paths.OFFERS_PATH_SEGMENT;

            case MasterPartition:
            case ServerPartition:
                return Paths.PARTITIONS_PATH_SEGMENT;

            case PartitionKeyRange:
                return Paths.PARTITION_KEY_RANGE_PATH_SEGMENT;

            case Media:
                return Paths.MEDIA_ROOT;

            case Schema:
                return Paths.SCHEMAS_PATH_SEGMENT;


            case DatabaseAccount:
            case Topology:

                return Paths.ROOT;

            default:
                String errorMessage = String.format(RMResources.UnknownResourceType, resourceType.toString());
                throw new BadRequestException(errorMessage);
        }
    }
}
