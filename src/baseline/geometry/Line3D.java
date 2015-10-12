package baseline.geometry;

public class Line3D {
	private Coordinate3D end1;
	private Coordinate3D end2;
	private double len;
	
	public Line3D(Coordinate3D end1, Coordinate3D end2){
		this.end1 = end1;
		this.end2 = end2;
		this.len = this.computeLength();
	}

	public Coordinate3D getEnd1() {
		return end1;
	}

	public void setEnd1(Coordinate3D end1) {
		this.end1 = end1;
	}

	public Coordinate3D getEnd2() {
		return end2;
	}

	public void setEnd2(Coordinate3D end2) {
		this.end2 = end2;
	}

	public double getLen() {
		return len;
	}
	
	private double computeLength(){
		double len = 0;
		len += Math.pow(end1.getX() - this.end2.getX(), 2);
		len += Math.pow(end1.getY() - this.end2.getY(), 2);
		len += Math.pow(end1.getZ() - this.end2.getZ(), 2);
		return Math.sqrt(len);
	}
}
