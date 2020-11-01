package OpenNL.NL_Language;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class NLReader extends BufferedReader{

	public NLReader(Reader in) {
		super(in);
		// TODO Auto-generated constructor stub
	}
	
	public String readAll() throws IOException{
		StringBuffer strbf = new StringBuffer();
		int c = -1;
		while((c=read())!=-1){
			strbf.append((char)(c));
		}
		return strbf.toString();
	}
	
}
