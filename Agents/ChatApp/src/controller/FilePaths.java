package controller;

import java.io.File;

public class FilePaths {

	public static File getPath(String s){
		File file = new File(s);
		File pathDatabase = new File(file.getParent()+"//DataBase");
		if(!pathDatabase.exists()){
			pathDatabase.mkdir();
		}
		return pathDatabase;
	}
	
	public static File getMediaPath(String s){
		File pathDatabase = new File(s);
		if(!pathDatabase.exists()){
			pathDatabase.mkdir();
		}
		return pathDatabase;
	}
}
