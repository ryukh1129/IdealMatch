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

@WebServlet("/MainServlet")
public class MainServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();

		String dbUrl = "jdbc:oracle:thin:@localhost:9999:orcl";
		String dbUser = "DB2025_501_2"; 
		String dbPw = "qwer1234";
		
		int totalCount=0, maleCount=0, femaleCount=0;
		Connection conn = null; PreparedStatement pstmt = null; ResultSet rs = null;

		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPw);
			
			String sql = "SELECT COUNT(*) FROM Member"; pstmt=conn.prepareStatement(sql); rs=pstmt.executeQuery(); if(rs.next()) totalCount=rs.getInt(1); rs.close(); pstmt.close();
			String sqlMale = "SELECT COUNT(*) FROM Member WHERE Gender='남성'"; pstmt=conn.prepareStatement(sqlMale); rs=pstmt.executeQuery(); if(rs.next()) maleCount=rs.getInt(1); rs.close(); pstmt.close();
			String sqlFemale = "SELECT COUNT(*) FROM Member WHERE Gender='여성'"; pstmt=conn.prepareStatement(sqlFemale); rs=pstmt.executeQuery(); if(rs.next()) femaleCount=rs.getInt(1);

			out.println("<!DOCTYPE html><html lang='ko'><head><meta charset='UTF-8'><title>Ideal Match</title>");
			out.println("<style>");
			out.println("@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');");
			
			out.println("body { font-family: 'Pretendard', sans-serif; background-color: #F9FAFB; margin: 0; height: 100vh; display: flex; justify-content: center; align-items: center; overflow: hidden; }");
			
			out.println(".container { width: 1000px; height: 600px; display: flex; background: white; border-radius: 40px; box-shadow: 0 30px 80px rgba(0,0,0,0.08); overflow: hidden; }");
			
			out.println(".left-panel { flex: 1.2; background: #3182F6; padding: 60px; display: flex; flex-direction: column; justify-content: space-between; color: white; position: relative; }");
			out.println(".logo { font-size: 24px; font-weight: 800; }");
			out.println(".hero-text h1 { font-size: 48px; font-weight: 800; line-height: 1.2; margin-bottom: 20px; }");
			out.println(".hero-text p { font-size: 18px; opacity: 0.8; font-weight: 500; }");
			
			// ★ 수정됨: 통계 배지 스타일 (3개 나열)
			out.println(".stats { display: flex; gap: 10px; flex-wrap: wrap; }");
			out.println(".badge { background: rgba(255,255,255,0.2); padding: 10px 16px; border-radius: 16px; font-size: 14px; backdrop-filter: blur(10px); display: flex; align-items: center; gap: 6px; }");
			out.println(".badge span { font-weight: 800; font-size: 16px; color: white; }");

			out.println(".right-panel { flex: 1; padding: 60px; display: flex; flex-direction: column; justify-content: center; }");
			out.println("h2 { font-size: 32px; font-weight: 700; color: #191F28; margin-bottom: 40px; }");
			
			out.println("input { width: 100%; padding: 18px 20px; margin-bottom: 12px; border: 1px solid #E5E8EB; border-radius: 16px; font-size: 16px; background: #F9FAFB; box-sizing: border-box; transition: 0.2s; }");
			out.println("input:focus { outline: none; border-color: #3182F6; background: white; }");
			
			out.println(".btn-login { width: 100%; padding: 18px; background: #3182F6; color: white; border: none; border-radius: 16px; font-size: 17px; font-weight: 700; cursor: pointer; margin-top: 10px; transition: 0.2s; }");
			out.println(".btn-login:hover { background: #1B64DA; }");
			
			out.println(".btn-signup { width: 100%; padding: 18px; background: white; color: #333D4B; border: 1px solid #E5E8EB; border-radius: 16px; font-size: 17px; font-weight: 700; cursor: pointer; margin-top: 10px; transition: 0.2s; }");
			out.println(".btn-signup:hover { background: #F9FAFB; }");
			
			out.println("</style></head><body>");

			out.println("<div class='container'>");
			
			out.println("<div class='left-panel'>");
			out.println("<div class='logo'>IM</div>");
			out.println("<div class='hero-text'><h1>새로운 인연,<br>여기서 시작하세요</h1><p>데이터 기반 알고리즘이<br>당신의 완벽한 이상형을 찾아드립니다.</p></div>");
			
			// ★ 통계 출력 부분 (3개)
			out.println("<div class='stats'>");
			out.println("<div class='badge'>👥 총 회원 <span>" + totalCount + "</span></div>");
			out.println("<div class='badge'>🙋‍♂️ 남성 <span>" + maleCount + "</span></div>");
			out.println("<div class='badge'>🙋‍♀️ 여성 <span>" + femaleCount + "</span></div>");
			out.println("</div>");
			
			out.println("</div>");

			out.println("<div class='right-panel'>");
			out.println("<h2>로그인</h2>");
			out.println("<form action='LoginServlet' method='post'>");
			out.println("<input type='text' name='id' placeholder='아이디' required>");
			out.println("<input type='password' name='password' placeholder='비밀번호' required>");
			out.println("<button type='submit' class='btn-login'>시작하기</button>");
			out.println("<button type='button' class='btn-signup' onclick=\"location.href='signup.html'\">회원가입</button>");
			out.println("</form>");
			out.println("</div>");

			out.println("</div></body></html>");

		} catch (Exception e) { e.printStackTrace(); } finally { try { if(conn!=null) conn.close(); } catch(Exception e){} }
	}
}