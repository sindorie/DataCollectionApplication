package tracker.UI.fragment;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import tracker.UI.activity.FrameActivity;
import tracker.UI.components.MyArrayListAdapter;
import tracker.springversion1.Common;
import tracker.springversion1.ProjectInterface;
import tracker.springversion1.R;
import tracker.springversion1.Common.Tables;
import tracker.springversion1.ProjectInterface.ActivityInterface;
import tracker.springversion1.ProjectInterface.ContactInterface;
import tracker.springversion1.R.drawable;
import tracker.springversion1.R.id;
import tracker.springversion1.R.layout;
import tracker.utility.Utility;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Contact {

	private static int UNDEFINED = -2;
	public static class ContactDetail extends Fragment {

		ContactInfomation infoReference;
		private View ground;
		public static String ADDING = "ADDING";
		public static String KEYID = "id";

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			getInfoFromDatabase();
			ground = inflater.inflate(R.layout.frag_contactdetail, container, false);
			EditText fnameView = (EditText) ground.findViewById(R.id.contactdetail_fname);
			EditText lnameView = (EditText) ground.findViewById(R.id.contactdetail_lname);
			EditText phoneView = (EditText) ground.findViewById(R.id.contactdetail_phone);
			EditText emailView = (EditText) ground.findViewById(R.id.contactdetail_email);
			EditText addressview = (EditText) ground.findViewById(R.id.contactdetail_address);
			ImageView photoview = (ImageView) ground.findViewById(R.id.contact_image);
			Button editButton = (Button) ground.findViewById(R.id.contact_submit);
			editButton.setOnClickListener(listener);
			if (infoReference != null) {
				fnameView.setText(infoReference.fname);
				lnameView.setText(infoReference.lname);
				phoneView.setText(infoReference.phone);
				addressview.setText(infoReference.address);
				emailView.setText(infoReference.email);
				if (!Utility.setImage("figure/" + infoReference.photo, this.getActivity().getAssets(), photoview)) {
					photoview.setImageResource(R.drawable.noimage);
				}
			} else {
				fnameView.setText("");
				lnameView.setText("");
				phoneView.setText("");
				addressview.setText("");
				emailView.setText("");
				editButton.setText("add");
				photoview.setImageResource(R.drawable.noimage);
			}
			return ground;
		}

		public String toString(){
			return "ContactDetail";
		}
		private void getInfoFromDatabase() {
			Bundle arg = this.getArguments();
			int id = 0;
			if (arg == null || !arg.containsKey(KEYID)) {
				return;
			} else {
				id = arg.getInt(KEYID);
			}
			Activity act = this.getActivity();
			if (act instanceof ProjectInterface.ActivityInterface) {
				SQLiteDatabase db = ((ProjectInterface.ActivityInterface) act).getDatabaseReferenceFromActivity();
				db.beginTransaction();
				String selection = Common.Tables.Contact._id + " = ?";
				String[] args = new String[] { id + "" };
				Cursor cur = db.query(Common.Tables.CONTACT, null, selection, args, null, null, null);
				if (cur.moveToFirst()) {
					infoReference = new ContactInfomation(cur);
				}
				db.setTransactionSuccessful();
				db.endTransaction();
			}
		}
		private View.OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Activity act = getActivity();
				if (act instanceof ProjectInterface.ContactInterface) {
					EditText fnameView = (EditText) ground.findViewById(R.id.contactdetail_fname);
					EditText lnameView = (EditText) ground.findViewById(R.id.contactdetail_lname);
					EditText phoneView = (EditText) ground.findViewById(R.id.contactdetail_phone);
					EditText emailView = (EditText) ground.findViewById(R.id.contactdetail_email);
					EditText addressview = (EditText) ground.findViewById(R.id.contactdetail_address);
					String first = fnameView.getText().toString().trim();
					String last = lnameView.getText().toString().trim();
					String phone = phoneView.getText().toString().trim();
					String address = addressview.getText().toString().trim();
					String email = emailView.getText().toString().trim();
					if (first.equals("")) {
						Toast.makeText(getActivity(), "please enter first name", Toast.LENGTH_LONG).show();
						return;
					}
					Toast.makeText(act.getApplicationContext(), "Update finished", Toast.LENGTH_LONG).show();
					int tosend = UNDEFINED;
					if (infoReference != null) {
						tosend = infoReference._id;
					}
					((ProjectInterface.ContactInterface) act).contactDetailSubmitCallback(v, tosend, first, last, address, phone, email);
				}
			}
		};
	}
	public static class ContactList extends Fragment {

		private MyArrayListAdapter<ContactInfomation> adapter;
		private int focusIndex = -1;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
		}
		private EditText searchbox;
		private ListView listview;
		private static ContactInfomation.FirstNameComparator fname_comparator 
				= new ContactInfomation.FirstNameComparator();
		private static ContactInfomation.LastNameComparator lname_comparator 
				= new ContactInfomation.LastNameComparator();
		
		// private int green = Color.parseColor("#80A2E889");
		// private int red = Color.parseColor("#80E3C1CD");
		// private int whilte = Color.parseColor("#80FFFFFF");
		public void onResume() {
			super.onResume();
			updateList("");
		}

		public String toString(){
			return "ContactList";
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstaceState) {
			View v = inflater.inflate(R.layout.frag_contactlist, container, false);
			FrameActivity act = (FrameActivity) this.getActivity();
			searchbox = (EditText) v.findViewById(R.id.contact_search_box);
			// searchbox.setOnFocusChangeListener(searchBoxFocusListener);
			searchbox.addTextChangedListener(watcher);
			listview = (ListView) v.findViewById(R.id.contact_list);
			listview.setAdapter(adapter);
			listview.setOnItemClickListener(listener);
			// listview.setOnTouchListener(act.disableInterceptListener);
			View add_button = v.findViewById(R.id.contact_add);
			add_button.setOnClickListener(add_Listener);
			updateList(null);
			return v;
		}
		private View.OnClickListener add_Listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Activity act = getActivity();
				if (act instanceof ProjectInterface.ContactInterface) {
					((ProjectInterface.ContactInterface) act).contactAddContactOnClickCallBack();
				}
			}
		};
		private TextWatcher watcher = new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				String message = arg0.toString().trim();
				Log.v("mark", "message is :" + message);
				updateList(message);
			}
		};

		private void updateList(String part) {
			Activity act = this.getActivity();
			if (!(act instanceof ProjectInterface.ActivityInterface)) {
				adapter = new MyArrayListAdapter<ContactInfomation>(this.getActivity(), new ContactInfomation[0]);
				Log.v("mark", "should not be");
				return;
			}
			SQLiteDatabase db = ((ProjectInterface.ActivityInterface) act).getDatabaseReferenceFromActivity();
			db.beginTransaction();
			if (part != null && !part.equals("")) {
				Cursor cur = db.query(Common.Tables.CONTACT, null, 
						Common.Tables.Contact._id + " != ? and  "
								+ "(" + Common.Tables.Contact.first + " LIKE ? or "
								+ Common.Tables.Contact.last + " LIKE ? )" , new String[] { "-1", part + "%" ,part + "%"}, null, null,
						null, null);
				if (cur.moveToFirst()) {
					ContactInfomation[] info = new ContactInfomation[cur.getCount()];
					int index = 0;
					do {
						info[index] = new ContactInfomation(cur);
						index += 1;
					} while (cur.moveToNext());
					//sort in ascending order
					Arrays.sort(info,fname_comparator);
					
					adapter = new MyArrayListAdapter<ContactInfomation>(this.getActivity(), info);
				} else {
					adapter = new MyArrayListAdapter<ContactInfomation>(this.getActivity(), new ContactInfomation[0]);
				}
			} else {
				Cursor cur = db.query(Common.Tables.CONTACT, null, Common.Tables.Contact._id + " != ?", new String[] { "-1" }, null, null, null, null);
				if (cur.moveToFirst()) {
					ContactInfomation[] info = new ContactInfomation[cur.getCount()];
					int index = 0;
					do {
						info[index] = new ContactInfomation(cur);
						index += 1;
					} while (cur.moveToNext());
					Arrays.sort(info,fname_comparator);
					adapter = new MyArrayListAdapter<ContactInfomation>(this.getActivity(), info);
				} else {
					adapter = new MyArrayListAdapter<ContactInfomation>(this.getActivity(), new ContactInfomation[0]);
				}
			}
			db.setTransactionSuccessful();
			db.endTransaction();
			listview.setAdapter(adapter);
		}
		private AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Activity act = getActivity();
				if (act instanceof ProjectInterface.ContactInterface) {
					((ProjectInterface.ContactInterface) act).contactListItemOnClickCallBack(arg0, arg1, arg2, arg3);
				}
			}
		};
	}
	
	public static class ContactInfomation implements MyArrayListAdapter.GenerateView {

		public int _id;
		public String fname, lname, phone, email, address, photo;

		public ContactInfomation(int id, String fname, String lname, String phone) {
			this._id = id;
			this.fname = (fname == null ? "" : fname);
			this.lname = (lname == null ? "" : lname);
			this.phone = (phone == null ? "" : phone);
		}

		public ContactInfomation(int id, String first, String last, String phone, String email, String address, String photo) {
			this._id = id;
			this.fname = first;
			this.lname = last;
			this.phone = phone;
			this.email = email;
			this.address = address;
			this.photo = photo;
		}

		public ContactInfomation(Cursor cur) {
			this(cur.getInt(cur.getColumnIndex(Common.Tables.Contact._id)), cur.getString(cur.getColumnIndex(Common.Tables.Contact.first)), cur.getString(cur
					.getColumnIndex(Common.Tables.Contact.last)), cur.getString(cur.getColumnIndex(Common.Tables.Contact.phone)), cur.getString(cur.getColumnIndex(Common.Tables.Contact.email)), cur
					.getString(cur.getColumnIndex(Common.Tables.Contact.address)), cur.getString(cur.getColumnIndex(Common.Tables.Contact.photo)));
		}

		@Override
		public View getView(Activity act, int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = act.getLayoutInflater();
			View view = inflater.inflate(R.layout.view_contactrecord, parent, false);
			TextView fnameView = (TextView) view.findViewById(R.id.contactrecord_fname);
			TextView lnameView = (TextView) view.findViewById(R.id.contactrecord_lname);
			// TextView phoneView = (TextView) view
			// .findViewById(R.id.contactrecord_phone);
			fnameView.setText(fname);
			lnameView.setText(lname);
			// phoneView.setText(phone);
			return view;
		}

		@Override
		public View getDropDownView(Activity act, int position, View convertView, ViewGroup parent) {
			return null;
		}

		@Override
		public String toString() {
			return _id + "," + fname + "," + lname + "," + phone + "," + address + "," + photo;
		}
	
		public static class FirstNameComparator implements Comparator<ContactInfomation>{
			@Override
			public int compare(ContactInfomation lhs, ContactInfomation rhs) {
				return lhs.fname.compareTo(rhs.fname);
			}
		}
		
		public static class LastNameComparator implements Comparator<ContactInfomation>{
			@Override
			public int compare(ContactInfomation lhs, ContactInfomation rhs) {
				return lhs.lname.compareTo(rhs.lname);
			}
		}
	}
}
