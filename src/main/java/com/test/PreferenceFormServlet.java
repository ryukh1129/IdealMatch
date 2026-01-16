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

@WebServlet("/PreferenceFormServlet")
public class PreferenceFormServlet extends HttpServlet {
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
		String ageMin="", ageMax="", gender="", region="", hobby="", mbti="";

		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPw);
			String sql = "SELECT p.* FROM Preference p JOIN Member m ON p.Member_ID = m.Member_ID JOIN Account a ON m.Account_ID = a.Account_ID WHERE a.Username = ?";
			pstmt = conn.prepareStatement(sql); pstmt.setString(1, userID); rs = pstmt.executeQuery();

			if (rs.next()) {
				int min = rs.getInt("Age_MIN"); if(min != 0) ageMin = String.valueOf(min);
				int max = rs.getInt("Age_MAX"); if(max != 0) ageMax = String.valueOf(max);
				gender = rs.getString("Preferred_GENDER"); if(gender == null) gender = "";
				region = rs.getString("Preferred_REGION"); if(region == null) region = "";
				hobby = rs.getString("Preferred_HOBBY"); if(hobby == null) hobby = "";
				mbti = rs.getString("Preferred_PERSONALITY"); if(mbti == null) mbti = "";
			}

			out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>선호도 설정</title>");
			out.println("<style>");
			out.println("@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');");
			out.println("body { font-family: 'Pretendard', sans-serif; background-color: #F2F4F6; margin: 0; padding-top: 80px; }");
			
			// 내비게이션 (공통)
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

			out.println(".pref-form { background: white; padding: 40px; border-radius: 24px; box-shadow: 0 4px 20px rgba(0,0,0,0.03); }");
			out.println(".form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }");
			out.println(".full-width { grid-column: span 2; }");
			
			out.println("label { display: block; margin-bottom: 8px; font-weight: 600; color: #4E5968; font-size: 14px; }");
			out.println("input, select { width: 100%; padding: 14px 16px; border-radius: 12px; border: 1px solid #E5E8EB; box-sizing: border-box; background-color: #F9FAFB; font-size: 15px; transition: 0.2s; color: #191F28; }");
			out.println("input:focus, select:focus { outline: none; border-color: #3182F6; background: white; }");
			
			out.println("button { margin-top: 30px; width: 100%; padding: 16px; background: #3182F6; color: white; border: none; border-radius: 16px; cursor: pointer; font-size: 16px; font-weight: 700; transition: 0.2s; }");
			out.println("button:hover { background: #1B64DA; }");
			
			out.println("</style></head><body>");
			
			out.println("<nav><div class='nav-content'><a href='MatchingServlet' class='nav-logo'>Ideal Match</a>");
			out.println("<div class='nav-menu'><a href='MatchingServlet' class='nav-item'>홈</a><a href='MyPageServlet' class='nav-item'>내 정보</a><a href='PreferenceFormServlet' class='nav-item active'>선호도 설정</a><a href='ChatListServlet' class='nav-item'>채팅</a><a href='LogoutServlet' class='nav-item' style='color:#EF4444;'>로그아웃</a></div></div></nav>");
			
			out.println("<div class='container'>");
			out.println("<h2>이상형 조건 <span>설정</span></h2>");
			
			out.println("<form class='pref-form' action='PreferenceServlet' method='post'>");
			out.println("<div class='form-grid'>");
			out.println("<div><label>최소 나이</label><input type='number' name='age_min' min='1' placeholder='상관없음' value='" + ageMin + "'></div>");
			out.println("<div><label>최대 나이</label><input type='number' name='age_max' min='1' placeholder='상관없음' value='" + ageMax + "'></div>");
			out.println("<div class='full-width'><label>선호 성별</label><select name='preferred_gender'><option value='' " + (gender.equals("") ? "selected" : "") + ">상관없음</option><option value='남성' " + (gender.equals("남성") ? "selected" : "") + ">남성</option><option value='여성' " + (gender.equals("여성") ? "selected" : "") + ">여성</option></select></div>");
			out.println("<div><label>선호 지역</label><input type='text' name='preferred_region' placeholder='예: 서울' value='" + region + "'></div>");
			out.println("<div><label>선호 MBTI</label><input type='text' name='preferred_personality' placeholder='예: ENFP' value='" + mbti + "'></div>");
			out.println("<div class='full-width'><label>선호 취미 (키워드)</label><input type='text' name='preferred_hobby' placeholder='예: 영화, 운동' value='" + hobby + "'></div>");
			out.println("</div>");
			out.println("<button type='submit'>조건 저장하기</button>");
			out.println("</form></div></body></html>");

		} catch (Exception e) { e.printStackTrace(); } finally { try { if(conn!=null) conn.close(); } catch(Exception e){} }
	}
}