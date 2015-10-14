package baseline.geometry;

public class Utility {
	/**
	 * p1 is origin, p2 is dest
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static Coordinate3D makeVector(Coordinate3D p1, 
			Coordinate3D p2){
		return new Coordinate3D(p2.getX() - p1.getX(), 
				p2.getY() - p1.getY(), p2.getZ() - p1.getZ());
	}
	
	public static double dot(Coordinate3D vector1, Coordinate3D vector2){
		return vector1.getX()*vector2.getX()
				+ vector1.getY()*vector2.getY()
				+ vector1.getZ()*vector2.getZ();
	}
	
	public static Coordinate3D cross(Coordinate3D vector1, 
			Coordinate3D vector2){
		double crossX = vector1.getY()*vector2.getZ() - vector1.getZ()*vector2.getY();
		double crossY = vector1.getZ()*vector2.getX() - vector1.getX()*vector2.getZ();
		double crossZ = vector1.getX()*vector2.getY() - vector1.getY()*vector2.getX();
		return new Coordinate3D(crossX, crossY, crossZ);
	}
	
	public static void normalize(Coordinate3D vector){
		Line3D line = new Line3D(vector, new Coordinate3D());
		double len = line.getLen();
		
		vector.setX(vector.getX() / len);
		vector.setY(vector.getY() / len);
		vector.setZ(vector.getZ() / len);
	}
	
	public static void scale(Coordinate3D vector, double scale){
		vector.setX(vector.getX() * scale);
		vector.setY(vector.getY() * scale);
		vector.setZ(vector.getZ() * scale);
	}
	
	public static Coordinate3D pointVectorAdd(Coordinate3D point,
			Coordinate3D vector){
		return new Coordinate3D(point.getX()+vector.getX(),
				point.getY()+vector.getY(),
				point.getZ()+vector.getZ());
	}
	
	/**
	 * 3 by 3
	 * @param matrix
	 * @return
	 */
	public static double determinant(double[][] matrix){
		if(matrix.length!=3 || matrix[0].length!=3){
			return 0;
		}
		
		return matrix[0][0]*matrix[1][1]*matrix[2][2]
				+ matrix[0][1]*matrix[1][2]*matrix[2][0]
				+ matrix[0][2]*matrix[1][0]*matrix[2][1]
				- matrix[0][2]*matrix[1][1]*matrix[2][0]
				- matrix[0][1]*matrix[1][0]*matrix[2][2]
				- matrix[0][0]*matrix[1][2]*matrix[2][1];
	}
}
