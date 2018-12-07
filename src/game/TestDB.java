package game;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 * 用来测试在服务器有没有办法使用数据库
 */
public class TestDB extends HttpServlet{
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		resp.setCharacterEncoding("UTF-8"); // 设置字符编码
		GameInfo gameInfo = new GameInfo();
		try {
			gameInfo.saveToDatabase();
			resp.getWriter().println("数据库正常");
			resp.getWriter().println("database work.");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			resp.getWriter().println("数据库异常!");
			resp.getWriter().println("database not work!");
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}

}
