package android.HH100.Service;

public class Crystal_4x4x16  implements AlarmValue{
	
	//170215 BNC Requests
	//  3x3Nai * 3
	public int Move_Forward = 6000;
	public int In_range = 30000;
	public int Move_back = 30000;
	public int Danger = 13350;
	


	@Override
	public int Get_Move_Forward() {
		// TODO Auto-generated method stub
		return Move_Forward;
	}
	@Override
	public int Get_In_Range() {
		// TODO Auto-generated method stub
		return In_range;
	}
	@Override
	public int Get_Move_Back() {
		// TODO Auto-generated method stub
		return Move_back;
	}
	@Override
	public int Get_Danger() {
		// TODO Auto-generated method stub
		return Danger;
	}

}
