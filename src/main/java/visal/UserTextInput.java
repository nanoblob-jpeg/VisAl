package visal;

import javax.swing.JOptionPane;
public class UserTextInput {
    private String prompt;
    UserTextInput(){}
    UserTextInput(String a){
        prompt = a;
    }

    public String getUserInput(){
        String m = JOptionPane.showInputDialog(prompt);
        return m;
    }

    public String getUserInput(String s){
        String m = JOptionPane.showInputDialog(s);
        return m;
    }

    public void setPrompt(String a){
        prompt = a;
    }
}
