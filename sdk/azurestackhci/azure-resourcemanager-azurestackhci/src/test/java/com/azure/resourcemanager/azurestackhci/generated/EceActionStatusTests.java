// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.azurestackhci.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.azurestackhci.models.EceActionStatus;

public final class EceActionStatusTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        EceActionStatus model = BinaryData.fromString(
            "{\"status\":\"rtwaenuuzko\",\"steps\":[{\"name\":\"nrfdw\",\"description\":\"uhhziuiefozbhdm\",\"fullStepIndex\":\"l\",\"startTimeUtc\":\"qhoftrmaequiah\",\"endTimeUtc\":\"cslfaoqzpiyylha\",\"status\":\"swhccsphk\",\"steps\":[{\"name\":\"itqscywuggwoluhc\",\"description\":\"wem\",\"fullStepIndex\":\"i\",\"startTimeUtc\":\"brgz\",\"endTimeUtc\":\"msweypqwdxggicc\",\"status\":\"xqhuexm\",\"steps\":[{},{},{}],\"exception\":[\"tvlz\",\"wem\",\"zrncsdt\",\"lusiy\"]},{\"name\":\"sfgytguslfead\",\"description\":\"gq\",\"fullStepIndex\":\"yhejhzisxgfp\",\"startTimeUtc\":\"olppvksrpqvujz\",\"endTimeUtc\":\"ehtwdwrft\",\"status\":\"iby\",\"steps\":[{}],\"exception\":[\"h\",\"hfwpracstwit\",\"khevxccedc\",\"nmdyodnwzxl\"]},{\"name\":\"cvnhltiugc\",\"description\":\"avvwxqi\",\"fullStepIndex\":\"qunyowxwlmdjr\",\"startTimeUtc\":\"fgbvfvpdbo\",\"endTimeUtc\":\"cizsjqlhkrribdei\",\"status\":\"ipqkghvxndzwm\",\"steps\":[{},{}],\"exception\":[\"jpjorwkqnyhgb\",\"j\"]}],\"exception\":[\"vfxzsjab\",\"bsystawfsdjpvk\",\"p\",\"jxbkzbzkdvn\"]},{\"name\":\"abudurgk\",\"description\":\"mokzhjjklf\",\"fullStepIndex\":\"mouwqlgzrfzeey\",\"startTimeUtc\":\"izikayuhq\",\"endTimeUtc\":\"jbsybbqw\",\"status\":\"t\",\"steps\":[{\"name\":\"fp\",\"description\":\"mpipaslthaqfxs\",\"fullStepIndex\":\"wutwbdsre\",\"startTimeUtc\":\"drhneuyow\",\"endTimeUtc\":\"d\",\"status\":\"t\",\"steps\":[{}],\"exception\":[\"rcgp\",\"kpzi\",\"ejzanlfz\"]},{\"name\":\"av\",\"description\":\"bzonok\",\"fullStepIndex\":\"rjqc\",\"startTimeUtc\":\"gzpfrla\",\"endTimeUtc\":\"zrnw\",\"status\":\"indfpwpjyl\",\"steps\":[{},{},{},{}],\"exception\":[\"flsjc\"]}],\"exception\":[\"zfjvfbgofe\",\"jagrqmqhldvr\",\"iiojnal\",\"hfkvtvsexsowuel\"]}]}")
            .toObject(EceActionStatus.class);
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        EceActionStatus model = new EceActionStatus();
        model = BinaryData.fromObject(model).toObject(EceActionStatus.class);
    }
}
