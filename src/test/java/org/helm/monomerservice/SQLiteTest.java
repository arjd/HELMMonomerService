/*******************************************************************************
 * Copyright C 2012, The Pistoia Alliance
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

import java.util.List;

import org.helm.monomerservice.IMonomerLibrary;
import org.helm.monomerservice.LWMonomer;
import org.helm.monomerservice.LibraryManager;
import org.helm.monomerservice.JsonConverter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SQLiteTest {
 private LibraryManager MyLibrary = null;
  
@BeforeClass
  public void setUp() throws Exception {
    // code that will be invoked when this test is instantiated
	MyLibrary = LibraryManager.getInstance();
  }  

@Test
  public void testShowAllMonomersByType() throws Exception {
	  List<LWMonomer> monomerList;
	  	  
		  IMonomerLibrary MyLoaderLibrary = MyLibrary.getMonomerLibrary();
		  monomerList = MyLoaderLibrary.showAllMonomers();
		  System.out.println(monomerList.size());
		

  }

@Test
  public void testDeleteMonomer() throws Exception {
	IMonomerLibrary MyLoaderLibrary = MyLibrary.getMonomerLibrary();
	assertEquals(MyLoaderLibrary.deleteMonomer("RNA", "LR"), 168);
	}

@Test
  public void testInsertMonomer() throws Exception {
	LWMonomer monomer = new LWMonomer();
	monomer.setSymbol("Foo");
	monomer.setMonomerType("Backbone");
	monomer.setName("Bar");
	monomer.setNaturalAnalog("X");
	monomer.setMolfile("xxx");
	monomer.setPolymerType("PEPTIDE");
	monomer.setSmiles("cccccc");
	IMonomerLibrary MyLoaderLibrary = MyLibrary.getMonomerLibrary();
	
	MyLoaderLibrary.deleteMonomer("PEPTIDE", "Foo");
	
	assertNotEquals(MyLoaderLibrary.insertMonomer(monomer),-1);
	assertEquals(MyLoaderLibrary.insertMonomer(monomer),-1);
	monomer.setSmiles("c");
	assertEquals(MyLoaderLibrary.insertMonomer(monomer),-1);
	}

@Test
	public void testInsertRule() throws Exception {
	Rule rule = new Rule();
	rule.setId(4);
	rule.setCategory("Test");
	rule.setName("Foo");
	IRuleLibrary MyLoaderLibrary = MyLibrary.getRulesLibrary();
	MyLoaderLibrary.insertOrUpdateRule(rule);
}

@Test
public void testUpdateRule() throws Exception {
	Rule rule = new Rule();
	
	IRuleLibrary MyLoaderLibrary = MyLibrary.getRulesLibrary();
	rule = MyLoaderLibrary.showRule(1);
	rule.setDescription("SPAM2");
	MyLoaderLibrary.insertOrUpdateRule(rule);
	
}

@Test
  public void testGetMonomer() throws Exception {
  IMonomerLibrary MyLoaderLibrary = MyLibrary.getMonomerLibrary();
  JsonConverter converter = new JsonConverter();
  
  LWMonomer monomer = MyLoaderLibrary.monomerDetail("PEPTIDE", "A");
  System.out.println(converter.encodeMonomer(monomer));  
}

@Test
public void testUpdateMonomer() throws Exception {
	IMonomerLibrary MyLoaderLibrary = MyLibrary.getMonomerLibrary();
	JsonConverter converter = new JsonConverter();
	
	LWMonomer monomer = MyLoaderLibrary.monomerDetail("RNA", "3A6");
	monomer.setName("6-amino-hexanol (42 end)");
	MyLoaderLibrary.insertOrUpdateMonomer("RNA", "3A6", monomer);
	System.out.println(converter.encodeMonomer(monomer));  
}


@Test
public void insertOrUpdateMonomer() throws Exception {
	IMonomerLibrary MyLoaderLibrary = MyLibrary.getMonomerLibrary();
	JsonConverter converter = new JsonConverter();
	
	LWMonomer monomer = MyLoaderLibrary.monomerDetail("CHEM", "A6OH");
	monomer.setSymbol("test1");
	monomer.setSmiles("[H:1]OCCCN[H:2]");
	monomer.setMolfile("   ");
	
	MyLoaderLibrary.insertOrUpdateMonomer("CHEM", "test1", monomer);
	System.out.println(converter.encodeMonomer(monomer));  
}


@Test
  public void testFilterMonomers() throws Exception {
	List<LWMonomer> monomerList;
	IMonomerLibrary MyLoaderLibrary = MyLibrary.getMonomerLibrary();
	monomerList = MyLoaderLibrary.showMonomerList("CHEM","Undefined", "A", 0, 10);
	System.out.println(monomerList.size());
}
  
}

