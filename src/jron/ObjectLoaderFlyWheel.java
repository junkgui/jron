/*
 * ObjectLoaderFlyWheel.java
 *
 * Created on November 16, 2002, 1:53 PM
 */

package jron;

import ncsa.j3d.loaders.*;
import com.sun.j3d.loaders.*;
import javax.media.j3d.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author  junkgui
 */
public class ObjectLoaderFlyWheel {
    
    private static ObjectLoaderFlyWheel instance = null;
    private HashMap flyWheel = new HashMap();
    
    /** Creates a new instance of ObjectLoaderFlyWheel, use getInstance()*/
    private ObjectLoaderFlyWheel() {
    }
    
    public String toString() {
        return super.toString();
    }
    
    public static ObjectLoaderFlyWheel getInstance() {
        if (instance == null) {
            instance = new ObjectLoaderFlyWheel();
        } 
        return instance;
    }
    
    private Loader loader = new Loader_DXF();
    //private Loader loader = new Loader_WRL();
    
    public void setLoader(Loader loader) {
        this.loader = loader;
    }
    
    public Loader getLoader() {
        return loader;
    }
    
    public Node getLoadedObject(String objectToLoad, int childNumber) {
        try {
            BranchGroup bg = (getLoader().load(new InputStreamReader(getClass().getResourceAsStream(objectToLoad)))).getSceneGroup();
        
            TransformGroup n = (TransformGroup)bg.getChild(childNumber);
            Node tg = n.getChild(0).cloneNode(true);

            return tg;
            
        } catch (java.io.FileNotFoundException fne) {
            fne.printStackTrace();
            return null;
        }
        
    }
    
}
