# ecomsdk
Android SDK and sample for Flocash Ecom API

# How to use SDK lib
1. Add lib to your android application.

  Copy ecomsdk.jar to project libs folder. Change build.gradle and add following line
  ```
  dependencies {
  ....
    compile files('libs/ecomsdk.jar')
  ....
  }
  ```
2. Initial `FlocashService`

  There is two Environment setting on our sdk: `SANDBOX` and `LIVE`. You initial service as below for sandbox
  ```java
  FlocashService service = new FlocashService(Environment.SANDBOX)
  ```
  
3. Use `FlocashService`

  There are four methods in FlocashService
  * createOrder: Create ecommerce order
  * updatePaymentOpion: Select payment option for order to process payment
  * updateAdditionField: Some payment options need customer provide more info for process payment as mobile number wallet, otp etc... Call this to add more info for that kind order
  * getOrder: Use this method to query detail about order. Below is code scriptlet for how to use api
```java
        FlocashService service = new FlocashService(Environment.SANDBOX);
        OrderInfo orderInfo = new OrderInfo();
        OrderInfo order = new OrderInfo();
        PayerInfo payer = new PayerInfo();
        order.setAmount(new BigDecimal("1000.0"));
        order.setCurrency("KES");
        order.setItem_name("test android");
        order.setItem_price("10");
        order.setOrderId("test123");
        order.setQuantity("1");
        payer.setCountry("KE");
        payer.setFirstName("Thai Thinh");
        payer.setLastName("Pham");
        payer.setEmail("phamthaithinh@gmail.com");
        payer.setMobile("+0986518056");
        MerchantInfo merchant = new MerchantInfo();
        Request request = new Request();
        request.setOrder(order);
        request.setPayer(payer);
        request.setMerchant(merchant);
        merchant.setMerchantAccount("phamthaithinh@yahoo.com");
        try {
            Response response = service.createOrder(request);
            Log.i(response.getErrorCode(), response.getErrorMessage());
            if (response.isSuccess()) {
                request = new Request();
                request.setOrder(new OrderInfo());
                request.getOrder().setTraceNumber(response.getOrder().getTraceNumber());
                request.setPayOption(new PaymentMethodInfo());
                request.getPayOption().setId(response.getPaymentOptions().getMobiles().get(0).getId());
                response=service.updatePaymentOpion(request);
                if(response.isSuccess()){
                    Hashtable<String,String> params=new Hashtable<>();
                    params.put("mobile","0986518056");
                    response=service.updateAdditionField(request.getOrder().getTraceNumber(),params);
                    if(response.isSuccess()){
                        System.out.print(response.getOrder().getInstruction());
                    }else{
                        System.out.print(response.getErrorMessage());
                    }
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
```
  
