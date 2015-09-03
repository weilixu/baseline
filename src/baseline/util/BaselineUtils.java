package baseline.util;

import java.io.FileInputStream;
import java.util.Properties;

public final class BaselineUtils {
    private static final String CONFIG_PROPERTIES = "baseline.properties";
    private static final String ENERGYPLUS_DIR = "energyplus.directory";
    
    private static String directory = null;
    
    private static Properties baselineProps;
    
    static{
	initProperties();
    }
    
    private static void initProperties(){
	String tempDir = directory;
	
	try{
	    baselineProps = new Properties();
	    FileInputStream baselineIn = new FileInputStream(CONFIG_PROPERTIES);
	    
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
}
