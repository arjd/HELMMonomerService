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

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;


public class RestRuleTest extends StandaloneServer {

	
	@Test
	public void testShowRule() {
		Client client = createClient();
		UriBuilder builder = UriBuilder.fromUri(BASE_URI);
		builder.path("rule").path("1");
		URI uri = builder.build();
		Response response = client.target(uri).request().get();
		String retRule = response.readEntity(String.class);
		System.out.println(retRule);
		Assert.assertEquals(response.getStatus(),Response.Status.OK.getStatusCode());
	}
	
	@Test
	public void testShowAllRules() {
		Client client = createClient();
		UriBuilder builder = UriBuilder.fromUri(BASE_URI);
		builder.path("rule");
		URI uri = builder.build();
		Response response = client.target(uri).request().get();
		String retRule = response.readEntity(String.class);
		System.out.println(retRule);
		Assert.assertEquals(response.getStatus(),Response.Status.OK.getStatusCode());
	}
	
	@Test
	public void testUpdateRule() throws JsonParseException, JsonMappingException, IOException{
		Client client = createClient();
		JsonConverter converter = new JsonConverter();
		
		UriBuilder builder = UriBuilder.fromUri(BASE_URI);
		builder.path("rule").path("1");
		URI uri = builder.build();
		Response response = client.target(uri).request().get();
		String retRule = response.readEntity(String.class);
		Rule ruleObj = converter.decodeRule(retRule);
		ruleObj.setName("Replace base A with X");
		retRule = converter.encodeRule(ruleObj);
		UriBuilder builder2 = UriBuilder.fromUri(BASE_URI);
		builder2.path("rule");
		URI uri2 = builder2.build();
		
		Response response2 = client.target(uri2).request().put(Entity.entity(retRule, MediaType.APPLICATION_JSON), Response.class);
		String retRule2 = response2.readEntity(String.class);
		Assert.assertEquals(response2.getStatus(), 200);
	}
	
	@Test
	public void testAddRule() {
		///testAddRule + DeleteNewRule
		Client client = createClient();
		UriBuilder builder = UriBuilder.fromUri(BASE_URI);
	    builder.path("rule");
	    URI uri = builder.build();
	    Rule rule = new Rule();
	    Response response = client.target(uri).request().put(Entity.entity(rule,MediaType.APPLICATION_JSON), Response.class);
	    String retRule = response.readEntity(String.class);
		Assert.assertTrue(retRule.contains(rule.getName()) && retRule.contains(rule.getCategory()));
		
		builder = UriBuilder.fromUri(BASE_URI);
		builder.path("rule").path("0");
		uri = builder.build();
		response = client.target(uri).request().delete();
		Assert.assertEquals(response.getStatus(), 200);
		
		
		
	}
	
}
