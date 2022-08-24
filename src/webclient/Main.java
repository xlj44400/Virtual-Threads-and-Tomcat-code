package webclient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {
   private static HttpClient client = HttpClient.newBuilder()
         .followRedirects(HttpClient.Redirect.ALWAYS).build();
   
   public static <T> T getBody(String url, HttpResponse.BodyHandler<T> responseBodyHandler) 
         throws IOException, URISyntaxException, InterruptedException {
      HttpRequest request = HttpRequest.newBuilder()
         .uri(new URI(url))
         .timeout(Duration.ofSeconds(60))
         .GET()
         .build();
      HttpResponse<T> response = client.send(request, responseBodyHandler);
      int status = response.statusCode();
      if (status == 200)
         return response.body();
      else
         throw new IOException("Response status " + status + " " + url);
   }

   public static void main(String[] args) {
      int nthreads = args.length > 0 ? Integer.parseInt(args[0]) : 200;
      int delay = args.length > 1 ? Integer.parseInt(args[1]) : 1;
      String url = args.length > 2 ? args[2] :
         "http://localhost:8080/ThreadDemo/threadinfo";
      var q = new LinkedBlockingQueue<String>();
      long start = System.nanoTime();
      try (ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
         for (int i = 0; i < nthreads; i++) {
            exec.submit(() -> {
               try {
                  String response = getBody(url + "?sent=" + Instant.now(), HttpResponse.BodyHandlers.ofString());
                  q.put(response);
                  Thread.sleep(delay);
               }
               catch (Exception ex) {
                  ex.printStackTrace();
               }
            });
         }
      }
      long end = System.nanoTime();
      for (String s : q)
         System.out.println(s.strip());
      System.out.printf("%6.3f seconds%n", (end - start) / 1.0E9);
   }
}
