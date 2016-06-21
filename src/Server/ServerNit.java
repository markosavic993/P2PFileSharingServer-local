package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedList;

import klase.Fajl;
import klase.User;
import klase.Segment;

public class ServerNit extends Thread {
	
	//definisanje ulazni-izlaznih tokova
	BufferedReader ulazniTokOdKlijenta = null;
	PrintStream izlazniTokKaKlijentu = null;
	
	//online klijenti
	LinkedList<ServerNit> klijenti = null;
	
	Socket soketZaKomunikaciju = null;
	
	//fajlovi koji se seed-uju
	LinkedList<Fajl> fajlovi = null;
	

	public ServerNit(LinkedList<ServerNit> klijenti, Socket soketZaKomunikaciju, LinkedList<Fajl> fajlovi) {
		super();
		this.klijenti = klijenti;
		this.soketZaKomunikaciju = soketZaKomunikaciju;
		this.fajlovi = fajlovi;
		
	}

	@Override
	public void run(){
		try {
			
			ulazniTokOdKlijenta = new BufferedReader(new InputStreamReader
					(soketZaKomunikaciju.getInputStream()));
			izlazniTokKaKlijentu = new PrintStream(
					soketZaKomunikaciju.getOutputStream());

			izlazniTokKaKlijentu.println("Dobrodosli na pcelice.");

			String[] podaciOKlijentu = ulazniTokOdKlijenta.readLine().split("\\#");
			if (!(podaciOKlijentu[0].equals("nov"))) {// stari klijent, potrebno je promeniti ip adresu unetu u svakom segmentu
				for (int i = 0; i < fajlovi.size(); i++) {
					for (int j = 0; j < fajlovi.get(i).getListaSegmenata().size(); j++) {
						klase.Segment s = fajlovi.get(i).getListaSegmenata().get(j);
						for (int k = 0; k < s.getUsers().size(); k++) {
							if (s.getUsers().get(k).getIpAdresa().equals(podaciOKlijentu[0])) {
								s.getUsers().get(k).setIpAdresa(podaciOKlijentu[1]);
							}
						}
					}
				}

			}

			izlazniTokKaKlijentu.println("Da li zelite da seed-ujete neki fajl?");
			String daNe = ulazniTokOdKlijenta.readLine().toUpperCase();

			if (daNe.equals("DA")) {
				// metoda za ubacivanje fajlova koji se seeduju
				String ip = ((InetSocketAddress) soketZaKomunikaciju.getRemoteSocketAddress()).getHostString(); // postoji samo u okviru seedovanja
				boolean daLiJKraj = false;

				while (!daLiJKraj) {
					izlazniTokKaKlijentu.println("Unesite putanju do fajla...");
					String[] podaciOFajlu = ulazniTokOdKlijenta.readLine().split("\\#");// naziv#vecicina#checksum#da/ne
					System.out.println("Novi seed-er za fajl: " + podaciOFajlu[0]);
					boolean daLiPostoji = false;
					for (int i = 0; i < fajlovi.size(); i++) {

						//azuriranje postojecih fajlova
						
						if (fajlovi.get(i).getNaziv().equals(podaciOFajlu[0])
								&& fajlovi.get(i).getVelicina() == Long.parseLong(podaciOFajlu[1])
								&& fajlovi.get(i).getChecksum().equals(podaciOFajlu[2])) {

							for (int j = 0; j < fajlovi.get(i).getListaSegmenata().size(); j++) {
								fajlovi.get(i).getListaSegmenata().get(j).getUsers().add(new User(ip));
							}
							daLiPostoji = true;
							fajlovi.get(i).brojSeedera = fajlovi.get(i).brojSeedera + 1;
							break;
						}

					}

					//dodavanje novog fajla
					if (!daLiPostoji) {
						long brojSegmenata = 0;
						if (Long.parseLong(podaciOFajlu[1]) % 1300 == 0) { 
							brojSegmenata = Long.parseLong(podaciOFajlu[1]) / 1300;
						} else {
							brojSegmenata = (Long.parseLong(podaciOFajlu[1]) / 1300) + 1;
						}

						LinkedList<Segment> segmenti = new LinkedList<Segment>();
						for (int j = 0; j < brojSegmenata; j++) {
							Segment s = new Segment(j + 1);
							s.getUsers().add(new User(ip));
							segmenti.add(s);
						}

						Fajl noviFajl = new Fajl(podaciOFajlu[0], Long.parseLong(podaciOFajlu[1]), segmenti,
								podaciOFajlu[2]);
						fajlovi.add(noviFajl);
						System.out.println("Server.fajlovi: " + Server.fajlovi.size());
						System.out.println("fajlovi: " + fajlovi.size());
					}

					if (podaciOFajlu[3].toUpperCase().equals("NE")) {
						daLiJKraj = true;
					}

				}

			}

			
			while (true) {
				izlazniTokKaKlijentu
						.println("Unesite naziv fajla koji zelite download-ujete    ***za izlaz unesite /quit***");
				String nazivFajlaZaDownload = ulazniTokOdKlijenta.readLine().toUpperCase();
				
				// izlazak iz programa
				if(nazivFajlaZaDownload.equals("/QUIT")) {
					izlazniTokKaKlijentu.println("Dovidjenja!");
					break;
				}
				
				
				String fajloviZaSlanje = "";
				LinkedList<Fajl> listaZaSlanje = new LinkedList<Fajl>();
				int brojac = 1; // za definisanje rednog broja fajla kako bi se olaksao izbor klijentu
				boolean daLiPostoji = false;
				for (int i = 0; i < fajlovi.size(); i++) {
					
					
					if (fajlovi.get(i).getNaziv().toUpperCase().matches(".*\\s*" + nazivFajlaZaDownload + "\\s*.*")) { 
						fajloviZaSlanje += brojac + "/" + fajlovi.get(i).getNaziv() + "/"
								+ fajlovi.get(i).getVelicina() + "/" + fajlovi.get(i).brojSeedera + "#"; 
						listaZaSlanje.addLast(fajlovi.get(i));
						daLiPostoji = true;
						brojac++;
					}
				}
				
				if(daLiPostoji == false) {
					izlazniTokKaKlijentu.println("NE");
					continue;
				}

				izlazniTokKaKlijentu.println(fajloviZaSlanje);
				int izborKlijenta = Integer.parseInt(ulazniTokOdKlijenta.readLine());//redni broj fajla iz liste

				Fajl fajlZaKlijenta = listaZaSlanje.get(izborKlijenta - 1); 

				String spisakOdgovarajucihKlijenata = ""; // nije poslato
				for (int i = 0; i < fajlZaKlijenta.getListaSegmenata().size(); i++) {

					spisakOdgovarajucihKlijenata += fajlZaKlijenta.getListaSegmenata().get(i).getRedniBr() + ":";
					for (int j = 0; j < fajlZaKlijenta.getListaSegmenata().get(i).getUsers().size(); j++) {
						spisakOdgovarajucihKlijenata += fajlZaKlijenta.getListaSegmenata().get(i).getUsers().get(j)
								.getIpAdresa() + "/"; 
					}

					spisakOdgovarajucihKlijenata += "#";
				}
				
				izlazniTokKaKlijentu.println(spisakOdgovarajucihKlijenata); //1:ip1/ip2...#2:ip1/ip2/ip3..#
			
				
				String zavrsnaPoruka = ulazniTokOdKlijenta.readLine(); //zavrseno preuzimanje
				System.out.println(zavrsnaPoruka);
				
				
				String ip = ((InetSocketAddress) soketZaKomunikaciju.getRemoteSocketAddress()).getHostString();
				
				//azuriranje liste seed-era za downloadovan fajl
				for (int i = 0; i < fajlZaKlijenta.getListaSegmenata().size(); i++) {
					User u = new User(ip);
					fajlZaKlijenta.getListaSegmenata().get(i).getUsers().add(u);
				}
				
				System.out.println("Novi seed-er za fajl: " + fajlZaKlijenta.getNaziv());
				fajlZaKlijenta.brojSeedera++;

			}
			
			//azuriranje liste online klijenata pri planiranom prekidu programa
			klijenti.remove(this);
			Server.brojOnlineKlijenata--;
			soketZaKomunikaciju.close();
			

		} catch (IOException e) {
			//azuriranje liste online klijenata pri neplaniranom prekidu programa
			System.out.println("puko thread");
			Server.brojOnlineKlijenata--;
			klijenti.remove(this);
			System.out.println("Broj online klijenata: "+klijenti.size());
		}
	}

	

}
