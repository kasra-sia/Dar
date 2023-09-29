package net.floodlightcontroller.dijkstraroutin;

import net.floodlightcontroller.staticentry.web.ListStaticEntriesResource;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DijkstraRoutingResource extends ServerResource {
    protected static Logger log = LoggerFactory.getLogger(ListStaticEntriesResource.class);
    private DijkstraRouting dijkstraRouting ;
    @Get("json")
    public Object parseHttpReq() {
        this.dijkstraRouting = DijkstraRouting.getInstance();

        String src = (String) getRequestAttributes().get("src");
        String dst = (String) getRequestAttributes().get("dst");
//        path2 = dijkstraRouting.
        ;
        return dijkstraRouting.selectBestPath("A"+src+" "+dst);
    }

}
