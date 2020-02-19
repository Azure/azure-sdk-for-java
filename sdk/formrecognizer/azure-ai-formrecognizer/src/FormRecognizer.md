## Form recognizer API design proposal

### Methods

```java
// Train
public PollerFlux<OperationResult, Model> beginModelTraining(String sourceUrl) { }
public PollerFlux<OperationResult, Model> beginModelTraining(String sourceUrl, String filePrefix, boolean includeSubFolders) { }

public PollerFlux<OperationResult, SupervisedModel> beginSupervisedModelTraining(String sourceUrl) { }
public PollerFlux<OperationResult, SupervisedModel> beginSupervisedModelTraining(String sourceUrl, String filePrefix, boolean includeSubFolders) { }

// Analyze form with custom models
public PollerFlux<OperationResult, FormResult> beginAnalyzeForm(Flux<ByteBuffer> data, String modelId, FormContentType contentType, boolean includeTextDetails) { }
public PollerFlux<OperationResult, FormResult> beginAnalyzeForm(String uploadFilePath, String modelId, boolean convertToStream, 
boolean includeTextDetails) { }

// Analyze with supervised models
public PollerFlux<OperationResult, PredefinedFormResult> beginSupervisedAnalyzeForm(Flux<ByteBuffer> data, String modelId, FormContentType contentType, boolean includeTextDetails) { }
public PollerFlux<OperationResult, PredefinedFormResult> beginSupervisedAnalyzeForm(String uploadFilePath, String modelId, 
boolean convertToStream, boolean includeTextDetails) { }

// List custom Models
public PagedFlux<ModelInfo> listModels() {}
public ModelSummary getModelListSummary() {}
public Response<ModelSummary> getModelListSummaryWithResponse() {}

// Delete model
public Mono<Void> delete(String modelId) { }
public Mono<Response<Void>> deleteWithResponse(String modelId) { }

// Analyze receipt 
public PollerFlux<OperationResult, ExtractedReceiptResult> beginAnalyzeReceipt(Flux<ByteBuffer> data, FormContentType contentType, boolean includeTextDetails) { }
public PollerFlux<OperationResult, ExtractedReceiptResult> beginAnalyzeReceipt(String uploadFilePath,  boolean convertToStream, 
boolean includeTextDetails) { }

// Analyze Layout
public PollerFlux<OperationResult, ExtractedLayoutResult> beginAnalyzeLayout(String uploadFilePath, boolean convertToStream,
boolean includeTextDetails) { }
public PollerFlux<OperationResult, ExtractedLayoutResult> beginAnalyzeLayout(Flux<ByteBuffer> data, FormContentType contentType, 
boolean includeTextDetails) { }

### Models

```java
public class OperationResult {
    private String resultId;
}

// Unsupervised Model result
public class Model {
    private UnsupervisedModelInfo modelInfo;
    private List<FormRecognizerError> trainingErrors;
    private List<TrainingInput> trainingInputResult;
}

public class UnsupervisedModelInfo extends ModelInfo {
    private List<FormCluster> formClusters;
}

public class FormCluster {
    private List<String> fieldNames;
    private int formClusterId;
}

// Unsupervised model analyze form result
public class FormResult {
    private OperationStatus status; 
    private List<ExtractedPage> pages ;
    private List<RawPageExtraction> rawPageExtractions ;
}

public class ExtractedPage {
    private List<ExtractedField> fields ;
    private int clusterId ;
    private int pageNumber ;
    private List<ExtractedTable> tables ;
}

public class ExtractedField {
    private double confidence ;
    private ExtractedText extractedKey ;
    private ExtractedText extractedValue ;
    private List<String> elements; // When includeTextDetails is set to true, a list of references to the text elements constituting this key or value.
}

public class ExtractedText {
    private List<double> boundingBox ;
    private String text ;
}

public class ExtractedTable {
    private List<ExtractedTableCell> cells;
    private int columnCount;
    private int rowCount;
}

public class ExtractedTableCell extends ExtractedText {
    private int columnIndex;
    private int columnSpan;
    private double confidenceScore;
    private boolean isFooter;
    private boolean isHeader;
    private int rowIndex;
    private int rowSpan;
    private List<String> elements; // only included if text details true. Confirm others.
}

public ExpandableStringEnum<FormContentType> extends ExpandableStringEnum {
    private static final FormContentType JPEG = fromString("Jpeg");
    private static final FormContentType PDF = fromString("Pdf");
    private static final FormContentType PNG = fromString("png");
    private static final FormContentType Tiff = fromString("TIFF");
}

// Supervised analyze result
public class PredefinedFormResult {
    private OperationStatus status;
    private List<PredefinedFieldForm> forms ;
    private List<RawPageExtraction> rawPageExtractions ;
}

public class PredefinedFieldForm {
    private String formType ;
    private PageRange pageRange ;
    private List<PredefinedFieldPage> pages ;
}

public class PredefinedFieldPage {
    private List<PredefinedField> fields ;
    private int pageNumber ;
    private List<ExtractedTable> tables ;
}

public class PredefinedField {
    private double confidence ;
    private String name ;
    private PredefinedFieldValueType Type ;
    private ExtractedText extractedValue ;
    List<String> referenceElements;
}

public class PageRange {
    private int endPageNumber ;
    private int startPageNumber ;
}

public class RawPageExtraction {
    private TextLanguage language;
    private List<ExtractedLine> lines;
    private double pageHeight;
    private int pageNumber;
    private double pageWidth;
    private double textAngle;
    private DimensionUnit unit;
}

// Train supervised model
public class SupervisedModel {
    private PredefinedFieldModel modelInfo;
    public List<FormRecognizerError> trainingErrors;
    public List<TrainingInput> trainingInputResult;
}

public class TrainingInput {
    private String documentName ;
    private TrainingStatus status ;
    private int totalTrainedPages ;
    private FormRecognizerError[] trainingInputErrors ;
}

public class PredefinedFieldModel extends ModelInfo{
    private double averageFieldAccuracy ;
    private List<FieldDetails> fieldDetails ;
}

public class FieldDetails {
    private double accuracy ;
    private String fieldName ;
}

// List Models
public class ModelSummary {
    private int count;
    private int limit;
    private OffsetDateTime lastUpdatedDateTime;
}

public final class ModelInfo {
    private String modelId;
    private ModelStatus status;
    private OffsetDateTime createdDateTime;
    private OffsetDateTime lastUpdatedDateTime;
}

// Analyze Receipt
public class ExtractedReceiptResult {
    private List<ExtractedReceipt> receiptResultItems;
    private List<RawPageExtraction> rawPageExtractions;
}

public class ExtractedReceipt {
    public List<ReceiptItem> items;
    public String merchantAddress;
    public String merchantName;
    public String merchantPhoneNumber;
    public RawReceiptExtraction rawFields;
    public ReceiptType receiptType;
    public double subtotal;
    public double tax;
    public double tip;
    public double total;
    public String/DateTime transactionDate;
    public String/DateTime transactionTime;
}

public class ReceiptItem {
    public String name;
    public int quantity;
    public double totalPrice;
}

public enum ReceiptType {
    Unrecognized;
    Itemized;
}

public class RawReceiptExtraction {
    public List<RawReceiptItemExtraction> items;
    public PredefinedField merchantAddress;
    public PredefinedField merchantName;
    public PredefinedField merchantPhoneNumber;
    public PredefinedField subtotal;
    public PredefinedField tax;
    public PredefinedField tip;
    public PredefinedField total;
    public PredefinedField transactionDate;
    public PredefinedField transactionTime;
}

public class RawReceiptItemExtraction {
    public PredefinedField name;
    public PredefinedField quantity;
    public PredefinedField totalPrice;
}

 public class PredefinedField {
    public double confidence;
    public String name;
    public PredefinedFieldValueType type;
    public ExtractedText extractedValue;
}
public class PredefinedFieldForm {
    public String formType;
    public PageRange PageRange;
    public List<PredefinedFieldPage> pages;
}
public class PredefinedFieldPage {
    public List<PredefinedField> fields;
    public int PageNumber;
    public List<ExtractedTable> Tables;
}

public enum PredefinedFieldValueType {
    StringType,
    Date,
    Time,
    PhoneNumber,
    Number,
    IntegerType,
    Array,
    ObjectType,
}

// Analyze Layout
public class ExtractedLayoutResult {
    private List<LayoutPage> pages;
    private List<RawPageExtraction> rawPageExtractions;
}

public class LayoutPage {
    private int pageNumber;
    private List<ExtractedTable> tables;
}

public class ExtractedLine extends ExtractedText {
    private TextLanguage language;
    private List<ExtractedWord> words;
}

public enum TrainingStatus {
    SUCCEEDED("succeeded"),
    PARTIALLY_SUCCEEDED("partiallySucceeded"),
    FAILED("failed");
}

public enum OperationStatus {
    NOT_STARTED("notStarted"),
    RUNNING("running"),
    SUCCEEDED("succeeded"),
    FAILED("failed");
}

public enum ModelStatus {
    CREATING("creating"),
    READY("ready"),
    INVALID("invalid");
}
```

## Samples
### [Unsupervised] Train and analyze with custom model

```java
String trainingSetSource = "<storage-sas-url>";
PollerFlux<OperationResult, Model> trainingPoller = client.beginModelTraining(trainingSetSource);

Model unsupervisedModel = trainingPoller
    .take(Duration.ofMinutes(2))
    .last()
    .flatMap(trainingOperationResponse -> {
        if (trainingOperationResponse.getStatus().isComplete()) {
            // training completed successfully, retrieving final result.
            return trainingOperationResponse.getFinalResult();
        } else {
            throw new RuntimeException("polling completed unsuccessfully with status:"
                + trainingOperationResponse.getStatus());
        }
    }).block();

// Analyze with custom model
String analyzeFilePath = "https://templates.invoicehome.com/invoice-template-us-neat-750px.png";
String customModelId = unsupervisedModel.getModelInfo().getModelId().toString();
PollerFlux<OperationResult, FormResult> analyzePoller = client.beginAnalyzeForm(analyzeFilePath, customModelId, false, false);

FormResult analyzeFormResult = analyzePoller
    .last()
    .flatMap(analyzePollResponse -> {
        if (analyzePollResponse.getStatus().isComplete()) {
            // training completed successfully, return final result.
            return analyzePollResponse.getFinalResult();
        } else {
            throw new RuntimeException("Polling completed unsuccessfully with status:"
                + analyzePollResponse.getStatus());
        }
    }).block();

for(ExtractedPage extractedPage : analyzeFormResult.getPages()) {
    System.out.printf("Page Number:%s", extractedPage.getPageNumber());
    System.out.printf("Cluster Id:%s", extractedPage.getClusterId());

    for(ExtractedField extractedField : extractedPage.getFields()) {
        System.out.printf("Name:%s", extractedField.getExtractedKey().getText());
        System.out.printf("Value:%s", extractedPage.getExtractedValue().getText());
        System.out.printf("Confidence score:%s", extractedPage.getConfidence())
    }
}
```

### [Supervised] Train and analyze with custom model
```java
String trainingSetSource = "<storage-sas-url>";
PollerFlux<OperationResult, SupervisedModel> trainingPoller = client.beginSupervisedModelTraining(trainingSetSource);

SupervisedModel supervisedModel = trainingPoller
    .take(Duration.ofMinutes(2))
    .last()
    .flatMap(trainingOperationResponse -> {
        if (trainingOperationResponse.getStatus().isComplete()) {
            // training completed successfully, retrieving final result.
            return trainingOperationResponse.getFinalResult();
        } else {
            throw new RuntimeException("polling completed unsuccessfully with status:"
                + trainingOperationResponse.getStatus());
        }
    }).block();

// Analyze with custom model
String analyzeFilePath = "https://templates.invoicehome.com/invoice-template-us-neat-750px.png";
String customModelId = unsupervisedModel.getModelInfo().getModelId().toString();
PollerFlux<OperationResult, PredefinedFormResult> analyzePoller = client.beginAnalyzeForm(analyzeFilePath, customModelId, false, false);

PredefinedFormResult predefinedFormResult = analyzePoller
    .last()
    .flatMap(analyzePollResponse -> {
        if (analyzePollResponse.getStatus().isComplete()) {
            // training completed successfully, return final result.
            return analyzePollResponse.getFinalResult();
        } else {
            throw new RuntimeException("Polling completed unsuccessfully with status:"
                + analyzePollResponse.getStatus());
        }
    }).block();

for(PredefinedFieldForm extractedForm : predefinedFormResult.getForms()) {
    System.out.printf("Form Type:%s", extractedForm.getFormType());

    for(PredefinedFieldPage fieldPage : extractedForm.getPages()) {
            for(PredefinedField extractedField : extractedForm.getFields()) {
                System.out.printf("Name:%s", extractedField.getName());
                System.out.printf("Confidence score:%s", extractedField.getConfidenceScore());
                System.out.printf("Value:%s", extractedField.getExtractedValue().getText());
        }
    }
}
```

### Train unsupervised model
```java
String trainingSetSource = "<storage-sas-url>";
PollerFlux<OperationResult, Model> trainingPoller = client.beginModelTraining(trainingSetSource);

Model unsupervisedModel = trainingPoller
    .take(Duration.ofMinutes(2))
    .last()
    .flatMap(trainingOperationResponse -> {
        if (trainingOperationResponse.getStatus().isComplete()) {
            // training completed successfully, retrieving final result.
            return trainingOperationResponse.getFinalResult();
        } else {
            throw new RuntimeException("polling completed unsuccessfully with status:"
                + trainingOperationResponse.getStatus());
        }
    }).block();

System.out.printf("Model Id: %s", unsupervisedModel.getModelInfo().getModelId());
System.out.printf("Model status: %s", unsupervisedModel.getModelInfo().getModelStatus());
for(FormCluster formCluster : unsupervisedModel.getModelInfo().getFormClusters()){
    System.out.println(formCluster.getFormClusterId());
    for(String fieldName : formCluster.getFieldNames()) {
        System.out.printf("Field Name: %s", fieldName);
    }
}
```

### Train supervised model
```java
String trainingSetSource = "<storage-sas-url>";
PollerFlux<OperationResult, SupervisedModel> trainingPoller = client.beginSupervisedModelTraining(trainingSetSource);

SupervisedModel supervisedModel = trainingPoller
    .take(Duration.ofMinutes(2))
    .last()
    .flatMap(trainingOperationResponse -> {
        if (trainingOperationResponse.getStatus().isComplete()) {
            // training completed successfully, retrieving final result.
            return trainingOperationResponse.getFinalResult();
        } else {
            throw new RuntimeException("polling completed unsuccessfully with status:"
                + trainingOperationResponse.getStatus());
        }
    }).block();

System.out.printf("Model Id: %s", supervisedModel.getModelInfo().getModelId());
System.out.printf("Model status: %s", supervisedModel.getModelInfo().getModelStatus());
for(FieldDetails fieldDetails : supervisedModel.getModelInfo().getFieldAccuracies()){
    System.out.println("Field Name: %s", fieldAccuracy.getFieldName());
    System.out.println("Field Accuracy: %s", fieldAccuracy.getAccuracy());
}
```

### Delete model
```java
client.deleteWithResponse("{model Id}").subscribe(response ->
    System.out.printf("Delete operation 1 completed with status code: %d%n", response.getStatusCode())
);
```

### Analyze Receipt with prebuilt model
```java
String receiptFileUrl = "https://docs.microsoft.com/en-us/azure/cognitive-services/form-recognizer/media/contoso-invoice.png";
PollerFlux<OperationResult, ExtractedReceiptResult> analyzePoller = client.beginAnalyzeReceipt(receiptFileUrl, false, false);

ExtractedReceiptResult extractedReceiptResult = analyzePoller
    .last()
    .flatMap(analyzePollResponse -> {
        if (analyzePollResponse.getStatus().isComplete()) {
            // training completed successfully, return final result.
            return analyzePollResponse.getFinalResult();
        } else {
            throw new RuntimeException("Polling completed unsuccessfully with status:"
                + analyzePollResponse.getStatus());
        }
    }).block();

for(ExtractedReceipt receiptResultItem : extractedReceiptResult.getReceiptResultItems()) {
    System.out.println("Merchant Name: %s", receiptResultItem.getMerchantName());
    System.out.println("Receipt Type: %s", receiptResultItem.getReceiptType());
    System.out.println("Receipt Total: %s", receiptResultItem.getTotal());
    System.out.println("Transaction Date: %s", receiptResultItem.getTransactionDate());
    for(ReceiptItem receiptItem : receiptResultItem.getReceiptItem()) {
        System.out.println("Receipt item name: %s", receiptItem.getName());
        System.out.println("Receipt item quantity: %s", receiptItem.getQuantity());
        System.out.println("Receipt item total price: %s", receiptItem.getTotalPrice());
    }
}
```

### Analyze Layout with prebuilt model
```java
String layoutFileUrl = "https://docs.microsoft.com/en-us/azure/cognitive-services/form-recognizer/media/contoso-invoice.png";
PollerFlux<OperationResult, ExtractedLayoutResult> analyzePoller = client.beginAnalyzeLayout(layoutFileUrl, false, false);

ExtractedLayoutResult extractedLayoutResult = analyzePoller
    .last()
    .flatMap(analyzePollResponse -> {
        if (analyzePollResponse.getStatus().isComplete()) {
            // training completed successfully, return final result.
            return analyzePollResponse.getFinalResult();
        } else {
            throw new RuntimeException("Polling completed unsuccessfully with status:"
                + analyzePollResponse.getStatus());
        }
    }).block();

for(RawPageExtraction rawPageExtraction : extractedLayoutResult.getRawPageExtractions()) {
    System.out.println("Extracted Layout page number: %s", rawPageExtractions.getPageNumber());
    for(ExtractedLine extractedLine : rawPageExtractions..getLines()) {
        System.out.println("Extracted Line Language: %s", extractedLine.getLanguage());
        for(ExtractedWord extractedWord : extractedLine.getWords()) {
            System.out.println("Text :%s", extractedWord.getText());
            System.out.println("Confidence Score :%s", extractedWord.getConfidenceScore());
        }
    }
}
```