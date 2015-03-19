package com.rdinfo.fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
/**
 * ������
 */
public class Utils 
{
	/**
	 * ��ȡ�ļ�MD5ժҪ
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 */
	public static String getMd5ByFile(File file) throws FileNotFoundException 
	{
		long start = System.currentTimeMillis();
		System.out.println("getMd5ByFile start...");
		String value = null;
		FileInputStream in = new FileInputStream(file);
		try 
		{
			MappedByteBuffer byteBuffer = in.getChannel().map(
					FileChannel.MapMode.READ_ONLY, 0, file.length());
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(byteBuffer);
			BigInteger bi = new BigInteger(1, md5.digest());
			value = bi.toString(16);
			byteBuffer.clear();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		} 
		finally 
		{
			try 
			{
				if (null != in) 
				{
					in.close();
				}
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		System.out.println("getMd5ByFile end...��ʱ��" + (System.currentTimeMillis() - start) + "����");
		return value;
	} 
}
