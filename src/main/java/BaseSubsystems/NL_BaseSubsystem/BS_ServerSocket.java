package BaseSubsystems.NL_BaseSubsystem;

import java.net.ServerSocket;
import java.util.LinkedList;
import java.util.Queue;

public class BS_ServerSocket {

	public ServerSocket sSock;
	public Queue<Integer> socketsWaiting = new LinkedList<Integer>();
	
}
