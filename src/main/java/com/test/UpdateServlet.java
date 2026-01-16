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

@WebServlet("/UpdateServlet")
public class UpdateServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		
		HttpSession session = request.getSession();
		String userID = (String) session.getAttribute("userID");
		if (userID == null) { response.sendRedirect("login.html"); return; }

		// 1. 수정할 데이터 받기
		String name = request.getParameter("name");
		String heightStr = request.getParameter("height");
		String job = request.getParameter("job");
		String region = request.getParameter("region");
		String hobby = request.getParameter("hobby");
		String mbti = request.getParameter("mbti");
		
		int height = (heightStr != null && !heightStr.isEmpty()) ? Integer.parseInt(heightStr) : 0;

		// 2. DB 연결
		String dbUrl = "jdbc:oracle:thin:@localhost:9999:orcl";
		String dbUser = "DB2025_501_2"; 
		String dbPw = "qwer1234"; // ★ 수정 필수

		Connection conn = null;
		PreparedStatement pstmt = null;

		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPw);
			
			// 3. 업데이트 쿼리 실행
			// Member 테이블을 업데이트하되, 현재 로그인한 아이디(Account 테이블)와 연결된 사람만 수정
			String sql = "UPDATE Member SET Name=?, Height=?, Job=?, Region=?, Hobby=?, Personality_TYPE=? "
					   + "WHERE Account_ID = (SELECT Account_ID FROM Account WHERE Username = ?)";
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, name);
			pstmt.setInt(2, height);
			pstmt.setString(3, job);
			pstmt.setString(4, region);
			pstmt.setString(5, hobby);
			pstmt.setString(6, mbti);
			pstmt.setString(7, userID); // 마지막 물음표 (WHERE Username = ?)
			
			int result = pstmt.executeUpdate();
			
			if (result > 0) {
				// 성공하면 다시 마이페이지로 이동해서 바뀐 모습 보여주기
				response.sendRedirect("MyPageServlet");
			} else {
				throw new Exception("정보 수정에 실패했습니다.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			PrintWriter out = response.getWriter();
			out.println("<script>alert('에러 발생: " + e.getMessage() + "'); history.back();</script>");
		} finally {
			try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
			try { if (conn != null) conn.close(); } catch (Exception e) {}
		}
	}
}