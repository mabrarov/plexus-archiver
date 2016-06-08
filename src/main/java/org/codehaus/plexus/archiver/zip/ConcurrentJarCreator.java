/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.codehaus.plexus.archiver.zip;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import org.apache.commons.compress.archivers.zip.ParallelScatterZipCreator;
import org.apache.commons.compress.archivers.zip.ScatterZipOutputStream;
import org.apache.commons.compress.archivers.zip.StreamCompressor;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.parallel.InputStreamSupplier;
import org.apache.commons.compress.parallel.ScatterGatherBackingStore;
import org.apache.commons.compress.parallel.ScatterGatherBackingStoreSupplier;
import static org.apache.commons.compress.archivers.zip.ZipArchiveEntryRequest.createZipArchiveEntryRequest;

public class ConcurrentJarCreator
{

    private final ScatterZipOutputStream directories;

    private final ScatterZipOutputStream metaInfDir;

    private final ScatterZipOutputStream manifest;

    private final ScatterZipOutputStream synchronousEntries;

    private final ParallelScatterZipCreator parallelScatterZipCreator;

    private long zipCloseElapsed;

    private static class DeferredSupplier
        implements ScatterGatherBackingStoreSupplier
    {

        private int threshold;

        DeferredSupplier( int threshold )
        {
            this.threshold = threshold;
        }

        public ScatterGatherBackingStore get()
            throws IOException
        {
            return new DeferredScatterOutputStream( threshold );
        }

    }

    public static ScatterZipOutputStream createDeferred(
        ScatterGatherBackingStoreSupplier scatterGatherBackingStoreSupplier )
        throws IOException
    {
        ScatterGatherBackingStore bs = scatterGatherBackingStoreSupplier.get();
        StreamCompressor sc = StreamCompressor.create( Deflater.DEFAULT_COMPRESSION, bs );
        return new ScatterZipOutputStream( bs, sc );
    }

    public ConcurrentJarCreator( int nThreads ) throws IOException
    {
        ScatterGatherBackingStoreSupplier defaultSupplier = new DeferredSupplier( 100000000 / nThreads );
        directories = createDeferred( defaultSupplier );
        manifest = createDeferred( defaultSupplier );
        metaInfDir = createDeferred( defaultSupplier );
        synchronousEntries = createDeferred( defaultSupplier );
        parallelScatterZipCreator = new ParallelScatterZipCreator( Executors.newFixedThreadPool( nThreads ),
                                                                   defaultSupplier );

    }

    /**
     * Adds an archive entry to this archive.
     * <p/>
     * This method is expected to be called from a single client thread
     *
     * @param zipArchiveEntry The entry to add. Compression method
     * @param source The source input stream supplier
     * @param addInParallel Indicates if the entry should be add in parallel.
     * If set to {@code false} the entry is added synchronously.
     *
     * @throws java.io.IOException
     */
    public void addArchiveEntry( final ZipArchiveEntry zipArchiveEntry, final InputStreamSupplier source,
                                 final boolean addInParallel ) throws IOException
    {
        final int method = zipArchiveEntry.getMethod();
        if ( method == -1 )
        {
            throw new IllegalArgumentException( "Method must be set on the supplied zipArchiveEntry" );
        }
        if ( zipArchiveEntry.isDirectory() && !zipArchiveEntry.isUnixSymlink() )
        {
            final ByteArrayInputStream payload = new ByteArrayInputStream( new byte[]
            {
            } );

            directories.addArchiveEntry( createZipArchiveEntryRequest( zipArchiveEntry, createInputStreamSupplier(
                                                                       payload ) ) );

            payload.close();
        }
        else if ( "META-INF".equals( zipArchiveEntry.getName() ) || "META-INF/".equals( zipArchiveEntry.getName() ) )
        {
            InputStream payload = source.get();
            if ( zipArchiveEntry.isDirectory() )
            {
                zipArchiveEntry.setMethod( ZipEntry.STORED );
            }
            metaInfDir.addArchiveEntry( createZipArchiveEntryRequest( zipArchiveEntry,
                                                                      createInputStreamSupplier( payload ) ) );

            payload.close();
        }
        else if ( "META-INF/MANIFEST.MF".equals( zipArchiveEntry.getName() ) )
        {
            InputStream payload = source.get();
            if ( zipArchiveEntry.isDirectory() )
            {
                zipArchiveEntry.setMethod( ZipEntry.STORED );
            }
            manifest.addArchiveEntry( createZipArchiveEntryRequest( zipArchiveEntry,
                                                                    createInputStreamSupplier( payload ) ) );

            payload.close();
        }
        else if ( addInParallel )
        {
            parallelScatterZipCreator.addArchiveEntry( zipArchiveEntry, source );
        }
        else
        {
            synchronousEntries.addArchiveEntry( createZipArchiveEntryRequest( zipArchiveEntry, source ) );
        }
    }

    private InputStreamSupplier createInputStreamSupplier( final InputStream payload )
    {
        return new InputStreamSupplier()
        {

            @Override
            public InputStream get()
            {
                return payload;
            }

        };
    }

    public void writeTo( ZipArchiveOutputStream targetStream ) throws IOException, ExecutionException,
                                                                      InterruptedException
    {
        metaInfDir.writeTo( targetStream );
        manifest.writeTo( targetStream );
        directories.writeTo( targetStream );
        synchronousEntries.writeTo( targetStream );
        parallelScatterZipCreator.writeTo( targetStream );
        long startAt = System.currentTimeMillis();
        targetStream.close();
        zipCloseElapsed = System.currentTimeMillis() - startAt;
        metaInfDir.close();
        manifest.close();
        directories.close();
        synchronousEntries.close();
    }

    /**
     * Returns a message describing the overall statistics of the compression run
     *
     * @return A string
     */
    public String getStatisticsMessage()
    {
        return parallelScatterZipCreator.getStatisticsMessage() + " Zip Close: " + zipCloseElapsed + "ms";
    }

}
