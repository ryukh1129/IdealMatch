package com.test;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/LogoutServlet")
public class LogoutServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// 1. 현재 사용자의 세션(로그인 정보)을 가져옴
		// false 옵션: 세션이 없으면 새로 만들지 말고 그냥 null을 달라
		HttpSession session = request.getSession(false);
		
		if (session != null) {
			// 2. 세션 파기 (로그아웃의 핵심!)
			// 서버에 저장된 'userID' 등 모든 정보를 지웁니다.
			session.invalidate();
			System.out.println("[LogoutServlet] 로그아웃 완료");
		}
		
		// 3. 메인 홈페이지로 이동
		response.sendRedirect("MainServlet");
	}
}