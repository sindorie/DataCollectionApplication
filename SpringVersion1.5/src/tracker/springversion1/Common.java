package tracker.springversion1;

import java.util.HashMap;
import java.util.Map;

import android.util.SparseArray;

public class Common {
	
	public final static int durationBetweenSegment = 1000*5; //5s for testing
	public final static int durationBetweenFinalUploading = 1000*10; // 10s
	public final static int duratinoBetweenSkip = 100*5; // .5s
	
	public final static int ReminderNotificationId = 4568;
	public final static int AllFinishedNoticationId = 4583;
	
	public static SparseArray<Integer> idMap = new SparseArray<Integer>();
	
	public static class StreamChannel{
		public final static int SENSOR = 0, MOTION =1, KEY = 2, VIEW =3;
	}
	
	static {
		// idMap.put(0, R.id.account_arrow); -- example TODO

		idMap.put(1, R.id.right_ans_submit);
		idMap.put(2, R.layout.view_taskitem);// TODO
		idMap.put(3, R.id.contact_submit); 
		idMap.put(4, R.id.bottom_submit);
		idMap.put(5, R.id.transfer_button);
		idMap.put(6, R.id.news_detailelement_submit);
		idMap.put(7, R.id.social_messagesend_submit);
		idMap.put(8, R.id.social_news_comment_bottom_button);
		
		idMap.put(9, R.id.social_news_addstatus_submit);
		idMap.put(10, R.id.social_photo_pageview_submit);
		idMap.put(11, R.id.galleryimage);
		idMap.put(12, R.id.schedule_submit);
		idMap.put(13, R.id.infosubmit);
		
//		1	R.id.left_panel_submit
//		2	R.layout.view_taskitem
//		3	R.id.contact_submit
//		4	R.id.bottom_submit
//		5	R.id.transfer_button
//		6	R.id.news_detailelement_submit
//		7	R.id.social_messagesend_submit
//		8	R.id.social_news_comment_bottom_button
//		9	R.id.social_news_addstatus_submit
//		10	R.id.social_photo_pageview_submit
//		11	R.id.galleryimage
//		12	R.id.schedule_submit
//		13	R.id.infosubmit

	}
	
	
	public static class RequestIdMultiCast{
		public final static int TOUCHIMAGE = -10;
	}
	
	
	public static class CSV {
		public static String DELIMITER = "\\^";
	}
	
	public static String[] defaultBankProfile = new String[]{
		 "YOU", "", "123456789",
			"example@email.com", "Coover 1016"
	};

	public static class Task{
		public final static int CONTACT_id = 1;
		public final static int FOODIE_id = 2;
		public final static int GALLERY_id = 3;
		public final static int BANK_id = 4;
		public final static int NEWS_id = 5;
		public final static int SOCIAL_id = 6;
		
		public final static String Contact = "Contact";
		public final static String Foodie = "Foodie";
		public final static String Gallery = "Gallery";
		public final static String Bank = "Bank";
		public final static String News = "News";
		public final static String Social = "Social";
		
		public static Map<String,Integer> taskNameToId = new HashMap<String,Integer>();
		static{
			taskNameToId.put(Contact, CONTACT_id);
			taskNameToId.put(Foodie, FOODIE_id);
			taskNameToId.put(Gallery, GALLERY_id);
			taskNameToId.put(Bank, BANK_id);
			taskNameToId.put(News, NEWS_id);
			taskNameToId.put(Social, SOCIAL_id);
		}
		public static final String[] taskList = {Contact,Foodie,Gallery,Bank,News,Social};
	}
	
	
	public static class Tables {
		public final static String REVIEW = "review";
		public final static String CONTACT = "contact";
		public final static String RESTAURANT = "restaurant";
		public final static String ACCOUNT = "account";
		public final static String TRANS = "trans";
		public final static String NEWSFEED = "newsfeed";
		public final static String NEWSCOMMENT = "newscomment";
		public final static String FRIEND = "friend";
		public final static String CONVERSATION = "conversation";
		public final static String CONVERSATION_DETAIL = "conversationdetail";
		public final static String SOCIALPHOTO = "socialphoto";
		public final static String NEWSARTICLE = "newsarticle";
		public final static String INSTRUCTION = "instruction";
		public final static String USERINFO = "userinfo";
		public final static String BANKPAY = "bankpay";
		public final static String BANKINFO = "bankinfo";
		
		public static String[] TABLENAMES = { 
				Common.Tables.CONTACT,
				Common.Tables.RESTAURANT, 
				Common.Tables.REVIEW,
				Common.Tables.ACCOUNT, 
				Common.Tables.TRANS,
				Common.Tables.NEWSFEED, 
				Common.Tables.NEWSCOMMENT ,
				Common.Tables.FRIEND ,
				Common.Tables.CONVERSATION ,
				Common.Tables.CONVERSATION_DETAIL ,
				Common.Tables.SOCIALPHOTO,
				Common.Tables.NEWSARTICLE,
				Common.Tables.INSTRUCTION,
				Common.Tables.USERINFO,
				Common.Tables.BANKPAY,
				Common.Tables.BANKINFO
		};
		public static String[] FILENAMES = { 
				"csv/contact.csv",
				"csv/restaurant.csv", 
				"csv/review.csv", 
				"csv/account.csv",
				"csv/trans.csv", 
				"csv/newsfeed.csv",
				"csv/newsfeedcomment.csv",	//TODO
				"csv/friend.csv",
				"csv/conversation.csv",
				"csv/conversation_detail.csv",
				"csv/socialphoto.csv",
				"csv/newsarticle.csv",
				"csv/instruction.csv",	//TODO
				"csv/userinfo.csv",
				null,	//currentlyScheculed bank pay
				"csv/bankinfo.csv"
		};

		public static class Restaurant {
			public final static String _id = "_id";
			public final static String rate = "rate";
			public final static String name = "name";
			public final static String phone = "phone";
			public final static String address = "address";
			public final static String image = "image";
			public final static String description = "description";
		}

		public static class Contact {
			public final static String _id = "_id";
			public final static String first = "first";
			public final static String last = "last";
			public final static String phone = "phone";
			public final static String email = "email";
			public final static String address = "address";
			public final static String photo = "photo";
		}

		public static class Review {
			public final static String _id = "_id";
			public final static String rest_id = "rest_id";
			public final static String username = "username";
			public final static String stars = "stars";
			public final static String photo = "photo";
			public final static String comment = "comment";
		}

		public static class Account {
			public final static String _id = "_id";
			public final static String name = "name";
			public final static String type = "type";
			public final static String balance = "balance";
		}

		public static class Transaction {
			public final static String _id = "_id";
			public final static String acc_id = "acc_id";
			public final static String date = "date";
			public final static String event = "event";
			public final static String trans = "trans";
			public final static String balance = "balance";
		}

		public static class NewsFeed {
			public final static String _id = "_id";
			public final static String isusers = "isusers";
			public final static String first = "first";
			public final static String last = "last";
			public final static String photo = "photo";
			public final static String comment = "comment";
			public final static String likes = "likes";
			public final static String liked = "liked";
			public final static String status = "status";
		}

		// _id first last photo comment likfirstLinees liked status
		public static class NewsComment {
			public final static String _id = "_id";
			public final static String news_id = "news_id";
			public final static String isuers = "isusers";
			public final static String first = "first";
			public final static String last = "last";
			public final static String photo = "photo";
			public final static String likes = "likes";
			public final static String liked = "liked";
			public final static String comment = "comment";
		}

		public static class Friend {
			public final static String _id = "_id";
			public final static String first = "first";
			public final static String last = "last";
			public final static String isfriend = "isfriend";
			public final static String photo = "photo";
		}

		public static class Conversation {
			public final static String _id = "_id";
			public final static String friend_id = "friend_id";
			public final static String hasRead = "hasRead";
			public final static String time = "time";
			public final static String message = "message";
		}

		public static class ConversationDetail {
			public final static String _id = "_id";
			public final static String conversation_id = "conversation_id";
			public final static String friend_id = "friend_id";
			public final static String time = "time";
			public final static String message = "message";
		}
		
		public static class SocialPhoto{
			public final static String _id = "_id";
			public final static String name = "name";
			public final static String caption = "caption";
		}
		
		public static class NewsArticle{
			public final static String _id = "_id";
			public final static String titile = "title";
			public final static String author = "author";
			public final static String photo = "photo";
			public final static String comment = "comment";
			public final static String content = "content";
		}
		
		public static class Instruction{
			public final static String _id = "_id";
			public final static String task_id = "task_id";
			public final static String seg_id = "seg_id";
			public final static String instruction = "instruction";
			public final static String hint = "hint";
			public final static String imagename = "imagename";
			public final static String verif_src = "verif_src";
			public final static String verif_com = "verif_com";
			public final static String operator = "operator";
			public final static String cur_count = "cur_count";
			public final static String qualifier = "qualifier";
			public final static String difference = "difference";
			public final static String err_msg = "err_msg";
			public final static String event_obj = "event_obj";
			public final static String event = "event";
			public final static String start = "start";
			public final static String end = "end";
			public final static String state = "state";
		}
		
		public static class UserInfo{
			public final static String _id = "_id";
			public final static String inst_id = "inst_id";
			public final static String task_id = "task_id";
			public final static String seg_id = "seg_id";
			public final static String finished = "finished";
			public final static String uploaded = "uploaded";
		}
		
		public static class BankPay{
			public final static String _id = "_id";
			public final static String accountName = "accountName";
			public final static String payee = "payee";
			public final static String amount = "amount";
			public final static String date = "date";
		}
		public static class BankInfo{
			public final static String _id = "_id";
			public final static String email = "email";
			public final static String phone = "phone";
			public final static String address = "address"; 
		}
		
		
		public static String[] CREATION = {

				"CREATE TABLE contact(_id INTEGER PRIMARY KEY AUTOINCREMENT, "
						+ "first TEXT, "
						+ "last TEXT, "
						+ "phone TEXT, "
						+ "email TEXT,"
						+ "address TEXT, "
						+ "photo TEXT,"
						+ "new INTEGER DEFAULT 1);",
						
				// restaurant-related table creation
				"CREATE TABLE restaurant(_id INTEGER PRIMARY KEY AUTOINCREMENT, "
						+ "rate REAL, "
						+ "name TEXT, "
						+ "phone TEXT, "
						+ "address TEXT, "
						+ "image TEXT, "
						+ "description TEXT,"
						+ "new INTEGER DEFAULT 1);",
						
				"CREATE TABLE review(_id INTEGER PRIMARY KEY AUTOINCREMENT, "
						+ "rest_id INTEGER, "
						+ "username TEXT, "
						+ "stars INTEGER, "
						+ "photo TEXT, "
						+ "comment TEXT,"
						+ "new INTEGER DEFAULT 1);",
						
				// bank-related table creation
				"CREATE TABLE account(_id INTEGER PRIMARY KEY AUTOINCREMENT, "
						+ "name TEXT, "
						+ "type TEXT, "
						+ "balance REAL,"
						+ "new INTEGER DEFAULT 1);",
						
				"CREATE TABLE trans(_id INTEGER PRIMARY KEY AUTOINCREMENT, "
						+ "acc_id INTEGER, "
						+ "date TEXT, "
						+ "event TEXT, "
						+ "trans REAL, "
						+ "balance REAL,"
						+ "new INTEGER DEFAULT 1);",
						
				"CREATE TABLE newsfeed(_id INTEGER PRIMARY KEY AUTOINCREMENT, "
						+ "isusers INTEGER DEFAULT 0," 
						+ "first TEXT, "
						+ "last TEXT, " 
						+ "photo TEXT, " 
						+ "comment INTEGER, "
						+ "likes INTEGER DEFAULT 0, "
						+ "liked INTEGER DEFAULT 0, " 
						+ "status TEXT,"
						+ "new INTEGER DEFAULT 1);",

				"CREATE TABLE newscomment( _id INTEGER PRIMARY KEY AUTOINCREMENT, "
						+ "news_id INTEGER," 
						+ "isusers INTEGER DEFAULT 0,"
						+ "first TEXT," 
						+ "last TEXT," 
						+ "photo TEXT,"
						+ "likes INTEGER DEFAULT 0,"
						+ "liked INTEGER DEFAULT 0," 
						+ "comment TEXT ,"
						+ "new INTEGER DEFAULT 1);",

				"CREATE TABLE friend(_id INTEGER PRIMARY KEY AUTOINCREMENT,"
						+ "first TEXT,"
						+ "last TEXT,"
						+ "isfriend INTEGER DEFAULT 0," 
						+ "photo TEXT,"
						+ "new INTEGER DEFAULT 1);",

				"CREATE TABLE conversation(_id INTEGER PRIMARY KEY AUTOINCREMENT,"
						+ "friend_id INTEGER," 
						+ "hasRead INTEGER,"
						+ "time INTEGER," 
						+ "message TEXT,"
						+ "new INTEGER DEFAULT 1);",

				"CREATE TABLE conversationdetail(_id INTEGER PRIMARY KEY AUTOINCREMENT,"
						+ "conversation_id INTEGER," 
						+ "friend_id INTEGER,"
						+ "time INTEGER," 
						+ "message TEXT,"
						+ "new INTEGER DEFAULT 1);",
						
				"CREATE TABLE socialphoto(_id INTEGER PRIMARY KEY AUTOINCREMENT,"
						+ "name TEXT," 
						+ "caption TEXT,"
						+ "new INTEGER DEFAULT 1);",
						
				"CREATE TABLE newsarticle(_id INTEGER PRIMARY KEY AUTOINCREMENT,"
						+ "title TEXT,"
						+ "photo TEXT,"
						+ "author TEXT,"
						+ "comment TEXT DEFAULT \'\',"
						+ "content TEXT,"
						+ "new INTEGER DEFAULT 1);",
						
				"CREATE TABLE instruction(_id INTEGER PRIMARY KEY AUTOINCREMENT,"
						+ "task_id INTEGER,"
						+ "seg_id INTEGER,"
						+ "instruction TEXT,"
						+ "hint TEXT,"
						+ "imagename TEXT,"
						+ "verif_src INTEGER DEFAULT 1,"
						+ "verif_com TEXT,"
						+ "operator TEXT DEFAULT \'-\',"
						+ "cur_count INTEGER DEFAULT 0,"
						+ "qualifier TEXT,"
						+ "difference TEXT,"
						+ "err_msg TEXT,"
						+ "event_obj INTEGER DEFAULT 0,"
						+ "event TEXT,"
						+ "start INTEGER DEFAULT 0,"
						+ "end INTEGER DEFAULT 0,"
						+ "state INTEGER);",
						
						
				"CREATE TABLE userinfo(_id TEXT DEFAULT \'-2\', "
						+ "inst_id INTEGER DEFAULT 0,"
						+ "task_id INTEGER DEFAULT 0,"
						+ "seg_id INTEGER DEFAULT 0,"
						+ "finished INTEGER DEFAULT 0,"
						+ "uploaded INTEGER DEFAULT 0);",
						
				"CREATE TABLE bankpay(_id INTEGER PRIMARY KEY AUTOINCREMENT, "
						+ "accountName TEXT,"
						+ "payee TEXT,"
						+ "amount REAL DEFAULT 0,"
						+ "date TEXT,"
						+ "new INTEGER DEFAULT 1);",
						
				"CREATE TABLE bankinfo(_id INTEGER PRIMARY KEY AUTOINCREMENT, "
						+ "email TEXT,"
						+ "phone TEXT,"
						+ "address TEXT);"
			};
	}

	public static class FakeData {
		public static String[] Payee = { "payee1", "payee2" };
	}

	public static class Data {
		public static String[] Mouth = { "Jan.", "Feb.", "Mar.", "Apr.", "May",
				"Jun.", "Jul.", "Aug.", "Sep.", "Oct.", "Nov.", "Dec." };
	}

	// "CREATE TABLE contact(_id INT, first TEXT, last TEXT, phone TEXT, address TEXT, photo TEXT)",
	// //restaurant-related table creation
	// "CREATE TABLE restaurant(_id INT, rate REAL, name TEXT, phone TEXT, address TEXT, image TEXT, description TEXT)",
	// "CREATE TABLE review(_id INT, rest_id INT, user_id, stars INT, photo TEXT, comment TEXT)"
}
