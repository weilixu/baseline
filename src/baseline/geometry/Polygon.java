package baseline.geometry;

import java.util.ArrayList;
import java.util.List;

public class Polygon {
	public static final int SCALE_SUCCESS = 0;
	public static final int SCALE_POLYGON_INVALID = -1;
	public static final int SCALE_RATIO_INVALID = -2;
	
	private static final double NORMAL_SCALE = 100;
	
	private static final double CONTAIN_TEST_EPSILON = 0.0001;
	
	private List<Coordinate3D> coords;
	
	private List<Triangle> triangles;
	
	private double area;
	private boolean isValid;
	private int numPoints;
	
	/**
	 * Follow the sequence of the coordinates, 
	 * the polygon should not be self-intersected
	 * @param coords
	 */
	public Polygon(List<Coordinate3D> coords){
		this.numPoints = coords.size();
		if(this.numPoints<3){
			this.isValid = false;
			this.coords = null;
			//this.triangles = null;
			this.area = 0;
		}else {
			this.isValid = true;
			this.coords = coords;
			
			//this.buildTriangles();
			this.area = this.computeArea();
		}
	}
	
	public int getNumPoints(){
		return this.numPoints;
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
	 * @deprecated
	 */
	@SuppressWarnings("unused")
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
	
	/**
	 * @deprecated
	 */
	@SuppressWarnings("unused")
	private double computeArea_sum_triangle(){
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
	 * Polygon is not self-intersected
	 * @return
	 */
	private double computeArea(){
		if(!isValid){
			return 0;
		}
		
		Coordinate3D p1, p2;
		Coordinate3D sum = new Coordinate3D();
		for(int i=0;i<numPoints;i++){
			p1 = coords.get(i);
			if(i<numPoints-1){
				p2 = coords.get(i+1);
			}else {
				p2 = coords.get(0);
			}
			
			Coordinate3D cross = Utility.cross(p1, p2);
			sum.setX(sum.getX() + cross.getX());
			sum.setY(sum.getY() + cross.getY());
			sum.setZ(sum.getZ() + cross.getZ());
		}
		
		Coordinate3D normal = this.getNorm();
		Utility.normalize(normal);
		
		double area = Utility.dot(sum, normal);
		return Math.abs(area/2);
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
	 * areaScaleRatio must be great than or equal to 0<br/>
	 * areaScaleRatio<1 and >0 => shrink<br/>
	 * areaScaleRatio>1 => enlarge<br/>
	 * 
	 * @param areaScaleRatio
	 * @return
	 */
	public int scale(double areaScaleRatio){
		if(!isValid){
			return Polygon.SCALE_POLYGON_INVALID;
		}
		if(areaScaleRatio < 0){
			return Polygon.SCALE_RATIO_INVALID;
		}
		
		double edgeScaleRatio = Math.sqrt(areaScaleRatio);
		double vectorScaleRatio = edgeScaleRatio-1;
		
		Coordinate3D top = this.coords.get(0);
		
		Coordinate3D vectorNormal = this.getNorm();
		Utility.normalize(vectorNormal);
		Utility.scale(vectorNormal, Polygon.NORMAL_SCALE);
		
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
		//this.buildTriangles();
		
		// update area
		this.area = this.computeArea();
		
		return Polygon.SCALE_SUCCESS;
	}
	
	public boolean containsPoint(Coordinate3D point){
		double angleSum = 0;
		for(int i=0,j=coords.size()-1;i<coords.size();j=i,i++){
			Coordinate3D v1 = coords.get(i);
			Coordinate3D v2 = coords.get(j);
			
			Coordinate3D pv1Vector = Utility.makeVector(point, v1);
			Coordinate3D pv2Vector = Utility.makeVector(point, v2);
			
			double pv1Len = pv1Vector.vectorLen();
			double pv2Len = pv2Vector.vectorLen();
			
			double lenTimes = pv1Len * pv2Len;
			if(lenTimes <= Polygon.CONTAIN_TEST_EPSILON){
				return true; //point is on polygon's vertex, consider inside
			}
			
			double cosAngle = Utility.dot(pv1Vector, pv2Vector) / lenTimes;
			angleSum += Math.acos(cosAngle);
		}
		
		return Math.abs(2*Math.PI-angleSum) < Polygon.CONTAIN_TEST_EPSILON;
	}
}
