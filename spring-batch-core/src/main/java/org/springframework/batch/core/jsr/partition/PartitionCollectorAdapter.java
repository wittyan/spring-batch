/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.batch.core.jsr.partition;

import java.io.Serializable;
import java.util.Queue;

import javax.batch.api.partition.PartitionCollector;
import javax.batch.operations.BatchRuntimeException;

import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.util.Assert;

/**
 * Adapter class used to wrap a {@link PartitionCollector} so that it can be consumed
 * as a {@link ChunkListener}.  A thread safe {@link Queue} is required along with the
 * {@link PartitionCollector}.  The {@link Queue} is where the result of the call to
 * the PartitionCollector will be placed.
 *
 * @author Michael Minella
 * @since 3.0
 */
public class PartitionCollectorAdapter implements ChunkListener {

	private PartitionCollector collector;
	private Queue<Serializable> partitionQueue;

	public PartitionCollectorAdapter(Queue<Serializable> queue, PartitionCollector collector) {
		Assert.notNull(queue, "A thread safe Queue is required");
		Assert.notNull(collector, "A PartitionCollector is required");

		this.partitionQueue = queue;
		this.collector = collector;
	}

	@Override
	public void beforeChunk(ChunkContext context) {
	}

	@Override
	public void afterChunk(ChunkContext context) {
		try {
			synchronized (partitionQueue) {
				partitionQueue.add(collector.collectPartitionData());
			}
		} catch (Throwable e) {
			throw new BatchRuntimeException("An error occured while collecting data from the PartionCollector", e);
		}
	}

	@Override
	public void afterChunkError(ChunkContext context) {
	}
}
