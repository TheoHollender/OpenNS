package BaseSubsystems.NL_BaseSubsystem;

import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import com.google.crypto.tink.KeysetHandle;

import BaseSubsystems.NL_BaseSubsystem.NetUtils.NetMessage;

public class BS_Socket {
	
	public Socket defaultSock;
	public Socket encryptedTunnel;
	public Socket fallbackTunnel;
	public boolean isAvailable = false;
	public boolean isCLient = false;
	
	public KeysetHandle pri;
	public KeysetHandle pub;
	
	// InputStream 
	public InputStream sFall;
	public Queue<String> strs = new LinkedList<String>();
}
