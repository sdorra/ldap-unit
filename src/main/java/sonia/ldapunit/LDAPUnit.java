/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.ldapunit;

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
 *
 * @author Sebastian Sdorra
 */
public class LDAPUnit implements MethodRule
{

  /**
   * Method description
   *
   *
   * @param statement
   * @param method
   * @param target
   *
   * @return
   */
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
   * Method description
   *
   *
   * @return
   *
   * @throws NamingException
   */
  public LdapContext createLdapContext() throws NamingException
  {
    return new InitialLdapContext(getConnectionProperties(), null);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String getAdditionalBindDN()
  {
    return additionalBindDN;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getAdditionalBindPassword()
  {
    return additionalBindPassword;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getBaseDN()
  {
    return baseDN;
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws LDAPException
   */
  public LDAPInterface getConnection() throws LDAPException
  {
    return directoryServer.getConnection();
  }

  /**
   * Method description
   *
   *
   * @return
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

  /**
   * Method description
   *
   *
   * @return
   */
  public InMemoryDirectoryServer getDirectoryServer()
  {
    return directoryServer;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public int getPort()
  {
    return port;
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
