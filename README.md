ldap-unit
=========

JUnit rule to simplify ldap unit tests.

##Sample:

```java
public class LDAPUnitTest {

  @Test
  @LDAP(ldif = "/path/to/file.ldif")
  public void testLdapMethod(){
    LdapContext context = ldapUnit.createLdapContext();
  }

  @Rule
  public LDAPUnit ldapUnit = new LDAPUnit();
}
```
