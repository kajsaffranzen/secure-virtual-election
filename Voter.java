import java.io.*;
import java.util.*;
import java.net.*;
import java.security.KeyStore;
import javax.net.ssl.*;

public class Voter{
	private InetAddress host;
	private int port;
	private Boolean voteAccess = false;
	static final int CTF_PORT = 8190;
	static final int CLA_PORT = 8189;
	static final String KEYSTORE = "keystores/secureKeyStore.ks";
	static final String TRUSTSTORE = "keystores/secureTrustStore.ks";
	static final String STOREPASSWD = "abcdef";
	static final String ALIASPASSWD = "123456";
	int count = 0;
	SSLSocket clientToCTF;
	BufferedReader socketInCTF;
	PrintWriter socketOutCTF;


	//constructer
	public Voter(InetAddress host, int port){
		this.host = host;
		this.port = port;
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

			SSLSocketFactory sslFact = sslContext.getSocketFactory();
			SSLSocket client = (SSLSocket)sslFact.createSocket(host, CLA_PORT);
			client.setEnabledCipherSuites(client.getSupportedCipherSuites());
			client.setNeedClientAuth(true);

			BufferedReader socketIn;
			socketIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
			PrintWriter socketOut = new PrintWriter(client.getOutputStream(), true);
			

			//send a message to the CLA asking for a validation number, must  10
			String str;
			String randomNumber = "";
			do{
				System.out.println("Enter your digits: ");
				String theDigits = (new BufferedReader(new InputStreamReader(System.in))).readLine();
				socketOut.println(theDigits);
				socketOut.println("");

				
				while(!(str = socketIn.readLine()).equals("")){
					randomNumber = str;
					System.out.println("Voter str: " + randomNumber);
				}
			} while(Integer.parseInt(randomNumber) == 0);
			Boolean test = false;

			do{		
				test = false;
				//connect to CTFserv
				if(str != "Error"){
					if(count == 0){
						SSLContext sslContextCTF = SSLContext.getInstance("TLS");
						sslContextCTF.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

						SSLSocketFactory sslFactCTF = sslContextCTF.getSocketFactory();
						
						clientToCTF = (SSLSocket)sslFact.createSocket(host, CTF_PORT);
						clientToCTF.setNeedClientAuth(true);
						clientToCTF.setEnabledCipherSuites(sslFact.getSupportedCipherSuites());
						socketInCTF = new BufferedReader(new InputStreamReader(clientToCTF.getInputStream()));
						socketOutCTF = new PrintWriter(clientToCTF.getOutputStream(), true);
					}
					
					count++;	

					int option = createOptionMenu();
					String validationNr = randomNumber;
					
					String input = "";
					switch(option){
						case 1:
							//send voting message to CTF
							String theVote = createVote(validationNr);
							socketOutCTF.println(theVote);
							socketOutCTF.println("");
							
							String a = "";
							do{
								while(!(input = socketInCTF.readLine()).equals("")){
									a += input;
								}
								
								if(a != "")
									test = true;
								
							}while(!test);

							System.out.println(a);
							break;
								

						case 2:
							//see the result 
							String s = "2 " + validationNr;
							socketOutCTF.println(s);
							socketOutCTF.println("");

							String ans = "";
							do{
								while(!(input = socketInCTF.readLine()).equals("")){
									ans += input + "\n";	
								}
								if(ans != "")
									test = true;
								
							}while(!test);
							printResult(ans);
							break;
					}
					
				}
				else System.out.println("To bad, you can't vote!");	
			}while(true);

		} catch (Exception x) {
			System.out.println(x);
			x.printStackTrace();
		}
	}

	//create the option menu
	public int createOptionMenu(){
		int n = 0;
		try{
			System.out.println(" \n What do you want to do?");
			System.out.println("1. Vote");
			System.out.println("2. See result");
			String ans = (new BufferedReader(new InputStreamReader(System.in))).readLine();

			n = Integer.parseInt(ans);
		}catch (Exception x) {
			System.out.println(x);
			x.printStackTrace();
		}

		return n;
	}

	public String createVote(String _validationNr){
		String ans = _validationNr + " ";

		try{
			System.out.println(" \n Enter a username: ");
			ans += (new BufferedReader(new InputStreamReader(System.in))).readLine() + " ";
			System.out.println("********************************");
			System.out.println("What do you want for dinner?");
			System.out.println("1. Pulled pork");
			System.out.println("2. Pizza");
			System.out.println("3. Nothing, I'm on a diet");
			System.out.println("********************************");
			ans += (new BufferedReader(new InputStreamReader(System.in))).readLine();
		}catch (Exception x) {
			System.out.println(x);
			x.printStackTrace();
		}

		return ans;
	}

	//prints the result
	public void printResult(String s){		
		System.out.println("\n The result: \n ");
		System.out.println(s);
	}


	public static void main (String[] args){
		try {
			InetAddress host = InetAddress.getLocalHost();
			int port = CLA_PORT;
			if(args.length > 0) {
				port = Integer.parseInt(args[0]);
			}
			if(args.length > 1) {
				host = InetAddress.getByName(args[1]);
			}
			Voter addVoter = new Voter(host, port);
			addVoter.run();
		} catch (UnknownHostException uhx) {
			System.out.println(uhx);
			uhx.printStackTrace();
		}
	}
}