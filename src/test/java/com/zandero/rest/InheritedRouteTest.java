package com.zandero.rest;

import com.zandero.rest.test.ImplementationRest;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 */
@RunWith(VertxUnitRunner.class)
public class InheritedRouteTest extends VertxTest {

	@Before
	public void start(TestContext context) {

		super.before(context);

		Router router = RestRouter.register(vertx, ImplementationRest.class);

		vertx.createHttpServer()
		     .requestHandler(router::accept)
		     .listen(PORT);
	}

	@Test
	public void echoTest(TestContext context) {

		// call and check response
		final Async async = context.async();

		client.getNow("/abstract/echo?name=test", response -> {

			context.assertEquals(200, response.statusCode());

			response.handler(body -> {
				context.assertEquals("test", body.toString()); // JsonExceptionWriter
				async.complete();
			});
		});
	}

	@Test
	public void getTest(TestContext context) {

		// call and check response
		final Async async = context.async();

		client.getNow("/abstract/get/test", response -> {

			context.assertEquals(200, response.statusCode());

			response.handler(body -> {
				context.assertEquals("test", body.toString()); // JsonExceptionWriter
				async.complete();
			});
		});
	}
}
