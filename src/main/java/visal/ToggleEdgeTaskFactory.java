package visal;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.TaskIterator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class ToggleEdgeTaskFactory<T> implements NetworkTaskFactory {
    CyNetworkManager networkManager;
    CyNetworkViewManager viewManager;
    Color compareVal;
    changeToggler toggler;
    int index;
    public ToggleEdgeTaskFactory(Color compareVal, CyNetworkManager cNM, CyNetworkViewManager cNVM, int i){
        this.compareVal = compareVal;
        networkManager = cNM;
        viewManager = cNVM;
        index = i;
    }

    @Override
    public TaskIterator createTaskIterator(CyNetwork network) {
        if(index == 0) {
            toggler.displayAligned = !toggler.displayAligned;
            return new TaskIterator(new ToggleEdgeTask(toggler.displayAligned, compareVal, networkManager, viewManager, toggler.getInduced()));
        }else if(index == 1){
            toggler.displayUnalignedOne = !toggler.displayUnalignedOne;
            return new TaskIterator(new ToggleEdgeTask(toggler.displayUnalignedOne, compareVal, networkManager, viewManager, toggler.getInduced()));
        }else{
            toggler.displayUnalignedTwo = !toggler.displayUnalignedTwo;
            return new TaskIterator(new ToggleEdgeTask(toggler.displayUnalignedTwo, compareVal, networkManager, viewManager, toggler.getInduced()));
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
