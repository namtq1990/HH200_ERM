package android.HH100.Service;

public class Crystal_1_5x1_5 implements AlarmValue {

	public int Move_Forward = 2000;
	public int In_range = 10000;
	public int Move_back = 10000;
	public int Danger = 4450;

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
