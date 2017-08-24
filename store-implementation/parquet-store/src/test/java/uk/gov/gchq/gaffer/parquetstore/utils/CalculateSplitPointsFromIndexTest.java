/*
 * Copyright 2017. Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.gov.gchq.gaffer.parquetstore.utils;


import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.gov.gchq.gaffer.commonutil.TestGroups;
import uk.gov.gchq.gaffer.data.element.Element;
import uk.gov.gchq.gaffer.exception.SerialisationException;
import uk.gov.gchq.gaffer.parquetstore.index.ColumnIndex;
import uk.gov.gchq.gaffer.parquetstore.index.GraphIndex;
import uk.gov.gchq.gaffer.parquetstore.index.GroupIndex;
import uk.gov.gchq.gaffer.parquetstore.index.MinValuesWithPath;
import uk.gov.gchq.gaffer.parquetstore.operation.addelements.impl.CalculateSplitPointsFromIndex;
import uk.gov.gchq.gaffer.parquetstore.testutils.TestUtils;
import uk.gov.gchq.gaffer.store.StoreException;
import uk.gov.gchq.gaffer.store.schema.Schema;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class CalculateSplitPointsFromIndexTest {

    private SchemaUtils schemaUtils;
    private Schema gafferSchema;

    @Before
    public void setUp() throws StoreException {
        Logger.getRootLogger().setLevel(Level.WARN);
        gafferSchema = TestUtils.gafferSchema("schemaUsingLongVertexType");
        schemaUtils = new SchemaUtils(gafferSchema);
    }

    @Test
    public void calculateSplitsFromEmptyIndex() throws SerialisationException {
        final Iterable<Element> emptyIterable = new ArrayList<>();
        final GraphIndex emptyIndex = new GraphIndex();
        final Map<String, Map<Integer, Object>> splitPoints = CalculateSplitPointsFromIndex.apply(emptyIndex, schemaUtils, TestUtils.getParquetStoreProperties(), emptyIterable);
        for (final String group : gafferSchema.getGroups()) {
            Assert.assertTrue(splitPoints.containsKey(group));
            Assert.assertTrue(splitPoints.get(group).isEmpty());
        }
    }

    @Test
    public void calculateSplitsFromIndexUsingEntities() throws SerialisationException, StoreException {
        final Iterable<Element> emptyIterable = new ArrayList<>();
        final GraphIndex index = new GraphIndex();
        final GroupIndex entityGroupIndex = new GroupIndex();
        index.add(TestGroups.ENTITY, entityGroupIndex);
        final ColumnIndex vrtIndex = new ColumnIndex();
        entityGroupIndex.add(ParquetStoreConstants.VERTEX, vrtIndex);
        vrtIndex.add(new MinValuesWithPath(new Object[]{0L}, "part-0.parquet"));
        vrtIndex.add(new MinValuesWithPath(new Object[]{6L}, "part-1.parquet"));
        final Map<String, Map<Integer, Object>> splitPoints = CalculateSplitPointsFromIndex.apply(index, schemaUtils, TestUtils.getParquetStoreProperties(), emptyIterable);
        final Map<Integer, Object> expected = new TreeMap<>();
        expected.put(0, 0L);
        expected.put(1, 6L);
        for (final String group : gafferSchema.getGroups()) {
            Assert.assertTrue(splitPoints.containsKey(group));
            if (TestGroups.ENTITY.equals(group)) {
                Assert.assertEquals(expected, splitPoints.get(TestGroups.ENTITY));
            } else {
                Assert.assertTrue(splitPoints.get(group).isEmpty());
            }
        }
    }

    @Test
    public void calculateSplitsFromIndexUsingEdges() throws SerialisationException, StoreException {
        final Iterable<Element> emptyIterable = new ArrayList<>();
        final GraphIndex index = new GraphIndex();
        final GroupIndex entityGroupIndex = new GroupIndex();
        index.add(TestGroups.EDGE, entityGroupIndex);
        final ColumnIndex srcIndex = new ColumnIndex();
        entityGroupIndex.add(ParquetStoreConstants.SOURCE, srcIndex);
        srcIndex.add(new MinValuesWithPath(new Object[]{0L}, "part-0.parquet"));
        srcIndex.add(new MinValuesWithPath(new Object[]{6L}, "part-1.parquet"));
        final Map<String, Map<Integer, Object>> splitPoints = CalculateSplitPointsFromIndex.apply(index, schemaUtils, TestUtils.getParquetStoreProperties(), emptyIterable);
        final Map<Integer, Object> expected = new TreeMap<>();
        expected.put(0, 0L);
        expected.put(1, 6L);
        for (final String group : gafferSchema.getGroups()) {
            Assert.assertTrue(splitPoints.containsKey(group));
            if (TestGroups.EDGE.equals(group)) {
                Assert.assertEquals(expected, splitPoints.get(TestGroups.EDGE));
            } else {
                Assert.assertTrue(splitPoints.get(group).isEmpty());
            }
        }
    }
}
