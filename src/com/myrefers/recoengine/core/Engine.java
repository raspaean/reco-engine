package com.myrefers.recoengine.core;

	import java.io.BufferedReader;
import java.io.FileReader;
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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.*;

	@SuppressWarnings("deprecation")
	@Path("/engine")
	public class Engine {
		@POST
	    @Path("/recommendjobs")
	    @Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.APPLICATION_JSON)
		public Response getRecommendedJobsForThisUser(InputStream incomingData) throws ClientProtocolException, IOException {
		//public JSONObject getRecommendedJobsForThisUser(UserBean userData) throws ClientProtocolException, IOException {
			//return jsonResult;
			StringBuilder crunchifyBuilder = new StringBuilder();
	        try {
	            BufferedReader in = new BufferedReader(new InputStreamReader(incomingData));
	            String line = null;
	            while ((line = in.readLine()) != null) {
	                crunchifyBuilder.append(line);
	            }
	        } catch (Exception e) {
	            System.out.println("Error Parsing: - ");
	        }
	        System.out.println("Data Received: " + crunchifyBuilder.toString());
	        JSONObject userProfileJson = new JSONObject(crunchifyBuilder.toString());
	        HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://rossingh-w7-2:9200/myrefers/doc/_search");
			String mltQuery = "{\"query\":{\"bool\" : {\"should\" : [{\"more_like_this\" : {\"fields\" : [\"IndustryName\"],\"like_text\" : \"" + userProfileJson.get("IndustryName") + "\",\"min_term_freq\" : 1,\"min_doc_freq\" : 2,\"max_query_terms\" : 12,\"boost\" : 2.0}},{\"more_like_this\" : {\"fields\" : [\"Specialization\"],\"like_text\" : \"" + userProfileJson.get("Specialization") + "\",\"min_term_freq\" : 1,\"min_doc_freq\" : 2,\"max_query_terms\" : 12,\"boost\" : 1.0}},{\"more_like_this\" : {\"fields\" : [\"PrimarySkills\"],\"like_text\" : \"" + userProfileJson.get("PrimarySkills") + "\",\"min_term_freq\" : 1,\"min_doc_freq\" : 2,\"max_query_terms\" : 12,\"boost\" : 3.0}},{\"more_like_this\" : {\"fields\" : [\"Education\"],\"like_text\" : \"" + userProfileJson.get("Education") + "\",\"min_term_freq\" : 1,\"min_doc_freq\" : 2,\"max_query_terms\" : 12,\"boost\" : 2.0}}],\"minimum_number_should_match\" : 1}}}";
			System.out.println("mlt query: " + mltQuery);
			StringEntity input = new StringEntity(mltQuery);
			input.setContentType("application/json");
			post.setEntity(input);
			HttpResponse response = client.execute(post);
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = rd.readLine();
			JSONObject jsonResult = new JSONObject(line);
	        //jsonResult.put("server", "success");
	        // return HTTP response 200 in case of success
	        return Response.status(200).entity(jsonResult.toString()).build();
	        //return jsonResult;
		}
		
		@GET
	    @Path("/recommendjobs/user/{uid}")
		@Produces(MediaType.APPLICATION_JSON)
		public Response getRecommendedJobsForThisUser(@PathParam("uid") String UserId) throws ClientProtocolException, IOException {
	        HttpClient client = new DefaultHttpClient();
			HttpPost getUserFromES = new HttpPost("http://rossingh-w7-2:9200/myrefers/doc/_search");
			String mltQuery = "{ \"query\" : { \"filtered\" : { \"query\" : { \"match_all\" : {} }, \"filter\" : { \"term\" : { \"JSid\" : \"" + UserId + "\" } } } }}";
			System.out.println("mlt query: " + mltQuery);
			StringEntity getUserQuery = new StringEntity(mltQuery);
			getUserQuery.setContentType("application/json");
			getUserFromES.setEntity(getUserQuery);
			HttpResponse response = client.execute(getUserFromES);
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = rd.readLine();
			JSONObject userProfile = new JSONObject(line);
			int totalFound = userProfile.getJSONObject("hits").getInt("total");
			JSONObject jobList = new JSONObject();
			jobList.put("JSid", UserId);
			if(totalFound > 0){
				JSONObject userProfileJson = userProfile.getJSONObject("hits").getJSONArray("hits").getJSONObject(0).getJSONObject("_source");
				HttpPost getJobs = new HttpPost("http://rossingh-w7-2:9200/myrefers/doc/_search");
				mltQuery = "{ \"query\" : { \"filtered\" : {\"query\":{\"bool\" : {\"should\" : [{\"more_like_this\" : {\"fields\" : [\"IndustryName\"],\"like_text\" : \"" + userProfileJson.get("IndustryName") + "\",\"min_term_freq\" : 1,\"min_doc_freq\" : 2,\"max_query_terms\" : 12,\"boost\" : 2.0}},{\"more_like_this\" : {\"fields\" : [\"Specialization\"],\"like_text\" : \"" + userProfileJson.get("Specialization") + "\",\"min_term_freq\" : 1,\"min_doc_freq\" : 2,\"max_query_terms\" : 12,\"boost\" : 1.0}},{\"more_like_this\" : {\"fields\" : [\"PrimarySkills\"],\"like_text\" : \"" + userProfileJson.get("PrimarySkills") + "\",\"min_term_freq\" : 1,\"min_doc_freq\" : 2,\"max_query_terms\" : 12,\"boost\" : 3.0}},{\"more_like_this\" : {\"fields\" : [\"Education\"],\"like_text\" : \"" + userProfileJson.get("Education") + "\",\"min_term_freq\" : 1,\"min_doc_freq\" : 2,\"max_query_terms\" : 12,\"boost\" : 2.0}}],\"minimum_number_should_match\" : 1}}, \"filter\" : { \"bool\": { \"must\":[ {\"term\" : { \"Type\" : \"job\" }}, {\"term\" : { \"IsActive\": \"y\" }}, {\"range\":{ \"MinExperience\":{ \"lte\": " + userProfileJson.get("MinExperience") + "} }} ] } }}}}";
				//System.out.println("mlt query: " + mltQuery);
				StringEntity getJobsQuery = new StringEntity(mltQuery);
				getJobsQuery.setContentType("application/json");
				getJobs.setEntity(getJobsQuery);
				response = client.execute(getJobs);
				rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				line = rd.readLine();
				JSONObject finalResult = new JSONObject(line);
				jobList.put("count", finalResult.getJSONObject("hits").getInt("total"));
				jobList.put("list", finalResult.getJSONObject("hits").getJSONArray("hits"));
			}
			else{
				JSONArray list = new JSONArray();
				jobList.put("count", 0);
				jobList.put("list", list);
			}
	        // return HTTP response 200 in case of success
	        return Response.status(200).entity(jobList.toString()).build();
	        //return jsonResult;
		}
		
		@GET
	    @Path("/similarusers/user/{uid}")
		@Produces(MediaType.APPLICATION_JSON)
		public Response getSimilarUsersForThisUser(@PathParam("uid") String UserId) throws ClientProtocolException, IOException {
	        HttpClient client = new DefaultHttpClient();
			HttpPost getUserFromES = new HttpPost("http://rossingh-w7-2:9200/myrefers/doc/_search");
			String mltQuery = "{ \"query\" : { \"filtered\" : { \"query\" : { \"match_all\" : {} }, \"filter\" : { \"term\" : { \"JSid\" : \"" + UserId + "\" } } } }}";
			System.out.println("mlt query: " + mltQuery);
			StringEntity getUserQuery = new StringEntity(mltQuery);
			getUserQuery.setContentType("application/json");
			getUserFromES.setEntity(getUserQuery);
			HttpResponse response = client.execute(getUserFromES);
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = rd.readLine();
			JSONObject userProfile = new JSONObject(line);
			int totalFound = userProfile.getJSONObject("hits").getInt("total");
			JSONObject jobList = new JSONObject();
			jobList.put("JSid", UserId);
			if(totalFound > 0){
				JSONObject userProfileJson = userProfile.getJSONObject("hits").getJSONArray("hits").getJSONObject(0).getJSONObject("_source");
				HttpPost getJobs = new HttpPost("http://rossingh-w7-2:9200/myrefers/doc/_search");
				mltQuery = "{ \"query\" : { \"filtered\" : {\"query\":{\"bool\" : {\"should\" : [{\"more_like_this\" : {\"fields\" : [\"IndustryName\"],\"like_text\" : \"" + userProfileJson.get("IndustryName") + "\",\"min_term_freq\" : 1,\"min_doc_freq\" : 2,\"max_query_terms\" : 12,\"boost\" : 2.0}},{\"more_like_this\" : {\"fields\" : [\"Specialization\"],\"like_text\" : \"" + userProfileJson.get("Specialization") + "\",\"min_term_freq\" : 1,\"min_doc_freq\" : 2,\"max_query_terms\" : 12,\"boost\" : 1.0}},{\"more_like_this\" : {\"fields\" : [\"PrimarySkills\"],\"like_text\" : \"" + userProfileJson.get("PrimarySkills") + "\",\"min_term_freq\" : 1,\"min_doc_freq\" : 2,\"max_query_terms\" : 12,\"boost\" : 3.0}},{\"more_like_this\" : {\"fields\" : [\"Education\"],\"like_text\" : \"" + userProfileJson.get("Education") + "\",\"min_term_freq\" : 1,\"min_doc_freq\" : 2,\"max_query_terms\" : 12,\"boost\" : 2.0}}],\"minimum_number_should_match\" : 1}}, \"filter\" : { \"bool\": { \"must\":[ {\"term\" : { \"Type\" : \"seeker\" }}, {\"range\":{ \"MinExperience\":{ \"lte\": " + userProfileJson.get("MinExperience") + "} }} ] } }}}}";
				//System.out.println("mlt query: " + mltQuery);
				StringEntity getJobsQuery = new StringEntity(mltQuery);
				getJobsQuery.setContentType("application/json");
				getJobs.setEntity(getJobsQuery);
				response = client.execute(getJobs);
				rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				line = rd.readLine();
				JSONObject finalResult = new JSONObject(line);
				jobList.put("count", finalResult.getJSONObject("hits").getInt("total"));
				jobList.put("list", finalResult.getJSONObject("hits").getJSONArray("hits"));
			}
			else{
				JSONArray list = new JSONArray();
				jobList.put("count", 0);
				jobList.put("list", list);
			}
	        // return HTTP response 200 in case of success
	        return Response.status(200).entity(jobList.toString()).build();
	        //return jsonResult;
		}
	}
