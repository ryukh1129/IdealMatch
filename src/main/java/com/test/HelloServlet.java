package com.test;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// @WebServlet("/주소") : HTML의 form action 값과 일치해야 합니다.
@WebServlet("/HelloServlet")
public class HelloServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    // HTML에서 method="post"로 보냈으므로 doPost를 사용합니다.
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// 1. 한글 깨짐 방지
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		
		// 2. HTML에서 보낸 데이터 받기 (input 태그의 name 속성 사용)
		String name = request.getParameter("userName");
		
		// 3. 결과 출력 (Java가 HTML을 만들어 브라우저로 보냄)
		PrintWriter out = response.getWriter();
		out.println("<html><body>");
		out.println("<h1>" + name + "님 반갑습니다!</h1>");
		out.println("<p>Java와 연결에 성공했습니다.</p>");
		out.println("</body></html>");
	}
}