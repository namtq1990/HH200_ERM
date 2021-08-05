package android.HH100;

import android.HH100.MainActivity.MainMsg;
import android.HH100.MainActivity.Signal;
import android.HH100.Service.MainBroadcastReceiver;
import android.HH100.Structure.GCData;
import android.HH100.Structure.NcLibrary;
import android.HH100.Structure.ReadDetectorData;
import android.HH100.Structure.Spectrum;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import Debug.Debug;

import static android.HH100.MainService.PACKET1_STANDARD;
import static android.HH100.MainService.PACKET2_STANDARD;
import static android.HH100.MainService.PACKET3_STANDARD;
import static android.HH100.MainService.PACKET4_STANDARD;
import static android.HH100.MainService.PACKET5_STANDARD;
import static android.HH100.MainService.PACKET6_STANDARD;
import static android.HH100.MainService.PACKET7_STANDARD;
import static android.HH100.MainService.PACKET8_STANDARD;
import static android.content.Context.USB_SERVICE;

public class MainUsbService {
	// 수정 내용 : 브로드캐스트 리시버 수행 후 자동 종료되도록 소스 추가
	private static final String TAG = "MainService";
	private static final String TAG_RECV_DATA = "MainService_recvData";
	public static final boolean D = MainMsg.D;

	public static final boolean USB_CONNECT_CHECK = false;

	private static final String NAME = "Kainac";
	TimerTask mUnstableMessageTask;
	boolean mUnstableMessageStop = true;
	private final Handler mSuperHandler;

	public static int mState;
	public static Context mContext;

	TimerTask mTask, mTask1, mSendGSTask;
	Timer mSendGSTimer;
	public static boolean ThreadCheck = true;
	public static final int STATE_NONE = 0; // �븘臾닿쾬�룄 �옟���엳吏��븡�쓬
	boolean mDuplicateRock = true;
	public static String name = "";
	int secondRealTime = 0; //181214
	double secondRealTime1 = 0; //181214
	int count123 = 0;

	enum UsbMode {

		STATE_USB_CONNECTED, STATE_USB_CONNECTING, STATE_USB_DISCONNECTED

	}

	public static final int NEUTRON_ACCUM_SEC = 5; //181129 10-> 5로 수정
	public static final int USB_CONNECTED = 6;

	public static final int USB_DISCONNECTED = 7234234;

	public static boolean FIRST_PACKET = false;

	public static int FillCps;

	public static int mBatteryCount = 50;

	public int aad = 0;
	public static int count = 0;
	public static int mTimeTaskcount = 0;

	int mTimeCheck = 0, mFirstConnectCheck = 0, mBadCount = 0;

	private Handler mHandler = new Handler() {

		@SuppressWarnings("deprecation")
		public void handleMessage(Message msg) {

			super.handleMessage(msg);

		}

	};

	// usb Connect

	public UsbConnect UsbConnect;



	public MainUsbService(Context context, Handler handler) {

		UsbMode hello = UsbMode.STATE_USB_CONNECTED;
		switch (hello) {
			case STATE_USB_CONNECTED:

				break;

			default:
				break;
		}
		mContext = context;
		mState = STATE_NONE;
		mSuperHandler = handler;

		UsbConnect = new UsbConnect();
		UsbConnect.StartReceiver();
	}

	private Timer sender;

	private synchronized void setState(int state) {

		if (D)
			Log.d(TAG, "setState() " + mState + " -> " + state);
		mState = state;
		mSuperHandler.obtainMessage(MainMsg.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
	}

	public synchronized int getState() {

		return mState;
	}

	public void usbStart() {

		if(UsbConnect !=null)
		{
			UsbConnect.UsbRefresh();
		}


	}

	public void write(byte[] datas) {

		if(UsbConnect != null) {
			UsbConnect.sendCommend(datas);
		}
	}

	public void SendU2AA() {

		if(UsbConnect != null)
		{
			UsbConnect.SendU2AA();
		}

	}

	public void SendGS() {
		if(UsbConnect != null)
		{
			UsbConnect.SendGS();
		}
	}

	public void SendU4AA() {

		if(UsbConnect != null) {
			UsbConnect.SendU4AA();
		}
	}

	public void usbStop() {

		if(UsbConnect != null) {
			UsbConnect.closeUsbAccessory();
		}
	}

	// Add1
	public void UsbBrodcastStop() {

		UsbConnect.BrodcastStop();

	}

	public class UsbConnect {

		int countSpc = 0;

		Object obj = new Object();

		public UsbConnect()
		{
			Timer mTimer = new Timer();
			mTimer.schedule(mTask, 3000, 1000);
		}

		ArrayList<Integer> IsThereNeutronBit = new ArrayList<Integer>();
		int[] totalcount = new int[1024];
		private UsbManager myUsbManager;
		private UsbAccessory myUsbAccessory;
		private ParcelFileDescriptor myParcelFileDescriptor;
		private FileInputStream myFileInputStream;
		private FileOutputStream myFileOutputStream;
		private static final String ACTION_USB_PERMISSION = "com.example.helloadk.usb_permission";
		private PendingIntent PendingIntent_UsbPermission;
		private ArrayList<Float> arrayFloat;
		private static final String TAG = "ExampleThread2";
		private static final int RQS_USB_PERMISSION = 0;

		private int arrayTotalCount = 0;
		private int numberOfByteRead = 0;
		public final int RECEIVE_UUGK = 13;
		public final int RECEIVE_Battary = 14;
		//180921 추가
		public final int RECEIVE_SPECKTRUM406 = 406; // UUU ? + 400 + FF
		public final int RECEIVE_SPECKTRUM301 = 301; // UUU? + 295 + FF

		//181005 serial추가
		public final int RECEIVE_UUSN = 23; // UUSN(4)	+MCU(3)+FPGA(4)+BOARD(6)+Serial(6byte)

		private int RECEIVE_UUGK2 = 13 + 512;
		public final int RECEIVE_SPECKTRUM = 3095;
		private int RECEIVE_SPECKTRUM2 = 3599;
		private int BATTARY = 14;
		int mCompareSec = 0;

		Thread thread;
		float MaxY;
		int maxChannel;
		// 핸들러 적용 부분

		int packetCnt = 0; //spectrum index 401byte * 7 이기떄문에 구분하기위해 설정
		boolean firstPacket = false;
		boolean isPacketError = false;

		int IsThereNeutron = 0;
		int GM = 0;
		int Neutron = 0;

		private Vector<int[]> mTemp_for_PacketData = new Vector<int[]>();
		byte[] m_CompletedRealData = new byte[3087];
		private Vector<Integer> mNeutron = new Vector<Integer>();
		private Vector<Integer> mGM = new Vector<Integer>();
		public Spectrum MS = new Spectrum();
		byte[] buffer, tailByte;
		byte[] spectrumBuffer = new byte[5000];
		byte[] spectrumBuffer1 = new byte[4];
		Runnable myRunnable = new Runnable() {

			@Override

			public void run() {

				int numberOfByteRead = 0;

				buffer = new byte[5000];

				int GcCheck_count = 0;

				while (ThreadCheck) {

					try {

						if (ThreadCheck == false) {
							break;

						}
						arrayTotalCount = myFileInputStream.read(buffer, 0, buffer.length);
						// NcLibrary.SaveText1("packet size : "+arrayTotalCount+"\n","test");
						//String u = buffer[0] + ","+ buffer[1]+ ","+ buffer[2] + ","+ buffer[3]+"\n";
						//NcLibrary.SaveText("arrayTotalCount : "+arrayTotalCount);
						switch (arrayTotalCount) {
							//181005 serial 추가
							case RECEIVE_UUSN:
								//uusn
								//NcLibrary.SaveText("RECEIVE_UUSN \n");
								if (buffer[2] == 'S' && buffer[3] == 'N') {
									ReadDetectorData mReadData = new ReadDetectorData();
									String strRead = new String(buffer);

									String mcu = "";
									String fpga = "";

									String hex = Integer.toHexString(buffer[4]);
									mcu += hex;

									hex = Integer.toHexString(buffer[5]);
									mcu += hex;

									hex = Integer.toHexString(buffer[6]);
									mcu += hex;
									mReadData.MCU = mcu;

									mcu = "";
									hex = Integer.toHexString(buffer[7]);
									mcu += hex;

									hex = Integer.toHexString(buffer[8]);
									mcu += hex;

									hex = Integer.toHexString(buffer[9]);
									mcu += hex;

									hex = Integer.toHexString(buffer[10]);
									mcu += hex;
									mReadData.FPGA = mcu;

									mcu = "";
									hex = Integer.toHexString(buffer[15]);
									mcu += hex;

									hex = Integer.toHexString(buffer[16]);
									mcu += hex;
									mReadData.board = strRead.substring(11, 15) + mcu;
									mReadData.serial = strRead.substring(16, 22);

								/*	mReadData.board = strRead.substring(11,15);
									mReadData.serial = strRead.substring(16,22);
*/
									mSuperHandler.obtainMessage(MainMsg.MESSAGE_READ_SERIAL_DATA, 0, 0, mReadData).sendToTarget();

								}

								break;

							//android 6.0 이후 os부터  (400byte+7) + FF 2byte+ 295byte 로 나눠서 전송
							case RECEIVE_SPECKTRUM406: //UUU ? + 200 + FF : 406
								if (buffer[3] == PACKET1_STANDARD) {

									if (packetCnt != 0) {
										isPacketError = true;
									} else {
										isPacketError = false;
									}
									//srcPos 3 = U U U  ?  numberOfByteRead-2 : ff tail

									System.arraycopy(buffer, 4, spectrumBuffer, 0, 400);
									packetCnt++;
								}

								if (buffer[3] == PACKET2_STANDARD) {

									if (packetCnt != 1) {
										isPacketError = true;
									}
									//srcPos 3 = U U U  ?  numberOfByteRead-2 : ff tail
									System.arraycopy(buffer, 4, spectrumBuffer, 400, 400);
									packetCnt++;
								}
								if (buffer[3] == PACKET3_STANDARD) {

									if (packetCnt != 2) {
										isPacketError = true;
									}
									//srcPos 3 = U U U  ?  numberOfByteRead-2 : ff tail
									System.arraycopy(buffer, 4, spectrumBuffer, 800, 400);
									packetCnt++;
								}
								if (buffer[3] == PACKET4_STANDARD) {

									if (packetCnt != 3) {
										isPacketError = true;
									}
									//srcPos 3 = U U U  ?  numberOfByteRead-2 : ff tail
									System.arraycopy(buffer, 4, spectrumBuffer, 1200, 400);
									packetCnt++;
								}
								if (buffer[3] == PACKET5_STANDARD) {

									if (packetCnt != 4) {
										isPacketError = true;
									}
									//srcPos 3 = U U U  ?  numberOfByteRead-2 : ff tail
									System.arraycopy(buffer, 4, spectrumBuffer, 1600, 400);
									packetCnt++;
								}

								if (buffer[3] == PACKET6_STANDARD) {

									if (packetCnt != 5) {
										isPacketError = true;
									}
									//srcPos 3 = U U U  ?  numberOfByteRead-2 : ff tail
									System.arraycopy(buffer, 4, spectrumBuffer, 2000, 400);
									packetCnt++;
								}

								if (buffer[3] == PACKET7_STANDARD) {

									if (packetCnt != 6) {
										isPacketError = true;
									}
									//srcPos 3 = U U U  ?  numberOfByteRead-2 : ff tail
									System.arraycopy(buffer, 4, spectrumBuffer, 2400, 400);
									packetCnt++;
								}

								break;

							case RECEIVE_SPECKTRUM301:

								synchronized (obj) {
//									NcLibrary.SaveText1("countSpc = 0", "CheckCPS");

									countSpc = 0;
								}

								if (buffer[3] == PACKET8_STANDARD) {
									if (packetCnt != 7) {
										isPacketError = true;
									}
									//srcPos 3 = U U U  ?  numberOfByteRead-2 : ff tail
									System.arraycopy(buffer, 4, spectrumBuffer, 2800, 295);
									packetCnt++;
								}

								//	System.arraycopy( buffer, 0, spectrumBuffer, 2800, numberOfByteRead-1 );
								firstPacket = false;
								packetCnt = 0;

								if (!isPacketError) {
									int[] pdata = new int[1024];
									ReadDetectorData mReadData = new ReadDetectorData();

									mReadData.pdata = byteToDecimal_j3(spectrumBuffer, arrayTotalCount);
									mReadData.GetAVGNeutron = GetAVGNeutron();

									if (IsThereNeutron == 0) {
										mReadData.IsThereNeutron = false;
									} else {
										mReadData.IsThereNeutron = true;
									}

									SetPacketData(mReadData.pdata, Neutron, GM);

									mReadData.Neutron = Neutron;
									mReadData.GM = GM;
									mReadData.isRealTime = true;
									mReadData.mRealTime = secondRealTime;
									mReadData.time = secondRealTime1;

									isPacketError = true;
									spectrumBuffer = new byte[5000];
									SystemClock.sleep( 50 );
									mSuperHandler.obtainMessage(MainMsg.MESSAGE_READ_DETECTOR_DATA_J3, 0, 0, mReadData).sendToTarget();

								} else {
									isPacketError = true;
									spectrumBuffer = new byte[5000];
								}

								break;
							case RECEIVE_UUGK:

								GCData mGCData = new GCData();
								int temp1 = buffer[4] & 0xff;
								int temp2 = buffer[5] & 0xff;
								int s = (char) temp1 * 0x100 + ((char) temp2);

								mGCData.GC = (char) temp1 * 0x100 + ((char) temp2);

								temp1 = buffer[6] & 0xff;
								temp2 = buffer[7] & 0xff;
								int s2 = (char) temp1 * 0x100 + ((char) temp2);

								mGCData.K40_Ch = (char) temp1 * 0x100 + ((char) temp2);

								int DetType2 = buffer[8] & 0xff;

								mGCData.DetType = buffer[8] & 0xff;

								temp1 = buffer[9] & 0xff;
								temp2 = buffer[10] & 0xff;

								mGCData.Cs137_Ch1 = (char) temp1 * 0x100 + ((char) temp2);

								temp1 = buffer[11] & 0xff;
								temp2 = buffer[12] & 0xff;

								mGCData.Cs137_Ch2 = (char) temp1 * 0x100 + ((char) temp2);

								mSuperHandler.obtainMessage(MainMsg.MESSAGE_USB_READ_GC, s, s2, mGCData).sendToTarget();

								break;

							case RECEIVE_SPECKTRUM:

								synchronized (obj) {
//									NcLibrary.SaveText1("countSpc = 0", "CheckCPS");

									countSpc = 0;
								}

								//NcLibrary.SaveText1("RECEIVE_SPECKTRUM" ,"CheckCPS");

								ReadDetectorData mReadData = new ReadDetectorData();

								mReadData.pdata = byteToDecimal(buffer, arrayTotalCount);
								mReadData.GetAVGNeutron = GetAVGNeutron();
								if (IsThereNeutron == 0) {
									mReadData.IsThereNeutron = false;
								} else {
									mReadData.IsThereNeutron = true;
								}

								SetPacketData(mReadData.pdata, Neutron, GM);

								mReadData.Neutron = Neutron;
								mReadData.GM = GM;
								mReadData.isRealTime = false;

								mSuperHandler.obtainMessage(MainMsg.MESSAGE_READ_DETECTOR_DATA, 0, 0, mReadData).sendToTarget();

								break;

							default:
								break;
						}

						if (buffer[3] == MainService.HARDWAREKEY_SHOTPRESS) {

							byte[] batt = new byte[1];

							String abde = String.format("%d", buffer[4] & 0xff);

							mSuperHandler.obtainMessage(MainActivity.INPUT_HARDWARE_KEY, MainActivity.HW_KEY_SHORT, 1,
									String.valueOf(abde)).sendToTarget();

							continue;
						} else if (buffer[3] == MainService.HARDWAREKEY_LONGPRESS) {

							byte[] batt = new byte[1];

							String abde = String.format("%d", buffer[4] & 0xff);

							mSuperHandler.obtainMessage(MainActivity.INPUT_HARDWARE_KEY, MainActivity.HW_KEY_LONG, 1,
									String.valueOf(abde)).sendToTarget();
							continue;
						}

					} catch (IOException e) {

						NcLibrary.Write_ExceptionLog(e);

						break;
					}
				}
			}
		};

		TimerTask mTask = new TimerTask() {
			@Override
			public void run() {
				synchronized (obj) {
//					NcLibrary.SaveText1("countSpc++", "CheckCPS");
					countSpc++;

					if(countSpc >=3)
					{
						ResetHW();

						countSpc = 0;
					}
				}
			}
		};

		private void ResetHW()
		{
			NcLibrary.SaveText1("Reset Hardware", "CheckCPS");

			MainActivity.SendU4AA();

			Timer TimerSendReset = new Timer();
			TimerSendReset.schedule(new TimerTask() {
				@Override
				public void run() {
					MainActivity.SendU2AA();
				}
			}, 200);

//			try {
//				Thread.sleep(500);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}

			//MainActivity.SendU2AA();

		}

		public void UsbRefresh() {

			if (myFileInputStream == null || myFileOutputStream == null) {

				UsbAccessory[] usbAccessoryList = myUsbManager.getAccessoryList();
				UsbAccessory usbAccessory = null;

				if (usbAccessoryList != null)
				{

					usbAccessory = usbAccessoryList[0];

					if (usbAccessory != null) {

						if (myUsbManager.hasPermission(usbAccessory)) {

							// already have permission

							OpenUsbAccessory(usbAccessory);

						} else {

							Toast.makeText(mContext,

									"ask for permission",

									Toast.LENGTH_LONG).show();

							synchronized (myUsbReceiver) {

								myUsbManager.requestPermission(usbAccessory,

										PendingIntent_UsbPermission);

							}

						}

					}

				}

			}

		}

		private void OpenUsbAccessory(UsbAccessory acc) {

			myParcelFileDescriptor = myUsbManager.openAccessory(acc);

			if (myParcelFileDescriptor != null) {

				myUsbAccessory = acc;

				FileDescriptor fileDescriptor = myParcelFileDescriptor.getFileDescriptor();

				myFileInputStream = new FileInputStream(fileDescriptor);

				myFileOutputStream = new FileOutputStream(fileDescriptor);

				mFirstConnectCheck = 0;
				ThreadCheck = true;
				mDuplicateRock = false;

				try {
					if (mUnstableMessageTask != null) {
						mUnstableMessageStop = true;
						mUnstableMessageTask.cancel();
					}
				} catch (NullPointerException e) {
					NcLibrary.Write_ExceptionLog(e);
				}

				thread = new Thread(myRunnable);

				thread.start();

				SendGS();
				Timer mTimer;
				mSendGSTask = new TimerTask() {

					@Override
					public void run() {

						SendGS();
						mTimeTaskcount++;

					}
				};

				mSendGSTimer = new Timer();
				mSendGSTimer.schedule(mSendGSTask, 3000, 5000);

			}

		}

		private BroadcastReceiver myUsbReceiver = new BroadcastReceiver() {

			@Override

			public void onReceive(Context context, Intent intent) {

				String action = intent.getAction();

				if (Intent.ACTION_POWER_DISCONNECTED.equals(intent.getAction())) {

					SendU4AA();

					closeUsbAccessory();

					BrodcastStop();

					mSuperHandler.obtainMessage(MainMsg.MESSAGE_SHUTDOWN).sendToTarget();

				} else if (action.equals(UsbManager.ACTION_USB_ACCESSORY_DETACHED)) {

					UsbAccessory[] usbAccessory = myUsbManager.getAccessoryList();
					//UsbAccessory usbAccessory = UsbManager.getAccessory(intent);

					if (usbAccessory != null && usbAccessory.equals(myUsbAccessory)) {

						closeUsbAccessory();

						BrodcastStop();

						mSuperHandler.obtainMessage(USB_DISCONNECTED).sendToTarget();

					}

				} else if (Intent.ACTION_POWER_CONNECTED.equals(intent.getAction()))
				{

				} else if (action.equals(ACTION_USB_PERMISSION)) {

					synchronized (this) {

						//UsbAccessory usbAccessory = UsbManager.getAccessory(intent);
						UsbAccessory[] usbAccessory = myUsbManager.getAccessoryList();

						if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {

							OpenUsbAccessory(usbAccessory[0]);

						} else {

						}

						if (action.equals(UsbManager.ACTION_USB_ACCESSORY_DETACHED)) {

						}

					}

				}
			}

		};

		private BroadcastReceiver myUsbPermissionReceiver = new BroadcastReceiver() {

			@Override

			public void onReceive(Context context, Intent intent) {

				String action = intent.getAction();

				if (action.equals(ACTION_USB_PERMISSION)) {

					synchronized (this) {

						//UsbAccessory usbAccessory = UsbManager.getAccessory(intent);
						UsbAccessory[] usbAccessory = myUsbManager.getAccessoryList();

						if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {

							//OpenUsbAccessory(usbAccessory);
							OpenUsbAccessory(usbAccessory[0]);

						} else {

						}

						if (action.equals(UsbManager.ACTION_USB_ACCESSORY_DETACHED)) {

						}

					}

				}

			}

		};

		private void closeUsbAccessory() {

			Intent send_gs3 = new Intent(MainBroadcastReceiver.MSG_USB_DISCONNECT);

			LocalBroadcastManager.getInstance(mContext).sendBroadcast(send_gs3);

			Intent send_gs = new Intent(MainBroadcastReceiver.UPDATE_NEUTRONCPS);
			send_gs.putExtra(MainBroadcastReceiver.UPDATE_NEUTRONCPS_TEXT, -1);
			LocalBroadcastManager.getInstance(mContext).sendBroadcast(send_gs);

			// Update_StatusBar();

			// --===--
			Intent intent = new Intent(MainBroadcastReceiver.MSG_DISCONNECTED_BLUETOOTH);
			LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

			try {
				if (myFileInputStream != null) {
					myFileInputStream.close();
				}
			} catch (NullPointerException e1) {
				//NcLibrary.Write_ExceptionLog(e1);
			} catch (IOException e) {
				//NcLibrary.Write_ExceptionLog(e);
			}
			if (myParcelFileDescriptor != null) {

				try {

					myFileInputStream = null;

					myFileOutputStream = null;

					myParcelFileDescriptor.close();

				} catch (NullPointerException e) {

					NcLibrary.Write_ExceptionLog(e);

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			myParcelFileDescriptor = null;

			myUsbAccessory = null;
			ThreadCheck = false;
			try {
				if (mTask != null) {
					mTask.cancel();
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				//	NcLibrary.Write_ExceptionLog(e);
				}
				if (thread != null) {
					thread.interrupt();
				}
			} catch (NullPointerException e) {
				//NcLibrary.Write_ExceptionLog(e);
			}

			mSuperHandler.obtainMessage(USB_DISCONNECTED).sendToTarget();

		}

		public int[] byteToDecimal(byte[] buffer, int arrayTotalCount) {
			try {

				for (int i = 0; i < 1024; i++) {
					int a = 0, b = 0, c = 0, sum = 0;
					a = buffer[3 * i] & 0xff;
					b = buffer[3 * i + 1] & 0xff;
					c = buffer[3 * i + 2] & 0xff;

					totalcount[i] = ((char) a * 0x10000 + ((char) b * 0x100 + ((char) c)));

					//191118 수정 1500000->30000
					if (totalcount[i] > 30000) {
						totalcount[i] = 0;
					}

				}

				int a = 0, b = 0, c = 0;

				a = buffer[3075] & 0xff;
				b = buffer[3076] & 0xff;
				c = buffer[3077] & 0xff;

				GM = ((char) a * 0x10000 + ((char) b * 0x100 + ((char) c)));

				a = buffer[3078] & 0xff;
				b = buffer[3079] & 0xff;
				c = buffer[3080] & 0xff;

				Neutron = ((char) a * 0x10000 + ((char) b * 0x100 + ((char) c)));

				a = buffer[3081] & 0xff;
				b = buffer[3082] & 0xff;
				c = buffer[3083] & 0xff;


				if (MainActivity.mDetector.mHW_GC > 1024) {

					IsThereNeutronBit = byteToIntergerArray(buffer[3081]);

					IsThereNeutron = (int) Byte.parseByte("0000000" + Integer.toString(IsThereNeutronBit.get(0)), 2);

					a = (int) Byte.parseByte("0" + Integer.toString(IsThereNeutronBit.get(1))
							+ Integer.toString(IsThereNeutronBit.get(2)) + Integer.toString(IsThereNeutronBit.get(3))
							+ Integer.toString(IsThereNeutronBit.get(4)) + Integer.toString(IsThereNeutronBit.get(5))
							+ Integer.toString(IsThereNeutronBit.get(6)) + Integer.toString(IsThereNeutronBit.get(7)), 2);

					int FillCps;

					FillCps = ((char) a * 0x10000 + ((char) b * 0x100 + ((char) c)));

					MS.SetFillCps(FillCps);
				} else {
					IsThereNeutron = ((char) a * 0x10000 + ((char) b * 0x100 + ((char) c)));
				}


				int abdeInt = 0;
				int abdeInt1 = 0;
				int Battery = 0;

				abdeInt = buffer[3088] & 0xff;
				abdeInt1 = buffer[3089] & 0xff;
				Battery = ((char) abdeInt * 0x100 + ((char) abdeInt1));

				mBatteryCount++;

				if (mBatteryCount == 50 || mBatteryCount > 9) {

					mBatteryCount = 0;
					mSuperHandler.obtainMessage(MainMsg.MESSAGE_READ_BATTERY, AcumulBattery_and_getNowPercent(Battery), -1,
							AcumulBattery_and_getNowPercent(Battery)).sendToTarget();
				}
			} catch (NumberFormatException e) {
				NcLibrary.Write_ExceptionLog(e);

			}

			return totalcount;
		}

		public int[] byteToDecimal_j3(byte[] buffer, int arrayTotalCount) {
			try {

				for (int i = 0; i < 1024; i++) {
					int a = 0, b = 0, c = 0, sum = 0;
					a = buffer[3 * i] & 0xff;
					b = buffer[3 * i + 1] & 0xff;
					c = buffer[3 * i + 2] & 0xff;

					totalcount[i] = ((char) a * 0x10000 + ((char) b * 0x100 + ((char) c)));

					if (totalcount[i] > 1500000) {
						totalcount[i] = 0;
					}

				}

				int a = 0, b = 0, c = 0;

				a = buffer[3075] & 0xff;
				b = buffer[3076] & 0xff;
				c = buffer[3077] & 0xff;

				GM = ((char) a * 0x10000 + ((char) b * 0x100 + ((char) c)));

				a = buffer[3078] & 0xff;
				b = buffer[3079] & 0xff;
				c = buffer[3080] & 0xff;

				Neutron = ((char) a * 0x10000 + ((char) b * 0x100 + ((char) c)));

				a = buffer[3081] & 0xff;
				b = buffer[3082] & 0xff;
				c = buffer[3083] & 0xff;


				if (MainActivity.mDetector.mHW_GC > 1024) {

				//	NcLibrary.SaveText1("FillCps \n","test");
					IsThereNeutronBit = byteToIntergerArray(buffer[3081]);

					IsThereNeutron = (int) Byte.parseByte("0000000" + Integer.toString(IsThereNeutronBit.get(0)), 2);

					a = (int) Byte.parseByte("0" + Integer.toString(IsThereNeutronBit.get(1))
							+ Integer.toString(IsThereNeutronBit.get(2)) + Integer.toString(IsThereNeutronBit.get(3))
							+ Integer.toString(IsThereNeutronBit.get(4)) + Integer.toString(IsThereNeutronBit.get(5))
							+ Integer.toString(IsThereNeutronBit.get(6)) + Integer.toString(IsThereNeutronBit.get(7)), 2);

					int FillCps;

					FillCps = ((char) a * 0x10000 + ((char) b * 0x100 + ((char) c)));

		//			NcLibrary.SaveText1("FillCps"+FillCps +"\n","test");
					MS.SetFillCps(FillCps);

				} else {
					IsThereNeutron = ((char) a * 0x10000 + ((char) b * 0x100 + ((char) c)));
				}


				int abdeInt = 0;
				int abdeInt1 = 0;
				int Battery = 0;

				abdeInt = buffer[3088] & 0xff;
				abdeInt1 = buffer[3089] & 0xff;
				Battery = ((char) abdeInt * 0x100 + ((char) abdeInt1));

				int time1 = buffer[3090] & 0xff;
				int time2 = buffer[3091] & 0xff;
				secondRealTime1 = ((char) time1 * 0x100 + ((char) time2));
				secondRealTime = (int) Math.round(((secondRealTime1 / 1000) * 10) / 10);

				///NcLibrary.SaveText1("realTime : "+secondRealTime+"\n", "test");

				//190111
				b = buffer[3092] & 0xff;
				c = buffer[3093] & 0xff;
				int hv = ((char) b * 0x100 + ((char) c));
				//NcLibrary.SaveText1("hv : "+hv+"  bat : "+ Battery+"\n", "test");

				mBatteryCount++;

				if (mBatteryCount == 50 || mBatteryCount > 9) {

					mBatteryCount = 0;
					mSuperHandler.obtainMessage(MainMsg.MESSAGE_READ_BATTERY, AcumulBattery_and_getNowPercent(Battery), -1,
							AcumulBattery_and_getNowPercent(Battery)).sendToTarget();
				}
			} catch (NumberFormatException e) {
				NcLibrary.Write_ExceptionLog(e);

			}

			return totalcount;
		}


		private void SetPacketData(int[] ChArray, int Neutron, int GM) {

			int[] temp = ChArray;
			mTemp_for_PacketData.add(temp);
			while (true) {
				if (mTemp_for_PacketData.size() > 3)
					mTemp_for_PacketData.remove(0);
				else
					break;
			}

			int Neu = Neutron;
			//테스트 NcLibrary.SaveText1("mReadData.GetAVGNeutron : "+Neu+"\n", "Neutron");

			mNeutron.add(Neu);
			while (true) {
				if (mNeutron.size() > NEUTRON_ACCUM_SEC) //NEUTRON_ACCUM_SEC 10->5로 수정
					//테스트	if (mNeutron.size() > 5)
					mNeutron.remove(0);
				else
					break;
			}

			mGM.add(GM);
			while (true) {
				if (mGM.size() > 20)
					mGM.remove(0);
				else
					break;
			}
		}

		private int GetAvgGmValue() {

			double Avg = 0;
			for (int i = 0; i < mGM.size(); i++) {
				Avg += mGM.get(i);
			}
			Avg = Avg / mGM.size();
			return NcLibrary.Auto_floor(Avg);

		}

		private void sendCommend(byte[] GetGC) {

			if (myFileOutputStream != null) {
				try {
					myFileOutputStream.write(GetGC);

				} catch (IOException e) {
					NcLibrary.Write_ExceptionLog(e);

				}

			}

		}

		private double GetAVGNeutron() {

			double Avg = 0;
			for (int i = 0; i < mNeutron.size(); i++) {
				Avg += mNeutron.get(i);
			}
			Avg = Avg / NEUTRON_ACCUM_SEC;
			return Avg;
		}

		private synchronized void setState(int state) {

			if (D)
				Log.d(TAG, "setState() " + mState + " -> " + state);
			mState = state;
			mSuperHandler.obtainMessage(MainMsg.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
		}

		private int AcumulBattery_and_getNowPercent(int BattValue) {

			/* for only HH200
			int BATTERY_MIN = 830;
			int BATTERY_MAX = 960;// 985;

			double result = 0;
			double tt = BattValue;

			if (BattValue < BATTERY_MIN)
				return 0;

			tt -= BATTERY_MIN;
			tt = (tt / (BATTERY_MAX - BATTERY_MIN)) * 100.0;
			if (tt > 100)
				tt = 100;
			else if (tt < 0)
				tt = 0;

			return NcLibrary.Auto_floor(tt);
			*/



			// for HH200 and HH100
			int BATTERY_MIN = 0;
			int BATTERY_MAX = 0;// 985;
			double result = 0;
			double tt = BattValue;
			String str="Bat,"+BattValue+",GC,"+ MainActivity.mDetector.mHW_GC+"/n";
			//NcLibrary.SaveText("\n"+str);
			//NcLibrary.SaveText("battery.txt",str,true);



			try
			{

				if (MainActivity.mDetector.mHW_GC > 2014)
				{
					BATTERY_MIN = 830;
					BATTERY_MAX = 960;// 985;
					result = 0;

					if (BattValue < BATTERY_MIN)
						return 0;

					tt -= BATTERY_MIN;
					tt = (tt / (BATTERY_MAX - BATTERY_MIN)) * 100.0;
					if (tt > 100)
						tt = 100;
					else if (tt < 0)
						tt = 0;
				} else {
					/////////////////////////////
					// HH100
					BATTERY_MIN = 810;

					BATTERY_MAX = 968;// 985;
					result = 0;

					if (BattValue < BATTERY_MIN)
						return 0;

					tt -= BATTERY_MIN;
					tt = (tt / (BATTERY_MAX - BATTERY_MIN)) * 100.0;
					if (tt > 100)
						tt = 100;
					else if (tt < 0)
						tt = 0;
				}
			}catch(Exception e)
			{
				NcLibrary.Write_ExceptionLog(e);
			}


			return NcLibrary.Auto_floor(tt);
		}

		public void StartReceiver() {

			//myUsbManager = UsbManager.getInstance(mContext);
			myUsbManager = (UsbManager) mContext.getSystemService(USB_SERVICE);

			IntentFilter intentFilter = new IntentFilter();

			intentFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);

			intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);

			intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);

			intentFilter.addAction(ACTION_USB_PERMISSION);

			intentFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);

			mContext.registerReceiver(myUsbReceiver, intentFilter);

			Intent intent_UsbPermission = new Intent(ACTION_USB_PERMISSION);

			PendingIntent_UsbPermission = PendingIntent.getBroadcast(

					mContext,

					RQS_USB_PERMISSION, intent_UsbPermission,

					0);

		}

		private void ShutDown() {

			try {

				Runtime.getRuntime().exec("adb shell");
				Runtime.getRuntime().exec("/system/bin/reboot -p");

			} catch (Exception ex) {
				NcLibrary.Write_ExceptionLog(ex);
			}
		}

		private void SendU4AA() {

			if (myFileOutputStream != null) {

				try {

					myFileOutputStream.write(Signal.MESSAGE_END_HW);

				} catch (IOException e) {

					NcLibrary.Write_ExceptionLog(e);

				}

			}

		}

		private void SendU2AA() {

			if (myFileOutputStream != null) {

				try {

					myFileOutputStream.write(Signal.MESSAGE_START_HW);

				} catch (IOException e) {

					NcLibrary.Write_ExceptionLog(e);

				}

			}

		}

		private void SendGS() {

			//NcLibrary.SaveText("SendGS");
			if (myFileOutputStream != null) {

				try {

					myFileOutputStream.write(Signal.MESSAGE_GS_HW);
					//190110 myFileOutputStream.write(Signal.MESSAGE_SN_HW);

					try
					{
						Thread.sleep(200);
						myFileOutputStream.write(Signal.MESSAGE_GQ_HW);
					}catch(Exception e)
					{

					}
				} catch (IOException e) {

					NcLibrary.Write_ExceptionLog(e);

				}

			}

			if (mTimeTaskcount > 3)
			{
				try {
					//myFileOutputStream.write(Signal.MESSAGE_SN_HW);
					myFileOutputStream.write(Signal.MESSAGE_GQ_HW);
					if (mSendGSTask != null) {
						mSendGSTask.cancel();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}


			}

		}

		// Add2
		public void BrodcastStop() {

			LocalBroadcastManager.getInstance(mContext).unregisterReceiver(myUsbReceiver);

			Debug mDebug = new Debug();

			if (mDebug.IsDebugMode) {

				Toast.makeText(mContext, "BrodcastStop 동작됨", Toast.LENGTH_LONG).show();

			}

			try {
				if (mTask != null) {
					mTask.cancel();
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					NcLibrary.Write_ExceptionLog(e);
				}
				if (thread != null) {
					thread.interrupt();
				}
			} catch (NullPointerException e) {
				NcLibrary.Write_ExceptionLog(e);
			}

		}

		public ArrayList<Integer> byteToIntergerArray(byte b) {

			ArrayList<Integer> BitArray = new ArrayList<Integer>();
			byte[] masks = { -128, 64, 32, 16, 8, 4, 2, 1 };
			// StringBuilder builder = new StringBuilder();
			for (byte m : masks) {
				if ((b & m) == m) {
					// builder.append('1');
					BitArray.add(1);
				} else {
					// builder.append('0');
					BitArray.add(0);
				}
			}
			return BitArray;
		}
	}

}
