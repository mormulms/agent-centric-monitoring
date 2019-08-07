package org.mapper.local_alerting_component;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("esper")
public class EsperAPI {

	@GET
	@Path("statement")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getAllStatements() {
		System.out.println("API ACCESS - getALLStatements()");
		
		StringBuilder stringBuilder = new StringBuilder();

		for (String statement : LocalAlerting.getAllStatementNames()) {
			stringBuilder.append(statement);
			stringBuilder.append('\n');
		}
		
		return Response.ok().entity(stringBuilder.toString()).build();
	}

	@DELETE
	@Path("statement/{statementName}")
	public Response removeStatementByName(@PathParam("statementName") String statementName) {

		LocalAlerting.removeInfluxDBStatementByName(statementName);

		return Response.ok().entity(statementName + " deleted").build();
	}

	@POST
	@Path("statement/{statementName}")
	@Consumes(MediaType.TEXT_PLAIN)
	public Response addStatementWithName(@PathParam("statementName") String statementName, String statement) {

		LocalAlerting.addInfluxDBStatement(statementName, statement);

		return Response.ok().entity(statementName + " added").build();
	}

}
