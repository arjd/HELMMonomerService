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

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.helm.notation2.Chemistry;

/**
 * Provides REST-methods for monomers
 * 
 * path = HELM2MonomerService/monomer/...
 */

@Path("/monomer")
@Api(value="/monomer")
public class RestMonomer {

	
	@Path("{polymertype}/{symbol}")
	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Delete a Monomer", notes = "Delete a Monomer",response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Monomer successfully deleted"),
			@ApiResponse(code = 404, message = "Monomer not found") })
	public Response deleteMonomer(@PathParam("polymertype") String polymerType, @PathParam("symbol") String symbol) {
		try {
			int i = LibraryManager.getInstance().getMonomerLibrary().deleteMonomer(polymerType, symbol);
			if (i < 0) {
				return Response.status(Response.Status.NOT_FOUND).entity("Monomer not found in database: " + symbol).build();
				//return Response.noContent().build();
			} else {
				return Response.ok().build();
			}
		} catch (Exception e) {
			return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
		}
	}

	@Path("{polymertype}/{symbol}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Show details of monomer", httpMethod = "GET", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Details successesfully generated"),
			@ApiResponse(code = 404, message = "Monomer was not found") })
	public Response monomerDetail(@PathParam("polymertype") String polymerType, @PathParam("symbol") String symbol) {
		LWMonomer monomer;
		try {
			monomer = LibraryManager.getInstance().getMonomerLibrary().monomerDetail(polymerType, symbol);
			return Response.status(Response.Status.OK).entity(wrapMonomer(monomer)).build();
		} catch (Exception e) {
			return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
		}
	}

	@Path("{polymertype}")
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get all monomers sorted by type", notes = "Polymertype can be RNA, PEPTIDE, CHEM or ALL", httpMethod = "GET", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Monomers were successfully listed"),
			@ApiResponse(code = 400, message = "Monomers can not be listed") })
	public Response showMonomerList(@PathParam("polymertype") String polymerType,
			@QueryParam("monomertype") String monomerType, @QueryParam("filter") String filter,
			@QueryParam("filterField") String filterField, @QueryParam("limit") int limit,
			@QueryParam("offset") int offset) {
		List<LWMonomer> monomerList;
		try {
			IMonomerLibrary monomerLibrary = LibraryManager.getInstance().getMonomerLibrary();
			/*
			 * if (limit <= 0) { limit = 10; }
			 */
			monomerList = monomerLibrary.showMonomerList(polymerType, monomerType, filter, filterField, offset, limit);
			int total = monomerLibrary.getTotalCount();
			return Response.status(Response.Status.OK).entity(wrapMonomerList(monomerList, offset, limit, total))
					.build();
		} catch (Exception e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
		}

	}

	@Path("{polymertype}/{Symbol}")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Insert new monomer or update monomer", httpMethod = "PUT", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Monomer successfully inserted/updated"),
			@ApiResponse(code = 409, message = "Error input") })
	public Response insertOrUpdateMonomer(@PathParam("polymertype") String polymerType,
			@PathParam("Symbol") String symbol, @ApiParam(value = "Monomer", required = true) String monomerString) {
		JsonConverter converter = new JsonConverter();
		try {
			LWMonomer monomer = converter.decodeMonomer(monomerString);
			String SMILESStr = monomer.getSmiles();
			if(SMILESStr == null || SMILESStr.isEmpty()) {
				SMILESStr = Chemistry.getInstance().getManipulator().convertMolIntoSmilesWithAtomMapping(monomer.getMolfile());
				//SMILESStr = Chemistry.getInstance().getManipulator().convertExtendedSmiles(monomer.getSmiles());
				monomer.setSmiles(SMILESStr);
			}
			
			int id = -1;
			String MonomerType = monomer.getMonomerType(); 
			if (MonomerType.equals("Undefined") && !polymerType.equals("CHEM")) {
				id = -7000;
			} else if (!MonomerType.equals("Undefined") && polymerType.equals("CHEM")) {
				id = -7100;
			} else {			
				id = LibraryManager.getInstance().getMonomerLibrary().insertOrUpdateMonomer(polymerType,
					symbol, monomer);
			}
			
			
			
			switch (id) {
			case -1000:
				return Response.status(Response.Status.CONFLICT).entity("Monomer with this structure is already registered: " + monomer.getSymbol()).build();	
			case -2000:
				return Response.status(Response.Status.CONFLICT).entity("Monomer with this symbol is already registered: " + monomer.getSymbol()).build();	
			case -3000:
				return Response.status(Response.Status.CONFLICT).entity("(Update Monomer)Monomer with this symbol is not registered: " + monomer.getSymbol()).build();	
			case -5100:
				return Response.status(Response.Status.CONFLICT).entity("Monomer has no Molfile").build();
			case -5200:
				return Response.status(Response.Status.CONFLICT).entity("Monomer has no Smiles; Smiles could not be generated from Molfile and R-Groups").build();
			case -5300:
				return Response.status(Response.Status.CONFLICT).entity("Monomer has wrong R-Groups").build();
			case 5400:
				//return Response.status(Response.Status.CONFLICT).entity("Molfile and Smiles do not equal").build();
			case -6000:
				return Response.status(Response.Status.CONFLICT).entity("Monomer has no Name").build();
			case -6100:
				return Response.status(Response.Status.CONFLICT).entity("Monomer has no Polymertype").build();
			case -6200:
				return Response.status(Response.Status.CONFLICT).entity("Monomer has no Symbol").build();
			case -6300:
				return Response.status(Response.Status.CONFLICT).entity("Monomer has no Monomertype").build();
			case -7000:
				return Response.status(Response.Status.CONFLICT).entity("Undefined monomertype only allowed for CHEM monomers").build();
			case -7100:
				return Response.status(Response.Status.CONFLICT).entity("CHEM monomers must have an undefined monomertype").build();
			case -6400:
				//return Response.status(Response.Status.CONFLICT).entity("Monomer has no Natural Analog").build();
			default:
				break;
			}
			return Response.status(Response.Status.OK).entity(wrapMonomer(monomer)).build();
		} catch (Exception e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}



	private String wrapMonomer(LWMonomer monomer) throws JsonProcessingException {
		JsonConverter converter = new JsonConverter();
		try {
			return converter.encodeMonomer(monomer);
		} catch (JsonProcessingException e) {
			throw e;
		}
	}

	private String wrapMonomerList(List<LWMonomer> monomer, int offset, int limit, int total)
			throws JsonProcessingException {
		JsonConverter converter = new JsonConverter();
		JSONObject json = new JSONObject();
		json.put("offset", offset);
		json.put("limit", limit);
		json.put("total", total);

		JSONArray monomerarray = new JSONArray();
		json.put("monomers", monomerarray);

		for (int i = 0; i < monomer.size(); i++) {
			try {
				JSONObject monomerjson = new JSONObject(converter.encodeMonomer(monomer.get(i)));
				monomerarray.put(monomerjson);
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				throw e;
			}
		}

		return json.toString();
	}
}