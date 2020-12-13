package visal;

public class changeToggler {
    NodeSelectionEventListener listener;
    ToggleEdgeTaskFactory aligned;
    ToggleEdgeTaskFactory unalignedOne;
    ToggleEdgeTaskFactory unalignedTwo;

    boolean selectedNodes;
    boolean induced;
    boolean hasHiddenNodes;

    public boolean displayAligned;
    public boolean displayUnalignedOne;
    public boolean displayUnalignedTwo;
    public changeToggler(NodeSelectionEventListener listener, ToggleEdgeTaskFactory aligned, ToggleEdgeTaskFactory unalignedOne, ToggleEdgeTaskFactory unalignedTwo){
        this.listener = listener;
        this.aligned = aligned;
        this.unalignedOne = unalignedOne;
        this.unalignedTwo = unalignedTwo;
        selectedNodes = false;
        induced = false;
        displayAligned = true;
        displayUnalignedOne = true;
        displayUnalignedTwo = true;
        hasHiddenNodes = false;
    }

    public boolean getSelected(){
        return selectedNodes;
    }

    public void toggleSelected(){
        selectedNodes = !selectedNodes;
    }

    public boolean getInduced(){
        return induced;
    }

    public void toggleInduced(){
        induced = !induced;
    }
}
