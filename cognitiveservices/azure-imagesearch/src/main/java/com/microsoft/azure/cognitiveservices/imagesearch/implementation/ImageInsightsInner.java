/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.imagesearch.implementation;

import com.microsoft.azure.cognitiveservices.imagesearch.Query;
import com.microsoft.azure.cognitiveservices.imagesearch.ImageInsightsImageCaption;
import com.microsoft.azure.cognitiveservices.imagesearch.RelatedCollectionsModule;
import com.microsoft.azure.cognitiveservices.imagesearch.ImagesModule;
import com.microsoft.azure.cognitiveservices.imagesearch.AggregateOffer;
import com.microsoft.azure.cognitiveservices.imagesearch.RelatedSearchesModule;
import com.microsoft.azure.cognitiveservices.imagesearch.RecipesModule;
import com.microsoft.azure.cognitiveservices.imagesearch.RecognizedEntitiesModule;
import com.microsoft.azure.cognitiveservices.imagesearch.ImageTagsModule;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.microsoft.azure.cognitiveservices.imagesearch.Response;

/**
 * The top-level object that the response includes when an image insights
 * request succeeds. For information about requesting image insights, see the
 * [insightsToken](https://docs.microsoft.com/en-us/rest/api/cognitiveservices/bing-images-api-v7-reference#insightstoken)
 * query parameter. The modules query parameter affects the fields that Bing
 * includes in the response. If you set
 * [modules](https://docs.microsoft.com/en-us/rest/api/cognitiveservices/bing-images-api-v7-reference#modulesrequested)
 * to only Caption, then this object includes only the imageCaption field.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type",
        defaultImpl = ImageInsightsInner.class)
@JsonTypeName("ImageInsights")
public class ImageInsightsInner extends Response {
    /**
     * A token that you use in a subsequent call to the Image Search API to get
     * more information about the image. For information about using this
     * token, see the insightsToken query parameter. This token has the same
     * usage as the token in the Image object.
     */
    @JsonProperty(value = "imageInsightsToken", access = JsonProperty.Access.WRITE_ONLY)
    private String imageInsightsToken;

    /**
     * The query term that best represents the image. Clicking the link in the
     * Query object, takes the user to a webpage with more pictures of the
     * image.
     */
    @JsonProperty(value = "bestRepresentativeQuery", access = JsonProperty.Access.WRITE_ONLY)
    private Query bestRepresentativeQuery;

    /**
     * The caption to use for the image.
     */
    @JsonProperty(value = "imageCaption", access = JsonProperty.Access.WRITE_ONLY)
    private ImageInsightsImageCaption imageCaption;

    /**
     * A list of links to webpages that contain related images.
     */
    @JsonProperty(value = "relatedCollections", access = JsonProperty.Access.WRITE_ONLY)
    private RelatedCollectionsModule relatedCollections;

    /**
     * A list of webpages that contain the image. To access the webpage, use
     * the URL in the image's hostPageUrl field.
     */
    @JsonProperty(value = "pagesIncluding", access = JsonProperty.Access.WRITE_ONLY)
    private ImagesModule pagesIncluding;

    /**
     * A list of merchants that offer items related to the image. For example,
     * if the image is of an apple pie, the list contains merchants that are
     * selling apple pies.
     */
    @JsonProperty(value = "shoppingSources", access = JsonProperty.Access.WRITE_ONLY)
    private AggregateOffer shoppingSources;

    /**
     * A list of related queries made by others.
     */
    @JsonProperty(value = "relatedSearches", access = JsonProperty.Access.WRITE_ONLY)
    private RelatedSearchesModule relatedSearches;

    /**
     * A list of recipes related to the image. For example, if the image is of
     * an apple pie, the list contains recipes for making an apple pie.
     */
    @JsonProperty(value = "recipes", access = JsonProperty.Access.WRITE_ONLY)
    private RecipesModule recipes;

    /**
     * A list of images that are visually similar to the original image. For
     * example, if the specified image is of a sunset over a body of water, the
     * list of similar images are of a sunset over a body of water. If the
     * specified image is of a person, similar images might be of the same
     * person or they might be of persons dressed similarly or in a similar
     * setting. The criteria for similarity continues to evolve.
     */
    @JsonProperty(value = "visuallySimilarImages", access = JsonProperty.Access.WRITE_ONLY)
    private ImagesModule visuallySimilarImages;

    /**
     * A list of images that contain products that are visually similar to
     * products found in the original image. For example, if the specified
     * image contains a dress, the list of similar images contain a dress. The
     * image provides summary information about offers that Bing found online
     * for the product.
     */
    @JsonProperty(value = "visuallySimilarProducts", access = JsonProperty.Access.WRITE_ONLY)
    private ImagesModule visuallySimilarProducts;

    /**
     * A list of groups that contain images of entities that match the entity
     * found in the specified image. For example, the response might include
     * images from the general celebrity group if the entity was recognized in
     * that group.
     */
    @JsonProperty(value = "recognizedEntityGroups", access = JsonProperty.Access.WRITE_ONLY)
    private RecognizedEntitiesModule recognizedEntityGroups;

    /**
     * A list of characteristics of the content found in the image. For
     * example, if the image is of a person, the tags might indicate the
     * person's gender and the type of clothes they're wearing.
     */
    @JsonProperty(value = "imageTags", access = JsonProperty.Access.WRITE_ONLY)
    private ImageTagsModule imageTags;

    /**
     * Get the imageInsightsToken value.
     *
     * @return the imageInsightsToken value
     */
    public String imageInsightsToken() {
        return this.imageInsightsToken;
    }

    /**
     * Get the bestRepresentativeQuery value.
     *
     * @return the bestRepresentativeQuery value
     */
    public Query bestRepresentativeQuery() {
        return this.bestRepresentativeQuery;
    }

    /**
     * Get the imageCaption value.
     *
     * @return the imageCaption value
     */
    public ImageInsightsImageCaption imageCaption() {
        return this.imageCaption;
    }

    /**
     * Get the relatedCollections value.
     *
     * @return the relatedCollections value
     */
    public RelatedCollectionsModule relatedCollections() {
        return this.relatedCollections;
    }

    /**
     * Get the pagesIncluding value.
     *
     * @return the pagesIncluding value
     */
    public ImagesModule pagesIncluding() {
        return this.pagesIncluding;
    }

    /**
     * Get the shoppingSources value.
     *
     * @return the shoppingSources value
     */
    public AggregateOffer shoppingSources() {
        return this.shoppingSources;
    }

    /**
     * Get the relatedSearches value.
     *
     * @return the relatedSearches value
     */
    public RelatedSearchesModule relatedSearches() {
        return this.relatedSearches;
    }

    /**
     * Get the recipes value.
     *
     * @return the recipes value
     */
    public RecipesModule recipes() {
        return this.recipes;
    }

    /**
     * Get the visuallySimilarImages value.
     *
     * @return the visuallySimilarImages value
     */
    public ImagesModule visuallySimilarImages() {
        return this.visuallySimilarImages;
    }

    /**
     * Get the visuallySimilarProducts value.
     *
     * @return the visuallySimilarProducts value
     */
    public ImagesModule visuallySimilarProducts() {
        return this.visuallySimilarProducts;
    }

    /**
     * Get the recognizedEntityGroups value.
     *
     * @return the recognizedEntityGroups value
     */
    public RecognizedEntitiesModule recognizedEntityGroups() {
        return this.recognizedEntityGroups;
    }

    /**
     * Get the imageTags value.
     *
     * @return the imageTags value
     */
    public ImageTagsModule imageTags() {
        return this.imageTags;
    }

}
