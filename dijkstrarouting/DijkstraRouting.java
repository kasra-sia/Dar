package net.floodlightcontroller.dijkstraroutin;

import net.floodlightcontroller.core.*;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.types.NodePortTuple;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryListener;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryService;
import net.floodlightcontroller.linkdiscovery.Link;
import net.floodlightcontroller.linkdiscovery.internal.LinkInfo;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.routing.IRoutingDecision;
import net.floodlightcontroller.routing.IRoutingDecisionChangedListener;
import net.floodlightcontroller.routing.IRoutingService;
import net.floodlightcontroller.routing.Path;
import net.floodlightcontroller.topology.ITopologyListener;
import net.floodlightcontroller.topology.ITopologyService;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.protocol.OFType;

import java.util.*;

import org.projectfloodlight.openflow.protocol.instruction.OFInstruction;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.Masked;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U64;
import org.slf4j.Logger;

public class DijkstraRouting implements IOFMessageListener, IFloodlightModule,
        ILinkDiscoveryListener, IOFSwitchListener
        , ITopologyListener, IRoutingDecisionChangedListener {
    protected IFloodlightProviderService floodlightProvider;
    protected IRestApiService restApiService;
    protected IOFSwitchService switchService;
    protected ITopologyService topologyService;
    protected IRoutingService routingService;
    protected ILinkDiscoveryService linkDiscoveryService;
    protected List<LDUpdate> ldUpdates = new LinkedList<>();
    int counter = 0;
    protected String src = "";
    protected String dst = "";
    String test = " bedoone taghir";
    protected static Logger logger;
    protected static DijkstraRouting dijkstraRouting;

    public DijkstraRouting() {
        dijkstraRouting = this;
    }

    public static DijkstraRouting getInstance() {
        return dijkstraRouting;
    }

    @Override
    public String getName() {
        return DijkstraRouting.class.getSimpleName();
    }

    @Override
    public boolean isCallbackOrderingPrereq(OFType type, String name) {
        return false;
    }

    @Override
    public boolean isCallbackOrderingPostreq(OFType type, String name) {
        return false;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        return null;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        return null;
    }


    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IFloodlightProviderService.class);
        l.add(IRestApiService.class);
        l.add(ILinkDiscoveryService.class);
        l.add(IOFSwitchService.class);
        l.add(ITopologyService.class);
        l.add(IRoutingService.class);
        l.add(IRoutingService.class);
        return l;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
        restApiService = context.getServiceImpl(IRestApiService.class);
        switchService = context.getServiceImpl(IOFSwitchService.class);
        linkDiscoveryService = context.getServiceImpl(ILinkDiscoveryService.class);
        routingService = context.getServiceImpl(IRoutingService.class);
        topologyService = context.getServiceImpl(ITopologyService.class);
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
        restApiService.addRestletRoutable(new DijkstraRoutingWebRoutable());
        switchService.addOFSwitchListener(this);
        linkDiscoveryService.addListener(this);
        routingService.addRoutingDecisionChangedListener(this);
        topologyService.addListener(this);
    }

    @Override
    public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
        return Command.CONTINUE;
    }

    public String selectBestPath(String message) {
        Path path = null;
        if (counter<2) {
            counter++;
            System.out.println(counter);
            setLinkWeights();
        }

        if (message.charAt(0) == 'A') {
            int i = 1;
            while (message.charAt(i) != ' ') {
                i++;
            }
            src = message.substring(1, i);
            dst = message.substring(i + 1);

            path = findShortestPath(findDpId(src), findDpId(dst));

        }
        assert path != null;
        return path.getLatency().getValue() + "hahaha   "+path.toString();

//        return linkDiscoveryService.getLinks().toString();
    }

    public DatapathId findDpId(String id) {
        return DatapathId.of(id);
    }

    // Other overridden methods and additional functionality


    //    -----------------------------------------------------------------
    private Path findShortestPath(DatapathId src, DatapathId dst) {

        Path shortestPath = routingService.getPath(src, dst);
        return shortestPath;
    }

    //------------------------------------------------------------------------
    public void setLinkWeights() {

        Map<Link, LinkInfo> links = linkDiscoveryService.getLinks();
        for (Map.Entry<Link, LinkInfo> entry : links.entrySet()) {
            entry.getKey().setLatency(getRandomWeight());
            ldUpdates.add(new LDUpdate(entry.getKey().getSrc(),entry.getKey().getSrcPort(),
                    entry.getKey().getDst(),entry.getKey().getDstPort(),
                    entry.getKey().getLatency(),entry.getValue().getLinkType(),UpdateOperation.LINK_UPDATED));
            setBidirectionalWeight(entry.getKey(), getRandomWeight());
        }
//        this.linkDiscoveryUpdate(ldUpdates);
    }

    private void setBidirectionalWeight(Link link, U64 weight) {
        // Set the weight for both directions of the link
        for (Link l : linkDiscoveryService.getLinks().keySet()) {
            if (l.getSrc().equals(link.getDst())
                    && l.getDst().equals(link.getSrc())) {
                l.setLatency(weight);
                ldUpdates.add(new LDUpdate(l.getSrc(),l.getSrcPort(),
                        l.getDst(),l.getDstPort(),
                        l.getLatency(),linkDiscoveryService.getLinkInfo(l).getLinkType(),UpdateOperation.LINK_UPDATED));
            }
        }
    }

    private U64 getRandomWeight() {
        Random random = new Random();
        int weight = random.nextInt(10) + 1;
        return U64.of(weight);
    }


//    ______________________________________________________________________________


    @Override
    public void linkDiscoveryUpdate(List<LDUpdate> updateList) {
        for (LDUpdate ldUpdate:updateList) {
            System.out.println( ldUpdate.getOperation().toString());

        }
    }


//--------------------------------------------------------------------------------------


    //----------------------------------------------------------------------------------------
    @Override
    public void switchAdded(DatapathId switchId) {

    }


    @Override
    public void switchRemoved(DatapathId switchId) {

    }

    @Override
    public void switchActivated(DatapathId switchId) {

    }

    @Override
    public void switchPortChanged(DatapathId switchId, OFPortDesc port, PortChangeType type) {

    }

    @Override
    public void switchChanged(DatapathId switchId) {

    }

    @Override
    public void switchDeactivated(DatapathId switchId) {

    }

    //------------------------------------------------------------------------
    @Override
    public void topologyChanged(List<LDUpdate> linkUpdates) {
    }

    @Override
    public void routingDecisionChanged(Iterable<Masked<U64>> changedDecisions) {

    }
}
