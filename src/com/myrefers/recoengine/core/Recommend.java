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
	@Path("/recommend")
	public class Recommend {
		public String serverUrlES = "http://localhost:9200/";
		@POST
	    @Path("/jobs")
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
			HttpPost post = new HttpPost(serverUrlES + "myrefers/doc/_search");
			String mltQuery =  "{ \"query\" : { \"filtered\" : {\"query\":{\"bool\" : {\"should\" : [{\"more_like_this\" : {\"fields\" : [\"IndustryName\"],\"like_text\" : \"" + userProfileJson.get("IndustryName") + "\",\"min_term_freq\" : 1,\"min_doc_freq\" : 2,\"max_query_terms\" : 12,\"boost\" : 2.0}},{\"more_like_this\" : {\"fields\" : [\"Specialization\"],\"like_text\" : \"" + userProfileJson.get("Specialization") + "\",\"min_term_freq\" : 1,\"min_doc_freq\" : 2,\"max_query_terms\" : 12,\"boost\" : 1.0}},{\"more_like_this\" : {\"fields\" : [\"PrimarySkills\"],\"like_text\" : \"" + userProfileJson.get("PrimarySkills") + "\",\"min_term_freq\" : 1,\"min_doc_freq\" : 2,\"max_query_terms\" : 12,\"boost\" : 3.0}},{\"more_like_this\" : {\"fields\" : [\"Education\"],\"like_text\" : \"" + userProfileJson.get("Education") + "\",\"min_term_freq\" : 1,\"min_doc_freq\" : 2,\"max_query_terms\" : 12,\"boost\" : 2.0}}],\"minimum_number_should_match\" : 1}}, \"filter\" : { \"bool\": { \"must\":[ {\"term\" : { \"Type\" : \"job\" }}, {\"term\" : { \"IsActive\": \"y\" }}, {\"range\":{ \"MinExperience\":{ \"lte\": " + userProfileJson.get("MinExperience") + "}}}, {\"range\":{ \"MaxExperience\":{ \"gte\": " + userProfileJson.get("MinExperience") + "} }} ] } }}}}";
			//System.out.println("mlt query: " + mltQuery);
			StringEntity input = new StringEntity(mltQuery);
			input.setContentType("application/json");
			post.setEntity(input);
			HttpResponse response = client.execute(post);
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = rd.readLine();
			JSONObject jsonResult = new JSONObject(line);
			JSONObject jobList = new JSONObject();
			jobList.put("JSid", "guest");
			jobList.put("count", jsonResult.getJSONObject("hits").getInt("total"));
			jobList.put("list", jsonResult.getJSONObject("hits").getJSONArray("hits"));
	        // return HTTP response 200 in case of success
	        return Response.status(200).entity(jobList.toString()).build();
	        //return jsonResult;
		}
		
		@GET
	    @Path("/jobs/candidate/{uid}")
		@Produces(MediaType.APPLICATION_JSON)
		public Response getRecommendedJobsForThisUser(@PathParam("uid") String UserId) throws ClientProtocolException, IOException {
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
			JSONObject jobList = new JSONObject();
			jobList.put("JSid", UserId);
			if(userProfile.getJSONObject("hits").has("total")){
				totalFound = userProfile.getJSONObject("hits").getInt("total");
			}
			else{
				jobList.put("status", false);
				jobList.put("errcode", 1);
				jobList.put("reason", "Couldn't find the candidate");
				JSONArray list = new JSONArray();
				jobList.put("count", 0);
				jobList.put("list", list);
				return Response.status(200).entity(jobList.toString()).build();
			}
			if(totalFound > 0){
				JSONObject userProfileJson = userProfile.getJSONObject("hits").getJSONArray("hits").getJSONObject(0).getJSONObject("_source");
				HttpPost getJobs = new HttpPost(serverUrlES + "myrefers/doc/_search");
				mltQuery = "{ \"query\" : { \"filtered\" : {\"query\":{\"bool\" : {\"should\" : [{\"more_like_this\" : {\"fields\" : [\"IndustryName\"],\"like_text\" : \"" + userProfileJson.get("IndustryName") + "\",\"min_term_freq\" : 1,\"min_doc_freq\" : 2,\"max_query_terms\" : 12,\"boost\" : 2.0}},{\"more_like_this\" : {\"fields\" : [\"Specialization\"],\"like_text\" : \"" + userProfileJson.get("Specialization") + "\",\"min_term_freq\" : 1,\"min_doc_freq\" : 2,\"max_query_terms\" : 12,\"boost\" : 1.0}},{\"more_like_this\" : {\"fields\" : [\"PrimarySkills\"],\"like_text\" : \"" + userProfileJson.get("PrimarySkills") + "\",\"min_term_freq\" : 1,\"min_doc_freq\" : 2,\"max_query_terms\" : 12,\"boost\" : 3.0}},{\"more_like_this\" : {\"fields\" : [\"Education\"],\"like_text\" : \"" + userProfileJson.get("Education") + "\",\"min_term_freq\" : 1,\"min_doc_freq\" : 2,\"max_query_terms\" : 12,\"boost\" : 2.0}}],\"minimum_number_should_match\" : 1}}, \"filter\" : { \"bool\": { \"must\":[ {\"term\" : { \"Type\" : \"job\" }}, {\"term\" : { \"IsActive\": \"y\" }}, {\"range\":{ \"MinExperience\":{ \"lte\": " + userProfileJson.get("MinExperience") + "}}}, {\"range\":{ \"MaxExperience\":{ \"gte\": " + userProfileJson.get("MinExperience") + "} }} ] } }}}}";
				//System.out.println("mlt query: " + mltQuery);
				StringEntity getJobsQuery = new StringEntity(mltQuery);
				getJobsQuery.setContentType("application/json");
				getJobs.setEntity(getJobsQuery);
				response = client.execute(getJobs);
				rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				line = rd.readLine();
				JSONObject finalResult = new JSONObject(line);
				if(finalResult.getJSONObject("hits").has("total")){
					jobList.put("count", finalResult.getJSONObject("hits").getInt("total"));
					jobList.put("list", finalResult.getJSONObject("hits").getJSONArray("hits"));
				}
				else{
					JSONArray list = new JSONArray();
					jobList.put("count", 0);
					jobList.put("list", list);
					jobList.put("status", true);
					jobList.put("errcode", 2);
					jobList.put("reason", "Couldn't get recommendations");
				}
			}
			else{
				JSONArray list = new JSONArray();
				jobList.put("count", 0);
				jobList.put("list", list);
				jobList.put("status", true);
				jobList.put("errcode", 0);
				jobList.put("reason", "No such candidate");
			}
	        // return HTTP response 200 in case of success
	        return Response.status(200).entity(jobList.toString()).build();
	        //return jsonResult;
		}
		
		@GET
	    @Path("/candidates/job/{jobid}")
		@Produces(MediaType.APPLICATION_JSON)
		public Response getSimilarUsersForThisUser(@PathParam("jobid") String JobId) throws ClientProtocolException, IOException {
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
			JSONObject candidateList = new JSONObject();
			candidateList.put("JSid", JobId);
			if(jobDetail.getJSONObject("hits").has("total")){
				totalFound = jobDetail.getJSONObject("hits").getInt("total");
			}
			else{
				candidateList.put("status", false);
				candidateList.put("errcode", 1);
				candidateList.put("reason", "Couldn't find the job");
				JSONArray list = new JSONArray();
				candidateList.put("count", 0);
				candidateList.put("list", list);
				return Response.status(200).entity(candidateList.toString()).build();
			}
			candidateList.put("JobId", JobId);
			if(totalFound > 0){
				JSONObject userProfileJson = jobDetail.getJSONObject("hits").getJSONArray("hits").getJSONObject(0).getJSONObject("_source");
				HttpPost getCandidates = new HttpPost(serverUrlES + "myrefers/doc/_search");
				mltQuery = "{ \"query\" : { \"filtered\" : {\"query\":{\"bool\" : {\"should\" : [{\"more_like_this\" : {\"fields\" : [\"IndustryName\"],\"like_text\" : \"" + userProfileJson.get("IndustryName") + "\",\"min_term_freq\" : 1,\"min_doc_freq\" : 2,\"max_query_terms\" : 12,\"boost\" : 2.0}},{\"more_like_this\" : {\"fields\" : [\"Specialization\"],\"like_text\" : \"" + userProfileJson.get("Specialization") + "\",\"min_term_freq\" : 1,\"min_doc_freq\" : 2,\"max_query_terms\" : 12,\"boost\" : 1.0}},{\"more_like_this\" : {\"fields\" : [\"PrimarySkills\"],\"like_text\" : \"" + userProfileJson.get("PrimarySkills") + "\",\"min_term_freq\" : 1,\"min_doc_freq\" : 2,\"max_query_terms\" : 12,\"boost\" : 3.0}},{\"more_like_this\" : {\"fields\" : [\"Education\"],\"like_text\" : \"" + userProfileJson.get("Education") + "\",\"min_term_freq\" : 1,\"min_doc_freq\" : 2,\"max_query_terms\" : 12,\"boost\" : 2.0}}],\"minimum_number_should_match\" : 1}}, \"filter\" : { \"bool\": { \"must\":[ {\"term\" : { \"Type\" : \"seeker\" }}, {\"range\":{ \"MinExperience\":{ \"gte\": " + userProfileJson.get("MinExperience") + ", \"lte\": " + userProfileJson.get("MaxExperience") + "} }} ] } }}}}";
				//System.out.println("mlt query: " + mltQuery);
				StringEntity getCandidatesQuery = new StringEntity(mltQuery);
				getCandidatesQuery.setContentType("application/json");
				getCandidates.setEntity(getCandidatesQuery);
				response = client.execute(getCandidates);
				rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				line = rd.readLine();
				JSONObject finalResult = new JSONObject(line);
				if(finalResult.getJSONObject("hits").has("total")){
					candidateList.put("count", finalResult.getJSONObject("hits").getInt("total"));
					candidateList.put("list", finalResult.getJSONObject("hits").getJSONArray("hits"));
				}
				else{
					JSONArray list = new JSONArray();
					candidateList.put("count", 0);
					candidateList.put("list", list);
					candidateList.put("status", true);
					candidateList.put("errcode", 2);
					candidateList.put("reason", "Couldn't get recommendations");
				}
			}
			else{
				JSONArray list = new JSONArray();
				candidateList.put("count", 0);
				candidateList.put("list", list);
				candidateList.put("status", true);
				candidateList.put("errcode", 0);
				candidateList.put("reason", "No such job");
			}
	        // return HTTP response 200 in case of success
	        return Response.status(200).entity(candidateList.toString()).build();
	        //return jsonResult;
		}
	}

