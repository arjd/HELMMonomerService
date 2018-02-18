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

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class MongoDBTest {
	private LibraryManager MyLibrary = null;
	
	@BeforeClass
	  public void setUp() throws Exception {
	    // code that will be invoked when this test is instantiated
		MyLibrary = LibraryManager.getInstance();
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
		int i = MyLoaderLibrary.deleteMonomer("RNA", "5eU");
		assertEquals(i,0);
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
		IMonomerLibrary MyLoaderLibrary = MyLibrary.getMonomerLibrary();
		LWMonomer existingMonomer = MyLoaderLibrary.monomerDetail("RNA", "5A6");
		existingMonomer.setSmiles("ccccc");
		assertEquals(MyLoaderLibrary.insertMonomer(existingMonomer), -1);
		
		existingMonomer.setSmiles("[H:1]N([H:3])CCCCCCO[H:2]");
		existingMonomer.setSymbol("foo");
		assertEquals(MyLoaderLibrary.insertMonomer(existingMonomer), -1);
		
		existingMonomer.setSmiles("ccccc");
		assertEquals(MyLoaderLibrary.insertMonomer(existingMonomer) > 100, true);
	}
	
	@Test
	public void testInsertOrUpdateMonomer() throws Exception {
		IMonomerLibrary MyLoaderLibrary = MyLibrary.getMonomerLibrary();
		LWMonomer existingMonomer = MyLoaderLibrary.monomerDetail("RNA", "5A6");
		existingMonomer.setAuthor("John Doe");
		int id = MyLoaderLibrary.insertOrUpdateMonomer("RNA","5A6",existingMonomer);
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

		assertEquals(rule.getAuthor(),"John Doe");
	  }	
	
	@Test
	  public void testInsertRule() throws Exception {
		Rule rule = null;
		IRuleLibrary MyLoaderLibrary = MyLibrary.getRulesLibrary();
		rule = MyLoaderLibrary.showRule(2);
		rule.setName("New rule");
		MyLoaderLibrary.insertOrUpdateRule(rule);

		assertEquals(rule.getName(),"New rule");
	  }	
}
