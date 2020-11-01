package OpenNL.NL_Language;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class NLClassLoader {

	public static Class loadString(String source) throws IOException{
		
		File root = new File("NLBuilds/Classes/");
		File sourceFile = new File(root, "build.java");
		
		System.out.println(sourceFile.toPath());
		sourceFile.createNewFile();
		
		Files.write(sourceFile.toPath(), source.getBytes(StandardCharsets.UTF_8));
		
		Runtime r = Runtime.getRuntime();
		Process pr = r.exec(new String[] {"ls -l /"});
		int m = pr.getInputStream().read();
		while(m!=-1){
			System.out.println((char)(m));
			m = pr.getInputStream().read();
		}
		
		pr = r.exec(new String[] {"cd NLBuilds/", "javac cvfe Jar.jar Classes *.class"});
		
		
		
		return null;
	}
	
}
