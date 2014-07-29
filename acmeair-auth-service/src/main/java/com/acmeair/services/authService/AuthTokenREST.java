/*******************************************************************************
 * Copyright (c) 2013 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.acmeair.services.authService;

import java.util.concurrent.atomic.AtomicLong;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Component;

import com.acmeair.entities.CustomerSession;
import com.acmeair.service.CustomerService;
import com.acmeair.servo.Servo;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.annotations.Monitor;
import com.netflix.servo.monitor.Monitors;
import com.sun.jersey.spi.resource.Singleton;

@Path("/authtoken")
@Component
@Singleton
public class AuthTokenREST {

	public AuthTokenREST() {
		Servo.initMetricsPublishing();
		Monitors.registerObject(this);
	}

	@Monitor(name = "CreateToken", type = DataSourceType.COUNTER)
	private final AtomicLong createTokenCount = new AtomicLong(0);

	@Monitor(name = "ValidateToken", type = DataSourceType.COUNTER)
	private final AtomicLong validateTokenCount = new AtomicLong(0);

	@Monitor(name = "ErrorValidateToken", type = DataSourceType.COUNTER)
	private final AtomicLong errorValidateTokenCount = new AtomicLong(0);

	@Monitor(name = "InvalidateToken", type = DataSourceType.COUNTER)
	private final AtomicLong invalidateTokenCount = new AtomicLong(0);

	private final CustomerService customerService = ServiceLocator
			.getService(CustomerService.class);

	// private TransactionService transactionService = null;
	// private boolean initializedTXService = false;

	// private TransactionService getTxService() {
	// if (!this.initializedTXService) {
	// this.initializedTXService = true;
	// transactionService = ServiceLocator.getService(TransactionService.class);
	// }
	// return transactionService;
	// }

	@POST
	@Path("/byuserid/{userid}")
	@Produces("application/json")
	public/* CustomerSession */Response createToken(
			@PathParam("userid") String userid) {
		// setupTransaction();
		CustomerSession cs = customerService.createSession(userid);
		createTokenCount.incrementAndGet();
		return Response.ok(cs).build();
	}

	@GET
	@Path("{tokenid}")
	@Produces("application/json")
	public Response validateToken(@PathParam("tokenid") String tokenid) {
		// setupTransaction();
		CustomerSession cs = customerService.validateSession(tokenid);
		if (cs == null) {
			errorValidateTokenCount.incrementAndGet();
			throw new WebApplicationException(404);
		} else {
			validateTokenCount.incrementAndGet();
			return Response.ok(cs).build();
		}
	}

	@DELETE
	@Path("{tokenid}")
	@Produces("application/json")
	public Response invalidateToken(@PathParam("tokenid") String tokenid) {
		// setupTransaction();
		customerService.invalidateSession(tokenid);
		invalidateTokenCount.incrementAndGet();
		return Response.ok().build();
	}

	// private void setupTransaction() {
	// // The following code is to ensure that OG is always set on the thread
	// try {
	// TransactionService txService = getTxService();
	// if (txService != null) {
	// txService.prepareForTransaction();
	// }
	// }
	// catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
}
