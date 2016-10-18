import java.io.*;
import java.util.*;
import java.security.*;
import java.net.*;
import javax.net.ssl.*;

public class CTFServerTemp {
	private int port;
	private String keystore; 
	private String truststore;
	static final int CLIENT_PORT = 8190;
	static final int CLA_PORT = 8187;
	static private String KEYSTORE = "keystores/secureKeyStore.ks";
	static private String TRUSTSTORE = "keystores/secureTrustStore.ks";
	static private String CTFKEYSTORE = "keystores/ctfKeystore.ks";
	static private String CTFTRUSTSTORE = "keystores/CTFtruststore.ks";
	static final String STOREPASSWD = "abcdef";
	static final String ALIASPASSWD = "123456";

	private ArrayList<String> randomNumbers = new ArrayList<String>();
	private ArrayList<Vote> theVotes = new ArrayList<Vote>();

	CTFServerTemp(){
		System.out.println("Running");
	}

	public void run(){
		try{
			System.out.println("The client is running");
			KeyStore ks = KeyStore.getInstance("JCEKS");
			ks.load(new FileInputStream(KEYSTORE),
				ALIASPASSWD.toCharArray());
			// create an empty truststore and load it with the truststore´s file
			KeyStore ts = KeyStore.getInstance("JCEKS");
			ts.load(new FileInputStream(TRUSTSTORE),
					STOREPASSWD.toCharArray());

			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, ALIASPASSWD.toCharArray());
			// create and TrustManager and initilize with TrustStore objects
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(ts);
			// initlize sslContext with kmf and tmf and null=default random number which generates a secret key
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			
			SSLServerSocketFactory sslServerFactory = sslContext.getServerSocketFactory();
			// start listning for connections on the specfic port
			SSLServerSocket sssVoter = (SSLServerSocket)sslServerFactory.createServerSocket(8190);
			SSLServerSocket sssCLA = (SSLServerSocket)sslServerFactory.createServerSocket(8187);
			
			sssVoter.setEnabledCipherSuites(sssVoter.getSupportedCipherSuites());
			sssCLA.setEnabledCipherSuites(sssCLA.getSupportedCipherSuites());

			SSLSocket sslVoter = (SSLSocket)sssVoter.accept();
			SSLSocket sslCTF = (SSLSocket)sssCLA.accept();

			BufferedReader socketIn;
			socketIn = new BufferedReader(new InputStreamReader(sslVoter.getInputStream()));

			BufferedReader socketIn2;
			socketIn2 = new BufferedReader(new InputStreamReader(sslCTF.getInputStream()));

			String str;

			while(!(str = socketIn2.readLine()).equals("")){
				System.out.println("CLA str: " + str);
				randomNumbers.add(str);
			}

			while(!(str = socketIn.readLine()).equals("")){
				System.out.println("Voter str: " + str);
				try{
					String[] s = str.split(" ");
					int choice = Integer.parseInt(s[0]);

					if(choice == 2) {
						System.out.println("choice 2");
						if(!theVotes.isEmpty()) {
							for(int i = 0; i < theVotes.size(); i++) {
								System.out.println(theVotes.get(i).getUserID());
							}
						} else {
							System.out.println("No one has voted jet");
						}
					}
					else {
						if(!randomNumbers.contains(s[0])) {
							System.out.println("You are not allowed to vote");
						} else {
							Vote v = new Vote(s[0], s[1], s[2], true);
							if(!theVotes.contains(v)) {
								theVotes.add(v);
								System.out.println("Your vote has been registred");
							} else {
								System.out.println("You have already voted");
							}
						}
					}
				}catch(NumberFormatException nfe) {
					System.out.println("Sorry, something is wrong");
				}
			}

		} catch(Exception x) {
			System.out.println(x);
			x.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try{
			CTFServerTemp ctf = new CTFServerTemp();
			ctf.run();
		}catch(Exception x){
			System.out.println(x);
			x.printStackTrace();
		}
	}
}