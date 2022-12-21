// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation;

import com.azure.ai.formrecognizer.training.models.CustomFormModel;
import com.azure.ai.formrecognizer.training.models.CustomFormModelProperties;
import com.azure.ai.formrecognizer.training.models.TrainingDocumentInfo;
import java.util.List;

/**
 * The helper class to set the non-public properties of an {@link CustomFormModel} instance.
 */
public final class CustomFormModelHelper {
    private static CustomFormModelAccessor accessor;

    private CustomFormModelHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link CustomFormModel} instance.
     */
    public interface CustomFormModelAccessor {
        void setTrainingDocuments(CustomFormModel formModel, List<TrainingDocumentInfo> trainingDocuments);
        void setModelName(CustomFormModel formModel, String modelName);
        void setCustomFormModelProperties(CustomFormModel formModel,
                                          CustomFormModelProperties customFormModelProperties);
    }

    /**
     * The method called from {@link CustomFormModel} to set it's accessor.
     *
     * @param formModelAccessor The accessor.
     */
    public static void setAccessor(final CustomFormModelAccessor formModelAccessor) {
        accessor = formModelAccessor;
    }

    public static void setTrainingDocuments(CustomFormModel formModel, List<TrainingDocumentInfo> trainingDocuments) {
        accessor.setTrainingDocuments(formModel, trainingDocuments);
    }

    public static void setModelName(CustomFormModel formModel, String modelName) {
        accessor.setModelName(formModel, modelName);
    }

    public static void setCustomFormModelProperties(CustomFormModel formModel,
                                      CustomFormModelProperties customFormModelProperties) {
        accessor.setCustomFormModelProperties(formModel, customFormModelProperties);
    }
}
