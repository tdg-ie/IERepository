package filters;

import java.io.File;
import java.io.FileFilter;

public class KnowledgeBaseFilter implements FileFilter {
	
	protected static FileFilter instance;
	
	static {
		instance = new KnowledgeBaseFilter();
	}
		
	public static FileFilter getInstance() {
		return instance;
	}
	
	public boolean accept(File file) {
		return file.isFile() && file.getName().endsWith(".pl");
	}
	
}