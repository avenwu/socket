package com.badlogic.utils;

public class StringHelper
{
	public static String decodeContent(String s)
	{
		return s + Config.CONTENT_SEPERATE;
	}

	public static String undecodeContent(String s)
	{
		int index = s.indexOf(Config.CONTENT_SEPERATE, 0);
		s = index > 0 ? (String) s.subSequence(0, index) : s;
		return s;
	}
}
