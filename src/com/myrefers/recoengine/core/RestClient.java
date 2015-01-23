package com.myrefers.recoengine.core;

	import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
	 


import org.json.JSONObject;
	 
	/**
	 * @author Roshan
	 * 
	 */
	 
	public class RestClient {
	    public static void main(String[] args) {
	        //String string = "{\"data\":\"test data\"}";
	        try {
	            JSONObject jsonObject = new JSONObject();
	            jsonObject.put("JobId", "417");
	            jsonObject.put("PrimarySkills", "Advertising Online");
	            jsonObject.put("SecondarySkills", "12345");
	            jsonObject.put("EmployerName", "haL Tech");
	            jsonObject.put("IndustryName", "m a s");
	            jsonObject.put("Specialization", "");
	            jsonObject.put("Education", "xyz");
	            jsonObject.put("Location", "");
	            jsonObject.put("MinExperience", 6);
	            System.out.println(jsonObject);
	            
	            // Step2: Now pass JSON File Data to REST Service
	            try {
	                //URL url = new URL("http://localhost:8080/reco-cluster/reco-engine/engine/recommendjobs/user/6879");
	                URL url = new URL("http://localhost:8080/reco-cluster/reco-engine/crud/addjob");
	                URLConnection connection = url.openConnection();
	                connection.setDoOutput(true);
	                connection.setRequestProperty("Content-Type", "application/json");
	                connection.setConnectTimeout(5000);
	                connection.setReadTimeout(5000);
	                OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
	                out.write(jsonObject.toString());
	                out.close();
	                StringBuilder crunchifyBuilder = new StringBuilder();
	                BufferedReader in = new BufferedReader(new InputStreamReader(
	                        connection.getInputStream()));
	                try{
		                String line = null;
			            while ((line = in.readLine()) != null) {
			                crunchifyBuilder.append(line);
			            }
			        } catch (Exception e) {
			            System.out.println("Error Parsing: - ");
			        }
			        System.out.println("Response received at client: " + crunchifyBuilder.toString());
	                //while (in.readLine() != null) {
	                //}
	                JSONObject jsonResult = new JSONObject(crunchifyBuilder.toString());
	                //jsonResult.put("status", "success");
	                System.out.println("\nREST Service Invoked Successfully..");
	                in.close();
	                System.out.println("object: " + jsonResult.toString());
	            } catch (Exception e) {
	                System.out.println("\nError while calling REST Service");
	                System.out.println(e);
	            }
	 
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	}
