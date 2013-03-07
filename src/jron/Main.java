/*
 * Main.java
 *
 * Created on November 14, 2002, 8:03 PM
 */

package jron;

import java.awt.event.*;

/**
 *
 * @author  junkgui
 */
public class Main {
    
    /** Creates a new instance of Main */
    public Main() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        PlayFrame frame = new PlayFrame();
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        frame.setSize(512,512);
        frame.setVisible(true);
    }
    
}
