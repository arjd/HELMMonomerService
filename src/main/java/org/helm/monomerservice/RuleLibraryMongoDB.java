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

import java.util.ArrayList;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;

public class RuleLibraryMongoDB implements IRuleLibrary {
	
	private static final Logger LOG = LoggerFactory.getLogger(MonomerLibraryMongoDB.class);

	private final Validation validation = new Validation(LOG);



	@Override
	public int deleteRule(int id) throws Exception {
		MongoClient mongoClient = new MongoClient(LibraryManager.getHostname(),
				Integer.parseInt(LibraryManager.getPort()));
		MongoDatabase database = mongoClient.getDatabase(LibraryManager.getDatabase());
		try {
			MongoCollection<Document> collection = database.getCollection("rules");
			BasicDBObject mQuery = new BasicDBObject();
			mQuery = mQuery.append("id", id);
			
			try (MongoCursor<Document> cur = collection.find(mQuery).iterator()) {
				if (cur.hasNext()) {
					collection.deleteOne(mQuery);
					return id;
				}
				return -1;
			}
		} catch (Exception e) {
			LOG.error("Deleting of rule failed.");
			throw e;
		} finally {
			mongoClient.close();
			LOG.info("Closed database.");
		}
	}

	@Override
	public Rule showRule(int id) throws Exception {
		Rule rule = null;
		JsonConverter converter = new JsonConverter();
		MongoClient mongoClient = new MongoClient(LibraryManager.getHostname(),
				Integer.parseInt(LibraryManager.getPort()));
		MongoDatabase database = mongoClient.getDatabase(LibraryManager.getDatabase());
		try {
			MongoCollection<Document> collection = database.getCollection("rules");
			BasicDBObject mQuery = new BasicDBObject();
			mQuery = mQuery.append("id", id);
			
			try (MongoCursor<Document> cur = collection.find(mQuery).iterator()) {
				if (cur.hasNext()) {
					Document doc = cur.next();
					rule = converter.decodeRule(doc.toJson());
				}
			}
		return rule;	
		} catch (Exception e) {
			LOG.error("Reading of rule failed.");
			throw e;
		} finally {
			mongoClient.close();
			LOG.info("Closed database.");
		}
	}

	@Override
	public ArrayList<Rule> showAllRules() throws Exception {
		Rule rule = null;
		ArrayList<Rule> list = new ArrayList<Rule>();
		JsonConverter converter = new JsonConverter();
		MongoClient mongoClient = new MongoClient(LibraryManager.getHostname(),
				Integer.parseInt(LibraryManager.getPort()));
		MongoDatabase database = mongoClient.getDatabase(LibraryManager.getDatabase());
		try {
			MongoCollection<Document> collection = database.getCollection("rules");
			try (MongoCursor<Document> cur = collection.find().sort(Sorts.ascending("name")).iterator()) {
				while (cur.hasNext()) {
					Document doc = cur.next();
					// System.out.println(doc.toJson());
					rule = converter.decodeRule(doc.toJson());
					list.add(rule);
				}
			}
		return list;	
		} catch (Exception e) {
			LOG.error("Reading of rules failed.");
			throw e;
		} finally {
			mongoClient.close();
			LOG.info("Closed database.");
		}
	}

	@Override
	public int insertOrUpdateRule(Rule rule) throws Exception {
		
		//Check rule
		if(validation.checkRule(rule) != 0) {
			return validation.checkRule(rule);
		}
		
		JsonConverter converter = new JsonConverter();
		String ruleName = rule.getName();
		int ruleID = rule.getId();
		MongoClient mongoClient = new MongoClient(LibraryManager.getHostname(),
				Integer.parseInt(LibraryManager.getPort()));
		MongoDatabase database = mongoClient.getDatabase(LibraryManager.getDatabase());
		
		try {
			MongoCollection<Document> collection = database.getCollection("rules");
			BasicDBObject mQuery = new BasicDBObject();
			mQuery = mQuery.append("id", ruleID);
			Document newDoc = Document.parse(converter.encodeRule(rule));
			
			try (MongoCursor<Document> cur = collection.find(mQuery).iterator()) {
				if (cur.hasNext()) {
					//update
					
					mQuery = new BasicDBObject();
					mQuery = mQuery.append("name", ruleName);
					MongoCursor<Document> cur1 = collection.find(mQuery).iterator();
					while(cur1.hasNext()) {
						Document doc = cur1.tryNext();
						Rule ruleDB = converter.decodeRule(doc.toJson());
						if(ruleDB.getId() != ruleID) {
							LOG.info("Name of rule already exists with ID: " + ruleDB.getId());
							return -1;
						}
					}
					collection.replaceOne(mQuery, newDoc);
				} else {
					//insert
					
					mQuery = new BasicDBObject();
					mQuery = mQuery.append("name", ruleName);
					MongoCursor<Document> cur1 = collection.find(mQuery).iterator();
					if(cur1.hasNext()) {
						LOG.info("Name of rule already exists");
						return -1;
					}
					
					//new ID
					// if we have an empty database, we will start with 1
					int id = 0;
					try (MongoCursor<Document> cur2 = collection.find().sort(Sorts.descending("id")).limit(1).iterator()) {
						if (cur2.hasNext()) {
							Document doc = cur2.next();
							id = converter.decodeRule(doc.toJson()).getId() + 1;
						}
					}
					// set id in JSON
					rule.setId(id);
					// insert new monomer
					Document newDoc2 = Document.parse(converter.encodeRule(rule));
					collection.insertOne(newDoc2);
				}
			}
		return 0;	
		} catch (Exception e) {
			LOG.error("Reading of rule failed.");
			throw e;
		} finally {
			mongoClient.close();
			LOG.info("Closed database.");
		}
	}

}
