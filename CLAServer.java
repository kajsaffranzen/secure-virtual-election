import java.io.*;
import java.security.*;
import java.net.*;
import javax.net.ssl.*;
import java.util.StringTokenizer;
import java.util.*;

//Class that maintains Validations ID Lists
public class CLAServer {
	
	private int port, key;
	private InetAddress host;
	private String keystore; 
	private String truststore;

	static final int VOTER_PORT = 8189;
	static final int CTF_PORT = 8187;
	static private String KEYSTORE = "keystores/secureKeyStore.ks";
	static private String TRUSTSTORE = "keystores/secureTrustStore.ks";
	static final String STOREPASSWD = "abcdef";
	static final String ALIASPASSWD = "123456";

	// the list
	Hashtable<String, Integer> randomList = new Hashtable<String, Integer>();

	CLAServer (int port, InetAddress host,  String keystore, String truststore) {
		this.port = port;
		this.host = host;
		this.keystore = keystore;
		this.truststore = truststore;
	}

	// get the generated ssl key
	public void run() {
		try {
			System.out.println("The server is running");
			// statements to set up security
			// create an empty keystore and load it with the keystore´s file
			KeyStore ks = KeyStore.getInstance("JCEKS");
			ks.load(new FileInputStream(keystore),
				ALIASPASSWD.toCharArray());
			// create an empty truststore and load it with the truststore´s file
			KeyStore ts = KeyStore.getInstance("JCEKS");
			ts.load(new FileInputStream(truststore),
				STOREPASSWD.toCharArray());

			// SSL connection will require access to encryption keys and cert
			// create KeyManager and initilize with KeyStore
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, ALIASPASSWD.toCharArray());
			// create and TrustManager and initilize with TrustStore objects
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(ts);
			// initlize sslContext with kmf and tmf and null=default random number which generates a secret key
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			
			listningToPort(sslContext);
			
		} catch(Exception x) {
			System.out.println(x);
			x.printStackTrace();
		}
	}

	private void listningToPort(SSLContext sslContext) throws IOException {
		try{
		if(port == VOTER_PORT) {
			SSLServerSocketFactory sslServerFactory = sslContext.getServerSocketFactory();
			
			SSLServerSocket sss = (SSLServerSocket)sslServerFactory.createServerSocket(port);
			sss.setEnabledCipherSuites(sss.getSupportedCipherSuites());
			SSLSocket incoming = (SSLSocket)sss.accept();
			System.out.println("CLA connected to server");

			
			listningToVoter(incoming);

		} else if(port == CTF_PORT) {
			SSLSocketFactory sslFact = sslContext.getSocketFactory();

			// negotiating with the server to agree upon the cipher suite that will be used
			SSLSocket client = (SSLSocket)sslFact.createSocket(host, port);
			client.setEnabledCipherSuites(client.getSupportedCipherSuites());
			sendingToCTF(client);

		} else System.out.println("Problem in PARADISE!");
	}catch( Exception x ) {
			System.out.println( x );
			x.printStackTrace();
		}
	}

	private void listningToVoter(SSLSocket incoming) throws IOException {
		BufferedReader in;
		in = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
		PrintWriter out = new PrintWriter(incoming.getOutputStream(), true);

		String str;
		int r = 0;
		do{
			while(!(str = in.readLine()).equals("")) {
				// check the voter
				r = checkPersonalNr(str);
				if(r != 0){
					// generate a random key and send it back to voter
					out.println(r);
					out.println("");
					System.out.println(randomList.get(str));
				}
				else {
					out.println(0);
					out.println("");
					System.out.println();
					System.out.println("Error!");
				}
			}
		}while(r == 0);
		out.close();
		in.close();
		incoming.close();

		port = CTF_PORT;
		run();
	}

	private int checkPersonalNr(String personalNr) {
		// validate personal number
		// check for double voters
		if(personalNr.length() != 10) {
			System.out.println("Invalid personl number");
			return 0;
		} 
		else if(randomList.containsKey(personalNr)) {
			if(randomList.get(personalNr) == 0) {
				return generateRandomKey(personalNr);
			} else {
				return randomList.get(personalNr);
			}
		} else {
			// create a random key
			System.out.println("Something went wrong");
			return 0;
		}
	}

	private void createVotingList() {
		randomList.put("9104174444", 0);
		randomList.put("9211114323", 0);
		randomList.put("9007297243", 0);
	}

	private void sendingToCTF(SSLSocket client) throws IOException {
		try{
			BufferedReader socketIn;
			socketIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
			PrintWriter socketOut = new PrintWriter(client.getOutputStream(), true);
			socketOut.println(key);
			socketOut.println("");

		}catch( Exception x ) {
			System.out.println( x );
			x.printStackTrace();
		}
	}
	// create random key
	private int generateRandomKey(String personalNr) {
		SecureRandom random = new SecureRandom();
		int randomKey = 0;
		try {
			
			byte bytes[] = new byte[512];
			// returns a SecureRandom object
			random = SecureRandom.getInstance("SHA1PRNG");
			random.nextBytes(bytes);
			randomKey = random.nextInt();

			// put key and voter into randomList
			randomList.remove(personalNr);
			key = randomKey;
			randomList.put(personalNr, randomKey);
		} catch( Exception x ) {
			System.out.println(x);
			x.printStackTrace();
		}

		return randomKey;
	} 

	public static void main (String[] args) throws IOException {
		int port = VOTER_PORT;
		String keystore = KEYSTORE;
		String truststore = TRUSTSTORE;
		InetAddress host = InetAddress.getLocalHost();

		if(args.length > 0) {
			port = Integer.parseInt(args[0]);
		}

		CLAServer addServe = new CLAServer(port, host, keystore, truststore);
		addServe.createVotingList();
		addServe.run();	
		
		
	}
}