package baseline.main;

import lepost.shared.StatusMonitor;

public class TestMonitor extends StatusMonitor{
	@Override
	public void updateStatus(String key, String update, boolean needNewLine){
		System.out.println(update);
		if(needNewLine){
			System.out.println();
		}
	}
	
	@Override
	public void stop(String key){
		return;
	}
}
