/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.test.unit.index.search.nested;

import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.*;
import org.apache.lucene.search.join.ScoreMode;
import org.apache.lucene.search.join.ToParentBlockJoinQuery;
import org.elasticsearch.common.lucene.search.NotFilter;
import org.elasticsearch.common.lucene.search.XFilteredQuery;
import org.elasticsearch.index.fielddata.FieldDataType;
import org.elasticsearch.index.fielddata.IndexFieldData;
import org.elasticsearch.index.fielddata.fieldcomparator.DoubleValuesComparatorSource;
import org.elasticsearch.index.fielddata.fieldcomparator.SortMode;
import org.elasticsearch.index.fielddata.plain.DoubleArrayIndexFieldData;
import org.elasticsearch.index.search.nested.NestedFieldComparatorSource;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 */
public class DoubleNestedSortingTests extends AbstractNumberNestedSortingTests {

    @Override
    protected FieldDataType getFieldDataType() {
        return new FieldDataType("double");
    }

    @Override
    protected IndexFieldData.XFieldComparatorSource createInnerFieldComparator(String fieldName, SortMode sortMode) {
        DoubleArrayIndexFieldData fieldData = getForField(fieldName);
        return new DoubleValuesComparatorSource(fieldData, null, sortMode);
    }

    @Override
    protected IndexableField createField(String name, int value, Field.Store store) {
        return new DoubleField(name, value, store);
    }

    protected void assertAvgScoreMode(Filter parentFilter, IndexSearcher searcher, IndexFieldData.XFieldComparatorSource innerFieldComparator) throws IOException {
        SortMode sortMode = SortMode.AVG;
        Filter childFilter = new NotFilter(parentFilter);
        NestedFieldComparatorSource nestedComparatorSource = new NestedFieldComparatorSource(sortMode, innerFieldComparator, parentFilter, childFilter);
        Query query = new ToParentBlockJoinQuery(new XFilteredQuery(new MatchAllDocsQuery(), childFilter), new CachingWrapperFilter(parentFilter), ScoreMode.None);
        Sort sort = new Sort(new SortField("field2", nestedComparatorSource));
        TopDocs topDocs = searcher.search(query, 5, sort);
        assertThat(topDocs.totalHits, equalTo(7));
        assertThat(topDocs.scoreDocs.length, equalTo(5));
        assertThat(topDocs.scoreDocs[0].doc, equalTo(11));
        assertThat(((Number) ((FieldDoc) topDocs.scoreDocs[0]).fields[0]).intValue(), equalTo(2));
        assertThat(topDocs.scoreDocs[1].doc, equalTo(7));
        assertThat(((Number) ((FieldDoc) topDocs.scoreDocs[1]).fields[0]).intValue(), equalTo(2));
        assertThat(topDocs.scoreDocs[2].doc, equalTo(3));
        assertThat(((Number) ((FieldDoc) topDocs.scoreDocs[2]).fields[0]).intValue(), equalTo(3));
        assertThat(topDocs.scoreDocs[3].doc, equalTo(15));
        assertThat(((Number) ((FieldDoc) topDocs.scoreDocs[3]).fields[0]).intValue(), equalTo(3));
        assertThat(topDocs.scoreDocs[4].doc, equalTo(19));
        assertThat(((Number) ((FieldDoc) topDocs.scoreDocs[4]).fields[0]).intValue(), equalTo(3));
    }

}