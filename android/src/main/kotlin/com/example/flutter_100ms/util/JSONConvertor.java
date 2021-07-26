package com.example.flutter_100ms.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import live.hms.video.error.HMSException;
import live.hms.video.sdk.models.HMSMessage;
import live.hms.video.sdk.models.HMSPeer;
import live.hms.video.sdk.models.HMSRoleChangeRequest;
import live.hms.video.sdk.models.HMSRoom;
import live.hms.video.sdk.models.role.HMSRole;
import live.hms.video.sdk.models.role.PublishParams;
import live.hms.video.sdk.models.role.SubscribeParams;

public class JSONConvertor {
    public static JSONObject publishParamsToJSON(PublishParams publishParams) {
        JSONObject json = new JSONObject();
        try {
            json.put("allowed", publishParams.getAllowed());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static JSONObject subscribeParamsToJSON(SubscribeParams subscribeParams) {
        JSONObject json = new JSONObject();
        try {
            json.put("subscribeTo", subscribeParams.getSubscribeTo());
            json.put("maxSubsBitRate", subscribeParams.getMaxSubsBitRate());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static JSONObject hmsRoleToJSON(HMSRole role) {
        JSONObject json = new JSONObject();
        try {
            json.put("name", role.getName());
            json.put("permission", role.getPermission());
            json.put("priority", role.getPriority());
            if (role.getPublishParams() != null) {
                json.put("publishParams", publishParamsToJSON(role.getPublishParams()));
            }
            if (role.getSubscribeParams() != null) {
                json.put("subscribeParams", subscribeParamsToJSON(role.getSubscribeParams()));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static JSONObject hmsRoomToJSON(HMSRoom room) {
        JSONObject json = new JSONObject();
        try {
            json.put("name", room.getName());
            json.put("roomId", room.getRoomId());
            json.put("peerList", hmsPeerListToJSON(room.getPeerList()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static JSONObject hmsMessageToJSON(HMSMessage message) {
        JSONObject json = new JSONObject();
        try {
            json.put("message", message.getMessage());
            json.put("sender", hmsPeerToJSON(message.getSender()));
            json.put("time", message.getTime().getTime());
            json.put("type", message.getType());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static JSONObject hmsExceptionToJSON(HMSException exception) {
        JSONObject json = new JSONObject();
        try {
            json.put("action", exception.getAction());
            json.put("code", exception.getCode());
            json.put("description", exception.getDescription());
            json.put("message", exception.getMessage());
            json.put("name", exception.getName());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static JSONObject hmsRoleChangeRequest(HMSRoleChangeRequest hmsRoleChangeRequest) {
        JSONObject json = new JSONObject();
        try {
            if(hmsRoleChangeRequest.getRequestedBy()!=null) {
                json.put("requestedBy", hmsPeerToJSON(hmsRoleChangeRequest.getRequestedBy()));
            }
            json.put("suggestedRole", hmsRoleToJSON(hmsRoleChangeRequest.getSuggestedRole()));
            json.put("token", hmsRoleChangeRequest.getToken());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static JSONArray hmsPeerListToJSON(HMSPeer[] peers) {
        JSONArray json = new JSONArray();
        for(HMSPeer peer: peers){
            json.put(hmsPeerToJSON(peer));
        }
        return json;
    }

    public static JSONObject hmsPeerToJSON(HMSPeer peer) {
        JSONObject json = new JSONObject();
        try {
            json.put("name", peer.getName());
            json.put("peerId", peer.getPeerID());
            json.put("customerUserID", peer.getCustomerUserID());
            json.put("customerDescription", peer.getCustomerDescription());
            json.put("isLocal", peer.isLocal());
            json.put("hmsRole", hmsRoleToJSON(peer.getHmsRole()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}
