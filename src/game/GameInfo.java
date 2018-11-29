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
		// 遍历map中的key-value
		ArrayList<String> pairs = new ArrayList<String>();
		for(Map.Entry<String, String> entry : this.entrySet()) {
			pairs.add(toJSONPair(entry.getKey(), entry.getValue()));
		}
		
		// 为了JSON字符串最后一个属性的末尾没有 ','
		StringBuffer JSON = new StringBuffer("{");
		for(int i = 0; i < pairs.size() - 1; i++) {
			JSON.append(pairs.get(i));
			JSON.append(',');
		}
		if(pairs.size() > 0) {
			// 放入最后一个pair
			String last = pairs.get(pairs.size() - 1);
			JSON.append(last);
		} else {
			// 没有元素在pairs中
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
