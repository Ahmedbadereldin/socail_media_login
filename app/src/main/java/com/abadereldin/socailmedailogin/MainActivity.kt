package com.abadereldin.socailmedailogin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.twitter.sdk.android.core.*
import com.twitter.sdk.android.core.models.User
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException

class MainActivity : AppCompatActivity(), GoogleSignInHelper.OnGoogleSignInListener {

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager
    private var googleSignInHelper: GoogleSignInHelper? = null


    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_main)
        setUpActivity()
        setUpAction()
    }

    private fun setUpActivity() {
        mAuth = FirebaseAuth.getInstance()

        /***/
        printHashKey()

        /**Twitter*/
        TwitterCore.getInstance().sessionManager.clearActiveSession()

        t_login_button.callback = object :
            Callback<TwitterSession?>() {
            override fun success(result: Result<TwitterSession?>) {
                val session = TwitterCore.getInstance().sessionManager.activeSession
                val authToken = session.authToken
                val token = authToken.token
                val secret = authToken.secret
                print("OnGSignInSuccess ----- ${token + "  \n  " + session.authToken}")

                Log.d("successBB", "success: $token  ")
                Log.d("successBB        ", "success: " + session.authToken + "  ")
                loginWithTwitter(session)
            }

            override fun failure(exception: TwitterException) {
                val e = exception.message
            }
        }

        twitterBtn.setOnClickListener {
            t_login_button.performClick()
        }
        /**Twitter*/


        /**FaceBook*/
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().logOut()
        f_login_button.setReadPermissions(listOf("email", "public_profile"))
        f_login_button.registerCallback(callbackManager, facebookCallback)

        facebookBtn.setOnClickListener {
            f_login_button.performClick()
        }
        /**FaceBook*/

        /**Google*/
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        mGoogleSignInClient.signOut()

        googleSignInHelper = GoogleSignInHelper(this, this)
        googleSignInHelper!!.connect()
        googleBtn.setOnClickListener { googleSignInHelper!!.signIn() }
        /**Google*/

    }

    private fun setUpAction() {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
        googleSignInHelper!!.onActivityResult(requestCode, resultCode, data)
        t_login_button.onActivityResult(requestCode, resultCode, data)

    }


    private val facebookCallback: FacebookCallback<LoginResult>
        get() = object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
//                GlobalData.progressDialog(activiy, getString(R.string.please_wait_login), true)
                val accessToken = loginResult.accessToken

                val profile = Profile.getCurrentProfile()
                Log.i("OnGSignInSuccess", "Log facebook access token $accessToken")

                val request =
                    GraphRequest.newMeRequest(loginResult.accessToken) { `object`, response ->
                        try {
                            Log.d("onSuccess", "onSuccess: $`object`")
                            val id = `object`.getString("id")
                            Log.v("Id = ", " $id")
                            val Name = `object`.getString("name")
                            Log.v("Name = ", " $Name")
                            val FEmail = `object`.getString("email")
                            print("OnGSignInSuccess ----- $id -- $Name -- $FEmail")
//                        if (`object`.has("email")) {
//                            Log.v("Email = ", " $FEmail")
////                            loginSocial(id, FEmail, Name, Constants.FACEBOOK)
//
//                        } else {
////                            loginSocial(id, "", Name, Constants.FACEBOOK)
//
//                        }
                        } catch (e: JSONException) {
                            e.printStackTrace()

                        }
                    }
                val parameters = Bundle()
                parameters.putString("fields", "id,name,email")
                request.parameters = parameters
                request.executeAsync()
            }

            override fun onCancel() {}

            override fun onError(exception: FacebookException) {
                exception.printStackTrace()
            }
        }

    override fun OnGSignInSuccess(googleSignInAccount: GoogleSignInAccount?) {
        if (googleSignInAccount != null) {
//            GlobalData.progressDialog(activiy, getString(R.string.please_wait_login), true)

            val socialUserName = googleSignInAccount.givenName!! + googleSignInAccount.familyName!!
            val socialUserEmail = googleSignInAccount.email
            val socialId = googleSignInAccount.id

            print("OnGSignInSuccess ----- $socialUserName -- $socialUserEmail -- $socialId")
            //            loginSocial(socialId, socialUserEmail, socialUserName, Constants.GOOGLE);
        }
    }

    override fun OnGSignInError(error: String) {
        Log.d("OnGSignInError", "OnGSignInError: $error")
    }

    private fun printHashKey() {
        try {
            Log.d("AppLog", "key:" + FacebookSdk.getApplicationSignature(this))
            Log.d("AppLog", SignatureUtils.getOwnSignatureHash(this))
        } catch (e: Exception) {
            Log.e("AppLog", "error:", e)
        }
    }

    override fun onStart() {
        super.onStart()
        googleSignInHelper!!.onStart()
    }

    override fun onStop() {
        super.onStop()
        googleSignInHelper!!.signOut()
    }

    private fun loginWithTwitter(session: TwitterSession) {
        try {
            val apiClient = TwitterCore.getInstance().apiClient
            val userCall =
                apiClient.accountService.verifyCredentials(false, false, false)
            userCall.enqueue(object :
                Callback<User>() {
                override fun success(result: Result<User>) {
                    val user = result.data
                    val email = user.email
                    Log.d(
                        "successAAAA",
                        "success: " + session.authToken.token + "  \n  " + user
                    )
                    print("OnGSignInSuccess ----- ${session.authToken.token + "  \n  " + user.email + "  " + user.name}")

                }

                override fun failure(exception: TwitterException) {
                    val e = exception.message

                }
            })
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

}

