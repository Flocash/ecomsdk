package com.flocash.flocashecomgui;

import com.flocash.flocashecomgui.PaymentResultFragment.PaymentResultPushedEnum;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class VirtualTerminalFragment extends Fragment{
	
	private static final String KEY_URL = "com.flocash.flocashecomgui.VirtualTerminalFragment.URL";
	private static final String KEY_TRACENUMBER = "com.flocash.flocashecomgui.VirtualTerminalFragment.TraceNumber";
	private WebView wvPayment;
	private ProgressDialog pDialog;
	private String redirectUrl;
	private String traceNumber;
	
	public static VirtualTerminalFragment newInstance(String url, String traceNumber)
	{
		VirtualTerminalFragment fragment = new VirtualTerminalFragment();
		Bundle bundle = new Bundle();
		bundle.putString(KEY_URL, url);
		bundle.putString(KEY_TRACENUMBER, traceNumber);
		fragment.setArguments(bundle);
		return fragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		try {
			redirectUrl = getArguments().getString(KEY_URL);
			traceNumber = getArguments().getString(KEY_TRACENUMBER);
			//testing only
//			url = "https://mpi.valucardnigeria.com:443/index.jsp?OrderID=2714666&SessionID=E1F73AB11DBDBF6DC48A523D5988A49D";
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_virtual_terminal, container, false);
		fragmentGettingStarted(rootView);
		return rootView;
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	private void fragmentGettingStarted(View view) {
		try {
			wvPayment = (WebView) view.findViewById(R.id.wvPayment);
			wvPayment.getSettings().setJavaScriptEnabled(true);
			wvPayment.setWebViewClient(new WebViewClient(){
				public void onPageFinished(WebView view, String url) {
					if(pDialog.isShowing())
					{
						pDialog.dismiss();
					}
				};
				
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					view.loadUrl(url);
					if(!redirectUrl.equalsIgnoreCase(url))
					{
						((MainActivity) getActivity()).pushFragment(PaymentResultFragment.newInstance(PaymentResultPushedEnum.FROM_VIRTUAL.getValue(), traceNumber, null));
					}
					return false;
				}
			});
			loadWebviewUrl(redirectUrl);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void loadWebviewUrl(String url)
	{
		if(pDialog != null) {
			pDialog.cancel();
		}
		pDialog = new ProgressDialog(getActivity());
		pDialog.setMessage("Loading...");
		pDialog.setCanceledOnTouchOutside(false);
		pDialog.show();
		wvPayment.loadUrl(url);
	}

	@Override
	public void onDestroyView() {
		wvPayment.clearCache(true);
		super.onDestroyView();
	}
}
