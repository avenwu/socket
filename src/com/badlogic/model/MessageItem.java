package com.badlogic.model;
/**
 * item for each message detail
 * 
 * @author AvenWu
 * @2013-5-1
 */
public class MessageItem {
	private String content;
	private boolean isUser = true;
	private String smileIconName;
	private boolean isReaded;
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public boolean isUser() {
		return isUser;
	}
	public void setUser(boolean isUser) {
		this.isUser = isUser;
	}
	public String getSmileIconName() {
		return smileIconName;
	}
	public void setSmileIconName(String smileIconName) {
		this.smileIconName = smileIconName;
	}
	public void setSmileIconName(int smileIconName) {
		this.smileIconName = smileIconName + "";
	}
	public boolean isReaded() {
		return isReaded;
	}
	public void setReaded(boolean isReaded) {
		this.isReaded = isReaded;
	}

}
