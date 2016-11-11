package de.l3s.flower.gui;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

public class VisualLineCenterer {

	public Point2D center(RectangularShape visualCategory, VisualBounds lines) {

		Shape vcat = visualCategory;
		VisualBounds vlines = lines;
		
		int ovalheight = (int) (vlines.getRect().height);
		int ovalwidth = (int) (vlines.getRect().width);

		//int x = (int) ovalwidth / 2;
//		int y = (int) ovalheight / 2;

		visualCategory.setFrame(new Rectangle2D.Float(0, 0, (float)ovalwidth,
				(float) ovalheight));

	//	System.out.println("\n-------------\n");
	//	System.out.println("do "+this);
		//if(false)
		growbaby: 
		while(true){
		for (VisualLine line : vlines.getLines()) {

			//System.out.println(line.getX()+","+line.getY()+":"+line.label+"|"+circle.width+","+circle.height);
			int 
			x=(int) (visualCategory.getX() - 1),
			y=(int) visualCategory.getY() - 1,
			w=(int) visualCategory.getWidth() + 2,
			h=(int) (visualCategory.getHeight() + 2);
			if (!visualCategory.contains(line.getX(), line.getY())) {
				visualCategory.setFrame(new Rectangle2D.Float(x,y,w,h));
				
				//circle = new Ellipse2D.Float();
				continue growbaby;
			}
			if (!visualCategory.contains(line.getX(), line.getY() - line.fontSize)) {
				visualCategory.setFrame(new Rectangle2D.Float(x,y,w,h));
				continue growbaby;
			}
		}
		break;
		}
		//System.out.println("posle"+this);
	 Float corner = new Point2D.Float((int)visualCategory.getX(),(int)visualCategory.getY());
		
	 visualCategory.setFrame(new Rectangle2D.Float(0,0,(int)visualCategory.getWidth(),(int)visualCategory.getHeight()));
	 return corner;
}

}
