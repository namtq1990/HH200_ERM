package android.HH100;

import android.HH100.Identification.Isotope;
import android.HH100.Service.MainBroadcastReceiver;
import android.HH100.Structure.NcLibrary;
import android.HH100.Structure.Spectrum;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.Xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import NcLibrary.Coefficients;

public class TCPServerService extends Service {

    ServiceReceiver mReceiver = new ServiceReceiver();

    private ServerThread mThread;

    private static Context mContext;

    Object obj = new Object();

    private static int TimeSequenceSendSpectrum = 300; //5 minutes300

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;
        BrodcastDeclare();

        this.mThread = new ServerThread();
        this.mThread.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {

        super.onDestroy();

        LocalBroadcastManager.getInstance(mContext).unregisterReceiver((mReceiver));
        try {
            mThread.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void BrodcastDeclare() {

        IntentFilter filter = new IntentFilter();

        filter.addAction(MainBroadcastReceiver.MSG_RECV_SPECTRUM);

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mReceiver, filter);
    }

    class ServerThread extends Thread {

        private ServerSocket serverSocket = null;

        Socket socket = null;

        Boolean isRunning = true;

        Boolean isSocketClose = false;

        InputStream input;
        OutputStream output;

        public void close() throws IOException {
            isRunning = false;
            isSocketClose = true;

            if (serverSocket != null)
                serverSocket.close();
        }

        public void run() {
            try {
                serverSocket = new ServerSocket(8001);
                AcceptConnection();
            } catch (Exception ex) {
                NcLibrary.Write_ExceptionLog("Already AcceptConnection: " + ex.getMessage());
            }
        }

        public void SendPastData(String startTime, String stopTime) {
            try {
                String dta = "<NucareQueryResponse device=\"ERM111\" calibration=\"-4.11123013211306e-05 3.12609119455929 6.9939016308102\" >\n" +
                        "<Element time=\"2021-08-26 15:00:00\" doserate=\"2.5\" acqTime=\"300\" isSpectrum=\"true\">\n" +
                        "\t<Spectrum>0;0;0;2;26;66;103;101;112;108;101;108;123;152;172;189;211;217;255;271;300;333;371;340;358;344;351;354;367;343;372;382;379;323;361;352;336;334;350;319;332;326;299;344;260;282;267;279;275;257;256;253;274;235;248;236;243;251;230;227;220;214;220;203;213;210;201;194;183;193;191;203;192;173;158;178;161;141;126;126;131;142;145;125;125;112;115;122;112;111;132;106;116;107;104;92;99;105;87;93;81;86;111;106;104;97;122;88;92;94;99;89;82;102;70;72;65;60;71;58;77;53;59;65;55;65;65;52;58;59;66;49;53;55;44;47;36;49;41;42;46;39;39;52;38;41;44;42;31;43;44;51;38;50;38;42;40;41;33;40;31;36;40;33;37;35;43;41;37;43;33;34;29;29;42;40;42;23;20;39;45;33;32;43;29;31;40;37;47;42;49;38;47;37;29;56;44;24;46;52;65;68;56;60;51;48;64;41;39;57;27;33;33;18;31;30;21;17;18;18;21;14;16;17;22;12;18;18;24;16;16;18;18;11;16;21;13;24;21;20;19;14;13;21;20;9;14;14;14;18;8;18;15;19;13;16;16;13;17;10;10;10;19;17;12;21;11;11;18;15;17;12;20;17;15;18;13;15;19;20;17;12;17;16;10;19;14;16;15;14;15;12;14;11;11;22;17;18;18;11;12;6;16;15;15;15;18;6;12;15;5;10;7;9;13;10;12;15;11;11;7;10;8;12;5;12;12;13;11;10;6;11;13;10;9;11;10;16;4;17;7;10;6;15;13;17;9;13;12;17;12;11;17;10;4;9;10;10;10;5;9;7;10;8;14;11;11;5;9;9;11;13;11;8;10;7;14;9;4;6;4;6;7;10;3;10;8;5;9;4;8;3;5;3;8;8;9;2;4;8;8;4;6;2;6;2;4;2;2;8;6;5;7;7;2;5;3;4;3;3;8;5;4;4;5;5;6;5;6;4;10;4;6;7;10;12;15;11;14;17;14;18;14;21;23;19;18;22;23;26;23;18;28;20;23;22;29;15;19;17;23;16;15;18;19;17;10;13;5;11;3;3;5;6;2;7;3;5;4;0;0;0;2;3;4;2;3;1;4;3;0;0;4;2;2;0;2;2;1;2;3;2;1;0;2;2;2;1;1;2;1;2;5;2;1;1;0;1;2;1;1;3;1;4;0;2;4;5;3;1;1;2;3;1;4;2;1;3;3;3;1;2;4;0;1;1;3;4;2;3;7;1;2;7;3;3;4;2;2;2;1;2;2;1;4;1;2;0;2;2;0;0;0;1;1;3;1;0;2;0;1;1;2;0;2;0;0;2;1;0;5;1;4;0;1;1;1;0;1;1;2;1;0;2;2;0;4;1;0;3;3;2;0;0;1;1;0;1;2;2;1;0;1;3;3;0;0;0;1;0;1;1;0;0;2;3;1;1;0;2;0;1;2;0;1;0;1;3;0;1;1;4;1;1;1;1;0;2;2;4;1;0;1;2;3;2;1;2;2;1;1;3;0;2;5;3;3;0;2;2;0;0;1;2;0;3;0;0;0;0;4;1;0;1;1;5;1;1;0;0;0;2;3;0;2;1;1;2;0;0;0;1;1;2;2;0;0;0;2;0;2;2;2;2;0;0;2;2;1;1;1;0;1;1;0;0;1;0;0;1;1;1;1;2;3;0;0;0;0;0;0;1;0;0;0;0;0;1;0;0;0;1;0;0;0;0;3;2;1;0;0;1;1;1;0;1;0;0;1;0;0;0;0;0;0;1;1;0;0;1;0;0;1;0;0;0;1;1;1;1;0;0;1;0;1;0;0;1;0;1;0;2;1;0;1;6;1;2;2;1;5;3;0;4;5;1;3;2;4;2;1;2;3;1;3;3;2;4;5;2;1;1;0;0;1;1;0;0;0;2;0;0;0;1;0;1;0;0;0;1;0;0;0;0;0;1;0;0;0;1;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;1;0;0;0;0;0;0;0;0;0;1;0;0;0;0;1;0;0;0;0;1;0;0;0;0;0;0;0;0;0;1;0;0;0;0;0;1;0;0;0;0;1;0;0;0;0;0;0;0;0;0;0;0;0;0;0;1;0;0;0;0;0;1;0;0;0;0;0;1;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;1;0;0;0;0;0;0;0;0;0;0;0;0;1;0;0;0;1;0;0;0;0;1;1;0;0;0;0;0;\n" +
                        "\t</Spectrum>\n" +
                        "</Element>\n" +
                        "<Element time=\"2021-08-26 15:01:00\" doserate=\"2.5\" isSpectrum=\"false\"/>\n" +
                        "<Element time=\"2021-08-26 15:02:00\" doserate=\"2.5\" isSpectrum=\"false\"/>\n" +
                        "<Element time=\"2021-08-26 15:03:00\" doserate=\"2.5\" isSpectrum=\"false\"/>\n" +
                        "<Element time=\"2021-08-26 15:04:00\" doserate=\"2.5\" isSpectrum=\"false\"/>\n" +
                        "<Element time=\"2021-08-26 15:05:00\" doserate=\"2.5\" acqTime=\"300\" isSpectrum=\"true\">\n" +
                        "\t<Spectrum>0;0;0;0;9;22;41;42;45;32;50;48;59;70;80;89;96;109;121;134;140;137;162;144;145;156;160;155;143;146;122;140;131;134;145;116;134;130;101;100;98;111;91;108;90;82;88;106;103;86;69;92;85;75;85;80;86;72;67;61;58;63;53;65;75;58;55;64;69;55;48;45;55;49;47;55;52;45;39;39;42;43;39;35;41;47;38;35;30;37;34;28;31;27;34;27;27;28;32;25;16;30;29;31;28;26;23;34;26;21;19;19;24;20;24;21;21;14;16;10;16;11;17;25;20;16;17;26;14;17;9;13;10;11;12;15;14;17;17;16;11;14;16;10;21;13;13;13;8;6;10;15;13;11;16;13;16;14;12;12;11;8;8;15;15;11;8;12;9;9;13;6;1;7;10;4;5;8;8;4;6;9;2;10;1;11;15;10;15;8;10;9;11;10;10;8;6;8;3;13;2;4;3;6;6;10;2;4;10;3;5;3;6;14;4;3;3;7;8;8;4;1;5;8;6;6;4;5;6;5;10;11;6;4;6;3;4;8;3;5;6;5;4;4;8;5;4;4;3;2;6;5;6;10;6;3;4;6;5;4;4;1;8;3;5;5;5;5;8;3;5;2;5;3;4;5;7;12;1;2;4;5;2;6;4;6;3;5;4;1;6;6;5;4;2;8;0;5;5;5;5;5;3;3;5;5;3;6;5;6;5;6;2;3;4;4;5;2;2;4;2;5;8;7;4;6;1;3;3;1;5;3;4;6;4;4;3;2;4;4;1;5;3;3;7;3;6;3;1;2;2;3;8;2;5;2;3;2;0;4;0;5;2;3;2;1;3;5;3;5;1;2;5;2;2;2;1;2;3;4;2;4;3;2;0;2;1;0;2;2;2;1;2;2;3;3;1;3;1;2;2;0;2;0;0;0;1;2;0;2;3;2;1;2;0;3;2;4;1;0;2;1;1;2;3;1;2;0;0;2;2;0;2;0;2;3;1;1;3;2;2;1;3;1;1;4;4;8;2;2;5;5;8;8;8;4;6;5;5;5;9;9;12;5;8;9;6;8;13;8;6;5;7;6;5;7;4;7;6;4;7;4;0;3;1;3;1;2;1;1;0;0;1;0;0;0;1;0;1;1;0;2;1;0;1;1;1;2;1;2;1;0;0;0;1;0;1;2;1;0;0;1;0;2;2;0;0;1;3;1;0;1;0;0;0;0;0;0;0;1;0;1;0;1;0;0;0;0;2;1;1;0;1;0;0;1;2;1;0;1;2;2;1;2;0;2;0;0;3;2;0;0;0;1;1;2;0;0;3;0;1;2;1;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;1;0;0;1;1;1;0;0;0;1;1;0;0;0;0;1;0;0;0;0;0;0;0;0;1;0;0;0;0;0;0;0;1;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;1;1;0;0;0;1;0;0;0;0;0;1;0;0;0;1;1;0;0;0;1;0;1;0;0;0;0;0;0;0;1;1;1;0;3;1;1;0;0;0;0;0;1;1;2;0;0;0;0;0;0;0;1;0;0;0;0;1;0;1;0;1;2;0;0;3;0;0;0;0;1;1;0;0;1;0;0;1;2;0;0;0;1;0;1;0;0;0;0;0;0;0;0;0;1;1;1;0;0;0;0;0;0;0;0;0;0;3;0;0;0;1;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;1;0;0;0;0;1;1;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;1;0;1;0;0;0;1;0;0;0;0;0;0;1;0;1;2;0;0;0;1;0;0;0;0;0;0;1;2;0;1;0;0;1;0;0;0;2;0;1;0;2;0;0;1;1;0;0;1;0;0;0;0;0;0;0;0;0;0;0;1;0;0;0;0;1;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;1;0;0;0;0;0;0;0;0;0;0;0;1;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;1;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;1;0;1;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;1;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;1;0;0;0;\n" +
                        "\t</Spectrum>\n" +
                        "</Element>\n" +
                        "</NucareQueryResponse>";

                StringWriter writer = new StringWriter();
                writer.write(dta);
                SendDataBytes(output, writer);
            }
            catch (Exception ex)
            {
                Log.e("TuanPA", ex.getMessage());
            }
        }

        private void AcceptConnection() {
            if (isSocketClose)
                return;

            if (serverSocket != null) {
                try {
                    socket = null;
                    socket = serverSocket.accept();
                    socket.setSoTimeout(30000);

                    input = socket.getInputStream();
                    output = socket.getOutputStream();

                    while (isRunning) {
//                        byte[] buf = new byte[4];
//
//                        int offset = 0;
//                        int recvDataSize = 0;
//                        int leftDataSize = 4;
//
//                        while (leftDataSize > 0) {
//
//                            recvDataSize = input.read(buf, offset, leftDataSize);
//                            if (recvDataSize < 0) {
//                                NcLibrary.SaveText("RecvDataSize < 0, Need reset socket", "tcpServerService.txt", true);
//                                if (socket != null) {
//                                    try {
//                                        input.close();
//                                        output.close();
//                                        socket.close();
//                                        AcceptConnection();
//                                        return;
//                                    } catch (IOException e) {
//                                        NcLibrary.Write_ExceptionLog(e.getMessage());
//                                    }
//                                }
//                            }
//
//                            offset += recvDataSize;
//                            leftDataSize -= recvDataSize;
//                        }
                        int readByte = input.read();
                        if (readByte == -1)
                            break;

                        if (readByte != 'U') continue;

                        readByte = input.read();
                        if (readByte == -1)
                            break;

                        if (readByte != 'U') continue;

                        byte[] bufLengthData = new byte[4];
                        readByte = input.read(bufLengthData);
                        if (readByte == -1)
                            break;

                        int lengthData = ParseLength(bufLengthData);

                        int offset = 0;
                        int recvDataSize = 0;
                        byte[] buf = new byte[lengthData];

                        while (lengthData > 0) {

                            recvDataSize = input.read(buf, offset, lengthData);
                            if (recvDataSize == -1) {
                                break;
                            }

                            offset += recvDataSize;
                            lengthData -= recvDataSize;
                        }

                        String data = new String(buf, StandardCharsets.US_ASCII);

                        data = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + data;

                        Document doc = ConvertStringToDocument(data);
                        Node c = doc.getChildNodes().item(0);

                        if (c.getNodeName().equals("U2AA")) {
                            SendRealtimeData();
                        }
                        else if(c.getNodeName().equals("NucareQueryData"))
                        {
                            NodeList nodes = c.getChildNodes();
                            String start = "";
                            String end = "";

                            for(int i =0;i< nodes.getLength(); i++)
                            {
                                if (nodes.item(i).getNodeName().equals("Start"))
                                {
                                    start = nodes.item(i).getTextContent();
                                }
                                else if (nodes.item(i).getNodeName().equals("End"))
                                {
                                    end = nodes.item(i).getTextContent();
                                }
                            }

                            Log.i("TuanPA", "data start: " + start);
                            Log.i("TuanPA", "data end: " + end);

                            SendPastData(start,end);
                        }
                    }

                } catch (IOException e) {
                    StackTraceElement[] stackTraceElements = e.getStackTrace();
                    String logMessage = "IOException occured in method "
                            + stackTraceElements[0].getMethodName() + " - File name is "
                            + stackTraceElements[0].getFileName()
                            + " - At line number: "
                            + stackTraceElements[0].getLineNumber();
                    NcLibrary.Write_ExceptionLog("IOException: " + logMessage);
                } catch (Exception e) {
                    NcLibrary.Write_ExceptionLog("Exception: " + e.getMessage());
                } finally {
                    ResetSocket();
                }
            }
        }

        public int ParseLength(byte[] array) {
            int i = 0;
            int j = array.length - 1;
            byte tmp;

            String txt = "";
            for(byte f : array)
            {
                txt += String.valueOf(f) + ";";
            }

            while (j > i) {
                tmp = array[j];

                array[j] = array[i];
                array[i] = tmp;
                j--;
                i++;
            }
            int num = ByteBuffer.wrap(array).getInt();

            return num;
        }

        private Document ConvertStringToDocument(String xmlStr) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            try {
                builder = factory.newDocumentBuilder();
                Document doc = builder.parse(new InputSource(new StringReader(xmlStr)));
                return doc;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private void ResetSocket() {
            if (socket != null) {
                try {
                    NcLibrary.SaveText("ResetSocket", "tcpServerService.txt", true);

                    input.close();
                    output.close();
                    socket.close();

                    AcceptConnection();

                } catch (IOException e) {
                    NcLibrary.Write_ExceptionLog("Restart socket error: " + e.getMessage());
                }
            }
        }

        private void SendRealtimeData() throws IOException, InterruptedException {
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dateFormatter.setLenient(false);
            Date today = new Date();
            String _time = dateFormatter.format(today);

            synchronized (obj) {
                Vector<Isotope> isotopes = new Vector<>();
                String Spctemp = "";
                if (!spcAccumulation.IsAccumulateSpc()) {
                    //Spctemp = "0;0;0;0;0;33;230;487;525;507;559;598;639;696;743;893;952;1000;1091;1326;1396;1570;1650;1826;1848;1828;1936;1958;2057;2111;2122;2176;2077;2093;2001;2163;2141;2117;2157;2086;2017;2050;1990;1910;2018;1846;1764;1746;1693;1698;1669;1688;1609;1600;1573;1578;1463;1537;1494;1425;1391;1341;1316;1262;1303;1206;1185;1194;1193;1180;1194;1184;1192;1257;1202;1202;1085;1087;971;1002;895;874;826;795;777;835;794;771;728;774;768;709;676;765;698;675;646;583;599;623;573;595;575;592;550;563;591;549;590;571;608;591;570;545;497;506;443;464;389;382;373;401;341;361;342;363;331;330;343;343;354;284;300;304;292;290;304;274;256;284;265;267;263;312;270;268;265;268;225;242;291;290;269;247;241;256;263;259;263;224;232;255;237;253;231;228;231;232;234;229;218;227;216;177;215;202;231;215;215;207;248;225;208;240;236;257;276;254;268;282;270;273;279;280;273;241;249;247;224;203;227;190;183;173;163;156;146;143;144;117;151;141;144;129;119;122;111;140;128;129;120;123;133;121;118;116;125;124;113;125;114;114;129;124;110;96;111;118;134;110;123;114;120;125;116;112;118;120;106;114;110;96;117;112;106;113;105;94;97;95;103;86;101;89;93;82;104;94;99;100;97;88;104;79;94;86;87;95;86;102;84;110;79;83;111;102;107;106;113;111;117;129;103;102;98;121;117;104;97;113;118;102;101;116;94;97;115;121;93;90;104;104;105;84;83;79;74;70;69;65;62;74;67;68;69;53;62;73;55;64;64;54;56;64;41;66;48;50;67;61;57;58;64;49;49;78;49;63;74;85;71;73;71;65;75;86;73;70;75;66;79;61;52;78;78;65;67;74;72;67;61;67;53;51;60;57;33;51;46;42;45;50;56;55;52;46;48;36;51;55;50;43;46;40;38;56;50;58;52;41;53;41;30;48;30;49;41;41;34;40;37;38;37;28;32;43;44;30;36;33;33;32;29;22;39;31;29;17;31;34;32;30;23;25;33;36;36;37;28;45;34;51;51;49;46;49;42;56;43;47;60;63;67;74;64;69;72;78;81;80;103;107;109;119;114;121;93;128;122;98;101;108;81;94;82;87;102;82;79;64;62;60;37;54;38;38;29;27;39;39;25;24;24;25;24;31;18;25;29;19;14;20;24;16;12;20;15;28;13;14;26;20;19;30;16;24;24;20;20;25;21;26;15;10;14;19;16;18;13;18;12;12;14;12;17;23;17;11;14;18;21;18;17;23;16;11;18;18;18;15;22;23;26;23;21;28;20;23;32;34;24;18;16;23;16;32;17;28;23;27;17;30;25;21;19;21;20;15;27;21;12;16;18;16;17;9;19;15;21;18;9;11;16;13;23;14;9;9;13;9;8;11;13;10;7;3;14;7;18;5;9;8;6;13;6;14;12;15;14;9;14;13;10;13;3;7;9;9;9;12;8;9;13;7;12;11;8;6;10;7;12;5;11;9;5;5;5;14;8;10;12;10;9;6;5;8;10;10;13;7;15;15;10;7;9;11;11;10;15;12;7;15;6;18;15;17;11;12;12;15;19;10;14;11;7;14;10;11;16;10;11;17;13;16;13;7;18;14;6;18;10;12;13;9;12;16;14;7;12;14;11;5;9;6;13;7;10;13;12;9;16;6;19;9;8;8;8;5;11;9;11;4;6;3;7;9;9;7;5;8;6;6;10;4;6;9;7;3;4;5;7;2;8;9;6;7;11;7;4;8;3;10;4;5;9;5;7;2;8;4;5;5;3;5;7;1;0;2;4;3;4;2;4;7;4;6;4;4;2;3;1;3;3;2;6;1;3;4;6;6;3;8;5;5;4;3;6;10;7;4;8;4;6;14;12;15;11;8;11;10;19;21;23;16;14;22;23;18;19;20;13;27;17;14;24;17;21;9;16;23;19;21;16;15;15;10;10;8;16;12;9;10;13;3;8;5;6;4;6;8;5;5;4;3;2;4;1;2;1;1;1;2;2;0;2;3;0;0;2;1;0;0;1;0;1;0;0;1;0;0;0;1;0;3;0;0;1;1;0;0;0;1;1;4;0;0;0;0;0;0;0;0;0;0;0;0;0;2;0;1;0;0;0;1;0;0;0;1;0;0;0;0;0;0;0;1;1;0;0;0;0;0;0;0;1;1;0;0;0;0;0;0;1;0;1;1;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;1;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;1;2;0;0;0;0;0;1;0;0;0;0;0;0;0;0;0;0;1;0;0;0;0;0;0;0;0;0;0;0;0;";

                    Spctemp = spcAccumulation.ToStringSpc();

                    isotopes = spcAccumulation.FindIso(EnCoeff_Cali.get_Coefficients());

                    double temp_avg_doserate = spcAccumulation.GenarateDoserate(EnCoeff_Cali.get_Coefficients());
                    avg_doserate = Math.round(temp_avg_doserate * 1000.0) / 1000.0;
                }

                XmlSerializer serializer = Xml.newSerializer();
                StringWriter writer = new StringWriter();
                try {
                    serializer.setOutput(writer);

                    serializer.startTag(null, "N42InstrumentData");
                    /// ----------------------------------------------------
                    serializer.startTag(null, "Measurement");
                    serializer.attribute(null, "UUID", "54531d28-402b-11d8-af12-0002a5094c23");
                    serializer.startTag(null, "InstrumentInformation");
                    serializer.startTag(null, "InstrumentType");
                    serializer.text("Spectrometer");
                    serializer.endTag(null, "InstrumentType");

                    serializer.startTag(null, "Manufacturer");
                    serializer.text("Berkeley Nucleonics Corp.");
                    serializer.endTag(null, "Manufacturer");

                    serializer.startTag(null, "InstrumentModel");
                    serializer.text("HH200");
                    serializer.endTag(null, "InstrumentModel");

                    serializer.startTag(null, "InstrumentID");
                    serializer.text("HH200");
                    serializer.endTag(null, "InstrumentID");
                    serializer.endTag(null, "InstrumentInformation");
                    //
                    serializer.startTag(null, "MeasuredItemInformation");
                    serializer.startTag(null, "ItemDescription");
                    serializer.text("Cal standard 1");
                    serializer.endTag(null, "ItemDescription");

                    serializer.startTag(null, "ItemQuantity");
                    serializer.attribute(null, "Units", "Kg");
                    serializer.text("1.0.0");
                    serializer.endTag(null, "ItemQuantity");

                    serializer.startTag(null, "MeasurementLocation");
                    serializer.startTag(null, "MeasurementLocationName");
                    serializer.text("");
                    serializer.endTag(null, "MeasurementLocationName");

                    serializer.startTag(null, "Coordinates");
                    serializer.text("37.489898681640625 129.12225341796875");
                    //serializer.text(latlong);
                    serializer.endTag(null, "Coordinates");
                    serializer.endTag(null, "MeasurementLocation");

                    serializer.startTag(null, "ItemReferenceDate");
                    serializer.text(_time);
                    serializer.endTag(null, "ItemReferenceDate");
                    serializer.endTag(null, "MeasuredItemInformation");

                    // Mesurement spectrum
                    serializer.startTag(null, "Spectrum");
                    serializer.attribute(null, "CalibrationIDs", "en");
                    serializer.startTag(null, "StartTime");
                    serializer.text(_time);
                    serializer.endTag(null, "StartTime");

                    serializer.startTag(null, "RealTime");
                    serializer.text(String.valueOf(TimeSequenceSendSpectrum));
                    serializer.endTag(null, "RealTime");

                    serializer.startTag(null, "LiveTime");
                    serializer.text(String.valueOf(TimeSequenceSendSpectrum));
                    serializer.endTag(null, "LiveTime");

                    serializer.startTag(null, "SourceType");
                    serializer.text("Item");
                    serializer.endTag(null, "SourceType");

                    serializer.startTag(null, "ChannelData");
                    serializer.text(Spctemp);
//                                    serializer.text("0;0;0;0;0;33;230;487;525;507;559;598;639;696;743;893;952;1000;1091;1326;1396;1570;1650;1826;1848;1828;1936;1958;2057;2111;2122;2176;2077;2093;2001;2163;2141;2117;2157;2086;2017;2050;1990;1910;2018;1846;1764;1746;1693;1698;1669;1688;1609;1600;1573;1578;1463;1537;1494;1425;1391;1341;1316;1262;1303;1206;1185;1194;1193;1180;1194;1184;1192;1257;1202;1202;1085;1087;971;1002;895;874;826;795;777;835;794;771;728;774;768;709;676;765;698;675;646;583;599;623;573;595;575;592;550;563;591;549;590;571;608;591;570;545;497;506;443;464;389;382;373;401;341;361;342;363;331;330;343;343;354;284;300;304;292;290;304;274;256;284;265;267;263;312;270;268;265;268;225;242;291;290;269;247;241;256;263;259;263;224;232;255;237;253;231;228;231;232;234;229;218;227;216;177;215;202;231;215;215;207;248;225;208;240;236;257;276;254;268;282;270;273;279;280;273;241;249;247;224;203;227;190;183;173;163;156;146;143;144;117;151;141;144;129;119;122;111;140;128;129;120;123;133;121;118;116;125;124;113;125;114;114;129;124;110;96;111;118;134;110;123;114;120;125;116;112;118;120;106;114;110;96;117;112;106;113;105;94;97;95;103;86;101;89;93;82;104;94;99;100;97;88;104;79;94;86;87;95;86;102;84;110;79;83;111;102;107;106;113;111;117;129;103;102;98;121;117;104;97;113;118;102;101;116;94;97;115;121;93;90;104;104;105;84;83;79;74;70;69;65;62;74;67;68;69;53;62;73;55;64;64;54;56;64;41;66;48;50;67;61;57;58;64;49;49;78;49;63;74;85;71;73;71;65;75;86;73;70;75;66;79;61;52;78;78;65;67;74;72;67;61;67;53;51;60;57;33;51;46;42;45;50;56;55;52;46;48;36;51;55;50;43;46;40;38;56;50;58;52;41;53;41;30;48;30;49;41;41;34;40;37;38;37;28;32;43;44;30;36;33;33;32;29;22;39;31;29;17;31;34;32;30;23;25;33;36;36;37;28;45;34;51;51;49;46;49;42;56;43;47;60;63;67;74;64;69;72;78;81;80;103;107;109;119;114;121;93;128;122;98;101;108;81;94;82;87;102;82;79;64;62;60;37;54;38;38;29;27;39;39;25;24;24;25;24;31;18;25;29;19;14;20;24;16;12;20;15;28;13;14;26;20;19;30;16;24;24;20;20;25;21;26;15;10;14;19;16;18;13;18;12;12;14;12;17;23;17;11;14;18;21;18;17;23;16;11;18;18;18;15;22;23;26;23;21;28;20;23;32;34;24;18;16;23;16;32;17;28;23;27;17;30;25;21;19;21;20;15;27;21;12;16;18;16;17;9;19;15;21;18;9;11;16;13;23;14;9;9;13;9;8;11;13;10;7;3;14;7;18;5;9;8;6;13;6;14;12;15;14;9;14;13;10;13;3;7;9;9;9;12;8;9;13;7;12;11;8;6;10;7;12;5;11;9;5;5;5;14;8;10;12;10;9;6;5;8;10;10;13;7;15;15;10;7;9;11;11;10;15;12;7;15;6;18;15;17;11;12;12;15;19;10;14;11;7;14;10;11;16;10;11;17;13;16;13;7;18;14;6;18;10;12;13;9;12;16;14;7;12;14;11;5;9;6;13;7;10;13;12;9;16;6;19;9;8;8;8;5;11;9;11;4;6;3;7;9;9;7;5;8;6;6;10;4;6;9;7;3;4;5;7;2;8;9;6;7;11;7;4;8;3;10;4;5;9;5;7;2;8;4;5;5;3;5;7;1;0;2;4;3;4;2;4;7;4;6;4;4;2;3;1;3;3;2;6;1;3;4;6;6;3;8;5;5;4;3;6;10;7;4;8;4;6;14;12;15;11;8;11;10;19;21;23;16;14;22;23;18;19;20;13;27;17;14;24;17;21;9;16;23;19;21;16;15;15;10;10;8;16;12;9;10;13;3;8;5;6;4;6;8;5;5;4;3;2;4;1;2;1;1;1;2;2;0;2;3;0;0;2;1;0;0;1;0;1;0;0;1;0;0;0;1;0;3;0;0;1;1;0;0;0;1;1;4;0;0;0;0;0;0;0;0;0;0;0;0;0;2;0;1;0;0;0;1;0;0;0;1;0;0;0;0;0;0;0;1;1;0;0;0;0;0;0;0;1;1;0;0;0;0;0;0;1;0;1;1;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;1;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;1;2;0;0;0;0;0;1;0;0;0;0;0;0;0;0;0;0;1;0;0;0;0;0;0;0;0;0;0;0;0;");
                    serializer.endTag(null, "ChannelData");
                    serializer.startTag(null, "AccumulateSPC");
                    serializer.text(String.valueOf(spcAccumulation.accumulateSPC));
                    serializer.endTag(null, "AccumulateSPC");
                    serializer.startTag(null, "AvgDoserate");
                    serializer.text(String.valueOf(avg_doserate));
                    serializer.endTag(null, "AvgDoserate");
                    serializer.endTag(null, "Spectrum");

                    serializer.endTag(null, "Measurement");

                    //Analysis result:
                    serializer.startTag(null, "AnalysisResults");

                    serializer.startTag(null, "ThreatDescription");
                    serializer.endTag(null, "ThreatDescription");

                    serializer.startTag(null, "NuclideAnalysis");
                    for (int i = 0; i < isotopes.size(); i++) {
                        serializer.startTag(null, "Nuclide");
                        serializer.startTag(null, "NuclideName");
                        serializer.text(isotopes.get(i).isotopes);
                        serializer.endTag(null, "NuclideName");
                        serializer.startTag(null, "NuclideIDConfidenceIndication");
                        serializer.text(String.valueOf(isotopes.get(i).Confidence_Level));
                        serializer.endTag(null, "NuclideIDConfidenceIndication");
                        serializer.endTag(null, "Nuclide");
                    }
                    serializer.endTag(null, "NuclideAnalysis");

                    serializer.endTag(null, "AnalysisResults");
                    //End Analysis result

                    // -----------
                    serializer.startTag(null, "Calibration");
                    serializer.attribute(null, "EnergyUnits", "keV");
                    serializer.attribute(null, "ID", "en");
                    serializer.attribute(null, "Type", "Energy");

                    serializer.startTag(null, "Equation");
                    serializer.attribute(null, "Form", "Term0+(Term1*Ch)+(Term2*(Ch^2))");
                    serializer.attribute(null, "Model", "Polynomial");

                    serializer.startTag(null, "Coefficients");
                    serializer.text(EnCoeff_Cali.At(2) + " " + EnCoeff_Cali.At(1) + " " + EnCoeff_Cali.At(0));
//                                    serializer.text("10.509140014648438 3.089423656463623 7.895296585047618E-5");
                    serializer.endTag(null, "Coefficients");

                    serializer.endTag(null, "Equation");
                    serializer.endTag(null, "Calibration");
                    /// ------------------------

                    serializer.endTag(null, "N42InstrumentData");
                    serializer.endDocument();
                    // write xml data into the FileOutputStream
                    serializer.flush();
                    // finally we close the file stream
                    writer.close();

                } catch (Exception e) {
                    NcLibrary.Write_ExceptionLog(e.getMessage());
                    NcLibrary.Write_ExceptionLog("error occurred while creating xml file");
                }

                if (!spcAccumulation.IsAccumulateSpc()) {
                    spcAccumulation.InitAccumulate();
                }

                //NcLibrary.SaveText("Send file: " + writer.toString(), "tcpServerService_file.txt", true);

                SendDataBytes(output, writer);
            }
        }

        private void SendDataBytes(OutputStream out, StringWriter strwr) throws InterruptedException, IOException {
            SPCDataReadService reader = new SPCDataReadService(strwr);

            byte[] rcvBuffList = new byte[10];
            rcvBuffList[0] = 0x55;
            rcvBuffList[1] = 0x55;
            rcvBuffList[2] = 0x55;
            rcvBuffList[3] = 0x55;

            int fileSize = (int) reader.FileLength();
            rcvBuffList[4] = (byte) (fileSize >> 24);
            rcvBuffList[5] = (byte) (fileSize >> 16);
            rcvBuffList[6] = (byte) (fileSize >> 8);
            rcvBuffList[7] = (byte) (fileSize);

            rcvBuffList[8] = (byte) 'F';
            rcvBuffList[9] = (byte) 'F';

            out.write(rcvBuffList);
            out.flush();

            Thread.sleep(100);

            int packetCnt = (int) Math.ceil(fileSize / 1000.0);

            for (int i = 0; i < packetCnt; i++) {
                byte[] xmlbuffList = new byte[1000];
                byte[] buffList = new byte[1008];

                buffList[0] = 0x55;
                buffList[1] = 0x55;
                buffList[2] = (byte) 'D';
                buffList[3] = (byte) 'D';

                if (i == packetCnt - 1) {
                    buffList[2] = (byte) 'E';
                    buffList[3] = (byte) 'E';
                }

                int cnt = i + 1;

                buffList[4] = (byte) (cnt >> 8);
                buffList[5] = (byte) (cnt);

                xmlbuffList = reader.ReadData(1000);
                int countRead = reader.GerReadCount();

                for (int k = 0; k < countRead; k++) {
                    buffList[k + 6] = xmlbuffList[k];
                }

                buffList[countRead + 6] = (byte) 'F';
                buffList[countRead + 7] = (byte) 'F';

                byte[] _buffList = Arrays.copyOf(buffList, countRead + 8);

                out.write(_buffList);
                out.flush();

                Thread.sleep(50);
            }
        }
    }

    public String ToString(double[] _spc) {
        String result = "";

        for (int i = 0; i < _spc.length; i++) {
            result += ((int) _spc[i]) + ";";
        }
        return result;
    }

    double avg_doserate;
    Coefficients EnCoeff_Cali;

    SpectrumAccumulation spcAccumulation = new SpectrumAccumulation(TimeSequenceSendSpectrum);
    SpectrumAccumulation spcAccumulation_1m = new SpectrumAccumulation(60);
    SpectrumAccumulation spcAccumulation_5s = new SpectrumAccumulation(5);

    class ServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                switch (action) {
                    case MainBroadcastReceiver.MSG_RECV_SPECTRUM:

                        synchronized (obj) {

                            double _avg_doserate = MainActivity.mDetector.Get_Gamma_DoseRate_nSV() / 1000;
                            avg_doserate = Math.round(_avg_doserate * 1000.0) / 1000.0;

                            Coefficients TempEnCoeff_Cali = MainActivity.mDetector.MS.Get_Coefficients();// Energy Calibration
                            if (TempEnCoeff_Cali != null) {
                                EnCoeff_Cali = TempEnCoeff_Cali;
                            }

                            double[] tempSpc = MainActivity.mDetector.MS.ToDouble();

                            spcAccumulation.Accumulate(tempSpc);
                            spcAccumulation_1m.Accumulate(tempSpc);
                            spcAccumulation_5s.Accumulate(tempSpc);

                            break;
                        }
                }
            } catch (Exception e) {
                NcLibrary.Write_ExceptionLog("Receiver spectrum error: " + e.getMessage());
            }
        }
    }

    class SpectrumAccumulation {
        public SpectrumAccumulation(int timeAccu) {
            accumulateSPC = true;
            mSPC_Accumulattion = new double[1024];
            countData = 0;
            timeAccumulate = timeAccu;
        }

        public void InitAccumulate() {
            accumulateSPC = true;
            mSPC_Accumulattion = new double[1024];
            countData = 0;
        }

        public boolean IsAccumulateSpc() {
            return accumulateSPC;
        }

        public void Accumulate(double[] spc) {
            if (accumulateSPC) {
                countData = countData + 1;

                if (countData >= timeAccumulate) {
                    accumulateSPC = false;
                } else {
                    for (int i = 0; i < spc.length; i++) {
                        mSPC_Accumulattion[i] += spc[i];
                    }
                }
            }
        }

        public Vector<Isotope> FindIso(double[] coeff) {
            Vector<Isotope> iso = new Vector<>();

            Spectrum Spcinput = new Spectrum(mSPC_Accumulattion);
            Spcinput.Set_Coefficients(coeff);
            Spcinput.setWnd_Roi(MainActivity.mDetector.Real_BG.getWnd_Roi());
            Spcinput.setFWHM(MainActivity.mDetector.MS.getFWHM());
            Spcinput.setFindPeakN_Coefficients(MainActivity.mDetector.MS.getFindPeakN_Coefficients());
            Spcinput.mAcqTime = countData;

            iso = MainActivity.mIsoLib2.Find_Isotopes_with_Energy(Spcinput, MainActivity.mDetector.Real_BG);

            return iso;
        }

        public double GenarateDoserate(double[] coeff) {
            if (countData == 0) {
                return 0;
            }

            double tempDose = NcLibrary.DoseRateCalculate_GE(
                    mSPC_Accumulattion,
                    countData,
                    MainActivity.mDetector.Real_BG.ToDouble(),
                    MainActivity.mDetector.Real_BG.Get_AcqTime(),
                    coeff,
                    MainActivity.mDetector.mPmtSurface,
                    MainActivity.mDetector.mCrystal,
                    MainActivity.mDetector.getGECoef()) / (1000);

            return (tempDose / countData);
        }

        public String ToStringSpc() {
            String ret = "";
            for (int i = 0; i < mSPC_Accumulattion.length; i++) {
                ret += ((int) mSPC_Accumulattion[i]) + ";";
            }

            return ret;
        }

        public double[] mSPC_Accumulattion;

        private Boolean accumulateSPC;
        private int countData;
        private int timeAccumulate;
    }
}
