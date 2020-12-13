/*
change this to just a class, not a task
make a task that gets the paths and returns it to the CyActivator
 */

package visal;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.*;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

public class VisAlTask extends AbstractTask {
    CyNetworkFactory networkFactory;
    CyNetworkManager networkManager;
    CyNetworkViewManager networkViewManager;
    CyNetworkViewFactory networkViewFactory;
    CyEventHelper eventHelper;
    String graphTwoPath;
    String graphOnePath;
    String alignPath;
    public VisAlTask(CyNetworkFactory cNF, CyNetworkManager cNM, CyNetworkViewManager cNVM, CyNetworkViewFactory cNVF, CyEventHelper cEH, String graphOnePath, String graphTwoPath, String alignPath){
        networkFactory = cNF;
        networkManager = cNM;
        networkViewManager = cNVM;
        networkViewFactory = cNVF;
        eventHelper = cEH;
        this.graphOnePath = graphOnePath;
        this.graphTwoPath = graphTwoPath;
        this.alignPath = alignPath;
    }

    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {
        /*
        graphs are all zero indexed
        this task only creates graphs, all data needed is also encoded inside of the CyNetwork created
         */
        /*
        TODO
        for some reason, there are small little components not connected to the large mass
        this is a bug i think, even though the numbers check out
         */

        HashMap<String, Integer> converterGraphOne = new HashMap<String, Integer>();
        HashMap<Integer, String> reverseConverterGraphOne = new HashMap<Integer, String>();
        //edges in here twice so nondirected
        HashMap<Integer, ArrayList<Integer>> adjMatrixGraphOne = new HashMap<Integer, ArrayList<Integer>>();
        try{
            String fileExtension = graphOnePath.substring(graphOnePath.length() - 3);
            if(fileExtension.compareTo(".gw") == 0){
                readGWFile(graphOnePath, converterGraphOne, reverseConverterGraphOne,adjMatrixGraphOne);
            }else if(fileExtension.compareTo(".el") == 0){
                readELFile(graphOnePath, converterGraphOne, reverseConverterGraphOne,adjMatrixGraphOne);
            }else{
                throw new Exception();
            }
        }catch(Exception e){
            taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Error occurred while reading a file");
            return;
        }


        HashMap<String, Integer> converterGraphTwo = new HashMap<String, Integer>();
        HashMap<Integer, String> reverseConverterGraphTwo = new HashMap<Integer, String>();
        //edges in here twice so that it is nondirected
        HashMap<Integer, ArrayList<Integer>> adjMatrixGraphTwo = new HashMap<Integer, ArrayList<Integer>>();
        try{
            String fileExtension = graphTwoPath.substring(graphTwoPath.length() - 3);
            if(fileExtension.compareTo(".gw") == 0){
                readGWFile(graphTwoPath, converterGraphTwo, reverseConverterGraphTwo,adjMatrixGraphTwo);
            }else if(fileExtension.compareTo(".el") == 0){
                readELFile(graphTwoPath, converterGraphTwo, reverseConverterGraphTwo,adjMatrixGraphTwo);
            }else{
                throw new Exception();
            }
        }catch(Exception e){
            taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Error occurred while reading a file");
            return;
        }


        //maps graph two nodes to graph one nodes
        HashMap<Integer, Integer> comparisonTableForAlign = new HashMap<Integer, Integer>();
        try {
            readAlignFile(alignPath, converterGraphOne, converterGraphTwo, comparisonTableForAlign);
        }catch(Exception e){
            taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Error occurred while reading a file");
            return;
        }
        //expressed in terms of the graph 1 nodes
        //all edges only in here once, since we only need on copy for adding to network
        HashMap<Integer, ArrayList<Integer>> alignedEdges = new HashMap<Integer, ArrayList<Integer>>();
        for(Map.Entry<Integer, ArrayList<Integer>> entry : adjMatrixGraphTwo.entrySet()){
            int key = entry.getKey();
            ArrayList<Integer> edges = entry.getValue();
            int graphOneKey = comparisonTableForAlign.get(key);
            Iterator<Integer> it = edges.iterator();
            while(it.hasNext()){
                int value = it.next();
                int graphOneValue = comparisonTableForAlign.get(value);
                if(adjMatrixGraphOne.get(graphOneKey).contains(graphOneValue)){
                    if(!alignedEdges.containsKey(graphOneKey)) {
                        alignedEdges.put(graphOneKey, new ArrayList<Integer>());
                    }
                    alignedEdges.get(graphOneKey).add(graphOneValue);
                    it.remove();
                    if(adjMatrixGraphTwo.get(value).contains(key)){
                        adjMatrixGraphTwo.get(value).remove(adjMatrixGraphTwo.get(value).indexOf(key));
                    }
                    adjMatrixGraphOne.get(graphOneKey).remove(adjMatrixGraphOne.get(graphOneKey).indexOf(graphOneValue));
                    adjMatrixGraphOne.get(graphOneValue).remove(adjMatrixGraphOne.get(graphOneValue).indexOf(graphOneKey));
                }
            }
        }

        CyNetwork myNet = networkFactory.createNetwork();
        myNet.getRow(myNet).set(CyNetwork.NAME, "Alignment Network");




        //colors:
        //0 - aligned
        //1 - unaligned in graph one
        //2 - unaligned in graph two
        myNet.getDefaultNodeTable().createColumn("color", Integer.class, false);

        networkManager.addNetwork(myNet);


        ArrayList<DelayedVizProp> viewAttributes = new ArrayList<DelayedVizProp>();

        Set<Integer> graphOneNodesAdded = new HashSet<Integer>();
        Set<Integer> graphTwoNodesAdded = new HashSet<Integer>();
        //turns the number of the node in graph one and returns the CyNode related to it
        HashMap<Integer, CyNode> graphOneNodeNumberToCyNode = new HashMap<Integer, CyNode>();
        for(Map.Entry<Integer, Integer> entry : comparisonTableForAlign.entrySet()){
            CyNode newNode = myNet.addNode();
            int first = entry.getKey();
            int second = entry.getValue();
            graphOneNodesAdded.add(second);
            graphTwoNodesAdded.add(first);
            graphOneNodeNumberToCyNode.put(second, newNode);
            myNet.getDefaultNodeTable().getRow(newNode.getSUID()).set("name", reverseConverterGraphOne.get(second) + "/" + reverseConverterGraphTwo.get(first));
            myNet.getDefaultNodeTable().getRow(newNode.getSUID()).set("color", 0);
            viewAttributes.add(new DelayedVizProp(newNode, BasicVisualLexicon.NODE_FILL_COLOR, Color.MAGENTA, true));
            viewAttributes.add(new DelayedVizProp(newNode, BasicVisualLexicon.NODE_LABEL, myNet.getDefaultNodeTable().getRow(newNode.getSUID()).get("name", String.class), false));
            viewAttributes.add(new DelayedVizProp(newNode, BasicVisualLexicon.NODE_LABEL_COLOR, Color.BLACK, false));
            viewAttributes.add(new DelayedVizProp(newNode, BasicVisualLexicon.NODE_WIDTH, (double)20, false));
            viewAttributes.add(new DelayedVizProp(newNode, BasicVisualLexicon.NODE_HEIGHT, (double)20, false));
        }
        for(Map.Entry<Integer, String> entry : reverseConverterGraphOne.entrySet()){
            if(!graphOneNodesAdded.contains(entry.getKey())){
                CyNode newNode = myNet.addNode();
                graphOneNodeNumberToCyNode.put(entry.getKey(), newNode);
                myNet.getDefaultNodeTable().getRow(newNode.getSUID()).set("name", entry.getValue());
                myNet.getDefaultNodeTable().getRow(newNode.getSUID()).set("color", 1);
                viewAttributes.add(new DelayedVizProp(newNode, BasicVisualLexicon.NODE_FILL_COLOR, Color.RED, true));
                viewAttributes.add(new DelayedVizProp(newNode, BasicVisualLexicon.NODE_LABEL, myNet.getDefaultNodeTable().getRow(newNode.getSUID()).get("name", String.class), false));
                viewAttributes.add(new DelayedVizProp(newNode, BasicVisualLexicon.NODE_LABEL_COLOR, Color.BLACK, false));
                viewAttributes.add(new DelayedVizProp(newNode, BasicVisualLexicon.NODE_WIDTH, (double)20, false));
                viewAttributes.add(new DelayedVizProp(newNode, BasicVisualLexicon.NODE_HEIGHT, (double)20, false));
            }
        }


        myNet.getDefaultEdgeTable().createColumn("color", Integer.class, false);
        for(Map.Entry<Integer, ArrayList<Integer>> entry : alignedEdges.entrySet()){
            int key = entry.getKey();
            ArrayList<Integer> edges = entry.getValue();
            for(int i = 0; i < edges.size(); i++){
                CyEdge newEdge = myNet.addEdge(graphOneNodeNumberToCyNode.get(key), graphOneNodeNumberToCyNode.get(edges.get(i)), true);
                myNet.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("color", 0);
                viewAttributes.add(new DelayedVizProp(newEdge, BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, Color.MAGENTA, true));
                viewAttributes.add(new DelayedVizProp(newEdge, BasicVisualLexicon.EDGE_STROKE_SELECTED_PAINT, Color.MAGENTA, true));
                viewAttributes.add(new DelayedVizProp(newEdge, BasicVisualLexicon.EDGE_WIDTH, (double)5, true));
            }
        }
        for(Map.Entry<Integer, ArrayList<Integer>> entry : adjMatrixGraphOne.entrySet()){
            int key = entry.getKey();
            ArrayList<Integer> edges = entry.getValue();
            for(int i = 0; i < edges.size(); i++){
                if(adjMatrixGraphOne.get(edges.get(i)).contains(key)){
                    adjMatrixGraphOne.get(edges.get(i)).remove(adjMatrixGraphOne.get(edges.get(i)).indexOf(key));
                }
                CyEdge newEdge = myNet.addEdge(graphOneNodeNumberToCyNode.get(key), graphOneNodeNumberToCyNode.get(edges.get(i)), true);
                myNet.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("color", 1);
                viewAttributes.add(new DelayedVizProp(newEdge, BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, Color.RED, true));
                viewAttributes.add(new DelayedVizProp(newEdge, BasicVisualLexicon.EDGE_STROKE_SELECTED_PAINT, Color.RED, true));
                viewAttributes.add(new DelayedVizProp(newEdge, BasicVisualLexicon.EDGE_WIDTH, (double)5, true));
            }
        }
        for(Map.Entry<Integer, ArrayList<Integer>> entry : adjMatrixGraphTwo.entrySet()){
            int key = entry.getKey();
            ArrayList<Integer> edges = entry.getValue();
            for(int i = 0; i < edges.size(); i++){
                if(adjMatrixGraphTwo.get(edges.get(i)).contains(key)){
                    adjMatrixGraphTwo.get(edges.get(i)).remove(adjMatrixGraphTwo.get(edges.get(i)).indexOf(key));
                }
                CyEdge newEdge = myNet.addEdge(graphOneNodeNumberToCyNode.get(comparisonTableForAlign.get(key)), graphOneNodeNumberToCyNode.get(comparisonTableForAlign.get(edges.get(i))), true);
                myNet.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("color", 2);
                viewAttributes.add(new DelayedVizProp(newEdge, BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, Color.BLUE, true));
                viewAttributes.add(new DelayedVizProp(newEdge, BasicVisualLexicon.EDGE_STROKE_SELECTED_PAINT, Color.BLUE, true));
                viewAttributes.add(new DelayedVizProp(newEdge, BasicVisualLexicon.EDGE_WIDTH, (double)5, true));
            }
        }

        myNet.getDefaultNetworkTable().getRow(myNet.getSUID()).set("name", "Alignment Visualization");
        CyNetworkView myView = networkViewFactory.createNetworkView(myNet);
        networkViewManager.addNetworkView(myView);

        eventHelper.flushPayloadEvents();
        DelayedVizProp.applyAll(myView, viewAttributes);
        myView.updateView();

        if(cancelled){
            networkViewManager.destroyNetworkView(myView);
            networkManager.destroyNetwork(myNet);
        }
    }

    private void readGWFile(String path, HashMap<String, Integer> nameToNum, HashMap<Integer, String> numToName, HashMap<Integer, ArrayList<Integer>> adjMatrix) throws Exception{
        File toRead = new File(path);
        Scanner reader = new Scanner(toRead);
        for(int i = 0; i < 4; i++)
            reader.nextLine();
        int numNodes = Integer.parseInt(reader.nextLine());
        for(int i = 0; i < numNodes; i++){
            String line = reader.nextLine();
            String nodeName = line.substring(2, line.length() - 2);
            nameToNum.put(nodeName, i);
            numToName.put(i, nodeName);
        }
        int numEdges = Integer.parseInt(reader.nextLine());
        for(int i = 0; i < numEdges; i++){
            String line = reader.nextLine();
            int spaceIndex = line.indexOf(' ');
            int a = nameToNum.get(line.substring(0, spaceIndex));
            int b = nameToNum.get(line.substring(spaceIndex+1));
            if(!adjMatrix.containsKey(a))
                adjMatrix.put(a, new ArrayList<Integer>());
            if(!adjMatrix.containsKey(b))
                adjMatrix.put(b, new ArrayList<Integer>());
            adjMatrix.get(a).add(b);
            adjMatrix.get(b).add(a);
        }
    }

    private void readELFile(String path, HashMap<String, Integer> nameToNum, HashMap<Integer, String> numToName,HashMap<Integer, ArrayList<Integer>> adjMatrix) throws Exception{
        File toRead = new File(path);
        Scanner reader = new Scanner(toRead);
        int counter = 0;
        while(reader.hasNextLine()){
            String line = reader.nextLine();
            String[] nodes = line.split("\\s+");
            if(!nameToNum.containsKey(nodes[0])){
                nameToNum.put(nodes[0], counter);
                numToName.put(counter, nodes[0]);
                counter++;
            }
            if(!nameToNum.containsKey(nodes[1])){
                nameToNum.put(nodes[1], counter);
                numToName.put(counter, nodes[1]);
                counter++;
            }
            if(!adjMatrix.containsKey(nameToNum.get(nodes[0])))
                adjMatrix.put(nameToNum.get(nodes[0]), new ArrayList<Integer>());
            if(!adjMatrix.containsKey(nameToNum.get(nodes[1])))
                adjMatrix.put(nameToNum.get(nodes[1]), new ArrayList<Integer>());
            adjMatrix.get(nameToNum.get(nodes[0])).add(nameToNum.get(nodes[1]));
            adjMatrix.get(nameToNum.get(nodes[1])).add(nameToNum.get(nodes[0]));
        }
    }

    private void readAlignFile(String path, HashMap<String, Integer> converterGraphOne, HashMap<String, Integer> converterGraphTwo, HashMap<Integer, Integer> align) throws Exception{
        File toRead = new File(path);
        Scanner reader = new Scanner(toRead);
        while(reader.hasNextLine()){
            String line = reader.nextLine();
            String[] nodes = line.split("\\s+");
            align.put(converterGraphTwo.get(nodes[0]), converterGraphOne.get(nodes[1]));
        }
    }
}
