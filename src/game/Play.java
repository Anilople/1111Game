package game;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Play extends HttpServlet{
	// ���ڵȴ������, �������userID�����ظ�
	static Queue<GameInfo> playersWaiting = new LinkedList<GameInfo>();
	// �ڵȴ�����������
	static Set<String> IDInQueue = new HashSet<String>();
	// ����������
	static Map<String, GameInfo> inPlaying = new HashMap<String, GameInfo>();
	// �����ж�һ��GameInfo�Ƿ�Ӧ�ô������ݿ�
	static Map<String, GameInfo> lastSave = new HashMap<String, GameInfo>();
	static int getTimes = 0;
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
			throws ServletException, IOException {
		// super.doGet(req, resp); // ��Ҫ�ø���ķ���, ������405����
		System.out.println(getTimes + " times");
		getTimes++;
		
		for(String key : inPlaying.keySet()) {
			System.out.println(key + "��inPlaying��");
		}
		
		for(String key : IDInQueue) {
			System.out.println(key + "��IDInQueue��");
		}
		
		// ƥ�����, ֮����Կ�һ���߳����������
		if(playersWaiting.size() >= 2) {
			System.out.println("���������ƥ����");
			matchEnemy(playersWaiting, IDInQueue, inPlaying);
		} else {
			System.out.println("�������������");
		}
		
		// ��ȡ���в���
		GameInfo gameInfo = new GameInfo();
		Enumeration<String> parameterNames = req.getParameterNames();
		while(parameterNames.hasMoreElements()) {
			String name = parameterNames.nextElement();
			String value = req.getParameter(name);
			// value ��ΪNULL, ��ʲô��û�е�ʱ��, Ϊ"", �����ַ���
			// name �϶���Ϊ��
			gameInfo.put(name, value);
		}
		
		System.out.println("�ͻ��˴���������:");
		System.out.println(gameInfo.toJSON());
		
		// �жϵõ���GameInfo�ǲ���Ҫ�������ݿ�
		String userID = gameInfo.get("userID");
		// last����Ϊnull, ��ΪlastSave����֮ǰû�з���userID��Ӧ��GameInfo
		GameInfo last = lastSave.get(userID);
		if(gameInfo.equals(last)) {
			// ��δ����ĺ��ϴ����, ���ô������ݿ�
		} else {
			// ���ϴβ����, Ӧ�ô������ݿ�
			// �Ƚ��µ�gameInfo����lastSave(Ҳ����˵�Ǹ����ϵ�)
			lastSave.put(userID, gameInfo);
			// �ٴ������ݿ�
			try {
				gameInfo.saveToDatabase();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("�������ݿ�ʧ��");
			}
		}
		
		// ִ�ж���action
		switch(String.valueOf(gameInfo.get("action"))) {
		case "new": // ����ƥ�����
			System.out.println("action = new");
			gameInfo = actionNew(gameInfo);
			break;
		case "pull": // ��ȡ���ֵĲ���
			System.out.println("action = pull");
			gameInfo = actionPull(gameInfo);
			break;
		case "push": // �������Լ��Ĳ���
			System.out.println("action = push");
			gameInfo = actionPush(gameInfo);
			break;
		case "over": // ������ս
			System.out.println("action = over");
			gameInfo = actionOver(gameInfo);
		case "null": // String Ϊ�յ�ʱ��
			break;
		default:
			break;
		}
		
		System.out.println("���ؿͻ��˵�����:");
		System.out.println(gameInfo.toJSON());
		resp.setContentType("application/json"); // ����ǰ���ø�ʽ
		resp.setCharacterEncoding("UTF-8"); // �����ַ�����
		resp.getWriter().println(gameInfo.toJSON());
		System.out.println("============================");
	}
	
	private static GameInfo actionNew(GameInfo gameInfo) {
		String userID = gameInfo.get("userID"); // ���a��ID
		if(inPlaying.containsKey(userID)) { // ��λ���a�Ѿ���ƥ���� 
			System.out.println(userID + "�Ѿ���ƥ��");
			// �ҵ�a�������Ϣ
			GameInfo newGameInfo = inPlaying.get(userID);
			newGameInfo.put("actionSuccess", "yes");
			gameInfo = newGameInfo;
		} else { // ��û�б�ƥ��
			if(IDInQueue.contains(userID)) { // �������Ѿ��ڶ�������, �����ټ���
				System.out.println(userID + " �Ѿ��ڶ�����.");
			} else { // �����Ҳ��ڶ�����, ���Լ���
				playersWaiting.add(gameInfo); // �������
				IDInQueue.add(userID); // ������a�Ѿ��������
				System.out.println("�� " + userID + " �������.");
			}
		}
		return gameInfo;
	}
	
	/*
	 * ���aҪ��ȡ����b�Ĳ���
	 * ��Ҫ��b.userOperation�л�ȡ
	 * ��ȡ��Ϣ��, ��b.messageTaken = "yes"
	 */
	private static GameInfo actionPull(GameInfo gameInfo) {
		String userID = gameInfo.get("userID");
		GameInfo userGameInfo = inPlaying.get(userID);
		
		if(null == userGameInfo) {
			// �����Ϣ��inPlaying���Ҳ���
			System.out.println(userID + "��inPlaying���Ҳ���");
			return gameInfo;
		} else {
			
		}
		
		String enemyID = userGameInfo.get("enemyID"); // ��inPlaying�л�ȡ����ID
		GameInfo enemyGameInfo = inPlaying.get(enemyID);
		if(null == enemyGameInfo) {
			// �Ҳ������ֵ���Ϣ
			gameInfo.put("enemyOnline", "no");
			System.out.println(enemyID + "������");
		} else {
			// ��ȡ���ֵĲ���
			String enemyOperation = enemyGameInfo.get("userOperation");
			if(!"".equals(enemyOperation)) { // �Ӷ��ֵ��л�õĲ�����Ϊ��, ˵�����������˲���
				// ���ڽ����ֵĲ���ȡ����, �ŵ����a��gameInfo��
				gameInfo.put("enemyOperation", enemyOperation);
				// ȡ������ζ�����a��pull�ɹ�, Ҳ��ζ�����b��push�ɹ�
				gameInfo.put("actionSuccess", "yes"); // ������a��action=pull�ɹ�
				enemyGameInfo.put("messageTaken", "yes"); // ������b��push�ɹ�, ���a�õ�����Ϣ
				// �ŵ����a��gameInfo��
				System.out.println("pull �ɹ�");
			} else {
				System.out.println("pull ʧ��");
			}
		}
		inPlaying.put(userID, gameInfo); // �������aԭ������Ϣ
		return gameInfo;
	}
	
	/*
	 * ���a�����Լ��Ĳ���, ������b, 
	 * ���͵���Ϣ����a.userOperation��, 
	 * ����ж϶��ֻ����Ϣ����?
	 * ͨ�������inPlaying��a.messageTaken, ���Ϊ"yes"
	 * ����a���൱��push�ɹ���
	 */
	private static GameInfo actionPush(GameInfo gameInfo) {
		String userID = gameInfo.get("userID");
		GameInfo userGameInfo = inPlaying.get(userID);
		
		if(null == userGameInfo) {
			// �����Ϣ��inPlaying���Ҳ���
			System.out.println(userID + "��inPlaying���Ҳ���");
			return gameInfo;
		} else {
			
		}
		
		String enemyID = userGameInfo.get("enemyID"); // ��inPlaying�л�ȡ����ID
		GameInfo enemyGameInfo = inPlaying.get(enemyID);
		if(null == enemyGameInfo) {
			// �Ҳ������ֵ���Ϣ
			gameInfo.put("enemyOnline", "no");
			System.out.println(enemyID + "������");
		} else {
			String userMessageTaken = userGameInfo.get("messageTaken");
			if("yes".equals(userMessageTaken)) { // ��Ϊ��, ˵�������õ�����Ϣ
				// push�ɹ���
				System.out.println("push �ɹ�");
				gameInfo.put("actionSuccess", "yes");
				gameInfo.put("messageTaken", "yes");
			} else { // userGameInfo.actionSuccess��Ϊyes, ˵��û��push�ɹ�
				
			}
		}
		inPlaying.put(userID, gameInfo); // �������aԭ������Ϣ
		return gameInfo;
	}
	
	/*
	 * ������Ϸ
	 */
	private static GameInfo actionOver(GameInfo gameInfo) {
		String userID = gameInfo.get("userID");
		// �������ɾ��
		inPlaying.remove(userID);
		// ����ƥ���ɾ��
		if(IDInQueue.contains(userID)) { // ��ƥ�������
			IDInQueue.remove(userID);
			Queue<GameInfo> newPlayersWaiting = new LinkedList<GameInfo>();
			// �������е�ֵһһȡ��
			for(GameInfo head = playersWaiting.poll(); null != head; head = playersWaiting.poll()) {
				if(userID.equals(head.get("userID"))) { // ��Ҫ������id
					
				} else { // ����Ҫ������ID
					// �ŵ��¶�����
					newPlayersWaiting.add(head);
				}
			}
			// ���϶��е����¶���
			playersWaiting = newPlayersWaiting;
		} else { // ����ƥ�������
			
		}
		gameInfo.put("actionSuccess", "yes");
		return gameInfo;
	}
	
	private static void matchEnemy(Queue<GameInfo> playersWaiting, Set<String> IDInQueue, Map<String, GameInfo> inPlaying) {
		GameInfo gameInfo1 = playersWaiting.poll();
		String userID1 = gameInfo1.get("userID");
		GameInfo gameInfo2 = playersWaiting.poll();
		String userID2 = gameInfo2.get("userID");
		// �Ƴ���set�е�ID
		IDInQueue.remove(userID1);
		IDInQueue.remove(userID2);
		// �������ǻ�Ϊ����
		gameInfo1.put("enemyID", userID2);
		gameInfo2.put("enemyID", userID1);
		// ����˭�ȿ�ʼ����, userTurn
		gameInfo1.put("userTurn", "yes");
		gameInfo2.put("userTurn", "no");
		// �����Ѿ��������Ҷ�����
		inPlaying.put(userID1, gameInfo1);
		inPlaying.put(userID2, gameInfo2);
		System.out.println(userID1 + " ��ս " + userID2);
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
