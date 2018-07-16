package org.helm.monomerservice;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.helm.monomerservice.MonomerLibrarySQLite;

public class MonomerLibrarySQLiteTest {
  @Test
  public void testSymbolInDatabase() throws Exception {
	  MonomerLibrarySQLite MyLoaderLibrary = new MonomerLibrarySQLite();
		Assert.assertTrue(MyLoaderLibrary.symbolInDatabase("PEPTIDE","A"));
		Assert.assertFalse(MyLoaderLibrary.symbolInDatabase("PEPTIDE","A_XXX"));
		
  }
}
