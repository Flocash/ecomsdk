package com.flocash.flocashecomgui;

import java.util.Stack;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;

public class MainActivity extends FragmentActivity {
	
	private Stack<Fragment> mStack;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mStack = new Stack<Fragment>();
		Fragment checkoutFragment = new CheckoutFragment();
		pushFragment(checkoutFragment);
	}
	
	private void showFragment(Fragment fragment, boolean isPushing) {
		if (fragment != null) {
			FragmentManager fragmentManager = getSupportFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			if(isPushing)
			{
				fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
			} else {
				fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
			}
			fragmentTransaction.replace(R.id.container, fragment);
			fragmentTransaction.commit();
		}
	}

	protected void popFragment() {
		if (mStack.size() > 1) {
			// lay stack se pop
			Fragment fragment = mStack.get(mStack.size() - 2);
			mStack.pop();
			showFragment(fragment, false);
		}
	}

	protected void pushFragment(Fragment fragment) {
		if (fragment != null) {
			mStack.add(fragment);
			showFragment(fragment, true);
		}
	}

	@Override
	public void onBackPressed() {
		if(mStack.size() > 1)
		{
			popFragment();
		}else
		{
			super.onBackPressed();
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(mStack.size() == 0)
		{
			return;
		}		
		mStack.lastElement().onActivityResult(requestCode, resultCode, data);
	}

}
