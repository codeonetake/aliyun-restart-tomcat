package com.aliyun.jar.service;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.aliyun.jar.util.DoShell;
import com.aliyun.jar.util.ObjSave;

public class JarRestartService {
	private static String msg = "";
	private static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static String restartSerFile = "/root/data/aliyun/restart.ser";
	
	private static void start(){
		m("总重启tomcat开始");
		List<String> list = null;
		try {
			list = DoShell.shell("ps -ef|grep 'tomcat'");
			m("获取tomcat列表成功");
		} catch (Exception e) {
			m("获取tomcat列表失败");
		}
		if(null != list){
			String tomcatName = null;
			String pid = null;
			String filePath = null;
			boolean restartRes = false;
			for (String shell : list) {
				tomcatName = "";
				if(shell.contains("-classpath") && !shell.contains("aliyun-restart-tomcat")){
					//获取各种信息
					try {
						m("服务器信息获取开始");
						pid = shell.split("\\s+")[1];
						m("pid："+pid);
						shell = shell.substring(shell.indexOf("-classpath ")+11,shell.indexOf("/bin/bootstrap.jar"));
						filePath = shell + "/bin/startup.sh";
						m("filePath："+filePath);
						tomcatName = shell.substring(shell.lastIndexOf("/")+1);
						m("服务器(" + tomcatName + ")信息获取结束");
					} catch (Exception e) {
						System.out.println(shell);
						e.printStackTrace();
						m("服务器(" + tomcatName + ")解析失败");
						continue;
					}
					//重启服务器
					m("服务器(" + tomcatName + ")重启开始");
					restartRes = restart(filePath, pid);
					m("服务器(" + tomcatName + ")重启结束");
					if(restartRes){
						m("服务器(" + tomcatName + ")重启成功");
					}else{
						m("服务器(" + tomcatName + "重启失败");
					}
					m("==========================");
				}
			}
		}
		m("总重启tomcat结束");
		//保存结果
		File bakSer = new File(restartSerFile);
		if(!bakSer.getParentFile().exists()){
			bakSer.getParentFile().mkdirs();
		}
		System.out.println(msg);
		ObjSave.objectToFile(msg, restartSerFile);
	}
	
	public static void emptyRestartSerFile(){
		ObjSave.objectToFile("", restartSerFile);
	}
	
	public static String getRestartInfo(){
		return (String)ObjSave.fileToObject(restartSerFile);
	}
	
	private static boolean restart(String filePath,String pid){
		//先kill
		try {
			DoShell.shell("kill -9 "+pid);
		} catch (Exception e) {
			e.printStackTrace();
			//失败了，直接返回
			return false;
		}
		//在启动
		System.out.println(filePath);
		try {
			DoShell.shell("sh "+filePath);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private static void m(String content){
		if(content.contains("(")){
			content = content.replaceAll("\\(", "<b>");
		}
		if(content.contains(")")){
			content = content.replaceAll("\\)", "</b>");
		}
		if(content.contains("[")){
			content = content.replaceAll("\\[", "<code>");
		}
		if(content.contains("]")){
			content = content.replaceAll("\\]", "</code>");
		}
		if(content.contains("成功")){
			content = content.replaceAll("成功", "<font color='green'><b>成功</b></font>");
		}
		if(content.contains("失败")){
			content = content.replaceAll("失败", "<font color='red'><b>失败</b></font>");
		}
		if(content.endsWith("开始") || content.endsWith("结束")){
			String time = dateTimeFormat.format(new Date());
			msg += content + "，时间："+time+"<br/>";
		}else{
			msg += content + "<br/>";
		}
	}
	
	public static void main(String[] args) {
		try {
			start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
