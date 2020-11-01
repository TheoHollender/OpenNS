package OpenNL.OpenNLAlpha2020_10;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import OpenNL.NL_Language.NLBuilder;
import OpenNL.NL_Language.NLReader;

public class TestStrBuilder {
	
	public static void main(String[] args) throws Exception{
		
		NLBuilder.build(new NLReader(new FileReader(new File("build.nl"))).readAll());
		NLBuilder.buildAllProtocols();
		
	}
	
}
