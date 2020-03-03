## Form recognizer API design proposal

### Methods

```java
// Train
public PollerFlux<OperationResult, TrainedModel> beginModelTraining(String sourceUrl) { }
public PollerFlux<OperationResult, TrainedModel> beginModelTraining(String sourceUrl, String filePrefix, boolean includeSubFolders) { }

public PollerFlux<OperationResult, TrainedLabeledModel> beginLabeledModelTraining(String sourceUrl) { }
public PollerFlux<OperationResult, TrainedLabeledModel> beginLabeledModelTraining(String sourceUrl, String filePrefix, boolean includeSubFolders) { }

// Analyze form with custom models
public PollerFlux<OperationResult, FormResult> beginAnalyzeForm(Flux<ByteBuffer> data, String modelId, boolean includeTextDetails) { }
public PollerFlux<OperationResult, FormResult> beginAnalyzeForm(String uploadFilePath, String modelId, boolean includeTextDetails) { }

// Analyze with supervised models
public PollerFlux<OperationResult, LabeledFormResult> beginAnalyzeLabeledForm(Flux<ByteBuffer> data, String modelId, boolean includeTextDetails) { }
public PollerFlux<OperationResult, LabeledFormResult> beginAnalyzeLabeledForm(String uploadFilePath, String modelId, boolean includeTextDetails) { }

// List custom Models
public PagedFlux<ModelInfo> listModels() {}
public Mono<ModelListSummary> getModelListSummary() {}
public Mono<Response<ModelListSummary>> getModelListSummaryWithResponse() {}

// Delete model
public Mono<Void> delete(String modelId) { }
public Mono<Response<Void>> deleteWithResponse(String modelId) { }

// Analyze receipt 
public PollerFlux<OperationResult, ExtractedReceiptResult> beginAnalyzeReceipt(Flux<ByteBuffer> data, boolean includeTextDetails) { }
public PollerFlux<OperationResult, ExtractedReceiptResult> beginAnalyzeReceipt(String uploadFilePath, boolean includeTextDetails) { }

// Analyze Layout
public PollerFlux<OperationResult, ExtractedLayoutResult> beginAnalyzeLayout(String uploadFilePath, boolean includeTextDetails) { }
public PollerFlux<OperationResult, ExtractedLayoutResult> beginAnalyzeLayout(Flux<ByteBuffer> data, boolean includeTextDetails) { }
```

### Models

```java
public class OperationResult {
    private String resultId;
}

// Unsupervised Model
public class TrainedModel {
    private TrainedModelInfo modelInfo;
    private List<FormRecognizerError> trainingErrors;
    private List<TrainingInput> trainingInputResult;
}

public class TrainedModelInfo extends ModelInfo {
    private List<FormCluster> formClusters;
}

public class FormCluster {
    private List<String> fieldNames;
    private int formClusterId;
}

// Unsupervised model analyze form result
public class FormResult {
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
    private List<String> referenceElements; // only included if text details true. Confirm others.
}

// Supervised analyze result
public class LabeledFormResult {
    private List<LabeledFieldForm> forms ;
    private List<RawPageExtraction> rawPageExtractions ;
}

public class LabeledFieldForm {
    private String formType ;
    private PageRange pageRange ;
    private List<LabeledPageInfo> pages ;
}

public class LabeledPageInfo {
    private List<PredefinedField> fields ;
    private int pageNumber ;
    private List<ExtractedTable> tables ;
}

public class PredefinedField {
    private double confidence ;
    private String name ;
    private ValueType valueType ;
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
public class TrainedLabeledModel {
    private LabeledModelInfo modelInfo;
    public List<FormRecognizerError> trainingErrors;
    public List<TrainingInput> trainingInputResult;
}

public class TrainingInput {
    private String documentName ;
    private TrainingStatus status ;
    private int totalTrainedPages ;
    private List<FormRecognizerError> trainingInputErrors ;
}

public class LabeledModelInfo extends ModelInfo{
    private double averageFieldAccuracy ;
    private List<FieldDetails> fieldDetails ;
}

public class FieldDetails {
    private double accuracy ;
    private String fieldName ;
}

// List Models
public class ModelListSummary {
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
    public ValueType type;
    public ExtractedText extractedValue;
}
public class LabeledFieldForm {
    public String formType;
    public PageRange PageRange;
    public List<LabeledPageInfo> pages;
}
public class LabeledPageInfo {
    public List<PredefinedField> fields;
    public int PageNumber;
    public List<ExtractedTable> Tables;
}

public enum ValueType {
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

public enum ModelStatus {
    CREATING("creating"),
    READY("ready"),
    INVALID("invalid");
}
```

## Samples
### [Unsupervised] Train and analyze with custom model
Train without labels
- By default, Form Recognizer uses unsupervised learning to understand the layout and relationships between fields and entries in your forms.

```java
String trainingSetSource = "<storage-sas-url>";
PollerFlux<OperationResult, TrainedModel> trainingPoller = client.beginModelTraining(trainingSetSource);

TrainedModel unsupervisedModel = trainingPoller
    .last()
    .flatMap(trainingOperationResponse -> {
        if (trainingOperationResponse.getStatus().isComplete()) {
            // training completed successfully, retrieving final result.
            return trainingOperationResponse.getFinalResult();
        } else {
            throw new RuntimeException("Polling completed unsuccessfully with status:"
                + trainingOperationResponse.getStatus());
        }
    }).block();

// Analyze with custom model
String formFileUrl = "https://templates.invoicehome.com/invoice-template-us-neat-750px.png";
String customModelId = unsupervisedModel.getModelInfo().getModelId().toString();
PollerFlux<OperationResult, FormResult> analyzePoller = client.beginAnalyzeForm(formFileUrl, customModelId, false);

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
Train with labels
- When you train with labeled data, the model does supervised learning to extract values of interest, using the labeled forms you provide. 

```java
String trainingSetSource = "<storage-sas-url>";
PollerFlux<OperationResult, TrainedLabeledModel> trainingPoller = client.beginLabeledModelTraining(trainingSetSource);

TrainedLabeledModel supervisedModel = trainingPoller
    .last()
    .flatMap(trainingOperationResponse -> {
        if (trainingOperationResponse.getStatus().isComplete()) {
            // training completed successfully, retrieving final result.
            return trainingOperationResponse.getFinalResult();
        } else {
            throw new RuntimeException("Polling completed unsuccessfully with status:"
                + trainingOperationResponse.getStatus());
        }
    }).block();

// Analyze with custom model
String formFileUrl = "https://templates.invoicehome.com/invoice-template-us-neat-750px.png";
String customModelId = supervisedModel.getModelInfo().getModelId().toString();
PollerFlux<OperationResult, LabeledFormResult> analyzePoller = client.beginAnalyzeLabeledForm(formFileUrl, customModelId, false);

LabeledFormResult labeledFormResult = analyzePoller
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

for(LabeledFieldForm extractedForm : labeledFormResult.getForms()) {
    System.out.printf("Form Type: %s", extractedForm.getFormType());

    for(LabeledPageInfo fieldPage : extractedForm.getPages()) {
        System.out.printf("Page Number: %d", extractedField.getPageNumber());
            for(PredefinedField extractedField : extractedForm.getFields()) {
                System.out.printf("Name: %s", extractedField.getName());
                System.out.printf("Confidence score: %s", extractedField.getConfidenceScore());
                System.out.printf("Value: %s", extractedField.getExtractedValue().getText());
        }
    }
}
```

### Delete model
```java
client.deleteWithResponse("{model Id}").subscribe(response ->
    System.out.printf("Delete operation completed with status code: %d%n", response.getStatusCode())
);
```

### Analyze Receipt with prebuilt model
```java
String receiptFileUrl = "https://docs.microsoft.com/en-us/azure/cognitive-services/form-recognizer/media/contoso-invoice.png";
PollerFlux<OperationResult, ExtractedReceiptResult> analyzePoller = client.beginAnalyzeReceipt(receiptFileUrl, false);

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

System.out.println("Receipt contained the following values:");
for(ExtractedReceipt receiptResultItem : extractedReceiptResult.getReceiptResultItems()) {
    System.out.println("Merchant Name: %s", receiptResultItem.getMerchantName());
    System.out.println("Merchant Address: %s", receiptResultItem.getMerchantAddress());
    System.out.println("Merchant Phone Number: %s", receiptResultItem.getMerchantPhoneNumber());
    System.out.println("Transaction Date: %s", receiptResultItem.getTransactionDate());
    System.out.println("Transaction Time: %s", receiptResultItem.getTransactionTime());
    for(ReceiptItem receiptItem : receiptResultItem.getReceiptItem()) {
        System.out.println("Receipt item name: %s", receiptItem.getName());
        System.out.println("Receipt item quantity: %s", receiptItem.getQuantity());
        System.out.println("Receipt item total price: %s", receiptItem.getTotalPrice());
    }
    System.out.println("Subtotal: %s", receiptResultItem.getTotal());
    System.out.println("Tax: %s", receiptResultItem.getTransactionDate());
    System.out.println("Tip: %s", receiptResultItem.getTransactionDate());
    System.out.println("Total: %s", receiptResultItem.getTotal());
}
```

### Analyze Layout with prebuilt model
```java
String layoutFileUrl = "https://docs.microsoft.com/en-us/azure/cognitive-services/form-recognizer/media/contoso-invoice.png";
PollerFlux<OperationResult, ExtractedLayoutResult> analyzePoller = client.beginAnalyzeLayout(layoutFileUrl, false);

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
    for(ExtractedLine extractedLine : rawPageExtractions.getLines()) {
        System.out.println("Extracted Line Language: %s", extractedLine.getLanguage());
        for(ExtractedWord extractedWord : extractedLine.getWords()) {
            System.out.println("Text :%s", extractedWord.getText());
            System.out.println("Confidence Score :%s", extractedWord.getConfidenceScore());
        }
    }
}

// will customers be more interested in the tabular data?
```