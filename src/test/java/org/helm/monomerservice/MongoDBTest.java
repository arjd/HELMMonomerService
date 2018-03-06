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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.helm.notation2.Attachment;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class MongoDBTest {
	private LibraryManager MyLibrary = null;
	
	@BeforeClass
	  public void setUp() throws Exception {
	    // code that will be invoked when this test is instantiated
		MyLibrary = LibraryManager.getInstance();
		
		File file = new File(System.getProperty("user.dir") + "/src/test/resources/org/helm/monomerservice/resources/configMongoDB.txt");
		File file2 = new File(System.getProperty("user.dir") + "/src/test/resources/org/helm/monomerservice/resources/config.txt");
				
		Files.copy(file.toPath(),file2.toPath(),StandardCopyOption.REPLACE_EXISTING);
	  }  
	
	@Test
	  public void testShowAllMonomers() throws Exception {
		  List<LWMonomer> monomerList;
		  	  
			  IMonomerLibrary MyLoaderLibrary = MyLibrary.getMonomerLibrary();
			  monomerList = MyLoaderLibrary.showAllMonomers();
			  System.out.println(monomerList.size());
			  assertEquals((monomerList.size() > 100), true);
	  }	

	@Test
	  public void testShowAllMonomersByType() throws Exception {
		  List<LWMonomer> monomerList;
		  	  
			  IMonomerLibrary MyLoaderLibrary = MyLibrary.getMonomerLibrary();
			  monomerList = MyLoaderLibrary.showMonomerList("RNA","Backbone", "6-amino", "name", 0, 0);
			  System.out.println(monomerList.size());
			  assertEquals((monomerList.size() > 1), true);
	  }
	
	@Test
	public void testDeleteMonomer() throws Exception {
		IMonomerLibrary MyLoaderLibrary = MyLibrary.getMonomerLibrary();
		int i = MyLoaderLibrary.deleteMonomer("RNA", "25R");
		//assertEquals(i,132);
	}

	@Test
	public void testMonomerDetail() throws Exception {
		IMonomerLibrary MyLoaderLibrary = MyLibrary.getMonomerLibrary();
		JsonConverter converter = new JsonConverter();
		
		LWMonomer monomer = MyLoaderLibrary.monomerDetail("RNA", "5A6");
		System.out.println(converter.encodeMonomer(monomer));
		
		assertEquals(monomer.getSymbol(),"5A6");
	}
	
	@Test
	public void testInsertMonomer() throws Exception {
		/*
		IMonomerLibrary MyLoaderLibrary = MyLibrary.getMonomerLibrary();
		LWMonomer existingMonomer = MyLoaderLibrary.monomerDetail("RNA", "5A6");
		existingMonomer.setSmiles("ccccc");
		assertEquals(MyLoaderLibrary.insertMonomer(existingMonomer), -1);
		
		existingMonomer.setSmiles("[H:1]N([H:3])CCCCCCO[H:2]");
		existingMonomer.setSymbol("foo");
		assertEquals(MyLoaderLibrary.insertMonomer(existingMonomer), -1);
		
		existingMonomer.setSmiles("ccccc");
		assertEquals(MyLoaderLibrary.insertMonomer(existingMonomer) > 100, true);
		*/
		
		
		
		LWMonomer monomer = new LWMonomer();
		IMonomerLibrary MyLoaderLibrary = MyLibrary.getMonomerLibrary();
		
		MyLoaderLibrary.deleteMonomer("PEPTIDE", "Foo");
		MyLoaderLibrary.deleteMonomer("PEPTIDE", "ac");

		
		LWMonomer m = new LWMonomer();
		assertEquals(MyLoaderLibrary.insertMonomer(monomer),-5100);
		monomer.setMolfile("xxxx");
		assertEquals(MyLoaderLibrary.insertMonomer(monomer),-5200);
		monomer.setMolfile("\n" + 
				"  Marvin  06151012162D          \n" + 
				"\n" + 
				"  4  3  0  0  0  0            999 V2000\n" + 
				"   -6.8962    2.3258    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   -6.4837    1.6114    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   -6.8962    0.8969    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   -5.6587    1.6114    0.0000 R#  0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"  1  2  1  0  0  0  0\n" + 
				"  2  3  2  0  0  0  0\n" + 
				"  2  4  1  0  0  0  0\n" + 
				"M  RGP  1   4   2\n" + 
				"M  END\n" + 
				"\n" + 
				"$$$$");
		
		monomer.setSmiles("CC[OH:1]CC([OH:2])=O");
		assertEquals(MyLoaderLibrary.insertMonomer(monomer),-5300);
		Attachment attachment = new Attachment("R2", "OH");
		attachment.setAlternateId("R2-OH");
		attachment.setCapGroupSMILES("O[*:2]");
		monomer.addAttachment(attachment);
		monomer.setSmiles("CC[OH:1]CC");
		//assertEquals(MyLoaderLibrary.insertMonomer(monomer),-5400);
		monomer.setSmiles("CC([OH:2])=O");
		assertEquals(MyLoaderLibrary.insertMonomer(monomer),-6000);
		monomer.setName("Bar");
		assertEquals(MyLoaderLibrary.insertMonomer(monomer),-6100);
		monomer.setPolymerType("PEPTIDE");
		assertEquals(MyLoaderLibrary.insertMonomer(monomer),-6200);
		monomer.setSymbol("Foo");
		assertEquals(MyLoaderLibrary.insertMonomer(monomer),-6300);
		monomer.setMonomerType("Undefinded");
		assertEquals(MyLoaderLibrary.insertMonomer(monomer),-6400);
		monomer.setNaturalAnalog("X");
		
		MyLoaderLibrary.deleteMonomer("PEPTIDE", "Foo");
		
		assertNotEquals(MyLoaderLibrary.insertMonomer(monomer), -1000);
		assertEquals(MyLoaderLibrary.insertMonomer(monomer), -1000);
		
		
	}
	
	@Test
	public void testInsertOrUpdateMonomer() throws Exception {
		IMonomerLibrary MyLoaderLibrary = MyLibrary.getMonomerLibrary();
		LWMonomer existingMonomer = MyLoaderLibrary.monomerDetail("RNA", "5A6");
		existingMonomer.setAuthor("John Doe");
		int id = MyLoaderLibrary.insertOrUpdateMonomer("RNA","5A6",existingMonomer);
		System.err.println(id);
		assertEquals((id > 0), true);
	}
	
	
	@Test
	public void testUpdateMonomer() throws Exception {
		IMonomerLibrary MyLoaderLibrary = MyLibrary.getMonomerLibrary();
		LWMonomer existingMonomer = MyLoaderLibrary.monomerDetail("RNA", "5A6");
		int id = MyLoaderLibrary.updateMonomer("RNA","12345",existingMonomer);
		assertEquals(id < 0, true);
		
		existingMonomer = MyLoaderLibrary.monomerDetail("RNA", "5A6");
		existingMonomer.setAuthor("Max");
		
		id = MyLoaderLibrary.updateMonomer("RNA","5A6",existingMonomer);
		assertEquals((id > 0), true);
	}
	
	@Test
	public void testUniqueSmiles() throws Exception {
		IMonomerLibrary MyLoaderLibrary = MyLibrary.getMonomerLibrary();
		LWMonomer existingMonomer = MyLoaderLibrary.monomerDetail("RNA", "5A6");
		existingMonomer.setSymbol("6A6");
		assertEquals(MyLoaderLibrary.insertMonomer(existingMonomer), -1000);
		assertEquals(MyLoaderLibrary.updateMonomer("RNA", "MOE", existingMonomer), -1000);
		assertEquals(MyLoaderLibrary.updateMonomer("RNA", "5A6", existingMonomer), 137);
	}
	
	//you can run this test only once, because rule with ID = 4 is deleted and won't be found
	@Test
	public void testDeleteRule() throws Exception {
		IRuleLibrary MyLoaderLibrary = MyLibrary.getRulesLibrary();
		int id = MyLoaderLibrary.deleteRule(3);
		assertEquals(id, 3);
	}
	
	@Test void testErrorCodesRules() throws Exception{
		Rule rule = new Rule();
		IRuleLibrary MyLoaderLibrary = MyLibrary.getRulesLibrary();
		
		rule.setScript(null);
		assertEquals(MyLoaderLibrary.insertOrUpdateRule(rule), -1000);
		rule.setScript("here is a Script");
		rule.setCategory("");
		assertEquals(MyLoaderLibrary.insertOrUpdateRule(rule), -2000);
		rule.setCategory("TestIt");
		rule.setId(null);
		assertEquals(MyLoaderLibrary.insertOrUpdateRule(rule), -3000);
	}
	
	@Test
	  public void testShowAllRules() throws Exception {
		ArrayList<Rule> list = new ArrayList<Rule>();
		JsonConverter converter = new JsonConverter();
		
		IRuleLibrary MyLoaderLibrary = MyLibrary.getRulesLibrary();
		list = MyLoaderLibrary.showAllRules();
		for (int i = 0;i<list.size();i++) {
			System.out.println(converter.encodeRule(list.get(i)));
		}
		assertEquals((list.size() > 0), true);
	  }	
	
	@Test
	  public void testShowRule() throws Exception {
		Rule rule = null;
		IRuleLibrary MyLoaderLibrary = MyLibrary.getRulesLibrary();
		rule = MyLoaderLibrary.showRule(2);

		assertEquals(rule.getName(), "Replace base A with G");
	  }	
	
	@Test
	  public void testUpdateRule() throws Exception {
		Rule rule = null;
		IRuleLibrary MyLoaderLibrary = MyLibrary.getRulesLibrary();
		rule = MyLoaderLibrary.showRule(2);
		rule.setAuthor("John Doe");
		MyLoaderLibrary.insertOrUpdateRule(rule);
		assertEquals(MyLoaderLibrary.insertOrUpdateRule(rule), 0);
		rule.setName("Replace base A with U");
		assertEquals(MyLoaderLibrary.insertOrUpdateRule(rule), -1);
	  }	
	
	@Test
	  public void testInsertRule() throws Exception {
		Rule rule = null;
		IRuleLibrary MyLoaderLibrary = MyLibrary.getRulesLibrary();
		rule = MyLoaderLibrary.showRule(2);
		rule.setName("New rule");
		rule.setId(5);
		assertEquals(MyLoaderLibrary.insertOrUpdateRule(rule), 0);
		rule.setName("Replace base A with U");
		assertEquals(MyLoaderLibrary.insertOrUpdateRule(rule), -1);
	  }	
}
