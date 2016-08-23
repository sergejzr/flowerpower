package de.l3s.gui.topicflower;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import de.l3s.flower.Category;
import de.l3s.flower.Connection;
import de.l3s.flower.Flower;
import de.l3s.flower.TopicLink;
import de.l3s.gui.diagram.Quadrant;
import sl.shapes.RegularPolygon;

public class FlowerImage  {

	private Flower flower;
	int k = 5;
	Font font = null;
	private double margin = 10;
	Vector<VisualCategory> vcats;
	Vector<VisualLink> vlinks;
	private VisualCategory center;
	boolean transparent=false;
	private Float globalrect;
	BufferedImage bi=null;
int shiftmargin=1000;

	public void paint() {
	
		Graphics g;
		int yy,xx,ww,hh;
		if(bi==null){
	 bi=new BufferedImage(10000,10000, BufferedImage.TYPE_INT_ARGB);
		g=bi.getGraphics();
		
		
	//	super.paint(g);
	if(!transparent){
		g.setColor(Color.white);
		g.fillRect(0, 0, 10000, 10000);
	}
	g.translate(shiftmargin, shiftmargin);
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
				lines.add(computeVisualLine(g, font, getFontSize(cnt),
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
				conlines.add(computeVisualLine(g, font, getFontSize(cnt) - 1,
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
			genlines.add(computeVisualLine(g, font, getFontSize(cnt) + 3,
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
		
		g.setColor(Color.white);
		
		Rectangle2D maxbounds=new Rectangle2D.Double(0,0,r*2,r*2);
		for (int i = 0; i < vcats.size(); i++) 
		{
			VisualCategory curcat = vcats.get(i);
			Rectangle2D catmaxbounds=curcat.getMaxBounds();
			
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
	
		}
		
		g.setColor(Color.black);
		/*
		g.drawRect(
				(int)maxbounds.getX(),
				(int)maxbounds.getY(), 
				(int)(maxbounds.getWidth()-maxbounds.getX()),
				(int)(maxbounds.getHeight()-maxbounds.getY()));
		*/
				System.out.println("x: "+maxbounds.getX());
				System.out.println("y: "+maxbounds.getY());
		//g.setColor(Color.black);
				g.setColor(new Color(1, 0, 0, 1));
				g.drawOval((int)maxbounds.getX(),
				(int)maxbounds.getY(), 1, 1);
				/*
		g.drawRect(
				(int)maxbounds.getX(),
				(int)maxbounds.getY(), 
				(int)(maxbounds.getWidth()-maxbounds.getX()),
				(int)(maxbounds.getHeight()-maxbounds.getY()));
		*/
	
		float alpha = 0.5f;
		Color color = new Color(1, 0, 0, alpha); //Red 
		g.setColor(color);
		boolean firstresize=false;
		if(globalrect==null){firstresize=true;}
		globalrect=new Rectangle2D.Float(xx=(int)maxbounds.getX(),yy= (int)maxbounds.getY(), ww=(int)(maxbounds.getWidth()-maxbounds.getX()+1), hh=(int)(maxbounds.getHeight()-maxbounds.getY()+1));
	//ww+=200;
	//hh+=200;
		//	g.fillRect(xx=(int)maxbounds.getX(), yy=(int)maxbounds.getY(), ww=(int)(maxbounds.getWidth()-maxbounds.getX()+margin), hh=(int)(maxbounds.getHeight()-maxbounds.getY()+margin));
	Point corner=getCorner(bi, ww/2+shiftmargin,hh/2+shiftmargin);
		{
			System.out.println("x: "+(int)(xx-shiftmargin+margin-maxwidth) + "\t"+corner.x);
			System.out.println("y: "+(int)(yy-shiftmargin-maxheight) + "\t"+corner.y);
			BufferedImage bi2 = new BufferedImage((int)(ww),(int)(hh), BufferedImage.TYPE_INT_ARGB);
			Graphics gnew = bi2.getGraphics();
			//gnew.drawImage(bi, (int)(xx-500+margin), (int)(yy-500), null);
			gnew.drawImage(bi, (int)(-corner.x),(int)(-corner.y), null);
			bi=bi2;
		}
			
	
		}

		
		
		
	}
private Point getCorner(BufferedImage bi, int boundx, int boundy) {
/*
	for(int i=0;i<bi.getWidth();i++)
		for(int y=0;y<bi.getHeight();y++)
	{
 int pixel = bi.getRGB(i, y);
if(pixel!=-1) return new Point(i,y);
	}
		return new Point(0,0);
		*/
	int curlen=0;

	int tocheck=boundx>boundy?boundx:boundy;

	while(true){
	for(int i=0;i<curlen;i++)
	{
		 int pixel = bi.getRGB( curlen,i);
		 //System.out.println("P("+curlen+","+i+")");
		 if(pixel!=-1) return new Point(curlen,i);
	}
		for(int y=0;y<curlen;y++)
	{
			 int pixel = bi.getRGB(y,curlen);
			// System.out.println("P("+y+","+curlen+")");
			 if(pixel!=-1) return new Point(y,curlen);
	}
	if(curlen>=tocheck)
		break;
	curlen++;
	}
		return new Point(0,0);
		
	}
/*
public int getMinWidth() 
{
	if(globalrect==null) return 1000;
	return (int)globalrect.getWidth();
}
public int getMinHeigth() 
{
	if(globalrect==null) return 1000;
	return (int)globalrect.getHeight();
}
public int getWidth() {
	return getMinWidth();
	
};
public int getHeight() {
	return getMinHeigth();
	};
	public java.awt.Dimension getMaximumSize() {return new Dimension(2000,2000);};
	
	public Dimension getMinimumSize() {return new Dimension(2000,2000);};
	*/
	Hashtable<String, FontMetrics> fmidx = new Hashtable<String, FontMetrics>();
Hashtable<Integer, Color> coloridx=new Hashtable<Integer, Color>();
	private VisualLine computeVisualLine(Graphics g, Font font, int fontSize,
			String label,Integer topicid) {

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
	public BufferedImage getFlowerImage() {return bi;}
	
}
