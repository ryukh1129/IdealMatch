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

@WebServlet("/MatchingServlet")
public class MatchingServlet extends HttpServlet {
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
		PreparedStatement pstmtPref = null;
		PreparedStatement pstmtMatch = null;
		ResultSet rsPref = null;
		ResultSet rsMatch = null;

		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPw);
			
			String sqlPref = "SELECT p.* FROM Preference p JOIN Member m ON p.Member_ID = m.Member_ID JOIN Account a ON m.Account_ID = a.Account_ID WHERE a.Username = ?";
			pstmtPref = conn.prepareStatement(sqlPref); pstmtPref.setString(1, userID); rsPref = pstmtPref.executeQuery();
			
			int pAgeMin=0, pAgeMax=0; String pGender=null, pRegion=null, pHobby=null, pMbti=null;
			if (rsPref.next()) { pAgeMin = rsPref.getInt("Age_MIN"); pAgeMax = rsPref.getInt("Age_MAX"); pGender = rsPref.getString("Preferred_GENDER"); pRegion = rsPref.getString("Preferred_REGION"); pHobby = rsPref.getString("Preferred_HOBBY"); pMbti = rsPref.getString("Preferred_PERSONALITY"); }

			String sqlMatch = "SELECT m.*, TRUNC(MONTHS_BETWEEN(SYSDATE, m.Birth_DATE)/12) as AGE FROM Member m JOIN Account a ON m.Account_ID = a.Account_ID WHERE a.Username != ? AND (? = 0 OR TRUNC(MONTHS_BETWEEN(SYSDATE, m.Birth_DATE)/12) >= ?) AND (? = 0 OR TRUNC(MONTHS_BETWEEN(SYSDATE, m.Birth_DATE)/12) <= ?) AND (? IS NULL OR m.Gender = ?) AND (? IS NULL OR m.Region LIKE '%'||?||'%') AND (? IS NULL OR m.Hobby LIKE '%'||?||'%') AND (? IS NULL OR m.Personality_TYPE LIKE '%'||?||'%') ORDER BY m.Member_ID DESC";
			
			pstmtMatch = conn.prepareStatement(sqlMatch);
			int idx = 1; pstmtMatch.setString(idx++, userID); pstmtMatch.setInt(idx++, pAgeMin); pstmtMatch.setInt(idx++, pAgeMin); pstmtMatch.setInt(idx++, pAgeMax); pstmtMatch.setInt(idx++, pAgeMax); pstmtMatch.setString(idx++, pGender); pstmtMatch.setString(idx++, pGender); pstmtMatch.setString(idx++, pRegion); pstmtMatch.setString(idx++, pRegion); pstmtMatch.setString(idx++, pHobby); pstmtMatch.setString(idx++, pHobby); pstmtMatch.setString(idx++, pMbti); pstmtMatch.setString(idx++, pMbti);     
			rsMatch = pstmtMatch.executeQuery();

			out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>매칭 추천</title>");
			out.println("<style>");
			out.println("@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');");
			out.println("body { font-family: 'Pretendard', sans-serif; background-color: #F2F4F6; margin: 0; padding-top: 80px; }");

			// 상단 내비게이션 바
			out.println("nav { position: fixed; top: 0; width: 100%; height: 70px; background: rgba(255,255,255,0.8); backdrop-filter: blur(20px); border-bottom: 1px solid rgba(0,0,0,0.05); display: flex; align-items: center; justify-content: center; z-index: 1000; }");
			out.println(".nav-content { width: 1000px; display: flex; justify-content: space-between; align-items: center; padding: 0 20px; }");
			out.println(".nav-logo { font-size: 20px; font-weight: 800; color: #3182F6; text-decoration: none; }");
			out.println(".nav-menu { display: flex; gap: 30px; }");
			out.println(".nav-item { text-decoration: none; color: #4E5968; font-weight: 600; font-size: 16px; transition: 0.2s; }");
			out.println(".nav-item:hover { color: #3182F6; }");
			out.println(".nav-item.active { color: #3182F6; }");
			
			out.println(".container { max-width: 1000px; margin: 0 auto; padding: 40px 20px; }");
			out.println("h2 { font-size: 28px; font-weight: 800; color: #191F28; margin-bottom: 30px; }");
			out.println("h2 span { color: #3182F6; }");

			out.println(".grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 24px; }");

			out.println(".card { background: white; border-radius: 24px; padding: 28px; box-shadow: 0 4px 20px rgba(0,0,0,0.03); transition: all 0.3s ease; display: flex; flex-direction: column; justify-content: space-between; border: 1px solid transparent; }");
			out.println(".card:hover { transform: translateY(-8px); box-shadow: 0 15px 30px rgba(0,0,0,0.08); }");
			
			out.println(".profile-info h3 { margin: 0 0 5px 0; font-size: 22px; color: #191F28; display: flex; align-items: center; gap: 8px; }");
			out.println(".profile-info p { margin: 0 0 20px 0; color: #8B95A1; font-size: 15px; }");
			
			// ★ 성별 뱃지 스타일 추가
			out.println(".gender-badge { font-size: 12px; padding: 4px 8px; border-radius: 6px; font-weight: 700; }");
			out.println(".gender-male { background: #E8F3FF; color: #3182F6; }"); // 파란색
			out.println(".gender-female { background: #FCE7F3; color: #EC4899; }"); // 분홍색

			out.println(".tags { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 25px; }");
			out.println(".tag { background: #F2F4F6; color: #4E5968; padding: 6px 12px; border-radius: 12px; font-size: 13px; font-weight: 600; }");
			
			out.println(".chat-btn { width: 100%; padding: 14px; background: #3182F6; color: white; border: none; border-radius: 16px; font-size: 16px; font-weight: 700; cursor: pointer; transition: 0.2s; }");
			out.println(".chat-btn:hover { background: #1B64DA; }");
			out.println(".empty { text-align: center; color: #8B95A1; padding: 100px 0; font-size: 18px; }");

			out.println("</style></head><body>");
			
			out.println("<nav><div class='nav-content'>");
			out.println("<a href='MatchingServlet' class='nav-logo'>Ideal Match</a>");
			out.println("<div class='nav-menu'>");
			out.println("<a href='MatchingServlet' class='nav-item active'>홈</a>");
			out.println("<a href='MyPageServlet' class='nav-item'>내 정보</a>");
			out.println("<a href='PreferenceFormServlet' class='nav-item'>선호도 설정</a>");
			out.println("<a href='ChatListServlet' class='nav-item'>채팅</a>");
			out.println("<a href='LogoutServlet' class='nav-item' style='color:#EF4444;'>로그아웃</a>");
			out.println("</div></div></nav>");

			out.println("<div class='container'>");
			out.println("<h2>오늘의 <span>추천 매칭</span></h2>");
			
			out.println("<div class='grid'>");
			int count = 0;
			while (rsMatch.next()) {
				count++;
				int targetID = rsMatch.getInt("Member_ID");
				String name = rsMatch.getString("Name");
				int age = rsMatch.getInt("AGE");
				String gender = rsMatch.getString("Gender");
				int height = rsMatch.getInt("Height");
				String job = rsMatch.getString("Job");
				String region = rsMatch.getString("Region");
				String hobby = rsMatch.getString("Hobby"); if(hobby == null) hobby = "-";
				String mbti = rsMatch.getString("Personality_TYPE");
				
				// ★ 성별 뱃지 HTML 생성
				String genderBadge = "";
				if("남성".equals(gender)) {
					genderBadge = "<span class='gender-badge gender-male'>남성</span>";
				} else if("여성".equals(gender)) {
					genderBadge = "<span class='gender-badge gender-female'>여성</span>";
				}
				
				out.println("<div class='card'>");
				out.println("<div class='profile-info'>");
				// 이름 옆에 뱃지 추가
				out.println("<h3>" + name + " (" + age + ") " + genderBadge + "</h3>");
				out.println("<p>" + job + " · " + height + "cm</p>");
				out.println("</div>");
				
				out.println("<div class='tags'>");
				out.println("<span class='tag'>" + mbti + "</span>");
				out.println("<span class='tag'>" + region + "</span>");
				out.println("<span class='tag'>" + hobby + "</span>");
				out.println("</div>");
				
				out.println("<button class='chat-btn' onclick=\"location.href='StartChatServlet?targetID=" + targetID + "'\">대화하기</button>");
				out.println("</div>");
			}
			out.println("</div>"); // grid end
			
			if(count == 0) out.println("<div class='empty'>조건에 맞는 회원이 없습니다.<br>선호도 설정을 변경해보세요.</div>");
			out.println("</div></body></html>");

		} catch (Exception e) { e.printStackTrace(); } finally { try { if(conn!=null) conn.close(); } catch(Exception e){} }
	}
}