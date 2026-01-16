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
import javax.servlet.http.HttpSession;

@WebServlet("/ChatListServlet")
public class ChatListServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();
		HttpSession session = request.getSession();
		String userID = (String) session.getAttribute("userID");
		if (userID == null) { response.sendRedirect("login.html"); return; }

		String dbUrl = "jdbc:oracle:thin:@localhost:9999:orcl";
		String dbUser = "DB2025_501_2"; 
		String dbPw = "qwer1234"; 

		Connection conn = null; PreparedStatement pstmt = null; ResultSet rs = null;

		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPw);
			int myMemberID = 0;
			String sqlID = "SELECT Member_ID FROM Member m JOIN Account a ON m.Account_ID = a.Account_ID WHERE a.Username = ?";
			pstmt = conn.prepareStatement(sqlID); pstmt.setString(1, userID); rs = pstmt.executeQuery();
			if(rs.next()) myMemberID = rs.getInt(1); rs.close(); pstmt.close();

			String sqlList = "SELECT c.Chatroom_ID, CASE WHEN mt.Member1_ID = ? THEN m2.Name ELSE m1.Name END AS TargetName, TO_CHAR(c.Created_AT, 'YYYY-MM-DD') as CreateDate FROM Chatroom c JOIN Match mt ON c.Match_ID = mt.Match_ID JOIN Member m1 ON mt.Member1_ID = m1.Member_ID JOIN Member m2 ON mt.Member2_ID = m2.Member_ID WHERE mt.Member1_ID = ? OR mt.Member2_ID = ? ORDER BY c.Created_AT DESC";
			pstmt = conn.prepareStatement(sqlList); pstmt.setInt(1, myMemberID); pstmt.setInt(2, myMemberID); pstmt.setInt(3, myMemberID); rs = pstmt.executeQuery();

			out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>채팅 목록</title>");
			out.println("<style>");
			out.println("@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');");
			out.println("body { font-family: 'Pretendard', sans-serif; background-color: #F2F4F6; margin: 0; padding-top: 80px; }");

			out.println("nav { position: fixed; top: 0; width: 100%; height: 70px; background: rgba(255,255,255,0.8); backdrop-filter: blur(20px); border-bottom: 1px solid rgba(0,0,0,0.05); display: flex; align-items: center; justify-content: center; z-index: 1000; }");
			out.println(".nav-content { width: 1000px; display: flex; justify-content: space-between; align-items: center; padding: 0 20px; }");
			out.println(".nav-logo { font-size: 20px; font-weight: 800; color: #3182F6; text-decoration: none; }");
			out.println(".nav-menu { display: flex; gap: 30px; }");
			out.println(".nav-item { text-decoration: none; color: #4E5968; font-weight: 600; font-size: 16px; transition: 0.2s; }");
			out.println(".nav-item:hover { color: #3182F6; }");
			out.println(".nav-item.active { color: #3182F6; }");

			out.println(".container { max-width: 800px; margin: 0 auto; padding: 40px 20px; }");
			out.println("h2 { font-size: 28px; font-weight: 800; color: #191F28; margin-bottom: 30px; }");
			
			out.println(".chat-item { background: white; padding: 24px; border-radius: 20px; margin-bottom: 16px; display: flex; justify-content: space-between; align-items: center; cursor: pointer; transition: 0.2s; border: 1px solid transparent; box-shadow: 0 4px 20px rgba(0,0,0,0.03); }");
			out.println(".chat-item:hover { transform: translateY(-3px); box-shadow: 0 10px 30px rgba(0,0,0,0.05); border-color: #3182F6; }");
			
			out.println(".chat-left { display: flex; align-items: center; gap: 20px; }");
			out.println(".avatar { width: 56px; height: 56px; background: #E8F3FF; color: #3182F6; border-radius: 20px; display: flex; justify-content: center; align-items: center; font-weight: 700; font-size: 20px; }");
			out.println(".chat-name { font-size: 18px; font-weight: 700; color: #191F28; margin-bottom: 4px; }");
			out.println(".chat-date { font-size: 14px; color: #8B95A1; }");
			
			out.println(".leave-btn { padding: 10px 16px; background: #FFF0F0; color: #E93535; border: none; border-radius: 12px; font-weight: 600; font-size: 14px; cursor: pointer; transition: 0.2s; }");
			out.println(".leave-btn:hover { background: #FFE5E5; }");

			out.println("</style></head><body>");
			
			out.println("<nav><div class='nav-content'><a href='MatchingServlet' class='nav-logo'>Ideal Match</a>");
			out.println("<div class='nav-menu'><a href='MatchingServlet' class='nav-item'>홈</a><a href='MyPageServlet' class='nav-item'>내 정보</a><a href='PreferenceFormServlet' class='nav-item'>선호도 설정</a><a href='ChatListServlet' class='nav-item active'>채팅</a><a href='LogoutServlet' class='nav-item' style='color:#EF4444;'>로그아웃</a></div></div></nav>");

			out.println("<div class='container'>");
			out.println("<h2>메시지함</h2>");
			
			int count = 0;
			while(rs.next()) {
				count++;
				int roomID = rs.getInt("Chatroom_ID");
				String targetName = rs.getString("TargetName");
				String date = rs.getString("CreateDate");
				String initial = targetName.substring(0, 1);
				
				out.println("<div class='chat-item' onclick=\"location.href='ChatRoomServlet?roomID=" + roomID + "'\">");
				out.println("<div class='chat-left'><div class='avatar'>" + initial + "</div>");
				out.println("<div><div class='chat-name'>" + targetName + "</div><div class='chat-date'>" + date + "부터 대화 중</div></div></div>");
				out.println("<button class='leave-btn' onclick=\"event.stopPropagation(); if(confirm('나가시겠습니까?')) { location.href='DeleteChatServlet?roomID=" + roomID + "'; }\">나가기</button>");
				out.println("</div>");
			}
			if(count == 0) out.println("<div style='text-align:center; color:#8B95A1; margin-top:100px;'>진행 중인 대화가 없습니다.</div>");
			
			out.println("</div></body></html>");

		} catch (Exception e) { e.printStackTrace(); } finally { try { if(conn!=null) conn.close(); } catch(Exception e){} }
	}
}