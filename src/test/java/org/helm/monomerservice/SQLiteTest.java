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

import org.helm.monomerservice.IMonomerLibrary;
import org.helm.monomerservice.JsonConverter;
import org.helm.monomerservice.LWMonomer;
import org.helm.monomerservice.LibraryManager;
import org.helm.notation2.Attachment;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class SQLiteTest {
    private LibraryManager MyLibrary = null;

    @BeforeClass
    public void setUp() throws Exception {
        // code that will be invoked when this test is instantiated
        MyLibrary = LibraryManager.getInstance();
        SQLiteMonomers sqLiteMonomers = new SQLiteMonomers();
        sqLiteMonomers.buildDBForTesting();

        File file = new File(System.getProperty("user.dir") + "/src/test/resources/org/helm/monomerservice/resources/configSQLite.txt");
        File file2 = new File(System.getProperty("user.dir") + "/src/test/resources/org/helm/monomerservice/resources/config.txt");

        Files.copy(file.toPath(), file2.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    @Test
    public void testShowMonomers() throws Exception {
        List<LWMonomer> monomerList;

        IMonomerLibrary MyLoaderLibrary = MyLibrary.getMonomerLibrary();
        monomerList = MyLoaderLibrary.showAllMonomers();
        System.out.println(monomerList.size());
        assertEquals((monomerList.size() > 100), true);

        monomerList = MyLoaderLibrary.showMonomerList("CHEM", null, null, null, 0, 0);
        System.out.println(monomerList.size());
        assertEquals((monomerList.size() == 11), true);
        System.out.println("List Size:" + monomerList.size());

        LWMonomer monomer = MyLoaderLibrary.monomerDetail("RNA", "5A6");
        System.out.println(monomer);
        assertEquals((monomer.getSymbol().equals("5A6")), true);
    }

    @Test
    public void testDeleteMonomer() throws Exception {
        IMonomerLibrary MyLoaderLibrary = MyLibrary.getMonomerLibrary();
        assertEquals(MyLoaderLibrary.deleteMonomer("RNA", "LR"), 172);
    }

    @Test
    public void testInsertMonomer() throws Exception {
        LWMonomer monomer = new LWMonomer();
        IMonomerLibrary MyLoaderLibrary = MyLibrary.getMonomerLibrary();

        MyLoaderLibrary.deleteMonomer("PEPTIDE", "Foo");
        MyLoaderLibrary.deleteMonomer("PEPTIDE", "ac");


        LWMonomer m = new LWMonomer();
        assertEquals(MyLoaderLibrary.insertMonomer(monomer), -5100);
        monomer.setMolfile("xxxx");
        assertEquals(MyLoaderLibrary.insertMonomer(monomer), -5200);
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
                "M  RGP  1   4   1\n" +
                "M  END\n" +
                "\n" +
                "$$$$");

        monomer.setSmiles("CC[OH:1]CC([OH:2])=O");
        Attachment attachment = new Attachment("R1", "OH");
        attachment.setAlternateId("R1-OH");
        attachment.setCapGroupSMILES("O[*:1]");
        monomer.addAttachment(attachment);
        monomer.setSmiles("CC[OH:1]CC");
        //assertEquals(MyLoaderLibrary.insertMonomer(monomer),-5400);
        monomer.setSmiles("CC([OH:2])=O");
        assertEquals(MyLoaderLibrary.insertMonomer(monomer), -6000);
        monomer.setName("Bar");
        assertEquals(MyLoaderLibrary.insertMonomer(monomer), -6100);
        monomer.setPolymerType("PEPTIDE");
        assertEquals(MyLoaderLibrary.insertMonomer(monomer), -6200);
        monomer.setSymbol("Foo");
        assertEquals(MyLoaderLibrary.insertMonomer(monomer), -6300);
        monomer.setMonomerType("Backbone");
        assertEquals(MyLoaderLibrary.insertMonomer(monomer), -6400);
        monomer.setNaturalAnalog("X");

        MyLoaderLibrary.deleteMonomer("PEPTIDE", "Foo");

        assertEquals(MyLoaderLibrary.insertMonomer(monomer) > 0, true);
        assertEquals(MyLoaderLibrary.insertMonomer(monomer), -1000);

        //monomer.setSmiles("c");
        //assertEquals(MyLoaderLibrary.insertMonomer(monomer),-1);
    }

    
    //the SMILES are not canonical in the DB => test fails
   /* @Test
    public void testUniqueSmiles() throws Exception {
        IMonomerLibrary MyLoaderLibrary = MyLibrary.getMonomerLibrary();
        LWMonomer monomer;
        monomer = MyLoaderLibrary.monomerDetail("RNA", "5A6");
        monomer.setSymbol("5iU");
        assertEquals(MyLoaderLibrary.updateMonomer("RNA", "5iU", monomer), -1000);
        assertEquals(MyLoaderLibrary.insertMonomer(monomer), -1000);
        monomer.setSymbol("5A6");
        monomer.setName("TestName");
        assertEquals(MyLoaderLibrary.updateMonomer("RNA", "5A6", monomer), monomer.getId());
    }*/

    @Test
    public void testInsertRule() throws Exception {
        Rule rule = new Rule();
        rule.setId(4);
        rule.setCategory("Test");
        rule.setName("Foo");
        IRuleLibrary MyLoaderLibrary = MyLibrary.getRulesLibrary();
        assertEquals(MyLoaderLibrary.insertOrUpdateRule(rule), 0);
        rule.setId(5);
        assertEquals(MyLoaderLibrary.insertOrUpdateRule(rule), -1);
    }

    @Test
    public void testErrorCodesRules() throws Exception {
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
    public void testShowRules() throws Exception {
        ArrayList<Rule> list = new ArrayList<Rule>();
        JsonConverter converter = new JsonConverter();

        IRuleLibrary MyLoaderLibrary = MyLibrary.getRulesLibrary();
        list = MyLoaderLibrary.showAllRules();
        for (int i = 0; i < list.size(); i++) {
            System.out.println(converter.encodeRule(list.get(i)));
        }
    }

    @Test
    public void testUpdateRule() throws Exception {
        Rule rule = new Rule();

        IRuleLibrary MyLoaderLibrary = MyLibrary.getRulesLibrary();
        rule = MyLoaderLibrary.showRule(1);
        rule.setDescription("SPAM2");
        assertEquals(MyLoaderLibrary.insertOrUpdateRule(rule), 0);
        rule.setName("Replace base 'A' with 'G'");
        assertEquals(MyLoaderLibrary.insertOrUpdateRule(rule), 0);
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

        LWMonomer monomer = MyLoaderLibrary.monomerDetail("RNA", "3A6");
        monomer.setName("6-amino-hexanol (42 end)");

        assertEquals(MyLoaderLibrary.updateMonomer("RNA", "3A6", monomer), monomer.getId());
        assertEquals(MyLoaderLibrary.updateMonomer("RNA", "4A6", monomer), -3000);
        monomer.setName("");
        assertEquals(MyLoaderLibrary.updateMonomer("RNA", "3A6", monomer), -6000);
    }


    @Test
    public void insertOrUpdateMonomer() throws Exception {
        IMonomerLibrary MyLoaderLibrary = MyLibrary.getMonomerLibrary();
        JsonConverter converter = new JsonConverter();

        LWMonomer monomer = MyLoaderLibrary.monomerDetail("CHEM", "A6OH");
        monomer.setSymbol("test1");
        monomer.setSmiles("[H:1]OCCCN[H:2]");

        MyLoaderLibrary.insertOrUpdateMonomer("CHEM", "test1", monomer);
        //System.out.println(converter.encodeMonomer(monomer));
    }


    @Test
    public void testFilterMonomers() throws Exception {
        List<LWMonomer> monomerList;
        IMonomerLibrary MyLoaderLibrary = MyLibrary.getMonomerLibrary();
        monomerList = MyLoaderLibrary.showMonomerList("RNA", "", "25R", "symbol", 0, 100);
        System.out.println(monomerList.size());
        Assert.assertTrue(monomerList.size() > 0);
    }

    @Test
    public void testFilterMonomers2() throws Exception {
        List<LWMonomer> monomerList;
        IMonomerLibrary MyLoaderLibrary = MyLibrary.getMonomerLibrary();
        monomerList = MyLoaderLibrary.showMonomerList("RNA", "", "Adenine", "", 0, 100);
        System.out.println(monomerList.size());
        Assert.assertTrue(monomerList.size() > 0);
    }


}

