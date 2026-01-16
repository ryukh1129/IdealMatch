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

@WebServlet("/StartChatServlet")
public class StartChatServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// ★ 가장 먼저 인코딩 설정 (한글 깨짐 방지)
		response.setContentType("text/html; charset=UTF-8");
		request.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter(); // 화면 출력용 펜

		HttpSession session = request.getSession();
		String myUserID = (String) session.getAttribute("userID");
		String targetIDStr = request.getParameter("targetID");

		// 1. 기본 검사
		if (myUserID == null) { 
			out.println("<script>alert('로그인이 필요합니다.'); location.href='login.html';</script>");
			return; 
		}
		if (targetIDStr == null) { 
			out.println("<script>alert('상대방 정보가 없습니다.'); history.back();</script>");
			return; 
		}

		int targetMemberID = Integer.parseInt(targetIDStr);

		String dbUrl = "jdbc:oracle:thin:@localhost:9999:orcl";
		String dbUser = "DB2025_501_2"; 
		String dbPw = "qwer1234"; 

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPw);
			conn.setAutoCommit(false); // 수동 커밋 모드

			// 2. 내 Member_ID 찾기
			int myMemberID = 0;
			String sqlMyID = "SELECT Member_ID FROM Member m JOIN Account a ON m.Account_ID = a.Account_ID WHERE a.Username = ?";
			pstmt = conn.prepareStatement(sqlMyID);
			pstmt.setString(1, myUserID);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				myMemberID = rs.getInt(1);
			} else {
				throw new Exception("내 회원 정보를 DB에서 찾을 수 없습니다. (ID: " + myUserID + ")");
			}
			rs.close(); pstmt.close();

			// 3. 매칭(Match) 확인
			int matchID = 0;
			String sqlCheckMatch = "SELECT Match_ID FROM Match WHERE (Member1_ID=? AND Member2_ID=?) OR (Member1_ID=? AND Member2_ID=?)";
			pstmt = conn.prepareStatement(sqlCheckMatch);
			pstmt.setInt(1, myMemberID); pstmt.setInt(2, targetMemberID);
			pstmt.setInt(3, targetMemberID); pstmt.setInt(4, myMemberID);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				matchID = rs.getInt(1);
			} else {
				// 매칭 없으면 새로 생성
				pstmt.close(); // 기존 pstmt 닫기
				String sqlNewMatch = "INSERT INTO Match (Match_ID, Member1_ID, Member2_ID, Matched_AT) VALUES (SEQ_MATCH_ID.NEXTVAL, ?, ?, SYSDATE)";
				String[] cols = {"Match_ID"};
				pstmt = conn.prepareStatement(sqlNewMatch, cols);
				pstmt.setInt(1, myMemberID);
				pstmt.setInt(2, targetMemberID);
				pstmt.executeUpdate();
				
				rs = pstmt.getGeneratedKeys();
				if(rs.next()) matchID = rs.getInt(1);
			}
			pstmt.close(); // 사용 후 닫기

			// 4. 채팅방(Chatroom) 확인
			int chatroomID = 0;
			String sqlCheckRoom = "SELECT Chatroom_ID FROM Chatroom WHERE Match_ID = ?";
			pstmt = conn.prepareStatement(sqlCheckRoom);
			pstmt.setInt(1, matchID);
			rs = pstmt.executeQuery();
			
			if (rs.next()) {
				chatroomID = rs.getInt(1);
			} else {
				// 채팅방 없으면 생성
				pstmt.close();
				String sqlNewRoom = "INSERT INTO Chatroom (Chatroom_ID, Match_ID, Created_AT) VALUES (SEQ_CHAT_ID.NEXTVAL, ?, SYSDATE)";
				String[] cols = {"Chatroom_ID"};
				pstmt = conn.prepareStatement(sqlNewRoom, cols);
				pstmt.setInt(1, matchID);
				pstmt.executeUpdate();
				
				rs = pstmt.getGeneratedKeys();
				if(rs.next()) chatroomID = rs.getInt(1);
			}
			
			conn.commit(); // ★ 저장 확정

			// 5. 성공 시 이동
			response.sendRedirect("ChatRoomServlet?roomID=" + chatroomID);

		} catch (Exception e) {
			// ★ 에러 발생 시 롤백 및 에러 메시지 화면 출력 (하얀 화면 방지)
			try { if (conn != null) conn.rollback(); } catch(Exception ex) {}
			e.printStackTrace();
			
			out.println("<h3>🚫 에러가 발생했습니다 (StartChatServlet)</h3>");
			out.println("<p><b>에러 내용:</b> " + e.getMessage() + "</p>");
			out.println("<p>이클립스 콘솔(Console) 창을 확인하면 더 자세한 내용을 볼 수 있습니다.</p>");
			out.println("<button onclick='history.back()'>뒤로 가기</button>");
		} finally {
			try { if (conn != null) conn.close(); } catch (Exception e) {}
		}
	}
}