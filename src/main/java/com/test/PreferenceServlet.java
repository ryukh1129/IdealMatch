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

@WebServlet("/PreferenceServlet")
public class PreferenceServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		
		// 1. 로그인 여부 확인 (세션 체크)
		HttpSession session = request.getSession();
		String userID = (String) session.getAttribute("userID");
		
		if (userID == null) {
			// 로그인을 안 했으면 접근 불가
			response.sendRedirect("login.html");
			return;
		}

		// 2. 화면 데이터 받기
		String ageMin = request.getParameter("age_min");
		String ageMax = request.getParameter("age_max");
		String gender = request.getParameter("preferred_gender");
		String region = request.getParameter("preferred_region");
		String hobby = request.getParameter("preferred_hobby");
		String personality = request.getParameter("preferred_personality");

		// 3. DB 연결 정보 (PuTTY 터널링)
		String dbUrl = "jdbc:oracle:thin:@localhost:9999:orcl";
		String dbUser = "DB2025_501_2"; // 본인 ID
		String dbPw = "qwer1234";       // 본인 PW

		Connection conn = null;
		PreparedStatement pstmtFindID = null;
		PreparedStatement pstmtInsert = null;
		ResultSet rs = null;

		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
            conn = DriverManager.getConnection(dbUrl, dbUser, dbPw);
            
            // 1. 로그인한 아이디(userID)로 Member_ID 찾기 (이건 똑같음)
            String sqlFindID = "SELECT m.Member_ID FROM Member m JOIN Account a ON m.Account_ID = a.Account_ID WHERE a.Username = ?";
            pstmtFindID = conn.prepareStatement(sqlFindID);
            pstmtFindID.setString(1, userID);
            rs = pstmtFindID.executeQuery();
            
            int memberID = 0;
            if (rs.next()) {
                memberID = rs.getInt("Member_ID");
            } else {
                throw new Exception("회원 정보를 찾을 수 없습니다.");
            }

            // =======================================================
            // [업그레이드] MERGE 문 사용 (수정 + 등록 동시 처리)
            // =======================================================
            // 설명: Preference 테이블에서 내 Member_ID가 있는지 확인해서
            // 있으면 -> UPDATE (값 변경)
            // 없으면 -> INSERT (새로 등록)
            String sqlMerge = "MERGE INTO Preference p "
                    + "USING dual ON (p.Member_ID = ?) "
                    + "WHEN MATCHED THEN "
                    + "  UPDATE SET Age_MIN=?, Age_MAX=?, Preferred_GENDER=?, Preferred_REGION=?, Preferred_HOBBY=?, Preferred_PERSONALITY=? "
                    + "WHEN NOT MATCHED THEN "
                    + "  INSERT (Pref_ID, Member_ID, Age_MIN, Age_MAX, Preferred_GENDER, Preferred_REGION, Preferred_HOBBY, Preferred_PERSONALITY) "
                    + "  VALUES (SEQ_PREF_ID.NEXTVAL, ?, ?, ?, ?, ?, ?, ?)";
            
            pstmtInsert = conn.prepareStatement(sqlMerge);
            
            // 파라미터 순서가 중요합니다! (SQL 물음표 순서대로)
            
            // 1. ON (p.Member_ID = ?) -> 검사할 ID
            pstmtInsert.setInt(1, memberID);
            
            // 2. UPDATE SET ... (6개) -> 수정할 값들
            pstmtInsert.setInt(2, Integer.parseInt(ageMin));
            pstmtInsert.setInt(3, Integer.parseInt(ageMax));
            pstmtInsert.setString(4, gender);
            pstmtInsert.setString(5, region);
            pstmtInsert.setString(6, hobby);
            pstmtInsert.setString(7, personality);
            
            // 3. INSERT VALUES ... (Member_ID 포함 7개) -> 새로 만들 값들
            // (참고: 첫번째 물음표는 Member_ID입니다)
            pstmtInsert.setInt(8, memberID); 
            pstmtInsert.setInt(9, Integer.parseInt(ageMin));
            pstmtInsert.setInt(10, Integer.parseInt(ageMax));
            pstmtInsert.setString(11, gender);
            pstmtInsert.setString(12, region);
            pstmtInsert.setString(13, hobby);
            pstmtInsert.setString(14, personality);
            
            pstmtInsert.executeUpdate();
            
            // 저장 후 다시 매칭 화면으로 이동
            response.sendRedirect("matching.html");

		} catch (Exception e) {
			e.printStackTrace();
			PrintWriter out = response.getWriter();
			out.println("<script>alert('저장 실패: " + e.getMessage() + "'); history.back();</script>");
		} finally {
			try { if (rs != null) rs.close(); } catch (Exception e) {}
			try { if (pstmtFindID != null) pstmtFindID.close(); } catch (Exception e) {}
			try { if (pstmtInsert != null) pstmtInsert.close(); } catch (Exception e) {}
			try { if (conn != null) conn.close(); } catch (Exception e) {}
		}
	}
}