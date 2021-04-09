package OpenNL.OpenNLAlpha2020_10;

import BaseSubsystems.NL_BaseSubsystem.NL_BaseSubsystem;
import BaseSubsystems.NL_BaseSubsystem.NetUtils.NetMessage;
import OpenNL.Core.OpenNL;

public class TestSameLaunch {
	
	public static void main(String[] args) throws Exception {
		NL_BaseSubsystem system = new NL_BaseSubsystem();
		OpenNL.initSubsystem(system);
		
		int sindex = system.createServer(8000);
		int client = system.createSocket("localhost", 8000, true);
		while(!system.hasNewSocket(sindex)) {
			Thread.sleep(100);
		}
		int serv_sock = system.getNewSocket(sindex);
		NetMessage msg = new NetMessage();
		msg.message = "Hi";
		system.send(system.PROTOCOL_DEFAULT, serv_sock, msg);
		Thread.sleep(100);
		msg = system.recv(system.PROTOCOL_DEFAULT, client);
		System.out.println(msg.message);
		
		system.closeSocket(client);
		msg = system.recv(system.PROTOCOL_DEFAULT, serv_sock);
	}

}
