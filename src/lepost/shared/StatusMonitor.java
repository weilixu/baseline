package lepost.shared;

public abstract class StatusMonitor {
	public void updateStatus(String key, String update, boolean needNewLine){
		if(update.startsWith("ERROR")){
			if(update.endsWith("No Back")){
				update = "<div id=\"process_error\">"+update.split("\\|")[1]+"</div>";
			}else {
				update = "<div id=\"process_error\">"+update.split("\\|")[1]+"</div><a href=\"javascript: history.go(-1);\">Go Back</a>";
			}
		}else {
			update = "<div id=\"process\">"+update+"</div>";
			if(needNewLine){
				update += "<br/>";
			}
		}
		ProcessStatus.update(key, update);
	}
	
	public void stop(String key){
		ProcessStatus.stop(key);
	}
	
	public void saveResult(String key, Object object){
		ProcessStatus.saveResult(key, object);
	}
}
