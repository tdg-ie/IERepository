package walkers;

import java.io.File;
import java.io.FileFilter;

import filters.FolderFilter;

public class Walker {
	
	protected File root;
	
	public void setRoot(File root) {
		this.root = root;
	}
	
	public File getRoot() {
		return root;
	}
	
	public void run() {
		FileFilter filter;
		File categories[], sites[], attributes[];
		
		filter = FolderFilter.getInstance();
				
		categories = root.listFiles(filter);
		
		walkPreaction(root);
		for (File category : categories) {
			categoryPreaction(root, category);
			sites = category.listFiles(filter);			
			for (File site : sites) {
				sitePreaction(root, category, site);
				attributes = site.listFiles(filter);
				for (File attribute: attributes) {
					attributeAction(root, category, site, attribute);
				}
				sitePostAction(root, category, site);
			}
			categoryPostAction(root, category);
		}
		walkPostaction(root);		
	}

	public void walkPreaction(File root) {
		;
	}
	
	public void walkPostaction(File root) {
		;
	}
	
	public void categoryPreaction(File root, File category) {
		;
	}

	public void categoryPostAction(File root, File category) {
		;
	}

	public void sitePreaction(File root, File category, File site) {
		;
	}

	public void sitePostAction(File root, File category, File site) {
		;
	}

	public void attributeAction(File root, File category, File site, File attribute) {
		// System.out.printf("%s/%s/%s/%s\n", root.getName(), category.getName(), site.getName(), attribute.getName());
	}

}
