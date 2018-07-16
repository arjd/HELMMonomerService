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
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.helm.chemtoolkit.CTKException;
import org.helm.chemtoolkit.CTKSmilesException;
import org.helm.notation2.Attachment;
import org.helm.notation2.Chemistry;
import org.helm.notation2.exception.ChemistryException;
import org.helm.notation2.tools.SMILES;
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
	private static String connection = "jdbc:sqlite:"
			+ MonomerLibrarySQLite.class.getResource("resources/MonomerLib2.0.db").toString();
	private static int totalCount = 0;

	private final Validation validation = new Validation(LOG);

	public int deleteMonomer(String polymerType, String symbol) throws Exception {
		Connection c = null;
		Statement stmt = null;
		int id = -1;

		try {
			c = getConnection();
			stmt = c.createStatement();

			PreparedStatement pstmt = c
					.prepareStatement("SELECT * from MONOMERS where POLYMERTYPE = ? and SYMBOL = ?;");
			pstmt.setString(1, polymerType);
			pstmt.setString(2, symbol);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				id = rs.getInt("ID");

				stmt.executeUpdate("DELETE from MONOMER_ATTACHMENT where MONOMER_ID = " + id);
				stmt.executeUpdate("DELETE from MONOMERS where POLYMERTYPE = \'" + polymerType + "\' and SYMBOL = \'"
						+ symbol + "\';");

				LOG.info("Monomer with polymertype = " + polymerType + " and symbol = " + symbol + " deleted");

			} else {
				LOG.info("Monomer with polymertype = " + polymerType + " and symbol = " + symbol + " was not found");
				return -1;
			}

			try {
				pstmt.close();
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

	public List<LWMonomer> showMonomerList(String polymerType, String monomerType, String filter, String filterField,
			int offset, int limit) throws Exception {
		Connection c = null;
		PreparedStatement pstmt = null;
		int countClauses = 0;
		boolean polymerTypeBoolean = false;
		boolean monomerTyoeBoolean = false;
		boolean filterBoolean = false;
		boolean filterFieldBoolean = false;
		boolean limitclauseBoolean = false;
		LWMonomer monomer = null;
		ArrayList<LWMonomer> list = new ArrayList<LWMonomer>();

		try {
			c = getConnection();

			// where clause: certain polymer type or all?
			String whereClause = "";
			if (polymerType.equals("ALL")) {
				whereClause = " where polymertype is not null";
			} else {
				whereClause = " where polymertype = ?";
				polymerTypeBoolean = true;
			}

			// monomerType
			if (monomerType != null && !monomerType.isEmpty()) {
				whereClause = whereClause + " and monomertype = ?";
				monomerTyoeBoolean = true;
			}

			// filter clause
			if (filter != null && !filter.isEmpty()) {
				if (filterField != null && !filterField.isEmpty()) {
					whereClause = whereClause + " and " + filterField + " like ?";
					filterBoolean = false;
					filterFieldBoolean = true;
				} else {
					whereClause = whereClause + " and (symbol like ? or name like ?) ";
					filterBoolean = true;
					filterFieldBoolean = false;
				}
			}

			// total count
			pstmt = c.prepareStatement("select count(*) from monomers " + whereClause);
			if (polymerTypeBoolean) {
				countClauses++;
				pstmt.setString(countClauses, polymerType);
			}
			if (monomerTyoeBoolean) {
				countClauses++;
				pstmt.setString(countClauses, monomerType);
			}
			if (filterBoolean) {
				countClauses++;
				pstmt.setString(countClauses, "%" + filter + "%");
				countClauses++;
				pstmt.setString(countClauses, "%" + filter + "%");
			}
			if (filterFieldBoolean) {
				countClauses++;
				pstmt.setString(countClauses, "%" + filter + "%");
			}

			ResultSet rs = pstmt.executeQuery();
			rs.next();
			totalCount = rs.getInt(1);

			String limitclause = "";
			if (limit > 0) {
				limitclause = " limit ? offset ?";
				limitclauseBoolean = true;
			}

			pstmt = c.prepareStatement(
					"SELECT * from (SELECT * from MONOMERS " + whereClause + " order by symbol) " + limitclause);
			countClauses = 0;
			if (polymerTypeBoolean) {
				countClauses++;
				pstmt.setString(countClauses, polymerType);
			}
			if (monomerTyoeBoolean) {
				countClauses++;
				pstmt.setString(countClauses, monomerType);
			}
			if (filterBoolean) {
				countClauses++;
				pstmt.setString(countClauses, "%" + filter + "%");
				countClauses++;
				pstmt.setString(countClauses, "%" + filter + "%");
			}
			if (filterFieldBoolean) {
				countClauses++;
				pstmt.setString(countClauses, "%" + filter + "%");
			}
			if (limitclauseBoolean) {
				countClauses++;
				pstmt.setInt(countClauses, limit);
				countClauses++;
				pstmt.setInt(countClauses, offset);

			}

			System.out.println(pstmt);
			rs = pstmt.executeQuery();

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
				pstmt.close();
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
		LWMonomer monomer = null;

		try {
			c = getConnection();

			PreparedStatement pstmt = c
					.prepareStatement("SELECT * from MONOMERS where POLYMERTYPE = ? and SYMBOL = ?;");
			pstmt.setString(1, polymerType);
			pstmt.setString(2, symbol);
			ResultSet rs = pstmt.executeQuery();

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
				pstmt.close();
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

	public boolean symbolInDatabase(String polymerType, String symbol) throws Exception {
		Connection c = null;
		boolean inDatabase = false;

		try {
			c = getConnection();

			PreparedStatement pstmt = c
					.prepareStatement("SELECT * from MONOMERS where POLYMERTYPE = ? and SYMBOL = ?;");
			pstmt.setString(1, polymerType);
			pstmt.setString(2, symbol);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next())
				inDatabase = true;

			LOG.info("InsertOrUpdateMonomer - Monomer in database: " + inDatabase);

			try {
				pstmt.close();
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
		return inDatabase;
	}

	public boolean structureInDatabase(String polymerType, LWMonomer monomer) throws Exception {
		Connection c = null;
		boolean inDatabase = false;

		try {
			c = getConnection();
			// check if structure is already registered
			PreparedStatement pstmt = c.prepareStatement("SELECT Symbol from MONOMERS where MOLFILE = ?;");
			pstmt.setString(1, monomer.getMolfile()); // .getSmiles());

			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				LOG.info("Monomer with this structure isalready registered with Symbol: " + rs.getString("SYMBOL"));
				monomer.setSymbol(rs.getString("SYMBOL"));
				
			}

			try {
				pstmt.close();
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
		return inDatabase;
	}

	public int insertOrUpdateMonomer(String polymerType, String symbol, LWMonomer monomer) throws Exception {
		if (symbolInDatabase(polymerType, symbol)) {
			return updateMonomer(polymerType, symbol, monomer);
		} else {
			return insertMonomer(monomer);
		}
	}

	public int insertMonomer(LWMonomer monomer) throws Exception {
		Connection c = null;
		int id = -1;

		try {
			c = getConnection();

			// check if Monomer is correct
			if (validation.checkMonomer(monomer) != 0) {
				return validation.checkMonomer(monomer);
			}
			// check if structure is already registered
			PreparedStatement pstmt = c.prepareStatement("SELECT Symbol from MONOMERS where SMILES = ?;");
			pstmt.setString(1, monomer.getSmiles());

			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				LOG.info("Monomer with this structure isalready registered with Symbol: " + rs.getString("SYMBOL"));
				monomer.setSymbol(rs.getString("SYMBOL"));
				return -1000;
			}

			// check if symbol is already used
			pstmt = c.prepareStatement("SELECT Symbol from MONOMERS where POLYMERTYPE = ? and SYMBOL = ?;");
			pstmt.setString(1, monomer.getPolymerType());
			pstmt.setString(2, monomer.getSymbol());
			rs = pstmt.executeQuery();
			if (rs.next()) {
				LOG.info("Monomer with this id is already registered");
				return -2000;
			}

			pstmt = c.prepareStatement(
					"INSERT INTO MONOMERS (SYMBOL, MONOMERTYPE, NAME, NATURALANALOG, MOLFILE, POLYMERTYPE, SMILES, CREATEDATE, AUTHOR) "
							+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)");
			pstmt.setString(1, monomer.getSymbol());
			pstmt.setString(2, monomer.getMonomerType());
			pstmt.setString(3, monomer.getName());
			pstmt.setString(4, monomer.getNaturalAnalog());
			pstmt.setString(5, monomer.getMolfile());
			pstmt.setString(6, monomer.getPolymerType());
			pstmt.setString(7, monomer.getSmiles());
			pstmt.setString(8, monomer.getCreateDate());
			pstmt.setString(9, monomer.getAuthor());

			pstmt.execute();
			rs = pstmt.getGeneratedKeys();
			rs.next();
			id = rs.getInt(1);

			try {
				pstmt.close();
			} catch (Exception e1) {
				throw e1;
			}

			List<Attachment> list = monomer.getRgroups();
			Attachment attachment;
			String sql;
			Statement stmt = null;
			for (int i = 0; i < list.size(); i++) {
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

	public int updateMonomer(String polymerType, String symbol, LWMonomer monomer) throws Exception {
		Connection c = null;
		int id = -1;

		try {
			c = getConnection();

			PreparedStatement pstmt = c
					.prepareStatement("SELECT * from MONOMERS where POLYMERTYPE = ? and SYMBOL = ?;");
			pstmt.setString(1, polymerType);
			pstmt.setString(2, symbol);
			ResultSet rs = pstmt.executeQuery();

			// check if Monomer is registered
			if (!rs.next()) {
				LOG.info("Monomer not in database");
				return -3000;
			}

			id = rs.getInt(1);

			// check if Monomer is correct
			if (validation.checkMonomer(monomer) != 0) {
				return validation.checkMonomer(monomer);
			}

			// check if Smiles is unique
			pstmt = c.prepareStatement("SELECT * from MONOMERS where SMILES = ?;");
			pstmt.setString(1, monomer.getSmiles());
			rs = pstmt.executeQuery();

			LOG.info(monomer.getSmiles());

			while (rs.next()) {
				LOG.info(rs.getString("SYMBOL"));
				if (!rs.getString("SYMBOL").equals(symbol)) {
					LOG.info("Smiles already excists with ID: " + rs.getString("ID"));
					return -1000;
				}
			}

			pstmt = c.prepareStatement(
					"UPDATE MONOMERS set  MONOMERTYPE = ?, NAME = ?, NATURALANALOG = ?, MOLFILE = ?, POLYMERTYPE = ?, SMILES = ?, CREATEDATE = ?, AUTHOR = ? where ID = ?");
			pstmt.setString(1, monomer.getMonomerType());
			pstmt.setString(2, monomer.getName());
			pstmt.setString(3, monomer.getNaturalAnalog());
			pstmt.setString(4, monomer.getMolfile());
			pstmt.setString(5, monomer.getPolymerType());
			pstmt.setString(6, monomer.getSmiles());
			pstmt.setString(7, monomer.getCreateDate());
			pstmt.setString(8, monomer.getAuthor());
			pstmt.setInt(9, id);
			pstmt.executeUpdate();
			try {
				pstmt.close();
			} catch (Exception e1) {
				throw e1;
			}

			Statement stmt = c.createStatement();
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
		return id;
	}

	private int insertAttachment(Attachment attachment) throws Exception {
		Connection c = null;
		int result;

		try {
			c = getConnection();

			PreparedStatement pstmt = c
					.prepareStatement("SELECT ID from ATTACHMENT where LABEL = ? and CAPGROUPNAME = ?;");
			pstmt.setString(1, attachment.getLabel());
			pstmt.setString(2, attachment.getCapGroupName());
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				result = rs.getInt(1);
			} else {
				String capGroupName = attachment.getCapGroupName();
				if (capGroupName != null && capGroupName.contains("\'"))
					capGroupName = capGroupName.replaceAll("\'", "\'\'");

				pstmt = c.prepareStatement(
						"INSERT INTO ATTACHMENT (LABEL, CAPGROUPNAME, CAPGROUPSMILES, ALTERNATEID) VALUES(?, ?, ?, ?);");
				pstmt.setString(1, attachment.getLabel());
				pstmt.setString(2, capGroupName);
				pstmt.setString(3, attachment.getCapGroupSMILES());
				pstmt.setString(4, attachment.getAlternateId());
				pstmt.execute();
				rs = pstmt.getGeneratedKeys();
				rs.next();
				result = rs.getInt(1);
			}

			try {
				pstmt.close();
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
