package Debug;

public class Debug {
	public static boolean isSWDebug = false;

	public static boolean IsDebugMode = false;

	public static boolean BP = false; //190225 BP인지 체크 BP이면 런처설치 안함 설치되어있으면 삭제메세지 팝업

	public static boolean hw = false; //200218 hw test 용 true하면 gm count 표시

	public static boolean IsScaleSpectrumMode = false;
	
	public static boolean IsSendtoGCMode = true;
	
	public static boolean IsLauncherMode = true;

	public static int IsScaleSpectrumAcqtime = 60;

	public static boolean IsBattEnalbe = false;

	public static boolean IsSequenceCollect = true;

	public static boolean IsCalibrationMode = false;

	public static boolean IsVolumeDown = false;

	public static boolean IsSetSpectrumExcute = true;

	public static boolean IsAdminEnable = false;

	public static boolean IsGainStblizationSaveMode = false;

	public static boolean IsMailDefaultSetting = true;

	public static boolean IsIsotopeInvisibleViewFirstFiveSecond = false;

	public static boolean IsTextSaveMode = false;

	public static boolean IsUsbStopToast = false;

	public static Source SpecInfo = new K40();

	public Log_Setting mLog = new Log_Setting();

	public Toast mToast = new Toast();

	public FindpeakNDebug mFindpeakDebug = new FindpeakNDebug();

	public static Source getSpecInfo() {
		return SpecInfo;
	}

	public static void setSpecInfo(Source specInfo) {
		SpecInfo = specInfo;
	}
}
