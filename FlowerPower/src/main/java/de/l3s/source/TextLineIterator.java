package de.l3s.source;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

public class TextLineIterator implements Iterator<DBFileRow> {
	Iterator<File> it;
	Scanner scan;
File curfile=null;
	public TextLineIterator(ArrayList<File> filelist) {
		it = filelist.iterator();
		if (it.hasNext()) {
			File f = it.next();
			curfile=f;
			try {
				scan = new Scanner(f);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean hasNext() {
		if (scan.hasNext()) {
			return true;
		}

		if (!it.hasNext()) {
			return false;
		}
		try {
			scan = new Scanner(curfile=it.next());
			return hasNext();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

	}
int cnt=1;
	@Override
	public DBFileRow next() {
		return new DBFileRow(curfile.getParentFile().getName(),curfile.getName()+"_"+cnt,cnt+"",scan.nextLine());
		
	}

}
