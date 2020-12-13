package visal;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.events.SelectedNodesAndEdgesListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.osgi.framework.BundleContext;
import org.cytoscape.model.*;
import org.cytoscape.task.NetworkTaskFactory;

import java.awt.*;
import java.util.Properties;

import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

public class CyActivator extends AbstractCyActivator{
    public CyActivator(){
        super();
    }

    @Override
    public void start(BundleContext bc){
        CyNetworkFactory networkFactory = getService(bc, CyNetworkFactory.class);
        CyNetworkManager networkManager = getService(bc, CyNetworkManager.class);
        CyNetworkViewManager viewManager = getService(bc, CyNetworkViewManager.class);
        CyNetworkViewFactory viewFactory = getService(bc, CyNetworkViewFactory.class);
        CyEventHelper eventHelper = getService(bc, CyEventHelper.class);


        NodeSelectionEventListener nsel = new NodeSelectionEventListener(viewManager);
        registerService(bc, nsel, SelectedNodesAndEdgesListener.class, new Properties());

        NetworkTaskFactory myTaskFactory = new VisAlTaskFactory(networkFactory, networkManager, viewManager, viewFactory, eventHelper);
        Properties networkViewMenuButtonProps = setProps(
                TITLE, "Draw Alignment From Files",
                PREFERRED_MENU, "Apps.VisAl");
        registerService(bc, myTaskFactory, NetworkTaskFactory.class, networkViewMenuButtonProps);

        ToggleEdgeTaskFactory displayAligned = new ToggleEdgeTaskFactory(Color.MAGENTA, networkManager, viewManager, 0);
        Properties displayAlignedProps = setProps(
                TITLE, "Toggle Aligned Edges",
                PREFERRED_MENU, "Apps.VisAl");
        registerService(bc, displayAligned, NetworkTaskFactory.class, displayAlignedProps);

        ToggleEdgeTaskFactory displayUnalignedGraphOne = new ToggleEdgeTaskFactory(Color.RED, networkManager, viewManager, 1);
        Properties displayUnalignedOneProps = setProps(
                TITLE, "Toggle Graph 1 Unaligned Edges",
                PREFERRED_MENU, "Apps.VisAl");
        registerService(bc, displayUnalignedGraphOne, NetworkTaskFactory.class, displayUnalignedOneProps);

        ToggleEdgeTaskFactory displayUnalignedGraphTwo = new ToggleEdgeTaskFactory(Color.BLUE, networkManager, viewManager, 2);
        Properties displayUnalignedTwoProps = setProps(
                TITLE, "Toggle Graph 2 Unaligned Edges",
                PREFERRED_MENU, "Apps.VisAl");
        registerService(bc, displayUnalignedGraphTwo, NetworkTaskFactory.class, displayUnalignedTwoProps);

        inducedToggleTaskFactory toggleInducedSubgraph = new inducedToggleTaskFactory(viewManager, networkManager);
        Properties toggleInducedSubgraphProps = setProps(
                TITLE, "Toggle Induced SubGraph",
                PREFERRED_MENU, "Apps.VisAl");
        registerService(bc, toggleInducedSubgraph, NetworkTaskFactory.class, toggleInducedSubgraphProps);

        changeToggler toggler = new changeToggler(nsel, displayAligned, displayUnalignedGraphOne, displayUnalignedGraphTwo);
        displayAligned.addChangeToggler(toggler);
        displayUnalignedGraphOne.addChangeToggler(toggler);
        displayUnalignedGraphTwo.addChangeToggler(toggler);
        nsel.addChangeToggler(toggler);
        toggleInducedSubgraph.addChangeToggler(toggler);
    }

    private static Properties setProps(String... vals){
        final Properties props = new Properties();
        for(int i = 0; i < vals.length; i += 2){
            props.put(vals[i], vals[i+1]);
        }
        return props;
    }

}
