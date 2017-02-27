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

package org.codelibs.elasticsearch.action.admin.cluster.storedscripts;

import org.codelibs.elasticsearch.action.support.master.AcknowledgedRequestBuilder;
import org.codelibs.elasticsearch.client.ElasticsearchClient;
import org.codelibs.elasticsearch.common.bytes.BytesReference;

public class PutStoredScriptRequestBuilder extends AcknowledgedRequestBuilder<PutStoredScriptRequest,
        PutStoredScriptResponse, PutStoredScriptRequestBuilder> {

    public PutStoredScriptRequestBuilder(ElasticsearchClient client, PutStoredScriptAction action) {
        super(client, action, new PutStoredScriptRequest());
    }

    public PutStoredScriptRequestBuilder setScriptLang(String scriptLang) {
        request.scriptLang(scriptLang);
        return this;
    }

    public PutStoredScriptRequestBuilder setId(String id) {
        request.id(id);
        return this;
    }

    public PutStoredScriptRequestBuilder setSource(BytesReference source) {
        request.script(source);
        return this;
    }

}