# Code snippets and samples


## Operations

- [List](#operations_list)

## RatingAndReviewsOperations

- [CheckUserHasReview](#ratingandreviewsoperations_checkuserhasreview)
### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2023-01-01-preview/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to ReviewsManager.
     */
    public static void operationsList(com.azure.resourcemanager.marketplace.reviews.ReviewsManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### RatingAndReviewsOperations_CheckUserHasReview

```java
/**
 * Samples for RatingAndReviewsOperations CheckUserHasReview.
 */
public final class RatingAndReviewsOperationsCheckUserHasReviewSamples {
    /*
     * x-ms-original-file: 2023-01-01-preview/RatingAndReviews_CheckUserHasReview.json
     */
    /**
     * Sample code: RatingAndReviews_CheckUserHasReview.
     * 
     * @param manager Entry point to ReviewsManager.
     */
    public static void
        ratingAndReviewsCheckUserHasReview(com.azure.resourcemanager.marketplace.reviews.ReviewsManager manager) {
        manager.ratingAndReviewsOperations()
            .checkUserHasReviewWithResponse("WA123456789", com.azure.core.util.Context.NONE);
    }
}
```

