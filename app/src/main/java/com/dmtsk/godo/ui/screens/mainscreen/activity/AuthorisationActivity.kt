package com.dmtsk.godo.ui.screens.mainscreen.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.dmtsk.godo.R
import com.dmtsk.godo.ui.theme.GoDoTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class AuthorisationActivity : ComponentActivity()
{
	private val googleIdOption = GetGoogleIdOption.Builder()
		.setServerClientId("669046186697-5krt2aa6ietg9569shinr6ucf4e6oel6.apps.googleusercontent.com")
		.setFilterByAuthorizedAccounts(false)
		.build()
	
	private val request = GetCredentialRequest.Builder()
		.addCredentialOption(googleIdOption)
		.build()
	
	private lateinit var auth: FirebaseAuth
	
	
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		
		
		auth = Firebase.auth
		
		val currentUser = auth.currentUser
		if (currentUser != null)
		{
			navigateToMainActivity()
		}
		
		setContent {
			GoDoTheme {
				Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
					AuthorisationScreen(innerPadding)
				}
			}
		}
		
	}
	
	private fun navigateToMainActivity()
	{
		val intent = Intent(this, MainActivity::class.java)
		startActivity(intent)
	}
	
	@Composable
	private fun AuthorisationScreen(innerPaddingValues: PaddingValues)
	{
		Surface(
			modifier = Modifier
				.fillMaxSize()
				.padding(innerPaddingValues)
		) {
			Box(
				modifier = Modifier.fillMaxSize(),
				contentAlignment = Alignment.Center
			){
				Box(modifier = Modifier.fillMaxWidth()) {
					GoogleSignInButton(
						onClick = {
							val credentialManager = CredentialManager.create(this@AuthorisationActivity)
							
							lifecycleScope.launch {
								try
								{
									val result = credentialManager.getCredential(
										request = request,
										context = this@AuthorisationActivity
									)
									handleSignIn(result.credential)
								} catch (e: GetCredentialException)
								{
									Log.e("SignIn", "Sign in failed", e)
								}
							}
						},
						modifier = Modifier.padding(horizontal = 20.dp)
					)
				}
			}
			
			
		}
	}
	
	private fun handleSignIn(credential: Credential)
	{
		// Check if credential is of type Google ID
		if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)
		{
			// Create Google ID Token
			val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
			
			// Sign in to Firebase with using the token
			firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
		} else
		{
			Log.w("FirebaseAuth", "Credential is not of type Google ID!")
		}
	}
	
	@Composable
	fun GoogleSignInButton(
		onClick: () -> Unit,
		modifier: Modifier = Modifier,
	)
	{
		Button(
			onClick = onClick,
			colors = ButtonDefaults.buttonColors(
				containerColor = Color.White,
				contentColor = Color.Black
			),
			shape = RoundedCornerShape(4.dp),
			modifier = modifier
				.height(50.dp)
				.fillMaxWidth()
				.border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.Center,
			) {
				Image(
					painter = painterResource(id = R.drawable.ic_google_logo),
					contentDescription = "Google Logo",
					modifier = Modifier
						.size(24.dp)
				)
				Spacer(modifier = Modifier.width(12.dp))
				Text(text = "Sign in with Google")
			}
		}
	}
	
	private fun firebaseAuthWithGoogle(idToken: String)
	{
		val credential = GoogleAuthProvider.getCredential(idToken, null)
		auth.signInWithCredential(credential)
			.addOnCompleteListener(this) { task ->
				if (task.isSuccessful)
				{
					// Sign in success, update UI with the signed-in user's information
					Log.d("FirebaseAuth", "signInWithCredential:success")
					val user = auth.currentUser
					navigateToMainActivity()
//					updateUI(user)
				} else
				{
					// If sign in fails, display a message to the user
					Log.w("FirebaseAuth", "signInWithCredential:failure", task.exception)
//					updateUI(null)
				}
			}
	}
	
}