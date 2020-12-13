package visal;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskIterator;

import java.awt.event.ActionEvent;

public class inducedToggleTaskFactory implements NetworkTaskFactory {

    changeToggler toggler;
    CyNetworkViewManager viewManager;
    CyNetworkManager networkManager;
    public inducedToggleTaskFactory(CyNetworkViewManager viewManager, CyNetworkManager networkManager){
        this.viewManager = viewManager;
        this.networkManager = networkManager;
    }

    @Override
    public TaskIterator createTaskIterator(CyNetwork network) {
        toggler.toggleInduced();
        if(toggler.induced){
            toggler.hasHiddenNodes = true;
        }else
            toggler.hasHiddenNodes = false;
        if(!toggler.getSelected()){
            return new TaskIterator();
        }else{
            return new TaskIterator(new inducedToggleTask(toggler.getInduced(), viewManager, networkManager, toggler.displayAligned, toggler.displayUnalignedOne, toggler.displayUnalignedTwo));
        }
    }

    @Override
    public boolean isReady(CyNetwork network) {
        return true;
    }

    public void addChangeToggler(changeToggler t){
        toggler = t;
    }
}
