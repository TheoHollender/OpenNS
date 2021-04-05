package BaseSubsystems.NL_BaseSubsystem.NetUtils;

import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.HashMap;

import OpenNL.BaseNetMessage;
import OpenNL.Protocols.Protocol;

public class NetMessage extends BaseNetMessage{

	public static class NetHeader {
		public static HashMap<String, String> strings = new HashMap();
	}
	
	public NetHeader head = new NetHeader();
	public String message = "";
	
	public boolean crashed = false;
	public boolean hasMessage = true;
	
	public String toString(){
		head.strings.put("Message", message);
		StringBuffer strbf = new StringBuffer();
		
		for(String s:head.strings.keySet()){
			strbf.append(s);
			strbf.append("%");
			strbf.append(head.strings.get(s));
			strbf.append(";");
		}
		
		return strbf.toString();
	}
	
	
	public String toString(Protocol p) {
		head.strings.put("Message", message);
		if(p!=null){
			head.strings.put("Message", p.toSocket(head.strings.get("Message")));
		}
		
		Encoder e = Base64.getEncoder();
		StringBuffer strbf = new StringBuffer();
		
		for(String s:head.strings.keySet()){
			strbf.append(e.encodeToString(s.getBytes()));
			strbf.append("%");
			strbf.append(e.encodeToString(head.strings.get(s).getBytes()));
			strbf.append(";");
		}
		
		return strbf.toString();
	}
	public void fromString(String s, Protocol p) {
		String[] headStr = s.split(";");
		
		Decoder e = Base64.getDecoder();
		StringBuffer strbf = new StringBuffer();
		
		for(String header:headStr){
			String[] headWdata = header.split("%");
			head.strings.put(new String(e.decode(headWdata[0])), 
					new String(e.decode(headWdata[1])));
		}
		if(p!=null){
			head.strings.put("Message", p.fromSocket(head.strings.get("Message")));
		}
		message = head.strings.get("Message");
	}
}
