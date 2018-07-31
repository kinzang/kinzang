import javax.servlet.ServletException;
import javax.servlet.http.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.*;
import net.arnx.jsonic.JSON;
import java.io.*;
import java.nio.charset.StandardCharsets;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients; 
import org.apache.http.client.methods.HttpPost;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.regex.*;


public class Main extends HttpServlet {
	
	
	public static int randomRange(int min, int max){
        return (int) (Math.random()*(max-min)) + min;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.getWriter().print("Hello from Java!\n");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        BufferedReader bufferReaderBody = new BufferedReader(request.getReader());
        String body = bufferReaderBody.readLine();
        PrintWriter out = response.getWriter();

        out.println(body);
//        response.setStatus(200);

        ReqJson reqJson = JSON.decode(body, ReqJson.class);
        //String answer="";


        HttpPost httpPost = new HttpPost(System.getenv("REPLY_URL"));
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer " + System.getenv("BARE_TOKEN"));

        if (reqJson.events[0].message.type.equals("text")){
            
                String text="";
                String texta="";
				
                int flag=0;
                String answer="";
				
                texta = reqJson.events[0].message.text.toUpperCase();

                if(texta.equalsIgnoreCase("HAPPY")){
                    answer="😠";
                }
                else if(texta.equalsIgnoreCase("SAD")){
                    answer="🌞";
                }
                else{

                    try{
                        JSONParser parser = new JSONParser();
                        Reader reader = new FileReader("json.json");
                        JSONArray jsonArray = (JSONArray) parser.parse(reader);
                        for (int i = 0; i < jsonArray.size(); i++) {
                                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                                String question = (String) jsonObject.get("question");
                                question=question.toUpperCase();
                                String[] question_arr=question.split(","); 
                                for(int j=0; j<question_arr.length;j++)
                                {
                                    if(Pattern.compile(Pattern.quote(question_arr[j]), Pattern.CASE_INSENSITIVE).matcher(texta).find())
                                        {
                                        flag=1;
                                        answer = (String) jsonObject.get("answer");
                                        break;
                                    }
                                }
                                
                            }
                            if(flag==0)
                            {
                                answer = "開発している!!";
                            }
                            
                        }
                        catch (ParseException e) {
                            e.printStackTrace();
                        }
                        text = JSON.encode(new RespJson(reqJson.events[0].replyToken,answer));
                            StringEntity params = new StringEntity(text, StandardCharsets.UTF_8);
                            httpPost.setEntity(params);
                            CloseableHttpClient client = HttpClients.createDefault();
                            CloseableHttpResponse resp = client.execute(httpPost);
                    }
                    text = JSON.encode(new RespJson(reqJson.events[0].replyToken,answer));
                            StringEntity params = new StringEntity(text, StandardCharsets.UTF_8);
                            httpPost.setEntity(params);
                            CloseableHttpClient client = HttpClients.createDefault();
                            CloseableHttpResponse resp = client.execute(httpPost);
                    
        }        
		
		if (reqJson.events[0].message.type.equals("image")){
			 String text="";
             String texta="";
			String image1="";
			int flag = 0;
			String answer="";
			URL previewImageUrl1;
			String image = reqJson.events[0].message.text.toUpperCase();
                try{
                    JSONParser parser = new JSONParser();
                    Reader reader = new FileReader("json.json");
                    JSONArray jsonArray = (JSONArray) parser.parse(reader);
                    for (int i = 0; i < jsonArray.size(); i++) {
                            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                            String question = (String) jsonObject.get("question");
                            question=question.toUpperCase();
                            String[] question_arr=question.split(","); 
                            for(int j=0; j<question_arr.length;j++)
                            {
                                if(question_arr[j].equals(image))
                                {
                                    flag=1;
                                    image1 = (String) jsonObject.get("image");
                                    break;
                                }
                            }
                            
                        }
                        if(flag==0)
                        {
                            answer = "開発している!!";
                        }
                        text = JSON.encode(new RespJson(reqJson.events[0].replyToken,answer));
                        StringEntity params = new StringEntity(text, StandardCharsets.UTF_8);
                        httpPost.setEntity(params);
                        CloseableHttpClient client = HttpClients.createDefault();
                        CloseableHttpResponse resp = client.execute(httpPost);
                    }
                    catch (ParseException e) {
                        e.printStackTrace();
                    }
			
		}
        
            // sticker
        out.close();
        response.setStatus(200);
    }

    public static void main(String[] args) throws Exception{
        Server server = new Server(Integer.valueOf(System.getenv("PORT")));
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        context.addServlet(new ServletHolder(new Main()),"/*");
        server.start();
        server.join();   
    }
  class ReqJson{
    public Evs[] events;
      class Evs{
        public String type;
        public String replyToken;
        public String timestamp;
        public Mess message;
        public Source source;
		public String image;
        public Postback postback;
        class Postback{
          public String data;
        }

        class Mess{
          public String type;
          public String id;
          public String text;
		  public String image;
		  public String packageId;
          public String stickerId;
        }
        class Source{
          public String userId;
        }
      }
  }
  class RespJson{
      class Mes{
        public String type;
        public String text;
        Mes(){
       }
      }
      public String replyToken;
      public Mes[] messages = new Mes[1];
      RespJson(String token,String text){
        this.replyToken = token;
        this.messages[0] = new Mes();
        this.messages[0].type = "text";
        this.messages[0].text = text;
      }
  }	
	class RespJson2{
        class Image{
            public String type;
            public String originalContentUrl;
            public String previewImageUrl;
            Image(){}
        }
        public String replyToken;
        public Image[] messages = new Image[1];
        RespJson2(String token,String originalContentUrl,String previewImageUrl){
            this.replyToken = token;
            this.messages[0] = new Image();
            this.messages[0].type = "image";
            this.messages[0].originalContentUrl = originalContentUrl;
            this.messages[0].previewImageUrl = previewImageUrl;
        }
    }

}