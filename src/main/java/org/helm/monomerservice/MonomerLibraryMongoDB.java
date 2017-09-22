package org.helm.monomerservice;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.DeleteResult;

public class MonomerLibraryMongoDB implements IMonomerLibrary {
	
	private static int totalCount = 0;
	
	private static final Logger LOG = LoggerFactory.getLogger(MonomerLibraryMongoDB.class);

	@Override
	public int deleteMonomer(String polymerType, String symbol) throws Exception {
		MongoClient mongoClient = new MongoClient(LibraryManager.getHostname(),Integer.parseInt(LibraryManager.getPort()));
		MongoDatabase database = mongoClient.getDatabase(LibraryManager.getDatabase());
		MongoCollection<Document> collection = database.getCollection("monomers");
		
		BasicDBObject mQuery = new BasicDBObject();
		mQuery = mQuery.append("polymerType",polymerType);
		mQuery = mQuery.append("symbol",symbol);
		
		try (MongoCursor<Document> cur = collection.find(mQuery).iterator()) {
			while (cur.hasNext()) {
                Document doc = cur.next();
                System.out.println(doc.toJson());
            }
		}
		DeleteResult r = collection.deleteOne(mQuery);
		
        mongoClient.close();
		return (int) r.getDeletedCount();
	}

	@Override
	public List<LWMonomer> showMonomerList(String polymerType, String monomerType, String filter, int offset, int limit)
			throws Exception {
		LWMonomer monomer = null;
		ArrayList<LWMonomer> list = new ArrayList<LWMonomer>();
		
		JsonConverter converter = new JsonConverter();
		
		MongoClient mongoClient = new MongoClient(LibraryManager.getHostname(),Integer.parseInt(LibraryManager.getPort()));
		MongoDatabase database = mongoClient.getDatabase(LibraryManager.getDatabase());
		MongoCollection<Document> collection = database.getCollection("monomers");
		
		BasicDBObject mQuery = new BasicDBObject();
		
		//certain polymer type or all?
		if (!polymerType.equals("ALL")) {
			mQuery = mQuery.append("polymerType",polymerType);
		}

		//symbol filter
		if (filter != null && !filter.isEmpty()) {
			Pattern j = Pattern.compile(filter, Pattern.CASE_INSENSITIVE);
			mQuery = mQuery.append("symbol",j);
		}

		//monomerType
		if (monomerType != null && !monomerType.isEmpty()) {
			mQuery = mQuery.append("monomerType", monomerType);
		}
		
		
		try (MongoCursor<Document> cur = collection.find(mQuery)
				.sort(Sorts.ascending("symbol"))
				.limit(limit)
				.skip(offset)
				.iterator()) {
			while (cur.hasNext()) {
                Document doc = cur.next();
                //System.out.println(doc.toJson());
                monomer = converter.decodeMonomer(doc.toJson());
                list.add(monomer);
            }
		}
		// total record count
			totalCount = 0;
			try (MongoCursor<Document> cur2 = collection.find(mQuery)
					.sort(Sorts.ascending("symbol"))
					.iterator()) {
				while (cur2.hasNext()) {
	                totalCount = totalCount + 1;
	            }	
		}
        mongoClient.close();
		return list;
	}

	@Override
	public List<LWMonomer> showAllMonomers() throws Exception {
		
		return showMonomerList("ALL", "", "", 0, 0);
	}

	@Override
	public LWMonomer monomerDetail(String polymerType, String symbol) throws Exception {
		MongoClient mongoClient = new MongoClient(LibraryManager.getHostname(),Integer.parseInt(LibraryManager.getPort()));
		MongoDatabase database = mongoClient.getDatabase(LibraryManager.getDatabase());
		MongoCollection<Document> collection = database.getCollection("monomers");
		LWMonomer monomer = null;
		JsonConverter converter = new JsonConverter();
		
		BasicDBObject mQuery = new BasicDBObject();
		mQuery = mQuery.append("polymerType",polymerType);
		mQuery = mQuery.append("symbol",symbol);
		
		try (MongoCursor<Document> cur = collection.find(mQuery).iterator()) {
			if (cur.hasNext()) {
				Document doc = cur.tryNext();
	 			monomer = converter.decodeMonomer(doc.toJson());
			}
		}
        mongoClient.close();
		return monomer;
	}

	@Override
	public LWMonomer insertOrUpdateMonomer(String polymerType, String symbol, LWMonomer monomer) throws Exception {
		MongoClient mongoClient = new MongoClient(LibraryManager.getHostname(),Integer.parseInt(LibraryManager.getPort()));
		MongoDatabase database = mongoClient.getDatabase(LibraryManager.getDatabase());
		MongoCollection<Document> collection = database.getCollection("monomers");
		JsonConverter converter = new JsonConverter();
		
		//check if symbol is already used
		if (monomerDetail(monomer.getPolymerType(), monomer.getSymbol()) == null) {
			LOG.info("Monomer with this symbol is already registered");
			System.out.println("Monomer with this symbol is already registered");
			insertMonomer(monomer);
			monomer = monomerDetail(polymerType,symbol);
		} else {
			Document newDoc = Document.parse(converter.encodeMonomer(monomer));
			BasicDBObject mQuery = new BasicDBObject();
			mQuery = mQuery.append("polymerType",monomer.getPolymerType());
			mQuery = mQuery.append("symbol",monomer.getSymbol());
			collection.replaceOne(mQuery, newDoc);
		}
		mongoClient.close();
		return monomer;
	}

	@Override
	public int insertMonomer(LWMonomer monomer) throws Exception {
		MongoClient mongoClient = new MongoClient(LibraryManager.getHostname(),Integer.parseInt(LibraryManager.getPort()));
		MongoDatabase database = mongoClient.getDatabase(LibraryManager.getDatabase());
		MongoCollection<Document> collection = database.getCollection("monomers");
		LWMonomer existingMonomer = null;
		JsonConverter converter = new JsonConverter();
		
		BasicDBObject mQuery = new BasicDBObject();
		mQuery = mQuery.append("polymerType",monomer.getPolymerType());
		mQuery = mQuery.append("smiles",monomer.getSmiles());
		try (MongoCursor<Document> cur = collection.find(mQuery).iterator()) {
			if (cur.hasNext()) {
				Document doc = cur.next();
				existingMonomer = converter.decodeMonomer(doc.toJson());
				LOG.info("Monomer with this structure is already registered with ID: " + existingMonomer.getSymbol());
				System.out.println("Monomer with this structure is already registered with ID: " + existingMonomer.getSymbol());
				mongoClient.close();
				return -1;
			}
		}
		
		//check if symbol is already used
		if (monomerDetail(monomer.getPolymerType(), monomer.getSymbol()) != null) {
			LOG.info("Monomer with this symbol is already registered");
			System.out.println("Monomer with this symbol is already registered");
			mongoClient.close();
			return -1;
		}
		
		//new id
		//if we have an empty database, we will start with 1
		int id = 0;
		try (MongoCursor<Document> cur2 = collection.find().sort(Sorts.descending("id")).limit(1).iterator()) {
			if (cur2.hasNext()) {
				Document doc = cur2.next();
				id = converter.decodeMonomer(doc.toJson()).getId() + 1;
			}
		}
		//set id in JSON
		monomer.setId(id);
		//insert new monomer
		Document newDoc = Document.parse(converter.encodeMonomer(monomer));
		collection.insertOne(newDoc);
		
		mongoClient.close();
		return id;
	}

	@Override
	public LWMonomer updateMonomer(String polymerType, String symbol, LWMonomer monomer) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getTotalCount() throws Exception {
		return totalCount;
	}

}
