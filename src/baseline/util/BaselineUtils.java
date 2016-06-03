package baseline.util;

import java.io.FileInputStream;
import java.util.Properties;

import lepost.config.FilesPath;

public final class BaselineUtils {
    private static final String CONFIG_PROPERTIES = "baseline.properties";
    private static final String ENERGYPLUS_DIR = "baseline.directory";
    
    private static String directory = null;
    
    private static String absoluteDir = "";
    
    private static Properties baselineProps;
    
    static{
	initProperties();
    }
    
    private static void initProperties(){
	String tempDir = directory;
	
	try{
	    baselineProps = new Properties();
	    FileInputStream baselineIn = new FileInputStream(FilesPath.readProperty("ResourcePath_baseline") + CONFIG_PROPERTIES);
	    
	    baselineProps.load(baselineIn);
	    baselineIn.close();
	    
	    directory = baselineProps.getProperty(ENERGYPLUS_DIR);
	}catch(Exception e){
	    directory = tempDir;
	}
    }
    
    public static String getEnergyPlusDirectory(){
	return directory;
    }
    
    public static void setAbsoluteDir(String abs){
	absoluteDir = abs;
	//reinite properties because the change of directory
	initProperties();
    }
    
    public static String getAbsolutionDir(){
	return absoluteDir;
    }
}
