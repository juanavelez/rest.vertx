package com.zandero.rest.test;

import javax.ws.rs.Consumes;

/**
 *
 */
public class ImplementationRest extends AbstractRest {

	@Consumes("text") // override abstract
	@Override
	public String get(String id) {
		return id;
	}
}
