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
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.helm.notation2.exception.MonomerException;
import org.jdom2.JDOMException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Provides REST-methods for monomers
 * 
 * path = HELM2MonomerService/webService/service/....
 */

@Path("/library")
public class RestMonomer {

	@Path("monomer/{polymertype}/{symbol}")
	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Delete a Monomer", httpMethod = "DELETE", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Monomer successfully deleted"),
			@ApiResponse(code = 400, message = "Monomer was not deleted") })
	public Response deleteMonomer(@PathParam("polymertype") String polymerType, @PathParam("symbol") String symbol) {
		try {
			int i = LibraryManager.getInstance().getMonomerLibrary().deleteMonomer(polymerType, symbol);
			if (i < 0) {
				return Response.noContent().build();
			} else {
				return Response.ok().build();
			}
		} catch (Exception e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@Path("monomer/{polymertype}/{symbol}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Show details of monomer", httpMethod = "GET", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Details successesfully generated"),
			@ApiResponse(code = 400, message = "Error input") })
	public Response monomerDetail(@PathParam("polymertype") String polymerType, @PathParam("symbol") String symbol) {
		LWMonomer monomer;
		try {
			monomer = LibraryManager.getInstance().getMonomerLibrary().monomerDetail(polymerType, symbol);
			return Response.status(Response.Status.OK).entity(wrapMonomer(monomer)).build();
		} catch (Exception e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@Path("/monomer/{polymertype}")
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get all monomers sorted by type", httpMethod = "GET", response = Response.class)
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

	@Path("/monomer/{polymertype}/{Symbol}")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Insert new monomer or update monomer", httpMethod = "PUT", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Monomer successfully inserted/updated"),
			@ApiResponse(code = 400, message = "Error input") })
	public Response insertOrUpdateMonomer(@PathParam("polymertype") String polymerType,
			@PathParam("Symbol") String symbol, String monomerString) {
		JsonConverter converter = new JsonConverter();
		try {
			LWMonomer monomer = converter.decodeMonomer(monomerString);
			LWMonomer retMonomer = LibraryManager.getInstance().getMonomerLibrary().insertOrUpdateMonomer(polymerType,
					symbol, monomer);

			return Response.status(Response.Status.OK).entity(wrapMonomer(retMonomer)).build();
		} catch (Exception e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@Path("/insertMonomer")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Insert new monomer", httpMethod = "POST", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Monomer successfully inserted"),
			@ApiResponse(code = 400, message = "Error input") })
	public Response insertMonomer(String monomerString) throws JDOMException, IOException, MonomerException {
		JsonConverter converter = new JsonConverter();
		try {
			LWMonomer monomer = converter.decodeMonomer(monomerString);
			int id = LibraryManager.getInstance().getMonomerLibrary().insertMonomer(monomer);
			if (id > 0) {
				return Response.status(Response.Status.OK).entity(wrapMonomer(monomer)).build();
			} else {
				return Response.status(Response.Status.BAD_REQUEST).entity(wrapMonomer(monomer)).build();
			}
		} catch (Exception e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@Path("/updateMonomer/{polymertype}/{Symbol}")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Update monomer", httpMethod = "PUT", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Monomer successesfully updated"),
			@ApiResponse(code = 400, message = "Error input") })
	public Response updateMonomer(@PathParam("polymertype") String polymerType, @PathParam("Symbol") String symbol,
			String monomerString) {
		JsonConverter converter = new JsonConverter();
		try {
			LWMonomer monomer = converter.decodeMonomer(monomerString);
			LWMonomer retMonomer = LibraryManager.getInstance().getMonomerLibrary().updateMonomer(polymerType, symbol,
					monomer);
			return Response.status(Response.Status.OK).entity(wrapMonomer(retMonomer)).build();
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