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
package org.codelibs.elasticsearch.search.aggregations.metrics.tophits;

import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldDocs;
import org.codelibs.elasticsearch.ExceptionsHelper;
import org.codelibs.elasticsearch.common.io.stream.StreamInput;
import org.codelibs.elasticsearch.common.io.stream.StreamOutput;
import org.codelibs.elasticsearch.common.lucene.Lucene;
import org.codelibs.elasticsearch.common.xcontent.XContentBuilder;
import org.codelibs.elasticsearch.search.SearchHits;
import org.codelibs.elasticsearch.search.aggregations.InternalAggregation;
import org.codelibs.elasticsearch.search.aggregations.metrics.InternalMetricsAggregation;
import org.codelibs.elasticsearch.search.aggregations.pipeline.PipelineAggregator;
import org.codelibs.elasticsearch.search.internal.InternalSearchHit;
import org.codelibs.elasticsearch.search.internal.InternalSearchHits;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Results of the {@link TopHitsAggregator}.
 */
public class InternalTopHits extends InternalMetricsAggregation implements TopHits {
    private int from;
    private int size;
    private TopDocs topDocs;
    private InternalSearchHits searchHits;

    public InternalTopHits(String name, int from, int size, TopDocs topDocs, InternalSearchHits searchHits,
            List<PipelineAggregator> pipelineAggregators, Map<String, Object> metaData) {
        super(name, pipelineAggregators, metaData);
        this.from = from;
        this.size = size;
        this.topDocs = topDocs;
        this.searchHits = searchHits;
    }

    /**
     * Read from a stream.
     */
    public InternalTopHits(StreamInput in) throws IOException {
        super(in);
        from = in.readVInt();
        size = in.readVInt();
        topDocs = Lucene.readTopDocs(in);
        assert topDocs != null;
        searchHits = InternalSearchHits.readSearchHits(in);
    }

    @Override
    protected void doWriteTo(StreamOutput out) throws IOException {
        out.writeVInt(from);
        out.writeVInt(size);
        Lucene.writeTopDocs(out, topDocs);
        searchHits.writeTo(out);
    }

    @Override
    public String getWriteableName() {
        return TopHitsAggregationBuilder.NAME;
    }

    @Override
    public SearchHits getHits() {
        return searchHits;
    }

    @Override
    public InternalAggregation doReduce(List<InternalAggregation> aggregations, ReduceContext reduceContext) {
        InternalSearchHits[] shardHits = new InternalSearchHits[aggregations.size()];

        final TopDocs reducedTopDocs;
        final TopDocs[] shardDocs;

        try {
            if (topDocs instanceof TopFieldDocs) {
                Sort sort = new Sort(((TopFieldDocs) topDocs).fields);
                shardDocs = new TopFieldDocs[aggregations.size()];
                for (int i = 0; i < shardDocs.length; i++) {
                    InternalTopHits topHitsAgg = (InternalTopHits) aggregations.get(i);
                    shardDocs[i] = (TopFieldDocs) topHitsAgg.topDocs;
                    shardHits[i] = topHitsAgg.searchHits;
                }
                reducedTopDocs = TopDocs.merge(sort, from, size, (TopFieldDocs[]) shardDocs);
            } else {
                shardDocs = new TopDocs[aggregations.size()];
                for (int i = 0; i < shardDocs.length; i++) {
                    InternalTopHits topHitsAgg = (InternalTopHits) aggregations.get(i);
                    shardDocs[i] = topHitsAgg.topDocs;
                    shardHits[i] = topHitsAgg.searchHits;
                }
                reducedTopDocs = TopDocs.merge(from, size, shardDocs);
            }

            final int[] tracker = new int[shardHits.length];
            InternalSearchHit[] hits = new InternalSearchHit[reducedTopDocs.scoreDocs.length];
            for (int i = 0; i < reducedTopDocs.scoreDocs.length; i++) {
                ScoreDoc scoreDoc = reducedTopDocs.scoreDocs[i];
                int position;
                do {
                    position = tracker[scoreDoc.shardIndex]++;
                } while (shardDocs[scoreDoc.shardIndex].scoreDocs[position] != scoreDoc);
                hits[i] = (InternalSearchHit) shardHits[scoreDoc.shardIndex].getAt(position);
            }
            return new InternalTopHits(name, from, size, reducedTopDocs, new InternalSearchHits(hits, reducedTopDocs.totalHits,
                    reducedTopDocs.getMaxScore()),
                    pipelineAggregators(), getMetaData());
        } catch (IOException e) {
            throw ExceptionsHelper.convertToElastic(e);
        }
    }

    @Override
    public Object getProperty(List<String> path) {
        if (path.isEmpty()) {
            return this;
        } else {
            throw new IllegalArgumentException("path not supported for [" + getName() + "]: " + path);
        }
    }

    @Override
    public XContentBuilder doXContentBody(XContentBuilder builder, Params params) throws IOException {
        searchHits.toXContent(builder, params);
        return builder;
    }
}