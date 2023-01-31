/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Portions Copyright (c) Microsoft Corporation
 */

package com.azure.cosmos.implementation.apachecommons.text.translate;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * This class is shaded from version 1.10.0 of apache commons-text library
 */
public class AggregateTranslator extends CharSequenceTranslator {
    /**
     * Translator list.
     */
    private final List<CharSequenceTranslator> translators = new ArrayList<>();

    /**
     * Specify the translators to be used at creation time.
     *
     * @param translators CharSequenceTranslator array to aggregate
     */
    public AggregateTranslator(final CharSequenceTranslator... translators) {
        if (translators != null) {
            Stream.of(translators).filter(Objects::nonNull).forEach(this.translators::add);
        }
    }

    /**
     * The first translator to consume code points from the input is the 'winner'.
     * Execution stops with the number of consumed code points being returned.
     * {@inheritDoc}
     */
    @Override
    public int translate(final CharSequence input, final int index, final Writer writer) throws IOException {
        for (final CharSequenceTranslator translator : translators) {
            final int consumed = translator.translate(input, index, writer);
            if (consumed != 0) {
                return consumed;
            }
        }
        return 0;
    }
}
