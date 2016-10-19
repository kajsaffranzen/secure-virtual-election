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

	private ArrayList<String> randomNumbers;
	private ArrayList<String> thecheckOff;
	private ArrayList<Vote> theVotes; 
	private Map<Integer, Integer> theResult;

	CTFServer(){
		System.out.println("Running");
	}

	public void setVotingList(){
		theVotes = new ArrayList<Vote>();
		theResult = new HashMap<Integer, Integer>();
		randomNumbers = new ArrayList<String>();
		thecheckOff = new ArrayList<String>();
		
		//create dummy data
		theVotes.add(new Vote("1111", "Kajsa", "1", true));
		theVotes.add(new Vote("2222", "Cicci", "2", true));
		theVotes.add(new Vote("3333", "Kajsas kompis", "1", true));
		theResult.put(1, 2);
		theResult.put(2, 1);
	}

	public Boolean requestMyVote(String[] info){
		Vote v = new Vote(info[0], info[1], info[2], true);

		//check if the user aldready has voted or not
		if(randomNumbers.contains(info[0]) && !thecheckOff.contains(info[0])){
			theVotes.add(v);
			int choice = Integer.parseInt(info[2]);
			thecheckOff.add(info[0]);
			theResult.put(choice, (theResult.get(choice)!= null) ? theResult.get(choice) : 0+1);
			return true;
		}
		else return false;
	
	}

	public String getResult(String rnum){
		int size = theVotes.size();
		String ans = "";
		for(Integer key: theResult.keySet()){
            float res = 100*theResult.get(key)/size;
            ans += "Alternative " + key + ": " + res+"%" + " \n";
        }
        
        //print who has voted or not
        ans += " Voters: \n";
        for(Vote v: theVotes){
        	ans += v.getUserID()+" \n";
        }

        if(thecheckOff.contains(rnum)){
        	for(Vote v: theVotes) {
        		if(Integer.parseInt(v.getValidationNr()) == Integer.parseInt(rnum)) {
        			ans += "\n You voted: " + v.getVote() + " \n";
        			break;
        		}
        	}
        } else {
        	ans += "You have not voted yet \n";
        }

        return ans;
	}

	public void run(){
		try{
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

			String claStr = "";

			while(!(claStr = socketIn2.readLine()).equals("")){
				System.out.println("CLA str: " + claStr);
				randomNumbers.add(claStr);
			}
			
			do{
				String str = "";
				while(!(str = socketIn.readLine()).equals("")){
					try{
						String[] s = str.split(" ");
						int choice = Integer.parseInt(s[0]);
						
						if(choice == 2) {
							if(!theVotes.isEmpty()) {
								String res = getResult(s[1]);
								out.println(res);
								out.println("");
							} else {
								System.out.println("No one has voted jet");
							}
						}
						else {
							if(!randomNumbers.contains(s[0])) {
								System.out.println("You are not allowed to vote");
							} else {

								if(requestMyVote(s)) {
									out.println("Your vote has been registred!");
									out.println("");
								} else {
									out.println("You have already voted!");
									out.println("");
								}
							}
						}
					}catch(NumberFormatException nfe) {
						System.out.println("Sorry, something is wrong");
					}
				}
				
			}while(true);

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