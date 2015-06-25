package com.flocash.flocashecomgui;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.flocash.sdk.models.Environment;
import com.flocash.sdk.models.MerchantInfo;
import com.flocash.sdk.models.OrderInfo;
import com.flocash.sdk.models.PayerInfo;
import com.flocash.sdk.models.Request;
import com.flocash.sdk.models.Response;
import com.flocash.sdk.service.FlocashService;

public class CheckoutFragment extends Fragment{
	
	private Button btnNext;
	private ListView mListViewCheckout;
	private List<Request> listRequest;
	private Request mChoosenRequest;
	private CheckoutAdapter adapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_checkout, container, false);
		prepareData();
		fragmentGettingStarted(rootView);
		return rootView;
	}
	
	private void fragmentGettingStarted(View view){
		try {
			btnNext = (Button) view.findViewById(R.id.btnNext);
			btnNext.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
//					attempCreateOrder();
					if(mChoosenRequest != null)
					{
						CreateOrderTask task = new CreateOrderTask() {

							@Override
							protected void onPostExecute(Response result) {
								if(getDialog() != null)
									getDialog().cancel();
								if(result != null && result.isSuccess())
								{
									((MainActivity) getActivity()).pushFragment(ChoosePaymentOptionFragment.newInstance(result));
								} else if(result != null) {
									Toast.makeText(getActivity(), result.getErrorCode()+": "+ result.getErrorMessage(), Toast.LENGTH_SHORT).show();
								}
								super.onPostExecute(result);
							}
							
						};
						
						task.execute(mChoosenRequest);
					}
				}
			});
			
			mListViewCheckout = (ListView) view.findViewById(R.id.listCheckoutItem);
			adapter = new CheckoutAdapter(getActivity(), android.R.layout.select_dialog_singlechoice, listRequest);
			mChoosenRequest = listRequest.get(0);
			mListViewCheckout.setAdapter(adapter);
			mListViewCheckout.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					mChoosenRequest = (Request) parent.getItemAtPosition(position);
					adapter.updateChecked(position);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("hieult", "CheckoutFragment fragmentGettingStarted: "+e.getLocalizedMessage());
		}
	}
	
	private class CheckoutAdapter extends ArrayAdapter<Request>{
	
		private int mChecked;
		
		public CheckoutAdapter(Context context, int resource,
				List<Request> objects) {
			super(context, resource, objects);
		}
		
		public void updateChecked(int position)
		{
			mChecked = position;
			notifyDataSetChanged();
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView ==  null) {
				convertView = getActivity().getLayoutInflater().inflate(R.layout.item_checkout_list, null);
			}
			Request request = getItem(position);
			TextView tvName = (TextView) convertView.findViewById(R.id.item_name);
			TextView tvPrice = (TextView) convertView.findViewById(R.id.item_price);
			CheckedTextView checked = (CheckedTextView) convertView.findViewById(R.id.item_payment_text);
			checked.setChecked(mChecked == position ? true:false);
			tvName.setText(request.getOrder().getItem_name());
			tvPrice.setText("Price: "+request.getOrder().getItem_price());
			return convertView;
		}
	}
	
	private void prepareData()
	{
		listRequest = new ArrayList<Request>();
		
		// card payment
		OrderInfo order1 = new OrderInfo();
        PayerInfo payer1 = new PayerInfo();
        order1.setAmount(new BigDecimal("500.0"));
        order1.setCurrency("NGN");
        order1.setItem_name("Photo Campaign");
        order1.setItem_price("105");
        order1.setOrderId("photo105");
        order1.setQuantity("1");
        payer1.setCountry("NG");
        payer1.setFirstName("Thai Thinh");
        payer1.setLastName("Pham");
        payer1.setEmail("phamthaithinh@gmail.com");
        payer1.setMobile("+0986518056");
        MerchantInfo merchant1 = new MerchantInfo();
        Request request1 = new Request();
        request1.setOrder(order1);
        request1.setPayer(payer1);
        request1.setMerchant(merchant1);
        merchant1.setMerchantAccount("phamthaithinh@yahoo.com");
        
        // mobile payment
        OrderInfo order2 = new OrderInfo();
        PayerInfo payer2 = new PayerInfo();
        order2.setAmount(new BigDecimal("500.0"));
        order2.setCurrency("KES");
        order2.setItem_name("Boating Event");
        order2.setItem_price("175");
        order2.setOrderId("boating175");
        order2.setQuantity("1");
        payer2.setCountry("KE");
        payer2.setFirstName("Thai Thinh");
        payer2.setLastName("Pham");
        payer2.setEmail("phamthaithinh@gmail.com");
        payer2.setMobile("+01687372613");
        MerchantInfo merchant2 = new MerchantInfo();
        Request request2 = new Request();
        request2.setOrder(order2);
        request2.setPayer(payer2);
        request2.setMerchant(merchant2);
        merchant2.setMerchantAccount("phamthaithinh@yahoo.com");
        
        listRequest.add(request1);
        listRequest.add(request2);
	}
	
	private class CreateOrderTask extends AsyncTask<Request, Void, Response> {
		
		private ProgressDialog pDialog;
		
		@Override
		protected void onPreExecute() {
			if(pDialog != null) {
				pDialog.cancel();
			}
			pDialog = new ProgressDialog(getActivity());
			pDialog.setMessage("Creating order...");
			pDialog.setCanceledOnTouchOutside(false);
			pDialog.show();
			super.onPreExecute();
		}

		@Override
		protected Response doInBackground(Request... params) {
			FlocashService service = new FlocashService(Environment.SANDBOX);
			Response result = null;
			try {
				result = service.createOrder(params[0]);
			} catch (Exception e) {
				e.printStackTrace();
				Log.e("hieult", "CheckoutFragment CreateOrderTask: "+e.getLocalizedMessage());
			}
			return result;
		}
		
		@Override
		protected void onPostExecute(Response result) {
			if(pDialog != null)
			{
				pDialog.cancel();
			}
			super.onPostExecute(result);
		}
		
		public ProgressDialog getDialog() {
			return pDialog;
		}
		
	}
}