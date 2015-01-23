package com.myrefers.recoengine.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.*;

@SuppressWarnings("deprecation")
@Path("/crud")
public class Crud {
	public String serverUrlES = "http://localhost:9200/";
	@POST
    @Path("/adduser")
    @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addUser(InputStream incomingData) throws ClientProtocolException, IOException {
		StringBuilder profileBuilder = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(incomingData));
            String line = null;
            while ((line = in.readLine()) != null) {
            	profileBuilder.append(line);
            }
        } catch (Exception e) {
            System.out.println("Error Parsing: - ");
        }
        System.out.println("Data Received: " + profileBuilder.toString());
        JSONObject userProfileJson = new JSONObject(profileBuilder.toString());
        String UserId = userProfileJson.getString("JSid");
        HttpClient client = new DefaultHttpClient();
		HttpPost getUserFromES = new HttpPost(serverUrlES + "myrefers/doc/_search");
		String mltQuery = "{ \"query\" : { \"filtered\" : { \"query\" : { \"match_all\" : {} }, \"filter\" : { \"term\" : { \"JSid\" : \"" + UserId + "\" } } } }}";
		//System.out.println("mlt query: " + mltQuery);
		StringEntity getUserQuery = new StringEntity(mltQuery);
		getUserQuery.setContentType("application/json");
		getUserFromES.setEntity(getUserQuery);
		HttpResponse response = client.execute(getUserFromES);
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		String line = rd.readLine();
		JSONObject userProfile = new JSONObject(line);
		int totalFound = 0;
		JSONObject statusBean = new JSONObject();
		if(userProfile.getJSONObject("hits").has("total")){
			totalFound = userProfile.getJSONObject("hits").getInt("total");
		}
		else{
			statusBean.put("status", false);
			statusBean.put("created", false);
			statusBean.put("errcode", 1);
			statusBean.put("reason", "Couldn't check for existing user");
			return Response.status(200).entity(statusBean.toString()).build();
		}
		statusBean.put("JSid", UserId);
		if(totalFound == 0){
			HttpPost post = new HttpPost(serverUrlES + "myrefers/doc/");
			StringEntity input = new StringEntity(userProfileJson.toString());
			input.setContentType("application/json");
			post.setEntity(input);
			response = client.execute(post);
			rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			line = rd.readLine();
			JSONObject responseES = new JSONObject(line);
			if(responseES.has("created")){
				statusBean.put("status", responseES.getBoolean("created"));
				statusBean.put("created", responseES.getBoolean("created"));
				statusBean.put("errcode", 0);
			}
			else{
				statusBean.put("status", false);
				statusBean.put("created", false);
				statusBean.put("errcode", 2);
				statusBean.put("reason", "Couldn't add new user");
			}
		}
		else{
			statusBean.put("status", true);
			statusBean.put("created", false);
			statusBean.put("errcode", 0);
			statusBean.put("reason", "User already exists");
		}
        //jsonResult.put("server", "success");
        // return HTTP response 200 in case of success
        return Response.status(200).entity(statusBean.toString()).build();
        //return jsonResult;
	}
	
	@GET
    @Path("/deleteuser/{uid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteUser(@PathParam("uid") String UserId) throws ClientProtocolException, IOException {
        HttpClient client = new DefaultHttpClient();
		HttpPost getUserFromES = new HttpPost(serverUrlES + "myrefers/doc/_search");
		String mltQuery = "{ \"query\" : { \"filtered\" : { \"query\" : { \"match_all\" : {} }, \"filter\" : { \"term\" : { \"JSid\" : \"" + UserId + "\" } } } }}";
		//System.out.println("mlt query: " + mltQuery);
		StringEntity getUserQuery = new StringEntity(mltQuery);
		getUserQuery.setContentType("application/json");
		getUserFromES.setEntity(getUserQuery);
		HttpResponse response = client.execute(getUserFromES);
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		String line = rd.readLine();
		JSONObject userProfile = new JSONObject(line);
		int totalFound = 0;
		JSONObject statusBean = new JSONObject();
		if(userProfile.getJSONObject("hits").has("total")){
			totalFound = userProfile.getJSONObject("hits").getInt("total");
		}
		else{
			statusBean.put("status", false);
			statusBean.put("created", false);
			statusBean.put("errcode", 1);
			statusBean.put("reason", "Couldn't check for existing user");
			return Response.status(200).entity(statusBean.toString()).build();
		}
		statusBean.put("JSid", UserId);
		if(totalFound > 0){
			String ESid = userProfile.getJSONObject("hits").getJSONArray("hits").getJSONObject(0).getString("_id");
			HttpDelete deleteUser = new HttpDelete(serverUrlES + "myrefers/doc/"+ ESid);
			response = client.execute(deleteUser);
			rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			line = rd.readLine();
			JSONObject responseES = new JSONObject(line);
			if(responseES.has("found")){
				statusBean.put("status", responseES.getBoolean("found"));
				statusBean.put("deleted", responseES.getBoolean("found"));
				statusBean.put("errcode", 0);
			}
			else{
				statusBean.put("status", true);
				statusBean.put("deleted", false);
				statusBean.put("errcode", 2);
				statusBean.put("reason", "Couldn't delete user");
			}
		}
		else{
			statusBean.put("status", true);
			statusBean.put("deleted", false);
			statusBean.put("errcode", 0);
			statusBean.put("reason", "User doesn't exist");
		}
        // return HTTP response 200 in case of success
        return Response.status(200).entity(statusBean.toString()).build();
        //return jsonResult;
	}
	
	@POST
    @Path("/addjob")
    @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addJob(InputStream incomingData) throws ClientProtocolException, IOException {
		StringBuilder profileBuilder = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(incomingData));
            String line = null;
            while ((line = in.readLine()) != null) {
            	profileBuilder.append(line);
            }
        } catch (Exception e) {
            System.out.println("Error Parsing: - ");
        }
        System.out.println("Data Received: " + profileBuilder.toString());
        JSONObject jobDetailJson = new JSONObject(profileBuilder.toString());
        String JobId = jobDetailJson.getString("JobId");
        HttpClient client = new DefaultHttpClient();
		HttpPost getUserFromES = new HttpPost(serverUrlES + "myrefers/doc/_search");
		String mltQuery = "{ \"query\" : { \"filtered\" : { \"query\" : { \"match_all\" : {} }, \"filter\" : { \"term\" : { \"JobId\" : \"" + JobId + "\" } } } }}";
		//System.out.println("mlt query: " + mltQuery);
		StringEntity getUserQuery = new StringEntity(mltQuery);
		getUserQuery.setContentType("application/json");
		getUserFromES.setEntity(getUserQuery);
		HttpResponse response = client.execute(getUserFromES);
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		String line = rd.readLine();
		JSONObject jobDetail = new JSONObject(line);
		int totalFound = 0;
		JSONObject statusBean = new JSONObject();
		if(jobDetail.getJSONObject("hits").has("total")){
			totalFound = jobDetail.getJSONObject("hits").getInt("total");
		}
		else{
			statusBean.put("status", false);
			statusBean.put("created", false);
			statusBean.put("errcode", 1);
			statusBean.put("reason", "Couldn't check for existing job");
			return Response.status(200).entity(statusBean.toString()).build();
		}
		statusBean.put("JobId", JobId);
		if(totalFound == 0){
			HttpPost post = new HttpPost(serverUrlES + "myrefers/doc/");
			StringEntity input = new StringEntity(jobDetailJson.toString());
			input.setContentType("application/json");
			post.setEntity(input);
			response = client.execute(post);
			rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			line = rd.readLine();
			JSONObject responseES = new JSONObject(line);
			if(responseES.has("created")){
				statusBean.put("status", responseES.getBoolean("created"));
				statusBean.put("created", responseES.getBoolean("created"));
				statusBean.put("errcode", 0);
			}
			else{
				statusBean.put("status", false);
				statusBean.put("created", false);
				statusBean.put("errcode", 2);
				statusBean.put("reason", "Couldn't add new job");
			}
		}
		else{
			statusBean.put("status", true);
			statusBean.put("created", false);
			statusBean.put("errcode", 0);
			statusBean.put("reason", "Job already exists");
		}
        //jsonResult.put("server", "success");
        // return HTTP response 200 in case of success
        return Response.status(200).entity(statusBean.toString()).build();
        //return jsonResult;
	}
	
	@GET
    @Path("/deletejob/{uid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteJob(@PathParam("uid") String JobId) throws ClientProtocolException, IOException {
        HttpClient client = new DefaultHttpClient();
		HttpPost getJobFromES = new HttpPost(serverUrlES + "myrefers/doc/_search");
		String mltQuery = "{ \"query\" : { \"filtered\" : { \"query\" : { \"match_all\" : {} }, \"filter\" : { \"term\" : { \"JobId\" : \"" + JobId + "\" } } } }}";
		//System.out.println("mlt query: " + mltQuery);
		StringEntity getUserQuery = new StringEntity(mltQuery);
		getUserQuery.setContentType("application/json");
		getJobFromES.setEntity(getUserQuery);
		HttpResponse response = client.execute(getJobFromES);
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		String line = rd.readLine();
		JSONObject jobDetail = new JSONObject(line);
		int totalFound = 0;
		JSONObject statusBean = new JSONObject();
		if(jobDetail.getJSONObject("hits").has("total")){
			totalFound = jobDetail.getJSONObject("hits").getInt("total");
		}
		else{
			statusBean.put("status", false);
			statusBean.put("created", false);
			statusBean.put("errcode", 1);
			statusBean.put("reason", "Couldn't check for existing job");
			return Response.status(200).entity(statusBean.toString()).build();
		}
		statusBean.put("JobId", JobId);
		if(totalFound > 0){
			String ESid = jobDetail.getJSONObject("hits").getJSONArray("hits").getJSONObject(0).getString("_id");
			HttpDelete deleteJob = new HttpDelete(serverUrlES + "myrefers/doc/"+ ESid);
			response = client.execute(deleteJob);
			rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			line = rd.readLine();
			JSONObject responseES = new JSONObject(line);
			if(responseES.has("found")){
				statusBean.put("status", responseES.getBoolean("found"));
				statusBean.put("deleted", responseES.getBoolean("found"));
				statusBean.put("errcode", 0);
			}
			else{
				statusBean.put("status", true);
				statusBean.put("deleted", false);
				statusBean.put("errcode", 2);
				statusBean.put("reason", "Couldn't delete job");
			}
		}
		else{
			statusBean.put("status", true);
			statusBean.put("deleted", false);
			statusBean.put("errcode", 0);
			statusBean.put("reason", "Job doesn't exist");
		}
        // return HTTP response 200 in case of success
        return Response.status(200).entity(statusBean.toString()).build();
        //return jsonResult;
	}
}
