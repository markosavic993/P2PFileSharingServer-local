package Server;


import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;


import klase.Fajl;
import klase.User;

public class Server {
	
	
	
	
	static LinkedList<ServerNit> klijenti = new LinkedList<ServerNit>();
	static LinkedList<Fajl> fajlovi = new LinkedList<Fajl>();
	static LinkedList<User> users = new LinkedList<User>();
	static int brojOnlineKlijenata = 0;
	
	
	
	public static void main(String[] args) {
		
		
		int port = 9876;
		if(args.length > 0) {
			
			port = Integer.parseInt(args[0]);
		}
		
		Socket klijentSoket = null;
		try {
			ServerSocket serverSoket = new ServerSocket(port);
			System.out.println("Server je poceo sa radom!");
			
			
			while(true) {
				
				klijentSoket = serverSoket.accept();
				ServerNit klijent = new ServerNit(klijenti, klijentSoket, fajlovi);
				klijenti.add(klijent);
				brojOnlineKlijenata++;
				System.out.println("Broj online klijenata :" + brojOnlineKlijenata);
				klijent.start();
				
			}
		
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		
		
		

	}

}
