package output;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class OutputDataFile {
	
	private OutputStream out;
	private OutputStream outBuffer = null;
	
	public OutputDataFile(File f) throws FileNotFoundException{
		this.out = new FileOutputStream(f,true);
		this.outBuffer = new BufferedOutputStream(out);
	}
	
	public void writeToFile(String text) throws IOException{
//		out.write(text.getBytes());
		this.outBuffer.write(text.getBytes());
	}
	
//	private void writeToFile(StringBuffer buffer) throws IOException{
//		byte[] totalBytes = new byte[buffer.length()];
//		totalBytes = buffer.toString().getBytes();
//        out.write(totalBytes);
//        
//	}
	public void flush() throws IOException {
		outBuffer.flush();
	}
	
	public void closeFile() throws IOException{
		outBuffer.flush();
		if (this.outBuffer != null)
			 this.outBuffer.close();
		 if(this.out !=null)
        	 this.out.close();
	}
}
