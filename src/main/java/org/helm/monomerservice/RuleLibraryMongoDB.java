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

	@Override
	public void deleteRule(int id) throws Exception {
		// TODO Auto-generated method stub
		
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
	public Rule insertOrUpdateRule(Rule rule) throws Exception {
		JsonConverter converter = new JsonConverter();
		String ruleName = rule.getName();
		MongoClient mongoClient = new MongoClient(LibraryManager.getHostname(),
				Integer.parseInt(LibraryManager.getPort()));
		MongoDatabase database = mongoClient.getDatabase(LibraryManager.getDatabase());
		try {
			MongoCollection<Document> collection = database.getCollection("rules");
			BasicDBObject mQuery = new BasicDBObject();
			mQuery = mQuery.append("name", ruleName);
			Document newDoc = Document.parse(converter.encodeRule(rule));
			
			try (MongoCursor<Document> cur = collection.find(mQuery).iterator()) {
				if (cur.hasNext()) {
					//update
					collection.replaceOne(mQuery, newDoc);
				} else {
					//insert
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
		return rule;	
		} catch (Exception e) {
			LOG.error("Reading of rule failed.");
			throw e;
		} finally {
			mongoClient.close();
			LOG.info("Closed database.");
		}
	}

}
