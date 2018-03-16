package com.ifeng.common.email;

public interface IEmail {
	void sendEmail(String address, String subject, String msg);
	
	void sendEmail();
	
	void sendEmailLimit(Object key);
}
