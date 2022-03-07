package fluidSim2D;

import java.awt.Color;
import java.awt.Graphics2D;

public class main {
	private static final int n=256;
	private static final int scale = 5;
	private static DrawingBoard board = new DrawingBoard(0,0,n*scale,n*scale);
	private static Graphics2D g = (Graphics2D) board.getBufferedGraphics();
	private static Fluid fluid = new Fluid(n,20,0.2,0,0.0000001);
	private static int pmousex,pmousey;
	public static void main(String[] args) {
		long start;
		int end;
		while(true) {
			fluid.addDensity(n/2, n/2,400);
			fluid.addVelocity(n/2, n/2, new vector(0,100));
			start=System.currentTimeMillis();
			draw();
			fluid.update();
			end = 20-(int) (System.currentTimeMillis()-start);
			try {
				if(end>0)Thread.sleep(end);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	public static void mouseClicked(int x,int y) {
		if(x<0||y<0||x>n*scale||y>n*scale) return;
		pmousex=x/scale;
		pmousey=y/scale;
	}
	public static void mouseDragged(int x,int y) {
		if(x<0||y<0||x>n*scale||y>n*scale) return;
		fluid.addDensity(x/scale, y/scale, 500);
		fluid.addVelocity(x/scale, y/scale, new vector(x/scale-pmousex,y/scale-pmousey));
	}
	private static void draw() {
		board.clear();
		fluid.draw(g,scale);
		board.repaint();
	}
}
