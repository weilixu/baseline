package baseline.geometry;

import java.util.ArrayList;
import java.util.List;

public class Polygon {
	public static final int SCALE_SUCCESS = 0;
	public static final int SCALE_POLYGON_INVALID = -1;
	public static final int SHRINK_AREA_DELTA_EXCEED = -2;
	
	private List<Coordinate3D> coords;
	
	private List<Triangle> triangles;
	private double area;
	private boolean isValid;
	
	public Polygon(List<Coordinate3D> coords){
		if(coords.size()<3){
			this.isValid = false;
			this.coords = null;
			this.triangles = null;
			this.area = 0;
		}else {
			this.isValid = true;
			this.coords = coords;
			
			this.buildTriangles();
			this.area = this.computeArea();
		}
	}
	
	public List<Coordinate3D> getCoords() {
		return coords;
	}

	public double getArea() {
		return area;
	}

	public boolean isValid() {
		return isValid;
	}

	/**
	 * Assume the polygon is convex
	 */
	private void buildTriangles(){
		this.triangles = new ArrayList<Triangle>(coords.size()-2);
		
		Coordinate3D top = coords.get(0);
		for(int i=2;i<coords.size();i++){
			Coordinate3D p2 = coords.get(i-1);
			Coordinate3D p3 = coords.get(i);
			Triangle tri = new Triangle(top, p2, p3);
			triangles.add(tri);
			if(!tri.isValid()){
				coords = null;
				triangles = null;
				isValid = false;
				break;
			}
		}
	}
	
	private double computeArea(){
		if(!isValid){
			return 0;
		}
		
		double area = 0;
		for(Triangle tri : triangles){
			if(tri.isValid()){
				area += tri.getArea();
			}else {
				area = 0;
				break;
			}
		}
		return area;
	}
	
	/**
	 * Origin at first point in coords
	 * @return
	 */
	private Coordinate3D getNorm(){
		if(isValid){
			Coordinate3D p1 = this.coords.get(0);
			Coordinate3D p2 = this.coords.get(1);
			Coordinate3D p3 = this.coords.get(2);
			
			Coordinate3D vector21 = Utility.makeVector(p1, p2);
			Coordinate3D vector31 = Utility.makeVector(p1, p3);
			
			return Utility.cross(vector21, vector31);
		}
		
		return new Coordinate3D(); //origin
	}
	
	/**
	 * If area is greater or equal to polygon's area,
	 * return false and do nothing. Shrink and update
	 * coordinates and triangles otherwise.
	 * @param area
	 * @return
	 */
	public int scale(double areaDelta){
		if(!isValid){
			return Polygon.SCALE_POLYGON_INVALID;
		}
		if(areaDelta+this.area <= 0){
			return Polygon.SHRINK_AREA_DELTA_EXCEED;
		}
		
		double areaScaleRatio = (this.area+areaDelta) / this.area;
		double edgeScaleRatio = Math.sqrt(areaScaleRatio);
		double vectorScaleRatio = edgeScaleRatio-1;
		
		Coordinate3D top = this.coords.get(0);
		
		Coordinate3D vectorNormal = this.getNorm();
		Utility.normalize(vectorNormal);
		Utility.scale(vectorNormal, triangles.get(0).maxEdgeLen());
		
		Coordinate3D shiftedTop = Utility.pointVectorAdd(top, vectorNormal);
		Coordinate3D vectorNomralShiftback = vectorNormal.duplicate();
		Utility.scale(vectorNomralShiftback, vectorScaleRatio);
		
		// first point (i.e. top point) is not moved
		for(int i=1;i<coords.size();i++){
			Coordinate3D p = coords.get(i);
			
			Coordinate3D vectorShift = Utility.makeVector(shiftedTop, p);
			Utility.scale(vectorShift, vectorScaleRatio);
			
			Coordinate3D shiftedP = Utility.pointVectorAdd(p, vectorShift);
			Coordinate3D scaledP = Utility.pointVectorAdd(shiftedP, vectorNomralShiftback);
			
			coords.set(i, scaledP);
		}
		
		// update triangles
		this.buildTriangles();
		
		// update area
		this.area = this.computeArea();
		
		return Polygon.SCALE_SUCCESS;
	}
}
