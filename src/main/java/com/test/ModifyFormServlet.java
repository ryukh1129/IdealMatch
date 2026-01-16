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

@WebServlet("/ModifyFormServlet")
public class ModifyFormServlet extends HttpServlet {
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

		Connection conn = null; PreparedStatement pstmt = null; ResultSet rs = null;

		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPw);
			String sql = "SELECT m.* FROM Member m JOIN Account a ON m.Account_ID = a.Account_ID WHERE a.Username = ?";
			pstmt = conn.prepareStatement(sql); pstmt.setString(1, userID); rs = pstmt.executeQuery();

			if (rs.next()) {
				String name = rs.getString("Name"); if(name==null) name="";
				int height = rs.getInt("Height");
				String job = rs.getString("Job"); if(job==null) job="";
				String region = rs.getString("Region"); if(region==null) region="";
				String hobby = rs.getString("Hobby"); if(hobby==null) hobby="";
				String mbti = rs.getString("Personality_TYPE"); if(mbti==null) mbti="";

				out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>정보 수정</title>");
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

				out.println(".container { max-width: 600px; margin: 0 auto; padding: 40px 20px; }");
				out.println("h2 { font-size: 28px; font-weight: 800; color: #191F28; margin-bottom: 30px; text-align: center; }");
				out.println("h2 span { color: #3182F6; }");

				out.println(".edit-form { background: white; padding: 40px; border-radius: 24px; box-shadow: 0 4px 20px rgba(0,0,0,0.03); }");
				out.println("label { display: block; margin-top: 20px; font-weight: 600; color: #4E5968; margin-bottom: 8px; font-size: 14px; }");
				out.println("input { width: 100%; padding: 14px; border-radius: 12px; border: 1px solid #E5E8EB; box-sizing: border-box; background-color: #F9FAFB; font-size: 15px; transition: 0.2s; color: #191F28; }");
				out.println("input:disabled { background-color: #E5E8EB; color: #9CA3AF; }");
				out.println("input:focus { outline: none; border-color: #3182F6; background: white; }");
				
				out.println("button { margin-top: 30px; width: 100%; padding: 16px; background: #3182F6; color: white; border: none; border-radius: 16px; cursor: pointer; font-size: 16px; font-weight: 700; transition: 0.2s; }");
				out.println("button:hover { background: #1B64DA; }");
				
				out.println("</style></head><body>");
				
				out.println("<nav><div class='nav-content'><a href='MatchingServlet' class='nav-logo'>Ideal Match</a>");
				out.println("<div class='nav-menu'><a href='MatchingServlet' class='nav-item'>홈</a><a href='MyPageServlet' class='nav-item active'>내 정보</a><a href='PreferenceFormServlet' class='nav-item'>선호도 설정</a><a href='ChatListServlet' class='nav-item'>채팅</a><a href='LogoutServlet' class='nav-item' style='color:#EF4444;'>로그아웃</a></div></div></nav>");
				
				out.println("<div class='container'>");
				out.println("<h2>내 정보 <span>수정</span></h2>");
				out.println("<form class='edit-form' action='UpdateServlet' method='post'>");
				out.println("<label>아이디 (수정불가)</label><input type='text' value='" + userID + "' disabled>");
				out.println("<label>이름</label><input type='text' name='name' value='" + name + "'>");
				out.println("<label>키 (cm)</label><input type='number' name='height' value='" + height + "'>");
				out.println("<label>직업</label><input type='text' name='job' value='" + job + "'>");
				out.println("<label>지역</label><input type='text' name='region' value='" + region + "'>");
				out.println("<label>취미</label><input type='text' name='hobby' value='" + hobby + "'>");
				out.println("<label>MBTI</label><input type='text' name='mbti' value='" + mbti + "'>");
				out.println("<button type='submit'>수정 완료</button>");
				out.println("</form></div></body></html>");
			}
		} catch (Exception e) { e.printStackTrace(); } finally { try { if(conn!=null) conn.close(); } catch(Exception e){} }
	}
}