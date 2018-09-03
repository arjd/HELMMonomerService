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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.helm.notation2.Attachment;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class RestMonomerTest extends StandaloneServer {

    @BeforeClass
    public void setUp() throws Exception {
        // code that will be invoked when this test is instantiated
        SQLiteMonomers sqLiteMonomers = new SQLiteMonomers();
        sqLiteMonomers.buildDBForTesting();
        
        //if a DB should be tested:
        //sqLiteMonomers.setNewSmilesAndValidateMonomers();

        File file = new File(System.getProperty("user.dir") + "/src/test/resources/org/helm/monomerservice/resources/configSQLite.txt");
        File file2 = new File(System.getProperty("user.dir") + "/src/test/resources/org/helm/monomerservice/resources/config.txt");

        Files.copy(file.toPath(), file2.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    @Test
    public void testDeleteMonomer() {
        Client client = createClient();

        UriBuilder builder = UriBuilder.fromUri(BASE_URI);
        builder.path("monomer").path("RNA").path("5A6");
        URI uri = builder.build();
        Response response = client.target(uri).request().delete();
        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), 200);
    }

    @Test
    public void testDeleteNonExistingMonomer() {
        Client client = createClient();

        UriBuilder builder = UriBuilder.fromUri(BASE_URI);
        builder.path("monomer").path("PEPTIDE").path("Non-Exist");
        URI uri = builder.build();
        Response response = client.target(uri).request().delete();
        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), 404);

    }

    @Test
    public void testShowAllMonomers() {
        Client client = createClient();
        UriBuilder builder = UriBuilder.fromUri(BASE_URI);
        builder.path("monomer").path("ALL");
        URI uri = builder.build();
        Response response = client.target(uri).request().get();
        String retMonomerList = response.readEntity(String.class);
        System.out.println(retMonomerList);
        Assert.assertEquals(response.getStatus(), 200);
    }

    @Test
    public void testMonomerDetail() {
        Client client = createClient();
        UriBuilder builder = UriBuilder.fromUri(BASE_URI);
        builder.path("monomer").path("CHEM").path("Az");
        URI uri = builder.build();
        Response response = client.target(uri).request().get();
        String retMonomer = response.readEntity(String.class);
        System.out.println(retMonomer);
        Assert.assertTrue(retMonomer.contains("CHEM") && retMonomer.contains("Az"));
    }


    @Test
    public void testShowAllMonomersByType() {
        Client client = createClient();
        UriBuilder builder = UriBuilder.fromUri(BASE_URI);
        builder.path("monomer").path("CHEM");
        URI uri = builder.build();
        Response response = client.target(uri).request().get();
        String retMonomerList = response.readEntity(String.class);
        System.out.println(retMonomerList);
        Assert.assertTrue(!retMonomerList.contains("Adenine") && retMonomerList.contains("SMCC"));
    }

    @Test
    public void testShowAllMonomersByMonomerType() {
        Client client = createClient();
        UriBuilder builder = UriBuilder.fromUri(BASE_URI);
        builder.path("monomer").path("RNA");
        builder.queryParam("monomertype", "Branch");
        URI uri = builder.build();
        Response response = client.target(uri).request().get();
        String retMonomerList = response.readEntity(String.class);
        System.out.println(retMonomerList);
        Assert.assertTrue(retMonomerList.contains("Adenine") && !retMonomerList.contains("25R"));
    }

    @Test
    public void testShowFilteredMonomers() {
        Client client = createClient();
        UriBuilder builder = UriBuilder.fromUri(BASE_URI);
        builder.path("monomer").path("RNA");
        builder.queryParam("filter", "Adenine");
        builder.queryParam("filterField", "name");
        URI uri = builder.build();
        Response response = client.target(uri).request().get();
        String retMonomerList = response.readEntity(String.class);
        System.out.println(retMonomerList);
        Assert.assertTrue(retMonomerList.contains("daA") && !retMonomerList.contains("25R"));
    }

    @Test
    public void testShowFilteredMonomersWithType() {
        Client client = createClient();
        UriBuilder builder = UriBuilder.fromUri(BASE_URI);
        builder.path("monomer").path("RNA");
        builder.queryParam("filter", "25r");
        builder.queryParam("filterField", "symbol");
        URI uri = builder.build();
        Response response = client.target(uri).request().get();
        String retMonomerList = response.readEntity(String.class);
        System.out.println(retMonomerList);
        Assert.assertTrue(retMonomerList.contains("25R"));
    }

    @Test
    public void testMonomerPagination() {
        Client client = createClient();
        UriBuilder builder = UriBuilder.fromUri(BASE_URI);
        builder.path("monomer").path("ALL");
        builder.queryParam("limit", "3");
        builder.queryParam("offset", "3");
        URI uri = builder.build();
        Response response = client.target(uri).request().get();
        String retMonomerList = response.readEntity(String.class);
        System.out.println(retMonomerList);
        Assert.assertEquals(response.getStatus(), 200);

    }

    //can throw an error if no new database is used, because the monomer "MCC" has a new name and could not be found in the database
    @Test
    public void testUpdateMonomer() {
        Client client = createClient();
        UriBuilder builder = UriBuilder.fromUri(BASE_URI);
        builder.path("monomer").path("PEPTIDE").path("Aze");
        URI uri = builder.build();
        Response response = client.target(uri).request().get();
        String retMonomer = response.readEntity(String.class);
        retMonomer = retMonomer.replace("2-carboxyazetidine", "2-carboxyazetidine-Test");

        response = client.target(uri).request().put(Entity.entity(retMonomer, MediaType.APPLICATION_JSON), Response.class);
        retMonomer = response.readEntity(String.class);
        System.out.println(retMonomer);
        Assert.assertEquals(response.getStatus(), 200);
    }
    
    @Test
    public void testUpdateMonomerTypes() {
        Client client = createClient();
        UriBuilder builder = UriBuilder.fromUri(BASE_URI);
        builder.path("monomer").path("CHEM").path("Az");
        URI uri = builder.build();
        Response response = client.target(uri).request().get();
        String retMonomer = response.readEntity(String.class);
        System.out.println(retMonomer);
        
        retMonomer = retMonomer.replace("CHEM", "PEPTIDE");
        retMonomer = retMonomer.replace("naturalAnalog\":\"-\"", "naturalAnalog\":\"I\"");
        
        response = client.target(uri).request().put(Entity.entity(retMonomer, MediaType.APPLICATION_JSON), Response.class);
        String retResponse= response.readEntity(String.class);
        System.out.println(retResponse);
        Assert.assertEquals(response.getStatus(), 409);
        
        retMonomer = retMonomer.replace("Undefined", "Backbone");
        response = client.target(uri).request().put(Entity.entity(retMonomer, MediaType.APPLICATION_JSON), Response.class);
        retResponse= response.readEntity(String.class);
        System.out.println(retResponse);
        Assert.assertEquals(response.getStatus(), 200);
        
        retMonomer = retMonomer.replace("PEPTIDE", "RNA");
        response = client.target(uri).request().put(Entity.entity(retMonomer, MediaType.APPLICATION_JSON), Response.class);
        retResponse= response.readEntity(String.class);
        System.out.println(retResponse);
        Assert.assertEquals(response.getStatus(), 409);
        
        retMonomer = retMonomer.replace("Backbone", "Nix");
        response = client.target(uri).request().put(Entity.entity(retMonomer, MediaType.APPLICATION_JSON), Response.class);
        retResponse= response.readEntity(String.class);
        System.out.println(retResponse);
        Assert.assertEquals(response.getStatus(), 409);
    }
    
    @Test
    public void testUpdateMonomerNaturalAnalog() {
        Client client = createClient();
        UriBuilder builder = UriBuilder.fromUri(BASE_URI);
        builder.path("monomer").path("CHEM").path("EG");
        URI uri = builder.build();
        Response response = client.target(uri).request().get();
        String retMonomer = response.readEntity(String.class);
        
        retMonomer = retMonomer.replace("naturalAnalog\":\"-\"", "naturalAnalog\":\"TESTIT\"");
        response = client.target(uri).request().put(Entity.entity(retMonomer, MediaType.APPLICATION_JSON), Response.class);
        String retResponse= response.readEntity(String.class);
        System.out.println(retResponse);
        Assert.assertEquals(response.getStatus(), 409);
        
        retMonomer = retMonomer.replace("CHEM", "PEPTIDE");
        retMonomer = retMonomer.replace("Undefined", "Backbone");
        response = client.target(uri).request().put(Entity.entity(retMonomer, MediaType.APPLICATION_JSON), Response.class);
        retResponse= response.readEntity(String.class);
        System.out.println(retResponse);
        Assert.assertEquals(response.getStatus(), 200);
        
        retMonomer = retMonomer.replace("TESTIT", "");
        response = client.target(uri).request().put(Entity.entity(retMonomer, MediaType.APPLICATION_JSON), Response.class);
        retResponse= response.readEntity(String.class);
        System.out.println(retResponse);
        Assert.assertEquals(response.getStatus(), 409);
    }

    @Test
    public void testUpdateMonomerName() {
        Client client = createClient();
        UriBuilder builder = UriBuilder.fromUri(BASE_URI);
        builder.path("monomer").path("CHEM").path("Az");
        URI uri = builder.build();
        Response response = client.target(uri).request().get();
        String retMonomer = response.readEntity(String.class);
        retMonomer = retMonomer.replace("Azide", "AzideTest");

        response = client.target(uri).request().put(Entity.entity(retMonomer, MediaType.APPLICATION_JSON), Response.class);
        retMonomer = response.readEntity(String.class);
        System.out.print(retMonomer);
        Assert.assertEquals(response.getStatus(), 200);
    }

    @Test
    public void testInserOrUpdateMonomer() {
    	//Get a Monomer
        Client client = createClient();
        UriBuilder builder = UriBuilder.fromUri(BASE_URI);
        builder.path("monomer").path("CHEM").path("Az");
        URI uri = builder.build();
        Response response = client.target(uri).request().get();
        String retMonomer = response.readEntity(String.class);
        System.out.println(retMonomer);
        Assert.assertTrue(retMonomer.contains("CHEM") && retMonomer.contains("Az"));
        String deleted = retMonomer;

        //Delete Monomer
        builder = UriBuilder.fromUri(BASE_URI);
        builder.path("monomer").path("CHEM").path("Az");
        uri = builder.build();
        response = client.target(uri).request().delete();
        
        //modify Data
        LWMonomer monomer = new LWMonomer();
        monomer.setSymbol("Foo");
        monomer.setMonomerType("Undefined");
        monomer.setName("Bar");
        monomer.setNaturalAnalog("-");
        monomer.setMolfile("\n  "
        		+ "Marvin  10200909502D          "
        		+ "\n\n  9  8  0  0  0  0            999 V2000"
        		+ "\n    1.0902    4.6259    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0"
        		+ "\n    1.0902    3.8009    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0"
        		+ "\n    0.3757    3.3884    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0"
        		+ "\n   -0.3388    3.8009    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0"
        		+ "\n   -1.0532    3.3884    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0"
        		+ "\n   -1.7677    3.8009    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0"
        		+ "\n   -2.4822    3.3884    0.0000 N   0  3  0  0  0  0  0  0  0  0  0  0"
        		+ "\n   -3.1966    2.9759    0.0000 N   0  5  0  0  0  0  0  0  0  0  0  0"
        		+ "\n    1.8046    3.3884    0.0000 R#  0  0  0  0  0  0  0  0  0  0  0  0"
        		+ "\n  1  2  2  0  0  0  0"
        		+ "\n  2  3  1  0  0  0  0"
        		+ "\n  3  4  1  0  0  0  0"
        		+ "\n  4  5  1  0  0  0  0"
        		+ "\n  5  6  1  0  0  0  0"
        		+ "\n  6  7  2  0  0  0  0"
        		+ "\n  7  8  2  0  0  0  0"
        		+ "\n  2  9  1  0  0  0  0"
        		+ "\nM  CHG  2   7   1   8  -1"
        		+ "\nM  RGP  1   9   1"
        		+ "\nM  END"
        		+ ""
        		+ "\n\n$$$$\n");
        monomer.setPolymerType("CHEM");
        monomer.setSmiles("");
        	//Attachment
        Attachment a1 = new Attachment();
        a1.setAlternateId("R1-OH");
        a1.setCapGroupName("OH");
        a1.setLabel("R1");
        a1.setCapGroupSMILES("O[*:1]");
        monomer.addAttachment(a1);
        	//Convert Monomer
        JsonConverter converter = new JsonConverter();
        try {
            retMonomer = converter.encodeMonomer(monomer);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        System.out.print("RetMonomer: " + retMonomer);
        
        //Insert new Monomer
        builder = UriBuilder.fromUri(BASE_URI);
        builder.path("monomer").path("CHEM").path("Foo");
        uri = builder.build();
        response = client.target(uri).request().put(Entity.entity(retMonomer, MediaType.APPLICATION_JSON), Response.class);
        retMonomer = response.readEntity(String.class);
        System.out.print(retMonomer);
        Assert.assertEquals(response.getStatus(), 200);

        //check duplicate SMILES register
        monomer.setSymbol("Foo2");
        monomer.setSmiles("");
        try {
            retMonomer = converter.encodeMonomer(monomer);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        builder = UriBuilder.fromUri(BASE_URI);
        builder.path("monomer").path("CHEM").path("Foo2");
        uri = builder.build();
        response = client.target(uri).request().put(Entity.entity(retMonomer, MediaType.APPLICATION_JSON), Response.class);
        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), 409);
        
        //delete insertet Monomer
        builder = UriBuilder.fromUri(BASE_URI);
        builder.path("monomer").path("CHEM").path("Foo");
        uri = builder.build();
        response = client.target(uri).request().delete();
        
        //insert original Monomer (Az)
        builder = UriBuilder.fromUri(BASE_URI);
        builder.path("monomer").path("CHEM").path("Az");
        uri = builder.build();
        response = client.target(uri).request().put(Entity.entity(deleted, MediaType.APPLICATION_JSON), Response.class);
        retMonomer = response.readEntity(String.class);
        System.out.print(retMonomer);
        Assert.assertEquals(response.getStatus(), 200);
        
    }
    
    @Test
    public void testIsConnected() {
    	//Get a Monomer
        Client client = createClient();
        UriBuilder builder = UriBuilder.fromUri(BASE_URI);
        builder.path("monomer").path("CHEM").path("Az");
        URI uri = builder.build();
        Response response = client.target(uri).request().get();
        String retMonomer = response.readEntity(String.class);
        String deleted = retMonomer;

        //Delete Monomer
        builder = UriBuilder.fromUri(BASE_URI);
        builder.path("monomer").path("CHEM").path("Az");
        uri = builder.build();
        response = client.target(uri).request().delete();
        
        //modify Data => Monomer is not connected
        LWMonomer monomer = new LWMonomer();
        monomer.setSymbol("Foo");
        monomer.setMonomerType("Undefined");
        monomer.setName("Bar");
        monomer.setNaturalAnalog("null");
        monomer.setMolfile("\n  "
        		+ "Marvin  10200909502D          "
        		+ "\n\n  9  7  0  0  0  0            999 V2000"
        		+ "\n    1.0902    4.6259    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0"
        		+ "\n    1.0902    3.8009    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0"
        		+ "\n    0.3757    3.3884    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0"
        		+ "\n   -0.3388    3.8009    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0"
        		+ "\n   -1.0532    3.3884    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0"
        		+ "\n   -1.7677    3.8009    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0"
        		+ "\n   -2.4822    3.3884    0.0000 N   0  3  0  0  0  0  0  0  0  0  0  0"
        		+ "\n   -3.1966    2.9759    0.0000 N   0  5  0  0  0  0  0  0  0  0  0  0"
        		+ "\n    1.8046    3.3884    0.0000 R#  0  0  0  0  0  0  0  0  0  0  0  0"
        		+ "\n  1  2  2  0  0  0  0"
        		+ "\n  2  3  1  0  0  0  0"
        		+ "\n  4  5  1  0  0  0  0"
        		+ "\n  5  6  1  0  0  0  0"
        		+ "\n  6  7  2  0  0  0  0"
        		+ "\n  7  8  2  0  0  0  0"
        		+ "\n  2  9  1  0  0  0  0"
        		+ "\nM  CHG  2   7   1   8  -1"
        		+ "\nM  RGP  1   9   1"
        		+ "\nM  END"
        		+ ""
        		+ "\n\n$$$$\n");
        monomer.setPolymerType("CHEM");
        monomer.setSmiles("");
        	//Attachment
        Attachment a1 = new Attachment();
        a1.setAlternateId("R1-OH");
        a1.setCapGroupName("OH");
        a1.setLabel("R1");
        a1.setCapGroupSMILES("O[*:1]");
        monomer.addAttachment(a1);
        	//convertMonomer
        JsonConverter converter = new JsonConverter();
        try {
            retMonomer = converter.encodeMonomer(monomer);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        System.out.print("RetMonomer: " + retMonomer);
        
        //Insert new Monomer
        builder = UriBuilder.fromUri(BASE_URI);
        builder.path("monomer").path("CHEM").path("Foo");
        uri = builder.build();
        response = client.target(uri).request().put(Entity.entity(retMonomer, MediaType.APPLICATION_JSON), Response.class);
        retMonomer = response.readEntity(String.class);
        System.out.print(retMonomer);
        Assert.assertEquals(response.getStatus(), 409);

        //delete insertet Monomer
        builder = UriBuilder.fromUri(BASE_URI);
        builder.path("monomer").path("CHEM").path("Foo");
        uri = builder.build();
        response = client.target(uri).request().delete();
        
        //insert original Monomer (Az)
        builder = UriBuilder.fromUri(BASE_URI);
        builder.path("monomer").path("CHEM").path("Az");
        uri = builder.build();
        response = client.target(uri).request().put(Entity.entity(deleted, MediaType.APPLICATION_JSON), Response.class);
        retMonomer = response.readEntity(String.class);
        System.out.print(retMonomer);
        Assert.assertEquals(response.getStatus(), 200);
        
    }
    
    @Test
    public void testRestOfValidation() {
    	//Get a Monomer
        Client client = createClient();
        UriBuilder builder = UriBuilder.fromUri(BASE_URI);
        builder.path("monomer").path("CHEM").path("SS3");
        URI uri = builder.build();
        Response response = client.target(uri).request().get();
        String retMonomer = response.readEntity(String.class);
        String deleted = retMonomer;
        System.out.println("hi: "+deleted);
        
        //Delete Monomer
        response = client.target(uri).request().delete();
        
        LWMonomer monomer = new LWMonomer();
        //insert Monomer
        JsonConverter converter = new JsonConverter();
        try {
            retMonomer = converter.encodeMonomer(monomer);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        builder = UriBuilder.fromUri(BASE_URI);
        builder.path("monomer").path("CHEM").path("Foo");
        uri = builder.build();
        response = client.target(uri).request().put(Entity.entity(retMonomer, MediaType.APPLICATION_JSON), Response.class);
        retMonomer = response.readEntity(String.class);
        System.out.print(retMonomer);
        Assert.assertEquals(response.getStatus(), 409);
        
        
        monomer.setMolfile("\n  "
        		+ "Marvin  04140911352D          "
        		+ "\n\n 12 11  0  0  0  0            999 V2000"
        		+ "\n   -1.2835    0.3381    0.0000 R#  0  0  0  0  0  0  0  0  0  0  0  0"
        		+ "\n   -1.2917   -0.4833    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0"
        		+ "\n   -0.5814   -0.9030    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0"
        		+ "\n   -0.5875   -1.7250    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0"
        		+ "\n    0.1239   -2.1428    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0"
        		+ "\n    0.1167   -2.9667    0.0000 S   0  0  0  0  0  0  0  0  0  0  0  0"
        		+ "\n    0.8292   -3.3792    0.0000 S   0  0  0  0  0  0  0  0  0  0  0  0"
        		+ "\n    0.8250   -4.2042    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0"
        		+ "\n    1.5358   -4.6229    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0"
        		+ "\n    1.5286   -5.4479    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0"
        		+ "\n    2.2395   -5.8666    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0"
        		+ "\n    2.2323   -6.6916    0.0000 R#  0  0  0  0  0  0  0  0  0  0  0  0"
        		+ "\n  6  7  1  0  0  0  0"
        		+ "\n  2  3  1  0  0  0  0"
        		+ "\n  7  8  1  0  0  0  0"
        		+ "\n  8  9  1  0  0  0  0"
        		+ "\n  3  4  1  0  0  0  0"
        		+ "\n  9 10  1  0  0  0  0"
        		+ "\n 10 11  1  0  0  0  0"
        		+ "\n  4  5  1  0  0  0  0"
        		+ "\n 11 12  1  0  0  0  0"
        		+ "\n  1  2  1  0  0  0  0"
        		+ "\n  5  6  1  0  0  0  0"
        		+ "\nM  RGP  2   1   1  12   2"
        		+ "\nM  END"
        		+ "\n"
        		+ "\n$$$$\n");
        //insert Monomer
        converter = new JsonConverter();
        try {
            retMonomer = converter.encodeMonomer(monomer);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        builder = UriBuilder.fromUri(BASE_URI);
        builder.path("monomer").path("CHEM").path("Foo");
        uri = builder.build();
        response = client.target(uri).request().put(Entity.entity(retMonomer, MediaType.APPLICATION_JSON), Response.class);
        retMonomer = response.readEntity(String.class);
        System.out.print(retMonomer);
        Assert.assertEquals(response.getStatus(), 409);
        
        Attachment a1 = new Attachment();
        a1.setAlternateId("R1-OH");
        a1.setCapGroupName("OH");
        a1.setLabel("R1");
        a1.setCapGroupSMILES("O[*:1]");
        Attachment a2 = new Attachment();
        a2.setAlternateId("R2-OH");
        a2.setCapGroupName("OH");
        a2.setLabel("R2");
        a2.setCapGroupSMILES("O[*:2]");
        monomer.addAttachment(a1);
        monomer.addAttachment(a2);
        //insert Monomer
        converter = new JsonConverter();
        try {
            retMonomer = converter.encodeMonomer(monomer);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        builder = UriBuilder.fromUri(BASE_URI);
        builder.path("monomer").path("CHEM").path("Foo");
        uri = builder.build();
        response = client.target(uri).request().put(Entity.entity(retMonomer, MediaType.APPLICATION_JSON), Response.class);
        retMonomer = response.readEntity(String.class);
        System.out.print(retMonomer);
        Assert.assertEquals(response.getStatus(), 409);
        
        monomer.setName("Bar");
        //insert Monomer
        converter = new JsonConverter();
        try {
            retMonomer = converter.encodeMonomer(monomer);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        builder = UriBuilder.fromUri(BASE_URI);
        builder.path("monomer").path("CHEM").path("Foo");
        uri = builder.build();
        response = client.target(uri).request().put(Entity.entity(retMonomer, MediaType.APPLICATION_JSON), Response.class);
        retMonomer = response.readEntity(String.class);
        System.out.print(retMonomer);
        Assert.assertEquals(response.getStatus(), 409);
        
        monomer.setPolymerType("CHEM");
        //insert Monomer
        converter = new JsonConverter();
        try {
            retMonomer = converter.encodeMonomer(monomer);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        builder = UriBuilder.fromUri(BASE_URI);
        builder.path("monomer").path("CHEM").path("Foo");
        uri = builder.build();
        response = client.target(uri).request().put(Entity.entity(retMonomer, MediaType.APPLICATION_JSON), Response.class);
        retMonomer = response.readEntity(String.class);
        System.out.print(retMonomer);
        Assert.assertEquals(response.getStatus(), 409);
        
        monomer.setSymbol("Foo");
        //insert Monomer
        converter = new JsonConverter();
        try {
            retMonomer = converter.encodeMonomer(monomer);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        builder = UriBuilder.fromUri(BASE_URI);
        builder.path("monomer").path("CHEM").path("Foo");
        uri = builder.build();
        response = client.target(uri).request().put(Entity.entity(retMonomer, MediaType.APPLICATION_JSON), Response.class);
        retMonomer = response.readEntity(String.class);
        System.out.print(retMonomer);
        Assert.assertEquals(response.getStatus(), 409);
        
        monomer.setMonomerType("Undefined");
        //insert Monomer
        converter = new JsonConverter();
        try {
            retMonomer = converter.encodeMonomer(monomer);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        builder = UriBuilder.fromUri(BASE_URI);
        builder.path("monomer").path("CHEM").path("Foo");
        uri = builder.build();
        response = client.target(uri).request().put(Entity.entity(retMonomer, MediaType.APPLICATION_JSON), Response.class);
        retMonomer = response.readEntity(String.class);
        System.out.print(retMonomer);
        Assert.assertEquals(response.getStatus(), 409);
        
        monomer.setNaturalAnalog("-");
        //insert Monomer
        converter = new JsonConverter();
        try {
            retMonomer = converter.encodeMonomer(monomer);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        builder = UriBuilder.fromUri(BASE_URI);
        builder.path("monomer").path("CHEM").path("Foo");
        uri = builder.build();
        response = client.target(uri).request().put(Entity.entity(retMonomer, MediaType.APPLICATION_JSON), Response.class);
        retMonomer = response.readEntity(String.class);
        System.out.print(retMonomer);
        Assert.assertEquals(response.getStatus(), 200);
        

        //delete insertet Monomer
        builder = UriBuilder.fromUri(BASE_URI);
        builder.path("monomer").path("CHEM").path("Foo");
        uri = builder.build();
        response = client.target(uri).request().delete();
        
        //insert original Monomer (Az)
        builder = UriBuilder.fromUri(BASE_URI);
        builder.path("monomer").path("CHEM").path("SS3");
        uri = builder.build();
        response = client.target(uri).request().put(Entity.entity(deleted, MediaType.APPLICATION_JSON), Response.class);
        retMonomer = response.readEntity(String.class);
        
    }
}
