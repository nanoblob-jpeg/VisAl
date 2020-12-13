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

public class inducedToggleTask extends AbstractTask {

    boolean induced;
    CyNetworkViewManager viewManager;
    CyNetworkManager networkManager;
    boolean aligned;
    boolean unalignedOne;
    boolean unalignedTwo;
    public inducedToggleTask(boolean induced, CyNetworkViewManager viewManager, CyNetworkManager networkManager, boolean aligned, boolean unalignedOne, boolean unalignedTwo){
        this.induced = induced;
        this.viewManager = viewManager;
        this.networkManager = networkManager;
        this.aligned = aligned;
        this.unalignedOne = unalignedOne;
        this.unalignedTwo = unalignedTwo;
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
        Collection<CyNode> nodes = CyTableUtil.getNodesInState(myNet,"selected",true);
        Collection<View<CyEdge>> edgeViews = myView.getEdgeViews();
        Iterator<View<CyEdge>> edgeIterator = edgeViews.iterator();
        while(edgeIterator.hasNext()){
            View<CyEdge> edgeView = edgeIterator.next();
            edgeView.setVisualProperty(BasicVisualLexicon.EDGE_VISIBLE, false);
        }
        if(induced) {
            {
                Collection<View<CyNode>> nodeViews = myView.getNodeViews();
                Iterator<View<CyNode>> nodeViewIterator = nodeViews.iterator();
                while (nodeViewIterator.hasNext()) {
                    View<CyNode> a = nodeViewIterator.next();
                    a.setVisualProperty(BasicVisualLexicon.NODE_VISIBLE, false);
                }
                Iterator<CyNode> nodeIterator = nodes.iterator();
                while (nodeIterator.hasNext()) {
                    CyNode a = nodeIterator.next();
                    myView.getNodeView(a).setVisualProperty(BasicVisualLexicon.NODE_VISIBLE, true);
                }
            }
            edgeIterator = edgeViews.iterator();
            while(edgeIterator.hasNext()){
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
                    if (edgeView.getVisualProperty(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT) == Color.MAGENTA && aligned) {
                        edgeView.setVisualProperty(BasicVisualLexicon.EDGE_VISIBLE, true);
                    } else if (edgeView.getVisualProperty(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT) == Color.RED && unalignedOne) {
                        edgeView.setVisualProperty(BasicVisualLexicon.EDGE_VISIBLE, true);
                    } else if (edgeView.getVisualProperty(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT) == Color.BLUE && unalignedTwo) {
                        edgeView.setVisualProperty(BasicVisualLexicon.EDGE_VISIBLE, true);
                    }
                }
            }
        }else{
            {
                Collection<View<CyNode>> nodeViews = myView.getNodeViews();
                Iterator<View<CyNode>> nodeViewIterator = nodeViews.iterator();
                while (nodeViewIterator.hasNext()) {
                    View<CyNode> a = nodeViewIterator.next();
                    a.setVisualProperty(BasicVisualLexicon.NODE_VISIBLE, true);
                }
            }
            edgeIterator = edgeViews.iterator();
            while(edgeIterator.hasNext()){
                View<CyEdge> edgeView = edgeIterator.next();
                CyEdge edge = edgeView.getModel();
                CyNode n = edge.getSource();
                CyNode m = edge.getTarget();

                Iterator<CyNode> nodeIterator = nodes.iterator();
                while (nodeIterator.hasNext()) {
                    CyNode temp = nodeIterator.next();
                    if (temp.getSUID() == n.getSUID() || temp.getSUID() == m.getSUID()) {
                        if (edgeView.getVisualProperty(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT) == Color.MAGENTA && aligned) {
                            edgeView.setVisualProperty(BasicVisualLexicon.EDGE_VISIBLE, true);
                        } else if (edgeView.getVisualProperty(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT) == Color.RED && unalignedOne) {
                            edgeView.setVisualProperty(BasicVisualLexicon.EDGE_VISIBLE, true);
                        } else if (edgeView.getVisualProperty(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT) == Color.BLUE && unalignedTwo) {
                            edgeView.setVisualProperty(BasicVisualLexicon.EDGE_VISIBLE, true);
                        }
                        break;
                    }
                }
            }
        }
    }
}
