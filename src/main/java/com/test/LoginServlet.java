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

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// 1. 한글 설정 & 데이터 받기
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		
		String id = request.getParameter("id");
		String password = request.getParameter("password");
		
		// 2. DB 연결 정보 (PuTTY 터널링 주소 필수!)
		String dbUrl = "jdbc:oracle:thin:@localhost:9999:orcl";
		String dbUser = "DB2025_501_2"; // 본인 ID
		String dbPw = "qwer1234";       // 본인 PW
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPw);
			
			// 3. 쿼리 실행 (아이디와 비번이 둘 다 맞는지 검사)
			String sql = "SELECT * FROM Account WHERE Username = ? AND Password = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, id);
			pstmt.setString(2, password);
			
			rs = pstmt.executeQuery();
			
			// 4. 결과 확인
			if (rs.next()) {
				// ⭕ 로그인 성공 (일치하는 데이터가 있음)
				
				// [세션 생성] 서버에 "이 사람은 로그인한 사람이야"라고 메모해두는 것
				HttpSession session = request.getSession();
				session.setAttribute("userID", id); // 세션에 아이디 저장
				
				// 메인 화면(index.html)으로 이동
				response.sendRedirect("matching.html");
				
			} else {
				// ❌ 로그인 실패 (일치하는 데이터가 없음)
				PrintWriter out = response.getWriter();
				out.println("<script>");
				out.println("alert('ID 및 패스워드를 확인하세요');");
				out.println("history.back();"); // 다시 로그인 화면으로 돌아가기
				out.println("</script>");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			// 에러 발생 시 화면에 출력
			PrintWriter out = response.getWriter();
			out.println("에러 발생: " + e.getMessage());
		} finally {
			// 자원 정리
			try { if (rs != null) rs.close(); } catch (Exception e) {}
			try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
			try { if (conn != null) conn.close(); } catch (Exception e) {}
		}
	}
}