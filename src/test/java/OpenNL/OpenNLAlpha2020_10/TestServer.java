package OpenNL.OpenNLAlpha2020_10;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import BaseSubsystems.NL_BaseSubsystem.NL_BaseSubsystem;
import OpenNL.Core.OpenNL;

public class TestServer {

	public static void main(String[] args){
		NL_BaseSubsystem system = new NL_BaseSubsystem();
		OpenNL.initSubsystem(system);
		
		system.createServer(8008);
		Scanner sc = new Scanner(System.in);
		int sock = 0;
		while(true){
			String msg = sc.nextLine();
			String[] msgSpl = msg.split("_"); 
			if(system.hasNewSocket()){
				sock = system.getNewSocket();
				System.out.println(sock);
			}
			
			if(msgSpl.length>1 && msgSpl[0].equals("send")){
				system.send(system.PROTOCOL_DEFAULT, sock, msgSpl[1]);
				system.send(system.PROTOCOL_ENCRYPTED, sock, msgSpl[1]);
				system.send(system.PROTOCOL_FALLBACK, sock, msgSpl[1]);
			}else if(msgSpl.length>0 && msgSpl[0].equals("recv")){
				if(system.hasFallBackMessage(sock)){
					System.out.println(system.recv(system.PROTOCOL_DEFAULT, sock));
					System.out.println(system.recv(system.PROTOCOL_ENCRYPTED, sock));
					System.out.println(system.getFallBackMessage(sock));
				}
			}
		}
	}
	
}
