package org.oregan.xpi.tools;

import org.bouncycastle.crypto.tls.TlsProtocolHandler;
import org.bouncycastle.crypto.tls.AlwaysValidVerifyer;
import org.bouncycastle.util.encoders.Base64;

import java.net.Socket;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * Copyright Informatica Corporation 2007
 */
public class esmtp
{
    public static void main(String[] args)
    {
        try
        {
            Socket socket = new Socket("smtp.gmail.com", 587);
            OutputStream outputStream = socket.getOutputStream();
            InputStream in = socket.getInputStream();
            int read = 0;
            byte[] buf = new byte[2048];
            read = in.read(buf);
            System.out.println(read);
            System.out.println(new String(buf,0,read));
            outputStream.write("EHLO 10.153.25.2\r\n".getBytes());
            read = in.read(buf);
            System.out.println(read);
            System.out.println(new String(buf,0,read));
            outputStream.write("STARTTLS\r\n".getBytes());
            read = in.read(buf);
            System.out.println(read);
            System.out.println(new String(buf,0,read));


            TlsProtocolHandler tls = new TlsProtocolHandler(in,outputStream);
            tls.connect(new AlwaysValidVerifyer());

            InputStream secureIn = tls.getTlsInputStream();
            OutputStream secureOut = tls.getTlsOuputStream();
            secureOut.write("NOOP\r\n".getBytes());
            read = 0;
            buf = new byte[2048];
            read = secureIn.read(buf);
            System.out.println(read);
            System.out.println(new String(buf,0,read));

            secureOut.write("AUTH LOGIN\r\n".getBytes());
            read = secureIn.read(buf);
                        System.out.println(read);
                        System.out.println(new String(buf,0,read));

            byte[] username = Base64.encode("kevin.oregan@gmail.com".getBytes());
            System.out.println(new String(username));
            secureOut.write((new String(username) +"\r\n").getBytes());
            read = secureIn.read(buf);
                                    System.out.println(read);
                                    System.out.println(new String(buf,0,read));

            String pass = new String(Base64.encode("oliagop".getBytes()));
            System.out.println("pass = " + pass);
            secureOut.write((pass +"\r\n").getBytes());
            read = secureIn.read(buf);
                                    System.out.println(read);
                                    System.out.println(new String(buf,0,read));
            System.out.println("-- send mail");
            secureOut.write("HELP\r\n".getBytes());
            read = secureIn.read(buf);
                                                System.out.println(read);
                                                System.out.println(new String(buf,0,read));
            secureOut.write("MAIL FROM: <kevin.oregan@gmail.com>\r\n".getBytes());
            read = secureIn.read(buf);
                                                System.out.println(read);
                                                System.out.println(new String(buf,0,read));
            System.out.println("set rcpt");
            secureOut.write("RCPT TO: <kevin.oregan@gmail.com>\r\n".getBytes());
            read = secureIn.read(buf);
                                                System.out.println(read);
                                                System.out.println(new String(buf,0,read));
            secureOut.write("DATA\r\n".getBytes());
            read = secureIn.read(buf);
                                                System.out.println(read);
                                                System.out.println(new String(buf,0,read));

            secureOut.write("Subject: Testing\r\n\r\nThis is\r\n a test.\r\n.\r\n".getBytes());
                        read = secureIn.read(buf);
                                                            System.out.println(read);
                                                            System.out.println(new String(buf,0,read));
            secureOut.write("QUIT\r\n".getBytes());

            tls.close();

        } catch (IOException e)
        {
            e.printStackTrace();
        }


    }
}
