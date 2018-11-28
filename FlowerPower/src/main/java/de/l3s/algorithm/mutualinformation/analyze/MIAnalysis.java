package de.l3s.algorithm.mutualinformation.analyze;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import de.l3s.source.DataRow;
import de.l3s.source.DataSource;
import de.l3s.source.FowerReadException;

public class MIAnalysis {
public static void main(String[] args) {
	MIAnalysis mia = new MIAnalysis();
	mia.run();
}

private void run() {
	File dir1,dir2;
	
	dir1=new File("posdir");
	dir2=new File("negdir");
	
	ArrayList aPositiveTrainList=new ArrayList<>();
	ArrayList aNegativeTrainList=new ArrayList<>();
	
	readDir(dir1,aPositiveTrainList);
	readDir(dir2,aNegativeTrainList);
	
	
	
	

	MIselection mis=new MIselection(aPositiveTrainList, aNegativeTrainList);
	
	mis.computePositiveAndNegativeMIvalues();
	
	
	
	
	
}

private void readDir(File dir1, ArrayList aPositiveTrainList) {
	
	List<String> l=new ArrayList<>();
	
	DataSource ds=new DataSource(dir1);
	
try {
	ds.connect();
	
	while(ds.hasNext())
	{
		DataRow row = ds.getRow();
		MIselection.addTerms(aPositiveTrainList, row.getText().split("\\s"));
	}
} catch (FowerReadException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
	
	
}
}
