package com.example.quanlycongviec.util

import android.os.AsyncTask
import android.util.Log
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

/**
 * Email sender utility for sending OTP emails
 * NOTE: In a production app, you should use Firebase Cloud Functions or a dedicated email service
 * This implementation is for demonstration purposes only
 */
object EmailSender {
    private const val TAG = "EmailSender"
    
    // Replace these with your actual email credentials
    // WARNING: Never hardcode credentials in a real app!
    // In production, these should be stored securely (e.g., in Firebase Remote Config)
    private const val EMAIL_USERNAME = "quanglongmoi@gmail.com"
    private const val EMAIL_PASSWORD = "azdr wnrm bvcl jpde"
    
    fun sendOtpEmail(recipientEmail: String, otp: String, callback: (Boolean, String?) -> Unit) {
        SendEmailTask(recipientEmail, otp, callback).execute()
    }
    
    private class SendEmailTask(
        private val recipientEmail: String,
        private val otp: String,
        private val callback: (Boolean, String?) -> Unit
    ) : AsyncTask<Void, Void, Boolean>() {
        private var errorMessage: String? = null
        
        override fun doInBackground(vararg params: Void?): Boolean {
            val props = Properties()
            props["mail.smtp.host"] = "smtp.gmail.com"
            props["mail.smtp.socketFactory.port"] = "465"
            props["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
            props["mail.smtp.auth"] = "true"
            props["mail.smtp.port"] = "465"
            
            try {
                val session = Session.getDefaultInstance(props, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(EMAIL_USERNAME, EMAIL_PASSWORD)
                    }
                })
                
                val message = MimeMessage(session)
                message.setFrom(InternetAddress(EMAIL_USERNAME))
                message.addRecipient(Message.RecipientType.TO, InternetAddress(recipientEmail))
                message.subject = "Your Password Reset Code"
                
                // Create HTML email content
                val emailContent = """
                    <html>
                    <body style="font-family: Arial, sans-serif; color: #333333;">
                        <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #dddddd; border-radius: 5px;">
                            <h2 style="color: #4285F4;">Task Manager Password Reset</h2>
                            <p>You requested to reset your password. Please use the following verification code:</p>
                            <div style="background-color: #f5f5f5; padding: 15px; border-radius: 5px; text-align: center; font-size: 24px; letter-spacing: 5px; font-weight: bold;">
                                $otp
                            </div>
                            <p style="margin-top: 20px;">This code will expire in 10 minutes.</p>
                            <p>If you didn't request this password reset, please ignore this email.</p>
                            <p style="margin-top: 30px; font-size: 12px; color: #777777;">
                                This is an automated email. Please do not reply.
                            </p>
                        </div>
                    </body>
                    </html>
                """.trimIndent()
                
                message.setContent(emailContent, "text/html; charset=utf-8")
                
                // Send the message
                Transport.send(message)
                return true
            } catch (e: MessagingException) {
                Log.e(TAG, "Failed to send email", e)
                errorMessage = e.message
                return false
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error", e)
                errorMessage = e.message
                return false
            }
        }
        
        override fun onPostExecute(result: Boolean) {
            callback(result, errorMessage)
        }
    }
}
