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

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPInterface;
import com.unboundid.ldif.LDIFReader;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

//~--- JDK imports ------------------------------------------------------------

import java.io.InputStream;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.Hashtable;

import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;

/**
 * The ldap unit starts a in memory directory server for each method
 * which is annotated with the {@link LDAPUnit} annotation.
 *
 * @author Sebastian Sdorra
 */
public class LDAPUnit implements MethodRule
{

  /**
   * {@inheritDoc}
   */
  @Override
  public Statement apply(final Statement statement, FrameworkMethod method,
    Object target)
  {
    LDAP ldap = method.getAnnotation(LDAP.class);

    if (ldap != null)
    {
      try
      {
        port = ldap.port();
        baseDN = ldap.baseDN();
        additionalBindDN = ldap.additionalBindDN();
        additionalBindPassword = ldap.additionalBindPassword();
        directoryServer = createDirectoryServer();

        importLdif(ldap.ldif(), target);
      }
      catch (LDAPException ex)
      {
        throw new RuntimeException(
          "could not start in memory directory server", ex);
      }

      return new Statement()
      {

        @Override
        public void evaluate() throws Throwable
        {
          try
          {
            directoryServer.startListening();
            statement.evaluate();
          }
          finally
          {
            directoryServer.shutDown(true);
          }
        }
      };
    }

    return statement;
  }

  /**
   * Creates a new {@link LdapContext} for the in memory directory server.
   *
   *
   * @return {@link LdapContext} for in memory directory server
   *
   * @throws NamingException
   */
  public LdapContext createLdapContext() throws NamingException
  {
    return new InitialLdapContext(getConnectionProperties(), null);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns unboundid connection for the in memory directory server.
   *
   *
   * @return connection for in memory directory server
   *
   * @throws LDAPException
   */
  public LDAPInterface getConnection() throws LDAPException
  {
    return directoryServer.getConnection();
  }

  /**
   * Returns jndi properties for the in memory directory server.
   *
   *
   * @return jndi properties
   */
  @SuppressWarnings("UseOfObsoleteCollectionType")
  public Hashtable<String, String> getConnectionProperties()
  {
    StringBuilder url = new StringBuilder("ldap://");

    url.append(host).append(":").append(String.valueOf(port)).append("/");

    Hashtable<String, String> props = new Hashtable<String, String>(11);

    props.put(LdapContext.INITIAL_CONTEXT_FACTORY,
      "com.sun.jndi.ldap.LdapCtxFactory");
    props.put(LdapContext.PROVIDER_URL, url.toString());
    props.put(LdapContext.SECURITY_AUTHENTICATION, "simple");
    props.put(LdapContext.SECURITY_PRINCIPAL, additionalBindDN);
    props.put(LdapContext.SECURITY_CREDENTIALS, additionalBindPassword);

    return props;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws LDAPException
   */
  private InMemoryDirectoryServer createDirectoryServer() throws LDAPException
  {
    try
    {
      InMemoryDirectoryServerConfig cfg =
        new InMemoryDirectoryServerConfig(baseDN);

      InetAddress address = InetAddress.getLocalHost();

      host = address.getHostName();

      cfg.addAdditionalBindCredentials(additionalBindDN,
        additionalBindPassword);
      cfg.setListenerConfigs(new InMemoryListenerConfig("listener", address,
        port, ServerSocketFactory.getDefault(), SocketFactory.getDefault(),
        null));

      // disable schema check
      cfg.setSchema(null);

      return new InMemoryDirectoryServer(cfg);
    }
    catch (UnknownHostException ex)
    {
      throw new RuntimeException("could not read localhost address", ex);
    }
  }

  /**
   * Method description
   *
   *
   * @param ldif
   * @param target
   *
   * @throws LDAPException
   */
  private void importLdif(String ldif, Object target) throws LDAPException
  {
    if (ldif != null)
    {
      InputStream stream = target.getClass().getResourceAsStream(ldif);

      if (stream != null)
      {
        LDIFReader reader = new LDIFReader(stream);

        directoryServer.importFromLDIF(true, reader);
      }
      else
      {
        throw new RuntimeException("could not find ldif ".concat(ldif));
      }
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String additionalBindDN;

  /** Field description */
  private String additionalBindPassword;

  /** Field description */
  private String baseDN;

  /** Field description */
  private InMemoryDirectoryServer directoryServer;

  /** Field description */
  private String host;

  /** Field description */
  private int port;
}
