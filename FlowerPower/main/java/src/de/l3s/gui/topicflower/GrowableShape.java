package de.l3s.gui.topicflower;


public interface GrowableShape {

	public  void grow(float x, float y, float width, float height);

	public  int getX();

	public  int getY();

	public  int getHeight();

	public  int getWidth();

	public  boolean contains(int x, int y);

}
