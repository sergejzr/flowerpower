package de.l3s.gui.topicflower;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

import de.l3s.gui.diagram.Quadrant;



public class VisualCategory extends Ellipse2D.Float{

	VisualBounds lines;
	VisualBounds conlines;
	
	private int margin = 2;
	private java.awt.geom.Point2D corner;
	private String title;
	private boolean transparent;
	private java.awt.geom.Point2D.Double globalcorner;
	private Rectangle2D titlebounds;
	private Point2D cornertitle;
	Color background=Color.white;

public void setBackground(Color background) {
	this.background = background;
}
public Color getBackground() {
	return background;
}
public void calculate()
{
VisualLineCenterer centerer=new VisualLineCenterer();

corner=centerer.center(this,lines);
}
@Override
public String toString() {
	// TODO Auto-generated method stub
	return "x: "+getX()+" y:"+getY()+" w:"+getWidth()+" h:"+getHeight();
}
	public VisualCategory(String title, Vector<VisualLine> lines, Vector<VisualLine> conlines, boolean transparent) {
		super();
		this.transparent=transparent;
		this.title=title;
		this.lines = new VisualBounds(lines,margin);
		if(conlines!=null&&conlines.size()>0) {
		this.conlines = new VisualBounds(conlines,margin);
		}else
		{
			new VisualBounds(new Vector<VisualLine>(), margin);
		}
		calculate();
	}

	
public String getTitle() {
	return title;
}
	public VisualCategory(String title, Vector<VisualLine> genlines,boolean transparent) {this(title,genlines,null,transparent);

	}
	public Rectangle getCategoryBounds() {
		return lines.getRect();
	}

	public void paintCategory(Graphics g, Quadrant qa) {

		Graphics2D g2d = (Graphics2D) g;
		Stroke stroke = g2d.getStroke();
		
		VisualCategory vcat = this;
		VisualBounds vlines = vcat.lines;
		if(!transparent){
		g.setColor(Color.white);
		
	//	
		g.fillOval(0,0,
				(int) getWidth(), (int) getHeight());
		}
		g.setColor(Color.black);
		g.drawOval(0,0,
				(int) getWidth(), (int) getHeight());
		
		if(qa!=null){
			Font curfont=g.getFont();
			g.setFont(new Font("Serif", Font.BOLD, 12));
		//int titlex=0,titley=0;
	 titlebounds = g.getFontMetrics().getStringBounds(title, g);
	 cornertitle=null;
	int tostop=30;
	int margin=3;
	
	adjusttitle:
	while(true){
		if(tostop--<=0) break;
		switch(qa)
		{
		case FIRST:
		{
			if(cornertitle==null)
			cornertitle=new Point2D.Double((int)getWidth(), (int)titlebounds.getHeight());
		
		if(!contains(new Point2D.Double(cornertitle.getX()-margin,cornertitle.getY())) 
				&&!contains(new Point2D.Double(cornertitle.getX()-margin,cornertitle.getY()-titlebounds.getHeight()))
			)
		{
			cornertitle=new Point2D.Double(cornertitle.getX()-margin,cornertitle.getY());
		}else
		{
			break adjusttitle;
		}	
			
		}break;
		case SECOND:{
			if(cornertitle==null)
			cornertitle=new Point2D.Double((int)getWidth(), (int)getHeight());
			
			if(!contains(new Point2D.Double(cornertitle.getX()-margin,cornertitle.getY()))&&
					!contains(new Point2D.Double(cornertitle.getX()-margin,cornertitle.getY()-titlebounds.getHeight())))
			{
				cornertitle=new Point2D.Double(cornertitle.getX()-margin,cornertitle.getY());
			}else
			{
				break adjusttitle;
			}
			
			}break;
			//titlex=(int)getWidth(); titley=(int)getHeight();}break;
		case THIRD:{
			if(cornertitle==null)
			cornertitle=new Point2D.Double(-(int)titlebounds.getWidth(), (int)getHeight());
			
			if(!contains(new Point2D.Double(cornertitle.getX()+margin+titlebounds.getWidth(),cornertitle.getY()))
					&&!contains(new Point2D.Double(cornertitle.getX()+margin+titlebounds.getWidth(),cornertitle.getY()-titlebounds.getHeight()))	
					)
			{
				cornertitle=new Point2D.Double(cornertitle.getX()+margin,cornertitle.getY());
			}else
			{
				break adjusttitle;
			}
		}break;
		case FOURTH:{
			if(cornertitle==null)
			cornertitle=new Point2D.Double(-(int)titlebounds.getWidth(), (int)titlebounds.getHeight());
			
			if(!contains(new Point2D.Double(cornertitle.getX()+margin+titlebounds.getWidth(),cornertitle.getY())) 
					&&!contains(new Point2D.Double(cornertitle.getX()+margin+titlebounds.getWidth(),cornertitle.getY()-titlebounds.getHeight()))
					)
			{
				cornertitle=new Point2D.Double(cornertitle.getX()+margin,cornertitle.getY());
			}else
			{
				break adjusttitle;
			}
		}break;
		default:{	g.fillOval(0, 0, 10, 10);}
		}
		
	}

		g.drawString(title, (int)cornertitle.getX(), (int)cornertitle.getY());
		titlebounds=new Rectangle2D.Double((int)cornertitle.getX()+globalcorner.getX(), (int)cornertitle.getY()+globalcorner.getY(),titlebounds.getWidth(),titlebounds.getHeight());
		//g.drawRect((int)cornertitle.getX(), (int)(cornertitle.getY()-titlebounds.getHeight()),(int)titlebounds.getWidth(),(int)titlebounds.getHeight());
		g.setFont(curfont);
		}
		g.translate(-(int) corner.getX(), -(int) corner.getY());
	//	System.out.println("\n-------------\n"+this);
	
		
		g.setColor(Color.black);
		Font bf = g.getFont();
		//System.out.println("\n-------------\n");
		for (VisualLine line : vlines.getLines()) {
			Color tmp=g.getColor();
			
			Font font = new Font("Serif", Font.BOLD, line.fontSize);
			g.setFont(font);
			g.setColor(line.getColor());
		//	g.drawString(line.label, (int)(line.getX()+getX()), (int)(line.getY()+getY()));
			g.drawString(line.label, (int)(line.getX()), (int)(line.getY()));
			g.setColor(tmp);
			//g.drawRect(line.getX(), line.getY(), (int)line.rect.getWidth(), (int)line.rect.getHeight());
		//	System.out.println(line.getX()+","+line.getY()+":"+line.label+"|"+line.rect);
		}
		g.translate((int) corner.getX(), (int) corner.getY());
		g.setFont(bf);
		
	}
	public void paintLink(Graphics g, VisualCategory nextcat, int x2, int y2, Quadrant qa, VisualCategory centercat) {
		//g.translate((int) (corner.getX()-conlines.getRect().getWidth()), (int) (corner.getY()-conlines.getRect().getHeight()));
		//g.translate((int) (corner.getX()-conlines.getRect().getWidth()), (int) (corner.getY()-conlines.getRect().getHeight()));

		
		
		Polygon p = conlines.getPoly(); 
		
		
		
		
		
		Rectangle2D bounds=new Rectangle2D.Double(conlines.getRect().getX()+x2,conlines.getRect().getY()+y2,conlines.getRect().getWidth(),conlines.getRect().getHeight());
	//	Ellipse2D curcatellipse=new Ellipse2D.Double(getX()+corner.getX(),getY()+corner.getY(),getWidth(),getHeight());
		
		Ellipse2D curcatellipse=new Ellipse2D.Double(globalcorner.getX(),globalcorner.getY(),getWidth(),getHeight());
		
		Ellipse2D nextcatellipse=new Ellipse2D.Double(nextcat.globalcorner.getX(),nextcat.globalcorner.getY(),nextcat.getWidth(),nextcat.getHeight());
		
		Ellipse2D centercatellipse=new Ellipse2D.Double(centercat.globalcorner.getX(),centercat.globalcorner.getY(),centercat.getWidth(),centercat.getHeight());
		//Rectangle2D curcattitle=titlebounds;
		
	//	System.out.print("\n-------\n");
		//System.out.print("bounds was: "+bounds);
		/*
		g.setColor(Color.red);
		g.fillOval((int)curcatellipse.getX(), (int)curcatellipse.getY(), (int)curcatellipse.getWidth(), (int)curcatellipse.getHeight());
		g.setColor(Color.blue);
		g.fillOval((int)nextcatellipse.getX(), (int)nextcatellipse.getY(), (int)nextcatellipse.getWidth(), (int)nextcatellipse.getHeight());
*/
		
		int sichi=50;
		//if(false)
		adjust:
		while(true){
			if(sichi--<0) break;
		switch(qa)
		{
		
		case FIRST:
		{
			if(curcatellipse.intersects(bounds)||titlebounds.intersects(bounds))
			{
				bounds= new Rectangle2D.Double(bounds.getX()+3,bounds.getY(),bounds.getWidth(),bounds.getHeight());
				continue adjust;
			}
			
			if(nextcatellipse.intersects(bounds))
			{
				bounds= new Rectangle2D.Double(bounds.getX(),bounds.getY()-3,bounds.getWidth(),bounds.getHeight());
				continue adjust;
			}
			if(centercatellipse.intersects(bounds))
			{
				bounds= new Rectangle2D.Double(bounds.getX()+3,bounds.getY(),bounds.getWidth(),bounds.getHeight());
				continue adjust;
			}
			/*
			g.setColor(Color.green);
			g.fillRect((int)bounds.getX(),(int)bounds.getY(),(int)bounds.getWidth(),(int)bounds.getHeight());
			
			g.setColor(Color.red);
			g.fillOval((int)curcatellipse.getX(), (int)curcatellipse.getY(), (int)curcatellipse.getWidth(), (int)curcatellipse.getHeight());
			g.setColor(Color.blue);
			g.fillOval((int)nextcatellipse.getX(), (int)nextcatellipse.getY(), (int)nextcatellipse.getWidth(), (int)nextcatellipse.getHeight());
	*/
		
		}break;
		case SECOND:
		{
			if(nextcatellipse.intersects(bounds))
			{
				bounds= new Rectangle2D.Double(bounds.getX()+3,bounds.getY(),bounds.getWidth(),bounds.getHeight());
				continue adjust;
			}
			
			if(curcatellipse.intersects(bounds))
			{
				bounds= new Rectangle2D.Double(bounds.getX(),bounds.getY()+3,bounds.getWidth(),bounds.getHeight());
				continue adjust;
			}
			if(centercatellipse.intersects(bounds))
			{
				bounds= new Rectangle2D.Double(bounds.getX()+3,bounds.getY(),bounds.getWidth(),bounds.getHeight());
				continue adjust;
			}
		}break;
		case THIRD:
		{
			if(curcatellipse.intersects(bounds))
			{
				bounds= new Rectangle2D.Double(bounds.getX()-3,bounds.getY(),bounds.getWidth(),bounds.getHeight());
				continue adjust;
			}
			
			if(nextcatellipse.intersects(bounds))
			{
				bounds= new Rectangle2D.Double(bounds.getX(),bounds.getY()+4,bounds.getWidth(),bounds.getHeight());
				continue adjust;
			}
			if(centercatellipse.intersects(bounds))
			{
				bounds= new Rectangle2D.Double(bounds.getX()-3,bounds.getY(),bounds.getWidth(),bounds.getHeight());
				continue adjust;
			}
			
		}break;
		case FOURTH:{
			if(nextcatellipse.intersects(bounds))
			{
				bounds= new Rectangle2D.Double(bounds.getX()-3,bounds.getY(),bounds.getWidth(),bounds.getHeight());
				continue adjust;
			}
			
			if(curcatellipse.intersects(bounds))
			{
				bounds= new Rectangle2D.Double(bounds.getX(),bounds.getY()-3,bounds.getWidth(),bounds.getHeight());
				continue adjust;
			}
			if(centercatellipse.intersects(bounds))
			{
				bounds= new Rectangle2D.Double(bounds.getX()-3,bounds.getY(),bounds.getWidth(),bounds.getHeight());
				continue adjust;
			}
		}break;
		}
		}
		g.setColor(Color.green);
		//g.fillPolygon(p.getBounds2D());
		//Rectangle2D bounds = p.getBounds2D();
		//g.fillRect((int)bounds.getX(),(int)bounds.getY(),(int)bounds.getWidth(),(int)bounds.getHeight());
		//System.out.println("bounds now: "+bounds);
		 
		if(!transparent){
			g.setColor(Color.white);
			/*
			g.translate((int)(x2+bounds.getX()),(int)(y2+bounds.getY()));
			g.fillPolygon(p);
			g.translate((int)(-x2-bounds.getX()), (int)(-y2-bounds.getY()));
			*/
			//g.fillRect((int)bounds.getX(),(int)bounds.getY(),(int)bounds.getWidth(),(int)bounds.getHeight());
			
			
			g.translate((int)(bounds.getX()),(int)(bounds.getY()));
			g.fillPolygon(p);
			g.translate((int)(-bounds.getX()), (int)(-bounds.getY()));
			
		}
		conlines.setGlobalCorner(bounds);
		for (VisualLine line : conlines.getLines()) {
			Color cur=g.getColor();
			Font font = new Font("Serif", Font.TRUETYPE_FONT, line.fontSize);
			g.setFont(font);
		//	g.drawString(line.label, (int)(line.getX()+getX()), (int)(line.getY()+getY()));
			//g.drawString(line.label, (int)(line.getX()+x2), (int)(line.getY())+y2);
			/*
			if(!transparent){
			g.setColor(Color.white);
			g.fillRect((int)(line.getX()+x2), (int)(line.getY())+y2-(int)line.rect.getHeight(), (int)line.rect.getWidth(), (int)line.rect.getHeight());
			}
			*/
			g.setColor(Color.black);
			//Rectangle2D lm = g.getFontMetrics().getStringBounds(line.label.replaceAll("\\s", "_"), g);
			//g.drawRect((int)(line.getX()+bounds.getX()), (int)(line.getY()+bounds.getY()-lm.getHeight()), (int)lm.getWidth(),(int)lm.getHeight());
			
			//g.drawRect((int)(line.getX()+x2), (int)(line.getY())+y2-(int)line.rect.getHeight(), (int)line.rect.getWidth(), (int)line.rect.getHeight());
			//g.drawRect((int)(line.getX()+bounds.getX()), (int)(line.getY()+bounds.getY()-line.rect.getHeight()), (int)line.rect.getWidth(),(int)line.rect.getHeight());
			
			g.setColor(line.getColor());
			g.drawString(line.label, (int)(line.getX()+bounds.getX()), (int)(line.getY()+bounds.getY()));
			g.setColor(cur);
		//	System.out.println(line.getX()+","+line.getY()+":"+line.label+"|"+line.rect);
		}
		//g.translate(-(int) (corner.getX()-conlines.getRect().getWidth()), -(int) (corner.getY()-conlines.getRect().getHeight()));
		
		//g.translate((int) corner.getX(), (int) corner.getY());
		
		//g.drawString("sdsdsdfgsd", (int) getWidth()/2, (int) getHeight()/2);
		//g.drawString("sdsdsdfgsd",-(int) corner.getX(), -(int) corner.getY());
		//g.drawString(lines.getLines().get(0).label+" sdsdsdfgsd",x2,y2);
		
		//g.translate(-(int) corner.getX(), -(int) corner.getY());
		
	}
	public void setGlobalCorner(Point2D.Double p) {
		this.globalcorner=p;
		
	}

public java.awt.geom.Point2D.Double getGlobalcorner() {
	return globalcorner;
}
public Rectangle2D getMaxBounds() {
	//System.out.println(title);
	//System.out.println("globalcorner "+globalcorner+" getWidth():"+getWidth()+" getHeight():"+getHeight()+" conlines:"+conlines.getGlobalcorner());
	if(title.contains("terview"))
	{
		int x=0;
		x++;
	}
	return new Rectangle2D.Double(Math.min(Math.min(globalcorner.getX(),conlines==null||conlines.getGlobalcorner()==null?globalcorner.getX():conlines.getGlobalcorner().getX()),titlebounds.getX()),
			Math.min(Math.min(globalcorner.getY(),conlines==null||conlines.getGlobalcorner()==null?globalcorner.getY():conlines.getGlobalcorner().getY()), titlebounds.getY()-titlebounds.getHeight()),
			Math.max(Math.max(getWidth()+globalcorner.getX(),conlines==null||conlines.getGlobalcorner()==null?getWidth()+globalcorner.getX():conlines.getGlobalcorner().getX()+conlines.getRect().width),titlebounds.getX()+titlebounds.getWidth())
			,Math.max(Math.max(getHeight()+globalcorner.getY(),conlines==null||conlines.getGlobalcorner()==null?getHeight()+globalcorner.getY():conlines.getGlobalcorner().getY()+conlines.getRect().height),titlebounds.getY()));
	
}
public void computeCategory(Quadrant qa) {}
}
