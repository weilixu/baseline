package baseline.runeplus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import baseline.util.BaselineUtils;
import lepost.config.FilesPath;

/**
 * This class runs the EnergyPlus file for sizing purpose. The EnergyPlus
 * model's envelope thermal properties and lighting should be properly defined.
 * However, the window-wall ratio could be remain the same as proposed case.
 * This will be later judged from the sizing run results. If necessary,
 * fenestration area modification will be performed by other class and sizing
 * run is required to be performed again
 * 
 * This sizing simulation should not be version dependent, however, for the sake
 * of asset score tool usage, we fix it to V8.2 currently.
 * 
 * The Batch File is the EnergyPlus RunEplus.bat
 * 
 * @author Weili
 *
 */
public class SizingRun {
	private final Logger LOG = LoggerFactory.getLogger(this.getClass());
	
    private static final String EPLUSBAT = "RunEplus.bat";

    private File idfFile;
    private final File weatherFile;
    // folder should be the folder that contains the idf file
    private File folder;

    private boolean baseline;

    public SizingRun(File wea) {
	// idfFile = idf;
	weatherFile = wea;
	baseline = false;
    }

    /**
     * This method should always be called prior to runEnergyPlus method
     * 
     * @param idf
     */
    public void setEplusFile(File idf) {
	idfFile = idf;
	folder = idfFile.getParentFile();
    }

    public void setBaselineSizing() {
	baseline = true;
    }

    /**
     * Run EnergyPlus with defined idf file and weather file. This is not
     * parametric simulation - it only use for sizing purpose This method will
     * return the html results file once the simulation completed.
     * 
     * @throws IOException
     */
    public File runEnergyPlus(String targetHTML) throws IOException {
	File resultsFile = null;

	String path = idfFile.getAbsolutePath();
	String pathToIDF = path.substring(0, path.lastIndexOf("."));
	String weatherName = weatherFile.getName();
	String weather = weatherName.substring(0,weatherName.lastIndexOf("."));
	File eplusBatFile = createBatchFile();
	String[] commandline = { eplusBatFile.getAbsolutePath(), pathToIDF, weather};

	try {
	    Process p = Runtime.getRuntime().exec(commandline, null, folder);
	    ThreadedInputStream errStr = new ThreadedInputStream(
		    p.getErrorStream());
	    errStr.start();
	    ThreadedInputStream outStr = new ThreadedInputStream(
		    p.getInputStream());
	    outStr.start();
	    p.waitFor();

	    errStr.join();
	    outStr.join();
	    eplusBatFile.delete();
	} catch (Exception e) {
	    e.printStackTrace();
	}

	for (File f : folder.listFiles()) {
	    if (baseline) {
			if (f.getName().contains(targetHTML+".html")) {
			    resultsFile = f;
			}
	    } else {
			if (f.getName().contains("html")) {
			    resultsFile = f;
			}
	    }
	}
	return resultsFile;
    }

    /**
     * copy the batch file to the target folder with modified eplus directory
     * and weather file directory
     * 
     * @return
     * @throws IOException
     */
    private File createBatchFile() throws IOException {
	String keyWord = "set program_path=";
	String weaWord = "set weather_path=";
	File file = new File(folder.getAbsolutePath() + "\\" + EPLUSBAT);
	LOG.debug("Creating folder: "+folder.getAbsolutePath());
	file.createNewFile();
	// reading file and write to the new file
	File newBat = new File(FilesPath.readProperty("ResourcePath_baseline")+ EPLUSBAT);
	BufferedReader br = new BufferedReader(new FileReader(newBat));
	StringBuilder sb = new StringBuilder();

	try {
	    String line = br.readLine();

	    while (line != null) {
		if (line.contains(keyWord)) {
		    sb.append(keyWord);
		    sb.append(BaselineUtils.getEnergyPlusDirectory()); // fixed
								       // version
		} else if (line.contains(weaWord)) {
		    sb.append(weaWord);
		    sb.append(folder.getAbsolutePath() + "\\");
		} else {
		    sb.append(line);
		}
		sb.append(System.lineSeparator());
		line = br.readLine();
	    }
	} finally {
	    FileWriter results = null;
	    try {
		results = new FileWriter(file, true);
		PrintWriter pw = new PrintWriter(results);
		pw.append(sb.toString());
		pw.close();
	    } catch (IOException e) {
		// some warning??
	    }
	    // close the file
	    br.close();
	}
	return file;
    }

    @SuppressWarnings("unused")
    private class ThreadedInputStream extends Thread {
	private IOException ioExc;
	private InputStream is;
	private StringBuffer sb = null;

	public ThreadedInputStream(InputStream inputStream) {
	    is = inputStream;
	    sb = new StringBuffer();
	    ioExc = null;
	}

	public void run() {
	    try {
		byte[] by = new byte[1];
		while (this != null) {
		    int ch = is.read(by);
		    if (ch != -1) { // -1 indicates the end of the stream
			// System.out.print((char) by[0]);
			sb.append((char) by[0]);
		    } else {
			break;
		    }
		}
		is.close();
	    } catch (IOException e) {
		ioExc = e;
	    }
	}

	public void throwStoredException() throws IOException {
	    if (ioExc != null) {
		throw ioExc;
	    }
	}

	public String getInputStream() {
	    return new String(sb.toString());
	}
    }

}
