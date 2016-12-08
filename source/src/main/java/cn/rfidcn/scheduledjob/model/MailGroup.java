package cn.rfidcn.scheduledjob.model;

public class MailGroup {

	String problemName;
	String[] members;
	IdRange[] idRanges;

	public boolean isInGroup(int id){
        int low = 0;   
        int high = idRanges.length-1;   
        while(low <= high) {   
            int middle = (low + high)/2;   
            if(id>= idRanges[middle].getStart() && id<= idRanges[middle].getEnd()) {   
                return true;   
            }else if(id < idRanges[middle].getStart() ) {   
                high = middle - 1;   
            }else {   
                low = middle + 1;   
            }  
        }
        return false;  
	}
	
	public MailGroup(String problemName, String[] members, IdRange[] idRanges) {
		this.problemName = problemName;
		this.members = members;
		this.idRanges = idRanges;
	}
	public String[] getMembers() {
		return members;
	}
	public void setMembers(String[] members) {
		this.members = members;
	}
	public String getProblemName() {
		return problemName;
	}
	public void setProblemName(String problemName) {
		this.problemName = problemName;
	}

	public IdRange[] getIdRanges() {
		return idRanges;
	}
	public void setIdRanges(IdRange[] idRanges) {
		this.idRanges = idRanges;
	}

	@Override
	public int hashCode() {
		return problemName.hashCode();
	}
	
	
}
