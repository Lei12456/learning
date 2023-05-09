package com.yl.util;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;


/**
 * @author dingtalk
 */
public class HttpHelper {

    private static final Logger LOG = LoggerFactory.getLogger(HttpHelper.class);

    public static JSONObject httpGet(String url)  {
        return httpGet(url, null);
    }

	public static JSONObject httpGet(String url, Map<String, String> headers)  {
        HttpGet httpGet = new HttpGet(url);
        RequestConfig requestConfig = RequestConfig.custom().
                setSocketTimeout(5000).setConnectTimeout(5000).build();
        httpGet.setConfig(requestConfig);
        CloseableHttpResponse response = null;
        CloseableHttpClient httpClient = null;
        //　Header参数
        if (Objects.nonNull(headers)) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpGet.addHeader(entry.getKey(), entry.getValue());
            }
        }

        try {
            httpClient = HttpClients.createDefault();
            response = httpClient.execute(httpGet, new BasicHttpContext());
            if (response.getStatusLine().getStatusCode() != 200) {
                LOG.error("request url failed, http code=" + response.getStatusLine().getStatusCode()
                        + ", url=" + url);
                return null;
            }
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String resultStr = EntityUtils.toString(entity, "utf-8");

                JSONObject result = JSON.parseObject(resultStr);
                if (result.getInteger("errcode") == 0) {
                    return result;
                } else {
                    int errCode = result.getInteger("errcode");
                    String errMsg = result.getString("errmsg");
                }
            }
        } catch (IOException e) {
            LOG.error("request url=" + url + ", exception, msg=" + e.getMessage(), e);
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    LOG.error("request url=" + url + ", exception, msg=" + e.getMessage(), e);
                }
            }
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    LOG.error("request url=" + url + ", exception, msg=" + e.getMessage(), e);
                }
            }
        }

        return null;
    }
    public static JSONObject httpPostDingtalk(String url, Object data)  {
        return httpPostDingtalk(url,data,null);
    }
    public static JSONObject httpPostDingtalk(String url, Object data ,Map<String, String> headers){
        HttpPost httpPost = new HttpPost(url);
        CloseableHttpResponse response = null;
        CloseableHttpClient httpClient = null;
        RequestConfig requestConfig = RequestConfig.custom().
                setSocketTimeout(5000).setConnectTimeout(5000).build();
        httpPost.setConfig(requestConfig);
        httpPost.addHeader("Content-Type", "application/json");
        //　Header参数
        if (Objects.nonNull(headers)) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpPost.addHeader(entry.getKey(), entry.getValue());
            }
        }

        try {
            httpClient = HttpClients.createDefault();
            
            StringEntity requestEntity = new StringEntity(JSON.toJSONString(data), "utf-8");
            httpPost.setEntity(requestEntity);
            response = httpClient.execute(httpPost, new BasicHttpContext());
            
            if (response.getStatusLine().getStatusCode() != 200) {
                
                LOG.error("request url failed, http code=" + response.getStatusLine().getStatusCode()
                        + ", url=" + url);
                return null;
            }
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String resultStr = EntityUtils.toString(entity, "utf-8");
                LOG.info("request url" + url);
                LOG.info("request data" + data);
                LOG.info(resultStr);
                return  JSON.parseObject(resultStr);
            }
        } catch (IOException e) {
            LOG.error("request url=" + url + ", exception, msg=" + e.getMessage(), e);
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    LOG.error("request url=" + url + ", exception, msg=" + e.getMessage(), e);
                }
            }
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    LOG.error("request url=" + url + ", exception, msg=" + e.getMessage(), e);
                }
            }
        }
        
        return null;
    }

    public static JSONObject httpPost(String url, Object data)  {
	    return httpPost(url, data, null);
    }
	
	public static JSONObject httpPost(String url, Object data, Map<String, String> headerMap)  {
        HttpPost httpPost = new HttpPost(url);
        CloseableHttpResponse response = null;
        CloseableHttpClient httpClient = null;
        RequestConfig requestConfig = RequestConfig.custom().
                setSocketTimeout(5000).setConnectTimeout(5000).build();
        httpPost.setConfig(requestConfig);
        httpPost.addHeader("Content-Type", "application/json");
        if (Objects.nonNull(headerMap)) {
            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                httpPost.addHeader(entry.getKey(), entry.getValue());
            }
        }
        try {
            httpClient = HttpClients.createDefault();

        	StringEntity requestEntity = new StringEntity(JSON.toJSONString(data), "utf-8");
            httpPost.setEntity(requestEntity);
            response = httpClient.execute(httpPost, new BasicHttpContext());

            if (response.getStatusLine().getStatusCode() != 200) {

                LOG.error("request url failed, http code=" + response.getStatusLine().getStatusCode()
                        + ", url=" + url);
                return null;
            }
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String resultStr = EntityUtils.toString(entity, "utf-8");

                JSONObject result = JSON.parseObject(resultStr);
                if (result.getInteger("errcode") == 0) {
                	result.remove("errcode");
                	result.remove("errmsg");
                    return result;
                } else {
                    int errCode = result.getInteger("errcode");
                    String errMsg = result.getString("errmsg");
                }
            }
        } catch (IOException e) {
            LOG.error("request url=" + url + ", exception, msg=" + e.getMessage(), e);
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    LOG.error("request url=" + url + ", exception, msg=" + e.getMessage(), e);
                }
            }
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    LOG.error("request url=" + url + ", exception, msg=" + e.getMessage(), e);
                }
            }
        }

        return null;
    }
	
	





}
