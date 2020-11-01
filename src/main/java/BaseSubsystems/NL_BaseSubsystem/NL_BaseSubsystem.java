package BaseSubsystems.NL_BaseSubsystem;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.JsonKeysetWriter;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.config.TinkConfig;

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
	
	private static Queue<Integer> newSocketsWaiting = new LinkedList<Integer>();
	public static boolean hasNewSocket(){
		return newSocketsWaiting.size()>0;
	}
	public static int getNewSocket(){
		return newSocketsWaiting.poll();
	}
	
	private static ArrayList<BS_Socket> sockets;
	private static ServerSocket sSock;
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
						if(sc.sFall!=null && sc.sFall.available()>0){
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
						
						System.out.println("Connection Opened");
					}else{
						String[] s = response.split("_");
						if(s.length>1){
							int index = Integer.parseInt(s[1]);
							if(sockets.get(index).encryptedTunnel==null){
								sockets.get(index).encryptedTunnel=m;
								System.out.println("2/3");
							}else if(sockets.get(index).fallbackTunnel==null){
								sockets.get(index).fallbackTunnel=m;
								sockets.get(index).sFall = m.getInputStream();

								CleartextKeysetHandle.write(sockets.get(index).pri.getPublicKeysetHandle(), JsonKeysetWriter.withFile(KeySetSystem.keysetpub));
								sendPrivate(m, "CRYPTDATA%%%%%%"+new NLReader(new FileReader(KeySetSystem.keysetpub)).readAll());
								
								newSocketsWaiting.add(index);
								System.out.println("Connection Finished");
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
	
	private int createServerP(int port) throws Exception {
		sSock = new ServerSocket(port);
		
		if(thSSock!=null && thSSock.isAlive()){
			thSSock.stop();
			thSSock.destroy();
		}
		
		thSSock = new Thread(new ThreadAccept());
		thSSock.start();
		
		System.out.println("Created server");
		
		return 0;
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
	public boolean send(int protocol, int index, String s){
		try{
			Socket sc = getSock(protocol, sockets.get(index));
			Protocol p = getProt(protocol, sockets.get(index));
			if(p!=null){
				s=p.toSocket(s);
			}
			sendPrivate(sc, s);
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public String recv(int protocol, int index){
		try{
			Socket sc = getSock(protocol, sockets.get(index));
			Protocol p = getProt(protocol, sockets.get(index));
			String s = waitForServer(sc);
			if(p!=null){
				s=p.fromSocket(s);
			}
			return s;
		}catch(Exception e){
			e.printStackTrace();
			return "";
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
		
		System.out.println(response);
		
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

}