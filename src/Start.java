import javax.swing.*;
import java.util.Scanner;

public class Start {
    public static void main(String[] args) {
        System.out.println("Started");

        if (args.length > 0){
            if (Boolean.parseBoolean(args[1])){
                new File2Pic(new String[]{args[0]});
            }else {
                new Pic2File(new String[]{args[0]});
            }
        }else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    //Turn off metal's use of bold fonts
                    UIManager.put("swing.boldMetal", Boolean.FALSE);
                    try {
                        UIManager.setLookAndFeel(
                                UIManager.getSystemLookAndFeelClassName());
                    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e) {
                        e.printStackTrace();
                    }
                    UI.createAndShowGUI();
                }
            });
        }
    }
}
