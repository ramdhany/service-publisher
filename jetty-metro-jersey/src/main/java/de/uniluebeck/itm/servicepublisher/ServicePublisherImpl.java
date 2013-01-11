package de.uniluebeck.itm.servicepublisher;

import com.google.common.util.concurrent.Service;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.jasper.servlet.JspServlet;
import org.eclipse.jetty.http.spi.JettyHttpServerProvider;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.WebSocketServlet;

import javax.ws.rs.core.Application;

@Singleton
class ServicePublisherImpl extends ServicePublisherBase {

	private static final String HTTP_SERVER_PROVIDER = "com.sun.net.httpserver.HttpServerProvider";

	private Server server;

	private ServletContextHandler rootContext;

	@Inject
	public ServicePublisherImpl(final ServicePublisherConfig config) {

		super(config);

		server = new Server(config.getPort());

		// set up JAX-WS support for Jetty
		System.setProperty(HTTP_SERVER_PROVIDER, JettyHttpServerProvider.class.getCanonicalName());
		JettyHttpServerProvider.setServer(server);

		rootContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
		rootContext.setSessionHandler(new SessionHandler());
		rootContext.setContextPath("/");
		rootContext.setResourceBase(config.getResourceBase());
		rootContext.setClassLoader(Thread.currentThread().getContextClassLoader());
		rootContext.addServlet(DefaultServlet.class, "/");
		rootContext.addServlet(JspServlet.class, "*.jsp").setInitParameter("classpath", rootContext.getClassPath());

		final ContextHandlerCollection contextHandlerCollection = new ContextHandlerCollection();
		contextHandlerCollection.addHandler(rootContext);

		server.setHandler(contextHandlerCollection);
	}

	protected void doStartInternal() throws Exception {
		server.start();
	}

	protected void doStopInternal() throws Exception {
		/*
		for (Service service : newArrayList(servicesPublished)) {
			try {
				service.stopAndWait();
			} catch (Exception e) {
				log.warn("Exception while shutting down service {}", service, e.getMessage());
			}
		}
		*/
		server.stop();
	}

	int getPort() {
		return config.getPort();
	}

	protected Service createJaxWsServiceInternal(final String contextPath, final Object endpointImpl) {
		return new ServicePublisherJaxWsService(this, contextPath, endpointImpl);
	}

	protected Service createJaxRsServiceInternal(final String contextPath,
												 final Class<? extends Application> applicationClass) {
		return new ServicePublisherJaxRsService(rootContext, contextPath, applicationClass);
	}

	protected Service createWebSocketServiceInternal(final String contextPath,
													 final WebSocketServlet webSocketServlet) {
		return new ServicePublisherWebSocketService(rootContext, contextPath, webSocketServlet);
	}
}