package fluidSim2D;

public class vector {
	private double x,y;
	public vector() {
		this.x=0;
		this.y=0;
	}
	public vector(double x, double y) {
		this.x=x;
		this.y=y;
	}
	public void plus(vector t) {
		this.x+=t.getX();
		this.y+=t.getY();
	}
	public void minus(vector t) {
		this.x-=t.getX();
		this.y-=t.getY();
	}
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	public String toString() {
		return "("+ x +", "+y+")";
	}
}
