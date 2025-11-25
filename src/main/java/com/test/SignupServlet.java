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

@WebServlet("/SignupServlet")
public class SignupServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();

		// 1. 데이터 받기
		String id = request.getParameter("id"); 
		String password = request.getParameter("password");
		String email = request.getParameter("email");
		
		String name = request.getParameter("name");
		String gender = request.getParameter("gender");
		String birthDate = request.getParameter("birth_date");
		String heightStr = request.getParameter("height");
		String job = request.getParameter("job");
		String region = request.getParameter("region");
		String hobby = request.getParameter("hobby");
		String personality = request.getParameter("personality_type");

		int height = (heightStr != null && !heightStr.isEmpty()) ? Integer.parseInt(heightStr) : 0;

		// ==========================================
		// ★ DB 연결 정보 (본인 것으로 수정 필수) ★
		// ==========================================
		String dbUrl = "jdbc:oracle:thin:@localhost:9999:orcl";
		String dbUser = "DB2025_501_2"; // 아이디 확인!
		String dbPw = "qwer1234";   // 비번 확인!

		Connection conn = null;
		PreparedStatement pstmtAccount = null;
		PreparedStatement pstmtMember = null;
		ResultSet rs = null;

		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPw);
			conn.setAutoCommit(false); // 트랜잭션 시작

			// ==========================================
			// 1. Account 테이블 입력 (시퀀스 적용)
			// ==========================================
			// SEQ_ACCOUNT_ID.NEXTVAL : 번호표 뽑아서 Account_ID에 넣음
			String sqlAccount = "INSERT INTO Account (Account_ID, Username, Password, Email, Role, Created_AT) VALUES (SEQ_ACCOUNT_ID.NEXTVAL, ?, ?, ?, 'member', SYSDATE)";
			
			// 방금 뽑은 번호표(Account_ID)를 알아내야 함 (Member 테이블에 넣어줘야 하니까)
			String[] generatedColumns = {"Account_ID"};
			pstmtAccount = conn.prepareStatement(sqlAccount, generatedColumns);
			
			pstmtAccount.setString(1, id);
			pstmtAccount.setString(2, password);
			pstmtAccount.setString(3, email);
			pstmtAccount.executeUpdate();

			// 뽑힌 번호표 확인
			rs = pstmtAccount.getGeneratedKeys();
			int newAccountId = 0;
			if (rs.next()) {
				newAccountId = rs.getInt(1); // 방금 생긴 Account_ID 획득!
			}

			// ==========================================
			// 2. Member 테이블 입력 (시퀀스 적용)
			// ==========================================
			// SEQ_MEMBER_ID.NEXTVAL : Member_ID 자동 생성
			// ? (첫번째 물음표) : 아까 위에서 획득한 newAccountId를 넣음 (외래키 연결)
			String sqlMember = "INSERT INTO Member (Member_ID, Account_ID, Name, Gender, Birth_DATE, Height, Job, Region, Hobby, Personality_TYPE, Joined_AT) VALUES (SEQ_MEMBER_ID.NEXTVAL, ?, ?, ?, TO_DATE(?, 'YYYY-MM-DD'), ?, ?, ?, ?, ?, SYSDATE)";
			
			pstmtMember = conn.prepareStatement(sqlMember);
			pstmtMember.setInt(1, newAccountId); // ★ 연결고리
			pstmtMember.setString(2, name);
			pstmtMember.setString(3, gender);
			pstmtMember.setString(4, birthDate); 
			pstmtMember.setInt(5, height);
			pstmtMember.setString(6, job);
			pstmtMember.setString(7, region);
			pstmtMember.setString(8, hobby);
			pstmtMember.setString(9, personality);
			pstmtMember.executeUpdate();

			conn.commit(); // 성공 시 저장

			// 성공 화면
			out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>가입 완료</title>");
			out.println("<style>body { font-family: 'Pretendard', sans-serif; text-align: center; margin-top: 50px; } button { background: #2b4c7e; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; }</style>");
			out.println("</head><body>");
			out.println("<h2>🎉 회원가입 성공!</h2>");
			out.println("<p>" + name + "님 (" + id + ") 환영합니다!</p>");
			out.println("<button onclick=\"location.href='login.html'\">로그인 하러 가기</button>");
			out.println("</body></html>");

		} catch (Exception e) {
			try { if (conn != null) conn.rollback(); } catch (Exception rollbackEx) {}
			e.printStackTrace();
			out.println("<script>alert('오류 발생: " + e.getMessage() + "'); history.back();</script>");
		} finally {
			try { if (rs != null) rs.close(); } catch (Exception e) {}
			try { if (pstmtMember != null) pstmtMember.close(); } catch (Exception e) {}
			try { if (pstmtAccount != null) pstmtAccount.close(); } catch (Exception e) {}
			try { if (conn != null) conn.close(); } catch (Exception e) {}
		}
	}
}