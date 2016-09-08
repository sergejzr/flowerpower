package de.l3s.gui.topicflower;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.bind.JAXBException;

import de.l3s.flower.Category;
import de.l3s.flower.Flower;
import de.l3s.flower.TopicLink;

public class FlowerFrame extends JFrame{
	JButton savebutton;
	FlowerPanel fp;
	static int globcnt=0;
	String flowertitle;

public FlowerFrame(File dir, String flowerxml) {
	
	globcnt++;
	 fp=new FlowerPanel();
	
	 flowertitle=flowerxml.substring(0, flowerxml.length()-4);
		
	setTitle(flowertitle);
	
	Flower flower;
	try {
		flower = Flower.readFlower(new File(dir,flowerxml));
		List<TopicLink> top3 = flower.getGeneral().getTopic().subList(0, 2);
		
		int numWords=120;
		int numGeneralTopics= 3;
		int numConnectionTopics= 2;
		int numTopicPerCat= numWords / (flower.getCategories().getCategory().size()*3);
		
		flower.getGeneral().setTopic(top3);
		for(Category cat :flower.getCategories().getCategory())
		{
			
			cat.getTopic().subList(0, numTopicPerCat);
		}
		fp.setFlower(flower);
		fp.repaint();
	
		/*
		getContentPane().add(fp,BorderLayout.CENTER);
		setBounds(100, 100, 500, 500);
		setVisible(true);
		*/
		//f.pack();
	//	fp.resizePreferred();
		getContentPane().setLayout(new BorderLayout());
	
		getContentPane().add(new JScrollPane(fp),BorderLayout.CENTER);
		getContentPane().add(savebutton=new JButton("Save Image"), BorderLayout.NORTH);
		savebutton.addActionListener(new ActionListener() {
			
			private File curdir;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				/*
				 BufferedImage bi=fp.getFlowerImage();
				 JFrame im=new JFrame();
				 JLabel lab=new JLabel();
				 lab.setIcon(new ImageIcon(bi));
				 im.getContentPane().add(lab);
				 im.pack();
				 im.setVisible(true);
				 im.addWindowListener(new WindowAdapter() {
					 @Override
					public void windowClosing(WindowEvent e) {
						 ((JFrame)e.getSource() ).dispose();
						((JFrame)e.getSource() ).setVisible(false);
					}
				});
			*/
				
				
				 
				    JFileChooser chooser = new JFileChooser();
				   
				    chooser.addChoosableFileFilter(new FileNameExtensionFilter("PNG","png"));
				    if(curdir==null) curdir=new File("");
				    chooser.setCurrentDirectory(curdir);
				chooser.setSelectedFile(new File(flowertitle+".png"));
				    int retrival = chooser.showSaveDialog(null);
				    File f = chooser.getSelectedFile();
				   if(f!=null) curdir=f.getParentFile();
				    if (retrival == JFileChooser.APPROVE_OPTION) {
				    	writeToPNG(f);
				    }
			}

			
		});
	
		
	} catch (JAXBException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	

	addWindowListener(new WindowAdapter() {
		@Override
		public void windowClosed(WindowEvent e) {
			// TODO Auto-generated method stub
			if(--globcnt==0){
			super.windowClosed(e);
			
			System.exit(0);
			}
		}
		@Override
		public void windowClosing(WindowEvent e) {
			// TODO Auto-generated method stub
			if(--globcnt==0){
			super.windowClosed(e);
			System.exit(0);
			}
		}
	});
	
}
public static void convert(File flowerxml_in, File flowerimg_outdir)
{
	FlowerFrame f=new FlowerFrame(flowerxml_in.getParentFile(),flowerxml_in.getName());
	//f.repaint();
	//f.setVisible(true);
	//f.setVisible(false);
	f.writeToPNG(new File(flowerimg_outdir,f.flowertitle+".png"));
	
}

public static void convertIm(File flowerxml_in, File flowerimg_outdir)
{
	FlowerImage fi=new FlowerImage();
	try {	 
		
		String flowertitle=flowerxml_in.getName().substring(0, flowerxml_in.getName().length()-4);
		
		Flower flower = Flower.readFlower(flowerxml_in);
		fi.setFlower(flower);
		
		
		
		fi.paint();
	 BufferedImage bi=fi.getFlowerImage();
	 
	 ImageIO.write(bi, "png", new File(flowerimg_outdir,flowertitle+".png"));

		fi.setFlower(flowerxml_in);
	} catch (JAXBException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

}

private void writeToPNG(File f) {
    try {
    	 BufferedImage bi=fp.getFlowerImage();
    	
    	 if(!f.getName().endsWith(".png"))
    	 {
    		f=new File(f.getParent(),f.getName()+".png");
    	 }
    	 ImageIO.write(bi, "png", f);
      
    } catch (Exception ex) {
        ex.printStackTrace();
    }
}
public static void main(String[] args) {
	File dir = new File("C:\\Users\\singh\\sandbox\\FlowerPower");
	if (!dir.exists()) {
		dir = new File("/data3/zerr/autoflowersvilnus");
		dir=new File("/data3/zerr/flowers");
	}
	if (!dir.exists()) {
		dir = new File("/media/zerr/BA0E0E3E0E0DF3E3/twitter_cache/");
	}
	File outdir= new File("/media/zerr/BA0E0E3E0E0DF3E3/twitter_cache/");
	if(!outdir.exists()){outdir.mkdirs();}
	for(File cf:dir.listFiles()){
		if(!cf.toString().endsWith("xml")){continue;}
		
		convertIm(cf,outdir);
		/*
	//	if(!cf.toString().contains("christianity")){continue;}
	//	if(!cf.toString().contains("1000")){continue;}
		//if(!cf.toString().contains("flower_wikimovies_nopersons_1000.xml")){continue;}
		if(cf.toString().contains("auto5000")){continue;}
	//	if(!cf.toString().contains("rowtopocs_wikimovies")||!cf.toString().contains("_500")){continue;}
//		if(!cf.toString().contains("auto5000")||!cf.toString().contains("_500")){continue;}
		
	FlowerFrame f=new FlowerFrame(dir,cf.getName()
			//"flower_wikimovies_nopersons_500.xml"
			
			);
	//f.convert(flowerxml_in, flowerimg_outdir)
	
	//f.getContentPane().add(fp,BorderLayout.CENTER);
	f.setBounds(100, 100, 500, 500);
	f.setVisible(true);
	break;
	*/
	}

	
	
}
}
