package com.example.market;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

public class UserManager {
    private static final String PREFS_NAME = "user_prefs";
    private static final String USER_KEY = "current_user";
    private static SharedPreferences prefs;
    private static User currentUser;
    private static final String TAG = "UserManager";

    public static void init(Context context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            loadUserFromPrefs();
        }
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
        saveUserToPrefs();
    }

    public static void loadUserFromFirebase(FirebaseUser firebaseUser, UserLoadedCallback callback) {
        if (firebaseUser == null) {
            callback.onUserLoaded(null);
            return;
        }

        FirebaseFirestore.getInstance().collection("users")
                .document(firebaseUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc.exists()) {
                            User user = documentToUser(doc);
                            setCurrentUser(user);
                            callback.onUserLoaded(user);
                        } else {
                            // Создаем нового пользователя
                            User newUser = new User();
                            newUser.setId(firebaseUser.getUid());
                            newUser.setEmail(firebaseUser.getEmail());
                            newUser.setFirstName("");
                            newUser.setLastName("");
                            newUser.setMiddleName("");
                            saveUserToFirestore(newUser);
                            setCurrentUser(newUser);
                            callback.onUserLoaded(newUser);
                        }
                    } else {
                        Log.e(TAG, "Error loading user from Firestore", task.getException());
                        callback.onUserLoaded(null);
                    }
                });
    }

    public static void saveUserToFirestore(User user) {
        FirebaseFirestore.getInstance().collection("users")
                .document(user.getId())
                .set(user)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User saved to Firestore"))
                .addOnFailureListener(e -> Log.e(TAG, "Error saving user to Firestore", e));
    }

    public static void updateUserAvatar(String avatarUrl) {
        if (currentUser != null) {
            currentUser.setAvatarUrl(avatarUrl);
            saveUserToPrefs();
            saveUserToFirestore(currentUser);
        }
    }

    public static void updateUserProfile(String firstName, String lastName, String middleName, String phoneNumber) {
        if (currentUser != null) {
            currentUser.setFirstName(firstName);
            currentUser.setLastName(lastName);
            currentUser.setMiddleName(middleName);
            currentUser.setPhoneNumber(phoneNumber);
            saveUserToPrefs();
            saveUserToFirestore(currentUser);
        }
    }

    private static User documentToUser(DocumentSnapshot doc) {
        User user = new User();
        user.setId(doc.getId());
        user.setEmail(doc.getString("email"));
        user.setFirstName(doc.getString("firstName"));
        user.setLastName(doc.getString("lastName"));
        user.setMiddleName(doc.getString("middleName"));
        user.setAvatarUrl(doc.getString("avatarUrl"));
        user.setPhoneNumber(doc.getString("phoneNumber"));
        return user;
    }

    private static void saveUserToPrefs() {
        if (currentUser != null) {
            try {
                JSONObject userJson = new JSONObject();
                userJson.put("id", currentUser.getId());
                userJson.put("email", currentUser.getEmail());
                userJson.put("firstName", currentUser.getFirstName());
                userJson.put("lastName", currentUser.getLastName());
                userJson.put("middleName", currentUser.getMiddleName());
                userJson.put("avatarUrl", currentUser.getAvatarUrl());
                userJson.put("phoneNumber", currentUser.getPhoneNumber());

                prefs.edit().putString(USER_KEY, userJson.toString()).apply();
            } catch (JSONException e) {
                Log.e(TAG, "Error saving user to prefs", e);
            }
        }
    }

    private static void loadUserFromPrefs() {
        try {
            String userJsonString = prefs.getString(USER_KEY, null);
            if (userJsonString != null) {
                JSONObject userJson = new JSONObject(userJsonString);
                User user = new User();
                user.setId(userJson.optString("id"));
                user.setEmail(userJson.optString("email"));
                user.setFirstName(userJson.optString("firstName"));
                user.setLastName(userJson.optString("lastName"));
                user.setMiddleName(userJson.optString("middleName"));
                user.setAvatarUrl(userJson.optString("avatarUrl"));
                user.setPhoneNumber(userJson.optString("phoneNumber"));
                currentUser = user;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading user from prefs", e);
            currentUser = null;
        }
    }

    public static void logout() {
        currentUser = null;
        prefs.edit().remove(USER_KEY).apply();
    }

    public interface UserLoadedCallback {
        void onUserLoaded(User user);
    }
}