package game;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class GameInfo extends HashMap<String, String>{
	static Connection c = null;
	static private final String[] names = {
			"action",
			"actionSuccess",
			"status",
			"userID",
			"enemyID",
			"userOperation",
			"enemyOperation",
			"messageTaken",
			"userTurn",
			"enemyLeft",
			"enemyRight",
			"userLeft",
			"userRight",
			"enemyOnline",
	};
	
	// 获取数据库连接
	static {
		// 1. 注册驱动, 最好使用 反射技术
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// 2.获得数据库连接
		// static Connnection getConnection(String url, String user, String password);
		// url -- 数据库地址
		// 		jdbc:mysql://连接主机ip:端口号/数据库名字
		String url = "jdbc:mysql://localhost:3306/1111Game";
		String user = "1111Game";
		String password = "1111";
		try {
			c = DriverManager.getConnection(url, user, password);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(c);
	}
	
	private StringBuffer getSQLValues() {
		StringBuffer values = new StringBuffer("");
		// 按顺序放好value
		for(String name : names) {
			String value = this.get(name);
			if(null == value) {
				values.append("\"\"");
			} else {
				values.append("\"" + value + "\"");
			}
			values.append(",");
		}
		// 去除末尾的 ','
		values.deleteCharAt(values.length() - 1);
		return values;
	}
	
	public void saveToDatabase() throws SQLException {
		if(null == c) {
			System.out.println("没有connecttion");
			return;
		} else {
			
		}
		
		String prefix = "INSERT INTO GameInfo (action,\r\n" + 
				"actionSuccess,\r\n"+ 
				"status,\r\n" + 
				"userID,\r\n" + 
				"enemyID,\r\n" + 
				"userOperation,\r\n" + 
				"enemyOperation,\r\n" + 
				"messageTaken,\r\n" + 
				"userTurn,\r\n" + 
				"enemyLeft,\r\n" + 
				"enemyRight,\r\n" + 
				"userLeft,\r\n" + 
				"userRight,\r\n" + 
				"enemyOnline) VALUES ";
		StringBuffer values = getSQLValues();
		String statement = prefix + "(" + values.toString() + ");";
		System.out.println("sql command:");
		System.out.println(statement);
		// 3.获得语句执行平台
		PreparedStatement s = c.prepareStatement(statement);
		// 4.执行sql语句
		int ans = s.executeUpdate();
		System.out.println("execute result:" + ans);
		
		// 6.释放资源
		s.close();
		// 不要将static的connection给释放了
		// c.close();
	}
	
	private static String toJSONPair(String key, String value) {
		if(null == value) {
			return "\"" + key + "\"" + ":" + "null";
		} else {
			return "\"" + key + "\"" + ":" + "\"" + value + "\"";
		}
	}
	
	// 比较2个GameInfo是否相同
	// 他们key构成的集合相同, 并且key相对应的value相同, 就可以认为他们相同
	public boolean equals(GameInfo gameInfo) {
		if(null == gameInfo) {
			return false;
		} else {
			
		}
		
		Set<String> aKeys = this.keySet();
		Set<String> bKeys = gameInfo.keySet();
		
		// 确保所有 aKey 都在 bKeys 中
		for(String aKey : aKeys) {
			if(!bKeys.contains(aKey)) {
				return false;
			} else {
				
			}
		}
		
		// 确保所有 bKey 都在 aKeys 中
		for(String bKey : bKeys) {
			if(!aKeys.contains(bKey)) {
				return false;
			} else {
				
			}
		}
		
		// 运行到这里, aKeys = bKeys
		for(String key : aKeys) {
			String aValue = this.get(key);
			String bValue = gameInfo.get(key);
			if(null == aValue && null == bValue) {
				// 继续运行for循环
				continue;
			} else if(null == aValue && null != bValue) {
				return false;
			} else if(null != aValue && null == bValue) {
				return false;
			} else { // 2个都不为null
				if(!aValue.equals(bValue)) {
					return false;
				} else {
					// 继续运行for循环
					continue;
				}
			}
		}
		
		// 运行到这里, 可以说2个GameInfo等价了
		return true;
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
	
	public static void main(String[] args) throws SQLException {
//		GameInfo gameInfo = new GameInfo();
//		gameInfo.put("action", "push");
//		// System.out.println(gameInfo.getSQLValues());
//		gameInfo.saveToDatabase();
		GameInfo a = new GameInfo();
		GameInfo b = new GameInfo();
		a.put("action", "push");
		b.put("action", "push");
		System.out.println(a.equals(b));
		// System.out.println(gameInfo.toJSON());
	}
}
