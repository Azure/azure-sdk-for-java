package com.azure.search.documents.example;

import com.azure.search.documents.SearchApiKeyCredential;
import com.azure.search.documents.SearchIndexClient;
import com.azure.search.documents.SearchServiceClient;
import com.azure.search.documents.SearchServiceClientBuilder;
import com.azure.search.documents.models.AnalyzeRequest;
import com.azure.search.documents.models.Field;
import com.azure.search.documents.models.Index;
import com.azure.search.documents.models.analyzerName.LanguageAnalyzerName;
import com.azure.search.documents.models.analyzerName.StrictAnalyzerName;
import com.azure.search.documents.models.field.SimpleField;
import com.azure.search.documents.models.fieldInfo.AnalyzerInfo;
import com.azure.search.documents.models.fieldInfo.CommonFieldInfo;
import com.azure.search.documents.models.fieldInfo.SearchableFieldInfo;
import com.azure.search.documents.models.fieldInfo.SimpleFieldInfo;
import java.util.Arrays;

public class FieldExample {
    public static void main(String[] args) {
        String apiKey = "some key";
        SearchServiceClient searchServiceClient = new SearchServiceClientBuilder()
            .credential(new SearchApiKeyCredential(apiKey))
            .buildClient();
        Index index = new Index().setName("name").setFields(Arrays.asList(prepareField()));
        Index returnIndex = searchServiceClient.createIndex(index);
    }

    private static Field prepareField() {
        CommonFieldInfo commonFieldInfo = new CommonFieldInfo("prototype", true,
            true, false);

        SimpleFieldInfo simpleFieldInfo = new SimpleFieldInfo();
        simpleFieldInfo.setKeyField(commonFieldInfo);
        AnalyzerInfo analyzerInfo = new AnalyzerInfo();
        analyzerInfo.setStrictAnalyzerName(StrictAnalyzerName.languageAnalyzerName(LanguageAnalyzerName.EN_LUCENE));
        SearchableFieldInfo searchableFieldInfo = new SearchableFieldInfo(simpleFieldInfo, analyzerInfo, null);
        SimpleField simpleField = new SimpleField();
        simpleField.setSearchableField(searchableFieldInfo);
        return simpleField.toField();
    }
}
