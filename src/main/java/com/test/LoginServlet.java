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
				// ⭕ 로그인 성공!
				
				// ==========================================
				// ★ [추가된 기능] 마지막 로그인 시간 기록하기 (UPDATE)
				// ==========================================
				// 1. 기존 pstmt는 이미 썼으니 닫아주고 새로 만듭니다.
				pstmt.close(); 
				
				// 2. 현재 시간(SYSDATE)으로 Last_LOGIN 컬럼을 업데이트
				String sqlUpdate = "UPDATE Account SET Last_LOGIN = SYSDATE WHERE Username = ?";
				PreparedStatement pstmtUpdate = conn.prepareStatement(sqlUpdate);
				pstmtUpdate.setString(1, id);
				pstmtUpdate.executeUpdate(); // 실행!
				
				pstmtUpdate.close(); // 자원 정리
				// ==========================================
				
				// [세션 생성]
				HttpSession session = request.getSession();
				session.setAttribute("userID", id); 
				
				// 메인 화면으로 이동
				response.sendRedirect("MatchingServlet"); // 혹은 MainServlet
				
			} else {
				// ❌ 로그인 실패
				PrintWriter out = response.getWriter();
				out.println("<script>");
				out.println("alert('ID 및 패스워드를 확인하세요');");
				out.println("history.back();"); 
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