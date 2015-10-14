package baseline.htmlparser;

import java.util.List;

import baseline.geometry.Coordinate3D;
import baseline.geometry.Polygon;

/**
 * Implement later
 * @author wanghp18
 *
 */
public class WindowWallRatioParser {
	public static final int INVALID_POLYGON = -1;
	public static final int ZERO_AREA_WALL = -2;
	
    public static final double THRESHOLD = 0.4;
    
    private Polygon window;
    private Polygon wall;
    
    public WindowWallRatioParser(List<Coordinate3D> windowCoords, 
    		List<Coordinate3D> wallCoords){
    	this.window = new Polygon(windowCoords);
    	this.wall = new Polygon(wallCoords);
    }
    
    public List<Coordinate3D> getWindowCoords(){
    	return this.window.getCoords();
    }

	public Polygon getWindow() {
		return window;
	}

	public Polygon getWall() {
		return wall;
	}
    
    public double getWindowWallRatio(){
    	if(window.isValid() && wall.isValid()){
    		if(wall.getArea()>0){
    			return window.getArea()/wall.getArea();
    		}
    		return WindowWallRatioParser.ZERO_AREA_WALL;
    	}
    	
    	return WindowWallRatioParser.INVALID_POLYGON;
    }
    
    /**
     * Return whether the window is adjusted
     * @return
     */
    public boolean adjustToThreshold(){
    	double ratio = this.getWindowWallRatio();
    	if(ratio>WindowWallRatioParser.THRESHOLD){
    		double areaToCut = wall.getArea()
    				* (ratio - WindowWallRatioParser.THRESHOLD);
    		
    		window.scale(0 - areaToCut);
    		
    		return true;
    	}
    	return false;
    }
}
