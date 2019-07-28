/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.codehaus.plexus.archiver.util;

import org.codehaus.plexus.archiver.Ownership;
import org.codehaus.plexus.components.io.attributes.PlexusIoResourceAttributes;

@SuppressWarnings( "JavaDoc" )
public final class OwnershipUtils
{

    private OwnershipUtils()
    {
        // no op
    }

    public static Ownership buildOwner( final PlexusIoResourceAttributes attributes )
    {
        final Ownership ownership = new Ownership();
        ownership.setUserName( attributes.getUserName() );
        ownership.setUserId( attributes.getUserId() );
        ownership.setGroupName( attributes.getGroupName() );
        ownership.setGroupId( attributes.getGroupId() );
        return ownership;
    }

}
