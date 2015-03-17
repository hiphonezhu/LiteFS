package com.rdinfo.fs;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
/**
 * �ļ���Ϣ(��ʽ��token=��֤��Ϣ&md5=�ļ�ժҪ��Ϣ&fileLength=�ļ�����)
 * 0����ʽ�Ƿ�
 * 1���ļ��Ѵ���
 * 2�����Կ�ʼ�ϴ�
 * 3���ϴ��ɹ�
 * 4��IDLE���ӶϿ�
 * 5���������ڲ�����
 * @author hiphonezhu@gmail.com
 * @version [LiteFS, 2015-3-15]
 */
public class Decoder extends CumulativeProtocolDecoder
{
    private Map<String, String> basicMap = new HashMap<String, String>();
    private String destDirPath = "C:/FS"; // �ļ�����Ŀ¼
    boolean hasDecodeProtocol; // �ļ�Э���Ƿ��Ѿ�����
    long receivedFileLength;
    
    public Decoder()
    {
        File dir = new File(destDirPath);
        if (!dir.exists())
        {
            dir.mkdirs();
        }
    }
    
    @Override
	protected boolean doDecode(IoSession session, IoBuffer buffer,
			ProtocolDecoderOutput out) throws Exception {
    	if (hasDecodeProtocol)
    	{
    		readStream(buffer);
            if (receivedFileLength == Long.parseLong(basicMap.get("fileLength")))
            {
      		   out.write(3);
            
          	   // close
          	   if (bos != null)
          	   {
          	  	  bos.close();
          	  	  bos = null;
               }
            }
    		return true;
    	}
    	else
    	{
    		while(buffer.hasRemaining())
            {
    			byte b = buffer.get();
                if (b == '\n') // ��ȡ�ύ���ļ���Ϣ
                {
                	hasDecodeProtocol = true;
                	int currentPosition = buffer.position();
                    buffer.position(0);
                    
                    byte[] dest = new byte[currentPosition - 1];
                    buffer.get(dest);
                    String basicInfo = new String(dest, "utf-8");
                    
                    buffer.position(currentPosition);
                    
                    // ����ļ���Ϣ�Ƿ�Ϸ�
                    System.out.println("received:" + basicInfo);
                    readBasicInfo(basicInfo);
                    final String md5 = basicMap.get("md5");
                    if (!checkToken() || md5 == null || basicMap.get("fileLength") == null)
                    {
                        System.out.println("invalid msg:" + basicInfo);
                        
                        out.write(0);
                        return true;
                    }
                    
                    // ���������Ƿ��Ѿ����ڸ��ļ�
                    String[] fileNames = new File(destDirPath).list(new FilenameFilter()
                    {
                        @Override
                        public boolean accept(File dir, String name)
                        {
                            return md5.equals(name);
                        }
                    });
                    if (fileNames != null && fileNames.length > 0) // �������Ѵ��ڴ��ļ�, ֱ�ӷ��سɹ�
                    {
                        out.write(1);
                        return true;
                    }
                    out.write(2);
                	return true;
                }
            }
    		return false;
    	}
	}
    
    private void readStream(IoBuffer in) throws IOException
    {
        while(in.hasRemaining())
        {
            int positon = in.position();
            if (in.limit() - positon > 1024 * 4) // ���ݴ���4k
            {
                byte[] data = new byte[1024 * 4];
                in.get(data);
                saveData(data);
            }
            else
            {
                byte[] data = new byte[in.limit() - positon];
                in.get(data);
                saveData(data);
            }
        }
    }
    
    BufferedOutputStream bos;
    private void saveData(byte[] data)
    {
        if (bos == null)
        {
            final String md5 = basicMap.get("md5");
            try
            {
                File file = new File(destDirPath, md5);
                if (!file.exists())
                {
                    file.createNewFile();
                }
                bos = new BufferedOutputStream(new FileOutputStream(file));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        try
        {
        	receivedFileLength += data.length;
            bos.write(data);
            bos.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * ��ȡ������Ϣ����
     * @param basicInfo
     */
    private void readBasicInfo(String basicInfo)
    {
        if (basicInfo == null)
        {
            return;
        }
        String[] keyValues = basicInfo.split("&");
        if (keyValues != null)
        {
            for(String str : keyValues)
            {
                String[] keyValue = str.split("=");
                if (keyValue != null && keyValue.length > 1)
                {
                    basicMap.put(keyValue[0], keyValue[1]);
                }
            }
        }
    }
    
    private boolean checkToken()
    {
        String token = basicMap.get("token");
        // �Ϸ��Լ��
        return true;
    }
    
    @Override
    public void finishDecode(IoSession session,
            ProtocolDecoderOutput out) throws Exception
    {
    	// close
    	if (bos != null)
    	{
    		bos.close();
    		bos = null;
    	}
    }

    @Override
    public void dispose(IoSession paramIoSession) throws Exception
    {

    }
}
