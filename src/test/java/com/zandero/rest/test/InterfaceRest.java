package com.zandero.rest.test;

import javax.ws.rs.*;

/**
 *
 */
public interface InterfaceRest {

	@GET
	@Consumes("application/json")
	@Produces("application/json")
	@Path("echo")
	public String echo(@QueryParam("name") String name);
}
