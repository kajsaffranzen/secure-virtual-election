import java.io.*;
import java.util.*;
import java.net.*;
import java.security.KeyStore;
import javax.net.ssl.*;

public class Voter{
	private InetAddress host;
	private int port;

	static final int DEFAULT_PORT = 8189;
	static final String KEYSTORE = "keystores/secureKeyStore.ks";
	static final String TRUSTSTORE = "keystores/secureTrustStore.ks";
	static final String STOREPASSWD = "abcdef";
	static final String ALIASPASSWD = "123456";


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
			// negotiating with the server to agree upon the cipher suite that will be used
			SSLSocket client = (SSLSocket)sslFact.createSocket(host, port);
			client.setEnabledCipherSuites(client.getSupportedCipherSuites());

			BufferedReader socketIn;
			socketIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
			PrintWriter socketOut = new PrintWriter(client.getOutputStream(), true);

			String userID = "kajsa:1234";
			socketOut.println(userID);
			socketOut.println("");

		} catch (Exception x) {
			System.out.println(x);
			x.printStackTrace();
		}
	}

	//send a message to the CLA asking for a validation number.

	//creates a identification number
	//creates a message: identification nr, validation nr & vote
	//sends the message to CTF

	public static void main (String[] args){
		try {
			InetAddress host = InetAddress.getLocalHost();
			int port = DEFAULT_PORT;
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