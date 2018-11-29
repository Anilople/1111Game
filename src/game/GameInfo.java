package game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GameInfo extends HashMap<String, String>{

	private static String toJSONPair(String key, String value) {
		if(null == value) {
			return "\"" + key + "\"" + ":" + "null";
		} else {
			return "\"" + key + "\"" + ":" + "\"" + value + "\"";
		}
	}
	
	public String toJSON() {
		// ����map�е�key-value
		ArrayList<String> pairs = new ArrayList<String>();
		for(Map.Entry<String, String> entry : this.entrySet()) {
			pairs.add(toJSONPair(entry.getKey(), entry.getValue()));
		}
		
		// Ϊ��JSON�ַ������һ�����Ե�ĩβû�� ','
		StringBuffer JSON = new StringBuffer("{");
		for(int i = 0; i < pairs.size() - 1; i++) {
			JSON.append(pairs.get(i));
			JSON.append(',');
		}
		if(pairs.size() > 0) {
			// �������һ��pair
			String last = pairs.get(pairs.size() - 1);
			JSON.append(last);
		} else {
			// û��Ԫ����pairs��
		}
		JSON.append('}');
		return JSON.toString();
	}
	
	public static void main(String[] args) {
		GameInfo gameInfo = new GameInfo();
		gameInfo.put("action", "new");
		System.out.println(gameInfo.toJSON());
	}
}
