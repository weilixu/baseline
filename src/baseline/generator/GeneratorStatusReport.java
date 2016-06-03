package baseline.generator;

public class GeneratorStatusReport {
	private boolean isSuccess;
	
	private boolean hasErrorFile;
	private String errorFileName;
	
	private boolean hasErrorMsg;
	private String errorMsg;
	
	public GeneratorStatusReport(){
		this.isSuccess = true;
	}

	public boolean isSuccess() {
		return isSuccess;
	}

	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}

	public String getErrorFileName() {
		return errorFileName;
	}

	public void setErrorFileName(String errorFileName) {
		this.errorFileName = errorFileName;
	}

	public boolean isHasErrorFile() {
		return hasErrorFile;
	}

	public void setHasErrorFile(boolean hasErrorFile) {
		this.hasErrorFile = hasErrorFile;
	}

	public boolean isHasErrorMsg() {
		return hasErrorMsg;
	}

	public void setHasErrorMsg(boolean hasErrorMsg) {
		this.hasErrorMsg = hasErrorMsg;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	
	public void processError(String msg, String errorFile){
		this.isSuccess = false;
		
		if(msg!=null){
			this.errorMsg = msg;
			this.hasErrorMsg = true;
		}
		
		if(errorMsg!=null){
			this.errorFileName = errorFile;
			this.hasErrorFile = true;
		}
	}
}
