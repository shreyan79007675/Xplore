package com.example.xplore.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAA0s6UmQk:APA91bGdNv6JMB14ruGhVBibjuW-yIm-Hl6J6a0NtnOOj5It_3-0kRN-Zw9QndJ7dMVOYuE8p2A2w1z1OkEjn4RnLCvxRi59-zMNy8RwKDzbtBOAvzj3UIPUuVzmx0KWQyT7MHIbpX8s"

    })
    @POST("fcm/send")
    Call<Response> sendnotification(@Body Sender body);
}
