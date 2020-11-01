package OpenNL.NL_Language;

import java.util.ArrayList;

public class NLMethod {

	public String name;
	public String buildedFunc;
	public String code;
	
	private static class NLInput{
		String name;
		String type;
	}
	private static class NLOutput{
		String type;
	}
	
	public NLMethod(String s){
		s=s.replaceAll("\r", "");
		String[] spl = s.split("\n");
		boolean build = false;
		StringBuffer strbf = new StringBuffer();
		NLOutput out = new NLOutput();
		ArrayList<NLInput> inputs = new ArrayList<NLInput>();
		for(String m:spl){
			if(build && !m.equals("endconstruct")){
				strbf.append(m);
			}
			
			if(!m.equals("")){
				if(m.equals("construct")){
					build = true;
				}else if(m.equals("endconstruct")){
					build = false;
				}
				
				String[] splM = m.split(" ");
				if(splM.length>2 && splM[0].equals("in")){
					inputs.add(new NLInput());
					inputs.get(inputs.size()-1).type = splM[1];
					inputs.get(inputs.size()-1).name = splM[2];
				}else if(splM.length>1 && splM[0].equals("out")){
					out.type = splM[1];
				}else if(splM.length>1 && splM[0].equals("name")){
					name = splM[1];
				}
			}
		}
		
		StringBuffer strcode = new StringBuffer("public ");
		strcode.append(out.type);
		strcode.append(" ");
		strcode.append(name);
		strcode.append("(");
		for(int i=0; i<inputs.size(); i++){
			strcode.append(inputs.get(i).type);
			strcode.append(" ");
			strcode.append(inputs.get(i).name);
			if(i!=inputs.size()-1){
				strcode.append(",");
			}
		}
		strcode.append("){");
		strcode.append(strbf.toString());
		strcode.append("}");
		code = strcode.toString();
	}
	
}
