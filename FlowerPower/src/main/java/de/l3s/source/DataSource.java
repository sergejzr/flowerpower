package de.l3s.source;

import java.io.File;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

public class DataSource {

	static boolean driverup = false;


	private File sourcedir;

	public DataSource(File filedir) {

		this.sourcedir = filedir;
	}

	boolean mainset = true;

	public void selectMainSet() throws FowerReadException {
		mainset = true;
		connect();
	}

	public void selectBackgroundSet() throws FowerReadException {
		mainset = false;
		connect();
	}

	public void connect() throws FowerReadException {

		Stack<File> stack = new Stack<>();
		ArrayList<File> filelist = new ArrayList<>();
		stack.add(sourcedir);

		while (!stack.empty()) {
			File f = stack.pop();
			if (f.isDirectory()) {
				stack.addAll(Arrays.asList(f.listFiles()));
			} else {
				if (f.getName().endsWith(".txt")) {
					filelist.add(f);
				}
			}
		}
		it = new TextLineIterator(filelist);
	}

	TextLineIterator it;

	DataRow thisrow = null;
	private ResultSet thisrs;

	public boolean hasNext() throws FowerReadException {

		return it.hasNext();

	}

	public DataRow getRow() {
		return it.next();
	}


}
