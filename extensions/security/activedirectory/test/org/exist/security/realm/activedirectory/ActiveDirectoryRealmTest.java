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
package org.exist.security.realm.activedirectory;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.exist.config.Configuration;
import org.exist.config.Configurator;
import org.exist.security.AuthenticationException;
import org.exist.security.User;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class ActiveDirectoryRealmTest {

	private static String config = 
		"<ActiveDirectory>" +
		"	<context " +
		"		domain='local.domain' " +
		"		searchBase='cn=users,dc=local,dc=domain' " +
		"		url='ldap://localhost:389'/>" +
		"</ActiveDirectory>";

	private static ActiveDirectoryRealm realm;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		InputStream is = new ByteArrayInputStream(config.getBytes("UTF-8"));
		
		Configuration config = Configurator.parse(is);

		realm = new ActiveDirectoryRealm(config);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * Test method for {@link org.exist.security.realm.activedirectory.ActiveDirectoryRealm#authenticate(java.lang.String, java.lang.Object)}.
	 */
	@Test
	public void testAuthenticate() {
		User account = null;
		try {
			account = realm.authenticate("admin", "passwd");
		} catch (AuthenticationException e) {
			fail(e.getMessage());
		}
		
		assertNotNull(account);
	}

}