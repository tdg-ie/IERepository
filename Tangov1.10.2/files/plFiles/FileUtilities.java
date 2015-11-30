package plFiles;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import utilities.FoilException;

public class FileUtilities {

	
	public static void copyFile(String dstName, File src) {
		
		File dst = new File(dstName);
		if (dst.exists())
			if (!dst.delete())
				try {
					throw new FoilException("Delete " + dst.getAbsolutePath() + " failed");
				} catch (FoilException e) {
					e.printStackTrace();
				}
		
		try
		{
		     //Abrimos el archivo del cual extraeremos los datos
		    BufferedReader in = new BufferedReader(new FileReader(src));
		    //Creamos el archive destino
		    BufferedWriter out = new BufferedWriter(new FileWriter(dst));

		    //Leemos línea por línea el archivo origen
		    String p = System.getProperty("line.separator");
		    String s = in.readLine();
		    if (s != null)
		     {
		        while ((s = in.readLine()) != null)
		         {
		            //Escribimos cada línea en el archivo destino
		            out.write(s + p);
		         }
		    }
		    out.close();
		    in.close();
		}
		catch (IOException ex) {}
		}
	
	public static void copyFile(String source, String destFileName, List<String> linesToRemove) {
		
		//Construct the new file that will later be renamed to the original filename. 
		File tempFile = new File(destFileName + ".tmp");
		if (tempFile.exists())
			if (!tempFile.delete())
				try {
					throw new FoilException("Delete " + tempFile.getAbsolutePath() + " failed");
				} catch (FoilException e1) {
					e1.printStackTrace();
				}
		
		File sourceFile = new File(source);
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(sourceFile));
			PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
			String line = null;
			
			//Read from the original file and write to the new 
			//unless content matches data to be removed. 
			while ((line = br.readLine()) != null) {
				//este proceso hay que optimizarlo!!
				if (!searchLine(linesToRemove, line.trim())){ 
					pw.println(line); 
					pw.flush(); 
				} 
			}
			
			pw.close(); 
			br.close(); 

			//Delete the original file 
			if (!sourceFile.delete()) { 
				System.out.println("Could not delete file"); 
				return; 
			}
			
			//Rename the new file to the filename the original file had. 
			if (!tempFile.renameTo(sourceFile)){ 
				System.out.println("Could not rename file"); 
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static boolean searchLine(List<String> linesToRemove, String line) {
		boolean result = false;
		int i = 0;
		String s = "";
		while (!result && i < linesToRemove.size()) {
			String l = linesToRemove.get(i);
			int index = l.indexOf("= ");
			if (index != -1)
				s = l.substring(0, index);
			else
				s = l;
			if (line.startsWith(s))
				result = true;
			i++;
		}
		return result;
	}
	
	public static File createNewTextFile(String path, String name) {
		int i = 0;
		File dst = null;
		boolean end = false;
		String absolutePath = "";
		
		while (!end) {
			absolutePath = path + name + i + ".txt";
			dst = new File(absolutePath);
			if (dst.exists())
				i++;
			else 
				end = true;
		}
		File f = new File (absolutePath);
		return f;
	}
	
	public synchronized static void writeToFile(String path, String line) {
		FileWriter file = null;
        PrintWriter pw = null;
       
        try
        {
        	file = new FileWriter(path, true);
        	synchronized(file) { 
                pw = new PrintWriter(file);
                pw.print(line);
                pw.close();
        	}

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
            if (file != null)
              file.close();
            } catch (Exception e2) {
               e2.printStackTrace();
            }
         }
	}
	
	public synchronized static void closeFile(String path) {
		FileWriter file;
		try {
			file = new FileWriter(path);
			file.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}