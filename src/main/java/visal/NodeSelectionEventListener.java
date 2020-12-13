package visal;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.events.SelectedNodesAndEdgesEvent;
import org.cytoscape.model.events.SelectedNodesAndEdgesListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

import java.awt.*;
import java.util.Collection;
import java.util.Iterator;

public class NodeSelectionEventListener implements SelectedNodesAndEdgesListener {
    CyNetworkViewManager viewManager;
    int stall;
    changeToggler toggler;
    public NodeSelectionEventListener(CyNetworkViewManager cnvm){
        viewManager = cnvm;
        stall = 2;
    }
    @Override
    public void handleEvent(SelectedNodesAndEdgesEvent e){
        if(e.isCurrentNetwork() && e.nodesChanged() && stall == 0) {
            if(!toggler.getSelected()){
                toggler.toggleSelected();
            }
            CyNetwork myNet = e.getNetwork();
            Collection<CyNetworkView> views = viewManager.getNetworkViews(myNet);
            CyNetworkView myView = null;
            if(views.size() != 0)
                myView = views.iterator().next();
            if(myView == null){
                return;
            }
            Collection<View<CyEdge>> edgeViews = myView.getEdgeViews();
            Iterator<View<CyEdge>> edgeIterator = edgeViews.iterator();
            Collection<CyNode> selectedNodes = e.getSelectedNodes();
            if(selectedNodes.size() == 0){
                if(toggler.induced)
                    toggler.toggleInduced();
                if(toggler.hasHiddenNodes){
                    Collection<View<CyNode>> nodeViews = myView.getNodeViews();
                    Iterator<View<CyNode>> nodeViewIterator = nodeViews.iterator();
                    while(nodeViewIterator.hasNext()){
                        View<CyNode> a = nodeViewIterator.next();
                        a.setVisualProperty(BasicVisualLexicon.NODE_VISIBLE, true);
                    }
                    toggler.hasHiddenNodes = false;
                }
                if(toggler.getSelected())
                    toggler.toggleSelected();
                edgeIterator = edgeViews.iterator();
                while (edgeIterator.hasNext()) {
                    View<CyEdge> edgeView = edgeIterator.next();
                    if (edgeView.getVisualProperty(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT) == Color.MAGENTA && toggler.displayAligned) {
                        edgeView.setVisualProperty(BasicVisualLexicon.EDGE_VISIBLE, true);
                    } else if (edgeView.getVisualProperty(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT) == Color.RED && toggler.displayUnalignedOne) {
                        edgeView.setVisualProperty(BasicVisualLexicon.EDGE_VISIBLE, true);
                    } else if (edgeView.getVisualProperty(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT) == Color.BLUE && toggler.displayUnalignedTwo) {
                        edgeView.setVisualProperty(BasicVisualLexicon.EDGE_VISIBLE, true);
                    }
                }
            }else {
                while(edgeIterator.hasNext()){
                    View<CyEdge> edgeView = edgeIterator.next();
                    edgeView.setVisualProperty(BasicVisualLexicon.EDGE_VISIBLE, false);
                }
                if (!toggler.getInduced()) {
                    if(toggler.hasHiddenNodes){
                        Collection<View<CyNode>> nodeViews = myView.getNodeViews();
                        Iterator<View<CyNode>> nodeViewIterator = nodeViews.iterator();
                        while(nodeViewIterator.hasNext()){
                            View<CyNode> a = nodeViewIterator.next();
                            a.setVisualProperty(BasicVisualLexicon.NODE_VISIBLE, true);
                        }
                        toggler.hasHiddenNodes = false;
                    }
                    edgeIterator = edgeViews.iterator();
                    while (edgeIterator.hasNext()) {
                        View<CyEdge> edgeView = edgeIterator.next();
                        CyEdge edge = edgeView.getModel();
                        CyNode n = edge.getSource();
                        CyNode m = edge.getTarget();
                        Iterator<CyNode> nodeIterator = selectedNodes.iterator();
                        while (nodeIterator.hasNext()) {
                            CyNode temp = nodeIterator.next();
                            if (temp.getSUID() == n.getSUID() || temp.getSUID() == m.getSUID()) {
                                if (edgeView.getVisualProperty(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT) == Color.MAGENTA && toggler.displayAligned) {
                                    edgeView.setVisualProperty(BasicVisualLexicon.EDGE_VISIBLE, true);
                                } else if (edgeView.getVisualProperty(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT) == Color.RED && toggler.displayUnalignedOne) {
                                    edgeView.setVisualProperty(BasicVisualLexicon.EDGE_VISIBLE, true);
                                } else if (edgeView.getVisualProperty(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT) == Color.BLUE && toggler.displayUnalignedTwo) {
                                    edgeView.setVisualProperty(BasicVisualLexicon.EDGE_VISIBLE, true);
                                }
                                break;
                            }
                        }
                    }
                } else {
                    toggler.hasHiddenNodes = true;
                    edgeIterator = edgeViews.iterator();
                    Collection<View<CyNode>> nodeViews = myView.getNodeViews();
                    Iterator<View<CyNode>> nodeViewIterator = nodeViews.iterator();
                    while(nodeViewIterator.hasNext()){
                        View<CyNode> a = nodeViewIterator.next();
                        a.setVisualProperty(BasicVisualLexicon.NODE_VISIBLE, false);
                    }
                    Iterator<CyNode> nodeIterator = selectedNodes.iterator();
                    while(nodeIterator.hasNext()){
                        CyNode a = nodeIterator.next();
                        myView.getNodeView(a).setVisualProperty(BasicVisualLexicon.NODE_VISIBLE, true);
                    }
                    while (edgeIterator.hasNext()) {
                        View<CyEdge> edgeView = edgeIterator.next();
                        CyEdge edge = edgeView.getModel();
                        CyNode n = edge.getSource();
                        CyNode m = edge.getTarget();
                        boolean hasn = false;
                        boolean hasm = false;
                        nodeIterator = selectedNodes.iterator();
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
                            if (edgeView.getVisualProperty(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT) == Color.MAGENTA && toggler.displayAligned) {
                                edgeView.setVisualProperty(BasicVisualLexicon.EDGE_VISIBLE, true);
                            } else if (edgeView.getVisualProperty(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT) == Color.RED && toggler.displayUnalignedOne) {
                                edgeView.setVisualProperty(BasicVisualLexicon.EDGE_VISIBLE, true);
                            } else if (edgeView.getVisualProperty(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT) == Color.BLUE && toggler.displayUnalignedTwo) {
                                edgeView.setVisualProperty(BasicVisualLexicon.EDGE_VISIBLE, true);
                            }
                        }
                    }
                }
            }
        }else{
            if(stall > 0)
                stall--;
        }
    }

    public void addChangeToggler(changeToggler t){
        toggler = t;
    }
}
