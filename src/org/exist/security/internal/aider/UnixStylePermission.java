/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2010 The eXist Project
 *  http://exist-db.org
 *  
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *  
 *  $Id$
 */
package org.exist.security.internal.aider;

import java.io.DataInput;
import java.io.IOException;
import java.util.StringTokenizer;

import org.exist.security.Permission;
import org.exist.security.SecurityManager;
import org.exist.security.User;
import org.exist.util.SyntaxException;

public class UnixStylePermission implements Permission {

    //default Unix style permissions for a resource
	public final static int DEFAULT_PERM = 0755;
	
    //owner, default to DBA
    private String owner = SecurityManager.DBA_USER;
    private String ownerGroup = SecurityManager.DBA_GROUP;

    //permissions
    private int permissions = DEFAULT_PERM;

    public UnixStylePermission() { }


    /**
     * Construct a Permission with given permissions 
     *
     * @param  permissions  Description of the Parameter
     */
    public UnixStylePermission(int permissions) {
        this.permissions = permissions;
    }

    
    /**
     * Construct a permission with given user, group and permissions
     *
     * @param  user
     * @param  group
     * @param  permissions
     */
    public UnixStylePermission(String user, String group, int permissions) {
        this.owner = user;
        this.ownerGroup = group;
        this.permissions = permissions;
    }
    


    /**
     * Get the active permissions for group
     *
     * @return The groupPermissions value
     */
    public int getGroupPermissions() {
        return ( permissions & 0x38 ) >> 3;
    }


    /**
     * Gets the user who owns this resource
     *
     * @return    The owner value
     */
    public String getOwner() {
        return owner;
    }


    /**
     * Gets the group 
     *
     * @return    The ownerGroup value
     */
    public String getOwnerGroup() {
        return ownerGroup;
    }


    /**
     * Get the permissions
     *
     * @return    The permissions value
     */
    public int getPermissions() {
        return permissions;
    }


    /**
     * Get the active permissions for others
     *
     * @return    The publicPermissions value
     */
    public int getPublicPermissions() {
        return permissions & 0x7;
    }


    /**
     * Get the active permissions for the owner
     *
     * @return    The userPermissions value
     */
    public int getUserPermissions() {
        return ( permissions & 0x1c0 ) >> 6;
    }


    /**
     *  Set the owner group
     *
     *@param  group  The new group value
     */
    public void setGroup( String group ) {
        this.ownerGroup = group;
    }

    /**
     *  Sets permissions for group
     *
     *@param  perm  The new groupPermissions value
     */
    public void setGroupPermissions( int perm ) {
        permissions = permissions | ( perm << 3 );
    }


    /**
     *  Set the owner passed as User object
     *
     *@param  user  The new owner value
     */
    public void setOwner( User user ) {
    	// FIXME: assume guest identity if user gets lost due to a database corruption
    	if(user == null) {
    		this.owner = SecurityManager.GUEST_USER;
    	} else
    		this.owner = user.getName();
        //this.ownerGroup = user.getPrimaryGroup();
    }


    /**
     *  Set the owner
     *
     *@param  user  The new owner value
     */
    public void setOwner( String user ) {
        this.owner = user;
    }

    /**
     *  Set permissions using a string. The string has the
     * following syntax:
     * 
     * [user|group|other]=[+|-][read|write|update]
     * 
     * For example, to set read and write permissions for the group, but
     * not for others:
     * 
     * group=+read,+write,other=-read,-write
     * 
     * The new settings are or'ed with the existing settings.
     * 
     *@param  str                  The new permissions
     *@exception  SyntaxException  Description of the Exception
     */
    public void setPermissions( String str ) throws SyntaxException {
        StringTokenizer tokenizer = new StringTokenizer( str, ",= " );
        String token;
        int shift = -1;
        while ( tokenizer.hasMoreTokens() ) {
            token = tokenizer.nextToken();
            if ( token.equalsIgnoreCase( USER_STRING ) )
                shift = 6;
            else if ( token.equalsIgnoreCase( GROUP_STRING ) )
                shift = 3;
            else if ( token.equalsIgnoreCase( OTHER_STRING ) )
                shift = 0;
            else {
                char modifier = token.charAt( 0 );
                if ( !( modifier == '+' || modifier == '-' ) )
                    throw new SyntaxException( "expected modifier +|-" );
                else
                    token = token.substring( 1 );
                if ( token.length() == 0 )
                    throw new SyntaxException( "'read', 'write' or 'update' " +
                        "expected in permission string" );
                int perm;
                if ( token.equalsIgnoreCase( "read" ) )
                    perm = READ;
                else if ( token.equalsIgnoreCase( "write" ) )
                    perm = WRITE;
                else
                    perm = UPDATE;
                switch ( modifier ) {
                    case '+':
                        permissions = permissions | ( perm << shift );
                        break;
                    default:
                        permissions = permissions & ( ~( perm << shift ) );
                        break;
                }
            }
        }
    }


    /**
     *  Set permissions
     *
     *@param  perm  The new permissions value
     */
    public void setPermissions( int perm ) {
        this.permissions = perm;
    }


    /**
     *  Set permissions for others
     *
     *@param  perm  The new publicPermissions value
     */
    public void setPublicPermissions( int perm ) {
        permissions = permissions | perm;
    }


    /**
     *  Set permissions for the owner
     *
     *@param  perm  The new userPermissions value
     */
    public void setUserPermissions( int perm ) {
        permissions = permissions | ( perm << 6 );
    }


    /**
     *  Format permissions 
     *
     *@return    Description of the Return Value
     */
    public String toString() {
        final char ch[] = {
                ( permissions & ( READ << 6 ) ) == 0 ? '-' : 'r',
                ( permissions & ( WRITE << 6 ) ) == 0 ? '-' : 'w',
                ( permissions & ( UPDATE << 6 ) ) == 0 ? '-' : 'u',
                ( permissions & ( READ << 3 ) ) == 0 ? '-' : 'r',
                ( permissions & ( WRITE << 3 ) ) == 0 ? '-' : 'w',
                ( permissions & ( UPDATE << 3 ) ) == 0 ? '-' : 'u',
                ( permissions & READ ) == 0 ? '-' : 'r',
                ( permissions & WRITE ) == 0 ? '-' : 'w',
                ( permissions & UPDATE ) == 0 ? '-' : 'u'
        };
        return new String(ch);
    }
    
    public boolean validate( User user, int perm ) {
    	return false;
    }
    
    public void read( DataInput istream ) throws IOException {
    }
}