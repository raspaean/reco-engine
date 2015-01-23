package com.myrefers.recoengine.core;

public class UserBean {
	String uid;
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getMail() {
		return mail;
	}
	public void setMail(String mail) {
		this.mail = mail;
	}
	public CompanyBean getCompany() {
		return company;
	}
	public void setCompany(CompanyBean company) {
		this.company = company;
	}
	public ProfileBean getProfile() {
		return profile;
	}
	public void setProfile(ProfileBean profile) {
		this.profile = profile;
	}
	public SkillsBean getSkills() {
		return skills;
	}
	public void setSkills(SkillsBean skills) {
		this.skills = skills;
	}
	String name;
	String mail;
	CompanyBean company;
	ProfileBean profile;
	SkillsBean skills;
}

class ProfileBean{
	String id;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	String value;
}

class CompanyBean{
	String id;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	String value;
}

class SkillsBean{
	int count;
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public SkillBean[] getSkillList() {
		return skillList;
	}
	public void setSkillList(SkillBean[] skillList) {
		this.skillList = skillList;
	}
	SkillBean[] skillList;
}

class SkillBean{
	String id;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public Float getApplyScore() {
		return applyScore;
	}
	public void setApplyScore(Float applyScore) {
		this.applyScore = applyScore;
	}
	String value;
	Float applyScore;
}