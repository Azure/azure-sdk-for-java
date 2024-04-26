// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.cris.querystuckrepro;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.util.CosmosPagedFlux;

public class CosmosDBSqlApiRowByRowReader extends AbstractCosmosDBSqlApiReader {

    public CosmosDBSqlApiRowByRowReader(CosmosDBSqlApiReaderDTO readerMd) throws SDKException {
        super(readerMd);
    }

    @Override
    public int read(DataSession dataSession, ReadAttributes readAttr) throws SDKException {
        int pageSize = this.readerMd.getPartReadAttris().getPageSize() < 0 ? 50 : this.readerMd.getPartReadAttris().getPageSize();
        int rowsToRead = readAttr.getNumRowsToRead();

        ThroughputManager tpManager = new ThroughputManager(collThroughput, this.readContainer, this.readerMd.getClient(), logger);
        boolean tpUpdated = false;
        if (throughput > 0) {
            tpUpdated = tpManager.updateThroughput(throughput);
        }

        AzureDocumentDbUserContext aduc = getAzureDocumentDbUserContext(dataSession);
        logger.logMessage(EMessageLevel.MSG_DEBUG, ELogLevel.TRACE_VERBOSE_DATA,
            String.format("Initiating records fetch with rowsToRead [%d]", rowsToRead));
        if (pageSize > rowsToRead) {
            logger.logMessage(EMessageLevel.MSG_INFO, ELogLevel.TRACE_NONE,
                String.format(
                    "pageSize [%d] is greater than rowsToRead [%d]. Limiting pageSize to match rowsToRead.",
                    pageSize, rowsToRead));
            pageSize = rowsToRead;
        }

        int rowsReadFromSource = 0;
        int remainingRowsToRead = rowsToRead;
        int rowsRead = -1;
        int rowsFaildToParse = 0;
        List<Object> dataRowsFromPrevReaderCall = new ArrayList<>();
        while(!aduc.getRecordQueue().isEmpty() && remainingRowsToRead > 0) {
            dataRowsFromPrevReaderCall.add(aduc.getRecordQueue().poll());
            remainingRowsToRead--;
        }
        List<String> errors = new ArrayList<>();
        dataFormat.parse(dataRowsFromPrevReaderCall, dataSession, readAttr,errors);

        while (remainingRowsToRead > 0 && !aduc.isNoMoreRecords()) {
            //Because of page size setting we need to call rowsReadFromSource() method repeatedly
            //until the while condition satisfies
            List<Object> dataRows = new ArrayList<>();
            rowsReadFromSource = rowsReadFromSource
                + readDatafromSource(pageSize, aduc, isPartitionedColl, dataRows, this.query);

            int rowsToProcess = remainingRowsToRead < dataRows.size() ? remainingRowsToRead : dataRows.size();
            List<Object> dataRowsToProcess = dataRows.subList(0, rowsToProcess);
            List<Object> dataRowsToPushInQueue = dataRows.subList(rowsToProcess, dataRows.size());

            dataFormat.parse(dataRowsToProcess, dataSession, readAttr, errors);
            remainingRowsToRead = remainingRowsToRead - rowsToProcess;

            dataRowsToPushInQueue.stream().forEach(e -> aduc.getRecordQueue().offer(e.toString()));
        }

        rowsFaildToParse = errors.size();
        rowsRead = rowsToRead - remainingRowsToRead;
        readAttr.setNumRowsRead(rowsRead);
        logger.logMessage(EMessageLevel.MSG_DEBUG, ELogLevel.TRACE_VERBOSE_DATA, String.format(
            "Record fetch completed with total records read [%d], rows failed to parse [%d], rowsToRead [%d], "
                + "pageSize [%d], partitionKey [%s] ,throughput [%d] and filter query [%s]",
            rowsRead, rowsFaildToParse, rowsToRead, pageSize, partitionKeyStr, throughput,
            filterQueryOverride));

        if (tpUpdated) {
            // revert throughput with the original throughput
            boolean tpReverted = tpManager.revertThroughput();
            if (!tpReverted) {
                logger.logMessage(EMessageLevel.MSG_WARNING, ELogLevel.TRACE_NONE,
                    String.format("Failed to revert to original throughput [%d] for collection [%s] ",
                        collThroughput, this.readContainer));
            }
        }

        if (aduc.isNoMoreRecords() && aduc.getRecordQueue().isEmpty()) {
            logger.logMessage(EMessageLevel.MSG_INFO, ELogLevel.TRACE_NONE, "Read finished");
            return EReturnStatus.NO_MORE_DATA;
        }
        return EReturnStatus.SUCCESS;
    }

    private int readDatafromSource(int pageSize, AzureDocumentDbUserContext aduc, boolean isPartitionedColl,
                                   List<Object> dataRows, String query) throws SDKException {
        int rowsRead = 0;

        CosmosPagedFlux<Document> pagedFluxResponse = aduc.getCosmosPagedResponse();

        if (pagedFluxResponse == null) {
            try {
                pagedFluxResponse = readContainer
                    .queryItems(query, Document.class);
            } catch (IllegalStateException e) {
                String errMsg = String.format("Error occurred while executing query [%s] against collection [%s], %s",
                    query, this.readContainer.getId(), e.getMessage());
                logger.logMessage(EMessageLevel.MSG_FATAL_ERROR, ELogLevel.TRACE_NONE, errMsg);
                throw new SDKExceptionImpl(e.getMessage());
            }

            aduc.setCosmosPagedResponse(pagedFluxResponse);
        }

        FeedResponse<Document> feedResponse = null;
        Iterator<Document> docList = null;
        try {
            if (aduc.getContinuationToken() == null) {
                feedResponse = pagedFluxResponse.byPage(pageSize).next().block();
            } else {
                feedResponse = pagedFluxResponse.byPage(aduc.getContinuationToken(), pageSize).next().block();
            }
            aduc.setContinuationToken(feedResponse.getContinuationToken());
            docList = feedResponse.getResults().iterator();
        } catch (Exception e) {
            String errMsg = String.format(
                "Error occurred while fetching next block for query [%s] against collection [%s], %s", query,
                this.readContainer.getId(), e.getMessage());
            logger.logMessage(EMessageLevel.MSG_FATAL_ERROR, ELogLevel.TRACE_NONE, errMsg);
            throw new SDKExceptionImpl(e.getMessage());
        }
        if (docList != null) {
            while (docList.hasNext()) {
                Document document = docList.next();
                if (document == null) {
                    continue;// sometimes cosmos DB returns null document.
                }
                rowsRead++;
                dataRows.add(document.toString());
                aduc.IncrementNoOfRecordRead();
            }
        }

        if (aduc.getContinuationToken()==null) {
            aduc.setNoMoreRecords(true);
        }

        logger.logMessage(EMessageLevel.MSG_DEBUG, ELogLevel.TRACE_VERBOSE_DATA, String.format(
            "Records read [%d] , documents read in current batch [%d]", aduc.getNoOfRecordRead(), rowsRead));

        return rowsRead;
    }
}
