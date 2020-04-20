package org.jenkinsci.plugins.extremenotification;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;

import java.io.IOException;

/**
 * TODO
 *
 * @author xyctruth
 * @date 2020-04-18 ~ 17:09
 */
public class TestHttpClient {
    @Test
    public void httpClientPost() throws IOException {
        HttpClient client = new DefaultHttpClient();
        HttpResponse response = client.execute(new HttpPost("http://0.0.0.0:8082/adapter/ci/hook/push"));
        System.out.println(response);
    }

}
