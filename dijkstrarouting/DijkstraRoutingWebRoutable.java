package net.floodlightcontroller.dijkstraroutin;

import net.floodlightcontroller.restserver.RestletRoutable;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class DijkstraRoutingWebRoutable implements RestletRoutable {
    /**
     * Create the Restlet router and bind to the proper resources.
     */
    @Override
    public Restlet getRestlet(Context context) {
        Router router = new Router(context);

        router.attach("/{src}/{dst}", DijkstraRoutingResource.class); /* v2.0 advertised API */
        return router;
    }

    /**
     * Set the base path for the Topology
     */
    @Override
    public String basePath() {
        return "/run-algorithm";
    }
}