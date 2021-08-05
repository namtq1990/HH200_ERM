package android.HH100.Structure;

public class ReadDetectorData {

	public int[] pdata = new int[1024];
	public int Neutron = 0;
	public boolean IsThereNeutron = true;
	public int GM = 0;
	public double GetAVGNeutron = 0;
	/*
	 *181005 inseon.ahn
	 *UUSN(4)	+MCU(3)+FPGA(4)+BOARD(6)+Serial(6byte)
	 */
	public String MCU = "";
	public String FPGA = "";
	public String board = "";
	public String serial = "";

	//181214
	public int mRealTime= -1;
	public double time= -1;
	//시간정보 있는지 없는지 체크
	public boolean isRealTime = false;

}
