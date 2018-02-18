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

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonProcessingException;


public class RestMonomerTest extends StandaloneServer{

	/**
	 * Before testing run "Main" to create a new database
	 * copy the created database from the project-folder into the resource-folder
	 */
	
	
	@Test
	public void testDeleteMonomer() {
		Client client = createClient();
		
		UriBuilder builder = UriBuilder.fromUri(BASE_URI);
		builder.path("monomer").path("PEPTIDE").path("Cap");
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
		Assert.assertTrue(retMonomerList.contains("Alexa") && retMonomerList.contains("SMCC"));
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
		Assert.assertTrue(retMonomerList.contains("5fU"));
	}
	
	@Test
	public void testShowFilteredMonomers() {
		Client client = createClient();
		UriBuilder builder = UriBuilder.fromUri(BASE_URI);
		builder.path("monomer").path("RNA");
		builder.queryParam("filter", "ddR");
		//builder.queryParam("filterField", "symbol");
		URI uri = builder.build();
		Response response = client.target(uri).request().get();
		String retMonomerList = response.readEntity(String.class);
		System.out.println(retMonomerList);
		Assert.assertTrue(retMonomerList.contains("name"));
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
		Assert.assertEquals(response.getStatus(),200);
		
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
		Assert.assertEquals(response.getStatus(),200);
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
		Assert.assertEquals(response.getStatus(), 200);
		
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
		
		Client client = createClient();
		UriBuilder builder = UriBuilder.fromUri(BASE_URI);
		builder.path("monomer").path("CHEM").path("Az");
		URI uri = builder.build();
		Response response = client.target(uri).request().get();
		String retMonomer = response.readEntity(String.class);
		//System.out.println(retMonomer);
		Assert.assertTrue(retMonomer.contains("CHEM") && retMonomer.contains("Az"));
		
		LWMonomer monomer = new LWMonomer();
		monomer.setSymbol("Foo");
		monomer.setMonomerType("Undefinded");
		monomer.setName("Bar");
		monomer.setNaturalAnalog("X");
		monomer.setMolfile("\r\n" + 
				"  Mrv1541302141809162D          \r\n" + 
				"\r\n" + 
				"  7  6  0  0  0  0            999 V2000\r\n" + 
				"   -3.7054    0.9598    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\r\n" + 
				"   -2.9909    0.5473    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\r\n" + 
				"   -2.2764    0.9598    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\r\n" + 
				"   -1.5619    0.5473    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\r\n" + 
				"   -0.8475    0.9598    0.0000 R#  0  0  0  0  0  0  0  0  0  0  0  0\r\n" + 
				"   -3.4034   -0.1671    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\r\n" + 
				"   -2.9909   -0.8816    0.0000 R#  0  0  0  0  0  0  0  0  0  0  0  0\r\n" + 
				"  1  2  1  0  0  0  0\r\n" + 
				"  2  3  1  0  0  0  0\r\n" + 
				"  3  4  1  0  0  0  0\r\n" + 
				"  4  5  1  0  0  0  0\r\n" + 
				"  6  7  1  0  0  0  0\r\n" + 
				"  2  6  1  0  0  0  0\r\n" + 
				"M  RGP  2   5   1   7   2\r\n" + 
				"M  END\r\n" + 
				"");
		monomer.setPolymerType("CHEM");
		monomer.setSmiles("");
		JsonConverter converter = new JsonConverter();
		try {
			retMonomer = converter.encodeMonomer(monomer);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.print(retMonomer);
		
		UriBuilder builder2 = UriBuilder.fromUri(BASE_URI);
		builder2.path("monomer").path("CHEM").path("Foo");
		URI uri2 = builder2.build();
		
		response = client.target(uri2).request().put(Entity.entity(retMonomer, MediaType.APPLICATION_JSON), Response.class);
		retMonomer = response.readEntity(String.class);
		System.out.print(retMonomer);
		Assert.assertEquals(response.getStatus(), 200);
	}
	

	@Test
	public void testJSON() {
		Client client = createClient();
		UriBuilder builder = UriBuilder.fromUri(BASE_URI);
		builder.path("monomer").path("CHEM").path("Az");
		URI uri = builder.build();
		Response response = client.target(uri).request().get();
		String retMonomer = response.readEntity(String.class);
		//System.out.println(retMonomer);
		Assert.assertTrue(retMonomer.contains("CHEM") && retMonomer.contains("Az"));
		
		retMonomer = "{\r\n" + 
				"	\"symbol\": \"A1234Test\",\r\n" + 
				"	\"name\": \"A1234Test\",\r\n" + 
				"	\"polymertype\": \"CHEM\",\r\n" + 
				"	\"monomertype\": \"Backbone\",\r\n" + 
				"	\"r1\": \"H\",\r\n" + 
				"	\"doc\": \"<data>\\n<i n='symbol'>A1234Test</i>\\n<i n='name'>A1234Test</i>\\n<i n='polymertype'>CHEM</i>\\n<i n='monomertype'>Backbone</i>\\n<i n='r1'>H</i>\\n</data>\",\r\n" + 
				"	\"molfile\": \"\\n   JSDraw202181820112D\\n\\n  7  6  0  0  0  0              0 V2000\\n   14.5080   -3.1200    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   15.8590   -2.3400    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   14.5080   -4.6800    0.0000 R#  0  0  0  0  0  0  0  0  0  0  0  0\\n   13.1570   -2.3400    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   11.8060   -3.1200    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   11.8060   -4.6800    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n   13.1570   -5.4600    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\\n  1  2  1  0  0  0  0\\n  1  3  1  0  0  0  0\\n  1  4  1  0  0  0  0\\n  4  5  1  0  0  0  0\\n  5  6  1  0  0  0  0\\n  6  7  1  0  0  0  0\\nA    3\\nR1\\nM  RGP  1   3   1\\nM  END\\n\",\r\n" + 
				"	\"polymerType\": \"CHEM\",\r\n" + 
				"	\"monomerType\": \"Backbone\",\r\n" + 
				"	\"rgroups\": [{\r\n" + 
				"		\"label\": \"R1\",\r\n" + 
				"		\"capGroupName\": \"H\"\r\n" + 
				"	}]\r\n" + 
				"}";
		
		
		
		UriBuilder builder2 = UriBuilder.fromUri(BASE_URI);
		builder2.path("monomer").path("CHEM").path("A1234Test");
		URI uri2 = builder2.build();
		
		response = client.target(uri2).request().put(Entity.entity(retMonomer, MediaType.APPLICATION_JSON), Response.class);
		retMonomer = response.readEntity(String.class);
		System.out.print(retMonomer);
		Assert.assertEquals(response.getStatus(), 200);
	}
	
}
