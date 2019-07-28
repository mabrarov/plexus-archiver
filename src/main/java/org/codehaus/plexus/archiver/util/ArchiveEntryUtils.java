/*
 * Copyright 2014 The Codehaus Foundation.
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
package org.codehaus.plexus.archiver.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.attribute.UserPrincipalNotFoundException;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.Ownership;
import org.codehaus.plexus.components.io.attributes.AttributeUtils;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.Os;

@SuppressWarnings( "JavaDoc" )
public final class ArchiveEntryUtils
{

    private ArchiveEntryUtils()
    {
        // no op
    }

    /**
     * This method is now deprecated.
     *
     * The {@code useJvmChmod} flag is ignored as the JVM is always used.
     * The {@code logger} provided is no longer used.
     *
     * @deprecated Use {@link #chmod(File, int)}
     */
    @Deprecated
    public static void chmod( final File file, final int mode, final Logger logger, boolean useJvmChmod )
        throws ArchiverException
    {
        chmod( file, mode );
    }

    /**
     * This method is now deprecated.
     *
     * The {@code logger} provided is no longer used.
     *
     * @deprecated Use {@link #chmod(File, int)}
     */
    @Deprecated
    public static void chmod( final File file, final int mode, final Logger logger )
        throws ArchiverException
    {
        chmod( file, mode );
    }

    public static void chmod( final File file, final int mode )
        throws ArchiverException
    {
        if ( !Os.isFamily( Os.FAMILY_UNIX ) )
        {
            return;
        }

        try
        {
            AttributeUtils.chmod( file, mode );
        }
        catch ( IOException e )
        {
            throw new ArchiverException( "Failed setting file attributes", e );
        }
    }

    public static void chown( final File file, final Ownership ownership ) throws ArchiverException
    {
        if ( ownership == null )
        {
            return;
        }
        final String userName = ownership.getUserName();
        final String groupName = ownership.getGroupName();
        if ( userName == null && groupName == null )
        {
            return;
        }
        final Path path = file.toPath();
        final UserPrincipalLookupService principalLookupService = getPrincipalLookupService( path );
        if ( principalLookupService == null )
        {
            // Lookup for user and group are not supported by filesystem created file
            return;
        }
        if ( userName != null )
        {
            setOwner( path, getUserPrincipal( principalLookupService, userName ) );
        }
        if ( groupName != null )
        {
            setGroup( path, getGroupPrincipal( principalLookupService, groupName ) );
        }
    }

    private static UserPrincipalLookupService getPrincipalLookupService( final Path path )
    {
        try
        {
            return path.getFileSystem().getUserPrincipalLookupService();
        }
        catch ( UnsupportedOperationException e )
        {
            return null;
        }
    }

    private static UserPrincipal getUserPrincipal( final UserPrincipalLookupService principalLookupService,
                                                   final String userName )
    {
        try
        {
            return principalLookupService.lookupPrincipalByName( userName );
        }
        catch ( UserPrincipalNotFoundException e )
        {
            throw new ArchiverException( "User not found", e );
        }
        catch ( IOException e )
        {
            throw new ArchiverException( "User lookup failed", e );
        }
    }

    private static GroupPrincipal getGroupPrincipal( final UserPrincipalLookupService principalLookupService,
                                                     final String groupName )
    {
        try
        {
            return principalLookupService.lookupPrincipalByGroupName( groupName );
        }
        catch ( UserPrincipalNotFoundException e )
        {
            throw new ArchiverException( "Group not found", e );
        }
        catch ( IOException e )
        {
            throw new ArchiverException( "Group lookup failed", e );
        }
    }

    private static void setOwner( final Path path, final UserPrincipal userPrincipal )
    {
        try
        {
            Files.setOwner( path, userPrincipal );
        }
        catch ( IOException e )
        {
            throw new ArchiverException( "Failed setting file ownership", e );
        }
    }

    private static void setGroup( final Path path, final GroupPrincipal groupPrincipal )
    {
        final PosixFileAttributeView attributeView = Files.getFileAttributeView( path, PosixFileAttributeView.class,
                LinkOption.NOFOLLOW_LINKS );
        if ( attributeView == null )
        {
            return;
        }
        try
        {
            attributeView.setGroup( groupPrincipal );
        }
        catch ( IOException e )
        {
            throw new ArchiverException( "Failed setting file group", e );
        }
    }

}
