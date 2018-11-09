/*******************************************************************************
 * Copyright C 2017, The Pistoia Alliance
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/

package org.helm.monomerservice;

import org.helm.notation2.Attachment;
import org.helm.notation2.Monomer;
import org.helm.notation2.MonomerFactory;
import org.helm.notation2.MonomerStore;
import org.helm.notation2.tools.SMILES;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This creates the database MonomerLib2.0.db Run "Main" to create the database
 * <p>
 * To use the database, copy it from the project-folder into the resource-folder
 */

public class SQLiteMonomers {

    private static final Logger LOG = LoggerFactory.getLogger(MonomerLibrarySQLite.class);
    private static String forname = "org.sqlite.JDBC";
    private static String connection;

    public void buildDB() throws Exception {
        renewDB();
		
		/*
		String path = System.getProperty("user.dir")
				+ "/src/test/resources/org/helm/monomerservice/resources/MonomerLib2.0.db";
		File file = new File(path);
		file.delete();
		connection = "jdbc:sqlite:" + path;

		buildMonomers();
		loadMonomersFromStore();
		insertRules();
		
		String path2 = System.getProperty("user.dir")
				+ "/src/main/resources/org/helm/monomerservice/resources/MonomerLib2.0.db";
		
		File file2 = new File(path2);
		file2.delete();
		
		Files.copy(file.toPath(),file2.toPath(),StandardCopyOption.REPLACE_EXISTING);
		*/
    }

    private void renewDB() throws Exception {
        String path = System.getProperty("user.dir")
                + "/src/main/resources/org/helm/monomerservice/resources/BackupOriginalFromMonomerStoreMonomerLib2.0.db";
        File file = new File(path);

        String path1 = System.getProperty("user.dir")
                + "/src/main/resources/org/helm/monomerservice/resources/MonomerLib2.0.db";
        File file1 = new File(path1);
        file1.delete();

        String path2 = System.getProperty("user.dir")
                + "/src/test/resources/org/helm/monomerservice/resources/MonomerLib2.0.db";
        File file2 = new File(path2);
        file2.delete();

        Files.copy(file.toPath(), file1.toPath(), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(file.toPath(), file2.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    public void buildDBForTesting() throws Exception {
        String path = System.getProperty("user.dir")
                + "/src/main/resources/org/helm/monomerservice/resources/MonomerLib2.0.db";
        File file = new File(path);

        String path1 = System.getProperty("user.dir")
                + "/src/test/resources/org/helm/monomerservice/resources/MonomerLib2.0.db";
        File file1 = new File(path1);
        file1.delete();


        Files.copy(file.toPath(), file1.toPath(), StandardCopyOption.REPLACE_EXISTING);
		/*
		connection = "jdbc:sqlite:" + path;
		buildMonomers();
		loadMonomersFromStore();
		insertRules();	
		*/
    }
    
    
    //Just for testing new DBs if everything is ok; (except SMILES)
    public void setNewSmilesAndValidateMonomers() throws Exception{
    	
    	//Change path (the first one) to DB that should be tested
    	//change method at  @BeforeClass, 
    	//remove '//' at showMonomerList (=> after '// for testing a DB')
    	//and start test 'showAllMonomers' => look at the Log-file
    	
    	//path to be changed
    	String path = System.getProperty("user.dir")
                + "/src/main/resources/org/helm/monomerservice/resources/MonomerLib2.0.db";
        File file = new File(path);

        String path1 = System.getProperty("user.dir")
                + "/src/test/resources/org/helm/monomerservice/resources/MonomerLib2.0.db";
        File file1 = new File(path1);
        file1.delete();


        Files.copy(file.toPath(), file1.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private void buildMonomers() {
        Connection c = null;
        Statement stmt = null;

        try {
            c = getConnection();
            System.out.println("Opened database successfully");

            stmt = c.createStatement();
            String sql = "CREATE TABLE MONOMERS " + "(ID				INTEGER PRIMARY KEY	AUTOINCREMENT,"
                    + "SYMBOL 			TEXT 		UNIQUE		NOT NULL ,"
                    + "MONOMERTYPE		TEXT					NOT NULL,"
                    + "NAME 			TEXT     				NOT NULL,"
                    + "NATURALANALOG 	TEXT 			 	 	NOT NULL, "
                    + "MOLFILE     		TEXT     	UNIQUE		NOT NULL, "
                    + "POLYMERTYPE		TEXT					NOT NULL,"
                    + "SMILES 			TEXT					NOT NULL,"
                    + "CREATEDATE		TEXT							,"
                    + "AUTHOR			TEXT					NOT NULL)";
            stmt.executeUpdate(sql);

            sql = "CREATE TABLE ATTACHMENT " + "(ID			INTEGER	 PRIMARY KEY AUTOINCREMENT,"
                    + "ALTERNATEID			TEXT							,"
                    + "LABEL				TEXT     				NOT NULL,"
                    + "CAPGROUPNAME			TEXT 			 	 	NOT NULL, "
                    + "CAPGROUPSMILES		TEXT					)";
            stmt.executeUpdate(sql);

            sql = "CREATE TABLE MONOMER_ATTACHMENT " + "(ID			INTEGER	 PRIMARY KEY AUTOINCREMENT,"
                    + "MONOMER_ID			INTEGER			NOT NULL," + "ATTACHMENT_ID		INTEGER			NOT NULL)";
            stmt.executeUpdate(sql);

            sql = "CREATE TABLE RULES " + "(ID			INTEGER	 PRIMARY KEY AUTOINCREMENT,"
                    + "_ID			TEXT							,"
                    + "CATEGORY		TEXT     				NOT NULL,"
                    + "NAME			TEXT 			 	 	NOT NULL, "
                    + "AUTHOR		TEXT					NOT NULL,"
                    + "DESCRIPTION	TEXT     				NOT NULL, "
                    + "SCRIPT      TEXT 					NOT NULL)";
            stmt.executeUpdate(sql);

            stmt.close();
            c.commit();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    private void insertMonomer(LWMonomer monomer) throws Exception {
        Connection c = null;
        Statement stmt = null;

        try {
            String smiles = SMILES.convertMolToSMILESWithAtomMapping(monomer.getMolfile(), monomer.getRgroups());

            smiles = smiles.replaceAll("\\s+", "");
            monomer.setSmiles(smiles);
        } catch (Exception e) {

        }

        try {
            c = getConnection();
            System.out.println("Opened database successfully");

            stmt = c.createStatement();
            String sql = generateInsertStringForLWMonomer(monomer);
            stmt.executeUpdate(sql);
            ResultSet rs = stmt.getGeneratedKeys();
            rs.next();
            int monomerID = rs.getInt(1);

            List<Attachment> list = monomer.getRgroups();
            Attachment attachment;
            for (int i = 0; i < list.size(); i++) {
                stmt.close();
                c.commit();
                c.close();

                attachment = list.get(i);
                int attachmentID = insertAttachment(attachment);

                c = getConnection();
                stmt = c.createStatement();

                sql = "INSERT INTO MONOMER_ATTACHMENT (MONOMER_ID, ATTACHMENT_ID)" + "VALUES(" + monomerID + ","
                        + attachmentID + ");";
                stmt.executeUpdate(sql);
            }

            stmt.close();
            c.commit();
            c.close();
            System.out.println("Records created successfully");
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            stmt.close();
            c.close();
        }
    }

    private String generateInsertStringForLWMonomer(LWMonomer monomer) {
        String ret = null;

        String symbol = monomer.getSymbol();
        if (symbol != null && symbol.contains("\'"))
            symbol = symbol.replaceAll("\'", "\'\'");

        String monomerType = monomer.getMonomerType();
        if (monomerType != null && monomerType.contains("\'"))
            monomerType = monomerType.replaceAll("\'", "\'\'");

        String name = monomer.getName();
        if (name != null && name.contains("\'"))
            name = name.replaceAll("\'", "\\''");

        String naturalAnalog = monomer.getNaturalAnalog();
        if (naturalAnalog != null && naturalAnalog.contains("\'"))
            naturalAnalog = naturalAnalog.replaceAll("\'", "\'\'");

        String molFile = monomer.getMolfile();
        if (molFile != null && molFile.contains("\'"))
            molFile = molFile.replaceAll("\'", "\'\'");

        String polymerType = monomer.getPolymerType();
        if (polymerType != null && polymerType.contains("\'"))
            polymerType = polymerType.replaceAll("\'", "\'\'");

        String smiles = monomer.getSmiles();
        if (smiles != null && smiles.contains("\'"))
            smiles = smiles.replaceAll("\'", "\'\'");

        String createDate = monomer.getCreateDate();

        String author = monomer.getAuthor();

        ret = "INSERT INTO MONOMERS (SYMBOL, MONOMERTYPE, NAME, NATURALANALOG, MOLFILE, POLYMERTYPE, SMILES, CREATEDATE, AUTHOR)"
                + "VALUES ('" + symbol + "','" + monomerType + "','" + name + "','" + naturalAnalog + "','" + molFile
                + "','" + polymerType + "','" + smiles + "','" + createDate + "','" + author + "');";
        LOG.info("Insertion string: " + ret);
        return ret;
    }

    private int insertAttachment(Attachment attachment) throws Exception {
        Connection c = null;
        Statement stmt = null;
        int result;

        try {
            try {
                c = getConnection();
                LOG.info("Opened database successfully");
            } catch (Exception e) {
                throw e;
            }

            stmt = c.createStatement();
            LOG.info(attachment.getCapGroupName());

            ResultSet rs = stmt.executeQuery("SELECT ID from ATTACHMENT where LABEL = \'" + attachment.getLabel()
                    + "\'and CAPGROUPNAME = \'" + attachment.getCapGroupName() + "\';" + "\'and ALTERNATEID = \'"
                    + attachment.getAlternateId() + "\';" + "\'and CAPGROUPSMILES = \'" + attachment.getCapGroupSMILES()
                    + "\';");

            if (rs.next()) {
                result = rs.getInt(1);
            } else {
                String capGroupName = attachment.getCapGroupName();
                if (capGroupName != null && capGroupName.contains("\'"))
                    capGroupName = capGroupName.replaceAll("\'", "\'\'");

                String sql = "INSERT INTO ATTACHMENT (ALTERNATEID, LABEL, CAPGROUPNAME, CAPGROUPSMILES)" + "VALUES('"
                        + attachment.getAlternateId() + "','" + attachment.getLabel() + "','" + capGroupName + "','"
                        + attachment.getCapGroupSMILES() + "');";
                stmt.execute(sql);
                rs = stmt.getGeneratedKeys();
                rs.next();
                result = rs.getInt(1);
            }

            try {
                stmt.close();
                c.commit();
            } catch (Exception e1) {
                throw e1;
            }

        } catch (Exception e2) {
            c.rollback();
            throw e2;
        } finally {
            c.close();
        }
        return result;
    }

    private void loadMonomersFromStore() throws Exception {
        MonomerStore store = MonomerFactory.getInstance().getMonomerStore();
        Map<String, Map<String, Monomer>> mon = store.getMonomerDB();
        for (Map.Entry<String, Map<String, Monomer>> entry : mon.entrySet()) {
            LOG.debug(entry.getKey());
            Map<String, Monomer> map = entry.getValue();
            for (Map.Entry<String, Monomer> entry1 : map.entrySet()) {
                LOG.debug(entry1.getKey());
                Monomer monomer = entry1.getValue();

                LWMonomer lwMonomer = MonomerConverter.convertMonomerToLWMonomer(monomer);
                insertMonomer(lwMonomer);
            }
        }

    }

    private void insertRules() {
        Connection c = null;
        Statement stmt = null;

        try {
            c = getConnection();
            LOG.info("Opened database successfully");

            stmt = c.createStatement();
            String sql = "INSERT INTO RULES (CATEGORY,NAME,AUTHOR,DESCRIPTION,SCRIPT) "
                    + "VALUES ('General','Replace base A with U','Pistoia Alliance','Replace base A with U','function(plugin){var n = plugin.replaceMonomer(org.helm.webeditor.HELM.BASE, ''A'',''U'');return n>0;}');";
            stmt.executeUpdate(sql);

            sql = "INSERT INTO RULES (CATEGORY,NAME,AUTHOR,DESCRIPTION,SCRIPT) "
                    + "VALUES ('General','Replace base A with G','Pistoia Alliance','Replace base A with G','function(plugin){var n = plugin.replaceMonomer(org.helm.webeditor.HELM.BASE, ''A'',''G'');return n>0;}');";
            stmt.executeUpdate(sql);

            sql = "INSERT INTO RULES (CATEGORY,NAME,AUTHOR,DESCRIPTION,SCRIPT) "
                    + "VALUES ('General','Replace base A with T','Pistoia Alliance','Replace base A with T','function(plugin){var n = plugin.replaceMonomer(org.helm.webeditor.HELM.BASE, ''A'',''T'');return n>0;}');";
            stmt.executeUpdate(sql);

            stmt.close();
            c.commit();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        LOG.info("Records created successfully");
    }

    private Connection getConnection() throws Exception {
        Connection c = null;
        try {
            Class.forName(forname);
            c = DriverManager.getConnection(connection);
            c.setAutoCommit(false);
        } catch (Exception e) {
            throw e;
        }
        return c;
    }
}
