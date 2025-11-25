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

@WebServlet("/MyPageServlet")
public class MyPageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// 링크를 타고 들어오는 건 GET 방식입니다.
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		
		// 1. 로그인 체크 (세션)
		HttpSession session = request.getSession();
		String userID = (String) session.getAttribute("userID");
		
		if (userID == null) {
			response.sendRedirect("login.html");
			return;
		}

		// 2. DB 연결 정보 (터널링)
		String dbUrl = "jdbc:oracle:thin:@localhost:9999:orcl";
		String dbUser = "DB2025_501_2"; // 본인 ID
		String dbPw = "비밀번호";       // 본인 PW

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		PrintWriter out = response.getWriter();

		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPw);
			
			// 3. 내 정보 조회 쿼리 (Account 테이블과 Member 테이블 조인)
			// 아이디(Username)를 기준으로 내 상세 정보(Member.*)를 다 가져옵니다.
			String sql = "SELECT m.*, a.Username FROM Member m JOIN Account a ON m.Account_ID = a.Account_ID WHERE a.Username = ?";
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userID);
			
			rs = pstmt.executeQuery();
			
			if (rs.next()) {
				// DB에서 값 꺼내기 (NULL일 경우를 대비해 빈 문자열 처리)
				String name = rs.getString("Name");
				String gender = rs.getString("Gender");
				java.sql.Date dbDate = rs.getDate("Birth_DATE");
				String birth = (dbDate != null) ? dbDate.toString().split(" ")[0] : "정보 없음";
				int height = rs.getInt("Height");
				String job = rs.getString("Job");
				String region = rs.getString("Region");
				String hobby = rs.getString("Hobby");
				String mbti = rs.getString("Personality_TYPE");

				// 4. HTML 화면 출력 (기존 mypage.html 디자인 적용)
				out.println("<!DOCTYPE html>");
				out.println("<html lang='ko'>");
				out.println("<head>");
				out.println("<meta charset='UTF-8'>");
				out.println("<title>마이페이지</title>");
				out.println("<style>");
				out.println("body { font-family: 'Pretendard', sans-serif; background-color: #f7f9fc; margin: 0; }");
				out.println(".container { max-width: 600px; margin: 50px auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1);}");
				out.println("h2 { text-align: center; color: #2b4c7e; }");
				out.println(".info { margin: 20px 0; }");
				out.println(".info p { margin: 15px 0; border-bottom: 1px solid #eee; padding-bottom: 10px; }");
				out.println("strong { display: inline-block; width: 100px; color: #555; }");
				out.println(".btn-group { text-align: center; margin-top: 30px; }");
				out.println("button { padding: 10px 20px; background-color: #2b4c7e; color: white; border: none; border-radius: 5px; cursor: pointer; margin: 0 5px; }");
				out.println("button:hover { background-color: #1e345b; }");
				out.println(".btn-secondary { background-color: #6c757d; }");
				out.println(".btn-secondary:hover { background-color: #5a6268; }");
				out.println("</style>");
				out.println("</head>");
				out.println("<body>");
				
				out.println("<div class='container'>");
				out.println("<h2>마이페이지</h2>");
				
				out.println("<div class='info'>");
				out.println("<p><strong>아이디:</strong> " + userID + "</p>");
				out.println("<p><strong>이름:</strong> " + name + "</p>");
				out.println("<p><strong>성별:</strong> " + gender + "</p>");
				out.println("<p><strong>생년월일:</strong> " + birth + "</p>");
				out.println("<p><strong>키:</strong> " + height + "cm</p>");
				out.println("<p><strong>직업:</strong> " + job + "</p>");
				out.println("<p><strong>지역:</strong> " + region + "</p>");
				out.println("<p><strong>취미:</strong> " + hobby + "</p>");
				out.println("<p><strong>MBTI:</strong> " + mbti + "</p>");
				out.println("</div>");
				
				out.println("<div class='btn-group'>");
				// '정보 수정'은 나중에 구현할 수 있습니다.
				out.println("<button onclick=\"alert('준비 중인 기능입니다.')\">정보 수정</button>");
				// '뒤로 가기' 버튼 (매칭 화면으로)
				out.println("<button class='btn-secondary' onclick=\"location.href='matching.html'\">뒤로 가기</button>");
				out.println("</div>");
				
				out.println("</div>");
				out.println("</body>");
				out.println("</html>");
				
			} else {
				// DB에 정보가 없는 경우 (이런 일은 없어야 정상이지만 예외 처리)
				out.println("<script>alert('회원 정보를 불러올 수 없습니다.'); history.back();</script>");
			}

		} catch (Exception e) {
			e.printStackTrace();
			out.println("<script>alert('DB 오류: " + e.getMessage() + "'); history.back();</script>");
		} finally {
			try { if (rs != null) rs.close(); } catch (Exception e) {}
			try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
			try { if (conn != null) conn.close(); } catch (Exception e) {}
		}
	}
}