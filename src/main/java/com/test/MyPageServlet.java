package com.test;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/MyPageServlet")
public class MyPageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();
		
		HttpSession session = request.getSession();
		String userID = (String) session.getAttribute("userID");
		if (userID == null) { response.sendRedirect("login.html"); return; }

		String dbUrl = "jdbc:oracle:thin:@localhost:9999:orcl";
		String dbUser = "DB2025_501_2"; 
		String dbPw = "qwer1234";

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPw);
			String sql = "SELECT m.*, a.Username FROM Member m JOIN Account a ON m.Account_ID = a.Account_ID WHERE a.Username = ?";
			pstmt = conn.prepareStatement(sql); pstmt.setString(1, userID); rs = pstmt.executeQuery();
			
			if (rs.next()) {
				String name = rs.getString("Name"); if(name==null) name="";
				String gender = rs.getString("Gender"); if(gender==null) gender="-";
				Date sqlDate = rs.getDate("Birth_DATE"); String birth = (sqlDate != null) ? sqlDate.toString().split(" ")[0] : "미입력";
				int height = rs.getInt("Height");
				String job = rs.getString("Job"); if(job==null) job="-";
				String region = rs.getString("Region"); if(region==null) region="-";
				String hobby = rs.getString("Hobby"); if(hobby==null) hobby="-";
				String mbti = rs.getString("Personality_TYPE"); if(mbti==null) mbti="-";

				out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>내 정보</title>");
				out.println("<style>");
				out.println("@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');");
				out.println("body { font-family: 'Pretendard', sans-serif; background-color: #F2F4F6; margin: 0; padding-top: 80px; }");
				
				// 내비게이션 바 (공통)
				out.println("nav { position: fixed; top: 0; width: 100%; height: 70px; background: rgba(255,255,255,0.8); backdrop-filter: blur(20px); border-bottom: 1px solid rgba(0,0,0,0.05); display: flex; align-items: center; justify-content: center; z-index: 1000; }");
				out.println(".nav-content { width: 1000px; display: flex; justify-content: space-between; align-items: center; padding: 0 20px; }");
				out.println(".nav-logo { font-size: 20px; font-weight: 800; color: #3182F6; text-decoration: none; }");
				out.println(".nav-menu { display: flex; gap: 30px; }");
				out.println(".nav-item { text-decoration: none; color: #4E5968; font-weight: 600; font-size: 16px; transition: 0.2s; }");
				out.println(".nav-item:hover { color: #3182F6; }");
				out.println(".nav-item.active { color: #3182F6; }");

				out.println(".container { max-width: 600px; margin: 0 auto; padding: 40px 20px; }");
				out.println("h2 { font-size: 28px; font-weight: 800; color: #191F28; margin-bottom: 30px; text-align: center; }");
				out.println("h2 span { color: #3182F6; }");

				out.println(".info-card { background: white; padding: 40px; border-radius: 24px; box-shadow: 0 4px 20px rgba(0,0,0,0.03); }");
				out.println(".info-item { display: flex; justify-content: space-between; padding: 16px 0; border-bottom: 1px solid #F2F4F6; }");
				out.println(".info-item:last-child { border-bottom: none; }");
				out.println(".info-label { color: #8B95A1; font-weight: 600; }");
				out.println(".info-value { color: #191F28; font-weight: 500; }");
				
				out.println(".btn-group { margin-top: 30px; display: flex; gap: 15px; }");
				out.println("button { flex: 1; padding: 14px; border-radius: 12px; font-weight: 600; font-size: 15px; cursor: pointer; border: none; transition: 0.2s; }");
				out.println(".edit-btn { background: #3182F6; color: white; }");
				out.println(".edit-btn:hover { background: #1B64DA; }");
				out.println(".delete-btn { background: #FEF2F2; color: #EF4444; }");
				out.println(".delete-btn:hover { background: #FEE2E2; }");
				out.println("</style></head><body>");
				
				out.println("<nav><div class='nav-content'><a href='MatchingServlet' class='nav-logo'>Ideal Match</a>");
				out.println("<div class='nav-menu'><a href='MatchingServlet' class='nav-item'>홈</a><a href='MyPageServlet' class='nav-item active'>내 정보</a><a href='PreferenceFormServlet' class='nav-item'>선호도 설정</a><a href='ChatListServlet' class='nav-item'>채팅</a><a href='LogoutServlet' class='nav-item' style='color:#EF4444;'>로그아웃</a></div></div></nav>");
				
				out.println("<div class='container'>");
				out.println("<h2>내 프로필 <span>관리</span></h2>");
				out.println("<div class='info-card'>");
				out.println("<div class='info-item'><span class='info-label'>아이디</span><span class='info-value'>" + userID + "</span></div>");
				out.println("<div class='info-item'><span class='info-label'>이름</span><span class='info-value'>" + name + "</span></div>");
				out.println("<div class='info-item'><span class='info-label'>성별</span><span class='info-value'>" + gender + "</span></div>");
				out.println("<div class='info-item'><span class='info-label'>생년월일</span><span class='info-value'>" + birth + "</span></div>");
				out.println("<div class='info-item'><span class='info-label'>키</span><span class='info-value'>" + height + "cm</span></div>");
				out.println("<div class='info-item'><span class='info-label'>직업</span><span class='info-value'>" + job + "</span></div>");
				out.println("<div class='info-item'><span class='info-label'>지역</span><span class='info-value'>" + region + "</span></div>");
				out.println("<div class='info-item'><span class='info-label'>취미</span><span class='info-value'>" + hobby + "</span></div>");
				out.println("<div class='info-item'><span class='info-label'>MBTI</span><span class='info-value'>" + mbti + "</span></div>");
				out.println("<div class='btn-group'><button class='edit-btn' onclick=\"location.href='ModifyFormServlet'\">정보 수정</button><button class='delete-btn' onclick=\"if(confirm('정말 탈퇴하시겠습니까?')) location.href='DeleteServlet';\">회원 탈퇴</button></div>");
				out.println("</div></div></body></html>");
			}
		} catch (Exception e) { e.printStackTrace(); } finally { try { if(conn!=null) conn.close(); } catch(Exception e){} }
	}
}