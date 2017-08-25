package com.ds05.launcher.common;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public final class JsonSerializer
{
	private static final Gson gson = new GsonBuilder().create();
	
	public  static String serialize(Object obj)
	{
		return gson.toJson(obj);
	}
	
	public static <T> T deSerialize(String json,Class<T> clazz)
	{
		try
		{
			return gson.fromJson(json, clazz);
		}catch(JsonSyntaxException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public static <T>List<T> deSerialize(String json,Type type){
		try
		{
			return gson.fromJson(json, type);
		}catch(JsonSyntaxException e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
