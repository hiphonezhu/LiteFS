package com.rdinfo.fs;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;

import org.apache.mina.core.buffer.IoBuffer;

/**
 * �����ļ�
 */
public class StreamReader 
{
	private String destDirPath = "C:/FS"; // �ļ�����Ŀ¼
	private ProtocolReader protocolReader; // ��ȡ����Э��
	private long receivedFileLength; // �Ѿ����յ����ļ�����
	private BufferedOutputStream bos; // �ļ������
	
	public StreamReader(ProtocolReader protocolReader) 
	{
		this.protocolReader = protocolReader;
		File dir = new File(destDirPath);
        if (!dir.exists())
        {
            dir.mkdirs();
        }
	}
	
	/**
	 * ���������Ƿ��Ѿ����ڸ��ļ�
	 * @return
	 */
	public boolean fileExist()
	{
        String[] fileNames = new File(destDirPath).list(new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String name)
            {
                return protocolReader.get("md5").equals(name);
            }
        });
        if (fileNames != null && fileNames.length > 0)
        {
        	return true;
        }
        return false;
	}
	
	/**
	 * �ļ��Ƿ�������
	 * @return
	 */
	public boolean readOver()
	{
		return receivedFileLength == Long.parseLong(protocolReader.get("fileLength"));
	}
	
	/**
	 * �ļ�md5У���Ƿ�Ϸ�
	 * @return
	 * @throws FileNotFoundException
	 */
	public boolean md5Legal() throws FileNotFoundException
	{
		return Utils.getMd5ByFile(new File(destDirPath, protocolReader.get("md5"))).equals(protocolReader.get("md5"));
	}
	
	/**
	 * ��ȡbuffer�е�����
	 * @param in
	 * @throws IOException
	 */
	public void readStream(IoBuffer in) throws IOException
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
    
    /**
     * �����ݱ��浽�ļ�
     * @param data
     */
    private void saveData(byte[] data)
    {
        if (bos == null)
        {
            final String md5 = protocolReader.get("md5");
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
     * �ر��ļ���
     */
    public void close()
    {
    	// close
    	if (bos != null)
    	{
    		try {
				bos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
    		bos = null;
    	}
    }
}
