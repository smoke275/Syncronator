import views.ApplicationSystemTray;
import views.FileExplorer;

public class Main {
    public static void main(String args[]){
        ApplicationSystemTray.getInstance().init();
        FileExplorer.invokeFileManager();
    }
}
