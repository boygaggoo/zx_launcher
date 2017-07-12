package com.ichano.rvs.jni;

import com.ichano.rvs.internal.RvsLog;
import org.json.JSONException;
import org.json.JSONObject;

public class InternalCommand {
	public static final int INTERNAL_COMMAND_ID = 1000;
	public static final int NO_COMMAND_ID = -1;
	public static final int COMMAND_HEAD_LENGTH = 512;
	public static final int COMMAND_TYPE_STRING = 0;
	public static final int COMMAND_TYPE_BYTE = 1;

	public static boolean isInternalCommand(int commandId) {
		return (commandId <= 1000) && (-1 != commandId);
	}

	public static class CommonCommand {
		private static final String TAG = CommonCommand.class.getSimpleName();
		public static final String COMMAND_ID = "zy_command_id";
		public static final String COMMAND_TYPE = "zy_command_type";

		public static String getCommandHeadString(int commandID, int commandType) {
			String ret = "";
			JSONObject json = new JSONObject();
			try {
				json.put("zy_command_id", commandID);
				json.put("zy_command_type", commandType);
				ret = json.toString();
				RvsLog.i(InternalCommand.class, "CommonCommand : getCommandHeadString()",
						"command json string = " + ret);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return ret;
		}
	}

	public static class StreamerLocationCommand extends InternalCommand.CommonCommand {
		private static final String TAG = "StreamerLocationCommand";
		private static final String LATITUDE = "latitude";
		private static final String LONGITUDE = "longitude";

		public static class StreamerLocation {
			public double latitude;
			public double longitude;

			public StreamerLocation(boolean isSuccess, double latitude, double longitude) {
				this.latitude = latitude;
				this.longitude = longitude;
			}
		}

		public static class Get {
			public static final int GET_COMMAND_ID = 1;
			public static final String COMMAND = "GET_STREAMER_LOCATION";

			public static InternalCommand.StreamerLocationCommand.StreamerLocation getLocation(String command) {
				InternalCommand.StreamerLocationCommand.StreamerLocation loc = new InternalCommand.StreamerLocationCommand.StreamerLocation(
						false, 0.0D, 0.0D);
				try {
					JSONObject json = new JSONObject(command);
					loc.latitude = json.getDouble("latitude");
					loc.longitude = json.getDouble("longitude");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return loc;
			}
		}

		public static class Send {
			public static final int SEND_COMMAND_ID = 2;

			public static String getCommandString(double latitude, double longitude) {
				JSONObject json = new JSONObject();
				try {
					json.put("latitude", latitude);
					json.put("longitude", longitude);
					RvsLog.i(InternalCommand.class, "StreamerLocationCommand : Send : getCommandString()",
							"command json string = " + json.toString());
				} catch (JSONException e) {
					e.printStackTrace();
					RvsLog.e(InternalCommand.class, "StreamerLocationCommand : Send : getCommandString()",
							"get send commmand json error.");
				}
				return json.toString();
			}
		}
	}

	public static class StreamerFileCommand extends InternalCommand.CommonCommand {
		public static final int DATA_COMMAND_ID = 3;
		private static final String TAG = "StreamerFileCommand";
		public static final String FILE_TOTAL_PART = "file_total_part";
		public static final String FILE_PART_INDEX = "file_part_index";
		public static final String FILE_DISCRIPTION = "file_discription";

		public static class PartFileInfo {
			public int totalPart;
			public int index;
			public String fileDiscription = "";
			public byte[] fileData;
		}

		public static class Get {
			public static InternalCommand.StreamerFileCommand.PartFileInfo getFile(String commandHeader, byte[] data) {
				InternalCommand.StreamerFileCommand.PartFileInfo info = new InternalCommand.StreamerFileCommand.PartFileInfo();
				try {
					JSONObject json = new JSONObject(commandHeader);
					info.totalPart = json.getInt("file_total_part");
					info.index = json.getInt("file_part_index");
					info.fileDiscription = json.getString("file_discription");
					info.fileData = data;
					RvsLog.i(InternalCommand.class, "StreamerFileCommand : Get : getFile()",
							"get file: file discription = " + info.fileDiscription);
				} catch (JSONException e1) {
					RvsLog.e(InternalCommand.class, "StreamerFileCommand : Get : getFile()",
							"parse get file json command error.");
					e1.printStackTrace();
				}
				return info;
			}
		}

		public static class Send {
			public static String getSendCommandHeader(int totalPart, int partIndex, String fileDiscription) {
				JSONObject json = new JSONObject();
				try {
					json.put("zy_command_id", 3);
					json.put("zy_command_type", 1);
					json.put("file_total_part", totalPart);
					json.put("file_part_index", partIndex);
					json.put("file_discription", fileDiscription);
					RvsLog.i(InternalCommand.class, "StreamerFileCommand : Send : getSendCommandHeader()",
							"command json string = " + json.toString());
				} catch (JSONException e) {
					e.printStackTrace();
					RvsLog.e(InternalCommand.class, "StreamerFileCommand : Send : getSendCommandHeader()",
							"get send commmand json error.");
				}
				return json.toString();
			}
		}
	}
}
