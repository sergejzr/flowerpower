package de.l3s.gui.topicflower;

import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;



public class CenterPolygon extends RectangularShape{
Rectangle2D rect;
Polygon p;
	@Override
	public boolean contains(double arg0, double arg1) {
		
		return p.contains(arg0,arg1);
	}

	@Override
	public boolean contains(double arg0, double arg1, double arg2, double arg3) {
		
		return p.contains(arg0, arg1, arg2, arg3);
	}

	@Override
	public Rectangle2D getBounds2D() {
	
		return rect.getBounds2D();
	}

	@Override
	public PathIterator getPathIterator(AffineTransform arg0) {
		
		return p.getPathIterator(arg0);
	}

	@Override
	public boolean intersects(double arg0, double arg1, double arg2, double arg3) {
	
		return p.intersects(arg0, arg1, arg2, arg3);
	}

	@Override
	public double getHeight() {
		// TODO Auto-generated method stub
		return rect.getHeight();
	}

	@Override
	public double getWidth() {
	
		return rect.getWidth();
	}

	@Override
	public double getX() {
	
		return rect.getX();
	}

	@Override
	public double getY() {
		
		return rect.getY();
	}

	@Override
	public boolean isEmpty() {
	
		return rect.isEmpty();
	}
/*
	private Polygon calculate(double x, double y, double w, double h)
	{
		
	}
	*/
	@Override
	public void setFrame(double x, double y, double w, double h) {
		rect.setFrame(x, y, w, h);
		
	//	p=calculate(x, y, w, h);
		
	}}
