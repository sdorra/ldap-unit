ldap-unit
=========

JUnit rule to simplify ldap unit tests.

##Sample:

```java
public class LDAPUnitTest {

  @Test
  @LDAP(ldif = "/path/to/file.ldif")
  public void testLdapMethod(){
    LdapContext context = rule.createLdapContext();
  }

  @Rule
  public LDAPRule rule = new LDAPRule();
}
```
### Maven usage 

Artifacts are deployed to [Maven Central](http://search.maven.org). To use, drop this in your pom.xml: 
```xml
<dependency>
  <groupId>com.github.sdorra</groupId>
  <artifactId>ldap-unit</artifactId>
  <version>1.0.0</version>
</dependency>
```
