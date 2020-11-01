package OpenNL.OpenNLAlpha2020_10;

import java.util.Scanner;

import BaseSubsystems.NL_BaseSubsystem.NL_BaseSubsystem;
import OpenNL.Core.OpenNL;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		NL_BaseSubsystem system = new NL_BaseSubsystem();
		OpenNL.initSubsystem(system);
		
		int sock = system.createSocket("127.0.0.1", 8008, true);
		Scanner sc = new Scanner(System.in);
		while(true){
			String msg = sc.nextLine();
			String[] msgSpl = msg.split("_");
			if(msgSpl.length>1 && msgSpl[0].equals("send")){
				system.send(system.PROTOCOL_DEFAULT, sock, msgSpl[1]);
				system.send(system.PROTOCOL_ENCRYPTED, sock, msgSpl[1]);
				system.send(system.PROTOCOL_FALLBACK, sock, msgSpl[1]);
				System.out.println("sent");
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
