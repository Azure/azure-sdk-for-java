package com.microsoft.azure.servicebus;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Sasl;
import org.apache.qpid.proton.engine.Ssl;
import org.apache.qpid.proton.engine.SslDomain;
import org.apache.qpid.proton.engine.Transport;
import org.apache.qpid.proton.reactor.Handshaker;

/**
 * ServiceBus <-> ProtonReactor interaction handles all
 * amqp_connection/transport related events from reactor
 */
public final class ConnectionHandler extends BaseHandler {

	private static final Logger TRACE_LOGGER = Logger.getLogger("eventhub.trace");

	private final int port = 5671;
	private final String hostname;
	private final String username;
	private final String password;

	// TODO: sasl mechanism should be passed - since we currently only support
	// sasKey/Value - we are directly passing username/pass
	ConnectionHandler(final String hostname, final String username, final String password) {
		add(new Handshaker());
		this.hostname = hostname;
		this.username = username;
		this.password = password;
	}

	// TODO: REMOVE after hooking proper diagnostics
	@Override
	public void onUnhandled(Event event) {
		if (TRACE_LOGGER.isLoggable(Level.FINE)) {
			TRACE_LOGGER.log(Level.FINE,
					"Connection.onUnhandled: name[" + event.getConnection().getHostname() + "] : event[" + event + "]");
		}
	}

	@Override
	public void onConnectionBound(Event event) {
		Connection conn = event.getConnection();
		Transport trans = conn.getTransport();

		SslDomain domain = makeDomain(SslDomain.Mode.CLIENT);
		Ssl ssl = trans.ssl(domain);

		Sasl sasl = trans.sasl();
		sasl.plain(this.username, this.password);
	}

	@Override
	public void onTransportError(Event event) {
		ErrorCondition condition = event.getTransport().getCondition();
		if (condition != null) {
			System.err.println("Error: " + condition.getDescription());
		} else {
			System.err.println("Error (no description returned).");
		}
	}

	@Override
	public void onConnectionInit(Event event) {
		Connection conn = event.getConnection();
		event.getContext();
		conn.setHostname(hostname + ":" + port);
		conn.setContainer("Container1");
		conn.open();
	}

	private static SslDomain makeDomain(SslDomain.Mode mode) {
		SslDomain domain = Proton.sslDomain();
		domain.init(mode);

		domain.setPeerAuthentication(SslDomain.VerifyMode.ANONYMOUS_PEER);
		return domain;
	}
}
