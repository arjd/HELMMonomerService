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
		monomer.setMolfile("xxx");
		monomer.setPolymerType("CHEM");
		monomer.setSmiles("cccccc");
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
	

}
