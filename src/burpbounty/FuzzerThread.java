package burpbounty;
import burp.*;

public class FuzzerThread implements Runnable {

    private IBurpExtenderCallbacks callbacks;
    private Tags tagui;
    private IExtensionHelpers helpers;
    private byte[] request;
    private IHttpService httpService;
    private String payload;
    private String ParamName;

    FuzzerThread(IBurpExtenderCallbacks callbacks,Tags tagui,byte[] request,IHttpRequestResponse baseRequestResponse,String ParamName,String payload) {
        this.callbacks = callbacks;
        this.tagui = tagui;
        this.helpers = callbacks.getHelpers();
        this.request = request;
        this.httpService = baseRequestResponse.getHttpService();
        this.payload = payload;
        this.ParamName = ParamName;
    }

    @Override
    public void run() {
            IHttpRequestResponse requestResponse;
            Integer responseCode;
            IResponseInfo r;
            try {
                if(Config.get_run_status()==true)
                {
                    return;
                }

                requestResponse = callbacks.makeHttpRequest(httpService,request);
                r = helpers.analyzeResponse(requestResponse.getResponse());
                responseCode = new Integer(r.getStatusCode());
                String res_len = String.valueOf(requestResponse.getResponse().length-r.getBodyOffset());
                this.tagui.add(helpers.analyzeRequest(requestResponse).getUrl(), String.valueOf(responseCode),res_len,payload,ParamName, requestResponse);


            } catch (Exception ex) {
                callbacks.printError(ex.getMessage());
            }
        }

}

