package com.adirahav.diraleashkaa.data.network.services

import com.adirahav.diraleashkaa.common.Configuration.API_BASE_URL
import com.adirahav.diraleashkaa.data.network.models.EmailModel
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

class EmailService private constructor() {
    val emailAPI: EmailAPI

    companion object {
        var instance: EmailService? = null
            get() {
                if (field == null) {
                    field = EmailService()
                }
                return field
            }
            private set
    }

    init {
        val retrofit: Retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl(API_BASE_URL).build()
        emailAPI = retrofit.create(EmailAPI::class.java)
    }

    interface EmailAPI {
        @FormUrlEncoded
        @POST("email/send")
        fun sendEmail(
            @Field("user_uuid") userUUID: String?,
            @Field("type") type: String,
            @Field("subject") subject: String,
            @Field("message") message: String?,
        ): Call<EmailModel?>?

    }
}