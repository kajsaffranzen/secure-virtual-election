import java.io.*;
import java.util.*;
import java.security.*;
import java.net.*;
import javax.net.ssl.*;

public class CTFServer{
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

	private ArrayList<String> randomNumbers; // = new ArrayList<String>();
	private ArrayList<Vote> theVotes; // = new ArrayList<Vote>();
	private Map<Integer, Integer> theResult;

	SSLSocket sslVoter, sslCTF;

	CTFServer(){
		System.out.println("Running");
	}

	public void setVotingList(){
		theVotes = new ArrayList<Vote>();
		theResult = new HashMap<Integer, Integer>();
		randomNumbers = new ArrayList<String>();
		Vote v = new Vote("1111", "Kajsa", "1", true);
		Vote v2 = new Vote("2222", "Cicci", "2", true);
		Vote v3 = new Vote("3333", "Kajsas kompis", "1", true);
		theVotes.add(v);
		theVotes.add(v2);
		theVotes.add(v3);
		theResult.put(1, 2);
		theResult.put(2, 1);
	}

	public Boolean requestMyVote(String[] info){
		Vote v = new Vote(info[0], info[1], info[2], true);

		//check if the user aldready has voted or not
		//TODO: FIXA SÅ ATT EN ANVÄNDARE EJ KAN RÖSTA FLERA GÅNGER
		if(!theVotes.contains(v)){
			theVotes.add(v);
			int choice = Integer.parseInt(info[2]);

			theResult.put(choice, (theResult.get(choice)!= null) ? theResult.get(choice) : 0+1);
			return true;
		}
		else return false;
	
	}

	public String getResult(){
		int size = theVotes.size();
		String ans = "";
		System.out.println("hej: ");
		System.out.println("size: " + theResult.size());
		for(Integer key: theResult.keySet()){
			System.out.println("i result.key: " + theResult.get(key));
            float res = 100*theResult.get(key)/size;
            ans += "Alternative " + key + ": " + res+"%" + " \n";
        }

        //print who has voted or not
        for(Vote v: theVotes){
        	ans += v.getUserID()+" \n";
        }

        return ans;
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
			System.out.println("CTF is connected!");

			BufferedReader socketIn;
			socketIn = new BufferedReader(new InputStreamReader(sslVoter.getInputStream()));

			BufferedReader socketIn2;
			socketIn2 = new BufferedReader(new InputStreamReader(sslCTF.getInputStream()));

			PrintWriter out = new PrintWriter(sslVoter.getOutputStream(), true);

			String str;

			while(!(str = socketIn2.readLine()).equals("")){
				System.out.println("CLA str: " + str);
				randomNumbers.add(str);
			}
			
			while(true){
				while(!(str = socketIn.readLine()).equals("")){
					try{
						String[] s = str.split(" ");
						int choice = Integer.parseInt(s[0]);

						if(choice == 2) {
							//TDOD: fixa så att man kan visa resultatet varje gång
							System.out.println("nu ska vi visa!!");
							if(!theResult.isEmpty()) {
								String res = getResult();
								out.println(res);
								out.println("");

							} else {
								System.out.println("No one has voted jet");
							}
						}
						else {
							//TODO: kolla ifall personen får rösta eller ej. 
							if(!randomNumbers.contains(s[0])) {
								System.out.println("You are not allowed to vote");
							} else {

								if(requestMyVote(s)) {
									//theVotes.add(v);
									System.out.println("Your vote has been registred");
									out.println("Your vote has been registred!");
									out.println("");
								} else {
									System.out.println("You have already voted");
									out.println("You already voted!");
									out.println("");
								}
							}
						}
					}catch(NumberFormatException nfe) {
						System.out.println("Sorry, something is wrong");
					}
				}
			}

		} catch(Exception x) {
			System.out.println(x);
			x.printStackTrace();
		}



	}

	public static void main(String[] args) {
		CTFServer ctf;
		try{
			ctf = new CTFServer();
			ctf.setVotingList();
			ctf.run();
			
		}catch(Exception x){
			System.out.println(x);
			x.printStackTrace();
		}
	}
}