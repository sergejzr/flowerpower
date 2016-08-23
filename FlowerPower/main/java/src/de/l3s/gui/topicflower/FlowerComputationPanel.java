package de.l3s.gui.topicflower;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.JPanel;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import de.l3s.flower.Category;
import de.l3s.flower.Connection;
import de.l3s.flower.Flower;
import de.l3s.flower.TopicLink;
import de.l3s.gui.diagram.Quadrant;
import sl.shapes.RegularPolygon;

public class FlowerComputationPanel extends JPanel {

	private Flower flower;
	int k = 5;
	Font font = null;
	private double margin = 10;
	Vector<VisualCategory> vcats;
	//Vector<VisualLink> vlinks;
	private VisualCategory center;
	boolean transparent=false;
	Stroke lineStroke=new  BasicStroke(3);
	private Float globalrect;
	private Polygon weakconnectionpolygon;
	

	public FlowerComputationPanel() {
		// TODO Auto-generated constructor stub
	}
	public void compute()
	{
		Graphics g=getGraphics();




/*
		double deg = 360. / flower.getOrderedCategories().size();
		double startcenter = deg + deg / 2;
*/
		List<VisualCategory> vcats = new Vector<VisualCategory>();
		for (Category c : flower.getOrderedCategories()) {
			List<TopicLink> ot = flower.getOrderedTopics(
					c.getReprsentativetopic(), false);
			
			
			int cnt = 0;
			Vector<VisualLine> lines = new Vector<VisualLine>();
			for (TopicLink tl : ot) {
				if (cnt > k) {
					break;
				}
				getFontSize(cnt);
				lines.add(computeVisualLine(font, getFontSize(cnt),
						flower.label(tl, 3).trim(),tl.getTid()));
				cnt++;
			}

			Connection con = flower.getConnectionByLeadingCatId(c.getId());

			ot = flower.getOrderedTopics(con.getTopic(), false);
			
			cnt = 0;
			Vector<VisualLine> conlines = new Vector<VisualLine>();
			for (TopicLink tl : ot) {
				if (cnt++ > 2) {
					break;
				}
				getFontSize(cnt);
				conlines.add(computeVisualLine(font, getFontSize(cnt) - 1,
						flower.label(tl, 3).trim(),tl.getTid()));
			}

			vcats.add(new VisualCategory(c.getName(),lines, conlines,transparent));
		}

		Vector<VisualLine> genlines = new Vector<VisualLine>();
		int cnt = 0;
		for (TopicLink tl : flower.getGeneral().getTopic()) {
			if (cnt++ > k) {
				break;
			}
			getFontSize(cnt);
			genlines.add(computeVisualLine(font, getFontSize(cnt) + 3,
					flower.label(tl, 5).trim(),tl.getTid()));
		}
		center = new VisualCategory("general",genlines,transparent);
		
	
		
	//	vcats = vcats.subList(0, 9);

		double maxwidth = 0;
		double maxheight = 0;
		for (int i = 0; i < vcats.size() / 2; i++) {
			VisualCategory vc = vcats.get(i);

			if (maxwidth < vc.getCategoryBounds().getWidth())
				maxwidth = vc.getCategoryBounds().getWidth();
			if (maxheight < vc.getCategoryBounds().getHeight())
				maxheight = vc.getCategoryBounds().getHeight();
		}
		
	//	g2d.translate(maxwidth, maxheight);
		
		
		int n = vcats.size();
		double side = maxwidth + margin;
		double degree = 180. / n;
		double seg = 360. / n;
		double minr = (side / (2 * Math.sin(degree * (Math.PI / 180.))));

		double r = 0;

		double d = 2 * minr;

		if (d > side + center.getWidth()) {
			r = minr;
		} else {
			r = minr + (side+center.getWidth()-d) / 2.;
		}
//r+=r/2;
		RegularPolygon rp = new RegularPolygon((int) r, (int) r, (int) r, n,
				-Math.PI / 2);
		RegularPolygon rpx = new RegularPolygon((int) r, (int) r, (int) r, n);
		
		


		
		
		center.setGlobalCorner(new Point2D.Double((int) r - (int) center.getWidth() / 2,(int) r - (int) center.getHeight() / 2));
		

		AffineTransform afx = new AffineTransform();
		// afx.rotate(degree*(360./n/2), rp.getBounds().getWidth()+r,
		// rp.getBounds().getHeight()+r);
		afx.rotate(Math.toRadians(360. / n / 2), rp.getBounds().getWidth() / 2,
				rp.getBounds().getHeight() / 2);
		// afx.translate(-r, -r);
		// afx.rotate(angleRad);
		java.awt.Shape ss = afx.createTransformedShape(rpx);

		AffineTransform at = null;
		PathIterator pit = ss.getPathIterator(at);

		

	

		Point2D firstcat=new Point2D.Double(rp.xpoints[0],rp.ypoints[0]);
		Point2D lastcat=new Point2D.Double(rp.xpoints[rp.xpoints.length-1],rp.ypoints[rp.ypoints.length-1]);
		
		int xpoints[]=new int[]{(int)Math.min(-5,lastcat.getX()-5),(int)Math.min(-5,lastcat.getX()-5),(int)firstcat.getX()+5,(int)firstcat.getX()+5};
		int ypoints[]=new int[]{(int)lastcat.getY()+5,(int)firstcat.getY()-5,(int)firstcat.getY()-5,(int)lastcat.getY()+5};
 weakconnectionpolygon=new Polygon(xpoints,ypoints,xpoints.length);
		
		
		
	
		int centerx=(int)r, centery=(int)r;
		for (int i = 0; i < vcats.size(); i++) {
			
			int x = (int) (rp.xpoints[i]);
			int y = (int) (rp.ypoints[i]);

			Quadrant qa=null;
			
			if(x>=r&&y>rp.getBounds().getHeight() / 2) qa=Quadrant.SECOND;
			else
				if(x>=r&&y<=rp.getBounds().getHeight() / 2) qa=Quadrant.FIRST;
				else
					if(x<r&&y<=rp.getBounds().getHeight() / 2) qa=Quadrant.FOURTH;
					else
						{qa=Quadrant.THIRD;
						}

			 x = (int) (rp.xpoints[i]) - (int) vcats.get(i).getWidth() / 2;
			 y = (int) (rp.ypoints[i]) - (int) vcats.get(i).getHeight() / 2;

			
			
			
			
			
						
			
			if(qa==null){
				System.out.println("x="+x+", y="+y+",r="+r);
				}
			vcats.get(i).setGlobalCorner(new Point2D.Double( x,y));
			vcats.get(i).paintCategory(g,qa);
			
		
		/*
			int x1 = (int) (rp.xpoints[(i + 1) % vcats.size()]);
			int y1 = (int) (rp.ypoints[(i + 1) % vcats.size()]);
			x = (int) (rp.xpoints[i]);
			y = (int) (rp.ypoints[i]);

			//g.fillOval(centerx, centery, 4, 4);
			
			int x2 = x + (x1 - x) / 2;

			int y2 = y + (y1 - y) / 2;

			VisualBounds conlines = vcats.get(i).conlines;

			if (x2 < centerx) {
				x2 = (int) (x2 - conlines.getRect().getWidth());
			}
			
			if (y2 <= centery) {
				y2 = (int) (y2 - conlines.getRect().getHeight()/2);
			}
			/*
			if (y2 > centery) {
				y2 = (int) (y2 + conlines.getRect().getHeight()/2);
			}
			*/
/*
			if (x2 == centerx) {
				x2 = (int) (x2 - conlines.getRect().getWidth()/2);
				
				if (y2 > centery) {
					y2 = (int) (y2 + vcats.get(i).getHeight()/2);
				}
				if (y2 < centery) {
					y2 = (int) (y2 - vcats.get(i).getHeight()/2);
				}
			
			}
			


			vcats.get(i)
					.paintLink(g, vcats.get((i + 1) % vcats.size()), x2, y2,qa);
		*/	

			
			

		}
		r=(int)r;
		for (int i = 0; i < vcats.size(); i++) {
			//if(i>0) continue;
			int x = (int) (rp.xpoints[i]) - (int) vcats.get(i).getWidth() / 2;
			int y = (int) (rp.ypoints[i]) - (int) vcats.get(i).getHeight() / 2;
			 x = (int) (rp.xpoints[i]);
			 y = (int) (rp.ypoints[i]);

			Quadrant qa=null;
			if(x>r&&y>rp.getBounds().getHeight() / 2) qa=Quadrant.SECOND;
			else
				if(x>=r&&y<=rp.getBounds().getHeight() / 2) 
					qa=Quadrant.FIRST;
				else
					if(x<r&&y<rp.getBounds().getHeight() / 2) 
						qa=Quadrant.FOURTH;
					else
						{qa=Quadrant.THIRD;
						}
						
			
			
			int x1 = (int) (rp.xpoints[(i + 1) % vcats.size()]);
			int y1 = (int) (rp.ypoints[(i + 1) % vcats.size()]);
			 x = (int) (rp.xpoints[i]);
			 y = (int) (rp.ypoints[i]);

			//g.fillOval(centerx, centery, 4, 4);
			
			int x2 = x + (x1 - x) / 2;

			int y2 = y + (y1 - y) / 2;

			VisualBounds conlines = vcats.get(i).conlines;

			if(conlines!=null){
			if (x2 < centerx) {
				try{
				x2 = (int) (x2 - conlines.getRect().getWidth());
				}catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			
			if (y2 <= centery) {
				try{
				y2 = (int) (y2 - conlines.getRect().getHeight()/2);
				}catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			/*
			if (y2 > centery) {
				y2 = (int) (y2 + conlines.getRect().getHeight()/2);
			}
			*/

			if (x2 == centerx) {
				x2 = (int) (x2 - conlines.getRect().getWidth()/2);
				
				if (y2 > centery) {
					y2 = (int) (y2 + vcats.get(i).getHeight()/2);
				}
				if (y2 < centery) {
					y2 = (int) (y2 - vcats.get(i).getHeight()/2);
				}
			
			}
			


			vcats.get(i)
					.paintLink(g, vcats.get((i + 1) % vcats.size()), x2, y2,qa,center);
			
			}
			
			

		}
		int x = (int) r - (int) center.getWidth() / 2;
		int y = (int) r - (int) center.getHeight() / 2;

		g.translate(x, y);

		
		Graphics2D g2d = (Graphics2D)g;
		center.paintCategory(g2d,null);
		g.translate(-x, -y);
		/*
		 * Point2D.Double center=new Point2D.Double(rp.getBounds().getWidth()/2,
		 * rp.getBounds().getHeight()/2);
		 * 
		 * //g2d.rotate( Math.toRadians(360./n/2), center.getX(),center.getY());
		 * 
		 * for(int i=0;i<vcats.size();i++) {
		 * 
		 * 
		 * int x = (int)(rp.xpoints[i])-(int)vcats.get(i).getWidth()/2; int y =
		 * (int)(rp.ypoints[i])-(int)vcats.get(i).getHeight()/2;
		 * 
		 * Point2D.Double cat=new
		 * Point2D.Double((int)vcats.get(i).getWidth()/2,(
		 * int)vcats.get(i).getHeight()/2);
		 * 
		 * //g.drawLine((int)center.x, (int)center.y, (int)cat.x, (int)cat.y);
		 * //g.drawLine((int)center.x, (int)center.y, x, y);
		 * g.drawLine((int)center.x, (int)center.y, rp.xpoints[i],
		 * rp.ypoints[i]); //double angle =
		 * Math.atan2((center.x-rp.xpoints[i]),(center.y- rp.ypoints[i]));
		 * 
		 * // g2d.rotate( i*2*Math.PI/n, center.getX(),center.getY());
		 * g2d.rotate( Math.toRadians(36), rp.getBounds().getWidth()/2,
		 * rp.getBounds().getHeight()/2);
		 * 
		 * g2d.drawString(i+" trampapmpam", rp.xpoints[i], rp.ypoints[i]); //
		 * g2d.rotate( -i*2*Math.PI/n, center.getX(),center.getY());
		 * 
		 * 
		 * 
		 * 
		 * //double curangle=angle+seg/2.;
		 * 
		 * // g2d.rotate( Math.toRadians(angle), center.getX(),center.getY());
		 * 
		 * // g2d.drawString(i+" trampapmpam",(int) rp.getBounds().getWidth(),
		 * (int)rp.getBounds().getHeight()/2); //
		 * g2d.rotate(-Math.toRadians(angle), center.getX(),center.getY());
		 * 
		 * //g2d.drawLine((int)center.getX(), (int)center.getY(), x, y);
		 * 
		 * 
		 * 
		 * }
		 */
		g.setColor(Color.white);
		
		Rectangle2D maxbounds=new Rectangle2D.Double(0,0,r*2,r*2);
		for (int i = 0; i < vcats.size(); i++) 
		{
			VisualCategory curcat = vcats.get(i);
			Rectangle2D catmaxbounds=curcat.getMaxBounds();
			//System.out.println("before maxbounds "+maxbounds);
			//System.out.println("cataxbounds "+catmaxbounds);
			if(maxbounds.getX()>catmaxbounds.getX())
			{
				maxbounds=new Rectangle2D.Double(catmaxbounds.getX(),maxbounds.getY(),maxbounds.getWidth(),maxbounds.getHeight());
			}
			if(maxbounds.getWidth()<catmaxbounds.getWidth())
			{
				maxbounds=new Rectangle2D.Double(maxbounds.getX(),maxbounds.getY(),catmaxbounds.getWidth(),maxbounds.getHeight());
			}
			
			if(maxbounds.getY()>catmaxbounds.getY())
			{
				maxbounds=new Rectangle2D.Double(maxbounds.getX(),catmaxbounds.getY(),maxbounds.getWidth(),maxbounds.getHeight());
			}
			if(maxbounds.getHeight()<catmaxbounds.getHeight())
			{
				maxbounds=new Rectangle2D.Double(maxbounds.getX(),maxbounds.getY(),maxbounds.getWidth(),catmaxbounds.getHeight());
			}
		//	System.out.println("Maxbounds "+maxbounds);
		}
		

		
		g.setColor(Color.black);
		g.drawRect((int)maxbounds.getX(), (int)maxbounds.getY(), (int)(maxbounds.getWidth()-maxbounds.getX()), (int)(maxbounds.getHeight()-maxbounds.getY()));
		g.setColor(Color.white);
		globalrect=new Rectangle2D.Float((int)maxbounds.getX(), (int)maxbounds.getY(), (int)(maxbounds.getWidth()-maxbounds.getX()), (int)(maxbounds.getHeight()-maxbounds.getY()));
	
		
	}

	@Override
	public void paint(Graphics g) {

		super.paint(g);
	if(!transparent){
		g.setColor(Color.white);
		g.fillRect(0, 0, 1000, 1000);
	}
		g.setColor(Color.black);
		Graphics2D g2d = (Graphics2D) g;

	
		if (font == null) {
			font = g.getFont();
		}
		double deg = 360. / flower.getOrderedCategories().size();
		double startcenter = deg + deg / 2;

		List<VisualCategory> vcats = new Vector<VisualCategory>();
		for (Category c : flower.getOrderedCategories()) {
			List<TopicLink> ot = flower.getOrderedTopics(
					c.getReprsentativetopic(), false);
			g.setFont(font);
			int cnt = 0;
			Vector<VisualLine> lines = new Vector<VisualLine>();
			for (TopicLink tl : ot) {
				if (cnt > k) {
					break;
				}
				getFontSize(cnt);
				lines.add(computeVisualLine(font, getFontSize(cnt),
						flower.label(tl, 3).trim(),tl.getTid()));
				cnt++;
			}

			Connection con = flower.getConnectionByLeadingCatId(c.getId());

			ot = flower.getOrderedTopics(con.getTopic(), false);
			g.setFont(font);
			cnt = 0;
			Vector<VisualLine> conlines = new Vector<VisualLine>();
			for (TopicLink tl : ot) {
				if (cnt++ > 2) {
					break;
				}
				getFontSize(cnt);
				conlines.add(computeVisualLine( font, getFontSize(cnt) - 1,
						flower.label(tl, 3).trim(),tl.getTid()));
			}

			vcats.add(new VisualCategory(c.getName(),lines, conlines,transparent));
		}

		Vector<VisualLine> genlines = new Vector<VisualLine>();
		int cnt = 0;
		for (TopicLink tl : flower.getGeneral().getTopic()) {
			if (cnt++ > k) {
				break;
			}
			getFontSize(cnt);
			genlines.add(computeVisualLine( font, getFontSize(cnt) + 3,
					flower.label(tl, 5).trim(),tl.getTid()));
		}
		center = new VisualCategory("general",genlines,transparent);
		
	
		
	//	vcats = vcats.subList(0, 9);

		double maxwidth = 0;
		double maxheight = 0;
		for (int i = 0; i < vcats.size() / 2; i++) {
			VisualCategory vc = vcats.get(i);

			if (maxwidth < vc.getCategoryBounds().getWidth())
				maxwidth = vc.getCategoryBounds().getWidth();
			if (maxheight < vc.getCategoryBounds().getHeight())
				maxheight = vc.getCategoryBounds().getHeight();
		}
		
		g2d.translate(maxwidth, maxheight);
		
		
		int n = vcats.size();
		double side = maxwidth + margin;
		double degree = 180. / n;
		double seg = 360. / n;
		double minr = (side / (2 * Math.sin(degree * (Math.PI / 180.))));

		double r = 0;

		double d = 2 * minr;

		if (d > side + center.getWidth()) {
			r = minr;
		} else {
			r = minr + (side+center.getWidth()-d) / 2.;
		}
//r+=r/2;
		RegularPolygon rp = new RegularPolygon((int) r, (int) r, (int) r, n,
				-Math.PI / 2);
		RegularPolygon rpx = new RegularPolygon((int) r, (int) r, (int) r, n);
		
		


		
		
		center.setGlobalCorner(new Point2D.Double((int) r - (int) center.getWidth() / 2,(int) r - (int) center.getHeight() / 2));
		

		AffineTransform afx = new AffineTransform();
		// afx.rotate(degree*(360./n/2), rp.getBounds().getWidth()+r,
		// rp.getBounds().getHeight()+r);
		afx.rotate(Math.toRadians(360. / n / 2), rp.getBounds().getWidth() / 2,
				rp.getBounds().getHeight() / 2);
		// afx.translate(-r, -r);
		// afx.rotate(angleRad);
		java.awt.Shape ss = afx.createTransformedShape(rpx);

		AffineTransform at = null;
		PathIterator pit = ss.getPathIterator(at);

		g.setColor(Color.black);
		//g.drawPolygon(rp);

		g.setColor(Color.green);

		// g2d.draw(ss);
		g.setColor(Color.black);

		Stroke curstroke = g2d.getStroke();
		g2d.setStroke(new BasicStroke(3));
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		
		g.drawOval(0, 0, (int) r * 2, (int) r * 2);
		
		
		
g2d.setStroke(curstroke);
		setBounds(0, 0, rp.getBounds().height + 100 + 500,
				rp.getBounds().width + 100 + 500);
		// Graphics2D g2d =(Graphics2D) g;

		Point2D firstcat=new Point2D.Double(rp.xpoints[0],rp.ypoints[0]);
		Point2D lastcat=new Point2D.Double(rp.xpoints[rp.xpoints.length-1],rp.ypoints[rp.ypoints.length-1]);
		
		int xpoints[]=new int[]{(int)Math.min(-5,lastcat.getX()-5),(int)Math.min(-5,lastcat.getX()-5),(int)firstcat.getX()+5,(int)firstcat.getX()+5};
		int ypoints[]=new int[]{(int)lastcat.getY()+5,(int)firstcat.getY()-5,(int)firstcat.getY()-5,(int)lastcat.getY()+5};
		// g.drawPolygon(rp);
	Color tc=g.getColor();
	g.setColor(Color.white);
		g.fillPolygon(xpoints, ypoints, xpoints.length);
		
		g.setColor(tc);
		//g.drawPolygon(xpoints, ypoints, xpoints.length);
		Stroke tmstroke = g2d.getStroke();
		 float dash[] = { 10.0f };
		
		g2d.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
		        BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
		r=(int)r;
		g2d.drawOval(0, 0, (int) r * 2, (int) r * 2);
		
		g2d.setStroke(tmstroke);
		int centerx=(int)r, centery=(int)r;
		for (int i = 0; i < vcats.size(); i++) {
			
			int x = (int) (rp.xpoints[i]);
			int y = (int) (rp.ypoints[i]);

			Quadrant qa=null;
			
			if(x>=r&&y>rp.getBounds().getHeight() / 2) qa=Quadrant.SECOND;
			else
				if(x>=r&&y<=rp.getBounds().getHeight() / 2) qa=Quadrant.FIRST;
				else
					if(x<r&&y<=rp.getBounds().getHeight() / 2) qa=Quadrant.FOURTH;
					else
						{qa=Quadrant.THIRD;
						}

			 x = (int) (rp.xpoints[i]) - (int) vcats.get(i).getWidth() / 2;
			 y = (int) (rp.ypoints[i]) - (int) vcats.get(i).getHeight() / 2;

			
			
			g.translate(x, y);
			
			
						
			
			if(qa==null){
				System.out.println("x="+x+", y="+y+",r="+r);
				}
			vcats.get(i).setGlobalCorner(new Point2D.Double( x,y));
			vcats.get(i).paintCategory(g,qa);
			
			g.translate(-x, -y);
		/*
			int x1 = (int) (rp.xpoints[(i + 1) % vcats.size()]);
			int y1 = (int) (rp.ypoints[(i + 1) % vcats.size()]);
			x = (int) (rp.xpoints[i]);
			y = (int) (rp.ypoints[i]);

			//g.fillOval(centerx, centery, 4, 4);
			
			int x2 = x + (x1 - x) / 2;

			int y2 = y + (y1 - y) / 2;

			VisualBounds conlines = vcats.get(i).conlines;

			if (x2 < centerx) {
				x2 = (int) (x2 - conlines.getRect().getWidth());
			}
			
			if (y2 <= centery) {
				y2 = (int) (y2 - conlines.getRect().getHeight()/2);
			}
			/*
			if (y2 > centery) {
				y2 = (int) (y2 + conlines.getRect().getHeight()/2);
			}
			*/
/*
			if (x2 == centerx) {
				x2 = (int) (x2 - conlines.getRect().getWidth()/2);
				
				if (y2 > centery) {
					y2 = (int) (y2 + vcats.get(i).getHeight()/2);
				}
				if (y2 < centery) {
					y2 = (int) (y2 - vcats.get(i).getHeight()/2);
				}
			
			}
			


			vcats.get(i)
					.paintLink(g, vcats.get((i + 1) % vcats.size()), x2, y2,qa);
		*/	

			
			

		}
		r=(int)r;
		for (int i = 0; i < vcats.size(); i++) {
			//if(i>0) continue;
			int x = (int) (rp.xpoints[i]) - (int) vcats.get(i).getWidth() / 2;
			int y = (int) (rp.ypoints[i]) - (int) vcats.get(i).getHeight() / 2;
			 x = (int) (rp.xpoints[i]);
			 y = (int) (rp.ypoints[i]);

			Quadrant qa=null;
			if(x>r&&y>rp.getBounds().getHeight() / 2) qa=Quadrant.SECOND;
			else
				if(x>=r&&y<=rp.getBounds().getHeight() / 2) 
					qa=Quadrant.FIRST;
				else
					if(x<r&&y<rp.getBounds().getHeight() / 2) 
						qa=Quadrant.FOURTH;
					else
						{qa=Quadrant.THIRD;
						}
						
			
			
			int x1 = (int) (rp.xpoints[(i + 1) % vcats.size()]);
			int y1 = (int) (rp.ypoints[(i + 1) % vcats.size()]);
			 x = (int) (rp.xpoints[i]);
			 y = (int) (rp.ypoints[i]);

			//g.fillOval(centerx, centery, 4, 4);
			
			int x2 = x + (x1 - x) / 2;

			int y2 = y + (y1 - y) / 2;

			VisualBounds conlines = vcats.get(i).conlines;

			if(conlines!=null){
			if (x2 < centerx) {
				try{
				x2 = (int) (x2 - conlines.getRect().getWidth());
				}catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			
			if (y2 <= centery) {
				try{
				y2 = (int) (y2 - conlines.getRect().getHeight()/2);
				}catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			/*
			if (y2 > centery) {
				y2 = (int) (y2 + conlines.getRect().getHeight()/2);
			}
			*/

			if (x2 == centerx) {
				x2 = (int) (x2 - conlines.getRect().getWidth()/2);
				
				if (y2 > centery) {
					y2 = (int) (y2 + vcats.get(i).getHeight()/2);
				}
				if (y2 < centery) {
					y2 = (int) (y2 - vcats.get(i).getHeight()/2);
				}
			
			}
			


			vcats.get(i)
					.paintLink(g, vcats.get((i + 1) % vcats.size()), x2, y2,qa,center);
			
			}
			
			

		}
		int x = (int) r - (int) center.getWidth() / 2;
		int y = (int) r - (int) center.getHeight() / 2;

		g.translate(x, y);

		
		
		center.paintCategory(g2d,null);
		g.translate(-x, -y);
		/*
		 * Point2D.Double center=new Point2D.Double(rp.getBounds().getWidth()/2,
		 * rp.getBounds().getHeight()/2);
		 * 
		 * //g2d.rotate( Math.toRadians(360./n/2), center.getX(),center.getY());
		 * 
		 * for(int i=0;i<vcats.size();i++) {
		 * 
		 * 
		 * int x = (int)(rp.xpoints[i])-(int)vcats.get(i).getWidth()/2; int y =
		 * (int)(rp.ypoints[i])-(int)vcats.get(i).getHeight()/2;
		 * 
		 * Point2D.Double cat=new
		 * Point2D.Double((int)vcats.get(i).getWidth()/2,(
		 * int)vcats.get(i).getHeight()/2);
		 * 
		 * //g.drawLine((int)center.x, (int)center.y, (int)cat.x, (int)cat.y);
		 * //g.drawLine((int)center.x, (int)center.y, x, y);
		 * g.drawLine((int)center.x, (int)center.y, rp.xpoints[i],
		 * rp.ypoints[i]); //double angle =
		 * Math.atan2((center.x-rp.xpoints[i]),(center.y- rp.ypoints[i]));
		 * 
		 * // g2d.rotate( i*2*Math.PI/n, center.getX(),center.getY());
		 * g2d.rotate( Math.toRadians(36), rp.getBounds().getWidth()/2,
		 * rp.getBounds().getHeight()/2);
		 * 
		 * g2d.drawString(i+" trampapmpam", rp.xpoints[i], rp.ypoints[i]); //
		 * g2d.rotate( -i*2*Math.PI/n, center.getX(),center.getY());
		 * 
		 * 
		 * 
		 * 
		 * //double curangle=angle+seg/2.;
		 * 
		 * // g2d.rotate( Math.toRadians(angle), center.getX(),center.getY());
		 * 
		 * // g2d.drawString(i+" trampapmpam",(int) rp.getBounds().getWidth(),
		 * (int)rp.getBounds().getHeight()/2); //
		 * g2d.rotate(-Math.toRadians(angle), center.getX(),center.getY());
		 * 
		 * //g2d.drawLine((int)center.getX(), (int)center.getY(), x, y);
		 * 
		 * 
		 * 
		 * }
		 */
		g.setColor(Color.white);
		
		Rectangle2D maxbounds=new Rectangle2D.Double(0,0,r*2,r*2);
		for (int i = 0; i < vcats.size(); i++) 
		{
			VisualCategory curcat = vcats.get(i);
			Rectangle2D catmaxbounds=curcat.getMaxBounds();
			//System.out.println("before maxbounds "+maxbounds);
			//System.out.println("cataxbounds "+catmaxbounds);
			if(maxbounds.getX()>catmaxbounds.getX())
			{
				maxbounds=new Rectangle2D.Double(catmaxbounds.getX(),maxbounds.getY(),maxbounds.getWidth(),maxbounds.getHeight());
			}
			if(maxbounds.getWidth()<catmaxbounds.getWidth())
			{
				maxbounds=new Rectangle2D.Double(maxbounds.getX(),maxbounds.getY(),catmaxbounds.getWidth(),maxbounds.getHeight());
			}
			
			if(maxbounds.getY()>catmaxbounds.getY())
			{
				maxbounds=new Rectangle2D.Double(maxbounds.getX(),catmaxbounds.getY(),maxbounds.getWidth(),maxbounds.getHeight());
			}
			if(maxbounds.getHeight()<catmaxbounds.getHeight())
			{
				maxbounds=new Rectangle2D.Double(maxbounds.getX(),maxbounds.getY(),maxbounds.getWidth(),catmaxbounds.getHeight());
			}
		//	System.out.println("Maxbounds "+maxbounds);
		}
		

		
		g.setColor(Color.black);
		g.drawRect((int)maxbounds.getX(), (int)maxbounds.getY(), (int)(maxbounds.getWidth()-maxbounds.getX()), (int)(maxbounds.getHeight()-maxbounds.getY()));
		g.setColor(Color.white);
		globalrect=new Rectangle2D.Float((int)maxbounds.getX(), (int)maxbounds.getY(), (int)(maxbounds.getWidth()-maxbounds.getX()), (int)(maxbounds.getHeight()-maxbounds.getY()));
	}

public int getMinWidth() {return (int)globalrect.getWidth();};
public int getMinHeigth() {return (int)globalrect.getHeight();};
	Hashtable<String, FontMetrics> fmidx = new Hashtable<String, FontMetrics>();
Hashtable<Integer, Color> coloridx=new Hashtable<Integer, Color>();
	private VisualLine computeVisualLine(Font font, int fontSize,
			String label,Integer topicid) {

		Graphics g = getGraphics();
		String key = font.getFontName() + "|" + fontSize;
		FontMetrics fm = fmidx.get(key);
		if (fm == null) {
			Font cfont = new Font("Serif", Font.BOLD, fontSize);

			fm = g.getFontMetrics(cfont);
			fmidx.put(key, fm);
		}

		Font curfont = g.getFont();
		Font cfont = new Font("Serif", Font.TRUETYPE_FONT, fontSize);
		g.setFont(cfont);
		Rectangle2D rect = fm.getStringBounds(label, g);

		g.setFont(curfont);

		return new VisualLine(label, fontSize, fm, rect,getColorFor(topicid));
	}
private String selectrandomcolor() {
		
		Vector<String> shuf=new Vector<String>();
		shuf.addAll(Arrays.asList("481d2d,953f12,1f1040,140b26,682921".toUpperCase().split(",")));
		Collections.shuffle(shuf);
		return "#"+shuf.get(0);
	}
	private Color getColorFor(Integer topicid) {
		Color color = coloridx.get(topicid);
		if(color==null)
		{
			String colorstr=selectrandomcolor();
coloridx.put(topicid,color=Color.decode(colorstr));


		}
		return color;
	}

	private int getFontSize(int cnt) {
		int fontsize = 10;
		switch (cnt) {
		case 0:
			fontsize = 16;
			break;
		case 1:
			fontsize = 12;
			break;
		case 2:
			fontsize = 12;
			break;
		case 3:
			fontsize = 10;
			break;
		case 4:
			fontsize = 10;
			break;

		}
		return fontsize;
	}

	public void setFlower(Flower flower) {
		this.flower = flower;
	}

	public void setFlower(File flowerfile) throws JAXBException {
		this.flower = readFlower(flowerfile);
	}

	private Flower readFlower(File flowerfile) throws JAXBException {
		Flower flower = null;
		JAXBContext jaxbContext = JAXBContext.newInstance(Flower.class);
		if (flowerfile.exists()) {

			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			flower = (Flower) jaxbUnmarshaller.unmarshal(flowerfile);

		} else {
		}
		return flower;
	}
}
