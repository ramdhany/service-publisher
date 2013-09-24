package de.uniluebeck.itm.servicepublisher.demo;

import com.google.inject.Guice;
import com.google.inject.Module;
import de.uniluebeck.itm.servicepublisher.ServicePublisher;
import de.uniluebeck.itm.servicepublisher.ServicePublisherConfig;
import de.uniluebeck.itm.servicepublisher.ServicePublisherFactory;
import de.uniluebeck.itm.servicepublisher.cxf.ServicePublisherCxfModule;
import de.uniluebeck.itm.util.logging.Logging;

import java.io.File;

public class Demo {

	static {
		Logging.setLoggingDefaults();
	}

	public static void main(String[] args) throws InterruptedException {

		final boolean useCxf = args.length > 0 && "cxf".equalsIgnoreCase(args[0]);
		final int port = 8080;
		final ServicePublisherConfig config = new ServicePublisherConfig(port);
		final Module module = new ServicePublisherCxfModule();
		/*
		final Module module = useCxf ?
				new ServicePublisherCxfModule() :
				new ServicePublisherJettyMetroJerseyModule();
		*/

		final ServicePublisherFactory factory = Guice.createInjector(module).getInstance(ServicePublisherFactory.class);
		final ServicePublisher servicePublisher = factory.create(config);

		final File shiroIni = new File("/Coding/service-publisher/demo/shiro.ini");
		final File shiroSubcontextIni = new File("/Coding/service-publisher/demo/shiro_subcontext.ini");

		servicePublisher.createServletService("/", Demo.class.getResource("/rootcontext").toString(), shiroIni);
		servicePublisher.createServletService("/subcontext", Demo.class.getResource("/subcontext").toString(), shiroSubcontextIni);

		servicePublisher.createJaxRsService("/rest/v1.0", new DemoRestApplication(), null);
		servicePublisher.createJaxRsService("/rest/v2.0", new DemoRestApplication2(), null);

		servicePublisher.createJaxWsService("/soap/v1.0", new DemoSoapService(), null);
		servicePublisher.createJaxWsService("/soap/v2.0", new DemoSoapService2(), null);

		servicePublisher.createWebSocketService("/ws/v1.0", new DemoWebSocketServlet(), null);
		servicePublisher.createWebSocketService("/ws/v2.0", new DemoWebSocketServlet(), null);

		servicePublisher.startAndWait();

		Runtime.getRuntime().addShutdownHook(
				new Thread("Demo-ShutdownThread") {
					@Override
					public void run() {
						servicePublisher.stopAndWait();
					}
				}
		);
	}
}
