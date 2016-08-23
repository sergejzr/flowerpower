package de.l3s.gui.topicflower;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

public class VisualBounds
{
	private List<VisualLine> lines;
	private Rectangle rect;
	private Rectangle2D globalcorner;

	
	public VisualBounds(Vector<VisualLine> conlines,int margin) {
		lines=conlines;
		Collections.sort(conlines, new Comparator<VisualLine>() {

			@Override
			public int compare(VisualLine arg0, VisualLine arg1) {
				// TODO Auto-generated method stub
				return -1
						* java.lang.Double.compare(arg0.rect.getWidth(),
								arg1.rect.getWidth());
			}
		});
		/*
		if(lines.size()==0)
		{
			//lines=Arrays.asList(arr);
			rect=new Rectangle(0, 0);

		}*/
		if(lines.size()==0)
		{
			rect=new Rectangle(0,0);
			return;
		}
		VisualLine arr[] = new VisualLine[lines.size()];
		int middleline = lines.size() / 2 - 1;
		if(middleline<0&&lines.size()==1)
		{
			middleline=0;
		}
		/*
		if(middleline>lines.size()||middleline<0)
		{
			if(lines.size()==1)
			{
				arr[0]=lines.get(0);
				rect=new Rectangle((int)arr[0].rect.getWidth(),(int)arr[0].rect.getHeight());
			}
		}
		*/
		int cnthalf = middleline;
		int i = 0;
		int cntidx = 0;
		try{
		arr[middleline] = lines.get(0);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		i = 1;
		cntidx = 1;
		/*
		 * if(lines.size()%2!=0){ middleline+=1;
		 * arr[middleline]=lines.elementAt(0); i=1; cntidx=1; }else { i }
		 */

		for (; i < cnthalf + 4; i++) {

			if (lines.size() > middleline + i) {
				arr[middleline + i] = lines.get(cntidx);
				cntidx++;
			}

			if (middleline - i > -1) {
				arr[middleline - i] = lines.get(cntidx);
				cntidx++;
			}
		}
		int height = 0;

		int width = 0;

		for (i = 0; i < arr.length; i++) {

			if (width < arr[i].rect.getWidth()) {
				width = (int) arr[i].rect.getWidth();
			}

		}

		for (i = 0; i < arr.length; i++) {
			int left = (width - (int) arr[i].rect.getWidth()) / 2;
			// if(height!=0)
			
			height += arr[i].rect.getHeight();
			
			int top = height;
			{
				height += margin;
			}
			if (width < arr[i].rect.getWidth()) {
				width = (int) arr[i].rect.getWidth();
			}
			arr[i].setBounds(left, top);
		}
lines=Arrays.asList(arr);
rect=new Rectangle(width, height);

	}
	public List<VisualLine> getLines() {
		return lines;
	}
	public Rectangle getRect() {
		return rect;
	}
	public void remove(int i) {
		while(i>0){
			
			if(lines.size()>0){
		
			lines=lines.subList(0, lines.size()-2);
	
			}
			
		i--;
		}
		
	}
	public Polygon getPoly()
	{
		Vector<Integer> xp=new Vector<Integer>();
		Vector<Integer> yp=new Vector<Integer>();
VisualLine linefirst=lines.get(0);
VisualLine linelast=lines.get(lines.size()-1);

xp.add(linefirst.getX());
xp.add(linefirst.getX());
xp.add((int)(linefirst.getX()+linefirst.rect.getWidth()));
xp.add((int)(linefirst.getX()+linefirst.rect.getWidth()));

yp.add(linefirst.getY());
yp.add((int)(linefirst.getY()-linefirst.rect.getHeight()));
yp.add((int)(linefirst.getY()-linefirst.rect.getHeight()));
yp.add(linefirst.getY());

for(int i=1;i<lines.size();i++)
{
	VisualLine line = lines.get(i);
	xp.add((int)(line.getX()+line.rect.getWidth()));
	xp.add((int)(line.getX()+line.rect.getWidth()));
	yp.add((int)(line.getY()-line.rect.getHeight()));
	yp.add((int)(line.getY()));
	
	}
for(int i=lines.size()-1;i>0;i--)
{
	VisualLine line = lines.get(i);
	xp.add((int)(line.getX()));
	xp.add((int)(line.getX()));
	yp.add((int)(line.getY()));
	yp.add((int)(line.getY()-line.rect.getHeight()));

	}
int xpoints[]=new int[xp.size()];
int ypoints[]=new int[yp.size()];

for(int i=0;i<xp.size();i++)
{
	xpoints[i]=xp.get(i);}
for(int i=0;i<yp.size();i++)
{
	ypoints[i]=yp.get(i);}

	
	return new Polygon(xpoints, ypoints, xp.size());
	}
	public void setGlobalCorner(Rectangle2D globalcorner) {
		this.globalcorner=globalcorner;
		
	}
	public Rectangle2D getGlobalcorner() {
		return globalcorner;
	}
}