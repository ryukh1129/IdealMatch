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

		// 2. 화면 데이터 받기 (공백 처리 로직 추가)
        String ageMinStr = request.getParameter("age_min");
        String ageMaxStr = request.getParameter("age_max");
        String gender = request.getParameter("preferred_gender");
        String region = request.getParameter("preferred_region");
        String hobby = request.getParameter("preferred_hobby");
        String personality = request.getParameter("preferred_personality");

        // 숫자가 비어있으면 0으로 저장 (0은 '제한 없음'으로 취급할 예정)
        int ageMin = (ageMinStr != null && !ageMinStr.isEmpty()) ? Integer.parseInt(ageMinStr) : 0;
        int ageMax = (ageMaxStr != null && !ageMaxStr.isEmpty()) ? Integer.parseInt(ageMaxStr) : 0;
        
        // 문자열이 비어있으면 NULL로 저장
        if(gender != null && gender.isEmpty()) gender = null;
        if(region != null && region.isEmpty()) region = null;
        if(hobby != null && hobby.isEmpty()) hobby = null;
        if(personality != null && personality.isEmpty()) personality = null;

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
            
         // 2. UPDATE SET ... 
            pstmtInsert.setInt(2, ageMin);
            pstmtInsert.setInt(3, ageMax);
            pstmtInsert.setString(4, gender);
            pstmtInsert.setString(5, region);
            pstmtInsert.setString(6, hobby);
            pstmtInsert.setString(7, personality);
            
            // 3. INSERT VALUES ...
            pstmtInsert.setInt(8, memberID); 
            pstmtInsert.setInt(9, ageMin);
            pstmtInsert.setInt(10, ageMax);
            pstmtInsert.setString(11, gender);
            pstmtInsert.setString(12, region);
            pstmtInsert.setString(13, hobby);
            pstmtInsert.setString(14, personality);
            
            pstmtInsert.executeUpdate();
            
            // ★ 중요: 저장 후 'MatchingServlet'으로 이동해야 필터링된 화면이 뜸
            response.sendRedirect("MatchingServlet");

		} catch (Exception e) {
			e.printStackTrace();
			PrintWriter out = response.getWriter();
			out.println("<script>alert('저장 실패: " + e.getMessage() + "빈칸을 채워주세요");
		} finally {
			try { if (rs != null) rs.close(); } catch (Exception e) {}
			try { if (pstmtFindID != null) pstmtFindID.close(); } catch (Exception e) {}
			try { if (pstmtInsert != null) pstmtInsert.close(); } catch (Exception e) {}
			try { if (conn != null) conn.close(); } catch (Exception e) {}
		}
	}
}