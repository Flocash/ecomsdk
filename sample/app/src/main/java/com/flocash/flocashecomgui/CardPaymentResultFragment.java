package com.flocash.flocashecomgui;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.flocash.sdk.models.Environment;
import com.flocash.sdk.models.Response;
import com.flocash.sdk.service.FlocashService;

public class CardPaymentResultFragment extends Fragment{

	private static final String KEY_TRACENUMBER = "com.flocash.flocashecomgui.CardPaymentResultFragment.TraceNumber";
	private String traceNumber;
	private TextView tvResult;
	
	public static CardPaymentResultFragment newInstance(String traceNumber)
	{
		CardPaymentResultFragment fragment = new CardPaymentResultFragment();
		Bundle bundle = new Bundle();
		bundle.putString(KEY_TRACENUMBER, traceNumber);
		fragment.setArguments(bundle);
		return fragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		try {
			traceNumber = getArguments().getString(KEY_TRACENUMBER);
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_payment_result, container, false);
		fragmentGettingStarted(rootView);
		return rootView;
	}
	
	private void fragmentGettingStarted(View view) {
		try {
			tvResult = (TextView) view.findViewById(R.id.tvPaymentResult);
			new GetOrderTask().execute(new String[] {traceNumber});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private class GetOrderTask extends AsyncTask<String, Void, Response> {
		private ProgressDialog pDialog;
		
		@Override
		protected void onPreExecute() {
			if(pDialog != null)
			{
				pDialog.cancel();
			}
			
			pDialog = new ProgressDialog(getActivity());
			pDialog.setMessage("Getting data...");
			pDialog.show();
			super.onPreExecute();
		}
		
		@Override
		protected Response doInBackground(String... params) {
			FlocashService service = new FlocashService(Environment.SANDBOX);
			Response result = null;
			try {
				result = service.getOrder(params[0]);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return result;
		}
		
		@Override
		protected void onPostExecute(Response result) {
			if(pDialog != null)
			{
				pDialog.cancel();
			}
			if(result != null && result.isSuccess())
			{
				tvResult.setText(result.getErrorCode()+": "+ result.getErrorMessage());
			} else {
				tvResult.setText(result.getErrorCode()+": "+ result.getErrorMessage());
			}
			super.onPostExecute(result);
		}
		
	}
	
}
