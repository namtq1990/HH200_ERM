package android.HH100.Service;

import android.content.Context;

public class MainBroadcastReceiver extends android.content.BroadcastReceiver {

	public interface HW_KEY_ATT {

		int LONG = 0;
		int SHORT = 1;
		int DOUBLE = 2;

	}

	public interface HW_KEY_ACTION {

		int LEFT = 10;
		int RIGHT = 11;
		int UP = 12;
		int DOWN = 13;
		int BACK = 14;

	}
	
	public interface MainBroadcastReceiverValue{
		public String APPLICATION = "android.HH100.broadcast";
		public String MSG_RECV_SPECTRUM = APPLICATION + ".toast.spc";
	};

	public static final String APPLICATION = "android.HH100.broadcast";
	public static final String INTENT_SENDER = APPLICATION + ".Intent.String.sender";
	public static final String BROADCAST_TOAST_GLOBAL = APPLICATION + ".toast.global";
	public static final String BROADCAST_TOAST_LOCAL = APPLICATION + ".toast.local";
	// -------------------------
	public static final String MSG_RECV_SPECTRUM = APPLICATION + ".toast.spc";
	public static final String MSG_RECV_DOSERATE = APPLICATION + ".toast.doserate";
	public static final String MSG_RECV_NEUTRON = APPLICATION + ".toast.neutron";
	public static final String MSG_RECV_EVENT_SPECTRUM = APPLICATION + ".toast.spc_event";
	public static final String MSG_REMEASURE_BG = APPLICATION + ".toast.measured_bg";
	public static final String MSG_EN_CALIBRATION = APPLICATION + ".toast.recalibration";
	public static final String MSG_GAIN_STABILIZATION = APPLICATION + ".toast.gain_stb";
	public static final String MSG_POWER_DISCONNECT = APPLICATION + ".toast.power.disconnect";
	
	public static final String MSG_USB_DISCONNECT = APPLICATION + ".toast.usb.disconnect";

	public static final String MSG_EVENT = APPLICATION + ".toast.event";
	public static final String MSG_HEALTH_EVENT = APPLICATION + ".toast.health_event";

	public static final String MSG_MANUAL_ID = APPLICATION + ".toast.manualID";
	public static final String MSG_SEQUENCE_MODE = APPLICATION + ".toast.sequenceMode";

	public static final String MSG_DISCONNECTED_BLUETOOTH = APPLICATION + ".toast.disconnected_bluetooth";

	public static final String MSG_HEALTH_EVENT_IMG = APPLICATION + ".toast.health_event_img";

	// -------------------------
	public static final String DATA_DETECETOR = APPLICATION + ".data.det";
	public static final String DATA_EVENT = APPLICATION + ".data.event";
	public static final String DATA_IS_HEALTH_ALARM = APPLICATION + ".data.is_health_alarm";
	public static final String DATA_EVENT_STATUS = APPLICATION + ".data.event_status";
	public static final String DATA_EVENT_STATUS_IMG = APPLICATION + ".data.event_status_img";

	public static final String DATA_IS_EVENT = APPLICATION + ".data.is_event";
	public static final String DATA_SPECTRUM = APPLICATION + ".data.spc";
	
	
	public static final String CRYSTAL_TYPE = APPLICATION + ".crystal.type";
	
	
	
	public static final String DATA_NEUTRON = APPLICATION + ".data.neutron";
	public static final String DATA_GM = APPLICATION + ".data.gm";
	public static final String DATA_SOURCE_ID = APPLICATION + ".data.source_id";
	public static final String DATA_DATE = APPLICATION + ".data.date";

	public static final String DATA_COEFFCIENTS = APPLICATION + ".data.coeff";
	public static final String DATA_CALIBRATION_PEAKS = APPLICATION + ".data.calib_peaks";
	public static final String DATA_MANUAL_ID_STASTUS = APPLICATION + ".data.manual_id_status";
	public static final String DATA_SEQUENCE_STASTUS = APPLICATION + ".data.sequence_status";

	public static final String DATA_GC_GAIN = APPLICATION + ".data.gc_gain";
	public static final String DATA_K40_PEAK = APPLICATION + ".data.k40peak";

	public static final String DATA_GS_STATUS = APPLICATION + ".data.gs_status";

	public static final String DATA_HW_KEY_ATT = APPLICATION + ".data.key_att";
	public static final String DATA_HW_KEY_ACTION = APPLICATION + ".data.key_action";

	// --------------------------
	public static final int DATA_START = 5421477;
	public static final int DATA_END = 5421478;
	public static final int DATA_CANCEL = 5421479;

	// Add hongjae.lee

	public static final String MAIN_DATA_SEND = APPLICATION + ".toast.maintab.data_send";

	public static final String DATA_PAIRED = APPLICATION + ".toast.maintab.data_paired";
	public static final String DATA_LIBRARY = APPLICATION + ".toast.maintab.data_library";
	public static final String DATA_ALARM = APPLICATION + ".toast.maintab.data_alarm";
	public static final String DATA_LOGIN = APPLICATION + ".toast.maintab.data_login";

	public static final String MAIN_DATA_SEND1 = APPLICATION + ".toast.maintab.data_send1";

	public static final String DATA_BATTERY = APPLICATION + ".toast.maintab.data_battery";

	public static final String MSG_SOURCE_ID_RESULT_CANCEL = APPLICATION + ".msg.source_id_result_cancel";
	
	public static final String MSG_SOURCE_ID_RUNNING_CANCEL = APPLICATION + ".msg.source_id_running_cancel";

	public static final String START_ID_MODE = APPLICATION + ".toast.maintab.setup.mode";

	public static final String START_SETUP_MODE = APPLICATION + ".toast.maintab.start.setup.mode";

	public static final String M_GAMMAGUAGE_PANEL = APPLICATION + ".toast.maintab.start.m_gammaguage_panel";

	public static final String M_GAMMAGUAGE_PANEL_YN = APPLICATION + ".toast.maintab.start.m_gammaguage_panel_yn";

	public static final String UPDATE_NEUTRONCPS = APPLICATION + ".toast.maintab.Update_NeutronCPS";

	public static final String UPDATE_NEUTRONCPS_TEXT = APPLICATION + ".toast.maintab.Update_NeutronCPS_Text";

	public static final String MSG_SET_TOSVUNIT = APPLICATION + ".toast.set_tosvunit";

	public static final String DATA_SET_TOSVUNIT = APPLICATION + ".toast.data_set_tosvunit";

	public static final String MSG_TAB_BACKGROUND = APPLICATION + ".toast.msg.tab.background";

	public static final String MSG_REFRESH_ACTVITY = APPLICATION + ".toast.msg.background.cancel";

	public static final String MSG_BACKGROUND_CANCEL = APPLICATION + ".toast.msg.background.cancel";

	public static final String MSG_CALIBRATION_CANCEL = APPLICATION + ".toast.msg.calibration.cancel";

	public static final String MSG_SOURCE_ID_HW_BACK = APPLICATION + ".toast.msg.sourceid_hw_back";

	public static final String MSG_SOURCE_ID_RUNNING_START = APPLICATION + ".toast.msg.source_id_running_start";

	public static final String MSG_SEQUENTAL_MODE_RUNNING_START = APPLICATION
			+ ".toast.msg.sequential_mode_running_start";

	public static final String MSG_SPEC_VIEWFILPPER = APPLICATION + ".toast.msg.sourceid.viewfilpper";

	public static final String MSG_SOURCE_ID_TIMEUP = APPLICATION + ".toast.msg.sourceid.timeup";

	public static final String MSG_SOURCE_ID_TIMEDOWN = APPLICATION + ".toast.msg.sourceid.timedown";

	public static final String MSG_START_ID_MODE = APPLICATION + ".toast.start_id_mode";

	public static final String MSG_SOURCE_ID_RESULT = APPLICATION + ".msg.source_id_result";

	public static final String MSG_SOURCE_ID_HW_RIGHT = APPLICATION + ".msg.source_id_hw_right";

	public static final String MSG_SOURCE_ID_HW_LEFT = APPLICATION + ".msg.source_id_hw_left";

	public static final String MSG_SOURCE_ID_HW_UP = APPLICATION + ".msg.source_id_hw_up";

	public static final String MSG_SOURCE_ID_HW_DOWN = APPLICATION + ".msg.source_id_hw_down";

	public static final String MSG_SOURCE_ID_HW_ENTER = APPLICATION + ".msg.source_id_hw_enter";

	public static final String MSG_TAB_SIZE_MODIFY_FINISH = APPLICATION + ".msg.tab.size.modify";

	public static final String MSG_START_BACKGROUND = APPLICATION + ".toast.msg.start.background";

	public static final String MSG_START_SOURCE_ID = APPLICATION + ".toast.msg.start.source.id";

	public static final String MSG_START_CALIBRATION = APPLICATION + ".toast.msg.start.calibration";

	public static final String MSG_TAB_EN_CALIBRATION = APPLICATION + ".toast.msg.tab.en.calibration";

	public static final String MSG_TAB_SOURCE_ID = APPLICATION + ".toast.msg.tab.source.id";

	public static final String MSG_HW_START_SPECTRUM = APPLICATION + ".toast.msg.hw.start.spectrum";

	public static final String MSG_HW_STOP_SPECTRUM = APPLICATION + ".toast.msg.hw.stop.spectrum";

	public static final String MSG_SETTIONG_TAB_BACKGROUND = APPLICATION + ".toast.msg.settiong.tab.background";

	public static final String MSG_SETTION_TAB_CALIBRATION = APPLICATION + ".toast.msg.settiong.tab.calibration";

	public static final String MSG_BLUETOOTH_DISCONNECT = APPLICATION + ".toast.msg.bluetooth.disconnect";

	public static final String MSG_BLUETOOTH_CONNECTED = APPLICATION + ".toast.msg.bluetooth.connected";

	public static final String MSG_TAB_ENABLE = APPLICATION + ".toast.msg.tab.enable";

	public static final String MSG_TAB_DISABLE = APPLICATION + ".toast.msg.tab.disable";

	public static final String MSG_USB_CONNECTED = APPLICATION + ".toast.msg.usb.connected";

	public static final String MSG_HW_KEY = APPLICATION + ".toast.msg.hw_key";

	public static final String MSG_HW_KEY_ENTER = APPLICATION + ".toast.msg.hw_key_enter";

	public static final String MSG_HW_KEY_UP = APPLICATION + ".toast.msg.hw_key_up";

	public static final String MSG_HW_KEY_DOWN = APPLICATION + ".toast.msg.hw_key_down";

	public static final String MSG_HW_KEY_LEFT = APPLICATION + ".toast.msg.hw_key_left";

	public static final String MSG_HW_KEY_RIGHT = APPLICATION + ".toast.msg.hw_key_right";

	public static final String MSG_HW_KEY_BACK = APPLICATION + ".toast.msg.hw_key_back";

	public static final String MSG_MOVE_MAIN_NEXTFLIPPER = APPLICATION + ".toast.msg.move.main.nextflipper";

	public static final String MSG_MOVE_MAIN_PREFLIPPER = APPLICATION + ".toast.msg.move.main.proflipper";

	public static final String MSG_RECV_USB_NEUTRON = APPLICATION + ".toast.recv.neutron.usb";

	public static final String MSG_NOT_RECV_USB_NEUTRON = APPLICATION + ".toast.not.recv.neutron.usb";

	public static final String MSG_STOP_HEALTH_ALARM = APPLICATION + ".toast.stop.health.alarm";
	
	public static final String MSG_FIXED_GC_SEND = APPLICATION + ".toast.msg.fixed.gc.send";

	public static final String MSG_FIXED_HWCALI_SEND = APPLICATION + ".toast.msg.hwcali.send";

	@Override
	public void onReceive(Context context, android.content.Intent intent) {
		// Action �젙蹂대�� 媛�吏�怨� �삩�떎.
		String action = intent.getAction();
		// Sender �젙蹂대�� 媛�吏�怨� �삩�떎.
		String sender = intent.getStringExtra(INTENT_SENDER);
		// Action �뿉 �뵲�씪 �옉�뾽�쓣 �닔�뻾�븳�떎.
		if (action.equals(BROADCAST_TOAST_LOCAL) || action.equals(BROADCAST_TOAST_GLOBAL)) {
			// 留뚯빟 湲곗〈 Toast 媛� �엳�떎硫� 痍⑥냼�븯怨�.

		}
	}
}