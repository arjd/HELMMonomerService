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


public class RestMonomerTest extends StandaloneServer{

	/**
	 * Before testing run "Main" to create a new database
	 * copy the created database from the project-folder into the resource-folder
	 */
	
	
	@Test
	public void testDeleteMonomer() {
		Client client = createClient();
		
		UriBuilder builder = UriBuilder.fromUri(BASE_URI);
		builder.path("library").path("monomer").path("CHEM").path("xxx");
		URI uri = builder.build();
		Response response = client.target(uri).request().delete();
		System.out.println(response.readEntity(String.class));
		Assert.assertEquals(response.getStatus(), 204);
		
	}
	
	@Test
	public void testMonomerDetail() {
		Client client = createClient();
		UriBuilder builder = UriBuilder.fromUri(BASE_URI);
		builder.path("library").path("monomer").path("PEPTIDE").path("D");
		URI uri = builder.build();
		Response response = client.target(uri).request().get();
		String retMonomer = response.readEntity(String.class);
		System.out.println(retMonomer);
		Assert.assertTrue(retMonomer.contains("PEPTIDE") && retMonomer.contains("D"));
	}	
	
	
	
	@Test
	public void testShowAllMonomersByType() {
		Client client = createClient();
		UriBuilder builder = UriBuilder.fromUri(BASE_URI);
		builder.path("library").path("monomer").path("CHEM");
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
		builder.path("library").path("monomer").path("RNA");
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
		builder.path("library").path("monomer").path("CHEM");
		builder.queryParam("filter", "A");
		URI uri = builder.build();
		Response response = client.target(uri).request().get();
		String retMonomerList = response.readEntity(String.class);
		System.out.println(retMonomerList);
		Assert.assertTrue(retMonomerList.contains("A6OH") && !retMonomerList.contains("SMCC"));
	}	

	@Test
	public void testMonomerPagination() {
		Client client = createClient();
		UriBuilder builder = UriBuilder.fromUri(BASE_URI);
		builder.path("library").path("monomer").path("ALL");
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
		builder.path("library").path("monomer").path("ALL");
		URI uri = builder.build();
		Response response = client.target(uri).request().get();
		String retMonomerList = response.readEntity(String.class);
		Assert.assertEquals(response.getStatus(),200);
	}
	

	//can throw an error if no new database is used, because the monomer "MCC" has a new name and could not be found in the database
	@Test
	public void testUpdateMonomer() {
		Client client = createClient();
		UriBuilder builder = UriBuilder.fromUri(BASE_URI);
		builder.path("library").path("monomer").path("PEPTIDE").path("Aze");
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
		builder.path("library").path("monomer").path("CHEM").path("A6OH");
		URI uri = builder.build();
		Response response = client.target(uri).request().get();
		String retMonomer = response.readEntity(String.class);
		retMonomer = retMonomer.replace("6-amino-hexanol", "6'amino-hexanol");
		System.out.print(retMonomer);
		response = client.target(uri).request().put(Entity.entity(retMonomer, MediaType.APPLICATION_JSON), Response.class);
		retMonomer = response.readEntity(String.class);
		Assert.assertEquals(response.getStatus(), 200);
		
	}	
	
	@Test
	public void testInserOrUpdateMonomer() {
		Client client = createClient();
		UriBuilder builder = UriBuilder.fromUri(BASE_URI);
		builder.path("library").path("monomer").path("CHEM").path("A6OH");
		URI uri = builder.build();
		Response response = client.target(uri).request().get();
		String retMonomer = response.readEntity(String.class);
		
		retMonomer = retMonomer.replace("A6OH", "test3");
		retMonomer = retMonomer.replace("[H:1]OCCCCCCN[H:2]", "[H:1]OCCCCCCCN[H:2]");
		retMonomer = retMonomer.replace("11290920372", "126");
		retMonomer = retMonomer.replace("6-amino-hexanol", "7'amino-hexanol");
		retMonomer = retMonomer.replace("Undefined", "Backbone");
		
		UriBuilder builder2 = UriBuilder.fromUri(BASE_URI);
		builder2.path("library").path("monomer").path("CHEM").path("test3");
		URI uri2 = builder2.build();
		System.out.print(retMonomer);
		Response response2 = client.target(uri2).request().put(Entity.entity(retMonomer, MediaType.APPLICATION_JSON), Response.class);
		String retMonomer2 = response2.readEntity(String.class);
		Assert.assertEquals(response2.getStatus(), 200);
	}
	

}
