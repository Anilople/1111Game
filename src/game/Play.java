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
	// 正在等待的玩家, 队列里的userID不能重复
	static Queue<GameInfo> playersWaiting = new LinkedList<GameInfo>();
	// 在等待队列里的玩家
	static Set<String> IDInQueue = new HashSet<String>();
	// 正在玩的玩家
	static Map<String, GameInfo> inPlaying = new HashMap<String, GameInfo>();
	// 用来判断一个GameInfo是否应该存入数据库
	static Map<String, GameInfo> lastSave = new HashMap<String, GameInfo>();
	static int getTimes = 0;
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
			throws ServletException, IOException {
		// super.doGet(req, resp); // 不要用父类的方法, 否则有405错误
		System.out.println(getTimes + " times");
		getTimes++;
		
		for(String key : inPlaying.keySet()) {
			System.out.println(key + "在inPlaying中");
		}
		
		for(String key : IDInQueue) {
			System.out.println(key + "在IDInQueue中");
		}
		
		// 匹配对手, 之后可以开一个线程来做这件事
		if(playersWaiting.size() >= 2) {
			System.out.println("玩家数量够匹配了");
			matchEnemy(playersWaiting, IDInQueue, inPlaying);
		} else {
			System.out.println("玩家数量还不够");
		}
		
		// 获取所有参数
		GameInfo gameInfo = new GameInfo();
		Enumeration<String> parameterNames = req.getParameterNames();
		while(parameterNames.hasMoreElements()) {
			String name = parameterNames.nextElement();
			String value = req.getParameter(name);
			// value 不为NULL, 当什么都没有的时候, 为"", 即空字符串
			// name 肯定不为空
			gameInfo.put(name, value);
		}
		
		System.out.println("客户端传来的数据:");
		System.out.println(gameInfo.toJSON());
		
		// 判断得到的GameInfo是不是要存入数据库
		String userID = gameInfo.get("userID");
		// last可能为null, 因为lastSave可能之前没有放入userID对应的GameInfo
		GameInfo last = lastSave.get(userID);
		if(gameInfo.equals(last)) {
			// 这次传来的和上次相等, 不用存入数据库
		} else {
			// 和上次不相等, 应该存入数据库
			// 先将新的gameInfo放入lastSave(也可以说是覆盖老的)
			lastSave.put(userID, gameInfo);
			// 再存入数据库
			try {
				gameInfo.saveToDatabase();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("存入数据库失败");
			}
		}
		
		// 执行动作action
		switch(String.valueOf(gameInfo.get("action"))) {
		case "new": // 申请匹配对手
			System.out.println("action = new");
			gameInfo = actionNew(gameInfo);
			break;
		case "pull": // 拉取对手的操作
			System.out.println("action = pull");
			gameInfo = actionPull(gameInfo);
			break;
		case "push": // 给对手自己的操作
			System.out.println("action = push");
			gameInfo = actionPush(gameInfo);
			break;
		case "over": // 结束对战
			System.out.println("action = over");
			gameInfo = actionOver(gameInfo);
		case "null": // String 为空的时候
			break;
		default:
			break;
		}
		
		System.out.println("返回客户端的数据:");
		System.out.println(gameInfo.toJSON());
		resp.setContentType("application/json"); // 返回前设置格式
		resp.setCharacterEncoding("UTF-8"); // 设置字符编码
		resp.getWriter().println(gameInfo.toJSON());
		System.out.println("============================");
	}
	
	private static GameInfo actionNew(GameInfo gameInfo) {
		String userID = gameInfo.get("userID"); // 玩家a的ID
		if(inPlaying.containsKey(userID)) { // 这位玩家a已经被匹配了 
			System.out.println(userID + "已经被匹配");
			// 找到a的相关信息
			GameInfo newGameInfo = inPlaying.get(userID);
			newGameInfo.put("actionSuccess", "yes");
			gameInfo = newGameInfo;
		} else { // 还没有被匹配
			if(IDInQueue.contains(userID)) { // 这个玩家已经在队列里了, 不能再加入
				System.out.println(userID + " 已经在队列里.");
			} else { // 这个玩家不在队列里, 可以加入
				playersWaiting.add(gameInfo); // 加入队列
				IDInQueue.add(userID); // 标记玩家a已经加入队列
				System.out.println("将 " + userID + " 加入队列.");
			}
		}
		return gameInfo;
	}
	
	/*
	 * 玩家a要获取对手b的操作
	 * 需要到b.userOperation中获取
	 * 获取信息后, 将b.messageTaken = "yes"
	 */
	private static GameInfo actionPull(GameInfo gameInfo) {
		String userID = gameInfo.get("userID");
		GameInfo userGameInfo = inPlaying.get(userID);
		
		if(null == userGameInfo) {
			// 玩家信息在inPlaying中找不到
			System.out.println(userID + "在inPlaying中找不到");
			return gameInfo;
		} else {
			
		}
		
		String enemyID = userGameInfo.get("enemyID"); // 从inPlaying中获取对手ID
		GameInfo enemyGameInfo = inPlaying.get(enemyID);
		if(null == enemyGameInfo) {
			// 找不到对手的信息
			gameInfo.put("enemyOnline", "no");
			System.out.println(enemyID + "已下线");
		} else {
			// 获取对手的操作
			String enemyOperation = enemyGameInfo.get("userOperation");
			if(!"".equals(enemyOperation)) { // 从对手当中获得的操作不为空, 说明对手推送了操作
				// 现在将对手的操作取出来, 放到玩家a的gameInfo中
				gameInfo.put("enemyOperation", enemyOperation);
				// 取出来意味着玩家a的pull成功, 也意味着玩家b的push成功
				gameInfo.put("actionSuccess", "yes"); // 标记玩家a的action=pull成功
				enemyGameInfo.put("messageTaken", "yes"); // 标记玩家b的push成功, 玩家a拿到了消息
				// 放到玩家a的gameInfo中
				System.out.println("pull 成功");
			} else {
				System.out.println("pull 失败");
			}
		}
		inPlaying.put(userID, gameInfo); // 覆盖玩家a原来的信息
		return gameInfo;
	}
	
	/*
	 * 玩家a推送自己的操作, 给对手b, 
	 * 推送的信息放在a.userOperation中, 
	 * 如何判断对手获得信息了呢?
	 * 通过检测在inPlaying中a.messageTaken, 如果为"yes"
	 * 这样a就相当于push成功了
	 */
	private static GameInfo actionPush(GameInfo gameInfo) {
		String userID = gameInfo.get("userID");
		GameInfo userGameInfo = inPlaying.get(userID);
		
		if(null == userGameInfo) {
			// 玩家信息在inPlaying中找不到
			System.out.println(userID + "在inPlaying中找不到");
			return gameInfo;
		} else {
			
		}
		
		String enemyID = userGameInfo.get("enemyID"); // 从inPlaying中获取对手ID
		GameInfo enemyGameInfo = inPlaying.get(enemyID);
		if(null == enemyGameInfo) {
			// 找不到对手的信息
			gameInfo.put("enemyOnline", "no");
			System.out.println(enemyID + "已下线");
		} else {
			String userMessageTaken = userGameInfo.get("messageTaken");
			if("yes".equals(userMessageTaken)) { // 不为空, 说明对手拿到了信息
				// push成功了
				System.out.println("push 成功");
				gameInfo.put("actionSuccess", "yes");
				gameInfo.put("messageTaken", "yes");
			} else { // userGameInfo.actionSuccess不为yes, 说明没有push成功
				
			}
		}
		inPlaying.put(userID, gameInfo); // 覆盖玩家a原来的信息
		return gameInfo;
	}
	
	/*
	 * 结束游戏
	 */
	private static GameInfo actionOver(GameInfo gameInfo) {
		String userID = gameInfo.get("userID");
		// 将在玩的删除
		inPlaying.remove(userID);
		// 将在匹配的删除
		if(IDInQueue.contains(userID)) { // 在匹配队列里
			IDInQueue.remove(userID);
			Queue<GameInfo> newPlayersWaiting = new LinkedList<GameInfo>();
			// 将队列中的值一一取出
			for(GameInfo head = playersWaiting.poll(); null != head; head = playersWaiting.poll()) {
				if(userID.equals(head.get("userID"))) { // 是要结束的id
					
				} else { // 不是要结束的ID
					// 放到新队列中
					newPlayersWaiting.add(head);
				}
			}
			// 将老队列等于新队列
			playersWaiting = newPlayersWaiting;
		} else { // 不在匹配队列里
			
		}
		gameInfo.put("actionSuccess", "yes");
		return gameInfo;
	}
	
	private static void matchEnemy(Queue<GameInfo> playersWaiting, Set<String> IDInQueue, Map<String, GameInfo> inPlaying) {
		GameInfo gameInfo1 = playersWaiting.poll();
		String userID1 = gameInfo1.get("userID");
		GameInfo gameInfo2 = playersWaiting.poll();
		String userID2 = gameInfo2.get("userID");
		// 移除在set中的ID
		IDInQueue.remove(userID1);
		IDInQueue.remove(userID2);
		// 设置他们互为对手
		gameInfo1.put("enemyID", userID2);
		gameInfo2.put("enemyID", userID1);
		// 设置谁先开始操作, userTurn
		gameInfo1.put("userTurn", "yes");
		gameInfo2.put("userTurn", "no");
		// 放入已经在玩的玩家队列中
		inPlaying.put(userID1, gameInfo1);
		inPlaying.put(userID2, gameInfo2);
		System.out.println(userID1 + " 对战 " + userID2);
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
