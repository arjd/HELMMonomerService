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
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides access to SQLite database
 * 
 * Description of methods in IConfigLoaderRulesLibrary
 */

public class RuleLibrarySQLite implements IRuleLibrary {

	private static final Logger LOG = LoggerFactory.getLogger(RuleLibrarySQLite.class);

	private static String forname = "org.sqlite.JDBC";
	private static String connection = "jdbc:sqlite:" + MonomerLibrarySQLite.class.getResource("resources/MonomerLib2.0.db").toString();

	public void deleteRule(int id) throws Exception {
		Connection c = null;
		Statement stmt = null;

		try {
			c = getConnection();
			stmt = c.createStatement();

			String sql = "DELETE from RULES where ID = " + id;
			stmt.executeUpdate(sql);

			LOG.info("Rule with ID = " + id + " deleted");

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
	}


	public Rule showRule(int id) throws Exception {
		Connection c = null;
		Statement stmt = null;
		Rule rule = null;
		
		try {
			c = getConnection();
			stmt = c.createStatement();
			
			ResultSet rs = stmt.executeQuery("SELECT * FROM RULES WHERE ID = " + id);
				
			rule = new Rule(id, rs.getString("CATEGORY"), rs.getString("NAME"), rs.getString("DESCRIPTION"), rs.getString("SCRIPT"), rs.getString("AUTHOR"));
			//rule.set_id(rs.getString("_ID"));

			LOG.info("Rule with ID: " + id + " shown");

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
		return rule;
	}


	public ArrayList<Rule> showAllRules() throws Exception {
		Connection c = null;
		Statement stmt = null;
		Rule rule;
		ArrayList<Rule> list = new ArrayList<Rule>();
		
		
		try {
			c = getConnection();
			stmt = c.createStatement();
			
			ResultSet rs = stmt.executeQuery("SELECT * FROM RULES");

			while (rs.next()) {
				int id = rs.getInt("ID");
				
				rule = new Rule(id, rs.getString("CATEGORY"), rs.getString("NAME"), rs.getString("DESCRIPTION"), rs.getString("SCRIPT"), rs.getString("AUTHOR"));
				//rule.set_id(rs.getString("_ID"));
				
				list.add(rule);
				
				LOG.info("ShowAllRules - Rule with ID: " + id + " shown");
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


	public Rule insertOrUpdateRule(Rule rule) throws Exception {
		Connection c = null;
		Statement stmt = null;
		String sql;

		try {
			c = getConnection();
			stmt = c.createStatement();
			//check if rule already exists
			ResultSet rs = stmt.executeQuery("SELECT * from rules where ID = " + rule.getId());
			if (rs.next()) {
				sql = "update rules set category = \'" + rule.getCategory() + "\', name = \'" +
						rule.getName() + "\', author = \'" + rule.getAuthor() + "\', description = \'" +
						rule.getDescription() + "\', script = \'" + rule.getScript().replaceAll("'", "''") + "\' where id = " + rule.getId();
				stmt.execute(sql);
			} else {

				sql = "INSERT INTO RULES (CATEGORY,NAME,AUTHOR,DESCRIPTION,SCRIPT,ID) " + "VALUES('" + rule.getCategory()
				+ "','" + rule.getName() + "','" + rule.getAuthor() + "','" + rule.getDescription() + "','" + rule.getScript() + "','" + rule.getId() +"')";
				stmt.execute(sql);
				rs = stmt.getGeneratedKeys();
				rs.next();
				int id = rs.getInt(1);
				rule.setId(id);
				LOG.info("Rule was inserted with ID: " + id);
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
			LOG.info("Closed database ..");
		}
		
		return rule;
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
}
