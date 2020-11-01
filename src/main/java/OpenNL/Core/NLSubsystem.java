package OpenNL.Core;

public abstract class NLSubsystem {
	public abstract int createServer(int protocol);
	public abstract int createSocket(String IP, int port, boolean launch);

	public abstract boolean send(int protocol, int index, String s);
	public abstract String recv(int protocol, int index);
	public abstract int getDefauftProtocol();
	public abstract void init();
}
