/*
 * Licensed to Metamarkets Group Inc. (Metamarkets) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Metamarkets licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.druid.query.aggregation.distinctcount;

import io.druid.collections.bitmap.MutableBitmap;
import io.druid.query.aggregation.Aggregator;
import io.druid.segment.DimensionSelector;
import io.druid.segment.NullHandlingHelper;
import io.druid.segment.data.IndexedInts;
import org.roaringbitmap.IntIterator;

public class DistinctCountAggregator implements Aggregator
{

  private final DimensionSelector selector;
  private final MutableBitmap mutableBitmap;

  public DistinctCountAggregator(
      DimensionSelector selector,
      MutableBitmap mutableBitmap
  )
  {
    this.selector = selector;
    this.mutableBitmap = mutableBitmap;
  }

  @Override
  public void aggregate()
  {
    IndexedInts row = selector.getRow();
    for (int i = 0; i < row.size(); i++) {
      int index = row.get(i);
      mutableBitmap.add(index);
    }
  }

  @Override
  public void reset()
  {
    mutableBitmap.clear();
  }

  @Override
  public Object get()
  {
    return countValues();
  }

  @Override
  public float getFloat()
  {
    return (float) countValues();
  }

  @Override
  public void close()
  {
    mutableBitmap.clear();
  }

  @Override
  public long getLong()
  {
    return (long) countValues();
  }

  @Override
  public double getDouble()
  {
    return (double) countValues();
  }

  private int countValues()
  {
    if (NullHandlingHelper.useDefaultValuesForNull()) {
      return mutableBitmap.size();
    }
    int retVal = 0;
    IntIterator iterator = mutableBitmap.iterator();
    while (iterator.hasNext()) {
      String val = selector.lookupName(iterator.next());
      if (val != null) {
        retVal++;
      }
    }
    return retVal;
  }
}
