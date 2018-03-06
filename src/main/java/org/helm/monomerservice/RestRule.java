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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Provides REST-methods for rules
 * 
 * path = HELM2MonomerService/webService/service/....
 */

@Path ("/rule")
@Api(value="/rule")
public class RestRule {

	
	@Path("{id}")
	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Delete rule", httpMethod = "DELETE", response = Response.class)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Rule successfully deleted"), @ApiResponse(code = 404, message = "Rule not found")})
	public Response deleteRule(@PathParam ("id") int id) {
		try {
			int ret = LibraryManager.getInstance().getRulesLibrary().deleteRule(id);
			if(ret == -1) {
				return Response.status(Response.Status.NOT_FOUND).entity("Rule not found in database; ID of rule = " + id).build();
			}
			else {
				return Response.ok().build();
			}
		}
		catch(Exception e) {
			return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
		}
	}
	
	@Path("{id}")
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Show rule", httpMethod = "GET", response = Response.class)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Rule showed"), @ApiResponse(code = 404, message = "Rule not found")})
	public Response showRule(@PathParam("id") int id) {
		try {
			Rule rule = LibraryManager.getInstance().getRulesLibrary().showRule(id);
			return Response.status(Response.Status.OK).entity(rule).build();
		}
		catch(Exception e) {
			return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
		}
	}
	
	@Path("")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Show all rules", httpMethod = "GET", response = Response.class)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "List of rules generated"), @ApiResponse(code = 400, message = "Error input")})
	public Response showAllRules() {
		try {
			List<Rule> rules = new ArrayList<Rule>();
			rules = LibraryManager.getInstance().getRulesLibrary().showAllRules();
			return Response.ok().entity(rules).build();
		}
		catch(Exception e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}
	
	@Path("")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Add rule", httpMethod = "PUT", response = Response.class)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Rule successesfully added"), @ApiResponse(code = 400, message = "Error input")})
	public Response addRule( @ApiParam(value = "Rule", required = true) String ruleString) {
		JsonConverter converter = new JsonConverter();
		try {
			Rule rule = converter.decodeRule(ruleString);
			int status = LibraryManager.getInstance().getRulesLibrary().insertOrUpdateRule(rule);
			
			if(status == 0) {
				return Response.status(Response.Status.OK).entity(wrapRule(rule)).build();
			}else {
				switch(status) {
				case -1: return Response.status(Response.Status.CONFLICT).entity("Rule already exists with an other ID").build();
				case -1000: return Response.status(Response.Status.CONFLICT).entity("Rule has no Script").build();
				case -2000: return Response.status(Response.Status.CONFLICT).entity("Rule has no Category").build();
				case -3000: return Response.status(Response.Status.CONFLICT).entity("Rule has no ID").build();
				default: return Response.status(Response.Status.BAD_REQUEST).entity("Error input").build();
				}
			}
		}
		catch(Exception e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}
	
	private String wrapRule(Rule rule) throws JsonProcessingException {
		JsonConverter converter = new JsonConverter();
		try {
			return converter.encodeRule(rule);
		} catch (JsonProcessingException e) {
			throw e;
		}
	}
	
}
