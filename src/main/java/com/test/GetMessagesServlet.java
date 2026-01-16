package com.test;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/GetMessagesServlet")
public class GetMessagesServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json; charset=UTF-8"); // JSON으로 응답
		PrintWriter out = response.getWriter();
		
		String roomIDStr = request.getParameter("roomID");
		String lastMsgIDStr = request.getParameter("lastMsgID"); // ★ 마지막 메시지 ID 받기
		
		int lastMsgID = 0;
		if(lastMsgIDStr != null && !lastMsgIDStr.isEmpty()) {
			lastMsgID = Integer.parseInt(lastMsgIDStr);
		}

		if(roomIDStr == null) return;

		String dbUrl = "jdbc:oracle:thin:@localhost:9999:orcl";
		String dbUser = "DB2025_501_2"; 
		String dbPw = "qwer1234";

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPw);
			
			// ★ 핵심: 내가 가진 마지막 메시지(lastMsgID)보다 '큰(새로운)' 메시지만 가져오기
			String sqlMsg = "SELECT msg.*, m.Name as SenderName, a.Username as SenderID "
					+ "FROM Message msg "
					+ "JOIN Member m ON msg.Sender_ID = m.Member_ID "
					+ "JOIN Account a ON m.Account_ID = a.Account_ID "
					+ "WHERE msg.Chatroom_ID = ? AND msg.Message_ID > ? " // 이 부분 추가됨
					+ "ORDER BY msg.Sent_AT ASC";
			
			pstmt = conn.prepareStatement(sqlMsg);
			pstmt.setInt(1, Integer.parseInt(roomIDStr));
			pstmt.setInt(2, lastMsgID);
			rs = pstmt.executeQuery();

			// JSON 문자열 직접 만들기 (라이브러리 없이)
			// 예: [{"id":1, "text":"안녕", "sender":"user1"}, ...]
			StringBuilder json = new StringBuilder();
			json.append("[");
			boolean first = true;
			
			while(rs.next()) {
				if(!first) json.append(",");
				first = false;
				
				int msgID = rs.getInt("Message_ID");
				String text = rs.getString("Message_TEXT");
				String senderID = rs.getString("SenderID");
				String senderName = rs.getString("SenderName");
				
				// JSON 특수문자 처리 (따옴표 등)
				text = text.replace("\"", "\\\""); 
				
				json.append("{");
				json.append("\"id\":").append(msgID).append(",");
				json.append("\"text\":\"").append(text).append("\",");
				json.append("\"senderID\":\"").append(senderID).append("\",");
				json.append("\"senderName\":\"").append(senderName).append("\"");
				json.append("}");
			}
			json.append("]");
			
			out.print(json.toString());

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { if (conn != null) conn.close(); } catch (Exception e) {}
		}
	}
}