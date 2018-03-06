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
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;
import org.helm.notation2.Attachment;
import org.helm.notation2.Chemistry;
import org.helm.notation2.tools.SMILES;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.DeleteResult;

import javassist.bytecode.Mnemonic;

public class MonomerLibraryMongoDB implements IMonomerLibrary {

	private static int totalCount = 0;

	private static final Logger LOG = LoggerFactory.getLogger(MonomerLibraryMongoDB.class);

	@Override
	public int deleteMonomer(String polymerType, String symbol) throws Exception {
		MongoClient mongoClient = new MongoClient(LibraryManager.getHostname(),
				Integer.parseInt(LibraryManager.getPort()));
		MongoDatabase database = mongoClient.getDatabase(LibraryManager.getDatabase());
		try {
			LOG.info("Opened database.");
			MongoCollection<Document> collection = database.getCollection("monomers");

			BasicDBObject mQuery = new BasicDBObject();
			mQuery = mQuery.append("polymerType", polymerType);
			mQuery = mQuery.append("symbol", symbol);

			
			
			DeleteResult r = collection.deleteOne(mQuery);
			mongoClient.close();
			return (int) r.getDeletedCount()-1;
		} catch (Exception e) {
			LOG.error("Deletion of monomers failed.");
			throw e;
		} finally {
			mongoClient.close();
			LOG.info("Closed database.");
		}

	}

	@Override
	public List<LWMonomer> showMonomerList(String polymerType, String monomerType, String filter,String filterField, int offset, int limit)
			throws Exception {
		LWMonomer monomer = null;
		ArrayList<LWMonomer> list = new ArrayList<LWMonomer>();

		JsonConverter converter = new JsonConverter();

		MongoClient mongoClient = new MongoClient(LibraryManager.getHostname(),
				Integer.parseInt(LibraryManager.getPort()));
		MongoDatabase database = mongoClient.getDatabase(LibraryManager.getDatabase());
		try {
			MongoCollection<Document> collection = database.getCollection("monomers");

			BasicDBObject mQuery = new BasicDBObject();

			// certain polymer type or all?
			if (!polymerType.equals("ALL")) {
				mQuery = mQuery.append("polymerType", polymerType);
			}

			// filter
			if (filter != null && !filter.isEmpty()) {
				Pattern j = Pattern.compile(filter, Pattern.CASE_INSENSITIVE);
				if (filterField != null && !filterField.isEmpty()) {
					mQuery = mQuery.append(filterField, j);
				} else {
					BasicDBObject clause1 = new BasicDBObject("symbol", j);  
					BasicDBObject clause2 = new BasicDBObject("name", j);   
					BasicDBList or = new BasicDBList();
					or.add(clause1);
					or.add(clause2);
					mQuery = mQuery.append("$or", or);
				}
			}

			// monomerType
			if (monomerType != null && !monomerType.isEmpty()) {
				mQuery = mQuery.append("monomerType", monomerType);
			}

			try (MongoCursor<Document> cur = collection.find(mQuery).sort(Sorts.ascending("symbol")).limit(limit)
					.skip(offset).iterator()) {
				while (cur.hasNext()) {
					Document doc = cur.next();
					// System.out.println(doc.toJson());
					monomer = converter.decodeMonomer(doc.toJson());
					list.add(monomer);
				}
			}
			// total record count
			totalCount = 0;
			try (MongoCursor<Document> cur2 = collection.find(mQuery).sort(Sorts.ascending("symbol")).iterator()) {
				while (cur2.hasNext()) {
					cur2.next();
					totalCount = totalCount + 1;
				}
			}
		} catch (Exception e) {
			LOG.error("Reading of monomers failed.");
			throw e;
		} finally {
			mongoClient.close();
			LOG.info("Closed database.");
		}
		return list;
	}

	@Override
	public List<LWMonomer> showAllMonomers() throws Exception {

		return showMonomerList("ALL", "", "", "", 0, 0);
	}

	@Override
	public LWMonomer monomerDetail(String polymerType, String symbol) throws Exception {
		MongoClient mongoClient = new MongoClient(LibraryManager.getHostname(),
				Integer.parseInt(LibraryManager.getPort()));
		MongoDatabase database = mongoClient.getDatabase(LibraryManager.getDatabase());
		try {
			MongoCollection<Document> collection = database.getCollection("monomers");
			LWMonomer monomer = null;
			JsonConverter converter = new JsonConverter();

			BasicDBObject mQuery = new BasicDBObject();
			mQuery = mQuery.append("polymerType", polymerType);
			mQuery = mQuery.append("symbol", symbol);

			try (MongoCursor<Document> cur = collection.find(mQuery).iterator()) {
				if (cur.hasNext()) {
					Document doc = cur.tryNext();
					monomer = converter.decodeMonomer(doc.toJson());
				}
			}
			return monomer;
		} catch (Exception e) {
			LOG.error("Getting monomer detail failed.");
			throw e;
		} finally {
			mongoClient.close();
			LOG.info("Closed database.");
		}
	}

	@Override
	public int insertOrUpdateMonomer(String polymerType, String symbol, LWMonomer monomer) throws Exception {
		MongoClient mongoClient = new MongoClient(LibraryManager.getHostname(),
				Integer.parseInt(LibraryManager.getPort()));
		MongoDatabase database = mongoClient.getDatabase(LibraryManager.getDatabase());
		try {
		MongoCollection<Document> collection = database.getCollection("monomers");
		JsonConverter converter = new JsonConverter();

		// check if symbol is already used
		if (monomerDetail(monomer.getPolymerType(), monomer.getSymbol()) == null) {
			LOG.info("Monomer with this symbol is already registered");
			System.out.println("Monomer with this symbol is already registered");
			int id = insertMonomer(monomer);
			monomer = monomerDetail(polymerType, symbol);
			return id;
		} else {
			//Document newDoc = Document.parse(converter.encodeMonomer(monomer));
			//BasicDBObject mQuery = new BasicDBObject();
			//mQuery = mQuery.append("polymerType", monomer.getPolymerType());
			//mQuery = mQuery.append("symbol", monomer.getSymbol());
			//collection.replaceOne(mQuery, newDoc);
			//return monomer.getId();
			return updateMonomer(polymerType, symbol, monomer);
		}
		} catch (Exception e) {
			LOG.error("Insert or update of monomer failed.");
			throw e;
		} finally {
			mongoClient.close();
			LOG.info("Closed database.");
		}
	}

	@Override
	public int insertMonomer(LWMonomer monomer) throws Exception {
		
		//check Monomer
				if(checkMonomer(monomer) != 0) {
					return checkMonomer(monomer);
				}		
		
		MongoClient mongoClient = new MongoClient(LibraryManager.getHostname(),
				Integer.parseInt(LibraryManager.getPort()));
		MongoDatabase database = mongoClient.getDatabase(LibraryManager.getDatabase());
		try {
		MongoCollection<Document> collection = database.getCollection("monomers");
		LWMonomer existingMonomer = null;
		JsonConverter converter = new JsonConverter();

		//Check if smiles is unique
		BasicDBObject mQuery = new BasicDBObject();
		mQuery = mQuery.append("smiles", monomer.getSmiles());
		try (MongoCursor<Document> cur = collection.find(mQuery).iterator()) {
			if (cur.hasNext()) {
				Document doc = cur.next();
				existingMonomer = converter.decodeMonomer(doc.toJson());
				LOG.info("Monomer with this structure is already registered with ID: " + existingMonomer.getSymbol());
				return -1000;
			}
		}

		// check if symbol is already used
		if (monomerDetail(monomer.getPolymerType(), monomer.getSymbol()) != null) {
			LOG.info("Monomer with this symbol is already registered");
			System.out.println("Monomer with this symbol is already registered");
			return -2000;
		}

		// new id
		// if we have an empty database, we will start with 1
		int id = 0;
		try (MongoCursor<Document> cur2 = collection.find().sort(Sorts.descending("id")).limit(1).iterator()) {
			if (cur2.hasNext()) {
				Document doc = cur2.next();
				id = converter.decodeMonomer(doc.toJson()).getId() + 1;
			}
		}
		// set id in JSON
		monomer.setId(id);
		
		// insert new monomer
		Document newDoc = Document.parse(converter.encodeMonomer(monomer));
		collection.insertOne(newDoc);

		return id;
		} catch (Exception e) {
			LOG.error("Deletion of monomers failed.");
			throw e;
		} finally {
			mongoClient.close();
			LOG.info("Closed database.");
		}
	}

	@Override
	public int updateMonomer(String polymerType, String symbol, LWMonomer monomer) throws Exception {
		
		//check Monomer
		if(checkMonomer(monomer) != 0) {
			return checkMonomer(monomer);
		}
		
		// return -3000 if monomer is not already registered
		MongoClient mongoClient = new MongoClient(LibraryManager.getHostname(),
				Integer.parseInt(LibraryManager.getPort()));
		MongoDatabase database = mongoClient.getDatabase(LibraryManager.getDatabase());
		try {
		MongoCollection<Document> collection = database.getCollection("monomers");

		BasicDBObject mQuery = new BasicDBObject();
		mQuery = mQuery.append("polymerType", polymerType);
		mQuery = mQuery.append("symbol", symbol);
		try (MongoCursor<Document> cur = collection.find(mQuery).iterator()) {
			if (!cur.hasNext()) {
				return -3000;
			}
		}
		
		JsonConverter converter = new JsonConverter();
		
		//Ceck if Smiles is unique
		mQuery = new BasicDBObject();
		mQuery = mQuery.append("smiles", monomer.getSmiles());
		try (MongoCursor<Document> cur = collection.find(mQuery).iterator()) {
			while(cur.hasNext()) {
				Document doc = cur.tryNext();
				LWMonomer monomerDB = converter.decodeMonomer(doc.toJson());
				if(!monomerDB.getSymbol().equals(symbol)) {
					LOG.info(monomerDB.getSymbol());
					LOG.info(symbol);
					LOG.info("Smiles already exists with ID: " + monomerDB.getId());			
					return -1000;
				}
			}
		}
		
		Document newDoc = Document.parse(converter.encodeMonomer(monomer));
		mQuery = new BasicDBObject();
		mQuery = mQuery.append("polymerType", monomer.getPolymerType());
		mQuery = mQuery.append("symbol", monomer.getSymbol());
		collection.replaceOne(mQuery, newDoc);
		return monomer.getId();
		
		
		
		//return insertOrUpdateMonomer(polymerType, symbol, monomer);
		} catch (Exception e) {
			LOG.error("Deletion of monomers failed.");
			throw e;
		} finally {
			mongoClient.close();
			LOG.info("Closed database.");
		}
	}
	
private int checkMonomer(LWMonomer monomer) {
		
		//chekc if Molfile exists
		if(monomer.getMolfile().isEmpty() || monomer.getMolfile() == null) {
			LOG.info("Monomer has no Molfile");
			return -5100;
		}
		//check if SMILES exists
		/*if(monomer.getSmiles().isEmpty()) {
			LOG.info("Monomer has no SMILES");
			try {
				 String smiles = SMILES.convertMolToSMILESWithAtomMapping(monomer.getMolfile(), monomer.getRgroups());
				 monomer.setSmiles(smiles);
				 LOG.info("Generate SMILES form Molfile");
			} catch (Exception e) {
				LOG.info("SMILES could not be generated from Molfile");
				return -5200;
			}
		}*/
		
		//generate Smiles from Molfile
		try {
			String smiles = SMILES.convertMolToSMILESWithAtomMapping(monomer.getMolfile(), monomer.getRgroups());
			//smiles = Chemistry.getInstance().getManipulator().convertExtendedSmiles(smiles);
			smiles = smiles.replaceAll("\\s+", "");
			monomer.setSmiles(smiles);
			LOG.info("Generate SMILES form Molfile: " + smiles);
		} catch (Exception e) {
			LOG.info("SMILES could not be generated from Molfile");
			return -5200;
		}
		
		//check R-group
		if(!checkRGroup(monomer.getSmiles(), monomer.getRgroups())) {
			LOG.info("Monomer has wrong R-Groups");
			return -5300;
		}
		
		//check if Molfile and SMILES match
		/*try {
			String smilesMolfile = SMILES.convertMolToSMILESWithAtomMapping(monomer.getMolfile(), monomer.getRgroups());
			String smilesMolfileUnique = Chemistry.getInstance().getManipulator().canonicalize(smilesMolfile);
			String smilesUnique = Chemistry.getInstance().getManipulator().canonicalize(monomer.getSmiles());
			if(!smilesMolfileUnique.equals(smilesUnique)) {
				LOG.info("Smiles and Molfile do not equal");
				LOG.info(SMILES.convertMolToSMILESWithAtomMapping(monomer.getMolfile(), monomer.getRgroups()));
				LOG.info(monomer.getSmiles());
				return -5400;
			}
		} catch (Exception e) {
			LOG.info("SMILES could not be generated from Molfile");
			return -5200;
		}*/
		
		if(monomer.getName().isEmpty() || monomer.getName() == null) {
			LOG.info("Monomer has no Name");
			return -6000;
		}
		if(monomer.getPolymerType().isEmpty() || monomer.getPolymerType() == null) {
			LOG.info("Monomer has no Polymertype");
			return -6100;
		}
		if(monomer.getSymbol().isEmpty() || monomer.getSymbol() == null) {
			LOG.info("Monomer has no Symbol");
			return -6200;
		}
		if(monomer.getMonomerType().isEmpty() || monomer.getMonomerType() == null) {
			LOG.info("Monomer has no Monomertype");
			return -6300;
		}
		if(monomer.getNaturalAnalog().isEmpty() || monomer.getNaturalAnalog() == null) {
			LOG.info("Monomer has no Natural Analog");
			return -6400;
		}
		if(monomer.getCreateDate().isEmpty() || monomer.getCreateDate() == null) {
			Date date = new Date();
			monomer.setCreateDate(date.toString());
			LOG.info("Monomer has no CreateDate: current date is used");
		}
		if(monomer.getAuthor().isEmpty() || monomer.getAuthor() == null) {
			LOG.info("Monomer has no Author: Author is set to 'unknownAuthor'");
			monomer.setAuthor("unknownAuthor");
		}
		return 0;
	}
	
	private boolean checkRGroup(String smiles, List<Attachment> attachment) {
		int numberGivenAttachment = attachment.size();
		int numberAttachmentInSmiles = 0;
		
		Pattern pattern = Pattern.compile("\\[\\*:([1-9]\\d*)\\]|\\[\\w+:([1-9]\\d*)\\]");
		Matcher matcher = pattern.matcher(smiles);
		
		while(matcher.find()) {
			numberAttachmentInSmiles++;
		}
		
		if(numberAttachmentInSmiles == numberGivenAttachment)
			return true;
		else
			return false;
	}
	

	@Override
	public int getTotalCount() throws Exception {
		return totalCount;
	}

}
