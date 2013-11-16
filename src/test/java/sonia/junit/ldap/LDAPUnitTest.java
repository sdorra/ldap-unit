/**
 * The MIT License
 *
 * Copyright (c) 2013, Sebastian Sdorra
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */



package sonia.junit.ldap;

//~--- non-JDK imports --------------------------------------------------------

import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPInterface;

import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.ldap.LdapContext;

/**
 *
 * @author Sebastian Sdorra
 */
public class LDAPUnitTest
{

  /**
   * Constructs ...
   *
   */
  public LDAPUnitTest() {}

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws NamingException
   */
  @Test
  @LDAP(ldif = "/sonia/junit/ldap/001.ldif")
  public void testCreateLdapContext() throws NamingException
  {
    LdapContext context = null;

    try
    {
      context = ldapUnit.createLdapContext();
      assertNotNull(context);

      Attributes attributes =
        context.getAttributes(
          "uid=dent,ou=People,o=hitchhiker.com,dc=example,dc=com");

      assertNotNull(attributes);

      assertEquals("Arthur Dent", attributes.get("cn").get());
    }
    finally
    {
      if (context != null)
      {
        context.close();
      }
    }
  }

  /**
   * Method description
   *
   *
   * @throws LDAPException
   */
  @Test
  @LDAP(ldif = "/sonia/junit/ldap/001.ldif")
  public void testGetConnection() throws LDAPException
  {
    LDAPInterface connection = ldapUnit.getConnection();

    assertNotNull(connection);

    Entry entry = connection.getEntry(
                    "uid=tricia,ou=People,o=hitchhiker.com,dc=example,dc=com");

    assertNotNull(entry);

    assertEquals("Tricia McMillan", entry.getAttributeValue("cn"));
  }

  /**
   * Method description
   *
   */
  @Test
  @SuppressWarnings("UseOfObsoleteCollectionType")
  @LDAP(ldif = "/sonia/junit/ldap/001.ldif", additionalBindDN = "uid=slarti")
  public void testGetConnectionProperties()
  {
    Hashtable<String, String> env = ldapUnit.getConnectionProperties();

    assertNotNull(env);
    assertEquals("uid=slarti", env.get(Context.SECURITY_PRINCIPAL));
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Rule
  public LDAPUnit ldapUnit = new LDAPUnit();
}
