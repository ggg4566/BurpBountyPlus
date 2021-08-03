package burpbounty;
import burp.*;

import java.awt.*;

public class FuzzerThread implements Runnable {

    private IBurpExtenderCallbacks callbacks;
    private Tags tagui;
    private IExtensionHelpers helpers;
    private byte[] request;
    private IHttpService httpService;
    private String payload;
    private String ParamName;
    private String profileName;
    private boolean isLight = true;

    FuzzerThread(IBurpExtenderCallbacks callbacks,Tags tagui,byte[] request,IHttpRequestResponse baseRequestResponse,String ParamName,String payload,String profileName) {
        this.callbacks = callbacks;
        this.tagui = tagui;
        this.helpers = callbacks.getHelpers();
        this.request = request;
        this.httpService = baseRequestResponse.getHttpService();
        this.payload = payload;
        this.ParamName = ParamName;
        this.profileName = profileName;
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
                this.tagui.add(helpers.analyzeRequest(requestResponse).getUrl(), String.valueOf(responseCode),res_len,payload,ParamName,profileName, requestResponse);
                if(isLight)
                {
                    this.tagui.setRowColor();
                    isLight = false;
                }
            } catch (Exception ex) {
                callbacks.printError(ex.getMessage());
            }
        }

}

