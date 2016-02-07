package tracker.utility;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity; 
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.DefaultedHttpParams;
 















import org.apache.http.util.ByteArrayBuffer;

import android.R;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;


 
public class SSLCommunication {
	DefaultHttpClient client;
	Context context;
	public SSLCommunication(Context context){
		this.context =context;
	}
	public void sendFromInputStream(InputStream input, long length){
		client = new MyHttpClient(context);
		HttpPost httppost = new HttpPost("https://csl.ece.iastate.edu/android/appdata.php");
		httppost.setEntity(new InputStreamEntity(input,length));
		HttpResponse response;
		try {
			response = client.execute(httppost);
			Log.v("mark","SSLCommunication HttpResponse:"+response.toString());
		} catch (ClientProtocolException e) {
			Log.v("mark","SSLCommunication sendFile ClientProtocolException:"+e.getClass()+" with "+e.getMessage());
		} catch (IOException e) {
			Log.v("mark","SSLCommunication sendFile IOException:"+e.getClass()+" with "+e.getMessage());
		} 
	}
	 
	public void sendFromFile(final File myFile){
		Thread t = new Thread() {
			public void run(){
				FileEntity fileBody = new FileEntity(myFile, "txt");
				client = new MyHttpClient(context);
				HttpPost httppost = new HttpPost(host);

				httppost.setEntity(fileBody);
				HttpResponse response;
				try {
					response = client.execute(httppost);
					InputStream ins = response.getEntity().getContent();
					if(ins.available()>0){
						byte[] buf = new byte[256];
						ins.read(buf);
						Log.v("mark","reading:"+new String(buf));
					}else{
						Log.v("mark","reading: none");
					}
					
				} catch (ClientProtocolException e) {
					Log.v("mark","SSLCommunication sendFile ClientProtocolException:"+e.getClass()+" with "+e.getMessage());
				} catch (IOException e) {
					Log.v("mark","SSLCommunication sendFile IOException:"+e.getClass()+" with "+e.getMessage());
				} 
			}
		};
		t.start();
	}
	
	public void shutdown(){
		if(client != null) client.getConnectionManager().shutdown();
	}

	public void sendData(final ArrayList<NameValuePair> data )
	{
		Thread t = new Thread() {
			public void run(){
			    try{
			    	DefaultHttpClient client = new MyHttpClient(context);
			        HttpPost httppost = new HttpPost(host);

			        httppost.setEntity(new UrlEncodedFormEntity(data));
			        HttpResponse response = client.execute(httppost);
			        Log.v("mark", response.getEntity().toString());
			        InputStream in = response.getEntity().getContent();
			        byte[] buffer = new byte[300];
			        in.read(buffer);
			        Log.v("mark","reading:"+new String(buffer).trim());
			        
			        // response.getStatusLine();  // CONSIDER  Detect server complaints
      
			    }catch(Exception e){
			        Log.v("mark", "Error:  "+e.toString());
			    }  
			}
		};
		t.start();
	}
	
//	public boolean sendData(String label, String data, Context context){
//		
//		return false;
//	}
	String host = "https://csl.ece.iastate.edu/android/upload_file.php";
	public void sendFile(final File tosend){
		new Thread(new Runnable(){
			@Override
			public void run() { 
		    	DefaultHttpClient client = new MyHttpClient(context);
		        HttpPost post = new HttpPost(host);// + "uploadFile"
		        MultipartEntityBuilder multipartEntity = MultipartEntityBuilder.create();
		        multipartEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		        multipartEntity.addPart("file", new FileBody(tosend));
		        HttpEntity entity = multipartEntity.build();
		        post.setEntity(entity);
		       
		        HttpResponse response;
				try {
					response = client.execute(post);
					if(response!=null){
						InputStream in = response.getEntity().getContent();
				        byte[] buffer = new byte[300];
				        in.read(buffer);
				        Log.v("mark","reading:"+new String(buffer).trim());
					}
				} catch (ClientProtocolException e) {
					Log.v("mark","ClientProtocolException:"+e.getMessage());
				} catch (IOException e) {
					Log.v("mark","IOException:"+e.getMessage());
				}
		        
			}
		}).start();
	}

	public void transfer(final String filename){
		new Thread(new Runnable(){
			@Override
			public void run() { 
				sslFileTransfer(filename);
			}
		}).start();
	} 	

	
	private void sslFileTransfer(String filename){ 
		try {
			KeyStore trusted = KeyStore.getInstance("BKS");
			// Get the raw resource, which contains the keystore with
			// your trusted certificates (root and any intermediate certs)
			InputStream in = context.getResources().openRawResource(
					tracker.springversion1.R.raw.mykeystore
					);
			trusted.load(in, "mysecret".toCharArray());
			
			
			String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
			tmf.init(trusted);

			// Create an SSLContext that uses our TrustManager
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, tmf.getTrustManagers(), null);

			URL url = new URL(host);
			HttpsURLConnection urlConnection =
				    (HttpsURLConnection)url.openConnection();
				urlConnection.setSSLSocketFactory(context.getSocketFactory());

				transfer(urlConnection,filename);
			
			
//			SSLSocketFactory sf = new SSLSocketFactory(trusted);
//			// Hostname verification from certificate
//			// http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d4e506
//			sf.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
//			
//			Socket sslsocket = sf.createSocket();
//			sslsocket.setKeepAlive(true);
//			
//			InetSocketAddress address = new InetSocketAddress(host, 443);
//			sslsocket.connect(address);
//			
//			OutputStream sout = sslsocket.getOutputStream();
			
			
			
		} catch (KeyStoreException e) {
			Log.v("mark","KeyStoreException:"+e.getMessage());
		} catch (NoSuchAlgorithmException e) { 
			Log.v("mark","NoSuchAlgorithmException:"+e.getMessage());
		} catch (CertificateException e) { 
			Log.v("mark","CertificateException:"+e.getMessage());
		} catch (IOException e) { 
			Log.v("mark","IOException:"+e.getMessage());
		} catch (KeyManagementException e) {
			Log.v("mark","KeyManagementException:"+e.getMessage());
		} 
		
		
	} 
	
	private void transfer(HttpsURLConnection conn, String filename){
		DataOutputStream dos = null;
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024; 
		
		
		try {
			conn.setDoInput(true); // Allow Inputs
			conn.setDoOutput(true); // Allow Outputs
			conn.setUseCaches(false); // Don't use a Cached Copy
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("ENCTYPE", "multipart/form-data");
			conn.setRequestProperty("Content-Type",
					"multipart/form-data;boundary=" + boundary);
			

			dos = new DataOutputStream(conn.getOutputStream());

			

			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\""
					+filename + "\";filename=\"" + filename + "\"" + lineEnd);

			dos.writeBytes(lineEnd);
			FileInputStream fileInputStream = this.context.openFileInput(filename);
			bytesAvailable = fileInputStream.available();

			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];

			// read file and write it into form...
			bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			Log.v("mark","reading "+bytesRead+" bytes");
			while (bytesRead > 0) { 
				dos.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}

			// send multipart form data necesssary after file data...
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

			// Responses from the server (code and message)
			int serverResponseCode = conn.getResponseCode();
			String serverResponseMessage = conn.getResponseMessage();

			Log.v("mark", "HTTP Response is : "
					+ serverResponseMessage + ": " + serverResponseCode);
			fileInputStream.close();
			
			DataInputStream dis = new DataInputStream(conn.getInputStream());
			if(dis.available()>0){
				byte[] buf = new byte[256];
				dis.read(buf);
				Log.v("mark","line1:"+new String(buf));
			}else{
				Log.v("mark","line1:none");
			}
			
			
			// close the streams //
//			if(dis.available()>0){
//				byte[] buf = new byte[256];
//				dis.read(buf);
//				Log.v("mark","line2:"+new String(buf));
//			}else{
//				Log.v("mark","line2:none");
//			}
			dis.close();
			dos.flush();
			dos.close();
			
		} catch (ProtocolException e) {
			Log.v("mark","ProtocolException:"+e.getMessage());
		} catch (IOException e) {
			Log.v("mark","IOException:"+e.getMessage());
		}
		
		
		
	}
	
	
	public static class MyHttpClient extends DefaultHttpClient {

		final Context context;

		public MyHttpClient(Context context) {
			this.context = context;
		}

		@Override
		protected ClientConnectionManager createClientConnectionManager() {
			SchemeRegistry registry = new SchemeRegistry();
//			registry.register(new Scheme("http", PlainSocketFactory
//					.getSocketFactory(), 80));
			// Register for port 443 our SSLSocketFactory with our keystore
			// to the ConnectionManager
			registry.register(new Scheme("https", newSslSocketFactory(), 443));
			return new SingleClientConnManager(getParams(), registry);
		}

		private SSLSocketFactory newSslSocketFactory() {
			try {
				// Get an instance of the Bouncy Castle KeyStore format
				KeyStore trusted = KeyStore.getInstance("BKS");
				// Get the raw resource, which contains the keystore with
				// your trusted certificates (root and any intermediate certs)
				InputStream in = context.getResources().openRawResource(
						tracker.springversion1.R.raw.mykeystore
						);
			
				try {
					// Initialize the keystore with the provided trusted
					// certificates
					// Also provide the password of the keystore
					trusted.load(in, "mysecret".toCharArray());
				} finally {
					in.close();
				}
				// Pass the keystore to the SSLSocketFactory. The factory is
				// responsible
				// for the verification of the server certificate.
				SSLSocketFactory sf = new SSLSocketFactory(trusted);
				// Hostname verification from certificate
				// http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d4e506
				sf.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
//				 ALLOW_ALL_HOSTNAME_VERIFIER
				return sf;
			} catch (Exception e) {
				throw new AssertionError(e);
			}
		}
	}
	
	
	public boolean uploadFile( File tosend, String name, String encoding) {

		MyHttpClient http = new MyHttpClient(context);
//        SSLSocketFactory ssl =  (SSLSocketFactory)http.getConnectionManager().getSchemeRegistry().getScheme( "https" ).getSocketFactory(); 
//        ssl.setHostnameVerifier( SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER );
         
//        final String username = "xxx";
//        final String password = "xxx";
//        UsernamePasswordCredentials c = new UsernamePasswordCredentials(username,password);
//        BasicCredentialsProvider cP = new BasicCredentialsProvider(); 
//        cP.setCredentials(AuthScope.ANY, c); 
//        http.setCredentialsProvider(cP);
        
        HttpResponse res;
        try {
            HttpPost httpost = new HttpPost(host);
            MultipartEntity entity = new MultipartEntity(HttpMultipartMode.STRICT); 
            FileBody isb = new FileBody(tosend,"application/*");
            entity.addPart(name, isb);
            
//            for(int index=0; index < nameValuePairs.size(); index++) { 
//                ContentBody cb;
//                if(nameValuePairs.get(index).getName().equalsIgnoreCase("File")) { 
//                    File file = new File(nameValuePairs.get(index).getValue());
//                    //
//
//                    /*
//                    byte[] data = new byte[(int) file.length()];
//                    FileInputStream fis = new FileInputStream(file);
//                    fis.read(data);
//                    fis.close();
//
//                    ByteArrayBody bab = new ByteArrayBody(data,"application/*","File");
//                    entity.addPart(nameValuePairs.get(index).getName(), bab);
//                    */  
//                    entity.addPart(nameValuePairs.get(index).getName(), isb);
//                } else { 
//                    // Normal string data 
//                    cb =  new StringBody(nameValuePairs.get(index).getValue(),"", null);
//                    entity.addPart(nameValuePairs.get(index).getName(),cb); 
//                } 
//            } 


            httpost.setEntity(entity);
            res = http.execute(httpost);

            InputStream is = res.getEntity().getContent();
            BufferedInputStream bis = new BufferedInputStream(is);
            if(bis.available()>0){
            	byte[] buf = new byte[256];
            	bis.read(buf);
            	Log.v("mark", "reading:"+new String(buf));
            }else{
            	Log.v("mark","no response");
            }
            return  true;
           } 
        catch (ClientProtocolException e) {
        	Log.v("mark","ClientProtocolException"+e.getMessage());
            return true;
        } 
        catch (IOException e) {
        	Log.v("mark","IOException"+e.getMessage());
            return true;
        } 
}
}
