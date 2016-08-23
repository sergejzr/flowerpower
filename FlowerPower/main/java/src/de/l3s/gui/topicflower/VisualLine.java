package de.l3s.gui.topicflower;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;

public class VisualLine {


	String label;
	int fontSize; 
	FontMetrics fm;
	Rectangle2D rect;
	private int y;
	private int x;
	private Color color;
	public VisualLine(String label, int fontSize, FontMetrics fm,
			Rectangle2D rect, Color color) {
		super();
		this.label = label;
		this.fontSize = fontSize;
		this.fm = fm;
		this.rect = rect;
		this.color=color;
	}
	public void setBounds(int left, int top) {
		x=left; y=top;
		
	}
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	public Color getColor() {
		// TODO Auto-generated method stub
		return color;
	}
	
	
}
