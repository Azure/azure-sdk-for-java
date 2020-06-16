package com.azure.data.tables;

import java.util.List;
import java.util.Map;

public class TableClient {
    String tableName;

    public TableClient(String tableName){ this.tableName = tableName; }

    public List<TableEntity> queryEntity(String az, String selectString, String filterString){
        return null;
    }

    public TableEntity insertEntity(String row, String partition, Map<String, Object> tableEntityProperties){
        return new TableEntity();
    }
    public TableEntity insertEntity(TableEntity tableEntity){
        return tableEntity;
    }
    public void deleteEntity(TableEntity tableEntity){ }

    public void updateEntity(TableEntity te){ }
    public void updateAndReplaceEntity(TableEntity tableEntity){ }
    public void updateAndMergeEntity(TableEntity tableEntity){ }
}
