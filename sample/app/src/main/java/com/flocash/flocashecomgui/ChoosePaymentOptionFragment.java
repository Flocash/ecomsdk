package com.flocash.flocashecomgui;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.Toast;

import com.flocash.flocashecomgui.PaymentResultFragment.PaymentResultPushedEnum;
import com.flocash.sdk.models.CustomField;
import com.flocash.sdk.models.CustomField.Type;
import com.flocash.sdk.models.Environment;
import com.flocash.sdk.models.OrderInfo;
import com.flocash.sdk.models.PaymentMethodInfo;
import com.flocash.sdk.models.PaymentOptionInfo;
import com.flocash.sdk.models.Request;
import com.flocash.sdk.models.Response;
import com.flocash.sdk.service.FlocashService;

public class ChoosePaymentOptionFragment extends Fragment {

    private static final String KEY_OPTIONS = "com.flocash.flocashecomgui.ChoosePaymentOptions";

    public enum PaymentOptionEnum {
        CARD, MOBILE, BANK, KEYIN_CARD
    }

    private Button btnBack, btnNext;
    private PaymentOptionInfo receiveOptions;
    private ListView listViewPayment;
    private ListPaymentOptionAdapter adapter;
    private List<PaymentMethodInfo> listData;
    private PaymentMethodInfo mChoosenPayment;
    private Response mCreateOrderResponse;

    public static ChoosePaymentOptionFragment newInstance(Response response) {
        ChoosePaymentOptionFragment fragment = new ChoosePaymentOptionFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_OPTIONS, response);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            mCreateOrderResponse = (Response) getArguments().getSerializable(KEY_OPTIONS);
            if (mCreateOrderResponse != null) {
                receiveOptions = mCreateOrderResponse.getPaymentOptions();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_choose_options, container, false);
        fragmentGettingStarted(rootView);
        return rootView;
    }

    private void fragmentGettingStarted(View view) {
        try {
            btnNext = (Button) view.findViewById(R.id.btnNext);
            btnBack = (Button) view.findViewById(R.id.btnBack);
            btnNext.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mChoosenPayment != null) {
                        Request request = new Request();
                        request.setOrder(new OrderInfo());
                        request.getOrder().setTraceNumber(mCreateOrderResponse.getOrder().getTraceNumber());
                        request.setPayOption(new PaymentMethodInfo());
                        request.getPayOption().setId(mChoosenPayment.getId());
                        UpdatePaymentTask updateTask = new UpdatePaymentTask() {

                            @Override
                            protected void onPostExecute(Response result) {
                                if (getDialog() != null) {
                                    getDialog().cancel();
                                }
                                if (result != null && result.isSuccess()) {
                                    // if payment is card payment, do push url redirect to virtual terminal screen
                                    // else if is mobile payment, do update addition field and show instruction dialog if update success
                                    if (mCreateOrderResponse.getPaymentOptions().getCards().size() > 0) {
                                        ((MainActivity) getActivity()).pushFragment(VirtualTerminalFragment.newInstance(result.getOrder().getRedirect().getUrl(), result.getOrder().getTraceNumber()));
                                    } else if (mCreateOrderResponse.getPaymentOptions().getMobiles().size() > 0) {
                                        List<CustomField> customFields = result.getOrder().getAdditionFields().getFields();
                                        final String traceNumber = result.getOrder().getTraceNumber();
//										StringBuilder builder = new StringBuilder();
                                        final Dialog dialogAddition = new Dialog(getActivity(), R.style.CustomDialogTheme);

                                        dialogAddition.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                        dialogAddition.setContentView(R.layout.dialog_addition_info);
                                        dialogAddition.setCanceledOnTouchOutside(false);
                                        dialogAddition.setCancelable(false);
                                        dialogAddition.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                                        dialogAddition.setOwnerActivity(getActivity());

                                        LinearLayout container = (LinearLayout) dialogAddition.findViewById(R.id.lnAdditionContainer);
                                        LinearLayout lnUpdate = (LinearLayout) dialogAddition.findViewById(R.id.lnUpdate);
                                        LinearLayout lnExit = (LinearLayout) dialogAddition.findViewById(R.id.lnExit);
                                        container.setOrientation(LinearLayout.VERTICAL);

//										for (int i = 0; i < customFields.size(); i++) {
//											builder.append(customFields.get(i).getDisplayName()+"-name: "+customFields.get(i).getName()+"-type: "+customFields.get(i).getType());

                                        EditText edt = new EditText(getActivity());
                                        edt.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                                        edt.setHint(customFields.get(0).getDisplayName());
                                        edt.setMinHeight((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics()));
                                        if (customFields.get(0).getType() == Type.CHAR) {
                                            edt.setInputType(InputType.TYPE_CLASS_TEXT);
                                        } else if (customFields.get(0).getType() == Type.DATE) {
                                            edt.setInputType(InputType.TYPE_CLASS_DATETIME);
                                        } else if (customFields.get(0).getType() == Type.DIGIT || customFields.get(0).getType() == Type.NUMBER) {
                                            edt.setInputType(InputType.TYPE_CLASS_NUMBER);
                                        }

                                        if (customFields.get(0).getMaxlength() != null) {
                                            edt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(customFields.get(0).getMaxlength())});
                                        }
                                        container.addView(edt);
//										}
                                        final CustomField custom = customFields.get(0);
                                        final EditText edtField = edt;
                                        lnUpdate.setOnClickListener(new OnClickListener() {

                                            @Override
                                            public void onClick(View v) {
                                                //TODO: do update addition field here
                                                Hashtable<String, String> additionHashtable = new Hashtable<String, String>();
                                                additionHashtable.put(custom.getName(), edtField.getText().toString());
                                                UpdateAdditionField updateAdditionTask = new UpdateAdditionField(traceNumber, additionHashtable) {
                                                    @Override
                                                    protected void onPostExecute(Response result) {
                                                        dialogAddition.cancel();
                                                        if (getDialog() != null) {
                                                            getDialog().cancel();
                                                        }
                                                        // update success show instruction dialog
                                                        if (result != null && result.isSuccess() && result.getOrder() != null) {
                                                            String instructionField = result.getOrder().getInstruction();
                                                            try {
                                                                ((MainActivity) getActivity()).pushFragment(PaymentResultFragment.newInstance(PaymentResultPushedEnum.FROM_CHOOSE_PAYMENT.getValue(), traceNumber, instructionField));
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }
                                                        } else {
                                                            if (result != null) {
                                                                Toast.makeText(getActivity(), result.getErrorCode() + ": " + result.getErrorMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                        super.onPostExecute(result);
                                                    }
                                                };
                                                updateAdditionTask.execute();

                                            }
                                        });
                                        lnExit.setOnClickListener(new OnClickListener() {

                                            @Override
                                            public void onClick(View v) {
                                                dialogAddition.cancel();
                                            }
                                        });
                                        dialogAddition.show();
                                    }
                                } else {
                                    if (result != null) {
                                        Toast.makeText(getActivity(), result.getErrorCode() + ": " + result.getErrorMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                                super.onPostExecute(result);
                            }

                        };

                        updateTask.execute(request);
                    }
                }
            });

            btnBack.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    getActivity().onBackPressed();
                }
            });

            listViewPayment = (ListView) view.findViewById(R.id.listPayment);
            listData = new ArrayList<PaymentMethodInfo>();
            if (receiveOptions != null) {
                // bank
                if (receiveOptions.getBanks().size() > 0) {
                    listData.addAll(receiveOptions.getBanks());
                }

                // card
                if (receiveOptions.getCards().size() > 0) {
                    listData.addAll(receiveOptions.getCards());
                }

                // KeyinCards
                if (receiveOptions.getKeyinCards().size() > 0) {
                    listData.addAll(receiveOptions.getKeyinCards());
                }

                // getMobiles
                if (receiveOptions.getMobiles().size() > 0) {
                    listData.addAll(receiveOptions.getMobiles());
                }
            }

            if (listData != null && listData.size() > 0) {
                mChoosenPayment = listData.get(0);
            }
            adapter = new ListPaymentOptionAdapter(getActivity(), android.R.layout.select_dialog_singlechoice, listData);
            listViewPayment.setAdapter(adapter);
            listViewPayment.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        mChoosenPayment = (PaymentMethodInfo) parent.getItemAtPosition(position);
                        adapter.updateCheckedPosition(position);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ListPaymentOptionAdapter extends ArrayAdapter<PaymentMethodInfo> {

        private List<PaymentMethodInfo> lstPayment;
        private int mCheckedPosition;

        public ListPaymentOptionAdapter(Context context, int resource, List<PaymentMethodInfo> objects) {
            super(context, resource, objects);
            this.lstPayment = objects;
        }

        @Override
        public int getCount() {
            return lstPayment != null && lstPayment.size() > 0 ? lstPayment.size() : 0;
        }

        public void updateCheckedPosition(int position) {
            mCheckedPosition = position;
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.item_payment_list, null);
            }
            PaymentMethodInfo paymentOption = getItem(position);
            CheckedTextView text = (CheckedTextView) convertView.findViewById(R.id.item_payment_text);
            text.setChecked(mCheckedPosition == position ? true : false);
            text.setText(paymentOption.getDisplayName());
            ImageView image = (ImageView) convertView.findViewById(R.id.item_payment_image);
            try {
                new DownloadImageTask(image).execute(paymentOption.getLogo());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return convertView;
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    private class UpdatePaymentTask extends AsyncTask<Request, Void, Response> {

        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            if (pDialog != null) {
                pDialog.cancel();
            }
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Please wait...");
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Response doInBackground(Request... params) {
            FlocashService service = new FlocashService(Environment.SANDBOX);
            Response result = null;
            try {
                result = service.updatePaymentOpion(params[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(Response result) {
            if (pDialog != null) {
                pDialog.cancel();
            }
            super.onPostExecute(result);
        }

        public ProgressDialog getDialog() {
            return pDialog;
        }
    }

    private class UpdateAdditionField extends AsyncTask<Void, Void, Response> {
        private ProgressDialog pDialog;
        private String traceNumber;
        private Hashtable<String, String> hashTableAddition;

        public UpdateAdditionField(String traceNumber, Hashtable<String, String> paramsHash) {
            // TODO Auto-generated constructor stub
            this.traceNumber = traceNumber;
            this.hashTableAddition = paramsHash;
        }

        @Override
        protected void onPreExecute() {
            if (pDialog != null) {
                pDialog.cancel();
            }
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Please wait...");
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Response doInBackground(Void... params) {
            FlocashService service = new FlocashService(Environment.SANDBOX);
            Response result = null;
            try {
                result = service.updateAdditionField(traceNumber, hashTableAddition);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(Response result) {
            if (pDialog != null)
                pDialog.cancel();
            super.onPostExecute(result);
        }

        public ProgressDialog getDialog() {
            return pDialog;
        }
    }
}