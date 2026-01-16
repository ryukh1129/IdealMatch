package com.test;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/DeleteChatServlet")
public class DeleteChatServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();
		
		HttpSession session = request.getSession();
		String userID = (String) session.getAttribute("userID");
		String roomIDStr = request.getParameter("roomID");
		
		if (userID == null) { response.sendRedirect("login.html"); return; }
		if (roomIDStr == null) { response.sendRedirect("ChatListServlet"); return; }

		String dbUrl = "jdbc:oracle:thin:@localhost:9999:orcl";
		String dbUser = "DB2025_501_2"; 
		String dbPw = "qwer1234";

		Connection conn = null;
		PreparedStatement pstmt = null;

		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPw);
			
			// ★ 채팅방 삭제 (Cascade 설정 덕분에 메시지도 자동 삭제됨)
			String sql = "DELETE FROM Chatroom WHERE Chatroom_ID = ?";
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, Integer.parseInt(roomIDStr));
			
			int result = pstmt.executeUpdate();
			
			if(result > 0) {
				// 삭제 성공 시 채팅 목록으로 바로 이동 (새로고침 효과)
				response.sendRedirect("ChatListServlet");
			} else {
				out.println("<script>alert('삭제 실패: 방을 찾을 수 없습니다.'); history.back();</script>");
			}

		} catch (Exception e) {
			e.printStackTrace();
			out.println("<script>alert('오류 발생: " + e.getMessage() + "'); history.back();</script>");
		} finally {
			try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
			try { if (conn != null) conn.close(); } catch (Exception e) {}
		}
	}
}