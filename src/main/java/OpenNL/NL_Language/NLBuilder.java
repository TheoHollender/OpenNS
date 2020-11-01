package OpenNL.NL_Language;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import OpenNL.Protocols.BuiltInProtocol;

public class NLBuilder {

	private static ArrayList<String> strings = new ArrayList<String>();
	private static ArrayList<ArrayList<LineState>> states = new ArrayList();
	private static ArrayList<ArrayList<NLMethod>> methods = new ArrayList<ArrayList<NLMethod>>();
	
	private static ArrayList<BuiltInProtocol> protCreated = new ArrayList<BuiltInProtocol>();
	
	private static enum LineState{
		REQUIRE("require"), DEF("define"), ENDDEF("enddefine"), 
		CONSTR("construct"), ENDCONSTR("endconstruct"), IN("in"), OUT("out"), NONE("");
		LineState(String beg){
			this.beg=beg;
		}
		String beg;
	}

	private static String getRequires(String s){
		
		return "";
	}
	
	public static void buildAllProtocols(){
		File dir = new File("NLBuilds/");
		if(!dir.exists()){
			dir.mkdir();
		}

		dir = new File("NLBuilds/Classes");
		if(!dir.exists()){
			dir.mkdir();
		}
		
		StringBuffer nextCode = new StringBuffer();
		for(int i=0; i<strings.size(); i++){
			nextCode.append(getRequires(strings.get(i)));
			
			nextCode.append("public class build");
			nextCode.append(i);
			nextCode.append("{\n");

			for(NLMethod me:methods.get(i)){
				nextCode.append(me.code);
				nextCode.append("\n");
			}
			
			nextCode.append("}");
			
			try {
				NLClassLoader.loadString(nextCode.toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			nextCode.setLength(0);
		}
	}
	
	private static class InDevBuildException extends Exception{
		
	}
	
	public static int build(String s) throws InDevBuildException{
		
		if(true){
			throw new InDevBuildException();
		}
		
		strings.add(s);
		states.add(new ArrayList());
		methods.add(new ArrayList());
		protCreated.add(null);
		buildString(s);
		return strings.size()-1;
	}
	
	private static void buildString(String s){
		String[] splited = s.split("\n");
		boolean recordMethod = false;
		StringBuffer strbf = new StringBuffer();
		for(String st:splited){
			if(st.equals("\r")){
				continue;
			}
			
			if(recordMethod){
				strbf.append(st+"\n");
			}
			
			for(LineState l:LineState.values()){
				String[] m=st.split(l.beg);
				if(m.length==0 || m[0].equals("")){
					states.get(states.size()-1).add(l);
					if(l==l.DEF){
						recordMethod = true;
						strbf.append("name"+m[1]+"\n");
					}else if(l==l.ENDDEF){
						recordMethod = false;
						methods.get(methods.size()-1).add(new NLMethod(strbf.toString()));
						strbf.setLength(0);
					}
					break;
				}
			}
		}
	}
	
}
