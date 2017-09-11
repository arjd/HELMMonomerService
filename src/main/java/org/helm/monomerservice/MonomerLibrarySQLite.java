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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.helm.notation2.Attachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides access to SQLite database
 * 
 * Description of methods in IConfigLoaderMonomerLibrary
 */

public class MonomerLibrarySQLite implements IMonomerLibrary {

	private static final Logger LOG = LoggerFactory.getLogger(MonomerLibrarySQLite.class);

	private static String forname = "org.sqlite.JDBC";
	private static String connection = "jdbc:sqlite:" + MonomerLibrarySQLite.class.getResource("resources/MonomerLib2.0.db").toString();
	private static int totalCount = 0;

	public int deleteMonomer(String polymerType, String symbol) throws Exception {
		Connection c = null;
		Statement stmt = null;
		int id = -1;

		try {
			c = getConnection();
			stmt = c.createStatement();
			
			ResultSet rs = stmt.executeQuery("SELECT * from MONOMERS where POLYMERTYPE = \'" + polymerType
					+ "\' and SYMBOL = \'" + symbol + "\';");

			if (rs.next()) {
				id = rs.getInt("ID");

				stmt.executeUpdate("DELETE from MONOMER_ATTACHMENT where MONOMER_ID = " + id);
				stmt.executeUpdate("DELETE from MONOMERS where POLYMERTYPE = \'" + polymerType + "\' and SYMBOL = \'"
						+ symbol + "\';");

				LOG.info("Monomer with polymertype = " + polymerType + " and symbol = " + symbol + " deleted");
				
			} else {
				LOG.info("Monomer with polymertype = " + polymerType + " and symbol = " + symbol + " was not found");
			}

			try {
				stmt.close();
				c.commit();
			} catch (Exception e1) {
				throw e1;
			}
			return id;
		} catch (Exception e2) {
			c.rollback();
			throw e2;
		} finally {
			c.close();
			LOG.info("Closed database ..");
		}

	}


	public List<LWMonomer> showMonomerList(String polymerType, String monomerType, String filter, int offset, int limit) throws Exception {
		Connection c = null;
		Statement stmt = null;
		LWMonomer monomer = null;
		ArrayList<LWMonomer> list = new ArrayList<LWMonomer>();

		try {
			c = getConnection();
			stmt = c.createStatement();
			
			//where clause: certain polymer type or all?
			String whereClause = "";
			if (polymerType.equals("ALL")) {
				whereClause = " where polymertype is not null";
			} else {
				whereClause = " where polymertype = \'"  + polymerType + "\'";
			}
			
			//monomerType
			if (monomerType != null && !monomerType.isEmpty()) {
				whereClause = whereClause + " and monomertype = \'" + monomerType + "\' ";
			}
			
			//filter clause
			if (filter != null && !filter.isEmpty()) {
				whereClause = whereClause + " and symbol like \'" + filter + "%\' ";
			}
			
			//total count
			ResultSet rs = stmt.executeQuery("select count(*) from monomers " + whereClause);
			rs.next();
			totalCount = rs.getInt(1);
			
			String limitclause = "";
			if (limit > 0) {
				limitclause = " limit " + String.valueOf(limit) + " offset " + String.valueOf(offset);
			}
			
			rs = stmt.executeQuery("SELECT * from (SELECT * from MONOMERS " + whereClause + 
					" order by symbol) " + limitclause);

			while (rs.next()) {
				monomer = new LWMonomer(rs.getString("AUTHOR"), rs.getString("SYMBOL"), rs.getString("POLYMERTYPE"),
						rs.getString("NAME"), rs.getString("NATURALANALOG"), rs.getString("MOLFILE"),
						rs.getString("SMILES"), rs.getString("MONOMERTYPE"), null);
				int id = rs.getInt("ID");
				monomer.setId(id);

				monomer.setRgroups(buildAttachmentList(c, id));

				list.add(monomer);

				LOG.info("Monomer with ID = " + id + " shown");

			}

			try {
				stmt.close();
			} catch (Exception e1) {
				throw e1;
			}
		} catch (Exception e2) {
			c.rollback();
			throw e2;
		} finally {
			c.close();
			LOG.info("Closed database ..");
		}
		return list;
	}

	@SuppressWarnings("resource")
	public List<LWMonomer> showAllMonomers() throws Exception {
		Connection c = null;
		Statement stmt = null;
		LWMonomer monomer = null;
		ArrayList<LWMonomer> list = new ArrayList<LWMonomer>();
		int a = 0;

		try {
			c = getConnection();
			stmt = c.createStatement();

			ResultSet rs = stmt.executeQuery("SELECT * from MONOMERS;");

			while (rs.next()) {
				monomer = new LWMonomer(rs.getString("AUTHOR"), rs.getString("SYMBOL"), rs.getString("POLYMERTYPE"),
						rs.getString("NAME"), rs.getString("NATURALANALOG"), rs.getString("MOLFILE"),
						rs.getString("SMILES"), rs.getString("MONOMERTYPE"), null);
				int id = rs.getInt("ID");
				monomer.setId(id);

				monomer.setRgroups(buildAttachmentList(c, id));

				list.add(monomer);

				LOG.info("Monomer with ID = " + id + " shown");

				rs = stmt.executeQuery("SELECT * from MONOMERS;");

				for (int i = 0; i <= a; i++) {
					rs.next();
				}
				a++;
			}

			try {
				stmt.close();
			} catch (Exception e1) {
				throw e1;
			}
		} catch (Exception e2) {
			c.rollback();
			throw e2;
		} finally {
			c.close();
			LOG.info("Closed database ..");
		}
		return list;
	}

	public LWMonomer monomerDetail(String polymerType, String symbol) throws Exception {
		Connection c = null;
		Statement stmt = null;
		LWMonomer monomer = null;

		try {
			c = getConnection();
			stmt = c.createStatement();

			String sql = "SELECT * from MONOMERS where POLYMERTYPE = '" + polymerType + "' and SYMBOL = '" + symbol
					+ "';";
			ResultSet rs = stmt.executeQuery(sql);

			if (rs.next()) {
				monomer = new LWMonomer(rs.getString("AUTHOR"), rs.getString("SYMBOL"), rs.getString("POLYMERTYPE"),
						rs.getString("NAME"), rs.getString("NATURALANALOG"), rs.getString("MOLFILE"),
						rs.getString("SMILES"), rs.getString("MONOMERTYPE"), null);
				int id = rs.getInt("ID");
				monomer.setId(id);

				monomer.setRgroups(buildAttachmentList(c, id));

				LOG.info("Monomer with ID = " + id + " shown");
			}

			try {
				stmt.close();
			} catch (Exception e1) {
				throw e1;
			}
		} catch (Exception e2) {
			c.rollback();
			throw e2;
		} finally {
			c.close();
			LOG.info("Closed database ..");
		}
		return monomer;
	}

	


	public LWMonomer insertOrUpdateMonomer(String polymerType, String symbol, LWMonomer monomer) throws Exception {
		Connection c = null;
		Statement stmt = null;
		boolean inDatabase = false;

		try {
			c = getConnection();
			stmt = c.createStatement();

			ResultSet rs = stmt.executeQuery("SELECT * from MONOMERS where POLYMERTYPE = \'" + polymerType
					+ "\'and SYMBOL = \'" + symbol + "\';");

			if (rs.next())
				inDatabase = true;

			LOG.info("InsertOrUpdateMonomer - Monomer in database: " + inDatabase);

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
			LOG.info("Closed database ..");
		}

		if (inDatabase) {
			monomer = updateMonomer(polymerType, symbol, monomer);
		} else {
			insertMonomer(monomer);
			monomer = monomerDetail(polymerType, symbol);
		}
		return monomer;
	}

	public int insertMonomer(LWMonomer monomer) throws Exception {
		Connection c = null;
		Statement stmt = null;
		int id = -1;

		try {
			c = getConnection();
			stmt = c.createStatement();
			
			//check if structur is already registered
			ResultSet rs = stmt.executeQuery("SELECT Symbol from MONOMERS where POLYMERTYPE = \'" + monomer.getPolymerType()
					+ "\' and SMILES = \'" + monomer.getSmiles() + "\';");

			if (rs.next()) {
				LOG.info("Monomer with this structure isalready registered with ID: " + rs.getString("SYMBOL"));
				return -1;
			}
			
			//check if symbol is already used
			rs = stmt.executeQuery("SELECT Symbol from MONOMERS where POLYMERTYPE = \'" + monomer.getPolymerType()
					+ "\' and SYMBOL = \'" + monomer.getSymbol() + "\';");

			if (rs.next()) {
				LOG.info("Monomer with this id is already registered");
				return -1;
			}

			String sql = "INSERT INTO MONOMERS (SYMBOL, MONOMERTYPE, NAME, NATURALANALOG, MOLFILE, POLYMERTYPE, SMILES, CREATEDATE, AUTHOR) "
					+ "VALUES('" + monomer.getSymbol() + "','" + monomer.getMonomerType() + "',?"
					+ ",'" + monomer.getNaturalAnalog() + "','" + monomer.getMolfile() + "','"
					+ monomer.getPolymerType() + "','" + monomer.getSmiles() + "','" + monomer.getCreateDate() + "','"
					+ monomer.getAuthor() + "')";
			PreparedStatement preparedStatement = c.prepareStatement(sql);
			preparedStatement.setString(1, monomer.getName());
			preparedStatement.execute();
			rs = preparedStatement.getGeneratedKeys();
			//stmt.execute(sql);
			//rs = stmt.getGeneratedKeys();
			rs.next();
			id = rs.getInt(1);

			List<Attachment> list = monomer.getRgroups();
			Attachment attachment;
			for (int i = 0; i < list.size(); i++) {
				stmt.close();
				c.commit();
				c.close();

				attachment = list.get(i);
				int attachmentID = insertAttachment(attachment);

				c = getConnection();
				c.setAutoCommit(false);
				stmt = c.createStatement();

				sql = "INSERT INTO MONOMER_ATTACHMENT (MONOMER_ID, ATTACHMENT_ID)" + "VALUES(" + id + "," + attachmentID
						+ ");";
				stmt.executeUpdate(sql);
			}

			LOG.info("Monomer was inserted with ID: " + id);

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
			LOG.info("Closed database ..");
		}
		return id;
	}

	public LWMonomer updateMonomer(String polymerType, String symbol, LWMonomer monomer) throws Exception {
		Connection c = null;
		Statement stmt = null;

		try {
			c = getConnection();
			stmt = c.createStatement();

			ResultSet rs = stmt.executeQuery("SELECT * from MONOMERS where POLYMERTYPE = \'" + polymerType
					+ "\'and SYMBOL = \'" + symbol + "\';");
			rs.next();
			int id = rs.getInt(1);

			
			String sql = "UPDATE MONOMERS set  MONOMERTYPE = \'"
					+ monomer.getMonomerType() + "\', NAME = ?, NATURALANALOG = \'"
					+ monomer.getNaturalAnalog() + "\', MOLFILE = \'" + monomer.getMolfile() + "\', POLYMERTYPE = \'"
					+ monomer.getPolymerType() + "\', SMILES = \'" + monomer.getSmiles() + "\', CREATEDATE = \'"
					+ monomer.getCreateDate() + "\', AUTHOR = \'" + monomer.getAuthor() + "\' where ID = " + id;
			PreparedStatement preparedStatement = c.prepareStatement(sql);
			preparedStatement.setString(1, monomer.getName());
			preparedStatement.executeUpdate();
			
			
			stmt.executeUpdate("DELETE from MONOMER_ATTACHMENT where MONOMER_ID = " + id);

			List<Attachment> list = monomer.getRgroups();
			Attachment attachment;
			for (int i = 0; i < list.size(); i++) {
				stmt.close();
				c.commit();
				c.close();

				attachment = list.get(i);
				int attachmentID = insertAttachment(attachment);

				c = getConnection();
				c.setAutoCommit(false);
				stmt = c.createStatement();

				stmt.executeUpdate("INSERT INTO MONOMER_ATTACHMENT (MONOMER_ID, ATTACHMENT_ID)" + "VALUES(" + id + ","
						+ attachmentID + ");");
			}

			LOG.info("Monomer with ID: " + id + " was updated");

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
			LOG.info("Closed database ..");
		}
		return monomer;
	}

	private int insertAttachment(Attachment attachment) throws Exception {
		Connection c = null;
		Statement stmt = null;
		int result;

		try {
			c = getConnection();
			stmt = c.createStatement();

			ResultSet rs = stmt.executeQuery("SELECT ID from ATTACHMENT where LABEL = \'" + attachment.getLabel()
					+ "\'and CAPGROUPNAME = \'" + attachment.getCapGroupName() + "\';");

			if (rs.next()) {
				result = rs.getInt(1);
			} else {
				String capGroupName = attachment.getCapGroupName();
				if (capGroupName != null && capGroupName.contains("\'"))
					capGroupName = capGroupName.replaceAll("\'", "\'\'");

				String sql = "INSERT INTO ATTACHMENT (LABEL, CAPGROUPNAME, CAPGROUPSMILES, ALTERNATEID)" + "VALUES('" + attachment.getLabel() + "','"
						+ capGroupName + "','" + attachment.getCapGroupSMILES() + "','" + attachment.getAlternateId() + "');";
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

	private List<Attachment> buildAttachmentList(Connection c, int id) throws SQLException {
		ResultSet rs;
		List<Attachment> rgroups = new ArrayList<Attachment>();
		Attachment attachment;
		Statement stmt = c.createStatement();
		rs = stmt.executeQuery(
				"SELECT * from ATTACHMENT INNER JOIN MONOMER_ATTACHMENT ON MONOMER_ATTACHMENT.ATTACHMENT_ID = ATTACHMENT.ID "
						+ "WHERE MONOMER_ATTACHMENT.MONOMER_ID = " + id);

		if (rs.next()) {
			attachment = new Attachment(rs.getString("LABEL"), rs.getString("CAPGROUPNAME"));
			attachment.setAlternateId(rs.getString("ALTERNATEID"));
			attachment.setCapGroupSMILES(rs.getString("CAPGROUPSMILES"));
			//System.out.println(rs.getString("CAPGROUPSMILES"));
			rgroups.add(attachment);
			if (rs.next()) {
				attachment = new Attachment(rs.getString("LABEL"), rs.getString("CAPGROUPNAME"));
				attachment.setAlternateId(rs.getString("ALTERNATEID"));
				attachment.setCapGroupSMILES(rs.getString("CAPGROUPSMILES"));
				rgroups.add(attachment);
				if (rs.next()) {
					attachment = new Attachment(rs.getString("LABEL"), rs.getString("CAPGROUPNAME"));
					attachment.setAlternateId(rs.getString("ALTERNATEID"));
					attachment.setCapGroupSMILES(rs.getString("CAPGROUPSMILES"));
					rgroups.add(attachment);
				}
			}
		}

		return rgroups;
	}

	private Connection getConnection() throws Exception {
		Connection c = null;
		try {
			Class.forName(forname);
			c = DriverManager.getConnection(connection);
			c.setAutoCommit(false);
			LOG.info("Opened database ..");
		} catch (Exception e) {
			throw e;
		}
		return c;
	}
	
	public int getTotalCount() {
		return totalCount;
	}
}
