package burpbounty;

import burp.*;
import java.util.List;

import com.google.gson.*;

public class InsertionPoint implements IScannerInsertionPoint {

    private BurpBountyExtension parent;
    private byte[] baseRequest;
    private String baseName;
    private String baseValue;
    private String ParamName;
    private String ParamValue;
    byte nPointType;
    byte ins;

    InsertionPoint(BurpBountyExtension newParent, byte[] baseRequest, String basename, String basevalue,byte PointType,byte INS_PARAM,String ParamName,String ParamValue)
    {
        this.parent = newParent;
        this.baseRequest = baseRequest;
        this.baseName = basename;
        this.baseValue = basevalue;
        this.nPointType = PointType;
        this.ins = INS_PARAM;
        this.ParamName = ParamName;
        this.ParamValue = ParamValue;

    }

    //
    // implement IScannerInsertionPoint
    //

    @Override
    public String getInsertionPointName()
    {
        return baseName;
    }

    @Override
    public String getBaseValue()
    {
        return baseValue;
    }

    @Override
    public byte[] buildRequest(byte[] payload)
    {
        byte[] newRequest =baseRequest;
        String payloadPlain = parent.helpers.bytesToString(payload);
        if(nPointType == INS_PARAM_URL)
        {
            newRequest = parent.helpers.updateParameter(baseRequest, parent.helpers.buildParameter(baseName, payloadPlain, IParameter.PARAM_URL));
        }
        if(nPointType ==INS_PARAM_BODY)
        {
            newRequest =parent.helpers.updateParameter(baseRequest, parent.helpers.buildParameter(baseName, payloadPlain,IParameter.PARAM_BODY));
        }
        if(nPointType ==INS_PARAM_JSON)
        {
            if(ins ==INS_PARAM_URL) {
                JsonObject jsonObj = new JsonParser().parse(ParamValue).getAsJsonObject();
                JsonObject newJson = CustomHelpers.UpdateJsonValue(this.baseName, payloadPlain, jsonObj);
                String jsonStr = newJson.toString();
                newRequest = parent.helpers.updateParameter(baseRequest, parent.helpers.buildParameter(ParamName, jsonStr, IParameter.PARAM_URL));
            }
            if(ins ==INS_PARAM_BODY)
            {
                JsonObject jsonObj = new JsonParser().parse(ParamValue).getAsJsonObject();
                JsonObject newJson = CustomHelpers.UpdateJsonValue(this.baseName, payloadPlain, jsonObj);
                String jsonStr = newJson.toString();
                newRequest =parent.helpers.updateParameter(baseRequest, parent.helpers.buildParameter(ParamName, jsonStr,IParameter.PARAM_BODY));
            }
            if(ins == INS_PARAM_JSON)
            {
                IRequestInfo requestInfo =  parent.helpers.analyzeRequest(baseRequest);
                List<String> Headers = requestInfo.getHeaders();
                int bodyOffset = requestInfo.getBodyOffset();
                int body_length = baseRequest.length - bodyOffset;
                String body = new String(baseRequest, bodyOffset, body_length);

                JsonObject jsonObj = new JsonParser().parse(body).getAsJsonObject();

                JsonObject newJson = CustomHelpers.UpdateJsonValue(this.baseName, payloadPlain, jsonObj);
                String jsonStr = newJson.toString();
                //jsonStr = jsonStr.replace("payload",payloadPlain);
                byte[] reqBody = jsonStr.getBytes();
                newRequest = parent.helpers.buildHttpMessage(Headers, reqBody);

                //newRequest =parent.helpers.updateParameter(baseRequest, parent.helpers.buildParameter(baseName, payloadPlain,IParameter.PARAM_BODY));

            }
        }
        return newRequest;
    }

    @Override
    public int[] getPayloadOffsets(byte[] payload)
    {
        // since the payload is being inserted into a serialized data structure, there aren't any offsets
        // into the request where the payload literally appears
        return null;
    }

    @Override
    public byte getInsertionPointType()
    {
        return nPointType;
    }
}