package BaseSubsystems.NL_BaseSubsystem;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.JsonKeysetWriter;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.config.TinkConfig;

import BaseSubsystems.NL_BaseSubsystem.NetUtils.NetMessage;
import OpenNL.BaseNetMessage;
import OpenNL.Core.NLSubsystem;
import OpenNL.NL_Language.NLReader;
import OpenNL.Protocols.EncryptionProtocol;
import OpenNL.Protocols.Protocol;
import OpenNL.Protocols.Crypto.KeySetSystem;

public class NL_BaseSubsystem extends NLSubsystem {

	public static final int PROTOCOL_DEFAULT = 0;
	public static final int PROTOCOL_ENCRYPTED = 1;
	public static final int PROTOCOL_FALLBACK = 2;
	
	public static final Protocol PROTOCOL_DEFAULT_P = null;
	public static final EncryptionProtocol PROTOCOL_ENCRYPTED_P = new EncryptionProtocol();
	public static final Protocol PROTOCOL_FALLBACK_P = null;
	
	private static ArrayList<BS_ServerSocket> servers = new ArrayList<BS_ServerSocket>();
	public static boolean hasNewSocket(int index){
		return servers.get(index).socketsWaiting.size()>0;
	}
	public static int getNewSocket(int index){
		return servers.get(index).socketsWaiting.poll();
	}
	
	private static ArrayList<BS_Socket> sockets;
	private static Thread thSSock;
	private static Thread thCheckFallback;
	
	@Override
	public int createServer(int port){
		// TODO Auto-generated method stub
		try {
			return createServerP(port);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	
	public static boolean hasFallBackMessage(int index){
		return sockets.get(index).strs.size()>0;
	}
	
	public static String getFallBackMessage(int index){
		return sockets.get(index).strs.poll();
	}
	
	private static class ThreadFallback implements Runnable{

		public void run() {
			while(true){
				for(int i=0; i<sockets.size(); i++){
					BS_Socket sc = sockets.get(i);
					try {
						if(sc!=null && sc.sFall!=null && !sc.fallbackTunnel.isClosed() && sc.sFall.available()>0){
							String s = waitForServer(sc.fallbackTunnel);
							String[] sPl = s.split("%%%%%%");
							if(sPl.length>1 && sPl[0].equals("CRYPTDATA")){
								FileWriter fw = new FileWriter(KeySetSystem.keysetpub_extern);
								fw.append(sPl[1]);
								fw.close();
								
								sc.pub = CleartextKeysetHandle.read(JsonKeysetReader.withFile(KeySetSystem.keysetpub_extern));
								if(sc.isCLient){
									CleartextKeysetHandle.write(sc.pri.getPublicKeysetHandle(), JsonKeysetWriter.withFile(KeySetSystem.keysetpub));
									sendPrivate(sc.fallbackTunnel, "CRYPTDATA%%%%%%"+new NLReader(new FileReader(KeySetSystem.keysetpub)).readAll());
								}
							}else{
								sc.strs.add(s);
							}
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
	private static class ThreadAccept implements Runnable{

		public void run() {
			Socket m;
			String response;
			while(true){
				for(int i = 0; i < servers.size(); i++) {
					if (servers.get(i) == null || servers.get(i).sSock == null
							|| servers.get(i).sSock.isClosed()) {
						continue;
					}
					
					ServerSocket sSock = servers.get(i).sSock;
					
					try{
						
						m = sSock.accept();
						response = waitForServer(m);
						if(response!=""){
						
						if(response.equals("BSubsystem")){
							sockets.add(new BS_Socket());
							int index = sockets.size() -1;
							
							sockets.get(index).defaultSock = m;
							sockets.get(index).pri = pv;
							sendPrivate(m, String.valueOf(index));
							
							//System.out.println("Connection Opened");
						}else{
							String[] s = response.split("_");
							if(s.length>1){
								int index = Integer.parseInt(s[1]);
								if(sockets.get(index).encryptedTunnel==null){
									sockets.get(index).encryptedTunnel=m;
									//System.out.println("2/3");
								}else if(sockets.get(index).fallbackTunnel==null){
									sockets.get(index).fallbackTunnel=m;
									sockets.get(index).sFall = m.getInputStream();
	
									CleartextKeysetHandle.write(sockets.get(index).pri.getPublicKeysetHandle(), JsonKeysetWriter.withFile(KeySetSystem.keysetpub));
									sendPrivate(m, "CRYPTDATA%%%%%%"+new NLReader(new FileReader(KeySetSystem.keysetpub)).readAll());
									
									servers.get(i).socketsWaiting.add(index);
								}else{
									m.close();
								}
							}
						}
						
					}else{
						m.close();
					}
					
					}catch(Exception e){
						e.printStackTrace();
					}
				
				}
			}
		}
		
	}
	
	private int createServerP(int port) throws Exception {
		ServerSocket sSock = new ServerSocket(port);
		
		if(thSSock!=null && thSSock.isAlive()){
			thSSock.stop();
			thSSock.destroy();
		}
		
		thSSock = new Thread(new ThreadAccept());
		thSSock.start();
		
		BS_ServerSocket serv = new BS_ServerSocket();
		serv.sSock = sSock;
		servers.add(serv);
		
		return servers.size() - 1;
	}

	public static String recvForServer(Socket sc) throws Exception{
		InputStream oStr = sc.getInputStream();
		if(oStr.available()==0){
			return "";
		}
		int timeout = 0;
		while(oStr.available()==0){timeout+=1; if(timeout==5000){return "";}}
		
		byte[] bytes = new byte[oStr.available()];
		oStr.read(bytes);
		
		return new String(bytes);
	}
	
	public static String waitForServer(Socket sc) throws Exception{
		InputStream oStr = sc.getInputStream();
		
		int timeout = 0;
		while(oStr.available()==0){timeout+=1; if(timeout==5000){return "";}}
		
		byte[] bytes = new byte[oStr.available()];
		oStr.read(bytes);
		
		return new String(bytes);
	}
	
	private static Socket getSock(int prot, BS_Socket sc){
		if(prot == PROTOCOL_DEFAULT){
			return sc.defaultSock;
		}
		if(prot == PROTOCOL_ENCRYPTED){
			return sc.encryptedTunnel;
		}
		if(prot == PROTOCOL_FALLBACK){
			return sc.fallbackTunnel;
		}
		return null;
	}
	private static Protocol getProt(int prot, BS_Socket s){
		if(prot == PROTOCOL_DEFAULT){
			return PROTOCOL_DEFAULT_P;
		}
		if(prot == PROTOCOL_ENCRYPTED){
			PROTOCOL_ENCRYPTED_P.pri = s.pri;
			PROTOCOL_ENCRYPTED_P.pub = s.pub;
			return PROTOCOL_ENCRYPTED_P;
		}
		if(prot == PROTOCOL_FALLBACK){
			return PROTOCOL_FALLBACK_P;
		}
		return null;
	}
	
	
	@Override
	public boolean send(int protocol, int index, NetMessage msg){
		try{
			Socket sc = getSock(protocol, sockets.get(index));
			if(protocol==PROTOCOL_FALLBACK){
				sendPrivate(sc, msg.message);
			}else{
				Protocol p = getProt(protocol, sockets.get(index));
				String s = msg.toString(p);
				s = String.valueOf(s.length())+";"+s;
				sendPrivate(sc, s);
			}
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	private String recvWhile(char c, Socket sc) throws IOException{
		InputStream in = sc.getInputStream();
		int charac = in.read();
		StringBuffer strbf = new StringBuffer();
		while((int)c!=charac){
			strbf.append((char)charac);
			charac = in.read();
		}
		return strbf.toString();
	}
	
	@Override
	public NetMessage recv(int protocol, int index){
		try{
			Socket sc = getSock(protocol, sockets.get(index));
			Protocol p = getProt(protocol, sockets.get(index));
			
			try {
				int size = Integer.parseInt(recvWhile(';', sc));
				if(size==0){
					NetMessage n = new NetMessage();
					n.hasMessage = false;
					return n;
				}
				
				byte[] bytes = new byte[size];
				sc.getInputStream().read(bytes);
				
				NetMessage n = new NetMessage();
				n.fromString(new String(bytes), p);
				if (n.head.strings.containsKey("CLOSE-SOCKET")
						&& n.head.strings.get("CLOSE-SOCKET").equals("true")) {
					BS_Socket b_sc = sockets.get(index);
					closeSocket(b_sc, index);
					return null;
				}
				return n;
				
			}catch(Exception e) {
				NetMessage n = new NetMessage();
				n.crashed = true;
				return n;
			}
		}catch(Exception e){
			NetMessage n = new NetMessage();
			n.crashed = true;
			return n;
		}
	}
	
	private static void sendPrivate(Socket sc, String s) throws Exception{
		PrintWriter pw = new PrintWriter(sc.getOutputStream());
		pw.write(s);
		pw.flush();
	}
	
	private static int createSocketP(String IP, int port, boolean launch) throws Exception{
		BS_Socket bs_sock = new BS_Socket();
		
		// Create Default Socket
		bs_sock.defaultSock = new Socket(IP, port);
		
		// Send Base NL_BaseSubsystem Message
		sendPrivate(bs_sock.defaultSock,"BSubsystem");
		
		// Wait for Socket Index
		String response = waitForServer(bs_sock.defaultSock);
		
		if(response.equals("")){bs_sock.defaultSock.close();return -1;}
		
		//System.out.println(response);
		
		// Open Tunnel
		bs_sock.encryptedTunnel = new Socket(IP, port);
		// Send Encyption State
		sendPrivate(bs_sock.encryptedTunnel, "Connect_"+response);

		// Open Tunnel
		bs_sock.fallbackTunnel = new Socket(IP, port);
		// Send Encyption State
		sendPrivate(bs_sock.fallbackTunnel, "Connect_"+response);
		bs_sock.sFall = bs_sock.fallbackTunnel.getInputStream();
		
		bs_sock.isCLient = true;
		bs_sock.pri = pv;
		
		sockets.add(bs_sock);
		
		return sockets.size()-1;
	}
	
	@Override
	public int createSocket(String IP, int port, boolean launch) {
		try {
			return createSocketP(IP, port, launch);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int getDefauftProtocol() {
		// TODO Auto-generated method stub
		return 0;
	}

	private static KeysetHandle pv;
	private static KeysetHandle pub;
	
	@Override
	public void init() {
		sockets = new ArrayList<BS_Socket>();
		thCheckFallback = new Thread(new ThreadFallback());
		thCheckFallback.start();
		try {
			TinkConfig.init();
			pv = KeySetSystem.getPrivateKeysetHandle();
			pub = pv.getPublicKeysetHandle();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void closeSocket(BS_Socket sc, int index) {
		try {
			sc.defaultSock.close();
			sc.encryptedTunnel.close();
			sc.fallbackTunnel.close();
		} catch (IOException e) {}
		sockets.set(index, null);
	}
	@Override
	public void closeSocket(int index) {
		BS_Socket sc = sockets.get(index);
		NetMessage msg = new NetMessage();
		msg.head.strings.put("CLOSE-SOCKET", "true");
		msg.message = "NONE-CLOSE";
		send(this.PROTOCOL_DEFAULT, index, msg);
		closeSocket(sc, index);
	}
	@Override
	public void closeServer(int index) {
		if (servers.get(index) == null) {return;}
		ServerSocket sSock = servers.get(index).sSock;
		if (sSock != null && !sSock.isClosed()) {
			try {
				sSock.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		servers.set(index, null);
	}

	/**
	 * 
	 * 
	 *Possible passage en c++:
	 *
	 * Utilisation de GraalVM
	 * 
	 * 
	 */
	
}
