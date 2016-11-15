package doar.doe.app.presenter;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import doar.doe.app.R;
import doar.doe.app.views.LoginActivity;

/**
 * Created by Kanda on 09/11/2016.
 */

public class LoginPresenter implements GoogleApiClient.OnConnectionFailedListener, OnCompleteListener<AuthResult> {
    private LoginActivity activity;
    private static final String TAG = LoginPresenter.class.getSimpleName();
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInOptions gso;
    public static final int RC_SIGN_IN = 1000;

    public LoginPresenter(LoginActivity activity) {
        this.activity = activity;
        this.gso = configureGoogleSigIn();
        this.mGoogleApiClient = configureGoogleApiClient();
        this.mAuth = FirebaseAuth.getInstance();
        this.mAuthListener = authStateListener();
        this.mAuth.addAuthStateListener(this.mAuthListener);

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    public void onStop() {
        if (this.mAuthListener != null) {
            this.mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public FacebookCallback<LoginResult> facebookCallback() {
        return new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                authWithFacebook(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.e(TAG, "canceled");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e("Error", error.getCause() + error.getMessage());
            }
        };
    }

    private void authWithFacebook(AccessToken token) {
        Log.d(TAG, "authWithFacebook:" + token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity, this);
    }

    private GoogleSignInOptions configureGoogleSigIn() {
        return new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .requestId()
                .build();
    }

    private GoogleApiClient configureGoogleApiClient() {
        return new GoogleApiClient.Builder(activity)
                .enableAutoManage(activity, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private FirebaseAuth.AuthStateListener authStateListener() {
        return new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                Log.e("AUTH", firebaseAuth.toString());
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    activity.loginWithSucess();
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    public void sigInWithGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        activity.startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void authWithGoogle(final GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity, this);
    }

    @Override
    public void onComplete(@NonNull Task<AuthResult> task) {
        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
        // If sign in fails, display a message to the user. If sign in succeeds
        // the auth state listener will be notified and logic to handle the
        // signed in user can be handled in the listener.
        if (!task.isSuccessful()) {
            Log.w(TAG, "signInWithCredential", task.getException());
            Toast.makeText(activity, "Authentication failed.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public interface LoginStatus {
        void loginWithSucess();
    }
}
