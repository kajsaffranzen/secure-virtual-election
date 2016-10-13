import java.io.*;
import java.security.*;
import java.net.*;
import javax.net.ssl.*;
import java.util.StringTokenizer;
import java.util.*;

//Class that maintains Validations ID Lists
public class CTFServer {
	//send back random validation number to voter & to CTF
	//exempel finns i artiekel

	private int port;
	static final int CTF_PORT = 8187;
	static final String KEYSTORE = "keystores/ctfKeystore.ks";
	static final String TRUSTSTORE = "keystores/CTFtruststore.ks";
	static final String STOREPASSWD = "abcdef";
	static final String ALIASPASSWD = "123456";

	SSLServerSocketFactory sslServerFactory;

	// the list 
	Hashtable<String, Integer> randomList = new Hashtable<String, Integer>();

	CTFServer (int port) {
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
			
			sslServerFactory = sslContext.getServerSocketFactory();

			listningToPort(sslServerFactory);
			
		} catch(Exception x) {
			System.out.println(x);
			x.printStackTrace();
		}
	}

	private void listningToPort(SSLServerSocketFactory sslServerFactory) throws IOException {

		// start listning for connections on the specfic port
		SSLServerSocket sss = (SSLServerSocket)sslServerFactory.createServerSocket(port);
		sss.setEnabledCipherSuites(sss.getSupportedCipherSuites());
		SSLSocket incoming = (SSLSocket)sss.accept();

		if(port == CTF_PORT) {
			System.out.println("Nu är jag här, CTF!");
			listningToVoter(incoming);
		} else System.out.println("Problem in PARADISE!");
		
		
		incoming.close();
	}

	private void listningToVoter(SSLSocket incoming) throws IOException{
		// get content from client
		BufferedReader in;
		in = new BufferedReader( new InputStreamReader(incoming.getInputStream()));
		// send content to client
		PrintWriter out = new PrintWriter(incoming.getOutputStream(), true);

		String str;
		while(!(str = in.readLine()).equals("")) {
			System.out.println(str);
		}
		
	}

	public static void main (String[] args) {
		int port = CTF_PORT;
		if(args.length > 0) {
			port = Integer.parseInt(args[0]);
		}
		CTFServer addServe = new CTFServer(port);
		addServe.run();
	}
}