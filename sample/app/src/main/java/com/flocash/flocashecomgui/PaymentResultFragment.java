package com.flocash.flocashecomgui;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.flocash.sdk.models.Environment;
import com.flocash.sdk.models.Response;
import com.flocash.sdk.service.FlocashService;

public class PaymentResultFragment extends Fragment{
	
	public static enum PaymentResultPushedEnum {
		FROM_VIRTUAL(0), FROM_CHOOSE_PAYMENT(1);
		private int value;
		
		private PaymentResultPushedEnum(int _value) {
			this.value = _value;
		}
		
		public int getValue()
		{
			return value;
		}
	}

	private static final String KEY_TRACENUMBER = "com.flocash.flocashecomgui.CardPaymentResultFragment.TraceNumber";
	private static final String KEY_PUSHEDFROM = "com.flocash.flocashecomgui.CardPaymentResultFragment.PushedFrom";
	private static final String KEY_INSTRUCTION = "com.flocash.flocashecomgui.CardPaymentResultFragment.Instruction";
	private String traceNumber, instructionField;
	private TextView tvResult, tvPaymentTitle;
	private int pushedFrom;
	
	public static PaymentResultFragment newInstance(int pushedFrom, String traceNumber, String strInstruction)
	{
		PaymentResultFragment fragment = new PaymentResultFragment();
		Bundle bundle = new Bundle();
		bundle.putString(KEY_TRACENUMBER, traceNumber);
		bundle.putInt(KEY_PUSHEDFROM, pushedFrom);
		bundle.putString(KEY_INSTRUCTION, strInstruction);
		fragment.setArguments(bundle);
		return fragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		try {
			pushedFrom = getArguments().getInt(KEY_PUSHEDFROM);
			if(pushedFrom == PaymentResultPushedEnum.FROM_VIRTUAL.getValue())
			{
				traceNumber = getArguments().getString(KEY_TRACENUMBER);
			} else if (pushedFrom == PaymentResultPushedEnum.FROM_CHOOSE_PAYMENT.getValue()){
				instructionField = getArguments().getString(KEY_INSTRUCTION);
			}
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
			tvPaymentTitle = (TextView) view.findViewById(R.id.tvPaymentTitle);
			if(pushedFrom == PaymentResultPushedEnum.FROM_VIRTUAL.getValue())
			{
				new GetOrderTask().execute(new String[] {traceNumber});
			} else if (pushedFrom == PaymentResultPushedEnum.FROM_CHOOSE_PAYMENT.getValue())
			{
				tvPaymentTitle.setText("Please following below instruction:");
				tvPaymentTitle.setText(Html.fromHtml(instructionField));
			}
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
			if(result != null && result.isSuccess() && result.getOrder() != null)
			{
				StringBuilder message = new StringBuilder();
				message.append("Order number: "+ result.getOrder().getTraceNumber()+"\n");
				message.append("Amount: "+ result.getOrder().getAmount()+"\n");
				message.append("Status: "+ result.getOrder().getStatus()+"\n");
				message.append("Currency: "+ result.getOrder().getCurrency());
				tvResult.setText(message.toString());
			} else if(result != null && !result.isSuccess()){
				tvResult.setText(result.getErrorCode()+": "+ result.getErrorMessage());
			}
			super.onPostExecute(result);
		}
		
	}
	
}
