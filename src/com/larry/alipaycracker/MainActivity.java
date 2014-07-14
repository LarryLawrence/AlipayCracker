/*
 * δ���ܵ���������  �� ���ܺ�(des)��user idƴ�ӣ�Ȼ���ƴ�Ӻ���ַ�����sha1����,��ΪgesturePwd��
 * ��ʵ��Des.javaû���õ�����Ϊ�޸����벻��Ҫ����user id��
 * ����������е�ʱ���л�����Ľ���֮�����л����������̵�rootȨ�޾��Ѿ���ʧ�ˣ����ʱ�����޸������ǲ��еģ��������½��롣
 * ��Ҫ���֧����8.1����Ͱ汾ʹ�á����԰汾�õ���8.1.0.043001
 * Larry 2014
 */
package com.larry.alipaycracker;

import jackpal.androidterm.Exec;

import java.io.FileDescriptor;
import java.io.FileOutputStream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	String szUerIdString;
	TextView tvStatus;
	EditText etEncryptUserid;
	EditText etMyPwd;
	Button btnOK;
	
	final int[] processId = new int[1];
	final FileDescriptor fd = Exec.createSubprocess("/system/bin/sh", "-",
			null, processId);

	final FileOutputStream out = new FileOutputStream(fd);

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		try{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		szUerIdString = "";
		tvStatus = (TextView) findViewById(R.id.textStatus);
		etEncryptUserid = (EditText) findViewById(R.id.editEncryptUserId);
		etMyPwd = (EditText) findViewById(R.id.editMyselfPwd);
		btnOK = (Button) findViewById(R.id.btnSetting);

		if (RootUtils.hasRoot() == 0) {
			tvStatus.setText("������ֻ����ROOT�����ֻ������У�");
			return;
		}

		if (!isAppInstalled("com.eg.android.AlipayGphone")) {
			tvStatus.setText("��ȷ�����Ѿ���װ��֧����Ǯ����");
			return;
		}
		
		if(RootUtils.hasDB() == 0)
		{
			tvStatus.setText("û��DB");
			return;
		}

		String szUserId = getUserId();
//		String szUserId = "aa" ;
		if (!szUserId.isEmpty()) {

			szUerIdString = szUserId;
			etEncryptUserid.setText(szUserId);
			tvStatus.setText("��ȡuser id�ɹ����������Զ����������룡");

//			String szDecryptUserid = Des.decrypt(szUserId, "userInfo");
//			if (!szDecryptUserid.isEmpty()) {
//				etDecryptUserid.setText(szDecryptUserid);
//			} else {
//				tvStatus.setText("����user idʧ�ܣ�");
//			}

			btnOK.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					String szPwd = etMyPwd.getText().toString();
					if (szPwd.isEmpty()) {
						Toast.makeText(MainActivity.this,
								"���õ��Զ������벻��Ϊ�գ����������룡", Toast.LENGTH_LONG)
								.show();
					} else {
						StringBuilder sBuilder = new StringBuilder();
						sBuilder.append(szPwd);
						sBuilder.append(szUerIdString);

						String tmp = sBuilder.toString();
						String sha1 = SHA1.sha1(tmp);
						Log.v("TAG", sha1);

						if (!sha1.isEmpty()) {
							try {
								if (updateDatabaseGesturePwd(szUerIdString, sha1)) {
									tvStatus.setText("�����Զ�������ɹ���");
								} else {
									tvStatus.setText("�����Զ�������ʧ�ܣ�");
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			});
		} else {
			tvStatus.setText("��ȡuser idʧ�ܣ�");
		}
	}
	catch(Exception e)
	{
		e.printStackTrace();
		Log.e("HEY", "SOMETHING'S WRONG.");
	}
	}

	public boolean isAppInstalled(String uri) {
		PackageManager pm = getPackageManager();
		boolean installed = false;
		try {
			pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
			installed = true;
		} catch (PackageManager.NameNotFoundException e) {
			installed = false;
		}
		return installed;
	}

	// ��ȡ���ܵ�user id
	private String getUserId() throws Exception {
		String szRet = "";
		int USER_ID_INDEX = 4;

		// �޸����ݿ��ļ��Ķ�дȨ��
//		RootUtils
//				.runRootCommand("chmod 666 /data/data/com.eg.android.AlipayGphone/databases/alipayclient.db");
//		RootUtils
//				.runRootCommand("chmod 666 /data/data/com.eg.android.AlipayGphone/databases/alipayclient.db-journal");

		
		//By Larry �޸Ķ�дȨ��
		
		try {
			
			//run ��chmod 777 getroot�� and then run��getroot��immediately
			
			//question is , after runing this,how to start phase2 and copy su to system/bin? May 23,Larry
			
			String command = "su\n";//����ִ�е�ʱ��ҲҪ�ö��su��
			out.write(command.getBytes());
			out.flush();
			command = "chmod 777 /data/data/com.eg.android.AlipayGphone/databases/alipayclient.db\n";
			out.write(command.getBytes());
			out.flush();
			command = "su\n";//����ִ�е�ʱ��ҲҪ�ö��su��
			out.write(command.getBytes());
			out.flush();
			command = "chmod 777 /data/data/com.eg.android.AlipayGphone/databases/alipayclient.db-journal";
			out.write(command.getBytes());
			out.flush();
		} catch (Exception ex) {
			ex.printStackTrace();
			Log.e("chmod 777", ex.getMessage());
		}
		
		
		try {
			Context context = createPackageContext(
					"com.eg.android.AlipayGphone",
					Context.CONTEXT_IGNORE_SECURITY);
			
			String command = "su\n";
			out.write(command.getBytes());
			out.flush();
			
			SQLiteDatabase db = context.openOrCreateDatabase("alipayclient.db",
					0, null);
			Cursor cursor = db.rawQuery("select * from userinfo", null);
			if (cursor.moveToFirst()) {
				szRet = cursor.getString(USER_ID_INDEX);
			}
			db.close();
		} catch (NameNotFoundException e1) {
			e1.printStackTrace();
		}

		return szRet;
	}

	// �޸���������
	@SuppressLint("NewApi")
	private boolean updateDatabaseGesturePwd(String szUerId, String szPwd) throws Exception {
		boolean bRet = false;

		if (szPwd.isEmpty() || szUerId.isEmpty()) {
			return bRet;
		}

		try {
			Context context = createPackageContext(
					"com.eg.android.AlipayGphone",
					Context.CONTEXT_IGNORE_SECURITY);
			
			String command = "su\n";
			out.write(command.getBytes());
			out.flush();
			
			SQLiteDatabase db = context.openOrCreateDatabase("alipayclient.db",
					0, null);
			ContentValues cv = new ContentValues();
			cv.put("gesturePwd", szPwd);
			String[] args = { String.valueOf(szUerId) };
			int n = db.update("userinfo", cv, "userId=?", args);
			if (n > 0) {
				bRet = true;
			}
			db.close();
		} catch (NameNotFoundException e1) {
			e1.printStackTrace();
		}

		return bRet;
	}

}
