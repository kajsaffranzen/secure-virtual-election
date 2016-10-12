public class Vote{
	public String userID;
	public String validationNr;
	public String voting;
	public Boolean hasVoted;

	public Vote(String _validationNr, String _userID, String _voting, Boolean _hasVoted){
    	this.validationNr = _validationNr;
    	this.userID = _userID;
    	this.voting = _voting;
    	this.hasVoted = _hasVoted;
    	
    }

    public String getUserID(){ return userID; }
    public String getValidationNr(){ return validationNr; }

    

}