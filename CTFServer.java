import java.io.*;
import java.util.*;
import java.security.*;
import java.net.*;
import javax.net.ssl.*;

public class CTFServer implements Runnable{
	private int port;
	private String keystore; 
	private String truststore;
	private ArrayList<Vote> theVotes;
	private Map<Integer, Integer> theResult;
	static final int CLIENT_PORT = 8190;
	static final int CLA_PORT = 8187;
	static private String KEYSTORE = "keystores/secureKeyStore.ks";
	static private String TRUSTSTORE = "keystores/secureTrustStore.ks";
	static private String CTFKEYSTORE = "keystores/ctfKeystore.ks";
	static private String CTFTRUSTSTORE = "keystores/CTFtruststore.ks";
	static final String STOREPASSWD = "abcdef";
	static final String ALIASPASSWD = "123456";
	SSLSocket incoming;

	//constructor
	CTFServer (int port, String keystore, String truststore){
		this.port = port;
		this.keystore = keystore;
		this.truststore = truststore;
	}

	public void setSSLSocket(SSLSocket s){
		this.incoming = s;
	}

	//create new empty votinglist
	public void setVotingList(){
		theVotes = new ArrayList<Vote>();
		theResult = new HashMap<Integer, Integer>();
	}


	public SSLServerSocket sslConnection(int port, String keyStore, String trustStore){
		try{
			KeyStore ks = KeyStore.getInstance("JCEKS");
			ks.load(new FileInputStream(keyStore),
				ALIASPASSWD.toCharArray());
			KeyStore ts = KeyStore.getInstance("JCEKS");
			ts.load(new FileInputStream(trustStore),
					STOREPASSWD.toCharArray());

			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, ALIASPASSWD.toCharArray());
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(ts);
			// initlize sslContext with kmf and tmf and null=default random number which generates a secret key
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			
			SSLServerSocketFactory sslServerFactory = sslContext.getServerSocketFactory();
			// start listning for connections on the specfic port
			SSLServerSocket sss = (SSLServerSocket)sslServerFactory.createServerSocket(port);
			sss.setEnabledCipherSuites(sss.getSupportedCipherSuites());

			return sss;
		}catch (Exception x) {
			System.out.println(x);
			x.printStackTrace();
		}
		
		return null;
	}

	//get the generated ssl key
	public void run(){
		try{
			//get & send content from client
			BufferedReader in = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
			PrintWriter out = new PrintWriter(incoming.getOutputStream(), true);

			String str = "";
			Boolean isCandidate = false;
			Boolean validVote = false;
			String userID = "";

			while(!(str = in.readLine()).equals("")){
				try{
					if(port == CLIENT_PORT){
						System.out.println("i CLIENT_PORT");
						String[] s = str.split(" ");
						int choice = Integer.parseInt(s[0]);

						if(choice == 2){
							//send result to client
							System.out.println("i 2");
							String res = sendResult();
							out.println(res);
							out.println("");
						}
						else{
							if(requestMyVote(s)){
								out.println("Your vote has been registred!");
								out.println("");
							}
							else{
								out.println("You already voted!");
								out.println("");
							} 
						}

						
					}
					else if(port == CLA_PORT){
						System.out.println("i CLA_PORT");
						//do something
						System.out.println("str: " + str);
						in.close();
						out.close();
						incoming.close();
						
						keystore = KEYSTORE;
						truststore = TRUSTSTORE;
						SSLServerSocket ss = sslConnection(CLIENT_PORT, keystore, truststore);
						System.out.println("sss: " + ss);
						incoming = (SSLSocket)ss.accept();
						//System.out.println("CTF is active with client");
					}
					
					
				}catch(NumberFormatException nfe){
					out.println("Sorry, something is wrong!");
				}
			}
			System.out.println("CTFport: " + port);
			in.close();

			run();

		}catch (Exception x) {
			System.out.println(x);
			x.printStackTrace();
		}

	}


	//gets a validation number from CLA
	//receives a message from Voter
	//check if the validation nr is correct with CLA & cross it off
	public Boolean requestCandidate(String userId){
		//System.out.println("userID: "+ userId);
		//Vote newVote = new Vote();
		return null;
	}
		
	//adds the identification number to the list of people who voted 
	//for a particular candidate and adds one to the tally
	public Boolean requestMyVote(String[] info){
		Vote v = new Vote(info[0], info[1], info[2], true);

		//check if the user aldready has voted or not
		//TODO: FIX THIS
		if(!theVotes.contains(v)){
			theVotes.add(v);
			int choice = Integer.parseInt(info[2]);
			//theResult.put(choice, theResult.getOrDefault(choice, 0) +1);

			theResult.put(choice, (theResult.get(choice)!= null) ? theResult.get(choice) : 0+1);
			return true;
		}
		else return false;
	
	}

	//send the result to client 
	public String sendResult(){
		int size = theVotes.size();
		String ans = "";
		for(Integer key: theResult.keySet()){
            System.out.println("Option: " + key + " - " + theResult.get(key));
            float res = 100*theResult.get(key)/size;
            ans += "Alternative " + key + ": " + res+"%" + " \n";
        }

        //print who has voted or not
        for(Vote v: theVotes){
        	ans += v.getUserID()+" \n";
        }

        return ans;
	}

	// CTF publishes the outcome, as well as the lists of identification numbers and for whom their owners voted.
	public static void main (String[] args){
		CTFServer addCTFServer;
		try{
			int port = CLA_PORT;
			
			String keystore = KEYSTORE;
			String truststore = TRUSTSTORE;
			addCTFServer = new CTFServer(port, keystore, truststore);
			addCTFServer.setVotingList();
			

			//while(true){
				SSLServerSocket sss = addCTFServer.sslConnection(port, keystore, truststore);
				SSLSocket incoming = (SSLSocket)sss.accept();
				System.out.println("CTF is active!: " );
				addCTFServer.setSSLSocket(incoming);
				addCTFServer.run();
				Thread t = new Thread(addCTFServer);
				t.start();
			//}
		}catch (Exception x) {
			System.out.println(x);
			x.printStackTrace();
		}
		
		
	}
}