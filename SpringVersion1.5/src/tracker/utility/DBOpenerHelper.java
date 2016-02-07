package tracker.utility;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Scanner;
import tracker.springversion1.Common;
import tracker.springversion1.Common.CSV;
import tracker.springversion1.Common.Tables;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBOpenerHelper extends SQLiteOpenHelper {
	private Context context;
	static String[] create = Common.Tables.CREATION;
	static String[] tablesnames = Common.Tables.TABLENAMES;
	static String[] filenames = Common.Tables.FILENAMES;
	static String DELIMITER = Common.CSV.DELIMITER;

	public DBOpenerHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.v("mark", "db oncreate");
		for (String creation : create) {
			db.execSQL(creation);
		}
		initFromCSV(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
	}

	public void reset(SQLiteDatabase db) {
		for (String table : tablesnames) {
			db.execSQL("drop table " + table);
		}
		initFromCSV(db);
	}

	private void initFromCSV(SQLiteDatabase db) {
		Log.v("mark", "init from CSV");
		AssetManager manager = this.context.getAssets();

		Log.v("mark", "table count:" + tablesnames.length);
		for (int i = 0; i < tablesnames.length; i++) {
			try {
				if (filenames[i] != null)
					processing(db, manager, filenames[i], tablesnames[i]);
			} catch (Exception e) {
				Log.v("mark", "init db exception:"+e.toString());
			}
		}
	}

	private void processing(SQLiteDatabase db, AssetManager manager,
			String filename, String tablename) throws Exception {
		InputStream in = manager.open(filename);
		Scanner scan = new Scanner(in);
		if (scan.hasNext() == false) {
			scan.close();
			if (in != null)
				in.close();
			throw new IOException("first line empty");
		}
		Log.v("mark", "init table:" + tablename);
		String firstLine = scan.nextLine();
		String[] col = firstLine.split(DELIMITER);
//		for (int i = 0; i < col.length; i++) {
//			col[i] = col[i];
//		}
		Log.v("mark", "Input Cols::" + Arrays.toString(col));
		// ContentValues insertNew = new ContentValues();
		// insertNew.put("new", 0);
		ContentValues content = new ContentValues();
		// boolean hasNewColumn =!( tablename.equals(Common.Tables.INSTRUCTION)
		// || tablename.equals(Common.Tables.USERINFO));
		boolean hasFlagCol = true;
		// TODO

		if(tablename.equals(Common.Tables.USERINFO) || 
				tablename.equals(Common.Tables.INSTRUCTION) ||
				tablename.equals(Common.Tables.BANKINFO)){
			hasFlagCol = false;
		}
		
		
		// for(String s : col){
		// if(s!=null && s.equals("new")){
		// hasFlagCol = true;
		// break;
		// }
		// }
		while (scan.hasNext()) {
			String line = scan.nextLine();
			String[] parts = line.split(DELIMITER);
			for (int i = 0; i < col.length; i++) {
				content.put(col[i].trim(), parts[i].trim());
			}
			if (hasFlagCol) {
				content.put("new", 0);
			}
			long result = db.insert(tablename, null, content);
			if (result < 0) {
				Log.v("mark",
						"db insert fails with content:" + content.toString());
				Log.v("mark", "line: " + line);
			}
		}
		scan.close();
		if (in != null)
			in.close();
		Cursor con = db.query(tablename, null, null, null, null, null, null);
		Log.v("mark", "Cols:" + Arrays.toString(con.getColumnNames()));
		String[] arr = new String[con.getColumnCount()];
		if (con.moveToFirst()) {
			for (int i = 0; i < con.getColumnCount(); i++) {
				arr[i] = con.getString(i);
			}
		}
		Log.v("mark", "First:" + Arrays.toString(arr));
		con.close();
	}
}
