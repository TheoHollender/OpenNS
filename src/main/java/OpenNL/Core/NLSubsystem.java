package OpenNL.Core;

import BaseSubsystems.NL_BaseSubsystem.NetUtils.NetMessage;
import OpenNL.BaseNetMessage;

public abstract class NLSubsystem {
	public abstract int createServer(int protocol);
	public abstract int createSocket(String IP, int port, boolean launch);

	public abstract boolean send(int protocol, int index, NetMessage s);
	public abstract NetMessage recv(int protocol, int index);
	public abstract int getDefauftProtocol();
	public abstract void init();
	
	public abstract void closeSocket(int index);
	public abstract void closeServer(int index);
}
