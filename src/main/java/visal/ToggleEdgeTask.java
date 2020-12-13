package visal;

import org.cytoscape.model.*;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import java.awt.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class ToggleEdgeTask<T> extends AbstractTask {
    CyNetworkManager networkManager;
    CyNetworkViewManager viewManager;
    Color compareVal;
    boolean display;
    boolean induced;
    public ToggleEdgeTask(boolean display, Color compareVal, CyNetworkManager cNM, CyNetworkViewManager cNVM, boolean induced){
        this.compareVal = compareVal;
        networkManager = cNM;
        viewManager = cNVM;
        this.display = display;
        this.induced = induced;
    }

    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {
        Set<CyNetwork> networks = networkManager.getNetworkSet();
        CyNetwork myNet = null;
        for(CyNetwork net : networks){
            if(net.getRow(net).get(CyNetwork.NAME, String.class).equals("Alignment Visualization")){
                myNet = net;
                break;
            }
        }
        if(myNet == null){
            return;
        }
        final Collection<CyNetworkView> views = viewManager.getNetworkViews(myNet);
        CyNetworkView myView = null;
        if(views.size() != 0)
            myView = views.iterator().next();
        if(myView == null){
            return;
        }
        Collection<View<CyEdge>> edgeViews = myView.getEdgeViews();
        Iterator<View<CyEdge>> edgeIterator = edgeViews.iterator();
        Collection<CyNode> nodes = CyTableUtil.getNodesInState(myNet,"selected",true);
        while(edgeIterator.hasNext()){
            View<CyEdge> edgeView = edgeIterator.next();
            if(edgeView.getVisualProperty(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT) == compareVal){
                edgeView.setVisualProperty(BasicVisualLexicon.EDGE_VISIBLE, false);
            }
        }
        edgeIterator = edgeViews.iterator();
        if(nodes.size() == 0) {
            while(edgeIterator.hasNext()){
                View<CyEdge> edgeView = edgeIterator.next();
                if(edgeView.getVisualProperty(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT) == compareVal){
                    edgeView.setVisualProperty(BasicVisualLexicon.EDGE_VISIBLE, display);
                }
            }
        }else{
            if (!induced) {
                edgeIterator = edgeViews.iterator();
                while (edgeIterator.hasNext()) {
                    View<CyEdge> edgeView = edgeIterator.next();
                    CyEdge edge = edgeView.getModel();
                    CyNode n = edge.getSource();
                    CyNode m = edge.getTarget();
                    Iterator<CyNode> nodeIterator = nodes.iterator();
                    while (nodeIterator.hasNext()) {
                        CyNode temp = nodeIterator.next();
                        if (temp.getSUID() == n.getSUID() || temp.getSUID() == m.getSUID()) {
                            if (edgeView.getVisualProperty(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT) == compareVal){
                                edgeView.setVisualProperty(BasicVisualLexicon.EDGE_VISIBLE, display);
                            }
                            break;
                        }
                    }
                }
            } else {
                edgeIterator = edgeViews.iterator();
                while (edgeIterator.hasNext()) {
                    View<CyEdge> edgeView = edgeIterator.next();
                    CyEdge edge = edgeView.getModel();
                    CyNode n = edge.getSource();
                    CyNode m = edge.getTarget();
                    boolean hasn = false;
                    boolean hasm = false;
                    Iterator<CyNode> nodeIterator = nodes.iterator();
                    while (nodeIterator.hasNext()) {
                        CyNode temp = nodeIterator.next();
                        if (temp.getSUID() == n.getSUID()) {
                            hasn = true;
                            if (hasm)
                                break;
                        }
                        if (temp.getSUID() == m.getSUID()) {
                            hasm = true;
                            if (hasn)
                                break;
                        }
                    }
                    if (hasn && hasm) {
                        if (edgeView.getVisualProperty(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT) == compareVal){
                            edgeView.setVisualProperty(BasicVisualLexicon.EDGE_VISIBLE, display);
                        }
                    }
                }
            }
        }
        myView.updateView();
    }
}
