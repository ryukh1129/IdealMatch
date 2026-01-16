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

@WebServlet("/DeleteServlet")
public class DeleteServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html; charset=UTF-8");
		request.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		
		HttpSession session = request.getSession();
		String userID = (String) session.getAttribute("userID");
		
		if (userID == null) {
			response.sendRedirect("login.html");
			return;
		}

		String dbUrl = "jdbc:oracle:thin:@localhost:9999:orcl";
		String dbUser = "DB2025_501_2"; 
		String dbPw = "qwer1234";

		Connection conn = null;
		PreparedStatement pstmt = null;

		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		    conn = DriverManager.getConnection(dbUrl, dbUser, dbPw);
		    
		    // ★ 이제 이거 한 줄이면 끝납니다!
		    // DB가 알아서 Member -> Chat -> Message 까지 연쇄 폭발시킴
		    String sql = "DELETE FROM Account WHERE Username = ?";
		    
		    pstmt = conn.prepareStatement(sql);
		    pstmt.setString(1, userID);
		    
		    int result = pstmt.executeUpdate();
		    
		    if(result > 0) {
		        session.invalidate();
		        out.println("<script>alert('탈퇴 완료!'); location.href='MainServlet';</script>");
			} else {
				throw new Exception("삭제할 계정 정보를 찾을 수 없습니다.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			// ★ 에러 발생 시 화면에 출력 (이제 아무것도 안 뜨는 현상은 없어짐)
			out.println("<h3>🚫 탈퇴 실패</h3>");
			out.println("<p><b>에러 원인:</b> " + e.getMessage() + "</p>");
			out.println("<p>DB 제약조건(Foreign Key) 문제일 가능성이 큽니다.</p>");
			out.println("<button onclick='history.back()'>뒤로 가기</button>");
		} finally {
			try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
			try { if (conn != null) conn.close(); } catch (Exception e) {}
		}
	}
}