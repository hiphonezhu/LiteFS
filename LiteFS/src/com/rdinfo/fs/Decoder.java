package com.rdinfo.fs;

import java.io.FileNotFoundException;

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
 * 6���ļ�У�鲻��ȷ(�յ��ĳ��Ȳ��Ի�md5У�鲻��ȷ)
 * @author hiphonezhu@gmail.com
 * @version [LiteFS, 2015-3-15]
 */
public class Decoder extends CumulativeProtocolDecoder
{
	private StreamReader streamReader;
    private ProtocolReader protocolReader;
    
    public Decoder()
    {
        protocolReader = new ProtocolReader();
        streamReader = new StreamReader(protocolReader);
    }
    
    public boolean vertifyFile() throws FileNotFoundException
    {
    	return streamReader.readOver() && streamReader.md5Legal();
    }
    
    @Override
	protected boolean doDecode(IoSession session, IoBuffer buffer,
			ProtocolDecoderOutput out) throws Exception {
    	if (protocolReader.validate())
    	{
    		streamReader.readStream(buffer);
            if (vertifyFile())
            {
      		   out.write(3);
            
      		   streamReader.close();
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
                	int currentPosition = buffer.position();
                    buffer.position(0);
                    
                    byte[] dest = new byte[currentPosition - 1];
                    buffer.get(dest);
                    String protocolInfo = new String(dest, "utf-8");
                    
                    buffer.position(currentPosition);
                    
                    // ����ļ���Ϣ�Ƿ�Ϸ�
                    protocolReader.readProtocol(protocolInfo);
                    if (!protocolReader.validate())
                    {
                        System.out.println("invalid protocol:" + protocolInfo + ", correct format is 'token=��֤��Ϣ&md5=�ļ�ժҪ��Ϣ&fileLength=�ļ�����'");
                        out.write(0);
                        return true;
                    }
                    
                    if (streamReader.fileExist()) // �������Ѵ��ڴ��ļ�, ֱ�ӷ��سɹ�
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
    
    @Override
    public void finishDecode(IoSession session,
            ProtocolDecoderOutput out) throws Exception
    {
    	System.out.println("finishDecode...");
    	streamReader.close();
    }

    @Override
    public void dispose(IoSession paramIoSession) throws Exception
    {

    }
}
