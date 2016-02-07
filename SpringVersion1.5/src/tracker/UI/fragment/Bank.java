package tracker.UI.fragment;

import tracker.UI.activity.FrameActivity;
import tracker.UI.components.MyArrayListAdapter;
import tracker.springversion1.Common;
import tracker.springversion1.ProjectInterface;
import tracker.springversion1.R;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Bank{

	public static class BankFragment extends Fragment implements ProjectInterface.FragmentInterface {

		private static String[] payee = { "credit", "utility", "cable", "insurance", "phone" };
		private static String[] mounth = { "Jan.", "Feb.", "Mar.", "Apr.", "May", "Jun.", "Jul.", "Aug.", "Sep.", "Oct.", "Nov.", "Dec." };
		private View account, pay, transfer, profile;
		// transHistory;
		private View[] list = new View[4];
		private ViewPager mViewPager;
		private String[] titles = new String[] { "account", "pay", "Transfer", "info" };
		private String[] accountNames;
		private final static String TransKey = "TransKey";
		private boolean transFlag = false;
		private int selectedId;
		private final static String PreIndex = "PreIndex";
		private int previousIndex = 0;
		private String infoFlag = "infoflag";
		private MyArrayListAdapter<BankAccount> accountAdapter;
		private View bankpay_front, bankpay_topay, bankpay_schedule;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			accountAdapter = getAccountListAdapter();
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View ground = inflater.inflate(R.layout.frag_bank, container, false);
			// Bank Account
			list[0] = account = inflater.inflate(R.layout.view_bankaccount, mViewPager, false);
			accountNames = new String[accountAdapter.getCount()];
			for (int i = 0; i < accountNames.length; i++) {
				accountNames[i] = ((BankAccount) accountAdapter.getItem(i)).name;
			}
			ListView lv_account = (ListView) account.findViewById(R.id.account_list);
			lv_account.setAdapter(accountAdapter);
			lv_account.setOnItemClickListener(accountClickListener);
			ListView lv_trans = (ListView) account.findViewById(R.id.trans_list);
			// revised pay page
			list[1] = pay = inflater.inflate(R.layout.view_bank_payselection, mViewPager, false);
			bankpay_front = pay.findViewById(R.id.bankrevised_pay_front);
			bankpay_topay = pay.findViewById(R.id.bankrevised_pay_topay);
			bankpay_schedule = pay.findViewById(R.id.bankrevised_pay_scheduled);
			// Log.v("mark", "" + bankpay_front + "  " + bankpay_topay + "  "
			// + bankpay_schedule);
			bankpay_front.setVisibility(View.VISIBLE);
			bankpay_topay.setVisibility(View.INVISIBLE);
			bankpay_schedule.setVisibility(View.INVISIBLE);
			View topayButton = (View) pay.findViewById(R.id.banrevised_pay_pay);
			View scheduledButton = (View) pay.findViewById(R.id.banrevised_pay_scheduled);
			topayButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					change2thepage(1);
				}
			});
			scheduledButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					change2thepage(2);
					readSchedulePay();
				}
			});
			Spinner accountSpinner = (Spinner) pay.findViewById(R.id.pay_accountspinner);
			accountSpinner.setAdapter(new ArrayAdapter<String>(this.getActivity().getApplicationContext(), R.layout.spinner_item, accountNames));
			Spinner payeeSpinner = (Spinner) pay.findViewById(R.id.pay_payee);
			payeeSpinner.setAdapter(new ArrayAdapter<String>(this.getActivity().getApplicationContext(), R.layout.spinner_item, payee));
			Spinner mountSpinner = (Spinner) pay.findViewById(R.id.date_mounth);
			mountSpinner.setAdapter(new ArrayAdapter<String>(this.getActivity().getApplicationContext(), R.layout.spinner_item, mounth));
			mountSpinner.setSelection(0);
			final Spinner daySpinner = (Spinner) pay.findViewById(R.id.date_day);
			daySpinner.setAdapter(new ArrayAdapter<String>(this.getActivity().getApplicationContext(), R.layout.spinner_item, getNumString(31)));
			final Spinner yearSpinner = (Spinner) pay.findViewById(R.id.date_year);
			yearSpinner.setAdapter(new ArrayAdapter<String>(this.getActivity().getApplicationContext(), R.layout.spinner_item, getNumString(2014, 2015)));
			Button submitButton = (Button) pay.findViewById(R.id.schedule_submit);
			Button clearButton = (Button) pay.findViewById(R.id.schedule_clear);
			submitButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					click_submit(arg0);
				}
			});
			clearButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					click_clear(arg0);
				}
			});
			list[2] = transfer = inflater.inflate(R.layout.view_bank_transfer, mViewPager, false);
			Spinner spin_account1 = (Spinner) transfer.findViewById(R.id.transfer_account1);
			Spinner spin_account2 = (Spinner) transfer.findViewById(R.id.transfer_account2);
			spin_account1.setAdapter(new ArrayAdapter<String>(this.getActivity().getApplicationContext(), R.layout.spinner_item, accountNames));
			spin_account2.setAdapter(new ArrayAdapter<String>(this.getActivity().getApplicationContext(), R.layout.spinner_item, accountNames));
			Button transferButton = (Button) transfer.findViewById(R.id.transfer_button);
			transferButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					click_transferaction(arg0);
				}
			});
			// personal info
			list[3] = profile = inflater.inflate(R.layout.view_bankinfo_revised, mViewPager, false);
			Button profilesubmitButton = (Button) profile.findViewById(R.id.infosubmit);
			profilesubmitButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					click_infoSubmit(arg0);
				}
			});
			Button profileresetButton = (Button) profile.findViewById(R.id.inforeset);
			profileresetButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					click_inforeset(arg0);
				}
			});
			final ActionBar actionBar = this.getActivity().getActionBar();
			Log.v("mark", "action bar tab count:" + actionBar.getTabCount());
			if (actionBar.getTabCount() < 3) {
				actionBar.setHomeButtonEnabled(false);
				actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
				for (String title : titles) {
					actionBar.addTab(actionBar.newTab().setTabListener(lis).setText(title));
				}
			} else {
				actionBar.removeAllTabs();
				actionBar.setHomeButtonEnabled(false);
				actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
				for (String title : titles) {
					actionBar.addTab(actionBar.newTab().setTabListener(lis).setText(title));
				}
			}
			mViewPager = (ViewPager) ground.findViewById(R.id.activity_bank_pager);
			mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

				@Override
				public void onPageSelected(int position) {
					Log.v("mark", "on page change");
					actionBar.setSelectedNavigationItem(position);
					if (position != 0 && transFlag) swithclist(-1);
					if (position != 1) change2thepage(0);
				}
			});
			mViewPager.setAdapter(adapter);
			lv_trans.setVisibility(View.INVISIBLE);
			if (savedInstanceState != null) {
				if (savedInstanceState.containsKey(TransKey)) {
					int selectedId = savedInstanceState.getInt(TransKey);
					this.swithclist(selectedId);
				}
				if (savedInstanceState.containsKey(infoFlag)) {
					// infoEditing = savedInstanceState.getBoolean(infoFlag);
					// this.infoBundle =
					// savedInstanceState.getBundle(hasBundle);
				}
				if (savedInstanceState.containsKey(PreIndex)) {
					int temp = savedInstanceState.getInt(PreIndex);
					this.change2thepage(temp);
				}
			}
			readProfile();
			readSchedulePay();
			return ground;
		}

		@Override
		public boolean actionOnBackPressed() {
			if (transFlag) {
				swithclist(-1);
				return true;
			} else if (change2thepage(0)) {
				return true;
			} else {
				this.getActivity().getActionBar().removeAllTabs();
				this.getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
				return false;
			}
		}
		private ActionBar.TabListener lis = new ActionBar.TabListener() {

			@Override
			public void onTabReselected(Tab arg0, FragmentTransaction arg1) {}

			@Override
			public void onTabSelected(Tab arg0, FragmentTransaction arg1) {
				if (mViewPager != null) {
					mViewPager.setCurrentItem(arg0.getPosition());
				}
			}

			@Override
			public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {}
		};

		public void click_submit(View v) {
			// retrieve View Object
			Spinner accountSpinner = (Spinner) pay.findViewById(R.id.pay_accountspinner);
			Spinner payeeSpinner = (Spinner) pay.findViewById(R.id.pay_payee);
			EditText amountView = (EditText) pay.findViewById(R.id.info_update_first);
			Spinner mounthSpinner = (Spinner) pay.findViewById(R.id.date_mounth);
			Spinner dateSpinner = (Spinner) pay.findViewById(R.id.date_day);
			Spinner yearSpinner = (Spinner) pay.findViewById(R.id.date_year);
			// retrieve information
			String selectedAccountName = accountSpinner.getSelectedItem().toString();
			String payee = payeeSpinner.getSelectedItem().toString();
			String amountString = amountView.getText().toString();
			double amount = 0;
			try {
				double temp = Double.parseDouble(amountString);
				amount = temp;
			} catch (NumberFormatException nfe) {
				Log.v("mark", "bank pay click_submit:" + nfe.getMessage());
				amount = 0;
			}
			String mounth = mounthSpinner.getItemAtPosition(mounthSpinner.getSelectedItemPosition()).toString();
			String day = dateSpinner.getItemAtPosition(dateSpinner.getSelectedItemPosition()).toString();
			String year = yearSpinner.getItemAtPosition(yearSpinner.getSelectedItemPosition()).toString();
			String date = mounth + "/" + day + "/" + year;
			// save to datatbase
			Activity act = this.getActivity();
			if (act instanceof FrameActivity) {
				SQLiteDatabase db = ((FrameActivity) act).getDatabaseReferenceFromActivity();
				db.beginTransaction();
				ContentValues values = new ContentValues();
				values.put(Common.Tables.BankPay.accountName, selectedAccountName);
				values.put(Common.Tables.BankPay.payee, payee);
				values.put(Common.Tables.BankPay.amount, amount);
				values.put(Common.Tables.BankPay.date, date);
				long result = db.insert(Common.Tables.BANKPAY, null, values);
				if (result <= 0) {
					Log.v("mark", "bank click_submit result<=0");
				} else {
					((FrameActivity) act).closeKeyBoard();
					((FrameActivity) act).onBackPressed();
					Toast.makeText(this.getActivity(), "Schedule sucessful", Toast.LENGTH_SHORT).show();
				}
				db.setTransactionSuccessful();
				db.endTransaction();
			}
			readSchedulePay();
		}

		private void readSchedulePay() {
			Activity act = this.getActivity();
			if (act instanceof FrameActivity) {
				SQLiteDatabase db = ((FrameActivity) act).getDatabaseReferenceFromActivity();
				Cursor cur = db.query(Common.Tables.BANKPAY, null, null, null, null, null, null);
				db.beginTransaction();
				ScheduledPay[] list = new ScheduledPay[cur.getCount()];
				int total = 0;
				if (cur.moveToFirst()) {
					int index = 0;
					do {
						list[index] = new ScheduledPay(cur);
						total += list[index].amount;
						index += 1;
					} while (cur.moveToNext());
				}
				MyArrayListAdapter<ScheduledPay> adapter = new MyArrayListAdapter<ScheduledPay>(this.getActivity(), list);
				ListView billlist = (ListView) pay.findViewById(R.id.bankrevised_pay_scheduled_currentlist);
				billlist.setAdapter(adapter);
				billlist.invalidate();
				TextView totalview = (TextView) pay.findViewById(R.id.banrevised_paytotal);
				totalview.setText(total + "");
				db.setTransactionSuccessful();
				db.endTransaction();
			}
		}

		public void click_clear(View v) {
			Spinner accountSpinner = (Spinner) pay.findViewById(R.id.pay_accountspinner);
			Spinner payeeSpinner = (Spinner) pay.findViewById(R.id.pay_payee);
			EditText amountEdit = (EditText) pay.findViewById(R.id.info_update_first);
			Spinner mounthSpinner = (Spinner) pay.findViewById(R.id.date_mounth);
			Spinner dateSpinner = (Spinner) pay.findViewById(R.id.date_day);
			Spinner yearSpinner = (Spinner) pay.findViewById(R.id.date_year);
			accountSpinner.setSelection(0);
			payeeSpinner.setSelection(0);
			mounthSpinner.setSelection(0);
			dateSpinner.setSelection(0);
			yearSpinner.setSelection(0);
			amountEdit.setText("");
			this.getActivity().onBackPressed();
		}

		public void click_infoSubmit(View v) {
			saveProfile();
			readProfile();
		}

		public void click_inforeset(View v) {
			EditText phoneview = (EditText) this.profile.findViewById(R.id.info_update_phone);
			EditText addressview = (EditText) this.profile.findViewById(R.id.info_update_address);
			EditText emailview = (EditText) this.profile.findViewById(R.id.info_update_email);
			phoneview.setText("");
			addressview.setText("");
			emailview.setText("");
		}

		public void click_transferaction(View v) {
			Spinner spin_account1 = (Spinner) transfer.findViewById(R.id.transfer_account1);
			Spinner spin_account2 = (Spinner) transfer.findViewById(R.id.transfer_account2);
			EditText amountview = (EditText) transfer.findViewById(R.id.transfer_amount);
			String message1 = spin_account1.getSelectedItem().toString().trim();
			String message2 = spin_account2.getSelectedItem().toString().trim();
			String message3 = amountview.getText().toString().toString().trim();
			int acc1_index = spin_account1.getSelectedItemPosition();
			int acc2_index = spin_account2.getSelectedItemPosition();
			if (acc1_index == acc2_index) {
				Toast.makeText(this.getActivity().getApplicationContext(), "Cannot be the same accounts", Toast.LENGTH_LONG).show();
				return;
			}
			if (message3.equals("") || message1.equals(message2)) {
				Toast.makeText(this.getActivity().getApplicationContext(), "Invalid amount", Toast.LENGTH_LONG).show();
				return;
			}
			double value = 0;
			try {
				value = Double.parseDouble(message3);
			} catch (Exception e) {
				Toast.makeText(this.getActivity().getApplicationContext(), "Invalid amount", Toast.LENGTH_LONG).show();
				return;
			}
			// TODO improvement maybe required
			BankAccount source = (BankAccount) accountAdapter.getItem(acc1_index);
			BankAccount dest = (BankAccount) accountAdapter.getItem(acc2_index);
			source.balance -= value;
			dest.balance += value;
			Activity act = this.getActivity();
			if (act instanceof ProjectInterface.ActivityInterface) {
				// update account list
				SQLiteDatabase db = ((ProjectInterface.ActivityInterface) act).getDatabaseReferenceFromActivity();
				db.beginTransaction();
				ContentValues cv1 = source.toContentValues();
				cv1.put("new", 1);
				db.update(Common.Tables.ACCOUNT, cv1, Common.Tables.Account._id + " = ?", new String[] { source._id + "" });
				ContentValues cv2 = dest.toContentValues();
				cv2.put("new", 1);
				db.update(Common.Tables.ACCOUNT, cv2, Common.Tables.Account._id + " = ?", new String[] { dest._id + "" });
				accountAdapter = getAccountListAdapter();
				ListView lv_account = (ListView) account.findViewById(R.id.account_list);
				lv_account.setAdapter(accountAdapter);
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyy");
				String currentDateandTime = sdf.format(new Date());
				// update the transaction list
				ContentValues tranValue_srouce = new ContentValues();
				tranValue_srouce.put(Common.Tables.Transaction.acc_id, source._id);
				tranValue_srouce.put(Common.Tables.Transaction.balance, source.balance);
				tranValue_srouce.put(Common.Tables.Transaction.date, currentDateandTime);
				tranValue_srouce.put(Common.Tables.Transaction.event, "Money Transfer");
				tranValue_srouce.put(Common.Tables.Transaction.trans, -value);
				ContentValues tranValue_dest = new ContentValues();
				tranValue_dest.put(Common.Tables.Transaction.acc_id, dest._id);
				tranValue_dest.put(Common.Tables.Transaction.balance, dest.balance);
				tranValue_dest.put(Common.Tables.Transaction.date, currentDateandTime);
				tranValue_dest.put(Common.Tables.Transaction.event, "Money Transfer");
				tranValue_dest.put(Common.Tables.Transaction.trans, value);
				db.insert(Common.Tables.TRANS, null, tranValue_srouce);
				db.insert(Common.Tables.TRANS, null, tranValue_dest);
				db.setTransactionSuccessful();
				db.endTransaction();
			}
			Toast.makeText(this.getActivity().getApplicationContext(), "Tansaction complete!", Toast.LENGTH_LONG).show();
			logString(message1, message2, message3);
		}

		public void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);
			outState.putBoolean(TransKey, this.transFlag);
			outState.putInt(TransKey, selectedId);
			outState.putInt(PreIndex, previousIndex);
		}

		@Override
		public String toString(){
			if(mViewPager!=null){
				return "BankFragment:" + mViewPager.getCurrentItem();
			}else{
				return "BankFragment";
			}
		}

		private PagerAdapter adapter = new PagerAdapter() {

			@Override
			public int getCount() {
				return list.length;
			}

			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {
				return arg0 == arg1;
			}

			@Override
			public Object instantiateItem(ViewGroup vg, int position) {
				View v = list[position];
				if (v.getParent() != null) {
					((ViewGroup) v.getParent()).removeView(v);
				}
				vg.addView(v);
				return v;
			}

			@Override
			public void destroyItem(ViewGroup vg, int s, Object o) {}
		};
		private AdapterView.OnItemClickListener accountClickListener = new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				Object o = arg0.getAdapter().getItem(position);
				if (o instanceof BankAccount) {
					selectedId = ((BankAccount) o)._id;
					swithclist(selectedId);
				}
			}
		};

		private boolean change2thepage(int i) {
			if (i == previousIndex) { return false; }
			switch (i) {
			case 0: {
				bankpay_front.setVisibility(View.VISIBLE);
				bankpay_topay.setVisibility(View.INVISIBLE);
				bankpay_schedule.setVisibility(View.INVISIBLE);
				bankpay_front.invalidate();
			}
				break;
			case 1: {
				bankpay_front.setVisibility(View.INVISIBLE);
				bankpay_topay.setVisibility(View.VISIBLE);
				bankpay_schedule.setVisibility(View.INVISIBLE);
				bankpay_topay.invalidate();
			}
				break;
			case 2: {
				bankpay_front.setVisibility(View.INVISIBLE);
				bankpay_topay.setVisibility(View.INVISIBLE);
				bankpay_schedule.setVisibility(View.VISIBLE);
				bankpay_schedule.invalidate();
			}
				break;
			}
			previousIndex = i;
			return true;
		}

		private void saveProfile() {
			EditText phoneview = (EditText) this.profile.findViewById(R.id.info_update_phone);
			EditText emailview = (EditText) this.profile.findViewById(R.id.info_update_email);
			EditText addressview = (EditText) this.profile.findViewById(R.id.info_update_address);
			String phonephase = phoneview.getText().toString().trim();
			String emailphase = emailview.getText().toString().trim();
			String addressphase = addressview.getText().toString().trim();
			// Basic checking TODO improvement required
			if (phonephase.equals("")) {
				Toast.makeText(this.getActivity().getApplicationContext(), "phone cannot be empty", Toast.LENGTH_LONG).show();
				return;
			} else if (emailphase.equals("")) {
				Toast.makeText(this.getActivity().getApplicationContext(), "email cannot be empty", Toast.LENGTH_LONG).show();
				return;
			} else if (addressphase.equals("")) {
				Toast.makeText(this.getActivity().getApplicationContext(), "address cannot be empty", Toast.LENGTH_LONG).show();
				return;
			}
			Activity act = this.getActivity();
			if (act instanceof FrameActivity) {
				SQLiteDatabase db = ((FrameActivity) act).getDatabaseReferenceFromActivity();
				db.beginTransaction();
				ContentValues values = new ContentValues();
				values.put(Common.Tables.BankInfo.address, addressphase);
				values.put(Common.Tables.BankInfo.email, emailphase);
				values.put(Common.Tables.BankInfo.phone, phonephase);
				// no flag column
				long result = db.update(Common.Tables.BANKINFO, values, null, null);
				if (result <= 0) {
					Log.v("mark", "bank saveProfile result <= 0");
				}
				db.setTransactionSuccessful();
				db.endTransaction();
			}
		}

		private void readProfile() {
			Log.v("mark", "reading profile");
			// retrieve view object
			TextView first = (TextView) this.profile.findViewById(R.id.info_current_first);
			TextView last = (TextView) this.profile.findViewById(R.id.info_current_last);
			TextView phone = (TextView) this.profile.findViewById(R.id.info_current_phone);
			TextView email = (TextView) this.profile.findViewById(R.id.info_current_email);
			TextView address = (TextView) this.profile.findViewById(R.id.info_current_address);
			// no longer use last name since only username is displayed
			last.setText("");
			Activity act = this.getActivity();
			if (act instanceof FrameActivity) {
				SQLiteDatabase db = ((FrameActivity) act).getDatabaseReferenceFromActivity();
				db.beginTransaction();
				Cursor cur = db.query(Common.Tables.BANKINFO, null, null, null, null, null, null);
				BankInfo info;
				if (cur.moveToFirst()) {
					info = new BankInfo(cur);
				} else {
					info = new BankInfo();
				}
				cur.close();
				// get the username
				String username = ((FrameActivity) act).getMobileStudyApplication().getUserInfo().username;
				first.setText(username);
				phone.setText(info.phone);
				email.setText(info.email);
				address.setText(info.address);
				db.setTransactionSuccessful();
				db.endTransaction();
			}
		}

		private void swithclist(int id) {
			ListView tranlist = (ListView) account.findViewById(R.id.trans_list);
			ListView acclist = (ListView) account.findViewById(R.id.account_list);
			Log.v("mark", "before transaction flag is " + transFlag);
			if (transFlag) {
				tranlist.setVisibility(View.INVISIBLE);
				acclist.setVisibility(View.VISIBLE);
			} else {
				MyArrayListAdapter<BankTrans> transadapter = getTransListAapater(id);
				tranlist.setAdapter(transadapter);
				acclist.setVisibility(View.INVISIBLE);
				// acclist.setLayoutParams(new
				// ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,1));
				tranlist.setVisibility(View.VISIBLE);
				FrameActivity act = (FrameActivity) this.getActivity();
				act.recordProcedureData(this.toString());
			}
			transFlag = !transFlag;
		}

		private MyArrayListAdapter<BankAccount> getAccountListAdapter() {
			Activity act = this.getActivity();
			if (act instanceof ProjectInterface.ActivityInterface) {
				SQLiteDatabase db = ((ProjectInterface.ActivityInterface) act).getDatabaseReferenceFromActivity();
				db.beginTransaction();
				String table = Common.Tables.ACCOUNT;
				Cursor cur = db.query(table, null, null, null, null, null, null);
				BankAccount[] acclist = new BankAccount[cur.getCount()];
				if (cur.moveToFirst()) {
					int i = 0;
					do {
						acclist[i] = new BankAccount(cur);
						i += 1;
					} while (cur.moveToNext());
				}
				cur.close();
				db.setTransactionSuccessful();
				db.endTransaction();
				return new MyArrayListAdapter<BankAccount>(this.getActivity(), acclist);
			} else {
				return new MyArrayListAdapter<BankAccount>(this.getActivity(), new BankAccount[0]);
			}
		}

		private MyArrayListAdapter<BankTrans> getTransListAapater(int acc_id) {
			Activity act = this.getActivity();
			if (act instanceof ProjectInterface.ActivityInterface) {
				SQLiteDatabase db = ((ProjectInterface.ActivityInterface) act).getDatabaseReferenceFromActivity();
				String table = Common.Tables.TRANS;
				db.beginTransaction();
				Cursor cur1 = db.query(table, null, null, null, null, null, null);
				cur1.close();
				String selection = Common.Tables.Transaction.acc_id + " = ?";
				String[] selectionArgs = { "" + acc_id };
				Cursor cur = db.query(table, null, selection, selectionArgs, null, null, null);
				BankTrans[] translist = new BankTrans[cur.getCount()];
				if (cur.moveToFirst()) {
					int i = 0;
					do {
						translist[i] = new BankTrans(cur);
						i += 1;
					} while (cur.moveToNext());
				}
				db.setTransactionSuccessful();
				db.endTransaction();
				return new MyArrayListAdapter<BankTrans>(this.getActivity(), translist);
			} else {
				return new MyArrayListAdapter<BankTrans>(this.getActivity(), new BankTrans[0]);
			}
		}
//		, ProjectInterface.FragmentRecordInterface
//		@Override
//		public String getMajorViewString() {
//			return "current view page:" + this.mViewPager.getCurrentItem();
//		}
	}
	public static class BankInfo {

		String email, phone, address;
		int _id;

		public BankInfo(int _id, String email, String phone, String address) {
			this.email = email;
			this.phone = phone;
			this.address = address;
			this._id = _id;
		}

		public BankInfo(Cursor cur) {
			this(cur.getInt(cur.getColumnIndex(Common.Tables.BankInfo._id)), cur.getString(cur.getColumnIndex(Common.Tables.BankInfo.email)), cur.getString(cur
					.getColumnIndex(Common.Tables.BankInfo.phone)), cur.getString(cur.getColumnIndex(Common.Tables.BankInfo.address)));
		}

		public BankInfo() {
			// use the default one in case of error
			this.phone = "123456789";
			this.email = "example@email.com";
			this.address = "Coover 1016";
		}
	}
	// _id name type balance
	public static class BankAccount implements MyArrayListAdapter.GenerateView {

		int _id;
		String name, type;
		float balance;

		public BankAccount(int _id, String name, String type, float balance) {
			this._id = _id;
			this.name = name;
			this.type = type;
			this.balance = balance;
		}

		public ContentValues toContentValues() {
			ContentValues result = new ContentValues();
			result.put(Common.Tables.Account._id, _id);
			result.put(Common.Tables.Account.name, name);
			result.put(Common.Tables.Account.type, type);
			result.put(Common.Tables.Account.balance, balance);
			return result;
		}

		public BankAccount(Cursor cur) {
			this(cur.getInt(cur.getColumnIndex(Common.Tables.Account._id)), cur.getString(cur.getColumnIndex(Common.Tables.Account.name)), cur
					.getString(cur.getColumnIndex(Common.Tables.Account.type)), cur.getFloat(cur.getColumnIndex(Common.Tables.Account.balance)));
		}

		@Override
		public View getView(Activity act, int position, View convertView, ViewGroup parent) {
			LayoutInflater inflator = act.getLayoutInflater();
			View ground = inflator.inflate(R.layout.view_bank_account, parent, false);
			TextView accountname = (TextView) ground.findViewById(R.id.account_name);
			TextView accountBalance = (TextView) ground.findViewById(R.id.account_balance);
			accountname.setText(name);
			accountBalance.setText("$" + balance);
			return ground;
		}

		@Override
		public View getDropDownView(Activity act, int position, View convertView, ViewGroup parent) {
			TextView tv = new TextView(act.getApplicationContext());
			tv.setText(name);
			return tv;
		}
	}
	// acc_id date event trans balance
	public static class BankTrans implements MyArrayListAdapter.GenerateView {

		int _id, acc_id;
		String date, event;
		double trans, balance;

		public BankTrans(Cursor cur) {
			this(cur.getInt(cur.getColumnIndex(Common.Tables.Transaction._id)), cur.getInt(cur.getColumnIndex(Common.Tables.Transaction.acc_id)), cur.getString(cur
					.getColumnIndex(Common.Tables.Transaction.date)), cur.getString(cur.getColumnIndex(Common.Tables.Transaction.event)), cur.getDouble(cur
					.getColumnIndex(Common.Tables.Transaction.trans)), cur.getDouble(cur.getColumnIndex(Common.Tables.Transaction.balance)));
		}

		public BankTrans(int _id, int acc_id, String date, String event, double trans, double balance) {
			this._id = _id;
			this.acc_id = acc_id;
			this.date = date;
			this.event = event;
			this.trans = trans;
			this.balance = balance;
		}

		@Override
		public View getView(Activity act, int position, View convertView, ViewGroup parent) {
			LayoutInflater inflator = act.getLayoutInflater();
			View ground = inflator.inflate(R.layout.view_bank_transhistory, parent, false);
			TextView dateview = (TextView) ground.findViewById(R.id.trans_date);
			TextView amountview = (TextView) ground.findViewById(R.id.trans_amount);
			TextView eventview = (TextView) ground.findViewById(R.id.trans_event);
			dateview.setText(date);
			amountview.setText("$" + trans);
			eventview.setText(event);
			return ground;
		}

		@Override
		public View getDropDownView(Activity act, int position, View convertView, ViewGroup parent) {
			return null;
		}
	}
	public static class ScheduledPay implements MyArrayListAdapter.GenerateView {

		String date, payee, accountname;
		int amount, _id;

		public ScheduledPay(Cursor cur) {
			this(cur.getInt(cur.getColumnIndex(Common.Tables.BankPay._id)), cur.getString(cur.getColumnIndex(Common.Tables.BankPay.accountName)), cur.getString(cur
					.getColumnIndex(Common.Tables.BankPay.payee)), cur.getInt(cur.getColumnIndex(Common.Tables.BankPay.amount)), cur.getString(cur.getColumnIndex(Common.Tables.BankPay.date)));
		}

		public ScheduledPay(int _id, String accountname, String payee, int amount, String date) {
			this._id = _id;
			this.date = date;
			this.payee = payee;
			this.amount = amount;
			this.accountname = accountname;
		}

		@Override
		public View getView(Activity act, int position, View convertView, ViewGroup parent) {
			LayoutInflater inf = act.getLayoutInflater();
			View ground = inf.inflate(R.layout.view_bank_scheduled, parent, false);
			TextView dateview = (TextView) ground.findViewById(R.id.scheduled_date);
			TextView payeeview = (TextView) ground.findViewById(R.id.scheduled_payee);
			TextView amountview = (TextView) ground.findViewById(R.id.scheduled_amount);
			dateview.setText(date);
			payeeview.setText(payee);
			amountview.setText(amount + "");
			return ground;
		}

		@Override
		public View getDropDownView(Activity act, int position, View convertView, ViewGroup parent) {
			return null;
		}
	}

	public static void logString(String... ss) {
		Log.v("mark", Arrays.toString(ss));
	}

	private static String[] getNumString(int max) {
		String[] result = new String[max];
		for (int i = 0; i < max; i++) {
			result[i] = "" + (i + 1);
		}
		return result;
	}

	private static String[] getNumString(int from, int to) {
		String[] result = new String[to - from + 1];
		for (int i = 0; i < result.length; i++) {
			result[i] = "" + (from + i);
		}
		return result;
	}
}
