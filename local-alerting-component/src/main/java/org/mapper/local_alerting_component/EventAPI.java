package org.mapper.local_alerting_component;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/event")
public class EventAPI {

	@POST
	@Path("/influxdb")
	@Consumes(MediaType.TEXT_PLAIN)
	public Response processEvent(String body) {
		String[] influxDBLines = body.split("\\r?\\n");
		for (String influxDBLine : influxDBLines) {
			LocalAlerting.consumeInfluxDBEvent(InfluxDBEvent.fromInfluxDBLine(influxDBLine.trim()));
		}
		
		return Response.ok().build();
	}
}
