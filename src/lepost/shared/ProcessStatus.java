package lepost.shared;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

public class ProcessStatus extends HttpServlet {
	private final Logger LOG = LoggerFactory.getLogger(this.getClass());
	
	private static final long serialVersionUID = 4715467865497758004L;
	
	private static HashMap<String, String> statusPool = new HashMap<>();
	private static HashMap<String, Object> results = new HashMap<>();
	
	private static String status_title = "<div id=\"process_title\">Process Status...</div><br/>";
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		this.doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		LOG.info("Status reached!");		
		String key = req.getParameter("key");
		
		String reqType = req.getParameter("type");
		
		resp.setCharacterEncoding("utf-8");
		PrintWriter pw = null;
		
		if(reqType == null || reqType.isEmpty()){
			//By default return status
			String status = ProcessStatus.getStatus(key);
			if(status==null){
				status = "Status Unavailable!$"; //$ indicates no further status available
			}
			
			resp.setContentType("text");
			pw = resp.getWriter();
			pw.write(status_title+status);
		}else if(reqType.equals("result")){
			resp.setContentType("json");
			pw = resp.getWriter();
			
			Object res = results.get(key);
			JsonObject resJson = null;
			if(res == null){
				resJson = new JsonObject();
				resJson.addProperty("msg", "No result found!");
			}else {
				resJson = (JsonObject)res;
			}
			
			pw.print(resJson);
		}
		
		pw.flush();
		pw.close();
	}
	
	public static void stop(String key){
		synchronized(statusPool){
			if(statusPool.containsKey(key)){
				String update = statusPool.get(key)+"$"; //$ indicates no further status available
				statusPool.put(key, update);
			}
		}
	}
	
	public static void update(String key, String update){
		synchronized(statusPool){
			if(statusPool.containsKey(key)){
				update = statusPool.get(key)+update;
			}
			statusPool.put(key, update);
		}
	}
	
	public static String getStatus(String key){
		String status = null;
		synchronized(statusPool){
			if(statusPool.containsKey(key)){
				status = statusPool.get(key);
				if(status.endsWith("$")){
					statusPool.remove(key);
				}
			}
		}
		return status;
	}
	
	public static void saveResult(String key, Object res){
		synchronized(results){
			results.put(key, res);
		}
	}
}
