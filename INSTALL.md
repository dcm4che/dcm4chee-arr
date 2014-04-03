Getting Started with DCM4CHEE ARR 4.3.0-SNAPSHOT
==================================================

Requirements
------------
-   Java SE 6 or later - tested with [OpenJDK](http://openjdk.java.net/)
    and [Oracle JDK](http://java.com/en/download)

-   [JBoss Application Server 7.1.1.Final](http://www.jboss.org/jbossas/downloads)
	or [JBoss Enterprise Application Platform 6.x](http://www.jboss.org/jbossas/downloads)

-   Supported SQL Database:
    - [MySQL 5.6](http://dev.mysql.com/downloads/mysql)
    - [PostgreSQL 9.2.1](http://www.postgresql.org/download/)
	  (not yet tested!)
    - [Firebird 2.5.1](http://www.firebirdsql.org/en/firebird-2-5-1/)
	  (not yet tested!)
    - [DB2 10.1](http://www-01.ibm.com/software/data/db2/express/download.html)
	  (not yet tested!)
    - [Oracle 11g](http://www.oracle.com/technetwork/products/express-edition/downloads/)
    - [Microsoft SQL Server](http://www.microsoft.com/en-us/download/details.aspx?id=29062)
      (not yet tested!)

-   LDAP Server - tested with
    - [Apache DS 2.0.0-M8](http://directory.apache.org/apacheds/2.0/downloads.html).

-   LDAP Browser - [Apache Directory Studio 1.5.3](http://directory.apache.org/studio/)

    *Note*: Both LDAP and Java Preferences can be used to configure
	the ARR, however you need to do the configuration changes manually,
	either in the LDAP tree or in the Preferences XML file.
	Also be careful to load the Preferences XML file in case you will use
	Preferences as your configuration.
	A tool for loading preferences XML is available in the dcm4che library [xml2prefs, xml2prefs.bat]
	(http://sourceforge.net/projects/dcm4che/files/dcm4che3/3.3.1/dcm4che-3.3.1-bin.zip)


Build the binary
------------------------------------------------
After installation of [Maven 3](http://maven.apache.org):

   for java preferences config profile:
 
      mvn install -Ddb={db2|firebird|h2|mysql|oracle|psql|sqlserver}
      
   for ldap config profile:
  
      mvn install -Ddb={db2|firebird|h2|mysql|oracle|psql|sqlserver} -Dldap={apacheds|opends|slapd}

Initial Database Population
-------------------
After building the source, an initial database population can be done using the generated DDL file
in the following directory [dcm4chee-arr-cdi/dcm4chee-arr-entities/target/]
the file will have the following name create-table-${db}.ddl with ${db} as suffix signifying database
used (this is just a maven filter) ie. if you build for oracle it will be create-table-oracle.ddl

Initialize Database
-------------------

### MySQL

1. Enable remote access by commenting out `skip-networking` in configuration file `my.conf`.

2. Create database and grant access to user

        > mysql -u root -p<root-password>
        mysql> CREATE DATABASE <database-name>;
        mysql> GRANT ALL ON <database-name>.* TO '<user-name>' IDENTIFIED BY '<user-password>';
        mysql> quit

3. Create tables and indexes
       
        > mysql -u <user-name> -p<user-password> < create-table-mysql.ddl


### PostgreSQL

1. Create user with permission to create databases 

        > createuser -U postgres -P -d <user-name>
        Enter password for new role: <user-password> 
        Enter it again: <user-password> 

2. Create database

        > createdb -U <user-name> <database-name>

3. Create tables and indexes
       
        > psql -U <user-name> < create-table-psql.ddl


### Firebird

1. Define database name in configuration file `aliases.conf`:

        <database-name> = <database-file-path>

2. Create user

        > gsec -user sysdba -password masterkey \
          -add <user-name> -pw <user-password>

3. Create database, tables and indexes

        > isql 
        Use CONNECT or CREATE DATABASE to specify a database
        SQL> CREATE DATABASE 'localhost:<database-name>'
        CON> user '<user-name>' password '<user-password>';
        SQL> IN create-table-firebird.ddl;
        SQL> EXIT;

        
### DB2

1. Create database and grant authority to create tables to user
   (must match existing OS user)

        > sudo su db2inst1
        > db2
        db2 => CREATE DATABASE <database-name> PAGESIZE 16 K
        db2 => connect to <database-name>
        db2 => GRANT CREATETAB ON DATABASE TO USER <user-name>
        db2 => terminate
 
2. Create tables and indexes

        > su <user-name>
        Password: <user-password>
        > db2 connect to <database-name>
        > db2 -t < create-table-db2.ddl
        > db2 terminate
        

### Oracle 11g 

1. Install the oracle 11g database server
2. during the install you will be prompted to create a new Database
   instance create a new one database-name
3. keep the connection information you are given at the end
4. Connect to Oracle and create a new tablespace

        $ sqlplus / as sysdba
        SQL> CREATE BIGFILE TABLESPACE <tablespace-name> DATAFILE '<data-file-location>' SIZE <size>;

        Tablespace created.

5. Create a new user with privileges for the new tablespace

        $ sqlplus / as sysdba
        SQL> CREATE USER <user-name> 
        2  IDENTIFIED BY <user-password>
        3  DEFAULT TABLESPACE <tablespace-name>
        4  QUOTA UNLIMITED ON <tablespace-name>
        5  QUOTA 50M ON SYSTEM;

        User created.

        SQL> GRANT CREATE SESSION TO <user-name>;
        SQL> GRANT CREATE TABLE TO <user-name>;
        SQL> GRANT CREATE ANY INDEX TO <user-name>;
        SQL> GRANT CREATE SEQUENCE TO <user-name>;
        SQL> exit

6. Create tables and indexes

        $ sqlplus <user-name>/<user-password>
        SQL> @create-table-oracle.ddl

Setup LDAP Server
-----------------

### OpenDJ
Not yet tested.
#### OpenLDAP with slapd.conf configuration file
Not yet tested.
#### OpenLDAP with dynamic runtime configuration
Not yet tested.
### Apache DS 2.0

1.  Install [Apache DS 2.0.0-M8](http://directory.apache.org/apacheds/2.0/downloads.html)
    on your system and start Apache DS.

2.  Install [Apache Directory Studio 1.5.3](http://directory.apache.org/studio/) and
    create a new LDAP Connection with:

        Network Parameter:
            Hostname: localhost
            Port:     10398
        Authentication Parameter:
            Bind DN or user: uid=admin,ou=system
            Bind password:   secret

3.  Import LDAP schema files for Apache DS:
	dcm4chee-arr-cdi-conf-ldap/src/main/config/apacheds/
	imports are to be done in this order:
	
        1.dicom.ldif
        2.dcm4che.ldif
		3.CleanUpExtensionSchema.ldif
		4.EventTypeLoggingSchema.ldif

    using the LDIF import function of Apache Directory Studio LDAP Browser.

4.  You may modify the default Directory Base DN `dc=example,dc=com` by changing
    the value of attribute 

        ads-partitionsuffix: dc=example,dc=com`

    of object

        ou=config
        + ads-directoryServiceId=default
          + ou=partitions
              ads-partitionId=example
    
    using Apache Directory Studio LDAP Browser.


Import sample configuration into LDAP Server
--------------------------------------------  

1.  If not alread done, install
    [Apache Directory Studio 1.5.3](http://directory.apache.org/studio/) and create
    a new LDAP Connection corresponding to your LDAP Server configuration, e.g:

        Network Parameter:
            Hostname: localhost
            Port:     1398
        Authentication Parameter:
            Bind DN or user: uid=admin,ou=system
            Bind password:   secret
        Browser Options:
            Base DN: dc=example,dc=com
			
2.	Import basic configuration template dcm4chee-arr-cdi-conf-ldap/src/main/config/
	
		1.init-config.ldif
		2.arrdevice-sample(no init).ldif
		3.AuditLogger_with_supress_criteria.ldif
			
2.  If you configured a different Directory Base DN than`dc=example,dc=com`,
    you have to replace all occurrences of `dc=example,dc=com` in LDIF files
	before import by your Directory Base DN, e.g.:

        > sed -i s/dc=example,dc=com/dc=my-domain,dc=com/ init-config.ldif
        > sed -i s/dc=example,dc=com/dc=my-domain,dc=com/ arrdevice-sample(no init).ldif
        > sed -i s/dc=example,dc=com/dc=my-domain,dc=com/ AuditLogger_with_supress_criteria.ldif

Setup Java Preferences (LDAP alternative)
----------------
1.  Load the sample xml file dcm4chee-arr-sample.xml from dcm4chee-arr-cdi-conf-prefs/src/main/config/
	into your system properties.
2.	A tool for loading preferences XML is available in the dcm4che library [xml2prefs, xml2prefs.bat]
	(http://sourceforge.net/projects/dcm4che/files/dcm4che3/3.3.1/dcm4che-3.3.1-bin.zip)
		
		
Setup JBoss AS 7
----------------

1.  Create DCM4CHEE ARR LDAP Connection configuration file
    `$JBOSS_HOME/standalone/configuration/dcm4chee-arr/ldap.properties`:

        java.naming.factory.initial=com.sun.jndi.ldap.LdapCtxFactory
		java.naming.ldap.attributes.binary=dicomVendorData
		java.naming.provider.url=ldap://localhost:10389/dc=example,dc=com
		java.naming.security.principal=uid=admin,ou=system
		java.naming.security.credentials=secret

    If required adjust it to your LDAP Server configuration.

2.  Install required libraries as JBoss AS 7 modules:
    
	The Jboss modules can be obtained from the dcm4che library as follows:
		1. Download https://github.com/dcm4che/dcm4che/archive/master.zip
		2. Extract archive
		3.cd dcm4che/dcm4che-jboss-modules/
		4. mvn install
		5.unzip target/dcm4che-jboss-modules-3.0.0-SNAPSHOT.zip -d /$JBOSS_HOME

4.  Install JDBC Driver. DCM4CHEE ARR does not include
    a JDBC driver for the database for license issues. You may download it from:
    -   [MySQL](http://www.mysql.com/products/connector/)
    -   [PostgreSQL]( http://jdbc.postgresql.org/)
    -   [Firebird](http://www.firebirdsql.org/en/jdbc-driver/)
    -   [DB2](http://www-306.ibm.com/software/data/db2/java/), also included in DB2 Express-C
    -   [Oracle](http://www.oracle.com/technology/software/tech/java/sqlj_jdbc/index.htm),
        also included in Oracle 11g XE)
    -   [Microsoft SQL Server](http://msdn.microsoft.com/data/jdbc/)

    The JDBC driver can be installed either as a deployment or as a core module.
    [See](https://docs.jboss.org/author/display/AS71/Developer+Guide#DeveloperGuide-InstalltheJDBCdriver)
    
    Installation as deployment is limited to JDBC 4-compliant driver consisting of **one** JAR.

    For installation as a core module:
		1.	Download https://github.com/dcm4che/jdbc-jboss-modules/archive/master.zip
		2.	Extract archive and mvn install
		3.	Extract target/jdbc-jboss-modules-1.0.0-${db}.zip to $JBOSS_HOME and copy
			the JDBC Driver file(s) into the sub-directory,
			
		e.g.:
        >cp mysql-connector-java-5.1.22-bin.jar  $JBOSS_HOME/modules/com/mysql/main/

    Verify, that the actual JDBC Driver file(s) name matches the path(s) in the provided
    `module.xml`, e.g.:

         <?xml version="1.0" encoding="UTF-8"?>
         <module xmlns="urn:jboss:module:1.1" name="com.mysql">
             <resources>
                 <resource-root path="mysql-connector-java-5.1.22-bin.jar"/>
             </resources>
         
             <dependencies>
                 <module name="javax.api"/>
                 <module name="javax.transaction.api"/>
             </dependencies>
         </module>


5.  Start JBoss AS 7 or EAP 6.X in standalone mode with the Java EE 6 Full Profile configuration.
    To preserve the original JBoss AS 7 configuration you may copy the original
    configuration file for JavaEE 6 Full Profile:

        > cd $JBOSS_HOME/standalone/configuration/
        > cp standalone-full.xml dcm4chee-arr.xml

    and start JBoss AS 7 specifying the new configuration file:
        
        > $JBOSS_HOME/bin/standalone.sh -c dcm4chee-arr.xml [UNIX]
        > %JBOSS_HOME%\bin\standalone.bat -c dcm4chee-arr.xml [Windows]
   
    Verify, that JBoss AS 7 started successfully, e.g.:

        =========================================================================

          JBoss Bootstrap Environment

          JBOSS_HOME: /home/gunter/jboss7

          JAVA: /usr/lib/jvm/java-6-openjdk/bin/java

          JAVA_OPTS:  -server -XX:+UseCompressedOops -XX:+TieredCompilation ...

        =========================================================================

        13:01:48,788 INFO  [org.jboss.modules] JBoss Modules version 1.1.1.GA
        13:01:48,926 INFO  [org.jboss.msc] JBoss MSC version 1.0.2.GA
        13:01:48,969 INFO  [org.jboss.as] JBAS015899: JBoss AS 7.1.1.Final "Brontes" starting
        :
        13:01:51,239 INFO  [org.jboss.as] (Controller Boot Thread) JBAS015874: JBoss AS 7.1.1.Final "Brontes" started ...
                
    Running JBoss AS 7 in domain mode should work, but was not yet tested.

6.  Add JDBC Driver into the server configuration using JBoss AS 7 CLI in a new console window:

        > $JBOSS_HOME/bin/jboss-cli.sh -c [UNIX]
        > %JBOSS_HOME%\bin\jboss-cli.bat -c [Windows]
        [standalone@localhost:9999 /] /subsystem=datasources/jdbc-driver=<driver-name>:add(driver-module-name=<module-name>,driver-name=<driver-name>)

    You may choose any `<driver-name>` for the JDBC Driver, `<module-name>` must match the name
    defined in the module definition file `module.xml` of the JDBC driver, e.g.:

        [standalone@localhost:9999 /] /subsystem=datasources/jdbc-driver=mysql:add(driver-module-name=com.mysql,driver-name=mysql)

7.  Create and enable a new Data Source bound to JNDI name `java:/arrDS` using JBoss AS 7 CLI:

        [standalone@localhost:9999 /] data-source add --name=arrDS \
        >     --driver-name=<driver-name> \
        >     --connection-url=<jdbc-url> \
        >     --jndi-name=java:/arrDS \
        >     --user-name=<user-name> \
        >     --password=<user-password>
        [standalone@localhost:9999 /] data-source enable --name=arrDS

    The format of `<jdbc-url>` is JDBC Driver specific, e.g.:
    -  MySQL: `jdbc:mysql://localhost:3306/<database-name>`
    -  PostgreSQL: `jdbc:postgresql://localhost:5432/<database-name>`
    -  Firebird: `jdbc:firebirdsql:localhost/3050:<database-name>`
    -  DB2: `jdbc:db2://localhost:50000/<database-name>`
    -  Oracle: `jdbc:oracle:thin:@localhost:1521:<database-name>`
    -  Microsoft SQL Server: `jdbc:sqlserver://localhost:1433;databaseName=<database-name>`

8.  Specify a ldap location by system property org.dcm4chee.arr.ldap
    using JBoss AS 7 CLI:

        [standalone@localhost:9999 /] /system-property=org.dcm4chee.arr.ldap:add(value=<url>)

9.  By default, DCM4CHEE ARR will assume `dcm4chee-arr` as its Device Name, used to find its
    configuration in the configuration backend (LDAP Server or Java Preferences). You may specify a different
    Device Name by system property `org.dcm4chee.arr.deviceName` using JBoss AS 7 CLI:

        [standalone@localhost:9999 /] /system-property=org.dcm4chee.arr.deviceName:add(value=<device-name>)

		*Note*
		If you change the device name from the default you will have to change the dn of the sample configuration
		to match the new device name fom dcm4chee-arr to the new name.
		
10. DCM4CHEE ARR requires a keystore file for TLS as well as a truststore
	 
	 By default DCM4CHEE ARR will assume that both the keystore and the truststore are at ${jboss.server.config.url}/dcm4chee-arr/key.jks
	 from the sample config provided and will assume such keystore is of type JKS and with password changeit,
	 It will also assume that you have a key inside with a password changeit.
	 
	 It is up to you to change these configurations according to your needs, if you require proper TLS authentication,
	 then you should provide a new configuration parameter in the ldap config with the following attributes :
	 
	 dcmTrustStoreType
	 dcmTrustStoreURL
	 dcmTrustStorePin
	 
	 You will also need to change the property dcmTLSNeedClientAuth in the audit-tls sample connection configuration
	 to true, thus allowing the ARR to load the trust store and authenticate the incoming certificates.
	 
	 To generate your own key and keystore use the keytool supplied with the jdk as follows:
	 
	 
	 keytool.exe -genkey -alias mynewkey -keyalg RSA -validity 365  -storetype JKS --keystore myserver.store
	 
	 
	 you will be prompted for everything and in the end the file myserver.store will be generated.
	 
	 
	 To extract your public key from the truststore use keytool as follows:
	 
	 
	 keytool -export -alias mynewkey --keystore test.store -rfc -file mypublic.cert
	 
	 
	 Next step is to create a truststore and add your public certificate to it.
	 
	 
	 keytool -import -alias mynewkey -file mypublic.cert --storetype JKS --keystore server.truststore
	 
	 
	 TOTEST: If you would prefer to have your certificates on LDAP without having to use a local trust store this is possible 
	 using the following configuration scheme:
	 1.	add a dicomAuthorizedNodeCertificateReference attribute to your device node with the value a node that is either a device or uses pkiUser ldap object class.
	 2.	in the referenced node have a userCertificate attribute, specify it is binary and in the value you should add the certificate file 
	 (this can be done wasily with the ldap browser, just point to your certificate file using load certificate when adding attribute value)
	 
11.	Add Jms queue to the jboss configuration via CLI as follows:

	[standalone@localhost:9999 /] jms-queue add --queue-address=ARRIncoming --entries=queue/ARRIncoming
	 
12. Deploy DCM4CHEE ARR using JBoss AS 7 CLI, e.g.:

        [standalone@localhost:9999 /] deploy dcm4chee-arr-cdi/dcm4chee-arr-cdi-war/target/dcm4chee-arr-cdi-war-4.3.0-SNAPSHOT-${db}.war

    Verify that DCM4CHEE ARR was deployed and started successfully, e.g.:


13. You may undeploy DCM4CHEE ARR at any time using JBoss AS 7 CLI, e.g.:

        [standalone@localhost:9999 /] undeploy dcm4chee-arr-cdi-war-4.3.0-SNAPSHOT-${db}.war
		
14. You can also use the provided web interface for querying the ARR in an organized and clean way.
	To install the web application just deploy it on jboss as follows:
		[standalone@localhost:9999 /] deploy dcm4chee-arr-cdi/dcm4chee-arr-web/target/dcm4chee-arr-web-4.3.0-SNAPSHOT-${db}.war

Testing DCM4CHEE ARR 4.3.0-SNAPSHOT
----------------------------
Test running:
to test your ARR service is running you can do this via restful service after deployment just point your browser
to http://ip-address-of-JBOSSSERVER:JBOSSPORT/dcm4chee-arr/ctrl/running

Test Stop:
to test your ARR service stops you can do this via restful service after deployment just point your browser
to http://ip-address-of-JBOSSSERVER:JBOSSPORT/dcm4chee-arr/ctrl/stop

Test Start:
to test your ARR service starts you can do this via restful service after deployment just point your browser
to http://ip-address-of-JBOSSSERVER:JBOSSPORT/dcm4chee-arr/ctrl/start

Test Receive Audits:
A tool for sending Audit messages is available in the dcm4che library [syslog, syslog.bat]
	(http://sourceforge.net/projects/dcm4che/files/dcm4che3/3.3.1/dcm4che-3.3.1-bin.zip)

Here is a test message

<AuditMessage>
<EventIdentification EventActionCode=\"E\" EventDateTime=\"2014-03-05T13:35:46.881+01:00\" EventOutcomeIndicator=\"4\">
<EventID code=\"110114\" codeSystemName=\"DCM\" displayName=\"User Authentication\"/>
<EventTypeCode code=\"110122\" codeSystemName=\"DCM\" displayName=\"Login\"/>
</EventIdentification>
<ActiveParticipant UserID=\"admin\" NetworkAccessPointID=\"10.231.163.243\" NetworkAccessPointTypeCode=\"2\"/>
<AuditSourceIdentification AuditSourceID=\"SOMESOURCE\">
<AuditSourceTypeCode code=\"4\"/>
</AuditSourceIdentification>
</AuditMessage>
save it to an xml file and send this test message as follows:
syslog --udp -c ARR-ip:ARR-udp-port savedfile.xml

Test Query Audits:
To query for this received audit message just point your browser to 
http://ip-address-of-JBOSSSERVER:JBOSSPORT/dcm4chee-arr/view/xmllist?UserID=admin&AuditSourceID=SOMESOURCE

Testing the Web application:
To query the archive using the web application provided just point you browser to
http://ip-address-of-JBOSSSERVER:JBOSSPORT/dcm4chee-arr-web/

