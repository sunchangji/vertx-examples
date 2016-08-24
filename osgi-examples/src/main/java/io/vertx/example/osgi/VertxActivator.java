package io.vertx.example.osgi;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.spi.VertxMetricsFactory;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.logging.Logger;

import static io.vertx.example.osgi.TcclSwitch.executeWithTCCLSwitch;

/**
 * A bundle activator registering the Vert.x instance and the event bus as OSGi service.
 */
public class VertxActivator implements BundleActivator {

  private final static Logger LOGGER = Logger.getLogger("VertxPublisher");
  private ServiceRegistration<Vertx> vertxRegistration;
  private ServiceRegistration<EventBus> ebRegistration;

  @Override
  public void start(BundleContext context) throws Exception {
    LOGGER.info("Creating Vert.x instance");

    VertxOptions options = new VertxOptions().setMetricsOptions(new DropwizardMetricsOptions()
        .setJmxEnabled(true)
        .setJmxDomain("vertx")
        .setRegistryName("my-registry")
        .setFactory(VertxSpiHelper.lookup(VertxMetricsFactory.class, context, "dropwizard"))
    );

    Vertx vertx = executeWithTCCLSwitch(() -> Vertx.vertx(options));

    vertxRegistration = context.registerService(Vertx.class, vertx, null);
    LOGGER.info("Vert.x service registered");
    ebRegistration = context.registerService(EventBus.class, vertx.eventBus(), null);
    LOGGER.info("Vert.x Event Bus service registered");
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    if (vertxRegistration != null) {
      vertxRegistration.unregister();
      vertxRegistration = null;
    }
    if (ebRegistration != null) {
      ebRegistration.unregister();
      ebRegistration = null;
    }
  }


}
