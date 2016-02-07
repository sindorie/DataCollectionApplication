package tracker.springversion1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import tracker.UI.activity.ExperimentActivity;
import tracker.UI.activity.FrameActivity;
import tracker.utility.Utility;
import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class TaskManager {

	public static final int TASKPANEL = 0;
	public static final int CONTACT = 1;
	public static final int PASS = 0;
	public static final int DATABASE = 1;
	public static final int RIGHTDRAWER = 2;
	public static final int TASPANEL = 3;
	public static final int ADDITIONALINFO = 4;
	Instruction current;
	private String additionalInformation = null;
	MobileStudyApplication app;
	// temporary
	private String[] parts;
	private String table;
	private String[] columns;
	private String selection;
	private String[] args;
	private int type;

	public TaskManager(MobileStudyApplication app) {
		this.app = app;
		int cuurent_inst_id = app.getUserInfo().inst_id;
		Log.v("mark", "TaskManager created with user taskid:" + cuurent_inst_id);
		this.app.setInstructionCount(getTotalCount());
		nextInstruction(false, cuurent_inst_id);
	}

	private boolean isValidInstruction() {
		// TODO
		return true;
	}

	public Instruction getCurrentInstruction() {
		return this.current;
	}
	
	private int getTotalCount() {
		SQLiteDatabase db = app.getDB();
		Cursor cur = db.query(Common.Tables.INSTRUCTION, null, null, null, null, null, null);
		int result = cur.getCount();
		cur.close();
		return result;
	}

	public void nextInstruction(boolean isSucessful, int intented_nextId) {
		SQLiteDatabase db = app.getDB();
		db.beginTransaction();
		// deal with current Instruction
		if (current != null) {
			markCurrentInstructionState(db, current._id, isSucessful ? Instruction.COMPLETED : Instruction.SKIPPED);
			intented_nextId = current._id + 1;
		}
		Instruction nextInstruction = fetchNextInstruction(db, intented_nextId);
		if (nextInstruction != null) {// check has nextTask
			// check if in the same task
			if (current == null || current.task_id != nextInstruction.task_id) {
				// app.enterNewTask(current, nextInstruction);
				app.enterNewTask(nextInstruction.task_id);
			}
			boolean switchSegment = (current == null)? false:(nextInstruction.seg_id > current.seg_id);
			
			current = nextInstruction;
			app.setUserInfomraiton(null, nextInstruction._id, nextInstruction.task_id, nextInstruction.seg_id);
			
			if(switchSegment) app.onChangeSegment();
//			Log.v("mark", "nextInstruction id:" + nextInstruction);
		} else {// no more instruction
			app.enterNewTask(-1);
			// app.enterNewTask(current, null);
			current = null;
			Log.v("mark", "nextInstruction is null");
			app.onAllTaskFinished();
		}
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	public void nextInstruction(boolean isSucessful) {
		nextInstruction(isSucessful, -1);
	}

	private void markCurrentInstructionState(SQLiteDatabase db, int _id, int state) {
		String table = Common.Tables.INSTRUCTION;
		ContentValues values = new ContentValues();
		values.put(Common.Tables.Instruction.state, state);
		String where = Common.Tables.Instruction._id + " = ?";
		String[] args = new String[] { _id + "" };
		long result = db.update(table, values, where, args);
		if (result < 0) {
			Log.v("mark", "markCurrentInstructionState: update failure with id " + _id);
		}
	}

	public Instruction fetchNextInstruction(SQLiteDatabase db, int nextId) {
		Instruction result = null;
		Cursor cur = db.query(Common.Tables.INSTRUCTION, null, Common.Tables.Instruction._id + " >= ?", new String[] { nextId + "" }, null, null, null, null);
		if (cur.moveToFirst()) {
			result = new Instruction(cur);
		}
		cur.close();
		return result;
	}

	public void setAdditionalInformation(String msg) {
		Log.v("mark", "additionalInformation:" + msg);
		this.additionalInformation = msg;
	}

	public Instruction getInstructionReference() {
		return this.current;
	}
	private final Object simpleLock = new Object();

	public void onVerification(final int event_obj, final String event) {
		boolean sucessful = false;
		if (current == null) return;
		if (!current.isSameTarget(event_obj, event)) {
			Log.v("mark", "onVerification, is not the targeted object");
			return;
		} else {
			Log.v("mark", "onVerification, is the targeted object");
		}
		String[] answer = retrieveAnswer(current.verif_src);
		Log.v("mark","retrieved:"+Arrays.toString(answer));
		if (answer == null) {
			sucessful = false;
		} else {
			// String[] filteredAnswer = this.inputFilter(answer);
			sucessful = isCorrectAnswer(answer);
		}
		if (sucessful) {
			Activity act = app.getCurrentActivity();
			Log.v("mark", "onVerification: sucessful");
			this.nextInstruction(true);
			if (act instanceof FrameActivity) {
				((FrameActivity) act).closeRightDrawer();
				((FrameActivity) act).closeRightDrawer();
				((FrameActivity) act).openLeftDrawer();
			}
			postCorrectVerification();
		} else {
			Log.v("mark", "onVerification: failure");
			postWrongVerfication();
			if (this.current.err_msg == null || this.current.err_msg.equals("null")) { return; }
			Toast toast = Toast.makeText(app.getApplicationContext(), this.current.err_msg, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.TOP, 0, 0);
			toast.show();
		}
	}

	private void postWrongVerfication() {
		// postCorrectVerification();
	}

	private void postCorrectVerification() {
		switch (type) {
		case DATABASE: {
			SQLiteDatabase db = app.getDB();
			db.beginTransaction();
			ContentValues values = new ContentValues();
			Cursor cur = db.query(table, null, null, null, null, null, null);
			int index = cur.getColumnIndex("new");
			if (index >= 0) {
				values.put("new", 0);
				long result = db.update(table, values, "new = ?", new String[] { "1" });
				Log.v("mark", "postCorrectVerification set new to 0 result:" + result);
			}
			db.setTransactionSuccessful();
			db.endTransaction();
		}
			break;
		case RIGHTDRAWER: {
			Activity act = this.app.getCurrentActivity();
			if (act != null) {
				((FrameActivity) act).clearRightPanel();
			}
		}
			break;
		}
		return;
	}

	private String[] retrieveAnswer(int type) {
		this.type = type;
		switch (type) {
		case PASS: {
			Log.v("mark", "retrieveAnswer all pass");
			return new String[] {};
		}
		case DATABASE: {
			Log.v("mark", "retrieveAnswer from databse");
			SQLiteDatabase db = app.getDB();
			ArrayList<String> result = new ArrayList<String>();
			db.beginTransaction();
			String toDepacket = current.verif_com;
			if (toDepacket == null) {
				Log.v("mark", "retrieveAnswer verif_com is null");
				return null;
			}
			/* table;col1,col2,...;selection;arg1,arg2; */
			parts = toDepacket.split(";");
			table = parts[0];
			columns = parts[1].equals("null") ? null : parts[1].split(",");
			selection = parts[2].equals("null") ? null : parts[2];
			args = selection == null ? null : parts[3].split(",");
			Log.v("mark", "" + toDepacket);
			Log.v("mark", "table:" + table);
			Log.v("mark", "columns:" + Arrays.toString(columns));
			Log.v("mark", "selection:" + selection);
			Log.v("mark", "args:" + Arrays.toString(args));
			Cursor cur = db.query(table, columns, selection, args, null, null, null);
			if (cur.moveToFirst()) {
				do {
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < columns.length; i++) {
						String columnName = columns[i];
						int index = cur.getColumnIndex(columnName);
						if (index < 0) {
							Log.v("mark", "onVerification: columnName does not exist");
						} else {
							sb.append(cur.getString(index));
							if (i < columns.length - 1) {
								sb.append("><");
							}
						}
					}
					result.add(sb.toString());
				} while (cur.moveToNext());
				cur.close();
				db.setTransactionSuccessful();
				db.endTransaction();
				return result.toArray(new String[0]);
			} else {
				Log.v("mark", "retrieveAnswer cursor empty");
				cur.close();
				db.setTransactionSuccessful();
				db.endTransaction();
				return null;
			}
		}
		case RIGHTDRAWER: {
			Activity act = app.getCurrentActivity();
			Log.v("mark", "retrieveAnswer from rightdrawer:"+act);
			
			if (act instanceof FrameActivity) {
				return new String[] { ((FrameActivity) act).retrieveAnswerOnRightPanel() };
			} else {
				return null;
			}
		}
		case TASPANEL: {
			Log.v("mark", "retrieveAnswer from taskpanel");
			Activity act = app.getCurrentActivity();
			if(act instanceof ExperimentActivity){
				return new String[]{((ExperimentActivity)act).getTaskIconPattern()};
			}
//			if (act instanceof TaskPanel) { return new String[] { ((TaskPanel.TaskPanelFragment) act).getTaskIconPattern() }; }
			return null;
		}
		case ADDITIONALINFO: {
			Log.v("mark", "retrieveAnswer from addionalinfo");
			return new String[] { this.additionalInformation };
		}
		default:
			Log.v("mark", "onVerification: unidentified verif_src");
		}
		return null;
	}

	private boolean isCorrectAnswer(String ans[]) {
		Log.v("mark", "checking correctness");
		if (this.current.qualifier == null || this.current.qualifier.equals("null")) {
			return true;
		} else {
			String[] quals = current.qualifier.split(",");
			String[] diffs = current.difference.split(",");
			Log.v("mark", "qualifer:" + Arrays.toString(quals));
			Log.v("mark", "difference: " + Arrays.toString(diffs));
			int[] dv = new int[diffs.length];
			for (int i = 0; i < dv.length; i++) {
				try {
					dv[i] = Integer.parseInt(diffs[i]);
				} catch (NumberFormatException nfe) {
					Log.v("mark", "isCorrectAnswer diffs NumberFormatException #" + i + ":" + diffs[i]);
					dv[i] = 0;
				}
			}
			if (quals.length != diffs.length) {
				Log.v("mark", "isCorrectAnswer quals and diffs length differs");
			}
			for (int i = 0; i < ans.length; i++) {
				String[] parts = this.inputFilter(ans[i].split("><"));
				Log.v("mark", "diffs: " + Arrays.toString(parts));
				if (quals.length != parts.length) {
					Log.v("mark", "isCorrectAnswer quals and parts length differs");
				}
				for (int k = 0; k < parts.length; k++) {
					if (Utility.LevenshteinDistance(inputFilter(parts[k]), quals[k]) > dv[k]) {
						continue;
					}
					return true;
				}
			}
		}
		return false;
	}

	private String[] inputFilter(String[] input) {
		Log.v("mark", "inputFilter input: " + Arrays.toString(input));
		String result[] = new String[input.length];
		for (int i = 0; i < input.length; i++) {
			char[] arr = input[i].toLowerCase().toCharArray();
			StringBuilder sb = new StringBuilder();
			for (int j = 0; j < arr.length; j++) {
				if (Character.isLetter(arr[j]) || Character.isDigit(arr[j])) {
					sb.append(arr[j]);
				}
			}
			result[i] = sb.toString();
		}
		Log.v("mark", "inputFilter result: " + Arrays.toString(result));
		return result;
	}

	private String inputFilter(String input) {
		char[] arr = input.toLowerCase().toCharArray();
		StringBuilder sb = new StringBuilder();
		for (int j = 0; j < arr.length; j++) {
			if (Character.isLetter(arr[j]) || Character.isDigit(arr[j])) {
				sb.append(arr[j]);
			}
		}
		return sb.toString();
	}
	public static class Instruction {

		public static final int MULTIOBJECT = 0;
		public final static int COMPLETED = 2;
		public final static int INPROGRESS = 1;
		public final static int UNTOUCHED = 0;
		public final static int SKIPPED = -1;
		public int _id, task_id, seg_id, verif_src, cur_count, event_obj, completed;
		public String instruction, imagename, verif_com, operator, qualifier, err_msg, event, difference, hint;

		public Instruction(int _id, int task_id, int seg_id, String instruction, String hint, String imagename, int verif_src, String verif_com, String operator, int cur_count, String qualifier,
				String difference, String err_msg, int event_obj, String event, int completed) {
			this._id = _id;
			this.task_id = task_id;
			this.seg_id = seg_id;
			this.instruction = instruction;
			this.hint = hint;
			this.imagename = imagename;
			this.verif_src = verif_src;
			this.verif_com = verif_com;
			this.operator = operator;
			this.cur_count = cur_count;
			this.qualifier = qualifier;
			this.difference = difference;
			this.err_msg = err_msg;
			this.event_obj = event_obj;
			this.event = event;
			this.completed = completed;
		}

		public Instruction(Cursor cur) {
			this(cur.getInt(cur.getColumnIndex(Common.Tables.Instruction._id)), cur.getInt(cur.getColumnIndex(Common.Tables.Instruction.task_id)), cur.getInt(cur
					.getColumnIndex(Common.Tables.Instruction.seg_id)), cur.getString(cur.getColumnIndex(Common.Tables.Instruction.instruction)), cur.getString(cur
					.getColumnIndex(Common.Tables.Instruction.hint)), cur.getString(cur.getColumnIndex(Common.Tables.Instruction.imagename)), cur.getInt(cur
					.getColumnIndex(Common.Tables.Instruction.verif_src)), cur.getString(cur.getColumnIndex(Common.Tables.Instruction.verif_com)), cur.getString(cur
					.getColumnIndex(Common.Tables.Instruction.operator)), cur.getInt(cur.getColumnIndex(Common.Tables.Instruction.cur_count)), cur.getString(cur
					.getColumnIndex(Common.Tables.Instruction.qualifier)), cur.getString(cur.getColumnIndex(Common.Tables.Instruction.difference)), cur.getString(cur
					.getColumnIndex(Common.Tables.Instruction.err_msg)), cur.getInt(cur.getColumnIndex(Common.Tables.Instruction.event_obj)), cur.getString(cur
					.getColumnIndex(Common.Tables.Instruction.event)), cur.getInt(cur.getColumnIndex(Common.Tables.Instruction.state)));
		}

		public boolean isCompleted() {
			return this.completed == Instruction.COMPLETED;
		}

		public boolean isSkipped() {
			return this.completed == Instruction.SKIPPED;
		}

		public String toString() {
			return _id + "," + task_id + ","+seg_id+"," + instruction + "," + imagename + "," + verif_src + "," + verif_com + "," + operator + "," + cur_count + "," + qualifier + "," + err_msg + "," + event_obj
					+ "," + event + "," + completed;
		}

		public boolean isSameTarget(int incoming_id, String event) {
			if (this.event_obj == MULTIOBJECT || this.event_obj == Common.RequestIdMultiCast.TOUCHIMAGE) {// 0
				return true;
			}
			Log.v("mark", "incoming_id:" + incoming_id);
			Log.v("mark", "event_obj:" + event_obj);
			Integer intended_id = Common.idMap.get(this.event_obj);
			if (intended_id == null) {
				Log.v("mark", "intended_id does not exist");
				return false;
			}
			Log.v("mark", "intended_id:" + intended_id);
			return intended_id.intValue() == incoming_id;
		}
	}

	private String[] depacketSqlString(String input) {
		if (input == null || input.length() <= 0) { return null; }
		String[] result = new String[8];
		int index = 0;
		int len = input.length();
		boolean isEnd = false;
		OUTER: for (int i = 0; i < 8; i++) {
			// read until ";" or end of string
			StringBuilder sb = new StringBuilder();
			for (; index < len && input.charAt(index) == ';'; index++) {
				sb.append(input.charAt(index));
			}
			String tmp = sb.toString().trim();
			if (tmp.equals("null") || tmp.equals("")) {
				result[i] = null;
			} else {
				result[i] = sb.toString();
			}
		}
		// public Cursor query (String table, String[] columns, String
		// selection, String[] selectionArgs, String
		// groupBy, String having, String orderBy)
		return result;
	}
}
// private Cursor nextById(SQLiteDatabase db, int _id) {
// Cursor cur = db.query(Common.Tables.INSTRUCTION, null,
// Common.Tables.Instruction._id + " > ?",
// new String[] { current._id + "" }, null, null, null, "1");
// return cur;
// }
//
// if (current != null) {
// if (isSucessful) {
// markCurrentInstructionState(db, current._id,
// Instruction.COMPLETED);
// } else {
// markCurrentInstructionState(db, current._id,
// Instruction.SKIPPED);
// }
// Cursor cur = nextById(db, current._id);
// if (cur.moveToFirst()) {
// Instruction next = new Instruction(cur);
// Log.v("mark", "switching to next instruction");
// if (current.task_id != next.task_id) {
// app.enterNewTask(next.task_id);
// }
// current = next;
// markCurrentInstructionState(db, current._id,
// Instruction.INPROGRESS);
// updateUserInfo(db, current._id);
// } else {
// Log.v("mark",
// "nextInstruction cannot find an instruction with current_id,"
// + current._id);
// String selection = Common.Tables.Instruction._id + " > ?";
// String[] arg = new String[] { current._id + "" };
// Cursor max = db.query(Common.Tables.INSTRUCTION, null,
// selection, arg, null, null, null);
// if (max.moveToFirst()) {
// Log.v("mark", "nextInstruction: should not happen");
// } else {
// Log.v("mark",
// "nextInstruction: instruction set reach the end");
// }
// app.enterNewTask(-1);
// }
// } else {
// app.enterNewTask(-1);
// Log.v("mark", "nextInstruction: current instruction is null");
// }
// SQLiteDatabase db = app.getDB();
// String selection = Common.Tables.Instruction._id + " >= ?";
// String[] args = null;
//
// if (currentTask_id < 0) {
// args = new String[] { "0" };
// } else {
// args = new String[] { currentTask_id + "" };
// }
//
// args = new String[] { "0" };
//
// // TODO
// Cursor cur = db.query(Common.Tables.INSTRUCTION, null, selection, args,
// null, null, null);
//
// if (cur.moveToFirst()) {
// current = new Instruction(cur);
// } else {
// Log.v("mark", "TaskManager constructor: cur empty");
// }
// cur.close();
// if (current != null) {
// Log.v("mark", "init instruction:" + current.toString());
// } else {
// Log.v("mark", "init instruction is null ");
// }