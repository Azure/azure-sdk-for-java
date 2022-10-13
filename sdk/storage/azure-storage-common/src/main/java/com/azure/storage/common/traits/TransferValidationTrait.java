package com.azure.storage.common.traits;

import com.azure.storage.common.TransferValidationOptions;

public interface TransferValidationTrait<T extends TransferValidationTrait<T>> {
    T transferValidationOptions(TransferValidationOptions validationOptions);
}
