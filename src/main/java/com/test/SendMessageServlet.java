package com.test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/SendMessageServlet")
public class SendMessageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		
		HttpSession session = request.getSession();
		String myUserID = (String) session.getAttribute("userID");
		String roomIDStr = request.getParameter("roomID");
		String message = request.getParameter("message");
		
		if(myUserID == null || roomIDStr == null || message == null || message.trim().isEmpty()) {
			response.sendRedirect("MatchingServlet");
			return;
		}

		String dbUrl = "jdbc:oracle:thin:@localhost:9999:orcl";
		String dbUser = "DB2025_501_2"; 
		String dbPw = "qwer1234";

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPw);
			
			// 1. 내 Member_ID 찾기
			int myMemberID = 0;
			String sqlID = "SELECT Member_ID FROM Member m JOIN Account a ON m.Account_ID = a.Account_ID WHERE a.Username = ?";
			pstmt = conn.prepareStatement(sqlID);
			pstmt.setString(1, myUserID);
			rs = pstmt.executeQuery();
			if(rs.next()) myMemberID = rs.getInt(1);
			rs.close(); pstmt.close();
			
			// 2. 메시지 저장 (SEQ_MSG_ID 사용)
			String sqlInsert = "INSERT INTO Message (Message_ID, Chatroom_ID, Sender_ID, Message_TEXT, Sent_AT) "
					+ "VALUES (SEQ_MSG_ID.NEXTVAL, ?, ?, ?, SYSDATE)";
			
			pstmt = conn.prepareStatement(sqlInsert);
			pstmt.setInt(1, Integer.parseInt(roomIDStr));
			pstmt.setInt(2, myMemberID);
			pstmt.setString(3, message);
			pstmt.executeUpdate();
			
			// 3. 다시 채팅방으로 리다이렉트 (새로고침 효과)
			response.sendRedirect("ChatRoomServlet?roomID=" + roomIDStr);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { if (conn != null) conn.close(); } catch (Exception e) {}
		}
	}
}