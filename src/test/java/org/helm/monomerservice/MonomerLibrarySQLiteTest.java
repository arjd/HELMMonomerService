package org.helm.monomerservice;

import org.helm.monomerservice.MonomerLibrarySQLite;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MonomerLibrarySQLiteTest {
    @Test
    public void testSymbolInDatabase() throws Exception {
        MonomerLibrarySQLite MyLoaderLibrary = new MonomerLibrarySQLite();
        Assert.assertTrue(MyLoaderLibrary.symbolInDatabase("PEPTIDE", "A"));
        Assert.assertFalse(MyLoaderLibrary.symbolInDatabase("PEPTIDE", "A_XXX"));
    }
}
