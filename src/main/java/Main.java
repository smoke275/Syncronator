import views.FileExplorer;

import static java.awt.EventQueue.invokeLater;

public class Main {
    public static void main(String args[]){
        invokeLater(() -> {
            FileExplorer.getInstance().initUI();
        });
    }
}
