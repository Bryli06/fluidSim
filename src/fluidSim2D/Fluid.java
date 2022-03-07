package fluidSim2D;

import java.awt.Color;
import java.awt.Graphics2D;

public class Fluid {
	private int n,size;
	private double dt,diffusion,viscosity;
	private vector[][] prev,curr;
	private double[][] density,prev_den;
	private int iter=16;
	private final int[] dx = {0,0,1,-1};
	private final int[] dy = {1,-1,0,0};
	public Fluid(int size,int iter,double dt, double diffusion, double viscosity) {
		this.n = size+2;
		this.size=size;
		this.dt=dt;
		this.iter=iter;
		this.diffusion = diffusion;
		this.viscosity = viscosity;
		this.prev_den = new double[n][n];
		this.density = new double[n][n];
		this.prev = new vector[n][n];
		this.curr = new vector[n][n];
		
		for(int i=0;i<n;i++) {
			for(int j=0;j<n;j++) {
				this.prev[i][j]=new vector();
				this.curr[i][j]=new vector();
			}
		}
	}
	public void addDensity(int x, int y, double d) {
		density[x][y]+=d;
	}
	
	public void addVelocity(int x, int y, vector v) {
		curr[x][y].plus(v);
	}
	public void draw(Graphics2D g, int scale) {
		int max=255;
		//(int i=1;i<=size;i++) for(int j=1;j<=size;j++) max = Math.max(max, (int) (Math.abs(density[i][j])));
		for(int i=1;i<=size;i++) {
			for(int j=1;j<=size;j++) {
				int d = (int) ((int) Math.abs(density[i][j])*255.0/max);
				if(d>255) d = 255;
				//g.setColor(new Color(((int)curr[i][j].getX()) & ~-256,((int)curr[i][j].getX()) & ~-256,(int) density[i][j] > 255? 255:(int) density[i][j]));
				g.setColor(new Color(d,d,d));
				g.fillRect(scale*(i-1), scale*(j-1), scale, scale);
			}
		}
	}
	public void update() {
		swap();
		solve();
		project();
		swap();
		advect();
		project();
		
		swapDen();
		diffuse();
		swapDen();
		advectDensity();
	}
	private void swap() {
		vector[][] temp = this.curr;
		this.curr=prev;
		this.prev=temp;
	}
	private void swapDen() {
		double[][] temp = density;
		density = prev_den;
		prev_den = temp;
	}
	private void setBoundsX(vector[][] curr,boolean v) {
		for(int i=1;i<=size;i++) {
//			curr[i][0].setX(v ? (-1 * curr[i][1].getX() ): (curr[i][1].getX()));  
//			curr[i][n-1].setX(v ? (-1 * curr[i][n-2].getX()) : (curr[i][n-2].getX())); 
//			curr[0][i].setX(curr[1][i].getX());  
//			curr[n-1][i].setX(curr[n-2][i].getX()); 
//
			curr[i][0].setX(curr[i][1].getX());  
			curr[i][n-1].setX(curr[i][n-2].getX()); 
			curr[0][i].setX(v ? (-1 * curr[1][i].getX() ): (curr[1][i].getX()));  
			curr[n-1][i].setX(v ? (-1 * curr[n-2][i].getX()) : (curr[n-2][i].getX())); 
		}
		
		curr[0][0].setX((curr[1][0].getX()+curr[0][1].getX())*0.5);
		curr[0][n-1].setX((curr[0][n-2].getX()+curr[1][n-1].getX())*0.5);
		curr[n-1][0].setX((curr[n-2][0].getX()+curr[n-1][1].getX())*0.5);
		curr[n-1][n-1].setX((curr[n-2][n-1].getX()+curr[n-1][n-2].getX())*0.5);
	}
	private void setBoundsY(vector[][] curr,boolean v) {
		for(int i=1;i<=size;i++) {

//			curr[i][0].setY(curr[i][1].getY());  
//			curr[i][n-1].setY(curr[i][n-2].getY()); 
//			curr[0][i].setY(v ? (-1 * curr[1][i].getY() ): (curr[1][i].getY()));  
//			curr[n-1][i].setY(v ? (-1 * curr[n-2][i].getY()) : (curr[n-2][i].getY())); 
			

			curr[i][0].setY(v ? (-1 * curr[i][1].getY() ): (curr[i][1].getY()));  
			curr[i][n-1].setY(v ? (-1 * curr[i][n-2].getY()) : (curr[i][n-2].getY())); 
			curr[0][i].setY(curr[1][i].getY());  
			curr[n-1][i].setY(curr[n-2][i].getY()); 
		}
		
		curr[0][0].setY((curr[1][0].getY()+curr[0][1].getY())*0.5);
		curr[0][n-1].setY((curr[0][n-2].getY()+curr[1][n-1].getY())*0.5);
		curr[n-1][0].setY((curr[n-2][0].getY()+curr[n-1][1].getY())*0.5);
		curr[n-1][n-1].setY((curr[n-2][n-1].getY()+curr[n-1][n-2].getY())*0.5);
	}
	private void setBounds() {
		for(int i=1;i<=size;i++) {
			density[i][0]=density[i][1];  
			density[i][n-1]=density[i][n-2]; 
			density[0][i]=density[1][i];  
			density[n-1][i]=density[n-2][i]; 
		}
		
		density[0][0] = (density[1][0]+density[0][1])*0.5;
		density[0][n-1] = (density[0][n-2]+density[1][n-1])*0.5;
		density[n-1][0] = (density[n-2][0]+density[n-1][1])*0.5;
		density[n-1][n-1] = (density[n-2][n-1]+density[n-1][n-2])*0.5;
	}
	
	private void project() {
		for(int i=1;i<=size;i++) for(int j=1;j<=size;j++) prev[i][j] = new vector(-0.5/size * (curr[i+1][j].getX()-curr[i-1][j].getX()+curr[i][j+1].getY()-curr[i][j-1].getY()),0);
		setBoundsX(prev, false);
		setBoundsY(prev, false);
		for(int i=0;i<iter;i++) {
			for(int x=1;x<=size;x++) {
				for(int y=1; y<=size; y++) {
					double temp = 0;
					for(int j=0;j<4;j++) {
						temp+=prev[x+dx[j]][y+dy[j]].getX();
					}
					prev[x][y].setX((prev[x][y].getY()+temp)/4);
				}
			}
			setBoundsX(prev,false);
		}
		for(int x=1;x<=size;x++) {
			for(int y=1;y<=size;y++) {
				curr[x][y].minus(new vector(0.5*size*(prev[x+1][y].getX()-prev[x-1][y].getX()),0.5*size*(prev[x][y+1].getX()-prev[x][y-1].getX())));
			}
		}
		setBoundsX(curr,true);
		setBoundsY(curr,true);
	}
	
	
	private void advect() {
		double dt0=dt*size;
		int x0,x1,y0,y1;
		double s0,s1,t0,t1;
		for(int x=1;x <= size;x++) {
			for(int y=1;y <= size;y++) {
				double tempx = x- dt0*prev[x][y].getX();
				double tempy = y- dt0*prev[x][y].getY();
				if (tempx<0.5) tempx=0.5;
				else if (tempx>size+0.5) tempx=size+0.5;
				x0=(int) tempx;
				x1=x0+1;
				if (tempy<0.5) tempy=0.5;
				else if (tempy>size+0.5) tempy=size+0.5;
				y0=(int) tempy;
				y1=y0+1;
				
				s1=tempx-x0;
				s0=1-s1;
				t1=tempy-y0;
				t0=1-t1;
				curr[x][y] = new vector(s0*(t0*prev[x0][y0].getX()+t1*prev[x0][y1].getX())+s1*(t0*prev[x1][y0].getX()+t1*prev[x1][y1].getX()),
										s0*(t0*prev[x0][y0].getY()+t1*prev[x0][y1].getY())+s1*(t0*prev[x1][y0].getY()+t1*prev[x1][y1].getY()));
			}
		}
		setBoundsX(curr, true);
		setBoundsY(curr, true);
	}
	
	private void advectDensity() {
		double dt0=dt*size;
		int x0,x1,y0,y1;
		double s0,s1,t0,t1;
		for(int x=1;x <= size;x++) {
			for(int y=1;y <= size;y++) {
				double tempx = x- dt0*curr[x][y].getX();
				double tempy = y- dt0*curr[x][y].getY();
				if (tempx<0.5) tempx=0.5;
				else if (tempx>size+0.5) tempx=size+0.5;
				x0=(int) tempx;
				x1=x0+1;
				if (tempy<0.5) tempy=0.5;
				else if (tempy>size+0.5) tempy=size+0.5;
				y0=(int) tempy;
				y1=y0+1;
				
				s1=tempx-x0;
				s0=1-s1;
				t1=tempy-y0;
				t0=1-t1;
				density[x][y] = s0*(t0*prev_den[x0][y0]+t1*prev_den[x0][y1])+s1*(t0*prev_den[x1][y0]+t1*prev_den[x1][y1]);
			}
		}
		setBounds();
	}
	
	private void diffuse() {
		double temp = dt * diffusion * size * size;
		for (int i = 0; i < iter; i++) {
	        for (int x = 1; x<=size; x++) {
	            for (int y =1; y<=size; y++) {
	            	double t=prev_den[x][y];
					for(int j=0;j<4;j++) {
						t+=temp *density[x+dx[j]][y+dy[j]];
					}
	                density[x][y] = t/(1+4*temp);
	            }
	        }
	        setBounds();
	    }
	}
	private void solve() {
		double temp = dt * viscosity * size * size;
		for(int i=0;i<iter;i++) {
			for(int x=1;x<=size;x++) {
				for(int y=1;y<=size; y++) {
					double tempx = prev[x][y].getX();
					double tempy = prev[x][y].getY();
					for(int j=0;j<4;j++) {
						tempx+=temp *(curr[x+dx[j]][y+dy[j]].getX());
						tempy+=temp *(curr[x+dx[j]][y+dy[j]].getY());
					}
					curr[x][y] = new vector(tempx / (1+4*temp),tempy / (1+4*temp));
				}
			}
			setBoundsX(curr, true);
			setBoundsY(curr, true);
		}
	}
}
