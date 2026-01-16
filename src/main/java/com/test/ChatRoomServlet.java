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

@WebServlet("/ChatRoomServlet")
public class ChatRoomServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html; charset=UTF-8");
		request.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		
		HttpSession session = request.getSession();
		String myUserID = (String) session.getAttribute("userID");
		String roomIDStr = request.getParameter("roomID");
		
		if(myUserID == null) { response.sendRedirect("login.html"); return; }
		if(roomIDStr == null) { 
			out.println("<script>alert('잘못된 접근입니다.'); history.back();</script>");
			return; 
		}
		
		String dbUrl = "jdbc:oracle:thin:@localhost:9999:orcl";
		String dbUser = "DB2025_501_2"; 
		String dbPw = "qwer1234";

		Connection conn = null; PreparedStatement pstmt = null; ResultSet rs = null;

		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPw);
			
			String sqlTarget = "SELECT m.Name FROM Member m WHERE m.Member_ID = (SELECT CASE WHEN mt.Member1_ID = (SELECT Member_ID FROM Member WHERE Account_ID=(SELECT Account_ID FROM Account WHERE Username=?)) THEN mt.Member2_ID ELSE mt.Member1_ID END FROM Match mt JOIN Chatroom c ON mt.Match_ID = c.Match_ID WHERE c.Chatroom_ID = ?)";
			pstmt = conn.prepareStatement(sqlTarget);
			pstmt.setString(1, myUserID);
			pstmt.setString(2, roomIDStr);
			rs = pstmt.executeQuery();
			
			String targetName = "상대방";
			if(rs.next()) targetName = rs.getString(1);
			rs.close(); pstmt.close();

			out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>채팅</title>");
			out.println("<style>");
			out.println("@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');");
			out.println(":root { --primary: #3182F6; --bg: #F2F4F6; }");
			
			// ★ 배경은 다른 페이지와 통일 (#F2F4F6), 배치는 중앙 정렬 (Flex)
			out.println("body { font-family: 'Pretendard', sans-serif; background-color: var(--bg); margin: 0; height: 100vh; display: flex; justify-content: center; align-items: center; }");
			
			// ★ 요청하신 '가운데 창 형식' 유지 (700px 너비의 플로팅 박스)
			out.println(".app-wrapper { width: 700px; height: 85vh; background: white; border-radius: 30px; box-shadow: 0 20px 60px rgba(0,0,0,0.05); display: flex; flex-direction: column; overflow: hidden; border: 1px solid rgba(0,0,0,0.05); }");

			out.println("header { background: white; padding: 20px 25px; font-weight: 700; display: flex; justify-content: space-between; align-items: center; font-size: 18px; border-bottom: 1px solid #F2F4F6; color: #191F28; }");
			
			out.println(".chat-container { flex: 1; padding: 20px; overflow-y: auto; display: flex; flex-direction: column; gap: 12px; scroll-behavior: smooth; background-color: #FAFAFA; }");
			out.println(".chat-container::-webkit-scrollbar { width: 6px; }");
			out.println(".chat-container::-webkit-scrollbar-thumb { background-color: #E5E8EB; border-radius: 3px; }");

			out.println(".message { padding: 12px 18px; border-radius: 20px; font-size: 15px; position: relative; word-wrap: break-word; box-shadow: 0 2px 4px rgba(0,0,0,0.03); max-width: 70%; width: fit-content; line-height: 1.5; }");
			out.println(".my-msg { align-self: flex-end; background: var(--primary); color: white; border-bottom-right-radius: 4px; }");
			out.println(".other-msg { align-self: flex-start; background: white; color: #333D4B; border: 1px solid #E5E8EB; border-bottom-left-radius: 4px; }");
			
			out.println(".sender-name { font-size: 12px; color: #8B95A1; margin-bottom: 4px; margin-left: 6px; }");
			
			out.println(".input-area { background: white; padding: 20px; display: flex; gap: 12px; border-top: 1px solid #F2F4F6; }");
			out.println("input { flex: 1; padding: 14px 20px; border: 1px solid #E5E8EB; border-radius: 24px; outline: none; background-color: #F9FAFB; font-size: 15px; transition: 0.2s; }");
			out.println("input:focus { border-color: var(--primary); background: white; }");
			out.println("button { background: #333D4B; color: white; border: none; padding: 0 24px; border-radius: 24px; cursor: pointer; font-weight: 600; font-size: 15px; transition: 0.2s; }");
			out.println("button:hover { background: var(--primary); }");
			
			out.println("</style>");
			
			// ★★★ [중복 방지 로직 적용] ★★★
			out.println("<script>");
			out.println("var roomID = '" + roomIDStr + "';");
			out.println("var myUserID = '" + myUserID + "';");
			out.println("var lastMsgID = 0;");
			
			// ★ 1. 업데이트 중인지 확인하는 플래그 변수
			out.println("var isUpdating = false;");

			out.println("function updateChat() {");
			// ★ 2. 이미 업데이트 중이면 실행하지 않고 튕겨냄 (중복 실행 방지)
			out.println("    if(isUpdating) return;");
			out.println("    isUpdating = true;"); // 작업 시작 표시

			out.println("    fetch('GetMessagesServlet?roomID=' + roomID + '&lastMsgID=' + lastMsgID)");
			out.println("    .then(res => res.json())");
			out.println("    .then(data => {");
			out.println("        if(data.length > 0) {");
			out.println("            var chatBox = document.querySelector('.chat-container');");
			out.println("            var isAtBottom = (chatBox.scrollHeight - chatBox.scrollTop <= chatBox.clientHeight + 100);");
			
			out.println("            data.forEach(msg => {");
			out.println("                lastMsgID = msg.id;");
			out.println("                var html = '';");
			out.println("                if(msg.senderID === myUserID) {");
			out.println("                    html = '<div class=\"message my-msg\">' + msg.text + '</div>';");
			out.println("                } else {");
			out.println("                    html = '<div><div class=\"sender-name\">' + msg.senderName + '</div><div class=\"message other-msg\">' + msg.text + '</div></div>';");
			out.println("                }");
			out.println("                chatBox.insertAdjacentHTML('beforeend', html);");
			out.println("            });");
			
			out.println("            if(isAtBottom) { chatBox.scrollTop = chatBox.scrollHeight; }");
			out.println("        }");
			out.println("    })");
			// ★ 3. 작업이 끝나면(성공하든 실패하든) 플래그를 해제
			out.println("    .finally(() => { isUpdating = false; });");
			out.println("}");
			
			// 1초마다 자동 실행
			out.println("setInterval(updateChat, 1000);");
			
			out.println("function sendMessage() {");
			out.println("    var input = document.getElementById('msgInput');");
			out.println("    var msg = input.value;");
			out.println("    if(msg.trim() === '') return;");
			
			out.println("    fetch('SendMessageServlet', {");
			out.println("        method: 'POST',");
			out.println("        headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'},");
			out.println("        body: 'roomID=' + roomID + '&message=' + encodeURIComponent(msg)");
			out.println("    }).then(() => {");
			out.println("        input.value = '';");
			out.println("        updateChat(); // 즉시 업데이트 요청");
			out.println("    });");
			out.println("}");
			
			out.println("window.onload = function() {");
			out.println("    updateChat();");
			out.println("};");
			out.println("</script>");
			
			out.println("</head><body>");
			
			out.println("<div class='app-wrapper'>");
			
			out.println("<header>");
			out.println("<span>" + targetName + "님과의 대화</span>");
			out.println("<button onclick=\"location.href='ChatListServlet'\" style='background:rgba(0,0,0,0.05); color:#1F2937; padding:8px 16px; border-radius:12px; font-size:13px;'>목록으로</button>");
			out.println("</header>");
			
			out.println("<div class='chat-container'></div>");
			
			out.println("<form class='input-area' onsubmit='sendMessage(); return false;'>");
			out.println("<input type='text' id='msgInput' placeholder='메시지를 입력하세요...' required autocomplete='off'>");
			out.println("<button type='submit'>전송</button>");
			out.println("</form>");
			
			out.println("</div>");
			
			out.println("</body></html>");

		} catch (Exception e) {
			e.printStackTrace();
			out.println("<h3>🚫 에러</h3><p>" + e.getMessage() + "</p>");
		} finally {
			try { if (conn != null) conn.close(); } catch (Exception e) {}
		}
	}
}