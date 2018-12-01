/**
 * Handles the sign in button press.
 */
function toggleSignInEmail() {
    if (firebase.auth().currentUser) {
        // [START signout]
        firebase.auth().signOut();
        // [END signout]
    } else {
        var email = document.getElementById('email').value;
        var password = document.getElementById('password').value;
        if (email.length < 4) {
            alert('Please enter an email address.');
            return;
        }
        if (password.length < 4) {
            alert('Please enter a password.');
            return;
        }
        // Sign in with email and pass.
        // [START authwithemail]
        firebase.auth().signInWithEmailAndPassword(email, password).catch(function(error) {
            // Handle Errors here.
            var errorCode = error.code;
            var errorMessage = error.message;
            // [START_EXCLUDE]
            if (errorCode === 'auth/wrong-password') {
                alert('Wrong password.');
            } else {
                alert(errorMessage);
            }
            console.log(error);
            document.getElementById('email-sign-in').disabled = false;
            // [END_EXCLUDE]
        });
        // [END authwithemail]
    }
    document.getElementById('email-sign-in').disabled = true;
}

/**
 * Function called when clicking the Login/Logout button.
 */
// [START buttoncallback]
function toggleSignInGoogle() {
    if (!firebase.auth().currentUser) {
        // [START createprovider]
        var provider = new firebase.auth.GoogleAuthProvider();
        // [END createprovider]
        // [START addscopes]
        provider.addScope('https://www.googleapis.com/auth/contacts.readonly');
        // [END addscopes]
        // [START signin]
        firebase.auth().signInWithPopup(provider).then(function(result) {
            // This gives you a Google Access Token. You can use it to access the Google API.
            var token = result.credential.accessToken;
            // The signed-in user info.
            var user = result.user;
            // [START_EXCLUDE]
            document.getElementById('google-oauthtoken').textContent = token;
            // [END_EXCLUDE]
        }).catch(function(error) {
            // Handle Errors here.
            var errorCode = error.code;
            var errorMessage = error.message;
            // The email of the user's account used.
            var email = error.email;
            // The firebase.auth.AuthCredential type that was used.
            var credential = error.credential;
            // [START_EXCLUDE]
            if (errorCode === 'auth/account-exists-with-different-credential') {
                alert('You have already signed up with a different auth provider for that email.');
                // If you are using multiple auth providers on your app you should handle linking
                // the user's accounts here.
            } else {
                console.error(error);
            }
            // [END_EXCLUDE]
        });
        // [END signin]
    } else {
        // [START signout]
        firebase.auth().signOut();
        // [END signout]
    }
    // [START_EXCLUDE]
    document.getElementById('google-sign-in').disabled = true;
    // [END_EXCLUDE]
}
// [END buttoncallback]

/**
 * Function called when clicking the Login/Logout button.
 */
// [START buttoncallback]
function toggleSignInFacebook() {
    if (!firebase.auth().currentUser) {
        // [START createprovider]
        var provider = new firebase.auth.FacebookAuthProvider();
        // [END createprovider]
        // [START addscopes]
        provider.addScope('user_birthday');
        // [END addscopes]
        // [START signin]
        firebase.auth().signInWithPopup(provider).then(function(result) {
            // This gives you a Facebook Access Token. You can use it to access the Facebook API.
            var token = result.credential.accessToken;
            // The signed-in user info.
            var user = result.user;
            // [START_EXCLUDE]
            document.getElementById('facebook-oauthtoken').textContent = token;
            // [END_EXCLUDE]
        }).catch(function(error) {
            // Handle Errors here.
            var errorCode = error.code;
            var errorMessage = error.message;
            // The email of the user's account used.
            var email = error.email;
            // The firebase.auth.AuthCredential type that was used.
            var credential = error.credential;
            // [START_EXCLUDE]
            if (errorCode === 'auth/account-exists-with-different-credential') {
                alert('You have already signed up with a different auth provider for that email.');
                // If you are using multiple auth providers on your app you should handle linking
                // the user's accounts here.
            } else {
                console.error(error);
            }
            // [END_EXCLUDE]
        });
        // [END signin]
    } else {
        // [START signout]
        firebase.auth().signOut();
        // [END signout]
    }
    // [START_EXCLUDE]
    document.getElementById('facebook-sign-in').disabled = true;
    // [END_EXCLUDE]
}
// [END buttoncallback]

/**
 * initApp handles setting up UI event listeners and registering Firebase auth listeners:
 *  - firebase.auth().onAuthStateChanged: This listener is called when the user is signed in or
 *    out, and that is where we update the UI.
 */
function initApp() {
    // Listening for auth state changes.
    // [START authstatelistener]
    firebase.auth().onAuthStateChanged(function(user) {
        if (user) {
            // User is signed in.
            var displayName = user.displayName;
            var email = user.email;
            var emailVerified = user.emailVerified;
            var photoURL = user.photoURL;
            var isAnonymous = user.isAnonymous;
            var uid = user.uid;
            var providerData = user.providerData;
            // [START_EXCLUDE]
            document.getElementById('email-sign-in-status').textContent = 'Signed in';
            document.getElementById('email-sign-in').textContent = 'Sign out';
            document.getElementById('email-account-details').textContent = JSON.stringify(user, null, '  ');

            document.getElementById('google-sign-in-status').textContent = 'Signed in';
            document.getElementById('google-sign-in').textContent = 'Sign out';
            document.getElementById('google-account-details').textContent = JSON.stringify(user, null, '  ');

            document.getElementById('facebook-sign-in-status').textContent = 'Signed in';
            document.getElementById('facebook-sign-in').textContent = 'Sign out';
            document.getElementById('facebook-account-details').textContent = JSON.stringify(user, null, '  ');
            // [END_EXCLUDE]
        } else {
            // User is signed out.
            // [START_EXCLUDE]
            document.getElementById('email-sign-in-status').textContent = 'Signed out';
            document.getElementById('email-sign-in').textContent = 'Login with email';
            document.getElementById('email-account-details').textContent = 'null';

            document.getElementById('google-sign-in-status').textContent = 'Signed out';
            document.getElementById('google-sign-in').textContent = 'Login with Google';
            document.getElementById('google-account-details').textContent = 'null';
            document.getElementById('google-oauthtoken').textContent = 'null';

            document.getElementById('facebook-sign-in-status').textContent = 'Signed out';
            document.getElementById('facebook-sign-in').textContent = 'Login with Facebook';
            document.getElementById('facebook-account-details').textContent = 'null';
            document.getElementById('facebook-oauthtoken').textContent = 'null';
            // [END_EXCLUDE]
        }
        // [START_EXCLUDE]
        document.getElementById('email-sign-in').disabled = false;

        document.getElementById('google-sign-in').disabled = false;

        document.getElementById('facebook-sign-in').disabled = false;
        // [END_EXCLUDE]
    });
    // [END authstatelistener]
    document.getElementById('email-sign-in').addEventListener('click', toggleSignInEmail, false);

    document.getElementById('google-sign-in').addEventListener('click', toggleSignInGoogle, false);

    document.getElementById('facebook-sign-in').addEventListener('click', toggleSignInFacebook, false);
}

window.onload = function() {
    initApp();
};
