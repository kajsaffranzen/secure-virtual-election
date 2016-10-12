import java.io.*;
import java.security.*;
import java.net.*;
import javax.net.ssl.*;
import java.util.StringTokenizer;
import java.util.*;

//Class that maintains Validations ID Lists
public class CLAServer {
	//send back random validation number to voter & to CTF
	//exempel finns i artikel

	private int port;
	static final int CLIENT_PORT = 8189;
	static final int CTF_PORT = 8190;
	static final String KEYSTORE = "keystores/secureKeyStore.ks";
	static final String TRUSTSTORE = "keystores/secureTrustStore.ks";
	static final String STOREPASSWD = "abcdef";
	static final String ALIASPASSWD = "123456";

	// the list 
	private List<SecureRandom> listNR = new ArrayList<>();

	CLAServer (int port) {
		this.port = port;
	}

	// get the generated ssl key
	public void run() {
		try {
			System.out.println("The server is running");
			// statements to set up security
			// create an empty keystore and load it with the keystore´s file
			KeyStore ks = KeyStore.getInstance("JCEKS");
			ks.load(new FileInputStream(KEYSTORE),
				ALIASPASSWD.toCharArray());
			// create an empty truststore and load it with the truststore´s file
			KeyStore ts = KeyStore.getInstance("JCEKS");
			ts.load(new FileInputStream(TRUSTSTORE),
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
			
			SSLServerSocketFactory sslServerFactory = sslContext.getServerSocketFactory();
			// start listning for connections on the specfic port
			SSLServerSocket sss = (SSLServerSocket)sslServerFactory.createServerSocket(CLIENT_PORT);
			sss.setEnabledCipherSuites(sss.getSupportedCipherSuites());
			SSLSocket incoming = (SSLSocket)sss.accept();
			System.out.println("CLA connected to server");
			// get content from client
			BufferedReader in;
			in = new BufferedReader( new InputStreamReader(incoming.getInputStream()));
			// send content to client
			PrintWriter out = new PrintWriter(incoming.getOutputStream(), true);
			
			String str;
			while(!(str = in.readLine()).equals("")) {
				try {
					if(str == "validation number") {
						// create a random number
						out.println(generateRandomKey());
					}
				} catch(NumberFormatException nfe) {
					out.println("Sorry, not working (server)");
				}
			}
			incoming.close();
		} catch(Exception x) {
			System.out.println(x);
			x.printStackTrace();
		}
	}

	private SecureRandom generateRandomKey() {
		SecureRandom random = new SecureRandom();
		try {
			
			byte bytes[] = random.generateSeed(20);
			random = SecureRandom.getInstance("SHA1PRNG");
			random.setSeed(bytes);
			
		} catch( Exception x ) {
			System.out.println(x);
			x.printStackTrace();
		}

		return random;
	}

	public static void main (String[] args) {
		int port = CLIENT_PORT;
		if(args.length > 0) {
			port = Integer.parseInt(args[0]);
		}
		CLAServer addServe = new CLAServer(port);
		addServe.run();
	}
}