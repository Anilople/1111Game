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
	
	// ��ȡ���ݿ�����
	static {
		// 1. ע������, ���ʹ�� ���似��
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// 2.������ݿ�����
		// static Connnection getConnection(String url, String user, String password);
		// url -- ���ݿ��ַ
		// 		jdbc:mysql://��������ip:�˿ں�/���ݿ�����
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
		// ��˳��ź�value
		for(String name : names) {
			String value = this.get(name);
			if(null == value) {
				values.append("\"\"");
			} else {
				values.append("\"" + value + "\"");
			}
			values.append(",");
		}
		// ȥ��ĩβ�� ','
		values.deleteCharAt(values.length() - 1);
		return values;
	}
	
	public void saveToDatabase() throws SQLException {
		if(null == c) {
			System.out.println("û��connecttion");
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
		// 3.������ִ��ƽ̨
		PreparedStatement s = c.prepareStatement(statement);
		// 4.ִ��sql���
		int ans = s.executeUpdate();
		System.out.println("execute result:" + ans);
		
		// 6.�ͷ���Դ
		s.close();
		// ��Ҫ��static��connection���ͷ���
		// c.close();
	}
	
	private static String toJSONPair(String key, String value) {
		if(null == value) {
			return "\"" + key + "\"" + ":" + "null";
		} else {
			return "\"" + key + "\"" + ":" + "\"" + value + "\"";
		}
	}
	
	// �Ƚ�2��GameInfo�Ƿ���ͬ
	// ����key���ɵļ�����ͬ, ����key���Ӧ��value��ͬ, �Ϳ�����Ϊ������ͬ
	public boolean equals(GameInfo gameInfo) {
		if(null == gameInfo) {
			return false;
		} else {
			
		}
		
		Set<String> aKeys = this.keySet();
		Set<String> bKeys = gameInfo.keySet();
		
		// ȷ������ aKey ���� bKeys ��
		for(String aKey : aKeys) {
			if(!bKeys.contains(aKey)) {
				return false;
			} else {
				
			}
		}
		
		// ȷ������ bKey ���� aKeys ��
		for(String bKey : bKeys) {
			if(!aKeys.contains(bKey)) {
				return false;
			} else {
				
			}
		}
		
		// ���е�����, aKeys = bKeys
		for(String key : aKeys) {
			String aValue = this.get(key);
			String bValue = gameInfo.get(key);
			if(null == aValue && null == bValue) {
				// ��������forѭ��
				continue;
			} else if(null == aValue && null != bValue) {
				return false;
			} else if(null != aValue && null == bValue) {
				return false;
			} else { // 2������Ϊnull
				if(!aValue.equals(bValue)) {
					return false;
				} else {
					// ��������forѭ��
					continue;
				}
			}
		}
		
		// ���е�����, ����˵2��GameInfo�ȼ���
		return true;
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
