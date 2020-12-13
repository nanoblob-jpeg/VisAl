package visal;

import org.cytoscape.event.CyEvent;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskIterator;
import org.osgi.framework.BundleContext;

public class VisAlTaskFactory implements NetworkTaskFactory {
    CyNetworkFactory cyNetFac;
    CyNetworkManager cyNetMan;
    CyNetworkViewManager cyNetVMan;
    CyNetworkViewFactory cyNetVFac;
    CyEventHelper eventHelper;
    VisAlTaskFactory(CyNetworkFactory cNF, CyNetworkManager cNM, CyNetworkViewManager cNVM, CyNetworkViewFactory cNVF, CyEventHelper cEH){
        cyNetFac = cNF;
        cyNetMan = cNM;
        cyNetVMan = cNVM;
        cyNetVFac = cNVF;
        eventHelper = cEH;
    }
    @Override
    public TaskIterator createTaskIterator(CyNetwork network) {
        /*
        * This is run when ever the menu button is pressed for the app
         */
        UserTextInput textInput = new UserTextInput();
        String graphOnePath = textInput.getUserInput("Path to Graph 1 File");
        String graphTwoPath = textInput.getUserInput("Path to Graph 2 File");
        String alignPath = textInput.getUserInput("Path to .align File");
        return new TaskIterator(new VisAlTask(cyNetFac, cyNetMan, cyNetVMan, cyNetVFac, eventHelper, graphOnePath, graphTwoPath, alignPath));
    }

    @Override
    public boolean isReady(CyNetwork network) {
        return true;
    }
}
