package filters;

import java.io.File;
import java.io.FileFilter;

public class FolderFilter implements FileFilter {

	protected static FileFilter instance;
	
	static {
		instance = new FolderFilter();
	}
		
	public static FileFilter getInstance() {
		return instance;
	}
		
	public boolean accept(File file) {
		return file.isDirectory();
	}
		
}
