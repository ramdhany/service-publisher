package de.uniluebeck.itm.servicepublisher;

import com.google.common.util.concurrent.AbstractService;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.WebSocketServlet;

import java.net.URI;

public class ServicePublisherWebSocketService extends AbstractService implements ServicePublisherService {

	private final ServicePublisherImpl servicePublisher;

	private final String contextPath;

	private final WebSocketServlet webSocketServlet;

	private ServletContextHandler contextHandler;

	ServicePublisherWebSocketService(final ServicePublisherImpl servicePublisher,
									 final String contextPath,
									 final WebSocketServlet webSocketServlet) {
		this.servicePublisher = servicePublisher;
		this.contextPath = contextPath;
		this.webSocketServlet = webSocketServlet;
	}

	@Override
	protected void doStart() {
		try {

			final ServletHolder servletHolder = new ServletHolder(webSocketServlet);

			contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
			contextHandler.setSessionHandler(new SessionHandler());
			contextHandler.setContextPath(contextPath);
			contextHandler.setClassLoader(Thread.currentThread().getContextClassLoader());
			contextHandler.addServlet(servletHolder, "/*");

			servicePublisher.addHandler(contextHandler);

			contextHandler.start();

			notifyStarted();

		} catch (Exception e) {
			notifyFailed(e);
		}
	}

	@Override
	protected void doStop() {
		try {

			servicePublisher.removeHandler(contextHandler);

			notifyStopped();

		} catch (Exception e) {
			notifyFailed(e);
		}
	}

	private String getAddress(final String contextPath) {
		return "http://localhost:" + servicePublisher.getPort() + (contextPath.startsWith("/") ? contextPath :
				"/" + contextPath);
	}

	@Override
	public URI getURI() {
		return URI.create(getAddress(contextPath));
	}
}
