/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.codelibs.elasticsearch.index.analysis;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.TypeTokenFilter;
import org.codelibs.elasticsearch.common.settings.Settings;
import org.codelibs.elasticsearch.env.Environment;
import org.codelibs.elasticsearch.index.IndexSettings;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A {@link TokenFilterFactory} for {@link TypeTokenFilter}. This filter only
 * keep tokens that are contained in the set configured via
 * {@value #KEEP_TYPES_KEY} setting.
 * <p>
 * Configuration options:
 * <ul>
 * <li>{@value #KEEP_TYPES_KEY} the array of words / tokens to keep.</li>
 * </ul>
 */
public class KeepTypesFilterFactory extends AbstractTokenFilterFactory {
    private final Set<String> keepTypes;
    private static final String KEEP_TYPES_KEY = "types";

    public KeepTypesFilterFactory(IndexSettings indexSettings,
                                 Environment env, String name, Settings settings) {
        super(indexSettings, name, settings);

        final String[] arrayKeepTypes = settings.getAsArray(KEEP_TYPES_KEY, null);
        if ((arrayKeepTypes == null)) {
            throw new IllegalArgumentException("keep_types requires `" + KEEP_TYPES_KEY + "` to be configured");
        }

        this.keepTypes = new HashSet<>(Arrays.asList(arrayKeepTypes));
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new TypeTokenFilter(tokenStream, keepTypes, true);
    }
}