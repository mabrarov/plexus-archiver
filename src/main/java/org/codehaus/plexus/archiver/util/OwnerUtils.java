package org.codehaus.plexus.archiver.util;

import org.codehaus.plexus.archiver.Owner;
import org.codehaus.plexus.components.io.attributes.PlexusIoResourceAttributes;

@SuppressWarnings( "JavaDoc" )
public final class OwnerUtils
{

    private OwnerUtils()
    {
        // no op
    }

    public static Owner buildOwner( final PlexusIoResourceAttributes attributes )
    {
        final Owner owner = new Owner();
        owner.setUserName( attributes.getUserName() );
        owner.setUserId( attributes.getUserId() );
        owner.setGroupName( attributes.getGroupName() );
        owner.setGroupId( attributes.getGroupId() );
        return owner;
    }

}
