package servlet;

import java.io.*;
import java.time.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;

@WebServlet("/threadinfo")
public class ThreadInfo extends HttpServlet {
   public static int DELAY = 1000;

   @Override protected void doGet(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
      response.setContentType("text/plain");
      Instant start = Instant.now();
      try {
         Thread.sleep((int) (DELAY * 2 * Math.random()));
      } catch (InterruptedException ex) {}
      Instant end = Instant.now();
      PrintWriter out = response.getWriter();
      out.printf("Sent: %s Started: %s Completed: %s %s%n",
         request.getParameter("sent"),
         start,
         end,
         Thread.currentThread().toString());
   }
}
